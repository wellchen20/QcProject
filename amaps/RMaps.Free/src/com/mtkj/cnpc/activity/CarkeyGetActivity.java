package com.mtkj.cnpc.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mtkj.cnpc.protocol.bean.CarKey;
import com.mtkj.cnpc.protocol.constants.SysContants;
import com.mtkj.cnpc.protocol.socket.DataProcess;
import com.mtkj.utils.zxing.camera.MipcaActivityCapture;
import com.mtkj.cnpc.R;
import com.robert.maps.applib.utils.TimeUtil;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CarkeyGetActivity extends BaseActivity implements OnClickListener {

	private LinearLayout ll_getcarkey_carnum, ll_getcarkey_driver, ll_getcarkey_time;
	private TextView tv_getcarkey_carnum, tv_getcarkey_driver, tv_getcarkey_time;
	private Button btn_getcarkey_ok;
	
	private String carnum, driver, time;
	
	private CarKey carKey;
	private ProgressDialog progressDialog;
	private boolean isUpdate = false;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if (!CarkeyGetActivity.this.isFinishing() && !progressDialog.isShowing()) {
					progressDialog.show();
				}
				break;

			case 1:
				if (!CarkeyGetActivity.this.isFinishing() && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				break;
				
			case 2:
				if (!isUpdate) {
					if (!CarkeyGetActivity.this.isFinishing() && progressDialog.isShowing()) {
						progressDialog.dismiss();
					}
					
					showMessage("提交驻地出发信息失败");
					
					Intent intent = new Intent();
					setResult(RESULT_OK, intent);
					
					CarkeyGetActivity.this.finish();
				}
				break;
				
			case DataProcess.MSG.CARKEY:
//				Proto_TravelImforResponce traveImfor  = (Proto_TravelImforResponce) msg.obj;
//				if (traveImfor != null) {
//					try {
//						String starttime = new String(traveImfor.getMarktime().toByteArray(), "GB2312");
//						carKey trave = mPointDBDao.selectcarKeyByStarttime(starttime);
//						switch (traveImfor.getTraveltype()) {
//						case 1:
//							trave.start_isUpload = "1";
//							break;
//
//						case 2:
//							trave.arrived_isUpload = "1";
//							break;
//							
//						case 3:
//							trave.back_isUpload = "1";
//							break;
//							
//						case 4:
//							trave.end_isUpload = "1";
//							break;
//						}
//						mPointDBDao.updatecarKey(trave);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
					
					isUpdate = true;
					if (!CarkeyGetActivity.this.isFinishing() && progressDialog.isShowing()) {
						progressDialog.dismiss();
					}
					showMessage("提交驻地出发信息成功");
					
					Intent intent1 = new Intent();
					setResult(RESULT_OK, intent1);
					
					CarkeyGetActivity.this.finish();
//				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_carkey_get);
		
		carKey = new CarKey();
		
		progressDialog = new ProgressDialog(CarkeyGetActivity.this);
		progressDialog.setMessage("正在提交...");
		progressDialog.setCancelable(false);
		
		initViews();
		initDatas();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		DataProcess.GetInstance().setMsgHandler(handler);
	}

	private void initViews() {
		ll_getcarkey_carnum = (LinearLayout) findViewById(R.id.ll_getcarkey_carnum);
		ll_getcarkey_carnum.setOnClickListener(this);
		ll_getcarkey_driver = (LinearLayout) findViewById(R.id.ll_getcarkey_driver);
		ll_getcarkey_driver.setOnClickListener(this);
		ll_getcarkey_time = (LinearLayout) findViewById(R.id.ll_getcarkey_time);
		ll_getcarkey_time.setOnClickListener(this);
		tv_getcarkey_carnum = (TextView) findViewById(R.id.tv_getcarkey_carnum);
		tv_getcarkey_driver = (TextView) findViewById(R.id.tv_getcarkey_driver);
		tv_getcarkey_time = (TextView) findViewById(R.id.tv_getcarkey_time);
		btn_getcarkey_ok = (Button) findViewById(R.id.btn_getcarkey_ok);
		btn_getcarkey_ok.setOnClickListener(this);
		findViewById(R.id.ll_title_back).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CarkeyGetActivity.this.finish();
			}
		});;
		((TextView) findViewById(R.id.tv_title_title)).setText(getResources().getString(R.string.get_carkey_title));
	}
	
	private void initDatas() {
		carKey.id = getIntent().getStringExtra("id");
		carnum = getIntent().getStringExtra("carnum");
		if (carnum != null && !"".equals(carnum)) {
			tv_getcarkey_carnum.setText(carnum);
		} else {
//			carnum = getData(SysContants.CARNUM, "");
			tv_getcarkey_carnum.setText("");
		}
		driver = getIntent().getStringExtra("driver");
		if (driver != null && !"".equals(driver)) {
			tv_getcarkey_driver.setText(driver);
		} else {
			driver = getData(SysContants.USERNAME, "");
			tv_getcarkey_driver.setText(driver);
		}
		time = getIntent().getStringExtra("time");
		if (time != null && !"".equals(time)) {
			tv_getcarkey_time.setText(time);
		} else {
			time = TimeUtil.getCurrentTimeInString();
			tv_getcarkey_time.setText(time);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_getcarkey_carnum:
			Intent intent = new Intent(CarkeyGetActivity.this, MipcaActivityCapture.class);
			startActivityForResult(intent, 1);
			break;
			
		case R.id.ll_getcarkey_driver:
			
			break;
			
		case R.id.ll_getcarkey_time:
			UpdateTimeDialog();
			break;
			
			
		case R.id.btn_start_ok:
			handler.sendEmptyMessage(0);
			
			if (!sureOk()) {
				handler.sendEmptyMessage(1);
				return;
			}
			saveCarkey();
			sendCarkeyData();
			
			break;
		}
	}

	private boolean sureOk() {
		boolean isOk = false;
		if (!"".equals(carnum)) {
			isOk = true;
		} else {
			Toast.makeText(CarkeyGetActivity.this, "请输入车牌号", Toast.LENGTH_SHORT).show();
			return isOk;
		}
		return isOk;
	}

	private void saveCarkey() {
		if (!"".equals(carKey.id) ) {
			carKey.carnum = carnum;
			carKey.driver = driver;
			carKey.start_time = time;
			
			mPointDBDao.updateCarKey(carKey);
		} else {
			carKey.carnum = carnum;
			carKey.driver = driver;
			carKey.start_time = time;
			
			mPointDBDao.insertCarKey(carKey);
		}
	}

	private void sendCarkeyData() {
		try {
			new SendCarkeyTask().execute("");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public class SendCarkeyTask extends AsyncTask<String, Integer, Boolean> {
		
		@Override
		protected void onPreExecute() {
			handler.sendEmptyMessage(0);
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
//				Proto_TravelImfor proto_TravelImfor = Proto_TravelImfor.newBuilder()
//						.setCarnum(ByteString.copyFrom(plate, "GB2312"))
//						.setDriver(ByteString.copyFrom(driver, "GB2312"))
//						.setMarktime(ByteString.copyFrom(time, "GB2312"))
//						.setPlace(ByteString.copyFrom(destination, "GB2312"))
//						.setTime(ByteString.copyFrom(time, "GB2312"))
//						.setTraveltype(1)
//						.setWork(ByteString.copyFrom(task, "GB2312"))
//						.setIdealtime(ByteString.copyFrom(estimated_arrived_time, "GB2312"))
//						.setNumofPeople(Integer.valueOf(peopleNum))
//						.setLon(Double.valueOf(lon)).setLat(Double.valueOf(lat))
//						.setMemo(ByteString.copyFrom(remark, "GB2312")).build();
//				Proto_Head proto_Head = Proto_Head.newBuilder()
//						.setProtoMsgType(ProtoMsgType.ProtoMsgType_carKeyl)
//						.setCmdSize(proto_TravelImfor.toByteArray().length)
//						.addReceivers(ByteString.copyFrom(SysConfig.DSCLOUD, "GB2312"))
//						.setReceivers(0, ByteString.copyFrom(SysConfig.DSCLOUD, "GB2312"))
//						.setSender(ByteString.copyFrom(SysConfig.SC, "GB2312"))
//						.setMsgId(0).setPriority(1).setExpired(0).build();
//				
//				DataProcess.GetInstance().sendData(SocketUtils.writeBytes(proto_Head.toByteArray(),
//						proto_TravelImfor.toByteArray()));
				
				isUpdate = false;
				handler.sendEmptyMessageDelayed(2,	5000);
			} catch (Exception e) {
				e.printStackTrace();
				handler.sendEmptyMessage(2);
			}
			return null;
		}
		
	}
	
	/**
	 * 更新时间
	 */
	public void UpdateTimeDialog() {
		new AlertDialog.Builder(CarkeyGetActivity.this)
		.setTitle("提示")
		.setMessage("是否更新时间")
		.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				time = TimeUtil.getCurrentTimeInString();
				tv_getcarkey_time.setText(time);
			}
		}).setNegativeButton(getResources().getString(R.string.cancel), null).create().show();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case 1:
				String result = intent.getStringExtra("usercode");
				showMessage(result);
				break;
				
			default:
				break;
			}
		}
	}
	
}
