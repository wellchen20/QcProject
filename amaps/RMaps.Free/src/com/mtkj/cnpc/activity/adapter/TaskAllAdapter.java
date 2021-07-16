package com.mtkj.cnpc.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mtkj.utils.entity.TaskEntity;
import com.mtkj.cnpc.R;

import java.util.ArrayList;
import java.util.List;

public class TaskAllAdapter extends BaseAdapter{

	private Context mContext;
	private LayoutInflater mInflater;
	private List<TaskEntity> mTask = new ArrayList<TaskEntity>();

	public TaskAllAdapter(Context context, List<TaskEntity> mTask) {
		this.mContext = context;
		mInflater = LayoutInflater.from(context);
		this.mTask = mTask;
	}
	public void refush(List<TaskEntity> tasks) {
		if (mTask != null) {
			mTask.clear();
			for(TaskEntity newTask : tasks) {
				mTask.add(newTask);
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mTask == null ? 0 : mTask.size();
	}

	@Override
	public Object getItem(int position) {
		return mTask.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		HoldView holdView = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.news_list_item, null);
			holdView = new HoldView();
			holdView.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
			holdView.tv_type = (TextView) convertView.findViewById(R.id.tv_type);
			holdView.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
			convertView.setTag(holdView);
		} 
		holdView = (HoldView) convertView.getTag();
		if (mTask!=null){
			holdView.tv_time.setText(mTask.get(position).getStart_time()+" - "+mTask.get(position).getFinish_time());
			if (mTask.get(position).getTask_type()==1){
				holdView.tv_type.setText("放线任务");
			}else {
				holdView.tv_type.setText("井炮任务");
			}

			if (mTask.get(position).getTask()!=null){
				holdView.tv_content.setText("线号："+mTask.get(position).getTask().get(0).getLine_no()+
						" 开始点号："+mTask.get(position).getTask().get(0).getS_point_no()
						+" 结束点号："+mTask.get(position).getTask().get(0).getE_point_no());
			}
		}

		return convertView;
	}

	class HoldView {
		TextView tv_time;
		TextView tv_type;
		TextView tv_content;
	}
}
