package com.guojin.entities;

import java.util.List;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

public class PathFactory {
	private static final float TOUCH_TOLERANCE = 5;
	private List<Entity> pathList;
	private float sX;
	private float sY;
	private Paint mPaint;
	private Path cPath;
	private Matrix mMatrix;

	private float paitn_width = 0;
	private boolean isEraser;
	private BoardEntity board;
	private float px;
	private float py;

	public PathFactory(BoardEntity b,List<Entity> list) {
		this.pathList = list;
		this.board=b;

		paitn_width = 10f;
		mMatrix = new Matrix();
		cPath = new Path();
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.STROKE);
	}

	public void draw(Canvas canvas) {
		mPaint.setStrokeWidth((float) (paitn_width * board.getTotalScale()));
		// 正在画但没画完的笔触
		canvas.drawPath(cPath, mPaint);
	}

	private void touch_start(float x, float y) {
		cPath.reset();
		cPath.moveTo(x, y);
		px=sX = x;
		py=sY = y;
	}

	private void touch_move(float x, float y) {
		// 求当前点与上次点的坐标轴方向的差
		float dx = Math.abs(x - sX);
		float dy = Math.abs(y - sY);
		// 根据灵敏度，只有大于TOUCH_TOLERANCE的时候才在path添加一个点
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			cPath.quadTo(sX, sY, (x + sX) / 2, (y + sY) / 2);
			sX = x;
			sY = y;
		}
	}

	private void touch_up() {
		cPath.lineTo(sX, sY);
		// 当抬起时，一个笔触结束，于是，将其画到mBitmap上，并添加到pathList上
		pathList.add(new PathEntity(this.board,cPath, mPaint,px,py));
		// 重置cPath以便下一次重用
		cPath.reset();
	}

	public void onTouch(MotionEvent event) {
		// 当前触点
		float x = event.getX();
		float y = event.getY();
		// 判断action
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:// 刚按下
			touch_start(x, y);
			break;
		case MotionEvent.ACTION_MOVE:// 移动
			touch_move(x, y);
			break;
		case MotionEvent.ACTION_UP:// 离开屏幕
			touch_up();
			break;
		}
	}

}
