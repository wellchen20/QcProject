package com.mtkj.cnpc.activity;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.protobuf.ByteString;
import com.mtkj.cnpc.activity.service.SendService;
import com.mtkj.cnpc.protocol.bean.CarTrave;
import com.mtkj.cnpc.protocol.constants.DSSProtoDataConstants.ProtoMsgType;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_DeviceTrace;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_Head;
import com.mtkj.cnpc.protocol.socket.DataProcess;
import com.mtkj.cnpc.protocol.utils.SocketUtils;
import com.mtkj.cnpc.R;
import com.robert.maps.applib.utils.DialogUtils;
import com.robert.maps.applib.utils.TimeUtil;

/***
 * 车辆四汇报界面
 * 
 * @author TNT
 * 
 */
public class WorkTypeChoose extends BaseActivity implements OnClickListener {

	private Button btn_start_off, btn_arrived_at, btn_get_back, btn_end_off;
	private Drawable finish_drawable;
	private Drawable unfinish_drawable;
	
	private CarTrave carTrave;
	private boolean isArrived = false;
	private boolean isBack = false;
	private boolean isEnd = false;
	
	private boolean isFinish = false;
	
	public static class Trave {
		public final static int STARTOFF = 0X01;
		public final static int ARRIVEDAT = 0X02;
		public final static int GETBACK = 0x03;
		public final static int ENDOFF = 0x04;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mLocationListener = new SampleLocationListener();
		carTrave = new CarTrave();
		
		initViews();
		
		isFinish = getData("isFinish", false);
		isArrived = getData("isArrived", false);
		isBack = getData("isBack", false);
		isEnd = getData("isEnd", false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		keyBackClickCount = 0;
		if (!isFinish) {
			carTrave = mPointDBDao.selectLastCarTrave();
		}
		if (carTrave != null && !"".equals(carTrave.id)) {
		} else {
			carTrave = new CarTrave();
		}
		initUpdateViews();
		finishWork();
		
		DataProcess.GetInstance().setMsgHandler(new Handler());
		mLocationListener.getBestProvider();
		timer = new Timer(true);
		timerTask = new MyTimerTask();
		timer.schedule(timerTask, 0, SysConfig.GPS_UP_TIME_TIP * 1000);
	}
	
	private void finishWork() {
		if (Integer.valueOf(carTrave.start_isUpload) == 1) {
			if (Integer.valueOf(carTrave.arrived_isUpload) == 1) {
				if (Integer.valueOf(carTrave.back_isUpload) == 1) {
					if (Integer.valueOf(carTrave.end_isUpload) == 1) {
						DialogUtils.alertInfo(WorkTypeChoose.this, "", "本次四汇报已全部提交完成");
						
						isArrived = false;
						isBack = false;
						isEnd = false;
						
						btn_start_off.setCompoundDrawables(null, null, unfinish_drawable, null);
						btn_arrived_at.setCompoundDrawables(null, null, unfinish_drawable, null);
						btn_get_back.setCompoundDrawables(null, null, unfinish_drawable, null);
						btn_end_off.setCompoundDrawables(null, null, unfinish_drawable, null);
						
						isFinish = true;
						setData("isFinish", isFinish);
						setData("isArrived", isArrived);
						setData("isBack", isBack);
						setData("isEnd", isEnd);
						carTrave = new CarTrave();
					}
				}
			}
		}
	}
	
