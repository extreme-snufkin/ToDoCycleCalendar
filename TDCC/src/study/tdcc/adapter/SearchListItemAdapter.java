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


 クラス名：SearchListItemAdapter
 内容：検索画面ListViewのAdapterInterface
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
			0.1/2012.06.19/T.Mashiko
			0.2/2012.06.20/T.Mashiko
*/
package study.tdcc.adapter;

import java.util.List;

import study.tdcc.*;
import study.tdcc.bean.SearchListRow;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SearchListItemAdapter extends ArrayAdapter<SearchListRow>
{
	//レイアウトインフレート
	private LayoutInflater mInflater;

	public SearchListItemAdapter(Context context, int rid, List<SearchListRow> list)
	{
		super(context, rid, list);
		//インフレート使用
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	//リストビューのインフレート取得
	public View getView(int position, View convertView, ViewGroup parent)
	{
		Log.d("DEBUG", "SearchListItemAdapter getView Start");
		//インフレートを取り出す
		SearchListRow objSLR = (SearchListRow)getItem(position);

		//レイアウトファイルからビューを生成
		View view = mInflater.inflate(R.layout.searchlistrow, null);

		//表示年月日をセット
		TextView tvResultDate;
		tvResultDate = (TextView)view.findViewById(R.id.resultdate);
		tvResultDate.setText(objSLR.getStrDate());

		//検索タイプをセット
		TextView tvSearchTargetType;
		tvSearchTargetType = (TextView)view.findViewById(R.id.searchtargettype);
		tvSearchTargetType.setText(objSLR.getStrSearchType());

		Log.d("DEBUG", "SearchListItemAdapter getView End");
		//ビュー
		return view;
	}
}
