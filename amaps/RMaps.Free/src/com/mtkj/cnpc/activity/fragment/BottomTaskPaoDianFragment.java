package com.mtkj.cnpc.activity.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.andnav.osm.util.GeoPoint;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.protobuf.ByteString;
import com.mtkj.cnpc.activity.MainActivity;
import com.mtkj.cnpc.activity.MainActivity.MainMSG;
import com.mtkj.cnpc.activity.MainActivity.RequestCode;
import com.mtkj.cnpc.activity.fragment.adapter.PaiDuiLineAdapter;
import com.mtkj.cnpc.activity.fragment.adapter.ZhuangHaoAroundSelectAdapter;
import com.mtkj.cnpc.activity.fragment.adapter.ZhuangHaoNormalSelectAdapter;
import com.mtkj.cnpc.protocol.bean.ShotPoint;
import com.mtkj.cnpc.protocol.constants.DSSProtoDataConstants.ProtoMsgType;
import com.mtkj.cnpc.protocol.constants.ProtocolConstants;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.constants.SysContants;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_Head;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_WellShotData;
import com.mtkj.cnpc.protocol.shot.ErrorInfo;
import com.mtkj.cnpc.protocol.shot.GK01;
import com.mtkj.cnpc.protocol.shot.GK02;
import com.mtkj.cnpc.protocol.shot.GK03;
import com.mtkj.cnpc.protocol.shot.GK04;
import com.mtkj.cnpc.protocol.shot.GK05;
import com.mtkj.cnpc.protocol.shot.GK08;
import com.mtkj.cnpc.protocol.shot.GK09;
import com.mtkj.cnpc.protocol.shot.GK10;
import com.mtkj.cnpc.protocol.shot.GK11;
import com.mtkj.cnpc.protocol.shot.GK12;
import com.mtkj.cnpc.protocol.shot.RF01;
import com.mtkj.cnpc.protocol.shot.RF02;
import com.mtkj.cnpc.protocol.shot.RF03;
import com.mtkj.cnpc.protocol.shot.RF04;
import com.mtkj.cnpc.protocol.shot.RF05;
import com.mtkj.cnpc.protocol.shot.RF12;
import com.mtkj.cnpc.protocol.shot.RF13;
import com.mtkj.cnpc.protocol.socket.DataProcess;
import com.mtkj.cnpc.protocol.utils.MathUtils;
import com.mtkj.cnpc.protocol.utils.SocketUtils;
import com.mtkj.cnpc.sqlite.PointDBDao;
import com.mtkj.cnpc.view.TimerDialog;
import com.mtkj.cnpc.R;
import com.robert.maps.applib.utils.LogFileUtil;

import static com.mtkj.cnpc.activity.fragment.BottomTaskPaoDianFragment.MSG_FLAG.MSG_COUNT;
import static com.mtkj.cnpc.activity.fragment.BottomTaskPaoDianFragment.MSG_FLAG.MSG_COUNT_CANCEL;

/***
 *
 *
 * @author TNT
 * 井炮
 */
