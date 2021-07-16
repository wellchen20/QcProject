package com.mtkj.cnpc.activity.fragment;

import org.andnav.osm.util.GeoPoint;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mtkj.cnpc.activity.MainActivity;
import com.mtkj.cnpc.activity.MainActivity.RequestCode;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.constants.SysConfig.WorkType;
import com.mtkj.cnpc.protocol.utils.MathUtils;
import com.mtkj.cnpc.R;

/***
 * 
 * 
 * @author TNT
 * 导航
 * 
 */
public class BottomGuidingFragment extends BaseFragment implements
		View.OnClickListener {

	public View groupView = null;
	private TextView mainTextView = null;
	private TextView mainTextRemarkView = null;
	private TextView otherTextView = null;
	private TextView guidingFullShow = null;
	
	private String guidName = "";
	private GeoPoint endPoint2d = null;
	private boolean isTaskPointGuiding = false;
	private double dis = -1;
	private String gpsInfo = "";
	private Location location = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

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
		View rootView = inflater.inflate(R.layout.fragment_bottom_guiding_task,
				container, false);
		initView(rootView);
		refreshView();
		return rootView;
	}

	public void initView(View rootView) {
		groupView = rootView;
		mainTextView = (TextView) groupView.findViewById(R.id.txt_info);
		mainTextRemarkView = (TextView) groupView.findViewById(R.id.txt_info_remark);
		otherTextView = (TextView) groupView.findViewById(R.id.txt_info_two);
		guidingFullShow = (TextView) groupView.findViewById(R.id.btn_full_show_guide_way);
		guidingFullShow.setOnClickListener(this);
		groupView.findViewById(R.id.btn_stop_guiding).setOnClickListener(this);
	}

	public void refreshView() {
		refreshGuideInfo();
		refreshDisInfo();
	}
	
	private void refreshGuideInfo() {
		if (endPoint2d != null) {
			gpsInfo = "X=" + MathUtils.GetAccurateNumberText(endPoint2d.getLongitude(), 6)
					+ "  " + " Y="
					+ MathUtils.GetAccurateNumberText(endPoint2d.getLatitude(), 6);
			mainTextView.setText(this.guidName);
			mainTextRemarkView.setText("(" + gpsInfo + ")");
			otherTextView.setText("GPS定位中...");
		} else {
//			MLog.e(Constants.TAG, "GEO矢量信息为空");
		}
	}
	
	public void refreshDisInfo() {
		if (dis > -1) {
			if (dis > 1000) {
				otherTextView.setText("距离目的地还有:"
						+ MathUtils.GetAccurateNumberText(dis / 1000.0d, 2)
						+ "千米");
			} else {
				otherTextView.setText("距离目的地还有:"
						+ MathUtils.GetAccurateNumberText(dis, 0) + "米");
			}
		}
	}

	public void showView() {
		if (groupView != null && groupView.getVisibility() != View.VISIBLE) {
			groupView.setVisibility(View.VISIBLE);
		}
	}

	public boolean isShow() {
		return groupView.getVisibility() == View.VISIBLE;
	}

	public void hideView() {
		if (groupView != null && groupView.getVisibility() != View.GONE) {
			groupView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_full_show_guide_way: {// 全幅路径
			if (this.location != null && endPoint2d != null) {
//				Point2D[] arrPoint2ds = new Point2D[2];
//				arrPoint2ds[0] = this.location.mapPoint2d;
//				arrPoint2ds[1] = endPoint2d;
//				GeoLine geoLine = new GeoLine().Make(arrPoint2ds);
//				MapOperate.mMapView.SetViewBounds(geoLine.GetBounds());
//				MapOperate.ZoomOut(MapOperate.mMapView);
//				MapOperate.mMapView.Refresh();
			} else {
//				Util.MsgBox(getActivity(), "GPS未定位");
			}
		}
			break;
		case R.id.btn_stop_guiding: {// 结束导航
			isStopGuiding();
		}
			break;

		default:
			break;
		}
	}

	public void startGuiding(String name, GeoPoint endPoint2d, boolean isTaskPoint) {
		this.guidName = name;
		this.endPoint2d = endPoint2d;
		this.isTaskPointGuiding = isTaskPoint;
		dis = endPoint2d.distanceTo(MainActivity.unRectifyLocation.getLatitude(), MainActivity.unRectifyLocation.getLongitude());
		
		if (isAdded() && !isHidden()) {
			refreshView();
		}
	}

	public void stopGuiding(String name, GeoPoint endPoint2d, boolean isTaskPoint) {
		this.guidName = name;
		this.endPoint2d = endPoint2d;
		this.isTaskPointGuiding = isTaskPoint;
	}

	public void onGuidingRefresh(Location location) {
		this.location = location;
		dis = endPoint2d.distanceTo(location.getLatitude(), location.getLongitude());
		if (dis > -1) {
			if (isAdded() && !isHidden()) {
				refreshView();
			}
		}
	}

	/**
	 * 是否退出系统
	 * 
	 */
	public void isStopGuiding() {
		if (isTaskPointGuiding && dis > SysConfig.ShotproMax) {
			isStillStopGuiding();
		} else {
			isStopGuidingNormal();
		}
	}

	public void isStopGuidingNormal() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("提示");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (isTaskPointGuiding) {
					Message msg = new Message();
					msg.what = RequestCode.STOP_GUIDE;
					msg.obj = guidName;
					handler.sendMessage(msg);
					
					if (SysConfig.workType == WorkType.WORK_TYPE_DRILE) {
					} else if (SysConfig.workType == WorkType.WORK_TYPE_SHOT) {
					}
				} else {
					Message msg = new Message();
					msg.what = RequestCode.STOP_GUIDE;
					msg.obj = "";
					handler.sendMessage(msg);
				}
			}
		});
		builder.setNegativeButton("取消", null);
		builder.setMessage("是否要结束引导？");
		builder.show();
	}

	/**
	 * 是否执意退出系统
	 * 
	 */
	public void isStillStopGuiding() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("提示");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (isTaskPointGuiding) {
					Message msg = new Message();
					msg.what = RequestCode.STOP_GUIDE;
					msg.obj = guidName;
					handler.sendMessage(msg);
					
					if (SysConfig.workType == WorkType.WORK_TYPE_DRILE) {
					} else if (SysConfig.workType == WorkType.WORK_TYPE_SHOT) {
					}
				} else {
					Message msg = new Message();
					msg.what = RequestCode.STOP_GUIDE;
					msg.obj = "";
					handler.sendMessage(msg);
				}
			}
		});
		builder.setNegativeButton("取消", null);
		builder.setMessage("距离目的桩号距离大于匹配距离" + SysConfig.ShotproMax + "米" + "\n"
				+ "确定要结束引导？");
		builder.show();
	}

	@Override
	public void onActivityResult(int arg0, int arg1, Intent arg2) {
		super.onActivityResult(arg0, arg1, arg2);
		switch (arg0) {
		case 1:
//			if (arg1 == Constants.RESULT.QUERENINFO) {
//				MapOperate.mMapView.getGuidingManager().stopGuiding();
//			}
			break;
		}
	}
}
