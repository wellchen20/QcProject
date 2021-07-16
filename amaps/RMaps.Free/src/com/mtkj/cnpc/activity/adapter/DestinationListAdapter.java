package com.mtkj.cnpc.activity.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mtkj.cnpc.R;

public class DestinationListAdapter extends BaseAdapter {
	
	private LayoutInflater mInflater;
	private List<String> mStrings = new ArrayList<String>();
	private int selectedIndex = -1;
	private IDestination mDestination;
	
	public DestinationListAdapter(Context context, List<String> strings, IDestination destination) {
		mInflater = LayoutInflater.from(context);
		for (String string : strings) {
			mStrings.add(string);
		}
		mDestination = destination;
	}
	
	public void setmStrings(List<String> strings) {
		if (strings != null) {
			mStrings.clear();
			for (String string : strings) {
				mStrings.add(string);
			}
		}
		notifyDataSetChanged();
	}

	public void setSelectedIndex(int selectedIndex) {
		this.selectedIndex = selectedIndex;
	}

	@Override
	public int getCount() {
		return mStrings == null ? 0 : mStrings.size();
	}

	@Override
	public Object getItem(int position) {
		return mStrings.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		HoldView holdView = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listitem_onmap, null);
			holdView = new HoldView();
			holdView.tv_place_name = (TextView) convertView.findViewById(R.id.list_item_onmap_name);
			holdView.ck_place_isselected = (CheckBox) convertView.findViewById(R.id.list_item_onmap_isshow);
			holdView.rl_onmap = (RelativeLayout) convertView.findViewById(R.id.rl_onmap);
			convertView.setTag(holdView);
		} else {
			holdView = (HoldView) convertView.getTag();
		}
		
		holdView.tv_place_name.setText(mStrings.get(position));
		holdView.rl_onmap.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		holdView.ck_place_isselected.setChecked(position == selectedIndex);
		holdView.ck_place_isselected.setVisibility(View.GONE);
		holdView.ck_place_isselected.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (mDestination != null) {
					mDestination.onDestination(position, isChecked);
				}
			}
		});
		return convertView;
	}

	class HoldView {
		TextView tv_place_name;
		CheckBox ck_place_isselected;
		RelativeLayout rl_onmap;
	}
	
	public interface IDestination {
		void onDestination(int position, boolean isChecked);
	}
}
