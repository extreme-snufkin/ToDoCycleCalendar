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


 クラス名：ScheduleListItemAdapter
 内容：スケジュール(スケジュールタブ)画面ListViewのAdapterInterface
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
			0.1/2012.06.13/T.Mashiko
			0.2/2012.06.15/T.Mashiko
			0.3/2012.07.04/T.Mashiko スケジュール削除処理修正
			0.4/2012.07.18/T.Mashiko 不要ロジック削除
*/
package study.tdcc.adapter;

import java.util.List;

import study.tdcc.*;
import study.tdcc.bean.ScheduleListRow;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ScheduleListItemAdapter extends ArrayAdapter<ScheduleListRow>
{
	//レイアウトインフレート
	private LayoutInflater mInflater;

	public ScheduleListItemAdapter(Context context, int rid, List<ScheduleListRow> list)
	{
		super(context, rid, list);
		//インフレート使用
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	//リストビューのインフレート取得
	public View getView(int position, View convertView, ViewGroup parent)
	{
		Log.d("DEBUG", "ScheduleListItemAdapter getView Start");
		//インフレートを取り出す
		ScheduleListRow objSLR = (ScheduleListRow)getItem(position);
		//レイアウトファイルからビューを生成
		View view = mInflater.inflate(R.layout.schedulelistrow, null);
		//開始時間をセット
		TextView tvStartTime;
		tvStartTime = (TextView)view.findViewById(R.id.starttime);
		tvStartTime.setText(objSLR.getStrStartTime());
		//終日をセット
		TextView tvAllDay;
		tvAllDay = (TextView)view.findViewById(R.id.allday);
		tvAllDay.setText(objSLR.getStrAllDay());
		//終了時間をセット
		TextView tvEndTime;
		tvEndTime = (TextView)view.findViewById(R.id.endtime);
		tvEndTime.setText(objSLR.getStrEndTime());
		//タイトルをセット
		TextView tvTitle;
		tvTitle = (TextView)view.findViewById(R.id.scheduletitle);
		tvTitle.setText(objSLR.getStrTitle());
		//場所をセット
		TextView tvPlace;
		tvPlace = (TextView)view.findViewById(R.id.schedulewhere);
		tvPlace.setText(objSLR.getStrPlace());
		//説明をセット
		TextView tvContent;
		tvContent = (TextView)view.findViewById(R.id.schedulecontent);
		tvContent.setText(objSLR.getStrContent());
		//開始時間(数値)をセット
		//未使用項目の為、削除
		//端末アラーム対象フラグをセット
		TextView tvAlarmFlag;
		tvAlarmFlag = (TextView)view.findViewById(R.id.schedulealarmflag);
		tvAlarmFlag.setText(String.valueOf(objSLR.getLgAlarmFlag()));
		//スケジュールキーIDをセット
		TextView tvScheduleId;
		tvScheduleId = (TextView)view.findViewById(R.id.scheduleid);
		tvScheduleId.setText(String.valueOf(objSLR.getLgID()));
		//カレンダーIDをセット
		TextView tvCalendarId;
		tvCalendarId = (TextView)view.findViewById(R.id.calendarid);
		tvCalendarId.setText(String.valueOf(objSLR.getStrCalendarID()));
		Log.d("DEBUG", "ScheduleListItemAdapter getView End");
		//ビュー
		return view;
	}
}
