package com.guojin.store;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.guojin.entities.Entity;
import com.guojin.entities.NoteEntity;
import com.guojin.entities.PathEntity;
import com.guojin.entities.PictureEntity;
import com.guojin.store.DatabaseContract.NoteDBEntity;
import com.guojin.store.DatabaseContract.PathDBEntity;
import com.guojin.store.DatabaseContract.PicDBEntity;

public class DataManager {

	Context context;
	DBHelper dbHelper;
	SQLiteDatabase db;

	public DataManager(Context c) {
		context = c;
		dbHelper = new DBHelper(c);
		db = dbHelper.getWritableDatabase();
	}

	/**
	 * 获取用于检索所有Entity的Cursor
	 * 
	 * @param boardId
	 * @return [0]:noteCursor [1]:picCursor [2]:pathCousor
	 */
	public Cursor[] getAllCursor(int boardId) {

		Cursor noteCursor = db.query(NoteDBEntity.TABLE_NAME, null,
				NoteDBEntity.BOARD_ID + "=?", new String[] { boardId + "" },
				null, null, null);
		Cursor picCursor = db.query(PicDBEntity.TABLE_NAME, null,
				PicDBEntity.BOARD_ID + "=?", new String[] { boardId + "" },
				null, null, null);
		Cursor pathCursor = db.query(PathDBEntity.TABLE_NAME, null,
				PathDBEntity.BOARD_ID + "=?", new String[] { boardId + "" },
				null, null, null);

		return new Cursor[] { noteCursor, picCursor, pathCursor };
	}

	/**
	 * 保存数据
	 * 
	 * @param entity
	 */
	public void saveData(Entity entity) {
		long id = entity.getID();

		if (entity instanceof NoteEntity) {
			if (id == -1) {
				id = db.insertOrThrow(NoteDBEntity.TABLE_NAME, null,
						entity.getContentValues());
				entity.setID(id);
			} else {
				db.update(NoteDBEntity.TABLE_NAME, entity.getContentValues(),
						NoteDBEntity._ID + "=?", new String[] { "" + id });
			}
		} else if (entity instanceof PictureEntity) {
			if (id == -1) {
				id = db.insertOrThrow(PicDBEntity.TABLE_NAME, null,
						entity.getContentValues());
				entity.setID(id);
			} else {
				db.update(PicDBEntity.TABLE_NAME, entity.getContentValues(),
						NoteDBEntity._ID + "=?", new String[] { "" + id });
			}
		} else if (entity instanceof PathEntity) {
			if (id == -1) {
				id = db.insertOrThrow(PathDBEntity.TABLE_NAME, null,
						entity.getContentValues());
				entity.setID(id);
			} else {
				db.update(PathDBEntity.TABLE_NAME, entity.getContentValues(),
						NoteDBEntity._ID + "=?", new String[] { "" + id });
			}
		}

	}

	/**
	 * 删除一个实体
	 * 
	 * @param entity
	 */
	public void deleteEntity(Entity entity) {
		String tableName = null;
		if (entity instanceof NoteEntity) {
			tableName = NoteDBEntity.TABLE_NAME;
		} else if (entity instanceof PictureEntity) {
			tableName = PicDBEntity.TABLE_NAME;
		} else if (entity instanceof PathEntity) {
			tableName = PathDBEntity.TABLE_NAME;
		}
		
		long id = entity.getID();
		if (id != -1) {
			db.delete(tableName, NoteDBEntity._ID + "=?", new String[] { ""
					+ id });
		}
	}

	/**
	 * 关闭数据库连接
	 */
	public void closeDB() {
		db.close();
	}
}
