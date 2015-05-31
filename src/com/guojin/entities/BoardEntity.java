package com.guojin.entities;

import java.util.Collections;
import java.util.LinkedList;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.guojin.store.DataManager;
import com.guojin.store.DatabaseContract.NoteDBEntity;
import com.guojin.store.DatabaseContract.PathDBEntity;
import com.guojin.store.DatabaseContract.PicDBEntity;
import com.guojin.whiteboard.BoardView;
import com.guojin.whiteboard.R;
import com.guojin.whiteboard.WhiteBoardActivity;
import com.guojin.whiteboard.common.PhoneUtils;

public class BoardEntity {

	private int boardID = 1;

	// 模式常量
	public static final int MODE_HANDDRAW = 0x11;
	public static final int MODE_PIC = 0x12;
	public static final int MODE_NOTE = 0x13;

	public int mode = MODE_HANDDRAW; // 当前模式

	// 类型常量
	public static final int TYPE_PIC_ENTITY = 0x01;
	public static final int TYPE_NOTE_ENTITY = 0x02;
	public static final int TYPE_PATH_ENTITY = 0x03;

	// 缩放范围
	public static final double MAX_TOTAL_SCALE = 3;
	public static final double MIN_TOTAL_SCALE = 0.4;

	// 总缩放比例
	private double totalScale = 1;

	// 总偏移量（相对于最初始位置）
	private double totalOffsetX = 0;
	private double totalOffsetY = 0;

	// 需要绘制的坐标范围
	private double drawRangeLeft = 0;
	private double drawRangeTop = 0;
	private double drawRangeRight = 2000;
	private double drawRangeBottom = 2000;

	// 绑定的View
	private BoardView mBindedView = null;

	// Context
	private Context context;

	// 纸张实体
	private PaperEntity paperEntity;

	// 将手绘的功能分离出来给PathFactory
	PathFactory pathFactory;

	// 实体链表
	private LinkedList<Entity> entityList = new LinkedList<Entity>();

	// 标识是否已经有实体获取焦点
	private boolean isCapture = false;

	// 当前已经获取焦点的Entity
	private Entity focusedEntity = null;
	// 获取到焦点的Entity的原位置
	private int originEntityIndex = -1;

	// 遮盖层
	private Paint coverPaint = new Paint();
	private float picInsertX;
	private float picInsertY;

	// 数据操作
	private DataManager dataManager;
	// 显示顺序索引的最大值
	private int maxShowIndex = 0;
	//
	private float mWidth;
	private float mHeight;

	/**
	 * 构造函数
	 */
	public BoardEntity(Context c) {

		context = c;
		dataManager = new DataManager(c);

		Point p = new Point();
		PhoneUtils.getScreenSize(context, p);
		mWidth = p.x;
		mHeight = p.y;
		
		// 初始化画笔
		coverPaint.setAntiAlias(true);
		coverPaint.setColor(Color.argb(150, 200, 200, 200));

		// 初始化纸张实体
		paperEntity = new PaperEntity(this, PaperEntity.GRID_PAPER);
		// pathFactory用于分担手绘的任务
		pathFactory = new PathFactory(this, this.entityList);
		loadEntity();
	}

