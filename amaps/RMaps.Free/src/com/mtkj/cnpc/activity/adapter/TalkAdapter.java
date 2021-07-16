package com.mtkj.cnpc.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mtkj.utils.entity.TalkEntity;
import com.mtkj.cnpc.R;

import java.util.List;

public class TalkAdapter extends BaseAdapter{

	private Context mContext;
	private LayoutInflater mInflater;
	private List<TalkEntity> talkEntities;

	public TalkAdapter(Context context, List<TalkEntity> talkEntities) {
		this.mContext = context;
		mInflater = LayoutInflater.from(context);
		this.talkEntities = talkEntities;
	}

	public void refush(List<TalkEntity> entities) {
		if (entities != null) {
			talkEntities.clear();
			for(TalkEntity newEntity : entities) {
				talkEntities.add(newEntity);
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return talkEntities == null ? 0 : talkEntities.size();
	}

	@Override
	public Object getItem(int position) {
		return talkEntities.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		HoldView holdView = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_talk_person, null);
			holdView = new HoldView();
			holdView.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
			holdView.ll_others = (LinearLayout) convertView.findViewById(R.id.ll_others);
			holdView.tv_toOthers = (TextView) convertView.findViewById(R.id.tv_toOthers);
			holdView.iv_content_others = (ImageView) convertView.findViewById(R.id.iv_content_others);
			holdView.ll_me = (RelativeLayout) convertView.findViewById(R.id.ll_me);
			holdView.tv_fromMe = (TextView) convertView.findViewById(R.id.tv_fromMe);
			holdView.iv_content_me = (ImageView) convertView.findViewById(R.id.iv_content_me);
			holdView.ll_picOthers = (LinearLayout) convertView.findViewById(R.id.ll_picOthers);
			convertView.setTag(holdView);
		} 
		holdView = (HoldView) convertView.getTag();
		holdView.tv_time.setText(talkEntities.get(position).getTime());
		if (talkEntities.get(position).getType_who()==0){//聊天方是自己
			holdView.ll_me.setVisibility(View.VISIBLE);
			holdView.ll_others.setVisibility(View.GONE);
			if (talkEntities.get(position).getType_talk()==0){//内容是文字
				holdView.iv_content_me.setVisibility(View.GONE);
				holdView.ll_me.setVisibility(View.VISIBLE);
				holdView.tv_fromMe.setText(talkEntities.get(position).getContent());

			}else {//内容是图片
				holdView.iv_content_me.setVisibility(View.VISIBLE);
				holdView.ll_me.setVisibility(View.GONE);
				//holdView.iv_content_me.setImageResource(picture[position]);
			}

		}else {//聊天方是别人
			holdView.ll_me.setVisibility(View.GONE);
			holdView.ll_others.setVisibility(View.VISIBLE);
			if (talkEntities.get(position).getType_talk()==0){
				holdView.iv_content_others.setVisibility(View.GONE);
				holdView.ll_picOthers.setVisibility(View.VISIBLE);
				holdView.tv_toOthers.setText(talkEntities.get(position).getContent());
			}else {
				holdView.iv_content_others.setVisibility(View.VISIBLE);
				holdView.ll_picOthers.setVisibility(View.GONE);
//				holdView.iv_content_others.setImageResource(picture[position]);
			}
		}
		return convertView;
	}

	class HoldView {
		TextView tv_time;
		LinearLayout ll_others;
		LinearLayout ll_picOthers;
		TextView tv_toOthers;
		ImageView iv_content_others;
		RelativeLayout ll_me;
		TextView tv_fromMe;
		ImageView iv_content_me;
	}
}
