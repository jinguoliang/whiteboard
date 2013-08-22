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
	 * 记事本实体表
	 * @author donie
	 *
	 */
	public static abstract class NoteDBEntity implements BaseColumns {
		public static final String TABLE_NAME = "notetable";
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
}
