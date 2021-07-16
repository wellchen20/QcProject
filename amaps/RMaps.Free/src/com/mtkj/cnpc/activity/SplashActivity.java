package com.mtkj.cnpc.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.constants.SysConfig.WorkType;
import com.mtkj.cnpc.protocol.constants.SysContants;
import com.mtkj.cnpc.R;
import com.robert.maps.applib.utils.LogFileUtil;
import com.robert.maps.applib.utils.Ut;
import com.xylink.sdk.sample.IncomingCallService;

import java.util.ArrayList;
import java.util.List;

/***
 * 启动界面
 * 
 * @author TNT
 * 
 */
public class SplashActivity extends Activity {
	
	private static final int sleepTime = 3 / 2 * 1000;
	private SharedPreferences preferences;
	public static int PERMISSION_REQ = 0x123456;

	private String[] mPermission = new String[] {
			Manifest.permission.CAMERA,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.RECORD_AUDIO,
			Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.BLUETOOTH
//			Manifest.permission.SYSTEM_ALERT_WINDOW
	};

	private List<String> mRequestPermission = new ArrayList<String>();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
// 被叫服务，不使用被叫功能的请忽略
		Intent incomingCallService = new Intent(this, IncomingCallService.class);
		startService(incomingCallService);
		preferences = getSharedPreferences(SysContants.SYSCONFIG, MODE_PRIVATE);
		// 初始化项目目录
		Ut.getRMapsMapsDir(this);
		Ut.getRMapsProjectDir(this);
		Ut.getRMapsProjectPublicDir(this);
		Ut.getRMapsRMapsProjectPrivateDir(this);
		Ut.getRMapsProjectPrivatePointsDir(this);
		Ut.getRMapsProjectPrivatePointsInputDir(this);
		Ut.getRMapsProjectPrivatePointsOutputDir(this);
		Ut.getRMapsProjectPrivateTracksDir(this);
		Ut.getRMapsProjectPrivateTracksInputDir(this);
		Ut.getRMapsProjectPrivateTracksOutputDir(this);
		Ut.getRMapsProjectPrivateTasksDir(this);
		Ut.getRMapsProjectPrivateTasksInputDir(this);
		Ut.getRMapsProjectPrivateTasksOutputDir(this);
		LogFileUtil.getSize();
		// 更新参数
		refreshConfig();
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
			for (String one : mPermission) {
				if (PackageManager.PERMISSION_GRANTED != this.checkSelfPermission(one)) {
					mRequestPermission.add(one);
				}
			}
			if (!mRequestPermission.isEmpty()) {
				this.requestPermissions(mRequestPermission.toArray(new String[mRequestPermission.size()]), PERMISSION_REQ);
				return ;
			}
		}
		startActiviy();
	}


    @Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions,  int[] grantResults) {
		// 版本兼容
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
			return;
		}
		if (requestCode == PERMISSION_REQ) {
			for (int i = 0; i < grantResults.length; i++) {
				for (String one : mPermission) {
					if (permissions[i].equals(one) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
						mRequestPermission.remove(one);
					}
				}
			}
			startActiviy();
		}
	}

	private void refreshConfig() {

		try {
			LoginActivity.isLogin = preferences.getBoolean(SysContants.ISLOGIN, false);
			
			SysConfig.isOnlineMap = preferences.getBoolean(SysContants.ISONLINEMAP, true);
			SysConfig.isProjectShow = preferences.getBoolean(SysContants.ISPROJECTSHOW, false);
			SysConfig.shotSelectType = preferences.getInt(SysContants.SHOTSELECTTYPE,SysConfig.shotSelectType);
			
			SysConfig.isDSCloud = preferences.getBoolean(SysContants.ISCLOUD, SysConfig.isDSCloud);
			
			SysConfig.workType = preferences.getInt(SysContants.WORK_TYPE, WorkType.WORK_TYPE_NONE);
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

	public void startActiviy() {
		if (mRequestPermission.isEmpty()) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					Intent intent01 = new Intent(SplashActivity.this, MainActivity.class);
					Intent intent02 = new Intent(SplashActivity.this, LoginActivity.class);
					if (preferences.getBoolean(SysContants.ISLOGIN,false)){
						startActivity(intent01);
					}else {
						startActivity(intent02);
					}
					SplashActivity.this.finish();
				}
			},1000);
		} else {
			//在Manifest中未要求任何权限时也会跳到这里
			Toast.makeText(this, "权限注册失败!", Toast.LENGTH_LONG).show();
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					SplashActivity.this.finish();
				}
			}, 2000);
		}
	}

	public void setData(String key, Object o) {
		if (o != null) {
			SharedPreferences.Editor editor = preferences.edit();
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

}
