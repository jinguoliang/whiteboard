package com.guojin.store;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.guojin.store.DatabaseContract.NoteDBEntity;
import com.guojin.store.DatabaseContract.PathDBEntity;
import com.guojin.store.DatabaseContract.PicDBEntity;

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
		db.execSQL(PicDBEntity.SQL_CREATE);
		db.execSQL(PathDBEntity.SQL_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(NoteDBEntity.SQL_DELETE);
		db.execSQL(PicDBEntity.SQL_DELETE);
		db.execSQL(PathDBEntity.SQL_DELETE);
		onCreate(db);
	}
	
}
