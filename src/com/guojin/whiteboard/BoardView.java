package com.guojin.whiteboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import com.guojin.entities.BoardEntity;

public class BoardView extends View {

	private float oldDist = Float.NaN;	// 两触点之间的旧距离值
	private PointF oldMidPoint;			// 旧两触点中心点
	
	
	private Canvas bufCanvas;
	private Bitmap bufBitmap;

	// 画板实体
	private BoardEntity boardEntity = null;

	private InputMethodManager imm;

	private Context context;
	
	// private View layout;

	public BoardView(Context context) {
		super(context);
		this.context = context;
	}

	public BoardView(Context context, BoardEntity board) {
		this(context);

		boardEntity = board;

		// 设置双缓冲
		int screenWidth, screenHeight;
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		screenWidth = display.getWidth();
		screenHeight = display.getHeight();
		bufBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
				Config.ARGB_8888);
		bufCanvas = new Canvas(bufBitmap);

		// LayoutInflater inflater =
		// LayoutInflater.from(boardEntity.getContext());
		// layout = (LinearLayout)inflater.inflate(R.layout.entity_note, null);
		imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	/**
	 * 切换软键盘显示
	 * 
	 * @param open
	 *            true-显示 false-关闭
	 */
	public void toggleInput(boolean open) {
		if (open) {
			imm.showSoftInput(this, InputMethodManager.SHOW_FORCED);
		} else {
			imm.hideSoftInputFromWindow(getWindowToken(), 0);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {

		boardEntity.draw(bufCanvas);
		// layout.draw(canvas);

		canvas.drawBitmap(bufBitmap, 0, 0, null);
		super.onDraw(canvas);
	}

	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
		return new MyInputConnection(this, false);
	}

	class MyInputConnection extends BaseInputConnection {

		public MyInputConnection(View targetView, boolean fullEditor) {
			super(targetView, fullEditor);
		}

		@Override
		public boolean commitText(CharSequence text, int newCursorPosition) {
			boardEntity.commitInputText(text.toString(), false);
			invalidate();
			return true;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_ENTER:
			boardEntity.commitInputText(null, true);
			break;
		case KeyEvent.KEYCODE_DEL:
			boardEntity.delPrevInputText();
			break;
		}
		invalidate();
		return false;
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

		Log.d("DevLog", pointerCount + "");
		
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
				boardEntity.calculate(currMidPoint.x, currMidPoint.y, scale,
						dx, dy, this.getWidth(), this.getHeight());
				// 显示缩放比例
				((WhiteBoardActivity)context).setScaleText((int) (boardEntity.getTotalScale() * 100)
						+ "%");
				postInvalidate();

				// Log.d("DevLog", String.format("Scale: %f\nDist: %f , %f",
				// scale, dx, dy));
				break;
			case MotionEvent.ACTION_POINTER_UP:
				// Log.d("DevLog", "Action up");
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
		getLocationOnScreen(loc);
		
		return new PointF((x1 + x2) / 2, (y1 + y2) / 2 - loc[1]);
	}
}
