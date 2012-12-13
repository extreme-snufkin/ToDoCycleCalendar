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


 クラス名：GdataEvent
 内容：GdataEventデータBean
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
			0.1/2012.06.27/T.Mashiko
			0.2/2012.07.02/T.Mashiko
			0.3/2012.07.04/T.Mashiko 端末アラーム対象フラグ対応
*/
package study.tdcc.bean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import study.tdcc.lib.DateUtil;

public class GDataEvent {
	//ID
	private long lgId = 0l;
	//削除フラグ
	private long lgDeleted = 0l;
	//更新フラグ
	private long lgModified = 0l;
	//タイトル
	private String strTitle = null;
	//場所
	private String strWhere = null;
	//開始日時
	private GregorianCalendar gcStart = null;
	//終了日時
	private GregorianCalendar gcEnd = null;
	//説明
	private String strContent = null;
	//イベント作成日時
	private GregorianCalendar gcPublished = null;
	//イベント最終更新日時
	private GregorianCalendar gcUpdated = null;
	//カテゴリ
	private String strCategory = null;
	//編集URL
	private String strEditUrl = null;
	//状態
	private String strEventStatus = null;
	//カレンダーID
	private String strEventId = null;
	//ETAG
	private String strEtag = null;
	//繰り返しイベント(当該アプリでは未対応)
	private String strRecurrence = null;
	//アラーム(当該アプリでは未対応)
	private HashMap<String,ArrayList<String>> hmAlarm = null;
	//端末アラーム対象フラグ
	private long lgAlarmFlag = 0l;


	public long getLgId() {
		return lgId;
	}
	public void setLgId(long lgId) {
		this.lgId = lgId;
	}
	public long getLgDeleted() {
		return lgDeleted;
	}
	public void setLgDeleted(long lgDeleted) {
		this.lgDeleted = lgDeleted;
	}
	public long getLgModified() {
		return lgModified;
	}
	public void setLgModified(long lgModified) {
		this.lgModified = lgModified;
	}
	public String getStrTitle() {
		return strTitle;
	}
	public void setStrTitle(String strTitle) {
		this.strTitle = strTitle;
	}
	public String getStrWhere() {
		return strWhere;
	}
	public void setStrWhere(String strWhere) {
		this.strWhere = strWhere;
	}
	public Calendar getGcStart() {
		return gcStart;
	}
	public String getStartString(){
		return DateUtil.toDBDateString(gcStart);
	}
	public String getStartDateString(){
		return DateUtil.DATE_FORMAT.format(gcStart.getTime());
	}
	public String getStartTimeString(){
		return DateUtil.TIME_FORMAT.format(gcStart.getTime());
	}
	public void setGcStart(GregorianCalendar gcStart) {
		this.gcStart = gcStart;
	}
	public void setGcStart(String strStart) {
		this.gcStart = DateUtil.toCalendar(strStart);
	}
	public Calendar getGcEnd() {
		return gcEnd;
	}
	public String getEndString(){
		return DateUtil.toDBDateString(gcEnd);
	}
	public String getEndDateString(){
		return DateUtil.DATE_FORMAT.format(gcEnd.getTime());
	}
	public String getEndTimeString(){
		return DateUtil.TIME_FORMAT.format(gcEnd.getTime());
	}
	public void setGcEnd(GregorianCalendar gcEnd) {
		this.gcEnd = gcEnd;
	}
	public void setGcEnd(String strEnd){
		this.gcEnd = DateUtil.toCalendar(strEnd);
	}
	public String getStrContent() {
		return strContent;
	}
	public void setStrContent(String strContent) {
		this.strContent = strContent;
	}
	public GregorianCalendar getGcPublished() {
		return gcPublished;
	}
	public String getPublishedString() {
		return DateUtil.toDBDateString(gcPublished);
	}
	public void setGcPublished(GregorianCalendar gcPublished) {
		this.gcPublished = gcPublished;
	}
	public void setGcPublished(String strDate) {
		this.gcPublished = DateUtil.toCalendar(strDate);
	}
	public GregorianCalendar getGcUpdated() {
		return gcUpdated;
	}
	public String getUpdatedString() {
		return DateUtil.toDBDateString(gcUpdated);
	}
	public void setGcUpdated(GregorianCalendar gcUpdated) {
		this.gcUpdated = gcUpdated;
	}
	public void setGcUpdated(String strDate) {
		this.gcUpdated = DateUtil.toCalendar(strDate);
	}
	public String getStrCategory() {
		return strCategory;
	}
	public void setStrCategory(String strCategory) {
		this.strCategory = strCategory;
	}
	public String getStrEditUrl() {
		return strEditUrl;
	}
	public void setStrEditUrl(String strEditUrl) {
		this.strEditUrl = strEditUrl;
	}
	public String getStrEventStatus() {
		return strEventStatus;
	}
	public void setStrEventStatus(String strEventStatus) {
		this.strEventStatus = strEventStatus;
	}
	public boolean isConfirmed(){
		return strEventStatus.contains("confirmed");
	}

