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


 クラス名：ToDoRegistration
 内容：ToDo登録・編集
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
          0.1/2012.05.27/T.Mashiko
          0.2/2012.05.28/T.Mashiko
          0.3/2012.06.04/T.Mashiko
          0.4/2012.06.05/T.Mashiko
          0.5/2012.06.06/T.Mashiko インテントLong型修正
          0.6/2012.06.07/T.Mashiko
          0.7/2012.06.10/T.Mashiko
          0.8/2012.06.11/T.Mashiko onDestroy修正
          0.9/2012.06.12/T.Mashiko メッセージ変更
          1.0/2012.06.13/T.Mashiko リファクタリング
          1.1/2012.06.15/T.Mashiko YYYY-MM形式修正
          1.2/2012.07.09/T.Mashiko メッセージ変更、タイトル・場所・内容文字数チェック追加
          1.3/2012.07.10/T.Mashiko 所要時間入力チェック追加
          1.4/2012.07.17/T.Mashiko エラー文言修正
          1.5/2012.07.19/T.Mashiko ロギング表記修正
          1.6/2012.08.24/T.Mashiko ボタンの連打ロック対策
          1.7/2012.08.26/T.Mashiko 新規登録時の登録件数上限対策
          1.8/2012.09.27/T.Mashiko 強制終了対策
