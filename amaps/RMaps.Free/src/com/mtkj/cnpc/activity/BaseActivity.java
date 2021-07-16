package com.mtkj.cnpc.activity;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.List;

import org.andnav.osm.util.GeoPoint;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import com.mtkj.cnpc.activity.service.SendService;
import com.mtkj.cnpc.protocol.bean.ArrangePoint;
import com.mtkj.cnpc.protocol.bean.CarTrave;
import com.mtkj.cnpc.protocol.bean.DrillPoint;
import com.mtkj.cnpc.protocol.bean.ShotPoint;
import com.mtkj.cnpc.protocol.constants.DSSProtoDataConstants;
import com.mtkj.cnpc.protocol.constants.DSSProtoDataConstants.DeviceType;
import com.mtkj.cnpc.protocol.constants.DSSProtoDataConstants.ProtoMsgType;
import com.mtkj.cnpc.protocol.constants.ProtocolConstants;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.constants.SysConfig.WorkType;
import com.mtkj.cnpc.protocol.constants.SysContants;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_DpData;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_DpData.MsgSpsData;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_Head;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_Login_Request;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_SpData;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_SpData.MsgSPData;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_TaskData;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_TravelImforResponce;
import com.mtkj.cnpc.protocol.shot.GK08;
import com.mtkj.cnpc.protocol.shot.GK12;
import com.mtkj.cnpc.protocol.socket.DataProcess;
import com.mtkj.cnpc.protocol.socket.DataProcess.MSG;
import com.mtkj.cnpc.protocol.utils.SocketUtils;
import com.mtkj.cnpc.sqlite.PointDBDao;
import com.mtkj.cnpc.sqlite.constants.DBConstants;
import com.mtkj.utils.entity.ContactPersons;
import com.mtkj.utils.entity.FileUtils;
import com.mtkj.utils.entity.PointDemo;
import com.mtkj.utils.entity.ShareEntity;
import com.mtkj.utils.entity.TalkEntity;
import com.mtkj.utils.entity.TaskEntity;
import com.mtkj.cnpc.R;
import com.robert.maps.applib.utils.LogFileUtil;
import com.robert.maps.applib.utils.TimeUtil;

@SuppressLint("HandlerLeak")
public class BaseActivity extends FragmentActivity {

	private SharedPreferences mPreferences;

	protected PointDBDao mPointDBDao;
	protected int current = -1;
	protected int count = -1;
	private MediaPlayer mMediaPlayer;
	private InputMethodManager mInputMethodManager;
	String json;
	PointDemo pointDemo;
	TaskEntity taskEntity;
	TalkEntity talkEntity;
	ShareEntity shareEntity;
	ShotPoint shotPoint;
	com.mtkj.utils.entity.ShotPoint pointEntity;
	DrillPoint drillPoint;
	ArrangePoint arrangePoint;
	NotificationManager notificationManager;

	// 重启处理
	private UncaughtExceptionHandler m_handler = new UncaughtExceptionHandler() {
		@Override
		public void uncaughtException(Thread thread, Throwable ex) {
			// 异常信息存储
			LogFileUtil.saveFileToSDCard(FormatStackTrace(ex));
			System.exit(0);
		}
	};

