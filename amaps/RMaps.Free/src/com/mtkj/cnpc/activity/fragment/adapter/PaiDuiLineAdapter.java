package com.mtkj.cnpc.activity.fragment.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.R;

/***
 * 排队信息
 * 
 * @author TNT
 * 
 */
public class PaiDuiLineAdapter extends BaseAdapter {
	public Context context = null;
	public LayoutInflater layoutInflater = null;
	public int nSelected = -1;
	public List<String> lstBZJInfos = new ArrayList<String>();
	public String zhuanghao = "";

	public PaiDuiLineAdapter(Context context, String zhuanghao,
			List<String> lstDatas) {
		this.context = context;
		layoutInflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.zhuanghao = zhuanghao;
		if (this.zhuanghao == null) {
			this.zhuanghao = "";
		}
		if (lstDatas != null) {
			this.lstBZJInfos.clear();
			this.lstBZJInfos.addAll(lstDatas);
			for (int i = 0; i < lstDatas.size(); i++) {
				if (lstDatas.get(i).contains(SysConfig.BZJ_ID)) {
					nSelected = i;
					break;
				}
			}
		}
	}

	public void setSelectedItem(int nIndex) {
		nSelected = nIndex;
	}

	public int getSelectedItem() {
		return nSelected;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return lstBZJInfos.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return lstBZJInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.list_paidui_item,
					null);
		}
		String data = lstBZJInfos.get(position);
		((TextView) convertView.findViewById(R.id.textView1))
				.setText((position + 1) + "");
		((TextView) convertView.findViewById(R.id.textView2)).setText(data);
		((TextView) convertView.findViewById(R.id.textView3))
				.setText(zhuanghao);

		if (nSelected == position) {
			((TextView) convertView.findViewById(R.id.textView1))
					.setTextColor(Color.BLUE);
			((TextView) convertView.findViewById(R.id.textView2))
					.setTextColor(Color.BLUE);
			((TextView) convertView.findViewById(R.id.textView3))
					.setTextColor(Color.BLUE);
		}
		// else {
		// ((TextView) convertView.findViewById(R.id.textView1))
		// .setTextColor(Color.BLACK);
		// ((TextView) convertView.findViewById(R.id.textView2))
		// .setTextColor(Color.BLACK);
		// ((TextView) convertView.findViewById(R.id.textView3))
		// .setTextColor(Color.BLACK);
		// }

		return convertView;
	}
}
