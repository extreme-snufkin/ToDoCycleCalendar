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


 クラス名：InputCheckUtil
 内容：入力値チェック操作に関するユーティリティクラス
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
          0.1/2012.06.21/T.Mashiko
          0.2/2012.07.04/T.Mashiko コンバーター2件追加
          0.3/2012.07.10/T.Mashiko 所要時間(形式：「00.00」)の形式チェック処理追加
          0.4/2012.07.14/T.Mashiko コンバーター2件追加
          0.5/2012.07.17/T.Mashiko ログ修正
          0.6/2012.09.26/T.Mashiko 文字列データサイズチェック追加
*/
package study.tdcc.lib;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import android.util.Log;

public class InputCheckUtil {

	/**
	 * 半角英数字チェック
	 * 
	 * @param strInput    確認対象文字列
	 * @return boolean型の結果(true:OK,false:半角英数字以外)
	 */
	public static boolean alphaNumCheck(String strInput) {
		Log.d("DEBUG", "InputCheckUtil alphaNumCheck Start");
		boolean blResult = strInput.matches("^[a-zA-Z0-9]+$");
		Log.d("DEBUG", "InputCheckUtil alphaNumCheck End");
		return blResult;
	}

	/**
	 * 文字数チェック
	 * 
	 * @param strInput    確認対象文字列
	 * @param intCount    文字数上限値
	 * @return boolean型の結果(true:OK,false:指定文字数より大きい)
	 */
	public static boolean checkCount(String strInput,int intCount) {
		Log.d("DEBUG", "InputCheckUtil checkCount Start");
		boolean blResult = true;
		if(strInput.length() > intCount) {
			blResult = false;
		}
		Log.d("DEBUG", "InputCheckUtil checkCount End");
		return blResult;
	}

	/**
	 * 文字データサイズチェック
	 * 
	 * @param sbInput    確認対象文字列
	 * @param intSize    データサイズ上限値
	 * @return boolean型の結果(true:OK,false:指定サイズより大きい)
	 */
	public static boolean checkSizeCount(StringBuffer sbInput,int intSize) {
		Log.d("DEBUG", "InputCheckUtil checkSizeCount Start");
		boolean blResult = true;
		String strTempText = sbInput.toString();
		int intStringByte = 0;
		try {
			intStringByte = strTempText.getBytes("UTF-8").length;
		} catch (UnsupportedEncodingException e) {
			blResult = false;
		}
		if(intStringByte > intSize) {
			blResult = false;
		}
		Log.d("DEBUG", "InputCheckUtil checkSizeCount End");
		return blResult;
	}

	/**
	 * InputStreamデータのStringコンバーター
	 * 
	 * @param isObj    確認対象ストリーム
	 * @return String型の結果
	 * @throws UnsupportedEncodingException 
	 * @throws IOException 
	 */
	public static String convertStreamToString(InputStream isObj){
		Log.d("DEBUG", "InputCheckUtil convertStreamToString Start");
		try {
			Log.d("DEBUG", "InputCheckUtil convertStreamToString(1) End");
			return new java.util.Scanner(isObj, "UTF-8").useDelimiter("\\A").next();
		} catch (java.util.NoSuchElementException e) {
			Log.d("DEBUG", "InputCheckUtil convertStreamToString(2) End");
			return "";
		}
	}

	/**
	 * Null文字列を空文字に変換する処理
	 * 
	 * @param strInput    確認対象文字列
	 * @return String型の変換文字列
	 */
	public static String convNullKara(String strInput) {
		Log.d("DEBUG", "InputCheckUtil convNullKara Start");
		String strResult = "";
		if(strInput == null) {
			strResult = "";
		} else {
			strResult = strInput;
		}
		Log.d("DEBUG", "InputCheckUtil convNullKara End");
		return strResult;
	}

	/**
	 * 所要時間(形式：「00.00」)の形式チェック処理
	 * @param strInput　チェック元の文字列
	 * @return　boolean型の結果(形式：true,非形式:false)
	 */
	public static boolean checkTimeFormat(String strInput){
		Log.d("DEBUG", "InputCheckUtil checkTimeFormat Start");
		boolean blResult = false;
		if(strInput.matches("^[0-9][0-9].[0-9][0-9]$")){
			blResult = true;
		} else if(strInput.matches("^[0-9].[0-9][0-9]$")) {
			blResult = true;
		} else if(strInput.matches("^[0-9][0-9].[0-9]$")) {
			blResult = true;
		} else if(strInput.matches("^[0-9].[0-9]$")) {
			blResult = true;
		} else if(strInput.matches("^[0-9][0-9]$")) {
			blResult = true;
		} else if(strInput.matches("^[0-9]$")) {
			blResult = true;
		}
		Log.d("DEBUG", "InputCheckUtil checkTimeFormat End");
		return blResult;
	}

	/**
	 * タブコードを空文字に変換する処理
	 * 
	 * @param strInput    確認対象文字列
	 * @return String型の変換文字列
	 */
	public static String convTabKara(String strInput) {
		Log.d("DEBUG", "InputCheckUtil convTabKara Start");
		String strResult = strInput;
		if(!(strResult == null || strResult.equals(""))) {
			strResult = strResult.replaceAll("\\t", "");
		}
		Log.d("DEBUG", "InputCheckUtil convTabKara End");
		return strResult;
	}

	/**
	 * 改行コードを空文字に変換する処理
	 * 
	 * @param strInput    確認対象文字列
	 * @return String型の変換文字列
	 */
	public static String convKaigyouKara(String strInput) {
		Log.d("DEBUG", "InputCheckUtil convKaigyouKara Start");
		String strResult = strInput;
		if(!(strResult == null || strResult.equals(""))) {
			strResult = strResult.replaceAll("\\n", "");
		}
		Log.d("DEBUG", "InputCheckUtil convKaigyouKara End");
		return strResult;
	}

}