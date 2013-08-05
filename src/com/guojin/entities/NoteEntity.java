package com.guojin.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;

public class NoteEntity implements Entity, HandleTouchEvent {

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
	
	// 可移动标志位
	private boolean isMoveable = false;
	
	// 画笔
	private Paint bgPaint;
	
	private BoardEntity boardEntity;
	
	public NoteEntity(BoardEntity be) {
		boardEntity = be;
		
		bgPaint = new Paint();
		bgPaint.setAntiAlias(true);
		bgPaint.setColor(Color.rgb(0, 197, 205));
		bgPaint.setShadowLayer(shadowRadius, shadowX, shadowY, shadowColor);
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
		
		bgPaint.setShadowLayer(shadowRadius, ssx, ssy, shadowColor);
		canvas.drawRect(sp.x, sp.y, sp.x + sw, sp.y + sh, bgPaint);
	}
	
	@Override
	public void onEntityTouchEvent(MotionEvent event) {
		
		switch (event.getAction()) {
		
		case MotionEvent.ACTION_DOWN:
			oldX = event.getX();
			oldY = event.getY();
			if (isInNoteRange(oldX, oldY)) {
				isMoveable = true;
			} else {
				isMoveable = false;
			}
			break;
			
		case MotionEvent.ACTION_UP:
			isMoveable = false;
			break;
			
		case MotionEvent.ACTION_MOVE:
			if (isMoveable) {
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
			}
			break;
			
		default:
			break;
		}
		
	}
	
	/**
	 * 判断屏幕坐标是否在指定范围内
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
}
