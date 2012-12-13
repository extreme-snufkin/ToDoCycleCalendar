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


 クラス名：BackupRestore
 内容：バックアップ／リストア
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
          0.1/2012.05.30/T.Mashiko
          0.2/2012.05.31/T.Mashiko
          0.3/2012.06.06/T.Mashiko インテントLong型修正
          0.4/2012.06.21/T.Mashiko
          0.5/2012.06.22/T.Mashiko
          0.6/2012.06.25/T.Mashiko 非同期処理対応
          0.7/2012.06.26/T.Mashiko リストビューの摘み追加
          0.8/2012.07.18/T.Mashiko ロギング表記修正
          0.9/2012.08.24/T.Mashiko ボタンの連打ロック対策,ダイアログの戻るキー対策
*/
package study.tdcc.act;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import study.tdcc.*;
import study.tdcc.bean.*;
import study.tdcc.lib.FileUtil;
import study.tdcc.lib.InputCheckUtil;
import study.tdcc.lib.StorageUtil;
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
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class BackupRestore extends Activity implements OnItemClickListener, DialogInterface.OnCancelListener{
	//カスタムタイトルテキストビュー
	private TextView tvCustomTitle;
	//カスタムタイトル(バージョン)テキストビュー
	private TextView tvCustomTitleVersion;
	//バックアップダイアログ内エディットテキストビュー
	private EditText etBUName;
	//リストア対象文字列配列
	private ArrayList<String> alRestore = new ArrayList<String>();
	//画面遷移データオブジェクト
	private ScreenTransitionData stdObj = new ScreenTransitionData();
	//プログレスダイアログのインスタンス
	private ProgressDialog pdObj = null;
	//データを受け取るhandler
	private Handler hObj;
	//バックアップ、リストアトースト返却文字列
	String strToastMsg;
	//カスタムダイアログ内 リストア指定位置情報
	private int intPossition;
	//ボタン連打防止フラグ
	private boolean blBR = true;
	//ダイアログ内ボタン連打防止フラグ
	private boolean blDialogButton = true;
	//ダイアログ内ボタン連打防止フラグ
	private boolean blSecondDialogButton = true;
	
	/**
	 * onCreate
	 * @param savedInstanceState バンドル
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("DEBUG", "BackupRestore onCreate Start");
		super.onCreate(savedInstanceState);
		//window がフォーカスを受けたときに常に soft input area を隠す
		this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		//カスタムタイトル
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		//バックアップ／リストア画面描画処理
		setContentView(R.layout.backuprestore);
		//カスタムタイトル描画処理
		Window wObj = getWindow();
		wObj.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.customtitle);
		//画面遷移情報の取得
		getScreenTransitionData();
		//画面要素の取得処理
		getViewElement();
		//カスタムタイトルの内容セット
		setCustomTitle();
		//ボタン連打防止フラグ
		blBR = true;
		Log.d("DEBUG", "BackupRestore onCreate End");
	}

	/**
	 * 画面遷移情報の取得
	 *
	 */
	private void getScreenTransitionData() {
		Log.d("DEBUG", "BackupRestore getScreenTransitionData Start");
		//画面遷移時のインテント格納値取得
		Bundle bExtras = getIntent().getExtras();
		if (bExtras != null) {
			stdObj.setStrCalendarYearMonth(bExtras.getString("calym"));
			stdObj.setStrCalendarYearMonthDay(bExtras.getString("calymd"));
			stdObj.setStrUserInterfaceId(bExtras.getString("uiid"));
			stdObj.setLgKeyId(bExtras.getLong("keyid"));
		}
		Log.d("DEBUG", "BackupRestore getScreenTransitionData End");
	}

	/**
	 * 画面要素の取得
	 *
	 */
	private void getViewElement() {
		Log.d("DEBUG", "BackupRestore getViewElement Start");
		//カスタムタイトルテキストビュー
		tvCustomTitle = (TextView)this.findViewById(R.id.titletext);
		//カスタムタイトルバージョンビュー
		tvCustomTitleVersion = (TextView)this.findViewById(R.id.titleversion);
		Log.d("DEBUG", "BackupRestore getViewElement End");
	}

	/**
	 * カスタムタイトルの内容セット
	 *
	 */
	private void setCustomTitle() {
		Log.d("DEBUG", "BackupRestore setCustomTitle Start");
		tvCustomTitle.setText(getString(R.string.act_name12));
		StringBuilder sbVersion = new StringBuilder();
		sbVersion.append(getString(R.string.title_version));
		PackageManager pmObj = this.getPackageManager();
		try {
			PackageInfo piObj = pmObj.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
			sbVersion.append(piObj.versionName);
		} catch (NameNotFoundException e) {
			Log.e("ERROR", "BackupRestore setCustomTitle NameNotFoundException", e);
		}
		tvCustomTitleVersion.setText(sbVersion.toString());
		Log.d("DEBUG", "BackupRestore setCustomTitle End");
	}

	/**
	 * リストビューのクリック時の処理
	 *
	 * @param parent アダプタービュー
	 * @param view ビュー
	 * @param position 位置情報
	 * @param id ID情報
	 */
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d("DEBUG", "ProductMenu onItemClick Start position: "+ String.valueOf(position));
		if(blDialogButton == true) {
			//ダイアログ内ボタン連打ロックオン
			blDialogButton = false;
			//リストアダイアログ内のリストビューの場合
			dispRestoreDialog(position);
		}
		Log.d("DEBUG", "ProductMenu onItemClick End");
	}

	/**
	 * バックアップ実行確認処理
	 * @param view ビュー
	 */
	public void dispBackUpDialog(View view) {
		Log.d("DEBUG", "BackupRestore dispBackUpDialog Start");
		if(blBR == true) {
			//ボタン連打ロックオン
			blBR = false;
			AlertDialog.Builder adObj = new AlertDialog.Builder(this);
			//ダイアログ内ボタン連打ロックオフ
			blDialogButton = true;
			adObj.setTitle(getString(R.string.backuptitle));
			//外枠レイアウト作成
			LinearLayout llOut = new LinearLayout(this);
			llOut.setOrientation(LinearLayout.HORIZONTAL);
			//バックアップ名エディットテキスト追加
			etBUName = new EditText(this);
			etBUName.setHint(getString(R.string.backuphint));
			llOut.addView(etBUName);
			adObj.setView(llOut);
			//アラートダイアログのタッチイベントを設定
			adObj.setPositiveButton(getString(R.string.exec_btn), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if(blDialogButton == true) {
						//ダイアログ内ボタン連打ロックオン
						blDialogButton = false;
						//入力値チェック(空orNull以外,半角英数字のみ,文字数1-20文字以下,)
						boolean blCheckInput = checkInputBackUp(etBUName.getText().toString());
						if(blCheckInput == true) {
							//戻りを受け取るhandler
							hObj = new Handler();
							//バックアップ非同期処理
							backupData();
							dialog.dismiss();
						} else {
							//ボタン連打ロックオフ
							blBR = true;
						}
					}
				}
			});
			adObj.setNegativeButton(getString(R.string.cancel_btn), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if(blDialogButton == true) {
						//ダイアログ内ボタン連打ロックオン
						blDialogButton = false;
						//ボタン連打ロックオフ
						blBR = true;
						dialog.dismiss();
					}
				}
			});
			adObj.setOnCancelListener(this);
			adObj.show();
		}
		Log.d("DEBUG", "BackupRestore dispBackUpDialog End");
	}

	/**
	 * バックアップ名入力値チェック処理
	 * @param strInput 入力文字列
	 * @return 処理を行った場合はtrue
	 */
	private boolean checkInputBackUp(String strInput) {
		Log.d("DEBUG", "BackupRestore checkInputBackUp Start");
		boolean blResult = true;
		StringBuilder sbObj = new StringBuilder();
		if(strInput == null || strInput.equals("")) {
			//空（null）チェック
			blResult = false;
			sbObj.append(getString(R.string.backup_msg1) + getString(R.string.restoreConfirm1));
		} else {
			if(InputCheckUtil.alphaNumCheck(strInput) == false){
				//文字種チェック
				blResult = false;
				sbObj.append(getString(R.string.backup_msg2) + getString(R.string.restoreConfirm1));
			} else {
				if(InputCheckUtil.checkCount(strInput, 20) == false) {
					//文字数チェック
					blResult = false;
					sbObj.append(getString(R.string.backup_msg3) + getString(R.string.restoreConfirm1));
				}
			}
		}
		if(blResult == false) {
			showDialog(this, "", sbObj.toString(), getString(R.string.yes_btn));
		}
		Log.d("DEBUG", "BackupRestore checkInputBackUp End");
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
		Log.d("DEBUG", "BackupRestore showDialog Start");
		AlertDialog.Builder ad = new AlertDialog.Builder(context);
		ad.setTitle(title);
		ad.setMessage(text);
		ad.setPositiveButton(btnmsg, null);
		ad.show();
		Log.d("DEBUG", "BackupRestore showDialog End");
	}

	/**
	 * onCancel
	 * ダイアログ戻るボタン処理
	 *
	 * @param dialog dialog情報 
	 */
	@Override
	public void onCancel(DialogInterface dialog) {
		Log.d("DEBUG", "BackupRestore onCancel Start");
		//ダイアログ表示時に戻るボタンが押下された場合
		//ボタン連打ロックオフ
		blBR = true;
		//ダイアログ内ボタン連打ロックオフ
		blDialogButton = true;
		Log.d("DEBUG", "BackupRestore onCancel End");
	}

	/**
	 * バックアップ非同期処理
	 */
	public void backupData(){
		Log.d("DEBUG", "BackupRestore backupData Start");
		//プログレスダイアログの作成
		pdObj = new ProgressDialog(BackupRestore.this);
		pdObj.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pdObj.setMessage(getString(R.string.backup_msg6));
		//途中での停止不可設定
		pdObj.setCancelable(false);
		//プログレスダイアログの表示
		pdObj.show();
		//実際の処理を行うスレッドを作成
		Thread thread = new Thread(runBackup);
		//スレッドの実行開始
		thread.start();
		Log.d("DEBUG", "BackupRestore backupData End");
	}

	/**
	 * runBackupの実行を行うスレッド
	 */
	private Runnable runBackup = new Runnable(){
		public void run() {
			Log.d("DEBUG", "BackupRestore runBackup run Start");
			//トースト返却文字列
			strToastMsg = "";
			//バックアップ対象ファイルの一覧とサイズ取得
			//内部SQLiteファイルパス取得
			String strSQLitePath = Environment.getDataDirectory().getPath()+ getString(R.string.sqlite_directory_path1) + BackupRestore.this.getPackageName() + getString(R.string.sqlite_directory_path2);
			ArrayList<String> alFileName = FileUtil.getFileNameList(strSQLitePath);
			long lgTotalFileSize = FileUtil.getDirectoryTotalFileSize(strSQLitePath);
			if(alFileName.size() == 0 || lgTotalFileSize == -1) {
				strToastMsg = getString(R.string.backup_msg4);
			} else {
				//外部ストレージのマウント状態確認
				if(StorageUtil.checkExternalStorageState() == false) {
					strToastMsg = getString(R.string.storage_status_err);
				} else {
					//外部ストレージの空き容量確認
					if((StorageUtil.getExternalStorageAvailableSize() > lgTotalFileSize) == false) {
						Log.d("DEBUG", "BackupRestore runBackup ExternalStorageAvailableSize : " + StorageUtil.getExternalStorageAvailableSize());
						Log.d("DEBUG", "BackupRestore runBackup SQLiteFileSize : " + lgTotalFileSize);
						strToastMsg = getString(R.string.storage_disksize_err);
					} else {
						//外部ストレージに同名ディレクトリが存在しないか確認
						//外部ストレージパス取得
						StringBuilder sbOuterStoragePath = new StringBuilder();
						sbOuterStoragePath.append(StorageUtil.getExternalStoragePath().getPath());
						sbOuterStoragePath.append(getString(R.string.sqlite_directory_path3));
						sbOuterStoragePath.append(etBUName.getText().toString());
						if(FileUtil.ExecFileExists(sbOuterStoragePath.toString())) {
							Log.d("DEBUG", "BackupRestore runBackup ExecFileExists FileName : " + sbOuterStoragePath.toString());
							strToastMsg = getString(R.string.backup_exist_err);
						} else {
							//ファイルバックアップコピー
							//ディレクトリ作成
							File fNewDirectory = new File(sbOuterStoragePath.toString());
							if (fNewDirectory.mkdirs()){
								//ファイルコピー
								try {
									for (Iterator<String> iObj = alFileName.iterator(); iObj.hasNext();) {
										String strFileName = iObj.next();
										StringBuilder sbFromPath = new StringBuilder();
										sbFromPath.append(strSQLitePath);
										sbFromPath.append(strFileName);
										StringBuilder sbToPath = new StringBuilder();
										sbToPath.append(sbOuterStoragePath.toString());
										sbToPath.append(getString(R.string.sqlite_directory_kugiri));
										sbToPath.append(strFileName);
										FileUtil.copyTransfer(sbFromPath.toString(), sbToPath.toString());
									}
									strToastMsg = getString(R.string.backup_msg5);
								} catch (Exception e) {
									Log.e("ERROR", "BackupRestore runBackup FileCopyError", e);
									strToastMsg = getString(R.string.backup_copy_err);
								}
							}else{
								Log.e("ERROR", "BackupRestore runBackup DirectoryMakeError : " + sbOuterStoragePath.toString() );
								strToastMsg = getString(R.string.backup_copy_err);
							}
						}
					}
				}
			}
			hObj.post(new Runnable(){
				public void run(){
					//プログレスダイアログを消去
					Toast.makeText(BackupRestore.this, strToastMsg, Toast.LENGTH_LONG).show();
					//ボタン連打ロックオフ
					blBR = true;
					pdObj.dismiss();
				}
			});
			Log.d("DEBUG", "BackupRestore runBackup run End");
		}
	};

	/**
	 * リストア対象リスト表示処理
	 *
	 */
	public void dispRestoreList(View view) {
		Log.d("DEBUG", "BackupRestore dispRestoreList Start");
		if(blBR == true) {
			//ボタン連打ロックオン
			blBR = false;
			//外部ストレージのマウント状態確認
			if(StorageUtil.checkExternalStorageState() == false) {
				Toast.makeText(BackupRestore.this, getString(R.string.storage_status_err), Toast.LENGTH_LONG).show();
				//ボタン連打ロックオフ
				blBR = true;
			} else {
				//外部ストレージの規定ディレクトリ以下のディレクトリ名リスト取得
				StringBuilder sbOuterStoragePath = new StringBuilder();
				sbOuterStoragePath.append(StorageUtil.getExternalStoragePath().getPath());
				sbOuterStoragePath.append(getString(R.string.sqlite_directory_path3));
				alRestore = FileUtil.getFileNameList(sbOuterStoragePath.toString());
				//バックアップディレクトリが存在しない場合
				if(alRestore.size() == 0){
					Toast.makeText(BackupRestore.this, getString(R.string.restore_exist_err), Toast.LENGTH_LONG).show();
					//ボタン連打ロックオフ
					blBR = true;
				} else {
					AlertDialog.Builder adObj = new AlertDialog.Builder(this);
					//ダイアログ内ボタン連打ロックオフ
					blDialogButton = true;
					adObj.setTitle(getString(R.string.restoretitle));
					//外枠レイアウト作成
					LinearLayout llOut = new LinearLayout(this);
					llOut.setOrientation(LinearLayout.HORIZONTAL);
					//リストビュー追加
					ListView lvRestore = new ListView(this);
					llOut.addView(lvRestore);
					adObj.setView(llOut);
					lvRestore.setFastScrollEnabled(true);
					//リストビューデータ追加
					ArrayAdapter<String> aaObj = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, alRestore);
					lvRestore.setAdapter(aaObj);
					lvRestore.setOnItemClickListener(this);
					//閉じるボタンの設定
					adObj.setNeutralButton(getString(R.string.close_btn), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if(blDialogButton == true) {
								//ダイアログ内ボタン連打ロックオン
								blDialogButton = false;
								//ボタン連打ロックオフ
								blBR = true;
								dialog.dismiss();
							}
						}
					});
					adObj.setOnCancelListener(this);
					adObj.show();
				}
			}
		}
		Log.d("DEBUG", "BackupRestore dispRestoreList End");
	}

	/**
	 * リストア実行確認処理
	 *
	 * @param position 位置情報
	 */
	public void dispRestoreDialog(int position) {
		Log.d("DEBUG", "BackupRestore dispRestoreDialog Start");
		intPossition = position;
		AlertDialog.Builder adObj = new AlertDialog.Builder(this);
		//ダイアログ内ボタン連打ロックオフ
		blSecondDialogButton = true;
		adObj.setTitle(getString(R.string.restoretitle));
		//外枠レイアウト作成
		LinearLayout llOut = new LinearLayout(this);
		llOut.setOrientation(LinearLayout.HORIZONTAL);
		//リストア確認テキストビュー追加
		TextView tvRestore = new TextView(this);
		tvRestore.setText(getString(R.string.restoreConfirm2) + alRestore.get(intPossition) + getString(R.string.restoreConfirm3));
		tvRestore.setTextSize(18.0f);
		llOut.addView(tvRestore);
		adObj.setView(llOut);
		//アラートダイアログのタッチイベントを設定
		adObj.setPositiveButton(getString(R.string.exec_btn), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(blSecondDialogButton == true) {
					//ダイアログ内ボタン連打ロックオン
					blSecondDialogButton = false;
					//戻りを受け取るhandler
					hObj = new Handler();
					//リストア非同期処理
					restoreData();
					dialog.dismiss();
				}
			}
		});
		adObj.setNegativeButton(getString(R.string.cancel_btn), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(blSecondDialogButton == true) {
					//ダイアログ内ボタン連打ロックオン
					blSecondDialogButton = false;
					dialog.dismiss();
					//ダイアログ内ボタン連打ロックオフ
					blDialogButton = true;
				}
			}
		});
		adObj.setOnCancelListener(this);
		adObj.show();
		Log.d("DEBUG", "BackupRestore dispRestoreDialog End");
	}

	/**
	 * リストア非同期処理
	 */
	public void restoreData(){
		Log.d("DEBUG", "BackupRestore restoreData Start");
		//プログレスダイアログの作成
		pdObj = new ProgressDialog(BackupRestore.this);
		pdObj.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pdObj.setMessage(getString(R.string.restore_msg5));
		//途中での停止不可設定
		pdObj.setCancelable(false);
		//プログレスダイアログの表示
		pdObj.show();
		//実際の処理を行うスレッドを作成
		Thread thread = new Thread(runRestore);
		//スレッドの実行開始
		thread.start();
		Log.d("DEBUG", "BackupRestore restoreData End");
	}

	/**
	 * runRestoreの実行を行うスレッド
	 */
	private Runnable runRestore = new Runnable(){
		public void run() {
			Log.d("DEBUG", "BackupRestore runRestore run Start");
			//トースト返却文字列
			strToastMsg = "";
			//外部ストレージのマウント状態確認
			if(StorageUtil.checkExternalStorageState() == false) {
				strToastMsg = getString(R.string.storage_status_err);
			} else {
				//外部ストレージパス取得
				StringBuilder sbOuterStoragePath = new StringBuilder();
				sbOuterStoragePath.append(StorageUtil.getExternalStoragePath().getPath());
				sbOuterStoragePath.append(getString(R.string.sqlite_directory_path3));
				sbOuterStoragePath.append(alRestore.get(intPossition));
				//リストア対象ファイルの一覧とサイズ取得
				ArrayList<String>alRestoreFileList = FileUtil.getFileNameList(sbOuterStoragePath.toString());
				long lgTotalFileSize = FileUtil.getDirectoryTotalFileSize(sbOuterStoragePath.toString());
				if(alRestoreFileList.size() == 0 || lgTotalFileSize == -1) {
					strToastMsg = getString(R.string.restore_msg1);
				} else {
					//内部SQLiteファイルの一覧取得
					String strSQLitePath = Environment.getDataDirectory().getPath()+ getString(R.string.sqlite_directory_path1) + BackupRestore.this.getPackageName() + getString(R.string.sqlite_directory_path2);
					ArrayList<String> alDeleteFileList = FileUtil.getFilePathList(strSQLitePath);
					//内部SQLiteファイル削除
					boolean blDelete = true;
					for (Iterator<String> iObj = alDeleteFileList.iterator(); iObj.hasNext();) {
						String strFilePath = iObj.next();
						boolean blDeleteErr = FileUtil.ExecFileDelete(strFilePath);
						if(blDeleteErr == false) {
							blDelete = false;
						}
					}
					if(blDelete == false) {
						strToastMsg = getString(R.string.restore_msg2);
					} else {
						//内部ストレージの空き容量確認
						if(StorageUtil.getInteranlStorageAvailableSize() < lgTotalFileSize) {
							strToastMsg = getString(R.string.restore_msg3);
						} else {
							//リストアファイルコピー
							try {
								for (Iterator<String> iObj = alRestoreFileList.iterator(); iObj.hasNext();) {
									String strFileName = iObj.next();
									StringBuilder sbFromPath = new StringBuilder();
									sbFromPath.append(sbOuterStoragePath.toString());
									sbFromPath.append(getString(R.string.sqlite_directory_kugiri));
									sbFromPath.append(strFileName);
									StringBuilder sbToPath = new StringBuilder();
									sbToPath.append(strSQLitePath.toString());
									sbToPath.append(strFileName);
									FileUtil.copyTransfer(sbFromPath.toString(), sbToPath.toString());
								}
								strToastMsg = getString(R.string.restore_msg4);
							} catch (Exception e) {
								Log.e("ERROR", "BackupRestore dispBackUpDialog FileCopyError", e);
								strToastMsg = getString(R.string.restore_copy_err);
							}
						}
					}
				}
			}
			hObj.post(new Runnable(){
				public void run(){
					//プログレスダイアログを消去
					Toast.makeText(BackupRestore.this, strToastMsg, Toast.LENGTH_LONG).show();
					//ダイアログ内ボタン連打ロックオフ
					blDialogButton = true;
					pdObj.dismiss();
				}
			});
			Log.d("DEBUG", "BackupRestore runRestore run End");
		}
	};

	/**
	 * Activity遷移処理
	 *
	 */
	public void nextActivity() {
		Log.d("DEBUG", "BackupRestore nextActivity Start");
		//「カレンダー」画面に戻る
		//アプリ内のアクティビティを呼び出すインテントの生成
		Intent intent = new Intent(this, MainCalendar.class);
		//インテントのパラメータ設定
		//カレンダー年月
		intent.putExtra("calym", stdObj.getStrCalendarYearMonth());
		//選択年月日
		intent.putExtra("calymd", stdObj.getStrCalendarYearMonthDay());
		//選択元ユーザーインターフェースID
		intent.putExtra("uiid", getString(R.string.uiid14));
		//キーID
		intent.putExtra("keyid", stdObj.getLgKeyId());
		startActivity(intent);
		endActivity();
		Log.d("DEBUG", "BackupRestore nextActivity End");
	}

	/**
	 * Activity終了処理
	 *
	 */
	private void endActivity() {
		Log.d("DEBUG", "BackupRestore endActivity Start");
		//自アクティビティの終了
		finish();
		Log.d("DEBUG", "BackupRestore endActivity End");
	}

	/**
	 * 画面回転時に呼ばれるサイクル
	 *
	 * @param newConfig 新しい設定値
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d("DEBUG", "BackupRestore onConfigurationChanged Start");
		super.onConfigurationChanged(newConfig);
		Log.d("DEBUG", "BackupRestore onConfigurationChanged End");
	}

	/**
	 * 戻るボタンでカレンダー画面へ遷移
	 *
	 * @param kEvent キーイベント情報 
	 * @return 処理を行った場合はtrue
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent kEvent) {
		Log.d("DEBUG", "BackupRestore dispatchKeyEvent Start");
		//キー押下されたことを確認
		if (kEvent.getAction() == KeyEvent.ACTION_DOWN) {
			//戻るボタンが押されたか確認
			if (kEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				nextActivity();
			}
		}
		Log.d("DEBUG", "BackupRestore dispatchKeyEvent End");
		return super.dispatchKeyEvent(kEvent);
	}
}