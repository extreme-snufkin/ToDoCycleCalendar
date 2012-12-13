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


 クラス名：ToDoListItemAdapter
 内容：スケジュール(ToDoタブ)画面ListViewのAdapterInterface
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
			0.1/2012.06.05/T.Mashiko
			0.2/2012.06.06/T.Mashiko
*/
package study.tdcc.adapter;

import java.util.List;

import study.tdcc.*;
import study.tdcc.bean.ToDoListRow;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ToDoListItemAdapter extends ArrayAdapter<ToDoListRow>
{
	//レイアウトインフレート
	private LayoutInflater mInflater;
	//コンテキスト
	private Context cObj;

	public ToDoListItemAdapter(Context context, int rid, List<ToDoListRow> list)
	{
		super(context, rid, list);
		//インフレート使用
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//コンテキスト格納
		cObj = context;
	}

	// リストビューのインフレート取得
	public View getView(int position, View convertView, ViewGroup parent)
	{
		Log.d("DEBUG", "ToDoListItemAdapter getView Start");
		// インフレートを取り出す
		ToDoListRow objTDLR = (ToDoListRow)getItem(position);
		// レイアウトファイルからビューを生成
		View view = mInflater.inflate(R.layout.todolistrow, null);

		//状態をセット
		TextView tvStatus;
		tvStatus = (TextView)view.findViewById(R.id.status);
		tvStatus.setText(objTDLR.getStrStatus());
		changeYMColor(objTDLR,tvStatus);

		//優先順位をセット
		TextView tvPriority;
		tvPriority = (TextView)view.findViewById(R.id.priority);
		tvPriority.setText(objTDLR.getStrPriorityCode());
		changeYMColor(objTDLR,tvPriority);

		//所要時間をセット
		TextView tvTimeneeded;
		tvTimeneeded = (TextView)view.findViewById(R.id.timeneeded);
		tvTimeneeded.setText(objTDLR.getStrTAT());
		changeYMColor(objTDLR,tvTimeneeded);

		//カテゴリコードをセット
		TextView tvCategoryCode;
		tvCategoryCode = (TextView)view.findViewById(R.id.category);
		tvCategoryCode.setText(objTDLR.getStrCategoryCode());
		changeYMColor(objTDLR,tvCategoryCode);

		//サブカテゴリ名をセット
		TextView tvSubcategoryCode;
		tvSubcategoryCode = (TextView)view.findViewById(R.id.subcategory);
		tvSubcategoryCode.setText(objTDLR.getStrSubcategoryCode());
		changeYMColor(objTDLR,tvSubcategoryCode);

		//タイトルをセット
		TextView tvTitle;
		tvTitle = (TextView)view.findViewById(R.id.title);
		tvTitle.setText(objTDLR.getStrTitle());
		changeYMColor(objTDLR,tvTitle);

		//ToDo IDをセット
		TextView tvToDoId;
		tvToDoId = (TextView)view.findViewById(R.id.todoid);
		tvToDoId.setText(String.valueOf(objTDLR.getLgTDID()));

		Log.d("DEBUG", "ToDoListItemAdapter getView End");
		// ビュー
		return view;
	}

	/**
	 * 背景色変更処理
	 *
	 * @param objTDLR 行格納データ
	 * @param tvView テキストビュー
	 */
	public void changeYMColor(ToDoListRow objTDLR, TextView tvView) {
		Log.d("DEBUG", "ToDoListItemAdapter changeYMColor Start");
		if(objTDLR.getStrStatus().equals(cObj.getString(R.string.completion_mark1))) {
			//完了の場合(あおにび)
			tvView.setTextColor(Color.rgb(107, 123, 110));
		}
		Log.d("DEBUG", "ToDoListItemAdapter changeYMColor End");
	}
}
