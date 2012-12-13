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


 クラス名：ScheduleListRow
 内容：スケジュール(スケジュールタブ)画面ListViewのRowデータBean
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
			0.1/2012.06.13/T.Mashiko
			0.2/2012.06.15/T.Mashiko
			0.3/2012.07.04/T.Mashiko スケジュール削除処理修正
*/
package study.tdcc.bean;

public class ScheduleListRow {
	//終日
	private String strAllDay;
	//開始時間
	private String strStartTime;
	//終了時間
	private String strEndTime;
	//タイトル
	private String strTitle;
	//場所
	private String strPlace;
	//説明
	private String strContent;
	//開始日時(数値)
	private long lgStartTime;
	//端末アラーム対象フラグ
	private long lgAlarmFlag;
	//スケジュールキーID
	private long lgID;
	//カレンダーID
	private String strCalendarID;

	public String getStrAllDay() {
		return strAllDay;
	}
	public void setStrAllDay(String strAllDay) {
		this.strAllDay = strAllDay;
	}
	public String getStrStartTime() {
		return strStartTime;
	}
	public void setStrStartTime(String strStartTime) {
		this.strStartTime = strStartTime;
	}
	public String getStrEndTime() {
		return strEndTime;
	}
	public void setStrEndTime(String strEndTime) {
		this.strEndTime = strEndTime;
	}
	public String getStrTitle() {
		return strTitle;
	}
	public void setStrTitle(String strTitle) {
		this.strTitle = strTitle;
	}
	public String getStrPlace() {
		return strPlace;
	}
	public void setStrPlace(String strPlace) {
		this.strPlace = strPlace;
	}
	public String getStrContent() {
		return strContent;
	}
	public void setStrContent(String strContent) {
		this.strContent = strContent;
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
	public long getLgID() {
		return lgID;
	}
	public void setLgID(long lgID) {
		this.lgID = lgID;
	}
	public String getStrCalendarID() {
		return strCalendarID;
	}
	public void setStrCalendarID(String strCalendarID) {
		this.strCalendarID = strCalendarID;
	}

}