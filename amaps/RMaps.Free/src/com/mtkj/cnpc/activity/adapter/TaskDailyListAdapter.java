package com.mtkj.cnpc.activity.adapter;

import java.util.ArrayList;
import java.util.List;

import com.mtkj.cnpc.protocol.bean.DailyTask;
import com.mtkj.cnpc.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TaskDailyListAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<DailyTask> mDailyTasks = new ArrayList<DailyTask>();
	
	public TaskDailyListAdapter(Context context, List<DailyTask> dailyTasks) {
		mInflater = LayoutInflater.from(context);
		mDailyTasks = dailyTasks;
	}
	
	public void setmDailyTasks(List<DailyTask> dailyTasks) {
		if (dailyTasks != null) {
			mDailyTasks.clear();
			for (DailyTask dailyTask : dailyTasks) {
				mDailyTasks.add(dailyTask);
			}
		}
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return mDailyTasks == null ? 0 : mDailyTasks.size();
	}

	@Override
	public Object getItem(int position) {
		return mDailyTasks.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		HoldView holdView = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listitem_dailytask, null);
			holdView = new HoldView();
			holdView.iv_listitem_daily = (ImageView) convertView.findViewById(R.id.iv_listitem_daily);
			holdView.list_item_daily_date = (TextView) convertView.findViewById(R.id.list_item_daily_date);
			holdView.list_item_daily_process = (TextView) convertView.findViewById(R.id.list_item_daily_process);
			holdView.list_item_daily_operation = (TextView) convertView.findViewById(R.id.list_item_daily_operation);
			convertView.setTag(holdView);
		} else {
			holdView = (HoldView) convertView.getTag();
		}
		
		DailyTask dailyTask = mDailyTasks.get(position);
		
		holdView.list_item_daily_date.setText(dailyTask.time);
		holdView.list_item_daily_process.setText(dailyTask.done + "/" + dailyTask.total);
		if (dailyTask.done == dailyTask.total) {
			
		} else {
			
		}
		return convertView;
	}

	class HoldView {
		ImageView iv_listitem_daily;
		TextView list_item_daily_date;
		TextView list_item_daily_process;
		TextView list_item_daily_operation;
	}
	
}
