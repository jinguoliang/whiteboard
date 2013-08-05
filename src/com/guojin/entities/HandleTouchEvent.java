package com.guojin.entities;

import android.view.MotionEvent;

/**
 * 屏幕触摸处理接口
 * @author donie
 *
 */
public interface HandleTouchEvent {

	public void onEntityTouchEvent(MotionEvent event);
}
