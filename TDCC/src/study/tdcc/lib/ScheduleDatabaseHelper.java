/**
 ToDoCycleCalendar
 Copyright (C) 2012  Tohru Mashiko

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.


 クラス名：ScheduleDatabaseHelper
 内容：SQLite(schedule.db)ファイルの
       作成モジュール
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
          0.1/2012.05.21/T.Mashiko
          0.2/2012.05.23/T.Mashiko
          0.3/2012.05.31/T.Mashiko
          0.4/2012.06.11/T.Mashiko
          0.5/2012.06.12/T.Mashiko
          0.6/2012.06.14/T.Mashiko
          0.7/2012.06.19/T.Mashiko 終日スケジュールリストのパフォーマンス改善
          0.8/2012.06.20/T.Mashiko
          0.9/2012.06.28/T.Mashiko GDATA確認処理
          1.0/2012.06.29/T.Mashiko GDATA削除処理
          1.1/2012.06.30/T.Mashiko GDATAフラグ変更処理
          1.2/2012.07.02/T.Mashiko GDATA登録処理
          1.3/2012.07.04/T.Mashiko GDATA削除処理修正,GDATA更新処理時にアラームフラグがオフになる問題対応
          1.4/2012.07.06/T.Mashiko AuthInfoテーブルと関係メソッド追加
          1.5/2012.07.17/T.Mashiko コメント修正
          1.6/2012.09.14/T.Mashiko getColScheduleCount追加
*/
package study.tdcc.lib;

import java.util.ArrayList;

