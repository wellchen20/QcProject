package com.mtkj.cnpc.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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

public class ArrivedAtActivity extends BaseActivity implements OnClickListener {

	private LinearLayout ll_arrived_plate, ll_arrived_driver, ll_arrived_departure_time,
			ll_arrived_departure_place, ll_arrived_peoplenum;
	private TextView tv_arrived_plate, tv_arrived_driver,
			tv_arrived_departure_time,
			tv_arrived_departure_place, tv_arrived_peoplenum;
	private Button btn_arrived_ok;
	private EditText ed_arrived_remark;
	
	private final String[] departurePlaces = new String[]{"工地", "中营地"};
	private List<String> mDeparturePlaces = new ArrayList<String>();
	private DepartureListAdapter departureListAdapter;
	private int departureSelected = -1;
	
	private CarTrave carTrave;
	private String peopleNum, remark, plate, driver, time, place, start_time, lat, lon, task;
	private ProgressDialog progressDialog;
	private boolean isUpdate = false;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if (!ArrivedAtActivity.this.isFinishing() && !progressDialog.isShowing()) {
					progressDialog.show();
				}
				break;

			case 1:
				if (!ArrivedAtActivity.this.isFinishing() && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				break;
				
			case 2:
				if (!isUpdate) {
					if (!ArrivedAtActivity.this.isFinishing() && progressDialog.isShowing()) {
						progressDialog.dismiss();
					}
					showMessage("提交驻地出发信息失败");
					
					Intent intent = new Intent();
					setResult(RESULT_OK, intent);
					
					ArrivedAtActivity.this.finish();
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
					if (!ArrivedAtActivity.this.isFinishing() && progressDialog.isShowing()) {
						progressDialog.dismiss();
					}
					showMessage("提交驻地出发信息成功");
					
					Intent intent1 = new Intent();
					setResult(RESULT_OK, intent1);
					
					ArrivedAtActivity.this.finish();
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
		setContentView(R.layout.activity_arrived_at);
		
		initPb();
		
		initViews();
		initDatas();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		DataProcess.GetInstance().setMsgHandler(handler);
	}
	
	private void initPb() {
		progressDialog = new ProgressDialog(ArrivedAtActivity.this);
		progressDialog.setMessage("正在提交...");
		progressDialog.setCancelable(false);
	}

	private void initViews() {
		ll_arrived_plate = (LinearLayout) findViewById(R.id.ll_arrived_plate);
		ll_arrived_plate.setOnClickListener(this);
		ll_arrived_driver = (LinearLayout) findViewById(R.id.ll_arrived_driver);
		ll_arrived_driver.setOnClickListener(this);
		ll_arrived_departure_place = (LinearLayout) findViewById(R.id.ll_arrived_departure_place);
		ll_arrived_departure_place.setOnClickListener(this);
		ll_arrived_departure_time = (LinearLayout) findViewById(R.id.ll_arrived_departure_time);
		ll_arrived_departure_time.setOnClickListener(this);
		ll_arrived_peoplenum = (LinearLayout) findViewById(R.id.ll_arrived_peoplenum);
		ll_arrived_peoplenum.setOnClickListener(this);
		tv_arrived_plate = (TextView) findViewById(R.id.tv_arrived_plate);
		tv_arrived_driver = (TextView) findViewById(R.id.tv_arrived_driver);
		tv_arrived_departure_time = (TextView) findViewById(R.id.tv_arrived_departure_time);
		tv_arrived_departure_place = (TextView) findViewById(R.id.tv_arrived_departure_place);
		tv_arrived_peoplenum = (TextView) findViewById(R.id.tv_arrived_peoplenum);
		tv_arrived_peoplenum.setOnClickListener(this);
		btn_arrived_ok = (Button) findViewById(R.id.btn_arrived_ok);
		btn_arrived_ok.setOnClickListener(this);
		ed_arrived_remark = (EditText) findViewById(R.id.ed_arrived_remark);
		findViewById(R.id.ll_title_back).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ArrivedAtActivity.this.finish();
			}
		});;
		((TextView) findViewById(R.id.tv_title_title)).setText(getResources().getString(R.string.arrived_at));
	}
	
	private void initDatas() {
		task = getIntent().getStringExtra("task");
		plate = getIntent().getStringExtra("plate");
		tv_arrived_plate.setText(plate);
		driver = getIntent().getStringExtra("driver");
		tv_arrived_driver.setText(driver);
		peopleNum = getIntent().getStringExtra("peopleNum");
		tv_arrived_peoplenum.setText(peopleNum);
		place = getIntent().getStringExtra("place");
		tv_arrived_departure_place.setText(place);
		time = getIntent().getStringExtra("time");
		if (time != null && !"".equals(time)) {
			tv_arrived_departure_time.setText(time);
		} else {
			time = TimeUtil.getCurrentTimeInString();
			tv_arrived_departure_time.setText(time);
		}
		remark = getIntent().getStringExtra("remark");
		if (remark != null && !"".equals(remark)) {
			ed_arrived_remark.setText(remark);
		}
		start_time = getIntent().getStringExtra("start_time");
		carTrave = mPointDBDao.selectCarTraveByStarttime(start_time);
		
		for (int i = 0; i < departurePlaces.length; i++) {
			mDeparturePlaces.add(departurePlaces[i]);
		}
		departureListAdapter = new DepartureListAdapter(ArrivedAtActivity.this, mDeparturePlaces, mDeparture);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_arrived_plate:

			break;

		case R.id.ll_arrived_driver:

			break;

		case R.id.ll_arrived_departure_time:
//			UpdateTimeDialog();
			break;
			
		case R.id.ll_arrived_departure_place:
			setdepartureDialog();
			break;

		case R.id.ll_arrived_peoplenum:
		case R.id.tv_arrived_peoplenum:
			setPeopleNumDialog();
			break;

		case R.id.btn_arrived_ok:
			handler.sendEmptyMessage(0);
			remark = ed_arrived_remark.getText().toString().trim();
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
			if (!"".equals(peopleNum)) {
				isOk = true;
			} else {
				Toast.makeText(ArrivedAtActivity.this, "请输入人数", Toast.LENGTH_SHORT).show();
				return isOk;
			}
		} else {
			Toast.makeText(ArrivedAtActivity.this, "请选择到达地点", Toast.LENGTH_SHORT).show();
			return isOk;
		}
		return isOk;
	}

	private void saveTrave() {
		carTrave.arrived_time = time;
		carTrave.arrived_peoplenum = peopleNum;
		carTrave.arrived_place = place;
		carTrave.arrived_lat = lat;
		carTrave.arrived_lon = lon;
		carTrave.arrived_remark = remark;
		
		mPointDBDao.updateCarTrave(carTrave);
	}

	private void sendTraveData() {
		try {
			new SendTraveTask().execute("");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public class SendTraveTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			handler.sendEmptyMessage(0);
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
						.setTraveltype(2)
						.setWork(ByteString.copyFrom(task, "GB2312"))
						.setIdealtime(ByteString.copyFrom("", "GB2312"))
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
		new AlertDialog.Builder(ArrivedAtActivity.this)
		.setTitle("提示")
		.setMessage("是否更新时间")
		.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				time = TimeUtil.getCurrentTimeInString();
				tv_arrived_departure_time.setText(time);
			}
		}).setNegativeButton(getResources().getString(R.string.cancel), null).create().show();
	}
	
	/**
	 * 设置人数
	 */
	public void setPeopleNumDialog() {
		View view = LayoutInflater.from(ArrivedAtActivity.this).inflate(R.layout.dialog_set_peoplenum, null);
		final EditText ed_peoplenum = (EditText) view.findViewById(R.id.ed_peoplenum);
		ed_peoplenum.setText(peopleNum);
		new AlertDialog.Builder(ArrivedAtActivity.this).setView(view)
		.setTitle("人数设置")
		.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				String people = ed_peoplenum.getText().toString().trim();
				
				if (people == null || "".equals(people)) {
					Toast.makeText(ArrivedAtActivity.this, "请输入人数", Toast.LENGTH_SHORT).show();
				} else {
					peopleNum = people;
					hideSoftInput(ed_peoplenum);
					tv_arrived_peoplenum.setText(peopleNum);
				}
			}
		}).setNegativeButton(getResources().getString(R.string.cancel), null).create().show();
	}
	
	/**
	 * 设置地点
	 */
	private AlertDialog departureDialog;
	public void setdepartureDialog() {
		View view = LayoutInflater.from(ArrivedAtActivity.this).inflate(R.layout.dialog_set_place, null);
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
				tv_arrived_departure_place.setText(place);
			}
		});
		departureDialog = new AlertDialog.Builder(ArrivedAtActivity.this).setView(view)
				.setTitle("到达地点")
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
				tv_arrived_departure_place.setText(place);
			} else {
				departureSelected = -1;
				place = "";
				departureListAdapter.setSelectedIndex(departureSelected);
				departureListAdapter.notifyDataSetChanged();
			}
		}
	};
}