	public String FormatStackTrace(Throwable throwable) {
		if (throwable == null)
			return "";
		String rtn = throwable.getStackTrace().toString();
		try {
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			throwable.printStackTrace(printWriter);
			printWriter.flush();
			writer.flush();
			rtn = writer.toString();
			rtn = rtn.replaceAll("at ", "\r\n" + "at ");
			rtn = rtn.replaceAll("Caused by", "\r\n" + "Caused by");
			printWriter.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception ex) {
		}
		return rtn;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		Thread.setDefaultUncaughtExceptionHandler(m_handler);

		mInputMethodManager = (InputMethodManager) (this.getSystemService(Context.INPUT_METHOD_SERVICE));
		mPointDBDao = new PointDBDao(this);
		mPreferences = getSharedPreferences(SysContants.SYSCONFIG, MODE_PRIVATE);
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public void hideSoftInput(View v) {
		mInputMethodManager.hideSoftInputFromWindow(
				v.getApplicationWindowToken(), 0);
	}

	@Override
	protected void onDestroy() {
		if(mMediaPlayer!=null){
			mMediaPlayer.release();
		}
//		stopService(new Intent(BaseActivity.this,SendService.class));
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		DataProcess.GetInstance().setBaseHandler(handler);
//		Intent intent = new Intent(BaseActivity.this, SendService.class);
//		startService(intent);
		HashMap<String, String> packaget = mPointDBDao.selectPackagetNum(String.valueOf(SysConfig.workType));
		if (packaget.get(DBConstants.CURRENT) != null && !"".equals(packaget.get(DBConstants.CURRENT))) {
			current = Integer.valueOf(packaget.get(DBConstants.CURRENT));
		}
		if (packaget.get(DBConstants.COUNT) != null && !"".equals(packaget.get(DBConstants.COUNT))) {
			count = Integer.valueOf(packaget.get(DBConstants.COUNT));
		}
	}

	private Handler handler = new Handler(){
		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {

				case MSG.DRILL_PACKAGETNUM:
				/*int DPpackagetCount = (int) msg.obj;
				mPointDBDao.insertPackagetNum(String.valueOf(WorkType.WORK_TYPE_DRILE), "0", String.valueOf(DPpackagetCount), TimeUtil.getCurrentTimeInString());
				
				DataProcess.GetInstance().sendDpDataQuest(0);*/
					final int DPpackagetCount = (int) msg.obj;
					Bundle bundleD = msg.getData();
					final boolean isReplaceD = bundleD.getBoolean("isReplace");
					json = FileUtils.readLocalJson(BaseActivity.this,"zuan1.txt");
					new Thread(){
						public void run() {
							pointDemo = JSON.parseObject(json, PointDemo.class);//fastJson解析
							Message msg = new Message();
							msg.what = -11;
							msg.obj = isReplaceD;
							handler.sendMessage(msg);
						};
					}.start();
					break;

				case DataProcess.MSG.DRILL:
					Proto_DpData dpData = (Proto_DpData) msg.obj;
					if (dpData.getReplace()) {
						mPointDBDao.deleteAllDrillPoint();
					}
					for (MsgSpsData msgSpsData : dpData.getSpsDataList()) {
						DrillPoint drillPoint = new DrillPoint(DrillPoint.EMPTY_ID, msgSpsData.getLineNo().toStringUtf8() + msgSpsData.getSpointNo().toStringUtf8(), msgSpsData.getLineNo().toStringUtf8(),
								msgSpsData.getSpointNo().toStringUtf8(), GeoPoint.fromDouble(msgSpsData.getLat(), msgSpsData.getLon()), msgSpsData.getHeight(), msgSpsData.getWellnum(),
								msgSpsData.getDesWellDepth(), msgSpsData.getBombWeight(), msgSpsData.getDetonator(), 0, 0);
						mPointDBDao.insertDrillPoint(drillPoint);
					}

					int DPpacketNum = dpData.getPacketNum();
					DataProcess.GetInstance().sendDPDataResponse(DPpacketNum);
					mPointDBDao.updatePackagerNum(String.valueOf(WorkType.WORK_TYPE_DRILE), String.valueOf(DPpacketNum), String.valueOf(count), TimeUtil.getCurrentTimeInString());

					if (DPpacketNum + 1 <= count) {
						DataProcess.GetInstance().sendDpDataQuest(DPpacketNum + 1);
					}
					break;

				case MSG.SHOT_PACKAGETNUM:
				/*final int SPSpackagetCount = (int) msg.obj;
				mPointDBDao.insertPackagetNum(String.valueOf(WorkType.WORK_TYPE_SHOT), "0", String.valueOf(SPSpackagetCount), TimeUtil.getCurrentTimeInString());
				DataProcess.GetInstance().sendSPDataQuest(0);*/
					final int SPSpackagetCount = (int) msg.obj;
					Bundle bundleS = msg.getData();
					final boolean isReplaceS = bundleS.getBoolean("isReplace");
					json = FileUtils.readLocalJson(BaseActivity.this,"pao1.txt");
					new Thread(){
						public void run() {
							pointDemo = JSON.parseObject(json, PointDemo.class);//fastJson解析
							Message msg = new Message();
							msg.what = -10;
							msg.obj = isReplaceS;
							handler.sendMessage(msg);
						};
					}.start();
					break;

				case DataProcess.MSG.SHOT:
					Log.e("onSPData02","onSPData02");
					Proto_SpData spData = (Proto_SpData) msg.obj;
					if (spData.getReplace()) {
						mPointDBDao.deleteAllShot();
					}
					for (MsgSPData msgSPData : spData.getSpDataList()) {
						ShotPoint shotPoint = new ShotPoint(ShotPoint.EMPTY_ID, msgSPData.getLineNo().toStringUtf8() + msgSPData.getSpointNo().toStringUtf8(), msgSPData.getLineNo().toStringUtf8(),
								msgSPData.getSpointNo().toStringUtf8(), GeoPoint.fromDouble(msgSPData.getLat(), msgSPData.getLon()), msgSPData.getHeight(), 0, 0);
						mPointDBDao.insertShotPoint(shotPoint);
					}

					int SPpacketNum = spData.getPacketNum();
					DataProcess.GetInstance().sendSPDataResponse(SPpacketNum);
					mPointDBDao.updatePackagerNum(String.valueOf(WorkType.WORK_TYPE_SHOT), String.valueOf(SPpacketNum), String.valueOf(count), TimeUtil.getCurrentTimeInString());

					if (SPpacketNum + 1 <= count) {
						DataProcess.GetInstance().sendSPDataQuest(SPpacketNum + 1);
					}
					break;
				case DataProcess.MSG.TASKDATA:
					final Proto_TaskData taskData = (Proto_TaskData) msg.obj;
					if (taskData != null) {
						mPointDBDao.insertDailyTask(taskData.getPName(), taskData.getPTime(), 0, String.valueOf(SysConfig.workType));
					}
					String pMsg = taskData.getPMsg();
					try {
						String[] stationNos = pMsg.split(";");
						for (int i = 0; i < stationNos.length; i++) {
							String stationNo = stationNos[i];
							String lineNo = stationNo.split(":")[0];
							String[] spointNos = stationNo.split(":")[1].split(",");
							for (int j = 0; j < spointNos.length; j++) {
								mPointDBDao.insertDailyPoint(taskData.getPTime(), lineNo + spointNos[j], String.valueOf(SysConfig.workType));
							}
						}
					} catch (Exception e) {
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
					}
					break;

				case MSG.TASK_RECEIVE_OFFLINE:
					DSSProtoDataJava.Proto_DSG_TaskAssignment_ControledVibrator assignment = (DSSProtoDataJava.Proto_DSG_TaskAssignment_ControledVibrator) msg.obj;
					List<DSSProtoDataJava.Proto_DSG_TaskAssignment_ControledVibrator.TaskAssignmentControledVibratorPoints> points = assignment.getPointsList();
					Log.e("ControledVibratorPoints", "size: "+points.size());
					Log.e("count", "count: "+assignment.getCount());
					break;

				case MSG.WEB_TASK_NOTICE:
					DSSProtoDataJava.Proto_WebTaskNotice notice = (DSSProtoDataJava.Proto_WebTaskNotice) msg.obj;
					String no = notice.getMsgType().toStringUtf8();
					Intent intentw = new Intent("action_web_notice");
					sendBroadcast(intentw);
					sendNotification("您有新任务","您有新任务,请点击查看",4);
					break;

				case DataProcess.MSG.NOTICE:
					DSSProtoDataJava.Proto_Notice proto_notice = (DSSProtoDataJava.Proto_Notice) msg.obj;
					Log.e("getMsg",proto_notice.getMsg().toStringUtf8());
					final String json = proto_notice.getMsg().toStringUtf8();
					JSONObject jsonObject = JSONObject.parseObject(json);
					// 获取msgtype的值
					int msgtype = jsonObject.getInteger("msgtype");
					if (msgtype==1){//任务下发
						Intent intent = new Intent("action_get_message");
						sendBroadcast(intent);
						sendNotification("新任务消息","您有新消息，请点击查看",0);
						new Thread(){
							public void run() {
								taskEntity = JSON.parseObject(json, TaskEntity.class);//fastJson解析
								mPointDBDao.insertTask(taskEntity);
							};
						}.start();
					}else if (msgtype==2){
						//数据下发
						int type = jsonObject.getInteger("type");
						int op = jsonObject.getInteger("op");//1:覆盖 2：追加
						int begin = jsonObject.getInteger("begin");
						switch (type){
							case 1:break;//
							case 2://井炮（炸药震源） op 1覆盖0追加
								if (op==1 && begin==0){
									mPointDBDao.deleteAllDrillPoint();
									mPointDBDao.deleteAllDrillRecord();
									mPointDBDao.deleteAllShot();
									mPointDBDao.deleteAllTrave();
									mPointDBDao.deleteBulldozPoint();
									mPointDBDao.deleteBulldozTask();
									mPointDBDao.deleteCarkeys();
									mPointDBDao.deleteCarNums();
									mPointDBDao.deleteHistory();
									mPointDBDao.deleteAllArrange();
								}
								if (begin==0){
									sendNotification("数据导入","您有新的炮点数据导入",1);
								}
								Toast.makeText(BaseActivity.this,"正在更新炮点数据",Toast.LENGTH_SHORT).show();
								new MyInsertShotPointAsync().execute(json);
								break;
							case 3://钻井下药
								if (op==1 && begin==0){//op 1覆盖0追加
									mPointDBDao.deleteAllDrillPoint();
									mPointDBDao.deleteAllDrillRecord();
									mPointDBDao.deleteAllShot();
									mPointDBDao.deleteAllTrave();
									mPointDBDao.deleteBulldozPoint();
									mPointDBDao.deleteBulldozTask();
									mPointDBDao.deleteCarkeys();
									mPointDBDao.deleteCarNums();
									mPointDBDao.deleteHistory();
									mPointDBDao.deleteAllArrange();
								}
								if (begin==0){
									sendNotification("数据导入","您有新的钻井下药数据导入",1);
								}
								Toast.makeText(BaseActivity.this,"正在更新钻井下药数据",Toast.LENGTH_SHORT).show();
								new MyInsertDrillPointAsync().execute(json);
								break;
							case 4:
								if (op==1 && begin==0){//op 1覆盖0追加
									mPointDBDao.deleteAllArrange();
								}
								if (begin==0){
									sendNotification("新任务","您有新任务请查看",1);
								}
								new MyInsertArrangePointAsync().execute(json);
								break;//
						}
					}else if (msgtype==3){
						//聊天
						new Thread(){
							public void run() {
								talkEntity = JSON.parseObject(json, TalkEntity.class);//fastJson解析
								talkEntity.setType_who(1);
								mPointDBDao.insertTalk(talkEntity);//存储数据库
								Message message = new Message();
								if (talkEntity.getDevice().equals("szdzd")){
									message.what = MSG.TASK_NEWS_ALL;
								}else {
									message.what = MSG.TALK_NEWS;
								}
								handler.sendMessage(message);
							};
						}.start();
					}else if (msgtype==5){
						shareEntity = JSON.parseObject(json,ShareEntity.class);
						if (MainActivity.unRectifyLocation!=null){
							new MySendAsync().execute(shareEntity.getDevice());
						}

						/*shareEntity = JSON.parseObject(json,ShareEntity.class);
						Intent intent1 = new Intent("action_share_pos");
						intent1.putExtra("shareEntity",shareEntity);
						sendBroadcast(intent1);*/
					}else if (msgtype==6){
						shareEntity = JSON.parseObject(json,ShareEntity.class);
						Intent intent1 = new Intent("action_share_pos");
						intent1.putExtra("shareEntity",shareEntity);
						sendBroadcast(intent1);
					}

					break;

				case MSG.TASK_NEWS_ALL:
					Intent intent1 = new Intent("action_news_all");
					intent1.putExtra("talkEntity",talkEntity);
					sendBroadcast(intent1);
					sendNotification("新消息","您有新消息，请查看",3);
					break;
				case MSG.TALK_NEWS:
					Intent intent2 = new Intent("action_get_new");
					intent2.putExtra("talkEntity",talkEntity);
					sendBroadcast(intent2);
					sendNotification("新消息","您有新消息，请查看",2);
					break;
				case MSG.SHOT_SETTING:
					handleMsg((String) msg.obj);
					break;
				default:
					break;
			}
		}
	};

