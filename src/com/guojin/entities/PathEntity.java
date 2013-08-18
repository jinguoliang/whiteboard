package com.guojin.entities;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.Log;
import android.view.MotionEvent;

public class PathEntity implements Entity {
	private static final String TAG = "PathEntity";
	
	BoardEntity board;
	Matrix mMatrix;
	Paint mPaint;

	double originalScale;				//存储
	double boardX, boardY;		//存储
	double currentScale;				//存储
	Path mPath;										//存储
	private float paintSize;			//存储
	//相对屏幕左上角的坐标
	private float sx;
	private float sy;
	

	public PathEntity(BoardEntity b, Path path, Paint paint,float x,float y) {
		this.board = b;
		this.originalScale = b.getTotalScale();
		this.currentScale=originalScale;
		this.sx=x;
		this.sy=y;
		double xy[]= b.screenToBoardCoodTrans(x, y);
		this.boardX = xy[0];
		this.boardY = xy[1];
		this.mMatrix = new Matrix();
		this.mPath = new Path(path);
		this.mPaint = new Paint(paint);
		this.paintSize=mPaint.getStrokeWidth();
	}

	PointF tmpScreenPoint;
	double tmpScale;
	public void draw(Canvas canvas) {
	
		
		tmpScale=board.getTotalScale();
		if (Math.abs(currentScale - tmpScale) > 0) {
			Log.e(TAG,"cs="+currentScale+":ts="+tmpScale);
			mMatrix.reset();
			mMatrix.setScale((float) (tmpScale / currentScale),
					(float) (tmpScale / currentScale),this.sx,this.sy);
			mPath.transform(mMatrix);
			currentScale = tmpScale;
			mPaint.setStrokeWidth((float) (this.paintSize*currentScale/originalScale));
		}
		
		
		tmpScreenPoint=board.boardToScreenCoodTrans(boardX, boardY);
		if (Math.abs(sx-tmpScreenPoint.x)>0||Math.abs(sy-tmpScreenPoint.y)>0) {
			mMatrix.reset();
			mMatrix.setTranslate(tmpScreenPoint.x-sx, tmpScreenPoint.y-sy);
			mPath.transform(mMatrix);
			this.sx=tmpScreenPoint.x;
			this.sy=tmpScreenPoint.y;
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

	public boolean containPoint(Point p) {
		RectF bounds=new RectF();
		mPath.computeBounds(bounds, true);
		Region region=new Region();
		region.setPath(mPath, new Region((int)bounds.left,(int)bounds.top,(int)bounds.right,(int)bounds.bottom));
		if (region.contains(p.x, p.y)) {
			return true;
		}
		return false;
	}
}
