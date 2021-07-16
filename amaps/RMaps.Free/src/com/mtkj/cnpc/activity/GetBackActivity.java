package com.mtkj.cnpc.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.protobuf.ByteString;
import com.mtkj.cnpc.activity.adapter.DepartureListAdapter;
import com.mtkj.cnpc.activity.adapter.DepartureListAdapter.IDeparture;
import com.mtkj.cnpc.protocol.bean.CarTrave;
import com.mtkj.cnpc.protocol.constants.DSSProtoDataConstants.ProtoMsgType;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_Head;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_TravelImfor;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_TravelImforResponce;
import com.mtkj.cnpc.protocol.socket.DataProcess;
import com.mtkj.cnpc.protocol.utils.SocketUtils;
import com.mtkj.cnpc.view.swipemenulistview.SwipeMenuListView;
import com.mtkj.cnpc.R;
import com.robert.maps.applib.utils.TimeUtil;

public class GetBackActivity extends BaseActivity implements OnClickListener {

	private LinearLayout ll_back_plate, ll_back_driver, ll_back_departure_time,
			ll_back_departure_place, ll_back_peoplenum, ll_estimated_return_time;
	private TextView tv_back_plate, tv_back_driver,
			tv_back_departure_time, tv_back_departure_place,
			tv_back_peoplenum, tv_estimated_return_time;
	private Button btn_back_ok;
	private EditText ed_back_remark;

	private String plate, driver, time, place, peopleNum, remark, start_time, lat, lon, estimated_return_time, task;
	private final String[] departurePlaces = new String[]{"工地", "中营地"};
	private List<String> mDeparturePlaces = new ArrayList<String>();
	private DepartureListAdapter departureListAdapter;
	private int departureSelected = -1;
	
	private CarTrave carTrave;
	private ProgressDialog progressDialog;
	private boolean isUpdate = false;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if (!GetBackActivity.this.isFinishing() && !progressDialog.isShowing()) {
					progressDialog.show();
				}
				break;

