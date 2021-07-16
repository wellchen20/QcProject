package com.mtkj.cnpc.activity.fragment.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.mtkj.cnpc.protocol.bean.ShotPoint;
import com.mtkj.cnpc.R;

/***
 * 桩号顺序选择过滤
 * 
 * @author TNT
 * 
 */
public class ZhuangHaoNormalSelectAdapter extends BaseAdapter {
	public Context context = null;
	public LayoutInflater layoutInflater = null;
	public int nSelected = 0;
	public List<ShotPoint> lstTaskEntities = new ArrayList<ShotPoint>();

	public ZhuangHaoNormalSelectAdapter(Context context,
			List<ShotPoint> lstDatas) {
		this.context = context;
		layoutInflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (lstDatas != null && lstDatas.size() > 0) {
			this.lstTaskEntities.clear();
			this.lstTaskEntities.addAll(lstDatas);
		}
	}

	public void setSelectedItem(int nIndex) {
		if (!lstTaskEntities.get(nIndex).isDone) {
			nSelected = nIndex;
		}
	}

	public int getSelectedItem() {
		return nSelected;
	}

	@Override
	public int getCount() {
		return lstTaskEntities.size();
	}

	@Override
	public Object getItem(int position) {
		return lstTaskEntities.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = layoutInflater.inflate(
					R.layout.list_pipei_zhuanghao_normal_item, null);
		}
		ShotPoint taskEntity = lstTaskEntities.get(position);
		((TextView) convertView.findViewById(R.id.textView1)).setText(position
				+ "");
		((TextView) convertView.findViewById(R.id.textView2))
				.setText(taskEntity.stationNo);
		if (taskEntity.isDone) {
			((TextView) convertView.findViewById(R.id.textView3))
					.setTextColor(Color.BLUE);
			((TextView) convertView.findViewById(R.id.textView3))
					.setText("已完成");
		} else {
			((TextView) convertView.findViewById(R.id.textView3))
					.setTextColor(Color.BLACK);
			((TextView) convertView.findViewById(R.id.textView3))
					.setText("未完成");
		}
		
		if (taskEntity.isDone) {
			convertView.findViewById(android.R.id.text1).setVisibility(
					View.INVISIBLE);
		} else {
			if (convertView.findViewById(android.R.id.text1).getVisibility() != View.VISIBLE) {
				convertView.findViewById(android.R.id.text1).setVisibility(
						View.VISIBLE);
			}
			if (nSelected == position) {
				// convertView.findViewById(R.id.radioButton1).setSelected(true);
				((CheckedTextView) convertView.findViewById(android.R.id.text1))
						.setChecked(true);
			} else {
				// convertView.findViewById(R.id.radioButton1).setSelected(false);
				((CheckedTextView) convertView.findViewById(android.R.id.text1))
						.setChecked(false);
			}
		}

		return convertView;
	}

}
