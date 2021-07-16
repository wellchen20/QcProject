package com.mtkj.cnpc.activity.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mtkj.cnpc.R;

public class OutMapListAdapter extends BaseAdapter {
	
	private LayoutInflater mInflater;
	private List<File> mFiles = new ArrayList<File>();
	private String selectedMapName;
	private IOutmap mOutmap;

	public OutMapListAdapter(Context context, List<File> files, String name, IOutmap outmap) {
		mInflater = LayoutInflater.from(context);
		mFiles = files;
		selectedMapName = name;
		mOutmap= outmap;
	}
	
	public void setmFiles(List<File> files) {
		if (files != null) {
			mFiles.clear();
			for(File file : files) {
				mFiles.add(file);
			}
		}
		notifyDataSetChanged();
	}
	
	public void setSelectedMapName(String name) {
		this.selectedMapName = name;
	}

	@Override
	public int getCount() {
		return mFiles == null ? 0 :mFiles.size();
	}

	@Override
	public Object getItem(int position) {
		return mFiles.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final HoldView holdView = new HoldView();
//		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listitem_outmap, null);
//			holdView = new HoldView();
			holdView.list_item_outmap_name = (TextView) convertView.findViewById(R.id.list_item_outmap_name);
			holdView.list_item_outmap_isshow = (CheckBox) convertView.findViewById(R.id.list_item_outmap_isshow);
			holdView.list_item_outmap = (RelativeLayout) convertView.findViewById(R.id.list_item_outmap);
			holdView.list_item_outmap_ckeck = (LinearLayout) convertView.findViewById(R.id.list_item_outmap_ckeck);
			convertView.setTag(holdView);
//		} else {
//			holdView = (HoldView) convertView.getTag();
//		}
		
		holdView.list_item_outmap.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		holdView.list_item_outmap.setBackgroundResource(R.drawable.selector_background);
		File file = mFiles.get(position);
		holdView.list_item_outmap_name.setText(file.getName());
		if ("".equals(selectedMapName)) {
			holdView.list_item_outmap_isshow.setChecked(false);
		} else {
			if (selectedMapName.endsWith(file.getName())) {
				holdView.list_item_outmap_isshow.setChecked(true);
			} else {
				holdView.list_item_outmap_isshow.setChecked(false);
			}
		}
		holdView.list_item_outmap_ckeck.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean isChecked = !holdView.list_item_outmap_isshow.isChecked();
				if (isChecked) {
					if (mOutmap != null) {
						mOutmap.selectedOutmap(position);
					}
				} else {
					if (mOutmap != null) {
						mOutmap.selectedOutmap(-1);
					}
				}
			}
		});
		holdView.list_item_outmap_isshow.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					if (mOutmap != null) {
						mOutmap.selectedOutmap(position);
					}
				} else {
					if (mOutmap != null) {
						mOutmap.selectedOutmap(-1);
					}
				}
			}
		});
		
		return convertView;
	}

	class HoldView {
		TextView list_item_outmap_name;
		CheckBox list_item_outmap_isshow;
		RelativeLayout list_item_outmap;
		LinearLayout list_item_outmap_ckeck;
	}
	
	public interface IOutmap {
		void deleteOutmap(int position);
		void selectedOutmap(int position);
	}
}
