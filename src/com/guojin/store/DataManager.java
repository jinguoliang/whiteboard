package com.guojin.store;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.guojin.entities.Entity;
import com.guojin.entities.NoteEntity;
import com.guojin.entities.PictureEntity;
import com.guojin.store.DatabaseContract.NoteDBEntity;

public class DataManager {

	Context context;
	DBHelper dbHelper;
	SQLiteDatabase db;
	
	public DataManager(Context c) {
		context = c;
		dbHelper = new DBHelper(c);
	}
	
	/**
	 * 保存数据
	 * @param entity
	 */
	public void saveData(Entity entity) {
		long id = entity.getID();
		db = dbHelper.getWritableDatabase();
		if (entity instanceof NoteEntity) {
			if (id == -1) {
				id = db.insert(NoteDBEntity.TABLE_NAME, null, entity.getContentValues());
				entity.setID(id);
			} else {
				db.update(NoteDBEntity.TABLE_NAME, entity.getContentValues(),
						NoteDBEntity._ID + "=?", new String[] {"" + id});
			}
		} else if (entity instanceof PictureEntity) {
			
		}
		
		db.close();
	}
	
	
	/**
	 * 删除一个实体
	 * @param entity
	 */
	public void deleteEntity(Entity entity) {
		long id = entity.getID();
		if (id != -1) {
			db.delete(NoteDBEntity.TABLE_NAME,
					NoteDBEntity._ID + "=?", new String[] {"" + id});
		}
	}
}
