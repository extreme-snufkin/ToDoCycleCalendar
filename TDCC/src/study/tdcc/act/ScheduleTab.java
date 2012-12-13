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


 クラス名：ScheduleTab
 内容：スケジュールタブレイアウトモジュール
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
          0.1/2012.05.23/T.Mashiko
          0.2/2012.05.25/T.Mashiko
          0.3/2012.05.29/T.Mashiko
          0.4/2012.06.06/T.Mashiko インテントLong型修正
          0.5/2012.06.13/T.Mashiko
          0.6/2012.06.14/T.Mashiko
          0.7/2012.06.15/T.Mashiko
          0.8/2012.06.19/T.Mashiko 端末依存アラーム設定エラー対策
          0.9/2012.07.04/T.Mashiko 削除処理の仕様矛盾対応
          1.0/2012.07.08/T.Mashiko 端末依存アラーム設定エラー対策
          1.1/2012.07.11/T.Mashiko 日送りフリック対応
          1.2/2012.07.18/T.Mashiko ロギング表記修正
          1.3/2012.08.23/T.Mashiko ボタンの連打ロック対策,ダイアログの戻るキー対策
          1.4/2012.09.05/T.Mashiko AsyncTaskのエラー処理の条件修正,非同期処理内にてUI操作(トースト)を実行していた為、修正対応
          1.5/2012.09.10/T.Mashiko スケジュールデータの論理削除時に更新日時をアップデートしていない問題の対処
