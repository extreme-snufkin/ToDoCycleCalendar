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


 クラス名：MainCalendar
 内容：カレンダー画面Activity
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
			0.1/2012.05.21/T.Mashiko
			0.2/2012.05.22/T.Mashiko
			0.3/2012.05.29/T.Mashiko
			0.4/2012.06.04/T.Mashiko
			0.5/2012.06.06/T.Mashiko インテントLong型修正
			0.6/2012.06.15/T.Mashiko YYYY-MM形式修正
			0.7/2012.06.17/T.Mashiko
			0.8/2012.06.18/T.Mashiko
			0.9/2012.06.19/T.Mashiko 非同期ロジック修正,パフォーマンス改善
			1.0/2012.06.25/T.Mashiko OAuthCode取得処理
			1.1/2012.06.26/T.Mashiko OAuthCode取得処理,GDATA取得処理
			1.2/2012.06.27/T.Mashiko GDATA取得処理
			1.3/2012.06.28/T.Mashiko GDATA取得処理
			1.4/2012.07.02/T.Mashiko GDATA取得処理
			1.5/2012.07.03/T.Mashiko GDATA取得処理
			1.7/2012.07.04/T.Mashiko 端末アラーム設定機能対応
			1.8/2012.07.05/T.Mashiko OAuth認証エラー発生時の不具合修正
			1.9/2012.07.06/T.Mashiko OAuth認証のプリファレンスをSQLite化＋TokenExpires処理の不具合改修
			2.0/2012.07.08/T.Mashiko 定数値の整理
			2.1/2012.07.09/T.Mashiko カレンダー年月指定ダイアログ改修,年月範囲指定
			2.2/2012.07.10/T.Mashiko 年月日範囲指定修正
			2.3/2012.07.11/T.Mashiko 不要インポート削除
			2.4/2012.07.18/T.Mashiko ロギング表記修正
			2.5/2012.08.22/T.Mashiko 対象年月の連打ロック対策,ダイアログの戻るキー対策
			2.6/2012.08.23/T.Mashiko 対象年月の連打ロック対策追加
			2.7/2012.09.04/T.Mashiko 通信時のリターンコードのログとエラーハンドリング追加
			2.8/2012.09.05/T.Mashiko カレンダー同期機能の非同期処理内にてUI操作(トースト)を実行していた為、修正
			2.9/2012.09.06/T.Mashiko 通信時のリターンコードのログ処理追加,同値更新時に無限更新に陥る回避ロジックを削除
			3.0/2012.09.07/T.Mashiko 同期処理時のログ追加,同期Insert処理時のイベント取得URLに更新日時条件追加
			3.1/2012.09.08/T.Mashiko 同期Delete処理時の成功判定ロジック追加,同期処理後の未反映データ件数出力追加
			3.2/2012.09.10/T.Mashiko Insert同期処理のイベント取得URLへ追加する更新日時を処理ごとに更新する対応
			3.3/2012.09.13/T.Mashiko 同期データ取得時に25件を超えた取得を行う際に再クエリー発行時に更新日時の条件が2重に記述されエラーとなっていた対応,parseメソッドのリターンがnull値の場合に条件エラーとなる対応
			3.4/2012.09.14/T.Mashiko 同期処理中のアクセストーク期限切れ対応,OutOfMemoryエラー対策
			3.5/2012.09.19/T.Mashiko 同期時のOutOfMemoryエラー対応(取得データ数300件変更処理)
			3.6/2012.09.26/T.Mashiko メニュー：About追加
			3.7/2012.09.27/T.Mashiko 強制終了対策
