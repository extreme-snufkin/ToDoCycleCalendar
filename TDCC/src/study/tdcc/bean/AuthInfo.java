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


 クラス名：AuthInfo
 内容：AuthInfoデータBean
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
			0.1/2012.07.06/T.Mashiko
*/
package study.tdcc.bean;

public class AuthInfo {
	//アクセストークン
	private String strAccessToken;
	//アクセストークン期限
	private long lgAccessTokenExpire;
	//リフレッシュトークン
	private String strRefreshToken;
	//最終更新日時
	private String strLastUpdate;
	
	public String getStrAccessToken() {
		return strAccessToken;
	}
	public void setStrAccessToken(String strAccessToken) {
		this.strAccessToken = strAccessToken;
	}
	public long getLgAccessTokenExpire() {
		return lgAccessTokenExpire;
	}
	public void setLgAccessTokenExpire(long lgAccessTokenExpire) {
		this.lgAccessTokenExpire = lgAccessTokenExpire;
	}
	public String getStrRefreshToken() {
		return strRefreshToken;
	}
	public void setStrRefreshToken(String strRefreshToken) {
		this.strRefreshToken = strRefreshToken;
	}
	public String getStrLastUpdate() {
		return strLastUpdate;
	}
	public void setStrLastUpdate(String strLastUpdate) {
		this.strLastUpdate = strLastUpdate;
	}

}
