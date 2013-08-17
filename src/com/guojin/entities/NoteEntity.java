package com.guojin.entities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

import com.guojin.text.DynamicTextLayout;
import com.guojin.text.DynamicTextLayout.TextOutSizeListener;

public class NoteEntity implements Entity {
	
	// 实体类型
	private int type = BoardEntity.TYPE_NOTE_ENTITY;
	@Override
	public int getType() {
		return type;
	}
	
	// 是否获取到焦点
	public boolean isFocus = false;
	
	// 便签的最小长宽
	private static final double MIN_WIDTH = 150;
	private static final double MIN_HEIGHT = 230;
	
	// 坐标
	private double boardX = 0;
	private double boardY = 0;
	
	// 便签的宽高
	private double noteWidth = 300;
	private double noteHeight = 300;
	
	// 内边距
	private double padding = 10;
	
	// 阴影参数
	private float shadowRadius = 3;
	private float shadowX = 3;
	private float shadowY = 3;
	private int shadowColor = Color.argb(150, 100, 100, 100);
	
	// 旧触摸点
	private float oldX;
	private float oldY;
	
	// 可移动标志位
	private boolean isMoveable = false;
	// 可控制大小标志位
	private boolean isResizeable = false;
	
	// 移动指示器高度
	private float moveIndicateHeight = 30;
	// 大小控制指示器尺寸 
	private float resizeIndicateSize = 20;
	// 删除指示器尺寸
	private float delIndicateSize = 40;
	// 删除指示器的笔画宽度
	private float delIndicateStrokeWidth = 2;
	// 文本尺寸
	private float boardTextSize = 20;
	
	// 背景画笔
	private Paint bgPaint;
	// 大小控制指示器画笔
	private Paint resizeIndicatePaint;
	// 移动指示器画笔
	private Paint moveIndicatePaint;
	// 删除指示器画笔
	private Paint delIndicatePaint;
	
	// 背景颜色
	private int bgColor = Color.rgb(0, 197, 205);
	
	private BoardEntity boardEntity;
	private Context context;
	private DynamicTextLayout textLayout;
	
	// 文本字间距
	private double textSpan = 1;
	
	public NoteEntity(BoardEntity be, Context c) {
		
		boardEntity = be;
		context = c;
		
		// 初始化背景画笔
		bgPaint = new Paint();
		bgPaint.setAntiAlias(true);
		bgPaint.setColor(bgColor);
		bgPaint.setShadowLayer(shadowRadius, shadowX, shadowY, shadowColor);
		
		// 初始化大小控制指示器画笔
		resizeIndicatePaint = new Paint();
		resizeIndicatePaint.setAntiAlias(true);
		resizeIndicatePaint.setColor(Color.argb(200, 255, 193, 37));
		
		// 初始化位置指示器画笔
		moveIndicatePaint = new Paint();
		moveIndicatePaint.setAntiAlias(true);
		moveIndicatePaint.setStrokeWidth(2);
		moveIndicatePaint.setColor(Color.argb(200, 100, 100, 100));
		
		// 初始化删除指示器画笔
		delIndicatePaint = new Paint();
		delIndicatePaint.setAntiAlias(true);
		delIndicatePaint.setColor(Color.rgb(238, 44, 44));
		
		// 初始化文本显示
		PointF sp = boardEntity.boardToScreenCoodTrans(boardX, boardY);
		float lw = boardEntity.boardToScreenSizeTrans(noteWidth);
		float lh = boardEntity.boardToScreenSizeTrans(noteHeight);
		textLayout = new DynamicTextLayout(sp.x, sp.y, lw, lh);
		// 设置当文字超出边界范围时的监听器
		textLayout.setOnTextOutSizeListener(new TextOutSizeListener() {
			
			@Override
			public void onSizeChange(float w, float h) {
				noteWidth = boardEntity.screenToBoardSizeTrans(w);
				noteHeight = boardEntity.screenToBoardSizeTrans(h);
				boardEntity.invalidateView();
			}
		});
		
	}
	
	// 设置在画板中的位置
	public void setBoardPosition(double x, double y) {
		boardX = x;
		boardY = y;
	}
	
