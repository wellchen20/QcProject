package com.mtkj.cnpc.activity.adapter;

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
import com.robert.maps.applib.kml.Track;

public class TracksListAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<Track> mTracks = new ArrayList<Track>();
	private ITrack mTrack;
	
	public TracksListAdapter(Context context, List<Track> tracks, ITrack track) {
		mInflater = LayoutInflater.from(context);
		mTracks = tracks;
		mTrack = track;
	}
	
	public void setmTracks(List<Track> tracks) {
		if (tracks != null) {
			mTracks.clear();
			for (Track track : tracks) {
				mTracks.add(track);
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mTracks == null ? 0 : mTracks.size();
	}

	@Override
	public Object getItem(int position) {
		return mTracks.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final HoldView holdView = new HoldView();
//		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listitem_tracks, null);
//			holdView = new HoldView();
			holdView.list_item_track_name = (TextView) convertView.findViewById(R.id.list_item_track_name);
			holdView.list_item_track_isshow = (CheckBox) convertView.findViewById(R.id.list_item_track_isshow);
			holdView.list_item_track = (RelativeLayout) convertView.findViewById(R.id.list_item_track);
			holdView.list_item_track_check = (LinearLayout) convertView.findViewById(R.id.list_item_track_check);
			convertView.setTag(holdView);
//		} else {
//			holdView = (HoldView) convertView.getTag();
//		}
		
		holdView.list_item_track.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		holdView.list_item_track.setBackgroundResource(R.drawable.selector_background);
		Track track = mTracks.get(position);
		holdView.list_item_track_name.setText(track.Name);
		holdView.list_item_track_isshow.setChecked(track.Show);
		holdView.list_item_track_check.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean isChecked = !holdView.list_item_track_isshow.isChecked();
				if (mTrack != null) {
					mTrack.isShowTrack(position, isChecked);
				}
			}
		});
		holdView.list_item_track_isshow.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (mTrack != null) {
					mTrack.isShowTrack(position, isChecked);
				}
			}
		});
		return convertView;
	}
	
	class HoldView {
		TextView list_item_track_name;
		CheckBox list_item_track_isshow;
		RelativeLayout list_item_track;
		LinearLayout list_item_track_check;
	}
	
	public interface ITrack {
		void isShowTrack(int position, boolean isShow);
		void editTrack(int position);
	}
}
