package com.mtkj.cnpc.services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mtkj.cnpc.activity.LoginActivity;
import com.mtkj.cnpc.activity.ReceiveTaskActivity;
import com.mtkj.cnpc.activity.TaskListActivity;
import com.mtkj.cnpc.protocol.bean.ShotPoint;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.constants.SysContants;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava;
import com.mtkj.cnpc.protocol.socket.DataProcess;
import com.mtkj.cnpc.sqlite.PointDBDao;
import com.mtkj.utils.entity.PointDemo;
import com.mtkj.utils.entity.TaskEntity;
import com.mtkj.cnpc.R;

import org.andnav.osm.util.GeoPoint;

import java.util.Timer;
import java.util.TimerTask;

import static android.app.Notification.VISIBILITY_SECRET;
import static android.support.v4.app.NotificationCompat.PRIORITY_DEFAULT;

public class ConnService extends Service {
    MyTimerTask timerTask;
    Timer mTimer;
    public static int no_count = 0;
    MyHandler myHandler;
    private SharedPreferences preferences;
    String json;
    protected PointDBDao mPointDBDao;
    PointDemo pointDemo;
    TaskEntity taskEntity;
    ShotPoint shotPoint;
    com.mtkj.utils.entity.ShotPoint pointEntity;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("onStartCommand", "onStartCommand: " );
        mPointDBDao = new PointDBDao(this);
        preferences = getSharedPreferences(SysContants.SYSCONFIG, MODE_PRIVATE);
        refreshConfig();
        initHandler();
        startConn();
        CheckConn();
        return super.onStartCommand(intent, flags, startId);
    }

    private void initHandler() {
        myHandler = new MyHandler();
        DataProcess.GetInstance().setMsgHandler(myHandler);
    }

    private void CheckConn() {
        mTimer = new Timer(true);
        timerTask = new MyTimerTask();
        mTimer.schedule(timerTask, 5 * 1000, 30 * 1000);
    }

    private void refreshConfig() {

        try {
            LoginActivity.isLogin = preferences.getBoolean(SysContants.ISLOGIN, false);

            SysConfig.isOnlineMap = preferences.getBoolean(SysContants.ISONLINEMAP, true);
            SysConfig.isProjectShow = preferences.getBoolean(SysContants.ISPROJECTSHOW, false);

            SysConfig.isDSCloud = preferences.getBoolean(SysContants.ISCLOUD, SysConfig.isDSCloud);

            SysConfig.workType = preferences.getInt(SysContants.WORK_TYPE, SysConfig.WorkType.WORK_TYPE_NONE);
            SysConfig.APP_KEY = preferences.getString(SysContants.APPKEY, SysConfig.APP_KEY);
            SysConfig.IP = preferences.getString(SysContants.WIFI_IP, SysConfig.IP);
            SysConfig.PORT = preferences.getInt(SysContants.WIFI_PORT, SysConfig.PORT);

//			HttpContact.URL = "http://" + SysConfig.IP + ":" + SysConfig.PORT + "/huobao-service/";

            SysConfig.BZJ_ID = preferences.getString(SysContants.BZJ, SysConfig.BZJ_ID);
            SysConfig.SC_ID = preferences.getString(SysContants.SC, SysConfig.SC_ID);
            SysConfig.ZZJG_ID = preferences.getString(SysContants.ZZJG, SysConfig.ZZJG_ID);
            SysConfig.SC = new StringBuffer().append(SysConfig.HANDSET).append(SysConfig.SC_ID).toString();

            SysConfig.ShotproMax = preferences.getFloat(SysContants.SHOTPRO_MAX, SysConfig.ShotproMax);
            SysConfig.safe_Distance = preferences.getFloat(SysContants.SAFE_DISTANCE, SysConfig.safe_Distance);
            SysConfig.Readytimeout = preferences.getFloat(SysContants.READY_TIMEOUT, SysConfig.Readytimeout);
            SysConfig.PowerTimeout = preferences.getFloat(SysContants.POWER_TIMEOUT, SysConfig.PowerTimeout);
            SysConfig.GPS_UP_TIME_TIP = preferences.getInt(SysContants.GPS_UPTIME, SysConfig.GPS_UP_TIME_TIP);

        } catch (Exception e) {
        }
    }

    public void startConn() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                if (DataProcess.GetInstance().isConnected()) {

                } else {
                    DataProcess.GetInstance().startConn(SysConfig.IP,
                            SysConfig.PORT);
                }
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
            timerTask.cancel();
            timerTask = null;
        }
        //DataProcess.GetInstance().stopConn();
        Log.e("SERVICE", "CLOSED");
        Intent localIntent = new Intent();
        localIntent.setClass(this, ConnService.class); //销毁时重新启动Service
        this.startService(localIntent);
    }

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            if (!DataProcess.GetInstance().isConnected()) {
                if (!DataProcess.isConning) {
                    DataProcess.GetInstance().startConn(SysConfig.IP, SysConfig.PORT);
                }
            }
        }
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DataProcess.MSG.WEB_TASK_NOTICE:
                    DSSProtoDataJava.Proto_WebTaskNotice notice = (DSSProtoDataJava.Proto_WebTaskNotice) msg.obj;
                    String no = notice.getMsgType().toStringUtf8();
                    Log.e("WebTaskNotice", "handleMessage: "+no);
//                    sendNotification("您有新任务","您有新任务,请点击查看");
                    break;
                default:
                    break;
            }
        }

        class MyInsertAsync extends AsyncTask<String, Integer, Boolean> {
            @Override
            protected Boolean doInBackground(String... params) {
                pointEntity = JSON.parseObject(params[0], com.mtkj.utils.entity.ShotPoint.class);//fastJson解析
                for (int i = 0; i < pointEntity.getPoints().size(); i++) {

                    shotPoint = new ShotPoint(ShotPoint.EMPTY_ID(),
                            pointEntity.getPoints().get(i).get(0),
                            pointEntity.getPoints().get(i).get(1),
                            pointEntity.getPoints().get(i).get(2),
                            GeoPoint.from2DoubleString(pointEntity.getPoints().get(i).get(3), pointEntity.getPoints().get(i).get(4)),
                            //new GeoPoint(Integer.parseInt(pointEntity.getPoints().get(i).get(3)),Integer.parseInt(pointEntity.getPoints().get(i).get(4))),
                            Double.parseDouble(pointEntity.getPoints().get(i).get(5)),
                            0,
                            0
                    );
                    mPointDBDao.insertShotPoint(shotPoint);
                }
                return null;
            }
        }



    }
}
