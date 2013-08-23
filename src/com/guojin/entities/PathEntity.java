package com.guojin.entities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.util.ArrayList;

import android.R.array;
import android.content.ContentValues;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.Log;
import android.view.MotionEvent;

import com.guojin.store.DatabaseContract.PathDBEntity;

/**
 * 记录了一个path的相关信息,并且依据board的缩放对它进行缩放
 * 
 * @author jinux
 * 
 */
public class PathEntity extends Entity {

	@Override
	public long getID() {
		return -1;
	}

	@Override
	public void setID(long id) {
	}

	private static final String TAG = "PathEntity";

	BoardEntity board;
	Matrix mMatrix;
	Paint mPaint;
	Path mPath;

	/**
	 * 录path被添加到board上时board的缩放比例--------------存储
	 */
	double originalScale;
	/**
	 * 当前board的比例,通过currentScale/orginalScale可求得path需要缩放的比例-- 存储
	 */
	double currentScale;
	/**
	 * path在board上的位置---------------------------------------- 存储
	 */
	double boardX, boardY;
	/**
	 * 笔触的大小------------------------------------------------------- 存储
	 */
	private int paintSize;
	/**
	 * 原始点的数组-----------------------------存储
	 */
	private ArrayList<float[]> pathPointsList;
	/**
	 * 笔触颜色-------------------------------------------------------------存储
	 */
	private int color;

	/**
	 * 相对屏幕左上角的坐标
	 */
	private float sx;
	private float sy;

	public PathEntity(BoardEntity b, Path path, Paint paint,
			ArrayList<float[]> pointsList) {
		this.board = b;
		this.originalScale = b.getTotalScale();
		this.currentScale = originalScale;

		this.pathPointsList = pointsList;
		// 以点数组的第一个代表path的位置
		this.sx = pointsList.get(0)[0];
		this.sy = pointsList.get(0)[1];
		// 转化为board上的位置
		double xy[] = b.screenToBoardCoodTrans(this.sx, this.sy);
		this.boardX = xy[0];
		this.boardY = xy[1];

		this.mMatrix = new Matrix();
		this.mPath = new Path(path);
		this.mPaint = new Paint(paint);
		this.color = mPaint.getColor();
		this.paintSize = (int) (mPaint.getStrokeWidth() / b.getTotalScale());
	}

