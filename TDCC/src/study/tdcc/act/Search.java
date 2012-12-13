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


 クラス名：Search
 内容：検索
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
          0.1/2012.05.28/T.Mashiko
          0.2/2012.06.06/T.Mashiko インテントLong型修正
          0.3/2012.06.19/T.Mashiko
          0.4/2012.06.20/T.Mashiko
          0.5/2012.07.09/T.Mashiko カレンダー年月指定ダイアログ改修,年月範囲指定
          0.6/2012.07.15/T.Mashiko スケジュール検索の入力チェックエラーにおける無限連打ロック不具合対応
          0.7/2012.07.19/T.Mashiko ロギング表記修正
          0.8/2012.08.22/T.Mashiko マイクと対象年月の連打ロック対策,ダイアログの戻るキー対策
          0.9/2012.08.23/T.Mashiko 対象年月の連打ロック対策追加
          1.0/2012.09.05/T.Mashiko AsyncTaskのエラー処理の条件修正,非同期処理内にてUI操作(トースト)を実行していた為、修正対応
          1.1/2012.09.27/T.Mashiko 強制終了対策
*/
package study.tdcc.act;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import study.tdcc.*;
import study.tdcc.adapter.SearchListItemAdapter;
import study.tdcc.bean.*;
import study.tdcc.lib.*;
import study.tdcc.lib.ScheduleDatabaseHelper.ScheduleCursor;
import study.tdcc.lib.ToDoDatabaseHelper.ToDoCursor;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class Search extends Activity {
	//カスタムタイトルテキストビュー
	private TextView tvCustomTitle;
	//カスタムタイトル(バージョン)テキストビュー
	private TextView tvCustomTitleVersion;
	//検索リラティブレイアウト
	private RelativeLayout rlSearch;
	//検索文字列テキストビュー
	private TextView tvSearchString;
	//検索文字列エディットテキストビュー
	private EditText etSearchString;
	//検索文字列音声入力イメージビュー
	private ImageView ivMicIcon1;
	//検索対象テキストビュー
	private TextView tvSearchTarget;
	//検索対象スピナー
	private Spinner sSearchTarget;
	//対象年月テキストビュー
	private TextView tvTargetDate;
	//スケジュール検索ボタン
	private Button btSCSearch;
	//ToDo検索用リニアレイアウトビュー
	private LinearLayout llToDO;
	//検索結果リストビュー
	private ListView lvResultList;
	//リクエストコード
	private static final int REQUEST_CODE1 = 71;
	//スケジュールデータベースオブジェクト
	private ScheduleDatabaseHelper sdhDB;
	//ToDoデータベースオブジェクト
	private ToDoDatabaseHelper tdhDB;
	//リストビュー用アダプタ
	private SearchListItemAdapter sliaObj;
	//検索結果リスト
	private List<SearchListRow> lSLR;
	//画面遷移データオブジェクト
	private ScreenTransitionData stdObj = new ScreenTransitionData();
	//リスナー初期値設定完了フラグ
	private boolean blRDY = false;
	//検索ボタン連打防止フラグ
	private boolean blSearch = true;
	//ダイアログ内ボタン連打防止フラグ
	private boolean blDialogButton = true;

	/**
	 * onCreate
	 * @param savedInstanceState バンドル
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("DEBUG", "Search onCreate Start");
		//リスナー初期値設定完了フラグオフ
		blRDY = false;
		super.onCreate(savedInstanceState);
		//window がフォーカスを受けたときに常に soft input area を隠す
		this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		//カスタムタイトル
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		//スケジュール登録画面描画処理
		setContentView(R.layout.search);
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
		//検索ボタン連打ロック解除
		blSearch = true;
		Log.d("DEBUG", "Search onCreate End");
	}

	/**
	 * 画面遷移情報の取得
	 *
	 */
	private void getScreenTransitionData() {
		Log.d("DEBUG", "Search getScreenTransitionData Start");
		//画面遷移時のインテント格納値取得
		Bundle bExtras = getIntent().getExtras();
		if (bExtras != null) {
			stdObj.setStrCalendarYearMonth(bExtras.getString("calym"));
			stdObj.setStrCalendarYearMonthDay(bExtras.getString("calymd"));
			stdObj.setStrUserInterfaceId(bExtras.getString("uiid"));
			stdObj.setLgKeyId(bExtras.getLong("keyid"));
		}
		Log.d("DEBUG", "Search getScreenTransitionData End");
	}

	/**
	 * 画面要素の取得
	 *
	 */
	private void getViewElement() {
		Log.d("DEBUG", "Search getViewElement Start");
		//カスタムタイトルテキストビュー
		tvCustomTitle = (TextView)this.findViewById(R.id.titletext);
		//カスタムタイトルバージョンビュー
		tvCustomTitleVersion = (TextView)this.findViewById(R.id.titleversion);
		//検索リラティブレイアウト
		rlSearch = (RelativeLayout)this.findViewById(R.id.search);
		//検索文字列テキストビュー
		tvSearchString = (TextView)this.findViewById(R.id.tvsearchstring);
		//検索文字列エディットテキストビュー
		etSearchString = (EditText)this.findViewById(R.id.etsearchstring);
		//キーワード音声入力イメージビュー
		ivMicIcon1 = (ImageView)this.findViewById(R.id.ivmicicon1);
		//検索対象テキストビュー
		tvSearchTarget = (TextView)this.findViewById(R.id.tvsearchtarget);
		//検索対象スピナー
		sSearchTarget = (Spinner)this.findViewById(R.id.ssearchtarget);
		//対象年月テキストビュー
		tvTargetDate = (TextView)this.findViewById(R.id.tvtargetdate);
		//スケジュール検索ボタン
		btSCSearch = (Button)this.findViewById(R.id.btscsearch);
		//ToDo検索用リニアレイアウト
		llToDO = (LinearLayout)this.findViewById(R.id.ll4);
		//検索結果リストビュー
		lvResultList = (ListView)this.findViewById(R.id.resultlist);
		Log.d("DEBUG", "Search getViewElement End");
	}

	/**
	 * カスタムタイトルの内容セット
	 *
	 */
	private void setCustomTitle() {
		Log.d("DEBUG", "Search setCustomTitle Start");
		tvCustomTitle.setText(getString(R.string.act_name8));
		StringBuilder sbVersion = new StringBuilder();
		sbVersion.append(getString(R.string.title_version));
		PackageManager pmObj = this.getPackageManager();
		try {
			PackageInfo piObj = pmObj.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
			sbVersion.append(piObj.versionName);
		} catch (NameNotFoundException e) {
			Log.e("ERROR", "Search setCustomTitle NameNotFoundException", e);
		}
		tvCustomTitleVersion.setText(sbVersion.toString());
		Log.d("DEBUG", "Search setCustomTitle End");
	}

	/**
	 * 画面要素へのデータセット
	 *
	 * @param savedInstanceState バンドル
	 */
	private void setViewElement(Bundle savedInstanceState) {
		Log.d("DEBUG", "Search setViewElement Start");
		//指定年月日を取得
		Calendar clTargetDate;
		if(savedInstanceState != null) {
			//復元情報から取得
			clTargetDate = DateUtil.toCalendar(savedInstanceState.getString("calym") + DateUtil.FIRST_DAY);
		} else {
			clTargetDate = DateUtil.toCalendar(stdObj.getStrCalendarYearMonth() + DateUtil.FIRST_DAY);
		}
		//対象年月日のTextViewの初期値設定
		tvTargetDate.setText(DateUtil.YEARMONTH_FORMAT.format(clTargetDate.getTime()));
		//マイク機能が使用可能か確認し、使用不可能な場合、非表示とする
		boolean blHasMicFeature = getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
		if(blHasMicFeature) {
			//マイク機能が使用可能な場合
			ivMicIcon1.setVisibility(View.VISIBLE);
			Log.d("DEBUG", "Search setViewElement FEATURE_MICROPHONE:OK");
		} else {
			//マイク機能が使用不可能な場合
			ivMicIcon1.setVisibility(View.INVISIBLE);
			Log.d("DEBUG", "Search setViewElement FEATURE_MICROPHONE:OUT");
		}
		Log.d("DEBUG", "Search setViewElement End");
	}

	/**
	 * 画面要素へのリスナーセット
	 *
	 */
	private void setViewListener() {
		Log.d("DEBUG", "Search setViewListener Start");
		//マイクアイコンのリスナー
		ivMicIcon1.setOnClickListener(iconOnClickListener);
		//検索対象スピナーのリスナー
		sSearchTarget.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				//リスナー初期値設定完了フラグチェック
				if(blRDY == true) {
					//初回表示内容が初期化されないようフラグでチェック
					Log.d("DEBUG", "SubcategoryEdit setViewListener CategorySpinner Listener GO");
					//検索画面ビュー構成変更
					String strTarget = (String) sSearchTarget.getSelectedItem();
					if(strTarget.equals(getString(R.string.scsearchtarget))) {
						//スケジュールの場合
						rlSearch.setBackgroundColor(Color.rgb(188, 226, 232));
						tvSearchString.setTextColor(Color.rgb(22, 74, 132));
						tvSearchTarget.setTextColor(Color.rgb(22, 74, 132));
						ivMicIcon1.setImageResource(R.drawable.mic_blue);
						btSCSearch.setVisibility(0);
						llToDO.setVisibility(8);
					} else {
						//ToDoの場合
						rlSearch.setBackgroundColor(Color.rgb(244, 179, 194));
						tvSearchString.setTextColor(Color.rgb(0, 123, 187));
						tvSearchTarget.setTextColor(Color.rgb(0, 123, 187));
						ivMicIcon1.setImageResource(R.drawable.mic_pink);
						btSCSearch.setVisibility(8);
						llToDO.setVisibility(0);
					}
				} else {
					//リスナー初期値設定完了フラグで初回スキップ処理
					Log.d("DEBUG", "SubcategoryEdit setViewListener CategorySpinner RDY OK");
					blRDY = true;
				}
			}
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		//年月日テキストビューのリスナー
		tvTargetDate.setOnClickListener(new DateOnClickListener(this));
		//検索結果リストビューのリスナー
		lvResultList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				//検索結果詳細画面への遷移処理
				dispSearchResultDetail(position);
			}
		});
		Log.d("DEBUG", "Search setViewListener End");
	}

	/**
	 * DateOnClickListener
	 *  日付の文字列にセットされるリスナー
	 */
	private class DateOnClickListener implements OnClickListener, DialogInterface.OnCancelListener{
		private Context contextObj = null;
		public DateOnClickListener(Context context){
			Log.d("DEBUG", "Search DateOnClickListener DateOnClickListener Start");
			//Contextが必要なので、コンストラクタで渡して覚えておく
			contextObj = context;
			Log.d("DEBUG", "Search DateOnClickListener DateOnClickListener End");
		}

		/**
		 * クリックされた時呼び出される
		 * @param View クリックされたビュー
		 */
		public void onClick(View view) {
			Log.d("DEBUG", "Search DateOnClickListener onClick Start");
			GregorianCalendar gcObj = null;
			if(view == tvTargetDate && blSearch == true){
				//対象年月でクリックされた場合
				//ボタン連打ロックオン
				blSearch = false;
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
								blSearch = true;
							}
						}
					})
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface diObj, int intWhich) {
							if(blDialogButton == true) {
								//ダイアログ内ボタン連打ロックオン
								blDialogButton = false;
								//ボタン連打ロックオフ
								blSearch = true;
							}
						}
					})
					.setOnCancelListener(this)
					.create()
					.show();
			Log.d("DEBUG", "Search DateOnClickListener onClick End");
		}

		/**
		 * onCancel
		 * ダイアログ戻るボタン処理
		 *
		 * @param dialog dialog情報 
		 */
		@Override
		public void onCancel(DialogInterface dialog) {
			Log.d("DEBUG", "Search DateOnClickListener onCancel Start");
			//ダイアログ表示時に戻るボタンが押下された場合
			//ボタン連打ロックオフ
			blSearch = true;
			Log.d("DEBUG", "Search DateOnClickListener onCancel End");
		}
	}

	/**
	 * マイクアイコンクリック時のイベント
	 *
	 */
	private Button.OnClickListener iconOnClickListener = new Button.OnClickListener() {
		public void onClick(View view) {
			Log.d("DEBUG", "Search onClick Start");
			if(blSearch == true) {
				//ボタン連打ロックオン
				blSearch = false;
				try {
					Intent iObj = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
					iObj.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
					iObj.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.mes3_dialog));
					iObj.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
					startActivityForResult(iObj, REQUEST_CODE1);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(Search.this, getString(R.string.voice_entry_err), Toast.LENGTH_LONG).show();
					//ボタン連打ロックオフ
					blSearch = true;
				}
			}
			Log.d("DEBUG", "Search onClick End");
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
		Log.d("DEBUG", "Search onActivityResult Start");
		if(intRequestCode == REQUEST_CODE1 && intResultCode == RESULT_OK) {
			String strResult = "";
			ArrayList<String> alResults = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			for(int intCt = 0; intCt < alResults.size(); intCt++) {
				strResult += alResults.get(intCt);
			}
			etSearchString.setText(strResult);
		}
		//ボタン連打ロックオフ
		blSearch = true;
		super.onActivityResult(intRequestCode, intResultCode, intent);
		Log.d("DEBUG", "Search onActivityResult End");
	}

	/**
	 * 検索結果詳細画面への遷移処理
	 *
	 */
	public void dispSearchResultDetail(int position) {
		Log.d("DEBUG", "Search dispSearchResultDetail Start");
		//選択リスト行データ取得
		SearchListRow slrObj = new SearchListRow();
		slrObj = lSLR.get(position);
		//アプリ内のアクティビティを呼び出すインテントの生成
		Intent intent = new Intent(this, MainTab.class);
		//インテントのパラメータ設定
		//カレンダー年月
		intent.putExtra("calym", stdObj.getStrCalendarYearMonth());
		//選択年月日
		intent.putExtra("calymd", slrObj.getStrDate());
		//選択元ユーザーインターフェースID
		if(slrObj.getStrSearchType().equals(getString(R.string.scsearchtarget))) {
			intent.putExtra("uiid", getString(R.string.uiid9));
		} else {
			intent.putExtra("uiid", getString(R.string.uiid10));
		}
		//キーID
		intent.putExtra("keyid", stdObj.getLgKeyId());
		startActivity(intent);
		endActivity();
		Log.d("DEBUG", "Search dispSearchResultDetail End");
	}

	/**
	 * scSearch
	 *  スケジュール検索ボタン押下時の処理
	 * @param v 選択ビュー
	 */
	public void scSearch(View view) {
		Log.d("DEBUG", "Search scSearch Start");
		if(blSearch == true) {
			//検索ボタン連打ロックオン
			blSearch = false;
			//入力値チェック
			boolean blCheckInput = checkInputSearch(false);
			if(blCheckInput == true) {
				//検索文字列チェック
				String strSearchWord = etSearchString.getText().toString();
				//選択対象年月情報取得
				String strTargetDate = "";
				//検索処理
				SetSearchResultListTask task = new SetSearchResultListTask();
				task.execute(strTargetDate, strSearchWord);
			} else {
				//検索ボタン連打ロックオフ
				blSearch = true;
			}
		}
		Log.d("DEBUG", "Search scSearch End");
	}

	/**
	 * tdSearch
	 *  ToDo検索ボタン押下時の処理
	 * @param v 選択ビュー
	 */
	public void tdSearch(View view) {
		Log.d("DEBUG", "Search tdSearch Start");
		if(blSearch == true) {
			//検索ボタン連打ロックオン
			blSearch = false;
			//入力値チェック
			boolean blCheckInput = checkInputSearch(true);
			if(blCheckInput == true) {
				//検索文字列取得
				String strSearchWord = etSearchString.getText().toString();
				//選択対象年月情報取得
				GregorianCalendar gcObj = DateUtil.toCalendar(tvTargetDate.getText().toString() + DateUtil.FIRST_DAY);
				StringBuilder sbDateText = new StringBuilder();
				sbDateText.append(Integer.toString(gcObj.get(Calendar.YEAR)));
				sbDateText.append(Integer.toString(gcObj.get(Calendar.MONTH)+1));
				//検索処理
				SetSearchResultListTask task = new SetSearchResultListTask();
				task.execute(sbDateText.toString(), strSearchWord);
			} else {
				//検索ボタン連打ロックオフ
				blSearch = true;
			}
		}
		Log.d("DEBUG", "Search tdSearch End");
	}

	/**
	 * 入力値チェック処理
	 *
	 * @return 処理を行った場合はtrue
	 */
	private boolean checkInputSearch(boolean blSearchType) {
		Log.d("DEBUG", "Search checkInputSchedule Start");
		boolean blResult = true;
		StringBuilder sbObj = new StringBuilder();
		//検索対象文字列エディットテキスト
		String strSearchWord = etSearchString.getText().toString();
		if(strSearchWord == null || strSearchWord.equals("")) {
			//空（null）チェック
			blResult = false;
			sbObj.append(getString(R.string.sestring_msg) + getString(R.string.restoreConfirm1));
		}
		//ToDo検索時
		if(blSearchType) {
			//開始年月日ビュー
			String strTargetDate = tvTargetDate.getText().toString();
			if(strTargetDate == null || strTargetDate.equals("")) {
				//空（null）チェック
				blResult = false;
				sbObj.append(getString(R.string.setargetperiod_msg) + getString(R.string.restoreConfirm1));
			} else {
				//YYYY-MM形式チェック
				if(DateUtil.checkYMFormat(strTargetDate) == false) {
					blResult = false;
					sbObj.append(getString(R.string.setargetperiod_errmsg1) + getString(R.string.restoreConfirm1));
				}
			}
		}
		if(blResult == false) {
			showDialog(this, "", sbObj.toString(), getString(R.string.yes_btn));
		}
		Log.d("DEBUG", "Search checkInputSchedule End");
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
		Log.d("DEBUG", "Search showDialog Start");
		AlertDialog.Builder ad = new AlertDialog.Builder(context);
		ad.setTitle(title);
		ad.setMessage(text);
		ad.setPositiveButton(btnmsg, null);
		ad.show();
		Log.d("DEBUG", "Search showDialog End");
	}

	/**
	 * afterSetSearchResultList
	 *  検索結果リスト作成処理後の処理
	 * @param strReturnCode 結果コード
	 * @param SLRList 検索結果リスト
	 */
	public void afterSetSearchResultList(String strReturnCode, List<SearchListRow> SLRList) {
		Log.d("DEBUG", "Search afterSetSearchResultList Start");
		//検索結果リスト作成処理の結果受け取り
		Log.d("DEBUG","Search afterSetSearchResultList strReturnCode : " + strReturnCode);
		if(!(strReturnCode == null || strReturnCode.equals("")) && strReturnCode.equals("Success")) {
			//エラー無しの場合
			lSLR = SLRList;
			sliaObj = new SearchListItemAdapter(this, 0, lSLR);
			//検索結果リストビューにアダプタをセット
			lvResultList.setAdapter(sliaObj);
		} else {
			//エラー有りの場合
			//検索画面にエラーダイアログ表示
			Toast.makeText(Search.this, getString(R.string.sqlite_read_err), Toast.LENGTH_LONG).show();
			endActivity();
		}
		//検索ボタン連打ロックオフ
		blSearch = true;
		Log.d("DEBUG", "Search afterSetSearchResultList End");
	}

	/**
	 * Activity遷移処理
	 *
	 */
	public void backActivity() {
		Log.d("DEBUG", "Search backActivity Start");
		//「カレンダー」画面に戻る
		//アプリ内のアクティビティを呼び出すインテントの生成
		Intent intent = new Intent(this, MainCalendar.class);
		//インテントのパラメータ設定
		//カレンダー年月
		intent.putExtra("calym", stdObj.getStrCalendarYearMonth());
		//選択年月日
		intent.putExtra("calymd", stdObj.getStrCalendarYearMonthDay());
		//選択元ユーザーインターフェースID
		intent.putExtra("uiid", getString(R.string.uiid9));
		//キーID
		intent.putExtra("keyid", stdObj.getLgKeyId());
		startActivity(intent);
		endActivity();
		Log.d("DEBUG", "Search backActivity Start");
	}

	/**
	 * Activity終了処理
	 *
	 */
	private void endActivity() {
		Log.d("DEBUG", "Search endActivity Start");
		//自アクティビティの終了
		finish();
		Log.d("DEBUG", "Search endActivity End");
	}

	/**
	 * 画面回転時に呼ばれるサイクル
	 *
	 * @param newConfig 新しい設定値
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d("DEBUG", "Search onConfigurationChanged Start");
		super.onConfigurationChanged(newConfig);
		Log.d("DEBUG", "Search onConfigurationChanged End");
	}

	/**
	 * バックグランド時に呼ばれるサイクル
	 *
	 * @param outState 保管設定値
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {  
		Log.d("DEBUG", "Search onSaveInstanceState Start");
		super.onSaveInstanceState(outState);
		outState.putString("calym", tvTargetDate.getText().toString());
		Log.d("DEBUG", "Search onSaveInstanceState End");
	} 

	/**
	 * onDestroy
	 * データベースヘルパークローズ処理
	 */
	@Override
	public void onDestroy(){
		Log.d("DEBUG", "Search onDestroy Start");
		super.onDestroy();
		if(sdhDB != null) {
			sdhDB.close();
		}
		if(tdhDB != null) {
			tdhDB.close();
		}
		Log.d("DEBUG", "Search onDestroy End");
	}

	/**
	 * 戻るボタンでカレンダー画面へ遷移
	 *
	 * @param kEvent キーイベント情報 
	 * @return 処理を行った場合はtrue
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent kEvent) {
		Log.d("DEBUG", "Search dispatchKeyEvent Start");
		//キー押下されたことを確認
		if (kEvent.getAction() == KeyEvent.ACTION_DOWN) {
			//戻るボタンが押されたか確認
			if (kEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				backActivity();
			}
		}
		Log.d("DEBUG", "Search dispatchKeyEvent End");
		return super.dispatchKeyEvent(kEvent);
	}

	//検索結果作成処理(内部クラス)
	private class SetSearchResultListTask extends AsyncTask<String, Integer, StringBuilder>{
		//スリープ設定情報読み込み
		private long lgSleepTime = Long.parseLong(getString(R.string.search_sleep));
		//ダイアログ
		private ProgressDialog pdObj;
		//検索結果リスト
		private List<SearchListRow> lSLR;

		//実行前準備処理(インジケータのセットアップ)
		@Override
		protected void onPreExecute() {
			Log.d("DEBUG","Search SetSearchResultListTask onPreExecute Start");
			//ダイアログの生成
			pdObj = new ProgressDialog(Search.this);
			//メッセージのセット
			pdObj.setMessage(getString(R.string.searchprocess));
			//ダイアログの戻るキー無効
			pdObj.setCancelable(false);
			//ダイアログ表示
			pdObj.show();
			Log.d("DEBUG","Search SetSearchResultListTask onPreExecute End");
		}

		//バックグラウンド実行処理(本体)
		@Override
		protected StringBuilder doInBackground(String... params) {
			Log.d("DEBUG","Search SetSearchResultListTask doInBackground Start");
			StringBuilder sbResult = new StringBuilder();
			try {
				//対象年月とカテゴリコードの取得
				String strDateText = params[0];
				String strSearchWord = params[1];
				Boolean blSearchResult = true;
				publishProgress(1);
				Thread.sleep(lgSleepTime);
				if(strDateText == null) {
					blSearchResult = false;
				} else if(strDateText.equals("")) {
					//ScheduleDB接続準備
					sdhDB = new ScheduleDatabaseHelper(Search.this);
					//スケジュールデータ取得処理
					blSearchResult = searchSCHEDULE(strSearchWord);
				} else {
					//ToDoDB接続準備
					tdhDB = new ToDoDatabaseHelper(Search.this, strDateText + getString(R.string.sqlite_todo_filename));
					//ToDoデータ取得処理
					blSearchResult = searchTODO(strSearchWord);
				}
				if(blSearchResult == false) {
					sbResult.append("Error");
				}
				publishProgress(2);
				Thread.sleep(lgSleepTime);
				sbResult.append("Success");
			} catch (Exception e) {
				Log.e("ERROR","Search SetSearchResultListTask",e);
				//予期せぬException(スレッドエラー）
				sbResult.append("Error");
			}
			Log.d("DEBUG","Search SetSearchResultListTask doInBackground End");
			return sbResult;
		}

		//バックグラウンド処理の進捗状況をUIスレッドで表示する為の処理
		@Override
		protected void onProgressUpdate(Integer... values) {
			Log.d("DEBUG","Search SetSearchResultListTask onProgressUpdate Start");
			//ダイアログのメッセージ変更
			pdObj.setMessage(getString(R.string.searchprocess) + values[0] + "/2");
			Log.d("DEBUG","Search SetSearchResultListTask onProgressUpdate End");
		}

		//バックグランド処理終了後のUIスレッド呼び出し処理
		@Override
		protected void onPostExecute(StringBuilder result) {
			Log.d("DEBUG","Search SetSearchResultListTask onPostExecute Start");
			//ダイアログ終了
			pdObj.dismiss();
			//集計リスト作成処理の結果コードとリストデータ返却
			afterSetSearchResultList(result.toString(), lSLR);
			Log.d("DEBUG","Search SetSearchResultListTask onPostExecute End");
		}

		/**
		 * スケジュール情報のデータベース検索処理
		 *
		 * @strSearchWord 検索ワード
		 * @return 処理を行った場合はtrue
		 */
		private boolean searchSCHEDULE(String strSearchWord) {
			Log.d("DEBUG", "Search SetSearchResultListTask searchSCHEDULE Start");
			boolean blResult = true;
			ScheduleCursor scObj = null;
			lSLR = new ArrayList<SearchListRow>();
			try {
				String[] where_args = {strSearchWord, strSearchWord, strSearchWord};
				//カーソルの取得
				scObj = sdhDB.getScheduleSearchResult(where_args);
				//カーソルポインター初期化
				startManagingCursor(scObj);
				Log.d("DEBUG", "Search SetSearchResultListTask searchSCHEDULE ScheduleCursor Count : " + scObj.getCount());
				for( int intCt=0; intCt<scObj.getCount(); intCt++){
					//スケジュールのセット
					SearchListRow slrObj = new SearchListRow();
					slrObj.setStrDate(scObj.getColTargetDate());
					slrObj.setStrSearchType(getString(R.string.scsearchtarget));
					lSLR.add(slrObj);
					scObj.moveToNext();
				}
				scObj.close();
			} catch (SQLException e) {
				blResult = false;
				Log.e("ERROR", "Search SetSearchResultListTask searchSCHEDULE DB Error",e);
			} finally {
				if(scObj != null) {
					scObj.close();
				}
			}
			Log.d("DEBUG", "Search SetSearchResultListTask searchSCHEDULE End");
			return blResult;
		}
		/**
		 * ToDo情報のデータベース検索処理
		 *
		 * @strSearchWord 検索ワード
		 * @return 処理を行った場合はtrue
		 */
		private boolean searchTODO(String strSearchWord) {
			Log.d("DEBUG", "Search SetSearchResultListTask searchTODO Start");
			boolean blResult = true;
			ToDoCursor tdcObj = null;
			lSLR = new ArrayList<SearchListRow>();
			try {
				String[] where_args = {strSearchWord, strSearchWord};
				//カーソルの取得
				tdcObj = tdhDB.getToDoSearchResult(where_args);
				//カーソルポインター初期化
				startManagingCursor(tdcObj);
				Log.d("DEBUG", "Search SetSearchResultListTask searchTODO ScheduleCursor Count : " + tdcObj.getCount());
				for( int intCt=0; intCt<tdcObj.getCount(); intCt++){
					//スケジュールのセット
					SearchListRow slrObj = new SearchListRow();
					slrObj.setStrDate(DateUtil.convBaseYMD(tdcObj.getColDate()));
					slrObj.setStrSearchType(getString(R.string.tdsearchtarget));
					lSLR.add(slrObj);
					tdcObj.moveToNext();
				}
				tdcObj.close();
			} catch (SQLException e) {
				blResult = false;
				Log.e("ERROR", "Search SetSearchResultListTask searchTODO DB Error",e);
			} finally {
				if(tdcObj != null) {
					tdcObj.close();
				}
			}
			Log.d("DEBUG", "Search SetSearchResultListTask searchTODO End");
			return blResult;
		}
	}
}