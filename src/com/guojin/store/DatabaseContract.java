package com.guojin.store;

import android.provider.BaseColumns;

/**
 * 数据库Contract
 * @author donie
 *
 */
public class DatabaseContract {

	public DatabaseContract() {
	}

	/**
	 * 白板实体表
	 * @author donie
	 *
	 */
	public static abstract class BoardDBEntity implements BaseColumns {
		public static final String TABLE_NAME = "notetable";
		public static final String NAME = "name";
		public static final String MTIME = "mtime";
		public static final String CTIME = "ctime";
		public static final String THUMB_SRC = "thumb_src";
		public static final String SCALE = "scale";
		public static final String OFF_X = "off_x";
		public static final String OFF_Y = "off_y";
		
		// 创建语句
		public static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
				_ID + " INTEGER PRIMARY KEY," +
				NAME + "Text, " +
				MTIME + " DATETIME," +
				CTIME + " DATETIME," +
				THUMB_SRC + " TEXT," +
				SCALE + " REAL," +
				OFF_X + " REAL," +
				OFF_Y + " REAL" +
				")";
		// 删除语句
		public static final String SQL_DELETE = "DROP TABLE IF EXISTS " + TABLE_NAME;
	}
	
	/**
	 * 记事本实体表
	 * @author donie
	 *
	 */
	public static abstract class NoteDBEntity implements BaseColumns {
		public static final String TABLE_NAME = "NOTE_ENTITYS";
		public static final String BOARD_ID = "board_id";
		public static final String SHOW_INDEX = "show_index";
		public static final String POS_X = "pos_x";
		public static final String POS_Y = "pos_y";
		public static final String WIDTH = "width";
		public static final String HEIGHT = "height";
		public static final String TEXT = "text";
		public static final String BG_COLOR = "bg_color";
		public static final String TEXT_COLOR = "text_color";
		public static final String TEXT_SIZE = "text_size";
		
		// 创建语句
		public static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
				_ID + " INTEGER PRIMARY KEY," +
				BOARD_ID + " INTEGER," +
				SHOW_INDEX + " INTEGER," +
				POS_X + " REAL," +
				POS_Y + " REAL," +
				WIDTH + " REAL," +
				HEIGHT + " REAL," +
				TEXT + " TEXT," +
				BG_COLOR + " INTEGER," +
				TEXT_COLOR + " INTEGER," +
				TEXT_SIZE + " INTEGER" + 
				")";
		// 删除语句
		public static final String SQL_DELETE = "DROP TABLE IF EXISTS " + TABLE_NAME;
	}
	
	/**
	 * 图片实体表
	 * @author donie
	 *
	 */
	public static abstract class PicDBEntity implements BaseColumns {
		public static final String TABLE_NAME = "PIC_ENTITYS";
		public static final String BOARD_ID = "board_id";
		public static final String SHOW_INDEX = "show_index";
		public static final String POS_X = "pos_x";
		public static final String POS_Y = "pos_y";
		public static final String WIDTH = "width";
		public static final String HEIGHT = "height";
		public static final String SCALE = "scale";
		public static final String ROTATE = "rotate";
		public static final String SRC = "src";
		
		// 创建语句
		public static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
				_ID + " INTEGER PRIMARY KEY," +
				BOARD_ID + " INTEGER," +
				SHOW_INDEX + " INTEGER," +
				POS_X + " REAL," +
				POS_Y + " REAL," +
				WIDTH + " REAL," +
				HEIGHT + " REAL," +
				SCALE + " REAL," +
				ROTATE + " REAL," +
				SRC + " TEXT" +
				")";
		// 删除语句
		public static final String SQL_DELETE = "DROP TABLE IF EXISTS " + TABLE_NAME;
	}
	
	/**
	 * 绘制路径表
	 * @author donie
	 *
	 */
	public static abstract class PathDBEntity implements BaseColumns {
		public static final String TABLE_NAME = "PATH_ENTITYS";
		public static final String BOARD_ID = "board_id";
		public static final String SHOW_INDEX = "show_index";
		public static final String POS_X = "pos_x";
		public static final String POS_Y = "pos_y";
		public static final String CUR_SCALE = "curscale";
		public static final String ORI_SCALE = "oriscale";
		public static final String STROKE_WIDTH = "strokewidth";
		public static final String STROKE_COLOR = "strokecolor";
		public static final String POINTS = "points";
		
		// 创建语句
		public static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
				_ID + " INTEGER PRIMARY KEY," +
				BOARD_ID + " INTEGER," +
				SHOW_INDEX + " INTEGER," +
				POS_X + " REAL," +
				POS_Y + " REAL," +
				CUR_SCALE + " REAL," +
				ORI_SCALE + " REAL," +
				STROKE_COLOR + " INTEGER," +
				STROKE_WIDTH + " INTEGER, " +
				POINTS + " BLOB" +
				")";
		// 删除语句
		public static final String SQL_DELETE = "DROP TABLE IF EXISTS " + TABLE_NAME;
	}
}