	private void initUpdateViews() {
		if (Integer.valueOf(carTrave.start_isUpload) == 1) {
			btn_start_off.setCompoundDrawables(null, null, finish_drawable, null);
		} else {
			btn_start_off.setCompoundDrawables(null, null, unfinish_drawable, null);
		}
		if (Integer.valueOf(carTrave.arrived_isUpload) == 1) {
			btn_arrived_at.setCompoundDrawables(null, null, finish_drawable, null);
		} else {
			btn_arrived_at.setCompoundDrawables(null, null, unfinish_drawable, null);
		}
		if (Integer.valueOf(carTrave.back_isUpload) == 1) {
			btn_get_back.setCompoundDrawables(null, null, finish_drawable, null);
		} else {
			btn_get_back.setCompoundDrawables(null, null, unfinish_drawable, null);
		}
		if (Integer.valueOf(carTrave.end_isUpload) == 1) {
			btn_end_off.setCompoundDrawables(null, null, finish_drawable, null);
		} else {
			btn_end_off.setCompoundDrawables(null, null, unfinish_drawable, null);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		setData("isFinish", isFinish);
		setData("isArrived", isArrived);
		setData("isBack", isBack);
		setData("isEnd", isEnd);
		
		if (timer != null) {
			 timer.cancel();
			 timer.purge();
			 timer = null;
			 timerTask.cancel();
			 timerTask = null;
		 }
	}
	
	private void initViews() {
		setContentView(R.layout.activity_work_type);
		
		btn_start_off = (Button) findViewById(R.id.btn_start_off);
		btn_arrived_at = (Button) findViewById(R.id.btn_arrived_at);
		btn_get_back = (Button) findViewById(R.id.btn_get_back);
		btn_end_off = (Button) findViewById(R.id.btn_end_off);
		
		btn_start_off.setOnClickListener(this);
		btn_arrived_at.setOnClickListener(this);
		btn_get_back.setOnClickListener(this);
		btn_end_off.setOnClickListener(this);
		
		finish_drawable = getResources().getDrawable(R.drawable.btn_check_buttonless_on);
		finish_drawable.setBounds(0, 0, finish_drawable.getMinimumWidth(), finish_drawable.getMinimumHeight());
		unfinish_drawable = getResources().getDrawable(R.drawable.btn_check_buttonless_off);
		unfinish_drawable.setBounds(0, 0, unfinish_drawable.getMinimumWidth(), unfinish_drawable.getMinimumHeight());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_start_off:
			Intent startIntent = new Intent(WorkTypeChoose.this, StartOffActivity.class);
			startIntent.putExtra("id", carTrave.id);
			startIntent.putExtra("plate", carTrave.carnum);
			startIntent.putExtra("driver", carTrave.driver);
			startIntent.putExtra("destination", carTrave.destination);
			startIntent.putExtra("task", carTrave.task);
			
			startIntent.putExtra("place", carTrave.start_place);
			startIntent.putExtra("peopleNum", carTrave.start_peoplenum);
			startIntent.putExtra("time", carTrave.start_time);
			startIntent.putExtra("remark", carTrave.start_remark);
			startIntent.putExtra("estimated_arrived_time", carTrave.estimated_arrived_time);
			startActivityForResult(startIntent, Trave.STARTOFF);
			break;
			
		case R.id.btn_arrived_at:
			if (isArrived) {
				Intent arrivedIntent = new Intent(WorkTypeChoose.this, ArrivedAtActivity.class);
				arrivedIntent.putExtra("start_time", carTrave.start_time);
				arrivedIntent.putExtra("plate", carTrave.carnum);
				arrivedIntent.putExtra("driver", carTrave.driver);
				arrivedIntent.putExtra("task", carTrave.task);
				
				arrivedIntent.putExtra("peopleNum", carTrave.arrived_peoplenum);
				arrivedIntent.putExtra("place", carTrave.arrived_place);
				arrivedIntent.putExtra("time", carTrave.arrived_time);
				arrivedIntent.putExtra("remark", carTrave.arrived_remark);
				startActivityForResult(arrivedIntent, Trave.ARRIVEDAT);
			}
			break;
			
		case R.id.btn_get_back:
			if (isBack) {
				Intent backIntent = new Intent(WorkTypeChoose.this, GetBackActivity.class);
				backIntent.putExtra("start_time", carTrave.start_time);
				backIntent.putExtra("plate", carTrave.carnum);
				backIntent.putExtra("driver", carTrave.driver);
				backIntent.putExtra("task", carTrave.task);
				
				backIntent.putExtra("time", carTrave.back_time);
				backIntent.putExtra("peopleNum", carTrave.back_peoplenum);
				backIntent.putExtra("place", carTrave.back_place);
				backIntent.putExtra("remark", carTrave.back_remark);
				backIntent.putExtra("estimated_return_time", carTrave.estimated_return_time);
				startActivityForResult(backIntent, Trave.GETBACK);
			}
			break;
			
		case R.id.btn_end_off:
			if (isEnd) {
				Intent endIntent = new Intent(WorkTypeChoose.this, EndOffActivity.class);
				endIntent.putExtra("start_time", carTrave.start_time);
				endIntent.putExtra("plate", carTrave.carnum);
				endIntent.putExtra("driver", carTrave.driver);
				endIntent.putExtra("task", carTrave.task);
				
				endIntent.putExtra("peopleNum", carTrave.end_peoplenum);
				endIntent.putExtra("place", carTrave.end_place);
				endIntent.putExtra("time", carTrave.end_time);
				endIntent.putExtra("remark", carTrave.end_remark);
				startActivityForResult(endIntent, Trave.ENDOFF);
			}
			break;
			
		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case Trave.STARTOFF:
			if (resultCode == RESULT_OK) {
				isArrived = true;
				
//				carTrave = mPointDBDao.selectLastCarTrave();
//				btn_start_off.setCompoundDrawables(null, null, finish_drawable, null);
				
				isFinish = false;
			}
			break;

		case Trave.ARRIVEDAT:
			if (resultCode == RESULT_OK) {
				isBack = true;
				
//				carTrave = mPointDBDao.selectLastCarTrave();
//				btn_arrived_at.setCompoundDrawables(null, null, finish_drawable, null);
				
				isFinish = false;
			}
			break;
			
		case Trave.GETBACK:
			if (resultCode == RESULT_OK) {
				isEnd = true;
				
//				carTrave = mPointDBDao.selectLastCarTrave();
//				btn_get_back.setCompoundDrawables(null, null, finish_drawable, null);
				
				isFinish = false;
			}
			break;
			
		case Trave.ENDOFF:
			if (resultCode == RESULT_OK) {
				finishWork();
//				if (isFinish) {
//					DialogUtils.alertInfo(WorkTypeChoose.this, "", "本次四汇报已全部提交完成");
//					
//					isArrived = false;
//					isBack = false;
//					isEnd = false;
//					
//					btn_start_off.setCompoundDrawables(null, null, unfinish_drawable, null);
//					btn_arrived_at.setCompoundDrawables(null, null, unfinish_drawable, null);
//					btn_get_back.setCompoundDrawables(null, null, unfinish_drawable, null);
//					
//					isFinish = true;
//					carTrave = new CarTrave();
//				} else {
//					isArrived = true;
//					isBack = true;
//					isEnd = true;
//					
//					isFinish = false;
//				}
			}
			break;
		}
	}
	
