package com.robert.maps.applib.kml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.robert.maps.applib.R;

public class ImportFileListAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<File> mFiles = new ArrayList<File>();
	private int selectedIndex = -1;
	
	public ImportFileListAdapter(Context context, List<File> files) {
		mInflater = LayoutInflater.from(context);
		mFiles = files;
	}
	
	public void setmFiles(List<File> files) {
		if (files != null) {
			mFiles = files;
		}
		notifyDataSetChanged();
	}
	
	public void setSelectedIndex(int selectedIndex) {
		this.selectedIndex = selectedIndex;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mFiles == null ? 0 : mFiles.size();
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
			convertView = mInflater.inflate(R.layout.listitem_import_file, null);
			holdView = new HoldView();
			holdView.list_item_import_name = (TextView) convertView.findViewById(R.id.list_item_import_name);
			holdView.list_item_import_isshow = (CheckBox) convertView.findViewById(R.id.list_item_import_isshow);
			holdView.list_item_import = (RelativeLayout) convertView.findViewById(R.id.list_item_import);
			convertView.setTag(holdView);
		} else {
			holdView = (HoldView) convertView.getTag();
		}
		holdView.list_item_import_isshow.setVisibility(View.GONE);
		holdView.list_item_import_name.setText(mFiles.get(position).getName());
		if (position == selectedIndex) {
			holdView.list_item_import.setBackgroundResource(R.color.gainsboro);
		} else {
			holdView.list_item_import.setBackgroundResource(R.color.white);
		}
		
		
		return convertView;
	}
	
	class HoldView {
		TextView list_item_import_name;
		CheckBox list_item_import_isshow;
		RelativeLayout list_item_import;
	}
}
