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


 クラス名：AggregateCalculation
 内容：集計
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
          0.1/2012.05.28/T.Mashiko
          0.2/2012.05.29/T.Mashiko
          0.3/2012.06.06/T.Mashiko インテントLong型修正
          0.4/2012.06.08/T.Mashiko
          0.5/2012.06.09/T.Mashiko
          0.6/2012.06.10/T.Mashiko
          0.7/2012.06.11/T.Mashiko onDestroy修正
          0.8/2012.07.09/T.Mashiko カレンダー年月指定ダイアログ改修,年月範囲指定
          0.9/2012.07.12/T.Mashiko 集計リストビューの機能追加
          1.0/2012.07.13/T.Mashiko 集計リストビューの機能追加
          1.1/2012.07.14/T.Mashiko 共有機能の追加
          1.2/2012.07.18/T.Mashiko ロギング表記修正
          1.3/2012.08.22/T.Mashiko 対象年月の連打ロック対策,ダイアログの戻るキー対策
          1.4/2012.08.23/T.Mashiko 対象年月の連打ロック対策追加
          1.5/2012.09.01/T.Mashiko 合計時間の表示修正
          1.6/2012.09.05/T.Mashiko AsyncTaskのエラー処理の条件修正,非同期処理内にてUI操作(トースト)を実行していた為、修正対応
          1.7/2012.09.26/T.Mashiko 共有機能のデータ上限チェック追加
          1.8/2012.09.27/T.Mashiko 強制終了対策
*/
package study.tdcc.act;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import study.tdcc.*;
import study.tdcc.adapter.SummaryTableExpandableListItemAdapter;
import study.tdcc.bean.*;
import study.tdcc.lib.*;
import study.tdcc.lib.ToDoDatabaseHelper.SubcategoryCursor;
import study.tdcc.lib.ToDoDatabaseHelper.ToDoCursor;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;

public class AggregateCalculation extends Activity implements OnChildClickListener {
	//カスタムタイトルテキストビュー
	private TextView tvCustomTitle;
	//カスタムタイトル(バージョン)テキストビュー
	private TextView tvCustomTitleVersion;
	//対象年月テキストビュー
	private TextView tvTargetDate;
	//カテゴリスピナー
	private Spinner sCategory;
	//データベースオブジェクト
	private ToDoDatabaseHelper tdhDB;
	//画面遷移データオブジェクト
	private ScreenTransitionData stdObj = new ScreenTransitionData();
	//カテゴリアダプター
	ArrayAdapter<String> aaCategory;
	//カテゴリスピナーキー
	private HashMap<Integer, String> hmCategorySpinner;
	//カテゴリ名称マップ(共有機能使用)
	private HashMap<String, String> hmCategoryName;
	//集計ボタン連打防止フラグ
	private boolean blCalc = true;
	//ダイアログ内ボタン連打防止フラグ
	private boolean blDialogButton = true;
	//集計テーブルリストビュー
	private ExpandableListView elvSummaryTableList;
	//リストビュー用アダプタ
	private SummaryTableExpandableListItemAdapter steliaObj;
	//集計テーブル親リスト
	private List<SummaryTableListParentRow> lSTLPR;
	//集計テーブル子リスト
	private List<List<SummaryTableListChildRow>> llSTLCR;
	//プログレスダイアログのインスタンス
	private ProgressDialog pdObj = null;
	//共有処理後の処理を受け取るhandler
	private Handler hObj;
	//共有機能出力テキスト
	private StringBuffer sbAggregateResult ;
	//共有機能用集計テーブル親リスト
	private ArrayList<ShareSummaryTableListRow> alSSTLR;

	/**
	 * onCreate
	 * @param savedInstanceState バンドル
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("DEBUG", "AggregateCalculation onCreate Start");
		super.onCreate(savedInstanceState);
		//window がフォーカスを受けたときに常に soft input area を隠す
		this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		//カスタムタイトル
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		//集計画面描画処理
		setContentView(R.layout.aggregatecalculation);
		//カスタムタイトル描画処理
		Window wObj = getWindow();
		wObj.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.customtitle);
		//画面遷移情報の取得
		getScreenTransitionData();
		//画面要素の取得処理
		getViewElement();
		//カスタムタイトルの内容セット
		setCustomTitle();
		//画面要素へのデータセット
		setViewElement(savedInstanceState);
		//画面要素のリスナーセット
		setViewListener();
		//集計ボタン連打ロック解除
		blCalc = true;
		Log.d("DEBUG", "AggregateCalculation onCreate End");
	}

	/**
	 * 画面遷移情報の取得
	 *
	 */
	private void getScreenTransitionData() {
		Log.d("DEBUG", "AggregateCalculation getScreenTransitionData Start");
		//画面遷移時のインテント格納値取得
		Bundle bExtras = getIntent().getExtras();
		if (bExtras != null) {
			stdObj.setStrCalendarYearMonth(bExtras.getString("calym"));
			stdObj.setStrCalendarYearMonthDay(bExtras.getString("calymd"));
			stdObj.setStrUserInterfaceId(bExtras.getString("uiid"));
			stdObj.setLgKeyId(bExtras.getLong("keyid"));
		}
		Log.d("DEBUG", "AggregateCalculation getScreenTransitionData End");
	}

