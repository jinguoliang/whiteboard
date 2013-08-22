package com.guojin.entities;

import android.content.ContentValues;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;

/**
 * 纸张实体
 * @author donie
 *
 */
public class PaperEntity implements Entity {
	
	@Override
	public int getType() {
		return 0;
	}
	
	@Override
	public long getID() { return -1; }
	
	@Override
	public void setID(long id) {  }
	
	/**
	 * 格子纸
	 */
	public static final int GRID_PAPER = 0x01;
	/**
	 * 横线纸
	 */
	public static final int HOR_LINE_PAPER = 0x02;
	/**
	 * 空白纸
	 */
	public static final int BLANK_PAPER = 0x03;
	
	// 纸张模式，默认为空白纸
	private int mode = BLANK_PAPER;
	
	// 行高
	private int LINEHEIGHT = 50;
	// 单格边长
	private int GRIDSIZE = 30;
	// 线条粗细
	private float STROKEWIDTH = 1f;
	// 背景颜色
	private int backgroundColor = Color.rgb(255, 250, 205);
	
	// 画笔
	private Paint linePaint;
	
	private BoardEntity boardEntity;
	
	public PaperEntity(BoardEntity be) {
		
		boardEntity = be;
		
		// 初始化线条Paint
		linePaint = new Paint();
		linePaint.setAntiAlias(true);
		linePaint.setColor(Color.rgb(150, 150, 150));
		linePaint.setStrokeWidth(STROKEWIDTH);
	}
	
	/**
	 * 构造函数
	 * @param be
	 * @param mode GRID_PAPER:格子纸 <br/>HOR_LINE_PAPER:横线纸 <br/>BLANK_PAPER:空白纸
	 */
	public PaperEntity(BoardEntity be, int mode) {
		this(be);
		this.mode = mode;
	}
	
	@Override
	public void draw(Canvas canvas) {
	
		// 绘制背景色
		canvas.drawColor(backgroundColor);
		
		switch (mode) {
		case GRID_PAPER:
			drawGridBg(canvas, linePaint);
			break;
		case HOR_LINE_PAPER:
			drawHorLineBg(canvas, linePaint);
			break;
		case BLANK_PAPER:
			break;
		}
	}
	
	/**
	 * 设置纸张模式
	 * @param mode
	 */
	public void setMode(int mode) {
		this.mode = mode;
	}
	
	/**
	 * 在横线纸模式下，设置行高
	 * @param height
	 */
	public void setLineHeight(int height) {
		LINEHEIGHT = height;
	}
	
	/**
	 * 在格子纸模式下，设置格子的大小，即每一个方格的边长
	 * @param size
	 */
	public void setGridSize(int size) {
		GRIDSIZE = size;
	}
	
	/**
	 * 设置线条颜色
	 * @param color 可以使用Color.rgb(int,int,int)设置RGB值
	 */
	public void setLineColor(int color) {
		linePaint.setColor(color);
	}
	
	/**
	 * 设置纸张背景颜色
	 * @param color 可以使用Color.rgb(int,int,int)设置RGB值
	 */
	public void setBackgroundColor(int color) {
		backgroundColor = color;
	}
	
