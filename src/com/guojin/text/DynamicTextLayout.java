package com.guojin.text;

import java.util.LinkedList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;


public class DynamicTextLayout {
	class MChar {
		float x;
		float y;
		float w;
		String character;
		public MChar(String str) {
			character = str.substring(0, 1); 
		}
	}
	
	class MCursor {
		int ln = 0;		// 所在行号
		int index = 0;	// 所在索引
		float h;
		float w;
	}
	
	// 文本保存链表
	private LinkedList<LinkedList<MChar>> textList = new LinkedList<LinkedList<MChar>>();
	
	// 坐标、宽高
	private float x = 0;
	private float y = 0;
	private float width = 200;
	private float height = 200;
	
	// 绘制文字时的坐标
	private float drawX = x;
	private float drawY;
	
	// 文字相关变量
	private Paint textPaint = new Paint();
	private int textColor = Color.BLACK;
	private float textSize = 25;
	private float lineHeight;		// 行高
	private float span = 2;			// 字符间距
	
	// Cursor相关变量
	private MCursor cursor = new MCursor();
	private Paint cursorPaint = new Paint();
	private int cursorColor = Color.BLACK;
	private float cursorWidth = 2;
	private boolean showCursor = false;
	
	// 标识量
	private boolean isTouchDown = false;	// 点击下
	private boolean isSelection = false;	// 选择
	
	// 选择模式下记录结束字符位置
	private int selEndLn = 0;
	private int selEndIndex = 0;
	
	// 选择相关变量
	private Paint selBgPaint = new Paint();
	private int selBgColor = Color.argb(100, 255, 193, 37);
	
	private TextOutSizeListener listener = null;
	
	/**
	 * 构造函数
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public DynamicTextLayout(float x, float y, float w, float h) {
		this.x = x;
		this.y = y;
		this.width = w;
		this.height = h;
		
		init();
	}
	
	private void init() {
		// 初始化文字相关变量
		textPaint.setAntiAlias(true);
		textPaint.setColor(textColor);
		textPaint.setTextSize(textSize);
		FontMetrics fm = textPaint.getFontMetrics();
		lineHeight = fm.descent - fm.top;
		
		// 初始化Cursor相关变量
		cursor.h = lineHeight;
		cursor.w = cursorWidth;
		cursorPaint.setAntiAlias(true);
		cursorPaint.setColor(cursorColor);
		textList.add(new LinkedList<MChar>());
		
		// 初始化绘制坐标
		drawX = x;
		drawY = y - fm.top;
		
		// 初始化选择相关变量
		selBgPaint.setAntiAlias(true);
		selBgPaint.setColor(selBgColor);
	}
	
	/**
	 * 重新设置位置和尺寸
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public void resize(float x, float y, float w, float h) {
		this.x = x;
		this.y = y;
		this.width = w;
		this.height = h;
		
		init();
	}
	
	/**
	 * 设置文本画笔尺寸
	 * @param size
	 */
	public void setTextSize(float size) {
		textSize = size;
		init();
	}
	
	/**
	 * 设置字符间距
	 * @param span
	 */
	public void setSpan(float span) {
		this.span = span;
		init();
	}
	
	/**
	 * 是否显示Cursor
	 * @param show
	 */
	public void showCursor(boolean show) {
		showCursor = show;
	}
	
	/**
	 * 设置文字超出尺寸监听器
	 * @param listener
	 */
	public void setOnTextOutSizeListener(TextOutSizeListener listener) {
		this.listener = listener;
	}
	
	/**
	 * 添加文本
	 * @param str
	 */
	public void appendText(String str) {
		for (int i = 0; i < str.length(); i++) {
			MChar c = new MChar(str.substring(i, i + 1));
			c.w = getMCharWidth(c.character);
			LinkedList<MChar> line = textList.get(cursor.ln);
			if (line == null) {
				textList.add(cursor.ln, new LinkedList<MChar>());
				line = textList.get(cursor.ln);
			}
			line.add(cursor.index, c);
			cursor.index ++;
		}
	}
	
	/**
	 * 删除前一个字符
	 */
	public void delPrev() {
		if (cursor.ln == 0 && cursor.index == 0) {
			return;
		} else if (cursor.ln > 0 && cursor.index == 0) {
			// 删除换行符
			LinkedList<MChar> ol = textList.get(cursor.ln);
			cursor.ln--;
			cursor.index = textList.get(cursor.ln).size();
			LinkedList<MChar> nl = textList.get(cursor.ln);
			nl.addAll(cursor.index, ol);
			textList.remove(cursor.ln + 1);
		} else if (cursor.index > 0) {
			LinkedList<MChar> line = textList.get(cursor.ln);
			cursor.index--;
			line.remove(cursor.index);
			
		}
	}
	