@SuppressLint("UseSparseArrays")
public class BottomTaskPaoDianFragment extends BaseFragment implements
		OnClickListener {

	public View groupView = null;

	private TextView mainTextView = null;
	private ViewGroup detailsButton = null;
	private TextView detailsTextView = null;

	private ViewGroup shotTaskGroup = null;
	private ViewGroup connectGroup = null;
	private ViewGroup pairGroup = null;
	private ViewGroup locationGroup = null;
	private ViewGroup lockGroup = null;
	private ViewGroup requestLineGroup = null;
	private ViewGroup lineDetailGroup = null;
	private ViewGroup shotFinishGroup = null;
	private ViewGroup nextGroup = null;
	private ViewGroup judgeGroup = null;

	public HashMap<Integer, ViewGroup> mapViewGroup = new HashMap<Integer, ViewGroup>();

	private int curState = TASK_STATE.CONN;

	/** 是否已连接 */
	public static boolean isConn = false;
	/** 是否已连接WSC */
	public static boolean isKuiTan = false;
	/** 是否配对 **/
	public static boolean isPeiDui = false;
	/** 是否已排队 **/
	public static boolean isInLine = false;
	/** 是否排队超时 **/
	public static boolean isPaiduiChaoshi = false;
	/** 是否放炮ok **/
	public static boolean isFangPaoOk = false;
	/** 是否结束任务 */
	public static boolean isStop = false;

	public int cancelPaiDuiOpt = 0;
	public final int CACEL_OPT_1_RE_PAIDUI = 1;
	public final int CACEL_OPT_2_STOP = 2;
	public final int CACEL_OPT_3_INLINE_CANCEL = 3;
	/***
	 * 当前任务实体
	 */
	private ShotPoint currShotPoint = null;

	/**
	 * 目标任务实体
	 */
	public static ShotPoint targetShotPoint = new ShotPoint();

	/***
	 * 当前锁定的任务
	 */
	public ShotPoint lockShotPoint = null;

	/***
	 * 问题反馈
	 */
//	public QuestionEntity questionEntity = null;

	/****
	 * 当前锁定的GPS坐标
	 */
	public Location lockLocation = null;

	/** 当前排队顺序 **/
	public int lineNumber = -1;

	/***
	 * 安全距离提示对话框
	 */
	public AlertDialog disAlertDlg = null;

	/***
	 * 充电提示对话框
	 */
	public TimerDialog chongDianDlg = null;

	/***
	 * 匹配距离提示
	 */
	public Timer pipeiTimer = null;

	/***
	 * 安全提示
	 */
	public Timer safeTimer = null;

	/***
	 * 排队列表
	 */
	public List<String> lineList = null;

	private long pfsDataTime = 0;
	private long paiduiDataTime = 0;

	public PaoDianHandler paoDianHandler = null;

	private Timer timer;

	public PointDBDao mPointDBDao = null;

	// 语音提示
	//private SpeechUtilOffline speechUtilOffline;
	// 语音提示
	private MediaPlayer mMediaPlayer;
	TextView tv_tount;
	String REFUSH_STATUS_PHONE = "refush_status_phone";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPointDBDao = new PointDBDao(getActivity());
		paoDianHandler = new PaoDianHandler(Looper.myLooper());

	}

	@Override
	public void onResume() {
		super.onResume();
		DataProcess.GetInstance().setMsgHandler(paoDianHandler);
//		speechUtilOffline = new SpeechUtilOffline(getActivity());
		if(mMediaPlayer==null) {
			mMediaPlayer = MediaPlayer.create(getActivity(), R.raw.voice);
		}
	}

	@Override
	public void onDestroy() {
		if(mMediaPlayer!=null){
			mMediaPlayer.release();
		}
		super.onDestroy();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		refreshView();
	}

	private View rootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_bottom_task_paodian,
				container, false);
		initView(rootView);
		refreshView();
		return rootView;
	}

	public void refreshView() {
		if (!isHidden()) {
			switchState(curState);
		}
	}

	public void initView(View rootView) {
		groupView = rootView;

		mainTextView = (TextView) rootView.findViewById(R.id.txt_info);
		tv_tount = (TextView) rootView.findViewById(R.id.tv_tount);
		/*detailsTextView = (TextView) rootView.findViewById(R.id.txt_info_two);
		detailsTextView.setVisibility(View.GONE);*/
		detailsButton = (ViewGroup) rootView.findViewById(R.id.vg_details);
		detailsButton.setOnClickListener(this);
		detailsButton.setVisibility(View.GONE);
		rootView.findViewById(R.id.line_split_1).setVisibility(View.GONE);

		/**
		 * shotTaskGroup 井炮源任务 connectGroup 建立连接 pairGroup 开始配对 locationGroup 坐标匹配
		 * lockGroup 重新匹配 requestLineGroup 请求排队 lineDetailGroup 重新排队 shotFinishGroup 合格炮
		 * nextGroup 周边查询
		 */
		shotTaskGroup = (ViewGroup) rootView.findViewById(R.id.vg_00);
		connectGroup = (ViewGroup) rootView.findViewById(R.id.vg_01);
		pairGroup = (ViewGroup) rootView.findViewById(R.id.vg_02);
		locationGroup = (ViewGroup) rootView.findViewById(R.id.vg_03);
		lockGroup = (ViewGroup) rootView.findViewById(R.id.vg_04);
		requestLineGroup = (ViewGroup) rootView.findViewById(R.id.vg_05);
		lineDetailGroup = (ViewGroup) rootView.findViewById(R.id.vg_06);
		shotFinishGroup = (ViewGroup) rootView.findViewById(R.id.vg_07);
		nextGroup = (ViewGroup) rootView.findViewById(R.id.vg_08);
		judgeGroup = (ViewGroup) rootView.findViewById(R.id.vg_09);

		shotTaskGroup.setVisibility(View.INVISIBLE);
		connectGroup.setVisibility(View.INVISIBLE);
		pairGroup.setVisibility(View.INVISIBLE);
		locationGroup.setVisibility(View.INVISIBLE);
		lockGroup.setVisibility(View.INVISIBLE);
		requestLineGroup.setVisibility(View.INVISIBLE);
		lineDetailGroup.setVisibility(View.INVISIBLE);
		shotFinishGroup.setVisibility(View.INVISIBLE);
		nextGroup.setVisibility(View.INVISIBLE);
		judgeGroup.setVisibility(View.INVISIBLE);

		mapViewGroup.clear();
		mapViewGroup.put(TASK_STATE.NONE, shotTaskGroup);
		mapViewGroup.put(TASK_STATE.CONN, connectGroup);
		mapViewGroup.put(TASK_STATE.PEI_DUI, pairGroup);
		mapViewGroup.put(TASK_STATE.PIPEI, locationGroup);
		mapViewGroup.put(TASK_STATE.SUODING, lockGroup);
		mapViewGroup.put(TASK_STATE.PAIDUI, requestLineGroup);
		mapViewGroup.put(TASK_STATE.PAIDUI_WAIT, lineDetailGroup);
		mapViewGroup.put(TASK_STATE.RESULT, shotFinishGroup);
		mapViewGroup.put(TASK_STATE.RESULT_WAIT, shotFinishGroup);
		mapViewGroup.put(TASK_STATE.NEXT, nextGroup);
		mapViewGroup.put(TASK_STATE.JUDGE, judgeGroup);

		connectGroup.findViewById(R.id.btn_conn).setOnClickListener(this);
		connectGroup.findViewById(R.id.btn_conn_setting).setOnClickListener(this);

		pairGroup.findViewById(R.id.btn_peidui).setOnClickListener(this);
		pairGroup.findViewById(R.id.btn_peidui_setting).setOnClickListener(
				this);

		locationGroup.findViewById(R.id.btn_pipei).setOnClickListener(this);
		locationGroup.findViewById(R.id.btn_pipei_setting)
				.setOnClickListener(this);

		lockGroup.findViewById(R.id.btn_suoding).setOnClickListener(this);
		lockGroup.findViewById(R.id.btn_suoding_done).setOnClickListener(this);

		requestLineGroup.findViewById(R.id.btn_paidui).setOnClickListener(this);
		lineDetailGroup.findViewById(R.id.btn_paidui_details).setOnClickListener(
				this);

		lineDetailGroup.findViewById(R.id.btn_paidui_redo).setOnClickListener(this);
		lineDetailGroup.findViewById(R.id.btn_paidui_stop).setOnClickListener(this);

		shotFinishGroup.findViewById(R.id.btn_result_opt1).setOnClickListener(this);
		shotFinishGroup.findViewById(R.id.btn_result_opt2).setOnClickListener(this);
		shotFinishGroup.findViewById(R.id.btn_result_opt3).setOnClickListener(this);

		nextGroup.findViewById(R.id.btn_next_opt1).setOnClickListener(this);
		nextGroup.findViewById(R.id.btn_next_opt2).setOnClickListener(this);

		judgeGroup.findViewById(R.id.btn_judge_opt1).setOnClickListener(this);
		judgeGroup.findViewById(R.id.btn_judge_opt2).setOnClickListener(this);
	}

	/***
	 * 刷新任务信息
	 *
	 * @param entity
	 */
	public void refreshData(ShotPoint shotPoint) {
		this.currShotPoint = shotPoint;
		initArgs();
		if (this.currShotPoint != null) {
			switchState(TASK_STATE.NONE);
		}
	}

	public void initArgs() {
		isInLine = false;
	}

	/***
	 * 结束任务执行
	 */
	public void stopTask() {
		currShotPoint = null;
		lockShotPoint = null;
		switchState(TASK_STATE.NONE);

		// 结束安全距离判断
		stopSafeDisJudge();
		isStop = true;
//		MainActivity.isWorking = false;
		if (isInLine) {
			cancelPaiDui(CACEL_OPT_2_STOP);
		}
	}

	/***
	 * 下一个任务点
	 */
	private void nextWorkPoint() {
		// 更新数据库
		if (mPointDBDao.shotIsAllDone()) {
			// 提示当前任务所有炮点已完成
			showTaskDoneDlg();
		} else {
			// 弹出界面选择下一个任务桩号
			switchState(TASK_STATE.NEXT);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.vg_details: {
				if (curState == TASK_STATE.PAIDUI_WAIT) {
					// 进入排队详情界面
					if (lineList != null) {
						showPaiDuiInfoDlg();
					}
				}
			}
			break;
			case R.id.btn_conn: {
				new ConnTask(ConnTask.TYPE_NEW_CONN).execute("");
			}
			break;
			case R.id.btn_conn_setting: {
				showConnSettingDlg();
			}
			break;
			case R.id.btn_peidui: {
				new PeiDuiTask(PeiDuiTask.TYPE_NEW_PEIDUI).execute("");
			}
			break;
			case R.id.btn_peidui_setting: {
				showBZJSelected();
			}
			break;
			case R.id.btn_pipei: {
				piPeiZhuangHao();
			}
			break;
			case R.id.btn_pipei_setting: {
				PiPeiDisSetting();
			}
			break;
			case R.id.btn_suoding: {// 存储锁定
				switchState(TASK_STATE.PIPEI);
			}
			break;
			case R.id.btn_suoding_done: {// 锁定完成
				if (lockShotPoint != null) {
					stopPiPeiDisJudge();
					startSafeDisJudge();// 开始安全距离判断
				} else {
					showMessage("请先锁定桩号");
				}
			}
			break;
			case R.id.btn_paidui: {// 排队
				// hideDisAlertDlg();
				/**
				 * 判断水平精度因子
				 */
//			if (NaviGPS.GetGpsLocation().GNGGA != null
//					&& !"".equals(NaviGPS.GetGpsLocation().GNGGA)) {
//				String HDOP = NaviGPS.GetGpsLocation().GNGGA.split("\\,")[7];
//				Toast.makeText(getActivity(), "水平精度因子" + HDOP,
//						Toast.LENGTH_SHORT).show();
//				if (Double.valueOf(HDOP) > 10) {
//				} else {
//				}
//			}

				Location location = MainActivity.unRectifyLocation;
				if (lockShotPoint != null && location != null) {
					// 判断是否到达安全距离
					double dis = GeoPoint.fromDouble(location.getLatitude(), location.getLongitude())
							.distanceTo(lockShotPoint.geoPoint);

					new AlertDialog.Builder(getActivity()).setTitle("提示")
							.setMessage("安全距离" + SysConfig.safe_Distance + "米，当前距离井口" + dis + "米")
							.setPositiveButton("是", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									showPaiDui();
								}
							}).create().show();
				}

//			if (isSafeDis()) {
//				showPaiDui();
//			} else {
//				new AlertDialog.Builder(getActivity()).setTitle("提示")
//				.setMessage("安全距离" + SysConfig.safe_Distance + "米")
//				.setPositiveButton("是", new DialogInterface.OnClickListener() {
//
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						showPaiDui();
//					}
//				}).create().show();
//			}

				// switchState(TASK_STATE.PAIDUI_WAIT);
			}
			break;
			case R.id.btn_paidui_redo: {// 重新排队
				rePaidDui();
			}
			break;
			case R.id.btn_paidui_stop: {// 取消排队
				// 发送取消排队消息--无此消息
				cancelPaiDuiOpt = 0;
				cancelPaiDui(CACEL_OPT_2_STOP);
				// 停止距离判断
				stopSafeDisJudge();
				// 回到坐标匹配界面
				switchState(TASK_STATE.PIPEI);
			}
			break;
			case R.id.btn_paidui_details: {// 排队详情
				if (curState == TASK_STATE.PAIDUI_WAIT) {
					// 进入排队详情界面
					if (lineList != null) {
						showPaiDuiInfoDlg();
					}
				}
			}
			break;

			case R.id.btn_result_opt1: {// /合格炮
				Message message = new Message();
				message.what = RequestCode.SHOTPOINT_ISDONE;
				message.obj = lockShotPoint.stationNo;
				handler.sendMessage(message);
				nextWorkPoint();
			}
			break;
			case R.id.btn_result_opt2: {// 废炮
				// showFangPaoQuestionDlg();
				// 进入问题反馈界面
//			Intent intent = new Intent(getActivity(),
//					lockShotPoint.class);
//			intent.putExtra(Constants.KEY.DATA_1, lockShotPoint.getName());
//			intent.putExtra(Constants.KEY.DATA_2, lockShotPoint.getType());
//			intent.putExtra(Constants.KEY.DATA_3,
//					lockShotPoint.getQuestion());
//			intent.putExtra(Constants.KEY.DATA_4,
//					lockShotPoint.getZhuangHao());
//			intent.putExtra(Constants.KEY.DATA_5,
//					lockShotPoint.getLineNumber());
//			intent.putExtra(Constants.KEY.DATA_5,
//					lockShotPoint.getPointNumber());
//			startActivityForResult(intent,
//					Constants.REQUEST.TASK_QUESTION_ACTIVITY);
			}
			break;
			case R.id.btn_result_opt3: {// 原地再放炮
				rePaidDui();
			}
			break;
			case R.id.btn_next_opt1: {// 周边查询
				if (!SysConfig.isTestMode) {
					if (MainActivity.unRectifyLocation != null) {
						new ZhuangHaoAroundQueryTask(
								GeoPoint.fromDouble(MainActivity.unRectifyLocation.getLatitude(),
										MainActivity.unRectifyLocation.getLongitude()), 0).execute("");
					} else {
						showMessage("未定位");
					}
				} else {
					if (MainActivity.unRectifyLocation != null) {
						new ZhuangHaoAroundQueryTask(
								GeoPoint.fromDouble(MainActivity.unRectifyLocation.getLatitude(),
										MainActivity.unRectifyLocation.getLongitude()), 0).execute("");
					} else {
						showMessage("未定位");
					}
				}
			}
			break;
			case R.id.btn_next_opt2: {
//				showTaskPaoDianSelectHandle();
				getActivity().onBackPressed();
			}
			break;
			case R.id.btn_judge_opt1:
				//给wsc发送放炮成功指令
				detailsButton.setVisibility(View.GONE);
				new OverTimeTask().execute("1");
				Message message = new Message();
				message.what = RequestCode.SHOTPOINT_ISDONE;
				message.obj = lockShotPoint.stationNo;
				handler.sendMessage(message);
				nextWorkPoint();
				if (mTimerTask!=null && mTimer!=null){
					mTimer.cancel();
					mTimerTask.cancel();
					mTimerTask = null;
					mTimer = null;
				}
				break;
			case R.id.btn_judge_opt2:
				//给wsc发送放炮失败指令
				new OverTimeTask().execute("0");
				mainTextView.setText("执行成功");
				if (mTimerTask!=null && mTimer!=null){
					mTimer.cancel();
					mTimerTask.cancel();
					mTimerTask = null;
					mTimer = null;
				}
				switchState(TASK_STATE.NONE);
				break;
			default:
				break;
		}
	}



	private void piPeiZhuangHao() {
		if (!SysConfig.isTestMode) {
			// 判断定位
			if (MainActivity.unRectifyLocation != null) {
				Location location = MainActivity.unRectifyLocation;
				new PiPeiZhuangHaoTask(location).execute("");
			} else {
				showMessage("当前设备未定位,请稍候再试");
			}
		} else {
			Location location = MainActivity.unRectifyLocation;
			if (location != null) {
				new PiPeiZhuangHaoTask(location).execute("");
			} else {
				showMessage("当前设备未定位,请稍候再试");
			}
		}
	}

	/***
	 * 取消排队
	 */
	private boolean cancelPaiDui(int nOpt) {
		boolean bRt = false;
		cancelPaiDuiOpt = nOpt;
		if (isInLine) {
			// 取消排队
			new CancelPaiduiTask().execute("");
			bRt = true;
		} else {
			bRt = true;
		}
		return bRt;
	}

	public class CancelPaiduiTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				if (SysConfig.isDSCloud) {
					Proto_WellShotData proto_WellShotData = Proto_WellShotData
							.newBuilder()
							.setMdata(
									ByteString.copyFrom(SocketUtils
											.writeBodyBytes(new RF05(
													String.valueOf(Integer.valueOf(SysConfig.SC_ID))).content
													.getBytes()))).build();
					Proto_Head head = Proto_Head
							.newBuilder()
							.setProtoMsgType(
									ProtoMsgType.protoMsgType_HandsetToWSC)
							.setCmdSize(proto_WellShotData.toByteArray().length)
							.addReceivers(ByteString.copyFrom(SysConfig.WSC, "GB2312"))
							.setReceivers(0, ByteString.copyFrom(SysConfig.WSC, "GB2312"))
							.setSender(
									ByteString
											.copyFrom(SysConfig.SC, "GB2312")).setMsgId(0)
							.setPriority(1).setExpired(0).build();
					try {
						if (DataProcess.isLoginDscloud) {
							DataProcess.GetInstance().sendData(
									SocketUtils.writeBytes(head.toByteArray(),
											proto_WellShotData.toByteArray()));
						}
						DataProcess.GetInstance().sendData(
								SocketUtils.writeBytes(head.toByteArray(),
										proto_WellShotData.toByteArray()));
						paoDianHandler.sendEmptyMessageDelayed(
								MSG_FLAG.MSG_QUXIAOPAIDUI_WAIT, 5000);
						lineList = null;
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					DataProcess.GetInstance().sendData(
							new RF05(String.valueOf(Integer.valueOf(SysConfig.SC_ID))));
					paoDianHandler.sendEmptyMessageDelayed(
							MSG_FLAG.MSG_QUXIAOPAIDUI_WAIT, 5000);
					lineList = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
				LogFileUtil.saveFileToSDCard("RF05: " + e.getMessage());
			}
			return null;
		}

	}

	private ProgressDialog progressDialog = null;
	/***
	 * 开始排队
	 */
	private boolean startPaiDui() {
		boolean bRt = false;
		// 排队
		new StartPaiduiTask().execute("");
		return true;
	}

	public class StartPaiduiTask extends AsyncTask<String, Integer, Boolean> {



		@Override
		protected void onPreExecute() {
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			progressDialog = new ProgressDialog(getActivity());
			progressDialog.setMessage("正在发送排队请求...");
			progressDialog.setCancelable(false);
			progressDialog.show();
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				if (SysConfig.isDSCloud) {
					Proto_WellShotData proto_WellShotData = Proto_WellShotData
							.newBuilder()
							.setMdata(
									ByteString.copyFrom(SocketUtils
											.writeBodyBytes(new RF04(String.valueOf(Integer.valueOf(SysConfig.SC_ID)),
													currShotPoint.stationNo, currShotPoint.geoPoint,
													lockLocation).content
													.getBytes()))).build();
					Proto_Head head = Proto_Head
							.newBuilder()
							.setProtoMsgType(ProtoMsgType.protoMsgType_HandsetToWSC)
							.setCmdSize(proto_WellShotData.toByteArray().length)
							.addReceivers(ByteString.copyFrom(SysConfig.WSC, "GB2312"))
							.setReceivers(0, ByteString.copyFrom(SysConfig.WSC, "GB2312"))
							.setSender(
									ByteString.copyFrom(SysConfig.SC, "GB2312"))
							.setMsgId(0).setPriority(1).setExpired(0).build();
					try {
						if (DataProcess.isLoginDscloud) {
							DataProcess.GetInstance().sendData(
									SocketUtils.writeBytes(head.toByteArray(),
											proto_WellShotData.toByteArray()));
							isPaiduiChaoshi = true;
							paoDianHandler.sendEmptyMessageDelayed(
									MSG_FLAG.MSG_PAIDUI_WAIT, 5000);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					DataProcess.GetInstance().sendData(
							new RF04(String.valueOf(Integer.valueOf(SysConfig.SC_ID)), currShotPoint.stationNo, currShotPoint.geoPoint,
									lockLocation));
					isPaiduiChaoshi = true;
					paoDianHandler.sendEmptyMessageDelayed(
							MSG_FLAG.MSG_PAIDUI_WAIT, 5000);
				}
			} catch (IOException e) {
				e.printStackTrace();
				LogFileUtil.saveFileToSDCard("RF04: " + e.getMessage());
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
		}

	}


	/**
	 * 是否退出任务
	 *
	 *
	 * public void isStopTask() { AlertDialog.Builder builder = new
	 * AlertDialog.Builder(getActivity()); builder.setTitle("提示");
	 * builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
	 * public void onClick(DialogInterface dialog, int which) {
	 * Log.i(Constants.TAG, "结束任务"); // 退出 Message msg = new Message(); msg.what
	 * = MainActivity.MainMSG.MSG_SHOW_TASK_STOP; handler.sendMessage(msg); }
	 * }); builder.setNegativeButton("取消", null);
	 * builder.setMessage("是否要结束任务作业？"); builder.show(); }
	 */

	private void showConn() {
		if (!DataProcess.GetInstance().isConnected()) {
			mainTextView.setText(CONN_STATE.UNCONNECTED);
			new ConnTask(ConnTask.TYPE_NEW_CONN).execute("");
		} else if (!isPeiDui) {
			mainTextView.setText(CONN_STATE.UNPEIDUI);
			if ((SysConfig.BZJ_ID != null && SysConfig.BZJ_ID.length() > 0)
					&& (SysConfig.SC_ID != null && SysConfig.SC_ID.length() > 0)
					&& (SysConfig.ZZJG_ID != null && SysConfig.ZZJG_ID.length() > 0)) {
				new PeiDuiTask(PeiDuiTask.TYPE_NEW_PEIDUI).execute("");
			} else {
				showBZJSelected();
			}
		} else {// 进入坐标匹配状态
			mainTextView.setText(CONN_STATE.PEIDUI);
			switchState(TASK_STATE.PIPEI);
		}
	}

	public void refreshConnState() {
		if (!DataProcess.GetInstance().isConnected()) {
			connDlg();
		} else if (!isPeiDui) {
			peiduiDlg();
		} else {
			switchState(TASK_STATE.PIPEI);
		}
	}

	public void showPiPei() {
		mainTextView.setText("匹配桩号");
		// 开始匹配
		piPeiZhuangHao();
	}

	/***
	 * 显示排队界面
	 *
	 * @param isStartPaiduiAuto
	 */
	public void showPaiDui() {
		paiduiDataTime = System.currentTimeMillis();
		long timeDiffince = (paiduiDataTime - pfsDataTime) / 1000;
		if (timeDiffince > SysConfig.PowerTimeout) {
			if (startPaiDui()) {
				if (curState != TASK_STATE.PAIDUI_WAIT) {
					switchState(TASK_STATE.PAIDUI_WAIT);
				}
			} else {// 信息发送失败
				showMessage("发送排队消息失败");
				connDlg();
			}
		} else {
			showMessage("还在能量衰减中");
		}
	}

	/***
	 * 排队等待界面
	 */
	public void showPaiDuiWait() {
		// 监听排队信息
		if (lineList != null) {
			refreshLineInfo(lineNumber);
		}
	}

	/**
	 * 显示放炮结果界面
	 */
	public void showResultWait() {
		// 等待放炮结果反馈
		waitFangPaoResult();
	}

	/**
	 * 显示放炮结果界面
	 */
	public void showResult() {
		// 结束安全距离判断
		stopSafeDisJudge();
		// 放炮结果--操作
		fangPaoResult();
	}

	/***
	 * 显示下一个炮点选择界面
	 */
	public void showNext() {
		mainTextView.setText("请选择下一个任务点");
//		showTaskPaoDianSelect();
		// new ZhuangHaoNormalQueryTask().execute("");
	}

	/***
	 * 刷新排队信息
	 */
	public void refreshLineInfo(int lineNumber) {
		if (curState == TASK_STATE.PAIDUI_WAIT) {
			if (lineNumber > 0) {
				mainTextView.setText("已排至第" + lineNumber + "位");
				if (lineNumber == 1) {
					mainTextView.setText("已排至第" + lineNumber + "位");
					// 进入充电结果等待界面
					// switchState(TASK_STATE.RESULT_WAIT);
				} else {
					// 提示排队等待中
					showMessage("您已排至第" + lineNumber + "位");
				}
			} else {
				mainTextView.setText("不在队列");
			}
		}
	}

	private void waitFangPaoResult() {
		mainTextView.setText("等待放炮结果");
		shotFinishGroup.findViewById(R.id.btn_result_opt1).setEnabled(false);
		// shotFinishGroup.findViewById(R.id.btn_result_opt2).setEnabled(false);
		// shotFinishGroup.findViewById(R.id.btn_result_opt3).setEnabled(false);
	}

	private void fangPaoResult() {
		mainTextView.setText("放炮完成");
		shotFinishGroup.findViewById(R.id.btn_result_opt1).setEnabled(true);
		// shotFinishGroup.findViewById(R.id.btn_result_opt2).setEnabled(true);
		// shotFinishGroup.findViewById(R.id.btn_result_opt3).setEnabled(true);
	}

	/***
	 * 开始匹配距离判断
	 */
	private void startPiPeiDisJudge() {
		if (pipeiTimer == null) {
			pipeiTimer = new Timer();
		}
		pipeiTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				// 发送排队消息
				paoDianHandler.sendEmptyMessage(MSG_FLAG.MSG_PIPEI_DIS_INFO);
			}
		}, 100, 2000);
	}

	/***
	 * 结束匹配距离判断
	 */
	private void stopPiPeiDisJudge() {
		if (pipeiTimer != null) {
			pipeiTimer.cancel();
			pipeiTimer = null;
		}
	}

	/***
	 * 开始安全距离判断
	 */
	private void startSafeDisJudge() {
//		if (safeTimer != null) {
//			stopSafeDisJudge();
//		}
//		if (safeTimer == null) {
//			safeTimer = new Timer();
//		}
//		safeTimer.schedule(new TimerTask() {
//
//			@Override
//			public void run() {
//				// 发送排队消息
//				paoDianHandler.sendEmptyMessage(MSG_FLAG.MSG_PAIDUI_OPT);
//			}
//		}, 100, 2000);
	}

	/***
	 * 结束安全距离判断
	 */
	private void stopSafeDisJudge() {
		if (safeTimer != null) {
			safeTimer.cancel();
			safeTimer = null;
		}
	}

	/**
	 * 是否安全距离
	 *
	 * @return
	 */
	public boolean isSafeDis() {
		boolean isSafe = false;
		Location location = MainActivity.unRectifyLocation;
		if (lockShotPoint != null && location != null) {
			// 判断是否到达安全距离
			double dis = lockShotPoint.geoPoint.distanceTo(location.getLatitude(), location.getLongitude());
			dis = MathUtils.GetAccurateNumber(dis, 1);
			double disOff = MathUtils.GetAccurateNumber(
					dis - location.getAccuracy(), 1);
			if (dis > SysConfig.safe_Distance) {
				isSafe = true;
			} else {
				isSafe = false;
			}
		}
		return isSafe;
	}

	private void switchState(int nState) {
		if (mapViewGroup != null && mapViewGroup.size() > 0) {
			changeView(nState);
			switch (nState) {
				case TASK_STATE.NONE: {// 恢复初始化
					curState = TASK_STATE.CONN;
				}
				break;
				case TASK_STATE.CONN:
					if (detailsButton.getVisibility() != View.GONE) {
						detailsButton.setVisibility(View.GONE);
					}
					isStop = false;
					if (!DataProcess.GetInstance().isConnected()) {
						mainTextView.setText(CONN_STATE.UNCONNECTED);
						new ConnTask(ConnTask.TYPE_NEW_CONN).execute("");
					} else {
						isConn = true;
						switchState(TASK_STATE.PEI_DUI);
					}
					break;
				case TASK_STATE.PEI_DUI:
					isStop = false;
					showConn();
					break;
				case TASK_STATE.PIPEI:
					showPiPei();
					break;
				case TASK_STATE.SUODING:
					stopPiPeiDisJudge();
					// 开始安全距离判断
					startSafeDisJudge();

					switchState(TASK_STATE.PAIDUI);
					break;
				case TASK_STATE.PAIDUI:
					// {{-----取消自动排队---20141118-----
					// showPaiDui();
					// if (isSafeDis()) {
					// hideDisAlertDlg();
					/**
					 * 判断水平精度因子
					 */
//				if (NaviGPS.GetGpsLocation().GNGGA != null
//						&& !"".equals(NaviGPS.GetGpsLocation().GNGGA)) {
//					String HDOP = NaviGPS.GetGpsLocation().GNGGA.split("\\,")[7];
//					Toast.makeText(getActivity(), "水平精度因子" + HDOP,
//							Toast.LENGTH_SHORT).show();
//					if (Double.valueOf(HDOP) > 10) {
//					} else {
//					}
//				}

//				if (isSafeDis()) {
//					mainTextView.setText("已进入安全区，可以开始排队");
//					Util.MsgBox(getActivity(), "已进入安全区，可以开始排队");
//					Util.Vibrate(getActivity(), 200);
//				} else {
//					mainTextView.setText("请移动到安全距离");
//				}
					mainTextView.setText("可以开始排队");

					break;
				case TASK_STATE.PAIDUI_WAIT:
					showPaiDuiWait();
					break;
				case TASK_STATE.RESULT:
					showResult();
					break;
				case TASK_STATE.JUDGE:
					mainTextView.setText("爆炸超时，请手动选择放炮结果");
					break;
				case TASK_STATE.NEXT: {
					showNext();
				}
				break;
				default:
					break;
			}
		}
	}

	int count = 44;
	private Timer mTimer;
	private TimerTask mTimerTask;
	private void startTimer() {
		mTimerTask = new TimerTask() {
			@Override
			public void run() {
				if (count==0){
					paoDianHandler.sendEmptyMessage(MSG_COUNT_CANCEL);
				}else {
					paoDianHandler.sendEmptyMessage(MSG_COUNT);
					count--;
				}
			}
		};
		mTimer = new Timer();
		mTimer.schedule(mTimerTask,0,1000);
	}
	// 界面切换
	public void changeView(int nState) {
		switch (nState) {
			case TASK_STATE.NONE:
				mapViewGroup.get(TASK_STATE.NONE).setVisibility(View.VISIBLE);
				mapViewGroup.get(TASK_STATE.CONN).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PEI_DUI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PIPEI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.SUODING).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PAIDUI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PAIDUI_WAIT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.RESULT_WAIT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.RESULT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.NEXT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.JUDGE).setVisibility(View.GONE);
				detailsButton.setVisibility(View.GONE);
				break;

			case TASK_STATE.CONN:
				mapViewGroup.get(TASK_STATE.NONE).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.CONN).setVisibility(View.VISIBLE);
				mapViewGroup.get(TASK_STATE.PEI_DUI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PIPEI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.SUODING).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PAIDUI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PAIDUI_WAIT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.RESULT_WAIT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.RESULT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.NEXT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.JUDGE).setVisibility(View.GONE);
				break;

			case TASK_STATE.PEI_DUI:
				mapViewGroup.get(TASK_STATE.NONE).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.CONN).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PEI_DUI).setVisibility(View.VISIBLE);
				mapViewGroup.get(TASK_STATE.PIPEI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.SUODING).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PAIDUI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PAIDUI_WAIT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.RESULT_WAIT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.RESULT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.NEXT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.JUDGE).setVisibility(View.GONE);
				break;

			case TASK_STATE.PIPEI:
				mapViewGroup.get(TASK_STATE.NONE).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.CONN).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PEI_DUI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PIPEI).setVisibility(View.VISIBLE);
				mapViewGroup.get(TASK_STATE.SUODING).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PAIDUI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PAIDUI_WAIT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.RESULT_WAIT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.RESULT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.NEXT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.JUDGE).setVisibility(View.GONE);
				break;

			case TASK_STATE.SUODING:
				mapViewGroup.get(TASK_STATE.NONE).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.CONN).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PEI_DUI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PIPEI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.SUODING).setVisibility(View.VISIBLE);
				mapViewGroup.get(TASK_STATE.PAIDUI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PAIDUI_WAIT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.RESULT_WAIT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.RESULT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.NEXT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.JUDGE).setVisibility(View.GONE);
				break;

			case TASK_STATE.PAIDUI:
				mapViewGroup.get(TASK_STATE.NONE).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.CONN).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PEI_DUI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PIPEI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.SUODING).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PAIDUI).setVisibility(View.VISIBLE);
				mapViewGroup.get(TASK_STATE.PAIDUI_WAIT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.RESULT_WAIT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.RESULT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.NEXT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.JUDGE).setVisibility(View.GONE);
				break;

			case TASK_STATE.PAIDUI_WAIT:
				mapViewGroup.get(TASK_STATE.NONE).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.CONN).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PEI_DUI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PIPEI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.SUODING).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PAIDUI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PAIDUI_WAIT).setVisibility(View.VISIBLE);
				mapViewGroup.get(TASK_STATE.RESULT_WAIT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.RESULT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.NEXT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.JUDGE).setVisibility(View.GONE);
				break;

			case TASK_STATE.RESULT_WAIT:
				mapViewGroup.get(TASK_STATE.NONE).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.CONN).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PEI_DUI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PIPEI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.SUODING).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PAIDUI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PAIDUI_WAIT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.RESULT_WAIT).setVisibility(View.VISIBLE);
				mapViewGroup.get(TASK_STATE.RESULT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.NEXT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.JUDGE).setVisibility(View.GONE);
				break;

			case TASK_STATE.RESULT:
				mapViewGroup.get(TASK_STATE.NONE).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.CONN).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PEI_DUI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PIPEI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.SUODING).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PAIDUI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PAIDUI_WAIT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.RESULT_WAIT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.RESULT).setVisibility(View.VISIBLE);
				mapViewGroup.get(TASK_STATE.NEXT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.JUDGE).setVisibility(View.GONE);
				break;

			case TASK_STATE.NEXT:
				mapViewGroup.get(TASK_STATE.NONE).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.CONN).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PEI_DUI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PIPEI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.SUODING).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PAIDUI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PAIDUI_WAIT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.RESULT_WAIT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.RESULT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.NEXT).setVisibility(View.VISIBLE);
				mapViewGroup.get(TASK_STATE.JUDGE).setVisibility(View.GONE);
				break;

			case TASK_STATE.JUDGE:
				detailsButton.setVisibility(View.VISIBLE);
				mapViewGroup.get(TASK_STATE.NONE).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.CONN).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PEI_DUI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PIPEI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.SUODING).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PAIDUI).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.PAIDUI_WAIT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.RESULT_WAIT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.RESULT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.NEXT).setVisibility(View.GONE);
				mapViewGroup.get(TASK_STATE.JUDGE).setVisibility(View.VISIBLE);
				count=44;
				startTimer();
				break;
		}
		curState = nState;
	}

	public interface TASK_STATE {
		int NONE = 0X1000;
		int CONN = 0X1001;
		int PEI_DUI = 0X1002;
		int PIPEI = 0X1003;
		int SUODING = 0X1004;
		int PAIDUI = 0X1005;
		int PAIDUI_WAIT = 0X1006;
		int RESULT_WAIT = 0X1007;
		int RESULT = 0X1008;
		int NEXT = 0X1009;
		int JUDGE = 0X1010;
	}

	public interface MSG_FLAG {
		int MSG_SWITCH_TO = 0x100;
		int MSG_CHECK_IS_PEIDUI_OK = 0x101;
		int MSG_PIPEI_DIS_INFO = 0x102;
		int MSG_PAIDUI_OPT = 0x103;
		int MSG_PAIDUI_WAIT = 0x104;
		int MSG_QUXIAOPAIDUI_WAIT = 0x106;
		int MSG_KUITAN = 0x105;
		int MSG_COUNT_CANCEL = 0x107;
		int MSG_COUNT = 0x108;

	}

	public class PaoDianHandler extends Handler {

		public PaoDianHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			if (msg != null) {
				switch (msg.what) {
					case MSG_FLAG.MSG_KUITAN:{
						if (DataProcess.GetInstance().isConnected()) {

							Message message = new Message();
							message.what = MainMSG.MSG_IS_KUITAN;
							message.obj = false;
							handler.sendMessage(message);
						}
					}
					case MSG_FLAG.MSG_SWITCH_TO: {// 切换
					}
					break;
					case MSG_FLAG.MSG_CHECK_IS_PEIDUI_OK: {
						if (DataProcess.GetInstance().isConnected()) {
							if (!isPeiDui) {
								if (curState != TASK_STATE.RESULT) {
									DlgPeiDuiFailed();
								}
							}
						} else {
							connDlg();
						}

					}
					break;
					case MSG_FLAG.MSG_PIPEI_DIS_INFO: {
						if (curState == TASK_STATE.PIPEI) {
							Location location = MainActivity.unRectifyLocation;
							if (currShotPoint != null && location != null) {
								double dis = currShotPoint.geoPoint.distanceTo(location.getLatitude(), location.getLongitude());
								if (dis > SysConfig.ShotproMax) {
									mainTextView
											.setText("桩号:"
													+ currShotPoint.stationNo
													+ "\n-距离:"
													+ MathUtils.GetAccurateNumber(dis,
													1) + "米");
								} else {
									mainTextView.setText("进入匹配范围");
									stopPiPeiDisJudge();
								}
							}
						}
					}
					break;
					case MSG_FLAG.MSG_PAIDUI_OPT: {
						// hideDisAlertDlg();
						/**
						 * 判断水平精度因子
						 */
//					if (NaviGPS.GetGpsLocation().GNGGA != null
//							&& !"".equals(NaviGPS.GetGpsLocation().GNGGA)) {
//						String HDOP = NaviGPS.GetGpsLocation().GNGGA.split("\\,")[7];
//						Toast.makeText(getActivity(), "水平精度因子" + HDOP,
//								Toast.LENGTH_SHORT).show();
//						if (Double.valueOf(HDOP) > 10) {
//						} else {
//						}
//					}
						if (isSafeDis()) {
							if (curState == TASK_STATE.SUODING) {// 是否在存储锁定状态
								switchState(TASK_STATE.PAIDUI);
							}
						} else {
							// 取消
							if (isInLine || curState >= TASK_STATE.PAIDUI) {// 如果在排队状态，则取消
								// isInLine = false;
								cancelPaiDuiOpt = 0;
								// 取消排队
								cancelPaiDui(CACEL_OPT_3_INLINE_CANCEL);
								// 切换到排队请求界面
							} else if (curState == TASK_STATE.SUODING) {
								switchState(TASK_STATE.PAIDUI);
							} else if (curState == TASK_STATE.PAIDUI) {
								switchState(TASK_STATE.PAIDUI);
							}
						}
					}
					break;
					case MSG_FLAG.MSG_PAIDUI_WAIT: {
						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
						if (isInLine) {
							switchState(TASK_STATE.PAIDUI_WAIT);
						} else {
							if (isPaiduiChaoshi) {
								showPaiDuiTimeOutDlg();
								isPaiduiChaoshi = false;
							} else {
								showMessage("已被踢出队伍");
							}
						}
					}
					break;

					case MSG_FLAG.MSG_QUXIAOPAIDUI_WAIT:{
					}
					case DataProcess.MSG.CONN_START:
						isConn = true;
						isPeiDui = false;
						break;
					case DataProcess.MSG.CONN_BREAK:
						isConn = false;
						isPeiDui = false;
						refreshConnState();
						if (SysConfig.isTestMode) {
							showMessage("连接断开--CONN_BREAK");
						}
						break;
					case DataProcess.MSG.CONN_STOP:
						isConn = false;
						isPeiDui = false;
						refreshConnState();
						if (SysConfig.isTestMode) {
							showMessage("连接断开--CONN_STOP");
						}
						break;
					case DataProcess.MSG.RECEIVE:
						String msgs = (String) msg.obj;
//						Toast.makeText(getActivity(), msgs, Toast.LENGTH_SHORT).show();
						Log.e("RECIEVE MSG: ",msgs);
						LogFileUtil.saveFileToSDCard("RECIEVE MSG: " + (String) msg.obj);
						handleMsg((String) msg.obj);
						break;
					case DataProcess.MSG.WRITING:

						break;
					case MSG_COUNT_CANCEL:
						mainTextView.setText("请执行下一步操作");
						tv_tount.setText(count+"s");
						stopTask();
						if (mTimer!=null && mTimerTask!=null){
							mTimer.cancel();
							mTimerTask.cancel();
							mTimer = null;
							mTimerTask = null;
						}
						count=44;
						break;
					case MSG_COUNT:
						tv_tount.setText(count+"s");
						break;
					case DataProcess.MSG.LOGIN:
						Intent intent = new Intent(REFUSH_STATUS_PHONE);
						getActivity().sendBroadcast(intent);
						break;
					default:
						break;
				}
				super.handleMessage(msg);
			}
		}
	}

	private void handleMsg(String msg) {
		if (msg != null && msg.length() > 0) {
			if (msg.startsWith(ProtocolConstants.NAME_JINGPAO.GK01)) {// 窥探反馈
				handleMsgGK01(msg);

			} else if (msg.startsWith(ProtocolConstants.NAME_JINGPAO.GK02)) {// 配对反馈
				handleMsgGK02(msg);

			} else if (msg.startsWith(ProtocolConstants.NAME_JINGPAO.GK03)) {// 请求取消配对反馈
				handleMsgGK03(msg);

			} else if (msg.startsWith(ProtocolConstants.NAME_JINGPAO.GK04)) {// 请求排队反馈
				handleMsgGK04(msg);

			} else if (msg.startsWith(ProtocolConstants.NAME_JINGPAO.GK05)) {// 取消排队
				handleMsgGK05(msg);

			} else if (msg.startsWith(ProtocolConstants.NAME_JINGPAO.GK08)) {// 参数反馈
				Log.e("gk08",msg);
				handleMsgGK08(msg);

			} else if (msg.startsWith(ProtocolConstants.NAME_JINGPAO.GK09)) {// 充电提示
				handleMsgGK09(msg);

			} else if (msg.startsWith(ProtocolConstants.NAME_JINGPAO.GK10)) {// 充电错误
				handleMsgGK10(msg);

			} else if (msg.startsWith(ProtocolConstants.NAME_JINGPAO.GK11)) {// 完成消息
				handleMsgGK11(msg);

			}else if (msg.startsWith(ProtocolConstants.NAME_JINGPAO.GK12)) {// 更改放炮模式
				Log.e("gk12",msg);
				handleMsgGK12(msg);

			} else {

			}
		}
	}

	/***
	 * 窥探反馈--处理
	 *
	 * @param msg
	 **/
	private void handleMsgGK01(String msg) {
		GK01 gk01 = new GK01(msg);
		isKuiTan = (Boolean) gk01.parseMsg(msg);
		Message message = new Message();
		message.what = MainMSG.MSG_IS_KUITAN;
		message.obj = isKuiTan;
		handler.sendMessage(message);
	}

	/***
	 * 请求配对反馈--处理
	 *
	 * @param msg
	 */
	private void handleMsgGK02(String msg) {
		if (progressDialog != null && progressDialog.isShowing() && !getActivity().isFinishing()) {
			progressDialog.dismiss();
		}
		GK02 gk02 = new GK02(msg);
		boolean isOk = (Boolean) gk02.parseMsg(msg);
		if (isOk) {
			isPeiDui = true;
		} else {
			isPeiDui = false;
		}
		refreshConnState();
	}

	/***
	 * 请求取消配对反馈--处理
	 *
	 * @param msg
	 */
	private void handleMsgGK03(String msg) {
		GK03 gk03 = new GK03(msg);
		boolean isOk = (Boolean) gk03.parseMsg(msg);
		if (isOk) {// 取消配对成功
			isPeiDui = false;
			if (!isStop) {
				showMessage("已取消配对");
				peiduiDelDlg();
			}
		} else {
		}
	}

	/***
	 * 请求排队反馈--处理
	 *
	 * @param msg
	 */
	@SuppressWarnings("unchecked")
	private void handleMsgGK04(String msg) {
		if (progressDialog != null && progressDialog.isShowing() && !getActivity().isFinishing()) {
			progressDialog.dismiss();
		}
		GK04 gk04 = new GK04(msg);
		if (msg != null && msg.length() > 0) {
			String[] datas = msg.split(ProtocolConstants.SPLITE_SYMBLE);
			if (datas != null && datas.length >= 3) {
				if ("0".equals(datas[1])) {
					if (lineList != null) {
						lineList.clear();
					}
					if (isInLine) {
						lineList = (List<String>) gk04.parseMsg(msg);
					}
				} else if (String.valueOf(Integer.valueOf(SysConfig.SC_ID)).equals(datas[1])) {
					isPaiduiChaoshi = false;
					if (lineList != null) {
						lineList.clear();
					}
					lineList = (List<String>) gk04.parseMsg(msg);
				}
			}
		}
		if (lineList != null && lineList.size() > 0) {
			isInLine = false;
			for (String id : lineList) {
				if (String.valueOf(Integer.valueOf(SysConfig.SC_ID)).equals(id)) {
					isInLine = true;
					curState = TASK_STATE.PAIDUI_WAIT;
					break;
				}
			}
		} else {
			isInLine = false;
		}
		lineNumber = 0;
		if (isInLine) {
			// 刷新排队信息
			if (lineList != null) {
				for (int i = 0; i < lineList.size(); i++) {
					if (lineList.get(i).equals(String.valueOf(Integer.valueOf(SysConfig.SC_ID)))) {
						lineNumber = i + 1;
						break;
					}
				}
			}
			stopSafeDisJudge();
			// 刷新排队信息
			refreshLineInfo(lineNumber);
		} else {
			refreshLineInfo(lineNumber);
			curState = TASK_STATE.PAIDUI;
			hideChongDianDlg();
			hideWaitResultDlg();
		}
	}

	/***
	 * 重新配对提示
	 */
	private void peiduiDlg() {
		new AlertDialog.Builder(getActivity()).setTitle("提示")
				.setMessage("当前设备未配对，是否重新配对?")
				.setPositiveButton("是", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						new PeiDuiTask(PeiDuiTask.TYPE_REPEIDUI).execute("");
					}
				}).setNegativeButton("否", null).create().show();
	}

	/***
	 * 重新配对提示
	 */
	private void peiduiDelDlg() {
		new AlertDialog.Builder(getActivity()).setTitle("提示")
				.setMessage("当前设备未配对，是否重新配对?")
				.setPositiveButton("是", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						new PeiDuiTask(PeiDuiTask.TYPE_REPEIDUI).execute("");
					}
				}).setNegativeButton("否", null).create().show();
	}

	/***
	 * 重新连接
	 */
	private void connDlg() {
		new ConnTask(ConnTask.TYPE_RECONN).execute("");
	}

	/**
	 * 取消排队
	 *
	 * @param msg
	 */
	private void handleMsgGK05(String msg) {
		if (progressDialog != null && progressDialog.isShowing() && !getActivity().isFinishing()) {
			progressDialog.dismiss();
		}
		GK05 gk05 = new GK05(msg);
		if ((Boolean) gk05.parseMsg(msg)) {
			isInLine = false;

			showMessage( "已取消排队");
		}
		// 结束操作
		if (cancelPaiDuiOpt == CACEL_OPT_2_STOP) {
			lockShotPoint = null;
			lockLocation = null;
		} else if (cancelPaiDuiOpt == CACEL_OPT_3_INLINE_CANCEL) {
			hideChongDianDlg();
			hideWaitResultDlg();
			mainTextView.setText("进入危险区域，已取消排队");
			showMessage("进入危险区域，已取消排队");
			adapter.showType = adapter.TYPE_INLINE_CANCEL;
			adapter.optTipContent = "进入危险区域，已取消排队";
			adapter.notifyDataSetChanged();
		}
		cancelPaiDuiOpt = 0;
	}

	/***
	 * 配置更新
	 *
	 * @param msg
	 */
	@SuppressWarnings("unchecked")
	private void handleMsgGK08(String msg) {
		GK08 gk08 = new GK08(msg);
		List<String> configResult = (List<String>) gk08.parseMsg(msg);
		if (configResult != null && configResult.size() > 0) {

			try {
				String shotproMax = configResult.get(0);
				if (shotproMax != null && shotproMax.length() > 0) {
					SysConfig.ShotproMax = Float.parseFloat(shotproMax);
					savePiPeiDisConfig();
				}
			} catch (Exception e) {
			}

			try {
				String Distance = configResult.get(1);
				if (Distance != null && Distance.length() > 0) {
					SysConfig.safe_Distance = Float.parseFloat(Distance);
					saveSafeDisConfig();
				}
			} catch (Exception e) {
			}

			try {
				String Distance = configResult.get(2);
				if (Distance != null && Distance.length() > 0) {
					SysConfig.Readytimeout = Float.parseFloat(Distance);
					saveReadyTimeOutConfig();
				}
			} catch (Exception e) {
			}

			try {
				String Distance = configResult.get(3);
				if (Distance != null && Distance.length() > 0) {
					SysConfig.PowerTimeout = Float.parseFloat(Distance);
					savePowerTimeOutConfig();
				}
			} catch (Exception e) {
			}
		}
	}

	/***
	 * 充电提示信号
	 *
	 * @param msg
	 */
	private void handleMsgGK09(String msg) {
		GK09 gk09 = new GK09(msg);
		String id = (String) gk09.parseMsg(msg);
		if (String.valueOf(Integer.valueOf(SysConfig.SC_ID)).equals(id)) {
//			speechUtilOffline.play("请充电");
			if(mMediaPlayer==null) {
				mMediaPlayer = MediaPlayer.create(getActivity(), R.raw.voice);
			}
			mMediaPlayer.start();
			showChongDianDlg();
		}
	}

	/**
	 * 充电错误提示
	 *
	 * @param msg
	 */
	private void handleMsgGK10(String msg) {
		GK10 gk10 = new GK10(msg);
		String result = (String) gk10.parseMsg(msg);
		if ("1".equals(result)) { // 充电超时信号
			// 启动重新排队
			isInLine = false;
			showPaiDuiFailedDlg(ErrorInfo.ERROR_04);
		} else if ("2".equals(result)) { // 起爆超时信号
			isInLine = false;
			hideChongDianDlg();
			hideWaitResultDlg();
			switchState(TASK_STATE.JUDGE);
//			showPaiDuiFailedDlg(ErrorInfo.ERROR_01);
		} else if ("0".equals(result)) {
			hideChongDianDlg();
			showWaitResultDlg();
		}
	}

	/**
	 * 放炮结果
	 *
	 * @param msg
	 */
	private void handleMsgGK11(String msg) {
		pfsDataTime = System.currentTimeMillis();
		GK11 gk11 = new GK11(msg);
		// 隐藏充电界面
		hideChongDianDlg();
		hideWaitResultDlg();
		String result = (String) gk11.parseMsg(msg);
		if ("0".equals(result)) {
			result = "放炮失败";
		} else if ("1".equals(result)) {
			result = "放炮成功";
		} else if ("2".equals(result)) {
			result = "没有收到爆炸机状态";
		} else if ("3".equals(result)) {
			result = "放炮成功";
		} else if ("4".equals(result)) {
			result = "放炮成功";
		} else if ("5".equals(result)) {
			result = "放炮成功";
		} else if ("6".equals(result)) {
			result = "放炮成功";
		} else if ("7".equals(result)) {
			result = "放炮成功";
		}
		showMessage("放炮结果: " + result);
		if (result != null && !"".equals(result) && !"-1".equals(result)) {
			if (lockShotPoint != null) {
				String lastResult = lockShotPoint.result;
				String resultString = "1";
				if("0".equals(gk11.getResult())){
					resultString = "0";
				}else{
					resultString = "1";
				}
				gotoNext(lastResult,resultString);
			}
		}
	}

	public void handleMsgGK12(String msg){
		GK12 gk12 = new GK12(msg);
		String result = (String) gk12.parseMsg(msg);
		if (result.equals("0")){//更新为普通放炮模式
			SysConfig.shotSelectType = Integer.parseInt(result);
			saveShotSelectTypeConfig();
		}else if (result.equals("1")){//更新为中继放炮模式
			SysConfig.shotSelectType = Integer.parseInt(result);
			saveShotSelectTypeConfig();
		}
	}

	private void gotoNext(String lastResult,String resultString){
		if (lastResult != null && lastResult.trim().length() > 0) {
			lockShotPoint.result = lastResult + ";" + resultString;
		} else {
			lockShotPoint.result = resultString;
		}
		// 保存放炮结果数据
		mPointDBDao.updateShotPoint(lockShotPoint);
		isInLine = false;
		switchState(TASK_STATE.RESULT);
	}

	public interface CONN_STATE {
		public String CONNECTED = "已连接";
		public String UNCONNECTED = "未连接";
		public String PEIDUI = "已配对";
		public String UNPEIDUI = "未配对";
	}

	public class ConnTask extends AsyncTask<String, Integer, Boolean> {
		public static final int TYPE_NEW_CONN = 0;
		public static final int TYPE_RECONN = 1;
		public int nType = TYPE_NEW_CONN;

		public ConnTask(int type) {
			nType = type;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			boolean iscon = false;
			if (DataProcess.GetInstance().isConnected()) {
				iscon = true;
			} else {
				iscon = DataProcess.GetInstance().startConn(SysConfig.IP,
						SysConfig.PORT);
			}
			return iscon;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
			if (nType == TYPE_NEW_CONN) {
				if (!result) {
					DlgConnFailed();
				} else {
					isConn = true;
					if (curState == TASK_STATE.CONN) {
						switchState(TASK_STATE.PEI_DUI);
					}
				}
			} else if (nType == TYPE_RECONN) {
				if (!result) {
					DlgConnFailed();
				} else {
					isConn = true;
				}
			}
		}

	}

	public class KuiTanTask extends AsyncTask<String, Integer, Boolean> {

		public KuiTanTask() {
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				// 发送窥探
				if (SysConfig.isDSCloud) {
					Proto_WellShotData proto_WellShotData = Proto_WellShotData
							.newBuilder()
							.setMdata(
									ByteString.copyFrom(SocketUtils
											.writeBodyBytes(new RF01(
													String.valueOf(Integer.valueOf(SysConfig.SC_ID))).content
													.getBytes()))).build();
					Proto_Head head = Proto_Head
							.newBuilder()
							.setProtoMsgType(
									ProtoMsgType.protoMsgType_HandsetToWSC)
							.setCmdSize(proto_WellShotData.toByteArray().length)
							.addReceivers(ByteString.copyFrom(SysConfig.WSC, "GB2312"))
							.setReceivers(0, ByteString.copyFrom(SysConfig.WSC, "GB2312"))
							.setSender(
									ByteString
											.copyFrom(SysConfig.SC, "GB2312")).setMsgId(0)
							.setPriority(1).setExpired(0).build();
					if (DataProcess.isLoginDscloud) {
						try {
							return DataProcess.GetInstance().sendData(
									SocketUtils.writeBytes(head.toByteArray(),
											proto_WellShotData.toByteArray()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					paoDianHandler.sendEmptyMessageDelayed(
							MSG_FLAG.MSG_KUITAN, 5000);
				} else {
					paoDianHandler.sendEmptyMessageDelayed(
							MSG_FLAG.MSG_KUITAN, 5000);
					return DataProcess.GetInstance().sendData(
							new RF01(String.valueOf(Integer.valueOf(SysConfig.SC_ID))));
				}
			} catch (IOException e) {
				e.printStackTrace();
				LogFileUtil.saveFileToSDCard("RF01: " + e.getMessage());
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (!result) {
				isConn = DataProcess.GetInstance().isConnected();
				if (!isConn) {
					switchState(TASK_STATE.CONN);
				}
			}
		}

	}

	public class PeiDuiTask extends AsyncTask<String, Integer, Boolean> {
		public static final int TYPE_NEW_PEIDUI = 0;
		public static final int TYPE_REPEIDUI = 1;
		public int nType = TYPE_NEW_PEIDUI;

		public PeiDuiTask(int type) {
			this.nType = type;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(getActivity());
			progressDialog.setMessage("正在发送配对请求...");
			progressDialog.setCancelable(false);
			progressDialog.show();
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				// 发送配对
				if (SysConfig.isDSCloud) {
					Proto_WellShotData proto_WellShotData = Proto_WellShotData
							.newBuilder()
							.setMdata(
									ByteString.copyFrom(SocketUtils
											.writeBodyBytes(new RF02(
													String.valueOf(Integer.valueOf(String.valueOf(Integer.valueOf(SysConfig.SC_ID)))),
													SysConfig.BZJ_ID,
													SysConfig.ZZJG_ID).content
													.getBytes()))).build();
					Proto_Head head = Proto_Head
							.newBuilder()
							.setProtoMsgType(
									ProtoMsgType.protoMsgType_HandsetToWSC)
							.setCmdSize(proto_WellShotData.toByteArray().length)
							.addReceivers(ByteString.copyFrom(SysConfig.WSC, "GB2312"))
							.setReceivers(0, ByteString.copyFrom(SysConfig.WSC, "GB2312"))
							.setSender(
									ByteString
											.copyFrom(SysConfig.SC, "GB2312")).setMsgId(0)
							.setPriority(1).setExpired(0).build();
					try {
						boolean issendpei = false;
						if (DataProcess.isLoginDscloud) {
							issendpei = DataProcess.GetInstance().sendData(
									SocketUtils.writeBytes(head.toByteArray(),
											proto_WellShotData.toByteArray()));
						}
						return issendpei;
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					return DataProcess.GetInstance().sendData(
							new RF02(String.valueOf(Integer.valueOf(SysConfig.SC_ID)), SysConfig.BZJ_ID,
									SysConfig.ZZJG_ID));
				}
			} catch (IOException e) {
				e.printStackTrace();
				LogFileUtil.saveFileToSDCard("RF02: " + e.getMessage());
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (!result) {
				if (progressDialog != null) {
					progressDialog.dismiss();
				}
				isConn = DataProcess.GetInstance().isConnected();
				if (!isConn) {
					switchState(TASK_STATE.CONN);
				}
				paoDianHandler.sendEmptyMessageDelayed(
						MSG_FLAG.MSG_CHECK_IS_PEIDUI_OK, 100);
			} else {
				new Timer().schedule(new TimerTask() {

					@Override
					public void run() {
						if (progressDialog != null) {
							progressDialog.dismiss();
						}
					}
				}, 5000);
				paoDianHandler.sendEmptyMessageDelayed(
						MSG_FLAG.MSG_CHECK_IS_PEIDUI_OK, 5000);
			}
		}

	}

	public class PiPeiZhuangHaoTask extends AsyncTask<String, Integer, Boolean> {
		ProgressDialog progressDialog = null;
		List<ShotPoint> lstShotPoints = new ArrayList<ShotPoint>();
		Location location = null;

		public PiPeiZhuangHaoTask(Location location) {
			this.location = location;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(getActivity());
			progressDialog.setMessage("正在匹配桩号...");
			progressDialog.setCancelable(false);
			progressDialog.show();
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			float index = SysConfig.ShotproMax / 10;
			try {
				SparseArray<ShotPoint> sparseArray  = mPointDBDao.selectShotListNotHidden(16, GeoPoint.fromDouble(this.location.getLatitude(),
						this.location.getLongitude()), 0.001 * index , 0.001 * index);
				if (sparseArray != null && sparseArray.size() > 0) {
					for (int i = 0; i < sparseArray.size(); i++) {
						lstShotPoints.add(sparseArray.get(sparseArray.keyAt(i)));
					}
				}
			} catch (Exception e) {
				lstShotPoints = new ArrayList<ShotPoint>();
			}

			// 测试使用
			lstShotPoints.add(currShotPoint);

			return lstShotPoints != null && lstShotPoints.size() > 0;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
			if (pipeiTimer == null) {
				startPiPeiDisJudge();
			}
			if (!result) {
				if (lstShotPoints == null) {
					mainTextView.setText("桩号匹配失败");
					showMessage("桩号匹配失败");
				} else {
					showMessage("请移动至匹配范围"
							+ SysConfig.ShotproMax + "米内");
				}
			} else {
				showSelectDlg(lstShotPoints, location);
			}
		}
	}

	/***
	 * 顺序任务桩号查找
	 *
	 * @author TNT
	 *
	 */
	public class ZhuangHaoNormalQueryTask extends
			AsyncTask<String, Integer, Boolean> {
		ProgressDialog progressDialog = null;
		List<ShotPoint> lstShotPoints = null;

		public ZhuangHaoNormalQueryTask() {
		}

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(getActivity());
			progressDialog.setMessage("顺序查找下一个任务点...");
			progressDialog.setCancelable(false);
			progressDialog.show();
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			lstShotPoints = mPointDBDao.selectShotPointunDone();
			return lstShotPoints != null && lstShotPoints.size() > 0;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
			if (!result) {
				showMessage("未查找到任务点");
			} else {
				showTaskNormalSelectDlg(lstShotPoints);
			}
		}
	}

	/***
	 * 周边任务桩号查找
	 *
	 * @author TNT
	 *
	 */
	public class ZhuangHaoAroundQueryTask extends AsyncTask<String, Integer, Boolean> {
		ProgressDialog progressDialog = null;
		List<ShotPoint> lstShotPoints = new ArrayList<ShotPoint>();
		GeoPoint centerPoint2d = null;
		double[] arrDis = new double[] { 0.002, 0.005, 0.01, 0.05, 0.1,
				0.5 };
		double dis = 100;

		public ZhuangHaoAroundQueryTask(GeoPoint center, double dis) {
			this.centerPoint2d = center;
			if (dis > 0) {
				this.dis = dis;
			}
		}

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(getActivity());
			progressDialog.setMessage("正在周边查找...");
			progressDialog.setCancelable(false);
			progressDialog.show();
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			SparseArray<ShotPoint> sparseArray = new SparseArray<ShotPoint>();
			sparseArray = mPointDBDao.selectShotListNotHidden(16, centerPoint2d, 0.001, 0.001);
			if (sparseArray == null || sparseArray.size() == 0) {
				for (double newDis : arrDis) {
					sparseArray = mPointDBDao.selectShotListNotHidden(16, centerPoint2d, newDis, newDis);
					if (sparseArray != null && sparseArray.size() > 0) {
						for (int i = 0; i < sparseArray.size(); i++) {
							lstShotPoints.add(sparseArray.get(sparseArray.keyAt(i)));
						}
						break;
					}
				}
			} else {
				for (int i = 0; i < sparseArray.size(); i++) {
					lstShotPoints.add(sparseArray.get(sparseArray.keyAt(i)));
				}
			}

			return lstShotPoints != null && lstShotPoints.size() > 0;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
			if (!result) {
				showMessage("未查找到任务点");
			} else {
				showTaskAroundSelectDlg(lstShotPoints);
			}
		}
	}

	/***
	 * 桩号查找
	 *
	 * @author TNT
	 *
	 */
	public class ZhuangHaoQueryTask extends AsyncTask<String, Integer, Boolean> {
		ProgressDialog progressDialog = null;
		public String zhuanghao = "";
		public ShotPoint newTaskEntity = null;

		public ZhuangHaoQueryTask(String zhuanghao) {
			this.zhuanghao = zhuanghao;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(getActivity());
			progressDialog.setMessage("正在查找桩号...");
			progressDialog.setCancelable(false);
			progressDialog.show();
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			newTaskEntity = mPointDBDao.selectShotPoint(zhuanghao);
			return newTaskEntity != null;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
			if (!result) {
				showMessage("桩号：" + zhuanghao + "不存在，请手动选择");
				showTaskPaoDianSelectHandle();
			} else {
				toNextTaskEntity(newTaskEntity);
			}
		}
	}


	//爆炸超时处理
	public class OverTimeTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			String status = params[0];
			try {
				// 发送窥探
				if (SysConfig.isDSCloud) {
					Proto_WellShotData proto_WellShotData = Proto_WellShotData
							.newBuilder()
							.setMdata(
									ByteString.copyFrom(SocketUtils
											.writeBodyBytes(new RF12(
													String.valueOf(Integer.valueOf(SysConfig.SC_ID)),
													lockShotPoint.stationNo,status).content
													.getBytes()))).build();
					Proto_Head head = Proto_Head
							.newBuilder()
							.setProtoMsgType(
									ProtoMsgType.protoMsgType_HandsetToWSC)
							.setCmdSize(proto_WellShotData.toByteArray().length)
							.addReceivers(ByteString.copyFrom(SysConfig.WSC, "GB2312"))
							.setReceivers(0, ByteString.copyFrom(SysConfig.WSC, "GB2312"))
							.setSender(
									ByteString
											.copyFrom(SysConfig.SC, "GB2312")).setMsgId(0)
							.setPriority(1).setExpired(0).build();
					if (DataProcess.isLoginDscloud) {
						try {
							return DataProcess.GetInstance().sendData(
									SocketUtils.writeBytes(head.toByteArray(),
											proto_WellShotData.toByteArray()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				} else {
					return DataProcess.GetInstance().sendData(
							new RF12(
							String.valueOf(Integer.valueOf(SysConfig.SC_ID)),
							lockShotPoint.stationNo,status).content
							.getBytes());
				}
			} catch (IOException e) {
				e.printStackTrace();
				LogFileUtil.saveFileToSDCard("RF12: " + e.getMessage());
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (!result) {
				isConn = DataProcess.GetInstance().isConnected();
				if (!isConn) {
					switchState(TASK_STATE.CONN);
				}
			}
		}

	}


	//中继模式ready处理
	public class ReadyTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				// 发送窥探
				if (SysConfig.isDSCloud) {
					Proto_WellShotData proto_WellShotData = Proto_WellShotData
							.newBuilder()
							.setMdata(
									ByteString.copyFrom(SocketUtils
											.writeBodyBytes(new RF13(
													String.valueOf(Integer.valueOf(SysConfig.BZJ_ID))).content
													.getBytes()))).build();
					Proto_Head head = Proto_Head
							.newBuilder()
							.setProtoMsgType(
									ProtoMsgType.protoMsgType_HandsetToWSC)
							.setCmdSize(proto_WellShotData.toByteArray().length)
							.addReceivers(ByteString.copyFrom(SysConfig.WSC, "GB2312"))
							.setReceivers(0, ByteString.copyFrom(SysConfig.WSC, "GB2312"))
							.setSender(
									ByteString
											.copyFrom(SysConfig.SC, "GB2312")).setMsgId(0)
							.setPriority(1).setExpired(0).build();
					if (DataProcess.isLoginDscloud) {
						try {
							return DataProcess.GetInstance().sendData(
									SocketUtils.writeBytes(head.toByteArray(),
											proto_WellShotData.toByteArray()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				} else {
					return DataProcess.GetInstance().sendData(
							new RF13(
									String.valueOf(Integer.valueOf(SysConfig.SC_ID))).content
									.getBytes());
				}
			} catch (IOException e) {
				e.printStackTrace();
				LogFileUtil.saveFileToSDCard("RF13: " + e.getMessage());
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (!result) {
				isConn = DataProcess.GetInstance().isConnected();
				if (!isConn) {
					switchState(TASK_STATE.CONN);
				}
			}
		}

	}

	/***
	 * 距离提示更新对话框
	 */
	private MAlertDialogAdapter adapter = null;

	/****
	 * 显示锁定对话框
	 *
	 */
	private AlertDialog showDisAlertDlg(double dis, double gpsAccuracy) {
		if (adapter == null) {
			adapter = new MAlertDialogAdapter();
			adapter.showType = adapter.TYPE_NORMAL;
		}
		adapter.dis = dis;
		adapter.gpsAccuracy = gpsAccuracy;
		adapter.zhuanghao = lockShotPoint.stationNo;
		if (disAlertDlg == null) {
			disAlertDlg = new AlertDialog.Builder(getActivity())
					.setTitle("安全提示")
					.setAdapter(adapter, null)
					.setNegativeButton(R.string.cancel, null)
					.setPositiveButton("请求排队",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
													int which) {
									showPaiDui();
								}
							}).setCancelable(false).create();
		}
		if (!disAlertDlg.isShowing()) {
			disAlertDlg.show();
		}
		adapter.notifyDataSetChanged();
		disAlertDlg.getButton(Dialog.BUTTON_POSITIVE).setEnabled(
				dis >= (SysConfig.safe_Distance + gpsAccuracy));
		return disAlertDlg;
	}

	/****
	 * 显示锁定对话框
	 *
	 */
	private AlertDialog showDisAlertDlg2(double dis, double gpsAccuracy) {
		if (adapter == null) {
			adapter = new MAlertDialogAdapter();
			adapter.showType = adapter.TYPE_INLINE_CANCEL;
			adapter.optTipContent = "进入危险区域";
		}
		adapter.dis = dis;
		adapter.gpsAccuracy = gpsAccuracy;
		adapter.zhuanghao = lockShotPoint.stationNo;
		if (disAlertDlg == null) {
			disAlertDlg = new AlertDialog.Builder(getActivity())
					.setTitle("安全提示")
					.setAdapter(adapter, null)
					.setNegativeButton(R.string.cancel, null)
					.setPositiveButton("重新排队",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
													int which) {
									showPaiDui();
								}
							}).setCancelable(false).create();
		}
		if (!disAlertDlg.isShowing()) {
			disAlertDlg.show();
		}
		adapter.notifyDataSetChanged();
		disAlertDlg.getButton(Dialog.BUTTON_POSITIVE).setEnabled(
				dis >= (SysConfig.safe_Distance + gpsAccuracy));
		return disAlertDlg;
	}

	public void hideDisAlertDlg() {
		if (disAlertDlg != null && disAlertDlg.isShowing()) {
			disAlertDlg.dismiss();
			disAlertDlg = null;
			adapter = null;
		}
	}

	/****
	 * 显示所有炮点已完成对话框
	 *
	 */
	private void showTaskDoneDlg() {
		new AlertDialog.Builder(getActivity()).setTitle("任务完成")
				.setMessage("当前工程所有任务已完成").setNegativeButton(R.string.ok,

				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						handler.sendEmptyMessage(MainMSG.MSG_SHOW_TASK_STOP);
					}
				}).create().show();
	}

	private void PiPeiDisSetting() {
		View view = getActivity().getLayoutInflater().inflate(
				R.layout.dialog_set_min_pipei_dis, null);
		final EditText editText = (EditText) view.findViewById(R.id.name);
		editText.setText(SysConfig.ShotproMax + "");
		new AlertDialog.Builder(getActivity())
				.setTitle(getString(R.string.pipei_dis_setting))
				.setView(view)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
												int which) {
								String info = editText.getText().toString()
										.trim();
								if (!info.equalsIgnoreCase("")) {
									try {
										float dis = Float.parseFloat(info);
										if (dis <= 1000) {
											SysConfig.ShotproMax = dis;
											savePiPeiDisConfig();
										} else {
											showMessage("匹配距离太大");
										}
									} catch (Exception e) {
									}
								} else {
									showMessage("不可为空");
								}
							}
						}).setNegativeButton(R.string.cancel, null).create()
				.show();
	}

	/****
	 * 显示连接设置对话框
	 *
	 */
	private void showConnSettingDlg() {
		View view = getActivity().getLayoutInflater().inflate(
				R.layout.dlg_server_setting, null);
		final EditText editText_ip_1 = (EditText) view
				.findViewById(R.id.tv_ip_1);
		final EditText editText_ip_2 = (EditText) view
				.findViewById(R.id.tv_ip_2);
		final EditText editText_ip_3 = (EditText) view
				.findViewById(R.id.tv_ip_3);
		final EditText editText_ip_4 = (EditText) view
				.findViewById(R.id.tv_ip_4);
		final EditText portEditText = (EditText) view
				.findViewById(R.id.et_value2);
		view.findViewById(R.id.setting_is_dscloud).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						v.setSelected(!v.isSelected());
						SysConfig.isDSCloud = v.isSelected();
						setData(SysContants.ISCLOUD, SysConfig.isDSCloud);
					}
				});
		view.findViewById(R.id.setting_is_dscloud).setSelected(getData(SysContants.ISCLOUD, SysConfig.isDSCloud));
		String[] ip = SysConfig.IP.split("\\.");
		editText_ip_1.setText(ip[0]);
		editText_ip_2.setText(ip[1]);
		editText_ip_3.setText(ip[2]);
		editText_ip_4.setText(ip[3]);
		portEditText.setText(SysConfig.PORT + "");
		new AlertDialog.Builder(getActivity())
				.setTitle("连接设置")
				.setView(view)
				.setNegativeButton(R.string.cancel, null)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
												int which) {
								String ip_1 = editText_ip_1.getText()
										.toString().trim();
								String ip_2 = editText_ip_2.getText()
										.toString().trim();
								String ip_3 = editText_ip_3.getText()
										.toString().trim();
								String ip_4 = editText_ip_4.getText()
										.toString().trim();
								String port = portEditText.getText().toString()
										.trim();
								if (!ip_1.equalsIgnoreCase("")
										&& !ip_2.equalsIgnoreCase("")
										&& !ip_3.equalsIgnoreCase("")
										&& !ip_4.equalsIgnoreCase("")
										&& Integer.parseInt(ip_1) > 0
										&& Integer.parseInt(ip_1) < 256
										&& Integer.parseInt(ip_2) >= 0
										&& Integer.parseInt(ip_2) < 256
										&& Integer.parseInt(ip_3) >= 0
										&& Integer.parseInt(ip_3) < 256
										&& Integer.parseInt(ip_4) >= 0
										&& Integer.parseInt(ip_4) < 255
										&& port != null
										&& !"".equalsIgnoreCase(port)) {
									SysConfig.IP = new StringBuffer()
											.append(ip_1).append(".")
											.append(ip_2).append(".")
											.append(ip_3).append(".")
											.append(ip_4).toString();
									try {
										SysConfig.PORT = Integer.parseInt(port);
									} catch (Exception e) {
										SysConfig.PORT = 8899;
									}
								} else {
									showMessage("ip设置无效");
									showConnSettingDlg();
								}
								saveConnConfig();
							}
						}).create().show();
	}

	/****
	 * 显示配对设置对话框
	 *
	 */
	private void showPeiDuiSettingDlg() {
		View view = getActivity().getLayoutInflater().inflate(
				R.layout.dlg_peidui_setting, null);
		final EditText editText1 = (EditText) view.findViewById(R.id.et_value1);
		final EditText editText2 = (EditText) view.findViewById(R.id.et_value2);
		final EditText editText3 = (EditText) view.findViewById(R.id.et_value3);
		editText1.setText(SysConfig.BZJ_ID);
		editText2.setText(String.valueOf(Integer.valueOf(SysConfig.SC_ID)));
		editText3.setText(SysConfig.ZZJG_ID);
		new AlertDialog.Builder(getActivity())
				.setTitle("配对设置")
				.setView(view)
				.setNegativeButton(R.string.cancel, null)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
												int which) {
								String value1 = editText1.getText().toString()
										.trim();
								String value2 = editText2.getText().toString()
										.trim();
								String value3 = editText3.getText().toString();
								if (value1.equalsIgnoreCase("")
										|| value2.equalsIgnoreCase("")
										|| value3.equalsIgnoreCase("")) {
									showMessage("不可为空");
									showPeiDuiSettingDlg();
								} else {
									SysConfig.BZJ_ID = value1;
									SysConfig.SC_ID = value2;
									SysConfig.ZZJG_ID = value3;
								}
								savePeiDuiBZJConfig();
//								savePeiDuiNAVConfig();
								savePeiDuiZZJGConfig();
							}
						}).create().show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