	/**
	 * 绘制横线背景
	 * @param canvas
	 */
	private void drawHorLineBg(Canvas canvas, Paint paint) {
		
		// 计算绘制线条粗细
		float ssw = (float)(STROKEWIDTH * boardEntity.getTotalScale());
		// 设置paint
		paint.setStrokeWidth(ssw);
		
		// 绘制范围数组，元素依次为：左上右下
		double[] rangeArr = boardEntity.getDrawRangeArr();
		// 需要绘制的线条的上下范围
		double topLineY;
		if (rangeArr[1] > 0) {
			topLineY = rangeArr[1] - rangeArr[1] % LINEHEIGHT;
		} else {
			topLineY = rangeArr[1] - (LINEHEIGHT + rangeArr[1] % LINEHEIGHT);
		}
		double bottomLineY;
		if (rangeArr[2] > 0) {
			bottomLineY = rangeArr[3] + (LINEHEIGHT - rangeArr[3] % LINEHEIGHT);
		} else {
			bottomLineY = rangeArr[3] - rangeArr[3] % LINEHEIGHT;
		}
		
		// 绘制到屏幕上
		double y = topLineY;
		while (y < bottomLineY) {
			
			PointF slt = boardEntity.boardToScreenCoodTrans(rangeArr[0], y);
			PointF srb = boardEntity.boardToScreenCoodTrans(rangeArr[2], y);
			
			canvas.drawLine(slt.x, slt.y, srb.x, srb.y, paint);
			
			y += LINEHEIGHT;
		}
	}
	
	/**
	 * 绘制格子背景
	 * @param canvas
	 * @param paint
	 */
	private void drawGridBg(Canvas canvas, Paint paint) {
		// 计算绘制线条粗细
		float ssw = (float)(STROKEWIDTH * boardEntity.getTotalScale());
		// 设置paint
		paint.setStrokeWidth(ssw);
		
		// 绘制范围数组，元素依次为：左上右下
		double[] rangeArr = boardEntity.getDrawRangeArr();
		
		// 需要绘制的横线条的上限
		double topLineY;
		if (rangeArr[1] > 0) {
			topLineY = rangeArr[1] - rangeArr[1] % GRIDSIZE;
		} else {
			topLineY = rangeArr[1] - (GRIDSIZE + rangeArr[1] % GRIDSIZE);
		}
		// 需要绘制的横线条的下限
		double bottomLineY;
		if (rangeArr[3] > 0) {
			bottomLineY = rangeArr[3] + (GRIDSIZE - rangeArr[3] % GRIDSIZE);
		} else {
			bottomLineY = rangeArr[3] - rangeArr[3] % GRIDSIZE;
		}
		// 需要绘制的竖线条的左限
		double leftLineX;
		if (rangeArr[0] > 0) {
			leftLineX = rangeArr[0] - rangeArr[0] % GRIDSIZE;
		} else {
			leftLineX = rangeArr[0] - (GRIDSIZE + rangeArr[0] % GRIDSIZE);
		}
		// 需要绘制的竖线条的右限
		double rightLineX;
		if (rangeArr[2] > 0) {
			rightLineX = rangeArr[2] + (GRIDSIZE - rangeArr[2] % GRIDSIZE);
		} else {
			rightLineX = rangeArr[2] - rangeArr[2] % GRIDSIZE;
		}
		
		// 绘制横线
		double y = topLineY;
		while (y < bottomLineY) {
			
			// 计算横线两端坐标
			PointF shlt = boardEntity.boardToScreenCoodTrans(rangeArr[0], y);
			PointF shrb = boardEntity.boardToScreenCoodTrans(rangeArr[2], y);
			
			canvas.drawLine(shlt.x, shlt.y, shrb.x, shrb.y, paint);
			
			y += GRIDSIZE;
		}
		
		// 绘制竖线
		double x = leftLineX;
		while (x < rightLineX) {
			
			// 计算竖线两端坐标
			PointF svlt = boardEntity.boardToScreenCoodTrans(x, rangeArr[1]);
			PointF svrb = boardEntity.boardToScreenCoodTrans(x, rangeArr[3]);
			
			canvas.drawLine(svlt.x, svlt.y, svrb.x, svrb.y, paint);
			
			x += GRIDSIZE;
		}
	}
	
	@Override
	public void onEntityTouchEvent(MotionEvent event) {
		// 空实现
	}

	@Override
	public boolean isInRange(float x, float y) {
		// 空实现
		return false;
	}

	@Override
	public void removeFocus() {
		// 空实现
	}
	
	@Override
	public ContentValues getContentValues() {
		// 空实现
		return null;
	}
}
