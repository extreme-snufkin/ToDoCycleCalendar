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


 クラス名：Schedule
 内容：ScheduleデータBean
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
			0.1/2012.06.12/T.Mashiko

*/
package study.tdcc.bean;

public class Schedule {
	//キーID 
	private long lgId;
	//削除フラグ
	private long lgDeleteFlag;
	//更新フラグ
	private long lgModified;
	//アラーム
	private long lgAlarm;
	//アラームリスト
	private String strAlarmList;
	//タイトル
	private String strTitle;
	//説明
	private String strContent;
	//場所
	private String strGDWhere;
	//終了日時
	private String strGDWhenEndTime;
	//開始日時
	private String strGDWhenStartTime;
	//作成日時
	private String strPublished;
	//更新日時
	private String strUpdated;
	//カテゴリ
	private String strCategory;
	//編集URL
	private String strEditUrl;
	//状態
	private String strGDEventStatus;
	//カレンダーID
	private String strCalendarId;
	//ETAG
	private String strEtag;
	//終了日時(数値)
	private long lgEndTime;
	//開始日時(数値)
	private long lgStartTime;
	//端末アラーム対象フラグ
	private long lgAlarmFlag;

	public long getLgId() {
		return lgId;
	}
	public void setLgId(long lgId) {
		this.lgId = lgId;
	}
	public long getLgDeleteFlag() {
		return lgDeleteFlag;
	}
	public void setLgDeleteFlag(long lgDeleteFlag) {
		this.lgDeleteFlag = lgDeleteFlag;
	}
	public long getLgModified() {
		return lgModified;
	}
	public void setLgModified(long lgModified) {
		this.lgModified = lgModified;
	}
	public long getLgAlarm() {
		return lgAlarm;
	}
	public void setLgAlarm(long lgAlarm) {
		this.lgAlarm = lgAlarm;
	}
	public String getStrAlarmList() {
		return strAlarmList;
	}
	public void setStrAlarmList(String strAlarmList) {
		this.strAlarmList = strAlarmList;
	}
	public String getStrTitle() {
		return strTitle;
	}
	public void setStrTitle(String strTitle) {
		this.strTitle = strTitle;
	}
	public String getStrContent() {
		return strContent;
	}
	public void setStrContent(String strContent) {
		this.strContent = strContent;
	}
	public String getStrGDWhere() {
		return strGDWhere;
	}
	public void setStrGDWhere(String strGDWhere) {
		this.strGDWhere = strGDWhere;
	}
	public String getStrGDWhenEndTime() {
		return strGDWhenEndTime;
	}
	public void setStrGDWhenEndTime(String strGDWhenEndTime) {
		this.strGDWhenEndTime = strGDWhenEndTime;
	}
	public String getStrGDWhenStartTime() {
		return strGDWhenStartTime;
	}
	public void setStrGDWhenStartTime(String strGDWhenStartTime) {
		this.strGDWhenStartTime = strGDWhenStartTime;
	}
	public String getStrPublished() {
		return strPublished;
	}
	public void setStrPublished(String strPublished) {
		this.strPublished = strPublished;
	}
	public String getStrUpdated() {
		return strUpdated;
	}
	public void setStrUpdated(String strUpdated) {
		this.strUpdated = strUpdated;
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
	public String getStrGDEventStatus() {
		return strGDEventStatus;
	}
	public void setStrGDEventStatus(String strGDEventStatus) {
		this.strGDEventStatus = strGDEventStatus;
	}
	public String getStrCalendarId() {
		return strCalendarId;
	}
	public void setStrCalendarId(String strCalendarId) {
		this.strCalendarId = strCalendarId;
	}
	public String getStrEtag() {
		return strEtag;
	}
	public void setStrEtag(String strEtag) {
		this.strEtag = strEtag;
	}
	public long getLgEndTime() {
		return lgEndTime;
	}
	public void setLgEndTime(long lgEndTime) {
		this.lgEndTime = lgEndTime;
	}
	public long getLgStartTime() {
		return lgStartTime;
	}
	public void setLgStartTime(long lgStartTime) {
		this.lgStartTime = lgStartTime;
	}
	public long getLgAlarmFlag() {
		return lgAlarmFlag;
	}
	public void setLgAlarmFlag(long lgAlarmFlag) {
		this.lgAlarmFlag = lgAlarmFlag;
	}
}
