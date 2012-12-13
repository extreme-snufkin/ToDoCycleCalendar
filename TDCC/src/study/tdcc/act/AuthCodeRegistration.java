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


 クラス名：AuthCodeRegistration
 内容：AuthCode登録画面
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
          0.1/2012.06.25/T.Mashiko
          0.2/2012.06.26/T.Mashiko
          0.3/2012.07.18/T.Mashiko ロギング表記修正
          0.4/2012.08.23/T.Mashiko ボタンの連打ロック対策
*/
package study.tdcc.act;

import study.tdcc.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;

public class AuthCodeRegistration extends Activity {
	//カスタムタイトルテキストビュー
	private TextView tvCustomTitle;
	//カスタムタイトル(バージョン)テキストビュー
	private TextView tvCustomTitleVersion;
	//AuthCode入力エディットテキストビュー
	private EditText etAuthCode;
	//ボタン連打防止フラグ
	private boolean blSave = true;
	
	/**
	 * onCreate
	 * @param savedInstanceState バンドル
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("DEBUG", "AuthCodeRegistration onCreate Start");
		super.onCreate(savedInstanceState);
		//window がフォーカスを受けたときに常に soft input area を隠す
		this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		//カスタムタイトル
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		//コード登録画面描画処理
		setContentView(R.layout.authcoderegistration);
		//カスタムタイトル描画処理
		Window wObj = getWindow();
		wObj.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.customtitle);
		//画面要素の取得処理
		getViewElement();
		//カスタムタイトルの内容セット
		setCustomTitle();
		//ボタン連打ロック解除
		blSave = true;
		Log.d("DEBUG", "AuthCodeRegistration onCreate End");
	}

	/**
	 * 画面要素の取得
	 *
	 */
	private void getViewElement() {
		Log.d("DEBUG", "AuthCodeRegistration getViewElement Start");
		//カスタムタイトルテキストビュー
		tvCustomTitle = (TextView)this.findViewById(R.id.titletext);
		//カスタムタイトルバージョンビュー
		tvCustomTitleVersion = (TextView)this.findViewById(R.id.titleversion);
		//コードエディットテキストビュー
		etAuthCode = (EditText)this.findViewById(R.id.etAuthCode);
		Log.d("DEBUG", "AuthCodeRegistration getViewElement End");
	}

	/**
	 * カスタムタイトルの内容セット
	 *
	 */
	private void setCustomTitle() {
		Log.d("DEBUG", "AuthCodeRegistration setCustomTitle Start");
		tvCustomTitle.setText(getString(R.string.act_name13));
		StringBuilder sbVersion = new StringBuilder();
		sbVersion.append(getString(R.string.title_version));
		PackageManager pmObj = this.getPackageManager();
		try {
			PackageInfo piObj = pmObj.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
			sbVersion.append(piObj.versionName);
		} catch (NameNotFoundException e) {
			Log.e("ERROR", "AuthCodeRegistration setCustomTitle NameNotFoundException", e);
		}
		tvCustomTitleVersion.setText(sbVersion.toString());
		Log.d("DEBUG", "AuthCodeRegistration setCustomTitle End");
	}

	/**
	 * acrSave
	 * 保存ボタン押下時の処理
	 * @param v 選択ビュー
	 */
	public void acrSave(View view) {
		Log.d("DEBUG", "AuthCodeRegistration acrSave Start");
		if(blSave == true) {
			//ボタン連打ロックオン
			blSave = false;
			//入力チェック
			boolean blCheckInputResult = checkInputCode();
			if(blCheckInputResult == true) {
				Intent intent = new Intent();
				//保存がクリックされたらresultIntentにAuthCodeの文字列をセット
				intent.putExtra(MainCalendar.AUTH_CODE, etAuthCode.getText().toString());
				setResult(Activity.RESULT_OK, intent);
				endActivity();
			} else {
				//ボタン連打ロックオフ
				blSave = true;
			}
		}
		Log.d("DEBUG", "AuthCodeRegistration acrSave End");
	}

	/**
	 * 入力値チェック処理
	 *
	 * @return 処理を行った場合はtrue
	 */
	private boolean checkInputCode() {
		Log.d("DEBUG", "AuthCodeRegistration checkInputCode Start");
		boolean blResult = true;
		StringBuilder sbObj = new StringBuilder();
		//コードエディットテキスト
		String strAuthCode = etAuthCode.getText().toString();
		if(strAuthCode == null || strAuthCode.equals("")) {
			//空（null）チェック
			blResult = false;
			sbObj.append(getString(R.string.acroauthcodehint));
		}
		if(blResult == false) {
			showDialog(this, "", sbObj.toString(), getString(R.string.yes_btn));
		}
		Log.d("DEBUG", "AuthCodeRegistration checkInputCode End");
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
		Log.d("DEBUG", "AuthCodeRegistration showDialog Start");
		AlertDialog.Builder ad = new AlertDialog.Builder(context);
		ad.setTitle(title);
		ad.setMessage(text);
		ad.setPositiveButton(btnmsg, null);
		ad.show();
		Log.d("DEBUG", "AuthCodeRegistration showDialog End");
	}

	/**
	 * acrCancel
	 *  キャンセルボタン押下時の処理
	 * @param v 選択ビュー
	 */
	public void acrCancel(View view) {
		Log.d("DEBUG", "AuthCodeRegistration acrCancel Start");
		if(blSave == true) {
			//ボタン連打ロックオン
			blSave = false;
			//キャンセルをクリックされたらRESULT_CANCELEDを返して終了
			Intent intent = new Intent();
			setResult(RESULT_CANCELED,intent);
			endActivity();
		}
		Log.d("DEBUG", "AuthCodeRegistration acrCancel End");
	}

	/**
	 * Activity終了処理
	 *
	 */
	private void endActivity() {
		Log.d("DEBUG", "AuthCodeRegistration endActivity Start");
		//自アクティビティの終了
		finish();
		Log.d("DEBUG", "AuthCodeRegistration endActivity End");
	}

	/**
	 * 画面回転時に呼ばれるサイクル
	 *
	 * @param newConfig 新しい設定値
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d("DEBUG", "AuthCodeRegistration onConfigurationChanged Start");
		super.onConfigurationChanged(newConfig);
		Log.d("DEBUG", "AuthCodeRegistration onConfigurationChanged End");
	}

	/**
	 * 戻るボタンでカレンダー画面へ遷移
	 *
	 * @param kEvent キーイベント情報 
	 * @return 処理を行った場合はtrue
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent kEvent) {
		Log.d("DEBUG", "AuthCodeRegistration dispatchKeyEvent Start");
		//キー押下されたことを確認
		if (kEvent.getAction() == KeyEvent.ACTION_DOWN) {
			//戻るボタンが押されたか確認
			if (kEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				//戻るボタンが押されても何もしない
				return true;
			}
		}
		Log.d("DEBUG", "AuthCodeRegistration dispatchKeyEvent End");
		return super.dispatchKeyEvent(kEvent);
	}
}