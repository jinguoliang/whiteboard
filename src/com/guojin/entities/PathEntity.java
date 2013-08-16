package com.guojin.entities;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

public class PathEntity {
	Path mPath;
	Matrix mMatrix;
	Paint mPaint;
	
	public PathEntity(Path path,Paint paint) {
		this.mPath=new Path(path);
		this.mPaint=new Paint(paint);
	}
	
	public void draw(Canvas canvas) {
		mPath.transform(mMatrix);
		canvas.drawPath(mPath, mPaint);
	}
}
