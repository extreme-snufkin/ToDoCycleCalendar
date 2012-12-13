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


 クラス名：ShareScheduleListRow
 内容：スケジュール(メインタブ)画面 共有機能のRowデータBean
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
			0.1/2012.07.15/T.Mashiko
*/
package study.tdcc.bean;

public class ShareScheduleListRow {
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

}