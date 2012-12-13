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


 クラス名：MainTab
 内容：スケジュールタブレイアウトメインモジュール
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
          0.1/2012.05.23/T.Mashiko
          0.2/2012.05.28/T.Mashiko
          0.3/2012.05.29/T.Mashiko
          0.4/2012.06.06/T.Mashiko インテントLong型修正
          0.5/2012.06.15/T.Mashiko YYYY-MM形式修正
          0.6/2012.06.20/T.Mashiko 検索結果時の分岐処理追加
          0.7/2012.07.11/T.Mashiko タブ画面での日送りフリック対応
          0.8/2012.07.13/T.Mashiko 集計結果時の分岐処理追加
          0.9/2012.07.15/T.Mashiko スケジュール共有機能追加
          1.0/2012.07.16/T.Mashiko スケジュール共有機能追加
          1.1/2012.07.18/T.Mashiko ロギング表記修正
          1.2/2012.08.29/T.Mashiko 初期表示タブをToDo側に変更
          1.3/2012.09.26/T.Mashiko スケジュール共有機能のデータ上限チェック追加
*/
package study.tdcc.act;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import study.tdcc.*;
import study.tdcc.bean.*;
import study.tdcc.lib.DateUtil;
import study.tdcc.lib.InputCheckUtil;
import study.tdcc.lib.ScheduleDatabaseHelper;
import study.tdcc.lib.ScheduleDatabaseHelper.ScheduleCursor;
import study.tdcc.lib.ToDoDatabaseHelper;
import study.tdcc.lib.ToDoDatabaseHelper.ToDoCursor;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class MainTab extends TabActivity {
	//カスタムタイトルテキストビュー
	private TextView tvCustomTitle;
	//カスタムタイトル(バージョン)テキストビュー
	private TextView tvCustomTitleVersion;
	//ターゲット年月日を保持する変数
	private GregorianCalendar gcTargetYearMonthDay;
	//画面遷移データオブジェクト
	private ScreenTransitionData stdObj = new ScreenTransitionData();
	//プログレスダイアログのインスタンス
	private ProgressDialog pdObj = null;
	//共有処理後の処理を受け取るhandler
	private Handler hObj;
	//共有機能出力テキスト
	private StringBuffer sbScheduleToDoResult;
	//データベースオブジェクト(ToDo)
	private ToDoDatabaseHelper tdhDB;
	//データベースオブジェクト(スケジュール)
	private ScheduleDatabaseHelper sdhDB;
	//スケジュールテーブルリスト(終日スケジュール以外の一時リスト)
	private List<ShareScheduleListRow> lSSLRTemp;
	//スケジュールテーブルリスト
	private List<ShareScheduleListRow> lSSLR;
	//ToDoリスト
	private List<ShareToDoListRow> lSTDLR;

	/**
	 * onCreate
	 * @param savedInstanceState バンドル
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("DEBUG", "MainTab onCreate Start");
		super.onCreate(savedInstanceState);
		//カスタムタイトル
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		//スケジュールメインタブビュー描画処理
		setContentView(R.layout.maintab);
		//カスタムタイトル描画処理
		Window wObj = getWindow();
		wObj.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.customtitle);
		//画面遷移情報の取得
		getScreenTransitionData();
		//画面要素の取得処理
		getViewElement();
		//カスタムタイトルの内容セット
		setCustomTitle();
		//タブ要素初期化処理
		initTabs();
		Log.d("DEBUG", "MainTab onCreate End");
	}

	/**
	 * 画面遷移情報の取得
	 *
	 */
	private void getScreenTransitionData() {
		Log.d("DEBUG", "MainTab getScreenTransitionData Start");
		//画面遷移時のインテント格納値取得
		Bundle bExtras = getIntent().getExtras();
		if (bExtras != null) {
			stdObj.setStrCalendarYearMonth(bExtras.getString("calym"));
			stdObj.setStrCalendarYearMonthDay(bExtras.getString("calymd"));
			stdObj.setStrUserInterfaceId(bExtras.getString("uiid"));
			stdObj.setLgKeyId(bExtras.getLong("keyid"));
		}
		Log.d("DEBUG", "MainTab getScreenTransitionData End");
	}

	/**
	 * 画面要素の取得
	 *
	 */
	private void getViewElement() {
		Log.d("DEBUG", "MainTab getViewElement Start");
		//カスタムタイトルテキストビュー
		tvCustomTitle = (TextView)this.findViewById(R.id.titletext);
		//カスタムタイトルバージョンビュー
		tvCustomTitleVersion = (TextView)this.findViewById(R.id.titleversion);
		Log.d("DEBUG", "MainTab getViewElement End");
	}

	/**
	 * カスタムタイトルの内容セット
	 *
	 */
	private void setCustomTitle() {
		Log.d("DEBUG", "MainTab setCustomTitle Start");
		StringBuilder sbText = new StringBuilder();
		//カスタムタイトル情報取得
		gcTargetYearMonthDay = DateUtil.toCalendar(stdObj.getStrCalendarYearMonthDay());
		//年月の取得
		sbText.append(DateUtil.DATE_FORMAT.format(gcTargetYearMonthDay.getTime()));
		sbText.append(getString(R.string.maewaku));
		sbText.append(DateUtil.toDayOfWeek(this, gcTargetYearMonthDay));
		sbText.append(getString(R.string.ushirowaku));
		tvCustomTitle.setText(sbText.toString());
		StringBuilder sbVersion = new StringBuilder();
		sbVersion.append(getString(R.string.title_version));
		PackageManager pmObj = this.getPackageManager();
		try {
			PackageInfo piObj = pmObj.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
			sbVersion.append(piObj.versionName);
		} catch (NameNotFoundException e) {
			Log.e("ERROR", "MainTab setCustomTitle NameNotFoundException", e);
		}
		tvCustomTitleVersion.setText(sbVersion.toString());
		Log.d("DEBUG", "MainTab setCustomTitle End");
	}

	/**
	 * タブ要素初期化処理
	 *
	 */
	protected void initTabs(){
		Log.d("DEBUG", "MainTab initTabs Start");
		Resources rObj = getResources();
		TabHost thObj = getTabHost();
		TabHost.TabSpec tsObj;
		Intent intent;

		//ScheduleTab
		intent = new Intent().setClass(this, ScheduleTab.class);
		//インテントのパラメータ設定
		intent = setExtraInfo(intent);
		tsObj = thObj.newTabSpec("Schedule").setIndicator("Schedule", rObj.getDrawable(R.drawable.ic_tab_schedule)).setContent(intent);
		thObj.addTab(tsObj);

		//ToDoTab
		intent = new Intent().setClass(this, ToDoTab.class);
		//インテント情報の引渡し
		intent = setExtraInfo(intent);
		tsObj = thObj.newTabSpec("ToDo").setIndicator("ToDo", rObj.getDrawable(R.drawable.ic_tab_todo)).setContent(intent);
		thObj.addTab(tsObj);

		//デフォルト表示タブ設定
		if(stdObj.getStrUserInterfaceId().equals(getString(R.string.uiid1)) || stdObj.getStrUserInterfaceId().equals(getString(R.string.uiid4)) || stdObj.getStrUserInterfaceId().equals(getString(R.string.uiid7)) || stdObj.getStrUserInterfaceId().equals(getString(R.string.uiid8)) || stdObj.getStrUserInterfaceId().equals(getString(R.string.uiid10)) || stdObj.getStrUserInterfaceId().equals(getString(R.string.uiid12)) || stdObj.getStrUserInterfaceId().equals(getString(R.string.uiid13))) {
			//ToDoタブ表示
			thObj.setCurrentTab(1);
		} else {
			//スケジュールタブ表示
			thObj.setCurrentTab(0);
		}
		Log.d("DEBUG", "MainTab initTabs End");
	}

	/**
	 * インテントパラメータセット処理
	 *
	 * @param intent インテント
	 * @return インテント
	 */
	private Intent setExtraInfo (Intent intent){
		Log.d("DEBUG", "MainTab setExtraInfo Start");
		//カレンダー年月
		intent.putExtra("calym", stdObj.getStrCalendarYearMonth());
		//選択年月日
		intent.putExtra("calymd", stdObj.getStrCalendarYearMonthDay());
		//選択元ユーザーインターフェースID
		intent.putExtra("uiid", stdObj.getStrUserInterfaceId());
		//キーID
		intent.putExtra("keyid", stdObj.getLgKeyId());
		Log.d("DEBUG", "MainTab setExtraInfo End");
		return intent;
	}

	/**
	 * メニューボタン押下時の処理
	 *
	 * @param Menu 現在のメニュー
	 * @return メニューの生成に成功したらtrue
	 */
	public boolean onCreateOptionsMenu (Menu menu){
		Log.d("DEBUG", "MainTab onCreateOptionsMenu Start");
		//MenuInflater取得
		MenuInflater miObj = getMenuInflater();
		//MenuInflaterを使用してメニューをリソースから作成する
		miObj.inflate(R.menu.tab_menu,menu);
		Log.d("DEBUG", "MainTab onCreateOptionsMenu End");
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * メニュー選択時の処理
	 *
	 * @param MenuItem 選択されたメニューアイテム
	 * @return 処理を行った場合はtrue 
	 */
	public boolean onOptionsItemSelected (MenuItem item){
		Log.d("DEBUG", "MainTab onOptionsItemSelected Start");
		if (item.getItemId() == R.id.shareMenu) {
			//共有メニュー処理
			//共有処理後の処理を受け取るhandler
			hObj = new Handler();
			//共有メニュー処理
			shareScheduleToDoResult();
			Log.d("DEBUG", "MainTab onOptionsItemSelected(1) End");
			return true;
		} else if (item.getItemId() == R.id.subcategoryEdit) {
			//サブカテゴリ編集メニュー処理
			nextActivity();
			//アプリケーション終了
			endActivity();
			Log.d("DEBUG", "MainTab onOptionsItemSelected(2) End");
			return true;
		} else if (item.getItemId() == R.id.endMenu) {
			//終了メニュー処理
			//終了確認yes/noダイアログの表示
			showYesNoDialog(this, R.string.mes1_dialog,R.string.mes2_dialog,
					new DialogInterface.OnClickListener() {
						//クリック時に呼ばれる
						public void onClick(DialogInterface dialog,int whith) {
							if (whith == DialogInterface.BUTTON_POSITIVE) {
								//アプリケーション終了
								endActivity();
							}
						}
					});
			Log.d("DEBUG", "MainTab onOptionsItemSelected(3) End");
			return true;
		}
		Log.d("DEBUG", "MainTab onOptionsItemSelected(4) End");
		return false;
	}

	/**
	 * 共有処理
	 *
	 */
	public void shareScheduleToDoResult() {
		Log.d("DEBUG", "MainTab shareScheduleToDoResult Start");
		//プログレスダイアログの作成
		pdObj = new ProgressDialog(MainTab.this);
		pdObj.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pdObj.setMessage(getString(R.string.scload_msg));
		//途中での停止不可設定
		pdObj.setCancelable(false);
		//プログレスダイアログの表示
		pdObj.show();
		//実際の処理を行うスレッドを作成
		Thread thread = new Thread(runMakeScheduleToDoResult);
		//スレッドの実行開始
		thread.start();
		Log.d("DEBUG", "MainTab shareScheduleToDoResult End");
	}

	/**
	 * shareScheduleToDoResultの実行を行うスレッド
	 */
	private Runnable runMakeScheduleToDoResult = new Runnable(){
		public void run() {
			Log.d("DEBUG", "MainTab runMakeScheduleToDoResult run Start");
			sbScheduleToDoResult = new StringBuffer();
			//DB接続準備(スケジュール)
			sdhDB = new ScheduleDatabaseHelper(MainTab.this);
			//指定年月日を取得
			Calendar clTargetDate = DateUtil.toCalendar(stdObj.getStrCalendarYearMonthDay());
			StringBuilder sbDateText = new StringBuilder();
			sbDateText.append(Integer.toString(clTargetDate.get(Calendar.YEAR)));
			sbDateText.append(Integer.toString(clTargetDate.get(Calendar.MONTH)+1));
			//対象年月日(YYYYMMDD)取得
			long lgTargetYMD = DateUtil.convToDoYMD((GregorianCalendar) clTargetDate);
			//DB接続準備(ToDo)
			tdhDB = new ToDoDatabaseHelper(MainTab.this, sbDateText.toString() + getString(R.string.sqlite_todo_filename));
			//当日開始時間のミリ秒
			long lgStartMSec = clTargetDate.getTimeInMillis();
			//当日終了時間のミリ秒
			clTargetDate.add(Calendar.DAY_OF_MONTH, 1);
			long lgEndMSec = clTargetDate.getTimeInMillis();
			//スケジュールデータ取得処理
			boolean blScheDuleResult = selectSchedule(stdObj.getStrCalendarYearMonthDay(), lgStartMSec, lgEndMSec);
			if(blScheDuleResult == false) {
				hObj.post(new Runnable(){
					public void run(){
						//エラー出力
						//プログレスダイアログを消去
						pdObj.dismiss();
						Toast.makeText(MainTab.this, getString(R.string.sqlite_read_err), Toast.LENGTH_LONG).show();
						endActivity();
					}
				});
			} else {
				//ToDoデータ取得処理
				boolean blToDoResult = selectTODO(lgTargetYMD);
				if(blToDoResult == false) {
					hObj.post(new Runnable(){
						public void run(){
							//エラー出力
							//プログレスダイアログを消去
							pdObj.dismiss();
							Toast.makeText(MainTab.this, getString(R.string.sqlite_read_err), Toast.LENGTH_LONG).show();
							endActivity();
						}
					});
				} else {
					//共有テキストヘッダ作成
					sbScheduleToDoResult.append(getString(R.string.act_name1));
					sbScheduleToDoResult.append(getString(R.string.restoreConfirm1));
					sbScheduleToDoResult.append(getString(R.string.tabshare_msg1));
					sbScheduleToDoResult.append(getString(R.string.restoreConfirm1));
					sbScheduleToDoResult.append(getString(R.string.tabshare_msg2));
					//年月の取得
					sbScheduleToDoResult.append(DateUtil.DATE_FORMAT.format(gcTargetYearMonthDay.getTime()));
					sbScheduleToDoResult.append(getString(R.string.maewaku));
					sbScheduleToDoResult.append(DateUtil.toDayOfWeek(MainTab.this, gcTargetYearMonthDay));
					sbScheduleToDoResult.append(getString(R.string.ushirowaku));
					//共有テキスト明細作成
					sbScheduleToDoResult.append(getString(R.string.tabshare_msg3));
					sbScheduleToDoResult.append(makeScheduleShareDocument());
					sbScheduleToDoResult.append(getString(R.string.tabshare_msg4));
					sbScheduleToDoResult.append(makeToDoShareDocument());
					sbScheduleToDoResult.append(getString(R.string.acshare_msg3));
				}
			}
			//プログレスダイアログを消去
			pdObj.dismiss();
			hObj.post(new Runnable(){
				public void run(){
					//メインプロセスで実行される。
					//共有データサイズの確認
					if(InputCheckUtil.checkSizeCount(sbScheduleToDoResult, Integer.parseInt(getString(R.string.ods_limit1)))) {
						//サイズが小さい場合
						//外部アプリ起動インテントの生成
						Intent intent = new Intent();
						//インテントのパラメータ設定
						intent.setAction(Intent.ACTION_SEND);
						intent.setType("text/plain");
						//当該月の集計情報一覧
						intent.putExtra(Intent.EXTRA_TEXT, sbScheduleToDoResult.toString());
						//アクティビティの呼び出し
						startActivity(intent);
					} else {
						//サイズが大きい場合
						Toast.makeText(MainTab.this, getString(R.string.share_size_err_msg), Toast.LENGTH_LONG).show();
					}
				}
			});
			Log.d("DEBUG", "MainTab runMakeScheduleToDoResult run End");
		}
	};

	/**
	 * スケジュール情報のデータベース読み込み処理
	 *
	 * @lgStartMSec 当日開始時間のミリ秒
	 * @lgEndMSec 当日終了時間のミリ秒
	 * @return 処理を行った場合はtrue
	 */
	private boolean selectSchedule(String strYMD, long lgStartMSec, long lgEndMSec) {
		Log.d("DEBUG", "MainTab selectSchedule Start");
		boolean blResult = true;
		ScheduleCursor scObj = null;
		lSSLR = new ArrayList<ShareScheduleListRow>();
		lSSLRTemp = new ArrayList<ShareScheduleListRow>();
		try {
			String[] where_args = {String.valueOf(lgStartMSec), String.valueOf(lgEndMSec), String.valueOf(lgEndMSec), String.valueOf(lgStartMSec), String.valueOf(lgEndMSec), String.valueOf(lgStartMSec), String.valueOf(lgEndMSec)};
			//カーソルの取得
			scObj = sdhDB.getScheduleList(where_args);
			//カーソルポインター初期化
			startManagingCursor(scObj);
			Log.d("DEBUG", "MainTab selectSchedule getScheduleList ScheduleCursor Count : " + scObj.getCount());
			for( int intCt=0; intCt<scObj.getCount(); intCt++){
				//スケジュールのセット
				ShareScheduleListRow slrObj = new ShareScheduleListRow();
				//終日チェック
				if(scObj.getColStarttime() <= lgStartMSec && scObj.getColEndtime() >= lgEndMSec) {
					//終日の場合
					//終日
					slrObj.setStrAllDay(getString(R.string.allday));
					//開始時間
					slrObj.setStrStartTime("");
					//終了時間
					slrObj.setStrEndTime("");
					//タイトル
					slrObj.setStrTitle(scObj.getColTitle());
					//場所
					slrObj.setStrPlace(scObj.getColGdWhere());
					//説明
					slrObj.setStrContent(scObj.getColContent());
					lSSLR.add(slrObj);
				} else {
					//終日以外の場合
					//終日
					slrObj.setStrAllDay(getString(R.string.karakigou));
					//開始時間
					Calendar calStart = DateUtil.toCalendar(scObj.getColGdWhenStarttime());
					if(DateUtil.DATE_FORMAT.format(calStart.getTime()).equals(strYMD)) {
						//開始時分
						slrObj.setStrStartTime(DateUtil.TIME_FORMAT.format(calStart.getTime()));
					} else {
						slrObj.setStrStartTime("");
					}
					//終了時間
					Calendar calEnd = DateUtil.toCalendar(scObj.getColGdWhenEndtime());
					if(DateUtil.DATE_FORMAT.format(calEnd.getTime()).equals(strYMD)) {
						//終了時分
						slrObj.setStrEndTime(DateUtil.TIME_FORMAT.format(calEnd.getTime()));
					} else {
						slrObj.setStrEndTime("");
					}
					//タイトル
					slrObj.setStrTitle(scObj.getColTitle());
					//場所
					slrObj.setStrPlace(scObj.getColGdWhere());
					//説明
					slrObj.setStrContent(scObj.getColContent());
					lSSLRTemp.add(slrObj);
				}
				scObj.moveToNext();
			}
			scObj.close();
			//終日のみのリストに終日以外のリストを追加
			for (Iterator<ShareScheduleListRow> iSSLR = lSSLRTemp.iterator(); iSSLR.hasNext();) {
				ShareScheduleListRow objSSLR = iSSLR.next();
				lSSLR.add(objSSLR);
			}
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "MainTab selectSchedule DB Error",e);
		} finally {
			if(scObj != null) {
				scObj.close();
			}
		}
		Log.d("DEBUG", "MainTab selectSchedule End");
		return blResult;
	}

	/**
	 * ToDo情報のデータベース読み込み処理
	 *
	 * @lgTargetDate 対象年月日
	 * @return 処理を行った場合はtrue
	 */
	private boolean selectTODO(long lgTargetYMD) {
		Log.d("DEBUG", "MainTab selectTODO Start");
		boolean blResult = true;
		ToDoCursor tdcObj = null;
		lSTDLR = new ArrayList<ShareToDoListRow>();
		try {
			String[] where_args = {String.valueOf(lgTargetYMD)};
			//カーソルの取得
			tdcObj = tdhDB.getToDoShareList(where_args);
			//カーソルポインター初期化
			startManagingCursor(tdcObj);
			Log.d("DEBUG", "MainTab selectTODO getToDoShareList ToDoCursor Count : " + tdcObj.getCount());
			for( int intCt=0; intCt<tdcObj.getCount(); intCt++){
				//ToDoのセット
				ShareToDoListRow stdlrObj = new ShareToDoListRow();
				//状態(完了)
				if(tdcObj.getColStatus() == 0l) {
					//未完了の場合
					stdlrObj.setStrStatus(getString(R.string.completion_text2));
				} else {
					//完了の場合
					stdlrObj.setStrStatus(getString(R.string.completion_text1));
				}
				//優先順位
				stdlrObj.setStrPriorityCode(tdcObj.getColPriorityCode());
				//優先順位名
				stdlrObj.setStrPriorityName(tdcObj.getColPriorityName());
				//所要時間
				stdlrObj.setStrTAT(tdcObj.getColTAT());
				//カテゴリ
				stdlrObj.setStrCategoryCode(tdcObj.getColCategoryCode());
				//カテゴリ名
				stdlrObj.setStrCategoryCodeName(tdcObj.getColCategoryName());
				//サブカテゴリ
				stdlrObj.setStrSubcategoryCode(tdcObj.getColSubcategoryCode());
				//サブカテゴリ名
				stdlrObj.setStrSubcategoryCodeName(tdcObj.getColSubcategoryName());
				//タイトル
				stdlrObj.setStrTitle(tdcObj.getColTitle());
				//内容
				stdlrObj.setStrContent(tdcObj.getColDetail());
				lSTDLR.add(stdlrObj);
				tdcObj.moveToNext();
			}
			tdcObj.close();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "MainTab getToDoShareList DB Error",e);
		} finally {
			if(tdcObj != null) {
				tdcObj.close();
			}
		}
		Log.d("DEBUG", "MainTab getToDoShareList End");
		return blResult;
	}

	/**
	 * //スケジュール共有テキスト作成
	 * makeScheduleShareDocument();
	 *
	 * @return スケジュール共有テキスト
	 */
	private String makeScheduleShareDocument() {
		Log.d("DEBUG", "MainTab makeScheduleShareDocument Start");
		StringBuffer sbResult = new StringBuffer();
		for (Iterator<ShareScheduleListRow> iObj = lSSLR.iterator(); iObj.hasNext();) {
			ShareScheduleListRow sslrObj = iObj.next();
			//時間
			sbResult.append(sslrObj.getStrStartTime());
			sbResult.append(sslrObj.getStrAllDay());
			sbResult.append(sslrObj.getStrEndTime());
			sbResult.append(getString(R.string.acdelimiter));
			//タイトル
			sbResult.append(InputCheckUtil.convKaigyouKara(InputCheckUtil.convTabKara(sslrObj.getStrTitle())));
			sbResult.append(getString(R.string.acdelimiter));
			//場所
			sbResult.append(InputCheckUtil.convKaigyouKara(InputCheckUtil.convTabKara(sslrObj.getStrPlace())));
			sbResult.append(getString(R.string.acdelimiter));
			//説明
			sbResult.append(InputCheckUtil.convKaigyouKara(InputCheckUtil.convTabKara(sslrObj.getStrContent())));
			sbResult.append(getString(R.string.restoreConfirm1));
		}
		Log.d("DEBUG", "MainTab makeScheduleShareDocument End");
		return sbResult.toString();
	}

	/**
	 * //ToDo共有テキスト作成
	 * makeToDoShareDocument();
	 *
	 * @return ToDo共有テキスト
	 */
	private String makeToDoShareDocument() {
		Log.d("DEBUG", "MainTab makeToDoShareDocument Start");
		StringBuffer sbResult = new StringBuffer();
		for (Iterator<ShareToDoListRow> iObj = lSTDLR.iterator(); iObj.hasNext();) {
			ShareToDoListRow stdlrObj = iObj.next();
			//状態
			sbResult.append(stdlrObj.getStrStatus());
			sbResult.append(getString(R.string.acdelimiter));
			//優先順位
			sbResult.append(stdlrObj.getStrPriorityCode());
			sbResult.append(getString(R.string.acbindmark));
			//優先順位名
			sbResult.append(stdlrObj.getStrPriorityName());
			sbResult.append(getString(R.string.acdelimiter));
			//所要時間
			sbResult.append(stdlrObj.getStrTAT());
			sbResult.append(getString(R.string.actimemark));
			sbResult.append(getString(R.string.acdelimiter));
			//カテゴリ
			sbResult.append(stdlrObj.getStrCategoryCode());
			sbResult.append(getString(R.string.acbindmark));
			//カテゴリ名
			sbResult.append(stdlrObj.getStrCategoryCodeName());
			sbResult.append(getString(R.string.acdelimiter));
			//サブカテゴリ
			sbResult.append(stdlrObj.getStrSubcategoryCode());
			sbResult.append(getString(R.string.acbindmark));
			//サブカテゴリ名
			sbResult.append(InputCheckUtil.convKaigyouKara(InputCheckUtil.convTabKara(stdlrObj.getStrSubcategoryCodeName())));
			sbResult.append(getString(R.string.acdelimiter));
			//タイトル
			sbResult.append(InputCheckUtil.convKaigyouKara(InputCheckUtil.convTabKara(stdlrObj.getStrTitle())));
			sbResult.append(getString(R.string.acdelimiter));
			//内容
			sbResult.append(InputCheckUtil.convKaigyouKara(InputCheckUtil.convTabKara(stdlrObj.getStrContent())));
			sbResult.append(getString(R.string.restoreConfirm1));
		}
		Log.d("DEBUG", "MainTab makeToDoShareDocument End");
		return sbResult.toString();
	}

	/**
	 * Activity遷移処理
	 *
	 */
	public void nextActivity() {
		Log.d("DEBUG", "MainTab nextActivity Start");
		//アプリ内のアクティビティを呼び出すインテントの生成
		Intent intent = new Intent(this, SubcategoryEdit.class);
		//インテントのパラメータ設定
		//カレンダー年月
		intent.putExtra("calym", stdObj.getStrCalendarYearMonth());
		//選択年月日
		intent.putExtra("calymd", stdObj.getStrCalendarYearMonthDay());
		//選択元ユーザーインターフェースID
		intent.putExtra("uiid", getString(R.string.uiid2));
		//キーID
		intent.putExtra("keyid", stdObj.getLgKeyId());
		//アクティビティの呼び出し
		startActivity(intent);
		Log.d("DEBUG", "MainTab nextActivity End");
		//自アクティビティの終了
		endActivity();
	}

	/**
	 * 戻るボタンでカレンダー画面へ遷移
	 *
	 * @param kEvent キーイベント情報 
	 * @return 処理を行った場合はtrue
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent kEvent) {
		Log.d("DEBUG", "MainTab dispatchKeyEvent Start");
		//キー押下されたことを確認
		if (kEvent.getAction() == KeyEvent.ACTION_DOWN) {
			//戻るボタンが押されたか確認
			if (kEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				//「カレンダー」画面に戻る
				//アプリ内のアクティビティを呼び出すインテントの生成
				Intent intent = new Intent(this, MainCalendar.class);
				//インテントのパラメータ設定
				//カレンダー年月
				intent.putExtra("calym", stdObj.getStrCalendarYearMonth());
				//選択年月日
				intent.putExtra("calymd", stdObj.getStrCalendarYearMonthDay());
				//選択元ユーザーインターフェースID
				intent.putExtra("uiid", getString(R.string.uiid2));
				//キーID
				intent.putExtra("keyid", stdObj.getLgKeyId());
				startActivity(intent);
				endActivity();
			}
		}
		Log.d("DEBUG", "MainTab dispatchKeyEvent End");
		return super.dispatchKeyEvent(kEvent);
	}

	/**
	 * 終了確認yes/noダイアログの表示
	 *
	 * @param context コンテキスト
	 * @param titleMsg タイトルメッセージ定数情報
	 * @param mainMsg メッセージ定数情報
	 * @param listener リスナー情報 
	 */
	private static void showYesNoDialog(Context context, int titleMsg, int mainMsg, DialogInterface.OnClickListener listener) {
		Log.d("DEBUG", "MainTab showYesNoDialog Start");
		AlertDialog.Builder ad = new AlertDialog.Builder(context);
		ad.setTitle(titleMsg);
		ad.setMessage(mainMsg);
		ad.setPositiveButton(R.string.yes_btn, listener);
		ad.setNegativeButton(R.string.no_btn, listener);
		ad.show();
		Log.d("DEBUG", "MainTab showYesNoDialog End");
	}

	/**
	 * Activity終了処理
	 *
	 */
	private void endActivity() {
		Log.d("DEBUG", "MainTab endActivity Start");
		//自アクティビティの終了
		finish();
		Log.d("DEBUG", "MainTab endActivity End");
	}

	/**
	 * 画面回転時に呼ばれるサイクル
	 *
	 * @param newConfig 新しい設定値
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d("DEBUG", "MainTab onConfigurationChanged Start");
		super.onConfigurationChanged(newConfig);
		Log.d("DEBUG", "MainTab onConfigurationChanged End");
	}

	/**
	 * onDestroy
	 * データベースヘルパークローズ処理
	 */
	@Override
	public void onDestroy(){
		Log.d("DEBUG", "MainTab onDestroy Start");
		super.onDestroy();
		if(sdhDB != null) {
			sdhDB.close();
		}
		if(tdhDB != null) {
			tdhDB.close();
		}
		Log.d("DEBUG", "MainTab onDestroy End");
	}
}