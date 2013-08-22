package com.guojin.store;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.guojin.store.DatabaseContract.NoteDBEntity;

/**
 * 数据库辅助类
 * @author donie
 *
 */
public class DBHelper extends SQLiteOpenHelper {

	public static final int DB_VERSION = 1;
	public static final String DB_NAME = "WhiteBoard.db";
	
	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(NoteDBEntity.SQL_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(NoteDBEntity.SQL_DELETE);
		onCreate(db);
	}
	
}
