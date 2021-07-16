package com.mtkj.cnpc.activity.fragment;

import org.andnav.osm.util.GeoPoint;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mtkj.cnpc.activity.MainActivity;
import com.mtkj.cnpc.activity.MainActivity.RequestCode;
import com.mtkj.cnpc.R;
import com.robert.maps.applib.kml.PoiManager;
import com.robert.maps.applib.kml.PoiPoint;

/***
 * 
 * 
 * @author TNT
 * 兴趣点
 */
public class BottomPoiShowFragment extends BaseFragment implements
		OnClickListener {
	
	/** 兴趣点数据管理 */
	private View rootView;
	private GeoPoint mapPoint2d;
	private TextView favNameTextView;
	private TextView longitude;
	private TextView latitude;
	private PoiManager mPoiManager;
	private PoiPoint mPoiPoint;
	private ImageView addFav;

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			refreshView();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_bottom_poi_show, container,
				false);
		mPoiManager = new PoiManager(getActivity());
		initView();
		refreshView();
		return rootView;
	}

	public void initView() {
		favNameTextView = (TextView) rootView.findViewById(R.id.fav_name);
//		favNameTextView.setVisibility(View.GONE);
		longitude = (TextView) rootView.findViewById(R.id.txt_longitude);
		latitude = (TextView) rootView.findViewById(R.id.txt_latitude);
		addFav = (ImageView) rootView.findViewById(R.id.poi_coll);
		addFav.setOnClickListener(this);
		rootView.findViewById(R.id.poi_guide).setOnClickListener(this);
	}

	/***
	 * 刷新poi数据
	 * 
	 * @param recordInfo
	 * @param isCenter
	 */
	public void refreshGeoData(PoiPoint poiPoint) {
		this.mPoiPoint = poiPoint;
		if (isAdded() && !isHidden()) {
			refreshView();
		}
	}
	
	public void refreshView() {
		if(mPoiManager.isHasPoiPoint(mPoiPoint.getId())){
			addFav.setImageDrawable(getResources().getDrawable(R.drawable.menu_collect_pre));
		} else {
			addFav.setImageDrawable(getResources().getDrawable(R.drawable.menu_collect));
		}
		if (mPoiPoint != null) {
			refreshText();
		}
	}

	public void refreshText() {
		rootView.findViewById(R.id.poi_guide).setVisibility(View.VISIBLE);
		if (mPoiPoint.Title == null || "".equals(mPoiPoint.Title)) {
			favNameTextView.setVisibility(View.GONE);
		} else {
			if ("定位".equals(mPoiPoint.Title)) {
				rootView.findViewById(R.id.poi_guide).setVisibility(View.GONE);
				favNameTextView.setVisibility(View.GONE);
			} else {
				favNameTextView.setVisibility(View.VISIBLE);
				favNameTextView.setText(mPoiPoint.Title);
			}
		}
		
		if (this.mPoiPoint.GeoPoint != null) {
			mapPoint2d = this.mPoiPoint.GeoPoint;
			longitude.setText(mapPoint2d.getLongitude() + "");
			latitude.setText(mapPoint2d.getLatitude() + "");
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.poi_guide: {// 导航到此
			if (MainActivity.unRectifyLocation == null) {
				showMessage("没有定位,不能实行导航！");
				return;
			}
			
			Message message = new Message();
			message.what = RequestCode.POI_GUIDE;
			message.obj = mPoiPoint;
			handler.sendMessage(message);
		}
			break;
		case R.id.poi_coll:
			
			Message message = new Message();
			message.what = RequestCode.COLLECT_POI;
			message.obj = mPoiPoint;
			handler.sendMessage(message);
			
			break;
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

}
