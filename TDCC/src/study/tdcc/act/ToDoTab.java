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


 クラス名：ToDoTab
 内容：ToDoタブレイアウトモジュール
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
          0.1/2012.05.23/T.Mashiko
          0.2/2012.05.27/T.Mashiko
          0.3/2012.05.29/T.Mashiko
          0.4/2012.06.05/T.Mashiko
          0.5/2012.06.06/T.Mashiko
          0.6/2012.06.07/T.Mashiko
          0.7/2012.06.11/T.Mashiko
          0.8/2012.07.11/T.Mashiko 日送りフリック対応
          0.9/2012.07.19/T.Mashiko ロギング表記修正
          1.0/2012.08.23/T.Mashiko ボタンの連打ロック対策,ダイアログの戻るキー対策
          1.1/2012.08.25/T.Mashiko 繰越処理の制限対策
          1.2/2012.09.01/T.Mashiko 合計所要時間の表示修正
*/
package study.tdcc.act;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import study.tdcc.*;
import study.tdcc.adapter.ToDoListItemAdapter;
import study.tdcc.bean.*;
import study.tdcc.lib.DateUtil;
import study.tdcc.lib.ToDoDatabaseHelper;
import study.tdcc.lib.ToDoDatabaseHelper.ToDoCursor;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class ToDoTab extends Activity implements DialogInterface.OnCancelListener {
	//ToDoリストビュー
	private ListView lvToDoList;
	//合計所要時間のTextView
	private TextView tvTotalTimeNeeded;
	//データベースオブジェクト
	private ToDoDatabaseHelper tdhDB;
	//画面遷移データオブジェクト
	private ScreenTransitionData stdObj = new ScreenTransitionData();
	//リストビュー用アダプタ
	private ToDoListItemAdapter tdliaObj;
	//ToDoリスト
	private List<ToDoListRow> lTDLR;
	//所要時間リスト
	private ArrayList<Float> alTAT;
	//カスタムダイアログ内 ToDoリスト指定位置情報
	private int intPossition;
	//繰越用未完了ToDoリスト
	private ArrayList<ToDo> alUnfinishedToDo;
	//繰越後のToDoリスト件数
	private int intToDoListCount;
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
			Log.d("DEBUG", "ToDoTab onDown Start");
			Log.d("DEBUG", "ToDoTab onDown End");
			return false;
		}
		//フリック処理を実装
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			Log.d("DEBUG", "ToDoTab onFling Start");
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
							Log.d("DEBUG", "ToDoTab onFling   Lower limit correction implementation.");
						}
					} else {
						//1日加算する
						gcTargetYearMonthDay.add(Calendar.DAY_OF_MONTH, 1);
						//上限月補正
						long lgUpperLimitTime = DateUtil.convMSec(getString(R.string.upper_limit_time));
						if(gcTargetYearMonthDay.getTimeInMillis() > lgUpperLimitTime) {
							gcTargetYearMonthDay.add(Calendar.DAY_OF_MONTH, -1);
							Log.d("DEBUG", "ToDoTab onFling   Upper limit correction implementation.");
						}
					}
					//yyyy-MM-dd 年月日文字列を生成
					stdObj.setStrCalendarYearMonthDay(DateUtil.DATE_FORMAT.format(gcTargetYearMonthDay.getTime()));
					//MainTabへの画面遷移
					nextActivity("flick");
					Log.d("DEBUG", "ToDoTab onFling(1) End");
					return true;
				}
			}
			Log.d("DEBUG", "ToDoTab onFling(2) End");
			return false;
		}
		public void onLongPress(MotionEvent arg0) {
			Log.d("DEBUG", "ToDoTab onLongPress Start");
			Log.d("DEBUG", "ToDoTab onLongPress End");
		}
		public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
			Log.d("DEBUG", "ToDoTab onScroll Start");
			Log.d("DEBUG", "ToDoTab onScroll End");
			return false;
		}
		public void onShowPress(MotionEvent arg0) {
			Log.d("DEBUG", "ToDoTab onShowPress Start");
			Log.d("DEBUG", "ToDoTab onShowPress End");
		}
		public boolean onSingleTapUp(MotionEvent arg0) {
			Log.d("DEBUG", "ToDoTab onSingleTapUp Start");
			Log.d("DEBUG", "ToDoTab onSingleTapUp End");
			return false;
		}
	};

	/**
	 * onCreate
	 * @param savedInstanceState バンドル
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("DEBUG", "ToDoTab onCreate Start");
		super.onCreate(savedInstanceState);
		//ToDoタブビュー描画処理
		setContentView(R.layout.todotab);
		//画面遷移情報の取得
		getScreenTransitionData();
		//画面要素の取得処理
		getViewElement();
		//画面要素へのデータセット
		setViewElement();
		//画面要素のリスナーセット
		setViewListener();
		//フリック連打防止フラグ
		blFlick = true;
		Log.d("DEBUG", "ToDoTab onCreate End");
	}

	/**
	 * 画面遷移情報の取得
	 *
	 */
	private void getScreenTransitionData() {
		Log.d("DEBUG", "ToDoTab getScreenTransitionData Start");
		//画面遷移時のインテント格納値取得
		Bundle bExtras = getIntent().getExtras();
		if (bExtras != null) {
			stdObj.setStrCalendarYearMonth(bExtras.getString("calym"));
			stdObj.setStrCalendarYearMonthDay(bExtras.getString("calymd"));
			stdObj.setStrUserInterfaceId(bExtras.getString("uiid"));
			stdObj.setLgKeyId(bExtras.getLong("keyid"));
		}
		Log.d("DEBUG", "ToDoTab getScreenTransitionData End");
	}

	/**
	 * 画面要素の取得
	 *
	 */
	private void getViewElement() {
		Log.d("DEBUG", "ToDoTab getViewElement Start");
		//スケジュールリストビュー
		lvToDoList = (ListView)this.findViewById(R.id.todolist);
		//合計所要時間テキストビュー
		tvTotalTimeNeeded = (TextView) findViewById(R.id.totaltimeneeded);
		//ビューフリッパー
		vfList = (ViewFlipper)this.findViewById(R.id.vfList);
		//ジェスチャーディテクターを生成
		gdObj = new GestureDetector(this, oglObj);
		Log.d("DEBUG", "ToDoTab getViewElement End");
	}

	/**
	 * 画面要素へのデータセット
	 *
	 */
	private void setViewElement() {
		Log.d("DEBUG", "ToDoTab setViewElement Start");
		//ToDoリストビューの生成
		lvToDoList = (ListView) vfList.getCurrentView();
		//ToDoリスト用データ読込
		setToDoList();
		//合計所要時間テキストビューの集計処理
		BigDecimal bdTotalTAT = new BigDecimal(0d);
		BigDecimal bdTempTAT;
		double dbTotalTAT = 0d;
		for (Iterator<Float> iObj = alTAT.iterator(); iObj.hasNext();) {
			Float flObj = iObj.next(); 
			bdTempTAT = new BigDecimal((double)flObj);
			bdTotalTAT = bdTotalTAT.add(bdTempTAT);
		}
		//四捨五入
		bdTotalTAT = bdTotalTAT.setScale(2,BigDecimal.ROUND_HALF_UP);
		//四捨五入した値をdouble型として取得する
		dbTotalTAT = bdTotalTAT.doubleValue();
		tvTotalTimeNeeded.setText(getString(R.string.totaltime1) + dbTotalTAT + getString(R.string.totaltime2));
		Log.d("DEBUG", "ToDoTab setViewElement End");
	}

	/**
	 * ToDoリスト用データ読込処理
	 *
	 */
	private void setToDoList() {
		Log.d("DEBUG", "ToDoTab setToDoList Start");
		//指定年月日を取得
		Calendar clTargetDate = DateUtil.toCalendar(stdObj.getStrCalendarYearMonthDay());
		StringBuilder sbDateText = new StringBuilder();
		sbDateText.append(Integer.toString(clTargetDate.get(Calendar.YEAR)));
		sbDateText.append(Integer.toString(clTargetDate.get(Calendar.MONTH)+1));
		//ToDoDatabaseHelper初期化
		tdhDB = new ToDoDatabaseHelper(this,sbDateText.toString() + getString(R.string.sqlite_todo_filename));
		//対象年月日(YYYYMMDD)取得
		long lgTargetYMD = DateUtil.convToDoYMD((GregorianCalendar) clTargetDate);
		boolean blToDoResult = selectTODO(lgTargetYMD);
		if(blToDoResult == false) {
			Toast.makeText(ToDoTab.this, getString(R.string.sqlite_read_err), Toast.LENGTH_LONG).show();
			endActivity();
		}
		//ListItemAdapterを生成
		tdliaObj = new ToDoListItemAdapter(this, 0, lTDLR);
		//ToDoリストビューにアダプタをセット
		lvToDoList.setAdapter(tdliaObj);
		Log.d("DEBUG", "ToDoTab setToDoList End");
	}

	/**
	 * ToDo情報のデータベース読み込み処理
	 *
	 * @lgTargetDate 対象年月日
	 * @return 処理を行った場合はtrue
	 */
	private boolean selectTODO(long lgTargetYMD) {
		Log.d("DEBUG", "ToDoTab selectTODO Start");
		boolean blResult = true;
		ToDoCursor tdcObj = null;
		lTDLR = new ArrayList<ToDoListRow>();
		alTAT = new ArrayList<Float>();
		try {
			String[] where_args = {String.valueOf(lgTargetYMD)};
			//カーソルの取得
			tdcObj = tdhDB.getToDoList(where_args);
			//カーソルポインター初期化
			startManagingCursor(tdcObj);
			Log.d("DEBUG", "ToDoTab selectTODO ToDoCursor Count : " + tdcObj.getCount());
			for( int intCt=0; intCt<tdcObj.getCount(); intCt++){
				//ToDoのセット
				ToDoListRow tdlrObj = new ToDoListRow();
				//状態(完了)
				if(tdcObj.getColStatus() == 0l) {
					//未完了の場合
					tdlrObj.setStrStatus(getString(R.string.completion_mark2));
				} else {
					//完了の場合
					tdlrObj.setStrStatus(getString(R.string.completion_mark1));
				}
				//優先順位
				tdlrObj.setStrPriorityCode(tdcObj.getColPriorityCode());
				//所要時間
				tdlrObj.setStrTAT(tdcObj.getColTAT());
				alTAT.add(Float.parseFloat(tdcObj.getColTAT()));
				//カテゴリ
				tdlrObj.setStrCategoryCode(tdcObj.getColCategoryCode());
				//サブカテゴリ
				tdlrObj.setStrSubcategoryCode(tdcObj.getColSubcategoryCode());
				//タイトル
				tdlrObj.setStrTitle(tdcObj.getColTitle());
				//ToDoキー
				tdlrObj.setLgTDID(tdcObj.getColTdid());
				lTDLR.add(tdlrObj);
				tdcObj.moveToNext();
			}
			tdcObj.close();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ToDoTab selectTODO DB Error",e);
		} finally {
			if(tdcObj != null) {
				tdcObj.close();
			}
		}
		Log.d("DEBUG", "ToDoTab selectTODO End");
		return blResult;
	}

	/**
	 * 画面要素へのリスナーセット
	 *
	 */
	private void setViewListener() {
		Log.d("DEBUG", "ToDoTab setViewListener Start");
		//ToDoリストビューのリスナー
		lvToDoList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				//ToDo編集画面へ遷移
				dispToDoEditScreen(position);
			}
		});
		lvToDoList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
				//ToDo削除処理
				dispToDoDeleteDialog(position);
				return false;
			}
		});
		lvToDoList.setOnTouchListener(otlObj);
		Log.d("DEBUG", "ToDoTab setViewListener End");
	}

	/**
	 * ToDo編集画面表示処理
	 *
	 * @position リストポジション
	 * 
	 */
	public void dispToDoEditScreen(int position) {
		Log.d("DEBUG", "ToDoTab dispToDoEditScreen Start");
		intPossition = position;
		//選択ToDo情報取得
		ToDoListRow tdlrObj = new ToDoListRow();
		tdlrObj = lTDLR.get(intPossition);
		stdObj.setLgKeyId(tdlrObj.getLgTDID());
		nextActivity("update");
		Log.d("DEBUG", "ToDoTab dispToDoEditScreen End");
	}

	/**
	 * ToDo削除ダイアログ表示処理
	 *
	 * @position リストポジション
	 * 
	 */
	public void dispToDoDeleteDialog(int position) {
		Log.d("DEBUG", "ToDoTab dispToDoDeleteDialog Start");
		intPossition = position;
		AlertDialog.Builder adObj = new AlertDialog.Builder(this);
		//ダイアログ内ボタン連打ロックオフ
		blDialogButton = true;
		//ダイアログタイトル設定
		adObj.setTitle(getString(R.string.deletedialog_title));
		//ダイアログメッセージ設定
		adObj.setMessage(getString(R.string.deletedialog_msg));
		//アラートダイアログのタッチイベントを設定
		adObj.setPositiveButton(getString(R.string.exec_btn), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(blDialogButton == true) {
					//ダイアログ内ボタン連打ロックオン
					blDialogButton = false;
					//ToDo削除処理
					boolean blDeleteResult = deleteTODO(intPossition);
					if(blDeleteResult == false) {
						Toast.makeText(ToDoTab.this, getString(R.string.sqlite_write_err), Toast.LENGTH_LONG).show();
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
		Log.d("DEBUG", "ToDoTab dispToDoDeleteDialog End");
	}

	/**
	 * ToDo情報のデータベース削除処理
	 *
	 * @intPosition リストポジション
	 * @return 処理を行った場合はtrue
	 */
	private boolean deleteTODO(int intPosition) {
		Log.d("DEBUG", "ToDoTab deleteTODO Start");
		boolean blResult = true;
		//指定年月日を取得
		Calendar clTargetDate = DateUtil.toCalendar(stdObj.getStrCalendarYearMonthDay());
		StringBuilder sbDateText = new StringBuilder();
		sbDateText.append(Integer.toString(clTargetDate.get(Calendar.YEAR)));
		sbDateText.append(Integer.toString(clTargetDate.get(Calendar.MONTH)+1));
		//ToDoDatabaseHelper初期化
		tdhDB = new ToDoDatabaseHelper(this,sbDateText.toString() + getString(R.string.sqlite_todo_filename));
		//選択ToDo情報取得
		ToDoListRow tdlrObj = new ToDoListRow();
		tdlrObj = lTDLR.get(intPossition);
		//SQLパラメータ作成(TDID)
		ToDo tdObj = new ToDo();
		tdObj.setLgTdId(tdlrObj.getLgTDID());
		//ToDOテーブル削除
		blResult = tdhDB.deleteToDo(tdObj);
		Log.d("DEBUG", "ToDoTab deleteTODO End");
		return blResult;
	}

	/**
	 * newAdd
	 *  新規追加ボタン押下時の処理
	 * @param view 選択ビュー
	 */
	public void newAdd(View view) {
		Log.d("DEBUG", "ToDoTab moveMonth Start");
		if(blFlick == true) {
			//ボタン連打ロックオン
			blFlick = false;
			nextActivity("add");
		}
		Log.d("DEBUG", "ToDoTab moveMonth End");
	}

	/**
	 * carryOver
	 *  繰越ボタン押下時の処理
	 * @param view 選択ビュー
	 */
	public void carryOver(View view) {
		Log.d("DEBUG", "ToDoTab carryOver Start");
		if(blFlick == true) {
			//ボタン連打ロックオン
			blFlick = false;
			dispToDoCarryOverDialog();
		}
		Log.d("DEBUG", "ToDoTab carryOver End");
	}

	/**
	 * ToDo繰越ダイアログ表示処理
	 *
	 */
	public void dispToDoCarryOverDialog() {
		Log.d("DEBUG", "ToDoTab dispToDoCarryOverDialog Start");
		AlertDialog.Builder adObj = new AlertDialog.Builder(this);
		//ダイアログ内ボタン連打ロックオフ
		blDialogButton = true;
		//ダイアログタイトル設定
		adObj.setTitle(getString(R.string.carryoverdialog_title));
		//ダイアログメッセージ設定
		adObj.setMessage(getString(R.string.carryoverdialog_msg));
		//アラートダイアログのタッチイベントを設定
		adObj.setPositiveButton(getString(R.string.exec_btn), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(blDialogButton == true) {
					//ダイアログ内ボタン連打ロックオン
					blDialogButton = false;
					//ToDo繰越処理
					boolean blCarryOverResult = carryOverTODO();
					if(blCarryOverResult == false) {
						Toast.makeText(ToDoTab.this, getString(R.string.sqlite_write_err), Toast.LENGTH_LONG).show();
						dialog.dismiss();
						endActivity();
					}
					setViewElement();
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
		Log.d("DEBUG", "ToDoTab dispToDoCarryOverDialog End");
	}

	/**
	 * onCancel
	 * ダイアログ戻るボタン処理
	 *
	 * @param dialog dialog情報 
	 */
	@Override
	public void onCancel(DialogInterface dialog) {
		Log.d("DEBUG", "ToDoTab onCancel Start");
		//ダイアログ表示時に戻るボタンが押下された場合
		//ボタン連打ロックオフ
		blFlick = true;
		Log.d("DEBUG", "ToDoTab onCancel End");
	}

	/**
	 * ToDoデータ繰越処理
	 *
	 */
	private boolean carryOverTODO() {
		Log.d("DEBUG", "ToDoTab carryOverTODO Start");
		boolean blCOResult = true;
		intToDoListCount = 0;
		//転送元年月日を取得
		Calendar clBeforeDate = DateUtil.toCalendar(stdObj.getStrCalendarYearMonthDay());
		StringBuilder sbBeforeDateText = new StringBuilder();
		sbBeforeDateText.append(Integer.toString(clBeforeDate.get(Calendar.YEAR)));
		sbBeforeDateText.append(Integer.toString(clBeforeDate.get(Calendar.MONTH)+1));
		//転送元年月日(YYYYMMDD)取得
		long lgBeforeYMD = DateUtil.convToDoYMD((GregorianCalendar) clBeforeDate);

		//転送先年月日を取得
		//カレンダーオブジェクトを1日増加
		Calendar clAfterDate = DateUtil.toCalendar(stdObj.getStrCalendarYearMonthDay());
		clAfterDate.add(Calendar.DAY_OF_MONTH, 1);
		StringBuilder sbAfterDateText = new StringBuilder();
		sbAfterDateText.append(Integer.toString(clAfterDate.get(Calendar.YEAR)));
		sbAfterDateText.append(Integer.toString(clAfterDate.get(Calendar.MONTH)+1));
		//転送先年月日(YYYYMMDD)取得
		long lgAfterYMD = DateUtil.convToDoYMD((GregorianCalendar) clAfterDate);

		//ToDoDatabaseHelper初期化
		tdhDB = new ToDoDatabaseHelper(this,sbBeforeDateText.toString() + getString(R.string.sqlite_todo_filename));
		boolean blBeforeResult = selectUnfinishedTODO(lgBeforeYMD, lgAfterYMD);
		if(blBeforeResult == false) {
			blCOResult = false;
			Log.d("DEBUG", "ToDoTab carryOverTODO(1) End");
			return blCOResult;
		}
		//繰越対象件数のチェック
		if(alUnfinishedToDo.size() > 0) {
			//ToDoDatabaseHelper初期化
			tdhDB = new ToDoDatabaseHelper(this,sbAfterDateText.toString() + getString(R.string.sqlite_todo_filename));
			//繰越後の件数チェック(繰越後のToDo件数が設定値以上の場合、繰越しない)
			boolean blCountResult = selectTODOCount(lgAfterYMD);
			if(blCountResult == false) {
				blCOResult = false;
				Log.d("DEBUG", "ToDoTab carryOverTODO(2) End");
				return blCOResult;
			} else {
				intToDoListCount = intToDoListCount + alUnfinishedToDo.size();
				if(intToDoListCount <= Integer.parseInt(getString(R.string.todo_limit_count))) {
					boolean blAfterResult = tdhDB.insertUnfinishedTODOList(alUnfinishedToDo);
					if(blAfterResult == false) {
						blCOResult = false;
						Log.d("DEBUG", "ToDoTab carryOverTODO(3) End");
						return blCOResult;
					}
				} else {
					//繰り越し後の件数が上限を超えている場合
					Toast.makeText(ToDoTab.this, getString(R.string.carryoverstop_msg2), Toast.LENGTH_LONG).show();
				}
			}
		} else {
			//繰り越し対象が0件の場合
			Toast.makeText(ToDoTab.this, getString(R.string.carryoverstop_msg), Toast.LENGTH_LONG).show();
		}
		Log.d("DEBUG", "ToDoTab carryOverTODO(4) End");
		return blCOResult;
	}

	/**
	 * 未完了ToDo情報のデータベース読み込み処理
	 *
	 * @lgBeforeDate 繰越元年月日
	 * @lgAfterDate 繰越先年月日
	 * @return 処理を行った場合はtrue
	 */
	private boolean selectUnfinishedTODO(long lgBeforeYMD, long lgAfterYMD) {
		Log.d("DEBUG", "ToDoTab selectUnfinishedTODO Start");
		boolean blResult = true;
		ToDoCursor tdcObj = null;
		//繰越用未完了ToDoリスト
		alUnfinishedToDo = new ArrayList<ToDo>();
		try {
			String[] where_args = {String.valueOf(lgBeforeYMD)};
			//カーソルの取得
			tdcObj = tdhDB.getUnfinishedToDoList(where_args);
			//カーソルポインター初期化
			startManagingCursor(tdcObj);
			Log.d("DEBUG", "ToDoTab selectUnfinishedTODO ToDoCursor Count : " + tdcObj.getCount());
			for( int intCt=0; intCt<tdcObj.getCount(); intCt++){
				//未完了ToDoのセット
				ToDo tdObj = new ToDo();
				//TDID(自動採番の為,セットしない)
				//DATE(繰越日をセット)
				tdObj.setLgDate(lgAfterYMD);
				//TITLE
				tdObj.setStrTitle(tdcObj.getColTitle());
				//PRIORITY_CODE
				tdObj.setStrPriorityCode(tdcObj.getColPriorityCode());
				//TAT
				tdObj.setStrTAT(tdcObj.getColTAT());
				//CATEGORY_CODE
				tdObj.setStrCategoryCode(tdcObj.getColCategoryCode());
				//SUBCATEGORY_CODE
				tdObj.setStrSubcategoryCode(tdcObj.getColSubcategoryCode());
				//STATUS
				tdObj.setLgStatus(tdcObj.getColStatus());
				//DETAIL
				tdObj.setStrDetail(tdcObj.getColDetail());
				alUnfinishedToDo.add(tdObj);
				tdcObj.moveToNext();
			}
			tdcObj.close();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ToDoTab selectUnfinishedTODO DB Error",e);
		} finally {
			if(tdcObj != null) {
				tdcObj.close();
			}
		}
		Log.d("DEBUG", "ToDoTab selectUnfinishedTODO End");
		return blResult;
	}

	/**
	 * ToDo情報の件数取得処理
	 *
	 * @lgAfterDate 繰越先年月日
	 * @return 処理を行った場合はtrue
	 */
	private boolean selectTODOCount(long lgAfterYMD) {
		Log.d("DEBUG", "ToDoTab selectTODOCount Start");
		boolean blResult = true;
		ToDoCursor tdcObj = null;
		try {
			String[] where_args = {String.valueOf(lgAfterYMD)};
			//カーソルの取得
			tdcObj = tdhDB.getToDoListCount(where_args);
			//カーソルポインター初期化
			startManagingCursor(tdcObj);
			//繰越先のToDoの件数取得
			intToDoListCount = tdcObj.getCount();
			Log.d("DEBUG", "ToDoTab selectTODOCount ToDoCursor Count : " + tdcObj.getCount());
			tdcObj.close();
		} catch (SQLException e) {
			blResult = false;
			Log.e("ERROR", "ToDoTab selectTODOCount DB Error",e);
		} finally {
			if(tdcObj != null) {
				tdcObj.close();
			}
		}
		Log.d("DEBUG", "ToDoTab selectTODOCount End");
		return blResult;
	}

	/**
	 * Activity遷移処理
	 *
	 * @param strNext add:ToDo登録 or update:ToDo編集 or flick:MainTab
	 */
	public void nextActivity(String strNext) {
		Log.d("DEBUG", "ToDoTab nextActivity Start");
		//アプリ内のアクティビティを呼び出すインテントの生成
		Intent intent;
		if(strNext.equals("add")) {
			//ToDo登録画面遷移の場合
			intent = new Intent(this, ToDoRegistration.class);
			//インテントのパラメータ設定
			//キーID
			intent.putExtra("keyid", 0l);
		} else if(strNext.equals("update")) {
			//ToDo編集画面遷移の場合
			intent = new Intent(this, ToDoRegistration.class);
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
		intent.putExtra("uiid", getString(R.string.uiid4));
		//アクティビティの呼び出し
		startActivity(intent);
		Log.d("DEBUG", "ToDoTab nextActivity End");
		//自アクティビティの終了
		endActivity();
	}

	/**
	 * Activity終了処理
	 *
	 */
	private void endActivity() {
		Log.d("DEBUG", "ToDoTab endActivity Start");
		//自アクティビティの終了
		finish();
		Log.d("DEBUG", "ToDoTab endActivity End");
	}

	/**
	 * 画面回転時に呼ばれるサイクル
	 *
	 * @param newConfig 新しい設定値
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d("DEBUG", "ToDoTab onConfigurationChanged Start");
		super.onConfigurationChanged(newConfig);
		Log.d("DEBUG", "ToDoTab onConfigurationChanged End");
	}

	/**
	 * onDestroy
	 * データベースヘルパークローズ処理
	 */
	@Override
	public void onDestroy(){
		Log.d("DEBUG", "ToDoTab onDestroy Start");
		super.onDestroy();
		if(tdhDB != null) {
			tdhDB.close();
		}
		Log.d("DEBUG", "ToDoTab onDestroy End");
	}
}