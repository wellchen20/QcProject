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
import com.robert.maps.applib.kml.PoiPoint;

public class InterestListAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<PoiPoint> mPoints = new ArrayList<PoiPoint>();
	private IInterest mInterest;
	
	public InterestListAdapter(Context context, List<PoiPoint> poiPoints, IInterest interest) {
		mInflater = LayoutInflater.from(context);
		mPoints = poiPoints;
		mInterest = interest;
	}
	
	public void setmPoints(List<PoiPoint> points) {
		if (points != null) {
			mPoints.clear();
			for(PoiPoint poiPoint : points) {
				mPoints.add(poiPoint);
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mPoints == null ? 0 : mPoints.size();
	}

	@Override
	public Object getItem(int position) {
		return mPoints.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final HoldView holdView = new HoldView();
//		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listitem_interest, null);
			holdView.list_item_interest_name = (TextView) convertView.findViewById(R.id.list_item_interest_name);
			holdView.list_item_interest_isshow = (CheckBox) convertView.findViewById(R.id.list_item_interest_isshow);
			holdView.list_item_interest = (RelativeLayout) convertView.findViewById(R.id.list_item_interest);
			holdView.list_item_interest_check = (LinearLayout) convertView.findViewById(R.id.list_item_interest_check);
			convertView.setTag(holdView);
//		} else {
//			holdView = (HoldView) convertView.getTag();
//		}
		
		holdView.list_item_interest.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		holdView.list_item_interest.setBackgroundResource(R.drawable.selector_background);
		PoiPoint point = mPoints.get(position);
		holdView.list_item_interest_name.setText(point.Title);
		holdView.list_item_interest_isshow.setChecked(!point.Hidden);
		holdView.list_item_interest_check.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean isChecked = !holdView.list_item_interest_isshow.isChecked();
				if (mInterest != null) {
					mInterest.isShowInterest(position, isChecked);
				}
			}
		});
		holdView.list_item_interest_isshow.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (mInterest != null) {
					mInterest.isShowInterest(position, isChecked);
				}
			}
		});
		return convertView;
	}
	
	class HoldView {
		TextView list_item_interest_name;
		CheckBox list_item_interest_isshow;
		RelativeLayout list_item_interest;
		LinearLayout list_item_interest_check;
	}
	
	public interface IInterest {
		void isShowInterest(int position, boolean isShow);
		void editInterest(int position);
	}

}