*/
package study.tdcc.act;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import study.tdcc.*;
import study.tdcc.bean.*;
import study.tdcc.lib.*;
import study.tdcc.lib.ToDoDatabaseHelper.SubcategoryCursor;
import study.tdcc.lib.ToDoDatabaseHelper.ToDoCursor;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
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
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ToDoRegistration extends Activity {
	//カスタムタイトルテキストビュー
	private TextView tvCustomTitle;
	//カスタムタイトル(バージョン)テキストビュー
	private TextView tvCustomTitleVersion;
	//タイトルエディットテキストビュー
	private EditText etTitle;
	//タイトル音声入力イメージビュー
	private ImageView ivMicIcon1;
	//優先順位スピナー
	private Spinner sPriority;
	//所要時間エディットテキストビュー
	private EditText etTimeNeeded;
	//カテゴリースピナー
	private Spinner sCategory;
	//サブカテゴリースピナー
	private Spinner sSubcategory;
	//完了チェックボックス
	private CheckBox cbCompletion;
	//内容音声入力イメージビュー
	private ImageView ivMicIcon2;
	//内容エディットテキストビュー
	private EditText etContent;
	//リクエストコード
	private static final int REQUEST_CODE1 = 61;
	private static final int REQUEST_CODE2 = 62;
	//データベースオブジェクト
	private ToDoDatabaseHelper tdhDB;
	//画面遷移データオブジェクト
	private ScreenTransitionData stdObj = new ScreenTransitionData();
	//ターゲット年月日を保持する変数
	private GregorianCalendar gcTargetYearMonthDay;
	//優先順位アダプター
	private ArrayAdapter<String> aaPriority;
	//カテゴリアダプター
	private ArrayAdapter<String> aaCategory;
	//サブカテゴリアダプター
	private ArrayAdapter<String> aaSubcategory;
	//優先順位スピナーキー
	private HashMap<Integer, String> hmPrioritySpinner;
	//カテゴリスピナーキー
	private HashMap<Integer, String> hmCategorySpinner;
	//サブカテゴリスピナーキー
	private HashMap<Integer, String> hmSubcategorySpinner;
	//逆引き優先順位スピナーキー
	private HashMap<String, Integer> hmRevPrioritySpinner;
	//逆引きカテゴリスピナーキー
	private HashMap<String, Integer> hmRevCategorySpinner;
	//逆引き サブカテゴリスピナーキー
	private HashMap<String, Integer> hmRevSubcategorySpinner;
	//リスナー初期値設定完了フラグ
	private boolean blRDY = false;
	//復元情報
	private String strRestoreSubcategory = null;
	//ボタン連打防止フラグ
	private boolean blSave = true;
	//対象日のToDoリストの件数
	private int intToDoListCount;

	/**
	 * onCreate
	 * @param savedInstanceState バンドル
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("DEBUG", "ToDoRegistration onCreate Start");
		//リスナー初期値設定完了フラグオフ
		blRDY = false;
		super.onCreate(savedInstanceState);
		//window がフォーカスを受けたときに常に soft input area を隠す
		this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		//カスタムタイトル
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		//ToDo登録・編集画面描画処理
		setContentView(R.layout.todoregistration);
		//カスタムタイトル描画処理
		Window wObj = getWindow();
		wObj.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.customtitle);
		//画面遷移情報の取得
		getScreenTransitionData();
		//画面復元情報の取得
		getScreenRestoreData(savedInstanceState);
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
		Log.d("DEBUG", "ToDoRegistration onCreate End");
	}

	/**
	 * 画面遷移情報の取得
	 *
	 */
	private void getScreenTransitionData() {
		Log.d("DEBUG", "ToDoRegistration getScreenTransitionData Start");
		//画面遷移時のインテント格納値取得
		Bundle bExtras = getIntent().getExtras();
		if (bExtras != null) {
			stdObj.setStrCalendarYearMonth(bExtras.getString("calym"));
			stdObj.setStrCalendarYearMonthDay(bExtras.getString("calymd"));
			stdObj.setStrUserInterfaceId(bExtras.getString("uiid"));
			stdObj.setLgKeyId(bExtras.getLong("keyid"));
		}
		Log.d("DEBUG", "ToDoRegistration getScreenTransitionData End");
	}

	/**
	 * 画面復元情報の取得
	 * @param savedInstanceState バンドル
	 */
	private void getScreenRestoreData(Bundle savedInstanceState) {
		Log.d("DEBUG", "ToDoRegistration getScreenRestoreData Start");
		if(savedInstanceState != null) {
			//復元情報から取得
			//サブカテゴリスピナー
			strRestoreSubcategory = savedInstanceState.getString("subcategorycode");
		}
		Log.d("DEBUG", "ToDoRegistration getScreenRestoreData End");
	}

	/**
	 * 画面要素の取得
	 *
	 */
	private void getViewElement() {
		Log.d("DEBUG", "ToDoRegistration getViewElement Start");
		//カスタムタイトルテキストビュー
		tvCustomTitle = (TextView)this.findViewById(R.id.titletext);
		//カスタムタイトルバージョンビュー
		tvCustomTitleVersion = (TextView)this.findViewById(R.id.titleversion);
		//タイトルエディットテキストビュー
		etTitle = (EditText)this.findViewById(R.id.ettitle);
		//タイトル音声入力イメージビュー
		ivMicIcon1 = (ImageView)this.findViewById(R.id.ivmicicon1);
		//優先順位スピナー
		sPriority = (Spinner)this.findViewById(R.id.spriority);
		//所要時間エディットテキストビュー
		etTimeNeeded = (EditText)this.findViewById(R.id.ettimeneeded);
		//カテゴリスピナー
		sCategory = (Spinner)this.findViewById(R.id.scategory);
		//サブカテゴリスピナー
		sSubcategory = (Spinner)this.findViewById(R.id.ssubcategory);
		//完了チェックボックス
		cbCompletion = (CheckBox)this.findViewById(R.id.cbcompletion);
		//内容音声入力イメージビュー
		ivMicIcon2 = (ImageView)this.findViewById(R.id.ivmicicon2);
		//内容エディットテキストビュー
		etContent = (EditText)this.findViewById(R.id.etcontent);
		Log.d("DEBUG", "ToDoRegistration getViewElement End");
	}

	/**
	 * カスタムタイトルの内容セット
	 *
	 */
	private void setCustomTitle() {
		Log.d("DEBUG", "ToDoRegistration setCustomTitle Start");
		//対象年月日設定
		StringBuilder sbText = new StringBuilder();
		//カスタムタイトル情報取得
		gcTargetYearMonthDay = DateUtil.toCalendar(stdObj.getStrCalendarYearMonthDay());
		//年月の取得
		sbText.append(DateUtil.DATE_FORMAT.format(gcTargetYearMonthDay.getTime()));
		sbText.append(getString(R.string.maewaku));
		sbText.append(DateUtil.toDayOfWeek(this, gcTargetYearMonthDay));
		sbText.append(getString(R.string.ushirowaku));
		if(stdObj.getLgKeyId() == 0) {
			//遷移先画面がToDo登録画面の場合
			Log.d("DEBUG", "ToDoRegistration setViewElement UI:ToDo Registration");
			tvCustomTitle.setText(getString(R.string.act_name6) + "　" +sbText.toString());
		} else {
			//遷移先画面がToDo編集画面の場合
			Log.d("DEBUG", "ToDoRegistration setViewElement UI:ToDo Edit");
			tvCustomTitle.setText(getString(R.string.act_name7) + "　" +sbText.toString());
		}
		StringBuilder sbVersion = new StringBuilder();
		sbVersion.append(getString(R.string.title_version));
		PackageManager pmObj = this.getPackageManager();
		try {
			PackageInfo piObj = pmObj.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
			sbVersion.append(piObj.versionName);
		} catch (NameNotFoundException e) {
			Log.e("ERROR", "ToDoRegistration setCustomTitle NameNotFoundException", e);
		}
		tvCustomTitleVersion.setText(sbVersion.toString());
		Log.d("DEBUG", "ToDoRegistration setCustomTitle End");
	}

	/**
	 * 画面要素へのデータセット
	 *
	 */
	private void setViewElement() {
		Log.d("DEBUG", "ToDoRegistration setViewElement Start");
		//対象年月日データ名取得
		StringBuilder sbDateText = new StringBuilder();
		//カスタムタイトル情報取得
		gcTargetYearMonthDay = DateUtil.toCalendar(stdObj.getStrCalendarYearMonthDay());
		//年月の取得
		sbDateText.append(Integer.toString(gcTargetYearMonthDay.get(Calendar.YEAR)));
		sbDateText.append(Integer.toString(gcTargetYearMonthDay.get(Calendar.MONTH)+1));
		tdhDB = new ToDoDatabaseHelper(this,sbDateText.toString() + getString(R.string.sqlite_todo_filename));
		//優先順位情報のデータベース読み込み処理
		boolean blPriorityResult = selectPRIORITY();
		if(blPriorityResult == false) {
			Toast.makeText(ToDoRegistration.this, getString(R.string.sqlite_read_err), Toast.LENGTH_LONG).show();
			endActivity();
		}
		//カテゴリスピナー DB読み込み
		boolean blCategoryResult = selectCATEGORY();
		if(blCategoryResult == false) {
			Toast.makeText(ToDoRegistration.this, getString(R.string.sqlite_read_err), Toast.LENGTH_LONG).show();
			endActivity();
		}
		//指定ToDoデータ読み込み
		if(stdObj.getLgKeyId() != 0l) {
			//ToDo編集画面の場合
			Log.d("DEBUG", "ToDoRegistration setViewElement UI:ToDo Registration");
			//ToDo情報のデータベース読み込み処理
			boolean blToDoResult = selectTODO();
			if(blToDoResult == false) {
				Toast.makeText(ToDoRegistration.this, getString(R.string.sqlite_read_err), Toast.LENGTH_LONG).show();
				endActivity();
			}
		} else {
			//ToDo登録画面の場合
			Log.d("DEBUG", "ToDoRegistration setViewElement UI:ToDo Edit");
			//選択カテゴリ情報取得
			long lgCategoryPossition = sCategory.getSelectedItemId();
			//サブカテゴリスピナー DB読み込み
			boolean blSubcategoryResult = selectSUBCATEGORY(hmCategorySpinner.get((int)lgCategoryPossition));
			if(blSubcategoryResult == false) {
				Toast.makeText(ToDoRegistration.this, getString(R.string.sqlite_read_err), Toast.LENGTH_LONG).show();
				endActivity();
			}
		}
		//マイク機能が使用可能か確認し、使用不可能な場合、非表示とする
		boolean blHasMicFeature = getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
		if(blHasMicFeature) {
			//マイク機能が使用可能な場合
			ivMicIcon1.setVisibility(View.VISIBLE);
			ivMicIcon2.setVisibility(View.VISIBLE);
			Log.d("DEBUG", "ToDoRegistration setViewElement FEATURE_MICROPHONE:OK");
		} else {
			//マイク機能が使用不可能な場合
			ivMicIcon1.setVisibility(View.INVISIBLE);
			ivMicIcon2.setVisibility(View.INVISIBLE);
			Log.d("DEBUG", "ToDoRegistration setViewElement FEATURE_MICROPHONE:OUT");
		}
		Log.d("DEBUG", "ToDoRegistration setViewElement End");
	}

	/**
	 * 優先順位情報のデータベース読み込み処理
	 *
	 * @return 処理を行った場合はtrue
	 */
	private boolean selectPRIORITY() {
		Log.d("DEBUG", "ToDoRegistration selectPRIORITY Start");
		boolean blResult = true;
		ToDoDatabaseHelper.PriorityCursor pcObj = null;
		ArrayList<String> alTemp = new ArrayList<String>();
		hmPrioritySpinner = new HashMap<Integer,String>();
		hmRevPrioritySpinner = new HashMap<String,Integer>();
		try {
			//データベース検索処理
			String[] where_args = {};
			//カーソルの取得
			pcObj = tdhDB.getPriority(where_args);
			//カーソルの取得
			startManagingCursor(pcObj);
			Log.d("DEBUG", "ToDoRegistration selectPRIORITY PriorityCursor Count : " + pcObj.getCount());
			for( int intCt=0; intCt<pcObj.getCount(); intCt++){
				//カテゴリのセット
				StringBuilder sbTempLine = new StringBuilder();
				sbTempLine.append(pcObj.getColName());
				sbTempLine.append(" ");
				sbTempLine.append(getString(R.string.maewaku));
				sbTempLine.append(pcObj.getColCode());
				sbTempLine.append(getString(R.string.ushirowaku));
				alTemp.add(sbTempLine.toString());
				hmPrioritySpinner.put(intCt, pcObj.getColCode());
				hmRevPrioritySpinner.put(pcObj.getColCode(), intCt);
				pcObj.moveToNext();
			}
			pcObj.close();
			//Adapterの作成
			aaPriority = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, alTemp);
			//ドロップダウンのレイアウトを指定
			aaPriority.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			sPriority.setAdapter(aaPriority);
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ToDoRegistration selectPRIORITY DB Error",e);
		} finally {
			if(pcObj != null) {
				pcObj.close();
			}
		}
		Log.d("DEBUG", "ToDoRegistration selectPRIORITY End");
		return blResult;
	}

	/**
	 * カテゴリ情報のデータベース読み込み処理
	 *
	 * @return 処理を行った場合はtrue
	 */
	private boolean selectCATEGORY() {
		Log.d("DEBUG", "ToDoRegistration selectCATEGORY Start");
		boolean blResult = true;
		ToDoDatabaseHelper.CategoryCursor ccObj = null;
		ArrayList<String> alTemp = new ArrayList<String>();
		hmCategorySpinner = new HashMap<Integer,String>();
		hmRevCategorySpinner = new HashMap<String,Integer>();
		try {
			// データベース検索処理
			String[] where_args = {};
			//カーソルの取得
			ccObj = tdhDB.getCategory(where_args);
			Log.d("DEBUG", "ToDoRegistration selectCATEGORY CategoryCursor Count : " + ccObj.getCount());
			startManagingCursor(ccObj);
			for( int intCt=0; intCt<ccObj.getCount(); intCt++){
				//カテゴリのセット
				StringBuilder sbTempLine = new StringBuilder();
				sbTempLine.append(ccObj.getColName());
				sbTempLine.append(" ");
				sbTempLine.append(getString(R.string.maewaku));
				sbTempLine.append(ccObj.getColCode());
				sbTempLine.append(getString(R.string.ushirowaku));
				alTemp.add(sbTempLine.toString());
				hmCategorySpinner.put(intCt, ccObj.getColCode());
				hmRevCategorySpinner.put(ccObj.getColCode(), intCt);
				ccObj.moveToNext();
			}
			ccObj.close();
			//Adapterの作成
			aaCategory = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, alTemp);
			//ドロップダウンのレイアウトを指定
			aaCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			sCategory.setAdapter(aaCategory);
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ToDoRegistration selectCATEGORY DB Error",e);
		} finally {
			if(ccObj != null) {
				ccObj.close();
			}
		}
		Log.d("DEBUG", "ToDoRegistration selectCATEGORY End");
		return blResult;
	}

	/**
	 * サブカテゴリ情報のデータベース読み込み処理
	 *
	 * @strCategoryKey カテゴリ略字記号キー
	 * @return 処理を行った場合はtrue
	 */
	private boolean selectSUBCATEGORY(String strCategoryKey) {
		Log.d("DEBUG", "ToDoRegistration selectSUBCATEGORY Start");
		boolean blResult = true;
		SubcategoryCursor scObj = null;
		ArrayList<String> alTemp = new ArrayList<String>();
		hmSubcategorySpinner = new HashMap<Integer,String>();
		hmRevSubcategorySpinner = new HashMap<String,Integer>();
		try {
			String[] where_args = {strCategoryKey};
			//カーソルの取得
			scObj = tdhDB.getSubcategory(where_args);
			Log.d("DEBUG", "ToDoRegistration selectSUBCATEGORY SubcategoryCursor Count : " + scObj.getCount());
			//カーソルポインター初期化
			startManagingCursor(scObj);
			for( int intCt=0; intCt<scObj.getCount(); intCt++){
				//サブカテゴリのセット
				StringBuilder sbTempLine = new StringBuilder();
				sbTempLine.append(scObj.getColName());
				sbTempLine.append(" ");
				sbTempLine.append(getString(R.string.maewaku));
				sbTempLine.append(scObj.getColCcode());
				sbTempLine.append(getString(R.string.ushirowaku));
				alTemp.add(sbTempLine.toString());
				hmSubcategorySpinner.put(intCt, scObj.getColCcode());
				hmRevSubcategorySpinner.put(scObj.getColCcode(), intCt);
				scObj.moveToNext();
			}
			scObj.close();		
			//Adapterの作成
			aaSubcategory = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, alTemp);
			//ドロップダウンのレイアウトを指定
			aaSubcategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			sSubcategory.setAdapter(aaSubcategory);
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ToDoRegistration selectSUBCATEGORY DB Error",e);
		} finally {
			if(scObj != null) {
				scObj.close();
			}
		}
		Log.d("DEBUG", "ToDoRegistration selectSUBCATEGORY End");
		return blResult;
	}

	/**
	 * ToDo情報のデータベース読み込み処理
	 *
	 * @return 処理を行った場合はtrue
	 */
	private boolean selectTODO() {
		Log.d("DEBUG", "ToDoRegistration selectTODO Start");
		boolean blResult = true;
		ToDoCursor tdcObj = null;
		ToDo todoObj = new ToDo();
		try {
			//データベース検索処理
			String[] where_args = {String.valueOf(stdObj.getLgKeyId())};
			//カーソルの取得
			tdcObj = tdhDB.getToDo(where_args);
			//カーソルの取得
			startManagingCursor(tdcObj);
			Log.d("DEBUG", "ToDoRegistration selectTODO ToDoCursor Count : " + tdcObj.getCount());
			for( int intCt=0; intCt<tdcObj.getCount(); intCt++){
				//ToDoデータ格納
				//TDID
				todoObj.setLgTdId(tdcObj.getColTdid());
				//DATE
				todoObj.setLgDate(tdcObj.getColDate());
				//TITLE
				todoObj.setStrTitle(tdcObj.getColTitle());
				//PRIORITY_CODE
				todoObj.setStrPriorityCode(tdcObj.getColPriorityCode());
				//TAT
				todoObj.setStrTAT(tdcObj.getColTAT());
				//CATGORY_CODE
				todoObj.setStrCategoryCode(tdcObj.getColCategoryCode());
				//SUBCATEGORY_CODE
				todoObj.setStrSubcategoryCode(tdcObj.getColSubcategoryCode());
				//STATUS
				todoObj.setLgStatus(tdcObj.getColStatus());
				//DETAIL
				todoObj.setStrDetail(tdcObj.getColDetail());
				tdcObj.moveToNext();
			}
			tdcObj.close();
			//取得ToDoデータのビュー格納処理
			setViewData(todoObj);
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ToDoRegistration selectTODO DB Error",e);
		} finally {
			if(tdcObj != null) {
				tdcObj.close();
			}
		}
		Log.d("DEBUG", "ToDoRegistration selectTODO End");
		return blResult;
	}

	/**
	 * 画面要素への登録済みToDoデータセット
	 *
	 */
	private void setViewData(ToDo todoObj) {
		Log.d("DEBUG", "ToDoRegistration setViewData Start");
		//タイトル
		etTitle.setText(todoObj.getStrTitle());
		//優先順位
		sPriority.setSelection(hmRevPrioritySpinner.get(todoObj.getStrPriorityCode()));
		//所要時間
		etTimeNeeded.setText(todoObj.getStrTAT());
		//カテゴリ
		sCategory.setSelection(hmRevCategorySpinner.get(todoObj.getStrCategoryCode()));
		//サブカテゴリ(スピナー再構築後に選択)
		//サブカテゴリスピナー DB読み込み
		boolean blSubcategoryResult = selectSUBCATEGORY(todoObj.getStrCategoryCode());
		if(blSubcategoryResult == false) {
			Toast.makeText(ToDoRegistration.this, getString(R.string.sqlite_read_err), Toast.LENGTH_LONG).show();
			endActivity();
		}
		Log.d("DEBUG", "ToDoRegistration setViewData SubcategoryCode : " + todoObj.getStrSubcategoryCode());
		sSubcategory.setSelection(hmRevSubcategorySpinner.get(todoObj.getStrSubcategoryCode()));
		//完了
		if(todoObj.getLgStatus() == 1l) {
			cbCompletion.setChecked(true);
		}
		//詳細
		etContent.setText(todoObj.getStrDetail());
		Log.d("DEBUG", "ToDoRegistration setViewData End");
	}

	/**
	 * 画面要素へのリスナーセット
	 *
	 */
	private void setViewListener() {
		Log.d("DEBUG", "ToDoRegistration setViewListener Start");
		//マイクアイコンのリスナー
		ivMicIcon1.setOnClickListener(iconOnClickListener);
		ivMicIcon2.setOnClickListener(iconOnClickListener);
		//カテゴリスピナーのリスナー
		sCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				//リスナー初期値設定完了フラグチェック
				if(blRDY == true) {
					//初回表示内容が初期化されないようフラグでチェック
					Log.d("DEBUG", "ToDoRegistration setViewListener CategorySpinner Listener GO");
					//選択カテゴリ情報取得
					long lgCategoryPossition = sCategory.getSelectedItemId();
					//サブカテゴリスピナー DB読み込み
					boolean blSubcategoryResult = selectSUBCATEGORY(hmCategorySpinner.get((int)lgCategoryPossition));
					if(blSubcategoryResult == false) {
						Toast.makeText(ToDoRegistration.this, getString(R.string.sqlite_read_err), Toast.LENGTH_LONG).show();
						endActivity();
					}
					//復元データが有効な場合
					if(strRestoreSubcategory != null) {
						sSubcategory.setSelection(hmRevSubcategorySpinner.get(strRestoreSubcategory));
						//復元データ初期化
						strRestoreSubcategory = null;
					}
				} else {
					//リスナー初期値設定完了フラグで初回スキップ処理
					Log.d("DEBUG", "ToDoRegistration setViewListener CategorySpinner RDY OK");
					blRDY = true;
					//復元データが有効な場合
					if(strRestoreSubcategory != null) {
						sSubcategory.setSelection(hmRevSubcategorySpinner.get(strRestoreSubcategory));
					}
				}
			}
			public void onNothingSelected(AdapterView<?> parent) {
			
			}
		});
		Log.d("DEBUG", "ToDoRegistration setViewListener End");
	}

	/**
	 * マイクアイコンクリック時のイベント
	 *
	 */
	private Button.OnClickListener iconOnClickListener = new Button.OnClickListener() {
		public void onClick(View view) {
			Log.d("DEBUG", "ToDoRegistration onClick Start");
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
					} else {
						intRequestCode = REQUEST_CODE2;
					}
					Log.d("DEBUG", "ToDoRegistration onClick intRequestCode:" + intRequestCode);
					startActivityForResult(iObj, intRequestCode);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(ToDoRegistration.this, getString(R.string.voice_entry_err), Toast.LENGTH_LONG).show();
					//ボタン連打ロックオフ
					blSave = true;
				}
			}
			Log.d("DEBUG", "ToDoRegistration onClick End");
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
		Log.d("DEBUG", "ToDoRegistration onActivityResult Start");
		if((intRequestCode == REQUEST_CODE1 || intRequestCode == REQUEST_CODE2 ) && intResultCode == RESULT_OK) {
			String strResult = "";
			ArrayList<String> alResults = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			for(int intCt = 0; intCt < alResults.size(); intCt++) {
				strResult += alResults.get(intCt);
			}
			if(intRequestCode == REQUEST_CODE1) {
				etTitle.setText(strResult);
			} else {
				etContent.setText(strResult);
			}
		}
		//ボタン連打ロックオフ
		blSave = true;
		super.onActivityResult(intRequestCode, intResultCode, intent);
		Log.d("DEBUG", "ToDoRegistration onActivityResult End");
	}

	/**
	 * tdSave
	 *  新規追加ボタン押下時の処理
	 * @param view 選択ビュー
	 */
	public void tdSave(View view) {
		Log.d("DEBUG", "ToDoRegistration tdSave Start");
		if(blSave == true) {
			//ボタン連打ロックオン
			blSave = false;
			//入力チェック
			boolean blCheckInputTodoResult = checkInputToDo();
			if(blCheckInputTodoResult == true) {
				//ToDo 登録・更新処理
				boolean blUpdateToDoResult = savePreparationToDo();
				if(blUpdateToDoResult == false) {
					Toast.makeText(ToDoRegistration.this, getString(R.string.sqlite_write_err), Toast.LENGTH_LONG).show();
					endActivity();
				}
				nextActivity();
			} else {
				//ボタン連打ロックオフ
				blSave = true;
			}
		}
		Log.d("DEBUG", "ToDoRegistration tdSave End");
	}

	/**
	 * 入力値チェック処理
	 *
	 * @return 処理を行った場合はtrue
	 */
	private boolean checkInputToDo() {
		Log.d("DEBUG", "ToDoRegistration checkInputToDo Start");
		boolean blResult = true;
		StringBuilder sbObj = new StringBuilder();
		//タイトルエディットテキスト
		String strTitle = etTitle.getText().toString();
		if(strTitle == null || strTitle.equals("")) {
			blResult = false;
			sbObj.append(getString(R.string.tdtitle_msg) + getString(R.string.restoreConfirm1));
		} else {
			//文字数チェック
			if(InputCheckUtil.checkCount(strTitle, Integer.parseInt(getString(R.string.isc_limit1))) == false) {
				blResult = false;
				sbObj.append(getString(R.string.sctitle_msg) + getString(R.string.restoreConfirm1));
			}
		}
		//優先順位スピナー
		String strPriority = (String) sPriority.getSelectedItem();
		if(strPriority == null || strPriority.equals("")) {
			blResult = false;
			sbObj.append(getString(R.string.tdpriority_msg) + getString(R.string.restoreConfirm1));
		}
		//所要時間エディットテキスト
		String strTimeNeeded = etTimeNeeded.getText().toString();
		if(strTimeNeeded != null && (strTimeNeeded.equals("") == false)) {
			float ftCheck = 0.0f;
			//00.00の形式チェック
			if(InputCheckUtil.checkTimeFormat(strTimeNeeded) == false) {
				blResult = false;
				sbObj.append(getString(R.string.tdtimeneeded_msg1) + getString(R.string.restoreConfirm1));
			} else {
				try{
					ftCheck = Float.parseFloat(strTimeNeeded);
				} catch(NumberFormatException e) {
					blResult = false;
					sbObj.append(getString(R.string.tdtimeneeded_msg1) + getString(R.string.restoreConfirm1));
				}
				//24.0＞0.0範囲チェック
				if(ftCheck > 24.0f || ftCheck < 0.0f) {
					blResult = false;
					sbObj.append(getString(R.string.tdtimeneeded_msg2) + getString(R.string.restoreConfirm1));
				}
			}
		}
		//カテゴリスピナー
		String strCategory = (String) sCategory.getSelectedItem();
		if(strCategory == null || strCategory.equals("")) {
			blResult = false;
			sbObj.append(getString(R.string.tdcategory_msg) + getString(R.string.restoreConfirm1));
		}
		//サブカテゴリスピナー
		String strSubcategory = (String) sSubcategory.getSelectedItem();
		if(strSubcategory == null || strSubcategory.equals("")) {
			blResult = false;
			sbObj.append(getString(R.string.tdsubcategory_msg) + getString(R.string.restoreConfirm1));
		}
		//内容エディットテキスト
		String strContent = etContent.getText().toString();
		if(!(strContent == null || strContent.equals(""))) {
			//文字数チェック
			if(InputCheckUtil.checkCount(strContent, Integer.parseInt(getString(R.string.isc_limit2))) == false) {
				blResult = false;
				sbObj.append(getString(R.string.tdcontent_msg) + getString(R.string.restoreConfirm1));
			}
		}
		if(blResult == false) {
			showDialog(this, "", sbObj.toString(), getString(R.string.yes_btn));
		}
		Log.d("DEBUG", "ToDoRegistration checkInputToDo End");
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
		Log.d("DEBUG", "ToDoRegistration showDialog Start");
		AlertDialog.Builder ad = new AlertDialog.Builder(context);
		ad.setTitle(title);
		ad.setMessage(text);
		ad.setPositiveButton(btnmsg, null);
		ad.show();
		Log.d("DEBUG", "ToDoRegistration showDialog End");
	}

	/**
	 * ToDo情報のデータベース登録・更新処理
	 *
	 * @return 処理を行った場合はtrue
	 */
	private boolean savePreparationToDo() {
		Log.d("DEBUG", "ToDoRegistration savePreparationToDo Start");
		boolean blResult = true;
		//選択対象年月情報取得
		StringBuilder sbDateText = new StringBuilder();
		sbDateText.append(Integer.toString(gcTargetYearMonthDay.get(Calendar.YEAR)));
		sbDateText.append(Integer.toString(gcTargetYearMonthDay.get(Calendar.MONTH)+1));
		//ToDoDatabaseHelper初期化
		tdhDB = new ToDoDatabaseHelper(this,sbDateText.toString() + getString(R.string.sqlite_todo_filename));
		//SQLパラメータ作成(TDID,DATE,TITLE,PRIORITY_CODE,TAT,CATEGORY_CODE,SUBCATEGORY_CODE,STATUS,DETAIL)
		ToDo tdObj = new ToDo();
		tdObj.setLgDate(DateUtil.convToDoYMD(gcTargetYearMonthDay));
		tdObj.setStrTitle(etTitle.getText().toString());
		tdObj.setStrPriorityCode(hmPrioritySpinner.get((int)sPriority.getSelectedItemId()));
		if(etTimeNeeded.getText().toString() != null && (etTimeNeeded.getText().toString().equals("") == false)) {
			tdObj.setStrTAT(etTimeNeeded.getText().toString());
		} else {
			tdObj.setStrTAT("0");
		}
		tdObj.setStrCategoryCode(hmCategorySpinner.get((int)sCategory.getSelectedItemId()));
		tdObj.setStrSubcategoryCode(hmSubcategorySpinner.get((int)sSubcategory.getSelectedItemId()));
		if(cbCompletion.isChecked()) {
			tdObj.setLgStatus(1l);
		} else {
			tdObj.setLgStatus(0l);
		}
		tdObj.setStrDetail(etContent.getText().toString());

		if(stdObj.getLgKeyId() != 0) {
			//ToDo編集処理
			tdObj.setLgTdId(stdObj.getLgKeyId());
			blResult = tdhDB.updateToDo(tdObj);
		} else {
			//ToDo登録件数上限チェック処理
			intToDoListCount = 0;
			blResult = selectTODOCount(tdObj.getLgDate());
			if(blResult == true) {
				if(intToDoListCount < Integer.parseInt(getString(R.string.todo_limit_count))) {
					//ToDo登録処理
					blResult = tdhDB.insertToDo(tdObj);
				} else {
					//対象日のToDo件数が上限値の場合
					Toast.makeText(ToDoRegistration.this, getString(R.string.tdlimitcount_msg), Toast.LENGTH_LONG).show();
				}
			}
		}
		Log.d("DEBUG", "ToDoRegistration savePreparationToDo End");
		return blResult;
	}

	/**
	 * ToDo情報の件数取得処理
	 *
	 * @lgTargetDate 対象年月日
	 * @return 処理を行った場合はtrue
	 */
	private boolean selectTODOCount(long lgTargetDate) {
		Log.d("DEBUG", "ToDoRegistration selectTODOCount Start");
		boolean blResult = true;
		ToDoCursor tdcObj = null;
		try {
			String[] where_args = {String.valueOf(lgTargetDate)};
			//カーソルの取得
			tdcObj = tdhDB.getToDoListCount(where_args);
			//カーソルポインター初期化
			startManagingCursor(tdcObj);
			//対象日のToDoの件数取得
			intToDoListCount = tdcObj.getCount();
			Log.d("DEBUG", "ToDoRegistration selectTODOCount ToDoCursor Count : " + tdcObj.getCount());
			tdcObj.close();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ToDoRegistration selectTODOCount DB Error",e);
		} finally {
			if(tdcObj != null) {
				tdcObj.close();
			}
		}
		Log.d("DEBUG", "ToDoRegistration selectTODOCount End");
		return blResult;
	}

	/**
	 * tdCancel
	 *  キャンセルボタン押下時の処理
	 * @param view 選択ビュー
	 */
	public void tdCancel(View view) {
		Log.d("DEBUG", "ToDoRegistration tdCancel Start");
		if(blSave == true) {
			//ボタン連打ロックオン
			blSave = false;
			nextActivity();
		}
		Log.d("DEBUG", "ToDoRegistration tdCancel End");
	}

	/**
	 * Activity遷移処理
	 *
	 */
	public void nextActivity() {
		Log.d("DEBUG", "ToDoRegistration nextActivity Start");
		//「ToDoタブ」画面に戻る
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
			intent.putExtra("uiid", getString(R.string.uiid7));
		} else {
			intent.putExtra("uiid", getString(R.string.uiid8));
		}
		//キーID
		intent.putExtra("keyid", stdObj.getLgKeyId());
		startActivity(intent);
		endActivity();
		Log.d("DEBUG", "ToDoRegistration nextActivity End");
	}

	/**
	 * Activity終了処理
	 *
	 */
	private void endActivity() {
		Log.d("DEBUG", "ToDoRegistration endActivity Start");
		//自アクティビティの終了
		finish();
		Log.d("DEBUG", "ToDoRegistration endActivity End");
	}

	/**
	 * 画面回転時に呼ばれるサイクル
	 *
	 * @param newConfig 新しい設定値
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d("DEBUG", "ToDoRegistration onConfigurationChanged Start");
		super.onConfigurationChanged(newConfig);
		Log.d("DEBUG", "ToDoRegistration onConfigurationChanged End");
	}

	/**
	 * バックグランド時に呼ばれるサイクル
	 *
	 * @param outState 保管設定値
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d("DEBUG", "ToDoRegistration onSaveInstanceState Start");
		super.onSaveInstanceState(outState);
		//サブカテゴリスピナー
		outState.putString("subcategorycode", hmSubcategorySpinner.get((int)sSubcategory.getSelectedItemId()));
		Log.d("DEBUG", "ToDoRegistration onSaveInstanceState End");
	}

	/**
	 * onDestroy
	 * データベースヘルパークローズ処理
	 */
	@Override
	public void onDestroy(){
		Log.d("DEBUG", "ToDoRegistration onDestroy Start");
		super.onDestroy();
		if(tdhDB != null) {
			tdhDB.close();
		}
		Log.d("DEBUG", "ToDoRegistration onDestroy End");
	}

	/**
	 * 戻るボタンでカレンダー画面へ遷移
	 *
	 * @param kEvent キーイベント情報 
	 * @return 処理を行った場合はtrue
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent kEvent) {
		Log.d("DEBUG", "ToDoRegistration dispatchKeyEvent Start");
		//キー押下されたことを確認
		if (kEvent.getAction() == KeyEvent.ACTION_DOWN) {
			//戻るボタンが押されたか確認
			if (kEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				nextActivity();
			}
		}
		Log.d("DEBUG", "ToDoRegistration dispatchKeyEvent End");
		return super.dispatchKeyEvent(kEvent);
	}
}