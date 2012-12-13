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


 クラス名：ToDo
 内容：ToDoデータBean
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
			0.1/2012.06.05/T.Mashiko
			0.2/2012.06.12/T.Mashiko Longオブジェクト→long型に修正
*/
package study.tdcc.bean;

public class ToDo {
	//キーID 
	private long lgTdId;
	//年月日
	private long lgDate;
	//タイトル
	private String strTitle;
	//優先順位
	private String strPriorityCode;
	//所要時間
	private String strTAT;
	//カテゴリー
	private String strCategoryCode;
	//サブカテゴリー
	private String strSubcategoryCode;
	//状態
	private long lgStatus;
	//内容
	private String strDetail;

	public long getLgTdId() {
		return lgTdId;
	}
	public void setLgTdId(long lgTdId) {
		this.lgTdId = lgTdId;
	}
	public long getLgDate() {
		return lgDate;
	}
	public void setLgDate(long lgDate) {
		this.lgDate = lgDate;
	}
	public String getStrTitle() {
		return strTitle;
	}
	public void setStrTitle(String strTitle) {
		this.strTitle = strTitle;
	}
	public String getStrPriorityCode() {
		return strPriorityCode;
	}
	public void setStrPriorityCode(String strPriorityCode) {
		this.strPriorityCode = strPriorityCode;
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
	public String getStrSubcategoryCode() {
		return strSubcategoryCode;
	}
	public void setStrSubcategoryCode(String strSubcategoryCode) {
		this.strSubcategoryCode = strSubcategoryCode;
	}
	public long getLgStatus() {
		return lgStatus;
	}
	public void setLgStatus(long lgStatus) {
		this.lgStatus = lgStatus;
	}
	public String getStrDetail() {
		return strDetail;
	}
	public void setStrDetail(String strDetail) {
		this.strDetail = strDetail;
	}
}