	/**
	 * 加载实体
	 */
	private void loadEntity() {
		Cursor[] cursors = dataManager.getAllCursor(boardID);
		Cursor noteCursor = cursors[0];
		Cursor picCursor = cursors[1];
		Cursor pathCursor = cursors[2];

		// 加载note
		LinkedList<Entity> eList = new LinkedList<Entity>();
		if (noteCursor != null && noteCursor.moveToFirst()) {
			for (noteCursor.moveToFirst(); !noteCursor.isAfterLast(); noteCursor
					.moveToNext()) {
				long id = noteCursor.getLong(noteCursor
						.getColumnIndexOrThrow(NoteDBEntity._ID));
				int showIndex = noteCursor.getInt(noteCursor
						.getColumnIndexOrThrow(NoteDBEntity.SHOW_INDEX));
				double posX = noteCursor.getDouble(noteCursor
						.getColumnIndexOrThrow(NoteDBEntity.POS_X));
				double posY = noteCursor.getDouble(noteCursor
						.getColumnIndexOrThrow(NoteDBEntity.POS_Y));
				double width = noteCursor.getDouble(noteCursor
						.getColumnIndexOrThrow(NoteDBEntity.WIDTH));
				double height = noteCursor.getDouble(noteCursor
						.getColumnIndexOrThrow(NoteDBEntity.HEIGHT));
				String text = noteCursor.getString(noteCursor
						.getColumnIndexOrThrow(NoteDBEntity.TEXT));
				int bgColor = noteCursor.getInt(noteCursor
						.getColumnIndexOrThrow(NoteDBEntity.BG_COLOR));
				int textColor = noteCursor.getInt(noteCursor
						.getColumnIndexOrThrow(NoteDBEntity.TEXT_COLOR));
				float textSize = noteCursor.getFloat(noteCursor
						.getColumnIndexOrThrow(NoteDBEntity.TEXT_SIZE));
				eList.add(new NoteEntity(this, context, id, boardID, showIndex,
						posX, posY, width, height, text, bgColor, textColor,
						textSize));
			}
		}
		// 加载图片
		if (picCursor != null && picCursor.moveToFirst()) {
			for (picCursor.moveToFirst(); !picCursor.isAfterLast(); picCursor
					.moveToNext()) {
				long id = picCursor.getLong(picCursor
						.getColumnIndexOrThrow(PicDBEntity._ID));
				int showIndex = picCursor.getInt(picCursor
						.getColumnIndexOrThrow(PicDBEntity.SHOW_INDEX));
				double posX = picCursor.getDouble(picCursor
						.getColumnIndexOrThrow(PicDBEntity.POS_X));
				double posY = picCursor.getDouble(picCursor
						.getColumnIndexOrThrow(PicDBEntity.POS_Y));
				double width = picCursor.getDouble(picCursor
						.getColumnIndexOrThrow(PicDBEntity.WIDTH));
				double height = picCursor.getDouble(picCursor
						.getColumnIndexOrThrow(PicDBEntity.HEIGHT));
				float scale = picCursor.getFloat(picCursor
						.getColumnIndexOrThrow(PicDBEntity.SCALE));
				float rotate = picCursor.getFloat(picCursor
						.getColumnIndexOrThrow(PicDBEntity.ROTATE));
				String src = picCursor.getString(picCursor
						.getColumnIndexOrThrow(PicDBEntity.SRC));

				eList.add(new PictureEntity(this, id, showIndex, src, posX,
						posY, rotate, scale));
			}
		}
		// 加载path
		if (pathCursor != null && pathCursor.moveToFirst()) {
			for (pathCursor.moveToFirst(); !pathCursor.isAfterLast(); pathCursor
					.moveToNext()) {
				long id = pathCursor.getLong(pathCursor
						.getColumnIndexOrThrow(PathDBEntity._ID));
				int showIndex = pathCursor.getInt(pathCursor
						.getColumnIndexOrThrow(PathDBEntity.SHOW_INDEX));
				double posX = pathCursor.getDouble(pathCursor
						.getColumnIndexOrThrow(PathDBEntity.POS_X));
				double posY = pathCursor.getDouble(pathCursor
						.getColumnIndexOrThrow(PathDBEntity.POS_Y));
				float curScale = pathCursor.getFloat(pathCursor
						.getColumnIndexOrThrow(PathDBEntity.CUR_SCALE));
				float orginalScale = pathCursor.getFloat(pathCursor
						.getColumnIndexOrThrow(PathDBEntity.ORI_SCALE));
				int paintColor = pathCursor.getInt(pathCursor
						.getColumnIndexOrThrow(PathDBEntity.STROKE_COLOR));
				int paintSize = pathCursor.getInt(pathCursor
						.getColumnIndexOrThrow(PathDBEntity.STROKE_WIDTH));
				byte[] points = pathCursor.getBlob(pathCursor
						.getColumnIndexOrThrow(PathDBEntity.POINTS));

				eList.add(new PathEntity(this, id, showIndex, orginalScale, curScale,
						paintColor, paintSize, posX,posY,points));
			}
		}
		// 加载笔触

		noteCursor.close();
		picCursor.close();
		pathCursor.close();

		Collections.sort(eList);
		entityList.addAll(eList);
		if (!entityList.isEmpty()) {
			maxShowIndex = entityList.getLast().showIndex;
		}
	}

