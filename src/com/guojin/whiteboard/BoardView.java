package com.guojin.whiteboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.guojin.entities.BoardEntity;

public class BoardView extends View {

	private Canvas bufCanvas;
	private Bitmap bufBitmap;
	
	// 画板实体
	private BoardEntity boardEntity = null; 
	
	private View layout;
	
	
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
		
		LayoutInflater inflater = LayoutInflater.from(boardEntity.getContext());
		layout = (LinearLayout)inflater.inflate(R.layout.entity_note, null);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		boardEntity.draw(bufCanvas);
		layout.draw(canvas);
		
		canvas.drawBitmap(bufBitmap, 0, 0, null);
		super.onDraw(canvas);
	}

}
