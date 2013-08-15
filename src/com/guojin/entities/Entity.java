package com.guojin.entities;

import android.graphics.Canvas;
import android.view.MotionEvent;

public interface Entity {
	/**
	 * 绘制方法
	 * @param canvas
	 */
	public void draw(Canvas canvas);
	
}