	/**
	 * 设置便签字体大小
	 * 
	 * @param textSize
	 */
	public void setNoteTextSize(float textSize) {
		if (focusedEntity != null
				&& focusedEntity.getType() == TYPE_NOTE_ENTITY) {
			((NoteEntity) focusedEntity).setTextSize(textSize);
			invalidateView();
		}
	}

	/**
	 * 设置便签样式颜色
	 * 
	 * @param color
	 */
	public void setNoteStyleColor(int color) {
		if (focusedEntity != null
				&& focusedEntity.getType() == TYPE_NOTE_ENTITY) {
			((NoteEntity) focusedEntity).setStyleColor(color);
			invalidateView();
		}
	}

	/**
	 * 删除一个实体
	 * 
	 * @param entity
	 */
	public void delEntity(Entity entity) {
		if (focusedEntity.equals(entity)) {
			focusedEntity = null;
		} else {
			entityList.remove(entity);
		}
		dataManager.deleteEntity(entity);
		if (!entityList.isEmpty()) {
			maxShowIndex = entityList.getLast().showIndex;
		} else {
			maxShowIndex = 0;
		}
		invalidateView();
	}

	/**
	 * 绘制方法
	 * 
	 * @param canvas
	 */
	public void draw(Canvas canvas) {
		// 绘制纸张背景
		paperEntity.draw(canvas);

		
		canvas.saveLayer(0, 0, mWidth, mHeight, null, Canvas.ALL_SAVE_FLAG);
		for (Entity e : entityList) {
			e.draw(canvas);
		}

		// 绘制获取焦点的实体
		if (focusedEntity != null) {
			int[] loc = new int[2];
			mBindedView.getLocationOnScreen(loc);
			RectF bounds = new RectF(0, 0, mBindedView.getWidth(),
					mBindedView.getHeight());
			canvas.drawRect(bounds, coverPaint);
			focusedEntity.draw(canvas);
		}

		pathFactory.draw(canvas);
		canvas.restore();
	}

	/**
	 * 更改模式
	 * 
	 * @param mode
	 */
	public void changeMode(int mode) {
		if (this.mode != mode) {
			this.mode = mode;
			if (focusedEntity != null) {
				focusedEntity.removeFocus();
				entityList.add(originEntityIndex, focusedEntity);
				focusedEntity = null;
			}
		}
	}

	/**
	 * 添加实体
	 * 
	 * @param mode
	 */
	public void addEntity() {
		switch (mode) {
		case MODE_NOTE:
			Entity noteEntity = new NoteEntity(this, context, -1, boardID,
					++maxShowIndex, totalOffsetX + 100, totalOffsetY + 100,
					200, 300, "", context.getResources().getColor(
							R.color.note_style_blue), Color.BLACK, 20);
			dataManager.saveData(noteEntity);
			entityList.add(noteEntity);
			break;
		}
		invalidateView();
	}

	/**
	 * 保存实体
	 * 
	 * @param entity
	 */
	public void saveEntity(Entity entity) {
		dataManager.saveData(entity);
	}

