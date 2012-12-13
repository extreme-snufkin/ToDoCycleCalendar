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


 クラス名：ToDoDatabaseHelper
 内容：SQLite(todo.db)ファイルの
       作成モジュール
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
          0.1/2012.05.21/T.Mashiko
          0.2/2012.05.23/T.Mashiko
          0.3/2012.05.31/T.Mashiko
          0.4/2012.06.01/T.Mashiko
          0.5/2012.06.04/T.Mashiko
          0.6/2012.06.05/T.Mashiko
          0.7/2012.06.06/T.Mashiko
          0.8/2012.06.12/T.Mashiko
          0.9/2012.06.20/T.Mashiko
          1.0/2012.07.10/T.Mashiko
          1.1/2012.07.13/T.Mashiko
          1.2/2012.07.14/T.Mashiko
          1.3/2012.07.15/T.Mashiko
          1.4/2012.07.17/T.Mashiko コメント修正
          1.5/2012.08.25/T.Mashiko 繰越機能時の件数チェック処理
          1.6/2012.08.26/T.Mashiko 繰越機能時のSQLITE_BUSY対策
*/
package study.tdcc.lib;

import java.util.ArrayList;
import java.util.Iterator;

import study.tdcc.*;
import study.tdcc.bean.Subcategory;
import study.tdcc.bean.ToDo;
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

public class ToDoDatabaseHelper extends SQLiteOpenHelper {
	private Context objContext;
	private static final int DATABASE_VERSION = 1;

	//データベース生成
	public ToDoDatabaseHelper(Context context,String dbname) {
		//ストレージ(ローカルファイル)にDBを作成
		super(context, dbname, null, DATABASE_VERSION);
		objContext = context;
	}

	/**
	 * 文字列配列内の全てのSQLを実行
	 * @param db DBオブジェクト
	 * @param sql SQLが格納された文字列配列
	 */
	private void execMultipleSQL(SQLiteDatabase db, String[] sql){
		Log.d("DEBUG", "ToDoDatabaseHelper execMultipleSQL Start");
		for( String strObj : sql ) {
			if (strObj.trim().length()>0) {
				db.execSQL(strObj);
			}
		}
		Log.d("DEBUG", "ToDoDatabaseHelper execMultipleSQL End");
	}

	/** データベースの作成時に実行されるメソッド */
	@Override
	public void onCreate(SQLiteDatabase db) throws SQLException {
		Log.d("DEBUG", "ToDoDatabaseHelper onCreate Start");
		//スケジュールテーブル
		String[] sql = objContext.getString(R.string.sqlite_todo_create).split("\n");
		//トランザクション開始
		db.beginTransaction();
		try {
			//Create tables & Initial data
			execMultipleSQL(db, sql);
			//トランザクションコミット
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e("ERROR","ToDoDatabaseHelper onCreate",e);
			throw new SQLException();
		} finally {
			//トランザクションロールバック
			db.endTransaction();
		}
		Log.d("DEBUG", "ToDoDatabaseHelper onCreate End");
	}

	/** データベースのアップグレード時に実行されるメソッド */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d("DEBUG", "ToDoDatabaseHelper onUpgrade Start");
		Log.d("DEBUG", "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		//スケジュールテーブル
		String[] sql = objContext.getString(R.string.sqlite_todo_upgrade).split("\n");
		//トランザクション開始
		db.beginTransaction();
		try {
			//Create tables & Initial data
			execMultipleSQL(db, sql);
			//トランザクションコミット
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e("ERROR","ToDoDatabaseHelper onUpgrade",e);
			throw new SQLException();
		} finally {
			//トランザクションロールバック
			db.endTransaction();
		}
		//This is cheating.  In the real world, you'll need to add columns,
		//not rebuild from scratch
		onCreate(db);
		Log.d("DEBUG", "ToDoDatabaseHelper onUpgrade End");
	}

