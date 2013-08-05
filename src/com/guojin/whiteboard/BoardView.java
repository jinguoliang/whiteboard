package com.guojin.whiteboard;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import com.guojin.entities.BoardEntity;

public class BoardView extends View {

	// 画板实体
	private BoardEntity boardEntity = null; 
	
	public BoardView(Context context, BoardEntity board) {
		super(context);
		
		boardEntity = board;
		
	}

	@Override
	protected void onDraw(Canvas canvas) {
		
		boardEntity.draw(canvas);
		
		super.onDraw(canvas);
	}
	
}
