package com.mtkj.cnpc.activity.fragment.adapter;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.mtkj.cnpc.R;
import com.mtkj.cnpc.protocol.bean.ArrangePoint;
import com.mtkj.cnpc.protocol.bean.ShotPoint;

import org.andnav.osm.util.GeoPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/***
 * 桩号选择过滤
 * 
 * @author TNT
 * 
 */
public class ZhuangHaoAroundArrangeAdapter extends BaseAdapter implements
		ListAdapter {
	public Context context = null;
	public LayoutInflater layoutInflater = null;
	public int nSelected = 0;
	public List<ArrangePoint> lstTaskEntities = new ArrayList<ArrangePoint>();

	public ZhuangHaoAroundArrangeAdapter(Context context,
                                         List<ArrangePoint> lstDatas, Location location) {
		this.context = context;
		GeoPoint locGeoPoint = GeoPoint.fromDouble(location.getLatitude(), location.getLongitude());
		layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (lstDatas != null && lstDatas.size() > 0) {
			this.lstTaskEntities.clear();
			for (ArrangePoint data : lstDatas) {
				if (data.dis > 0) {
					data.remark = data.dis + " 米";
				} else {
					int dis = (int) locGeoPoint.distanceTo(data.geoPoint);
					data.dis = dis;
					data.remark = dis + " 米";
				}
				this.lstTaskEntities.add(data);
			}
			// 排序
			Collections.sort(lstTaskEntities, new Comparator<ArrangePoint>() {

				@Override
				public int compare(ArrangePoint lhs, ArrangePoint rhs) {
					// TODO Auto-generated method stub
					return ((Integer) lhs.dis).compareTo((Integer) rhs.dis);
				}
			});
			lstDatas.clear();
			lstDatas.addAll(this.lstTaskEntities);
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
					R.layout.list_pipei_zhuanghao_item, null);
		}

		if (position==0){
			convertView.findViewById(R.id.tv_title).setVisibility(View.VISIBLE);
		}
		ArrangePoint taskEntity = lstTaskEntities.get(position);

		// if (convertView.findViewById(R.id.radioButton1).getVisibility() !=
		// View.VISIBLE) {
		// convertView.findViewById(R.id.radioButton1).setVisibility(
		// View.VISIBLE);
		// }
		if (!taskEntity.isDone) {
			((TextView) convertView.findViewById(R.id.textView1))
					.setTextColor(Color.BLACK);
			((TextView) convertView.findViewById(R.id.textView2))
					.setTextColor(Color.BLACK);
			((TextView) convertView.findViewById(R.id.textView3))
					.setTextColor(Color.BLACK);

			((TextView) convertView.findViewById(R.id.textView1))
					.setText((position + 1) + "");
			((TextView) convertView.findViewById(R.id.textView2))
					.setText(taskEntity.stationNo);
			((TextView) convertView.findViewById(R.id.textView3))
					.setText(taskEntity.remark);

			// ((CheckedTextView) convertView.findViewById(android.R.id.text1))
			// .setEnabled(true);

		} else {
			((TextView) convertView.findViewById(R.id.textView1))
					.setTextColor(Color.RED);
			((TextView) convertView.findViewById(R.id.textView2))
					.setTextColor(Color.RED);
			((TextView) convertView.findViewById(R.id.textView3))
					.setTextColor(Color.RED);

			((TextView) convertView.findViewById(R.id.textView1))
					.setText((position + 1) + "");
			((TextView) convertView.findViewById(R.id.textView2))
					.setText(taskEntity.stationNo + "-已完成");
			((TextView) convertView.findViewById(R.id.textView3))
					.setText(taskEntity.remark);

			// ((CheckedTextView) convertView.findViewById(android.R.id.text1))
			// .setVisibility(View.GONE);
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

		return convertView;
	}

}