	/**
	 * 画面要素の取得
	 *
	 */
	private void getViewElement() {
		Log.d("DEBUG", "AggregateCalculation getViewElement Start");
		//カスタムタイトルテキストビュー
		tvCustomTitle = (TextView)this.findViewById(R.id.titletext);
		//カスタムタイトルバージョンビュー
		tvCustomTitleVersion = (TextView)this.findViewById(R.id.titleversion);
		//対象年月テキストビュー
		tvTargetDate = (TextView)this.findViewById(R.id.tvtargetyearmonth);
		//カテゴリスピナー
		sCategory = (Spinner)this.findViewById(R.id.scategory);
		//集計テーブルリストビュー
		elvSummaryTableList = (ExpandableListView)findViewById(R.id.spreadsheet);
		Log.d("DEBUG", "AggregateCalculation getViewElement End");
	}

	/**
	 * カスタムタイトルの内容セット
	 *
	 */
	private void setCustomTitle() {
		Log.d("DEBUG", "AggregateCalculation setCustomTitle Start");
		tvCustomTitle.setText(getString(R.string.act_name10));
		StringBuilder sbVersion = new StringBuilder();
		sbVersion.append(getString(R.string.title_version));
		PackageManager pmObj = this.getPackageManager();
		try {
			PackageInfo piObj = pmObj.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
			sbVersion.append(piObj.versionName);
		} catch (NameNotFoundException e) {
			Log.e("ERROR", "AggregateCalculation setCustomTitle NameNotFoundException", e);
		}
		tvCustomTitleVersion.setText(sbVersion.toString());
		Log.d("DEBUG", "AggregateCalculation setCustomTitle End");
	}

	/**
	 * 画面要素へのデータセット
	 *
	 * @param savedInstanceState バンドル
	 */
	private void setViewElement(Bundle savedInstanceState) {
		Log.d("DEBUG", "AggregateCalculation setViewElement Start");
		//指定年月を取得
		Calendar clTargetDate;
		if(savedInstanceState != null) {
			//復元情報から取得
			clTargetDate = DateUtil.toCalendar(savedInstanceState.getString("calym") + DateUtil.FIRST_DAY);
		} else {
			clTargetDate = DateUtil.toCalendar(stdObj.getStrCalendarYearMonth() + DateUtil.FIRST_DAY);
		}
		//対象年月のTextViewの初期値設定
		tvTargetDate.setText(DateUtil.YEARMONTH_FORMAT.format(clTargetDate.getTime()));
		StringBuilder sbDateText = new StringBuilder();
		sbDateText.append(Integer.toString(clTargetDate.get(Calendar.YEAR)));
		sbDateText.append(Integer.toString(clTargetDate.get(Calendar.MONTH)+1));
		tdhDB = new ToDoDatabaseHelper(this,sbDateText.toString() + getString(R.string.sqlite_todo_filename));
		//カテゴリスピナー DB読み込み
		boolean blCategoryResult = selectCATEGORY();
		if(blCategoryResult == false) {
			Toast.makeText(AggregateCalculation.this, getString(R.string.sqlite_read_err), Toast.LENGTH_LONG).show();
			endActivity();
		}
		Log.d("DEBUG", "AggregateCalculation setViewElement End");
	}

