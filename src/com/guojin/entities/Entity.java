package com.guojin.entities;

import android.content.ContentValues;
import android.graphics.Canvas;
import android.view.MotionEvent;

public abstract class Entity implements Comparable<Entity> {
	
	/**
	 * 绘制方法
	 * @param canvas
	 */
	public abstract void draw(Canvas canvas);
	public abstract void onEntityTouchEvent(MotionEvent event);
	public abstract int getType();
	public abstract boolean isInRange(float x, float y);
	/**
	 * 移除焦点
	 */
	public abstract void removeFocus();
	
	/**
	 * 获取需要存储的数据
	 * @return
	 */
	public abstract ContentValues getContentValues();
	
	public abstract long getID();
	public abstract void setID(long id);
	
	public int showIndex = -1;
	
	@Override
	public int compareTo(Entity another) {
		return Integer.valueOf(this.showIndex).compareTo(Integer.valueOf(another.showIndex));
	}
}
