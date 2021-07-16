package com.mtkj.cnpc.activity.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mtkj.cnpc.protocol.bean.ArrangePoint;
import com.mtkj.cnpc.R;

import java.util.List;

public class TaskArrangesReceiveAdapter extends BaseAdapter{

	private Context mContext;
	private List<ArrangePoint> mArrangePoints ;

	public TaskArrangesReceiveAdapter(Context context, List<ArrangePoint> mArrangePoints ) {
		this.mContext = context;
		this.mArrangePoints = mArrangePoints;
	}


	@Override
	public int getCount() {
		return mArrangePoints == null ? 0 : mArrangePoints.size();
	}

	@Override
	public Object getItem(int position) {
		return mArrangePoints.get(position);
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
		if (mArrangePoints!=null){
			holdView.tv_stationNo.setText(mArrangePoints.get(position).stationNo);
		}
		return convertView;
	}

	class HoldView {
		TextView tv_stationNo;
	}
}
