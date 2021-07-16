package com.mtkj.cnpc.activity.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mtkj.cnpc.activity.MainActivity;
import com.mtkj.cnpc.activity.MainActivity.RequestCode;
import com.mtkj.cnpc.activity.SeeDrillActivity;
import com.mtkj.cnpc.protocol.bean.DrillPoint;
import com.mtkj.cnpc.protocol.bean.TaskPoint;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.constants.SysConfig.WorkType;
import com.mtkj.cnpc.protocol.utils.MathUtils;
import com.mtkj.cnpc.sqlite.PointDBDao;
import com.mtkj.cnpc.R;

/***
 * 
 * 
 * @author TNT
 * 钻井下药
 */
public class BottomTaskShowFragment extends BaseFragment implements
		OnClickListener {

	public View groupView = null;
	private TextView mainTextView = null;
	private TextView otherTextView = null;
	private LinearLayout detailsTextView = null;
	private TextView favButton = null;
	
	public PointDBDao mPointDBDao;
	private TaskPoint mTaskPoint;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPointDBDao = new PointDBDao(getActivity());
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
		View rootView = inflater.inflate(R.layout.fragment_bottom_task_show, container, false);
		initView(rootView);
		refreshView();
		return rootView;
	}

	public void initView(View rootView) {
		try {
			groupView = rootView;
			
			detailsTextView = (LinearLayout) groupView.findViewById(R.id.txt_details);
			mainTextView = (TextView) groupView.findViewById(R.id.txt_info);
			otherTextView = (TextView) groupView.findViewById(R.id.txt_info_two);
			favButton = (TextView) groupView.findViewById(R.id.btn_add_to_fav);
			groupView.findViewById(R.id.btn_search_around).setOnClickListener(this);
			groupView.findViewById(R.id.btn_guide_to_this).setOnClickListener(this);
			
			detailsTextView.setOnClickListener(this);
			favButton.setOnClickListener(this);
			if (SysConfig.workType == SysConfig.WorkType.WORK_TYPE_DRILE){
				favButton.setText("录入信息");
			}else if (SysConfig.workType == WorkType.WORK_TYPE_SHOT){
				favButton.setText("开始匹配");
			}else if (SysConfig.workType == WorkType.WORK_TYPE_ARRANGE){
				favButton.setText("录入");
			}
		} catch (Exception e) {
		}
	}

	public void hideFaveBtn() {
		if (favButton.getVisibility() == View.VISIBLE) {
			favButton.setVisibility(View.GONE);
			groupView.findViewById(R.id.line_split_2).setVisibility(View.GONE);
		}
	}

	public void showFaveBtn() {
		if (favButton.getVisibility() == View.GONE) {
			favButton.setVisibility(View.VISIBLE);
			groupView.findViewById(R.id.line_split_2).setVisibility(
					View.VISIBLE);
		}
	}

	public void hideDetailsBtn() {
		if (detailsTextView.getVisibility() == View.VISIBLE) {
			detailsTextView.setVisibility(View.GONE);
			groupView.findViewById(R.id.line_split_1).setVisibility(View.GONE);
		}
	}

	public void showDetailsBtn() {
		if (detailsTextView.getVisibility() == View.GONE) {
			detailsTextView.setVisibility(View.VISIBLE);
			groupView.findViewById(R.id.line_split_1).setVisibility(
					View.VISIBLE);
		}
	}

	/***
	 * 刷新poi数据
	 * 
	 * @param
	 * @param
	 */
	public void refreshGeoData(TaskPoint taskPoint) {
		this.mTaskPoint = taskPoint;
		if (isAdded() && !isHidden()) {
			refreshView();
		}
	}

	public void refreshButton() {
		showFaveBtn();
	}

	public void refreshView() {
		if (mTaskPoint != null) {
			refreshButton();
			refreshText();
			if (SysConfig.workType == SysConfig.WorkType.WORK_TYPE_DRILE){
				favButton.setText("录入信息");
			}else if (SysConfig.workType == WorkType.WORK_TYPE_SHOT){
				favButton.setText("开始匹配");
			}else if (SysConfig.workType == WorkType.WORK_TYPE_ARRANGE){
				favButton.setText("录入");
			}
		}
	}

	public void refreshText() {
		if (this.mTaskPoint != null) {
			String otherInfo = "X="
					+ MathUtils.GetAccurateNumberText(mTaskPoint.geoPoint.getLongitude(), 6)
					+ "  " + " Y="
					+ MathUtils.GetAccurateNumberText(mTaskPoint.geoPoint.getLatitude(), 6);;
			mainTextView.setText(this.mTaskPoint.stationNo);
			otherTextView.setText(otherInfo);
		}
	}

	public void showView() {
		if (groupView.getVisibility() != View.VISIBLE) {
			groupView.setVisibility(View.VISIBLE);
		}
	}

	public boolean isShow() {
		return groupView.getVisibility() == View.VISIBLE;
	}

	public void hideView() {
		if (groupView.getVisibility() != View.GONE) {
			groupView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.txt_details: {
			// 进入详细界面
			if (SysConfig.workType == WorkType.WORK_TYPE_DRILE) {
				DrillPoint drillPoint = mPointDBDao.selectDrillPoint(mTaskPoint.stationNo);
				if (drillPoint.isDone) {
					Intent intent = new Intent(getActivity(), SeeDrillActivity.class);
					intent.putExtra("drillPoint", mTaskPoint.stationNo);
					getActivity().startActivity(intent);
				}
			}
		}
			break;
		case R.id.btn_add_to_fav: {//
			Message message = new Message();
			message.what = RequestCode.TASK_WRITE;
			message.obj = mTaskPoint;
			handler.sendMessage(message);

//			if (favButton
//					.getText()
//					.toString()
//					.equalsIgnoreCase(
//							getActivity().getString(R.string.add_to_fav))) {
//				Intent intent = new Intent(getActivity(), FavEditActivity.class);
//				intent.putExtra("NAME", recordInfo.getKeyValue());
//				if (!favManager.isExsit(recordInfo.getKeyValue())) {
//					intent.putExtra("X", recordInfo2FavEntity(recordInfo).getX());
//					intent.putExtra("Y", recordInfo2FavEntity(recordInfo).getY());
//				}
//				intent.putExtra("BACK_TEXT", "地图");
//				startActivity(intent);
//				addFavDlg(recordInfo2FavEntity(recordInfo));
//			} else {
//				if (deleteFavDef(recordInfo2FavEntity(recordInfo))) {
//					favButton.setText(getActivity().getString(
//							R.string.add_to_fav));
//					hideDetailsBtn();
//				}
//			}
		}
			break;
		case R.id.btn_guide_to_this: {// 导航到此
			if (MainActivity.unRectifyLocation == null) {
				showMessage("没有定位,不能实行导航！");
				return;
			}
			
			Message message = new Message();
			message.what = RequestCode.TASK_GUIDE;
			message.obj = mTaskPoint;
			handler.sendMessage(message);
		}
			break;
		case R.id.btn_search_around: {// 周边查询
			// Intent intent = new Intent();
			// startActivityForResult(intent, requestCode)
		}
			break;

		default:
			break;
		}
	}

}