//		case Constants.REQUEST.TASK_QUESTION_ACTIVITY: {
//			if (resultCode == Activity.RESULT_OK) {
//				String question = data.getStringExtra(Constants.KEY.DATA_1);
//				if (question == null) {
//					question = "";
//				}
//				lockShotPoint.setQuestion(question);
//			}
//			new AlertDialog.Builder(getActivity())
//					.setTitle("操作提示")
//					.setMessage("是否开始下一组炮点任务？")
//					.setPositiveButton(R.string.ok,
//							new DialogInterface.OnClickListener() {
//
//								@Override
//								public void onClick(DialogInterface dialog,
//										int which) {
//									// 开始进入桩号切换界面
//									nextWorkPoint();
//								}
//							}).setNegativeButton(R.string.cancel, null)
//					.create().show();
//		}
//			break;

			default:
				break;
		}
	}

	private void savePeiDuiBZJConfig() {
		setData(SysContants.BZJ, SysConfig.BZJ_ID);
	}

	private void savePeiDuiNAVConfig() {
		setData(SysContants.SC, SysConfig.SC_ID);
	}

	protected void savePeiDuiZZJGConfig() {
		setData(SysContants.ZZJG, SysConfig.ZZJG_ID);
	}

	private void saveConnConfig() {
		setData(SysContants.WIFI_IP, SysConfig.IP);
		setData(SysContants.WIFI_PORT, SysConfig.PORT);
//		HttpContact.URL = "http://" + SysConfig.IP + ":" + SysConfig.PORT + "/huobao-service/";
	}

	private void savePiPeiDisConfig() {
		setData(SysContants.SHOTPRO_MAX, SysConfig.ShotproMax);
	}

	private void saveSafeDisConfig() {
		setData(SysContants.SAFE_DISTANCE, SysConfig.safe_Distance);
	}

	private void saveReadyTimeOutConfig() {
		setData(SysContants.READY_TIMEOUT, SysConfig.Readytimeout);
	}

	private void savePowerTimeOutConfig() {
		setData(SysContants.POWER_TIMEOUT, SysConfig.PowerTimeout);
	}

	private void saveShotSelectTypeConfig(){
		setData(SysContants.SHOTSELECTTYPE,SysConfig.shotSelectType);
	}

	/****
	 * 显示放炮失败对话框
	 *
	 */
	private void showPaiDuiFailedDlg(String errorInfo) {

		mainTextView.setText("充电异常:" + errorInfo);

		hideChongDianDlg();
		hideWaitResultDlg();
		new AlertDialog.Builder(getActivity())
				.setTitle("提示")
				.setMessage(errorInfo)
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (SysConfig.isTestMode) {
							showResultWait();
						}
					}
				})
				.setPositiveButton("重新排队",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
												int which) {
								rePaidDui();
							}
						}).create().show();
	}

	/****
	 * 显示排队失败对话框
	 *
	 */
	private void showPaiDuiTimeOutDlg() {

		mainTextView.setText("排队异常:");

		new AlertDialog.Builder(getActivity())
				.setTitle("提示")
				.setMessage("排队失败：\n")
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (SysConfig.isTestMode) {
							showResultWait();
						}
					}
				})
				.setPositiveButton("重新排队",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
												int which) {
								rePaidDui();
							}
						}).create().show();
	}

	/****
	 * 显示放炮结果对话框
	 *
	 */
	private Dialog showFangPaoResultDlg(String result) {

		Dialog dialog = new AlertDialog.Builder(getActivity())
				.setTitle("放炮结果反馈").setMessage(result)
				.setPositiveButton(R.string.ok, null).create();
		dialog.show();
		return dialog;
	}

	/****
	 * 是否重新放炮
	 *
	 */
	private void showReDoTaskDialog(final ShotPoint shotpoint,
									final Location location) {

		new AlertDialog.Builder(getActivity())
				.setTitle("任务提示")
				.setMessage("当前桩号任务已完成，确定重新执行？")
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
												int which) {
								showSuoDingDlg(shotpoint, location);
							}
						}).setNegativeButton(R.string.cancel, null).create()
				.show();
	}

	private TimerDialog waitResultDialog = null;

	/***
	 * 充电提示对话框
	 */
	private void showWaitResultDlg() {
		// 如果距离提示框可见，则隐藏
		hideWaitResultDlg();

		View view = getActivity().getLayoutInflater().inflate(
				R.layout.layout_chongdian_tip, null);
		((TextView) view.findViewById(R.id.txt_info_1)).setText("已充电，等待放炮结果");
		waitResultDialog = new TimerDialog(getActivity());
		waitResultDialog.setTitle("等待放炮结果提示");
		waitResultDialog.setView(view);
		waitResultDialog.setCancelable(false);
		waitResultDialog.setPositiveButton("取消",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						hideWaitResultDlg();
					}
				}, (int) 120);
		waitResultDialog.show();
		waitResultDialog.setButtonType(Dialog.BUTTON_POSITIVE,
				(int) SysConfig.Readytimeout, false);
	}

	private void hideWaitResultDlg() {
		if (waitResultDialog != null && waitResultDialog.isShowing()
				&& !getActivity().isFinishing()) {
			waitResultDialog.dismiss();
			waitResultDialog = null;
		}
	}

	/***
	 * 充电提示对话框
	 */

	private boolean istimeOut = false;
	private void showChongDianDlg() {
		// 如果距离提示框可见，则隐藏
		hideDisAlertDlg();
		hideChongDianDlg();
		istimeOut = false;
		View view = getActivity().getLayoutInflater().inflate(
				R.layout.layout_chongdian_tip, null);
		chongDianDlg = new TimerDialog(getActivity());
		chongDianDlg.setTitle("提示");
		chongDianDlg.setView(view);
		chongDianDlg.setCancelable(false);
		chongDianDlg.setPositiveButton("重新排队",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 重新排队
						istimeOut = true;
						rePaidDui();
					}
				}, (int) SysConfig.Readytimeout);
		chongDianDlg.show();
		chongDianDlg.setButtonType(Dialog.BUTTON_POSITIVE,
				(int) SysConfig.Readytimeout, false);
		stopSafeDisJudge();
		showReadyDlg();
	}

	private AlertDialog readyDialog;
	private AlertDialog readySureDoalog;
	public void showReadyDlg(){
		if (getData(SysContants.SHOTSELECTTYPE,SysConfig.shotSelectType)==1){
			readyDialog = new AlertDialog.Builder(getActivity())
					.setTitle("提示")
					.setMessage("请确认您是否正在充电？")
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
													int which) {
									showReadySureDlg();//再次确认是否充电
								}
							}).setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
											int which) {
							showCancelPaiduiDlg();//不充电，结束排队请求
						}
					}).create();
			readyDialog.show();
		}

	}

	private AlertDialog cancelPaiduiDialog;
	private void showCancelPaiduiDlg() {
		cancelPaiduiDialog = new AlertDialog.Builder(getActivity())
				.setTitle("提示")
				.setMessage("结束排队？")
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
												int which) {
								cancelPaidDui();//结束排队
							}
						}).setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
										int which) {
						showReadyDlg();//确认是否充电
					}
				}).create();
		cancelPaiduiDialog.show();
	}

	private void showReadySureDlg() {
		readySureDoalog = new AlertDialog.Builder(getActivity())
				.setTitle("提示（风险）")
				.setMessage("请再次确认您是否正在充电？")
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
												int which) {
								sendReady();//发送ready信号准备
							}
						}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
										int which) {
						showCancelPaiduiDlg();//结束排队
					}
				}).create();
		readySureDoalog.show();
	}

	private void sendReady() {
		new ReadyTask().execute("");
	}

	private void hideChongDianDlg() {
		if (chongDianDlg != null && chongDianDlg.isShowing()
				&& !getActivity().isFinishing()) {
			chongDianDlg.dismiss();
			chongDianDlg = null;
		}
	}

	/***
	 * 重新排队
	 */
	private void rePaidDui() {
		hideChongDianDlg();
		cancelPaiDuiOpt = 0;

		if (istimeOut) {
			showPaiDui();
			istimeOut = false;
		} else {
			if (!isInLine) {
				showPaiDui();
			}
		}
	}

	/***
	 * 取消排队
	 */
	private void cancelPaidDui() {
		// 发送取消排队消息--无此消息
		cancelPaiDuiOpt = 0;
		cancelPaiDui(CACEL_OPT_2_STOP);
		// 停止距离判断
		stopSafeDisJudge();
		// 回到坐标匹配界面
		switchState(TASK_STATE.PIPEI);
	}

	/***
	 * 常规任务点顺序选择
	 */
	private void showTaskNormalSelectDlg(List<ShotPoint> shotPoints) {
		View view = getActivity().getLayoutInflater().inflate(
				R.layout.layout_task_paodian_select_normal, null);
		ListView listView = (ListView) view.findViewById(R.id.listView1);
		listView.setVisibility(View.GONE);
		final ZhuangHaoNormalSelectAdapter adapter = new ZhuangHaoNormalSelectAdapter(
				getActivity(), shotPoints);
		new AlertDialog.Builder(getActivity())
				.setTitle("请选择下一个任务桩号")
				// .setView(view)
				.setSingleChoiceItems(adapter, -1,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
												int which) {
								adapter.setSelectedItem(which);
								adapter.notifyDataSetChanged();
								adapter.notifyDataSetInvalidated();
							}
						})
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
												int which) {
								switchState(TASK_STATE.NONE);
								ShotPoint shotPoint = (ShotPoint) adapter.getItem(adapter.getSelectedItem());
								String stationNo = shotPoint.stationNo;
								Intent intent = new Intent(ACTION_POINT_LOC);
								intent.putExtra("stationNo",stationNo);
								getActivity().sendBroadcast(intent);
								switchState(TASK_STATE.NONE);
							}
						}).setNegativeButton(R.string.cancel, null).create()
				.show();
	}

	/***
	 * 常规任务点周边查询
	 */
	private void showTaskAroundSelectDlg(final List<ShotPoint> shotPoints) {
		final ZhuangHaoAroundSelectAdapter adapter = new ZhuangHaoAroundSelectAdapter(
				getActivity(), shotPoints, MainActivity.unRectifyLocation);

		// 设置默认选择
		int defIndex = 0;
		adapter.setSelectedItem(defIndex);

		new AlertDialog.Builder(getActivity())
				.setTitle("周边任务桩号选择")
				// .setView(view)
				.setSingleChoiceItems(adapter, defIndex,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
												int which) {
								// TODO Auto-generated method stub
								adapter.setSelectedItem(which);
								adapter.notifyDataSetChanged();
								adapter.notifyDataSetInvalidated();
							}
						})
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
												int which) {
								ShotPoint shotPoint = shotPoints.get(adapter
										.getSelectedItem());
								String stationNo = shotPoint.stationNo;
								Intent intent = new Intent(ACTION_POINT_LOC);
								intent.putExtra("stationNo",stationNo);
								getActivity().sendBroadcast(intent);
								switchState(TASK_STATE.NONE);
							}
						}).setNegativeButton(R.string.cancel, null).create()
				.show();
	}

	private void showPaiDuiInfoDlg() {
		View view = getActivity().getLayoutInflater().inflate(
				R.layout.layout_task_paodian_step_paidui_line, null);
		ListView listView = (ListView) view.findViewById(R.id.listView1);
		final PaiDuiLineAdapter adapter = new PaiDuiLineAdapter(getActivity(),
				lockShotPoint.stationNo, lineList);
		listView.setAdapter(adapter);
		new AlertDialog.Builder(getActivity())
				.setTitle("排队详情")
				.setView(view)
				.setPositiveButton("结束排队",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
												int which) {
								cancelPaidDui();
							}
						}).setNegativeButton("返回", null).create().show();
	}

	/****
	 * 显示锁定对话框
	 *
	 * @param taskEntity
	 * @param location
	 */
	private void showSuoDingDlg(final ShotPoint shotPoint, final Location location) {
		cancelPaiDuiOpt = 0;
		lockShotPoint = shotPoint;
		lockLocation = location;
		RF04.HisGNGGA = MainActivity.GNGGA;
		RF04.HisGPGGA = MainActivity.GPGGA;
		switchState(TASK_STATE.SUODING);
		Message msg = new Message();
		msg.obj = lockShotPoint;
		msg.what = MainMSG.MSG_UPDATA_TOP_TASK_DATA;
		getHandler().sendMessage(msg);
	}

	/***
	 * 获取点击增减按钮后的值
	 *
	 * @param oldNumber
	 * @param isUp
	 * @return
	 */
	public String getNewNumber(String oldNumber, int offValue, boolean isUp) {
		if (oldNumber != null && oldNumber.length() > 0) {
			try {
				float oldLine = Float.parseFloat(oldNumber);
				float newLine = oldLine;
				if (isUp) {
					newLine += offValue;
				} else {
					newLine -= offValue;
				}
				if (oldNumber.contains(".")) {
					return newLine + "";
				} else {
					return ((int) newLine) + "";
				}
			} catch (Exception e) {
				return oldNumber;
			}
		}
		return oldNumber;
	}

	/***
	 * 根据规则获取下一个桩号的值
	 *
	 * @param lineNumber
	 * @return
	 */
	public String getNextRuleLineNumber(String lineNumber) {
		if (lineNumber != null && lineNumber.length() > 0) {
			try {
				float oldLine = Float.parseFloat(lineNumber);
				float newLine = SysConfig.getLineNumber(oldLine);
				if (lineNumber.contains(".")) {
					return newLine + "";
				} else {
					return ((int) newLine) + "";
				}
			} catch (Exception e) {
				return lineNumber;
			}
		}
		return lineNumber;
	}

	/***
	 * 根据规则获取下一个点号的值
	 *
	 * @param pointNumber
	 * @return
	 */
	public String getNextRulePointNumber(String pointNumber) {
		if (pointNumber != null && pointNumber.length() > 0) {
			try {
				float oldPoint = Float.parseFloat(pointNumber);
				float newPoint = SysConfig.getPointNumber(oldPoint);
				if (pointNumber.contains(".")) {
					return newPoint + "";
				} else {
					return ((int) newPoint) + "";
				}
			} catch (Exception e) {
				return pointNumber;
			}
		}
		return pointNumber;
	}

	/****
	 * 显示锁定对话框
	 *
	 * @param taskEntity
	 * @param location
	 */
	String ACTION_POINT_LOC = "action_point_loc";
	private void showTaskPaoDianSelect() {

		String newLine = getNextRuleLineNumber(lockShotPoint.lineNo);
		String newPoint = getNextRulePointNumber(lockShotPoint.spointNo);

		new ZhuangHaoQueryTask(newLine + newPoint).execute();

	}

	private void toNextTaskEntity(ShotPoint newShotPoint) {
		String stationNo = newShotPoint.stationNo;
		Intent intentx = new Intent(ACTION_POINT_LOC);
		intentx.putExtra("stationNo",stationNo);
		getActivity().sendBroadcast(intentx);
		/*Message msg = new Message();
		switchState(TASK_STATE.NONE);
		TaskPoint taskPoint = new TaskPoint();
		taskPoint.Id = newShotPoint.Id;
		taskPoint.stationNo = newShotPoint.stationNo;
		taskPoint.lineNo = newShotPoint.lineNo;
		taskPoint.spointNo = newShotPoint.spointNo;
		taskPoint.geoPoint = newShotPoint.geoPoint;
		taskPoint.Alt = newShotPoint.Alt;
		taskPoint.isDone = newShotPoint.isDone;
		msg.what = RequestCode.TASK_GUIDE;
		msg.obj = taskPoint;
		Bundle bundle = new Bundle();
		msg.setData(bundle);
		handler.sendMessage(msg);*/
	}

	/****
	 * 显示大小号选择对话框
	 *
	 * @param taskEntity
	 * @param location
	 */
	private void showTaskPaoDianSelectHandle() {
		View view = getActivity().getLayoutInflater().inflate(
				R.layout.dialog_selecte_task_point, null);
		String newLine = getNextRuleLineNumber(lockShotPoint.lineNo);
		String newPoint = getNextRulePointNumber(lockShotPoint.spointNo);
		final EditText lineEditText = (EditText) view
				.findViewById(R.id.et_line);
		final EditText pointEditText = (EditText) view
				.findViewById(R.id.et_point);
		lineEditText.setText(newLine);
		pointEditText.setText(newPoint);
		view.findViewById(R.id.btn_line_up).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						lineEditText.setText(getNewNumber(lineEditText
										.getText().toString(),
								SysConfig.SelectRuleLine, true));
					}
				});
		view.findViewById(R.id.btn_line_down).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						lineEditText.setText(getNewNumber(lineEditText
										.getText().toString(),
								SysConfig.SelectRuleLine, false));
					}
				});
		view.findViewById(R.id.btn_point_up).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						pointEditText.setText(getNewNumber(pointEditText
										.getText().toString(),
								SysConfig.SelectRulePoint, true));

					}
				});
		view.findViewById(R.id.btn_point_down).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						pointEditText.setText(getNewNumber(pointEditText
										.getText().toString(),
								SysConfig.SelectRulePoint, false));

					}
				});
		new AlertDialog.Builder(getActivity()).setTitle("大小号选择").setView(view)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						new ZhuangHaoQueryTask(lineEditText.getText().toString().trim()
								+ pointEditText.getText().toString().trim()).execute();
					}
				}).setNegativeButton(R.string.cancel, null).create().show();
	}

	/***
	 * 坐标匹配结果选择
	 *
	 * @param taskEntities
	 * @param location
	 */
	private void showSelectDlg(final List<ShotPoint> shotPoints, final Location location) {

		if (currShotPoint != null && currShotPoint.stationNo != null) {
			boolean isHas = false;
			int index = -1;
			for (int i = 0; i < shotPoints.size(); i++) {
				if (currShotPoint.stationNo.equals(shotPoints.get(i).stationNo)) {
					targetShotPoint = shotPoints.get(i);
					isHas = true;
					index = i;
				}
			}
			if (isHas) {
				shotPoints.remove(index);
				isHas = false;
				index = -1;
			} else {
				showMessage("匹配失败");
				return ;
			}
			shotPoints.clear();
			shotPoints.add(0, targetShotPoint);
		}

		final ZhuangHaoAroundSelectAdapter adapter = new ZhuangHaoAroundSelectAdapter(getActivity(), shotPoints, location);

		// 设置默认选择
		int defIndex = 0;
		adapter.setSelectedItem(defIndex);

		showSuoDingDlg(shotPoints.get(0), location);
	}

	private void DlgConnFailed() {
		new AlertDialog.Builder(getActivity())
				.setTitle("连接失败")
				.setMessage(
						"当前IP：" + SysConfig.IP + "\n" + "当前端口:"
								+ SysConfig.PORT + "\n请确认:\n"
								+ "	1.是否已连接上相应的WIFI" + "\n"
								+ "	2.相应地址IP和端口是否一致")
				.setPositiveButton("确定", null).create().show();
	}

	private void showBZJSelected() {
		final String[] arrBZJ = getResources().getStringArray(R.array.bzj_list);
		new AlertDialog.Builder(getActivity())
				.setTitle("请选择爆炸机编号")
				.setNeutralButton("自定义", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						showPeiDuiSettingDlg();
					}
				})
				.setSingleChoiceItems(arrBZJ, -1,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
												int which) {
								dialog.dismiss();
								SysConfig.BZJ_ID = arrBZJ[which];
								savePeiDuiBZJConfig();
								// 选择导航设备编号
//								showNAVSelected();
								showPeiDuiZZJGConfig();
							}
						}).create().show();
	}

	private void showNAVSelected() {
		final String[] arrNAV = getResources().getStringArray(R.array.nav_list);
		new AlertDialog.Builder(getActivity())
				.setTitle("请选手持机编号")
				.setSingleChoiceItems(arrNAV, -1,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
												int which) {
								dialog.dismiss();
								SysConfig.SC_ID = arrNAV[which];

								if (!"".equals(SysConfig.SC_ID)) {
									if (Integer.valueOf(SysConfig.SC_ID) < 10) {
//										if (SysConfig.workType == WorkType.WORK_TYPE_SHOT) {
										SysConfig.SC = new StringBuffer().append(SysConfig.HANDSET).append("0").append(SysConfig.SC_ID).toString();
//										} else if (SysConfig.workType == WorkType.WORK_TYPE_DRILE) {
//											SysConfig.SC = new StringBuffer().append(SysConfig.DRILLSET).append("0").append(SysConfig.SC_ID).toString();
//										}
									} else {
//										if (SysConfig.workType == WorkType.WORK_TYPE_SHOT) {
										SysConfig.SC = new StringBuffer().append(SysConfig.HANDSET).append(SysConfig.SC_ID).toString();
//										} else if (SysConfig.workType == WorkType.WORK_TYPE_DRILE) {
//											SysConfig.SC = new StringBuffer().append(SysConfig.DRILLSET).append(SysConfig.SC_ID).toString();
//										}
									}
								}

								savePeiDuiNAVConfig();
								showPeiDuiZZJGConfig();
							}
						}).create().show();
	}

	protected void showPeiDuiZZJGConfig() {
		final String[] arrZZJG = getResources().getStringArray(
				R.array.zzjg_list);
		new AlertDialog.Builder(getActivity())
				.setTitle("请选组织机构编号")
				.setSingleChoiceItems(arrZZJG, -1,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
												int which) {
								dialog.dismiss();
								SysConfig.ZZJG_ID = arrZZJG[which];
								savePeiDuiZZJGConfig();

								new cancelPeiduiTask().execute("");
							}
						}).create().show();
	}

	private void DlgPeiDuiFailed() {
		new AlertDialog.Builder(getActivity())
				.setTitle("配对失败")
				.setMessage(
						"当前爆炸机编号：" + SysConfig.BZJ_ID + "\n" + "当前手持机编号："
								+ String.valueOf(Integer.valueOf(SysConfig.SC_ID)) + "\n" + "当前组织机构编号："
								+ SysConfig.ZZJG_ID + "\n"
								+ "请确认:\n	1.编号是否正确\n	2.是否已连接中控机")
				.setPositiveButton("确定", null).create().show();
	}

	public class cancelPeiduiTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				if (SysConfig.isDSCloud) {
					Proto_WellShotData proto_WellShotData = Proto_WellShotData
							.newBuilder()
							.setMdata(
									ByteString.copyFrom(SocketUtils
											.writeBodyBytes(new RF03(
													String.valueOf(Integer.valueOf(SysConfig.SC_ID)),
													SysConfig.BZJ_ID,
													SysConfig.ZZJG_ID).content
													.getBytes()))).build();
					Proto_Head head = Proto_Head
							.newBuilder()
							.setProtoMsgType(ProtoMsgType.protoMsgType_HandsetToWSC)
							.setCmdSize(proto_WellShotData.toByteArray().length)
							.addReceivers(ByteString.copyFrom(SysConfig.WSC, "GB2312"))
							.setReceivers(0, ByteString.copyFrom(SysConfig.WSC, "GB2312"))
							.setSender(
									ByteString.copyFrom(SysConfig.SC, "GB2312"))
							.setMsgId(0).setPriority(1).setExpired(0).build();
					try {
						if (DataProcess.isLoginDscloud) {
							DataProcess.GetInstance().sendData(
									SocketUtils.writeBytes(head.toByteArray(),
											proto_WellShotData.toByteArray()));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					DataProcess.GetInstance().sendData(
							new RF03(String.valueOf(Integer.valueOf(SysConfig.SC_ID)), SysConfig.BZJ_ID,
									SysConfig.ZZJG_ID));
				}
				isConn = false;
			} catch (IOException e) {
				e.printStackTrace();
				LogFileUtil.saveFileToSDCard("RF03: " + e.getMessage());
			}
			return null;
		}

	}

	/****
	 * 报警数据适配器
	 *
	 * @author TNT
	 *
	 */
	public class MAlertDialogAdapter extends BaseAdapter {

		public final int TYPE_NORMAL = 0;
		public final int TYPE_INLINE_CANCEL = 1;

		public double dis = 0;
		public double gpsAccuracy = 0;
		public String zhuanghao = "";
		public int showType = TYPE_NORMAL;

		public String optTipContent = "";

		public MAlertDialogAdapter() {

		}

		@Override
		public int getCount() {
			return 1;
		}

		@Override
		public Object getItem(int position) {
			return 1;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.layout_task_paodian_step_02_01, null);
			}
			((TextView) convertView.findViewById(R.id.txt_info_0))
					.setText(zhuanghao + "");
			if (dis >= SysConfig.safe_Distance + gpsAccuracy) {
				((TextView) convertView.findViewById(R.id.txt_main_tip))
						.setText("您已进入安全区域!");
				((TextView) convertView.findViewById(R.id.txt_main_tip))
						.setTextColor(0x7f070050);// R.color.forest_green
				((TextView) convertView.findViewById(R.id.txt_opt_tip))
						.setText("可以开始排队");

				((TextView) convertView.findViewById(R.id.txt_info_1))
						.setText(SysConfig.safe_Distance + "米");
				if ((dis - gpsAccuracy) > 0) {
//					((TextView) convertView.findViewById(R.id.txt_info_2))
//							.setText(MathUtils.GetAccurateNumberText(dis
//									- gpsAccuracy, 1)
//									+ "米");
				} else {
					((TextView) convertView.findViewById(R.id.txt_info_2))
							.setText(0 + "米");
				}
				// ((TextView) convertView.findViewById(R.id.txt_info_3))
				// .setText(MathUtils
				// .GetAccurateNumberText(gpsAccuracy, 1) + "米");
			} else {
				((TextView) convertView.findViewById(R.id.txt_main_tip))
						.setTextColor(Color.RED);
				if (showType == TYPE_INLINE_CANCEL) {
					((TextView) convertView.findViewById(R.id.txt_main_tip))
							.setText(optTipContent);
				} else {
					((TextView) convertView.findViewById(R.id.txt_main_tip))
							.setText("危险!");
				}
				((TextView) convertView.findViewById(R.id.txt_opt_tip))
						.setText("请移动至安全区域");

				((TextView) convertView.findViewById(R.id.txt_info_1))
						.setText(SysConfig.safe_Distance + "米");
				if ((dis - gpsAccuracy) > 0) {
//					((TextView) convertView.findViewById(R.id.txt_info_2))
//							.setText(MathUtils.GetAccurateNumberText(dis
//									- gpsAccuracy, 1)
//									+ "米");
				} else {
					((TextView) convertView.findViewById(R.id.txt_info_2))
							.setText(0 + "米");
				}
			}
			return convertView;
		}
	}

}
