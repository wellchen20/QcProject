package com.mtkj.cnpc.activity.service;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.protobuf.ByteString;
import com.mtkj.cnpc.R;
import com.mtkj.cnpc.activity.ReceiveTaskActivity;
import com.mtkj.cnpc.protocol.constants.DSSProtoDataConstants.ProtoMsgType;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_Head;
import com.mtkj.cnpc.protocol.socket.DataProcess;
import com.mtkj.cnpc.protocol.utils.SocketUtils;
import com.robert.maps.applib.utils.TimeUtil;

import static android.app.Notification.VISIBILITY_SECRET;
import static android.support.v4.app.NotificationCompat.PRIORITY_DEFAULT;
import static com.mtkj.cnpc.activity.MainActivity.unRectifyLocation;

public class SendService extends Service {
	private Timer timer;
	private MyTimerTask timerTask;
	String CHANNEL_ID = "channel";
	String CHANNEL_NAME = "channel_name";
	NotificationCompat.Builder mBuilder;
	NotificationManager notificationManager;
	MyHandler myHandler;
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("onStartCommand", "onStartCommand: ");
		initHandler();
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//		createNotificationChannel();
		if (SysConfig.isDSCloud) {
			if (!DataProcess.GetInstance().isConnected()) {
				new ConnTask().execute();
			}
		}
		if (timer==null && timerTask==null){
			initTimer();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void initTimer() {
		timer = new Timer(true);
		timerTask = new MyTimerTask();
		timer.schedule(timerTask, 0, SysConfig.GPS_UP_TIME_TIP * 1000);
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		if (timer != null) {
			timer.cancel();
			timer.purge();
			timer = null;
			timerTask.cancel();
			timerTask = null;
		}
		DataProcess.GetInstance().stopConn();
	}

	public class ConnTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			boolean iscon = false;
			if (!DataProcess.isConning) {
				iscon = DataProcess.GetInstance().startConn(SysConfig.IP,
						SysConfig.PORT);
			}
			return iscon;
		}

		/*@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (!result) {
				new ConnTask().execute();
			}
		}*/

	}

	private void initHandler() {
		myHandler = new MyHandler();
		DataProcess.GetInstance().setBaseHandler(myHandler);
	}

	class MyTimerTask extends TimerTask {

		@Override
		public void run() {
			// ????????????????????????
			sendLocationTo();
		}
	};

	private void sendLocationTo() {
		if (unRectifyLocation != null) {
			if (!DataProcess.GetInstance().isConnected()) {
				if (!DataProcess.isConning) {
					new ConnTask().execute();
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
				DSSProtoDataJava.Proto_DeviceTrace proto_DeviceTrace = DSSProtoDataJava.Proto_DeviceTrace
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


	class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case DataProcess.MSG.WEB_TASK_NOTICE:
//					DSSProtoDataJava.Proto_WebTaskNotice notice = (DSSProtoDataJava.Proto_WebTaskNotice) msg.obj;
//					String no = notice.getMsgType().toStringUtf8();
					sendNotifications("?????????","?????????,???????????????");
//					Log.e("WebTaskNotice", "handleMessage: "+no);
					break;
				default:
					break;
			}
		}
	}

	public void sendNotifications(String title,String content) {
		Log.e("sendNoti", "sendNotifi");
		if (mBuilder==null){
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				mBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
			} else {
				mBuilder = new NotificationCompat.Builder(getApplicationContext());
				mBuilder.setPriority(PRIORITY_DEFAULT);
			}
		}
		//????????????
		Intent intent = new Intent(this, ReceiveTaskActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		mBuilder.setContentTitle(title)
				//????????????
				.setContentText(content)
				//???????????????
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon))
				//???????????????
				.setSmallIcon(R.drawable.news)
				//??????????????????
				.setWhen(System.currentTimeMillis())
				//???????????????????????????
				.setTicker("??????")
				//???????????????????????????????????????????????????????????????????????????????????????
				.setDefaults(Notification.DEFAULT_SOUND)
				.setContentIntent(pendingIntent)
				.setAutoCancel(true);
		//??????????????????
		notificationManager.notify(1, mBuilder.build());
	}

	//??????channel
/*	@TargetApi(Build.VERSION_CODES.O)
	private void createNotificationChannel() {
		Log.e("Channel", "createNotificationCh");
		NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
		//??????????????????????????????
		channel.canBypassDnd();
		//?????????
		channel.enableLights(true);
		//??????????????????
		channel.setLockscreenVisibility(VISIBILITY_SECRET);
		//????????????????????????
		channel.setLightColor(Color.RED);
		//??????launcher???????????????
		channel.canShowBadge();
		//??????????????????
		channel.enableVibration(true);
		//???????????????????????????????????????
		channel.getAudioAttributes();
		//?????????????????????
		channel.getGroup();
		//???????????????  ??????????????????
		channel.setBypassDnd(true);
		//??????????????????
//		channel.setVibrationPattern(new long[]{100, 100, 200});
		//??????????????????
		channel.shouldShowLights();
		notificationManager.createNotificationChannel(channel);
	}*/
}