	//井炮插入
	class MyInsertShotPointAsync extends AsyncTask<String, Integer, Boolean>{
		@Override
		protected Boolean doInBackground(String... params) {
			pointEntity = JSON.parseObject(params[0], com.mtkj.utils.entity.ShotPoint.class);//fastJson解析
			for (int i=0;i<pointEntity.getPoints().size();i++){
				shotPoint = new ShotPoint(ShotPoint.EMPTY_ID(),
						pointEntity.getPoints().get(i).get(0),
						pointEntity.getPoints().get(i).get(1),
						pointEntity.getPoints().get(i).get(2),
						GeoPoint.from2DoubleString(pointEntity.getPoints().get(i).get(3),pointEntity.getPoints().get(i).get(4)),
						Double.parseDouble(pointEntity.getPoints().get(i).get(5)),
						0,
						0
				);
				mPointDBDao.insertShotPoint(shotPoint);
			}
			return null;
		}
	}

	//钻井下药插入
	class MyInsertDrillPointAsync extends AsyncTask<String, Integer, Boolean>{
		@Override
		protected Boolean doInBackground(String... params) {
			pointEntity = JSON.parseObject(params[0], com.mtkj.utils.entity.ShotPoint.class);//fastJson解析
			for (int i=0;i<pointEntity.getPoints().size();i++){
				drillPoint = new DrillPoint(ShotPoint.EMPTY_ID(),
						pointEntity.getPoints().get(i).get(0),
						pointEntity.getPoints().get(i).get(1),
						pointEntity.getPoints().get(i).get(2),
						GeoPoint.from2DoubleString(pointEntity.getPoints().get(i).get(3),pointEntity.getPoints().get(i).get(4)),
						Double.parseDouble(pointEntity.getPoints().get(i).get(5)),
						Double.parseDouble(pointEntity.getPoints().get(i).get(6)),
						Double.parseDouble(pointEntity.getPoints().get(i).get(7)),
						Float.parseFloat(pointEntity.getPoints().get(i).get(8)),
						Double.parseDouble(pointEntity.getPoints().get(i).get(9)),
						0,
						0
						);
				mPointDBDao.insertDrillPoint(drillPoint);
			}
			return null;
		}
	}

