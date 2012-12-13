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


 クラス名：SummaryTableListChildRow
 内容：集計画面ExpandableListViewの子RowデータBean
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
			0.1/2012.07.13/T.Mashiko

*/
package study.tdcc.bean;

public class SummaryTableListChildRow {
	//サブカテゴリコード
	private String strSubcategoryCode;
	//日付
	private String strDate;
	//所要時間
	private String strTime;

	public String getStrSubcategoryCode() {
		return strSubcategoryCode;
	}
	public void setStrSubcategoryCode(String strSubcategoryCode) {
		this.strSubcategoryCode = strSubcategoryCode;
	}
	public String getStrDate() {
		return strDate;
	}
	public void setStrDate(String strDate) {
		this.strDate = strDate;
	}
	public String getStrTime() {
		return strTime;
	}
	public void setStrTime(String strTime) {
		this.strTime = strTime;
	}

}