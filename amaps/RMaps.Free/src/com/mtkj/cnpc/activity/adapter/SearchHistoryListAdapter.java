package com.mtkj.cnpc.activity.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mtkj.cnpc.R;

public class SearchHistoryListAdapter extends BaseAdapter {
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<String> mHistorires = new ArrayList<String>();
	
	public SearchHistoryListAdapter(Context context, List<String> histories) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mHistorires = histories;
	}

	public void setmHistorires(List<String> historires) {
		if (historires != null) {
			mHistorires.clear();
			for (String string : historires) {
				mHistorires.add(string);
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mHistorires == null ? 0 : mHistorires.size();
	}

	@Override
	public Object getItem(int position) {
		return mHistorires.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		HoldView holdView = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listitem_history, null);
			holdView = new HoldView();
			holdView.rl_history = (RelativeLayout) convertView.findViewById(R.id.rl_history);
			holdView.list_item_history = (TextView) convertView.findViewById(R.id.list_item_history);
			convertView.setTag(holdView);
		} else {
			holdView = (HoldView) convertView.getTag();
		}
		
//		holdView.rl_history.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		holdView.rl_history.setBackgroundResource(R.drawable.selector_background);
		holdView.list_item_history.setText(mHistorires.get(position));
		return convertView;
	}

	class HoldView {
		RelativeLayout rl_history;
		TextView list_item_history;
	}
	
	public interface ISearch {
		void onSearchGuide(int position);
	}
}

