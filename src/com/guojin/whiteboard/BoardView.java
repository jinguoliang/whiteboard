package com.guojin.whiteboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import com.guojin.entities.BoardEntity;

public class BoardView extends View {

	private Canvas bufCanvas;
	private Bitmap bufBitmap;
	
	// 画板实体
	private BoardEntity boardEntity = null; 
	
	private InputMethodManager imm;
//	private View layout;
	
	
	public BoardView(Context context) {
		super(context);
	}
	
	public BoardView(Context context, BoardEntity board) {
		this(context);
		
		boardEntity = board;
		
		// 设置双缓冲
		int screenWidth, screenHeight;
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		screenWidth = display.getWidth();
		screenHeight = display.getHeight();
		bufBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Config.ARGB_8888);
		bufCanvas = new Canvas(bufBitmap);
		
//		LayoutInflater inflater = LayoutInflater.from(boardEntity.getContext());
//		layout = (LinearLayout)inflater.inflate(R.layout.entity_note, null);
		imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
	}
	
	/**
	 * 切换软键盘显示
	 * @param open true-显示    false-关闭
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
//		layout.draw(canvas);
		
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
}