import study.tdcc.*;
import study.tdcc.bean.AllDayWhere;
import study.tdcc.bean.AuthInfo;
import study.tdcc.bean.Schedule;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class ScheduleDatabaseHelper extends SQLiteOpenHelper {
	private Context objContext;
	private static final int DATABASE_VERSION = 1;

	// データベース生成
	public ScheduleDatabaseHelper(Context context) {
		// ストレージ(ローカルファイル)にDBを作成
		super(context, context.getResources().getString(R.string.sqlite_schedule_filename), null, DATABASE_VERSION);
		objContext = context;
	}

	/**
	 * 文字列配列内の全てのSQLを実行
	 * @param db DBオブジェクト
	 * @param sql SQLが格納された文字列配列
	 */
	private void execMultipleSQL(SQLiteDatabase db, String[] sql){
		Log.d("DEBUG", "ScheduleDatabaseHelper execMultipleSQL Start");
		for( String strObj : sql ) {
			if (strObj.trim().length()>0) {
				db.execSQL(strObj);
			}
		}
		Log.d("DEBUG", "ScheduleDatabaseHelper execMultipleSQL End");
	}

	/** データベースの作成時に実行されるメソッド */
	@Override
	public void onCreate(SQLiteDatabase db) throws SQLException {
		//スケジュールテーブル
		String[] sql = objContext.getString(R.string.sqlite_schedule_create).split("\n");
		//トランザクション開始
		db.beginTransaction();
		try {
			//Create tables & Initial data
			execMultipleSQL(db, sql);
			//トランザクションコミット
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e("ERROR","ScheduleDatabaseHelper onCreate",e);
			throw new SQLException();
		} finally {
			//トランザクションロールバック
			db.endTransaction();
		}
	}

	/** データベースのアップグレード時に実行されるメソッド */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) throws SQLException {
		Log.d("DEBUG", "ScheduleDatabaseHelper onUpgrade Start");
		Log.d("DEBUG", "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		//スケジュールテーブル
		String[] sql = objContext.getString(R.string.sqlite_schedule_upgrade).split("\n");
		//トランザクション開始
		db.beginTransaction();
		try {
			//Create tables & Initial data
			execMultipleSQL(db, sql);
			//トランザクションコミット
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e("ERROR","ScheduleDatabaseHelper onUpgrade",e);
			throw new SQLException();
		} finally {
			//トランザクションロールバック
			db.endTransaction();
		}
		// This is cheating.  In the real world, you'll need to add columns,
		// not rebuild from scratch
		onCreate(db);
		Log.d("DEBUG", "ScheduleDatabaseHelper onUpgrade End");
	}

	/**
	 * Provides self-contained query-specific cursor for Schedule.
	 * The query and all Accessor methods are in the class.
	 */
	public static class ScheduleCursor extends SQLiteCursor {

		//カーソルコンストラクター
		private ScheduleCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
			super(db, driver, editTable, query);
		}

		//rawQueryWithFactory()メソッド実行用のファクトリークラス
		private static class Factory implements SQLiteDatabase.CursorFactory{
			public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
				return new ScheduleCursor(db, driver, editTable, query);
			}
		}

		//データベースカラム対になるアクセサ定義
		public long getColId() {
			return getLong(getColumnIndexOrThrow("ID"));
		}
		public long getColDeleted() {
			return getLong(getColumnIndexOrThrow("DELETED"));
		}
		public long getColModified() {
			return getLong(getColumnIndexOrThrow("MODIFIED"));
		}
		public long getColAlarm() {
			return getLong(getColumnIndexOrThrow("ALARM"));
		}
		public String getColAlarmList() {
			return getString(getColumnIndexOrThrow("ALARM_LIST"));
		}
		public String getColTitle() {
			return getString(getColumnIndexOrThrow("TITLE"));
		}
		public String getColContent() {
			return getString(getColumnIndexOrThrow("CONTENT"));
		}
		public String getColGdWhere() {
			return getString(getColumnIndexOrThrow("GD_WHERE"));
		}
		public String getColGdWhenEndtime() {
			return getString(getColumnIndexOrThrow("GD_WHEN_ENDTIME"));
		}
		public String getColGdWhenStarttime() {
			return getString(getColumnIndexOrThrow("GD_WHEN_STARTTIME"));
		}
		public String getColPublished() {
			return getString(getColumnIndexOrThrow("PUBLISHED"));
		}
		public String getColUpdated() {
			return getString(getColumnIndexOrThrow("UPDATED"));
		}
		public String getColCategory() {
			return getString(getColumnIndexOrThrow("CATEGORY"));
		}
		public String getColEditUrl() {
			return getString(getColumnIndexOrThrow("EDIT_URL"));
		}
		public String getColGdEventstatus() {
			return getString(getColumnIndexOrThrow("GD_EVENTSTATUS"));
		}
		public String getColCalendarId() {
			return getString(getColumnIndexOrThrow("CALENDAR_ID"));
		}
		public String getColEtag() {
			return getString(getColumnIndexOrThrow("ETAG"));
		}
		public long getColEndtime() {
			return getLong(getColumnIndexOrThrow("ENDTIME"));
		}
		public long getColStarttime() {
			return getLong(getColumnIndexOrThrow("STARTTIME"));
		}
		public long getColAlarmFlag() {
			return getLong(getColumnIndexOrThrow("ALARM_FLAG"));
		}
		public String getColTargetDate() {
			return getString(getColumnIndexOrThrow("TARGET_DATE"));
		}
		public long getColScheduleCount() {
			return getLong(getColumnIndexOrThrow("SCHEDULE_COUNT"));
		}
	}

	/** スケジュール取得処理
	 * @param strArgs SQLにバインドする文字列配列
	 */
	public ScheduleCursor getSchedule(String[] strArgs) {
		Log.d("DEBUG", "ScheduleDatabaseHelper getSchedule Start");
		String sql = objContext.getString(R.string.sqlite_schedule_select1);
		SQLiteDatabase sdObj = getReadableDatabase();
		ScheduleCursor cObj = (ScheduleCursor) sdObj.rawQueryWithFactory(
					 new ScheduleCursor.Factory(),
					 sql,
					 strArgs,
					 null);
		cObj.moveToFirst();
		sdObj.close();
		sdObj = null;
		Log.d("DEBUG", "ScheduleDatabaseHelper getSchedule End");
		return cObj;
	}

	/** スケジュールリスト取得処理
	 * @param strArgs SQLにバインドする文字列配列
	 */
	public ScheduleCursor getScheduleList(String[] strArgs) {
		Log.d("DEBUG", "ScheduleDatabaseHelper getScheduleList Start");
		String sql = objContext.getString(R.string.sqlite_schedule_select2);
		SQLiteDatabase sdObj = getReadableDatabase();
		ScheduleCursor cObj = (ScheduleCursor) sdObj.rawQueryWithFactory(
					 new ScheduleCursor.Factory(),
					 sql,
					 strArgs,
					 null);
		cObj.moveToFirst();
		sdObj.close();
		sdObj = null;
		Log.d("DEBUG", "ScheduleDatabaseHelper getScheduleList End");
		return cObj;
	}

	/** 終日スケジュールリスト取得処理
	 * @param alSqlWhere AllDayWhere BeanのArrayList
	 */
	public ArrayList<String> getScheduleAllDayList(ArrayList<AllDayWhere> alSqlWhere) {
		Log.d("DEBUG", "ScheduleDatabaseHelper getScheduleAllDayList Start");
		ArrayList<String> alResult = new ArrayList<String>();
		AllDayWhere objADW = new AllDayWhere();
		String sql = objContext.getString(R.string.sqlite_schedule_select3);
		SQLiteDatabase sdObj = getReadableDatabase();
		for (int intALCt = 0; intALCt < alSqlWhere.size(); intALCt++) {
			objADW = alSqlWhere.get(intALCt);
			StringBuffer sbResult = new StringBuffer();
			String[] strArgs = {String.valueOf(objADW.getLgStartTime()), String.valueOf(objADW.getLgEndTime())};
			ScheduleCursor cObj = (ScheduleCursor) sdObj.rawQueryWithFactory(
						 new ScheduleCursor.Factory(),
						 sql,
						 strArgs,
						 null);
			cObj.moveToFirst();
			for( int intCt=0; intCt<cObj.getCount(); intCt++){
				//終日の場合
				sbResult.append(cObj.getColTitle());
				sbResult.append("\n");
				cObj.moveToNext();
			}
			if(cObj != null) {
				cObj.close();
			}
			alResult.add(intALCt, sbResult.toString());
		}
		sdObj.close();
		sdObj = null;
		Log.d("DEBUG", "ScheduleDatabaseHelper getScheduleAllDayList End");
		return alResult;
	}

	/** スケジュール検索結果取得処理
	 * @param strArgs SQLにバインドする文字列配列
	 */
	public ScheduleCursor getScheduleSearchResult(String[] strArgs) {
		Log.d("DEBUG", "ScheduleDatabaseHelper getScheduleSearchResult Start");
		String sql = objContext.getString(R.string.sqlite_schedule_select4);
		SQLiteDatabase sdObj = getReadableDatabase();
		ScheduleCursor cObj = (ScheduleCursor) sdObj.rawQueryWithFactory(
					 new ScheduleCursor.Factory(),
					 sql,
					 strArgs,
					 null);
		cObj.moveToFirst();
		sdObj.close();
		sdObj = null;
		Log.d("DEBUG", "ScheduleDatabaseHelper getScheduleSearchResult End");
		return cObj;
	}

	/** 更新対象スケジュール取得処理
	 * @param strArgs SQLにバインドする文字列配列
	 */
	public ScheduleCursor getScheduleUpdated(String[] strArgs) {
		Log.d("DEBUG", "ScheduleDatabaseHelper getScheduleUpdated Start");
		String sql = objContext.getString(R.string.sqlite_schedule_select5);
		SQLiteDatabase sdObj = getReadableDatabase();
		ScheduleCursor cObj = (ScheduleCursor) sdObj.rawQueryWithFactory(
					 new ScheduleCursor.Factory(),
					 sql,
					 strArgs,
					 null);
		cObj.moveToFirst();
		sdObj.close();
		sdObj = null;
		Log.d("DEBUG", "ScheduleDatabaseHelper getScheduleUpdated End");
		return cObj;
	}

	/** 変更対象スケジュールリスト取得処理
	 * @param strArgs SQLにバインドする文字列配列
	 */
	public ScheduleCursor getChangeScheduleList(String[] strArgs) {
		Log.d("DEBUG", "ScheduleDatabaseHelper getChangeScheduleList Start");
		String sql = objContext.getString(R.string.sqlite_schedule_select6);
		SQLiteDatabase sdObj = getReadableDatabase();
		ScheduleCursor cObj = (ScheduleCursor) sdObj.rawQueryWithFactory(
					 new ScheduleCursor.Factory(),
					 sql,
					 strArgs,
					 null);
		cObj.moveToFirst();
		sdObj.close();
		sdObj = null;
		Log.d("DEBUG", "ScheduleDatabaseHelper getChangeScheduleList End");
		return cObj;
	}

	/** 変更対象スケジュール件数取得処理
	 * @param strArgs SQLにバインドする文字列配列
	 */
	public ScheduleCursor getChangeScheduleCount(String[] strArgs) {
		Log.d("DEBUG", "ScheduleDatabaseHelper getChangeScheduleCount Start");
		String sql = objContext.getString(R.string.sqlite_schedule_select7);
		SQLiteDatabase sdObj = getReadableDatabase();
		ScheduleCursor cObj = (ScheduleCursor) sdObj.rawQueryWithFactory(
					 new ScheduleCursor.Factory(),
					 sql,
					 strArgs,
					 null);
		cObj.moveToFirst();
		sdObj.close();
		sdObj = null;
		Log.d("DEBUG", "ScheduleDatabaseHelper getChangeScheduleCount End");
		return cObj;
	}

	/**
	 * スケジュール登録処理
	 * 
	 * @param objSchedule    Schedule Bean
	 */
	public boolean insertSchedule(Schedule objSchedule) {
		Log.d("DEBUG", "ScheduleDatabaseHelper insertSchedule Start");
		boolean blResult = true;
		//データベースをオープン
		SQLiteDatabase sdObj = getWritableDatabase();
		//トランザクション処理開始
		sdObj.beginTransaction();
		try {
			//プリコンパイルステートメントの使用
			SQLiteStatement stmt = sdObj.compileStatement(objContext.getString(R.string.sqlite_schedule_insert1));
			//DELETED
			stmt.bindLong(1, objSchedule.getLgDeleteFlag());
			//MODIFIED
			stmt.bindLong(2, objSchedule.getLgModified());
			//TITLE
			stmt.bindString(3, objSchedule.getStrTitle());
			//CONTENT
			stmt.bindString(4, objSchedule.getStrContent());
			//GD_WHERE
			stmt.bindString(5, objSchedule.getStrGDWhere());
			//GD_WHEN_ENDTIME
			stmt.bindString(6, objSchedule.getStrGDWhenEndTime());
			//GD_WHEN_STARTTIME
			stmt.bindString(7, objSchedule.getStrGDWhenStartTime());
			//UPDATED
			stmt.bindString(8, objSchedule.getStrUpdated());
			//ENDTIME
			stmt.bindLong(9, objSchedule.getLgEndTime());
			//STARTTIME
			stmt.bindLong(10, objSchedule.getLgStartTime());
			//ALARM_FLAG
			stmt.bindLong(11, objSchedule.getLgAlarmFlag());
			//データ登録処理
			stmt.executeInsert();
			//コミット処理
			sdObj.setTransactionSuccessful();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ScheduleDatabaseHelper insertSchedule DBWriteError",e);
		} finally {
			//トランザクションの終了
			sdObj.endTransaction();
			sdObj.close();
			sdObj = null;
		}
		Log.d("DEBUG", "ScheduleDatabaseHelper insertSchedule End");
		return blResult;
	}

	/**
	 * スケジュール更新処理
	 * 
	 * @param objSchedule    Schedule Bean
	 */
	public boolean updateSchedule(Schedule objSchedule) {
		Log.d("DEBUG", "ScheduleDatabaseHelper updateSchedule Start");
		boolean blResult = true;
		//データベースをオープン
		SQLiteDatabase sdObj = getWritableDatabase();
		//トランザクション処理開始
		sdObj.beginTransaction();
		try {
			//プリコンパイルステートメントの使用
			SQLiteStatement stmt = sdObj.compileStatement(objContext.getString(R.string.sqlite_schedule_update1));
			//DELETED
			stmt.bindLong(1, objSchedule.getLgDeleteFlag());
			//MODIFIED
			stmt.bindLong(2, objSchedule.getLgModified());
			//TITLE
			stmt.bindString(3, objSchedule.getStrTitle());
			//CONTENT
			stmt.bindString(4, objSchedule.getStrContent());
			//GD_WHERE
			stmt.bindString(5, objSchedule.getStrGDWhere());
			//GD_WHEN_ENDTIME
			stmt.bindString(6, objSchedule.getStrGDWhenEndTime());
			//GD_WHEN_STARTTIME
			stmt.bindString(7, objSchedule.getStrGDWhenStartTime());
			//UPDATED
			stmt.bindString(8, objSchedule.getStrUpdated());
			//ENDTIME
			stmt.bindLong(9, objSchedule.getLgEndTime());
			//STARTTIME
			stmt.bindLong(10, objSchedule.getLgStartTime());
			//ALARM_FLAG
			stmt.bindLong(11, objSchedule.getLgAlarmFlag());
			//ID
			stmt.bindLong(12, objSchedule.getLgId());
			//データ更新処理
			stmt.executeInsert();
			//コミット処理
			sdObj.setTransactionSuccessful();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ScheduleDatabaseHelper updateSchedule DBWriteError",e);
		} finally {
			//トランザクションの終了
			sdObj.endTransaction();
			sdObj.close();
			sdObj = null;
		}
		Log.d("DEBUG", "ScheduleDatabaseHelper updateSchedule End");
		return blResult;
	}

	/**
	 * スケジュール論理削除処理
	 * 
	 * @param objSchedule    Schedule Bean
	 */
	public boolean updateScheduleDelete(Schedule objSchedule) {
		Log.d("DEBUG", "ScheduleDatabaseHelper updateScheduleDelete Start");
		boolean blResult = true;
		//データベースをオープン
		SQLiteDatabase sdObj = getWritableDatabase();
		//トランザクション処理開始
		sdObj.beginTransaction();
		try {
			//プリコンパイルステートメントの使用
			SQLiteStatement stmt = sdObj.compileStatement(objContext.getString(R.string.sqlite_schedule_update2));
			//DELETED
			stmt.bindLong(1, objSchedule.getLgDeleteFlag());
			//MODIFIED
			stmt.bindLong(2, objSchedule.getLgModified());
			//UPDATED
			stmt.bindString(3, objSchedule.getStrUpdated());
			//ID
			stmt.bindLong(4, objSchedule.getLgId());
			//データ更新処理(論理削除)
			stmt.executeInsert();
			//コミット処理
			sdObj.setTransactionSuccessful();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ScheduleDatabaseHelper updateScheduleDelete DBWriteError",e);
		} finally {
			//トランザクションの終了
			sdObj.endTransaction();
			sdObj.close();
			sdObj = null;
		}
		Log.d("DEBUG", "ScheduleDatabaseHelper updateScheduleDelete End");
		return blResult;
	}

	/**
	 * スケジュール物理削除処理
	 * 
	 * @param objSchedule    Schedule Bean
	 */
	public boolean deleteScheduleCalendarId(Schedule objSchedule) {
		Log.d("DEBUG", "ScheduleDatabaseHelper deleteScheduleCalendarId Start");
		boolean blResult = true;
		//データベースをオープン
		SQLiteDatabase sdObj = getWritableDatabase();
		//トランザクション処理開始
		sdObj.beginTransaction();
		try {
			//プリコンパイルステートメントの使用
			SQLiteStatement stmt = sdObj.compileStatement(objContext.getString(R.string.sqlite_schedule_delete1));
			//CALENDAR_ID
			stmt.bindString(1, objSchedule.getStrCalendarId());
			//データ削除処理
			stmt.execute();
			//コミット処理
			sdObj.setTransactionSuccessful();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ScheduleDatabaseHelper deleteScheduleCalendarId DBWriteError",e);
		} finally {
			//トランザクションの終了
			sdObj.endTransaction();
			sdObj.close();
			sdObj = null;
		}
		Log.d("DEBUG", "ScheduleDatabaseHelper deleteScheduleCalendarId End");
		return blResult;
	}

	/**
	 * スケジュール物理削除処理
	 * 
	 * @param objSchedule    Schedule Bean
	 */
	public boolean deleteScheduleId(Schedule objSchedule) {
		Log.d("DEBUG", "ScheduleDatabaseHelper deleteScheduleId Start");
		boolean blResult = true;
		//データベースをオープン
		SQLiteDatabase sdObj = getWritableDatabase();
		//トランザクション処理開始
		sdObj.beginTransaction();
		try {
			//プリコンパイルステートメントの使用
			SQLiteStatement stmt = sdObj.compileStatement(objContext.getString(R.string.sqlite_schedule_delete2));
			//ID
			stmt.bindLong(1, objSchedule.getLgId());
			//データ削除処理
			stmt.execute();
			//コミット処理
			sdObj.setTransactionSuccessful();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ScheduleDatabaseHelper deleteScheduleId DBWriteError",e);
		} finally {
			//トランザクションの終了
			sdObj.endTransaction();
			sdObj.close();
			sdObj = null;
		}
		Log.d("DEBUG", "ScheduleDatabaseHelper deleteScheduleId End");
		return blResult;
	}

	/**
	 * スケジュール更新処理
	 * 
	 * @param objSchedule    Schedule Bean
	 */
	public boolean updateScheduleEventIdFull(Schedule objSchedule) {
		Log.d("DEBUG", "ScheduleDatabaseHelper updateScheduleEventIdFull Start");
		boolean blResult = true;
		//データベースをオープン
		SQLiteDatabase sdObj = getWritableDatabase();
		//トランザクション処理開始
		sdObj.beginTransaction();
		try {
			//プリコンパイルステートメントの使用
			SQLiteStatement stmt = sdObj.compileStatement(objContext.getString(R.string.sqlite_schedule_update3));
			//DELETED
			stmt.bindLong(1, objSchedule.getLgDeleteFlag());
			//MODIFIED
			stmt.bindLong(2, objSchedule.getLgModified());
			//ALARM
			stmt.bindLong(3, objSchedule.getLgAlarm());
			//ALARM_LIST
			stmt.bindString(4, objSchedule.getStrAlarmList());
			//TITLE
			stmt.bindString(5, objSchedule.getStrTitle());
			//CONTENT
			stmt.bindString(6, objSchedule.getStrContent());
			//GD_WHERE
			stmt.bindString(7, objSchedule.getStrGDWhere());
			//GD_WHEN_ENDTIME
			stmt.bindString(8, objSchedule.getStrGDWhenEndTime());
			//GD_WHEN_STARTTIME
			stmt.bindString(9, objSchedule.getStrGDWhenStartTime());
			//PUBLISHED
			stmt.bindString(10, objSchedule.getStrPublished());
			//UPDATED
			stmt.bindString(11, objSchedule.getStrUpdated());
			//CATEGORY
			stmt.bindString(12, objSchedule.getStrCategory());
			//EDIT_URL
			stmt.bindString(13, objSchedule.getStrEditUrl());
			//GD_EVENTSTATUS
			stmt.bindString(14, objSchedule.getStrGDEventStatus());
			//ETAG
			stmt.bindString(15, objSchedule.getStrEtag());
			//ENDTIME
			stmt.bindLong(16, objSchedule.getLgEndTime());
			//STARTTIME
			stmt.bindLong(17, objSchedule.getLgStartTime());
			//CALENDAR_ID
			stmt.bindString(18, objSchedule.getStrCalendarId());

			//データ更新処理
			stmt.executeInsert();
			//コミット処理
			sdObj.setTransactionSuccessful();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ScheduleDatabaseHelper updateScheduleEventIdFull DBWriteError",e);
		} finally {
			//トランザクションの終了
			sdObj.endTransaction();
			sdObj.close();
			sdObj = null;
		}
		Log.d("DEBUG", "ScheduleDatabaseHelper updateScheduleEventIdFull End");
		return blResult;
	}

	/**
	 * スケジュール更新処理
	 * 
	 * @param objSchedule    Schedule Bean
	 */
	public boolean updateScheduleChangeModified(Schedule objSchedule) {
		Log.d("DEBUG", "ScheduleDatabaseHelper updateScheduleChangeModified Start");
		boolean blResult = true;
		//データベースをオープン
		SQLiteDatabase sdObj = getWritableDatabase();
		//トランザクション処理開始
		sdObj.beginTransaction();
		try {
			//プリコンパイルステートメントの使用
			SQLiteStatement stmt = sdObj.compileStatement(objContext.getString(R.string.sqlite_schedule_update4));
			//MODIFIED
			stmt.bindLong(1, objSchedule.getLgModified());
			//UPDATED
			stmt.bindString(2, objSchedule.getStrUpdated());
			//CALENDAR_ID
			stmt.bindString(3, objSchedule.getStrCalendarId());
			//データ更新処理
			stmt.executeInsert();
			//コミット処理
			sdObj.setTransactionSuccessful();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ScheduleDatabaseHelper updateScheduleChangeModified DBWriteError",e);
		} finally {
			//トランザクションの終了
			sdObj.endTransaction();
			sdObj.close();
			sdObj = null;
		}
		Log.d("DEBUG", "ScheduleDatabaseHelper updateScheduleChangeModified End");
		return blResult;
	}

	/**
	 * スケジュール登録処理
	 * 
	 * @param objSchedule    Schedule Bean
	 */
	public boolean insertScheduleEventIdFull(Schedule objSchedule) {
		Log.d("DEBUG", "ScheduleDatabaseHelper insertScheduleEventIdFull Start");
		boolean blResult = true;
		//データベースをオープン
		SQLiteDatabase sdObj = getWritableDatabase();
		//トランザクション処理開始
		sdObj.beginTransaction();
		try {
			//プリコンパイルステートメントの使用
			SQLiteStatement stmt = sdObj.compileStatement(objContext.getString(R.string.sqlite_schedule_insert2));
			//DELETED
			stmt.bindLong(1, objSchedule.getLgDeleteFlag());
			//MODIFIED
			stmt.bindLong(2, objSchedule.getLgModified());
			//ALARM
			stmt.bindLong(3, objSchedule.getLgAlarm());
			//ALARM_LIST
			stmt.bindString(4, objSchedule.getStrAlarmList());
			//TITLE
			stmt.bindString(5, objSchedule.getStrTitle());
			//CONTENT
			stmt.bindString(6, objSchedule.getStrContent());
			//GD_WHERE
			stmt.bindString(7, objSchedule.getStrGDWhere());
			//GD_WHEN_ENDTIME
			stmt.bindString(8, objSchedule.getStrGDWhenEndTime());
			//GD_WHEN_STARTTIME
			stmt.bindString(9, objSchedule.getStrGDWhenStartTime());
			//PUBLISHED
			stmt.bindString(10, objSchedule.getStrPublished());
			//UPDATED
			stmt.bindString(11, objSchedule.getStrUpdated());
			//CATEGORY
			stmt.bindString(12, objSchedule.getStrCategory());
			//EDIT_URL
			stmt.bindString(13, objSchedule.getStrEditUrl());
			//GD_EVENTSTATUS
			stmt.bindString(14, objSchedule.getStrGDEventStatus());
			//CALENDAR_ID
			stmt.bindString(15, objSchedule.getStrCalendarId());
			//ETAG
			stmt.bindString(16, objSchedule.getStrEtag());
			//ENDTIME
			stmt.bindLong(17, objSchedule.getLgEndTime());
			//STARTTIME
			stmt.bindLong(18, objSchedule.getLgStartTime());
			//ALARM_FLAG
			stmt.bindLong(19, objSchedule.getLgAlarmFlag());

			//データ登録処理
			stmt.executeInsert();
			//コミット処理
			sdObj.setTransactionSuccessful();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ScheduleDatabaseHelper insertScheduleEventIdFull DBWriteError",e);
		} finally {
			//トランザクションの終了
			sdObj.endTransaction();
			sdObj.close();
			sdObj = null;
		}
		Log.d("DEBUG", "ScheduleDatabaseHelper insertScheduleEventIdFull End");
		return blResult;
	}

	/**
	 * AuthInfoテーブルに対するクエリ固有のカーソルを提供
	 * ※クラス内にすべてのアクセサメソッドを含む
	 */
	public static class AuthInfoCursor extends SQLiteCursor {

		//カーソルコンストラクター
		private AuthInfoCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
			super(db, driver, editTable, query);
		}

		//rawQueryWithFactory()メソッド実行用のファクトリークラス
		private static class Factory implements SQLiteDatabase.CursorFactory{
			public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
				return new AuthInfoCursor(db, driver, editTable, query);
			}
		}

		//データベースカラム対になるアクセサ定義
		public String getColAccessToken() {
			return getString(getColumnIndexOrThrow("ACCESS_TOKEN"));
		}
		public long getColAccessTokenExpire() {
			return getLong(getColumnIndexOrThrow("ACCESS_TOKEN_EXPIRE"));
		}
		public String getColRefreshToken() {
			return getString(getColumnIndexOrThrow("REFRESH_TOKEN"));
		}
		public String getColLastUpdate() {
			return getString(getColumnIndexOrThrow("LASTUPDATE"));
		}
	}

	/** Auth情報取得処理
	 * @param strArgs SQLにバインドする文字列配列
	 */
	public AuthInfoCursor getAuthInfo(String[] strArgs) {
		Log.d("DEBUG", "ScheduleDatabaseHelper getAuthInfo Start");
		String sql = objContext.getString(R.string.sqlite_authinfo_select1);
		SQLiteDatabase sdObj = getReadableDatabase();
		AuthInfoCursor scObj = (AuthInfoCursor) sdObj.rawQueryWithFactory(
					 new AuthInfoCursor.Factory(),
					 sql,
					 strArgs,
					 null);
		scObj.moveToFirst();
		sdObj.close();
		sdObj = null;
		Log.d("DEBUG", "ScheduleDatabaseHelper getAuthInfo End");
		return scObj;
	}

	/**
	 * AuthInfo更新処理
	 * 
	 * @param objAuthInfo    AuthInfo Bean
	 */
	public boolean updateAuthInfoAccessToken(AuthInfo objAuthInfo) {
		Log.d("DEBUG", "ScheduleDatabaseHelper updateAuthInfoAccessToken Start");
		boolean blResult = true;
		//データベースをオープン
		SQLiteDatabase sdObj = getWritableDatabase();
		//トランザクション処理開始
		sdObj.beginTransaction();
		try {
			//プリコンパイルステートメントの使用
			SQLiteStatement stmt = sdObj.compileStatement(objContext.getString(R.string.sqlite_authinfo_update2));
			//ACCESS_TOKEN
			stmt.bindString(1, objAuthInfo.getStrAccessToken());
			//ACCESS_TOKEN_EXPIRE
			stmt.bindLong(2, objAuthInfo.getLgAccessTokenExpire());
			//データ更新処理
			stmt.executeInsert();
			//コミット処理
			sdObj.setTransactionSuccessful();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ScheduleDatabaseHelper updateAuthInfoAccessToken DBWriteError",e);
		} finally {
			//トランザクションの終了
			sdObj.endTransaction();
			sdObj.close();
			sdObj = null;
		}
		Log.d("DEBUG", "ScheduleDatabaseHelper updateAuthInfoAccessToken End");
		return blResult;
	}

	/**
	 * AuthInfo更新処理
	 * 
	 * @param objAuthInfo    AuthInfo Bean
	 */
	public boolean updateAuthInfoRefreshToken(AuthInfo objAuthInfo) {
		Log.d("DEBUG", "ScheduleDatabaseHelper updateAuthInfoRefreshToken Start");
		boolean blResult = true;
		//データベースをオープン
		SQLiteDatabase sdObj = getWritableDatabase();
		//トランザクション処理開始
		sdObj.beginTransaction();
		try {
			//プリコンパイルステートメントの使用
			SQLiteStatement stmt = sdObj.compileStatement(objContext.getString(R.string.sqlite_authinfo_update1));
			//ACCESS_TOKEN
			stmt.bindString(1, objAuthInfo.getStrAccessToken());
			//ACCESS_TOKEN_EXPIRE
			stmt.bindLong(2, objAuthInfo.getLgAccessTokenExpire());
			//REFRESH_TOKEN
			stmt.bindString(3, objAuthInfo.getStrRefreshToken());
			//データ更新処理
			stmt.executeInsert();
			//コミット処理
			sdObj.setTransactionSuccessful();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ScheduleDatabaseHelper updateAuthInfoRefreshToken DBWriteError",e);
		} finally {
			//トランザクションの終了
			sdObj.endTransaction();
			sdObj.close();
			sdObj = null;
		}
		Log.d("DEBUG", "ScheduleDatabaseHelper updateAuthInfoRefreshToken End");
		return blResult;
	}

	/**
	 * AuthInfo更新処理
	 * 
	 * @param objAuthInfo    AuthInfo Bean
	 */
	public boolean updateAuthInfoLastUpdate(AuthInfo objAuthInfo) {
		Log.d("DEBUG", "ScheduleDatabaseHelper updateAuthInfoLastUpdate Start");
		boolean blResult = true;
		//データベースをオープン
		SQLiteDatabase sdObj = getWritableDatabase();
		//トランザクション処理開始
		sdObj.beginTransaction();
		try {
			//プリコンパイルステートメントの使用
			SQLiteStatement stmt = sdObj.compileStatement(objContext.getString(R.string.sqlite_authinfo_update3));
			//LASTUPDATE
			stmt.bindString(1, objAuthInfo.getStrLastUpdate());
			//データ更新処理
			stmt.executeInsert();
			//コミット処理
			sdObj.setTransactionSuccessful();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ScheduleDatabaseHelper updateAuthInfoLastUpdate DBWriteError",e);
		} finally {
			//トランザクションの終了
			sdObj.endTransaction();
			sdObj.close();
			sdObj = null;
		}
		Log.d("DEBUG", "ScheduleDatabaseHelper updateAuthInfoLastUpdate End");
		return blResult;
	}

}