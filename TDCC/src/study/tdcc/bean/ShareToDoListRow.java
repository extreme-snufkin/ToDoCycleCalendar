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


 クラス名：ShareToDoListRow
 内容：スケジュール(メインタブ)画面 共有機能のRowデータBean
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
			0.1/2012.07.15/T.Mashiko
*/
package study.tdcc.bean;

public class ShareToDoListRow {
	//完了
	private String strStatus;
	//優先順位
	private String strPriorityCode;
	//優先順位名
	private String strPriorityName;
	//所要時間
	private String strTAT;
	//カテゴリ
	private String strCategoryCode;
	//カテゴリ名
	private String strCategoryCodeName;
	//サブカテゴリ
	private String strSubcategoryCode;
	//サブカテゴリ名
	private String strSubcategoryCodeName;
	//タイトル
	private String strTitle;
	//内容
	private String strContent;

	public String getStrStatus() {
		return strStatus;
	}
	public void setStrStatus(String strStatus) {
		this.strStatus = strStatus;
	}
	public String getStrPriorityCode() {
		return strPriorityCode;
	}
	public void setStrPriorityCode(String strPriorityCode) {
		this.strPriorityCode = strPriorityCode;
	}
	public String getStrPriorityName() {
		return strPriorityName;
	}
	public void setStrPriorityName(String strPriorityName) {
		this.strPriorityName = strPriorityName;
	}
	public String getStrTAT() {
		return strTAT;
	}
	public void setStrTAT(String strTAT) {
		this.strTAT = strTAT;
	}
	public String getStrCategoryCode() {
		return strCategoryCode;
	}
	public void setStrCategoryCode(String strCategoryCode) {
		this.strCategoryCode = strCategoryCode;
	}
	public String getStrCategoryCodeName() {
		return strCategoryCodeName;
	}
	public void setStrCategoryCodeName(String strCategoryCodeName) {
		this.strCategoryCodeName = strCategoryCodeName;
	}
	public String getStrSubcategoryCode() {
		return strSubcategoryCode;
	}
	public void setStrSubcategoryCode(String strSubcategoryCode) {
		this.strSubcategoryCode = strSubcategoryCode;
	}
	public String getStrSubcategoryCodeName() {
		return strSubcategoryCodeName;
	}
	public void setStrSubcategoryCodeName(String strSubcategoryCodeName) {
		this.strSubcategoryCodeName = strSubcategoryCodeName;
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

}