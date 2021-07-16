package com.mtkj.cnpc.activity.adapter;

import java.io.File;
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

public class ProjectLayerListAdapter extends BaseAdapter {
	
	private LayoutInflater mInflater;
	private List<File> mFiles = new ArrayList<File>();
	private IProjectLayer mProjectLayer;

	public ProjectLayerListAdapter(Context context, List<File> files, IProjectLayer projectLayer) {
		mInflater = LayoutInflater.from(context);
		mFiles = files;
		mProjectLayer = projectLayer;
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
		HoldView holdView = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listitem_outmap, null);
			holdView = new HoldView();
			holdView.list_item_outmap_name = (TextView) convertView.findViewById(R.id.list_item_outmap_name);
			holdView.list_item_outmap_isshow = (CheckBox) convertView.findViewById(R.id.list_item_outmap_isshow);
			holdView.list_item_outmap = (RelativeLayout) convertView.findViewById(R.id.list_item_outmap);
			convertView.setTag(holdView);
		} else {
			holdView = (HoldView) convertView.getTag();
		}
		
		File file = mFiles.get(position);
		holdView.list_item_outmap_name.setText(file.getName());
		holdView.list_item_outmap.setBackgroundResource(R.drawable.selector_background);
		holdView.list_item_outmap_isshow.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					if (mProjectLayer != null) {
						mProjectLayer.selectedProjectLayer(position);
					}
				} else {
					if (mProjectLayer != null) {
						mProjectLayer.cancelProjectLayer(position);
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
	}
	
	public interface IProjectLayer {
		void selectedProjectLayer(int position);
		void cancelProjectLayer(int position);
		void deleteProjectLayer(int position);
	}
}
