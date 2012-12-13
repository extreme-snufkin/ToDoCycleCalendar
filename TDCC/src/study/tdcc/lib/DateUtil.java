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


 クラス名：DateUtil
 内容：日付情報操作に関するユーティリティクラス
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
 		0.1/2012.05.13/T.Mashiko
 		0.2/2012.06.04/T.Mashiko
 		0.3/2012.06.05/T.Mashiko
 		0.4/2012.06.11/T.Mashiko
 		0.5/2012.06.20/T.Mashiko
 		0.6/2012.07.17/T.Mashiko ロギング追加
*/
package study.tdcc.lib;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import study.tdcc.R;

import android.content.Context;
import android.util.Log;

public class DateUtil {
	//日付のみのフォーマット
	public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	//年月のみのフォーマット
	public final static SimpleDateFormat YEARMONTH_FORMAT = new SimpleDateFormat("yyyy-MM");
	//時刻のみのフォーマット
	public final static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
	//月のみのフォーマット
	public final static SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("MM");
	//UTC時刻（タイムゾーン無し）のフォーマット
	public static SimpleDateFormat UTCDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	//RFC822に従ったミリ秒単位の時刻フォーマット
	public static SimpleDateFormat RFC822MilliDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	//1時間の分数
	public final static int HOUR_BY_MINUTES = 60;
	//1分の秒数
	public final static int MINUTE_BY_SECONDS = 60;
	//1秒のミリ秒数
	public final static int SECOND_BY_MILLI = 1000;
	//1分のミリ秒数
	public final static int MINUTE_BY_MILLI = MINUTE_BY_SECONDS*SECOND_BY_MILLI;
	//1週間の日数
	public final static int DAYS_OF_WEEK = 7;
	//yyyy-MM文字列に追加するdd
	public final static String FIRST_DAY = "-01";
	//年月日分割記号
	public final static String HYPHEN_MARK = "-";
	
	/**
	 * タイムゾーンの文字列を生成する
	 *  RFC 3339で使用するため時、分の区切りに：を入れる
	 * @param tz タイムゾーン
	 * @return タイムゾーン文字列
	 *  例：+9時間の場合   +09:00
	 *      -3時間の場合   -03:00
	 *           0の場合   Z
	 */
	public static String timeZoneToString(TimeZone tz){
		Log.d("DEBUG", "DateUtil timeZoneToString Start");
		//カレンダークラスのインスタンスを作成
		Calendar calObj = Calendar.getInstance();
		String strDir = null;
		// TimeZoneからミリ秒単位のUTCからのずれを取得
		int intOffSet = tz.getRawOffset();
		// 正負と値の分離
		if(intOffSet < 0){
			// offsetがマイナスなら符号は-
			// ずれは正にしておく
			intOffSet = -intOffSet;
			strDir = "-";
		}else if(intOffSet > 0){
			// オフセットがプラスなら符号は＋
			strDir = "+";
		}else if(intOffSet == 0){
			// UTCに一致する場合はZを返す
			Log.d("DEBUG", "DateUtil timeZoneToString(1) End");
			return "Z";
		}
		//時、分を計算しCalendarにセット
		int intOffSetMin = intOffSet/MINUTE_BY_MILLI;
		int intOffSetHour = intOffSetMin/HOUR_BY_MINUTES;
		intOffSetMin = intOffSetMin%60;
		calObj.set(Calendar.HOUR_OF_DAY, intOffSetHour);
		calObj.set(Calendar.MINUTE, intOffSetMin);
		Log.d("DEBUG", "DateUtil timeZoneToString(2) End");
		//正負の符号を追加した文字列を返す
		return strDir + TIME_FORMAT.format(calObj.getTime());
	}

	/**
	 * 日付、時刻の文字列からDB に保存するための時刻文字列に変換する
	 * @param date　変換元の日付
	 * @param time　変換もとの時刻
	 * @return　RFC 3339形式の日時文字列
	 */
	public static String toDBDateString(String date,String time){
		Log.d("DEBUG", "DateUtil toDBDateString(1) Start");
		//追加可能な文字列クラスStringBuilderを作成
		StringBuilder sbObj = new StringBuilder();
		sbObj.append(date);
		sbObj.append("T");
		sbObj.append(time);
		sbObj.append(":00.000");
		//TimeZone文字列を作成し追加
		sbObj.append(timeZoneToString(TimeZone.getDefault()));
		Log.d("DEBUG", "DateUtil toDBDateString(1) End");
		return sbObj.toString();
	}

