package com.guojin.entities;

import android.graphics.PointF;

/**
 * 屏幕坐标映射操作
 * @author donie
 *
 */
public class CoodTransOper {

	// 中心位置坐标
	private float boardMidX;
	private float boardMidY;
	private float screenMidX;
	private float screenMidY;
	
	// 总缩放比例
	private float totalScale;
	
	public CoodTransOper(float bmx, float bmy, float smx, float smy, float ts) {
		boardMidX = bmx;
		boardMidY = bmy;
		screenMidX = smx;
		screenMidY = smy;
		totalScale = ts;
	}
	
	/**
	 * 画板坐标到屏幕坐标的转换
	 * @param bx 画板坐标X
	 * @param by 画板坐标Y
	 * @return 转换后的屏幕坐标
	 */
	public PointF boardToScreenCoodTrans(float bx, float by) {
		float sx = (bx - boardMidX) * totalScale + screenMidX;
		float sy = (by - boardMidY) * totalScale + screenMidY;
		return new PointF(sx, sy);
	}
	
	/**
	 * 屏幕坐标到画板坐标的转换
	 * @param sx 屏幕坐标X
	 * @param sy 屏幕坐标Y
	 * @return 转换后的画板坐标
	 */
	public PointF screenToBoardCoodTrans(float sx, float sy) {
		float bx = (sx - screenMidX) / totalScale + boardMidX;
		float by = (sy - screenMidY) / totalScale + boardMidY;
		return new PointF(bx, by);
	}
}
