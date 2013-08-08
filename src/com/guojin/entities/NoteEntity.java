package com.guojin.entities;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.widget.EditText;

public class NoteEntity implements Entity, HandleTouchEvent {

	// 便签的最小长宽
	private static final double MIN_WIDTH = 150;
	private static final double MIN_HEIGHT = 150;
	
	// 坐标
	private double boardX = 0;
	private double boardY = 0;
	
	// 便签的宽高
	private double noteWidth = 300;
	private double noteHeight = 300;
	
	// 内边距
	private double padding = 20;
	
	// 阴影参数
	private float shadowRadius = 3;
	private float shadowX = 3;
	private float shadowY = 3;
	private int shadowColor = Color.argb(150, 100, 100, 100);
	
	// 旧触摸点
	private float oldX;
	private float oldY;
	
	// 是否显示边界指示
	private boolean isShowSizeIndicate = false;
	// 可移动标志位
	private boolean isMoveable = false;
	// 可控制大小标志位
	private boolean isResizeable = false;
	
	// 移动指示器高度
	private double moveIndicateHeight = 30;
	
	// 大小控制指示器尺寸 
	private double resizeIndicateSize = 30;
	
	// 文本尺寸
	private float boardTextSize = 20;
	
	// 背景画笔
	private Paint bgPaint;
	// 大小控制指示器画笔
	private Paint resizeIndicatePaint;
	// 移动指示器画笔
	private Paint moveIndicatePaint;
	// 文本画笔
	private Paint textPaint;
	
	// 文本
	private String contentText = "";
	
	private BoardEntity boardEntity;
	private Context context;
	
	public NoteEntity(BoardEntity be, Context c) {
		boardEntity = be;
		context = c;
		
		// 初始化背景画笔
		bgPaint = new Paint();
		bgPaint.setAntiAlias(true);
		bgPaint.setColor(Color.rgb(0, 197, 205));
		bgPaint.setShadowLayer(shadowRadius, shadowX, shadowY, shadowColor);
		
		// 初始化大小控制指示器画笔
		resizeIndicatePaint = new Paint();
		resizeIndicatePaint.setAntiAlias(true);
		resizeIndicatePaint.setStrokeWidth(1);
		resizeIndicatePaint.setColor(Color.WHITE);
		
		// 初始化位置指示器画笔
		moveIndicatePaint = new Paint();
		moveIndicatePaint.setAntiAlias(true);
		moveIndicatePaint.setStrokeWidth(2);
		moveIndicatePaint.setColor(Color.argb(200, 100, 100, 100));
		
		// 初始化文本画笔
		textPaint = new Paint();
		textPaint.setAntiAlias(true);
		textPaint.setColor(Color.BLACK);
		
		
	}
	
	// 设置在画板中的位置
	public void setBoardPosition(double x, double y) {
		boardX = x;
		boardY = y;
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
		
		// 绘制背景
		bgPaint.setShadowLayer(shadowRadius, ssx, ssy, shadowColor);
		canvas.drawRect(sp.x, sp.y, sp.x + sw, sp.y + sh, bgPaint);
		
		// 绘制控制点
		if (isShowSizeIndicate) {
			drawMoveIndicate(canvas);
			drawResizeIndicate(canvas);
		}
		
	}
	
	/**
	 * 绘制移动指示器
	 * @param canvas
	 */
	private void drawMoveIndicate(Canvas canvas) {
		
		float bakStrokeWidth = moveIndicatePaint.getStrokeWidth();
		float strokeWidth = boardEntity.boardToScreenSizeTrans(bakStrokeWidth);
		moveIndicatePaint.setStrokeWidth(strokeWidth);
		
		// 左上角屏幕绘制位置
		PointF sp = boardEntity.boardToScreenCoodTrans(boardX, boardY - moveIndicateHeight);
		// 屏幕绘制高
		float h = boardEntity.boardToScreenSizeTrans(moveIndicateHeight);
		// 屏幕绘制宽
		float w = boardEntity.boardToScreenSizeTrans(noteWidth);
		
		canvas.drawRect(sp.x, sp.y, sp.x + w, sp.y + h, moveIndicatePaint);
		
		float ph = h / 5;
		int bakColor = moveIndicatePaint.getColor();
		moveIndicatePaint.setColor(Color.WHITE);
		for (int i = 1; i <= 4; i++) {
			canvas.drawLine(sp.x + 5, sp.y + ph * i, sp.x + w - 5, sp.y + ph * i, moveIndicatePaint);
		}
		moveIndicatePaint.setColor(bakColor);
		moveIndicatePaint.setStrokeWidth(bakStrokeWidth);
	}
	
	/**
	 * 绘制控制指示点
	 * @param canvas
	 */
	private void drawResizeIndicate(Canvas canvas) {
		
		float l = boardEntity.boardToScreenSizeTrans(resizeIndicateSize);
		PointF rbp = boardEntity.boardToScreenCoodTrans(boardX + noteWidth, boardY + noteHeight);
		
		// 绘制斜线用于标明指示区域
		float d = l / 5;
		for (int i = 1; i <= 4; i++) {
			canvas.drawLine(rbp.x - l + d * i + 1, rbp.y - 1, rbp.x - 1, rbp.y - l + d * i + 1, resizeIndicatePaint);
		}
		
	}
	
	@Override
	public void onEntityTouchEvent(MotionEvent event) {
		
		switch (event.getAction()) {
		
		case MotionEvent.ACTION_DOWN:
			
			oldX = event.getX();
			oldY = event.getY();
			
			// 判断点击位置
			if (isInNoteRange(oldX, oldY)) {
				if (!isShowSizeIndicate) {
					// 选中、获取焦点
					isShowSizeIndicate = true;
					isMoveable = false;
					isResizeable = false;
				} else {
//					boardEntity.openInput();
				}
			}
			
			// 判断是否为移动操作
			if (isShowSizeIndicate && isInMoveIndicRange(oldX, oldY)) {
				isMoveable = true;
			} else {
				isMoveable = false;
			}
			
			// 判断是否为尺寸操作
			if (isShowSizeIndicate && isInResizeIndicRange(oldX, oldY)) {
				isResizeable = true;
			} else {
				isResizeable = false;
			}
			
			if (!isInNoteRange(oldX, oldY) && !isMoveable && !isResizeable) {
				isShowSizeIndicate = false;
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
	 * 判断屏幕坐标是否在文字输入范围内
	 * @param sx
	 * @param sy
	 * @return
	 */
	private boolean isInNoteRange(float sx, float sy) {
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
				&& bp[1] > boardY - moveIndicateHeight && bp[1] < boardY) {
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
		double exSize = 10;
		double[] bp = boardEntity.screenToBoardCoodTrans(sx, sy);
		if (bp[0] > boardX + noteWidth - resizeIndicateSize - exSize 
				&& bp[0] < boardX + noteWidth + exSize
				&& bp[1] > boardY + noteHeight - resizeIndicateSize - exSize 
				&& bp[1] < boardY + noteHeight + exSize) {
			return true;
		}
		return false;
	}
	
}
