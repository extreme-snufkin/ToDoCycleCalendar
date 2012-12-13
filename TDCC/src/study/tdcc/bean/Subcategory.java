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


 クラス名：Subcategory
 内容：SubcategoryデータBean
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
			0.1/2012.06.05/T.Mashiko

*/
package study.tdcc.bean;

public class Subcategory {
	//略字記号(親カテゴリ)
	private String strPCode;
	//略字記号(子カテゴリ)
	private String strCCode;
	//名称
	private String strName;

	public String getStrPCode() {
		return strPCode;
	}
	public void setStrPCode(String strPCode) {
		this.strPCode = strPCode;
	}
	public String getStrCCode() {
		return strCCode;
	}
	public void setStrCCode(String strCCode) {
		this.strCCode = strCCode;
	}
	public String getStrName() {
		return strName;
	}
	public void setStrName(String strName) {
		this.strName = strName;
	}

}
