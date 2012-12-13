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


 クラス名：AllDayWhere
 内容：終日スケジュール取得クエリーデータBean
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
			0.1/2012.06.19/T.Mashiko

*/
package study.tdcc.bean;

public class AllDayWhere {
	//終了日時(数値)
	private long lgEndTime;
	//開始日時(数値)
	private long lgStartTime;
	
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
}