	/**
	 * ToDoテーブルに対するクエリ固有のカーソルを提供
	 * ※クラス内にすべてのアクセサメソッドを含む
	 */
	public static class ToDoCursor extends SQLiteCursor {

		//カーソルコンストラクター
		private ToDoCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
			super(db, driver, editTable, query);
		}

		//rawQueryWithFactory()メソッド実行用のファクトリークラス
		private static class Factory implements SQLiteDatabase.CursorFactory{
			public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
				return new ToDoCursor(db, driver, editTable, query);
			}
		}

		//データベースカラム対になるアクセサ定義
		public long getColTdid() {
			return getLong(getColumnIndexOrThrow("TDID"));
		}
		public long getColDate() {
			return getLong(getColumnIndexOrThrow("DATE"));
		}
		public String getColTitle() {
			return getString(getColumnIndexOrThrow("TITLE"));
		}
		public String getColPriorityCode() {
			return getString(getColumnIndexOrThrow("PRIORITY_CODE"));
		}
		public String getColTAT() {
			return getString(getColumnIndexOrThrow("TAT"));
		}
		public String getColCategoryCode() {
			return getString(getColumnIndexOrThrow("CATEGORY_CODE"));
		}
		public String getColSubcategoryCode() {
			return getString(getColumnIndexOrThrow("SUBCATEGORY_CODE"));
		}
		public long getColStatus() {
			return getLong(getColumnIndexOrThrow("STATUS"));
		}
		public String getColDetail() {
			return getString(getColumnIndexOrThrow("DETAIL"));
		}
		public String getColSTAT() {
			return getString(getColumnIndexOrThrow("STAT"));
		}
		public String getColPriorityName() {
			return getString(getColumnIndexOrThrow("PRIORITY_NAME"));
		}
		public String getColCategoryName() {
			return getString(getColumnIndexOrThrow("CATEGORY_NAME"));
		}
		public String getColSubcategoryName() {
			return getString(getColumnIndexOrThrow("SUBCATEGORY_NAME"));
		}
	}

	/** ToDoリスト取得処理
	 * @param strArgs SQLにバインドする文字列配列
	 */
	public ToDoCursor getToDoList(String[] strArgs) {
		Log.d("DEBUG", "ToDoDatabaseHelper getToDoList Start");
		String sql = objContext.getString(R.string.sqlite_todo_select1);
		SQLiteDatabase sdObj = getReadableDatabase();
		ToDoCursor cObj = (ToDoCursor) sdObj.rawQueryWithFactory(
					 new ToDoCursor.Factory(),
					 sql,
					 strArgs,
					 null);
		cObj.moveToFirst();
		sdObj.close();
		sdObj = null;
		Log.d("DEBUG", "ToDoDatabaseHelper getToDoList End");
		return cObj;
	}

	/** ToDo取得処理
	 * @param strArgs SQLにバインドする文字列配列
	 */
	public ToDoCursor getToDo(String[] strArgs) {
		Log.d("DEBUG", "ToDoDatabaseHelper getToDo Start");
		String sql = objContext.getString(R.string.sqlite_todo_select2);
		SQLiteDatabase sdObj = getReadableDatabase();
		ToDoCursor cObj = (ToDoCursor) sdObj.rawQueryWithFactory(
					 new ToDoCursor.Factory(),
					 sql,
					 strArgs,
					 null);
		cObj.moveToFirst();
		sdObj.close();
		sdObj = null;
		Log.d("DEBUG", "ToDoDatabaseHelper getToDo End");
		return cObj;
	}

	/** 未完了ToDoリスト取得処理
	 * @param strArgs SQLにバインドする文字列配列
	 */
	public ToDoCursor getUnfinishedToDoList(String[] strArgs) {
		Log.d("DEBUG", "ToDoDatabaseHelper getUnfinishedToDoList Start");
		String sql = objContext.getString(R.string.sqlite_todo_select3);
		SQLiteDatabase sdObj = getReadableDatabase();
		ToDoCursor cObj = (ToDoCursor) sdObj.rawQueryWithFactory(
					 new ToDoCursor.Factory(),
					 sql,
					 strArgs,
					 null);
		cObj.moveToFirst();
		sdObj.close();
		sdObj = null;
		Log.d("DEBUG", "ToDoDatabaseHelper getUnfinishedToDoList End");
		return cObj;
	}

	/** 日次集計単位のToDoリスト取得処理
	 * @param strArgs SQLにバインドする文字列配列
	 */
	public ToDoCursor getToDoDailySTAT(String[] strArgs) {
		Log.d("DEBUG", "ToDoDatabaseHelper getToDoDailySTAT Start");
		String sql = objContext.getString(R.string.sqlite_todo_select4);
		SQLiteDatabase sdObj = getReadableDatabase();
		ToDoCursor cObj = (ToDoCursor) sdObj.rawQueryWithFactory(
					 new ToDoCursor.Factory(),
					 sql,
					 strArgs,
					 null);
		cObj.moveToFirst();
		sdObj.close();
		sdObj = null;
		Log.d("DEBUG", "ToDoDatabaseHelper getToDoDailySTAT End");
		return cObj;
	}

	/** ToDo検索結果取得処理
	 * @param strArgs SQLにバインドする文字列配列
	 */
	public ToDoCursor getToDoSearchResult(String[] strArgs) {
		Log.d("DEBUG", "ToDoDatabaseHelper getToDoSearchResult Start");
		String sql = objContext.getString(R.string.sqlite_todo_select5);
		SQLiteDatabase sdObj = getReadableDatabase();
		ToDoCursor cObj = (ToDoCursor) sdObj.rawQueryWithFactory(
					 new ToDoCursor.Factory(),
					 sql,
					 strArgs,
					 null);
		cObj.moveToFirst();
		sdObj.close();
		sdObj = null;
		Log.d("DEBUG", "ToDoDatabaseHelper getToDoSearchResult End");
		return cObj;
	}

	/** ToDo所要時間取得処理
	 * @param strArgs SQLにバインドする文字列配列
	 */
	public ToDoCursor getToDoTAT(String[] strArgs) {
		Log.d("DEBUG", "ToDoDatabaseHelper getToDoTAT Start");
		String sql = objContext.getString(R.string.sqlite_todo_select6);
		SQLiteDatabase sdObj = getReadableDatabase();
		ToDoCursor cObj = (ToDoCursor) sdObj.rawQueryWithFactory(
					 new ToDoCursor.Factory(),
					 sql,
					 strArgs,
					 null);
		cObj.moveToFirst();
		sdObj.close();
		sdObj = null;
		Log.d("DEBUG", "ToDoDatabaseHelper getToDoTAT End");
		return cObj;
	}

	/** 共有機能向けToDoリスト取得処理
	 * @param strArgs SQLにバインドする文字列配列
	 */
	public ToDoCursor getToDoShareList(String[] strArgs) {
		Log.d("DEBUG", "ToDoDatabaseHelper getToDoShareList Start");
		String sql = objContext.getString(R.string.sqlite_todo_select7);
		SQLiteDatabase sdObj = getReadableDatabase();
		ToDoCursor cObj = (ToDoCursor) sdObj.rawQueryWithFactory(
					 new ToDoCursor.Factory(),
					 sql,
					 strArgs,
					 null);
		cObj.moveToFirst();
		sdObj.close();
		sdObj = null;
		Log.d("DEBUG", "ToDoDatabaseHelper getToDoShareList End");
		return cObj;
	}

	/** 繰越機能向けToDoリスト件数取得処理
	 * @param strArgs SQLにバインドする文字列配列
	 */
	public ToDoCursor getToDoListCount(String[] strArgs) {
		Log.d("DEBUG", "ToDoDatabaseHelper getToDoListCount Start");
		String sql = objContext.getString(R.string.sqlite_todo_select8);
		SQLiteDatabase sdObj = getReadableDatabase();
		ToDoCursor cObj = (ToDoCursor) sdObj.rawQueryWithFactory(
					 new ToDoCursor.Factory(),
					 sql,
					 strArgs,
					 null);
		cObj.moveToFirst();
		sdObj.close();
		sdObj = null;
		Log.d("DEBUG", "ToDoDatabaseHelper getToDoListCount End");
		return cObj;
	}

	/**
	 * Priorityテーブルに対するクエリ固有のカーソルを提供
	 * ※クラス内にすべてのアクセサメソッドを含む
	 */
	public static class PriorityCursor extends SQLiteCursor {

		//カーソルコンストラクター
		private PriorityCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
			super(db, driver, editTable, query);
		}

		//rawQueryWithFactory()メソッド実行用のファクトリークラス
		private static class Factory implements SQLiteDatabase.CursorFactory{
			public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
				return new PriorityCursor(db, driver, editTable, query);
			}
		}

		//データベースカラム対になるアクセサ定義
		public String getColCode() {
			return getString(getColumnIndexOrThrow("CODE"));
		}
		public String getColName() {
			return getString(getColumnIndexOrThrow("NAME"));
		}
	}

	/** 優先順位取得処理
	 * @param strArgs SQLにバインドする文字列配列
	 */
	public PriorityCursor getPriority(String[] strArgs) {
		Log.d("DEBUG", "ToDoDatabaseHelper getPriority Start");
		String sql = objContext.getString(R.string.sqlite_priority_select1);
		SQLiteDatabase sdObj = getReadableDatabase();
		PriorityCursor pObj = (PriorityCursor) sdObj.rawQueryWithFactory(
					 new PriorityCursor.Factory(),
					 sql,
					 strArgs,
					 null);
		pObj.moveToFirst();
		sdObj.close();
		sdObj = null;
		Log.d("DEBUG", "ToDoDatabaseHelper getPriority End");
		return pObj;
	}

	/**
	 * Categoryテーブルに対するクエリ固有のカーソルを提供
	 * ※クラス内にすべてのアクセサメソッドを含む
	 */
	public static class CategoryCursor extends SQLiteCursor {

		//カーソルコンストラクター
		private CategoryCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
			super(db, driver, editTable, query);
		}

		//rawQueryWithFactory()メソッド実行用のファクトリークラス
		private static class Factory implements SQLiteDatabase.CursorFactory{
			public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
				return new CategoryCursor(db, driver, editTable, query);
			}
		}

		//データベースカラム対になるアクセサ定義
		public String getColCode() {
			return getString(getColumnIndexOrThrow("CODE"));
		}
		public String getColName() {
			return getString(getColumnIndexOrThrow("NAME"));
		}
	}

	/** カテゴリ取得処理
	 * @param strArgs SQLにバインドする文字列配列
	 */
	public CategoryCursor getCategory(String[] strArgs) {
		Log.d("DEBUG", "ToDoDatabaseHelper getCategory Start");
		String sql = objContext.getString(R.string.sqlite_category_select1);
		SQLiteDatabase sdObj = getReadableDatabase();
		CategoryCursor cObj = (CategoryCursor) sdObj.rawQueryWithFactory(
					 new CategoryCursor.Factory(),
					 sql,
					 strArgs,
					 null);
		cObj.moveToFirst();
		sdObj.close();
		sdObj = null;
		Log.d("DEBUG", "ToDoDatabaseHelper getCategory End");
		return cObj;
	}

	/**
	 * Subcategoryテーブルに対するクエリ固有のカーソルを提供
	 * ※クラス内にすべてのアクセサメソッドを含む
	 */
	public static class SubcategoryCursor extends SQLiteCursor {

		//カーソルコンストラクター
		private SubcategoryCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
			super(db, driver, editTable, query);
		}

		//rawQueryWithFactory()メソッド実行用のファクトリークラス
		private static class Factory implements SQLiteDatabase.CursorFactory{
			public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
				return new SubcategoryCursor(db, driver, editTable, query);
			}
		}

		//データベースカラム対になるアクセサ定義
		public String getColPcode() {
			return getString(getColumnIndexOrThrow("PCODE"));
		}
		public String getColCcode() {
			return getString(getColumnIndexOrThrow("CCODE"));
		}
		public String getColName() {
			return getString(getColumnIndexOrThrow("NAME"));
		}
	}

	/** サブカテゴリ取得処理
	 * @param strArgs SQLにバインドする文字列配列
	 */
	public SubcategoryCursor getSubcategory(String[] strArgs) {
		Log.d("DEBUG", "ToDoDatabaseHelper getSubcategory Start");
		String sql = objContext.getString(R.string.sqlite_subcategory_select1);
		SQLiteDatabase sdObj = getReadableDatabase();
		SubcategoryCursor scObj = (SubcategoryCursor) sdObj.rawQueryWithFactory(
					 new SubcategoryCursor.Factory(),
					 sql,
					 strArgs,
					 null);
		scObj.moveToFirst();
		sdObj.close();
		sdObj = null;
		Log.d("DEBUG", "ToDoDatabaseHelper getSubcategory End");
		return scObj;
	}

	/**
	 * サブカテゴリ名更新処理
	 * @param objSubcategory    Subcategory Bean
	 */
	public boolean updateSubcategoryName(Subcategory objSubcategory) {
		Log.d("DEBUG", "ToDoDatabaseHelper updateSubcategoryName Start");
		boolean blResult = true;
		//データベースをオープン
		SQLiteDatabase sdObj = getWritableDatabase();
		//トランザクション処理開始
		sdObj.beginTransaction();
		try {
			//プリコンパイルステートメントの使用
			SQLiteStatement stmt = sdObj.compileStatement(objContext.getString(R.string.sqlite_subcategory_update1));
			//NAME
			stmt.bindString(1, objSubcategory.getStrName());
			//PCODE
			stmt.bindString(2, objSubcategory.getStrPCode());
			//CCODE
			stmt.bindString(3, objSubcategory.getStrCCode());
			//データ更新処理
			stmt.executeInsert();
			//コミット処理
			sdObj.setTransactionSuccessful();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ToDoDatabaseHelper updateSubcategoryName DBWriteError",e);
		} finally {
			//トランザクションの終了
			sdObj.endTransaction();
			sdObj.close();
			sdObj = null;
		}
		Log.d("DEBUG", "ToDoDatabaseHelper updateSubcategoryName End");
		return blResult;
	}

	/**
	 * サブカテゴリ更新処理
	 * @param alSubcategory   Subcategory BeanのArrayList
	 */
	public boolean updateAfterSubcategory(ArrayList<Subcategory> alSubcategory) {
		Log.d("DEBUG", "ToDoDatabaseHelper updateAfterSubcategory Start");
		boolean blResult = true;
		//データベースをオープン
		SQLiteDatabase sdObj = getWritableDatabase();
		//トランザクション処理開始
		sdObj.beginTransaction();
		try {
			for (Iterator<Subcategory> iSObj = alSubcategory.iterator(); iSObj.hasNext();) {
				Subcategory sObj = iSObj.next(); 
				//プリコンパイルステートメントの使用
				SQLiteStatement stmt = sdObj.compileStatement(objContext.getString(R.string.sqlite_subcategory_update1));
				//NAME
				stmt.bindString(1, sObj.getStrName());
				//PCODE
				stmt.bindString(2, sObj.getStrPCode());
				//CCODE
				stmt.bindString(3, sObj.getStrCCode());
				//データ更新処理
				stmt.executeInsert();
			}
			//コミット処理
			sdObj.setTransactionSuccessful();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ToDoDatabaseHelper updateAfterSubcategory DBWriteError",e);
		} finally {
			//トランザクションの終了
			sdObj.endTransaction();
			sdObj.close();
			sdObj = null;
		}
		Log.d("DEBUG", "ToDoDatabaseHelper updateAfterSubcategory End");
		return blResult;
	}

	/**
	 * ToDo登録処理
	 * @param objToDo    ToDo Bean
	 */
	public boolean insertToDo(ToDo objToDo) {
		Log.d("DEBUG", "ToDoDatabaseHelper insertToDo Start");
		boolean blResult = true;
		//データベースをオープン
		SQLiteDatabase sdObj = getWritableDatabase();
		//トランザクション処理開始
		sdObj.beginTransaction();
		try {
			//プリコンパイルステートメントの使用
			SQLiteStatement stmt = sdObj.compileStatement(objContext.getString(R.string.sqlite_todo_insert));
			//DATE
			stmt.bindLong(1, objToDo.getLgDate());
			//TITLE
			stmt.bindString(2, objToDo.getStrTitle());
			//PRIORITY_CODE
			stmt.bindString(3, objToDo.getStrPriorityCode());
			//TAT
			stmt.bindString(4, objToDo.getStrTAT());
			//CATEGORY_CODE
			stmt.bindString(5, objToDo.getStrCategoryCode());
			//SUBCATEGORY_CODE
			stmt.bindString(6, objToDo.getStrSubcategoryCode());
			//STATUS
			stmt.bindLong(7, objToDo.getLgStatus());
			//DETAIL
			stmt.bindString(8, objToDo.getStrDetail());
			//データ登録処理
			stmt.executeInsert();
			//コミット処理
			sdObj.setTransactionSuccessful();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ToDoDatabaseHelper insertToDo DBWriteError",e);
		} finally {
			//トランザクションの終了
			sdObj.endTransaction();
			sdObj.close();
			sdObj = null;
		}
		Log.d("DEBUG", "ToDoDatabaseHelper insertToDo End");
		return blResult;
	}

	/**
	 * ToDo削除処理
	 * @param objToDo    ToDo Bean
	 */
	public boolean deleteToDo(ToDo objToDo) {
		Log.d("DEBUG", "ToDoDatabaseHelper deleteToDo Start");
		boolean blResult = true;
		//データベースをオープン
		SQLiteDatabase sdObj = getWritableDatabase();
		//トランザクション処理開始
		sdObj.beginTransaction();
		try {
			//プリコンパイルステートメントの使用
			SQLiteStatement stmt = sdObj.compileStatement(objContext.getString(R.string.sqlite_todo_delete));
			//TDID
			stmt.bindLong(1, objToDo.getLgTdId());
			//データ削除処理
			stmt.execute();
			//コミット処理
			sdObj.setTransactionSuccessful();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ToDoDatabaseHelper deleteToDo DBWriteError",e);
		} finally {
			//トランザクションの終了
			sdObj.endTransaction();
			sdObj.close();
			sdObj = null;
		}
		Log.d("DEBUG", "ToDoDatabaseHelper deleteToDo End");
		return blResult;
	}

	/**
	 * ToDo更新処理
	 * @param objToDo    ToDo Bean
	 */
	public boolean updateToDo(ToDo objToDo) {
		Log.d("DEBUG", "ToDoDatabaseHelper updateToDo Start");
		boolean blResult = true;
		//データベースをオープン
		SQLiteDatabase sdObj = getWritableDatabase();
		//トランザクション処理開始
		sdObj.beginTransaction();
		try {
			//プリコンパイルステートメントの使用
			SQLiteStatement stmt = sdObj.compileStatement(objContext.getString(R.string.sqlite_todo_update));
			//TITLE
			stmt.bindString(1, objToDo.getStrTitle());
			//PRIORITY_CODE
			stmt.bindString(2, objToDo.getStrPriorityCode());
			//TAT
			stmt.bindString(3, objToDo.getStrTAT());
			//CATEGORY_CODE
			stmt.bindString(4, objToDo.getStrCategoryCode());
			//SUBCATEGORY_CODE
			stmt.bindString(5, objToDo.getStrSubcategoryCode());
			//STATUS
			stmt.bindLong(6, objToDo.getLgStatus());
			//DETAIL
			stmt.bindString(7, objToDo.getStrDetail());
			//TDID
			stmt.bindLong(8, objToDo.getLgTdId());
			//データ更新処理
			stmt.executeInsert();
			//コミット処理
			sdObj.setTransactionSuccessful();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ToDoDatabaseHelper updateToDo DBWriteError",e);
		} finally {
			//トランザクションの終了
			sdObj.endTransaction();
			sdObj.close();
			sdObj = null;
		}
		Log.d("DEBUG", "ToDoDatabaseHelper updateToDo End");
		return blResult;
	}

	/**
	 * 未完了ToDoリストの登録処理
	 * @param alToDo   ToDo BeanのArrayList
	 */
	public boolean insertUnfinishedTODOList(ArrayList<ToDo> alToDo) {
		Log.d("DEBUG", "ToDoDatabaseHelper insertUnfinishedTODOList Start");
		boolean blResult = true;
		//データベースをオープン
		SQLiteDatabase sdObj = getWritableDatabase();
		//トランザクション処理開始
		sdObj.beginTransaction();
		try {
			for (Iterator<ToDo> iTDObj = alToDo.iterator(); iTDObj.hasNext();) {
				ToDo tdObj = iTDObj.next(); 
				//プリコンパイルステートメントの使用
				SQLiteStatement stmt = sdObj.compileStatement(objContext.getString(R.string.sqlite_todo_insert));
				//DATE
				stmt.bindLong(1, tdObj.getLgDate());
				//TITLE
				stmt.bindString(2, tdObj.getStrTitle());
				//PRIORITY_CODE
				stmt.bindString(3, tdObj.getStrPriorityCode());
				//TAT
				stmt.bindString(4, tdObj.getStrTAT());
				//CATEGORY_CODE
				stmt.bindString(5, tdObj.getStrCategoryCode());
				//SUBCATEGORY_CODE
				stmt.bindString(6, tdObj.getStrSubcategoryCode());
				//STATUS
				stmt.bindLong(7, tdObj.getLgStatus());
				//DETAIL
				stmt.bindString(8, tdObj.getStrDetail());
				//データ登録処理
				stmt.executeInsert();
			}
			//コミット処理
			sdObj.setTransactionSuccessful();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ToDoDatabaseHelper insertUnfinishedTODOList DBWriteError",e);
		} finally {
			//トランザクションの終了
			sdObj.endTransaction();
			//android.database.sqlite.SQLiteException: unable to close due to unfinalised statementsエラー対策
			//SQLiteのCore C APIのsqlite3_close()実行時にプリペアードステートメントが全て終了していない状態であると
			//SQLITE_BUSYというリターンコードと上記と類似のメッセージを返すようだ。
			//Androidフレームワークを使用して千件単位のプリペアードステートメントを発行した際に
			//上記のExceptionが発生したが、Exceptionが発生してもロールバックが実行されずにコミットされている為、
			//ステートメントの件数を仕様で制限し、Exceptionを隠蔽する。
			if(sdObj != null) {
				try {
					sdObj.close();
				} catch(Exception e) {
					Log.e("ERROR", "ToDoDatabaseHelper insertUnfinishedTODOList SQLITE_BUSY", e);
				}
			}
			sdObj = null;
		}
		Log.d("DEBUG", "ToDoDatabaseHelper insertUnfinishedTODOList End");
		return blResult;
	}

}