	/**
	 * 屏幕触摸方法
	 * 
	 * @param event
	 */
	public void onEntityTouchEvent(MotionEvent event) {
		// 将触摸点位置设置为相对View
		int[] loc = new int[2];
		mBindedView.getLocationOnScreen(loc);
		float x = event.getX();
		float y = event.getY();
		event.setLocation(x + loc[0], y - loc[1]);

		// 图片模式或便签模式下
		if (mode == MODE_PIC || mode == MODE_NOTE) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (focusedEntity != null
						&& focusedEntity.isInRange(event.getX(), event.getY())) {
					// 如果点击位置在已经获取焦点的实体范围内

					focusedEntity.onEntityTouchEvent(event);
				} else {
					if (focusedEntity != null) {
						focusedEntity.removeFocus();
						entityList.add(originEntityIndex, focusedEntity);
						focusedEntity = null;
					}

					isCapture = false;
					// 便利查找获取焦点的实体
					for (int i = entityList.size() - 1; i >= 0; i--) {
						Entity e = entityList.get(i);
						if (mode == MODE_NOTE
								&& e.getType() == TYPE_NOTE_ENTITY
								|| mode == MODE_PIC
								&& e.getType() == TYPE_PIC_ENTITY) {
							if (!isCapture
									&& e.isInRange(event.getX(), event.getY())) {
								isCapture = true;
								originEntityIndex = i;
								entityList.remove(i);

								e.onEntityTouchEvent(event);
								focusedEntity = e;
								break;
							}
						}

					}

					// 如果点击位置不再任何entity上则要添加新的entity
					if (focusedEntity == null) {
						switch (this.mode) {
						case BoardEntity.MODE_PIC:
							this.picInsertX = event.getX();
							this.picInsertY = event.getY();
							((WhiteBoardActivity) (context)).loadPicture();
							break;
						case BoardEntity.MODE_NOTE:

							break;
						}
					}

				}
			} else {
				if (focusedEntity != null) {
					focusedEntity.onEntityTouchEvent(event);
				}
			}
		} else if (mode == MODE_HANDDRAW) {
			// 手绘模式
			pathFactory.onTouch(event);
			invalidateView();
		}

	}

	/**
	 * 获取Context对象
	 * 
	 * @return
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * 通知View重绘
	 */
	public void invalidateView() {
		mBindedView.invalidate();
	}

	/**
	 * 获取总缩放比例
	 * 
	 * @return
	 */
	public double getTotalScale() {
		return totalScale;
	}

	/**
	 * 获取绘制范围数组
	 * 
	 * @return 数组元素依次为：左上右下
	 */
	public double[] getDrawRangeArr() {
		return new double[] { drawRangeLeft, drawRangeTop, drawRangeRight,
				drawRangeBottom };
	}

	/**
	 * 获取总偏移
	 * 
	 * @return 数组元素依次为：偏移X 偏移Y
	 */
	public double[] getTotalOffsetArr() {
		return new double[] { totalOffsetX, totalOffsetY };
	}

	/**
	 * 绑定View
	 * 
	 * @param v
	 */
	public void bindView(BoardView v) {
		mBindedView = v;
	}

	/**
	 * 提交输入的文本
	 * 
	 * @param text
	 *            需要提交的文本
	 * @param isNewLine
	 *            是否为新起一行，如果为true，参数text可以为null
	 */
	public void commitInputText(String text, boolean isNewLine) {
		if (focusedEntity != null
				&& focusedEntity.getType() == TYPE_NOTE_ENTITY) {
			((NoteEntity) focusedEntity).commitInputText(text, isNewLine);
		}
	}

	/**
	 * 删除之前提交的一个文本字符
	 */
	public void delPrevInputText() {
		if (focusedEntity != null
				&& focusedEntity.getType() == TYPE_NOTE_ENTITY) {
			((NoteEntity) focusedEntity).delPrevInputText();
		}
	}

	/**
	 * 切换软键盘显示
	 * 
	 * @param open
	 *            true-显示 false-关闭
	 */
	public void toggleInput(boolean open) {
		mBindedView.toggleInput(open);
	}

	/**
	 * 画板坐标到屏幕坐标转换
	 * 
	 * @param bx
	 * @param by
	 * @return 转换后的屏幕坐标
	 */
	public PointF boardToScreenCoodTrans(double bx, double by) {
		float sx = (float) ((bx + totalOffsetX) * totalScale);
		float sy = (float) ((by + totalOffsetY) * totalScale);
		return new PointF(sx, sy);
	}

	/**
	 * 屏幕坐标转换为画板坐标
	 * 
	 * @param sx
	 * @param sy
	 * @return 数组元素依次为：画板坐标X，画板坐标Y
	 */
	public double[] screenToBoardCoodTrans(float sx, float sy) {
		double bx = sx / totalScale - totalOffsetX;
		double by = sy / totalScale - totalOffsetY;
		return new double[] { bx, by };
	}

	/**
	 * 画板上尺寸到屏幕上尺寸的转换
	 * 
	 * @param size
	 * @return
	 */
	public float boardToScreenSizeTrans(double bs) {
		return (float) (bs * totalScale);
	}

	/**
	 * 屏幕上尺寸到画板上尺寸的转换
	 * 
	 * @param size
	 * @return
	 */
	public double screenToBoardSizeTrans(float ss) {
		return ss / totalScale;
	}

	/**
	 * 计算画板参数
	 * 
	 * @param smx
	 *            屏幕缩放中心点X
	 * @param smy
	 *            屏幕缩放中心点Y
	 * @param scale
	 *            缩放比例
	 * @param sdx
	 *            屏幕坐标偏移X
	 * @param sdy
	 *            屏幕坐标偏移Y
	 * @param sMaxX
	 *            屏幕可绘制区域最大X
	 * @param sMaxY
	 *            屏幕可绘制区域最大Y
	 */
	public void calculate(float smx, float smy, float scale, float sdx,
			float sdy, float sMaxX, float sMaxY) {

		// Log.d("DevLog", String.format("bdx:bdy=(%f,%f)\nscale=%f",
		// bdx.floatValue(), bdy.floatValue(), scale));

		float bmx = (float) (smx / totalScale - totalOffsetX);
		float bmy = (float) (smy / totalScale - totalOffsetY);

		// 计算总缩放比例
		if (totalScale * scale > MIN_TOTAL_SCALE
				&& totalScale * scale < MAX_TOTAL_SCALE) {
			totalScale *= scale;
		}

		totalOffsetX = smx / totalScale - bmx;
		totalOffsetY = smy / totalScale - bmy;

		// 屏幕偏移转化为画板偏移
		double dx = sdx / totalScale;
		double dy = sdy / totalScale;

		// 计算画板总偏移
		totalOffsetX += dx;
		totalOffsetY += dy;

		// 计算绘制范围
		drawRangeLeft = -totalOffsetX;
		drawRangeTop = -totalOffsetY;
		float maxX = mBindedView.getWidth();
		float maxY = mBindedView.getHeight();
		drawRangeRight = maxX / totalScale - totalOffsetX;
		drawRangeBottom = maxY / totalScale - totalOffsetY;

		// Log.d("DevLog", String.format("(%f,%f,%f,%f)", drawRangeLeft,
		// drawRangeTop, drawRangeRight, drawRangeBottom));
	}

	/**
	 * 与WhiteBoardActivity的loadPicture配对使用，当它获取到图片后会调用该函数将其传入进来
	 * 
	 * @param b
	 */
	public void receivePicture(String fileName) {
		Entity entity = new PictureEntity(this, getMaxShowIndex(),fileName, picInsertX,
				picInsertY);
		entityList.add(entity);
		invalidateView();
		dataManager.saveData(entity);
	}

	public PathFactory getPathFactory() {
		return this.pathFactory;
	}

	public int getBoardID() {
		return boardID;
	}

	public DataManager getDataManager() {
		return dataManager;
	}
	public int getMaxShowIndex() {
		return ++maxShowIndex;
	}
}