	/**
	 * CalendarからDBに格納するための文字列を作成する
	 * @param cal 変換もとの値
	 * @return 日時文字列
	 */
	public static String toDBDateString(Calendar cal){
		Log.d("DEBUG", "DateUtil toDBDateString(2) Start");
		//RFC 822形式で文字列を生成
		String strDate = RFC822MilliDateFormat.format(cal.getTime());
		//タイムゾーン部分を処理
		if(strDate.matches(".+[+-][0-9]{4}$")){
			strDate = strDate.replaceAll("([+-][0-9]{2})([0-9]{2})","$1:$2");
		}
		Log.d("DEBUG", "DateUtil toDBDateString(2) End");
		return strDate;
	}

	/**
	 * 日時文字列からカレンダーへの変換
	 * @param startTime 変換もとの日時文字列
	 * @return gcalObj 
	 */
	public static GregorianCalendar toCalendar(String startTime) {
		Log.d("DEBUG", "DateUtil toCalendar Start");
		GregorianCalendar gcalObj = new GregorianCalendar();
		if(startTime == null){
			Log.d("DEBUG", "DateUtil toCalendar(1) End");
			return gcalObj;
		}
		//文字列を数値以外の文字で分割して切り分ける
		String[] strs = startTime.split("[^0-9]");
		TimeZone timeZone = TimeZone.getDefault();
		if(startTime.matches("^[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]$")){
			//日付のみの文字列　時刻を00:00に設定
			gcalObj.set(Calendar.YEAR, Integer.valueOf(strs[0]));
			gcalObj.set(Calendar.MONTH,Integer.valueOf(strs[1])-1);
			gcalObj.set(Calendar.DAY_OF_MONTH,Integer.valueOf(strs[2]));
			gcalObj.set(Calendar.HOUR_OF_DAY,0);
			gcalObj.set(Calendar.MINUTE,0);
			gcalObj.set(Calendar.SECOND,0);
			gcalObj.set(Calendar.MILLISECOND,0);
			gcalObj.setTimeZone(timeZone);
		}else{
			//日時文字列　数値文字列を数値に変換して設定
			gcalObj.set(Calendar.YEAR, Integer.valueOf(strs[0]));
			gcalObj.set(Calendar.MONTH,Integer.valueOf(strs[1])-1);
			gcalObj.set(Calendar.DAY_OF_MONTH,Integer.valueOf(strs[2]));
			gcalObj.set(Calendar.HOUR_OF_DAY,Integer.valueOf(strs[3]));
			gcalObj.set(Calendar.MINUTE,Integer.valueOf(strs[4]));
			gcalObj.set(Calendar.SECOND,Integer.valueOf(strs[5]));
			gcalObj.set(Calendar.MILLISECOND,Integer.valueOf(strs[6]));
			//TimeZoneのパターンによる処理
			if(startTime.matches(".+Z$")){
				//UTC
				timeZone.setRawOffset(0);
			}else if(startTime.matches(".+\\+[0-9][0-9]:[0-9][0-9]$")){
				//オフセットがマイナス
				timeZone.setRawOffset((Integer.valueOf(strs[7])*HOUR_BY_MINUTES+Integer.valueOf(strs[8]))*MINUTE_BY_MILLI);
			}else if(startTime.matches(".+-[0-9][0-9]:[0-9][0-9]$")){
				//オフセットがプラス
				timeZone.setRawOffset(-(Integer.valueOf(strs[7])*HOUR_BY_MINUTES+Integer.valueOf(strs[8]))*MINUTE_BY_MILLI);
			}
			//TimeZoneを設定
			gcalObj.setTimeZone(timeZone);
		}
		Log.d("DEBUG", "DateUtil toCalendar(2) End");
		return gcalObj;
	}

	/**
	 * カレンダー日時データをUTC（協定世界時）文字列に変換
	 * 日本時間と9時間のずれあり
	 * @param cal
	 * @return
	 */
	public static String toUTCString(Calendar cal){
		Log.d("DEBUG", "DateUtil toUTCString Start");
		UTCDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Log.d("DEBUG", "DateUtil toUTCString End");
		return UTCDateFormat.format(cal.getTime())+"Z";
	}

	/**
	 * 時刻の文字列(00:00)から時間と分に分割した文字列配列に変換する
	 * @param time　変換もとの時刻
	 * @return　文字列配列[時間,分]
	 */
	public static String[] toDivideTime(String time){
		Log.d("DEBUG", "DateUtil toDivideTime Start");		
		String[] strArrayResult = time.split("[^0-9]");
		Log.d("DEBUG", "DateUtil toDivideTime End");
		return strArrayResult;
	}

	/**
	 * 年月の文字列(YYYY/MM)から年と月に分割した文字列配列に変換する
	 * @param time　変換もとの時刻
	 * @return　文字列配列[年,月]
	 */
	public static String[] toDivideYearMonth(String yearMonth){
		Log.d("DEBUG", "DateUtil toDivideYearMonth Start");
		String[] strArrayResult = yearMonth.split("[^0-9]");
		Log.d("DEBUG", "DateUtil toDivideYearMonth End");
		return strArrayResult;
	}

