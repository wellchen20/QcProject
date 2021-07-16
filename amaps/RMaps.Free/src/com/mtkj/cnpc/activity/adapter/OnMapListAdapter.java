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
import android.widget.TextView;

import com.mtkj.cnpc.R;

public class OnMapListAdapter extends BaseAdapter {
	
	private LayoutInflater mInflater;
	private List<String> mStrings = new ArrayList<String>();
	private List<String> mIds = new ArrayList<String>();
	private IInmap mOnmap;
	private String selectedName;
	
	public OnMapListAdapter(Context context, List<String> strings, List<String> ids, IInmap inmap, String name) {
		mInflater = LayoutInflater.from(context);
		mStrings = strings;
		mIds = ids;
		mOnmap = inmap;
		selectedName = name;
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

	public void setSelectedName(String name) {
		this.selectedName = name;
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
			holdView.list_item_onmap_name = (TextView) convertView.findViewById(R.id.list_item_onmap_name);
			holdView.list_item_onmap_isshow = (CheckBox) convertView.findViewById(R.id.list_item_onmap_isshow);
			convertView.setTag(holdView);
		} else {
			holdView = (HoldView) convertView.getTag();
		}
		
		holdView.list_item_onmap_name.setText(mStrings.get(position));
		if ("".equals(selectedName)) {
			holdView.list_item_onmap_isshow.setChecked(false);
		} else {
			if (mIds.get(position).equals(selectedName)) {
				holdView.list_item_onmap_isshow.setChecked(true);
			} else {
				holdView.list_item_onmap_isshow.setChecked(false);
			}
		}
		holdView.list_item_onmap_isshow.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					if (mOnmap != null) {
						mOnmap.selectedInmap(position);
					}
				} else {
					if (mOnmap != null) {
						mOnmap.selectedInmap(-1);
					}
				}
			}
		});
		return convertView;
	}

	class HoldView {
		TextView list_item_onmap_name;
		CheckBox list_item_onmap_isshow;
	}
	
	public interface IInmap {
		void selectedInmap(int position);
	}
}
