package com.robert.maps.applib;

import java.util.List;
import java.util.Locale;

import org.andnav.osm.util.GeoPoint;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDexApplication;

import com.ainemo.sdk.otf.NemoSDK;
import com.ainemo.sdk.otf.Settings;
import com.xylink.sdk.sample.IncomingCallService;
import com.xylink.sdk.sample.utils.AlertUtil;
import com.xylink.sdk.sample.utils.DeviceInfoUtils;

public class MapApplication extends MultiDexApplication {
	private Locale locale = null;
	private Locale defLocale = null;
	
	@Override
	public void onCreate() {
		super.onCreate();

/**
 * 小鱼
 *
 * APPID:BVNGNNNNNNVNT
 *
 * token：
 * 973970f676450cee37f841c613fab8da95a7126481ddbe4ad4454fd02d3fb9e6
 *
 * enterpriseID(AINEMO_EXT_ID)：
 * 3bd8784bc2863b9ab404344f3575e7a58c909920
 * */
		Settings settings = new Settings("3bd8784bc2863b9ab404344f3575e7a58c909920");

		AlertUtil.init(getApplicationContext());
		// 默认使用扬声器或听筒模式c
		// settings.setSpeakerOnModeDefault(false);
		// 0:后置 默认1:前置
		//settings.setDefaultCameraId(1);
		int pId = Process.myPid();
		String processName = "";
		ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> ps = am.getRunningAppProcesses();
		for (ActivityManager.RunningAppProcessInfo p : ps) {
			if (p.pid == pId) {
				processName = p.processName;
				break;
			}
		}

		// 避免被初始化多次
		if (processName.equals(getPackageName())) {
			NemoSDK nemoSDK = NemoSDK.getInstance();
			nemoSDK.init(this, settings);

			/*// 被叫服务，不使用被叫功能的请忽略
			Intent incomingCallService = new Intent(this, IncomingCallService.class);
			startService(incomingCallService);*/
		}
		DeviceInfoUtils.init(this);
	
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        Configuration config = getBaseContext().getResources().getConfiguration();
        defLocale = config.locale;
        locale = defLocale;
        
        String lang = pref.getString("pref_locale", "");
		if(lang.equalsIgnoreCase("zh_CN")) {
			locale = Locale.SIMPLIFIED_CHINESE;
		} else if(lang.equalsIgnoreCase("zh_TW")) {
			locale = Locale.TRADITIONAL_CHINESE;
		} else if(!lang.equalsIgnoreCase("") && !lang.equalsIgnoreCase(" ")) {
            locale = new Locale(lang);
		} 
        Locale.setDefault(locale);
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }
	
	public Locale getDefLocale() {
		return defLocale;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        Configuration config = getBaseContext().getResources().getConfiguration();
        defLocale = config.locale;
        locale = defLocale;
        
        String lang = pref.getString("pref_locale", "");
		if(lang.equalsIgnoreCase("zh_CN")) {
			locale = Locale.SIMPLIFIED_CHINESE;
		} else if(lang.equalsIgnoreCase("zh_TW")) {
			locale = Locale.TRADITIONAL_CHINESE;
		} else if(!lang.equalsIgnoreCase("") && !lang.equalsIgnoreCase(" ")) {
            locale = new Locale(lang);
		} 
        Locale.setDefault(locale);
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
	}
	
}