	/**
	 * 设置文本尺寸
	 * @param textSize
	 */
	public void setTextSize(float textSize) {
		boardTextSize = textSize;
	}
	
	/**
	 * 设置背景颜色
	 * @param color
	 */
	public void setStyleColor(int color) {
		bgColor = color;
		bgPaint.setColor(bgColor);
	}
	
	@Override
	public void draw(Canvas canvas) {
		// 屏幕上位置坐标
		PointF sp = boardEntity.boardToScreenCoodTrans(boardX, boardY);
		// 屏幕上宽高
		float sw = boardEntity.boardToScreenSizeTrans(noteWidth);
		float sh = boardEntity.boardToScreenSizeTrans(noteHeight);
		// 阴影尺寸
		float ssx = boardEntity.boardToScreenSizeTrans(shadowX);
		float ssy = boardEntity.boardToScreenSizeTrans(shadowY);
		// 内边距
		float spadding = boardEntity.boardToScreenSizeTrans(padding);
		
		// 绘制背景
		bgPaint.setShadowLayer(shadowRadius, ssx, ssy, shadowColor);
		canvas.drawRect(sp.x, sp.y, sp.x + sw, sp.y + sh, bgPaint);
		
		// 绘制控制点
		if (isFocus) {
			drawMoveIndicate(canvas);
			drawResizeIndicate(canvas);
			drawDelIndicate(canvas);
		}
		
		// 绘制文本
		textLayout.resize(sp.x + spadding, sp.y + spadding
				, sw - spadding * 2, sh - spadding * 2);
		float stextSize = boardEntity.boardToScreenSizeTrans(boardTextSize);
		float stextSpan = boardEntity.boardToScreenSizeTrans(textSpan);
		textLayout.setTextSize(stextSize);
		textLayout.setSpan(stextSpan);
		textLayout.draw(canvas);
	}
	
	/**
	 * 提交输入的文本
	 * @param text 需要提交的文本
	 * @param isNewLine 是否为新起一行，如果为true，参数text可以为null
	 */
	public void commitInputText(String text, boolean isNewLine) {
		if (isNewLine) {
			textLayout.nextNewLine();
		} else {
			textLayout.appendText(text);
		}
	}
	
	/**
	 * 删除之前提交的一个文本字符
	 */
	public void delPrevInputText() {
		textLayout.delPrev();
	}
	
	/**
	 * 绘制移动指示器
	 * @param canvas
	 */
	private void drawMoveIndicate(Canvas canvas) {
		
//		float bakStrokeWidth = moveIndicatePaint.getStrokeWidth();
//		float strokeWidth = boardEntity.boardToScreenSizeTrans(bakStrokeWidth);
//		moveIndicatePaint.setStrokeWidth(strokeWidth);
		
		// 左上角屏幕绘制位置
//		PointF sp = boardEntity.boardToScreenCoodTrans(boardX, boardY - moveIndicateHeight);
		// 屏幕绘制高
//		float h = boardEntity.boardToScreenSizeTrans(moveIndicateHeight);
		// 屏幕绘制宽
//		float w = boardEntity.boardToScreenSizeTrans(noteWidth);
		
		PointF sp = boardEntity.boardToScreenCoodTrans(boardX, boardY);
		sp.y -= moveIndicateHeight;
		float h = moveIndicateHeight;
		float w = boardEntity.boardToScreenSizeTrans(noteWidth);
		
		canvas.drawRect(sp.x, sp.y, sp.x + w, sp.y + h, moveIndicatePaint);
		
		float ph = h / 5;
		int bakColor = moveIndicatePaint.getColor();
		moveIndicatePaint.setColor(Color.WHITE);
		for (int i = 1; i <= 4; i++) {
			canvas.drawLine(sp.x + 5, sp.y + ph * i, sp.x + w - 5, sp.y + ph * i, moveIndicatePaint);
		}
		moveIndicatePaint.setColor(bakColor);
//		moveIndicatePaint.setStrokeWidth(bakStrokeWidth);
	}
	