*/
package study.tdcc.act;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import study.tdcc.*;
import study.tdcc.adapter.*;
import study.tdcc.bean.*;
import study.tdcc.lib.*;
import study.tdcc.lib.ScheduleDatabaseHelper.ScheduleCursor;
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
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class MainCalendar extends Activity implements OnItemClickListener{
	//カスタムタイトルテキストビュー
	private TextView tvCustomTitle;
	//カスタムタイトル(バージョン)テキストビュー
	private TextView tvCustomTitleVersion;
	//ビューフリッパー
	private ViewFlipper vfCalendar;
	//ジェスチャーオブジェクト
	private GestureDetector gdObj;
	//年月テキストビュー
	private TextView tvYearMonth;
	//グリッドビュー
	private GridView gvCalendar;
	//グリッドビュー
	private GridView gvObj;
	//CalendarCellAdapterオブジェクト
	private CalendarCellAdapter ccaDay = null;
	//ターゲット年月日を保持する変数
	private GregorianCalendar gcTargetYearMonthDay;
	//当日の年月日を保持する変数
	private GregorianCalendar gcNow;
	//データベースオブジェクト
	private ScheduleDatabaseHelper sdhDB;
	//画面遷移データオブジェクト
	private ScreenTransitionData stdObj = new ScreenTransitionData();
	//プログレスダイアログのインスタンス
	private ProgressDialog pdObj = null;
	//カレンダーセル内データリスト
	private ArrayList<String> alDateCell;
	//カレンダーセル内データを受け取るhandler
	private Handler hObj;
	//カレンダーフリック連打防止フラグ
	private boolean blFlick = true;
	//ダイアログ内ボタン連打防止フラグ
	private boolean blDialogButton = true;
	//トークンデータ取得handler
	private Handler hGetTokenObj;
	//トークン情報
	private Map<String,String> mpTokens;
	//カレンダーデータ取得handler
	private Handler hSyncCalendarObj;

	//AuthCodeを保持する変数
	private String strAuthCode;
	//更新開始日時
	private String strUpdateStartTime = null;
	//前回更新日時
	private String strLastUpdate = null;
	//前回更新日時の為のタグ
	public static final String LAST_UPDATE = "LastUpdate";
	//認証用トークンを保持する変数
	private String strAccessToken;
	private String strRefreshToken;
	private long lgAccessTokenExpire;
	//認証用トークンの名前
	private static final String ACCESS_TOKEN = "access_token";
	private static final String REFRESH_TOKEN = "refresh_token";
	private static final String EXPIRES_IN = "expires_in";

	//クライアントシークレット Google APISにて別途、要取得
	public static final String CLIENT_SECRET = "My_Client_secret";
	//クライアントID　Google APISにて別途、要取得
	public static final String CLIENT_ID = "My_Client_ID";

	//リダイレクトURI　Google APISから取得
	public static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
	//OAUTH用URL
	public static final String OAUTH_URL = "https://accounts.google.com/o/oauth2/auth";
	//許可の種類
	public static final String GRANT_TYPE_A = "authorization_code";
	public static final String GRANT_TYPE_R = "refresh_token";
	//トークン取得URL
	public static final String TOKEN_URL = "https://accounts.google.com/o/oauth2/token";
	//コンテントタイプ
	public static final String CONTENT_TYPE_AX = "application/x-www-form-urlencoded";
	public static final String CONTENT_TYPE_AA = "application/atom+xml";
	//カレンダーのスコープ
	public static final String SCOPE = "https://www.google.com/calendar/feeds/";
	//レスポンスコード　スタンドアロンアプリケーションなので、コードで取得する
	public static final String RESPONSE_CODE = "code";
	//OAUTHに使うURI　
	public static final Uri OAUTH_URI = Uri.parse(OAUTH_URL
			+"?client_id="+CLIENT_ID
			+"&redirect_uri="+REDIRECT_URI
			+"&scope="+SCOPE
			+"&response_type="+RESPONSE_CODE);
	//onActivityResultで使用するActivityのrequestコード（ブラウザ起動用）
	public static final int BROWSER = 11;
	//onActivityResultで使用するActivityのrequestコード（AuthCode入力用）
	public static final int AUTH_CODE_REGISTRATION = 12;
	//AuthCodeを受け取る為のタグ
	public static final String AUTH_CODE = "AuthCode";

	//GDATAヘッダータグ
	public static final String GDATA_VERSION_TAG = "GData-Version";
	//GDATAバージョン
	public static final String GDATA_VERSION = "2";
	//GoogleCalendarのFEEDを取得するURL
	//showdeleted=trueをつけることでキャンセルされた（削除された）イベントの情報も取得
	public static final String CALENDAR_FEED_URL = "https://www.google.com/calendar/feeds/default/private/full?max-results=300&showdeleted=true";
	//カレンダーAPIに特定のFeedではないデータにアクセスするためのURL
	public static final String DEFAULT_URL = "https://www.google.com/calendar/feeds/default/private/full";
	//HTTP通信処理のリターンステータスを保持するメンバ変数
	private boolean blHttpSucceeded = false;
	//HTTP通信処理のレスポンスコードを保持するメンバ変数
	private int intResponseCode = 0;
	//GData更新対象リスト(Insert用)
	ArrayList<GDataEvent> alInsertGCEvents;
	//GData更新対象リスト(Update用)
	ArrayList<GDataEvent> alUpdateGCEvents;
	//GData更新対象リスト(Delete用)
	ArrayList<GDataEvent> alDeleteGCEvents;
	//Googleカレンダーとの同期時のエラー判定メンバ変数
	private boolean blSyncGC = true;
	//Googleカレンダーとの同期時にデータが反映されなかった件数を保持するメンバ変数
	private int intUnreflected = 0;
	
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
			Log.d("DEBUG", "MainCalendar onDown Start");
			Log.d("DEBUG", "MainCalendar onDown End");
			return false;
		}
		//フリック処理を実装
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			Log.d("DEBUG", "MainCalendar onFling Start");
			float flCoordX = Math.abs(velocityX);
			float flCoordY = Math.abs(velocityY);
			if (flCoordX > flCoordY && flCoordX > 150) {
				if(blFlick == true) {
					//カレンダーフリック連打防止フラグ
					blFlick = false;
					//現在の注目している日付を当月の1日に変更する
					gcTargetYearMonthDay.set(Calendar.DAY_OF_MONTH,1);
					if (e1.getX() < e2.getX()) {
						//1ヶ月減算する
						gcTargetYearMonthDay.add(Calendar.MONTH, -1);
						//下限月補正
						long lgLowerLimitTime = DateUtil.convMSec(getString(R.string.lower_limit_time));
						if(gcTargetYearMonthDay.getTimeInMillis() < lgLowerLimitTime) {
							Log.d("DEBUG", "MainCalendar onFling 下限月補正実施");
							gcTargetYearMonthDay.add(Calendar.MONTH, 1);
						}
					} else {
						//1ヶ月加算する
						gcTargetYearMonthDay.add(Calendar.MONTH, 1);
						//上限月補正
						long lgUpperLimitTime = DateUtil.convMSec(getString(R.string.upper_limit_time));
						if(gcTargetYearMonthDay.getTimeInMillis() > lgUpperLimitTime) {
							gcTargetYearMonthDay.add(Calendar.MONTH, -1);
							Log.d("DEBUG", "MainCalendar onFling 上限月補正実施");
						}
					}
					tvYearMonth.setText(DateUtil.YEARMONTH_FORMAT.format(gcTargetYearMonthDay.getTime()));
					//年月テキストビューの文字色変更処理
					changeYMColor(gcTargetYearMonthDay.get(Calendar.MONTH)+1);
					//終日スケジュールを受け取るhandler
					hObj = new Handler();
					//カレンダーセル内データ作成(非同期)
					makeCalDateCell();
					Log.d("DEBUG", "MainCalendar onFling End");
					return true;
				}
			}
			Log.d("DEBUG", "MainCalendar onFling End");
			return false;
		}
		public void onLongPress(MotionEvent arg0) {
			Log.d("DEBUG", "MainCalendar onLongPress Start");
			Log.d("DEBUG", "MainCalendar onLongPress End");
		}
		public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
			Log.d("DEBUG", "MainCalendar onScroll Start");
			Log.d("DEBUG", "MainCalendar onScroll End");
			return false;
		}
		public void onShowPress(MotionEvent arg0) {
			Log.d("DEBUG", "MainCalendar onShowPress Start");
			Log.d("DEBUG", "MainCalendar onShowPress End");
		}
		public boolean onSingleTapUp(MotionEvent arg0) {
			Log.d("DEBUG", "MainCalendar onSingleTapUp Start");
			Log.d("DEBUG", "MainCalendar onSingleTapUp End");
			return false;
		}
	};

	/**
	 * onCreate
	 * @param savedInstanceState バンドル
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("DEBUG", "MainCalendar onCreate Start");
		super.onCreate(savedInstanceState);
		//カスタムタイトル
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		//カレンダービュー描画処理
		setContentView(R.layout.calendar);
		//カスタムタイトル描画処理
		Window window = getWindow();
		window.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.customtitle);
		//画面遷移情報の取得
		getScreenTransitionData();
		//画面復元情報の取得
		getScreenRestoreData(savedInstanceState);
		//画面要素の取得処理
		getViewElement();
		//カスタムタイトルの内容セット
		setCustomTitle();
		//画面要素のリスナーセット
		setViewListener();
		//画面要素へのデータセット
		setViewElement();
		//カレンダーフリック連打防止フラグ
		blFlick = true;
		Log.d("DEBUG", "MainCalendar onCreate End");
	}

	/**
	 * 画面遷移情報の取得
	 */
	private void getScreenTransitionData() {
		Log.d("DEBUG", "MainCalendar getScreenTransitionData Start");
		//画面遷移時のインテント格納値取得
		Bundle bExtras = getIntent().getExtras();
		if (bExtras != null) {
			stdObj.setStrCalendarYearMonth(bExtras.getString("calym"));
			stdObj.setStrCalendarYearMonthDay(bExtras.getString("calymd"));
			stdObj.setStrUserInterfaceId(bExtras.getString("uiid"));
			stdObj.setLgKeyId(bExtras.getLong("keyid"));
		}
		Log.d("DEBUG", "MainCalendar getScreenTransitionData End");
	}
	
	/**
	 * 画面復元情報の取得
	 * @param savedInstanceState バンドル
	 */
	private void getScreenRestoreData(Bundle savedInstanceState) {
		Log.d("DEBUG", "MainCalendar getScreenRestoreData Start");
		if(savedInstanceState != null) {
			//復元情報から取得
			stdObj.setStrCalendarYearMonth(savedInstanceState.getString("calym"));
		}
		Log.d("DEBUG", "MainCalendar getScreenRestoreData End");
	}

	/**
	 * 画面要素の取得
	 *
	 */
	private void getViewElement() {
		Log.d("DEBUG", "MainCalendar getViewElement Start");
		//カスタムタイトルテキストビュー
		tvCustomTitle = (TextView)this.findViewById(R.id.titletext);
		//カスタムタイトルバージョンビュー
		tvCustomTitleVersion = (TextView)this.findViewById(R.id.titleversion);
		//ビューフリッパー
		vfCalendar = (ViewFlipper)this.findViewById(R.id.vfCalendar);
		//年月テキストビュー
		tvYearMonth = (TextView)findViewById(R.id.yearMonth);
		//カレンダーグリッドビュー
		gvCalendar = (GridView)findViewById(R.id.gvCalendar);
		//ジェスチャーディテクターを生成
		gdObj = new GestureDetector(this, oglObj);
		Log.d("DEBUG", "MainCalendar getViewElement End");
	}

	/**
	 * カスタムタイトルの内容セット
	 *
	 */
	private void setCustomTitle() {
		Log.d("DEBUG", "MainCalendar setCustomTitle Start");
		tvCustomTitle.setText(getString(R.string.act_name1));
		StringBuilder sbObj = new StringBuilder();
		sbObj.append(getString(R.string.title_version));
		PackageManager pmObj = this.getPackageManager();
		try {
			PackageInfo piObj = pmObj.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
			sbObj.append(piObj.versionName);
		} catch (NameNotFoundException e) {
			Log.e("ERROR", "MainCalendar setCustomTitle NameNotFoundException", e);
		}
		tvCustomTitleVersion.setText(sbObj.toString());
		Log.d("DEBUG", "MainCalendar setCustomTitle End");
	}

	/**
	 * 画面要素へのリスナーセット
	 *
	 */
	private void setViewListener() {
		Log.d("DEBUG", "MainCalendar setViewListener Start");
		//対象年月テキストビューのリスナー
		tvYearMonth.setOnClickListener(new DateOnClickListener(this));
		//カレンダーグリッドビューのリスナー
		gvCalendar.setOnItemClickListener(this);
		gvCalendar.setOnTouchListener(otlObj);
		Log.d("DEBUG", "MainCalendar setViewListener End");
	}

	/**
	 * 画面要素へのデータセット
	 *
	 */
	private void setViewElement() {
		Log.d("DEBUG", "MainCalendar setViewElement Start");
		//GregorianCalendarのインスタンスの作成
		//アプリケーション使用日
		gcNow = new GregorianCalendar();
		//画面遷移元の指定年月の反映
		if(stdObj.getStrCalendarYearMonth() == null || stdObj.getStrCalendarYearMonth().equals("")) {
			//カレンダーの表示年月
			gcTargetYearMonthDay = new GregorianCalendar();
			String strInitialDate = DateUtil.YEARMONTH_FORMAT.format(gcTargetYearMonthDay.getTime());
			gcTargetYearMonthDay = DateUtil.toCalendar(strInitialDate + DateUtil.FIRST_DAY);
			Log.d("DEBUG","MainCalendar setViewElement stdObj:空");
		} else {
			gcTargetYearMonthDay = DateUtil.toCalendar(stdObj.getStrCalendarYearMonth() + DateUtil.FIRST_DAY);
			Log.d("DEBUG","MainCalendar setViewElement stdObj:有り");
		}
		//年月のビューへの表示
		tvYearMonth.setText(DateUtil.YEARMONTH_FORMAT.format(gcTargetYearMonthDay.getTime()));
		//年月テキストビューの文字色変更処理
		//月の取得
		int intMonth = gcTargetYearMonthDay.get(Calendar.MONTH)+1;
		changeYMColor(intMonth);
		//カレンダーグリッドビューの生成
		gvObj = (GridView) vfCalendar.getCurrentView();
		//カレンダーグリッドビューのカラム数を設定する
		gvObj.setNumColumns(DateUtil.DAYS_OF_WEEK);
		//ScheduleDatabaseHelper初期化
		sdhDB = new ScheduleDatabaseHelper(this);
		//終日スケジュールを受け取るhandler
		hObj = new Handler();
		//カレンダーセル内データ作成(非同期)
		makeCalDateCell();
		Log.d("DEBUG", "MainCalendar setViewElement End");
	}

	/**
	 * カレンダーセル内データ作成(非同期)
	 */
	public void makeCalDateCell(){
		Log.d("DEBUG", "MainCalendar makeCalDateCell Start");
		//プログレスダイアログの作成
		pdObj = new ProgressDialog(MainCalendar.this);
		pdObj.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pdObj.setMessage(getString(R.string.scload_msg));
		//途中での停止不可設定
		pdObj.setCancelable(false);
		//プログレスダイアログの表示
		pdObj.show();
		//実際の処理を行うスレッドを作成
		Thread thread = new Thread(runLoadAllDay);
		//スレッドの実行開始
		thread.start();
		Log.d("DEBUG", "MainCalendar makeCalDateCell End");
	}

	/**
	 * runLoadAllDayの実行を行うスレッド
	 */
	private Runnable runLoadAllDay = new Runnable(){
		public void run() {
			Log.d("DEBUG", "MainCalendar runLoadAllDay run Start");
			alDateCell = new ArrayList<String>();
			ArrayList<AllDayWhere> alSqlWhere = new ArrayList<AllDayWhere>();
			for(int intCt=0; intCt < (DateUtil.DAYS_OF_WEEK * 6); intCt++) {
				//42日分の開始時間と終了時間のエポックミリ秒セット作成
				AllDayWhere objADW = new AllDayWhere();
				Calendar calCell = (Calendar)gcTargetYearMonthDay.clone();
				calCell.set(Calendar.DAY_OF_MONTH, 1);
				calCell.add(Calendar.DAY_OF_MONTH, intCt-calCell.get(Calendar.DAY_OF_WEEK)+1);
				//当日開始時間のミリ秒
				objADW.setLgStartTime(calCell.getTimeInMillis());
				//当日終了時間のミリ秒
				calCell.add(Calendar.DAY_OF_MONTH, 1);
				objADW.setLgEndTime(calCell.getTimeInMillis());
				alSqlWhere.add(intCt, objADW);
			}
			//終日スケジュールリスト取得
			alDateCell = selectAllDaySchedule(alSqlWhere);
			if(alDateCell == null) {
				hObj.post(new Runnable(){
					public void run(){
						//エラー出力
						//プログレスダイアログを消去
						pdObj.dismiss();
						Toast.makeText(MainCalendar.this, getString(R.string.sqlite_read_err), Toast.LENGTH_LONG).show();
						endActivity();
					}
				});
			} else {
				hObj.post(new Runnable(){
					public void run(){
						//GridViewに「DateCellAdapter」をセット
						//DateCellAdapterのインスタンスを作成する(非同期)
						ccaDay = new CalendarCellAdapter(MainCalendar.this, alDateCell, gcTargetYearMonthDay, gcNow);
						gvObj.setAdapter(ccaDay);
						//カレンダーフリック連打防止フラグ
						blFlick = true;
						//プログレスダイアログを消去
						pdObj.dismiss();
					}
				});
			}
			Log.d("DEBUG", "MainCalendar runLoadAllDay run End");
		}
	};

	/**
	 * 終日スケジュール情報のデータベース読み込み処理
	 *
	 * @alSqlWhere 42日分の当日開始時間のミリ秒と当日終了時間のミリ秒リスト
	 * @return 42日分の週日スケジュールリスト
	 */
	private ArrayList<String> selectAllDaySchedule(ArrayList<AllDayWhere> alSqlWhere) {
		Log.d("DEBUG", "MainCalendar selectAllDaySchedule Start");
		ArrayList<String> alResult = new ArrayList<String>();
		try {
			//カーソルの取得
			alResult = sdhDB.getScheduleAllDayList(alSqlWhere);
		} catch (SQLException e) {
			alResult = null;
			Log.e("ERROR", "MainCalendar selectAllDaySchedule DB Error",e);
		} 
		Log.d("DEBUG", "MainCalendar selectAllDaySchedule End");
		return alResult;
	}

	/**
	 * 年／月文字色変更処理
	 *
	 * @param intMonth カレンダー月
	 */
	public void changeYMColor(int intMonth) {
		Log.d("DEBUG", "MainCalendar changeYMColor Start");
		if(intMonth % 2 == 0) {
			//偶数月の場合(紺瑠璃)
			tvYearMonth.setTextColor(Color.rgb(22, 74, 132));
		} else {
			//奇数月の場合(濃い紅)
			tvYearMonth.setTextColor(Color.rgb(162, 32, 65));
		}
		Log.d("DEBUG", "MainCalendar changeYMColor End");
	}

	/**
	 * グリッドビューのクリック時の処理
	 *
	 * @param parent アダプタービュー
	 * @param view ビュー
	 * @param position 位置情報
	 * @param id ID情報
	 */
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d("DEBUG", "MainCalendar onItemClick Start");
		if(blFlick == true) {
			//カレンダーフリック連打防止フラグ
			blFlick = false;
			//カレンダーをコピー
			Calendar calDay = (Calendar)gcTargetYearMonthDay.clone();
			//yyyy-MM 年月文字列を生成
			String strYM = DateUtil.YEARMONTH_FORMAT.format(calDay.getTime());
			//positionから日付を計算
			calDay.set(Calendar.DAY_OF_MONTH, 1);
			calDay.add(Calendar.DAY_OF_MONTH, position-calDay.get(Calendar.DAY_OF_WEEK)+1);
			//年月日上限・下限値チェック
			long lgLowerLimitTime = DateUtil.convMSec(getString(R.string.lower_limit_time));
			long lgUpperLimitTime = DateUtil.convMSec(getString(R.string.upper_limit_time));
			if(calDay.getTimeInMillis() < lgLowerLimitTime) {
				//下限日より過去日の場合
				Toast.makeText(MainCalendar.this, getString(R.string.select_errmsg1), Toast.LENGTH_SHORT).show();
				blFlick = true;
				return;
			} else if(calDay.getTimeInMillis() > lgUpperLimitTime) {
				//上限日より未来日の場合
				Toast.makeText(MainCalendar.this, getString(R.string.select_errmsg2), Toast.LENGTH_SHORT).show();
				blFlick = true;
				return;
			}
			//yyyy-MM-dd 年月日文字列を生成
			String strYMD = DateUtil.DATE_FORMAT.format(calDay.getTime());
			nextActivity(MainTab.class, strYMD, strYM);
		}
		Log.d("DEBUG", "MainCalendar onItemClick End");
	}

	/**
	 * DateOnClickListener
	 *  日付の文字列にセットされるリスナー
	 */
	private class DateOnClickListener implements OnClickListener, DialogInterface.OnCancelListener{
		private Context contextObj = null;
		public DateOnClickListener(Context context){
			Log.d("DEBUG", "MainCalendar DateOnClickListener DateOnClickListener Start");
			//Contextが必要なので、コンストラクタで渡して覚えておく
			contextObj = context;
			Log.d("DEBUG", "MainCalendar DateOnClickListener DateOnClickListener End");
		}

		/**
		 * クリックされた時呼び出される
		 * @param View クリックされたビュー
		 */
		public void onClick(View view) {
			Log.d("DEBUG", "MainCalendar DateOnClickListener onClick Start");
			GregorianCalendar gcObj = null;
			if(view == tvYearMonth && blFlick == true){
				//対象年月でクリックされた場合
				//ボタン連打ロックオン
				blFlick = false;
				String strTempYear = DateUtil.toDivideYearMonth(tvYearMonth.getText().toString())[0];
				String strTempMonth = DateUtil.toDivideYearMonth(tvYearMonth.getText().toString())[1];
				gcObj = DateUtil.toCalendar(DateUtil.toAddZeroYear(strTempYear) + "-" + DateUtil.toAddZeroMonth(strTempMonth) + DateUtil.FIRST_DAY);
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
								String strSkipDate = DateUtil.YEARMONTH_FORMAT.format(gcalObj.getTime());
								//引数で渡された年月日を該当するViewにセットする
								gcTargetYearMonthDay = DateUtil.toCalendar(strSkipDate + DateUtil.FIRST_DAY);
								//アプリケーション使用日
								gcNow = new GregorianCalendar();
								//カレンダーグリッドビューの生成
								gvObj = (GridView) vfCalendar.getCurrentView();
								//カレンダーグリッドビューのカラム数を設定する
								gvObj.setNumColumns(DateUtil.DAYS_OF_WEEK);
								tvYearMonth.setText(DateUtil.YEARMONTH_FORMAT.format(gcTargetYearMonthDay.getTime()));
								//年月テキストビューの文字色変更処理
								changeYMColor(gcTargetYearMonthDay.get(Calendar.MONTH)+1);
								//ScheduleDatabaseHelper初期化
								sdhDB = new ScheduleDatabaseHelper(MainCalendar.this);
								//終日スケジュールを受け取るhandler
								hObj = new Handler();
								//カレンダーセル内データ作成(非同期)
								makeCalDateCell();
							}
						}
					})
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface diObj, int intWhich) {
							if(blDialogButton == true) {
								//ダイアログ内ボタン連打ロックオン
								blDialogButton = false;
								//ボタン連打ロックオフ
								blFlick = true;
							}
						}
					})
					.setOnCancelListener(this)
					.create()
					.show();
			Log.d("DEBUG", "MainCalendar DateOnClickListener onClick End");
		}

		/**
		 * onCancel
		 * ダイアログ戻るボタン処理
		 *
		 * @param dialog dialog情報 
		 */
		@Override
		public void onCancel(DialogInterface dialog) {
			Log.d("DEBUG", "MainCalendar DateOnClickListener onCancel Start");
			//ダイアログ表示時に戻るボタンが押下された場合
			//ボタン連打ロックオフ
			blFlick = true;
			Log.d("DEBUG", "MainCalendar DateOnClickListener onCancel End");
		}

	}

	/**
	 * メニューボタン押下時の処理
	 *
	 * @param Menu 現在のメニュー
	 * @return メニューの生成に成功したらtrue
	 */
	public boolean onCreateOptionsMenu (Menu menu){
		Log.d("DEBUG", "MainCalendar onCreateOptionsMenu Start");
		//MenuInflater取得
		MenuInflater miObj = getMenuInflater();
		//MenuInflaterを使用してメニューをリソースから作成する
		miObj.inflate(R.menu.calendar_menu,menu);
		Log.d("DEBUG", "MainCalendar onCreateOptionsMenu End");
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * メニュー選択時の処理
	 *
	 * @param MenuItem 選択されたメニューアイテム
	 * @return 処理を行った場合はtrue 
	 */
	public boolean onOptionsItemSelected (MenuItem item){
		Log.d("DEBUG", "MainCalendar onOptionsItemSelected Start");
		if(item.getItemId() == R.id.searchMenu){
		//検索メニュー処理
			//カレンダーをコピー
			Calendar calDay = (Calendar)gcTargetYearMonthDay.clone();
			//yyyy-MM 年月文字列を生成
			String strYM = DateUtil.YEARMONTH_FORMAT.format(calDay.getTime());
			//検索画面へ遷移
			nextActivity(Search.class, "", strYM);
			//カレンダー画面終了
			endActivity();
			Log.d("DEBUG", "MainCalendar onOptionsItemSelected(1) End");
			return true;
		} else if (item.getItemId() == R.id.syncMenu) {
		//同期メニュー処理
			syncCalendar();
			Log.d("DEBUG", "MainCalendar onOptionsItemSelected(2) End");
			return true;
		} else if (item.getItemId() == R.id.aggregateCalculationMenu) {
		//集計メニュー処理
			//カレンダーをコピー
			Calendar calDay = (Calendar)gcTargetYearMonthDay.clone();
			//yyyy-MM 年月文字列を生成
			String strYM = DateUtil.YEARMONTH_FORMAT.format(calDay.getTime());
			//集計画面へ遷移
			nextActivity(AggregateCalculation.class, "", strYM);
			//カレンダー画面終了
			endActivity();
			Log.d("DEBUG", "MainCalendar onOptionsItemSelected(3) End");
			return true;
		} else if (item.getItemId() == R.id.subcategoryEdit) {
		//サブカテゴリ編集メニュー処理
			//カレンダーをコピー
			Calendar calDay = (Calendar)gcTargetYearMonthDay.clone();
			//yyyy-MM 年月文字列を生成
			String strYM = DateUtil.YEARMONTH_FORMAT.format(calDay.getTime());
			//サブカテゴリ編集画面へ遷移
			nextActivity(SubcategoryEdit.class, "", strYM);
			//カレンダー画面終了
			endActivity();
			Log.d("DEBUG", "MainCalendar onOptionsItemSelected(4) End");
			return true;
		} else if (item.getItemId() == R.id.backUpRestore) {
		//バックアップ／リストアメニュー処理
			//カレンダーをコピー
			Calendar calDay = (Calendar)gcTargetYearMonthDay.clone();
			//yyyy-MM 年月文字列を生成
			String strYM = DateUtil.YEARMONTH_FORMAT.format(calDay.getTime());
			//バックアップ／リストア画面へ遷移
			nextActivity(BackupRestore.class, "", strYM);
			//カレンダー画面終了
			endActivity();
			Log.d("DEBUG", "MainCalendar onOptionsItemSelected(5) End");
			return true;
		} else if (item.getItemId() == R.id.about) {
		//Aboutメニュー処理
			showDialog(this, getString(R.string.about_msg1),getString(R.string.about_msg2));
			Log.d("DEBUG", "MainCalendar onOptionsItemSelected(6) End");
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
			Log.d("DEBUG", "MainCalendar onOptionsItemSelected(7) End");
			return true;
		}
		Log.d("DEBUG", "MainCalendar onOptionsItemSelected(8) End");
		return false;
	}

	/**
	 * Googleカレンダーと同期処理
	 */
	public void syncCalendar(){
		Log.d("DEBUG", "MainCalendar syncCalendar Start");
		//ネットワーク導通確認処理
		if(NetWorkUtil.isOffline(this.getApplicationContext())) {
			//オフラインの場合
			Log.d("DEBUG","MainCalendar onCreate Offline");
			Toast.makeText(MainCalendar.this, getString(R.string.network_if_err), Toast.LENGTH_LONG).show();
		} else {
			//オンラインの場合
			Log.d("DEBUG","MainCalendar onCreate Online");
			//更新開始時刻を保存
			strUpdateStartTime = DateUtil.toUTCString(new GregorianCalendar());
			//スケジュール編集画面DB読み込み
			sdhDB = new ScheduleDatabaseHelper(this);
			AuthInfo aiObj = selectAuthInfo();
			if(aiObj == null) {
				Toast.makeText(MainCalendar.this, getString(R.string.sqlite_read_err), Toast.LENGTH_LONG).show();
				endActivity();
			} else {
				strAccessToken = aiObj.getStrAccessToken();
				strRefreshToken = aiObj.getStrRefreshToken();
				lgAccessTokenExpire = aiObj.getLgAccessTokenExpire();
				//前回更新時刻を取得
				strLastUpdate = aiObj.getStrLastUpdate();
				if(strRefreshToken == null){
					//RefreshTokenが無いので、認証処理を開始
					if(strAuthCode == null || strAuthCode.equals("")){
						//AuthCodeがなければgetOAuthCodeを実行
						getOAuthCode();
					}else{
						//AuthCodeがあれば、すでにWebでのAuth処理は行っているので、AccessTokenを取得
						//認証結果を受け取るhandler
						hGetTokenObj = new Handler();
						//カレンダー情報を受け取るhandler
						hSyncCalendarObj = new Handler();
						//終日スケジュールを受け取るhandler
						hObj = new Handler();
						getAccessToken();
					}
				} else if (lgAccessTokenExpire<Calendar.getInstance().getTimeInMillis()){
					Log.d("DEBUG", "MainCalendar syncCalendar lgAccessTokenExpire：" + lgAccessTokenExpire);
					Log.d("DEBUG", "MainCalendar syncCalendar < " + Calendar.getInstance().getTimeInMillis());
					//認証結果を受け取るhandler
					hGetTokenObj = new Handler();
					//カレンダー情報を受け取るhandler
					hSyncCalendarObj = new Handler();
					//終日スケジュールを受け取るhandler
					hObj = new Handler();
					//AccessTokenが期限切れなのでAccessTokenを再取得してから、getGoogleCalendarを実行
					refreshToken();
				}else{
					//終日スケジュールを受け取るhandler
					hObj = new Handler();
					//カレンダー情報を受け取るhandler
					hSyncCalendarObj = new Handler();
					//AccessTokenが有効なので、そのままgetGoogleCalendar()を実行
					syncGoogleCalendar();
				}
			}
		}
		Log.d("DEBUG", "MainCalendar syncCalendar End");
	}

	/**
	 * startOAuth
	 * OAuth 2.0に従った認証処理の為にブラウザを使用して
	 * Googleの認証ページを開く
	 */
	private void getOAuthCode(){
		Log.d("DEBUG", "MainCalendar getOAuthCode Start");
		//URIを指定してブラウザを起動する為のIntentを作成
		Intent intent = new Intent(Intent.ACTION_VIEW, OAUTH_URI);
		//暗黙的インテントなので、ユーザの選択したブラウザでURIを開く
		startActivityForResult(intent,BROWSER);
		Log.d("DEBUG", "MainCalendar getOAuthCode End");
	}

	/**
	 * AccessToken取得非同期開始処理
	 *
	 */
	private void getAccessToken(){
		Log.d("DEBUG", "MainCalendar getAccessToken Start");
		//プログレスダイアログの作成
		pdObj = new ProgressDialog(MainCalendar.this);
		pdObj.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pdObj.setMessage(getString(R.string.getToken));
		//途中での停止不可設定
		pdObj.setCancelable(false);
		//プログレスダイアログの表示
		pdObj.show();
		//実際の処理を行うスレッドを作成
		Thread thread = new Thread(runGetAccessToken);
		//スレッドの実行開始
		thread.start();
		Log.d("DEBUG", "MainCalendar getAccessToken End");
	}

	/**
	 * runGetAccessTokenの実行を行うスレッド
	 */
	private Runnable runGetAccessToken = new Runnable(){
		public void run() {
			Log.d("DEBUG", "MainCalendar runGetAccessToken run Start");
			strAccessToken = null;
			strRefreshToken = null;
			ArrayList<NameValuePair> alNVP = new ArrayList<NameValuePair>();
			//サーバに渡すパラメータをnameValuePairにセット
			//codeはgetOAuthCodeで取得した認証コード(AuthCode)
			alNVP.add(new BasicNameValuePair("client_id",CLIENT_ID));
			alNVP.add(new BasicNameValuePair("client_secret",CLIENT_SECRET));
			alNVP.add(new BasicNameValuePair("redirect_uri",REDIRECT_URI));
			alNVP.add(new BasicNameValuePair("grant_type",GRANT_TYPE_A));
			alNVP.add(new BasicNameValuePair("code",strAuthCode));
			//token列の取得処理
			mpTokens = getToken(alNVP);
			if(mpTokens != null){
				hGetTokenObj.post(new Runnable(){
					public void run(){
						//tokensに格納された値を変数に格納
						strAccessToken = mpTokens.get(ACCESS_TOKEN);
						strRefreshToken = mpTokens.get(REFRESH_TOKEN);
						lgAccessTokenExpire = Long.valueOf(mpTokens.get(EXPIRES_IN));
						AuthInfo aiObj = new AuthInfo();
						aiObj.setStrAccessToken(strAccessToken);
						aiObj.setStrRefreshToken(strRefreshToken);
						aiObj.setLgAccessTokenExpire(lgAccessTokenExpire);
						boolean blResult = sdhDB.updateAuthInfoRefreshToken(aiObj);
						if(blResult == false) {
							//エラー出力
							//プログレスダイアログを消去
							strAuthCode = null;
							pdObj.dismiss();
							Toast.makeText(MainCalendar.this, getString(R.string.getToken_err), Toast.LENGTH_LONG).show();
						} else {
							//プログレスダイアログを消去
							pdObj.dismiss();
							//Googleカレンダー情報の取得更新処理
							syncGoogleCalendar();
						}
					}
				});
			} else {
				hGetTokenObj.post(new Runnable(){
					public void run(){
						//エラー出力
						//プログレスダイアログを消去
						strAuthCode = null;
						pdObj.dismiss();
						Toast.makeText(MainCalendar.this, getString(R.string.getToken_err), Toast.LENGTH_LONG).show();
					}
				});
			}
			Log.d("DEBUG", "MainCalendar runGetAccessToken run End");
		}
	};

	/**
	 * サーバからTokenを取得する
	 *
	 * @param ArrayList<NameValuePair> 名前と値のペアのリスト
	 * @return Map<String,String> 名前と文字列のMap (実体はHashMap)
	 */
	private Map<String,String> getToken(ArrayList<NameValuePair> nameValuePair) {
		Log.d("DEBUG", "MainCalendar getToken Start");
		long lgNowTime = Calendar.getInstance().getTimeInMillis();
		HashMap<String,String> hmResult = new HashMap<String,String>();
		//HttpPostクラスのオブジェクトをURLを指定して作成
		HttpPost hpObj = new HttpPost(TOKEN_URL);
		//DefaultHttpClientメソッドでHttpClientを取得し、ヘッダやEntityを設定してexecute
		HttpClient hcObj = new DefaultHttpClient();
		hpObj.setHeader("Content-Type",CONTENT_TYPE_AX);
		try {
			hpObj.setEntity(new UrlEncodedFormEntity(nameValuePair));
			HttpResponse response = hcObj.execute(hpObj);
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				//executeの結果のHttpResponseから受信したデータを取得
				BufferedReader brObj = new BufferedReader(
					new InputStreamReader(
					new BufferedInputStream(response.getEntity().getContent())));
				String strJson = null;
				try{
					StringBuilder sbObj = new StringBuilder();
					String strLine;
					while((strLine = brObj.readLine()) != null){
						sbObj.append(strLine);
					}
					strJson = sbObj.toString();
					//取り出したデータをJSONパーサで解釈して値を取得
					JSONObject joRoot = new JSONObject(strJson);
					if(joRoot.has(ACCESS_TOKEN)){
						//Log.d("DEBUG","MainCalendar getToken ACCESS_TOKEN：" + joRoot.getString(ACCESS_TOKEN));
						hmResult.put(ACCESS_TOKEN, joRoot.getString(ACCESS_TOKEN));
					}
					if(joRoot.has(REFRESH_TOKEN)){
						//Log.d("DEBUG","MainCalendar getToken REFRESH_TOKEN：" + joRoot.getString(REFRESH_TOKEN));
						hmResult.put(REFRESH_TOKEN, joRoot.getString(REFRESH_TOKEN));
					}
					if(joRoot.has(EXPIRES_IN)){
						//Log.d("DEBUG","MainCalendar getToken EXPIRES_IN：" + joRoot.getString(EXPIRES_IN));
						long lgExpires = Long.valueOf(joRoot.getString(EXPIRES_IN)) * 1000;
						lgNowTime = lgNowTime + lgExpires;
						hmResult.put(EXPIRES_IN, String.valueOf(lgNowTime));
					}
				} catch (JSONException e) {
					Log.e("ERROR", "MainCalendar getToken JSONException : ", e);
					return null;
				}
			} else {
				//認証コードが誤っている場合、HTTPStatusエラー400発生
				Log.e("ERROR", "MainCalendar getToken HttpStatus Error:" + response.getStatusLine().getStatusCode());
				return null;
			}
		} catch (UnsupportedEncodingException e) {
			Log.e("ERROR", "MainCalendar getToken UnsupportedEncodingException : ", e);
			return null;
		} catch (ClientProtocolException e) {
			Log.e("ERROR", "MainCalendar getToken ClientProtocolException : ", e);
			return null;
		} catch (IOException e) {
			Log.e("ERROR", "MainCalendar getToken IOException : ", e);
			return null;
		}
		Log.d("DEBUG", "MainCalendar getToken End");
		return hmResult;
	}

	/**
	 * AccessTokenの更新処理
	 *
	 */
	private void refreshToken(){
		Log.d("DEBUG", "MainCalendar refreshToken Start");
		//プログレスダイアログの作成
		pdObj = new ProgressDialog(MainCalendar.this);
		pdObj.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pdObj.setMessage(getString(R.string.getToken));
		//途中での停止不可設定
		pdObj.setCancelable(false);
		//プログレスダイアログの表示
		pdObj.show();
		//実際の処理を行うスレッドを作成
		Thread thread = new Thread(runRefreshToken);
		//スレッドの実行開始
		thread.start();
		Log.d("DEBUG", "MainCalendar refreshToken End");
	}

	/**
	 * runRefreshTokenの実行を行うスレッド
	 */
	private Runnable runRefreshToken = new Runnable(){
		public void run() {
			Log.d("DEBUG", "MainCalendar runRefreshToken run Start");
			strAccessToken = null;
			//サーバに渡すパラメータをnameValuePairにセット
			ArrayList<NameValuePair> alNVP = new ArrayList<NameValuePair>();
			alNVP.add(new BasicNameValuePair("client_id",CLIENT_ID));
			alNVP.add(new BasicNameValuePair("client_secret",CLIENT_SECRET));
			alNVP.add(new BasicNameValuePair("refresh_token",strRefreshToken));
			alNVP.add(new BasicNameValuePair("grant_type",GRANT_TYPE_R));
			//getTokenを実行し、token列を取得
			mpTokens = getToken(alNVP);
			if(mpTokens != null){
				hGetTokenObj.post(new Runnable(){
					public void run(){
						strAccessToken = mpTokens.get(ACCESS_TOKEN);
						lgAccessTokenExpire = Long.valueOf(mpTokens.get(EXPIRES_IN));
						AuthInfo aiObj = new AuthInfo();
						aiObj.setStrAccessToken(strAccessToken);
						aiObj.setLgAccessTokenExpire(lgAccessTokenExpire);
						boolean blResult = sdhDB.updateAuthInfoAccessToken(aiObj);
						if(blResult == false) {
							//エラー出力
							//プログレスダイアログを消去
							pdObj.dismiss();
							Toast.makeText(MainCalendar.this, getString(R.string.getToken_err), Toast.LENGTH_LONG).show();
						} else {
							//プログレスダイアログを消去
							pdObj.dismiss();
							//Googleカレンダー情報の取得更新処理
							syncGoogleCalendar();
						}
					}
				});
			} else {
				hGetTokenObj.post(new Runnable(){
					public void run(){
						//エラー出力
						//プログレスダイアログを消去
						pdObj.dismiss();
						Toast.makeText(MainCalendar.this, getString(R.string.getToken_err), Toast.LENGTH_LONG).show();
					}
				});
			}
			Log.d("DEBUG", "MainCalendar runRefreshToken run End");
		}
	};

	/**
	 * getGoogleCalendar と updateGoogleCalendarを呼び出す
	 * プログレスダイアログを表示する為に別スレッドで実行する。
	 */
	public void syncGoogleCalendar(){
		Log.d("DEBUG", "MainCalendar syncGoogleCalendar Start");
		//プログレスダイアログの作成
		pdObj = new ProgressDialog(MainCalendar.this);
		pdObj.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pdObj.setMessage(getString(R.string.nowOnSync));
		//途中での停止不可設定
		pdObj.setCancelable(false);
		//プログレスダイアログの表示
		pdObj.show();
		//実際の処理を行うスレッドを作成
		Thread thread = new Thread(runSyncGoogleCalendar);
		//スレッドの実行開始
		thread.start();
		Log.d("DEBUG", "MainCalendar syncGoogleCalendar End");
	}

	/**
	 * getGoogleCalendarの実行を行うスレッド
	 */
	private Runnable runSyncGoogleCalendar = new Runnable(){
		public void run() {
			Log.d("DEBUG", "MainCalendar runSyncGoogleCalendar run Start");
			blSyncGC = true;
			boolean blGetGC = true;
			boolean blUpdateGC = true;
			intUnreflected = 0;
			//取得処理
			blGetGC = getGoogleCalendar();
			if(blGetGC == false) {
				blSyncGC = false;
			} else {
				//更新処理
				blUpdateGC = updateGoogleCalendar();
				if(blUpdateGC == false) {
					blSyncGC = false;
				} else {
					//更新失敗データ件数取得
					boolean blChangeScheduleCountResult = selectChangeScheduleCount();
					if(blChangeScheduleCountResult == false) {
						Log.e("ERROR","MainCalendar runSyncGoogleCalendar 更新スケジュールカウント取得エラー");
						blSyncGC = false;
					}
				}
			}
			//メモリ開放
			System.gc();
			//更新が終わったらプログレスダイアログを消去
			pdObj.dismiss();
			hSyncCalendarObj.post(new Runnable(){
				public void run(){
					//同期処理のエラーメッセージ出力
					if(blSyncGC == false) {
						Toast.makeText(MainCalendar.this, getString(R.string.gCSync_err), Toast.LENGTH_LONG).show();
					}
					//同期処理時に反映されなかったデータの件数出力
					if(intUnreflected != 0) {
						Toast.makeText(MainCalendar.this, getString(R.string.gCSync_Unreflected) + intUnreflected, Toast.LENGTH_LONG).show();
					}
					//hSyncCalendarObjを持っているプロセスで実行される。
					//カレンダーセル内データ作成(非同期)
					makeCalDateCell();
				}
			});
			Log.d("DEBUG", "MainCalendar runSyncGoogleCalendar run End");
		}
	};

	/**
	 * Googleカレンダーの情報を取得する
	 * 
	 * @return boolean true:正常に処理完了, false:処理中にエラー発生
	 */
	public boolean getGoogleCalendar(){
		Log.d("DEBUG", "MainCalendar getGoogleCalendar Start");
		boolean blResult = true;
		boolean blInitFlg = true;
		String strUpdatedQuery = null;
		if(strLastUpdate != null){
			//前回更新日時が存在していれば、それをupdated-minにセットし、それ以降の更新データのみ取得
			strUpdatedQuery = "&updated-min=" + strLastUpdate;
		} else {
			//一度も更新していなければ、updateQueryは指定しない
			strUpdatedQuery = "";
		}
		//初回(1〜300件目)のstrNextUrlセット
		String strNextUrl = CALENDAR_FEED_URL;
		//ScheduleDatabaseHelper初期化
		sdhDB = new ScheduleDatabaseHelper(MainCalendar.this);
		//nextUrlがあるかぎり繰り返す
		while(strNextUrl != null){
			//パーサー作成(ループごとにオブジェクトを初期化したい為、あえてループ内に記述)
			GDataCalendarParser gcpObj = new GDataCalendarParser(this, sdhDB);
			StringBuffer sbObj = new StringBuffer();
			sbObj.append(strNextUrl);
			//認証のためにAccessTokenをoauth_tokenとしてURLに追加
			sbObj.append("&oauth_token=");
			//Googleカレンダーデータ取得中にアクセストークンの有効期限が切れた場合
			if(strRefreshToken != null && (lgAccessTokenExpire < Calendar.getInstance().getTimeInMillis())) {
				boolean blReToken = true;
				//アクセストークンの再取得処理
				blReToken = reacquisitionToken();
				if(blReToken == false) {
					//アクセストークンの再取得にてエラーが発生した場合
					Log.e("ERROR", "MainCalendar getGoogleCalendar アクセストークンの再取得に失敗");
					blResult = false;
					break;
				}
			}
			sbObj.append(strAccessToken);
			//300件目以降のデータ取得の場合は前回更新日時がstrNextUrlに含まれる為、追加しない
			if(blInitFlg == true) {
				sbObj.append(strUpdatedQuery);
			}
			ArrayList<String> alResult = new ArrayList<String>();
			alResult = gcpObj.parse(httpGet(sbObj.toString()), 0l);
			if(alResult == null || alResult.get(1) != null) {
				//パーサー内で処理中にエラーが発生した場合
				blResult = false;
				//Googleカレンダーオブジェクト初期化(OutOfMemoryError対策)
				alResult = null;
				break;
			} else {
				strNextUrl = alResult.get(0);
				Log.d("DEBUG", "MainCalendar getGoogleCalendar strNextUrl：" + strNextUrl);
				blInitFlg = false;
				//判定オブジェクト初期化(OutOfMemoryError対策)
				alResult = null;
			}
			//StringBuilderオブジェクト初期化(OutOfMemoryError対策)
			sbObj = null;
			//パーサーオブジェクト初期化(OutOfMemoryError対策)
			gcpObj = null;
			//メモリ開放
			System.gc();
		}
		if(blResult == true) {
			strLastUpdate = strUpdateStartTime;
			//直前の更新日時保存処理
			blResult = saveLastUpdate();
		}
		Log.d("DEBUG", "MainCalendar getGoogleCalendar End");
		return blResult;
	}

	/**
	 * アクセストークンの再取得処理
	 * 
	 * @return boolean true:正常に処理完了, false:処理中にエラー発生
	 */
	public boolean reacquisitionToken(){
		Log.d("DEBUG", "MainCalendar reacquisitionToken Start");
		boolean blResult = true;
		//サーバに渡すパラメータをnameValuePairにセット
		ArrayList<NameValuePair> alNVP = new ArrayList<NameValuePair>();
		alNVP.add(new BasicNameValuePair("client_id",CLIENT_ID));
		alNVP.add(new BasicNameValuePair("client_secret",CLIENT_SECRET));
		alNVP.add(new BasicNameValuePair("refresh_token",strRefreshToken));
		alNVP.add(new BasicNameValuePair("grant_type",GRANT_TYPE_R));
		//getTokenを実行し、token列を取得
		mpTokens = getToken(alNVP);
		if(mpTokens != null){
			strAccessToken = mpTokens.get(ACCESS_TOKEN);
			lgAccessTokenExpire = Long.valueOf(mpTokens.get(EXPIRES_IN));
			AuthInfo aiObj = new AuthInfo();
			aiObj.setStrAccessToken(strAccessToken);
			aiObj.setLgAccessTokenExpire(lgAccessTokenExpire);
			blResult = sdhDB.updateAuthInfoAccessToken(aiObj);
		} else {
			blResult = false;
		}
		Log.d("DEBUG", "MainCalendar reacquisitionToken End");
		return blResult;
	}

	/**
	 * URLを渡してデータをInputStreamで返す
	 * @param String アクセスするURL
	 * *return InputStream サーバから受信したデータにアクセスするためのInputStream
	 */
	public InputStream httpGet(String strURL){
		Log.d("DEBUG", "MainCalendar httpGet Start");
		HttpGet hgObj = new HttpGet(strURL);
		HttpClient hcObj = new DefaultHttpClient();
		//GData Version 2用のHeaderをセット
		hgObj.setHeader(GDATA_VERSION_TAG,GDATA_VERSION);
		HttpResponse response;
		try {
			//HTTPアクセスを実行する
			response = hcObj.execute(hgObj);
			int statusCode = response.getStatusLine().getStatusCode();
			Log.d("DEBUG","MainCalendar httpGet HttpResponseCode : " + statusCode);
			//HttpGet,HttpClientオブジェクト初期化(OutOfMemoryエラー対策)
			hgObj = null;
			hcObj = null;
			if(statusCode == HttpStatus.SC_OK){
				//アクセスに成功したらresponseからInputStreamを取得する
				return response.getEntity().getContent();
			}else if(statusCode == HttpStatus.SC_UNAUTHORIZED){
				return null;
			}
		} catch (Exception e) {
			Log.e("ERROR", "MainCalendar httpGet ERROR：", e);
			//HttpGet,HttpClientオブジェクト初期化(OutOfMemoryエラー対策)
			hgObj = null;
			hcObj = null;
		}
		Log.d("DEBUG", "MainCalendar httpGet End");
		return null;
	}

	/**
	 * 直前の更新日時保存処理
	 * 
	 * @return boolean true:正常に処理完了, false:処理中にエラー発生
	 */
	public boolean saveLastUpdate(){
		Log.d("DEBUG", "MainCalendar saveLastUpdate Start");
		boolean blResult = true;
		AuthInfo aiObj = new AuthInfo();
		aiObj.setStrLastUpdate(strLastUpdate);
		blResult = sdhDB.updateAuthInfoLastUpdate(aiObj);
		Log.d("DEBUG", "MainCalendar saveLastUpdate End");
		return blResult;
	}

	/**
	 * Googleカレンダーにデータをアップロードする
	 * 
	 * @return boolean true:正常に処理完了, false:処理中にエラー発生
	 */
	public boolean updateGoogleCalendar(){
		Log.d("DEBUG", "MainCalendar updateGoogleCalendar Start");
		boolean blResult = true;
		//ScheduleDatabaseHelper初期化
		sdhDB = new ScheduleDatabaseHelper(this);
		//スケジュールリスト取得
		boolean blChangeScheduleResult = selectChangeSchedule();
		if(blChangeScheduleResult == false) {
			Log.e("ERROR","MainCalendar updateGoogleCalendar 更新スケジュールリスト取得エラー");
			blResult = false;
		} else {
			//パーサー作成
			GDataCalendarParser gcpObj = new GDataCalendarParser(this, sdhDB);
			//Delete処理時のストリーム結果
			String strDeleteResult = null;
			String strInsertTime = null;
			//Insert処理
			for(GDataEvent gdeObj : alInsertGCEvents){
				//Insert用のデータを作成しPOSTして結果を取得
				//結果をparseして新しいデータとして登録
				StringBuffer sbPostURL = new StringBuffer();
				sbPostURL.append(DEFAULT_URL);
				sbPostURL.append("?oauth_token=");
				//Googleカレンダーデータ更新中にアクセストークンの有効期限が切れた場合
				if(strRefreshToken != null && (lgAccessTokenExpire < Calendar.getInstance().getTimeInMillis())) {
					boolean blReToken = true;
					//アクセストークンの再取得処理
					blReToken = reacquisitionToken();
					if(blReToken == false) {
						//アクセストークンの再取得にてエラーが発生した場合
						Log.e("ERROR", "MainCalendar updateGoogleCalendar アクセストークンの再取得に失敗");
						blResult = false;
						break;
					}
				}
				sbPostURL.append(strAccessToken);
				//新規登録時刻を保存
				strInsertTime = DateUtil.toUTCString(new GregorianCalendar());
				//新規登録時刻をupdated-minにセットし、新規登録後の更新データのみ取得
				sbPostURL.append("&updated-min=");
				sbPostURL.append(strInsertTime);
				ArrayList<String> alResult = new ArrayList<String>();
				alResult = gcpObj.parse(httpPost(sbPostURL.toString(), gcpObj.insertSerializer(gdeObj)), gdeObj.getLgAlarmFlag());
				if(blHttpSucceeded && alResult != null && alResult.get(1) == null && intResponseCode == HttpURLConnection.HTTP_CREATED){
					//古いデータを削除
					//SQLパラメータ作成(ID)
					Schedule objSchedule = new Schedule();
					objSchedule.setLgId(gdeObj.getLgId());
					//SCHEDULEテーブル削除(物理削除)
					blResult = sdhDB.deleteScheduleId(objSchedule);
					if(blResult == false) {
						//SCHEDULEテーブルデータの物理削除エラー
						Log.e("ERROR","MainCalendar updateGoogleCalendar Insert処理の物理削除失敗 ： " + gdeObj.getLgId());
						//判定オブジェクト初期化(OutOfMemoryError対策)
						alResult = null;
						break;
					}
					//判定オブジェクト初期化(OutOfMemoryError対策)
					alResult = null;
				} else if(blHttpSucceeded && alResult != null && alResult.get(1) == null && intResponseCode == HttpURLConnection.HTTP_OK) {
					//通信自体は正常終了したが、Googleカレンダー側で新規登録されなかった場合
					Log.e("ERROR","MainCalendar updateGoogleCalendar Insert処理のリターンコードが200の時 ： " + gdeObj.getLgId());
					//判定オブジェクト初期化(OutOfMemoryError対策)
					alResult = null;
				} else {
					//HttpPost処理時にエラー発生
					Log.e("ERROR","MainCalendar updateGoogleCalendar Insert処理 ： " + gdeObj.getLgId());
					blResult = false;
					//判定オブジェクト初期化(OutOfMemoryError対策)
					alResult = null;
					break;
				}
			}

			//Insert処理中にエラーが発生していなければUpdate処理を実行
			if(blResult == true) {
				//Update処理
				for(GDataEvent gdeObj : alUpdateGCEvents){
					//編集用URLを取得
					StringBuffer sbPostURL = new StringBuffer();
					sbPostURL.append(gdeObj.getStrEditUrl());
					sbPostURL.append("?oauth_token=");
					//Googleカレンダーデータ更新中にアクセストークンの有効期限が切れた場合
					if(strRefreshToken != null && (lgAccessTokenExpire < Calendar.getInstance().getTimeInMillis())) {
						boolean blReToken = true;
						//アクセストークンの再取得処理
						blReToken = reacquisitionToken();
						if(blReToken == false) {
							//アクセストークンの再取得にてエラーが発生した場合
							Log.e("ERROR", "MainCalendar updateGoogleCalendar アクセストークンの再取得に失敗");
							blResult = false;
							break;
						}
					}
					sbPostURL.append(strAccessToken);
					//アップデート結果をパースして現在のデータを更新
					ArrayList<String> alResult = new ArrayList<String>();
					alResult = gcpObj.parse(httpPut(sbPostURL.toString(),gcpObj.updateSerializer(httpGet(sbPostURL.toString()),gdeObj)), gdeObj.getLgAlarmFlag());
					//同値更新時にGoogleカレンダー側が更新されない為、
					//Modifiedフラグが残って正常時にも無限更新を実施してしまう問題が発生したが、
					//その対処として、この位置でMODIFIEDフラグを初期化すべきではない。
					//同値更新が行われないようにすべき。)
					if(alResult == null || !(blHttpSucceeded && alResult.get(1) == null)){
						//HttpPut処理時或いは、DB更新時にエラー発生
						Log.e("ERROR","MainCalendar updateGoogleCalendar Update処理 ： " + gdeObj.getLgId());
						blResult = false;
						//判定オブジェクト初期化(OutOfMemoryError対策)
						alResult = null;
						break;
					}
					//判定オブジェクト初期化(OutOfMemoryError対策)
					alResult = null;
				}
			}

			//Insert処理orUpdate処理中にエラーが発生していなければDelete処理を実行
			if(blResult == true) {
				//Delete処理
				for(GDataEvent gdeObj : alDeleteGCEvents){
					//編集用URLを使って、httpDeleteの処理
					StringBuffer sbPostURL = new StringBuffer();
					sbPostURL.append(gdeObj.getStrEditUrl());
					sbPostURL.append("?oauth_token=");
					//Googleカレンダーデータ更新中にアクセストークンの有効期限が切れた場合
					if(strRefreshToken != null && (lgAccessTokenExpire < Calendar.getInstance().getTimeInMillis())) {
						boolean blReToken = true;
						//アクセストークンの再取得処理
						blReToken = reacquisitionToken();
						if(blReToken == false) {
							//アクセストークンの再取得にてエラーが発生した場合
							Log.e("ERROR", "MainCalendar updateGoogleCalendar アクセストークンの再取得に失敗");
							blResult = false;
							break;
						}
					}
					sbPostURL.append(strAccessToken);
					InputStream isResult = httpDelete(sbPostURL.toString());
					if(isResult != null) {
						strDeleteResult = InputCheckUtil.convertStreamToString(isResult);
					} else {
						strDeleteResult = null;
					}
					if(blHttpSucceeded && (strDeleteResult != null && strDeleteResult.equals(""))){
						//データベースからも削除
						//SQLパラメータ作成(ID)
						Schedule objSchedule = new Schedule();
						objSchedule.setLgId(gdeObj.getLgId());
						//SCHEDULEテーブル削除(物理削除)
						blResult = sdhDB.deleteScheduleId(objSchedule);
						if(blResult == false) {
							//SCHEDULEテーブルデータの物理削除エラー
							Log.e("ERROR","MainCalendar updateGoogleCalendar Delete処理の物理削除失敗 ： " + gdeObj.getLgId());
							break;
						}
					} else if(blHttpSucceeded && (strDeleteResult != null && strDeleteResult.equals("") == false)) {
						//通信自体は正常終了したが、Googleカレンダー側で削除されなかった場合
						Log.e("ERROR","MainCalendar updateGoogleCalendar Delete処理のリターンコードが200で削除されなかった場合 ： " + gdeObj.getLgId());
					} else {
						//HttpPost処理時にエラー発生
						Log.e("ERROR","MainCalendar updateGoogleCalendar Delete処理 ： " + gdeObj.getLgId());
						blResult = false;
						break;
					}
				}
			}
			//オブジェクト初期化(OutOfMemoryError対策)
			gcpObj = null;
			alInsertGCEvents = null;
			alUpdateGCEvents = null;
			alDeleteGCEvents = null;
		}
		Log.d("DEBUG", "MainCalendar updateGoogleCalendar End");
		return blResult;
	}

	/**
	 * 更新対象スケジュール情報のデータベース読み込み処理
	 *
	 * @return 処理を行った場合はtrue
	 */
	private boolean selectChangeSchedule() {
		Log.d("DEBUG", "MainCalendar selectChangeSchedule Start");
		boolean blResult = true;
		ScheduleCursor scObj = null;
		//insert/update/deleteそれぞれのデータのリスト
		alInsertGCEvents = new ArrayList<GDataEvent>();
		alUpdateGCEvents = new ArrayList<GDataEvent>();
		alDeleteGCEvents = new ArrayList<GDataEvent>();
		try {
			String[] where_args = {};
			//カーソルの取得
			scObj = sdhDB.getChangeScheduleList(where_args);
			//カーソルポインター初期化
			startManagingCursor(scObj);
			Log.d("DEBUG", "MainCalendar selectChangeSchedule ScheduleCursor Count : " + scObj.getCount());
			for( int intCt=0; intCt<scObj.getCount(); intCt++){
				//スケジュールのセット
				GDataEvent gdeObj = new GDataEvent();
				//ID
				gdeObj.setLgId(scObj.getColId());
				//削除フラグ
				gdeObj.setLgDeleted(scObj.getColDeleted());
				//更新フラグ
				gdeObj.setLgModified(scObj.getColModified());
				//件名
				gdeObj.setStrTitle(scObj.getColTitle());
				//場所
				gdeObj.setStrWhere(scObj.getColGdWhere());
				//開始日時
				gdeObj.setGcStart(scObj.getColGdWhenStarttime());
				//終了日時
				gdeObj.setGcEnd(scObj.getColGdWhenEndtime());
				//説明
				gdeObj.setStrContent(scObj.getColContent());
				//イベント作成日時
				gdeObj.setGcPublished(scObj.getColPublished());
				//イベント最終更新日時
				gdeObj.setGcUpdated(scObj.getColUpdated());
				//カテゴリ
				gdeObj.setStrCategory(scObj.getColCategory());
				//編集URL
				gdeObj.setStrEditUrl(scObj.getColEditUrl());
				//状態
				gdeObj.setStrEventStatus(scObj.getColGdEventstatus());
				//カレンダーID
				gdeObj.setStrEventId(scObj.getColCalendarId());
				//ETAG
				gdeObj.setStrEtag(scObj.getColEtag());
				//繰り返しイベント(当該アプリでは未対応)
				//アラームリスト(当該アプリでは未対応)
				gdeObj.setAlarmMap(scObj.getColAlarmList());
				//端末アラーム設定フラグ(TDCC固有機能)
				gdeObj.setLgAlarmFlag(scObj.getColAlarmFlag());
				
				if(gdeObj.getStrEventId() == null){
					//イベントIdが設定されていないデータはGoogleカレンダーにはない新規データなのでinsertリストに登録
					alInsertGCEvents.add(gdeObj);
				}else if(gdeObj.getLgDeleted() == 1){
					//削除フラグが立っているのでdeleteリストに登録
					alDeleteGCEvents.add(gdeObj);
				}else {
					//それ以外はupdateリストに登録
					alUpdateGCEvents.add(gdeObj);
				}
				scObj.moveToNext();
			}
			scObj.close();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "MainCalendar selectChangeSchedule DB Error",e);
		} finally {
			if(scObj != null) {
				scObj.close();
			}
		}
		Log.d("DEBUG", "MainCalendar selectChangeSchedule End");
		return blResult;
	}

	/**
	 * Http Delete処理
	 * 
	 * @param strUrl
	 * @return InputStream 処理結果のXMLを返すInputStream
	 */
	public InputStream httpDelete(String strUrl){
		Log.d("DEBUG", "MainCalendar httpDelete Start");
		Log.d("DEBUG", "MainCalendar httpDelete End");
		return httpPostXml(strUrl, "", "DELETE");
	}

	/**
	 * Http Post処理
	 *
	 * @param strUrl
	 * @param strXml
	 * @return InputStream 処理結果のXMLを返すInputStream
	 */
	public InputStream httpPost(String strUrl, String strXml){
		Log.d("DEBUG", "MainCalendar httpPost Start");
		Log.d("DEBUG", "MainCalendar httpPost End");
		return httpPostXml(strUrl, strXml, null);
	}

	/**
	 * Http Put処理
	 *
	 * @param strUrl
	 * @param strXml
	 * @return InputStream 処理結果のXMLを返すInputStream
	 */
	public InputStream httpPut(String strUrl,String strXml){
		Log.d("DEBUG", "MainCalendar httpPut Start");
		Log.d("DEBUG", "MainCalendar httpPut End");
		return httpPostXml(strUrl, strXml,"PUT");
	}

	/**
	 * XMLファイルを適当なメソッドで送信する。
	 * 
	 * @param strUrl
	 * @param strXml
	 * @param strMethod メソッド(PUT,DELETE)
	 * @return InputStream 処理結果を返すInputStream
	 */
	public InputStream httpPostXml(String strUrl, String strXml, String strMethod){
		Log.d("DEBUG", "MainCalendar httpPostXml Start");
		blHttpSucceeded = false;
		try {
			while(strUrl != null){
				URL urlObj = new URL(strUrl);
				//URLを指定してコネクションを開く
				HttpURLConnection httpURLCObj = (HttpURLConnection)urlObj.openConnection();
				//メソッドはPOSTを使用する。
				httpURLCObj.setRequestMethod("POST");
				//GData-Versionを指定する
				httpURLCObj.setRequestProperty(GDATA_VERSION_TAG,GDATA_VERSION);
				if(strMethod != null){
					//POST以外のメソッドを指定された時は、ヘッダにIf-Match:*とX-HTTP-Method-Overrideを指定
					httpURLCObj.setRequestProperty("If-Match","*");
					httpURLCObj.setRequestProperty("X-HTTP-Method-Override",strMethod);
				}
				//コンテンツ出力設定
				httpURLCObj.setDoOutput(true);
				//Content-Typeの設定はXMLファイル
				httpURLCObj.setRequestProperty("Content-Type", CONTENT_TYPE_AA);
				//キャッシュ無効
				httpURLCObj.setUseCaches(false);
				//OutputStreamWriterに引数のXMLを設定して出力
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLCObj.getOutputStream(),"UTF-8");
				outputStreamWriter.write(strXml);
				outputStreamWriter.close();
				//HTTPレスポンスコード取得
				intResponseCode = 0;
				intResponseCode = httpURLCObj.getResponseCode();
				strUrl = null;
				Log.d("DEBUG","MainCalendar httpPostXml HttpResponseCode : " + intResponseCode);
				if(intResponseCode == HttpURLConnection.HTTP_OK || intResponseCode == HttpURLConnection.HTTP_CREATED){
					//レスポンスコードがOKまたはCREATEDの場合は処理が完了
					blHttpSucceeded = true;
					Log.d("DEBUG", "MainCalendar httpPostXml End(1)");
					//入力のInputStreamを返して終了
					return httpURLCObj.getInputStream();
				}else if(intResponseCode == HttpURLConnection.HTTP_MOVED_TEMP){
					//レスポンスコードがMOVED_TEMPだった場合、リダイレクトなので、
					//ヘッダからLocationヘッダを取り出して、URLに指定して再実行(先頭のwhileに戻る）
					Map<String,List<String>> mResponseHeaders = httpURLCObj.getHeaderFields();
					if(mResponseHeaders.containsKey("Location")){
						strUrl = mResponseHeaders.get("Location").get(0);
					}else if(mResponseHeaders.containsKey("location")){
						strUrl = mResponseHeaders.get("location").get(0);
					}
				} else {
					//レスポンスコードがOKまたはCREATEDまたはMOVED_TEMP以外の場合
					blHttpSucceeded = false;
					Log.d("DEBUG", "MainCalendar httpPostXml End(2) ： レスポンスコードがOK,CREATED,MOVED_TEMP以外の場合");
				}
			}
		} catch (Exception e) {
			Log.e("ERROR", "MainCalendar httpPostXml ERROR：", e);
		}
		Log.d("DEBUG", "MainCalendar httpPostXml End(3)");
		return null;
	}

	/**
	 * AuthInfoのデータベース読み込み処理
	 * @param strAryArgs SQL条件
	 * @return 処理を行った場合はtrue
	 */
	private AuthInfo selectAuthInfo() {
		Log.d("DEBUG", "MainCalendar selectAuthInfo Start");
		AuthInfo authInfoObj = new AuthInfo();
		ScheduleDatabaseHelper.AuthInfoCursor sdObj = null;
		try {
			//データベース検索処理
			String[] where_args = {};
			//カーソルの取得
			sdObj = sdhDB.getAuthInfo(where_args);
			//カーソルの取得
			startManagingCursor(sdObj);
			Log.d("DEBUG", "MainCalendar selectAuthInfo AuthInfoCursor Count : " + sdObj.getCount());
			for( int intCt=0; intCt<sdObj.getCount(); intCt++){
				//AuthInfoデータ格納
				//アクセストークン
				authInfoObj.setStrAccessToken(sdObj.getColAccessToken());
				//アクセストークン期限
				authInfoObj.setLgAccessTokenExpire(sdObj.getColAccessTokenExpire());
				//リフレッシュトークン
				authInfoObj.setStrRefreshToken(sdObj.getColRefreshToken());
				//最終更新日時
				authInfoObj.setStrLastUpdate(sdObj.getColLastUpdate());
				sdObj.moveToNext();
			}
			sdObj.close();
		} catch (SQLException e) {
			Log.e("ERROR", "MainCalendar selectAuthInfo DB Error",e);
			if(sdObj != null) {
				sdObj.close();
			}
			authInfoObj = null;
		} finally {
			if(sdObj != null) {
				sdObj.close();
			}
		}
		Log.d("DEBUG", "MainCalendar selectAuthInfo End");
		return authInfoObj;
	}

	/**
	 * 更新対象スケジュール情報の件数確認処理
	 *
	 * @return 処理を行った場合はtrue
	 */
	private boolean selectChangeScheduleCount() {
		Log.d("DEBUG", "MainCalendar selectChangeScheduleCount Start");
		boolean blResult = true;
		ScheduleCursor scObj = null;
		try {
			String[] where_args = {};
			//カーソルの取得
			scObj = sdhDB.getChangeScheduleCount(where_args);
			//カーソルポインター初期化
			startManagingCursor(scObj);
			for( int intCt=0; intCt<scObj.getCount(); intCt++){
				//更新対象スケジュール件数
				intUnreflected = (int)scObj.getColScheduleCount();
				scObj.moveToNext();
			}
			scObj.close();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "MainCalendar selectChangeScheduleCount DB Error",e);
		} finally {
			if(scObj != null) {
				scObj.close();
			}
		}
		Log.d("DEBUG", "MainCalendar selectChangeScheduleCount End");
		return blResult;
	}

	/**
	 * Aboutメニューボタンでダイアログ表示
	 *
	 * @param context コンテキスト
	 * @param title タイトル
	 * @param text テキスト
	 */
	//Aboutダイアログの表示
	private static void showDialog(Context context, String title, String text) {
		Log.d("DEBUG", "MainCalendar showDialog Start");
		AlertDialog.Builder ad = new AlertDialog.Builder(context);
		ad.setTitle(title);
		ad.setMessage(text);
		ad.show();
		Log.d("DEBUG", "MainCalendar showDialog End");
	}

	/**
	 * 戻るボタンで終了確認ダイアログ表示
	 *
	 * @param kEvent キーイベント情報 
	 * @return 処理を行った場合はtrue
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent kEvent) {
		Log.d("DEBUG", "MainCalendar dispatchKeyEvent Start");
		//キー押下されたことを確認
		if (kEvent.getAction() == KeyEvent.ACTION_DOWN) {
			//戻るボタンが押されたか確認
			if (kEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				//終了確認yes/noダイアログの表示
				showYesNoDialog(this, R.string.mes1_dialog, R.string.mes2_dialog, new DialogInterface.OnClickListener() {
					//クリック時に呼ばれる
					public void onClick(DialogInterface dialog,
						int whith) {
						if (whith == DialogInterface.BUTTON_POSITIVE) {
							//アプリケーション終了
							endActivity();
						} else if (whith == DialogInterface.BUTTON_NEGATIVE) {
							//戻る
							return;
						}
					}
				});
			}
		}
		Log.d("DEBUG", "MainCalendar dispatchKeyEvent End");
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
		Log.d("DEBUG", "MainCalendar showYesNoDialog Start");
		AlertDialog.Builder ad = new AlertDialog.Builder(context);
		ad.setTitle(titleMsg);
		ad.setMessage(mainMsg);
		ad.setPositiveButton(R.string.yes_btn, listener);
		ad.setNegativeButton(R.string.no_btn, listener);
		ad.show();
		Log.d("DEBUG", "MainCalendar showYesNoDialog End");
	}

	/**
	 * Activity終了処理
	 *
	 */
	private void endActivity() {
		Log.d("DEBUG", "MainCalendar endActivity Start");
		//自アクティビティの終了
		finish();
		Log.d("DEBUG", "MainCalendar endActivity End");
	}

	/**
	 * Activity遷移処理
	 *
	 * @param clsNext 遷移先クラス
	 * @param strYMD 選択年月日
	 * @param strYM カレンダー年月
	 */
	public void nextActivity(Class clsNext, String strYMD, String strYM) {
		Log.d("DEBUG", "MainCalendar nextActivity Start");
		//アプリ内のアクティビティを呼び出すインテントの生成
		Intent intent = new Intent(this, clsNext);
		//インテントのパラメータ設定
		//カレンダー年月
		intent.putExtra("calym", strYM);
		//選択年月日
		intent.putExtra("calymd", strYMD);
		//選択元ユーザーインターフェースID
		intent.putExtra("uiid", getString(R.string.uiid1));
		//キーID
		intent.putExtra("keyid", 0l);
		//アクティビティの呼び出し
		startActivity(intent);
		Log.d("DEBUG", "MainCalendar nextActivity End");
		//自アクティビティの終了
		endActivity();
	}

	/**
	 * onActivityResult
	 *  呼び出したEditorの処理が完了したとき呼び出される
	 * @param requestCode 起動時に指定したrequestCode
	 * @param resultCode 呼び出したActivityが終了時に設定した終了コード
	 * @param data 呼び出したActivityが終了時に設定したIntent
	 */
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		Log.d("DEBUG", "MainCalendar onActivityResult Start");
		if(requestCode == BROWSER){
			Log.d("DEBUG", "MainCalendar onActivityResult requestCode : BROWSER");
			//ブラウザが終了したら、コードを入力するアクティビティを起動する。
			Intent intent = new Intent(MainCalendar.this,AuthCodeRegistration.class);
			startActivityForResult(intent,AUTH_CODE_REGISTRATION);
		}else if(requestCode == AUTH_CODE_REGISTRATION && resultCode==RESULT_OK){
			Log.d("DEBUG", "MainCalendar onActivityResult requestCode : AUTH_CODE_REGISTRATION");
			//AuthCodeが正常終了したらIntentからAuthCodeを取得
			strAuthCode = data.getStringExtra(AUTH_CODE);
			//AuthCodeが正しく取得できた場合は、再度syncCalendarを実行
			syncCalendar();
		}
		Log.d("DEBUG", "MainCalendar onActivityResult End");
	}

	/**
	 * 画面回転時に呼ばれるサイクル
	 *
	 * @param newConfig 新しい設定値
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d("DEBUG", "MainCalendar onConfigurationChanged Start");
		super.onConfigurationChanged(newConfig);
		Log.d("DEBUG", "MainCalendar onConfigurationChanged End");
	}

	/**
	 * バックグランド時に呼ばれるサイクル
	 *
	 * @param outState 保管設定値
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {  
		Log.d("DEBUG", "MainCalendar onSaveInstanceState Start");
		super.onSaveInstanceState(outState);
		outState.putString("calym", tvYearMonth.getText().toString());
		Log.d("DEBUG", "MainCalendar onSaveInstanceState End");
	} 

	/**
	 * onDestroy
	 * データベースヘルパークローズ処理
	 */
	@Override
	public void onDestroy(){
		Log.d("DEBUG", "MainCalendar onDestroy Start");
		super.onDestroy();
		if(sdhDB != null) {
			sdhDB.close();
		}
		Log.d("DEBUG", "MainCalendar onDestroy End");
	}
}