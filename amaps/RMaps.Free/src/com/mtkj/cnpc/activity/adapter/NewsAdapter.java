package com.mtkj.cnpc.activity.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mtkj.cnpc.activity.NewsActivity;
import com.mtkj.utils.entity.ContactPersons;
import com.mtkj.cnpc.R;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends BaseAdapter{

	private Context mContext;
	private List<ContactPersons.UserlistBean> userlistBeans = new ArrayList<>();

	public NewsAdapter(Context context, List<ContactPersons.UserlistBean> userlistBeans) {
		this.mContext = context;
		this.userlistBeans = userlistBeans;
	}
	public void refush(List<ContactPersons.UserlistBean>  newPersons) {
		if (newPersons != null) {
			userlistBeans.clear();
			for (int i=0;i<newPersons.size();i++){
				userlistBeans.add(newPersons.get(i));
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return userlistBeans == null ? 0 : userlistBeans.size();
	}

	@Override
	public Object getItem(int position) {
		return userlistBeans.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		HoldView holdView = null;
		if (convertView == null) {
			convertView = View.inflate(mContext,R.layout.item_news_person, null);
			holdView = new HoldView();
			holdView.iv_person = (ImageView) convertView.findViewById(R.id.iv_person);
			holdView.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			holdView.tv_tel = (TextView) convertView.findViewById(R.id.tv_tel);
			holdView.iv_isOn = (ImageView) convertView.findViewById(R.id.iv_isOn);
			holdView.tv_count = (TextView) convertView.findViewById(R.id.tv_count);
			convertView.setTag(holdView);
		} 
		holdView = (HoldView) convertView.getTag();

		if (NewsActivity.flag && position==NewsActivity.posArr){
			holdView.tv_count.setVisibility(View.VISIBLE);
		}else {
			holdView.tv_count.setVisibility(View.INVISIBLE);
		}

		if (userlistBeans!=null){
			holdView.tv_name.setText(userlistBeans.get(position).getName());
		}
		if (position==0){
			holdView.iv_person.setImageResource(R.drawable.friends);
			holdView.iv_isOn.setVisibility(View.INVISIBLE);
			holdView.tv_tel.setVisibility(View.GONE);
			holdView.tv_name.setText("聊天组");
		}else {
			holdView.iv_isOn.setVisibility(View.VISIBLE);
			holdView.tv_tel.setVisibility(View.VISIBLE);
			holdView.iv_person.setImageResource(R.drawable.friend);
			holdView.tv_tel.setText(userlistBeans.get(position).getPhone());
			if (userlistBeans.get(position).getStatus()==101){
				holdView.iv_isOn.setImageResource(R.drawable.point_online);
			}else {
				holdView.iv_isOn.setImageResource(R.drawable.point_offline);
			}
		}

		return convertView;
	}

	class HoldView {
		ImageView iv_person;
		TextView tv_name;
		TextView tv_tel;
		TextView tv_count;
		ImageView iv_isOn;
	}
}
