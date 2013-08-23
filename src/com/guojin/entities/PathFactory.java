package com.guojin.entities;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.MotionEvent;

public class PathFactory {

	private static final String TAG = "PathFactory";
	/**
	 * 
	 */
	private static final float TOUCH_TOLERANCE = 10f;
	/**
	 * path 有各种模式我这叫类型,橡皮擦属于其中一种
	 */
	public static final int PATH_MODE_PAINT = 0x01f;
	public static final int PATH_MODE_ERASER = 0x02f;

	/**
	 * 记录当前的path模式
	 */
	private int currentPathMode;

	/**
	 * 记录刚按下的位置
	 */
	private float sX;
	private float sY;

	private Paint mPaint;
	private Path cPath;
	private int paintSize;
	private int paintColor;

	private BoardEntity board;
	private List<Entity> entityList;

	/**
	 * 记录画path时的每个点,以便存储恢复
	 */
	private ArrayList<float[]> pathPoints;

	public PathFactory(BoardEntity b, List<Entity> list) {
		this.entityList = list;
		this.board = b;

		paintSize = 10;
		cPath = new Path();
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		paintColor = Color.BLACK;

		setPathMode(PATH_MODE_PAINT);
	}

	/**
	 * 更改path的样式
	 * 
	 * @param mode
	 */
	public void setPathMode(int mode) {
		this.currentPathMode = mode;
		switch (mode) {
		case PATH_MODE_PAINT:
			break;
		case PATH_MODE_ERASER:
			break;
		}
	}

	public void draw(Canvas canvas) {
		mPaint.setStrokeWidth((float) (paintSize * board.getTotalScale()));
		mPaint.setColor(this.paintColor);
		// 正在画但没画完的笔触
		canvas.drawPath(cPath, mPaint);
	}

	private void touch_start(float x, float y) {
		pathPoints = new ArrayList<float[]>();
		cPath.reset();
		cPath.moveTo(x, y);
		if (currentPathMode != PathFactory.PATH_MODE_ERASER) {
			pathPoints.add(new float[] { x, y });
		}
		sX = x;
		sY = y;
	}

	private void touch_move(float x, float y) {
		// 求当前点与上次点的坐标轴方向的差
		float dx = Math.abs(x - sX);
		float dy = Math.abs(y - sY);
		// 根据灵敏度，只有大于TOUCH_TOLERANCE的时候才在path添加一个点
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			cPath.quadTo(sX, sY, (x + sX) / 2, (y + sY) / 2);
			if (currentPathMode != PathFactory.PATH_MODE_ERASER) {
				pathPoints.add(new float[] { (x + sX) / 2, (y + sY) / 2 });
			}
			sX = x;
			sY = y;
		}
	}

	Entity tmp;
	private void touch_up() {
		cPath.lineTo(sX, sY);
		// 当抬起时，如果不是橡皮,一个笔触结束，于是，将其画到mBitmap上，并添加到pathList上
		if (currentPathMode != PathFactory.PATH_MODE_ERASER) {
			pathPoints.add(new float[] { sX, sY });
			 tmp = new PathEntity(this.board, board.getMaxShowIndex(),cPath, mPaint, pathPoints);
			entityList.add(tmp);
			board.getDataManager().saveData(tmp);
		} else {
			ArrayList<PointF> list = PathEntity.getPointsArray(cPath);
			List<Entity> tmplist = new ArrayList<Entity>();
			for (PointF p : list) {
				for (Entity entity : entityList) {
					if (entity.getType() == BoardEntity.TYPE_PATH_ENTITY) {
						if (((PathEntity) entity).containPoint(p)) {
							tmplist.add(entity);
						}
					}
				}
			}
			for (Entity path : tmplist) {
				board.getDataManager().deleteEntity(path);
				entityList.remove(path);
			}
		}
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

	public int getMode() {
		return this.currentPathMode;
	}

}
