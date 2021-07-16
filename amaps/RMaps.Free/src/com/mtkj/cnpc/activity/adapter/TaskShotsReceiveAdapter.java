package com.mtkj.cnpc.activity.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mtkj.cnpc.protocol.bean.ShotPoint;
import com.mtkj.cnpc.R;

import java.util.List;

public class TaskShotsReceiveAdapter extends BaseAdapter{

	private Context mContext;
	private List<ShotPoint> mShotPoints ;

	public TaskShotsReceiveAdapter(Context context, List<ShotPoint> mShotPoints) {
		this.mContext = context;
		this.mShotPoints = mShotPoints;
	}


	@Override
	public int getCount() {
		return mShotPoints == null ? 0 : mShotPoints.size();
	}

	@Override
	public Object getItem(int position) {
		return mShotPoints.get(position);
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
		if (mShotPoints!=null){
			holdView.tv_stationNo.setText(mShotPoints.get(position).stationNo);
		}
		return convertView;
	}

	class HoldView {
		TextView tv_stationNo;
	}
}
