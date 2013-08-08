package com.guojin.whiteboard;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.AbsoluteLayout;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.guojin.entities.BoardEntity;

public class WhiteBoardActivity extends Activity {
	
	private float oldDist = Float.NaN;	// 两触点之间的旧距离值
	private PointF oldMidPoint;			// 旧两触点中心点
	
	private BoardEntity boardEntity = null;	// Board实体
	
	private BoardView boardView;	// Board View
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		boardEntity = new BoardEntity(this);
		boardView = new BoardView(this, boardEntity);
		// 设置view可以获取焦点
		boardView.setFocusable(true);
		boardView.setFocusableInTouchMode(true);
		boardEntity.bindView(boardView);
		
		setContentView(boardView);
	}
	
	/**
	 * 获取两触点之间的距离
	 * @param event
	 * @return
	 */
	private float getPointsDist(MotionEvent event) {
		float dx = event.getX(0) - event.getX(1);
		float dy = event.getY(0) - event.getY(1);
		return (float)Math.sqrt(dx * dx + dy * dy);
	}
	
	/**
	 * 获取两触点之间的中心点
	 * @param event
	 * @return
	 */
	private PointF getMidPoint(MotionEvent event) {
		float x1 = event.getX(0);
		float y1 = event.getY(0);
		float x2 = event.getX(1);
		float y2 = event.getY(1);
		
		int[] loc = new int[2];
		boardView.getLocationOnScreen(loc);
		
		return new PointF((x1 + x2) / 2, (y1 + y2) / 2 - loc[1]);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		// 触点数目
		int pointerCount = event.getPointerCount();
		// 缩放比例
		float scale = 1;
		// 偏移距离
		float dx = 0f;
		float dy = 0f;
		
//		Log.d("DevLog", String.format("touch position: %f,%f", event.getX(), event.getY()));
		
		if (pointerCount == 2) {
			switch (event.getActionMasked()) {
			case MotionEvent.ACTION_MOVE:
				
				// 计算缩放比例scale
				if (Float.isNaN(oldDist)) {
					oldDist = getPointsDist(event);
				} else {
					float newDist = getPointsDist(event);
					
					if (Math.abs(newDist - oldDist) > 1f) {
						scale = newDist / oldDist;
						oldDist = newDist;
					} 
				}
				
				// 计算偏移距离
				if (oldMidPoint == null) {
					oldMidPoint = getMidPoint(event);
				} else {
					PointF newMidPoint = getMidPoint(event);
					
					float tdx = newMidPoint.x - oldMidPoint.x;
					float tdy = newMidPoint.y - oldMidPoint.y;
					
					if (Math.abs(tdx) > 1f || Math.abs(tdy) > 1f) {
						dx = tdx;
						dy = tdy;
						oldMidPoint = newMidPoint;
					}
				}
				
				PointF currMidPoint = getMidPoint(event);
				boardEntity.calculate(currMidPoint.x, currMidPoint.y, scale
						, dx, dy, boardView.getWidth(), boardView.getHeight());
				boardView.postInvalidate();
				
				
//				Log.d("DevLog", String.format("Scale: %f\nDist: %f , %f", scale, dx, dy));
				break;
			case MotionEvent.ACTION_POINTER_UP:
//				Log.d("DevLog", "Action up");
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				oldDist = getPointsDist(event);
				oldMidPoint = getMidPoint(event);
				break;
			}
		} else {
			boardEntity.onEntityTouchEvent(event);
		}
		
		return false;
	}
}
