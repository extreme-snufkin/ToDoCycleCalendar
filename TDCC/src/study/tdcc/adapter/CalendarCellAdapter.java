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


 クラス名：CalendarCellAdapter
 内容：カレンダー画面GridViewのAdapterInterface
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
			0.1/2012.05.22/T.Mashiko
			0.2/2012.06.17/T.Mashiko
			0.3/2012.06.19/T.Mashiko データ取得処理分離
*/
package study.tdcc.adapter;

import study.tdcc.*;
import study.tdcc.lib.DateUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class CalendarCellAdapter extends BaseAdapter {
	private static final int NUM_ROWS = 6;
	private static final int NUM_OF_CELLS = DateUtil.DAYS_OF_WEEK * NUM_ROWS;
	//セル内スケジュールデータリスト
	private ArrayList<String> alDateCell;
	//ターゲット年月日を保持する変数
	private GregorianCalendar gcTargetYearMonthDay;
	//処理当日の年月日を保持する変数
	private GregorianCalendar gcNow;
	//レイアウトインフラッターオブジェクト
	private LayoutInflater liObj;
	//終日予定のテキストビュー
	private TextView scheduleView;

	/**
	 * コンストラクタではパラメタで受け取ったcontextを使用して
	 * 「LayoutInflater」のインスタンスを作成する。
	 * @param context アクティビティ
	 */
	public CalendarCellAdapter(Context context, ArrayList<String> alDateCell, GregorianCalendar gcTargetYearMonthDay, GregorianCalendar gcNow){
		Log.d("DEBUG", "CalendarCellAdapter Constractor Start");
		//getSystemServiceでContextからLayoutInflaterを取得
		liObj = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.alDateCell = alDateCell;
		this.gcTargetYearMonthDay = gcTargetYearMonthDay;
		this.gcNow = gcNow;
		Log.d("DEBUG", "CalendarCellAdapter Constractor End");
	}
	/**
	 * getCount
	 * 「NUM_OF_CELLS」 (42)を返す
	 */
	public int getCount() {
		Log.d("DEBUG", "CalendarCellAdapter getCount Start");
		Log.d("DEBUG", "CalendarCellAdapter getCount End");
		return NUM_OF_CELLS;
	}
	/**
	 * getItem
	 * 必要ないのでnullを返す
	 */
	public Object getItem(int position) {
		Log.d("DEBUG", "CalendarCellAdapter getItem Start");
		Log.d("DEBUG", "CalendarCellAdapter getItem End");
		return null;
	}
	/**
	 * getItemId
	 * 必要ないので0を返す
	 */
	public long getItemId(int position) {
		Log.d("DEBUG", "CalendarCellAdapter getItemId Start");
		Log.d("DEBUG", "CalendarCellAdapter getItemId End");
		return 0;
	}
	/**
	 * getView
	 *  DateCellのViewを作成して返すためのメソッド
	 *  @param int position セルの位置
	 *  @param View convertView 前に使用したView
	 *  @param ViewGroup parent 親ビュー　ここではGridView
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d("DEBUG", "CalendarCellAdapter getView Start");
		//Log.d("DEBUG", "CalendarCellAdapter getView Position : " + position);
		if(convertView == null){
			convertView = liObj.inflate(R.layout.datecell,null);
		}
		//Viewの最小の高さを設定する
		convertView.setMinimumHeight(parent.getHeight()/NUM_ROWS-2);
		TextView dayOfMonthView = (TextView)convertView.findViewById(R.id.dayOfMonth);
		scheduleView = (TextView)convertView.findViewById(R.id.schedule);
		Calendar cal = (Calendar)gcTargetYearMonthDay.clone();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.add(Calendar.DAY_OF_MONTH, position-cal.get(Calendar.DAY_OF_WEEK)+1);
		dayOfMonthView.setText(""+cal.get(Calendar.DAY_OF_MONTH));
		if(position%7 == 0){
			dayOfMonthView.setBackgroundResource(R.color.kokin);
			scheduleView.setBackgroundResource(R.color.mizuiro);
		} else if(position%7 == 6){
			dayOfMonthView.setBackgroundResource(R.color.konai);
			scheduleView.setBackgroundResource(R.color.mizuiro);
		} else {
			dayOfMonthView.setBackgroundResource(R.color.wasurenagusa);
			scheduleView.setBackgroundResource(R.color.mizuiro);
		}
		//当月日以外の背景色変更
		if(DateUtil.MONTH_FORMAT.format(gcTargetYearMonthDay.getTime()).equals(DateUtil.MONTH_FORMAT.format(cal.getTime())) == false) {
			dayOfMonthView.setBackgroundResource(R.color.gray);
			scheduleView.setBackgroundResource(R.color.lightGray);
		}
		//当日の背景色変更
		if(DateUtil.DATE_FORMAT.format(gcNow.getTime()).equals(DateUtil.DATE_FORMAT.format(cal.getTime()))) {
			dayOfMonthView.setBackgroundResource(R.color.nanohanairo);
		}
		//終日スケジュールの表示
		scheduleView.setText(alDateCell.get(position));
		Log.d("DEBUG", "CalendarCellAdapter getView End");
		return convertView;		
	}
}