	public boolean isCanceled(){
		return strEventStatus.contains("canceled");
	}

	public boolean isTentative(){
		return strEventStatus.contains("tentative");
	}
	public String getStrEventId() {
		return strEventId;
	}
	/**
	 * EventIdは
	 * http://www.google.com/calendar/feeds/default/events/xxxxxxxxxxxxxxxxxxxxxxxxx
	 * の様にURLの形になっていますが実際には、xxxxの部分のみがIdなので、
	 * この部分のみを取り出します。
	 * 
	 * @param calendarId
	 */
	public void setStrEventId(String strCalendarId) {
		if(strCalendarId != null && strCalendarId.contains("/")){
			String[] splited = strCalendarId.split("/");
			this.strEventId = splited[splited.length - 1];
		}else{
			this.strEventId = strCalendarId;
		}
	}
	public String getStrEtag() {
		return strEtag;
	}
	public void setStrEtag(String strEtag) {
		this.strEtag = strEtag;
	}
	public String getStrRecurrence() {
		return strRecurrence;
	}
	public void setStrRecurrence(String strRecurrence) {
		this.strRecurrence = strRecurrence;
	}
	public HashMap<String, ArrayList<String>> getHmAlarm() {
		return hmAlarm;
	}
	public void setHmAlarm(HashMap<String, ArrayList<String>> hmAlarm) {
		this.hmAlarm = hmAlarm;
	}

	/**
	 * hmAlarmに値を追加する。
	 *
	 * @param String key    キー
	 * @param String value  値
	 */
	public void addToAlarmMap(String key,String value) {
		if(key == null || key.equals("")){
			return ;
		}
		if(value == null){
			value = "";
		}
		if(hmAlarm == null){
			// マップが存在しない場合はHashMapのインスタンスを作成
			hmAlarm = new HashMap<String,ArrayList<String>>();
		}
		if(hmAlarm.containsKey(key)){
			// MapにKeyが存在したら、そのKeyに値を追加
			hmAlarm.get(key).add(value);
		}else{
			// MapにKeyが存在しないのでKeyとListのセットを追加する。
			ArrayList<String>alObj = new ArrayList<String>();
			alObj.add(value);
			hmAlarm.put(key, alObj);
		}
	}

	/**
	 * AlarmMapを文字列で取り出す
	 *
	 * @return String Alarmの値を持った文字列
	 */
	public String getAlarmMapString(){
		if(hmAlarm == null || hmAlarm.isEmpty()){
			return "";
		}
		StringBuilder sbObj = new StringBuilder();
		Set<String> keys = hmAlarm.keySet();
		for(String strKeyObj : keys){
			List<String>values = hmAlarm.get(strKeyObj);
			for(String strValueObj : values){
				sbObj.append("" + strKeyObj + ":" + strValueObj + ",");
			}
		}
		return sbObj.toString();
	}

	/**
	 * Alarmの文字列からMapに値を設定する
	 *
	 * @param String alarm情報の文字列
	 */
	public void setAlarmMap(String strAlarm){
		if(strAlarm == null || strAlarm.equals("")){
			return;
		}
		String[] strArrayPairs = strAlarm.split(",");
		for(String strObj : strArrayPairs){
			if(!strObj.equals("")){
				String[] strArrayKV = strObj.split(":");
				addToAlarmMap(strArrayKV[0],strArrayKV[1]);
			}
		}
	}

	public long getLgAlarmFlag() {
		return lgAlarmFlag;
	}
	public void setLgAlarmFlag(long lgAlarmFlag) {
		this.lgAlarmFlag = lgAlarmFlag;
	}

}