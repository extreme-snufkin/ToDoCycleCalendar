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


 クラス名：ShareSummaryTableListRow
 内容：集計画面共有機能の親RowデータBean
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
			0.1/2012.07.14/T.Mashiko
*/
package study.tdcc.bean;

import java.util.ArrayList;

public class ShareSummaryTableListRow {
	//サブカテゴリコード
	private String strSubcategoryCode;
	//サブカテゴリ名
	private String strSubcategoryName;
	//合計時間
	private String strSummaryTime;
	//明細
	private ArrayList<ShareTATListRow> alSTATLR;

	public String getStrSubcategoryCode() {
		return strSubcategoryCode;
	}
	public void setStrSubcategoryCode(String strSubcategoryCode) {
		this.strSubcategoryCode = strSubcategoryCode;
	}
	public String getStrSubcategoryName() {
		return strSubcategoryName;
	}
	public void setStrSubcategoryName(String strSubcategoryName) {
		this.strSubcategoryName = strSubcategoryName;
	}
	public String getStrSummaryTime() {
		return strSummaryTime;
	}
	public void setStrSummaryTime(String strSummaryTime) {
		this.strSummaryTime = strSummaryTime;
	}
	public ArrayList<ShareTATListRow> getAlSTATLR() {
		return alSTATLR;
	}
	public void setAlSTATLR(ArrayList<ShareTATListRow> alSTATLR) {
		this.alSTATLR = alSTATLR;
	}

}