package com.mtkj.cnpc.activity.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mtkj.cnpc.protocol.bean.DrillPoint;
import com.mtkj.cnpc.R;

import java.util.List;

public class TaskDrillsReceiveAdapter extends BaseAdapter{

	private Context mContext;
	private List<DrillPoint> mDrillPoints ;

	public TaskDrillsReceiveAdapter(Context context, List<DrillPoint> mDrillPoints) {
		this.mContext = context;
		this.mDrillPoints = mDrillPoints;
	}


	@Override
	public int getCount() {
		return mDrillPoints == null ? 0 : mDrillPoints.size();
	}

	@Override
	public Object getItem(int position) {
		return mDrillPoints.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		HoldView holdView = null;
		if (convertView == null) {
			convertView = View.inflate(mContext,R.layout.item_task_receive, null);
			holdView = new HoldView();
			holdView.tv_stationNo = (TextView) convertView.findViewById(R.id.tv_stationNo);
			convertView.setTag(holdView);
		} 
		holdView = (HoldView) convertView.getTag();
		if (mDrillPoints!=null){
			holdView.tv_stationNo.setText(mDrillPoints.get(position).stationNo);
		}
		return convertView;
	}

	class HoldView {
		TextView tv_stationNo;
	}
}
