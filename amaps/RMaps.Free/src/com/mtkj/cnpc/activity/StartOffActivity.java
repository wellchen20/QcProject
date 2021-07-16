package com.mtkj.cnpc.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
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
import com.mtkj.cnpc.activity.adapter.CarNumListAdapter;
import com.mtkj.cnpc.activity.adapter.CarNumListAdapter.ICarNum;
import com.mtkj.cnpc.activity.adapter.DepartureListAdapter;
import com.mtkj.cnpc.activity.adapter.DepartureListAdapter.IDeparture;
import com.mtkj.cnpc.activity.adapter.DestinationListAdapter;
import com.mtkj.cnpc.activity.adapter.DestinationListAdapter.IDestination;
import com.mtkj.cnpc.protocol.bean.CarTrave;
import com.mtkj.cnpc.protocol.constants.DSSProtoDataConstants.ProtoMsgType;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.constants.SysContants;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_Head;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_TravelImfor;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_TravelImforResponce;
import com.mtkj.cnpc.protocol.socket.DataProcess;
import com.mtkj.cnpc.protocol.utils.SocketUtils;
import com.mtkj.cnpc.view.swipemenulistview.SwipeMenuListView;
import com.mtkj.cnpc.R;
import com.robert.maps.applib.utils.ListViewUtils;
import com.robert.maps.applib.utils.TimeUtil;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class StartOffActivity extends BaseActivity implements OnClickListener {

	private LinearLayout ll_start_plate, ll_start_driver,
			ll_start_destination, ll_start_departure_time,
			ll_start_departure_place, ll_start_peoplenum, ll_estimated_arrived_time;
	private TextView tv_start_plate, tv_start_driver, tv_start_destination,
			tv_start_departure_time, tv_start_departure_place,
			tv_start_peoplenum, tv_estimated_arrived_time;
	private Button btn_start_ok;
	private EditText ed_start_remark;
	
	private String plate, driver, task, destination, place, time, peopleNum, remark, lat, lon, estimated_arrived_time;
	
	private List<String> mCarNums = new ArrayList<String>();
	private CarNumListAdapter carNumListAdapter;
	private int carnumSelected = -1;
	
	private final String[] destinations = new String[]{"营地", "中营地", "工地", "其他"};
	private List<String> mDestinations = new ArrayList<String>();
	private final String[] tasks = new String[]{"测量", "推土", "查线", "放线", "震源", "前炮", "后炮", "工农", "其他"};
	private List<String> mTasks = new ArrayList<String>();
	private DestinationListAdapter destinationAdapter;
	
	private final String[] departurePlaces = new String[]{"营地", "中营地"};
	private List<String> mDeparturePlaces = new ArrayList<String>();
	private DepartureListAdapter departureListAdapter;
	
	private CarTrave carTrave;
	private ProgressDialog progressDialog;
	private boolean isUpdate = false;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if (!StartOffActivity.this.isFinishing() && !progressDialog.isShowing()) {
					progressDialog.show();
				}
				break;

			case 1:
				if (!StartOffActivity.this.isFinishing() && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				break;
				
			case 2:
				if (!isUpdate) {
					if (!StartOffActivity.this.isFinishing() && progressDialog.isShowing()) {
						progressDialog.dismiss();
					}
					
					showMessage("提交驻地出发信息失败");
					
					Intent intent = new Intent();
					setResult(RESULT_OK, intent);
					
					StartOffActivity.this.finish();
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
					if (!StartOffActivity.this.isFinishing() && progressDialog.isShowing()) {
						progressDialog.dismiss();
					}
					showMessage("提交驻地出发信息成功");
					
					Intent intent1 = new Intent();
					setResult(RESULT_OK, intent1);
					
					StartOffActivity.this.finish();
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
		setContentView(R.layout.activity_start_off);
		
		carTrave = new CarTrave();
		
		progressDialog = new ProgressDialog(StartOffActivity.this);
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
		ll_start_plate = (LinearLayout) findViewById(R.id.ll_start_plate);
		ll_start_plate.setOnClickListener(this);
		ll_start_driver = (LinearLayout) findViewById(R.id.ll_start_driver);
		ll_start_driver.setOnClickListener(this);
		ll_start_destination = (LinearLayout) findViewById(R.id.ll_start_destination);
		ll_start_destination.setOnClickListener(this);
		ll_start_departure_time = (LinearLayout) findViewById(R.id.ll_start_departure_time);
		ll_start_departure_time.setOnClickListener(this);
		ll_start_departure_place = (LinearLayout) findViewById(R.id.ll_start_departure_place);
		ll_start_departure_place.setOnClickListener(this);
		ll_start_peoplenum = (LinearLayout) findViewById(R.id.ll_start_peoplenum);
		ll_start_peoplenum.setOnClickListener(this);
		tv_start_plate = (TextView) findViewById(R.id.tv_start_plate);
		tv_start_driver = (TextView) findViewById(R.id.tv_start_driver);
		tv_start_destination = (TextView) findViewById(R.id.tv_start_destination);
		tv_start_departure_time = (TextView) findViewById(R.id.tv_start_departure_time);
		tv_start_departure_place = (TextView) findViewById(R.id.tv_start_departure_place);
		tv_start_peoplenum = (TextView) findViewById(R.id.tv_start_peoplenum);
		btn_start_ok = (Button) findViewById(R.id.btn_start_ok);
		btn_start_ok.setOnClickListener(this);
		ed_start_remark = (EditText) findViewById(R.id.ed_start_remark);
		ll_estimated_arrived_time = (LinearLayout) findViewById(R.id.ll_estimated_arrived_time);
		tv_estimated_arrived_time = (TextView) findViewById(R.id.tv_estimated_arrived_time);
		ll_estimated_arrived_time.setOnClickListener(this);
		findViewById(R.id.ll_title_back).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				StartOffActivity.this.finish();
			}
		});;
		((TextView) findViewById(R.id.tv_title_title)).setText(getResources().getString(R.string.start_off));
	}
	
	private void initDatas() {
		carTrave.id = getIntent().getStringExtra("id");
		if (!"".equals(carTrave.id)) {
			carTrave = mPointDBDao.selectCarTraveByID(carTrave.id);
		} else {
			carTrave = new CarTrave();
		}
		plate = getIntent().getStringExtra("plate");
		if (plate != null && !"".equals(plate)) {
			tv_start_plate.setText(plate);
		} else {
			plate = getData(SysContants.CARNUM, "");
			tv_start_plate.setText(plate);
		}
		driver = getIntent().getStringExtra("driver");
		if (driver != null && !"".equals(driver)) {
			tv_start_driver.setText(driver);
		} else {
			driver = getData(SysContants.USERNAME, "");
			tv_start_driver.setText(driver);
		}
		task = getIntent().getStringExtra("task");
		destination = getIntent().getStringExtra("destination");
		if (destination != null && !"".equals(destination)) {
			tv_start_destination.setText(task + "/" + destination);
		}
		place = getIntent().getStringExtra("place");
		if (place != null && !"".equals(place)) {
			tv_start_departure_place.setText(place);
		} else {
			place = "营地";
			tv_start_departure_place.setText(place);
		}
		peopleNum = getIntent().getStringExtra("peopleNum");
		if (peopleNum != null && !"".equals(peopleNum)) {
			tv_start_peoplenum.setText(peopleNum);
		}
		time = getIntent().getStringExtra("time");
		if (time != null && !"".equals(time)) {
			tv_start_departure_time.setText(time);
		} else {
			time = TimeUtil.getCurrentTimeInString();
			tv_start_departure_time.setText(time);
		}
		estimated_arrived_time = getIntent().getStringExtra("estimated_arrived_time");
		if (estimated_arrived_time != null && !"".equals(estimated_arrived_time)) {
			tv_estimated_arrived_time.setText(estimated_arrived_time);
		}
		remark = getIntent().getStringExtra("remark");
		if (remark != null && !"".equals(remark)) {
			ed_start_remark.setText(remark);
		}
		ed_start_remark.requestFocus();
		
		mCarNums = mPointDBDao.selectAddCarNum();
		carNumListAdapter = new CarNumListAdapter(StartOffActivity.this, mCarNums, mCarNum);
		for (int i = 0; i < tasks.length; i++) {
			mTasks.add(tasks[i]);
		}
		for (int i = 0; i < destinations.length; i++) {
			mDestinations.add(destinations[i]);
		}
		destinationAdapter = new DestinationListAdapter(StartOffActivity.this, mTasks, mDestination);
		for (int i = 0; i < departurePlaces.length; i++) {
			mDeparturePlaces.add(departurePlaces[i]);
		}
		departureListAdapter = new DepartureListAdapter(StartOffActivity.this, mDeparturePlaces, mDeparture);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_start_plate:
			setCarnumeDialog();
			break;
			
		case R.id.ll_start_driver:
			
			break;
			
		case R.id.ll_start_destination:
			setDestinationDialog();
			break;
	
		case R.id.ll_start_departure_place:
			setdepartureDialog();
			break;
	
		case R.id.ll_start_departure_time:
//			UpdateTimeDialog();
			break;
			
		case R.id.ll_start_peoplenum:
			setPeopleNumDialog();
			break;
	
		case R.id.ll_estimated_arrived_time:
			setEstimatedarrivedtime_1();
			break;
			
		case R.id.btn_start_ok:
			handler.sendEmptyMessage(0);
			remark = ed_start_remark.getText().toString().trim();
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
		if (!"".equals(plate)) {
			if (!"".equals(task)) {
				if (!"".equals(destination)) {
					if (!"".equals(place)) {
						if (!"".equals(estimated_arrived_time)) {
							if (!"".equals(peopleNum)) {
								isOk = true;
							} else{
								Toast.makeText(StartOffActivity.this, "请输入人数", Toast.LENGTH_SHORT).show();
								return isOk;
							}
						} else {
							Toast.makeText(StartOffActivity.this, "请输入预计到达时间", Toast.LENGTH_SHORT).show();
							return isOk;
						}
					} else{
						Toast.makeText(StartOffActivity.this, "请选择出发地点", Toast.LENGTH_SHORT).show();
						return isOk;
					}
				} else {
					Toast.makeText(StartOffActivity.this, "请输选择目的地", Toast.LENGTH_SHORT).show();
					return isOk;
				}
			} else {
				Toast.makeText(StartOffActivity.this, "请选择任务", Toast.LENGTH_SHORT).show();
				return isOk;
			}
		} else {
			Toast.makeText(StartOffActivity.this, "请输入车牌号", Toast.LENGTH_SHORT).show();
			return isOk;
		}
		return isOk;
	}

	private void saveTrave() {
		if (!"".equals(carTrave.id) ) {
			carTrave.carnum = plate;
			carTrave.driver = driver;
			carTrave.destination = destination;
			carTrave.arrived_place = destination;
			carTrave.task = task;
			
			carTrave.start_place = place;
			carTrave.start_peoplenum = peopleNum;
			carTrave.arrived_peoplenum = peopleNum;
			carTrave.start_time = time;
			carTrave.start_lat = lat;
			carTrave.start_lon = lon;
			carTrave.start_remark = remark;
			carTrave.estimated_arrived_time = estimated_arrived_time;
			
			mPointDBDao.updateCarTrave(carTrave);
		} else {
			carTrave.carnum = plate;
			carTrave.driver = driver;
			carTrave.destination = destination;
			carTrave.arrived_place = destination;
			carTrave.task = task;
			
			carTrave.start_place = place;
			carTrave.start_peoplenum = peopleNum;
			carTrave.arrived_peoplenum = peopleNum;
			carTrave.start_time = time;
			carTrave.start_lat = lat;
			carTrave.start_lon = lon;
			carTrave.start_remark = remark;
			carTrave.estimated_arrived_time = estimated_arrived_time;
			
			mPointDBDao.insertCarTrave(carTrave);
		}
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
			handler.sendEmptyMessage(0);
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				Proto_TravelImfor proto_TravelImfor = Proto_TravelImfor.newBuilder()
						.setCarnum(ByteString.copyFrom(plate, "GB2312"))
						.setDriver(ByteString.copyFrom(driver, "GB2312"))
						.setMarktime(ByteString.copyFrom(time, "GB2312"))
						.setPlace(ByteString.copyFrom(destination, "GB2312"))
						.setTime(ByteString.copyFrom(time, "GB2312"))
						.setTraveltype(1)
						.setWork(ByteString.copyFrom(task, "GB2312"))
						.setIdealtime(ByteString.copyFrom(estimated_arrived_time, "GB2312"))
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
	 * 设置车牌号
	 */
	private AlertDialog carnumDialog;
	public void setCarnumeDialog() {
		View view = LayoutInflater.from(StartOffActivity.this).inflate(R.layout.dialog_set_place, null);
		final SwipeMenuListView listView = (SwipeMenuListView) view.findViewById(R.id.lv_destination);
		listView.setAdapter(carNumListAdapter);
		carNumListAdapter.setSelectedIndex(carnumSelected);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position < mCarNums.size()) {
					carnumDialog.dismiss();
					plate = mCarNums.get(position);
					carnumSelected = position;
					tv_start_plate.setText(plate);
				} else {
					carnumDialog.dismiss();
					addCarNumDialog();
				}
			}
		});
		carnumDialog = new AlertDialog.Builder(StartOffActivity.this).setView(view)
				.setTitle("车牌号")
				.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						carnumDialog.dismiss();
					}
				}).create();
		carnumDialog.show();
	}
	private ICarNum mCarNum = new ICarNum() {
		
		@Override
		public void onCarnum(int position, boolean isChecked) {
			if (isChecked) {
				carnumDialog.dismiss();
				plate = mCarNums.get(position);
				carnumSelected = position;
				tv_start_plate.setText(plate);
			} else {
				carnumSelected = -1;
				place = "";
				carNumListAdapter.setSelectedIndex(carnumSelected);
				carNumListAdapter.notifyDataSetChanged();
			}
		}
	};
	/**
	 * 添加车牌
	 */
	public void addCarNumDialog() {
		View view = LayoutInflater.from(StartOffActivity.this).inflate(R.layout.dialog_add_carnum, null);
		final EditText ed_padd_carnum = (EditText) view.findViewById(R.id.ed_add_carnum);
		new AlertDialog.Builder(StartOffActivity.this).setView(view)
		.setTitle("增加车牌")
		.setPositiveButton(getResources().getString(R.string.cancel), null).setNegativeButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				String carnum = ed_padd_carnum.getText().toString().trim();
				
				if (carnum == null || "".equals(carnum)) {
					Toast.makeText(StartOffActivity.this, "请输入车牌号", Toast.LENGTH_SHORT).show();
				} else {
					plate = carnum;
					hideSoftInput(ed_padd_carnum);
					tv_start_plate.setText(plate);
					
					mPointDBDao.insertCarNum(carnum);
					mCarNums = mPointDBDao.selectAddCarNum();
					for (int i = 0; i < mCarNums.size(); i++) {
						if (carnum.equals(mCarNums.get(i))) {
							carnumSelected = i;
						}
					}
					carNumListAdapter.setmStrings(mCarNums);
					carNumListAdapter.notifyDataSetChanged();
				}
			}
		}).create().show();
	}
	
	/**
	 * 更新时间
	 */
	public void UpdateTimeDialog() {
		new AlertDialog.Builder(StartOffActivity.this)
		.setTitle("提示")
		.setMessage("是否更新时间")
		.setPositiveButton(getResources().getString(R.string.cancel), null).setNegativeButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				time = TimeUtil.getCurrentTimeInString();
				tv_start_departure_time.setText(time);
			}
		}).create().show();
	}
	
	/**
	 * 设置人数
	 */
	public void setPeopleNumDialog() {
		View view = LayoutInflater.from(StartOffActivity.this).inflate(R.layout.dialog_set_peoplenum, null);
		final EditText ed_peoplenum = (EditText) view.findViewById(R.id.ed_peoplenum);
		ed_peoplenum.setText(peopleNum);
		new AlertDialog.Builder(StartOffActivity.this).setView(view)
		.setTitle("人数设置")
		.setPositiveButton(getResources().getString(R.string.cancel), null).setNegativeButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				String people = ed_peoplenum.getText().toString().trim();
				
				if (people == null || "".equals(people)) {
					Toast.makeText(StartOffActivity.this, "请输入人数", Toast.LENGTH_SHORT).show();
				} else {
					peopleNum = people;
					hideSoftInput(ed_peoplenum);
					tv_start_peoplenum.setText(peopleNum);
				}
			}
		}).create().show();
	}
	
	/**
	 * 设置目的地
	 */
	private AlertDialog destinationDialog;
	private boolean isArrived = false;
	public void setDestinationDialog() {
		View view = LayoutInflater.from(StartOffActivity.this).inflate(R.layout.dialog_set_place, null);
		final SwipeMenuListView listView = (SwipeMenuListView) view.findViewById(R.id.lv_destination);
		listView.setAdapter(destinationAdapter);
		destinationAdapter.notifyDataSetChanged();
		ListViewUtils.getListViewHeightBasedOnChildren(listView);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (isArrived) {
					destinationDialog.dismiss();
					destination = mDestinations.get(position);
					tv_start_destination.setText(task + "/" + destination);
					
					destinationAdapter.setmStrings(mTasks);
					destinationAdapter.notifyDataSetChanged();
					
					isArrived = false;
				} else {
					task = mTasks.get(position);
					destinationAdapter.setmStrings(mDestinations);
					destinationAdapter.notifyDataSetChanged();
					isArrived = true;
				}
				
			}
		});
		destinationDialog = new AlertDialog.Builder(StartOffActivity.this).setView(view)
				.setTitle("任务/目的地")
				.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						destinationDialog.dismiss();
					}
				}).create();
		destinationDialog.show();
		isArrived = false;
	}
	private IDestination mDestination = new IDestination() {

		@Override
		public void onDestination(int position, boolean isChecked) {
			if (isArrived) {
				destinationDialog.dismiss();
				destination = mDestinations.get(position);
				tv_start_destination.setText(task + "/" + destination);
				
				destinationAdapter.setmStrings(mTasks);
				destinationAdapter.notifyDataSetChanged();
				
				isArrived = false;
			} else {
				task = mTasks.get(position);
				destinationAdapter.setmStrings(mDestinations);
				destinationAdapter.notifyDataSetChanged();
				isArrived = true;
			}
		}
	};
	
	/**
	 * 设置出发地点
	 */
	private AlertDialog departureDialog;
	public void setdepartureDialog() {
		View view = LayoutInflater.from(StartOffActivity.this).inflate(R.layout.dialog_set_place, null);
		final SwipeMenuListView listView = (SwipeMenuListView) view.findViewById(R.id.lv_destination);
		listView.setAdapter(departureListAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				departureDialog.dismiss();
				place = mDeparturePlaces.get(position);
				tv_start_departure_place.setText(place);
			}
		});
		departureDialog = new AlertDialog.Builder(StartOffActivity.this).setView(view)
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
				place = mDeparturePlaces.get(position);
				tv_start_departure_place.setText(place);
			} else {
				place = "";
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
		new AlertDialog.Builder(StartOffActivity.this)
				.setTitle("请选择预计花费时间")
				.setSingleChoiceItems(arrBZJ, 0,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								estimated_arrived_time =  TimeUtil.getTime(currTime + (which + 1) * 60 * 60 * 1000);
								tv_estimated_arrived_time.setText((which + 1) + "小时");
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
		timePickerDialog = new TimePickerDialog(StartOffActivity.this, new OnTimeSetListener() {
			
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				if (hourOfDay > 9) {
					if (minute > 9) {
						estimated_arrived_time = TimeUtil.getCurrentTimeInString().substring(0, 10) + " "  + hourOfDay + ":" + minute + ":00";
					} else {
						estimated_arrived_time = TimeUtil.getCurrentTimeInString().substring(0, 10) + " "  + hourOfDay + ":0" + minute + ":00";
					}
				} else {
					if (minute > 9) {
						estimated_arrived_time = TimeUtil.getCurrentTimeInString().substring(0, 10) + " 0" + hourOfDay + ":" + minute + ":00";
					} else {
						estimated_arrived_time = TimeUtil.getCurrentTimeInString().substring(0, 10) + " 0" + hourOfDay + ":0" + minute + ":00";
					}
				}
				if (estimated_arrived_time.compareTo(time) > 0) {
					tv_estimated_arrived_time.setText(estimated_arrived_time);
				} else {
					Toast.makeText(StartOffActivity.this, "预计达到时间不能小于当前时间，请重新设置", Toast.LENGTH_SHORT).show();
					estimated_arrived_time = "";
				}
				
			}
		}, hourOfDay, minute, true);
		timePickerDialog.setTitle("预计到达时间");
		timePickerDialog.show();
	}
	
}