*/
package study.tdcc.act;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import study.tdcc.*;
import study.tdcc.adapter.*;
import study.tdcc.bean.*;
import study.tdcc.lib.*;
import study.tdcc.lib.ScheduleDatabaseHelper.ScheduleCursor;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class ScheduleTab extends Activity implements DialogInterface.OnCancelListener {
	//スケジュールリストビュー
	private ListView lvScheduleList;
	//アラーム設定ボタンビュー
	private Button btnAlart;
	//データベースオブジェクト
	private ScheduleDatabaseHelper sdhDB;
	//画面遷移データオブジェクト
	private ScreenTransitionData stdObj = new ScreenTransitionData();
	//リストビュー用アダプタ
	private ScheduleListItemAdapter sliaObj;
	//Scheduleリスト
	private List<ScheduleListRow> lSLRObj;
	//カスタムダイアログ内 Scheduleリスト指定位置情報
	private int intPossition;
	//ビューフリッパー
	private ViewFlipper vfList;
	//ジェスチャーオブジェクト
	private GestureDetector gdObj;
	//フリック連打防止フラグ
	private boolean blFlick = true;
	//ダイアログ内ボタン連打防止フラグ
	private boolean blDialogButton = true;

	//タッチ処理リスナー
	OnTouchListener otlObj = new OnTouchListener() {
		public boolean onTouch(View view, MotionEvent event) {
			if (gdObj.onTouchEvent(event))
				return true;
			return false;
		}
	};

	//ジェスチャーリスナー
	OnGestureListener oglObj = new OnGestureListener() {
		public boolean onDown(MotionEvent event) {
			Log.d("DEBUG", "ScheduleTab onDown Start");
			Log.d("DEBUG", "ScheduleTab onDown End");
			return false;
		}
		//フリック処理を実装
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			Log.d("DEBUG", "ScheduleTab onFling Start");
			float flCoordX = Math.abs(velocityX);
			float flCoordY = Math.abs(velocityY);
			GregorianCalendar gcTargetYearMonthDay = new GregorianCalendar();
			if (flCoordX > flCoordY && flCoordX > 150) {
				if(blFlick == true) {
					//フリック連打防止フラグ
					blFlick = false;
					gcTargetYearMonthDay = DateUtil.toCalendar(stdObj.getStrCalendarYearMonthDay());
					if (e1.getX() < e2.getX()) {
						//1日減算する
						gcTargetYearMonthDay.add(Calendar.DAY_OF_MONTH, -1);
						//下限日補正
						long lgLowerLimitTime = DateUtil.convMSec(getString(R.string.lower_limit_time));
						if(gcTargetYearMonthDay.getTimeInMillis() < lgLowerLimitTime) {
							gcTargetYearMonthDay.add(Calendar.DAY_OF_MONTH, 1);
							Log.d("DEBUG", "ScheduleTab onFling   Lower limit correction implementation.");
						}
					} else {
						//1日加算する
						gcTargetYearMonthDay.add(Calendar.DAY_OF_MONTH, 1);
						//上限月補正
						long lgUpperLimitTime = DateUtil.convMSec(getString(R.string.upper_limit_time));
						if(gcTargetYearMonthDay.getTimeInMillis() > lgUpperLimitTime) {
							gcTargetYearMonthDay.add(Calendar.DAY_OF_MONTH, -1);
							Log.d("DEBUG", "ScheduleTab onFling   Upper limit correction implementation.");
						}
					}
					//yyyy-MM-dd 年月日文字列を生成
					stdObj.setStrCalendarYearMonthDay(DateUtil.DATE_FORMAT.format(gcTargetYearMonthDay.getTime()));
					//MainTabへの画面遷移
					nextActivity("flick");
					Log.d("DEBUG", "ScheduleTab onFling End");
					return true;
				}
			}
			Log.d("DEBUG", "ToDoTab onFling End");
			return false;
		}
		public void onLongPress(MotionEvent arg0) {
			Log.d("DEBUG", "ScheduleTab onLongPress Start");
			Log.d("DEBUG", "ScheduleTab onLongPress End");
		}
		public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
			Log.d("DEBUG", "ScheduleTab onScroll Start");
			Log.d("DEBUG", "ScheduleTab onScroll End");
			return false;
		}
		public void onShowPress(MotionEvent arg0) {
			Log.d("DEBUG", "ScheduleTab onShowPress Start");
			Log.d("DEBUG", "ScheduleTab onShowPress End");
		}
		public boolean onSingleTapUp(MotionEvent arg0) {
			Log.d("DEBUG", "ScheduleTab onSingleTapUp Start");
			Log.d("DEBUG", "ScheduleTab onSingleTapUp End");
			return false;
		}
	};

	/**
	 * onCreate
	 * @param savedInstanceState バンドル
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("DEBUG", "ScheduleTab onCreate Start");
		super.onCreate(savedInstanceState);
		//スケジュールタブビュー描画処理
		setContentView(R.layout.scheduletab);
		//画面遷移情報の取得
		getScreenTransitionData();
		//画面要素の取得処理
		getViewElement();
		//画面要素のリスナーセット
		setViewListener();
		//画面要素へのデータセット(非同期処理)
		setViewElement();
		//フリック連打防止フラグ
		blFlick = true;
		Log.d("DEBUG", "ScheduleTab onCreate End");
	}

	/**
	 * 画面遷移情報の取得
	 *
	 */
	private void getScreenTransitionData() {
		Log.d("DEBUG", "ScheduleTab getScreenTransitionData Start");
		//画面遷移時のインテント格納値取得
		Bundle bExtras = getIntent().getExtras();
		if (bExtras != null) {
			stdObj.setStrCalendarYearMonth(bExtras.getString("calym"));
			stdObj.setStrCalendarYearMonthDay(bExtras.getString("calymd"));
			stdObj.setStrUserInterfaceId(bExtras.getString("uiid"));
			stdObj.setLgKeyId(bExtras.getLong("keyid"));
		}
		Log.d("DEBUG", "ScheduleTab getScreenTransitionData End");
	}

	/**
	 * 画面要素の取得
	 *
	 */
	private void getViewElement() {
		Log.d("DEBUG", "ScheduleTab getViewElement Start");
		//アラーム設定ボタンビュー
		btnAlart = (Button)this.findViewById(R.id.alarmbutton);
		//スケジュールリストビュー
		lvScheduleList = (ListView)this.findViewById(R.id.schedulelist);
		//ビューフリッパー
		vfList = (ViewFlipper)this.findViewById(R.id.vfList);
		//ジェスチャーディテクターを生成
		gdObj = new GestureDetector(this, oglObj);
		Log.d("DEBUG", "ScheduleTab getViewElement End");
	}

	/**
	 * 画面要素へのリスナーセット
	 *
	 */
	private void setViewListener() {
		Log.d("DEBUG", "ScheduleTab setViewListener Start");
		//スケジュールリストビューのリスナー
		lvScheduleList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				//スケジュール編集画面表示処理
				dispScheduleEditScreen(position);
			}
		});
		lvScheduleList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
				//スケジュール削除ダイアログ表示処理
				dispScheduleDeleteDialog(position);
				return false;
			}
		});
		lvScheduleList.setOnTouchListener(otlObj);
		Log.d("DEBUG", "ScheduleTab setViewListener End");
	}

	/**
	 * スケジュール編集画面表示処理
	 *
	 * @position リストポジション
	 * 
	 */
	public void dispScheduleEditScreen(int position) {
		Log.d("DEBUG", "ScheduleTab dispScheduleEditScreen Start");
		intPossition = position;
		//選択スケジュール情報取得
		ScheduleListRow slrObj = new ScheduleListRow();
		slrObj = lSLRObj.get(intPossition);
		stdObj.setLgKeyId(slrObj.getLgID());
		nextActivity("update");
		Log.d("DEBUG", "ScheduleTab dispScheduleEditScreen End");
	}

	/**
	 * スケジュール削除ダイアログ表示処理
	 *
	 * @position リストポジション
	 * 
	 */
	public void dispScheduleDeleteDialog(int position) {
		Log.d("DEBUG", "ScheduleTab dispScheduleDeleteDialog Start");
		intPossition = position;
		AlertDialog.Builder adObj = new AlertDialog.Builder(this);
		//ダイアログ内ボタン連打ロックオフ
		blDialogButton = true;
		//ダイアログタイトル設定
		adObj.setTitle(getString(R.string.scdeletedialog_title));
		//ダイアログメッセージ設定
		adObj.setMessage(getString(R.string.deletedialog_msg));
		//アラートダイアログのタッチイベントを設定
		adObj.setPositiveButton(getString(R.string.exec_btn), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(blDialogButton == true) {
					//ダイアログ内ボタン連打ロックオン
					blDialogButton = false;
					//スケジュール削除処理
					boolean blDeleteResult = deleteSCHEDULE(intPossition);
					if(blDeleteResult == false) {
						Toast.makeText(ScheduleTab.this, getString(R.string.sqlite_write_err), Toast.LENGTH_LONG).show();
						dialog.dismiss();
						endActivity();
					}
					setViewElement();
					dialog.dismiss();
				}
			}
		});
		adObj.setNegativeButton(getString(R.string.cancel_btn), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(blDialogButton == true) {
					//ダイアログ内ボタン連打ロックオン
					blDialogButton = false;
					dialog.dismiss();
				}
			}
		});
		adObj.show();
		Log.d("DEBUG", "ScheduleTab dispScheduleDeleteDialog End");
	}

	/**
	 * スケジュール情報のデータベース削除処理
	 *
	 * @intPosition リストポジション
	 * @return 処理を行った場合はtrue
	 */
	private boolean deleteSCHEDULE(int intPosition) {
		Log.d("DEBUG", "ScheduleTab deleteSCHEDULE Start");
		boolean blResult = true;
		//ScheduleDatabaseHelper初期化
		sdhDB = new ScheduleDatabaseHelper(this);
		//選択スケジュール情報取得
		ScheduleListRow slrObj = new ScheduleListRow();
		slrObj = lSLRObj.get(intPossition);
		//SQLパラメータ作成(ID)
		Schedule objSchedule = new Schedule();
		objSchedule.setLgId(slrObj.getLgID());
		objSchedule.setLgDeleteFlag(1l);
		objSchedule.setLgModified(1l);
		//カレンダーIDが存在したら論理削除するべき
		if(slrObj.getStrCalendarID() == null || slrObj.getStrCalendarID().equals("")) {
			//SCHEDULEテーブル削除(物理削除)
			blResult = sdhDB.deleteScheduleId(objSchedule);
		} else {
			//SCHEDULEテーブル更新(論理削除)
			objSchedule.setStrUpdated(DateUtil.toDBDateString(new GregorianCalendar()));
			blResult = sdhDB.updateScheduleDelete(objSchedule);
		}
		Log.d("DEBUG", "ScheduleTab deleteSCHEDULE End");
		return blResult;
	}

	/**
	 * 画面要素へのデータセット
	 *
	 */
	private void setViewElement() {
		Log.d("DEBUG", "ScheduleTab setViewElement Start");
		//Scheduleリストビューの生成
		lvScheduleList = (ListView) vfList.getCurrentView();
		//操作時の年月日取得
		GregorianCalendar gcNowYearMonthDay = new GregorianCalendar();
		String strNowDate = DateUtil.DATE_FORMAT.format(gcNowYearMonthDay.getTime());
		//当該タブの年月日取得
		if(stdObj.getStrCalendarYearMonthDay().equals(strNowDate)) {
			//当日のスケジュールリストの場合
			btnAlart.setVisibility(0);
		}
		//スケジュールリスト用データ読込
		setScheduleList();
		Log.d("DEBUG", "ScheduleTab setViewElement End");
	}

	/**
	 * スケジュールリスト用データ読込処理
	 *
	 */
	private void setScheduleList() {
		Log.d("DEBUG", "ScheduleTab setScheduleList Start");
		//スケジュールリスト取得処理(非同期処理)
		GetScheduleListTask task = new GetScheduleListTask();
		task.execute();
		Log.d("DEBUG", "ScheduleTab setScheduleList End");
	}

	/**
	 * afterGetScheduleList
	 *  スケジュールリスト作成処理後の処理
	 * @param strReturnCode 結果コード
	 * @param SLRList スケジュールリスト
	 */
	//スケジュールリスト作成処理後の遷移処理
	public void afterGetScheduleList(String strReturnCode, List<ScheduleListRow> SLRList) {
		Log.d("DEBUG", "ScheduleTab afterGetScheduleList Start");
		//スケジュールリスト取得処理の結果受け取り
		Log.d("DEBUG","ScheduleTab afterGetScheduleList strReturnCode : " + strReturnCode);
		if(!(strReturnCode == null || strReturnCode.equals("")) && strReturnCode.equals("Success")) {
			//エラー無しの場合
			lSLRObj = SLRList;
			//ListItemAdapterを生成
			sliaObj = new ScheduleListItemAdapter(this, 0, lSLRObj);
			//スケジュールリストビューにアダプタをセット
			lvScheduleList.setAdapter(sliaObj);
		} else {
			//エラー有りの場合
			//スケジュールタブ画面にエラートースト表示
			Toast.makeText(ScheduleTab.this, getString(R.string.sqlite_read_err), Toast.LENGTH_LONG).show();
			endActivity();
		}
		Log.d("DEBUG", "ScheduleTab afterGetScheduleList End");
	}

	/**
	 * newAdd
	 *  新規追加ボタン押下時の処理
	 * @param view 選択ビュー
	 */
	public void newAdd(View view) {
		Log.d("DEBUG", "ScheduleTab newAdd Start");
		if(blFlick == true) {
			//ボタン連打ロックオン
			blFlick = false;
			nextActivity("add");
		}
		Log.d("DEBUG", "ScheduleTab newAdd End");
	}

	/**
	 * Activity遷移処理
	 *
	 * @param strNext add:スケジュール登録 or update:スケジュール編集 or flick:MainTab
	 */
	public void nextActivity(String strNext) {
		Log.d("DEBUG", "ScheduleTab nextActivity Start");
		// アプリ内のアクティビティを呼び出すインテントの生成
		Intent intent;
		if(strNext.equals("add")) {
			//スケジュール登録画面遷移の場合
			intent = new Intent(this, ScheduleRegistration.class);
			//インテントのパラメータ設定
			//キーID
			intent.putExtra("keyid", 0l);
		} else if(strNext.equals("update")) {
			//スケジュール編集画面遷移の場合
			intent = new Intent(this, ScheduleRegistration.class);
			//インテントのパラメータ設定
			//キーID
			intent.putExtra("keyid", stdObj.getLgKeyId());
		} else {
			//スケジュールメインタブ
			intent = new Intent(this, MainTab.class);
			//インテントのパラメータ設定
			//キーID
			intent.putExtra("keyid", 0l);
		}
		//カレンダー年月
		intent.putExtra("calym", stdObj.getStrCalendarYearMonth());
		//選択年月日
		intent.putExtra("calymd", stdObj.getStrCalendarYearMonthDay());
		//選択元ユーザーインターフェースID
		intent.putExtra("uiid", getString(R.string.uiid3));
		//アクティビティの呼び出し
		startActivity(intent);
		Log.d("DEBUG", "ScheduleTab nextActivity End");
		//自アクティビティの終了
		endActivity();
	}

	/**
	 * Activity終了処理
	 *
	 */
	private void endActivity() {
		Log.d("DEBUG", "ScheduleTab endActivity Start");
		//自アクティビティの終了
		finish();
		Log.d("DEBUG", "ScheduleTab endActivity End");
	}

	/**
	 * 画面回転時に呼ばれるサイクル
	 *
	 * @param newConfig 新しい設定値
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d("DEBUG", "ScheduleTab onConfigurationChanged Start");
		super.onConfigurationChanged(newConfig);
		Log.d("DEBUG", "ScheduleTab onConfigurationChanged End");
	}

	/**
	 * onDestroy
	 * データベースヘルパークローズ処理
	 */
	@Override
	public void onDestroy(){
		Log.d("DEBUG", "ScheduleTab onDestroy Start");
		super.onDestroy();
		if(sdhDB != null) {
			sdhDB.close();
		}
		Log.d("DEBUG", "ScheduleTab onDestroy End");
	}

	/**
	 * alarmSetting
	 *  アラーム設定ボタン押下時の処理
	 * @param view 選択ビュー
	 */
	//アラーム設定 ボタンクリック時に呼ばれる
	public void alarmSetting(View view) {
		Log.d("DEBUG", "ScheduleTab alarmSetting Start");
		if(blFlick == true) {
			//ボタン連打ロックオン
			blFlick = false;
			dispScheduleAlarmSetDialog();
		}
		Log.d("DEBUG", "ScheduleTab alarmSetting End");
	}

	/**
	 * アラーム設定ダイアログ表示処理
	 *
	 */
	public void dispScheduleAlarmSetDialog() {
		Log.d("DEBUG", "ScheduleTab dispScheduleAlarmSetDialog Start");
		AlertDialog.Builder adObj = new AlertDialog.Builder(this);
		//ダイアログ内ボタン連打ロックオフ
		blDialogButton = true;
		//ダイアログタイトル設定
		adObj.setTitle(getString(R.string.scalarmset_title));
		//ダイアログメッセージ設定
		adObj.setMessage(getString(R.string.scalarmset_msg1));
		//アラートダイアログのタッチイベントを設定
		adObj.setPositiveButton(getString(R.string.exec_btn), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(blDialogButton == true) {
					//ダイアログ内ボタン連打ロックオン
					blDialogButton = false;
					boolean blError = false;
					int intAlartCount = 0;
					//実行タイミングのエポックミリ秒取得
					GregorianCalendar gcNowYearMonthDay = new GregorianCalendar();
					long lgNowTime = gcNowYearMonthDay.getTimeInMillis();
					//アラーム設定処理
					for (Iterator<ScheduleListRow> iSLR = lSLRObj.iterator(); iSLR.hasNext();) {
						ScheduleListRow objSLR = iSLR.next();
						if(objSLR.getLgStartTime() >= lgNowTime && objSLR.getLgAlarmFlag() == 1) {
							//アラーム設定を要求するインテントを作成
							Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
							//アラーム起動時刻を設定
							//開始時分
							String[] strArrayTime = DateUtil.toDivideTime(objSLR.getStrStartTime());
							intent.putExtra(AlarmClock.EXTRA_HOUR, Integer.parseInt(strArrayTime[0]));
							intent.putExtra(AlarmClock.EXTRA_MINUTES, Integer.parseInt(strArrayTime[1]));
							intent.putExtra(AlarmClock.EXTRA_MESSAGE, objSLR.getStrTitle());
							try {
								//インテントを発行
								startActivity(intent);
							} catch(Exception e) {
								blError = true;
								Log.e("ERROR", "ScheduleTab dispScheduleAlarmSetDialog", e);
								Toast.makeText(ScheduleTab.this, getString(R.string.scalarmset_err), Toast.LENGTH_LONG).show();
								break;
							}
							intAlartCount++;
						}
					}
					if(blError == false) {
						if(intAlartCount == 0) {
							Toast.makeText(ScheduleTab.this, getString(R.string.scalarmset_msg2), Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(ScheduleTab.this, getString(R.string.scalarmset_msg3), Toast.LENGTH_LONG).show();
						}
					}
					//ボタン連打ロックオフ
					blFlick = true;
					dialog.dismiss();
				}
			}
		});
		adObj.setNegativeButton(getString(R.string.cancel_btn), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(blDialogButton == true) {
					//ダイアログ内ボタン連打ロックオン
					blDialogButton = false;
					//ボタン連打ロックオフ
					blFlick = true;
					dialog.dismiss();
				}
			}
		});
		adObj.setOnCancelListener(this);
		adObj.show();
		Log.d("DEBUG", "ScheduleTab dispScheduleAlarmSetDialog End");
	}

	/**
	 * onCancel
	 * ダイアログ戻るボタン処理
	 *
	 * @param dialog dialog情報 
	 */
	@Override
	public void onCancel(DialogInterface dialog) {
		Log.d("DEBUG", "ScheduleTab onCancel Start");
		//ダイアログ表示時に戻るボタンが押下された場合
		//ボタン連打ロックオフ
		blFlick = true;
		Log.d("DEBUG", "ScheduleTab onCancel End");
	}

	//スケジュールリスト作成処理(内部クラス)
	private class GetScheduleListTask extends AsyncTask<String, Integer, StringBuilder>{
		//スリープ設定情報読み込み
		private long lgSleepTime = Long.parseLong(getString(R.string.scheduletab_sleep));
		//ダイアログ
		private ProgressDialog pdObj;
		//スケジュールテーブルリスト(終日スケジュール以外の一時リスト)
		private List<ScheduleListRow> lSLRTemp;
		//スケジュールテーブルリスト
		private List<ScheduleListRow> lSLR;
		
		//実行前準備処理(インジケータのセットアップ)
		@Override
		protected void onPreExecute() {
			Log.d("DEBUG","GetScheduleListTask onPreExecute Start");
			//ダイアログの生成
			pdObj = new ProgressDialog(ScheduleTab.this);
			//メッセージのセット
			pdObj.setMessage(getString(R.string.scload_msg));
			//ダイアログの戻るキー無効
			pdObj.setCancelable(false);
			//ダイアログ表示
			pdObj.show();
			Log.d("DEBUG","GetScheduleListTask onPreExecute End");
		}

		//バックグラウンド実行処理(本体)
		@Override
		protected StringBuilder doInBackground(String... params) {
			Log.d("DEBUG","GetScheduleListTask doInBackground Start");
			StringBuilder sbResult = new StringBuilder();
			try {
				//指定年月日を取得
				GregorianCalendar clStartTargetDate = DateUtil.toCalendar(stdObj.getStrCalendarYearMonthDay());
				//当日開始時間のミリ秒
				long lgStartMSec = clStartTargetDate.getTimeInMillis();
				//当日終了時間のミリ秒
				clStartTargetDate.add(Calendar.DAY_OF_MONTH, 1);
				long lgEndMSec = clStartTargetDate.getTimeInMillis();
				//ScheduleDatabaseHelper初期化
				sdhDB = new ScheduleDatabaseHelper(ScheduleTab.this);
				publishProgress(1);
				Thread.sleep(lgSleepTime);
				//スケジュールリスト取得
				boolean blScheduleResult = selectSchedule(stdObj.getStrCalendarYearMonthDay(), lgStartMSec,lgEndMSec);
				if(blScheduleResult == false) {
					sbResult.append("Error");
				}
				publishProgress(2);
				Thread.sleep(lgSleepTime);
				sbResult.append("Success");
			} catch (Exception e) {
				Log.e("ERROR","GetScheduleListTask",e);
				//予期せぬException(スレッドエラー）
				sbResult.append("Error");
			}
			Log.d("DEBUG","GetScheduleListTask doInBackground End");
			return sbResult;
		}

		//バックグラウンド処理の進捗状況をUIスレッドで表示する為の処理
		@Override
		protected void onProgressUpdate(Integer... values) {
			Log.d("DEBUG","GetScheduleListTask onProgressUpdate Start");
			//ダイアログのメッセージ変更
			pdObj.setMessage(getString(R.string.scload_msg) + values[0] + "/2");
			Log.d("DEBUG","GetScheduleListTask onProgressUpdate End");
		}

		//バックグランド処理終了後のUIスレッド呼び出し処理
		@Override
		protected void onPostExecute(StringBuilder result) {
			Log.d("DEBUG","GetScheduleListTask onPostExecute Start");
			//ダイアログ終了
			pdObj.dismiss();
			//スケジュールリスト作成処理の結果コードとリストデータ返却
			afterGetScheduleList(result.toString(), lSLR);
			Log.d("DEBUG","GetScheduleListTask onPostExecute End");
		}

		/**
		 * スケジュール情報のデータベース読み込み処理
		 *
		 * @lgStartMSec 当日開始時間のミリ秒
		 * @lgEndMSec 当日終了時間のミリ秒
		 * @return 処理を行った場合はtrue
		 */
		private boolean selectSchedule(String strYMD, long lgStartMSec, long lgEndMSec) {
			Log.d("DEBUG", "GetScheduleListTask selectSchedule Start");
			boolean blResult = true;
			ScheduleCursor scObj = null;
			lSLR = new ArrayList<ScheduleListRow>();
			lSLRTemp = new ArrayList<ScheduleListRow>();
			try {
				String[] where_args = {String.valueOf(lgStartMSec), String.valueOf(lgEndMSec), String.valueOf(lgEndMSec), String.valueOf(lgStartMSec), String.valueOf(lgEndMSec), String.valueOf(lgStartMSec), String.valueOf(lgEndMSec)};
				//カーソルの取得
				scObj = sdhDB.getScheduleList(where_args);
				//カーソルポインター初期化
				startManagingCursor(scObj);
				Log.d("DEBUG", "GetScheduleListTask selectSchedule ScheduleCursor Count : " + scObj.getCount());
				for( int intCt=0; intCt<scObj.getCount(); intCt++){
					//スケジュールのセット
					ScheduleListRow slrObj = new ScheduleListRow();
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
						//開始日時(数値)
						slrObj.setLgStartTime(scObj.getColStarttime());
						//端末アラーム対象フラグ
						slrObj.setLgAlarmFlag(scObj.getColAlarmFlag());
						//スケジュールキーID
						slrObj.setLgID(scObj.getColId());
						//カレンダーID
						slrObj.setStrCalendarID(scObj.getColCalendarId());
						lSLR.add(slrObj);
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
						//開始日時(数値)
						slrObj.setLgStartTime(scObj.getColStarttime());
						//端末アラーム対象フラグ
						slrObj.setLgAlarmFlag(scObj.getColAlarmFlag());
						//スケジュールキーID
						slrObj.setLgID(scObj.getColId());
						//カレンダーID
						slrObj.setStrCalendarID(scObj.getColCalendarId());
						lSLRTemp.add(slrObj);
					}
					scObj.moveToNext();
				}
				scObj.close();
				//終日のみのリストに終日以外のリストを追加
				for (Iterator<ScheduleListRow> iSLR = lSLRTemp.iterator(); iSLR.hasNext();) {
					ScheduleListRow objSLR = iSLR.next();
					lSLR.add(objSLR);
				}
			} catch (SQLException e) {
				blResult = false;
				Log.e("ERROR", "GetScheduleListTask selectSchedule DB Error",e);
			} finally {
				if(scObj != null) {
					scObj.close();
				}
			}
			Log.d("DEBUG", "GetScheduleListTask selectSchedule End");
			return blResult;
		}
	}
}