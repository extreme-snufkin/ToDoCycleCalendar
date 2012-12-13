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


 クラス名：FileUtil
 内容：ファイル操作に関するユーティリティクラス
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
          0.1/2012.02.20/T.Mashiko
          0.2/2012.03.12/T.Mashiko
          0.3/2012.06.21/T.Mashiko
*/
package study.tdcc.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import android.util.Log;

public class FileUtil {

	/**
	 * ファイルの存在確認処理
	 * 
	 * @param strInput    確認対象文字列
	 * @return boolean型の結果(存在した場合：true,存在しない場合：false)
	 */
	public static boolean ExecFileExists(String strFilePath) {
		Log.d("DEBUG", "FileUtil ExecFileExists Start");
		boolean blResult = false;
		try {
			File objFile = new File(strFilePath);
			blResult = objFile.exists();
		} catch (SecurityException e) {
			blResult = false;
		}
		Log.d("DEBUG", "FileUtil ExecFileExists End");
		return blResult;
	}

	/**
	 * ファイルの削除処理
	 * 
	 * @param strInput    確認対象文字列
	 * @return boolean型の結果(削除した場合：true, 削除時にエラーが発生した場合：false)
	 */
	public static boolean ExecFileDelete(String strFilePath) {
		Log.d("DEBUG", "FileUtil ExecFileDelete Start");
		boolean blResult = true;
		try {
			File objFile = new File(strFilePath);
			blResult = objFile.delete();
		} catch (SecurityException e) {
			blResult = false;
		}
		Log.d("DEBUG", "FileUtil ExecFileDelete End");
		return blResult;
	}

	/**
	 * ファイルパスの一覧取得処理(非再帰)
	 * 
	 * @param strDirPath    確認したいディレクトリパス
	 * @return ArrayListに格納したファイルパス文字列
	 */
	public static ArrayList<String> getFilePathList(String strDirPath) {
		Log.d("DEBUG", "FileUtil getFilePathList Start");
		ArrayList<String> alResult = new ArrayList<String>();
		if(ExecFileExists(strDirPath)) {
			File fDir = new File(strDirPath);
			File[] files = fDir.listFiles();
			for(File fObj : files){
				alResult.add(fObj.getPath());
			}
		}
		Log.d("DEBUG", "FileUtil getFilePathList End");
		return alResult;
	}

	/**
	 * ファイル名の一覧取得処理(非再帰)
	 * 
	 * @param strDirPath    確認したいディレクトリパス
	 * @return ArrayListに格納したファイルパス文字列
	 */
	public static ArrayList<String> getFileNameList(String strDirPath) {
		Log.d("DEBUG", "FileUtil getFileNameList Start");
		ArrayList<String> alResult = new ArrayList<String>();
		if(ExecFileExists(strDirPath)) {
			File fDir = new File(strDirPath);
			File[] files = fDir.listFiles();
			for(File fObj : files){
				alResult.add(fObj.getName());
			}
		}
		Log.d("DEBUG", "FileUtil getFileNameList End");
		return alResult;
	}

	/**
	 * ファイルサイズ取得処理
	 * 
	 * @param strFilePath    確認したいファイルパス
	 * @return long型ファイルバイトサイズ(ファイルが見つからない場合,-1)
	 */
	public static long getFileSize(String strFilePath) {
		Log.d("DEBUG", "FileUtil getFileSize Start");
		long lgResult = -1l;
		if(ExecFileExists(strFilePath)) {
			File objFile = new File(strFilePath);
			lgResult = objFile.length();
		}
		Log.d("DEBUG", "FileUtil getFileSize End");
		return lgResult;
	}

	/**
	 * ディレクトリ内、合計ファイルサイズ取得処理(非再帰)
	 * ※サブディレクトリのサイズは含まない
	 * 
	 * @param strDirPath    確認したいディレクトリパス
	 * @return long型ファイルバイトサイズ(ファイルが見つからない場合,-1)
	 */
	public static long getDirectoryTotalFileSize(String strDirPath) {
		Log.d("DEBUG", "FileUtil getDirectoryTotalFileSize Start");
		long lgResult = 0l;
		if(ExecFileExists(strDirPath)) {
			File fDir = new File(strDirPath);
			File[] files = fDir.listFiles();
			for(File fObj : files){
				lgResult = lgResult + fObj.length();
			}
		} else {
			lgResult = -1l;
		}
		Log.d("DEBUG", "FileUtil getDirectoryTotalFileSize End");
		return lgResult;
	}

	/**
	 * コピー元のパス[srcPath]から、コピー先のパス[destPath]へ
	 * ファイルのコピーを行います。
	 * コピー処理にはFileChannel#transferToメソッドを利用します。
	 * ※コピー処理終了後、入力・出力のチャネルをクローズします。
	 * 
	 * @param srcPath    コピー元のパス
	 * @param destPath    コピー先のパス
	 * @throws Exception    何らかの入出力処理例外が発生した場合
	 */
	public static void copyTransfer(String srcPath, String destPath) throws Exception {
		Log.d("DEBUG", "FileUtil copyTransfer Start");
		FileChannel srcChannel = new FileInputStream(srcPath).getChannel();
		FileChannel destChannel = new FileOutputStream(destPath).getChannel();
		try {
			srcChannel.transferTo(0, srcChannel.size(), destChannel);
		}finally {
			srcChannel.close();
			destChannel.close();
		}
		Log.d("DEBUG", "FileUtil copyTransfer End");
	}
}