			case 1:
				if (!GetBackActivity.this.isFinishing() && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				break;
			case 2:
				if (!isUpdate) {
					if (!GetBackActivity.this.isFinishing() && progressDialog.isShowing()) {
						progressDialog.dismiss();
					}
					showMessage("提交驻地出发信息失败");
					
					Intent intent = new Intent();
					setResult(RESULT_OK, intent);
					
					GetBackActivity.this.finish();
				}
				break;
				
			case DataProcess.MSG.TRAVE:
				Proto_TravelImforResponce traveImfor  = (Proto_TravelImforResponce) msg.obj;
				if (traveImfor != null) {
					try {
						String starttime = new String(traveImfor.getMarktime().toByteArray(), "GB2312");
						CarTrave trave = mPointDBDao.selectCarTraveByStarttime(starttime);
						switch (traveImfor.getTraveltype()) {
						case 1:
							trave.start_isUpload = "1";
							break;

						case 2:
							trave.arrived_isUpload = "1";
							break;
							
						case 3:
							trave.back_isUpload = "1";
							break;
							
						case 4:
							trave.end_isUpload = "1";
							break;
						}
						mPointDBDao.updateCarTrave(trave);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					isUpdate = true;
					if (!GetBackActivity.this.isFinishing() && progressDialog.isShowing()) {
						progressDialog.dismiss();
					}
					showMessage("提交驻地出发信息成功");
					
					Intent intent1 = new Intent();
					setResult(RESULT_OK, intent1);
					
					GetBackActivity.this.finish();
				}
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
		setContentView(R.layout.activity_get_back);
		
		progressDialog = new ProgressDialog(GetBackActivity.this);
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
		ll_back_plate = (LinearLayout) findViewById(R.id.ll_back_plate);
		ll_back_plate.setOnClickListener(this);
		ll_back_driver = (LinearLayout) findViewById(R.id.ll_back_driver);
		ll_back_driver.setOnClickListener(this);
		ll_back_departure_place = (LinearLayout) findViewById(R.id.ll_back_departure_place);
		ll_back_departure_place.setOnClickListener(this);
		ll_back_departure_time = (LinearLayout) findViewById(R.id.ll_back_departure_time);
		ll_back_departure_time.setOnClickListener(this);
		ll_back_peoplenum = (LinearLayout) findViewById(R.id.ll_back_peoplenum);
		ll_back_peoplenum.setOnClickListener(this);
		tv_back_plate = (TextView) findViewById(R.id.tv_back_plate);
		tv_back_driver = (TextView) findViewById(R.id.tv_back_driver);
		tv_back_departure_time = (TextView) findViewById(R.id.tv_back_departure_time);
		tv_back_departure_place = (TextView) findViewById(R.id.tv_back_departure_place);
		tv_back_peoplenum = (TextView) findViewById(R.id.tv_back_peoplenum);
		ll_estimated_return_time = (LinearLayout) findViewById(R.id.ll_estimated_return_time);
		tv_estimated_return_time = (TextView) findViewById(R.id.tv_estimated_return_time);
		ll_estimated_return_time.setOnClickListener(this);
		btn_back_ok = (Button) findViewById(R.id.btn_back_ok);
		btn_back_ok.setOnClickListener(this);
		ed_back_remark = (EditText) findViewById(R.id.ed_back_remark);
		findViewById(R.id.ll_title_back).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				GetBackActivity.this.finish();
			}
		});;
		((TextView) findViewById(R.id.tv_title_title)).setText(getResources().getString(R.string.get_back));
	}
	
	private void initDatas() {
		task = getIntent().getStringExtra("task");
		plate = getIntent().getStringExtra("plate");
		tv_back_plate.setText(plate);
		driver = getIntent().getStringExtra("driver");
		tv_back_driver.setText(driver);
		place = getIntent().getStringExtra("place");
		if (place != null && !"".equals(place)) {
			tv_back_departure_place.setText(place);
		} else {
			place = "工地";
			tv_back_departure_place.setText(place);
		}
		peopleNum = getIntent().getStringExtra("peopleNum");
		if (peopleNum != null && !"".equals(peopleNum)) {
			tv_back_peoplenum.setText(peopleNum);
		}
		time = getIntent().getStringExtra("time");
		if (time != null && !"".equals(time)) {
			tv_back_departure_time.setText(time);
		} else {
			time = TimeUtil.getCurrentTimeInString();
			tv_back_departure_time.setText(time);
		}
		estimated_return_time = getIntent().getStringExtra("estimated_return_time");
		if (estimated_return_time != null && !"".equals(estimated_return_time)) {
			tv_estimated_return_time.setText(estimated_return_time);
		}
		remark = getIntent().getStringExtra("remark");
		if (remark != null && !"".equals(remark)) {
			ed_back_remark.setText(remark);
		}
		ed_back_remark.requestFocus();
		
		for (int i = 0; i < departurePlaces.length; i++) {
			mDeparturePlaces.add(departurePlaces[i]);
		}
		departureListAdapter = new DepartureListAdapter(GetBackActivity.this, mDeparturePlaces, mDeparture);
		start_time = getIntent().getStringExtra("start_time");
		carTrave = mPointDBDao.selectCarTraveByStarttime(start_time);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_back_plate:
			
			break;
			
		case R.id.ll_back_driver:
			
			break;
			
		case R.id.ll_back_departure_time:
//			UpdateTimeDialog();
			break;
			
		case R.id.ll_back_departure_place:
			setdepartureDialog();
			break;
	
		case R.id.ll_back_peoplenum:
			setPeopleNumDialog();
			break;
	
		case R.id.ll_estimated_return_time:
			setEstimatedarrivedtime_1();
			break;
			
		case R.id.btn_back_ok:
			handler.sendEmptyMessage(0);
			remark = ed_back_remark.getText().toString().trim();
			if (WorkTypeChoose.unRectifyLocation != null) {
				lat = String.valueOf(WorkTypeChoose.unRectifyLocation.getLatitude());
				lon = String.valueOf(WorkTypeChoose.unRectifyLocation.getLongitude());
			} else {
				lat = "-1";
				lon = "-1";
			}
			
			if (!sureOk()) {
				handler.sendEmptyMessage(1);
				return;
			}
			saveTrave();
			sendTraveData();
//			sendTraveDataOther();
			break;
		}
	}

	private boolean sureOk() {
		boolean isOk = false;
		if (!"".equals(place)) {
			if (!"".equals(estimated_return_time)) {
				if (!"".equals(peopleNum)) {
					isOk = true;
				} else{
					Toast.makeText(GetBackActivity.this, "请输入人数", Toast.LENGTH_SHORT).show();
					return isOk;
				}
			} else {
				Toast.makeText(GetBackActivity.this, "请输入预计返回时间", Toast.LENGTH_SHORT).show();
				return isOk;
			}
		} else{
			Toast.makeText(GetBackActivity.this, "请选择出发地点", Toast.LENGTH_SHORT).show();
			return isOk;
		}
		return isOk;
	}
	
	private void saveTrave() {
		carTrave.back_place = place;
		carTrave.back_peoplenum = peopleNum;
		carTrave.end_peoplenum = peopleNum;
		carTrave.back_time = time;
		carTrave.back_lat = lat;
		carTrave.back_lon = lon;
		carTrave.back_remark = remark;
		carTrave.estimated_return_time = estimated_return_time;
		
		mPointDBDao.updateCarTrave(carTrave);
	}

	private void sendTraveData() {
		try {
			new SendTraveTask().execute("");
			
			Thread.sleep(1 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public class SendTraveTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			handler.sendEmptyMessage(0);
			super.onPreExecute();
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			try {
				Proto_TravelImfor proto_TravelImfor = Proto_TravelImfor.newBuilder()
						.setCarnum(ByteString.copyFrom(plate, "GB2312"))
						.setDriver(ByteString.copyFrom(driver, "GB2312"))
						.setMarktime(ByteString.copyFrom(start_time, "GB2312"))
						.setPlace(ByteString.copyFrom(place, "GB2312"))
						.setTime(ByteString.copyFrom(time, "GB2312"))
						.setTraveltype(3)
						.setWork(ByteString.copyFrom(task, "GB2312"))
						.setIdealtime(ByteString.copyFrom(estimated_return_time, "GB2312"))
						.setNumofPeople(Integer.valueOf(peopleNum))
						.setLon(Double.valueOf(lon)).setLat(Double.valueOf(lat))
						.setMemo(ByteString.copyFrom(remark, "GB2312")).build();
				Proto_Head proto_Head = Proto_Head.newBuilder()
						.setProtoMsgType(ProtoMsgType.ProtoMsgType_CarTravel)
						.setCmdSize(proto_TravelImfor.toByteArray().length)
						.addReceivers(ByteString.copyFrom(SysConfig.DSCLOUD, "GB2312"))
						.setReceivers(0, ByteString.copyFrom(SysConfig.DSCLOUD, "GB2312"))
						.setSender(ByteString.copyFrom(SysConfig.SC, "GB2312"))
						.setMsgId(0).setPriority(1).setExpired(0).build();
				
				DataProcess.GetInstance().sendData(SocketUtils.writeBytes(proto_Head.toByteArray(),
						proto_TravelImfor.toByteArray()));
				
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
		new AlertDialog.Builder(GetBackActivity.this)
		.setTitle("提示")
		.setMessage("是否更新时间")
		.setPositiveButton(getResources().getString(R.string.cancel), null).setNegativeButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				time = TimeUtil.getCurrentTimeInString();
				tv_back_departure_time.setText(time);
			}
		}).create().show();
	}
	
	/**
	 * 设置人数
	 */
	public void setPeopleNumDialog() {
		View view = LayoutInflater.from(GetBackActivity.this).inflate(R.layout.dialog_set_peoplenum, null);
		final EditText ed_peoplenum = (EditText) view.findViewById(R.id.ed_peoplenum);
		ed_peoplenum.setText(peopleNum);
		new AlertDialog.Builder(GetBackActivity.this).setView(view)
		.setTitle("人数设置")
		.setPositiveButton(getResources().getString(R.string.cancel), null).setNegativeButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				String people = ed_peoplenum.getText().toString().trim();
				
				if (people == null || "".equals(people)) {
					Toast.makeText(GetBackActivity.this, "请输入人数", Toast.LENGTH_SHORT).show();
				} else {
					peopleNum = people;
					hideSoftInput(ed_peoplenum);
					tv_back_peoplenum.setText(peopleNum);
				}
			}
		}).create().show();
	}
	
	/**
	 * 设置出发地点
	 */
	private AlertDialog departureDialog;
	public void setdepartureDialog() {
		View view = LayoutInflater.from(GetBackActivity.this).inflate(R.layout.dialog_set_place, null);
		final SwipeMenuListView listView = (SwipeMenuListView) view.findViewById(R.id.lv_destination);
		listView.setAdapter(departureListAdapter);
		departureListAdapter.setSelectedIndex(departureSelected);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				departureDialog.dismiss();
				departureSelected = position;
				place = mDeparturePlaces.get(position);
				tv_back_departure_place.setText(place);
			}
		});
		departureDialog = new AlertDialog.Builder(GetBackActivity.this).setView(view)
				.setTitle("出发地点")
				.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						departureDialog.dismiss();
					}
				}).create();
		departureDialog.show();
	}
	private IDeparture mDeparture = new IDeparture() {
		
		@Override
		public void onDeparture(int position, boolean isChecked) {
			if (isChecked) {
				departureDialog.dismiss();
				departureSelected = position;
				place = mDeparturePlaces.get(position);
				tv_back_departure_place.setText(place);
			} else {
				departureSelected = -1;
				place = "";
				departureListAdapter.setSelectedIndex(departureSelected);
				departureListAdapter.notifyDataSetChanged();
			}
		}
	};
	
	/**
	 * 选择预计花费时间
	 * 
	 */
	private void setEstimatedarrivedtime_1 () {
		final String[] arrBZJ = getResources().getStringArray(R.array.user_time);
		final long currTime = System.currentTimeMillis();
		new AlertDialog.Builder(GetBackActivity.this)
				.setTitle("请选择预计花费时间")
				.setSingleChoiceItems(arrBZJ, 0,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								estimated_return_time =  TimeUtil.getTime(currTime + (which + 1) * 60 * 60 * 1000);
								tv_estimated_return_time.setText((which + 1) + "小时");
							}
						}).create().show();
	}
	
	/**
	 * 设置预计到达时间	 */
	private TimePickerDialog timePickerDialog;
	private int hourOfDay = 0;
	private int minute = 0;
	public void setEstimatedarrivedtime() {
		Calendar calendar = Calendar.getInstance();
		if (hourOfDay == 0 && minute == 0) {
			hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
	        minute = calendar.get(Calendar.MINUTE);
		}
		timePickerDialog = new TimePickerDialog(GetBackActivity.this, new OnTimeSetListener() {
			
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				if (hourOfDay > 9) {
					if (minute > 9) {
						estimated_return_time = TimeUtil.getCurrentTimeInString().substring(0, 10) + " "  + hourOfDay + ":" + minute + ":00";
					} else {
						estimated_return_time = TimeUtil.getCurrentTimeInString().substring(0, 10) + " "  + hourOfDay + ":0" + minute + ":00";
					}
				} else {
					if (minute > 9) {
						estimated_return_time = TimeUtil.getCurrentTimeInString().substring(0, 10) + " 0" + hourOfDay + ":" + minute + ":00";
					} else {
						estimated_return_time = TimeUtil.getCurrentTimeInString().substring(0, 10) + " 0" + hourOfDay + ":0" + minute + ":00";
					}
				}
				
				if (estimated_return_time.compareTo(time) > 0) {
					tv_estimated_return_time.setText(estimated_return_time);
				} else {
					Toast.makeText(GetBackActivity.this, "预计返回时间不能小于当前时间，请重新设置", Toast.LENGTH_SHORT).show();
					estimated_return_time = "";
				}
			}
		}, hourOfDay, minute, true);
		timePickerDialog.setTitle("预计返回时间");
		timePickerDialog.show();
	}
	
}