	/**
	 * GregorianCalendarから曜日の文字列に変換する
	 * @param gcObj　変換もとの時刻
	 * @return　曜日文字列
	 */
	public static String toDayOfWeek(Context context, GregorianCalendar gcObj){
		Log.d("DEBUG", "DateUtil toDayOfWeek Start");
		String strResult = "";
		switch (gcObj.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.SUNDAY: strResult = context.getString(R.string.sunday); break;
			case Calendar.MONDAY: strResult = context.getString(R.string.monday); break;
			case Calendar.TUESDAY: strResult = context.getString(R.string.tuesday); break;
			case Calendar.WEDNESDAY: strResult = context.getString(R.string.wednesday); break;
			case Calendar.THURSDAY: strResult = context.getString(R.string.thursday); break;
			case Calendar.FRIDAY: strResult = context.getString(R.string.friday); break;
			case Calendar.SATURDAY: strResult = context.getString(R.string.saturday); break;
		}
		Log.d("DEBUG", "DateUtil toDayOfWeek End");
		return strResult;
	}
	
	/**
	 * 年の文字列(YYYY)が4桁に満たなかった時の文字列変換
	 * @param year　変換もとの時刻
	 * @return　文字列[年]
	 */
	public static String toAddZeroYear(String year){
		Log.d("DEBUG", "DateUtil toAddZeroYear Start");
		String strResult = year;
		int intKetakazu = strResult.length();
		for(int intCt=0;intCt<(4 - intKetakazu);intCt++) {
			strResult = "0" + strResult;
		}
		Log.d("DEBUG", "DateUtil toAddZeroYear End");
		return strResult;
	}

	/**
	 * 月の文字列(MM)が2桁に満たなかった時の文字列変換
	 * @param month　変換もとの時刻
	 * @return　文字列[月]
	 */
	public static String toAddZeroMonth(String month){
		Log.d("DEBUG", "DateUtil toAddZeroMonth Start");
		String strResult = month;
		int intKetakazu = strResult.length();
		for(int intCt=0;intCt<(2 - intKetakazu);intCt++) {
			strResult = "0" + strResult;
		}
		Log.d("DEBUG", "DateUtil toAddZeroMonth End");
		return strResult;
	}

	/**
	 * GregorianCalendarからToDoテーブル(年月日YYYYMMDD)の数値に変換する
	 * @param gcObj　変換もとの時刻
	 * @return　Long型の年月日データ
	 */
	public static Long convToDoYMD(GregorianCalendar gcObj){
		Log.d("DEBUG", "DateUtil convToDoYMD Start");
		Long lgResult = 0l;
		StringBuilder sbDateText = new StringBuilder();		
		sbDateText.append(toAddZeroYear(Integer.toString(gcObj.get(Calendar.YEAR))));
		sbDateText.append(toAddZeroMonth(Integer.toString(gcObj.get(Calendar.MONTH)+1)));
		sbDateText.append(toAddZeroMonth(Integer.toString(gcObj.get(Calendar.DAY_OF_MONTH))));
		lgResult = Long.parseLong(sbDateText.toString());
		Log.d("DEBUG", "DateUtil convToDoYMD End");
		return lgResult;
	}

	/**
	 * 年月日(YYYYMMDD)の数値からDATE_FORMAT(YYYY-MM-DD)の文字列に変換する
	 * @param lgYMD　変換もとの数値
	 * @return　String型の年月日データ
	 */
	public static String convBaseYMD(long lgYMD){
		Log.d("DEBUG", "DateUtil convBaseYMD Start");
		String strTempDate = String.valueOf(lgYMD);
		StringBuilder sbDateText = new StringBuilder();		
		sbDateText.append(strTempDate.substring(0, 4));
		sbDateText.append(HYPHEN_MARK);
		sbDateText.append(strTempDate.substring(4, 6));
		sbDateText.append(HYPHEN_MARK);
		sbDateText.append(strTempDate.substring(6));
		Log.d("DEBUG", "DateUtil convBaseYMD End");
		return sbDateText.toString();
	}

	/**
	 * 年月日の文字列(形式：「YYYY-MM-DD」)の形式チェック処理
	 * @param strYMD　チェック元の年月日
	 * @return　boolean型の結果(形式：true,非形式:false)
	 */
	public static boolean checkYMDFormat(String strYMD){
		Log.d("DEBUG", "DateUtil checkYMDFormat Start");
		boolean blResult = false;
		if(strYMD.matches("^[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]$")){
			blResult = true;
		}
		Log.d("DEBUG", "DateUtil checkYMDFormat End");
		return blResult;
	}

