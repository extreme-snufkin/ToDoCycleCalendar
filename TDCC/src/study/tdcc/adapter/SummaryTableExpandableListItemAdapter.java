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


 クラス名：SummaryTableExpandableListItemAdapter
 内容：集計画面ListViewのAdapterInterface
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
			0.1/2012.07.12/T.Mashiko
			0.2/2012.07.13/T.Mashiko
*/
package study.tdcc.adapter;

import java.util.List;

import study.tdcc.R;
import study.tdcc.bean.SummaryTableListChildRow;
import study.tdcc.bean.SummaryTableListParentRow;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class SummaryTableExpandableListItemAdapter extends BaseExpandableListAdapter {

	private Context cObj;
	private List<SummaryTableListParentRow> lSTLPR;
	private List<List<SummaryTableListChildRow>> llSTLCR;
	private LayoutInflater lyObj;

	public SummaryTableExpandableListItemAdapter(Context context, List<SummaryTableListParentRow> lSTLPR, List<List<SummaryTableListChildRow>> llSTLCR) {
		super();
		//コンテキスト
		cObj = context;
		//インフレート使用
		lyObj = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//親リスト
		this.lSTLPR = lSTLPR;
		//子リスト
		this.llSTLCR = llSTLCR;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		Log.d("DEBUG", "SummaryTableExpandableListItemAdapter getChild Start");
		Log.d("DEBUG", "SummaryTableExpandableListItemAdapter getChild End");
		// TODO 自動生成されたメソッド・スタブ
		return llSTLCR.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		Log.d("DEBUG", "SummaryTableExpandableListItemAdapter getChildId Start");
		Log.d("DEBUG", "SummaryTableExpandableListItemAdapter getChildId End");
		// TODO 自動生成されたメソッド・スタブ
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		Log.d("DEBUG", "SummaryTableExpandableListItemAdapter getChildView Start");
		// TODO 自動生成されたメソッド・スタブ
		if (convertView == null) {
			convertView = lyObj.inflate(R.layout.summarytablelistchildrow, parent, false);
		}
		//内訳:日付
		TextView tvBreakdownDate = (TextView)convertView.findViewById(R.id.breakdowndate);
		tvBreakdownDate.setText(llSTLCR.get(groupPosition).get(childPosition).getStrDate());

		//内訳:時間
		TextView tvTime = (TextView)convertView.findViewById(R.id.time);
		StringBuilder sbTime = new StringBuilder();
		sbTime.append(llSTLCR.get(groupPosition).get(childPosition).getStrTime());
		sbTime.append(cObj.getString(R.string.actimemark));
		tvTime.setText(sbTime.toString());
		Log.d("DEBUG", "SummaryTableExpandableListItemAdapter getChildView End");
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		Log.d("DEBUG", "SummaryTableExpandableListItemAdapter getChildrenCount Start");
		Log.d("DEBUG", "SummaryTableExpandableListItemAdapter getChildrenCount End");
		// TODO 自動生成されたメソッド・スタブ
		return llSTLCR.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		Log.d("DEBUG", "SummaryTableExpandableListItemAdapter getGroup Start");
		Log.d("DEBUG", "SummaryTableExpandableListItemAdapter getGroup End");
		// TODO 自動生成されたメソッド・スタブ
		return lSTLPR.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		Log.d("DEBUG", "SummaryTableExpandableListItemAdapter getGroupCount Start");
		Log.d("DEBUG", "SummaryTableExpandableListItemAdapter getGroupCount End");
		// TODO 自動生成されたメソッド・スタブ
		return lSTLPR.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		Log.d("DEBUG", "SummaryTableExpandableListItemAdapter getGroupId Start");
		Log.d("DEBUG", "SummaryTableExpandableListItemAdapter getGroupId End");
		// TODO 自動生成されたメソッド・スタブ
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		Log.d("DEBUG", "SummaryTableExpandableListItemAdapter getGroupView Start");
		// TODO 自動生成されたメソッド・スタブ
		if (convertView == null) {
			convertView = lyObj.inflate(R.layout.summarytablelistrow, parent, false);
		}
		//サブカテゴリ名をセット
		TextView tvSubcategoryName = (TextView)convertView.findViewById(R.id.subcategoryname);
		StringBuilder sbSubcategoryName = new StringBuilder();
		sbSubcategoryName.append(cObj.getString(R.string.actopspace));
		sbSubcategoryName.append(lSTLPR.get(groupPosition).getStrSubcategoryCode());
		sbSubcategoryName.append(cObj.getString(R.string.acbindmark));
		sbSubcategoryName.append(lSTLPR.get(groupPosition).getStrSubcategoryName());
		tvSubcategoryName.setText(sbSubcategoryName.toString());

		//合計時間をセット
		TextView tvTotalTime = (TextView)convertView.findViewById(R.id.totaltime);
		StringBuilder sbTotalTime = new StringBuilder();
		sbTotalTime.append(lSTLPR.get(groupPosition).getStrSummaryTime());
		sbTotalTime.append(cObj.getString(R.string.actimemark));
		tvTotalTime.setText(sbTotalTime.toString());
		Log.d("DEBUG", "SummaryTableExpandableListItemAdapter getGroupView End");
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		Log.d("DEBUG", "SummaryTableExpandableListItemAdapter hasStableIds Start");
		Log.d("DEBUG", "SummaryTableExpandableListItemAdapter hasStableIds End");
		// TODO 自動生成されたメソッド・スタブ
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		Log.d("DEBUG", "SummaryTableExpandableListItemAdapter isChildSelectable Start");
		Log.d("DEBUG", "SummaryTableExpandableListItemAdapter isChildSelectable End");
		// TODO 自動生成されたメソッド・スタブ
		return true;
	}
}