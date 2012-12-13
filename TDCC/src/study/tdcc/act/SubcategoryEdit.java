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


 クラス名：SubcategoryEdit
 内容：サブカテゴリ編集
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
          0.1/2012.05.29/T.Mashiko
          0.2/2012.06.01/T.Mashiko
          0.3/2012.06.06/T.Mashiko インテントLong型修正
          0.4/2012.06.07/T.Mashiko
          0.5/2012.06.10/T.Mashiko
          0.6/2012.06.11/T.Mashiko onDestroy修正
          0.7/2012.06.22/T.Mashiko ダイアログのフォントサイズ修正
          0.8/2012.07.09/T.Mashiko カレンダー年月指定ダイアログ改修,年月範囲指定,文字数チェック追加
          0.9/2012.07.10/T.Mashiko サブカテゴリコピー機能追加
          1.0/2012.07.19/T.Mashiko ロギング表記修正
          1.1/2012.08.22/T.Mashiko 対象年月の連打ロック対策,ダイアログの戻るキー対策,スケジュールタブ画面からの遷移処理修正
          1.2/2012.08.23/T.Mashiko 対象年月の連打ロック対策追加
          1.3/2012.09.27/T.Mashiko 強制終了対策
*/
package study.tdcc.act;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import study.tdcc.*;
import study.tdcc.adapter.SubcategoryListItemAdapter;
import study.tdcc.bean.*;
import study.tdcc.lib.*;
import study.tdcc.lib.ToDoDatabaseHelper.SubcategoryCursor;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SubcategoryEdit extends Activity implements DialogInterface.OnCancelListener {
	//カスタムタイトルテキストビュー
	private TextView tvCustomTitle;
	//カスタムタイトル(バージョン)テキストビュー
	private TextView tvCustomTitleVersion;
	//対象年月テキストビュー
	private TextView tvTargetDate;
	//カテゴリスピナー
	private Spinner sCategory;
	//サブカテゴリリストビュー
	private ListView lvSubcategoryList;
	//データベースオブジェクト
	private ToDoDatabaseHelper tdhDB;
	//画面遷移データオブジェクト
	private ScreenTransitionData stdObj = new ScreenTransitionData();
	//カテゴリアダプター
	private ArrayAdapter<String> aaCategory;
	//カテゴリスピナーキー
	private HashMap<Integer, String> hmCategorySpinner;
	//リストビュー用アダプタ
	private SubcategoryListItemAdapter sliaObj;
	//サブカテゴリリスト
	private List<SubcategoryListRow> lSLR;
	//カスタムダイアログ内 サブカテゴリ名エディットテキストビュー
	private EditText etSName;
	//カスタムダイアログ内 サブカテゴリリスト指定位置情報
	private int intPossition;
	//リスナー初期値設定完了フラグ
	private boolean blRDY = false;
	//コピーボタン連打防止フラグ
	private boolean blCopy = true;
	//ダイアログ内ボタン連打防止フラグ
	private boolean blDialogButton = true;
	//コピー用前月サブカテゴリリスト
	private ArrayList<Subcategory> alCopyTarget;

	/**
	 * onCreate
	 * @param savedInstanceState バンドル
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("DEBUG", "SubcategoryEdit onCreate Start");
		//リスナー初期値設定完了フラグオフ
		blRDY = false;
		super.onCreate(savedInstanceState);
		//window がフォーカスを受けたときに常に soft input area を隠す
		this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		//カスタムタイトル
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		//サブカテゴリ編集画面描画処理
		setContentView(R.layout.subcategoryedit);
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
		//コピーボタン連打ロック解除
		blCopy = true;
		Log.d("DEBUG", "SubcategoryEdit onCreate End");
	}

	/**
	 * 画面遷移情報の取得
	 *
	 */
	private void getScreenTransitionData() {
		Log.d("DEBUG", "SubcategoryEdit getScreenTransitionData Start");
		//画面遷移時のインテント格納値取得
		Bundle bExtras = getIntent().getExtras();
		if (bExtras != null) {
			stdObj.setStrCalendarYearMonth(bExtras.getString("calym"));
			stdObj.setStrCalendarYearMonthDay(bExtras.getString("calymd"));
			stdObj.setStrUserInterfaceId(bExtras.getString("uiid"));
			stdObj.setLgKeyId(bExtras.getLong("keyid"));
		}
		Log.d("DEBUG", "SubcategoryEdit getScreenTransitionData End");
	}

	/**
	 * 画面要素の取得
	 *
	 */
	private void getViewElement() {
		Log.d("DEBUG", "SubcategoryEdit getViewElement Start");
		//カスタムタイトルテキストビュー
		tvCustomTitle = (TextView)this.findViewById(R.id.titletext);
		//カスタムタイトルバージョンビュー
		tvCustomTitleVersion = (TextView)this.findViewById(R.id.titleversion);
		//対象年月テキストビュー
		tvTargetDate = (TextView)this.findViewById(R.id.tvtargetyearmonth);
		//カテゴリスピナー
		sCategory = (Spinner)this.findViewById(R.id.scategory);
		//サブカテゴリリスト
		lvSubcategoryList = (ListView)this.findViewById(R.id.subcategorylist);
		Log.d("DEBUG", "SubcategoryEdit getViewElement End");
	}

	/**
	 * カスタムタイトルの内容セット
	 *
	 */
	private void setCustomTitle() {
		Log.d("DEBUG", "SubcategoryEdit setCustomTitle Start");
		tvCustomTitle.setText(getString(R.string.act_name11));
		StringBuilder sbVersion = new StringBuilder();
		sbVersion.append(getString(R.string.title_version));
		PackageManager pmObj = this.getPackageManager();
		try {
			PackageInfo piObj = pmObj.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
			sbVersion.append(piObj.versionName);
		} catch (NameNotFoundException e) {
			Log.e("ERROR", "SubcategoryEdit setCustomTitle NameNotFoundException", e);
		}
		tvCustomTitleVersion.setText(sbVersion.toString());
		Log.d("DEBUG", "SubcategoryEdit setCustomTitle End");
	}

	/**
	 * 画面要素へのデータセット
	 *
	 * @param savedInstanceState バンドル
	 */
	private void setViewElement(Bundle savedInstanceState) {
		Log.d("DEBUG", "SubcategoryEdit setViewElement Start");
		//指定年月を取得
		Calendar clTargetDate;
		if(savedInstanceState != null) {
			//復元情報から取得
			clTargetDate = DateUtil.toCalendar(savedInstanceState.getString("calym") + DateUtil.FIRST_DAY);
		} else {
			String strUserInterfaceId = stdObj.getStrUserInterfaceId();
			if(!(strUserInterfaceId == null || strUserInterfaceId.equals("")) && strUserInterfaceId.equals(getString(R.string.uiid2))) {
				clTargetDate = DateUtil.toCalendar(stdObj.getStrCalendarYearMonthDay());
			} else {
				clTargetDate = DateUtil.toCalendar(stdObj.getStrCalendarYearMonth() + DateUtil.FIRST_DAY);
			}
		}
		//対象年月のTextViewの初期値設定
		tvTargetDate.setText(DateUtil.YEARMONTH_FORMAT.format(clTargetDate.getTime()));
		StringBuilder sbDateText = new StringBuilder();
		sbDateText.append(Integer.toString(clTargetDate.get(Calendar.YEAR)));
		sbDateText.append(Integer.toString(clTargetDate.get(Calendar.MONTH)+1));
		//ToDoDatabaseHelper初期化
		tdhDB = new ToDoDatabaseHelper(this,sbDateText.toString() + getString(R.string.sqlite_todo_filename));
		//カテゴリスピナー データ読み込み
		boolean blCategoryResult = selectCATEGORY();
		if(blCategoryResult == false) {
			Toast.makeText(SubcategoryEdit.this, getString(R.string.sqlite_read_err), Toast.LENGTH_LONG).show();
			endActivity();
		}
		//サブカテゴリリスト用データ読込
		setSubcategoryList();
		Log.d("DEBUG", "SubcategoryEdit setViewElement End");
	}

	/**
	 * カテゴリ情報のデータベース読み込み処理
	 *
	 * @return 処理を行った場合はtrue
	 */
	private boolean selectCATEGORY() {
		Log.d("DEBUG", "SubcategoryEdit selectCATEGORY Start");
		boolean blResult = true;
		ToDoDatabaseHelper.CategoryCursor ccObj = null;
		ArrayList<String> alTemp = new ArrayList<String>();
		hmCategorySpinner = new HashMap<Integer,String>();
		try {
			String[] where_args = {};
			//カーソルの取得
			ccObj = tdhDB.getCategory(where_args);
			//カーソルポインター初期化
			startManagingCursor(ccObj);
			Log.d("DEBUG", "SubcategoryEdit selectCATEGORY CategoryCursor Count : " + ccObj.getCount());
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
			Log.e("ERROR", "SubcategoryEdit selectCATEGORY DB Error",e);
		} finally {
			if(ccObj != null) {
				ccObj.close();
			}
		}
		Log.d("DEBUG", "SubcategoryEdit selectCATEGORY End");
		return blResult;
	}

	/**
	 * サブカテゴリリスト読み込み処理
	 *
	 */
	private void setSubcategoryList() {
		Log.d("DEBUG", "SubcategoryEdit setSubcategoryList Start");
		//選択対象年月情報取得
		GregorianCalendar gcObj = DateUtil.toCalendar(tvTargetDate.getText().toString() + DateUtil.FIRST_DAY);
		StringBuilder sbDateText = new StringBuilder();
		sbDateText.append(Integer.toString(gcObj.get(Calendar.YEAR)));
		sbDateText.append(Integer.toString(gcObj.get(Calendar.MONTH)+1));
		//ToDoDatabaseHelper初期化
		tdhDB = new ToDoDatabaseHelper(this,sbDateText.toString() + getString(R.string.sqlite_todo_filename));
		//選択カテゴリ情報取得
		long lgCategoryPossition = sCategory.getSelectedItemId();
		boolean blSubCategoryResult = selectSUBCATEGORY(hmCategorySpinner.get((int)lgCategoryPossition));
		if(blSubCategoryResult == false) {
			Toast.makeText(SubcategoryEdit.this, getString(R.string.sqlite_read_err), Toast.LENGTH_LONG).show();
			endActivity();
		}
		//ListItemAdapterを生成
		sliaObj = new SubcategoryListItemAdapter(this, 0, lSLR);
		//サブカテゴリリストビューにアダプタをセット
		lvSubcategoryList.setAdapter(sliaObj);
		Log.d("DEBUG", "SubcategoryEdit setSubcategoryList End");
	}

	/**
	 * サブカテゴリ情報のデータベース読み込み処理
	 *
	 * @strCategoryKey カテゴリ略字記号キー
	 * @return 処理を行った場合はtrue
	 */
	private boolean selectSUBCATEGORY(String strCategoryKey) {
		Log.d("DEBUG", "SubcategoryEdit selectSUBCATEGORY Start");
		boolean blResult = true;
		SubcategoryCursor scObj = null;
		lSLR = new ArrayList<SubcategoryListRow>();
		try {
			String[] where_args = {strCategoryKey};
			//カーソルの取得
			scObj = tdhDB.getSubcategory(where_args);
			//カーソルポインター初期化
			startManagingCursor(scObj);
			Log.d("DEBUG", "SubcategoryEdit selectSUBCATEGORY CategoryCursor Count : " + scObj.getCount());
			for( int intCt=0; intCt<scObj.getCount(); intCt++){
				//サブカテゴリのセット
				SubcategoryListRow slrObj = new SubcategoryListRow();
				slrObj.setStrSubcategoryCode(scObj.getColCcode());
				slrObj.setStrSubcategoryName(scObj.getColName());
				lSLR.add(slrObj);
				scObj.moveToNext();
			}
			scObj.close();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "SubcategoryEdit selectSUBCATEGORY DB Error",e);
		} finally {
			scObj.close();
		}
		Log.d("DEBUG", "SubcategoryEdit selectSUBCATEGORY End");
		return blResult;
	}

	/**
	 * 画面要素へのリスナーセット
	 *
	 */
	private void setViewListener() {
		Log.d("DEBUG", "SubcategoryEdit setViewListener Start");
		//対象年月テキストビューのリスナー
		tvTargetDate.setOnClickListener(new DateOnClickListener(this));
		//カテゴリスピナーのリスナー
		sCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				//リスナー初期値設定完了フラグチェック
				if(blRDY == true) {
					//初回表示内容が初期化されないようフラグでチェック
					Log.d("DEBUG", "SubcategoryEdit setViewListener CategorySpinner Listener GO");
					//サブカテゴリリスト用データ読込
					setSubcategoryList();
				} else {
					//リスナー初期値設定完了フラグで初回スキップ処理
					Log.d("DEBUG", "SubcategoryEdit setViewListener CategorySpinner RDY OK");
					blRDY = true;
				}
			}
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		//サブカテゴリリストビューのリスナー
		lvSubcategoryList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				if(blCopy == true) {
					//ボタン連打ロックオン
					blCopy = false;
					//サブカテゴリ名編集ダイアログ表示処理
					dispSubcategoryCodeDialog(position);
				}
			}
		});
		Log.d("DEBUG", "SubcategoryEdit setViewListener End");
	}

	/**
	 * サブカテゴリ編集ダイアログ表示処理
	 *
	 */
	public void dispSubcategoryCodeDialog(int position) {
		Log.d("DEBUG", "SubcategoryEdit dispSubcategoryCodeDialog Start");
		intPossition = position;
		AlertDialog.Builder adObj = new AlertDialog.Builder(this);
		//ダイアログ内ボタン連打ロックオフ
		blDialogButton = true;
		//ダイアログタイトル
		SubcategoryListRow slrObj = new SubcategoryListRow();
		slrObj = lSLR.get(intPossition);
		adObj.setTitle(getString(R.string.sceDialogTitle) + slrObj.getStrSubcategoryCode());
		//外枠レイアウト作成
		LinearLayout llOut = new LinearLayout(this);
		llOut.setOrientation(LinearLayout.VERTICAL);
		//内枠1レイアウト作成
		LinearLayout llIn1 = new LinearLayout(this);
		llIn1.setOrientation(LinearLayout.HORIZONTAL);
		//内枠2レイアウト作成
		LinearLayout llIn2 = new LinearLayout(this);
		llIn2.setOrientation(LinearLayout.HORIZONTAL);
		//サブカテゴリ名：テキストビュー追加
		TextView tvSName = new TextView(this);
		tvSName.setText(getString(R.string.sceDialogTitle));
		tvSName.setTextSize(18.0f);
		llIn1.addView(tvSName);
		llOut.addView(llIn1);
		//サブカテゴリ名エディットテキスト追加
		etSName = new EditText(this);
		etSName.setHint(getString(R.string.sceDialogHint));
		llIn2.addView(etSName);
		llOut.addView(llIn2);
		adObj.setView(llOut);
		//アラートダイアログのタッチイベントを設定
		adObj.setPositiveButton(getString(R.string.exec_btn), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(blDialogButton == true) {
					//ダイアログ内ボタン連打ロックオン
					blDialogButton = false;
					//入力チェック
					boolean blCheckInputTodoResult = checkInput();
					if(blCheckInputTodoResult == true) {
						//サブカテゴリコード更新処理
						boolean blSubCategoryResult = updateSUBCATEGORY(intPossition, etSName.getText().toString());
						if(blSubCategoryResult == false) {
							Toast.makeText(SubcategoryEdit.this, getString(R.string.sqlite_write_err), Toast.LENGTH_LONG).show();
							dialog.dismiss();
							endActivity();
						}
					}
					setSubcategoryList();
					//ボタン連打ロックオフ
					blCopy = true;
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
					blCopy = true;
					dialog.dismiss();
				}
			}
		});
		adObj.setOnCancelListener(this);
		adObj.show();
		Log.d("DEBUG", "SubcategoryEdit dispSubcategoryCodeDialog End");
	}

	/**
	 * 入力値チェック処理
	 *
	 * @return 処理を行った場合はtrue
	 */
	private boolean checkInput() {
		Log.d("DEBUG", "SubcategoryEdit checkInput Start");
		boolean blResult = true;
		StringBuilder sbObj = new StringBuilder();
		//サブカテゴリ名エディットテキスト
		String strSubcategoryName = etSName.getText().toString();
		if(!(strSubcategoryName == null || strSubcategoryName.equals(""))) {
			//文字数チェック
			if(InputCheckUtil.checkCount(strSubcategoryName, Integer.parseInt(getString(R.string.isc_limit1))) == false) {
				blResult = false;
				sbObj.append(getString(R.string.sceName_msg) + getString(R.string.restoreConfirm1));
			}
		}
		if(blResult == false) {
			showDialog(this, "", sbObj.toString(), getString(R.string.yes_btn));
		}
		Log.d("DEBUG", "SubcategoryEdit checkInput End");
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
		Log.d("DEBUG", "SubcategoryEdit showDialog Start");
		AlertDialog.Builder ad = new AlertDialog.Builder(context);
		ad.setTitle(title);
		ad.setMessage(text);
		ad.setPositiveButton(btnmsg, null);
		ad.show();
		Log.d("DEBUG", "SubcategoryEdit showDialog End");
	}

	/**
	 * サブカテゴリ情報のデータベース更新処理
	 *
	 * @intPosition リストポジション
	 * @strSName 変更カテゴリ名
	 * @return 処理を行った場合はtrue
	 */
	private boolean updateSUBCATEGORY(int intPosition, String strSName) {
		Log.d("DEBUG", "SubcategoryEdit updateSUBCATEGORY Start");
		boolean blResult = true;
		//選択対象年月情報取得
		GregorianCalendar gcObj = DateUtil.toCalendar(tvTargetDate.getText().toString() + DateUtil.FIRST_DAY);
		StringBuilder sbDateText = new StringBuilder();
		sbDateText.append(Integer.toString(gcObj.get(Calendar.YEAR)));
		sbDateText.append(Integer.toString(gcObj.get(Calendar.MONTH)+1));
		//ToDoDatabaseHelper初期化
		tdhDB = new ToDoDatabaseHelper(this,sbDateText.toString() + getString(R.string.sqlite_todo_filename));
		//選択カテゴリ情報取得
		long lgCategoryPossition = sCategory.getSelectedItemId();
		//選択サブカテゴリ情報取得
		SubcategoryListRow slrObj = new SubcategoryListRow();
		slrObj = lSLR.get(intPossition);
		//SQLパラメータ作成(NAME,PCODE,CCODE)
		Subcategory objSubcategory = new Subcategory();
		objSubcategory.setStrName(strSName);
		objSubcategory.setStrPCode(hmCategorySpinner.get((int)lgCategoryPossition));
		objSubcategory.setStrCCode(slrObj.getStrSubcategoryCode());
		//SUBCATEGORYテーブル更新
		blResult = tdhDB.updateSubcategoryName(objSubcategory);
		Log.d("DEBUG", "SubcategoryEdit updateSUBCATEGORY End");
		return blResult;
	}

	/**
	 * DateOnClickListener
	 *  日付の文字列にセットされるリスナー
	 */
	private class DateOnClickListener implements OnClickListener, DialogInterface.OnCancelListener {
		private Context contextObj = null;
		public DateOnClickListener(Context context){
			Log.d("DEBUG", "SubcategoryEdit DateOnClickListener DateOnClickListener Start");
			//Contextが必要なので、コンストラクタで渡して覚えておく
			contextObj = context;
			Log.d("DEBUG", "SubcategoryEdit DateOnClickListener DateOnClickListener End");
		}

		/**
		 * クリックされた時呼び出される
		 * @param View クリックされたビュー
		 */
		public void onClick(View view) {
			Log.d("DEBUG", "SubcategoryEdit DateOnClickListener onClick Start");
			GregorianCalendar gcObj = null;
			if(view == tvTargetDate && blCopy == true){
				//対象年月でクリックされた場合
				//ボタン連打ロックオン
				blCopy = false;
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
								//サブカテゴリリスト用データ読込
								setSubcategoryList();
								//ボタン連打ロックオフ
								blCopy = true;
							}
						}
					})
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface diObj, int intWhich) {
							if(blDialogButton == true) {
								//ダイアログ内ボタン連打ロックオン
								blDialogButton = false;
								//ボタン連打ロックオフ
								blCopy = true;
							}
						}
					})
					.setOnCancelListener(this)
					.create()
					.show();
			Log.d("DEBUG", "SubcategoryEdit DateOnClickListener onClick End");
		}

		/**
		 * onCancel
		 * ダイアログ戻るボタン処理
		 *
		 * @param dialog dialog情報 
		 */
		@Override
		public void onCancel(DialogInterface dialog) {
			Log.d("DEBUG", "SubcategoryEdit DateOnClickListener onCancel Start");
			//ダイアログ表示時に戻るボタンが押下された場合
			//ボタン連打ロックオフ
			blCopy = true;
			Log.d("DEBUG", "SubcategoryEdit DateOnClickListener onCancel End");
		}

	}

	/**
	 * sctCopy
	 *  コピーボタン押下時の処理
	 * @param v 選択ビュー
	 */
	public void sctCopy(View view) {
		Log.d("DEBUG", "SubcategoryEdit sctCopy Start");
		if(blCopy == true) {
			//コピーボタン連打ロックオン
			blCopy = false;
			dispCopyDialog();
		}
		Log.d("DEBUG", "SubcategoryEdit sctCopy End");
	}

	/**
	 * サブカテゴリコピーダイアログ表示処理
	 *
	 */
	public void dispCopyDialog() {
		Log.d("DEBUG", "SubcategoryEdit dispCopyDialog Start");
		AlertDialog.Builder adObj = new AlertDialog.Builder(this);
		//ダイアログ内ボタン連打ロックオフ
		blDialogButton = true;
		//ダイアログタイトル設定
		adObj.setTitle(getString(R.string.copydialog_title));
		//ダイアログメッセージ設定
		adObj.setMessage(getString(R.string.copydialog_msg));
		//アラートダイアログのタッチイベントを設定
		adObj.setPositiveButton(getString(R.string.exec_btn), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(blDialogButton == true) {
					//ダイアログ内ボタン連打ロックオン
					blDialogButton = false;
					//サブカテゴリーコピー処理
					boolean blcopySubcategory = copySubcategory();
					if(blcopySubcategory == false) {
						Toast.makeText(SubcategoryEdit.this, getString(R.string.sqlite_write_err), Toast.LENGTH_LONG).show();
						//コピーボタン連打ロックオフ
						blCopy = true;
						dialog.dismiss();
						endActivity();
					}
					//サブカテゴリ更新
					setSubcategoryList();
					//ボタン連打ロックオフ
					blCopy = true;
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
					blCopy = true;
					dialog.dismiss();
				}
			}
		});
		adObj.setOnCancelListener(this);
		adObj.show();
		Log.d("DEBUG", "SubcategoryEdit dispCopyDialog End");
	}

	/**
	 * サブカテゴリデータコピー処理
	 * 
	 * @return 処理を行った場合はtrue
	 */
	private boolean copySubcategory() {
		Log.d("DEBUG", "SubcategoryEdit copySubcategory Start");
		boolean blCSResult = true;
		//対象前月年月日を取得
		GregorianCalendar gcBefore = DateUtil.toCalendar(tvTargetDate.getText().toString() + DateUtil.FIRST_DAY);
		gcBefore.add(Calendar.MONTH, -1);
		StringBuilder sbBeforeDateText = new StringBuilder();
		sbBeforeDateText.append(Integer.toString(gcBefore.get(Calendar.YEAR)));
		sbBeforeDateText.append(Integer.toString(gcBefore.get(Calendar.MONTH)+1));
		//対象年月日を取得
		GregorianCalendar gcAfter = DateUtil.toCalendar(tvTargetDate.getText().toString() + DateUtil.FIRST_DAY);
		StringBuilder sbAfterDateText = new StringBuilder();
		sbAfterDateText.append(Integer.toString(gcAfter.get(Calendar.YEAR)));
		sbAfterDateText.append(Integer.toString(gcAfter.get(Calendar.MONTH)+1));
		//ToDoDatabaseHelper初期化
		tdhDB = new ToDoDatabaseHelper(this,sbBeforeDateText.toString() + getString(R.string.sqlite_todo_filename));
		//選択カテゴリ情報取得
		long lgCategoryPossition = sCategory.getSelectedItemId();
		//コピー元サブカテゴリ情報のデータベース読み込み処理
		boolean blBeforeResult = selectBeforeSubcategory(hmCategorySpinner.get((int)lgCategoryPossition));
		if(blBeforeResult == false) {
			blCSResult = false;
			Log.d("DEBUG", "SubcategoryEdit copySubcategory(1) End");
			return blCSResult;
		} else {
			if(alCopyTarget.size() > 0) {
				//ToDoDatabaseHelper初期化
				tdhDB = new ToDoDatabaseHelper(this,sbAfterDateText.toString() + getString(R.string.sqlite_todo_filename));
				boolean blAfterResult = tdhDB.updateAfterSubcategory(alCopyTarget);
				if(blAfterResult == false) {
					blCSResult = false;
					Log.d("DEBUG", "SubcategoryEdit copySubcategory(2) End");
					return blCSResult;
				}
			} else {
				//更新対象が0件の場合
				blCSResult = false;
				Log.d("DEBUG", "SubcategoryEdit copySubcategory(3) End");
				return blCSResult;
			}
		}
		Log.d("DEBUG", "SubcategoryEdit copySubcategory(4) End");
		return blCSResult;
	}

	/**
	 * コピー元サブカテゴリ情報のデータベース読み込み処理
	 *
	 * @strCategoryKey カテゴリ略字記号キー
	 * @return 処理を行った場合はtrue
	 */
	private boolean selectBeforeSubcategory(String strCategoryKey) {
		Log.d("DEBUG", "SubcategoryEdit selectBeforeSubcategory Start");
		boolean blResult = true;
		SubcategoryCursor scObj = null;
		alCopyTarget = new ArrayList<Subcategory>();
		try {
			String[] where_args = {strCategoryKey};
			//カーソルの取得
			scObj = tdhDB.getSubcategory(where_args);
			//カーソルポインター初期化
			startManagingCursor(scObj);
			Log.d("DEBUG", "SubcategoryEdit selectBeforeSubcategory CategoryCursor Count : " + scObj.getCount());
			for( int intCt=0; intCt<scObj.getCount(); intCt++){
				//サブカテゴリのセット
				Subcategory sObj = new Subcategory();
				sObj.setStrPCode(scObj.getColPcode());
				sObj.setStrCCode(scObj.getColCcode());
				sObj.setStrName(scObj.getColName());
				alCopyTarget.add(sObj);
				scObj.moveToNext();
			}
			scObj.close();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "SubcategoryEdit selectBeforeSubcategory DB Error",e);
		} finally {
			scObj.close();
		}
		Log.d("DEBUG", "SubcategoryEdit selectBeforeSubcategory End");
		return blResult;
	}

	/**
	 * Activity遷移処理
	 *
	 */
	public void nextActivity() {
		Log.d("DEBUG", "SubcategoryEdit nextActivity Start");
		//「カレンダー」画面に戻る
		//アプリ内のアクティビティを呼び出すインテントの生成
		Class cObj;
		Log.d("DEBUG", "SubcategoryEdit nextActivity stdObj : " + stdObj.getStrUserInterfaceId());
		if(stdObj.getStrUserInterfaceId().equals(getString(R.string.uiid1))) {
			cObj = MainCalendar.class;
			Log.d("DEBUG", "SubcategoryEdit nextActivity objC : MainCalendar.class");
		} else {
			cObj = MainTab.class;
			Log.d("DEBUG", "SubcategoryEdit nextActivity objC : MainTab.class");
		}
		Intent intent = new Intent(this, cObj);
		//インテントのパラメータ設定
		//カレンダー年月
		intent.putExtra("calym", stdObj.getStrCalendarYearMonth());
		//選択年月日
		intent.putExtra("calymd", stdObj.getStrCalendarYearMonthDay());
		//選択元ユーザーインターフェースID
		intent.putExtra("uiid", getString(R.string.uiid13));
		//キーID
		intent.putExtra("keyid", stdObj.getLgKeyId());
		startActivity(intent);
		endActivity();
		Log.d("DEBUG", "SubcategoryEdit nextActivity Start");
	}

	/**
	 * Activity終了処理
	 *
	 */
	private void endActivity() {
		Log.d("DEBUG", "SubcategoryEdit endActivity Start");
		//自アクティビティの終了
		finish();
		Log.d("DEBUG", "SubcategoryEdit endActivity End");
	}

	/**
	 * 画面回転時に呼ばれるサイクル
	 *
	 * @param newConfig 新しい設定値
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d("DEBUG", "SubcategoryEdit onConfigurationChanged Start");
		super.onConfigurationChanged(newConfig);
		Log.d("DEBUG", "SubcategoryEdit onConfigurationChanged End");
	}

	/**
	 * バックグランド時に呼ばれるサイクル
	 *
	 * @param outState 保管設定値
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {  
		Log.d("DEBUG", "SubcategoryEdit onSaveInstanceState Start");
		super.onSaveInstanceState(outState);
		outState.putString("calym", tvTargetDate.getText().toString());
		Log.d("DEBUG", "SubcategoryEdit onSaveInstanceState End");
	} 

	/**
	 * onDestroy
	 * データベースヘルパークローズ処理
	 */
	@Override
	public void onDestroy(){
		Log.d("DEBUG", "SubcategoryEdit onDestroy Start");
		super.onDestroy();
		if(tdhDB != null) {
			tdhDB.close();
		}
		Log.d("DEBUG", "SubcategoryEdit onDestroy End");
	}

	/**
	 * 戻るボタンでカレンダー画面へ遷移
	 *
	 * @param kEvent キーイベント情報 
	 * @return 処理を行った場合はtrue
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent kEvent) {
		Log.d("DEBUG", "SubcategoryEdit dispatchKeyEvent Start");
		//キー押下されたことを確認
		if (kEvent.getAction() == KeyEvent.ACTION_DOWN) {
			//戻るボタンが押されたか確認
			if (kEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				nextActivity();
			}
		}
		Log.d("DEBUG", "SubcategoryEdit dispatchKeyEvent End");
		return super.dispatchKeyEvent(kEvent);
	}

	/**
	 * onCancel
	 * ダイアログ戻るボタン処理
	 *
	 * @param dialog dialog情報 
	 */
	@Override
	public void onCancel(DialogInterface dialog) {
		Log.d("DEBUG", "SubcategoryEdit onCancel Start");
		//ダイアログ表示時に戻るボタンが押下された場合
		//ボタン連打ロックオフ
		blCopy = true;
		Log.d("DEBUG", "SubcategoryEdit onCancel End");
	}
}