	/**
	 * カテゴリ情報のデータベース読み込み処理
	 *
	 * @return 処理を行った場合はtrue
	 */
	private boolean selectCATEGORY() {
		Log.d("DEBUG", "AggregateCalculation selectCATEGORY Start");
		boolean blResult = true;
		ToDoDatabaseHelper.CategoryCursor cObj = null;
		ArrayList<String> alTemp = new ArrayList<String>();
		hmCategorySpinner = new HashMap<Integer,String>();
		hmCategoryName = new HashMap<String,String>();
		try {
			String[] where_args = {};
			//カーソルの取得
			cObj = tdhDB.getCategory(where_args);
			//カーソルポインター初期化
			startManagingCursor(cObj);
			Log.d("DEBUG", "AggregateCalculation selectCATEGORY CategoryCursor Count : " + cObj.getCount());
			for( int intCt=0; intCt<cObj.getCount(); intCt++){
				//カテゴリのセット
				StringBuilder sbTempLine = new StringBuilder();
				sbTempLine.append(cObj.getColName());
				sbTempLine.append(" ");
				sbTempLine.append(getString(R.string.maewaku));
				sbTempLine.append(cObj.getColCode());
				sbTempLine.append(getString(R.string.ushirowaku));
				alTemp.add(sbTempLine.toString());
				hmCategoryName.put(cObj.getColCode(), sbTempLine.toString());
				hmCategorySpinner.put(intCt, cObj.getColCode());
				cObj.moveToNext();
			}
			cObj.close();
			//Adapterの作成
			aaCategory = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, alTemp);
			//ドロップダウンのレイアウトを指定
			aaCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			sCategory.setAdapter(aaCategory);
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "AggregateCalculation selectCATEGORY DB Error",e);
		} finally {
			if(cObj != null) {
				cObj.close();
			}
		}
		Log.d("DEBUG", "AggregateCalculation selectCATEGORY End");
		return blResult;
	}

	/**
	 * 画面要素へのリスナーセット
	 *
	 */
	private void setViewListener() {
		Log.d("DEBUG", "AggregateCalculation setViewListener Start");
		//対象年月テキストビューのリスナー
		tvTargetDate.setOnClickListener(new DateOnClickListener(this));
		//集計テーブル小リストビューのリスナー
		elvSummaryTableList.setOnChildClickListener(this);
		Log.d("DEBUG", "AggregateCalculation setViewListener End");
	}

	/**
	 * DateOnClickListener
	 *  日付の文字列にセットされるリスナー
	 */
	private class DateOnClickListener implements OnClickListener, DialogInterface.OnCancelListener{
		private Context contextObj = null;
		public DateOnClickListener(Context context){
			Log.d("DEBUG", "AggregateCalculation DateOnClickListener DateOnClickListener Start");
			//Contextが必要なので、コンストラクタで渡して覚えておく
			contextObj = context;
			Log.d("DEBUG", "AggregateCalculation DateOnClickListener DateOnClickListener End");
		}

		/**
		 * クリックされた時呼び出される
		 * @param View クリックされたビュー
		 */
		public void onClick(View view) {
			Log.d("DEBUG", "AggregateCalculation DateOnClickListener onClick Start");
			GregorianCalendar gcObj = null;
			if(view == tvTargetDate && blCalc == true){
				//対象年月でクリックされた場合
				//ボタン連打ロックオン
				blCalc = false;
				gcObj = DateUtil.toCalendar(tvTargetDate.getText().toString() + DateUtil.FIRST_DAY);
			} else {
				return;
			}
			//年月用AlertDialogを作成し表示する
			AlertDialog.Builder adbObj = new AlertDialog.Builder(contextObj);
			//ダイアログ内ボタン連打ロックオフ
			blDialogButton = true;
			final DatePicker dpObj = new DatePicker(contextObj);
			dpObj.updateDate(gcObj.get(Calendar.YEAR), gcObj.get(Calendar.MONTH), gcObj.get(Calendar.DAY_OF_MONTH));
			int intDayID = Resources.getSystem().getIdentifier("day", "id", "android");
			dpObj.findViewById(intDayID).setVisibility(View.GONE);
			adbObj.setView(dpObj)
					.setTitle(contextObj.getString(R.string.select_calendar))
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface diObj, int intWhich) {
							if(blDialogButton == true) {
								//ダイアログ内ボタン連打ロックオン
								blDialogButton = false;
								//上限・下限年月補正ロジック
								long lgLowerLimitTime = DateUtil.convMSec(getString(R.string.lower_limit_time));
								long lgUpperLimitTime = DateUtil.convMSec(getString(R.string.upper_limit_time));
								GregorianCalendar gcalObj = new GregorianCalendar();
								gcalObj.set(dpObj.getYear(),dpObj.getMonth(),dpObj.getDayOfMonth());
								if(gcalObj.getTimeInMillis() < lgLowerLimitTime) {
									gcalObj = DateUtil.toCalendar(getString(R.string.lower_limit_time));
								} else if(gcalObj.getTimeInMillis() > lgUpperLimitTime) {
									gcalObj = DateUtil.toCalendar(getString(R.string.upper_limit_time));
								}
								tvTargetDate.setText(DateUtil.YEARMONTH_FORMAT.format(gcalObj.getTime()));
								//ボタン連打ロックオフ
								blCalc = true;
							}
						}
					})
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface diObj, int intWhich) {
							if(blDialogButton == true) {
								//ダイアログ内ボタン連打ロックオン
								blDialogButton = false;
								//ボタン連打ロックオフ
								blCalc = true;
							}
						}
					})
					.setOnCancelListener(this)
					.create()
					.show();
			Log.d("DEBUG", "AggregateCalculation DateOnClickListener onClick End");
		}

		/**
		 * onCancel
		 * ダイアログ戻るボタン処理
		 *
		 * @param dialog dialog情報 
		 */
		@Override
		public void onCancel(DialogInterface dialog) {
			Log.d("DEBUG", "AggregateCalculation DateOnClickListener onCancel Start");
			//ダイアログ表示時に戻るボタンが押下された場合
			//ボタン連打ロックオフ
			blCalc = true;
			Log.d("DEBUG", "AggregateCalculation DateOnClickListener onCancel End");
		}

	}

	/**
	 * agcal
	 *  集計ボタン押下時の処理
	 * @param view 選択ビュー
	 */
	public void agcal(View view) {
		Log.d("DEBUG", "AggregateCalculation agcal Start");
		if(blCalc == true) {
			//集計ボタン連打ロックオン
			blCalc = false;
			//選択対象年月情報取得
			GregorianCalendar gcObj = DateUtil.toCalendar(tvTargetDate.getText().toString() + DateUtil.FIRST_DAY);
			StringBuilder sbDateText = new StringBuilder();
			sbDateText.append(Integer.toString(gcObj.get(Calendar.YEAR)));
			sbDateText.append(Integer.toString(gcObj.get(Calendar.MONTH)+1));
			//選択カテゴリ情報取得
			long lgCategoryPossition = sCategory.getSelectedItemId();
			String strCategoryCode = hmCategorySpinner.get((int)lgCategoryPossition);
			//集計リスト作成処理
			SetSummaryTableListTask task = new SetSummaryTableListTask();
			task.execute(sbDateText.toString(), strCategoryCode);
		}
		Log.d("DEBUG", "AggregateCalculation agcal End");
	}

	/**
	 * afterSetSummaryTableList
	 *  集計リスト作成処理後の処理
	 * @param strReturnCode 結果コード
	 * @param STLRList 集計リスト
	 */
	//集計リスト作成処理後の遷移処理
	public void afterSetSummaryTableList(String strReturnCode, List<SummaryTableListParentRow> lSTLPR, List<List<SummaryTableListChildRow>> llSTLCR) {
		Log.d("DEBUG", "AggregateCalculation afterSetSummaryTableList Start");
		//集計リスト作成処理の結果受け取り
		Log.d("DEBUG","AggregateCalculation afterSetSummaryTableList strReturnCode : " + strReturnCode);
		if(!(strReturnCode == null || strReturnCode.equals("")) && strReturnCode.equals("Success")) {
			//エラー無しの場合
			this.lSTLPR = lSTLPR;
			this.llSTLCR = llSTLCR;
			steliaObj = new SummaryTableExpandableListItemAdapter(this, this.lSTLPR, this.llSTLCR);
			//集計テーブルエクスパンダブルリストビューにアダプタをセット
			elvSummaryTableList.setAdapter(steliaObj);
		} else {
			//エラー有りの場合
			//集計画面にエラーダイアログ表示
			Toast.makeText(AggregateCalculation.this, getString(R.string.sqlite_read_err), Toast.LENGTH_LONG).show();
			endActivity();
		}
		//集計ボタン連打ロックオフ
		blCalc = true;
		Log.d("DEBUG", "AggregateCalculation afterSetSummaryTableList End");
	}

	/**
	 * onChildClick
	 * 集計結果の子リストのクリック処理
	 * @param elvObj エクスパンダブルリストビュー
	 * @param vObj ビューオブジェクト
	 * @param intGroupPosition 親リストの選択ポジション番号 
	 * @param intChildPosition 子リストの選択ポジション番号
	 * @param lId ID
	 * @return 処理を行った場合はtrue
	 */
	@Override
	public boolean onChildClick(ExpandableListView elvObj, View vObj, int intGroupPosition, int intChildPosition, long lId) {
		Log.d("DEBUG", "AggregateCalculation onChildClick Start");
		//集計結果詳細画面への遷移処理
		dispBreakdownDetail(intGroupPosition, intChildPosition);
		Log.d("DEBUG", "AggregateCalculation onChildClick End");
		return true;
	}

	/**
	 * 集計結果詳細画面への遷移処理
	 *
	 */
	public void dispBreakdownDetail(int intGroupPosition, int intChildPosition) {
		Log.d("DEBUG", "AggregateCalculation dispBreakdownDetail Start");
		//アプリ内のアクティビティを呼び出すインテントの生成
		Intent intent = new Intent(this, MainTab.class);
		//インテントのパラメータ設定
		//カレンダー年月
		intent.putExtra("calym", stdObj.getStrCalendarYearMonth());
		//選択年月日
		intent.putExtra("calymd", llSTLCR.get(intGroupPosition).get(intChildPosition).getStrDate());
		//選択元ユーザーインターフェースID
		intent.putExtra("uiid", getString(R.string.uiid12));
		//キーID
		intent.putExtra("keyid", stdObj.getLgKeyId());
		startActivity(intent);
		endActivity();
		Log.d("DEBUG", "AggregateCalculation dispBreakdownDetail End");
	}

	/**
	 * Activity遷移処理
	 *
	 */
	public void nextActivity() {
		Log.d("DEBUG", "AggregateCalculation nextActivity Start");
		//「カレンダー」画面に戻る
		//アプリ内のアクティビティを呼び出すインテントの生成
		Intent intent = new Intent(this, MainCalendar.class);
		//インテントのパラメータ設定
		//カレンダー年月
		intent.putExtra("calym", stdObj.getStrCalendarYearMonth());
		//選択年月日
		intent.putExtra("calymd", stdObj.getStrCalendarYearMonthDay());
		//選択元ユーザーインターフェースID
		intent.putExtra("uiid", getString(R.string.uiid12));
		//キーID
		intent.putExtra("keyid", stdObj.getLgKeyId());
		startActivity(intent);
		endActivity();
		Log.d("DEBUG", "AggregateCalculation nextActivity End");
	}

	/**
	 * Activity終了処理
	 *
	 */
	private void endActivity() {
		Log.d("DEBUG", "AggregateCalculation endActivity Start");
		//自アクティビティの終了
		finish();
		Log.d("DEBUG", "AggregateCalculation endActivity End");
	}

	/**
	 * 画面回転時に呼ばれるサイクル
	 *
	 * @param newConfig 新しい設定値
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d("DEBUG", "AggregateCalculation onConfigurationChanged Start");
		super.onConfigurationChanged(newConfig);
		Log.d("DEBUG", "AggregateCalculation onConfigurationChanged End");
	}

	/**
	 * バックグランド時に呼ばれるサイクル
	 *
	 * @param outState 保管設定値
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {  
		Log.d("DEBUG", "AggregateCalculation onSaveInstanceState Start");
		super.onSaveInstanceState(outState);
		outState.putString("calym", tvTargetDate.getText().toString());
		Log.d("DEBUG", "AggregateCalculation onSaveInstanceState End");
	} 

	/**
	 * onDestroy
	 * データベースヘルパークローズ処理
	 */
	@Override
	public void onDestroy(){
		Log.d("DEBUG", "AggregateCalculation onDestroy Start");
		super.onDestroy();
		if(tdhDB != null) {
			tdhDB.close();
		}
		Log.d("DEBUG", "AggregateCalculation onDestroy End");
	}

	/**
	 * 戻るボタンでカレンダー画面へ遷移
	 *
	 * @param kEvent キーイベント情報 
	 * @return 処理を行った場合はtrue
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent kEvent) {
		Log.d("DEBUG", "AggregateCalculation dispatchKeyEvent Start");
		//キー押下されたことを確認
		if (kEvent.getAction() == KeyEvent.ACTION_DOWN) {
			//戻るボタンが押されたか確認
			if (kEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				nextActivity();
			}
		}
		Log.d("DEBUG", "AggregateCalculation dispatchKeyEvent End");
		return super.dispatchKeyEvent(kEvent);
	}

	/**
	 * メニューボタン押下時の処理
	 *
	 * @param Menu 現在のメニュー
	 * @return メニューの生成に成功したらtrue
	 */
	public boolean onCreateOptionsMenu (Menu menu){
		Log.d("DEBUG", "AggregateCalculation onCreateOptionsMenu Start");
		//MenuInflater取得
		MenuInflater miObj = getMenuInflater();
		//MenuInflaterを使用してメニューをリソースから作成する
		miObj.inflate(R.menu.aggregatecalculation_menu,menu);
		Log.d("DEBUG", "AggregateCalculation onCreateOptionsMenu End");
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * メニュー選択時の処理
	 *
	 * @param MenuItem 選択されたメニューアイテム
	 * @return 処理を行った場合はtrue 
	 */
	public boolean onOptionsItemSelected (MenuItem item){
		Log.d("DEBUG", "AggregateCalculation onOptionsItemSelected Start");
		if (item.getItemId() == R.id.shareMenu) {
			//共有処理後の処理を受け取るhandler
			hObj = new Handler();
			//共有メニュー処理
			shareAggregateResult();
			Log.d("DEBUG", "AggregateCalculation onOptionsItemSelected(1) End");
			return true;
		}
		Log.d("DEBUG", "AggregateCalculation onOptionsItemSelected(2) End");
		return false;
	}

	/**
	 * 共有処理
	 *
	 */
	public void shareAggregateResult() {
		Log.d("DEBUG", "AggregateCalculation shareAggregateResult Start");
		//プログレスダイアログの作成
		pdObj = new ProgressDialog(AggregateCalculation.this);
		pdObj.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pdObj.setMessage(getString(R.string.actallyingprocess));
		//途中での停止不可設定
		pdObj.setCancelable(false);
		//プログレスダイアログの表示
		pdObj.show();
		//実際の処理を行うスレッドを作成
		Thread thread = new Thread(runMakeAggregateResult);
		//スレッドの実行開始
		thread.start();
		Log.d("DEBUG", "AggregateCalculation shareAggregateResult End");
	}

	/**
	 * shareAggregateResultの実行を行うスレッド
	 */
	private Runnable runMakeAggregateResult = new Runnable(){
		public void run() {
			Log.d("DEBUG", "AggregateCalculation runMakeAggregateResult run Start");
			sbAggregateResult = new StringBuffer();
			//選択対象年月情報取得
			GregorianCalendar gcObj = DateUtil.toCalendar(tvTargetDate.getText().toString() + DateUtil.FIRST_DAY);
			StringBuilder sbDateText = new StringBuilder();
			sbDateText.append(Integer.toString(gcObj.get(Calendar.YEAR)));
			sbDateText.append(Integer.toString(gcObj.get(Calendar.MONTH)+1));
			//選択カテゴリ情報取得
			long lgCategoryPossition = sCategory.getSelectedItemId();
			String strCategoryCode = hmCategorySpinner.get((int)lgCategoryPossition);
			//DB接続準備
			tdhDB = new ToDoDatabaseHelper(AggregateCalculation.this, sbDateText.toString() + getString(R.string.sqlite_todo_filename));
			//サブカテゴリデータ取得処理
			boolean blSubCategoryResult = selectSUBCATEGORY(strCategoryCode);
			if(blSubCategoryResult == false) {
				hObj.post(new Runnable(){
					public void run(){
						//エラー出力
						//プログレスダイアログを消去
						pdObj.dismiss();
						Toast.makeText(AggregateCalculation.this, getString(R.string.sqlite_read_err), Toast.LENGTH_LONG).show();
						endActivity();
					}
				});
			} else {
				//サブカテゴリ別所要時間取得処理
				boolean blToDoTATResult = selectBreakDownTODOTAT(strCategoryCode);
				if(blToDoTATResult == false) {
					hObj.post(new Runnable(){
						public void run(){
							//エラー出力
							//プログレスダイアログを消去
							pdObj.dismiss();
							Toast.makeText(AggregateCalculation.this, getString(R.string.sqlite_read_err), Toast.LENGTH_LONG).show();
							endActivity();
						}
					});
				} else {
					//共有テキストヘッダ作成
					sbAggregateResult.append(getString(R.string.act_name1));
					sbAggregateResult.append(getString(R.string.restoreConfirm1));
					sbAggregateResult.append(getString(R.string.acshare_msg4));
					sbAggregateResult.append(getString(R.string.restoreConfirm1));
					sbAggregateResult.append(getString(R.string.setargetperiod));
					sbAggregateResult.append(tvTargetDate.getText().toString());
					sbAggregateResult.append(getString(R.string.restoreConfirm1));
					sbAggregateResult.append(getString(R.string.acshare_msg1));
					sbAggregateResult.append(hmCategoryName.get(hmCategorySpinner.get((int)lgCategoryPossition)));
					sbAggregateResult.append(getString(R.string.acshare_msg2));
					//共有テキスト明細作成
					sbAggregateResult.append(makeShareDocument());
					sbAggregateResult.append(getString(R.string.acshare_msg3));
				}
			}
			//プログレスダイアログを消去
			pdObj.dismiss();
			hObj.post(new Runnable(){
				public void run(){
					//メインプロセスで実行される。
					//共有データサイズの確認
					if(InputCheckUtil.checkSizeCount(sbAggregateResult, Integer.parseInt(getString(R.string.ods_limit1)))) {
						//サイズが小さい場合
						//外部アプリ起動インテントの生成
						Intent intent = new Intent();
						//インテントのパラメータ設定
						intent.setAction(Intent.ACTION_SEND);
						intent.setType("text/plain");
						//当該月の集計情報一覧
						intent.putExtra(Intent.EXTRA_TEXT, sbAggregateResult.toString());
						//アクティビティの呼び出し
						startActivity(intent);
					} else {
						//サイズが大きい場合
						Toast.makeText(AggregateCalculation.this, getString(R.string.share_size_err_msg), Toast.LENGTH_LONG).show();
					}
				}
			});
			Log.d("DEBUG", "AggregateCalculation runMakeAggregateResult run End");
		}
	};

	/**
	 * サブカテゴリ情報のデータベース読み込み処理
	 *
	 * @strCategoryKey カテゴリ略字記号キー
	 * @return 処理を行った場合はtrue
	 */
	private boolean selectSUBCATEGORY(String strCategoryKey) {
		Log.d("DEBUG", "AggregateCalculation selectSUBCATEGORY Start");
		boolean blResult = true;
		SubcategoryCursor scObj = null;
		alSSTLR = new ArrayList<ShareSummaryTableListRow>();
		try {
			String[] where_args = {strCategoryKey};
			//カーソルの取得
			scObj = tdhDB.getSubcategory(where_args);
			//カーソルポインター初期化
			startManagingCursor(scObj);
			Log.d("DEBUG", "AggregateCalculation selectSUBCATEGORY CategoryCursor Count : " + scObj.getCount());
			for( int intCt=0; intCt<scObj.getCount(); intCt++){
				//サブカテゴリのセット
				ShareSummaryTableListRow sstlrObj = new ShareSummaryTableListRow();
				sstlrObj.setStrSubcategoryCode(scObj.getColCcode());
				sstlrObj.setStrSubcategoryName(scObj.getColName());
				//仮ArrayListオブジェクトの格納
				ArrayList<ShareTATListRow> alSTLRProxy = new ArrayList<ShareTATListRow>();
				sstlrObj.setAlSTATLR(alSTLRProxy);
				alSSTLR.add(sstlrObj);
				scObj.moveToNext();
			}
			scObj.close();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "AggregateCalculation selectSUBCATEGORY DB Error",e);
		} finally {
			if(scObj != null) {
				scObj.close();
			}
		}
		Log.d("DEBUG", "AggregateCalculation selectSUBCATEGORY End");
		return blResult;
	}

	/**
	 * サブカテゴリ別所要時間取得処理
	 *
	 * @return 処理を行った場合はtrue
	 */
	private boolean selectBreakDownTODOTAT(String strCategoryKey) {
		Log.d("DEBUG", "AggregateCalculation selectBreakDownTODOTAT Start");
		boolean blResult = true;
		ToDoCursor tdcObj = null;
		try {
			for (Iterator<ShareSummaryTableListRow> iObj = alSSTLR.iterator(); iObj.hasNext();) {
				ShareSummaryTableListRow sstlrObj = iObj.next();
				//サブカテゴリ別日別合計所要時間の取得処理
				tdcObj = null;
				BigDecimal bdSTAT = new BigDecimal(0d);
				BigDecimal bdTempTAT;
				String[] where_args = {strCategoryKey, sstlrObj.getStrSubcategoryCode()};
				//カーソルの取得
				tdcObj = tdhDB.getToDoTAT(where_args);
				//カーソルポインター初期化
				startManagingCursor(tdcObj);
				Log.d("DEBUG", "AggregateCalculation selectBreakDownTODOTAT getToDoTAT ToDoCursor Count : " + tdcObj.getCount());
				for( int intCt=0; intCt<tdcObj.getCount(); intCt++){
					//カテゴリ・サブカテゴリ・日別所要時間の積み上げ
					ShareTATListRow objSTLR = new ShareTATListRow();
					objSTLR.setStrDate(DateUtil.convBaseYMD(tdcObj.getColDate()));
					objSTLR.setStrTime(String.valueOf(Float.valueOf(tdcObj.getColTAT())));
					objSTLR.setStrTitle(tdcObj.getColTitle());
					bdTempTAT = new BigDecimal(Double.valueOf(tdcObj.getColTAT()));
					bdSTAT = bdSTAT.add(bdTempTAT);
					sstlrObj.getAlSTATLR().add(objSTLR);
					tdcObj.moveToNext();
				}
				tdcObj.close();
				//サブカテゴリ別合計所要時間のセット
				//四捨五入
				bdSTAT = bdSTAT.setScale(2,BigDecimal.ROUND_HALF_UP);
				sstlrObj.setStrSummaryTime(bdSTAT.toString());
			}
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "AggregateCalculation selectBreakDownTODOTAT DB Error",e);
		} finally {
			if(tdcObj != null) {
				tdcObj.close();
			}
		}
		Log.d("DEBUG", "AggregateCalculation selectBreakDownTODOTAT End");
		return blResult;
	}

	/**
	 * //共有テキスト作成
	 * makeShareDocument();
	 *
	 * @return 共有テキスト
	 */
	private String makeShareDocument() {
		Log.d("DEBUG", "AggregateCalculation makeShareDocument Start");
		StringBuffer sbResult = new StringBuffer();
		for (Iterator<ShareSummaryTableListRow> iObj = alSSTLR.iterator(); iObj.hasNext();) {
			ShareSummaryTableListRow sstlrObj = iObj.next();
			sbResult.append(sstlrObj.getStrSubcategoryCode());
			sbResult.append(getString(R.string.acbindmark));
			sbResult.append(InputCheckUtil.convKaigyouKara(InputCheckUtil.convTabKara(sstlrObj.getStrSubcategoryName())));
			sbResult.append(getString(R.string.acdelimiter));
			sbResult.append(sstlrObj.getStrSummaryTime());
			sbResult.append(getString(R.string.actimemark));
			sbResult.append(getString(R.string.restoreConfirm1));
			for (Iterator<ShareTATListRow> istlrObj = sstlrObj.getAlSTATLR().iterator(); istlrObj.hasNext();) {
				ShareTATListRow stlrObj = istlrObj.next();
				sbResult.append(getString(R.string.actopspace));
				sbResult.append(stlrObj.getStrDate());
				sbResult.append(getString(R.string.acdelimiter));
				sbResult.append(stlrObj.getStrTime());
				sbResult.append(getString(R.string.actimemark));
				sbResult.append(getString(R.string.acdelimiter));
				sbResult.append(InputCheckUtil.convKaigyouKara(InputCheckUtil.convTabKara(stlrObj.getStrTitle())));
				sbResult.append(getString(R.string.restoreConfirm1));
			}
		}
		Log.d("DEBUG", "AggregateCalculation makeShareDocument End");
		return sbResult.toString();
	}

	//集計リスト作成処理(内部クラス)
	private class SetSummaryTableListTask extends AsyncTask<String, Integer, StringBuilder>{
		//スリープ設定情報読み込み
		private long lgSleepTime = Long.parseLong(getString(R.string.aggregatecalculation_sleep));
		//ダイアログ
		private ProgressDialog dialog;
		//集計テーブル親リスト(一時)
		private List<SummaryTableListParentRow> lSTLPRTemp;
		//集計テーブル親リスト
		private List<SummaryTableListParentRow> lSTLPR;
		//集計テーブル子リスト
		private List<List<SummaryTableListChildRow>> llSTLCR;
		
		//実行前準備処理(インジケータのセットアップ)
		@Override
		protected void onPreExecute() {
			Log.d("DEBUG","SetSummaryTableListTask onPreExecute Start");
			//ダイアログの生成
			dialog = new ProgressDialog(AggregateCalculation.this);
			//メッセージのセット
			dialog.setMessage(getString(R.string.actallyingprocess));
			//ダイアログの戻るキー無効
			dialog.setCancelable(false);
			//ダイアログ表示
			dialog.show();
			Log.d("DEBUG","SetSummaryTableListTask onPreExecute End");
		}

		//バックグラウンド実行処理(本体)
		@Override
		protected StringBuilder doInBackground(String... params) {
			Log.d("DEBUG","SetSummaryTableListTask doInBackground Start");
			StringBuilder sbResult = new StringBuilder();
			try {
				//対象年月とカテゴリコードの取得
				String strDateText = params[0];
				String strCategoryCode = params[1];
				//DB接続準備
				tdhDB = new ToDoDatabaseHelper(AggregateCalculation.this, strDateText + getString(R.string.sqlite_todo_filename));
				publishProgress(1);
				Thread.sleep(lgSleepTime);
				//サブカテゴリデータ取得処理
				boolean blSubCategoryResult = selectSUBCATEGORY(strCategoryCode);
				if(blSubCategoryResult == false) {
					sbResult.append("Error");
				}
				publishProgress(2);
				Thread.sleep(lgSleepTime);
				//サブカテゴリ別所要時間取得処理
				boolean blToDoTATResult = selectTODOTAT(strCategoryCode);
				if(blToDoTATResult == false) {
					sbResult.append("Error");
				}
				publishProgress(3);
				Thread.sleep(lgSleepTime);
				sbResult.append("Success");
			} catch (Exception e) {
				Log.e("ERROR","SetSummaryTableListTask doInBackground",e);
				//予期せぬException(スレッドエラー）
				sbResult.append("Error");
			}
			Log.d("DEBUG","SetSummaryTableListTask doInBackground End");
			return sbResult;
		}

		//バックグラウンド処理の進捗状況をUIスレッドで表示する為の処理
		@Override
		protected void onProgressUpdate(Integer... values) {
			Log.d("DEBUG","SetSummaryTableListTask onProgressUpdate Start");
			//ダイアログのメッセージ変更
			dialog.setMessage(getString(R.string.actallyingprocess) + values[0] + "/3");
			Log.d("DEBUG","SetSummaryTableListTask onProgressUpdate End");
		}

		//バックグランド処理終了後のUIスレッド呼び出し処理
		@Override
		protected void onPostExecute(StringBuilder result) {
			Log.d("DEBUG","SetSummaryTableListTask onPostExecute Start");
			//ダイアログ終了
			dialog.dismiss();
			//集計リスト作成処理の結果コードとリストデータ返却
			afterSetSummaryTableList(result.toString(), lSTLPR, llSTLCR);
			Log.d("DEBUG","SetSummaryTableListTask onPostExecute End");
		}

		/**
		 * サブカテゴリ情報のデータベース読み込み処理
		 *
		 * @strCategoryKey カテゴリ略字記号キー
		 * @return 処理を行った場合はtrue
		 */
		private boolean selectSUBCATEGORY(String strCategoryKey) {
			Log.d("DEBUG", "SetSummaryTableListTask selectSUBCATEGORY Start");
			boolean blResult = true;
			SubcategoryCursor scObj = null;
			lSTLPRTemp = new ArrayList<SummaryTableListParentRow>();
			try {
				String[] where_args = {strCategoryKey};
				//カーソルの取得
				scObj = tdhDB.getSubcategory(where_args);
				//カーソルポインター初期化
				startManagingCursor(scObj);
				Log.d("DEBUG", "SetSummaryTableListTask selectSUBCATEGORY CategoryCursor Count : " + scObj.getCount());
				for( int intCt=0; intCt<scObj.getCount(); intCt++){
					//サブカテゴリのセット
					SummaryTableListParentRow stlrObj = new SummaryTableListParentRow();
					stlrObj.setStrSubcategoryCode(scObj.getColCcode());
					stlrObj.setStrSubcategoryName(scObj.getColName());
					lSTLPRTemp.add(stlrObj);
					scObj.moveToNext();
				}
				scObj.close();
			} catch (SQLException e) {
				blResult = false;
				Log.e("ERROR", "SetSummaryTableListTask selectSUBCATEGORY DB Error",e);
			} finally {
				if(scObj != null) {
					scObj.close();
				}
			}
			Log.d("DEBUG", "SetSummaryTableListTask selectSUBCATEGORY End");
			return blResult;
		}

		/**
		 * サブカテゴリ別所要時間取得処理
		 *
		 * @return 処理を行った場合はtrue
		 */
		private boolean selectTODOTAT(String strCategoryKey) {
			Log.d("DEBUG", "SetSummaryTableListTask selectTODOTAT Start");
			boolean blResult = true;
			ToDoCursor tdcObj = null;
			lSTLPR = new ArrayList<SummaryTableListParentRow>();
			llSTLCR = new ArrayList<List<SummaryTableListChildRow>>();
			try {
				for (Iterator<SummaryTableListParentRow> iObj = lSTLPRTemp.iterator(); iObj.hasNext();) {
					SummaryTableListParentRow stlrObj = iObj.next();
					//サブカテゴリ別日別合計所要時間の取得処理
					tdcObj = null;
					BigDecimal bdSTAT = new BigDecimal(0d);
					BigDecimal bdTempTAT;
					String[] where_args = {strCategoryKey, stlrObj.getStrSubcategoryCode()};
					ArrayList<SummaryTableListChildRow> alSTLCR = new ArrayList<SummaryTableListChildRow>();
					//カーソルの取得
					tdcObj = tdhDB.getToDoDailySTAT(where_args);
					//カーソルポインター初期化
					startManagingCursor(tdcObj);
					Log.d("DEBUG", "SetSummaryTableListTask selectTODOTAT getToDoDailySTAT ToDoCursor Count : " + tdcObj.getCount());
					for( int intCt=0; intCt<tdcObj.getCount(); intCt++){
						//カテゴリ・サブカテゴリ・日別所要時間の積み上げ
						SummaryTableListChildRow objSTLCR = new SummaryTableListChildRow();
						objSTLCR.setStrSubcategoryCode(stlrObj.getStrSubcategoryCode());
						objSTLCR.setStrDate(DateUtil.convBaseYMD(tdcObj.getColDate()));
						objSTLCR.setStrTime(String.valueOf(Float.valueOf(tdcObj.getColSTAT())));
						bdTempTAT = new BigDecimal(Double.valueOf(tdcObj.getColSTAT()));
						bdSTAT = bdSTAT.add(bdTempTAT);
						alSTLCR.add(objSTLCR);
						tdcObj.moveToNext();
					}
					tdcObj.close();
					llSTLCR.add(alSTLCR);
					//サブカテゴリ別合計所要時間のセット
					//四捨五入
					bdSTAT = bdSTAT.setScale(2,BigDecimal.ROUND_HALF_UP);
					stlrObj.setStrSummaryTime(bdSTAT.toString());
					lSTLPR.add(stlrObj);
				}
			} catch (SQLException e) {
				blResult = false;
				Log.e("ERROR", "SetSummaryTableListTask selectTODOTAT DB Error",e);
			} finally {
				if(tdcObj != null) {
					tdcObj.close();
				}
			}
			Log.d("DEBUG", "SetSummaryTableListTask selectTODOTAT End");
			return blResult;
		}
	}
}