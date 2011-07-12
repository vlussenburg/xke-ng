package com.xebia.xcoss.axcv.logic;

import hirondelle.date4j.DateTime;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class ProfileManager extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_NAME = "xkeng.db";
	private static final String TRACK_TABLE = "Track";
	private static final String TRACK_COL_ID = "id";
	private static final String TRACK_COL_USER = "user";
	private static final String TRACK_COL_SESSION = "sid";
	private static final String TRACK_COL_DATE = "date";

	private static final String TRACK_QUERY_SELECT = TRACK_COL_USER + " = ? AND " + TRACK_COL_SESSION + " = ?";
	private static final String TRACK_QUERY_NAME = TRACK_COL_USER + " = ?";
	private static final String TRACK_QUERY_PRUNE = TRACK_COL_DATE + " < ?";

	private SQLiteDatabase database = null;

	public ProfileManager(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void openConnection() {
		try {
			if (database == null || !database.isOpen()) {
				database = getWritableDatabase();
			}
		}
		catch (Exception e) {
			Log.w(XCS.LOG.COMMUNICATE, "Opening database failed: " + StringUtil.getExceptionMessage(e));
			database = null;
		}
	}

	public void closeConnection() {
		try {
			if (database != null) database.close();
			database = null;
		}
		catch (Exception e) {
			Log.w(XCS.LOG.COMMUNICATE, "Closing database failed: " + StringUtil.getExceptionMessage(e));
		}
		close();
	}

	private void checkConnection() {
		if (database == null ) {
			throw new SQLException("Database not started!");
		}
		if (!database.isOpen() || database.isReadOnly()) {
			throw new SQLException("Database not open or readonly!");
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		StringBuilder create = new StringBuilder();
		create.append("create table ");
		create.append(TRACK_TABLE);
		create.append(" (");
		create.append(TRACK_COL_ID);
		create.append(" integer primary key autoincrement, ");
		create.append(TRACK_COL_USER);
		create.append(" text not null, ");
		create.append(TRACK_COL_SESSION);
		create.append(" integer not null, ");
		create.append(TRACK_COL_DATE);
		create.append(" timestamp);");
		db.execSQL(create.toString());
	}

	public boolean markSession(String user, Session session) {
		Log.v(XCS.LOG.COMMUNICATE, "Marking session " + session.getId() + " for user " + user);
		try {
			checkConnection();
			ContentValues row = new ContentValues();
			row.put(TRACK_COL_USER, user);
			row.put(TRACK_COL_SESSION, session.getId());
			row.put(TRACK_COL_DATE, session.getDate().getMilliseconds(XCS.TZ));
			long rv = database.insert(TRACK_TABLE, null, row);
			return rv >= 0;
		}
		catch (Exception e) {
			Log.w(XCS.LOG.COMMUNICATE, "Marking session failed: " + StringUtil.getExceptionMessage(e));
			return false;
		}
	}

	public boolean unmarkSession(String user, Session session) {
		Log.v(XCS.LOG.COMMUNICATE, "Unmarking session " + session.getId() + " for user " + user);
		try {
			checkConnection();
			String[] whereArgs = new String[2];
			whereArgs[0] = user;
			whereArgs[1] = String.valueOf(session.getId());
			int rv = database.delete(TRACK_TABLE, TRACK_QUERY_SELECT, whereArgs);
			return rv > 0;
		}
		catch (Exception e) {
			Log.w(XCS.LOG.COMMUNICATE, "Unmarking session failed: " + StringUtil.getExceptionMessage(e));
			return false;
		}
	}

	public void pruneMarked(DateTime today) {
		Log.v(XCS.LOG.COMMUNICATE, "Pruning database");
		try {
			checkConnection();
			String[] whereArgs = new String[1];
			whereArgs[0] = String.valueOf(today.getMilliseconds(XCS.TZ));
			database.delete(TRACK_TABLE, TRACK_QUERY_PRUNE, whereArgs);
		}
		catch (Exception e) {
			Log.w(XCS.LOG.COMMUNICATE, "Pruning database failed: " + StringUtil.getExceptionMessage(e));
		}
	}

	public boolean isMarked(String user, int sessionId) {
		Log.v(XCS.LOG.COMMUNICATE, "Check if marked " + sessionId + " for user " + user);
		try {
			checkConnection();
			String[] whereArgs = new String[2];
			whereArgs[0] = user;
			whereArgs[1] = String.valueOf(sessionId);
			Cursor query = database.query(TRACK_TABLE, new String[] { TRACK_COL_ID }, TRACK_QUERY_SELECT, whereArgs,
					null, null, null);
			boolean hasMark = query.getCount() > 0;
			query.close();
			return hasMark;
		}
		catch (Exception e) {
			Log.w(XCS.LOG.COMMUNICATE, "Unmarking session failed: " + StringUtil.getExceptionMessage(e));
			return false;
		}
	}

	public int[] getMarkedSessionIds(String user) {
		Log.v(XCS.LOG.COMMUNICATE, "Get all marked sessions for user " + user);
		try {
			checkConnection();
			String[] whereArgs = new String[1];
			whereArgs[0] = user;
			Cursor query = database.query(TRACK_TABLE, new String[] { TRACK_COL_SESSION }, TRACK_QUERY_NAME,
					new String[] { user }, null, null, TRACK_COL_DATE + " ASC");
			int[] result = new int[query.getCount()];
			int i = 0;
			for (query.moveToFirst(); !query.isAfterLast(); query.moveToNext()) {
				result[i++] = query.getInt(query.getColumnIndex(TRACK_COL_SESSION));
			}
			query.close();
			return result;
		}
		catch (Exception e) {
			Log.w(XCS.LOG.COMMUNICATE, "Retrieval failed: " + StringUtil.getExceptionMessage(e));
			return new int[0];
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2) {
		// TODO Auto-generated method stub
	}
}