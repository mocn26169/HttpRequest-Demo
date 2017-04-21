package com.bourne.httprequest.multiThreadDownload.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bourne.httprequest.ThreadInfo;


import java.util.ArrayList;
import java.util.List;


/**
 * 數據庫增刪改查的實現類
 *
 */
public class MultiDAOImple implements MultiThreadDAO {
	private MultiDBHelper MultiDBHelper = null;

	public MultiDAOImple(Context context) {
		super();
		this.MultiDBHelper = MultiDBHelper.getInstance(context);
	}

	// 插入綫程
	@Override
	public synchronized void insertThread(ThreadInfo info) {
		SQLiteDatabase db = MultiDBHelper.getReadableDatabase();
		ContentValues values = new ContentValues();
		values.put("thread_id", info.getId());
		values.put("url", info.getUrl());
		values.put("start", info.getStart());
		values.put("end", info.getEnd());
		values.put("finished", info.getFinished());
		db.insert("thread_info", null, values);

		db.close();
	}

	// 刪除綫程
	@Override
	public synchronized void deleteThread(String url) {
		SQLiteDatabase db = MultiDBHelper.getReadableDatabase();
		db.delete("thread_info", "url = ?", new String[] { url});

		db.close();

	}

	// 更新綫程
	@Override
	public synchronized void updateThread(String url, int thread_id, int finished) {
		SQLiteDatabase db = MultiDBHelper.getReadableDatabase();

		db.execSQL("update thread_info set finished = ? where url = ? and thread_id = ?",
				new Object[]{finished, url, thread_id});
		db.close();
	}

	// 查詢綫程
	@Override
	public List<ThreadInfo> queryThreads(String url) {
		SQLiteDatabase db = MultiDBHelper.getReadableDatabase();

		List<ThreadInfo> list = new ArrayList<ThreadInfo>();

		Cursor cursor = db.query("thread_info", null, "url = ?", new String[] { url }, null, null, null);
		while (cursor.moveToNext()) {
			ThreadInfo thread = new ThreadInfo();
			thread.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
			thread.setUrl(cursor.getString(cursor.getColumnIndex("url")));
			thread.setStart(cursor.getInt(cursor.getColumnIndex("start")));
			thread.setEnd(cursor.getInt(cursor.getColumnIndex("end")));
			thread.setFinished(cursor.getInt(cursor.getColumnIndex("finished")));
			list.add(thread);
		}


		cursor.close();
		db.close();
		return list;
	}

	// 判斷綫程是否爲空
	@Override
	public boolean isExists(String url, int thread_id) {
		SQLiteDatabase db = MultiDBHelper.getReadableDatabase();
		Cursor cursor = db.query("thread_info", null, "url = ? and thread_id = ?", new String[] { url, thread_id + "" },
				null, null, null);
		boolean exists = cursor.moveToNext();

		db.close();
		cursor.close();
		return exists;
	}

}
