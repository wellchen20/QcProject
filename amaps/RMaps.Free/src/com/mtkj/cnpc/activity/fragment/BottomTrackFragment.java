package com.mtkj.cnpc.activity.fragment;

import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.mtkj.cnpc.activity.MainActivity.RequestCode;
import com.mtkj.cnpc.R;


/***
 * 
 * 
 * @author TNT
 * 轨迹
 */
public class BottomTrackFragment extends BaseFragment implements OnClickListener {
	
	private ImageView close;
	private Button startButton, pauseButton, stopButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.track_info_box, null);
		initView(rootView);
		return rootView;
	}

	private void initView(View rootView) {
		close = (ImageView) rootView.findViewById(R.id.close);
		close.setOnClickListener(this);
		startButton = (Button) rootView.findViewById(R.id.startButton);
		startButton.setOnClickListener(this);
		pauseButton = (Button) rootView.findViewById(R.id.pauseButton);
		pauseButton.setOnClickListener(this);
		stopButton = (Button) rootView.findViewById(R.id.stopButton);
		stopButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Message msg = new Message();
		switch (v.getId()) {
		case R.id.close:
			msg.what = RequestCode.CLOSE_TRACK;
			handler.sendMessage(msg);
			break;

		case R.id.startButton:
			startButton.setSelected(true);
			startButton.setClickable(false);
			pauseButton.setSelected(false);
			pauseButton.setClickable(true);
			
//			if (MainActivity.unRectifyLocation != null) {
//				getActivity().startService(new Intent("com.robert.maps.trackwriter"));// 开始记录轨迹
//				MainActivity.isTracked = true;
//			}
			msg.what = RequestCode.START_TRACK;
			handler.sendMessage(msg);
			break;
			
		case R.id.pauseButton:
			startButton.setSelected(false);
			startButton.setClickable(true);
			pauseButton.setSelected(true);
			pauseButton.setClickable(false);
			
//			if (MainActivity.isTracked) {
//				getActivity().stopService(new Intent("com.robert.maps.trackwriter"));// 暂停记录轨迹
//			}
			
			msg.what = RequestCode.PAUSE_TRACK;
			handler.sendMessage(msg);
			break;
			
		case R.id.stopButton:
			startButton.setSelected(false);
			startButton.setClickable(true);
			pauseButton.setSelected(false);
			pauseButton.setClickable(true);
			stopButton.setSelected(false);
			stopButton.setClickable(true);
			
//			if (MainActivity.isTracked) {
//				getActivity().stopService(new Intent("com.robert.maps.trackwriter"));// 结束记录轨迹
//				MainActivity.isTracked = false;
//			}
			
			msg.what = RequestCode.STOP_TRACK;
			handler.sendMessage(msg);
			break;
		}
	}
}
