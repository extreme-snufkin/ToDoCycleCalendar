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


 クラス名：ScreenTransitionData
 内容：画面遷移インテントデータBean
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
			0.1/2012.05.23/T.Mashiko
			0.2/2012.06.06/T.Mashiko インテントLong型修正

*/
package study.tdcc.bean;

public class ScreenTransitionData {
	//カレンダー年月
	private String strCalendarYearMonth;
	//選択年月日
	private String strCalendarYearMonthDay;
	//選択元ユーザーインターフェースID
	private String strUserInterfaceId;
	//キーID
	private long lgKeyId;

	public String getStrCalendarYearMonth() {
		return strCalendarYearMonth;
	}
	public void setStrCalendarYearMonth(String strCalendarYearMonth) {
		this.strCalendarYearMonth = strCalendarYearMonth;
	}
	public String getStrCalendarYearMonthDay() {
		return strCalendarYearMonthDay;
	}
	public void setStrCalendarYearMonthDay(String strCalendarYearMonthDay) {
		this.strCalendarYearMonthDay = strCalendarYearMonthDay;
	}
	public String getStrUserInterfaceId() {
		return strUserInterfaceId;
	}
	public void setStrUserInterfaceId(String strUserInterfaceId) {
		this.strUserInterfaceId = strUserInterfaceId;
	}
	public long getLgKeyId() {
		return lgKeyId;
	}
	public void setLgKeyId(long lgKeyId) {
		this.lgKeyId = lgKeyId;
	}
	
}