	/**
	 * 绘制控制指示点
	 * @param canvas
	 */
	private void drawResizeIndicate(Canvas canvas) {
		
//		float r = boardEntity.boardToScreenSizeTrans(resizeIndicateSize);
		float r = resizeIndicateSize;
		PointF rbp = boardEntity.boardToScreenCoodTrans(boardX + noteWidth, boardY + noteHeight);
		
		// 绘制斜线用于标明指示区域
		canvas.drawCircle(rbp.x, rbp.y, r, resizeIndicatePaint);
		
	}
	
	/**
	 * 绘制删除指示器
	 * @param canvas
	 */
	private void drawDelIndicate(Canvas canvas) {
//		float ss = boardEntity.boardToScreenSizeTrans(delIndicateSize);
		float ss = delIndicateSize;
		PointF sp = boardEntity.boardToScreenCoodTrans(boardX, boardY);
		float sw = boardEntity.boardToScreenSizeTrans(noteWidth);
//		float ssw = boardEntity.boardToScreenSizeTrans(delIndicateStrokeWidth);
		float ssw = delIndicateStrokeWidth;
		
		// 保存画笔原参数
		int oldColor = delIndicatePaint.getColor();
		
		RectF delRect = new RectF(sp.x + sw, sp.y
				, sp.x + sw + ss, sp.y + ss);
		canvas.drawRect(delRect, delIndicatePaint);
		
		// 圆形标识的中心点
		float scdx = sp.x + sw + ss / 2;
		float scdy = sp.y + ss / 2;
		float scdd = ss / 3;
		delIndicatePaint.setColor(Color.WHITE);
		delIndicatePaint.setStrokeWidth(ssw);
		canvas.drawLine(scdx - scdd, scdy - scdd, scdx + scdd, scdy + scdd, delIndicatePaint);
		canvas.drawLine(scdx - scdd, scdy + scdd, scdx + scdd, scdy - scdd, delIndicatePaint);
		
		// 恢复画笔
		delIndicatePaint.setColor(oldColor);
	}
	
	/**
	 * 移除焦点
	 */
	@Override
	public void removeFocus() {
		isFocus = false;
		// 关闭输入法显示
		boardEntity.toggleInput(false);
		textLayout.showCursor(false);
		
		// 请求重绘
		boardEntity.invalidateView();
	}
	
	@Override
	public void onEntityTouchEvent(MotionEvent event) {
		
		if (!isFocus) {
			isFocus = true;
			boardEntity.invalidateView();
			return;
		}
		
		switch (event.getAction()) {
		
		case MotionEvent.ACTION_DOWN:
			
			oldX = event.getX();
			oldY = event.getY();
			
			// 判断点击位置
			if (isInNormalRange(oldX, oldY)) {
				textLayout.onTouchEvent(event);
				boardEntity.toggleInput(true);
				textLayout.showCursor(true);
			}
			
			// 判断是否为移动操作
			if (isInMoveIndicRange(oldX, oldY)) {
				isMoveable = true;
			} else {
				isMoveable = false;
			}
			
			// 判断是否为尺寸操作
			if (isInResizeIndicRange(oldX, oldY)) {
				isResizeable = true;
			} else {
				isResizeable = false;
			}
			
			// 判断是否为删除操作
			if (isInDeleteIndicRange(oldX, oldY)) {
				boardEntity.delEntity(this);
				return;
			}
			
			boardEntity.invalidateView();
			break;
			
		case MotionEvent.ACTION_UP:
			isMoveable = false;
			isResizeable = false;
			break;
			
		case MotionEvent.ACTION_MOVE:
			
			if (isMoveable) {
				// 移动控制
				
				float newX = event.getX();
				float newY = event.getY();
				// 移动距离
				float distX = newX - oldX;
				float distY = newY - oldY;
				
				if (Math.abs(distX) > 0.5f || Math.abs(distY) > 0.5f) {
					boardX += boardEntity.screenToBoardSizeTrans(distX);
					boardY += boardEntity.screenToBoardSizeTrans(distY);
					
//					Log.d("DevLog", String.format("%f,%f", boardX, boardY));
					
					oldX = newX;
					oldY = newY;
					boardEntity.invalidateView();
				}
			} else if (isResizeable) {
				// 尺寸控制
				
				float newX = event.getX();
				float newY = event.getY();
				
				float distX = newX - oldX;
				float distY = newY - oldY;
				
				PointF sp = boardEntity.boardToScreenCoodTrans(boardX, boardY);
				float sminw = boardEntity.boardToScreenSizeTrans(MIN_WIDTH);
				float sminh = boardEntity.boardToScreenSizeTrans(MIN_HEIGHT);
				if (newX < sp.x + sminw) {
					distX = 0;
				}
				if (newY < sp.y + sminh) {
					distY = 0;
				}
				
				
				if (Math.abs(distX) > 0.5f || Math.abs(distY) > 0.5f) {
					// 临时值
					double tmpX = boardX;
					double tmpY = boardY;
					double tmpWidth = noteWidth;
					double tmpHeight = noteHeight;
					
					// 右下角
					tmpWidth += boardEntity.screenToBoardSizeTrans(distX);
					tmpHeight += boardEntity.screenToBoardSizeTrans(distY);
					
					if (tmpWidth >= MIN_WIDTH) {
						boardX = tmpX;
						noteWidth = tmpWidth;
					}
					if (tmpHeight >= MIN_HEIGHT) {
						boardY = tmpY;
						noteHeight = tmpHeight;
					}
					
					oldX = event.getX();
					oldY = event.getY();
					boardEntity.invalidateView();
					
				}
			}
			
			break;
			
		default:
			break;
		}
		
	}
	
