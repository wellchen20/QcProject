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

public class EndOffActivity extends BaseActivity implements OnClickListener {

	private LinearLayout ll_end_plate, ll_end_driver,
			ll_end_departure_place, ll_end_peoplenum;
	private TextView tv_end_plate, tv_end_driver, 
			tv_end_departure_time, tv_end_departure_place,
			tv_end_peoplenum;
	private Button btn_end_ok;
	private EditText ed_end_remark;

	private String peopleNum, remark, plate, driver, place = "营地", time, start_time, lat, lon, task;
	
	private final String[] departurePlaces = new String[]{"营地", "中营地"};
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
				if (!EndOffActivity.this.isFinishing() && !progressDialog.isShowing()) {
					progressDialog.show();
				}
				break;

			case 1:
				if (!EndOffActivity.this.isFinishing() && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				break;
			case 2:
				if (!isUpdate) {
					if (!EndOffActivity.this.isFinishing() && progressDialog.isShowing()) {
						progressDialog.dismiss();
					}
					showMessage("提交驻地出发信息失败");
					
					Intent intent = new Intent();
					setResult(RESULT_OK, intent);
					
					EndOffActivity.this.finish();
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
					if (!EndOffActivity.this.isFinishing() && progressDialog.isShowing()) {
						progressDialog.dismiss();
					}
					showMessage("提交驻地出发信息成功");
					
					Intent intent1 = new Intent();
					setResult(RESULT_OK, intent1);
					
					EndOffActivity.this.finish();
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
		setContentView(R.layout.activity_end_off);
		
		progressDialog = new ProgressDialog(EndOffActivity.this);
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
		ll_end_plate = (LinearLayout) findViewById(R.id.ll_end_plate);
		ll_end_plate.setOnClickListener(this);
		ll_end_driver = (LinearLayout) findViewById(R.id.ll_end_driver);
		ll_end_driver.setOnClickListener(this);
		ll_end_departure_place = (LinearLayout) findViewById(R.id.ll_end_departure_place);
		ll_end_departure_place.setOnClickListener(this);
		ll_end_peoplenum = (LinearLayout) findViewById(R.id.ll_end_peoplenum);
		ll_end_peoplenum.setOnClickListener(this);
		tv_end_plate = (TextView) findViewById(R.id.tv_end_plate);
		tv_end_driver = (TextView) findViewById(R.id.tv_end_driver);
		tv_end_departure_time = (TextView) findViewById(R.id.tv_end_departure_time);
		tv_end_departure_place = (TextView) findViewById(R.id.tv_end_departure_place);
		tv_end_peoplenum = (TextView) findViewById(R.id.tv_end_peoplenum);
		tv_end_peoplenum.setOnClickListener(this);
		btn_end_ok = (Button) findViewById(R.id.btn_end_ok);
		btn_end_ok.setOnClickListener(this);
		ed_end_remark = (EditText) findViewById(R.id.ed_end_remark); 
		findViewById(R.id.ll_title_back).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				EndOffActivity.this.finish();
			}
		});;
		((TextView) findViewById(R.id.tv_title_title)).setText(getResources().getString(R.string.end_off));
	}
	
	private void initDatas() {
		task = getIntent().getStringExtra("task");
		plate = getIntent().getStringExtra("plate");
		tv_end_plate.setText(plate);
		driver = getIntent().getStringExtra("driver");
		tv_end_driver.setText(driver);
		peopleNum = getIntent().getStringExtra("peopleNum");
		tv_end_peoplenum.setText(peopleNum);
		if (time != null && !"".equals(time)) {
			tv_end_departure_time.setText(time);
		} else {
			time = TimeUtil.getCurrentTimeInString();
			tv_end_departure_time.setText(time);
		}
		tv_end_departure_place.setText(place);
		remark = getIntent().getStringExtra("remark");
		if (remark != null && !"".equals(remark)) {
			ed_end_remark.setText(remark);
		}
		ed_end_remark.requestFocus();
		
		for (int i = 0; i < departurePlaces.length; i++) {
			mDeparturePlaces.add(departurePlaces[i]);
		}
		departureListAdapter = new DepartureListAdapter(EndOffActivity.this, mDeparturePlaces, mDeparture);
		start_time = getIntent().getStringExtra("start_time");
		carTrave = mPointDBDao.selectCarTraveByStarttime(start_time);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_end_plate:
			
			break;
			
		case R.id.ll_end_driver:
			
			break;
			
		case R.id.ll_end_departure_place:
			setdepartureDialog();
			break;
	
		case R.id.ll_end_peoplenum:
		case R.id.tv_end_peoplenum:
			setPeopleNumDialog();
			break;
	
		case R.id.btn_end_ok:
			handler.sendEmptyMessage(0);
			remark = ed_end_remark.getText().toString().trim();
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
		if (place != null && !"".equals(place)) {
			if (peopleNum != null && !"".equals(peopleNum)) {
				isOk = true;
			} else {
				Toast.makeText(EndOffActivity.this, "请输入人数", Toast.LENGTH_SHORT).show();
				return isOk;
			}
		} else {
			Toast.makeText(EndOffActivity.this, "请选择到达地点", Toast.LENGTH_SHORT).show();
			return isOk;
		}
		return isOk;
	}
	
	private void saveTrave() {
		carTrave.end_place = place;
		carTrave.end_time = time;
		carTrave.end_lat = lat;
		carTrave.end_lon = lon;
		carTrave.end_remark = remark;
		carTrave.end_peoplenum = peopleNum;
		
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
						.setTraveltype(4)
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
	 * 设置出发地点
	 */
	private AlertDialog departureDialog;
	public void setdepartureDialog() {
		View view = LayoutInflater.from(EndOffActivity.this).inflate(R.layout.dialog_set_place, null);
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
				tv_end_departure_place.setText(place);
			}
		});
		departureDialog = new AlertDialog.Builder(EndOffActivity.this).setView(view)
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
			departureDialog.dismiss();
			departureSelected = position;
			place = mDeparturePlaces.get(position);
			tv_end_departure_place.setText(place);
		}
	};
	
	/**
	 * 设置人数
	 */
	public void setPeopleNumDialog() {
		View view = LayoutInflater.from(EndOffActivity.this).inflate(R.layout.dialog_set_peoplenum, null);
		final EditText ed_peoplenum = (EditText) view.findViewById(R.id.ed_peoplenum);
		ed_peoplenum.setText(peopleNum);
		new AlertDialog.Builder(EndOffActivity.this).setView(view)
		.setTitle("人数设置")
		.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				String people = ed_peoplenum.getText().toString().trim();
				
				if (people == null || "".equals(people)) {
					Toast.makeText(EndOffActivity.this, "请输入人数", Toast.LENGTH_SHORT).show();
				} else {
					peopleNum = people;
					hideSoftInput(ed_peoplenum);
					tv_end_peoplenum.setText(peopleNum);
				}
			}
		}).setNegativeButton(getResources().getString(R.string.cancel), null).create().show();
	}
}
