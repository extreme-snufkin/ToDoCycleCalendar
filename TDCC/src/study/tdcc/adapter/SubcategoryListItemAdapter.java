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


 クラス名：SubcategoryListItemAdapter
 内容：サブカテゴリ編集画面ListViewのAdapterInterface
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
			0.1/2012.06.01/T.Mashiko
*/
package study.tdcc.adapter;

import java.util.List;

import study.tdcc.*;
import study.tdcc.bean.SubcategoryListRow;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SubcategoryListItemAdapter extends ArrayAdapter<SubcategoryListRow>
{
	//レイアウトインフレート
	private LayoutInflater mInflater;

	public SubcategoryListItemAdapter(Context context, int rid, List<SubcategoryListRow> list)
	{
		super(context, rid, list);
		//インフレート使用
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	//リストビューのインフレート取得
	public View getView(int position, View convertView, ViewGroup parent)
	{
		Log.d("DEBUG", "SubcategoryListItemAdapter getView Start");
		//インフレートを取り出す
		SubcategoryListRow objSLR = (SubcategoryListRow)getItem(position);

		//レイアウトファイルからビューを生成
		View view = mInflater.inflate(R.layout.subcategorylistrow, null);

		//サブカテゴリコードをセット
		TextView tvSubcategoryCode;
		tvSubcategoryCode = (TextView)view.findViewById(R.id.subcategorycode);
		tvSubcategoryCode.setText(objSLR.getStrSubcategoryCode());

		//サブカテゴリ名をセット
		TextView tvSubcategoryName;
		tvSubcategoryName = (TextView)view.findViewById(R.id.subcategoryname);
		tvSubcategoryName.setText(objSLR.getStrSubcategoryName());

		//サブカテゴリID(コード単体)をセット
		TextView tvSubCategoryId;
		tvSubCategoryId = (TextView)view.findViewById(R.id.subcategoryid);
		tvSubCategoryId.setText(objSLR.getStrSubcategoryCode());

		Log.d("DEBUG", "SubcategoryListItemAdapter getView End");
		// ビュー
		return view;
	}
}