	/**
	 * 新起下一行
	 */
	public void nextNewLine() {
		LinkedList<MChar> ol = textList.get(cursor.ln);
		cursor.ln ++;
		LinkedList<MChar> nl = new LinkedList<MChar>();
		for (int i = cursor.index; i < ol.size(); i++) {
			nl.add(ol.remove(i));
		}
		cursor.index = 0;
		textList.add(cursor.ln, nl);
	}
	
	/**
	 * 绘制方法
	 * @param canvas
	 */
	public void draw(Canvas canvas) {
		// 起始坐标
		float cx = drawX;
		float cy = drawY;
		
		canvas.save();
		// 设置剪切区域
		canvas.clipRect(new RectF(x, y, x + width, y + height));
		
		// 绘制文字
		for (int ln = 0; ln < textList.size(); ln++) {
			LinkedList<MChar> line = textList.get(ln);
			for (int i = 0; i < line.size(); i++) {
				MChar c = line.get(i);
				if (cx + c.w + span > drawX + width) {
					cx = drawX;
					cy += lineHeight;
				}
				c.x = cx;
				c.y = cy + textPaint.getFontMetrics().top;
				c.w = getMCharWidth(c.character);
				// 绘制文字
				canvas.drawText(c.character, cx, cy, textPaint);
				// 绘制选择相关图形
				if (isSelection) {
					// 无代码，暂时不开发此操作
				}
				cx += c.w + span;
			}
			cx = drawX;
			cy += lineHeight;
		}
		
		if (showCursor) {
			PointF csp = getCursorCood();
			canvas.drawRect(csp.x, csp.y, csp.x + cursor.w, csp.y + cursor.h, cursorPaint);
		}
		
		canvas.restore();
	}
	
	/**
	 * 接收点击事件
	 * @param event
	 */
	public void onTouchEvent(MotionEvent event) {
		float tx = event.getX();
		float ty = event.getY();
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (isSelection) {
				isSelection = false;
			}
			isTouchDown = true;
			int[] cPos = getMCharInPoint(tx, ty);
			cursor.ln = cPos[0];
			cursor.index = cPos[1];
			
			break;
		case MotionEvent.ACTION_UP:
			isTouchDown = false;
			break;
		case MotionEvent.ACTION_MOVE:
			if (!isTouchDown) {
				int[] newCPos = getMCharInPoint(tx, ty);
				int newln = newCPos[0];
				int newIndex = newCPos[1];
				if (newln != cursor.ln || newIndex != cursor.index) {
					isSelection = true;
				} else {
					isSelection = false;
				}
			}
			break;
		}
	}
	
	/**
	 * 获取某一点对应的字符位置
	 * @param tx
	 * @param ty
	 * @return float数组，[0]行号，[1]索引
	 */
	private int[] getMCharInPoint(float tx, float ty) {
		int lineNum = 0;
		int index = 0;
		
		float totalY = y;
		LinkedList<MChar> line = null;
		
		// 定位行位置
		for (int ln = 0; ln < textList.size(); ln++) {
			line = textList.get(ln);
			if (line.isEmpty()) {
				totalY += lineHeight;
			} else {
				totalY = line.getLast().y + lineHeight;
			}
			if (totalY > ty) {
				break;
			}
		}
		lineNum = textList.indexOf(line);
		
		// 定位列位置
		if (line.isEmpty()) {
			index = 0;
		} else {
			boolean cursorChange = false;
			for (int i = 0; i < line.size(); i++) {
				MChar c = line.get(i);
				if (tx > c.x && tx < c.x + c.w && ty > c.y && ty < c.y + lineHeight) {
					index = i;
					cursorChange = true;
					break;
				}
			}
			if (!cursorChange) {
				index = line.size();
			}
		}
		
		return new int[] {lineNum, index};
	}
	
	/**
	 * 获取Cursor坐标
	 * @return
	 */
	private PointF getCursorCood() {
		float cx = x;
		float cy = y;
		if (cursor.ln == 0 && cursor.index == 0) {
			cx = x;
			cy = y;
		} else if (cursor.ln > 0 && cursor.index == 0) {
			float nearEmptyLineTotalHeight = 0;
			for (int i = cursor.ln - 1; i >= 0; i--) {
				LinkedList<MChar> line = textList.get(i);
				if (line.isEmpty()) {
					nearEmptyLineTotalHeight += lineHeight;
				} else {
					cy = line.getLast().y + lineHeight;
					break;
				}
			}
			cx = x;
			cy += nearEmptyLineTotalHeight;
		} else if (cursor.index > 0) {
			MChar c = textList.get(cursor.ln).get(cursor.index - 1);
			cx = c.x + c.w;
			cy = c.y;
		}
		
		return new PointF(cx, cy);
	}
	
	/**
	 * 获取一个字符的宽度
	 * @param c
	 * @return
	 */
	private float getMCharWidth(String c) {
		float[] widths = new float[1];
		textPaint.getTextWidths(c, widths);
		return widths[0];
	}
	
	/**
	 * 文字超出尺寸监听器
	 * @author donie
	 *
	 */
	public interface TextOutSizeListener {
		public void onSizeChange(float w, float h);
	}
}
