package com.guojin.entities;

import com.guojin.whiteboard.BoardView;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class BoardEntity {

	// 总缩放比例
	private double totalScale = 1;
	
	// 总偏移量（相对于最初始位置）
	private double totalOffsetX = 0;
	private double totalOffsetY = 0;
	
	// 需要绘制的坐标范围
	private double drawRangeLeft = 0;
	private double drawRangeTop = 0;
	private double drawRangeRight = 2000;
	private double drawRangeBottom = 2000;
	
	// 绑定的View
	private View mBindedView = null;
	
	private SimpleEntity textEntity;
	
	// 纸张实体
	private PaperEntity paperEntity;
	// 便签实体
	private NoteEntity noteEntity;
	
	/**
	 * 构造函数
	 */
	public BoardEntity() {
		
		textEntity = new SimpleEntity(this);
		textEntity.x = 100L;
		textEntity.y = 100L;
		textEntity.initRadius = 20f;
		
		// 初始化纸张实体
		paperEntity = new PaperEntity(this, PaperEntity.GRID_PAPER);
		
		// 初始化便签实体
		noteEntity = new NoteEntity(this);
	}
	
	/**
	 * 绘制方法
	 * @param canvas
	 */
	public void draw(Canvas canvas) {
		// 绘制纸张背景
		paperEntity.draw(canvas);
		// 绘制便签
		noteEntity.draw(canvas);
		
//		textEntity.draw(canvas);
	}
	
	/**
	 * 屏幕触摸方法
	 * @param event
	 */
	public void onEntityTouchEvent(MotionEvent event) {
		// 将触摸点位置设置为相对View
		int[] loc = new int[2];
		mBindedView.getLocationOnScreen(loc);
		float x = event.getX();
		float y = event.getY();
		event.setLocation(x + loc[0], y - loc[1]);
		noteEntity.onEntityTouchEvent(event);
	}
	
	/**
	 * 通知View重绘
	 */
	public void invalidateView() {
		mBindedView.invalidate();
	}
	
	/**
	 * 获取总缩放比例
	 * @return
	 */
	public double getTotalScale() {
		return totalScale;
	}
	
	/**
	 * 获取绘制范围数组
	 * @return 数组元素依次为：左上右下
	 */
	public double[] getDrawRangeArr() {
		return new double[] {
				drawRangeLeft, drawRangeTop, drawRangeRight, drawRangeBottom
		};
	}
	
	/**
	 * 获取总偏移
	 * @return 数组元素依次为：偏移X 偏移Y
	 */
	public double[] getTotalOffsetArr() {
		return new double[] {totalOffsetX, totalOffsetY};
	}
	
	/**
	 * 绑定View
	 * @param v
	 */
	public void bindView(View v) {
		mBindedView = v;
	}
	
	/**
	 * 画板坐标到屏幕坐标转换
	 * @param bx
	 * @param by
	 * @return 转换后的屏幕坐标
	 */
	public PointF boardToScreenCoodTrans(double bx, double by) {
		float sx = (float)((bx + totalOffsetX) * totalScale);
		float sy = (float)((by + totalOffsetY) * totalScale);
		return new PointF(sx, sy);
	}
	
	/**
	 * 屏幕坐标转换为画板坐标
	 * @param sx
	 * @param sy
	 * @return 数组元素依次为：画板坐标X，画板坐标Y
	 */
	public double[] screenToBoardCoodTrans(float sx, float sy) {
		double bx = sx / totalScale - totalOffsetX;
		double by = sy / totalScale - totalOffsetY;
		return new double[] {bx, by};
	}
	
	/**
	 * 画板上尺寸到屏幕上尺寸的转换
	 * @param size
	 * @return
	 */
	public float boardToScreenSizeTrans(double bs) {
		return (float)(bs * totalScale);
	}
	
	/**
	 * 屏幕上尺寸到画板上尺寸的转换
	 * @param size
	 * @return
	 */
	public double screenToBoardSizeTrans(float ss) {
		return ss / totalScale;
	}
	
	/**
	 * 计算画板参数
	 * @param smx 屏幕缩放中心点X
	 * @param smy 屏幕缩放中心点Y
	 * @param scale 缩放比例
	 * @param sdx 屏幕坐标偏移X
	 * @param sdy 屏幕坐标偏移Y
	 * @param sMaxX 屏幕可绘制区域最大X
	 * @param sMaxY 屏幕可绘制区域最大Y
	 */
	public void calculate(float smx, float smy, float scale, float sdx, float sdy, float sMaxX, float sMaxY) {
		
//		Log.d("DevLog", String.format("bdx:bdy=(%f,%f)\nscale=%f", bdx.floatValue(), bdy.floatValue(), scale));
		
		float bmx = (float)(smx / totalScale - totalOffsetX);
		float bmy = (float)(smy / totalScale - totalOffsetY);
		
		// 计算总缩放比例
		totalScale *= scale;
		
		totalOffsetX = smx / totalScale - bmx;
		totalOffsetY = smy / totalScale - bmy;
		
		// 屏幕偏移转化为画板偏移
		double dx = sdx / totalScale;
		double dy = sdy / totalScale;
		
		// 计算画板总偏移
		totalOffsetX += dx;
		totalOffsetY += dy;
		
		// 计算绘制范围
		drawRangeLeft = -totalOffsetX;
		drawRangeTop = -totalOffsetY;
		float maxX = mBindedView.getWidth();
		float maxY = mBindedView.getHeight();
		drawRangeRight = maxX / totalScale - totalOffsetX;
		drawRangeBottom = maxY / totalScale - totalOffsetY;
		
//		Log.d("DevLog", String.format("(%f,%f,%f,%f)", drawRangeLeft, drawRangeTop, drawRangeRight, drawRangeBottom));
	}
}
