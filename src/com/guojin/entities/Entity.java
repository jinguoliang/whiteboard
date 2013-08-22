package com.guojin.entities;

import android.content.ContentValues;
import android.graphics.Canvas;
import android.view.MotionEvent;

public interface Entity {
	
	/**
	 * 绘制方法
	 * @param canvas
	 */
	public void draw(Canvas canvas);
	public void onEntityTouchEvent(MotionEvent event);
	public int getType();
	public boolean isInRange(float x, float y);
	/**
	 * 移除焦点
	 */
	public void removeFocus();
	
	/**
	 * 获取需要存储的数据
	 * @return
	 */
	public ContentValues getContentValues();
	
	public long getID();
	public void setID(long id);
}