	public PathEntity(BoardEntity b, long id, int showIndex,
			float originalScale, float currentScale, int color, int paintSize,
			double bx, double by, byte[] pointsBuf) {
		setID(id);
		this.showIndex = showIndex;

		this.board = b;
		this.originalScale = originalScale;
		this.currentScale = currentScale;
		// 以点数组的第一个代表path的位置
		this.boardX = bx;
		this.boardY = by;
		PointF p = b.boardToScreenCoodTrans(bx, by);
		this.sx = p.x;
		this.sy = p.y;

		this.mPath = new Path();
		try {
			pathPointsList = byteToPoints(pointsBuf);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.mPath.moveTo(pathPointsList.get(0)[0], pathPointsList.get(0)[1]);
		int i = 1;
		for (int size=pathPointsList.size(); i <  size -1; i++) {
			this.mPath.quadTo(pathPointsList.get(i - 1)[0],
					pathPointsList.get(i - 1)[1], pathPointsList.get(i)[0],
					pathPointsList.get(i)[1]);
		}
		this.mPath.lineTo(pathPointsList.get(i)[0], pathPointsList.get(i)[1]);

		this.mMatrix = new Matrix();
		this.mPaint = new Paint();
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		this.color = color;
		this.paintSize = paintSize;
		mPaint.setColor(color);
		mPaint.setStrokeWidth(paintSize);

	}

	PointF tmpScreenPoint;
	double tmpScale;

	public void draw(Canvas canvas) {
		// 获取现在的缩放比例
		tmpScale = board.getTotalScale();
		// 如果缩放比例变动则对矩阵进行缩放处理
		if (Math.abs(currentScale - tmpScale) > 0) {
			mMatrix.reset();
			mMatrix.setScale((float) (tmpScale / currentScale),
					(float) (tmpScale / currentScale), this.sx, this.sy);
			mPath.transform(mMatrix);
			currentScale = tmpScale;
			mPaint.setStrokeWidth((float) (this.paintSize * currentScale / originalScale));
		}

		// 如果位置发生变化,也对path进行平移变换
		tmpScreenPoint = board.boardToScreenCoodTrans(boardX, boardY);
		if (Math.abs(sx - tmpScreenPoint.x) > 0
				|| Math.abs(sy - tmpScreenPoint.y) > 0) {
			mMatrix.reset();
			mMatrix.setTranslate(tmpScreenPoint.x - sx, tmpScreenPoint.y - sy);
			mPath.transform(mMatrix);
			this.sx = tmpScreenPoint.x;
			this.sy = tmpScreenPoint.y;
		}

		canvas.drawPath(mPath, mPaint);
	}

	@Override
	public void onEntityTouchEvent(MotionEvent event) {
		// 空函数

	}

	@Override
	public int getType() {
		return BoardEntity.TYPE_PATH_ENTITY;
	}

	@Override
	public boolean isInRange(float x, float y) {
		return false;
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean containPoint(int x, int y) {
		RectF bounds = new RectF();
		mPath.computeBounds(bounds, true);
		Region region = new Region();
		region.setPath(mPath, new Region((int) bounds.left, (int) bounds.top,
				(int) bounds.right, (int) bounds.bottom));
		if (region.contains(x, y)) {
			return true;
		}
		return false;
	}

	@Override
	public void removeFocus() {
		// 空函数
	}

	static final float precision = 15f;

	/**
	 * 将point和path上的每一点进行距离计算,如果和某一点距离小于特定值,就表明该点在path上
	 * 
	 * @param point
	 * @return
	 */
	public boolean containPoint(PointF point) {
		ArrayList<PointF> list = getPointsArray(mPath);
		Log.e(TAG, "list.size=" + list.size());
		for (PointF p : list) {
			float dist = distanceBetween(p, point);
			if (dist - precision < 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取path的一系列点
	 * 
	 * @return
	 */
	static ArrayList<PointF> getPointsArray(Path mPath) {
		ArrayList<PointF> list = new ArrayList<PointF>();
		PathMeasure pm = new PathMeasure(mPath, false);
		float[] coords = null;
		for (float i = 0 + precision; i <= pm.getLength(); i += precision) {
			coords = new float[2];
			pm.getPosTan(i, coords, null);
			list.add(new PointF(coords[0], coords[1]));
		}
		return list;
	}

	/**
	 * 计算两个点的距离
	 * 
	 * @param p1
	 * @param p2
	 * @return
	 */
	private float distanceBetween(PointF p1, PointF p2) {
		return (float) Math.sqrt(Math.pow(p1.x - p2.x, 2)
				+ Math.pow(p1.y - p2.y, 2));
	}

	@Override
	public ContentValues getContentValues() {
		ContentValues values = new ContentValues();
		values.put(PathDBEntity.BOARD_ID, board.getBoardID());
		values.put(PathDBEntity.SHOW_INDEX, showIndex + "");
		values.put(PathDBEntity.POS_X, boardX);
		values.put(PathDBEntity.POS_Y, boardY);

		values.put(PathDBEntity.CUR_SCALE, this.currentScale);
		values.put(PathDBEntity.ORI_SCALE, this.originalScale);
		values.put(PathDBEntity.STROKE_COLOR, this.color);
		values.put(PathDBEntity.STROKE_WIDTH, this.paintSize);
		values.put(PathDBEntity.POINTS, getByteFromPoints());
		return values;
	}

	private byte[] getByteFromPoints() {
		ByteArrayOutputStream obj = new ByteArrayOutputStream();
		try {
			ObjectOutputStream out = new ObjectOutputStream(obj);
			out.writeObject(this.pathPointsList);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return obj.toByteArray();
	}

	private ArrayList<float[]> byteToPoints(byte[] buf)
			throws OptionalDataException, ClassNotFoundException, IOException {
		ByteArrayInputStream obj = new ByteArrayInputStream(buf);
		ObjectInputStream in = new ObjectInputStream(obj);
		return (ArrayList<float[]>) in.readObject();
	}

}
