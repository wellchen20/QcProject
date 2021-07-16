package com.mtkj.cnpc.activity.adapter;

import java.util.ArrayList;
import java.util.List;

import com.mtkj.cnpc.protocol.bean.TaskPoint;
import com.mtkj.cnpc.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TaskListAdapter extends BaseAdapter{
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<TaskPoint> mTaskPoints = new ArrayList<TaskPoint>();

	public TaskListAdapter(Context context, List<TaskPoint> taskPoints) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mTaskPoints = taskPoints;
	}
	
	public void setmTaskPoints(List<TaskPoint> taskPoints) {
		if (taskPoints != null) {
			mTaskPoints.clear();
			for(TaskPoint taskPoint : taskPoints) {
				mTaskPoints.add(taskPoint);
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mTaskPoints == null ? 0 : mTaskPoints.size();
	}

	@Override
	public Object getItem(int position) {
		return mTaskPoints.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		HoldView holdView = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listitem_tasklist, null);
			holdView = new HoldView();
			holdView.img = (ImageView) convertView.findViewById(R.id.img);
			holdView.txt_info = (TextView) convertView.findViewById(R.id.txt_info);
			holdView.txt_state = (TextView) convertView.findViewById(R.id.txt_state);
			convertView.setTag(holdView);
		} 
		holdView = (HoldView) convertView.getTag();
		
		TaskPoint taskPoint = mTaskPoints.get(position);
		holdView.txt_info.setText(taskPoint.stationNo);
		if (taskPoint.isDone) {
			holdView.img.setImageResource(R.drawable.point_isdone);
			holdView.txt_state.setText("已完成");
			holdView.txt_state.setTextColor(Color.RED);
		} else {
			holdView.img.setImageResource(R.drawable.point_undone);
			holdView.txt_state.setText("未完成");
			holdView.txt_state.setTextColor(Color.BLACK);
		}
		
		return convertView;
	}

	class HoldView {
		ImageView img;
		TextView txt_info;
		TextView txt_state;
	}
}