	private int keyBackClickCount = 0;
	
	@Override
	public void onBackPressed() {
		switch (keyBackClickCount++) {
		case 0:
			Toast.makeText(this,
					getResources().getString(R.string.press_again_exit),
					Toast.LENGTH_SHORT).show();
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					keyBackClickCount = 0;
				}
			}, 2000);
			break;
		case 1:
			super.onBackPressed();
			break;
		default:
			break;
		}
	}
	
	private Timer timer;
	private MyTimerTask timerTask;
	
	class MyTimerTask extends TimerTask {

		@Override
		public void run() {
			// 发送当前位置信息
			sendLocationTo();
		}
	};

	private void sendLocationTo() {
		if (unRectifyLocation != null) {
			if (!DataProcess.GetInstance().isConnected()) {
				if (!DataProcess.isConning) {
//					startService(new Intent(WorkTypeChoose.this, SendService.class));
				}
			} else {
					if (DataProcess.isLoginDscloud) {
						sendLocation(unRectifyLocation);
					}
			}
		}
	}
	private void sendLocation(final Location location) {
		new SendLocationTask(location).execute("");
	}
	
	public class SendLocationTask extends AsyncTask<String, Integer, Boolean> {
		
		private Location location;
		
		public SendLocationTask(Location params) {
			location = params;
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			try {
				Proto_DeviceTrace proto_DeviceTrace = Proto_DeviceTrace
						.newBuilder()
						.setDeviceName(ByteString.copyFrom(SysConfig.SC, "GB2312"))
						.setDataTime(ByteString.copyFrom(TimeUtil.getCurrentTimeInString(), "GB2312"))
						.setX(location.getLongitude())
						.setY(location.getLatitude())
						.setZ(location.getAltitude())
						.build();
				Proto_Head head = Proto_Head
						.newBuilder()
						.setProtoMsgType(ProtoMsgType.protoMsgType_HandsetTrace)
						.setCmdSize(proto_DeviceTrace.toByteArray().length)
						.addReceivers(ByteString.copyFrom("", "GB2312"))
						.setReceivers(0, ByteString.copyFrom("", "GB2312"))
						.setSender(
								ByteString.copyFrom(SysConfig.SC, "GB2312"))
						.setMsgId(0).setPriority(1).setExpired(0).build();
				DataProcess.GetInstance().sendData(
							SocketUtils.writeBytes(head.toByteArray(),
									proto_DeviceTrace.toByteArray()));
			} catch (Exception e) {
				e.printStackTrace();
				DataProcess.GetInstance().onConnBreak();
			}
			return null;
		}
		
		
	}

	private boolean mGPSFastUpdate;
	private SampleLocationListener mLocationListener, mNetListener;
	private String mGpsStatusName = "";
	public static Location unRectifyLocation;
	
	private class SampleLocationListener implements LocationListener {
		public static final String OFF = "off";

		public void onLocationChanged(Location loc) {
			unRectifyLocation = loc;
			if (loc.getProvider().equals(LocationManager.GPS_PROVIDER) && mNetListener != null) {
				getLocationManager().removeUpdates(mNetListener);
				mNetListener = null;
				mGpsStatusName = LocationManager.GPS_PROVIDER;
			}
		}

		public void onProviderDisabled(String provider) {
			if(provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER) && mNetListener != null)
				mGpsStatusName = LocationManager.NETWORK_PROVIDER;
			else
				mGpsStatusName = OFF;
			
			if(provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER) && mNetListener != null) {
				getLocationManager().removeUpdates(mNetListener);
				mNetListener = null;
				if(getLocationManager().isProviderEnabled(LocationManager.GPS_PROVIDER))
					mGpsStatusName = LocationManager.GPS_PROVIDER;
				else
					mGpsStatusName = OFF;
			}
		}

		public void onProviderEnabled(String provider) {
			if(provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER) && mNetListener == null)
				mGpsStatusName = LocationManager.GPS_PROVIDER;
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			mGpsStatusName = provider;
		}
		
		private LocationManager getLocationManager() {
			return (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}

		/**
		 * 我的位置刷新获取
		 */
		private void getBestProvider() {
			int minTime = 0;
			int minDistance = 0;
			final LocationManager lm = getLocationManager();
			final List<String> listProviders = lm.getAllProviders();
			mGpsStatusName = OFF;
			
			// 设定GPS更新参数
			if (!mGPSFastUpdate) {
//				minTime = 2000;
//				minDistance = 20;
				
				minTime = 1 * 1000;
				minDistance = 5;
			}
			
			lm.removeUpdates(mLocationListener);
			
			if (mNetListener != null)
				lm.removeUpdates(mNetListener);
			
			String bestProvider = lm.getBestProvider(getCriteria(), true);
			if (listProviders.contains(LocationManager.GPS_PROVIDER) && lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, mLocationListener);
				mGpsStatusName = LocationManager.GPS_PROVIDER;
				try {
					if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
						mNetListener = new SampleLocationListener();
						lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, mNetListener);
						mGpsStatusName = LocationManager.NETWORK_PROVIDER;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (listProviders.contains(LocationManager.NETWORK_PROVIDER) && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, mLocationListener);
				mGpsStatusName = LocationManager.NETWORK_PROVIDER;
			} else {
				lm.requestLocationUpdates(bestProvider, minTime, minDistance, mLocationListener);
				mGpsStatusName = bestProvider;
			}
		}

		private Criteria getCriteria() {
			Criteria criteria = new Criteria();
			// 设置定位精度Criteria.ACCURACY_COARSE 比较粗略，Criteria.ACCURACY_FINE比较精细
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			// 设置是否要求速度
			criteria.setSpeedRequired(true);
			// 设置是否运营商收费
			criteria.setCostAllowed(false);
			// 设置是否需要方位
			criteria.setBearingRequired(true);
			// 设置是否需要海拔
			criteria.setAltitudeRequired(true);
			// 设置对电源需求
			criteria.setPowerRequirement(Criteria.POWER_LOW);
			return criteria;
		}
	}

	@Override
	protected void onDestroy() {
		DataProcess.GetInstance().stopConn();
		super.onDestroy();
	}
}