	//查排列插入
	class MyInsertArrangePointAsync extends AsyncTask<String, Integer, Boolean>{
		@Override
		protected Boolean doInBackground(String... params) {
			pointEntity = JSON.parseObject(params[0], com.mtkj.utils.entity.ShotPoint.class);//fastJson解析
			for (int i=0;i<pointEntity.getPoints().size();i++){
				arrangePoint = new ArrangePoint(ArrangePoint.EMPTY_ID(),
						pointEntity.getPoints().get(i).get(0),
						pointEntity.getPoints().get(i).get(1),
						pointEntity.getPoints().get(i).get(2),
						"","",
						GeoPoint.from2DoubleString(pointEntity.getPoints().get(i).get(3),pointEntity.getPoints().get(i).get(4)),
						0,0
				);
				mPointDBDao.insertArrangePoint(arrangePoint);
			}
			return null;
		}
	}

	public void sendNotification(String title,String content,int type) {
		if (Build.VERSION.SDK_INT >= 26) {
			if(type==0 || type==1 || type==4) {
				mMediaPlayer = MediaPlayer.create(this, R.raw.task);//新任务
				mMediaPlayer.start();
			}else {
				mMediaPlayer = MediaPlayer.create(this, R.raw.news);//新消息
				mMediaPlayer.start();
			}

			/**
			 * 设置channel
			 * */
			/*Toast.makeText(BaseActivity.this,"您有新任务，请查看",Toast.LENGTH_SHORT).show();*/
		}else {
			/**
			 *  创建通知栏管理工具
			 */

			/**
			 *  实例化通知栏构造器
			 */

			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

			/**
			 *  设置Builder
			 */
			//设置标题
			Intent intent;
			if (type==0){
				intent = new Intent(this,TaskListActivity.class);
				PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
				mBuilder.setContentTitle(title)
						//设置内容
						.setContentText(content)
						//设置大图标
						.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon))
						//设置小图标
						.setSmallIcon(R.drawable.news)
						//设置通知时间
						.setWhen(System.currentTimeMillis())
						//首次进入时显示效果
						.setTicker("通知")
						//设置通知方式，声音，震动，呼吸灯等效果，这里通知方式为声音
						.setDefaults(Notification.DEFAULT_SOUND)
						.setContentIntent(pendingIntent)
						.setAutoCancel(true);
			}else if (type==1){
				mBuilder.setContentTitle(title)
						//设置内容
						.setContentText(content)
						//设置大图标
						.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon))
						//设置小图标
						.setSmallIcon(R.drawable.news)
						//设置通知时间
						.setWhen(System.currentTimeMillis())
						//首次进入时显示效果
						.setTicker("通知")
						//设置通知方式，声音，震动，呼吸灯等效果，这里通知方式为声音
						.setDefaults(Notification.DEFAULT_SOUND)
						.setAutoCancel(true);
			}else if (type==2){
				intent = new Intent(this,TalkActivity.class);
				ContactPersons.UserlistBean bean = new ContactPersons.UserlistBean();
				bean.setDevice(talkEntity.getDevice());
				bean.setName(talkEntity.getName());
				intent.putExtra("person",bean);
				PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
				mBuilder.setContentTitle(title)
						//设置内容
						.setContentText(content)
						//设置大图标
						.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon))
						//设置小图标
						.setSmallIcon(R.drawable.news)
						//设置通知时间
						.setWhen(System.currentTimeMillis())
						//首次进入时显示效果
						.setTicker("通知")
						//设置通知方式，声音，震动，呼吸灯等效果，这里通知方式为声音
						.setDefaults(Notification.DEFAULT_SOUND)
						.setContentIntent(pendingIntent)
						.setAutoCancel(true);
			}else if (type==3){
				intent = new Intent(this,TalkAllActivity.class);
				ContactPersons.UserlistBean bean = new ContactPersons.UserlistBean();
				bean.setDevice("szdzd");
				bean.setName(talkEntity.getName());
				intent.putExtra("person",bean);
				PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
				mBuilder.setContentTitle(title)
						//设置内容
						.setContentText(content)
						//设置大图标
						.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon))
						//设置小图标
						.setSmallIcon(R.drawable.news)
						//设置通知时间
						.setWhen(System.currentTimeMillis())
						//首次进入时显示效果
						.setTicker("通知")
						//设置通知方式，声音，震动，呼吸灯等效果，这里通知方式为声音
						.setDefaults(Notification.DEFAULT_SOUND)
						.setContentIntent(pendingIntent)
						.setAutoCancel(true);
			}else if (type==4){
				intent = new Intent(this,ReceiveTaskActivity.class);
				PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
				mBuilder.setContentTitle(title)
						//设置内容
						.setContentText(content)
						//设置大图标
						.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon))
						//设置小图标
						.setSmallIcon(R.drawable.news)
						//设置通知时间
						.setWhen(System.currentTimeMillis())
						//首次进入时显示效果
						.setTicker("通知")
						//设置通知方式，声音，震动，呼吸灯等效果，这里通知方式为声音
						.setDefaults(Notification.DEFAULT_SOUND)
						.setContentIntent(pendingIntent)
						.setAutoCancel(true);
			}

			//发送通知请求
			notificationManager.notify(type, mBuilder.build());
		}
	}

	protected void sendLogin() {
		try {
			if (SysConfig.isDSCloud) {
				if (SysConfig.SC == null || "".equals(SysConfig.SC)) {
					SysConfig.SC_ID = mPreferences.getString(SysContants.SC, SysConfig.SC_ID);
					SysConfig.SC = new StringBuffer().append(SysConfig.HANDSET).append(SysConfig.SC_ID).toString();
				}
				Proto_Login_Request login_Request = null;
				Proto_Head head = null;
				login_Request = Proto_Login_Request
						.newBuilder()
						.setAppKey(
								ByteString.copyFrom(SysConfig.APP_KEY, "GB2312"))
						.setUserName(ByteString.copyFrom(SysConfig.SC, "GB2312"))
						.setPasswd(ByteString.copyFrom("", "GB2312"))
						.setDeviceName(
								ByteString.copyFrom(SysConfig.SC, "GB2312"))
						.setDeviceType(DeviceType.DeviceType_Handset)
						.build();
//						}
				head = Proto_Head
						.newBuilder()
						.setProtoMsgType(ProtoMsgType.ProtoMsgType_Login)
						.setCmdSize(login_Request.toByteArray().length)
						.addReceivers(ByteString.copyFrom("dscloud", "GB2312"))
						.setReceivers(0, ByteString.copyFrom("dscloud", "GB2312"))
						.setSender(ByteString.copyFrom(SysConfig.SC, "GB2312"))
						.setMsgId(0).setPriority(1).setExpired(0).build();

				try {
					DataProcess.GetInstance().sendData(
							SocketUtils.writeBytes(head.toByteArray(),
									login_Request.toByteArray()));
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
		}
	}

	// ---------------------------------------------------------------------------
	/**
	 * 显示提示信息
	 *
	 * @param msg
	 */
	protected void showMessage(String msg) {
		Toast.makeText(BaseActivity.this, msg, Toast.LENGTH_SHORT).show();
	}

	public void hideFragment(Fragment f) {
		if (f != null && f.isAdded() && !f.isHidden()) {
			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction().setCustomAnimations(
							android.R.anim.fade_in, android.R.anim.fade_out);
			transaction.hide(f);
			transaction.commitAllowingStateLoss();
		}
	}

	public void showFragment(Fragment f) {
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction().setCustomAnimations(android.R.anim.fade_in,
						android.R.anim.fade_out);
		transaction.show(f);
		transaction.commitAllowingStateLoss();
	}

	public void addFragment(int id, Fragment f) {
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction().setCustomAnimations(android.R.anim.fade_in,
						android.R.anim.fade_out);
		transaction.add(id, f);
		transaction.commitAllowingStateLoss();
	}

	// ---------------------------------------------------------------------------
	public String getData(String key, String defValue) {
		return mPreferences.getString(key, defValue);
	}

	public int getData(String key, int defValue) {
		return mPreferences.getInt(key, defValue);
	}

	public long getData(String key, long defValue) {
		return mPreferences.getLong(key, defValue);
	}

	public float getData(String key, float defValue) {
		return mPreferences.getFloat(key, defValue);
	}

	public boolean getData(String key, boolean defValue) {
		return mPreferences.getBoolean(key, defValue);
	}

	public void setData(String key, Object o) {
		if (o != null) {
			SharedPreferences.Editor editor = mPreferences.edit();
			if (o instanceof Boolean) {
				editor.putBoolean(key, (Boolean) o);
			} else if (o instanceof Integer) {
				editor.putInt(key, (Integer) o);
			} else if (o instanceof Long) {
				editor.putLong(key, (Long) o);
			} else if (o instanceof Float) {
				editor.putFloat(key, (Float) o);
			} else if (o instanceof String) {
				editor.putString(key, (String) o);
			}
			editor.commit();
		}
	}

	class MySendAsync extends AsyncTask<String, Integer, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			String name = params[0];
			ShareEntity shareEntity = new ShareEntity();
			shareEntity.setLon(MainActivity.unRectifyLocation.getLongitude());
			shareEntity.setLat(MainActivity.unRectifyLocation.getLatitude());
			shareEntity.setName(getData(SysContants.USERNAME,""));
			shareEntity.setDevice(getData(SysContants.DEVICE,""));
			shareEntity.setMsgtype(6);
			String msg = JSON.toJSONString(shareEntity);//转化成json转发给所有用户
			try {
				DSSProtoDataJava.Proto_Notice proto_notice =
						DSSProtoDataJava.Proto_Notice.newBuilder().setMsg(ByteString.copyFrom(msg, "UTF-8")).
								build();
				DSSProtoDataJava.Proto_Head head = DSSProtoDataJava.Proto_Head.newBuilder()
						.setProtoMsgType(DSSProtoDataConstants.ProtoMsgType.ProtoMsgType_Notice)
						.setCmdSize(proto_notice.toByteArray().length)
						.addReceivers(ByteString.copyFrom(name,"GB2312"))
						.setSender(ByteString.copyFrom("", "GB2312"))
						.setPriority(1).setExpired(0).build();
				DataProcess.GetInstance().sendData(SocketUtils.writeBytes(head.toByteArray(), proto_notice.toByteArray()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
	}

	private void handleMsg(String msg) {
		if (msg != null && msg.length() > 0) {
			if (msg.startsWith(ProtocolConstants.NAME_JINGPAO.GK08)) {// 参数反馈
				handleMsgGK08(msg);
				Log.e("gk08",msg);
			} else if (msg.startsWith(ProtocolConstants.NAME_JINGPAO.GK12)){//更改放炮模式
				Log.e("gk12",msg);
				handleMsgGK12(msg);
			}
		}
	}

	private void saveShotSelectTypeConfig(){
		setData(SysContants.SHOTSELECTTYPE,SysConfig.shotSelectType);
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
}
