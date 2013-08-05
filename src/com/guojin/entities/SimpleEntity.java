package com.guojin.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

public class SimpleEntity {

	public double x;
	public double y;
	public float initRadius;
	
	public String content = "Hello World";
	public float initSize = 30;
	
	private BoardEntity boardEntity;
	
	private Paint paint;
	
	public SimpleEntity(BoardEntity be) {
		boardEntity = be;
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.BLACK);
	}
	
	public void draw(Canvas canvas) {
//		float radius = initRadius * (float)boardEntity.getTotalScale();
		float size = initSize * (float)boardEntity.getTotalScale();
		paint.setTextSize(size);
		PointF sp = boardEntity.boardToScreenCoodTrans(x, y);
		canvas.drawText(content, sp.x, sp.y, paint);
	}
}
