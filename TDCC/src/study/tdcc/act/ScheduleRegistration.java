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


 クラス名：ScheduleRegistration
 内容：スケジュール登録・編集
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
          0.1/2012.05.25/T.Mashiko
          0.2/2012.05.26/T.Mashiko
          0.3/2012.06.06/T.Mashiko インテントLong型修正
          0.4/2012.06.11/T.Mashiko
          0.5/2012.06.12/T.Mashiko
          0.6/2012.06.13/T.Mashiko
          0.7/2012.07.09/T.Mashiko 入力日付の上限・下限値チェック,タイトル空登録対応、タイトル・場所・内容文字数チェック追加
          0.8/2012.07.18/T.Mashiko ロギング表記修正
          0.9/2012.08.23/T.Mashiko ボタンの連打ロック対策,ダイアログの戻るキー対策
          1.0/2012.09.07/T.Mashiko Googleカレンダーの同期時に同値更新が無限ループされてしまう問題の対応(同値更新時(アラーム設定含む)に更新フラグをセットしない)
          1.1/2012.09.27/T.Mashiko 強制終了対策
*/
package study.tdcc.act;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import study.tdcc.*;
import study.tdcc.bean.*;
import study.tdcc.lib.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.SQLException;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class ScheduleRegistration extends Activity {
	//カスタムタイトルテキストビュー
	private TextView tvCustomTitle;
	//カスタムタイトル(バージョン)テキストビュー
	private TextView tvCustomTitleVersion;
	//タイトルエディットテキストビュー
	private EditText etTitle;
	//タイトル音声入力イメージビュー
	private ImageView ivMicIcon1;
	//開始年月日テキストビュー
	private TextView tvStartDate;
	//開始時分テキストビュー
	private TextView tvStartTime;
	//終了年月日テキストビュー
	private TextView tvEndDate;
	//終了時分テキストビュー
	private TextView tvEndTime;
	//終了チェックボックス
	private CheckBox cbAllDay;
	//場所エディットテキストビュー
	private EditText etPlace;
	//場所音声入力イメージビュー
	private ImageView ivMicIcon2;
	//内容音声入力イメージビュー
	private ImageView ivMicIcon3;
	//内容エディットテキストビュー
	private EditText etContent;
	//アラームスピナー
	private Spinner sAlarm;
	//リクエストコード
	private static final int REQUEST_CODE1 = 51;
	private static final int REQUEST_CODE2 = 52;
	private static final int REQUEST_CODE3 = 53;
	//データベースオブジェクト
	private ScheduleDatabaseHelper sdhDB;
	//画面表示用スケジュールオブジェクト(スケジュール編集画面時に使用)
	private Schedule sOrgObj;
	//画面遷移データオブジェクト
	private ScreenTransitionData stdObj = new ScreenTransitionData();
	//ボタン連打防止フラグ
	private boolean blSave = true;
	//ダイアログ内ボタン連打防止フラグ
	private boolean blDialogButton = true;
	
	/**
	 * onCreate
	 * @param savedInstanceState バンドル
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("DEBUG", "ScheduleRegistration onCreate Start");
		super.onCreate(savedInstanceState);
		//window がフォーカスを受けたときに常に soft input area を隠す
		this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		//カスタムタイトル
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		//スケジュール登録・編集画面描画処理
		setContentView(R.layout.scheduleregistration);
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
		setViewElement();
		//画面要素のリスナーセット
		setViewListener();
		//ボタン連打防止フラグ
		blSave = true;
		Log.d("DEBUG", "ScheduleRegistration onCreate End");
	}

	/**
	 * 画面遷移情報の取得
	 *
	 */
	private void getScreenTransitionData() {
		Log.d("DEBUG", "ScheduleRegistration getScreenTransitionData Start");
		//画面遷移時のインテント格納値取得
		Bundle bExtras = getIntent().getExtras();
		if (bExtras != null) {
			stdObj.setStrCalendarYearMonth(bExtras.getString("calym"));
			stdObj.setStrCalendarYearMonthDay(bExtras.getString("calymd"));
			stdObj.setStrUserInterfaceId(bExtras.getString("uiid"));
			stdObj.setLgKeyId(bExtras.getLong("keyid"));
		}
		Log.d("DEBUG", "ScheduleRegistration getScreenTransitionData End");
	}

	/**
	 * 画面要素の取得
	 *
	 */
	private void getViewElement() {
		Log.d("DEBUG", "ScheduleRegistration getViewElement Start");
		//カスタムタイトルテキストビュー
		tvCustomTitle = (TextView)this.findViewById(R.id.titletext);
		//カスタムタイトルバージョンビュー
		tvCustomTitleVersion = (TextView)this.findViewById(R.id.titleversion);
		//タイトルエディットテキストビュー
		etTitle = (EditText)this.findViewById(R.id.ettitle);
		//タイトル音声入力イメージビュー
		ivMicIcon1 = (ImageView)this.findViewById(R.id.ivmicicon1);
		//開始年月日テキストビュー
		tvStartDate = (TextView)this.findViewById(R.id.tvstartdate);
		//開始時分テキストビュー
		tvStartTime = (TextView)this.findViewById(R.id.tvstarttime);
		//終了年月日テキストビュー
		tvEndDate = (TextView)this.findViewById(R.id.tvenddate);
		//終了時分テキストビュー
		tvEndTime = (TextView)this.findViewById(R.id.tvendtime);
		//終了チェックボックス
		cbAllDay = (CheckBox)this.findViewById(R.id.cballday);
		//場所エディットテキストビュー
		etPlace = (EditText)this.findViewById(R.id.etplace);
		//場所音声入力イメージビュー
		ivMicIcon2 = (ImageView)this.findViewById(R.id.ivmicicon2);
		//内容音声入力イメージビュー
		ivMicIcon3 = (ImageView)this.findViewById(R.id.ivmicicon3);
		//内容エディットテキストビュー
		etContent = (EditText)this.findViewById(R.id.etcontent);
		//アラームスピナー
		sAlarm = (Spinner)this.findViewById(R.id.salarm);
		Log.d("DEBUG", "ScheduleRegistration getViewElement End");
	}

	/**
	 * カスタムタイトルの内容セット
	 *
	 */
	private void setCustomTitle() {
		Log.d("DEBUG", "ScheduleRegistration setCustomTitle Start");
		if(stdObj.getLgKeyId() == 0) {
			//遷移先画面がスケジュール登録画面の場合
			Log.d("DEBUG", "ScheduleRegistration setViewElement UI:Schedule Registration");
			tvCustomTitle.setText(getString(R.string.act_name4));
		} else {
			//遷移先画面がスケジュール編集画面の場合
			Log.d("DEBUG", "ScheduleRegistration setViewElement UI:Schedule Edit");
			tvCustomTitle.setText(getString(R.string.act_name5));
		}
		StringBuilder sbVersion = new StringBuilder();
		sbVersion.append(getString(R.string.title_version));
		PackageManager pmObj = this.getPackageManager();
		try {
			PackageInfo piObj = pmObj.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
			sbVersion.append(piObj.versionName);
		} catch (NameNotFoundException e) {
			Log.e("ERROR", "ScheduleRegistration setCustomTitle NameNotFoundException", e);
		}
		tvCustomTitleVersion.setText(sbVersion.toString());
		Log.d("DEBUG", "ScheduleRegistration setCustomTitle End");
	}

	/**
	 * 画面要素へのデータセット
	 *
	 */
	private void setViewElement() {
		Log.d("DEBUG", "ScheduleRegistration setViewElement Start");
		if(stdObj.getLgKeyId() == 0l) {
			//遷移先画面がスケジュール登録画面の場合
			Log.d("DEBUG", "ScheduleRegistration setViewElement UI:Schedule Registration");
			//指定年月日を取得
			Calendar clTargetDate = DateUtil.toCalendar(stdObj.getStrCalendarYearMonthDay());
			//年月日のTextViewの初期値設定
			tvStartDate.setText(DateUtil.DATE_FORMAT.format(clTargetDate.getTime()));
			tvEndDate.setText(DateUtil.DATE_FORMAT.format(clTargetDate.getTime()));
			//時分のTextViewの初期値設定
			tvStartTime.setText(DateUtil.TIME_FORMAT.format(clTargetDate.getTime()));
			tvEndTime.setText(DateUtil.TIME_FORMAT.format(clTargetDate.getTime()));
		} else {
			//遷移先画面がスケジュール編集画面の場合
			Log.d("DEBUG", "ScheduleRegistration setViewElement UI:Schedule Edit");
			//スケジュール編集画面DB読み込み
			sdhDB = new ScheduleDatabaseHelper(this);
			boolean blScheduleResult = selectSchedule();
			if(blScheduleResult == false) {
				Toast.makeText(ScheduleRegistration.this, getString(R.string.sqlite_read_err), Toast.LENGTH_LONG).show();
				endActivity();
			}
		}
		//マイク機能が使用可能か確認し、使用不可能な場合、非表示とする
		boolean blHasMicFeature = getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
		if(blHasMicFeature) {
			//マイク機能が使用可能な場合
			ivMicIcon1.setVisibility(View.VISIBLE);
			ivMicIcon2.setVisibility(View.VISIBLE);
			ivMicIcon3.setVisibility(View.VISIBLE);
			Log.d("DEBUG", "ScheduleRegistration setViewElement FEATURE_MICROPHONE:OK");
		} else {
			//マイク機能が使用不可能な場合
			ivMicIcon1.setVisibility(View.INVISIBLE);
			ivMicIcon2.setVisibility(View.INVISIBLE);
			ivMicIcon3.setVisibility(View.INVISIBLE);
			Log.d("DEBUG", "ScheduleRegistration setViewElement FEATURE_MICROPHONE:OUT");
		}
		Log.d("DEBUG", "ScheduleRegistration setViewElement End");
	}

	/**
	 * Schedule情報のデータベース読み込み処理
	 *
	 * @return 処理を行った場合はtrue
	 */
	private boolean selectSchedule() {
		Log.d("DEBUG", "ScheduleRegistration selectSchedule Start");
		boolean blResult = true;
		ScheduleDatabaseHelper.ScheduleCursor sdObj = null;
		//Schedule scheduleObj = new Schedule();
		sOrgObj = new Schedule();
		try {
			//データベース検索処理
			String[] where_args = {String.valueOf(stdObj.getLgKeyId())};
			//カーソルの取得
			sdObj = sdhDB.getSchedule(where_args);
			//カーソルの取得
			startManagingCursor(sdObj);
			Log.d("DEBUG", "ScheduleRegistration selectSchedule ScheduleCursor Count : " + sdObj.getCount());
			for( int intCt=0; intCt<sdObj.getCount(); intCt++){
				//Scheduleデータ格納
				//ID
				sOrgObj.setLgId(sdObj.getColId());
				//TITLE
				sOrgObj.setStrTitle(sdObj.getColTitle());
				//CONTENT
				sOrgObj.setStrContent(sdObj.getColContent());
				//GD_WHERE
				sOrgObj.setStrGDWhere(sdObj.getColGdWhere());
				//GD_WHEN_ENDTIME
				sOrgObj.setStrGDWhenEndTime(sdObj.getColGdWhenEndtime());
				//GD_WHEN_STARTTIME
				sOrgObj.setStrGDWhenStartTime(sdObj.getColGdWhenStarttime());
				//CALENDAR_ID
				sOrgObj.setStrCalendarId(sdObj.getColCalendarId());
				//ENDTIME
				sOrgObj.setLgEndTime(sdObj.getColEndtime());
				//STARTTIME
				sOrgObj.setLgStartTime(sdObj.getColStarttime());
				//ALARM_FLAG
				sOrgObj.setLgAlarmFlag(sdObj.getColAlarmFlag());
				sdObj.moveToNext();
			}
			sdObj.close();
			//取得スケジュールデータのビュー格納処理
			setViewData();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ScheduleRegistration selectSchedule DB Error",e);
		} finally {
			if(sdObj != null) {
				sdObj.close();
			}
		}
		Log.d("DEBUG", "ScheduleRegistration selectSchedule End");
		return blResult;
	}

	/**
	 * 画面要素への登録済みScheduleデータセット
	 *
	 */
	private void setViewData() {
		Log.d("DEBUG", "ScheduleRegistration setViewData Start");
		//タイトル
		etTitle.setText(sOrgObj.getStrTitle());
		//開始年月日
		Calendar calStart = DateUtil.toCalendar(sOrgObj.getStrGDWhenStartTime());
		tvStartDate.setText(DateUtil.DATE_FORMAT.format(calStart.getTime()));
		//開始時分
		tvStartTime.setText(DateUtil.TIME_FORMAT.format(calStart.getTime()));
		//終了年月日
		Calendar calEnd = DateUtil.toCalendar(sOrgObj.getStrGDWhenEndTime());
		tvEndDate.setText(DateUtil.DATE_FORMAT.format(calEnd.getTime()));
		//終了時分
		tvEndTime.setText(DateUtil.TIME_FORMAT.format(calEnd.getTime()));
		//終日
		if(calStart.get(Calendar.HOUR_OF_DAY) == 0 && calStart.get(Calendar.MINUTE) == 0){
			calStart.add(Calendar.DAY_OF_MONTH, 1);
			if(calStart.equals(calEnd)){
				//開始時刻が00:00で終了が翌日の00:00の場合、終日の予定
				tvStartTime.setVisibility(View.INVISIBLE);
				tvEndDate.setVisibility(View.INVISIBLE);
				tvEndTime.setVisibility(View.INVISIBLE);
				cbAllDay.setChecked(true);
			}
		}
		//場所
		etPlace.setText(sOrgObj.getStrGDWhere());
		//説明
		etContent.setText(sOrgObj.getStrContent());
		//アラーム
		sAlarm.setSelection((int)sOrgObj.getLgAlarmFlag());
		Log.d("DEBUG", "ScheduleRegistration setViewData End");
	}

	/**
	 * 画面要素へのリスナーセット
	 *
	 */
	private void setViewListener() {
		Log.d("DEBUG", "ScheduleRegistration setViewListener Start");
		//マイクアイコンのリスナー
		ivMicIcon1.setOnClickListener(iconOnClickListener);
		ivMicIcon2.setOnClickListener(iconOnClickListener);
		ivMicIcon3.setOnClickListener(iconOnClickListener);
		//年月日テキストビューのリスナー
		tvStartDate.setOnClickListener(new DateOnClickListener(this));
		tvEndDate.setOnClickListener(new DateOnClickListener(this));
		//時分テキストビューのリスナー
		tvStartTime.setOnClickListener(new TimeOnClickListener(this));
		tvEndTime.setOnClickListener(new TimeOnClickListener(this));
		//終日チェックボックスビューのリスナー
		cbAllDay.setOnClickListener(new AllDayOnClickListener());
		Log.d("DEBUG", "ScheduleRegistration setViewListener End");
	}

	/**
	 * 終日チェックボックスのリスナー
	 */
	private class AllDayOnClickListener implements OnClickListener{
		/**
		 * 終日チェックボックスをクリックされたとき呼び出される
		 */
		public void onClick(View v) {
			Log.d("DEBUG", "ScheduleRegistration AllDayOnClickListener onClick Start");
			if(((CheckBox)v).isChecked()){
				//終日がチェックされていた場合
				//開始年月日以外のテキストビューを隠す
				tvStartTime.setVisibility(View.INVISIBLE);
				tvEndDate.setVisibility(View.INVISIBLE);
				tvEndTime.setVisibility(View.INVISIBLE);
			}else{
				//終日がチェックされていない場合
				//開始年月日以外のテキストビューも表示する
				tvStartTime.setVisibility(View.VISIBLE);
				tvEndDate.setVisibility(View.VISIBLE);
				tvEndTime.setVisibility(View.VISIBLE);
			}
			Log.d("DEBUG", "ScheduleRegistration AllDayOnClickListener onClick End");
		}
	}

	/**
	 * DateOnClickListener
	 *  日付の文字列にセットされるリスナー
	 */
	private class DateOnClickListener implements OnClickListener, DialogInterface.OnDismissListener{
		private Context contextObj = null;
		public DateOnClickListener(Context context){
			Log.d("DEBUG", "ScheduleRegistration DateOnClickListener DateOnClickListener Start");
			//Contextが必要なので、コンストラクタで渡して覚えておく
			contextObj = context;
			Log.d("DEBUG", "ScheduleRegistration DateOnClickListener DateOnClickListener End");
		}

		/**
		 * クリックされた時呼び出される
		 * @param View クリックされたビュー
		 */
		public void onClick(View view) {
			Log.d("DEBUG", "ScheduleRegistration DateOnClickListener onClick Start");
			GregorianCalendar gcObj = null;
			if(view == tvStartDate && blSave == true) {
				//開始日でクリックされた場合
				//ボタン連打ロックオン
				blSave = false;
				gcObj = DateUtil.toCalendar(
						DateUtil.toDBDateString(tvStartDate.getText().toString(),tvStartTime.getText().toString()));
			} else if(view == tvEndDate && blSave == true) {
				//終了日でクリックされた場合
				//ボタン連打ロックオン
				blSave = false;
				gcObj = DateUtil.toCalendar(
						DateUtil.toDBDateString(tvEndDate.getText().toString(),tvEndTime.getText().toString()));
			} else {
				return;
			}
			//DatePickerDialogを作成し表示する
			DatePickerDialog dpdObj = new DatePickerDialog(
				contextObj, new DateSetListener(view), gcObj.get(Calendar.YEAR), gcObj.get(Calendar.MONTH), gcObj.get(Calendar.DAY_OF_MONTH));
			dpdObj.setOnDismissListener(this);
			//ダイアログ内ボタン連打ロックオフ
			blDialogButton = true;
			dpdObj.show();
			Log.d("DEBUG", "ScheduleRegistration DateOnClickListener onClick End");
		}

		/**
		 * onDismiss
		 * ダイアログキャンセルボタン処理
		 *
		 * @param dialog dialog情報 
		 */
		@Override
		public void onDismiss(DialogInterface dialog) {
			Log.d("DEBUG", "ScheduleRegistration DateOnClickListener onDismiss Start");
			//ダイアログ表示時に設定ボタンとキャンセルボタンと戻るボタンが押下された場合
			//ボタン連打ロックオフ
			blSave = true;
			Log.d("DEBUG", "ScheduleRegistration DateOnClickListener onDismiss End");
		}
	}

	/**
	 * DateSetListener
	 * DatePickerDialogにセットする、設定時に呼び出されるリスナー
	 */
	private class DateSetListener implements OnDateSetListener{
		private View viewObj = null;
		public DateSetListener(View view){
			Log.d("DEBUG", "DateSetListener DateSetListener Start");
			//Contextが必要なので、コンストラクタで渡して覚えておく
			viewObj = view;
			Log.d("DEBUG", "DateSetListener DateSetListener End");
		}

		/**
		 * DatePickerDialogで設定が押されたとき呼び出されるメソッド
		 * 
		 * @param picker DatePicker
		 * @param y 年
		 * @param m 月
		 * @param d 日
		 */
		public void onDateSet(DatePicker picker, int y, int m, int d) {
			Log.d("DEBUG", "ScheduleRegistration DateSetListener onDateSet Start");
			if(blDialogButton == true) {
				//ダイアログ内ボタン連打ロックオン
				blDialogButton = false;
				GregorianCalendar gcalObj = new GregorianCalendar();
				gcalObj.set(y,m,d);
				//引数で渡された年月日を該当するViewにセットする
				if(viewObj == tvStartDate){
					tvStartDate.setText(DateUtil.DATE_FORMAT.format(gcalObj.getTime()));
				} else if(viewObj == tvEndDate) {
					tvEndDate.setText(DateUtil.DATE_FORMAT.format(gcalObj.getTime()));
				}
			}
			Log.d("DEBUG", "ScheduleRegistration DateSetListener onDateSet End");
		}
	}

	/**
	 * TimeOnClickListener
	 *  時刻の文字列にセットされるリスナー
	 */
	private class TimeOnClickListener implements OnClickListener, DialogInterface.OnDismissListener{
		private Context contextObj = null;
		public TimeOnClickListener(Context context){
			Log.d("DEBUG", "ScheduleRegistration TimeOnClickListener TimeOnClickListener Start");
			//Contextが必要なので、コンストラクタで渡して覚えておく
			contextObj = context;
			Log.d("DEBUG", "ScheduleRegistration TimeOnClickListener TimeOnClickListener End");
		}

		/**
		 * クリックされた時呼び出される
		 * @param View クリックされたビュー
		 */
		public void onClick(View view) {
			Log.d("DEBUG", "ScheduleRegistration TimeOnClickListener onClick Start");
			GregorianCalendar gcObj = null;
			if(view == tvStartTime && blSave == true) {
				//開始時刻でクリックされた場合
				//ボタン連打ロックオン
				blSave = false;
				gcObj = DateUtil.toCalendar(
						DateUtil.toDBDateString(tvStartDate.getText().toString(), tvStartTime.getText().toString()));
			} else if(view == tvEndTime && blSave == true) {
				//終了時刻でクリックされた場合
				//ボタン連打ロックオン
				blSave = false;
				gcObj = DateUtil.toCalendar(
						DateUtil.toDBDateString(tvEndDate.getText().toString(), tvEndTime.getText().toString()));
			} else {
				return;
			}
			//TimePickerDialogを作成し表示する
			TimePickerDialog tpdObj = new TimePickerDialog(
				contextObj,new TimeSetListener(view),gcObj.get(Calendar.HOUR_OF_DAY), gcObj.get(Calendar.MINUTE),true);
			tpdObj.setOnDismissListener(this);
			//ダイアログ内ボタン連打ロックオフ
			blDialogButton = true;
			tpdObj.show();
			Log.d("DEBUG", "ScheduleRegistration TimeOnClickListener onClick End");
		}

		/**
		 * onDismiss
		 * ダイアログキャンセルボタン処理
		 *
		 * @param dialog dialog情報 
		 */
		@Override
		public void onDismiss(DialogInterface dialog) {
			Log.d("DEBUG", "ScheduleRegistration TimeOnClickListener onDismiss Start");
			//ダイアログ表示時に設定ボタンとキャンセルボタンと戻るボタンが押下された場合
			//ボタン連打ロックオフ
			blSave = true;
			Log.d("DEBUG", "ScheduleRegistration TimeOnClickListener onDismiss End");
		}
	}

	/**
	 * TimeSetListener
	 *  TimePickerDialogにセットする、設定時に呼び出されるリスナー
	 */
	private class TimeSetListener implements OnTimeSetListener{
		private View viewObj = null;
		public TimeSetListener(View view){
			Log.d("DEBUG", "ScheduleRegistration TimeSetListener TimeSetListener Start");
			//どのビューをクリックされて開かれたダイアログかを覚えておくために
			//コンストラクタでViewを保持しておく
			viewObj = view;
			Log.d("DEBUG", "ScheduleRegistration TimeSetListener TimeSetListener End");
		}

		/**
		 * TimePickerDialogで設定が押されたとき呼び出されるメソッド
		 * 
		 * @param picker TimePicker
		 * @param h 時
		 * @param m 分
		 */
		public void onTimeSet(TimePicker picker, int h, int m) {
			Log.d("DEBUG", "ScheduleRegistration TimeSetListener onTimeSet Start");
			if(blDialogButton == true) {
				//ダイアログ内ボタン連打ロックオン
				blDialogButton = false;
				GregorianCalendar gcalObj = new GregorianCalendar();
				gcalObj.set(Calendar.HOUR_OF_DAY,h);
				gcalObj.set(Calendar.MINUTE,m);
				//引数で渡された年月日を該当するViewにセットする
				if(viewObj == tvStartTime){
					tvStartTime.setText(DateUtil.TIME_FORMAT.format(gcalObj.getTime()));
				} else if(viewObj == tvEndTime) {
					tvEndTime.setText(DateUtil.TIME_FORMAT.format(gcalObj.getTime()));
				}
			}
			Log.d("DEBUG", "ScheduleRegistration TimeSetListener onTimeSet End");
		}
	}

	/**
	 * マイクアイコンクリック時のイベント
	 *
	 */
	private Button.OnClickListener iconOnClickListener = new Button.OnClickListener() {
		public void onClick(View view) {
			Log.d("DEBUG", "ScheduleRegistration onClick Start");
			if(blSave == true) {
				//ボタン連打ロックオン
				blSave = false;
				try {
					Intent iObj = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
					iObj.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
					iObj.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.mes3_dialog));
					iObj.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
					int intRequestCode;
					if(view == ivMicIcon1) {
						intRequestCode = REQUEST_CODE1;
					} else if(view == ivMicIcon2) {
						intRequestCode = REQUEST_CODE2;
					} else {
						intRequestCode = REQUEST_CODE3;
					}
					Log.d("DEBUG", "ScheduleRegistration onClick intRequestCode:" + intRequestCode);
					startActivityForResult(iObj, intRequestCode);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(ScheduleRegistration.this, getString(R.string.voice_entry_err), Toast.LENGTH_LONG).show();
					//ボタン連打ロックオフ
					blSave = true;
				}
			}
			Log.d("DEBUG", "ScheduleRegistration onClick End");
		}
	};

	/**
	 * onActivityResult
	 * 画面戻り処理
	 * @param int intRequestCode リクエストコード
	 * @param int intResultCode 結果コード
	 * @param Intent intent インテント
	 */
	@Override
	protected void onActivityResult(int intRequestCode, int intResultCode, Intent intent) {
		Log.d("DEBUG", "ScheduleRegistration onActivityResult Start");
		if((intRequestCode == REQUEST_CODE1 || intRequestCode == REQUEST_CODE2 || intRequestCode == REQUEST_CODE3 ) && intResultCode == RESULT_OK) {
			String strResult = "";
			ArrayList<String> alResults = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			for(int intCt = 0; intCt < alResults.size(); intCt++) {
				strResult += alResults.get(intCt);
			}
			if(intRequestCode == REQUEST_CODE1) {
				etTitle.setText(strResult);
			} else if (intRequestCode == REQUEST_CODE2) {
				etPlace.setText(strResult);
			} else {
				etContent.setText(strResult);
			}
		}
		//ボタン連打ロックオフ
		blSave = true;
		super.onActivityResult(intRequestCode, intResultCode, intent);
		Log.d("DEBUG", "ScheduleRegistration onActivityResult End");
	}

	/**
	 * scSave
	 * 保存ボタン押下時の処理
	 * @param v 選択ビュー
	 */
	public void scSave(View view) {
		Log.d("DEBUG", "ScheduleRegistration scSave Start");
		if(blSave == true) {
			//ボタン連打ロックオン
			blSave = false;
			//入力チェック
			boolean blCheckInputScheduleResult = checkInputSchedule();
			if(blCheckInputScheduleResult == true) {
				//スケジュール更新・登録処理
				boolean blSaveScheduleResult = savePreparationSchedule();
				if(blSaveScheduleResult == false) {
					Toast.makeText(ScheduleRegistration.this, getString(R.string.sqlite_write_err), Toast.LENGTH_LONG).show();
					endActivity();
				}
				nextActivity();
			} else {
				//ボタン連打ロックオフ
				blSave = true;
			}
		}
		Log.d("DEBUG", "ScheduleRegistration scSave End");
	}

	/**
	 * 入力値チェック処理
	 *
	 * @return 処理を行った場合はtrue
	 */
	private boolean checkInputSchedule() {
		Log.d("DEBUG", "ScheduleRegistration checkInputSchedule Start");
		boolean blResult = true;
		StringBuilder sbObj = new StringBuilder();
		//タイトルエディットテキスト
		String strTitle = etTitle.getText().toString();
		if(!(strTitle == null || strTitle.equals(""))) {
			//文字数チェック
			if(InputCheckUtil.checkCount(strTitle, Integer.parseInt(getString(R.string.isc_limit1))) == false) {
				blResult = false;
				sbObj.append(getString(R.string.sctitle_msg) + getString(R.string.restoreConfirm1));
			}
		}
		//終日チェックボックスビュー
		if(cbAllDay.isChecked()) {
			//オンの場合
			//開始年月日ビュー
			String strStartDate = tvStartDate.getText().toString();
			if(strStartDate == null || strStartDate.equals("")) {
				//空（null）チェック
				blResult = false;
				sbObj.append(getString(R.string.scstartdate_msg) + getString(R.string.restoreConfirm1));
			} else {
				//YYYY-MM-DD形式チェック
				if(DateUtil.checkYMDFormat(strStartDate) == false) {
					blResult = false;
					sbObj.append(getString(R.string.scstartdate_errmsg) + getString(R.string.restoreConfirm1));
				} else {
					//カレンダー上限日・下限日チェック
					long lgLowerLimitTime = DateUtil.convMSec(getString(R.string.lower_limit_time));
					long lgUpperLimitTime = DateUtil.convMSec(getString(R.string.upper_limit_time));
					long lgTargetTime = DateUtil.convMSec(strStartDate);
					if(lgLowerLimitTime > lgTargetTime) {
						blResult = false;
						sbObj.append(getString(R.string.scstartdate_errmsg2) + getString(R.string.restoreConfirm1));
					} else if(lgTargetTime > lgUpperLimitTime){
						blResult = false;
						sbObj.append(getString(R.string.scstartdate_errmsg3) + getString(R.string.restoreConfirm1));
					}
				}
			}
		} else {
			//オフの場合
			boolean blDateResult = true;
			//開始年月日ビュー
			String strStartDate = tvStartDate.getText().toString();
			if(strStartDate == null || strStartDate.equals("")) {
				//空（null）チェック
				blResult = false;
				blDateResult = false;
				sbObj.append(getString(R.string.scstartdate_msg) + getString(R.string.restoreConfirm1));
			} else {
				//YYYY-MM-DD形式チェック
				if(DateUtil.checkYMDFormat(strStartDate) == false) {
					blResult = false;
					blDateResult = false;
					sbObj.append(getString(R.string.scstartdate_errmsg) + getString(R.string.restoreConfirm1));
				} else {
					//カレンダー上限日・下限日チェック
					long lgLowerLimitTime = DateUtil.convMSec(getString(R.string.lower_limit_time));
					long lgUpperLimitTime = DateUtil.convMSec(getString(R.string.upper_limit_time));
					long lgTargetTime = DateUtil.convMSec(strStartDate);
					if(lgLowerLimitTime > lgTargetTime) {
						blResult = false;
						blDateResult = false;
						sbObj.append(getString(R.string.scstartdate_errmsg2) + getString(R.string.restoreConfirm1));
					} else if(lgTargetTime > lgUpperLimitTime) {
						blResult = false;
						blDateResult = false;
						sbObj.append(getString(R.string.scstartdate_errmsg3) + getString(R.string.restoreConfirm1));
					}
				}
			}
			//開始時間ビュー
			String strStartTime = tvStartTime.getText().toString();
			if(strStartTime == null || strStartTime.equals("")) {
				//空（null）チェック
				blResult = false;
				blDateResult = false;
				sbObj.append(getString(R.string.scstarttime_msg) + getString(R.string.restoreConfirm1));
			} else {
				//HH:mm形式チェック
				if(DateUtil.checkHMFormat(strStartTime) == false) {
					blResult = false;
					blDateResult = false;
					sbObj.append(getString(R.string.scstarttime_errmsg) + getString(R.string.restoreConfirm1));
				}
			}
			//終了年月日ビュー
			String strEndDate = tvEndDate.getText().toString();
			if(strEndDate == null || strEndDate.equals("")) {
				//空（null）チェック
				blResult = false;
				blDateResult = false;
				sbObj.append(getString(R.string.scenddate_msg) + getString(R.string.restoreConfirm1));
			} else {
				//YYYY-MM-DD形式チェック
				if(DateUtil.checkYMDFormat(strEndDate) == false) {
					blResult = false;
					blDateResult = false;
					sbObj.append(getString(R.string.scenddate_errmsg) + getString(R.string.restoreConfirm1));
				} else {
					//カレンダー上限日・下限日チェック
					long lgLowerLimitTime = DateUtil.convMSec(getString(R.string.lower_limit_time));
					long lgUpperLimitTime = DateUtil.convMSec(getString(R.string.upper_limit_time));
					long lgTargetTime = DateUtil.convMSec(strEndDate);
					if(lgLowerLimitTime > lgTargetTime) {
						blResult = false;
						blDateResult = false;
						sbObj.append(getString(R.string.scenddate_errmsg2) + getString(R.string.restoreConfirm1));
					} else if(lgTargetTime > lgUpperLimitTime) {
						blResult = false;
						blDateResult = false;
						sbObj.append(getString(R.string.scenddate_errmsg3) + getString(R.string.restoreConfirm1));
					}
				}
			}
			//終了時間ビュー
			String strEndTime = tvEndTime.getText().toString();
			if(strEndTime == null || strEndTime.equals("")) {
				//空（null）チェック
				blResult = false;
				blDateResult = false;
				sbObj.append(getString(R.string.scendtime_msg) + getString(R.string.restoreConfirm1));
			} else {
				//HH:mm形式チェック
				if(DateUtil.checkHMFormat(strEndTime) == false) {
					blResult = false;
					blDateResult = false;
					sbObj.append(getString(R.string.scendtime_errmsg) + getString(R.string.restoreConfirm1));
				}
			}
			//終了時間年月日時間と開始年月日時間の過去時間チェック
			if(blDateResult == true) {
				StringBuilder sbStart = new StringBuilder();
				sbStart.append(strStartDate);
				sbStart.append(" ");
				sbStart.append(strStartTime);
				long lgStartDateTime = DateUtil.convMSec(sbStart.toString());
				StringBuilder sbEnd = new StringBuilder();
				sbEnd.append(strEndDate);
				sbEnd.append(" ");
				sbEnd.append(strEndTime);
				long lgEndDateTime = DateUtil.convMSec(sbEnd.toString());
				if(lgEndDateTime < lgStartDateTime) {
					blResult = false;
					sbObj.append(getString(R.string.scdate_errmsg) + getString(R.string.restoreConfirm1));
				}
			}
		}
		//場所エディットテキスト
		String strPlace = etPlace.getText().toString();
		if(!(strPlace == null || strPlace.equals(""))) {
			//文字数チェック
			if(InputCheckUtil.checkCount(strPlace, Integer.parseInt(getString(R.string.isc_limit1))) == false) {
				blResult = false;
				sbObj.append(getString(R.string.scplace_msg) + getString(R.string.restoreConfirm1));
			}
		}
		//説明エディットテキスト
		String strContent = etContent.getText().toString();
		if(!(strContent == null || strContent.equals(""))) {
			//文字数チェック
			if(InputCheckUtil.checkCount(strContent, Integer.parseInt(getString(R.string.isc_limit2))) == false) {
				blResult = false;
				sbObj.append(getString(R.string.sccontent_msg) + getString(R.string.restoreConfirm1));
			}
		}
		//アラームスピナー
		String strAlarm = (String) sAlarm.getSelectedItem();
		if(strAlarm == null || strAlarm.equals("")) {
			blResult = false;
			sbObj.append(getString(R.string.scalarm_msg) + getString(R.string.restoreConfirm1));
		}
		if(blResult == false) {
			showDialog(this, "", sbObj.toString(), getString(R.string.yes_btn));
		}
		Log.d("DEBUG", "ScheduleRegistration checkInputSchedule End");
		return blResult;
	}

	/**
	 * ダイアログの表示
	 *
	 * @param Context context コンテキスト
	 * @param String title タイトル
	 * @param String text メッセージ
	 * @param String btnmsg ボタン
	 */
	private static void showDialog(Context context, String title, String text, String btnmsg) {
		Log.d("DEBUG", "ScheduleRegistration showDialog Start");
		AlertDialog.Builder ad = new AlertDialog.Builder(context);
		ad.setTitle(title);
		ad.setMessage(text);
		ad.setPositiveButton(btnmsg, null);
		ad.show();
		Log.d("DEBUG", "ScheduleRegistration showDialog End");
	}

	/**
	 * Schedule情報のデータベース登録・更新処理
	 *
	 * @return 処理を行った場合はtrue
	 */
	private boolean savePreparationSchedule() {
		Log.d("DEBUG", "ScheduleRegistration savePreparationSchedule Start");
		boolean blResult = true;
		//ScheduleDatabaseHelper初期化
		sdhDB = new ScheduleDatabaseHelper(this);
		//SQLパラメータ作成(ID,DELETED,MODIFIED,ALARM,ALARM_LIST,TITLE,CONTENT,
		//                 GD_WHERE,GD_WHEN_ENDTIME,GD_WHEN_STARTTIME,PUBLISHED,
		//                 UPDATED,CATEGORY,EDIT_URL,GD_EVENTSTATUS,CALENDAR_ID,
		//                 ETAG,ENDTIME,STARTTIME,ALARM_FLAG)
		Schedule sObj = new Schedule();
		sObj.setLgDeleteFlag(0l);
		//ALARM Null
		//ALARM_LIST Null
		sObj.setStrTitle(etTitle.getText().toString());
		sObj.setStrContent(etContent.getText().toString());
		sObj.setStrGDWhere(etPlace.getText().toString());
		//終日フラグ判断
		if(cbAllDay.isChecked()){
			// 終日が設定されていたら　終了日は翌日、時刻はともに00:00にする
			//GD_WHEN_STARTTIME
			GregorianCalendar gcObj = DateUtil.toCalendar(DateUtil.toDBDateString(tvStartDate.getText().toString(), "00:00"));
			sObj.setStrGDWhenStartTime(DateUtil.toDBDateString(gcObj));
			//STARTTIME
			sObj.setLgStartTime(gcObj.getTimeInMillis());
			//GD_WHEN_ENDTIME
			gcObj.add(Calendar.DAY_OF_MONTH, 1);
			sObj.setStrGDWhenEndTime(DateUtil.toDBDateString(gcObj));
			//ENDTIME
			sObj.setLgEndTime(gcObj.getTimeInMillis());
		}else{
			//GD_WHEN_STARTTIME
			sObj.setStrGDWhenStartTime(DateUtil.toDBDateString(tvStartDate.getText().toString(), tvStartTime.getText().toString()));
			//STARTTIME
			StringBuilder sbStart = new StringBuilder();
			sbStart.append(tvStartDate.getText().toString());
			sbStart.append(" ");
			sbStart.append(tvStartTime.getText().toString());
			sObj.setLgStartTime(DateUtil.convMSec(sbStart.toString()));
			//GD_WHEN_ENDTIME
			sObj.setStrGDWhenEndTime(DateUtil.toDBDateString(tvEndDate.getText().toString(), tvEndTime.getText().toString()));
			//ENDTIME
			StringBuilder sbEnd = new StringBuilder();
			sbEnd.append(tvEndDate.getText().toString());
			sbEnd.append(" ");
			sbEnd.append(tvEndTime.getText().toString());
			sObj.setLgEndTime(DateUtil.convMSec(sbEnd.toString()));
		}
		//Published Null
		sObj.setStrUpdated(DateUtil.toDBDateString(new GregorianCalendar()));
		//CATEGORY null
		//EDIT_URL null
		//GD_EVENTSTATUS null
		//CALENDAR_ID null
		//ETAG null
		String strAlarm = (String) sAlarm.getSelectedItem();
		if(strAlarm.equals(getString(R.string.onflag))) {
			sObj.setLgAlarmFlag(1l);
		} else {
			sObj.setLgAlarmFlag(0l);
		}
		//キーIDの存在確認
		if(stdObj.getLgKeyId() != 0) {
			//Scheduleテーブル 更新
			sObj.setLgId(stdObj.getLgKeyId());
			//入力値とSQLiteの値が同値の場合更新フラグを立てない(Googleカレンダーとの同値データの無限更新対応)
			if(checkEquivalence(sObj) && !(sOrgObj.getStrCalendarId() == null || sOrgObj.getStrCalendarId().equals(""))) {
				//同値且つカレンダーIDが有る場合
				sObj.setLgModified(0l);
				Log.d("DEBUG", "ScheduleRegistration checkEquivalence : " + "同値＋カレンダーID有りの場合");
			} else {
				sObj.setLgModified(1l);
				Log.d("DEBUG", "ScheduleRegistration checkEquivalence : " + "(同値＋カレンダーID有り)でない場合");
			}
			blResult = sdhDB.updateSchedule(sObj);
		} else {
			//Scheduleテーブル 新規登録
			sObj.setLgModified(1l);
			blResult = sdhDB.insertSchedule(sObj);
		}
		Log.d("DEBUG", "ScheduleRegistration savePreparationSchedule End");
		return blResult;
	}

	/**
	 * 入力値とSQLiteの同値チェック処理
	 *
	 * @param sUpdateDate 更新対象スケジュールオブジェクト
	 * @return 同値の場合:true,同値でない場合:false
	 */
	private boolean checkEquivalence(Schedule sUpdateDate) {
		Log.d("DEBUG", "ScheduleRegistration checkEquivalence Start");
		boolean blResult = true;
		//TITLE
		if(!(sOrgObj.getStrTitle().equals(sUpdateDate.getStrTitle()))) {
			blResult = false;
		}
		//CONTENT
		if(!(sOrgObj.getStrContent().equals(sUpdateDate.getStrContent()))) {
			blResult = false;
		}
		//GD_WHERE
		if(!(sOrgObj.getStrGDWhere().equals(sUpdateDate.getStrGDWhere()))) {
			blResult = false;
		}
		//GD_WHEN_ENDTIME
		if(!(sOrgObj.getStrGDWhenEndTime().equals(sUpdateDate.getStrGDWhenEndTime()))) {
			blResult = false;
		}
		//GD_WHEN_STARTTIME
		if(!(sOrgObj.getStrGDWhenStartTime().equals(sUpdateDate.getStrGDWhenStartTime()))) {
			blResult = false;
		}
		Log.d("DEBUG", "ScheduleRegistration checkEquivalence End");
		return blResult;
	}

	/**
	 * scCancel
	 *  キャンセルボタン押下時の処理
	 * @param v 選択ビュー
	 */
	public void scCancel(View view) {
		Log.d("DEBUG", "ScheduleRegistration scCancel Start");
		if(blSave == true) {
			//ボタン連打ロックオン
			blSave = false;
			nextActivity();
		}
		Log.d("DEBUG", "ScheduleRegistration scCancel End");
	}

	/**
	 * Activity遷移処理
	 *
	 */
	public void nextActivity() {
		Log.d("DEBUG", "ScheduleRegistration nextActivity Start");
		//「スケジュールタブ」画面に戻る
		//アプリ内のアクティビティを呼び出すインテントの生成
		Intent intent = new Intent(this, MainTab.class);
		//インテントのパラメータ設定
		//カレンダー年月
		intent.putExtra("calym", stdObj.getStrCalendarYearMonth());
		//選択年月日
		intent.putExtra("calymd", stdObj.getStrCalendarYearMonthDay());
		//選択元ユーザーインターフェースID
		//登録or編集画面ごとに処理分岐
		if(stdObj.getLgKeyId() == 0) {
			intent.putExtra("uiid", getString(R.string.uiid5));
		} else {
			intent.putExtra("uiid", getString(R.string.uiid6));
		}
		//キーID
		intent.putExtra("keyid", stdObj.getLgKeyId());
		startActivity(intent);
		endActivity();
		Log.d("DEBUG", "ScheduleRegistration nextActivity Start");
	}

	/**
	 * Activity終了処理
	 *
	 */
	private void endActivity() {
		Log.d("DEBUG", "ScheduleRegistration endActivity Start");
		//自アクティビティの終了
		finish();
		Log.d("DEBUG", "ScheduleRegistration endActivity End");
	}

	/**
	 * 画面回転時に呼ばれるサイクル
	 *
	 * @param newConfig 新しい設定値
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d("DEBUG", "ScheduleRegistration onConfigurationChanged Start");
		super.onConfigurationChanged(newConfig);
		Log.d("DEBUG", "ScheduleRegistration onConfigurationChanged End");
	}

	/**
	 * バックグランド時に呼ばれるサイクル
	 *
	 * @param outState 保管設定値
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {  
		Log.d("DEBUG", "ScheduleRegistration onSaveInstanceState Start");
		super.onSaveInstanceState(outState);
		outState.putString("startdate", tvStartDate.getText().toString());
		outState.putString("starttime", tvStartTime.getText().toString());
		outState.putString("enddate", tvEndDate.getText().toString());
		outState.putString("endtime", tvEndTime.getText().toString());
		Log.d("DEBUG", "ScheduleRegistration onSaveInstanceState End");
	} 

	/**
	 * フォアグランド時に呼ばれるサイクル
	 *
	 * @param savedInstanceState 保管設定値
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {  
		Log.d("DEBUG", "ScheduleRegistration onRestoreInstanceState Start");
		//復元情報からデータ取得
		super.onRestoreInstanceState(savedInstanceState);
		tvStartDate.setText(savedInstanceState.getString("startdate"));
		tvEndDate.setText(savedInstanceState.getString("enddate"));
		tvStartTime.setText(savedInstanceState.getString("starttime"));
		tvEndTime.setText(savedInstanceState.getString("endtime"));
		if(cbAllDay.isChecked()){
			//終日がチェックされていた場合
			//開始年月日以外のテキストビューを隠す
			tvStartTime.setVisibility(View.INVISIBLE);
			tvEndDate.setVisibility(View.INVISIBLE);
			tvEndTime.setVisibility(View.INVISIBLE);
		}else{
			//終日がチェックされていない場合
			//開始年月日以外のテキストビューも表示する
			tvStartTime.setVisibility(View.VISIBLE);
			tvEndDate.setVisibility(View.VISIBLE);
			tvEndTime.setVisibility(View.VISIBLE);
		}
		Log.d("DEBUG", "ScheduleRegistration onRestoreInstanceState End");
	} 

	/**
	 * onDestroy
	 * データベースヘルパークローズ処理
	 */
	@Override
	public void onDestroy(){
		Log.d("DEBUG", "ScheduleRegistration onDestroy Start");
		super.onDestroy();
		if(sdhDB != null) {
			sdhDB.close();
		}
		Log.d("DEBUG", "ScheduleRegistration onDestroy End");
	}

	/**
	 * 戻るボタンでカレンダー画面へ遷移
	 *
	 * @param kEvent キーイベント情報 
	 * @return 処理を行った場合はtrue
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent kEvent) {
		Log.d("DEBUG", "ScheduleRegistration dispatchKeyEvent Start");
		//キー押下されたことを確認
		if (kEvent.getAction() == KeyEvent.ACTION_DOWN) {
			//戻るボタンが押されたか確認
			if (kEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				nextActivity();
			}
		}
		Log.d("DEBUG", "ScheduleRegistration dispatchKeyEvent End");
		return super.dispatchKeyEvent(kEvent);
	}
}