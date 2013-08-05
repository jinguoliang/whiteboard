package com.guojin.whiteboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.guojin.entities.BoardEntity;

public class BoardView extends View {

	private Canvas bufCanvas;
	private Bitmap bufBitmap;
	
	// 画板实体
	private BoardEntity boardEntity = null; 
	
	public BoardView(Context context, BoardEntity board) {
		super(context);
		
		boardEntity = board;
		
		int screenWidth, screenHeight;
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		screenWidth = display.getWidth();
		screenHeight = display.getHeight();
		bufBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Config.ARGB_8888);
		bufCanvas = new Canvas(bufBitmap);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		
		boardEntity.draw(bufCanvas);
		
		canvas.drawBitmap(bufBitmap, 0, 0, null);
		super.onDraw(canvas);
	}
	
}