	/**
	 * 判断屏幕坐标是否在本实体范围之内
	 */
	@Override
	public boolean isInRange(float x, float y) {
		
		if (isFocus) {
			return isInNormalRange(x, y) || isInMoveIndicRange(x, y) 
					|| isInResizeIndicRange(x, y) || isInDeleteIndicRange(x, y);
		} else {
			return isInNormalRange(x, y);
		}
		
	}
	
	/**
	 * 判断屏幕坐标是否在文字输入范围内
	 * @param sx
	 * @param sy
	 * @return
	 */
	private boolean isInNormalRange(float sx, float sy) {
		double[] bp = boardEntity.screenToBoardCoodTrans(sx, sy);
//		Log.d("DevLog", String.format("%f,%f", bp[0], bp[1]));
		if (bp[0] > boardX && bp[0] < (boardX + noteWidth) 
				&& bp[1] > boardY && bp[1] < (boardY + noteHeight)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 判断屏幕坐标是否在移动指示区域内
	 * @param sx
	 * @param sy
	 * @return
	 */
	private boolean isInMoveIndicRange(float sx, float sy) {
		double[] bp = boardEntity.screenToBoardCoodTrans(sx, sy);
		if (bp[0] > boardX && bp[0] < (boardX + noteWidth) 
				&& bp[1] > boardY - boardEntity.screenToBoardSizeTrans(moveIndicateHeight)
				&& bp[1] < boardY) {
			return true;
		}
		return false;
	}
	
	/**
	 * 判断屏幕点击点是否在尺寸控制区域，并设置发生控制的位置
	 * @param sx
	 * @param sy
	 * @return
	 */
	private boolean isInResizeIndicRange(float sx, float sy) {
		double[] bp = boardEntity.screenToBoardCoodTrans(sx, sy);
		double a = (bp[0] - (boardX + noteWidth)) * (bp[0] - (boardX + noteWidth));
		double b = (bp[1] - (boardY + noteHeight)) * (bp[1] - (boardY + noteHeight));
		if (Math.sqrt(a + b) < boardEntity.screenToBoardSizeTrans(resizeIndicateSize)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 判断屏幕点击点是否在删除控制区域，并设置发生控制的位置
	 * @param sx
	 * @param sy
	 * @return
	 */
	private boolean isInDeleteIndicRange(float sx, float sy) {
		double[] bp = boardEntity.screenToBoardCoodTrans(sx, sy);
		double bs = boardEntity.screenToBoardSizeTrans(delIndicateSize);
		
		if (bp[0] > boardX + noteWidth && bp[0] < boardX + noteWidth + bs 
				&& bp[1] > boardY && bp[1] < boardY + bs) {
			return true;
		}
		return false;
	}
}
