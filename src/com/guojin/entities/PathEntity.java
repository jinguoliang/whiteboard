package com.guojin.entities;

import java.util.ArrayList;

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

public class PathEntity implements Entity {
	
	@Override
	public long getID() { return -1; }
	
	@Override
	public void setID(long id) {  }
	
	private static final String TAG = "PathEntity";

	BoardEntity board;
	Matrix mMatrix;
	Paint mPaint;
	Path mPath; 

	double originalScale; // 存储
	double boardX, boardY; // 存储
	double currentScale; // 存储
	private float paintSize; // 存储
	private ArrayList<PointF> pathPointsList;//存储
	private int color;			//存储
	
	// 相对屏幕左上角的坐标
	private float sx;
	private float sy;


	public PathEntity(BoardEntity b, Path path, Paint paint, ArrayList<PointF>pointsList) {
		this.board = b;
		this.originalScale = b.getTotalScale();
		this.currentScale = originalScale;

		this.pathPointsList=pointsList;
		this.sx = pointsList.get(0).x;
		this.sy = pointsList.get(0).y;
		double xy[] = b.screenToBoardCoodTrans(this.sx,this.sy);
		this.boardX = xy[0];
		this.boardY = xy[1];

		this.mMatrix = new Matrix();
		this.mPath = new Path(path);
		this.mPaint = new Paint(paint);
		this.color=mPaint.getColor();
		this.paintSize = mPaint.getStrokeWidth();
	}
	
	static PathEntity constructFromStr(String s){
		
		return null;
	}
	
	public String toString() {
			return null;
	};

	PointF tmpScreenPoint;
	double tmpScale;

	public void draw(Canvas canvas) {
		tmpScale = board.getTotalScale();
		if (Math.abs(currentScale - tmpScale) > 0) {
			mMatrix.reset();
			mMatrix.setScale((float) (tmpScale / currentScale),
					(float) (tmpScale / currentScale), this.sx, this.sy);
			mPath.transform(mMatrix);
			currentScale = tmpScale;
			mPaint.setStrokeWidth((float) (this.paintSize * currentScale / originalScale));
		}

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

	final float precision=5f;
	public boolean containPoint(PointF point) {
		ArrayList<PointF>list=getPointsArray();
		Log.e(TAG,"list.size="+list.size());
		for(PointF p:list){
			float dist=distanceBetween(p, point);
			if (dist-precision*2.5<0) {
				return true;
			}
		}
		return false;
	}
	private ArrayList<PointF> getPointsArray() {
		ArrayList<PointF>list=new ArrayList<PointF>();
		PathMeasure pm=new PathMeasure(mPath, false);
		float []coords=null;
		for(float  i=0+precision;i<=pm.getLength();i+=precision){
			coords=new float[2];
			pm.getPosTan(i, coords, null);
			list.add(new PointF(coords[0],coords[1]));
		}
		return list;
	}
	
	private float distanceBetween(PointF p1,PointF p2) {
		return (float) Math.sqrt(Math.pow(p1.x-p2.x, 2)+Math.pow(p1.y-p2.y, 2));
	}
	
	@Override
	public ContentValues getContentValues() {
		// TODO Auto-generated method stub
		return null;
	}
}
