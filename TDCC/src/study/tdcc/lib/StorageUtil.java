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


 クラス名：StorageUtil
 内容：外部ストレージ操作に関するユーティリティクラス
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
          0.1/2012.06.21/T.Mashiko
          0.2/2012.07.17/T.Mashiko コメント修正
*/
package study.tdcc.lib;

import java.io.File;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class StorageUtil {

	/**
	 * 外部ストレージの状態確認
	 * 
	 * @return　boolean型の結果(true:マウント済み,false:マウント未実施)
	 */
	public static boolean checkExternalStorageState() {
		Log.d("DEBUG", "StorageUtil checkExternalStorageState Start");
		String strState = Environment.getExternalStorageState();
		Log.d("DEBUG", "StorageUtil checkExternalStorageState End");
		return strState.equals(Environment.MEDIA_MOUNTED);
	}

	/**
	 * 外部ストレージのマウントファイル取得
	 * 
	 * @return　Fileオブジェクト(マウントファイル)
	 */
	public static File getExternalStoragePath(){
		Log.d("DEBUG", "StorageUtil getExternalStoragePath Start");
		if( checkExternalStorageState() ){
			Log.d("DEBUG", "StorageUtil getExternalStoragePath(1) End");
			return Environment.getExternalStorageDirectory();
		}
		Log.d("DEBUG", "StorageUtil getExternalStoragePath(2) End");
		return null;
	}

	/**
	 * 外部ストレージの空き容量取得
	 * 
	 * @return　long(利用可能バイトサイズ)
	 */
	public static long getExternalStorageAvailableSize(){
		Log.d("DEBUG", "StorageUtil getExteranlStorageAvailableSize Start");
		long lgSize = -1;
		if( checkExternalStorageState() ){
			File fPath = getExternalStoragePath();
			if( fPath != null ){
				StatFs sfObj = new StatFs(fPath.getPath());
				long lgBlockSize = sfObj.getBlockSize();
				long lgAvailableBlockSize = sfObj.getAvailableBlocks();
				lgSize = lgBlockSize * lgAvailableBlockSize;
			}
		}
		Log.d("DEBUG", "StorageUtil getExteranlStorageAvailableSize End");
		return lgSize;
	}

	/**
	 * 外部ストレージの総容量取得
	 * 
	 * @return　long(総容量のバイトサイズ)
	 */
	public static long getExteranlStorageSize(){
		Log.d("DEBUG", "StorageUtil getExteranlStorageSize Start");
		long lgSize = -1;
		if( checkExternalStorageState() ){
			File fPath = getExternalStoragePath();
			if( fPath != null ){
				StatFs sfObj = new StatFs(fPath.getPath());
				long lgBlockSize = sfObj.getBlockSize();
				long lgBlockCount = sfObj.getBlockCount();
				lgSize = lgBlockSize * lgBlockCount;
			}
		}
		Log.d("DEBUG", "StorageUtil getExteranlStorageSize End");
		return lgSize;
	}

	/**
	 * 内部ストレージの空き容量取得
	 * 
	 * @return　long(利用可能バイトサイズ)
	 */
	public static long getInteranlStorageAvailableSize(){
		Log.d("DEBUG", "StorageUtil getInteranlStorageAvailableSize Start");
		long lgSize = -1;
		File fPath = Environment.getDataDirectory();
		if( fPath != null ){
			StatFs sfObj = new StatFs(fPath.getPath());
			long lgBlockSize = sfObj.getBlockSize();
			long lgAvailableBlockSize = sfObj.getAvailableBlocks();
			lgSize = lgBlockSize * lgAvailableBlockSize;
		}
		Log.d("DEBUG", "StorageUtil getInteranlStorageAvailableSize End");
		return lgSize;
	}

	/**
	 * 内部ストレージの総容量取得
	 * 
	 * @return　long(総容量のバイトサイズ)
	 */
	public static long getInternalStorageSize(){
		Log.d("DEBUG", "StorageUtil getInternalStorageSize Start");
		long lgSize = -1;
		File fPath = Environment.getDataDirectory();
		if( fPath != null ){
			StatFs fsObj = new StatFs(fPath.getPath());
			long lgBlockSize = fsObj.getBlockSize();
			long lgBlockCount = fsObj.getBlockCount();

			lgSize = lgBlockSize * lgBlockCount;
		}
		Log.d("DEBUG", "StorageUtil getInternalStorageSize End");
		return lgSize;
	}

}