package com.mtkj.cnpc.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mtkj.utils.entity.TaskEntity;
import com.mtkj.cnpc.R;

public class TaskDetailsAdapter extends BaseAdapter{

	private Context mContext;
	private LayoutInflater mInflater;
	private TaskEntity mTask;

	public TaskDetailsAdapter(Context context, TaskEntity mTask) {
		this.mContext = context;
		mInflater = LayoutInflater.from(context);
		this.mTask = mTask;
	}

	@Override
	public int getCount() {
		return mTask.getTask() == null ? 0 : mTask.getTask().size();
	}

	@Override
	public Object getItem(int position) {
		return mTask.getTask().get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		HoldView holdView = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_task_details, null);
			holdView = new HoldView();
			holdView.tv_no = (TextView) convertView.findViewById(R.id.tv_no);
			holdView.tv_start = (TextView) convertView.findViewById(R.id.tv_start);
			holdView.tv_end = (TextView) convertView.findViewById(R.id.tv_end);
			convertView.setTag(holdView);
		} 
		holdView = (HoldView) convertView.getTag();
		if (mTask.getTask().size()!=0){
			holdView.tv_no.setText("线号："+mTask.getTask().get(position).getLine_no());
			holdView.tv_start.setText("开始点号："+mTask.getTask().get(position).getS_point_no());
			holdView.tv_end.setText("结束点号："+mTask.getTask().get(position).getE_point_no());
		}

		return convertView;
	}

	class HoldView {
		TextView tv_no;
		TextView tv_start;
		TextView tv_end;
	}
}
