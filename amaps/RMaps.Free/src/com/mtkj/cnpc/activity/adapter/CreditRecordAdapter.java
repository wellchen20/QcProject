package com.mtkj.cnpc.activity.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mtkj.utils.entity.RecordEntity;
import com.mtkj.cnpc.R;

public class CreditRecordAdapter extends BaseAdapter{

	private Context mContext;
	private RecordEntity recordEntity ;

	public CreditRecordAdapter(Context context, RecordEntity recordEntity) {
		this.mContext = context;
		this.recordEntity = recordEntity;
	}


	@Override
	public int getCount() {
		return recordEntity == null ? 0 : recordEntity.getRecords().size();
	}

	@Override
	public Object getItem(int position) {
		return recordEntity.getRecords().get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		HoldView holdView = null;
		if (convertView == null) {
			convertView = View.inflate(mContext,R.layout.credit_record_item, null);
			holdView = new HoldView();
			holdView.tv_credit = (TextView) convertView.findViewById(R.id.tv_credit);
			holdView.tv_reason = (TextView) convertView.findViewById(R.id.tv_reason);
			holdView.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
			convertView.setTag(holdView);
		} 
		holdView = (HoldView) convertView.getTag();
		if (recordEntity.getRecords()!=null){
			holdView.tv_credit.setText("积分+"+recordEntity.getRecords().get(position).getCredit());
			if (recordEntity.getRecords().get(position).getRemark().equals("LOGIN")){
				holdView.tv_reason.setText("每日登录奖励");
			}else if (recordEntity.getRecords().get(position).getRemark().equals("TASKDONE")){
				holdView.tv_reason.setText("完成指定任务奖励");
			}
			holdView.tv_time.setText(recordEntity.getRecords().get(position).getTime());
		}
		return convertView;
	}

	class HoldView {
		TextView tv_credit;
		TextView tv_reason;
		TextView tv_time;
	}
}