	/**
	 * 年月の文字列(形式：「YYYY-MM」)の形式チェック処理
	 * @param strYM　チェック元の年月
	 * @return　boolean型の結果(形式：true,非形式:false)
	 */
	public static boolean checkYMFormat(String strYM){
		Log.d("DEBUG", "DateUtil checkYMFormat Start");
		boolean blResult = false;
		if(strYM.matches("^[0-9][0-9][0-9][0-9]-[0-9][0-9]$")){
			blResult = true;
		}
		Log.d("DEBUG", "DateUtil checkYMFormat End");
		return blResult;
	}

	/**
	 * 時分の文字列(形式：「HH:mm」)の形式チェック処理
	 * @param strHM　チェック元の時分
	 * @return　boolean型の結果(形式：true,非形式:false)
	 */
	public static boolean checkHMFormat(String strHM){
		Log.d("DEBUG", "DateUtil checkHMFormat Start");
		boolean blResult = false;
		if(strHM.matches("^[0-9][0-9]:[0-9][0-9]$")){
			blResult = true;
		}
		Log.d("DEBUG", "DateUtil checkHMFormat End");
		return blResult;
	}

	/**
	 * 年月日時分、或いは、年月日文字列から
	 * GregorianCalendarオブジェクトを作成し、
	 * エポックからの経過時間をLong型のミリ秒で返す
	 * @param strYMDHm　変換もとの年月日時分文字列
	 * @return　Long型のミリ秒
	 */
	public static long convMSec(String strYMDHm){
		Log.d("DEBUG", "DateUtil convMSec Start");
		long lgResult = 0l;
		GregorianCalendar gcalObj = new GregorianCalendar();
		if(strYMDHm == null || strYMDHm.equals("")){
			Log.d("DEBUG", "DateUtil convMSec(1) End");
			return lgResult;
		}
		//文字列を数値以外の文字で分割して切り分ける
		String[] strs = strYMDHm.split("[^0-9]");
		TimeZone timeZone = TimeZone.getDefault();
		if(strYMDHm.matches("^[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9] [0-9][0-9]:[0-9][0-9]$")){
			//日付と時間分のみの文字列　秒とミリ秒を00.000に設定
			gcalObj.set(Calendar.YEAR, Integer.valueOf(strs[0]));
			gcalObj.set(Calendar.MONTH,Integer.valueOf(strs[1])-1);
			gcalObj.set(Calendar.DAY_OF_MONTH,Integer.valueOf(strs[2]));
			gcalObj.set(Calendar.HOUR_OF_DAY, Integer.valueOf(strs[3]));
			gcalObj.set(Calendar.MINUTE, Integer.valueOf(strs[4]));
			gcalObj.set(Calendar.SECOND,0);
			gcalObj.set(Calendar.MILLISECOND,0);
			gcalObj.setTimeZone(timeZone);
			lgResult = gcalObj.getTimeInMillis();
		} else if(strYMDHm.matches("^[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]$")) {
			//日付のみの文字列　時刻を00:00に設定
			gcalObj.set(Calendar.YEAR, Integer.valueOf(strs[0]));
			gcalObj.set(Calendar.MONTH,Integer.valueOf(strs[1])-1);
			gcalObj.set(Calendar.DAY_OF_MONTH,Integer.valueOf(strs[2]));
			gcalObj.set(Calendar.HOUR_OF_DAY, 0);
			gcalObj.set(Calendar.MINUTE, 0);
			gcalObj.set(Calendar.SECOND,0);
			gcalObj.set(Calendar.MILLISECOND,0);
			gcalObj.setTimeZone(timeZone);
			lgResult = gcalObj.getTimeInMillis();
		} else if(strYMDHm.matches("^[0-9][0-9][0-9][0-9]-[0-9][0-9]$")){
			//年月のみの文字列　日を1日、時刻を00:00に設定
			gcalObj.set(Calendar.YEAR, Integer.valueOf(strs[0]));
			gcalObj.set(Calendar.MONTH,Integer.valueOf(strs[1])-1);
			gcalObj.set(Calendar.DAY_OF_MONTH,1);
			gcalObj.set(Calendar.HOUR_OF_DAY, 0);
			gcalObj.set(Calendar.MINUTE, 0);
			gcalObj.set(Calendar.SECOND,0);
			gcalObj.set(Calendar.MILLISECOND,0);
			gcalObj.setTimeZone(timeZone);
			lgResult = gcalObj.getTimeInMillis();
		}
		Log.d("DEBUG", "DateUtil convMSec(2) End");
		return lgResult;
	}
}