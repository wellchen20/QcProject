package com.mtkj.cnpc.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.PopupWindowCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationQualityReport;
import com.amap.api.navi.model.NaviLatLng;
import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.google.protobuf.ByteString;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.mtkj.cnpc.R;
import com.mtkj.cnpc.activity.InterFace.ITaskShot;
import com.mtkj.cnpc.activity.fragment.BottomGuidingFragment;
import com.mtkj.cnpc.activity.fragment.BottomTaskPaoDianFragment;
import com.mtkj.cnpc.activity.fragment.PersonalFragment;
import com.mtkj.cnpc.activity.fragment.adapter.ZhuangHaoAroundArrangeAdapter;
import com.mtkj.cnpc.activity.fragment.adapter.ZhuangHaoAroundDrillAdapter;
import com.mtkj.cnpc.activity.fragment.adapter.ZhuangHaoAroundSelectAdapter;
import com.mtkj.cnpc.activity.service.SendService;
import com.mtkj.cnpc.activity.utils.DownLoadManager;
import com.mtkj.cnpc.activity.utils.FileUtils;
import com.mtkj.cnpc.activity.utils.UpdataInfoParser;
import com.mtkj.cnpc.broadcast.OpenMapBroadcast;
import com.mtkj.cnpc.broadcast.OpenMapBroadcast.IShowMapId;
import com.mtkj.cnpc.protocol.bean.ArrangePoint;
import com.mtkj.cnpc.protocol.bean.DrillPoint;
import com.mtkj.cnpc.protocol.bean.DrillRecord;
import com.mtkj.cnpc.protocol.bean.ShotPoint;
import com.mtkj.cnpc.protocol.bean.TaskPoint;
import com.mtkj.cnpc.protocol.constants.DSSProtoDataConstants.ProtoMsgType;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.constants.SysConfig.WorkType;
import com.mtkj.cnpc.protocol.constants.SysContants;
import com.mtkj.cnpc.protocol.overlay.ArrangePointOverlay;
import com.mtkj.cnpc.protocol.overlay.DrillPointOverlay;
import com.mtkj.cnpc.protocol.overlay.ShotPointOverlay;
import com.mtkj.cnpc.protocol.overlay.WorkAreaOverlay;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_Head;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_WellShotData;
import com.mtkj.cnpc.protocol.shot.RF01;
import com.mtkj.cnpc.protocol.socket.DataProcess;
import com.mtkj.cnpc.protocol.utils.SocketUtils;
import com.mtkj.cnpc.sqlite.PointDBDao;
import com.mtkj.utils.entity.CheckRecord;
import com.mtkj.utils.entity.ContactPersons;
import com.mtkj.utils.entity.UpdateInfo;
import com.mtkj.utils.entity.VoiceEntity;
import com.mtkj.utils.entity.WorkAreaEntity;
import com.mtkj.utils.entity.WorkAreaPoint;
import com.robert.maps.applib.MainPreferences;
import com.robert.maps.applib.downloader.AreaSelectorActivity;
import com.robert.maps.applib.downloader.FileDownloadListActivity;
import com.robert.maps.applib.kml.PoiListActivity;
import com.robert.maps.applib.kml.PoiManager;
import com.robert.maps.applib.kml.PoiPoint;
import com.robert.maps.applib.kml.TrackListActivity;
import com.robert.maps.applib.kml.XMLparser.PredefMapsParser;
import com.robert.maps.applib.overlays.MyLocationOverlay;
import com.robert.maps.applib.overlays.SearchResultOverlay;
import com.robert.maps.applib.overlays.TileOverlay;
import com.robert.maps.applib.tileprovider.TileProviderFileBase;
import com.robert.maps.applib.tileprovider.TileSource;
import com.robert.maps.applib.tileprovider.TileSourceBase;
import com.robert.maps.applib.utils.CrashReportHandler;
import com.robert.maps.applib.utils.DialogUtils;
import com.robert.maps.applib.utils.GpsCorrect;
import com.robert.maps.applib.utils.GpsCorrect.Gps;
import com.robert.maps.applib.utils.LogFileUtil;
import com.robert.maps.applib.utils.RException;
import com.robert.maps.applib.utils.SQLiteMapDatabase;
import com.robert.maps.applib.utils.SimpleThreadFactory;
import com.robert.maps.applib.utils.Ut;
import com.robert.maps.applib.view.IMoveListener;
import com.robert.maps.applib.view.MapView;
import com.robert.maps.applib.view.TileView;
import com.robert.maps.applib.view.TileViewOverlay;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.util.TypeConverter;
import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import detection.DetectorActivity;

@SuppressLint({"NewApi", "HandlerLeak"})
public class MainActivity extends SlidingFragmentActivity implements ITaskShot, IShowMapId, EventListener {
	private SensorManager mOrientationSensorManager;
	public class RequestCode {
		public final static int MAPSET = 0x01;
		public final static int AREA_SELETER = 0x02;
		public final static int POIS = 0x03;
		public final static int COLLECT_POI = 0x04;
		public final static int IMPORT_POI = 0x05;
		public final static int TRACKS = 0x06;
		public final static int EDIT_TRACK = 0x07;
		public final static int IMPORT_TRACK = 0x08;
		public final static int POI_GUIDE = 0x09;
		public final static int TASK_GUIDE = 0x0a;
		public final static int STOP_GUIDE = 0x0b;
		public final static int SHOTPOINT_ISDONE = 0X0e;
		public final static int PROJECT_MAP = 0x0f;
		public final static int SEARCH = 0x10;
		public final static int OUTMAP = 0x11;
		public final static int START_TRACK = 0x12;
		public final static int PAUSE_TRACK = 0x13;
		public final static int STOP_TRACK = 0x14;
		public final static int CLOSE_TRACK = 0x15;
		public final static int STOP_GUIDING = 0x16;
		public final static int TASK_WRITE = 0x17;
		public final static int CHECKED = 0x18;
	}

	private static final String MAPNAME = "MapName";
	private static final String ACTION_SHOW_POINTS = "com.robert.maps.action.SHOW_POINTS";

	private MapView mMap;
	private ImageView iv_person, map_zoom_in, map_zoom_full, map_zoom_out, // 右下，地图缩放
			map_location, tv_is_login; // 左下，地图定位
	private TextView map_menu_search;
	private AppCompatCheckBox checkBox;

	private TileSource mTileSource;
	private PoiManager mPoiManager;
	private Handler mCallbackHandler = new MainActivityCallbackHandler();
	private MoveListener mMoveListener = new MoveListener();
	private PowerManager.WakeLock myWakeLock;
	private SharedPreferences uiState;

	// Overlays
	private TileOverlay mTileOverlay = null;
	private boolean mShowOverlay = false;
	private String mMapId = null;
	private String mOverlayId = "";
	private MyLocationOverlay mMyLocationOverlay;
	private ShotPointOverlay mShotPointOverlay;
	private DrillPointOverlay mDrillPointOverlay;
	private ArrangePointOverlay mArrangePointOverlay;
	private WorkAreaOverlay mWorkAreaOverlay;

	private BottomGuidingFragment mGuidingFragment;
	private boolean mAutoFollow = true;
	private String mGpsStatusName = "";
	private float mLastSpeed, mLastBearing;
	private boolean mCompassEnabled;
	private boolean mDrivingDirectionUp;

	private ExecutorService mThreadPool = Executors.newSingleThreadExecutor(new SimpleThreadFactory("MainActivity.Search"));
	public static boolean isChangeMap = false;

	private BroadcastReceiver gpsReceiver;
	ProgressDialog pd;    //进度条对话框
	String ACTION_SHARE_POS = "action_share_pos";
	String ACTION_POINT_LOC = "action_point_loc";
	String REFUSH_STATUS_PHONE = "refush_status_phone";
	//声明mlocationClient对象
	public AMapLocationClient locationClient;
	//声明mLocationOption对象
	public AMapLocationClientOption locationOption = null;
	String guidName = "";

	private SharedPreferences mPreferences;
	protected PointDBDao mPointDBDao;
	private EventManager asr;
	private ImageView iv_speach;
	private ImageView iv_search;
	private ImageView iv_close_voice;
	protected boolean enableOffline = false; // 测试离线命令词，需要改成true
	private LinearLayout ll_search;
	private FrameLayout layout;
	private PopupWindow voice_popupWindow;
	private TextView tv_title;
	private TextView tv_content;
	private WorkAreaEntity mAreaEntity;
	String json;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		OpenMapBroadcast.setShowMapId(this);
		mPointDBDao = new PointDBDao(this);
		mPreferences = getSharedPreferences(SysContants.SYSCONFIG, MODE_PRIVATE);
		SysConfig.workType = getData(SysContants.WORK_TYPE, SysConfig.workType);
//		startService(new Intent(MainActivity.this, SendService.class));
		initRightMenu();
		initMap();
		initWorkArea();
		initLocation();
		startLocation();
		// 控制是否存储异常
		if (!OpenStreetMapViewConstants.DEBUGMODE)
			CrashReportHandler.attach(this);
		mOrientationSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		CreateContentView();
		initVoice();
		initPopu();
		mPoiManager = new PoiManager(this);
		mMap.setMoveListener(mMoveListener);

		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		uiState = getPreferences(Activity.MODE_PRIVATE);

		// Init
		mCompassEnabled = uiState.getBoolean("CompassEnabled", false);
		mAutoFollow = uiState.getBoolean("AutoFollow", true);

		mMap.getController().setCenter(new GeoPoint(uiState.getInt("Latitude", 39909604), uiState.getInt("Longitude", 116397228)));
//		mGPSFastUpdate = pref.getBoolean("pref_gpsfastupdate", true);
		mAutoFollow = uiState.getBoolean("AutoFollow", true);
		setAutoFollow(mAutoFollow, true);

		this.mMyLocationOverlay = new MyLocationOverlay(this);
		this.mShotPointOverlay = new ShotPointOverlay(this, mPointDBDao, null, false);
		this.mDrillPointOverlay = new DrillPointOverlay(this,mPointDBDao,null,false);
		this.mArrangePointOverlay = new ArrangePointOverlay(this,mPointDBDao,null,false);
		addWorkArea();

		// loadOverlay
		FillOverlays();
		mGuidingFragment = new BottomGuidingFragment();
		mGuidingFragment.setHandler(mCallbackHandler);
		mDrivingDirectionUp = pref.getBoolean("pref_drivingdirectionup", true);

		final int screenOrientation = Integer.parseInt(pref.getString("pref_screen_orientation", "-1"));
		setRequestedOrientation(screenOrientation);

		final boolean fullScreen = pref.getBoolean("pref_showstatusbar", true);
		if (fullScreen)
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		else
			getWindow()
					.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


		// GPS状态监听
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
		intentFilter.addAction(ACTION_SHARE_POS);
		intentFilter.addAction(ACTION_POINT_LOC);
		intentFilter.addAction(REFUSH_STATUS_PHONE);
		gpsReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(REFUSH_STATUS_PHONE)) {
					if (DataProcess.isLoginDscloud) {
						tv_is_login.setImageResource(R.drawable.online_m);
					} else {
						tv_is_login.setImageResource(R.drawable.offline_m);
					}
				} else if (intent.getAction().equals(ACTION_POINT_LOC)) {
					String stationNo = intent.getStringExtra("stationNo");
					TaskPoint taskPoint = null;
					if (SysConfig.workType == WorkType.WORK_TYPE_SHOT) {
						taskPoint = mPointDBDao.selectShotPointTotaskPoint(stationNo);
					}else if (SysConfig.workType == WorkType.WORK_TYPE_DRILE){
						taskPoint = mPointDBDao.selectDrillPoint(stationNo);
					}else if(SysConfig.workType == WorkType.WORK_TYPE_ARRANGE){
						taskPoint = mPointDBDao.selectArrangePoint(stationNo);
					}
					if (taskPoint != null && !"".equals(taskPoint.stationNo)) {
						mMap.getController().setCenter(taskPoint.geoPoint);
						if (SysConfig.workType == WorkType.WORK_TYPE_SHOT) {
							mMap.getTileView().mShotMenuInfo.EventGeoPoint = taskPoint.geoPoint;
							mMap.getTileView().mShotMenuInfo.MarkerIndex = taskPoint.Id;
							mMap.getTileView().mShotMenuInfo.stationNo = taskPoint.stationNo;
							mShotPointOverlay.setTapIndex(taskPoint.Id);
						}else if (SysConfig.workType == WorkType.WORK_TYPE_DRILE){
							mMap.getTileView().mDrillMenuInfo.EventGeoPoint = taskPoint.geoPoint;
							mMap.getTileView().mDrillMenuInfo.MarkerIndex = taskPoint.Id;
							mMap.getTileView().mDrillMenuInfo.stationNo = taskPoint.stationNo;
							mDrillPointOverlay.setTapIndex(taskPoint.Id);

						}else if (SysConfig.workType == WorkType.WORK_TYPE_ARRANGE){
							mMap.getTileView().mArrangeMenuInfo.EventGeoPoint = taskPoint.geoPoint;
							mMap.getTileView().mArrangeMenuInfo.MarkerIndex = taskPoint.Id;
							mMap.getTileView().mArrangeMenuInfo.stationNo = taskPoint.stationNo;
							mArrangePointOverlay.setTapIndex(taskPoint.Id);
						}
						mMap.getTileView().showContextMenu();

					}
				}
			}
		};
		registerReceiver(gpsReceiver, intentFilter);

		if (!uiState.getString("app_version", "").equalsIgnoreCase(Ut.getAppVersion(this))) {
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
		}

		final Intent queryIntent = getIntent();
		final String queryAction = queryIntent.getAction();

		if (ACTION_SHOW_POINTS.equalsIgnoreCase(queryAction)) {
			ActionShowPoints(queryIntent);
		} else if (Intent.ACTION_VIEW.equalsIgnoreCase(queryAction)) {
			Uri uri = queryIntent.getData();
			if (uri.getScheme().equalsIgnoreCase("geo")) {
				final String latlon = uri.getEncodedSchemeSpecificPart().replace("?" + uri.getEncodedQuery(), "");
				if (latlon.equals("0,0")) {

				} else {
					GeoPoint point = GeoPoint.fromDoubleString(latlon);
					setAutoFollow(false);
					mMap.getController().setCenter(point);
				}
			}
		} else if ("SHOW_MAP_ID".equalsIgnoreCase(queryAction)) {
			final Bundle bundle = queryIntent.getExtras();
			mMapId = bundle.getString(MAPNAME);
			if (bundle.containsKey("center")) {
				try {
					final GeoPoint geo = GeoPoint.fromDoubleString(bundle.getString("center"));
					mMap.getController().setCenter(geo);
				} catch (Exception e) {
				}
			}

			if (bundle.containsKey("zoom")) {
				try {
					final int zoom = Integer.valueOf(bundle.getString("zoom"));
					mMap.getController().setZoom(zoom);
					Editor editor = uiState.edit();
					editor.putInt("ZoomLevel", mMap.getZoomLevel());
					editor.commit();
				} catch (Exception e) {
				}
			}
			queryIntent.setAction("");
		}
		checkVersion();//检查版本
	}

	private void addWorkArea(){
		if (mWorkAreaOverlay==null){
			mWorkAreaOverlay = new WorkAreaOverlay(this);
			if (mAreaEntity!=null && mAreaEntity.getWork_area()!=null){
				for (int i=0;i<mAreaEntity.getWork_area().size();i++){
					mWorkAreaOverlay.addPoint(new WorkAreaPoint(i+"",GeoPoint.fromDouble(mAreaEntity.getWork_area().get(i).getLat(),mAreaEntity.getWork_area().get(i).getLon())),mMap.getTileView());
				}
			}
		}
	}

	//获取工区图层坐标点
	private void initWorkArea() {
		//project/private/points/input
		String path = Ut.getRMapsProjectPrivatePointsInputDir(this).getPath()+"/area.txt";
		File areaFile = new File(path);
		json = FileUtils.readTxt(path);
		Log.e("initWorkArea", "initWorkArea: "+json );
		if (json!=null && areaFile.exists()){
			mAreaEntity = JSON.parseObject(json,WorkAreaEntity.class);
		}

	}

	private static int makeDropDownMeasureSpec(int measureSpec) {
		int mode;
		if (measureSpec == ViewGroup.LayoutParams.WRAP_CONTENT) {
			mode = View.MeasureSpec.UNSPECIFIED;
		} else {
			mode = View.MeasureSpec.EXACTLY;
		}
		return View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(measureSpec), mode);
	}

	private void initPopu() {
		//加载布局
		layout = (FrameLayout) LayoutInflater.from(MainActivity.this).inflate(
				R.layout.layout_guide, null);
		tv_title = layout.findViewById(R.id.tv_title);
		tv_content = layout.findViewById(R.id.tv_content);
		iv_close_voice = layout.findViewById(R.id.iv_close_voice);
		iv_close_voice.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				voice_popupWindow.dismiss();
				stop();
			}
		});
		// 实例化popupWindow
		voice_popupWindow = new PopupWindow(layout, getWindowManager().getDefaultDisplay().getWidth()*9/10,350);
		View contentView = voice_popupWindow.getContentView();
		//需要先测量，PopupWindow还未弹出时，宽高为0
		contentView.measure(makeDropDownMeasureSpec(voice_popupWindow.getWidth()),
				makeDropDownMeasureSpec(voice_popupWindow.getHeight()));
		voice_popupWindow.setOutsideTouchable(false);
	}

	private void initVoice() {
		iv_speach = (ImageView) findViewById(R.id.iv_speach);
		//获取背景，并将其强转成AnimationDrawable
		AnimationDrawable animationDrawable = (AnimationDrawable) iv_speach.getBackground();
		//判断是否在运行
		if(!animationDrawable.isRunning()){
			//开启帧动画
			animationDrawable.start();
		}
		// 基于sdk集成1.1 初始化EventManager对象
		asr = EventManagerFactory.create(this, "asr");
		// 基于sdk集成1.3 注册自己的输出事件类
		asr.registerListener(this); //  EventListener 中 onEvent方法
		iv_speach.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				start();
//				int offsetX = Math.abs(voice_popupWindow.getContentView().getMeasuredWidth()-ll_search.getWidth()) / 2;
				int offsetX = Math.abs(getWindowManager().getDefaultDisplay().getWidth()-voice_popupWindow.getContentView().getMeasuredWidth()) / 2;
				int offsetY = 10;
//				PopupWindowCompat.showAsDropDown(voice_popupWindow, ll_search, offsetX, offsetY, Gravity.START);//开启语音窗口
				PopupWindowCompat.showAsDropDown(voice_popupWindow, findViewById(R.id.iv_person), offsetX, offsetY, Gravity.START);//开启语音窗口
			}
		});


		if (enableOffline) {
			loadOfflineEngine(); // 测试离线命令词请开启, 测试 ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH 参数时开启
		}
	}



	private void initRightMenu() {
		Fragment leftMenuFragment = new PersonalFragment();
		setBehindContentView(R.layout.left_menu_frame);
		getSupportFragmentManager().beginTransaction().replace(R.id.id_left_menu_frame, leftMenuFragment).commit();
		SlidingMenu menu = getSlidingMenu();
		menu.setMode(SlidingMenu.LEFT);
		// 设置触摸屏幕的模式
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		menu.setShadowWidthRes(R.dimen.shadow_width);
		menu.setShadowDrawable(R.drawable.shadow);
		// 设置滑动菜单视图的宽度
		menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		// 设置渐入渐出效果的值
		menu.setFadeDegree(0.35f);
		menu.setSecondaryShadowDrawable(R.drawable.shadow);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);//关闭手势滑动
	}


	public void showMenu() {
		getSlidingMenu().showMenu();
	}

	public void closeMenu(){
		getSlidingMenu().toggle();
	}


	/**
	 * 初始化定位
	 *
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private void initLocation() {
		//初始化client
		locationClient = new AMapLocationClient(this.getApplicationContext());
		locationOption = getDefaultOption();
		//设置定位参数
		locationClient.setLocationOption(locationOption);
		// 设置定位监听
		locationClient.setLocationListener(locationListener);

	}

	/**
	 * 默认的定位参数
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private AMapLocationClientOption getDefaultOption() {
		AMapLocationClientOption mOption = new AMapLocationClientOption();
		mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
		mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
		mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
		mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
		mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
		mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
		mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
		AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
		mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
		mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
		mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
		mOption.setGeoLanguage(AMapLocationClientOption.GeoLanguage.DEFAULT);//可选，设置逆地理信息的语言，默认值为默认语言（根据所在地区选择语言）
		return mOption;
	}

	/**
	 * 定位监听
	 */
	AMapLocationListener locationListener = new AMapLocationListener() {
		@Override
		public void onLocationChanged(AMapLocation location) {
			if (null != location) {
				location_x = location.getLatitude();
				location_y = location.getLongitude();
				Gps gps = GpsCorrect.GCJ02_TO_GPS84(new Gps(location.getLatitude(), location.getLongitude()));
				location.setLatitude(gps.lat);
				location.setLongitude(gps.lon);
				unRectifyLocation = location;
				mMyLocationOverlay.setLocation(location);
				mGpsStatusName = location.getProvider(); // + " 2 " + (cnt >= 0 ? cnt : 0);
//			setTitle(); // ？是否是多余的

				mLastSpeed = location.getSpeed();
//			LogFileUtil.saveFileToSDCard("Main:latitude" + loc.getLatitude() + " longitude:" + loc.getLongitude());

				if (location != null && location.getLatitude() != 0) {
					if (isTracked) {
						trackIndex++;
					}
					if (mAutoFollow) {
//					if (mDrivingDirectionUp)
//						if (loc.getSpeed() > 0.5)
//							mMap.setBearing(loc.getBearing());
						// 设置中心为我的位置
						mMap.getController().setCenter(TypeConverter.locationToGeoPoint(location));
						if (isTracked) {
							if (trackIndex == 5) {
								if (Build.VERSION.SDK_INT != 23) {
									mMap.Refresh();
								}
								trackIndex = 0;
							}
						}
					} else {
						if (isTracked) {
							if (trackIndex == 5) {
								if (Build.VERSION.SDK_INT != 23) {
								}
								trackIndex = 0;
							}
						}
						// 地图刷新
						mMap.Refresh();
					}
				}
				setTitle();


				StringBuffer sb = new StringBuffer();
				//errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
				if (location.getErrorCode() == 0) {
					sb.append("定位成功" + "\n");
					sb.append("定位类型: " + location.getLocationType() + "\n");
					sb.append("经    度    : " + location.getLongitude() + "\n");
					sb.append("纬    度    : " + location.getLatitude() + "\n");
					sb.append("精    度    : " + location.getAccuracy() + "米" + "\n");
					sb.append("提供者    : " + location.getProvider() + "\n");

					sb.append("速    度    : " + location.getSpeed() + "米/秒" + "\n");
					sb.append("角    度    : " + location.getBearing() + "\n");
					// 获取当前提供定位服务的卫星个数
					sb.append("星    数    : " + location.getSatellites() + "\n");
					sb.append("国    家    : " + location.getCountry() + "\n");
					sb.append("省            : " + location.getProvince() + "\n");
					sb.append("市            : " + location.getCity() + "\n");
					sb.append("城市编码 : " + location.getCityCode() + "\n");
					sb.append("区            : " + location.getDistrict() + "\n");
					sb.append("区域 码   : " + location.getAdCode() + "\n");
					sb.append("地    址    : " + location.getAddress() + "\n");
					sb.append("兴趣点    : " + location.getPoiName() + "\n");
					//定位完成的时间
//					sb.append("定位时间: " + Utils.formatUTC(location.getTime(), "yyyy-MM-dd HH:mm:ss") + "\n");
				} else {
					//定位失败
					sb.append("定位失败" + "\n");
					sb.append("错误码:" + location.getErrorCode() + "\n");
					sb.append("错误信息:" + location.getErrorInfo() + "\n");
					sb.append("错误描述:" + location.getLocationDetail() + "\n");
				}
				sb.append("***定位质量报告***").append("\n");
				sb.append("* WIFI开关：").append(location.getLocationQualityReport().isWifiAble() ? "开启" : "关闭").append("\n");
				sb.append("* GPS状态：").append(getGPSStatusString(location.getLocationQualityReport().getGPSStatus())).append("\n");
				sb.append("* GPS星数：").append(location.getLocationQualityReport().getGPSSatellites()).append("\n");
				sb.append("* 网络类型：" + location.getLocationQualityReport().getNetworkType()).append("\n");
				sb.append("* 网络耗时：" + location.getLocationQualityReport().getNetUseTime()).append("\n");
				sb.append("****************").append("\n");
				//定位之后的回调时间
//				sb.append("回调时间: " + Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss") + "\n");

				//解析定位结果，
				String result = sb.toString();


//				Log.e("result", result);
			} else {
//				tvResult.setText("定位失败，loc is null");
				Log.e("result", "定位失败，loc is null");
			}
		}
	};

	/**
	 * 获取GPS状态的字符串
	 * @param statusCode GPS状态码
	 * @return
	 */
	private String getGPSStatusString(int statusCode) {
		String str = "";
		switch (statusCode) {
			case AMapLocationQualityReport.GPS_STATUS_OK:
				str = "GPS状态正常";
				break;
			case AMapLocationQualityReport.GPS_STATUS_NOGPSPROVIDER:
				str = "手机中没有GPS Provider，无法进行GPS定位";
				break;
			case AMapLocationQualityReport.GPS_STATUS_OFF:
				str = "GPS关闭，建议开启GPS，提高定位质量";
				break;
			case AMapLocationQualityReport.GPS_STATUS_MODE_SAVING:
				str = "选择的定位模式中不包含GPS定位，建议选择包含GPS定位的模式，提高定位质量";
				break;
			case AMapLocationQualityReport.GPS_STATUS_NOGPSPERMISSION:
				str = "没有GPS定位权限，建议开启gps定位权限";
				break;
		}
		return str;
	}

	// 根据控件的选择，重新设置定位参数
	private void resetOption() {
		// 设置是否需要显示地址信息
		locationOption.setNeedAddress(true);
		/**
		 * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
		 * 注意：只有在高精度模式下的单次定位有效，其他方式无效
		 */
		locationOption.setGpsFirst(true);
		// 设置是否开启缓存
		locationOption.setLocationCacheEnable(false);
		// 设置是否单次定位
		locationOption.setOnceLocation(false);
		//设置是否等待设备wifi刷新，如果设置为true,会自动变为单次定位，持续定位时不要使用
		locationOption.setOnceLocationLatest(false);
		//设置是否使用传感器
		locationOption.setSensorEnable(true);
		//设置是否开启wifi扫描，如果设置为false时同时会停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
		String strInterval = "1000";
		if (!TextUtils.isEmpty(strInterval)) {
			try {
				// 设置发送定位请求的时间间隔,最小值为1000，如果小于1000，按照1000算
				locationOption.setInterval(Long.valueOf(strInterval));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		String strTimeout = "30000";
		if (!TextUtils.isEmpty(strTimeout)) {
			try {
				// 设置网络请求超时时间
				locationOption.setHttpTimeOut(Long.valueOf(strTimeout));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 开始定位
	 *
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private void startLocation() {
		//根据控件的选择，重新设置定位参数
		resetOption();
		// 设置定位参数
		locationClient.setLocationOption(locationOption);
		// 启动定位
		locationClient.startLocation();
	}

	/**
	 * 停止定位
	 *
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private void stopLocation() {
		// 停止定位
		locationClient.stopLocation();
	}

	private void destroyLocation() {
		if (null != locationClient) {
			/**
			 * 如果AMapLocationClient是在当前Activity实例化的，
			 * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
			 */
			locationClient.onDestroy();
			locationClient = null;
			locationOption = null;
		}
	}

	/**
	 * DENSITY_LOW = 120
	 * DENSITY_MEDIUM = 160  //默认值  
	 * DENSITY_TV = 213      //TV专用  
	 * DENSITY_HIGH = 240  
	 * DENSITY_XHIGH = 320  
	 * DENSITY_400 = 400  
	 * DENSITY_XXHIGH = 480  
	 * DENSITY_XXXHIGH = 640 
	 */
	private void initMap() {
		int mapSize = 256;
		int screenDensity = getResources().getDisplayMetrics().densityDpi;
		switch (screenDensity) {
			case DisplayMetrics.DENSITY_LOW:
				mapSize = 120;
				break;
			case DisplayMetrics.DENSITY_MEDIUM:
				mapSize = 160;
				break;
			case DisplayMetrics.DENSITY_HIGH:
				mapSize = 240;
				break;
			default:
				mapSize = 320;
				break;
		}
		TileSourceBase.MAPTILE_SIZEPX = getData("tilesize", mapSize);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		final String queryAction = intent.getAction();
		if (ACTION_SHOW_POINTS.equalsIgnoreCase(queryAction)) {
			ActionShowPoints(intent);
		} else if ("SHOW_MAP_ID".equalsIgnoreCase(queryAction)) {
			final Bundle bundle = intent.getExtras();
			mMapId = bundle.getString(MAPNAME);
			if (bundle.containsKey("center")) {
				try {
					final GeoPoint geo = GeoPoint.fromDoubleString(bundle.getString("center"));
					mMap.getController().setCenter(geo);
				} catch (Exception e) {
				}
			}
			if (bundle.containsKey("zoom")) {
				try {
					final int zoom = Integer.valueOf(bundle.getString("zoom"));
					mMap.getController().setZoom(zoom);
					SharedPreferences uiState = getPreferences(Activity.MODE_PRIVATE);
					Editor editor = uiState.edit();
					editor.commit();
					editor.putInt("ZoomLevel", mMap.getZoomLevel());
				} catch (Exception e) {
				}
			}
		}
	}

	// 加载地图布局
	private void CreateContentView() {
		setContentView(R.layout.main);

		// 获取布局状态
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		final int sideBottom = Integer.parseInt(pref.getString("pref_zoomctrl", "1"));
		final boolean showTitle = pref.getBoolean("pref_showtitle", true);
		final boolean showAutoFollow = pref.getBoolean("pref_show_autofollow_button", true);

		if (!showTitle)
			findViewById(R.id.screen).setVisibility(View.GONE);
		// 地图
		mMap = (MapView) findViewById(R.id.map_area);
		iv_person = (ImageView) findViewById(R.id.iv_person);
		iv_search = (ImageView) findViewById(R.id.iv_search);
		ll_search = (LinearLayout) findViewById(R.id.ll_search);
		checkBox = (AppCompatCheckBox) findViewById(R.id.map_workarea);
		iv_person.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showMenu();
			}
		});

		checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked){
					addWorkArea();
					FillOverlays();
					mMap.Refresh();
				}else {
					mWorkAreaOverlay = null;
					FillOverlays();
					mMap.Refresh();
				}
			}
		});

		// 登录显示
		tv_is_login = (ImageView) findViewById(R.id.tv_is_login);
/*		tv_is_login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!DataProcess.GetInstance().isConnected()) {
					startService(new Intent(MainActivity.this, SendService.class));
				} else {
					if (DataProcess.GetInstance().isConnected()) {
						if (DataProcess.isLoginDscloud) {
							new KuiTanTask().execute("");
						}
					}
				}
			}
		});*/

		iv_search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startSearchAround();
			}
		});
		refreshViews();
		/*map_zoom_in = (ImageView) findViewById(R.id.map_zoom_in);
		map_zoom_in.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mMap.getTileView().setZoomLevel(mMap.getTileView().getZoomLevel() + 1);
				if (mMoveListener != null)
					mMoveListener.onZoomDetected();
			}
		});
		map_zoom_in.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
				final int zoom = Integer.parseInt(pref.getString("pref_zoommaxlevel", "17"));
				if (zoom > 0) {
					mMap.getTileView().setZoomLevel(zoom - 1);
					if (mMoveListener != null)
						mMoveListener.onZoomDetected();
				}
				return true;
			}
		});
		map_zoom_full = (ImageView) findViewById(R.id.map_zoom_full);
		map_zoom_full.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (SysConfig.isOnlineMap) {
					mMap.getController().setZoom(12);
					mMap.Refresh();
				} else {
					String name = mTileSource.ID;
					try {
						File folder = Ut.getRMapsMapsDir(MainActivity.this);
						SQLiteMapDatabase cacheDatabase = new SQLiteMapDatabase();
						cacheDatabase.setFile(folder.getAbsolutePath() + "/" + name);
						int[] zooms = cacheDatabase.getZoom();
						if (zooms != null) {
							TileProviderFileBase provider = new TileProviderFileBase(MainActivity.this);
							mMap.getController().setZoom(zooms[0]);
							mMap.Refresh();
							provider.Free();
						}
					} catch (Exception e) {
					}
				}

			}
		});
		map_zoom_out = (ImageView) findViewById(R.id.map_zoom_out);
		map_zoom_out.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mMap.getTileView().setZoomLevel(mMap.getTileView().getZoomLevel() - 1);
				if (mMoveListener != null)
					mMoveListener.onZoomDetected();
			}
		});
		map_zoom_out.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
				final int zoom = Integer.parseInt(pref.getString("pref_zoomminlevel", "10"));
				if (zoom > 0) {
					mMap.getTileView().setZoomLevel(zoom - 1);
					if (mMoveListener != null)
						mMoveListener.onZoomDetected();
				}
				return true;
			}
		});*/
		map_location = (ImageView) findViewById(R.id.map_location);
		map_location.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setAutoFollow(true);
				setLastKnownLocation();
			}
		});
		map_location.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				setAutoFollow(true);
				setLastKnownLocation();

				if (unRectifyLocation != null) {
					PoiPoint point = new PoiPoint();
					point.Title = "定位";
					point.GeoPoint = GeoPoint.fromDouble(unRectifyLocation.getLatitude(), unRectifyLocation.getLongitude());
				}
				return false;
			}
		});
		map_menu_search = (TextView) findViewById(R.id.map_menu_search);
		map_menu_search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, SearchActivity.class);
				startActivityForResult(intent, RequestCode.SEARCH);
			}
		});

		// 注册上下文菜单
		registerForContextMenu(mMap);
	}

	public void refreshViews() {
	/*	if (DataProcess.isLoginDscloud) {
			tv_is_login.setImageResource(R.drawable.online_m);
		} else {
			tv_is_login.setImageResource(R.drawable.offline_m);
		}*/

		if (SysConfig.workType == WorkType.WORK_TYPE_NONE) {
			tv_is_login.setVisibility(View.GONE);
		} else {
			tv_is_login.setVisibility(View.VISIBLE);
		}
	}

	public static boolean isTracked = false;

	// 加载所有图层
	private void FillOverlays() {
		this.mMap.getOverlays().clear();
		// 地图底图
		if (mTileOverlay != null)
			this.mMap.getOverlays().add(mTileOverlay);

		// 定位图层
		this.mMap.getOverlays().add(mMyLocationOverlay);
		// 工序图层
		if (SysConfig.workType == WorkType.WORK_TYPE_SHOT) {
			if (mShotPointOverlay != null) {
				this.mMap.getOverlays().add(mShotPointOverlay);
			}
		}else if (SysConfig.workType == WorkType.WORK_TYPE_DRILE){
			if (mDrillPointOverlay != null) {
				this.mMap.getOverlays().add(mDrillPointOverlay);
			}
		}else if (SysConfig.workType == WorkType.WORK_TYPE_ARRANGE){
			if (mArrangePointOverlay != null){
				this.mMap.getOverlays().add(mArrangePointOverlay);
			}
		}
		if (mWorkAreaOverlay!=null){
			this.mMap.getOverlays().add(mWorkAreaOverlay);
		}

	}

	private void setAutoFollow(boolean autoFollow) {
		setAutoFollow(autoFollow, false);
	}

	private void setAutoFollow(boolean autoFollow, final boolean supressToast) {
		mAutoFollow = autoFollow;
	}

	/**
	 * 获取我的位置，设置我的位置在地图中心
	 *
	 */
	private void setLastKnownLocation() {
		final GeoPoint p = mMyLocationOverlay.getLastGeoPoint();
		if (p != null)
			mMap.getController().setCenter(p);
		else {
			// 网络、GPS同时定位
			final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				// TODO: Consider calling
				//    ActivityCompat#requestPermissions
				// here to request the missing permissions, and then overriding
				//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
				//                                          int[] grantResults)
				// to handle the case where the user grants the permission. See the documentation
				// for ActivityCompat#requestPermissions for more details.
				return;
			}
			final Location loc1 = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			final Location loc2 = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

			boolean boolGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
			boolean boolNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			String str = "";
			Location loc = null;

			if(loc1 == null && loc2 != null)
				loc = loc2;
			else if (loc1 != null && loc2 == null)
				loc = loc1;
			else if (loc1 == null && loc2 == null)
				loc = null;
			else
				loc = loc1.getTime() > loc2.getTime() ? loc1 : loc2;

			if(boolGpsEnabled){}
			else if(boolNetworkEnabled)
				str = getString(R.string.message_gpsdisabled);
			else if(loc == null)
				str = getString(R.string.message_locationunavailable);
			else
				str = getString(R.string.message_lastknownlocation);

			if (loc == null) {
				str = "当前未定位";
			}
			if(str.length() > 0)
				Toast.makeText(this, str, Toast.LENGTH_LONG).show();

			if(loc != null) {
				mMap.getController().setCenter(TypeConverter.locationToGeoPoint(loc));
//				unRectifyLocation = loc;//新添加的 会导致崩溃
				Log.e("loc", loc.getLongitude()+"||"+loc.getLatitude() );
			}
		}
	}

	private void setTitle(){
		try {
			final TextView leftText = (TextView) findViewById(R.id.left_text);
			if(leftText != null) {
				String overlayName = "";
				if(mMap.getTileSource() != null && mMap.getTileSource().MAP_TYPE != TileSourceBase.MIXMAP_PAIR)
					if(mMap.getTileSource().getTileSourceBaseOverlay() != null)
						overlayName = " / " + mMap.getTileSource().getTileSourceBaseOverlay().NAME;
				leftText.setText(mMap.getTileSource().NAME + overlayName);
			}

			final TextView gpsText = (TextView) findViewById(R.id.gps_text);
			if(gpsText != null){
				gpsText.setText(mGpsStatusName);
			}

			final TextView rightText = (TextView) findViewById(R.id.right_text);
			if(rightText != null){
				final double zoom = mMap.getZoomLevelScaled();
				if(zoom > mMap.getTileSource().ZOOM_MAXLEVEL) {
					rightText.setText(""+(mMap.getTileSource().ZOOM_MAXLEVEL+1)+"+");
				} else {
					rightText.setText(""+(1 + Math.round(zoom)));
				}
			}
		} catch (Exception e) {
		}
	}

	@Override
	protected void onResume() {
		keyBackClickCount = 0;

		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		final SharedPreferences uiState = getPreferences(Activity.MODE_PRIVATE);

		if (!isChangeMap) {
			if(mMapId == null)
				mMapId = uiState.getString(MAPNAME, TileSource.MAPNIK);
			if (mMapId.endsWith(".sqlitedb")) {
				File file = new File(Ut.getRMapsMapsDir(MainActivity.this).getAbsolutePath()+ "/" + mMapId.substring(8));
				if (!file.exists()) {
					SysConfig.isOnlineMap = true;
					mMapId = TileSource.MAPNIK;
					setData(SysContants.ISONLINEMAP, SysConfig.isOnlineMap);
				}
			}
			mOverlayId = uiState.getString("OverlayID", "");
			mShowOverlay = uiState.getBoolean("ShowOverlay", true);
			setTileSource(mMapId, mOverlayId, mShowOverlay);

			mMap.getController().setZoom(uiState.getInt("ZoomLevel", 16));
			setTitle();
			mMapId = null;
		} else {
			isChangeMap = false;
		}

		refreshViews();
		FillOverlays();
		mOrientationSensorManager.registerListener(mListener, mOrientationSensorManager
				.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
		if (pref.getBoolean("pref_keepscreenon", true)) {
			myWakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(
					PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "RMaps");
			myWakeLock.acquire();
		} else {
			myWakeLock = null;
		}

		DataProcess.GetInstance().setMsgHandler(mMainMsgHandler);
		super.onResume();
	}



	public interface MainMSG {
		public int MSG_SHOW_TASK_STOP = 0x041;
		public int MSG_UPDATA_TOP_TASK_DATA = 0x043;
		public int MSG_IS_KUITAN = 0x044;
		public int MSG_KUITAN_FAIL = 0x045;
	}
	private Handler mMainMsgHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case DataProcess.MSG.LOGIN:
					boolean loginStatus = (Boolean) msg.obj;
					if (DataProcess.isLoginDscloud) {
						tv_is_login.setImageResource(R.drawable.online_m);
					} else {
						tv_is_login.setImageResource(R.drawable.offline_m);
					}
					break;

				default:
					break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onPause() {
		final GeoPoint point = mMap.getMapCenter();

		// 保存地图参数
		SharedPreferences uiState = getPreferences(Activity.MODE_PRIVATE);
		Editor editor = uiState.edit();
		if(mTileSource != null) {
			editor.putString("MapName", mTileSource.ID);
			try {
				editor.putString("OverlayID", mTileOverlay == null ? mTileSource.getOverlayName() : mTileOverlay.getTileSource().ID);
			} catch (Exception e) {
			}
		}
		editor.putBoolean("ShowOverlay", mShowOverlay);
		editor.putInt("Latitude", point.getLatitudeE6());
		editor.putInt("Longitude", point.getLongitudeE6());
		editor.putInt("ZoomLevel", mMap.getZoomLevel());
		editor.putBoolean("CompassEnabled", mCompassEnabled);
		editor.putBoolean("AutoFollow", mAutoFollow);
		editor.putString("app_version", Ut.getAppVersion(this));
//		editor.putString("targetlocation", mMyLocationOverlay.getTargetLocation() == null ? "" : mMyLocationOverlay.getTargetLocation().toDoubleString());
		editor.commit();

		uiState = getSharedPreferences("MapName", Activity.MODE_PRIVATE);
		editor = uiState.edit();
		if(mTileSource != null)
			editor.putString("MapName", mTileSource.ID);
		editor.putInt("Latitude", point.getLatitudeE6());
		editor.putInt("Longitude", point.getLongitudeE6());
		editor.putInt("ZoomLevel", mMap.getZoomLevel());
		editor.putBoolean("CompassEnabled", mCompassEnabled);
		editor.putBoolean("AutoFollow", mAutoFollow);
		editor.commit();

		// 释放资源
		if (myWakeLock != null)
			myWakeLock.release();

		if(mOrientationSensorManager != null)
			mOrientationSensorManager.unregisterListener(mListener);
		if(mTileSource != null)

			mTileSource.Free();
		mTileSource = null;
		mPoiManager.FreeDatabases();

		if(mTileOverlay != null)
			mTileOverlay.Free();

		asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
		Log.i("ActivityMiniRecog","On pause");
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		for (TileViewOverlay osmvo : mMap.getOverlays())
			osmvo.Free();
		if(mTileSource != null)
			mTileSource.Free();
		mTileSource = null;
		mMap.setMoveListener(null);
//		mTracker.stopSession();
		mThreadPool.shutdown();

		//mSpeechUtilOffline.release();

		if (gpsReceiver != null) {
			unregisterReceiver(gpsReceiver);
		}
		destroyLocation();
		DataProcess.GetInstance().stopConn();
		// 基于SDK集成4.2 发送取消事件
		asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
		if (enableOffline) {
			unloadOfflineEngine(); // 测试离线命令词请开启, 测试 ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH 参数时开启
		}

		// 基于SDK集成5.2 退出事件管理器
		// 必须与registerListener成对出现，否则可能造成内存泄露
		asr.unregisterListener(this);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// 主菜单
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_option_menu, menu);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Menu submenu = menu.findItem(R.id.mapselector).getSubMenu();
		submenu.clear();
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		File folder = Ut.getRMapsMapsDir(MainActivity.this);
		if (folder.exists()) {
			File[] files = folder.listFiles();
			if (files != null)
				for (int i = 0; i < files.length; i++) {
					if (files[i].getName().toLowerCase().endsWith(".mnm")
							|| files[i].getName().toLowerCase().endsWith(".tar")
							|| files[i].getName().toLowerCase().endsWith(".sqlitedb")) {
//						String name = Ut.FileName2ID(files[i].getName());
						String name = files[i].getName();
						if (name.toLowerCase().endsWith("sqlitedb")) {
							String string = pref.getString(TileSourceBase.PREF_USERMAP_ + name + "_name", "");
							if (string == null || "".equals(string)) {
								final Editor editor = pref.edit();
								editor.putBoolean(TileSourceBase.PREF_USERMAP_ + name + "_enabled", true);
								editor.putString(TileSourceBase.PREF_USERMAP_ + name + "_name", (String) name.subSequence(0, name.length() - 9));
								editor.putString(TileSourceBase.PREF_USERMAP_ + name + "_projection", "1");
								editor.putString(TileSourceBase.PREF_USERMAP_ + name + "_baseurl", folder.getAbsolutePath() + "/" + name);
								editor.putBoolean(TileSourceBase.PREF_USERMAP_+name+"_isoverlay", mTileSource.LAYER);
								editor.commit();

								try {
									SQLiteMapDatabase cacheDatabase = new SQLiteMapDatabase();
									cacheDatabase.setFile(folder.getAbsolutePath() + "/" + files[i].getName());
									int[] zooms = cacheDatabase.getZoom();
									if (zooms != null) {
										TileProviderFileBase provider = new TileProviderFileBase(MainActivity.this);
										provider.CommitIndex(TileSourceBase.PREF_USERMAP_ + name, 0, 0, zooms[0], zooms[1]);
										provider.Free();
									}
								} catch (Exception e) {
								}
							}
						}
						if (pref.getBoolean("pref_usermaps_" + name + "_enabled", false) && !pref.getBoolean("pref_usermaps_" + name + "_isoverlay", false)) {
							MenuItem item = submenu.add(R.id.isoverlay, Menu.NONE, Menu.NONE, pref.getString("pref_usermaps_" + name + "_name", files[i].getName()));
							item.setTitleCondensed("usermap_" + name);
						}
					}
				}
		}

		Cursor c = mPoiManager.getGeoDatabase().getMixedMaps();
		if(c != null) {
			if(c.moveToFirst()) {
				do {
					if (pref.getBoolean("PREF_MIXMAPS_" + c.getInt(0) + "_enabled", true) && c.getInt(2) < 3) {
						MenuItem item = submenu.add(c.getString(1));
						item.setTitleCondensed("mixmap_" + c.getInt(0));
					}
				} while(c.moveToNext());
			}
			c.close();
		}

		final SAXParserFactory fac = SAXParserFactory.newInstance();
		SAXParser parser = null;
		try {
			parser = fac.newSAXParser();
			if(parser != null){
				final InputStream in = getResources().openRawResource(R.raw.predefmaps);
				parser.parse(in, new PredefMapsParser(submenu, pref));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		final GeoPoint point = mMap.getMapCenter();

		if(item.getItemId() == R.id.area_selector) {
			startActivity(new Intent(this, AreaSelectorActivity.class)
					.putExtra("new", true)
					.putExtra(MAPNAME, mTileSource.ID)
					.putExtra("Latitude", point.getLatitudeE6())
					.putExtra("Longitude", point.getLongitudeE6())
					.putExtra("ZoomLevel", mMap.getZoomLevel()));
			return true;
		} else if(item.getItemId() == R.id.downloadprepared) {
			startActivity(new Intent(this, FileDownloadListActivity.class));
			return true;
		}

		else if (item.getItemId() == R.id.poilist) {
			startActivityForResult((new Intent(this, PoiListActivity.class))
					.putExtra("lat", point.getLatitude())
					.putExtra("lon", point.getLongitude())
					.putExtra("title", "POI"), RequestCode.POIS);
			return true;
		} else if (item.getItemId() == R.id.tracks) {
			startActivityForResult(new Intent(this, TrackListActivity.class), RequestCode.TRACKS);
			return true;
		}
		else if (item.getItemId() == R.id.settings) {
			startActivityForResult(new Intent(this, MainPreferences.class), RequestCode.MAPSET);
			return true;
		} else if (item.getItemId() == R.id.task_settings) {
			startActivity(new Intent(this, TaskSettingActivity.class));
			return true;
		} else if (item.getItemId() == R.id.about) {
			showDialog(R.id.about);
			return true;
		} else if (item.getItemId() == R.id.mapselector) {
			return true;
		}
		else if (item.getItemId() == R.id.exit) {
			onPause();
			Process.killProcess(Process.myPid());
			System.exit(10);
			return true;
		} else {
			final String mapid = (String)item.getTitleCondensed();
			setTileSource(mapid, "", true);

			if(mTileSource.MAP_TYPE == TileSource.PREDEF_ONLINE) {
//				mTracker.setCustomVar(1, "MAP", mapid);
//				mTracker.trackPageView("/maps");
			}
			FillOverlays();
			setTitle();

			if (unRectifyLocation != null) {
				Location location = unRectifyLocation;
				mMyLocationOverlay.setLocation(location);
			}

			return true;
		}

	}

	/**
	 * 设置地图瓦片
	 *
	 * @param aMapId
	 * @param aOverlayId
	 * @param aShowOverlay
	 */
	private void setTileSource(String aMapId, String aOverlayId, boolean aShowOverlay) {
		final String mapId = aMapId == null ? (mTileSource == null ? TileSource.MAPNIK : mTileSource.ID) : aMapId;
		final String overlayId = aOverlayId == null ? mOverlayId : aOverlayId;
		final String lastMapID = mTileSource == null ? TileSource.MAPNIK : mTileSource.ID;

		if(mTileSource != null) mTileSource.Free();

		if(overlayId != null && !overlayId.equalsIgnoreCase("") && aShowOverlay) {
			mOverlayId = overlayId;
			mShowOverlay = true;
			try {
				mTileSource = new TileSource(this, mapId, overlayId);

			} catch (RException e) {
				mTileSource = null;
				addMessage(e);
			} catch (Exception e) {
				mTileSource = null;
				addMessage(new RException(R.string.error_other, e.getMessage()));
			}
		} else {
			if(mTileOverlay != null) {
				mTileOverlay.Free();
				mTileOverlay = null;
			}

			try {
				mTileSource = new TileSource(this, mapId, aShowOverlay);

				mShowOverlay = aShowOverlay;
				if(mapId != lastMapID)
					mOverlayId = "";
			} catch (RException e) {
				mTileSource = null;
				addMessage(e);
			} catch (Exception e) {
				mTileSource = null;
				addMessage(new RException(R.string.error_other, e.getMessage()));
			}
		}

		if(mTileSource != null) {
			final TileSource tileSource = mTileSource.getTileSourceForTileOverlay();
			if(tileSource != null) {
				if(mTileOverlay == null)
					mTileOverlay = new TileOverlay(mMap.getTileView(), true);
				mTileOverlay.setTileSource(tileSource);
			} else if(mTileOverlay != null) {
				mTileOverlay.Free();
				mTileOverlay = null;
			}
		} else {
			try {
				mTileSource = new TileSource(this, TileSource.MAPNIK);
			} catch (SQLiteException e) {
			} catch (RException e) {
			}
		}

		mMap.setTileSource(mTileSource);
		FillOverlays();

		if(mMyLocationOverlay != null && mTileSource != null)
			mMyLocationOverlay.correctScale(mTileSource.MAPTILE_SIZE_FACTOR, mTileSource.GOOGLESCALE_SIZE_FACTOR);

	}

	/**
	 * 显示Error
	 *
	 * @param e
	 */
	private void addMessage(RException e) {

		LogFileUtil.saveFileToSDCard(e.getStringRes(this));

	}

	PopupWindow popupWindow;
	private Animation animation;
	private CheckRecord checkRecord;
	private DrillRecord drillRecord;
	public void showPopuwindow(final TaskPoint shotPoint){
		popupWindow = new PopupWindow();
		popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		popupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.pop_up);
		animation.setFillAfter(true);//android动画结束后停在结束位置
		View view = LayoutInflater.from(this).inflate(R.layout.layout_popup_window,null);
		popupWindow.setContentView(view);
		popupWindow.showAtLocation(findViewById(R.id.tv_main),Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,0,0);
		view.startAnimation(animation);

		TextView tv_stationNo = view.findViewById(R.id.tv_stationNo);
		TextView tv_lon = view.findViewById(R.id.tv_lon);
		TextView tv_lat = view.findViewById(R.id.tv_lat);
		ImageView tv_leave = view.findViewById(R.id.tv_leave);
		ImageView iv_go_check = view.findViewById(R.id.tv_go_check);
		ImageView iv_guide = view.findViewById(R.id.tv_guide);
		ImageView iv_video = view.findViewById(R.id.iv_video);
		TextView tv_name = view.findViewById(R.id.tv_name);
		TextView tv_time = view.findViewById(R.id.tv_time);
		TextView tv_status = view.findViewById(R.id.tv_status);
		TextView tv_reason = view.findViewById(R.id.tv_reason);
		TextView tv_depth = view.findViewById(R.id.tv_depth);
		TextView tv_upload = view.findViewById(R.id.tv_upload);
		ImageView iv_see_check = view.findViewById(R.id.tv_see_check);
		LinearLayout ll_info = view.findViewById(R.id.ll_info);
		LinearLayout ll_video = view.findViewById(R.id.ll_video);
		LinearLayout ll_status = view.findViewById(R.id.ll_status);
		LinearLayout ll_reason = view.findViewById(R.id.ll_reason);
		LinearLayout ll_depth_video = view.findViewById(R.id.ll_depth_video);
		boolean isDone = false;
		if (SysConfig.workType == WorkType.WORK_TYPE_DRILE){
			isDone = mPointDBDao.selectDrillPoint(shotPoint.stationNo).isDone;
			if (isDone){
				drillRecord = mPointDBDao.selectDrillRecord(shotPoint.stationNo).get(0);
				ll_info.setVisibility(View.VISIBLE);
				ll_status.setVisibility(View.VISIBLE);
				iv_see_check.setVisibility(View.GONE);
				iv_go_check.setVisibility(View.GONE);
				ll_video.setVisibility(View.VISIBLE);
				ll_depth_video.setVisibility(View.VISIBLE);
				tv_name.setText(drillRecord.name);
				tv_time.setText(drillRecord.drilltime);
				tv_depth.setText(drillRecord.drilldepth+"米");
				if (drillRecord.isupload.equals("0")){
					tv_upload.setText("已上传");
				}else if (drillRecord.isupload.equals("1")){
					tv_upload.setText("未上传");
				}
				switch (drillRecord.status){
					case 0:
						tv_status.setText("合格");
						break;
					case 1:
						ll_reason.setVisibility(View.VISIBLE);
						tv_reason.setText(drillRecord.remark);
						tv_status.setText("不合格");
						break;
				}

				ll_video.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (drillRecord.video!=null){
							Intent intent = new Intent(MainActivity.this,VideoActivity.class);
							intent.putExtra("path",drillRecord.video);
							startActivity(intent);
						}else {
							Toast.makeText(MainActivity.this,"此任务无录像",Toast.LENGTH_SHORT).show();
						}
					}
				});
			}
		}else if (SysConfig.workType == WorkType.WORK_TYPE_ARRANGE){
			isDone = mPointDBDao.selectArrangePoint(shotPoint.stationNo).isDone;
			if (isDone){
				iv_video.setVisibility(View.VISIBLE);
				checkRecord = mPointDBDao.selectCheckRecord(shotPoint.stationNo);
				Log.e("checkRecord", "checkRecord: "+checkRecord.toString() );
				ll_info.setVisibility(View.VISIBLE);
				ll_status.setVisibility(View.VISIBLE);
				iv_see_check.setVisibility(View.VISIBLE);
				iv_go_check.setVisibility(View.GONE);
				tv_name.setText(checkRecord.getName());
				tv_time.setText(checkRecord.getTime());
				switch (checkRecord.getStatus()){
					case 0:
						tv_status.setText("合格");
						break;
					case 1:
						ll_reason.setVisibility(View.VISIBLE);
						tv_reason.setText(checkRecord.getRemark());
						tv_status.setText("不合格");
						break;
				}
				iv_video.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (checkRecord.getVideo()!=null){
							Intent intent = new Intent(MainActivity.this,VideoActivity.class);
							intent.putExtra("path",checkRecord.getVideo());
							startActivity(intent);
						}else {
							Toast.makeText(MainActivity.this,"此任务无录像",Toast.LENGTH_SHORT).show();
						}

					}
				});
			}
		}
		tv_stationNo.setText(shotPoint.stationNo);
		tv_lon.setText(shotPoint.geoPoint.getLongitude()+"");
		tv_lat.setText(shotPoint.geoPoint.getLatitude()+"");
		tv_leave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				popupWindow.dismiss();
			}
		});

		iv_guide.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Message message = new Message();
				message.what = RequestCode.TASK_GUIDE;
				message.obj = shotPoint;
				mCallbackHandler.sendMessage(message);
				if (popupWindow!=null){
					popupWindow.dismiss();
				}
			}
		});


		iv_go_check.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				int dis;
				dis = (int) GeoPoint.fromDouble(unRectifyLocation.getLatitude(),unRectifyLocation.getLongitude()).distanceTo(shotPoint.geoPoint);
//					Toast.makeText(MainActivity.this,"距离目标 "+dis+"米 "+"请到达目标点再试",Toast.LENGTH_SHORT).show();
					Intent intent = null;
					if (SysConfig.workType == WorkType.WORK_TYPE_DRILE){
						intent = new Intent(MainActivity.this, DrillRecognizeActivity.class);
//						intent = new Intent(MainActivity.this, DetectorActivity.class);
					}else if (SysConfig.workType == WorkType.WORK_TYPE_ARRANGE){
						intent = new Intent(MainActivity.this, RecognizeActivity.class);
					}
					intent.putExtra("stationNo",shotPoint.stationNo);
					intent.putExtra("isRecord",getData(SysContants.RECORD,false));
					if (dis>10){
						intent.putExtra("distance",dis);
					}
					startActivityForResult(intent, RequestCode.CHECKED);
					if (popupWindow!=null){
						popupWindow.dismiss();
					}

			}
		});

		iv_see_check.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,SeeCheckActivity.class);
				ArrayList<String> paths = new ArrayList<>();
				paths.add(checkRecord.getImage1());
				paths.add(checkRecord.getImage2());
				paths.add(checkRecord.getImage3());
				intent.putStringArrayListExtra("paths",paths);
				startActivity(intent);
			}
		});
	}

	/**
	 * 兴趣点menu
	 * 这里就是点击事件
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (!BottomTaskPaoDianFragment.isInLine) {
			if(menuInfo instanceof TileView.ShotMenuInfo) {
				final TileView.ShotMenuInfo info = (TileView.ShotMenuInfo) menuInfo;
				if(info.EventGeoPoint != null) {
					if(info.MarkerIndex > ShotPointOverlay.NO_TAP) {
						TaskPoint shotPoint = mPointDBDao.selectShotPoint(info.MarkerIndex);
						showPopuwindow(shotPoint);
						mMap.getTileView().mShotMenuInfo.EventGeoPoint = null;
					}
				}
			}else if(menuInfo instanceof TileView.DrillMenuInfo) {
				final TileView.DrillMenuInfo info = (TileView.DrillMenuInfo) menuInfo;
				if(info.EventGeoPoint != null) {
					if(info.MarkerIndex > DrillPointOverlay.NO_TAP) {
						TaskPoint drillPoint = mPointDBDao.selectDrillPoint(info.MarkerIndex);
						showPopuwindow(drillPoint);
						mMap.getTileView().mDrillMenuInfo.EventGeoPoint = null;
					}
				}
			}else if (menuInfo instanceof TileView.ArrangeMenuInfo){
				final TileView.ArrangeMenuInfo info = (TileView.ArrangeMenuInfo) menuInfo;
				if(info.EventGeoPoint != null) {
					if(info.MarkerIndex > ArrangePointOverlay.NO_TAP) {
						TaskPoint shotPoint = mPointDBDao.selectArrangePoint(info.MarkerIndex);
						showPopuwindow(shotPoint);
						mMap.getTileView().mArrangeMenuInfo.EventGeoPoint = null;
					}
				}
			}
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getGroupId() == R.id.isoverlay) {

			final String mapid = (String)item.getTitleCondensed();
			setTileSource(mapid, "", true);

			if(mTileSource.MAP_TYPE == TileSource.PREDEF_ONLINE) {
			}
			FillOverlays();
			setTitle();

			if (unRectifyLocation != null) {
				Location location = unRectifyLocation;
				mMyLocationOverlay.setLocation(location);
			}

		}

		final ContextMenuInfo menuInfo = item.getMenuInfo();
		if(menuInfo != null && menuInfo instanceof TileView.PoiMenuInfo) {
			((TileView.PoiMenuInfo) menuInfo).EventGeoPoint = null;
		} else if(menuInfo != null && menuInfo instanceof TileView.DrillMenuInfo) {
			((TileView.DrillMenuInfo) menuInfo).EventGeoPoint = null;
		} else if(menuInfo != null && menuInfo instanceof TileView.ShotMenuInfo) {
			((TileView.ShotMenuInfo) menuInfo).EventGeoPoint = null;
		}else if (menuInfo != null && menuInfo instanceof TileView.ArrangeMenuInfo){
			((TileView.ArrangeMenuInfo) menuInfo).EventGeoPoint = null;
		}

		return super.onContextItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RequestCode.STOP_GUIDING){
			Log.e("guidName", "guidName" );
			new Thread(new Runnable() {
				@Override
				public void run() {
					Message msg = new Message();
					msg.what = RequestCode.STOP_GUIDE;
					msg.obj = guidName;
					mCallbackHandler.sendMessage(msg);
				}
			}).start();
		}else if (resultCode==200){
			closeMenu();
			String stationNo = data.getStringExtra("stationNo");
			Intent intentx = new Intent(ACTION_POINT_LOC);
			intentx.putExtra("stationNo",stationNo);
			sendBroadcast(intentx);
		}else if (requestCode == RequestCode.SEARCH){
			if (data!=null){
				String satation = data.getStringExtra("shot");
				if (SysConfig.workType ==  WorkType.WORK_TYPE_SHOT){
					TaskPoint taskPoint = mPointDBDao.selectShotPointTotaskPoint(satation);
					mMap.getController().setCenter(taskPoint.geoPoint);
					mMap.getTileView().mShotMenuInfo.EventGeoPoint = taskPoint.geoPoint;
					mMap.getTileView().mShotMenuInfo.MarkerIndex = taskPoint.Id;
					mMap.getTileView().mShotMenuInfo.stationNo = taskPoint.stationNo;
					mShotPointOverlay.setTapIndex(taskPoint.Id);
				}else if (SysConfig.workType ==  WorkType.WORK_TYPE_DRILE){
					TaskPoint taskPoint = mPointDBDao.selectDrillPoint(satation);
					mMap.getController().setCenter(taskPoint.geoPoint);
					mMap.getTileView().mDrillMenuInfo.EventGeoPoint = taskPoint.geoPoint;
					mMap.getTileView().mDrillMenuInfo.MarkerIndex = taskPoint.Id;
					mMap.getTileView().mDrillMenuInfo.stationNo = taskPoint.stationNo;
					mDrillPointOverlay.setTapIndex(taskPoint.Id);

				}else if (SysConfig.workType ==  WorkType.WORK_TYPE_ARRANGE){
					TaskPoint taskPoint = mPointDBDao.selectArrangePoint(satation);
					mMap.getController().setCenter(taskPoint.geoPoint);
					mMap.getTileView().mArrangeMenuInfo.EventGeoPoint = taskPoint.geoPoint;
					mMap.getTileView().mArrangeMenuInfo.MarkerIndex = taskPoint.Id;
					mMap.getTileView().mArrangeMenuInfo.stationNo = taskPoint.stationNo;
					mArrangePointOverlay.setTapIndex(taskPoint.Id);
				}
				mMap.getTileView().showContextMenu();
			}

		}else if (requestCode == RequestCode.CHECKED){
			if (resultCode == RESULT_OK){
				String stationNo = data.getStringExtra("stationNo");
				if (stationNo!=null && SysConfig.workType ==  WorkType.WORK_TYPE_SHOT){
					ShotPoint shotPoint = mPointDBDao.selectShotPoint(stationNo);
					shotPoint.isDone = true;
					mPointDBDao.updateShotPoint(shotPoint);
					mShotPointOverlay.UpdateList();
					mMap.Refresh();
				}else if (stationNo!=null && SysConfig.workType ==  WorkType.WORK_TYPE_DRILE){
					DrillPoint drillPoint = mPointDBDao.selectDrillPoint(stationNo);
					drillPoint.isDone = true;
					mPointDBDao.updateDrillPoint(drillPoint);
					mDrillPointOverlay.UpdateList();
					mMap.Refresh();
				}else if (stationNo!=null && SysConfig.workType ==  WorkType.WORK_TYPE_ARRANGE){
					ArrangePoint arrangePoint = mPointDBDao.selectArrangePoint(stationNo);
					arrangePoint.isDone = true;
					mPointDBDao.updateArrangePoint(arrangePoint);
					mArrangePointOverlay.UpdateList();
					mMap.Refresh();
				}
				//周边查询
				startSearchAround();
			}

		}
	}
	Dialog dialog;
	private class MainActivityCallbackHandler extends Handler {
		@Override
		public void handleMessage(final Message msg) {
			final int what = msg.what;
			if (what == Ut.MAPTILEFSLOADER_SUCCESS_ID) {
				mMap.Refresh();
			} else if (what == R.id.user_moved_map) {
				// setAutoFollow(false);
			} else if (what == R.id.set_title) {
				setTitle();
			} else if (what == R.id.add_yandex_bookmark) {
				showDialog(R.id.add_yandex_bookmark);
			} else if (what == Ut.ERROR_MESSAGE) {
				if (msg.obj != null)
					Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
			}  else if (what == RequestCode.TASK_GUIDE) {
				if (msg.obj != null) {
					TaskPoint taskPoint = (TaskPoint) msg.obj;
					lastPoint = taskPoint.geoPoint;
					NaviLatLng mStartLatlng = new NaviLatLng(location_x,location_y);
					Gps endGps = GpsCorrect.GPS84_TO_GCJ02(new Gps(lastPoint.getLatitude(),lastPoint.getLongitude()));
					NaviLatLng mEndLatlng = new NaviLatLng(endGps.lat,endGps.lon);
					final Intent intent = new Intent(MainActivity.this, RouteNaviActivity.class);
					intent.putExtra("mStartLatlng",mStartLatlng);
					intent.putExtra("mEndLatlng",mEndLatlng);
					intent.putExtra("guidName",taskPoint.stationNo);
					guidName = taskPoint.stationNo;
					DrillPoint drillPoint = mPointDBDao.selectDrillPoint(guidName);
					ArrangePoint arrangePoint = mPointDBDao.selectArrangePoint(guidName);
					dialog = DialogUtils.Alert(MainActivity.this, "提示", "选择出行方式？",
							new String[]{"驾车", "步行"},
							new OnClickListener[]{new OnClickListener() {

								@Override
								public void onClick(View v) {
									intent.putExtra("flag",0);
//									startActivityForResult(intent,RequestCode.STOP_GUIDING);
									startActivity(intent);
									dialog.dismiss();
								}
							}, new OnClickListener() {

								@Override
								public void onClick(View v) {
									intent.putExtra("flag",1);
//									startActivityForResult(intent,RequestCode.STOP_GUIDING);
									startActivity(intent);
									dialog.dismiss();
								}
							}
							});
					dialog.show();
					lastPoint = null;
				}
			} else if (what == RequestCode.SHOTPOINT_ISDONE) {
				if (msg.obj != null && !"".equals(msg.obj)) {
					ShotPoint shotPoint = mPointDBDao.selectShotPoint((String) msg.obj);
					shotPoint.isDone = true;
					mPointDBDao.updateShotPoint(shotPoint);
					mShotPointOverlay.UpdateList();
					mMap.Refresh();
				}
			} else if (what == UPDATA_NONEED) {
				Toast.makeText(getApplicationContext(), "不需要更新",
						Toast.LENGTH_SHORT).show();
			}else if (what == UPDATA_CLIENT) {
				showUpdataDialog();
			}else if (what == GET_UNDATAINFO_ERROR) {
				//服务器超时
//				Toast.makeText(getApplicationContext(), "获取服务器更新信息失败", Toast.LENGTH_LONG).show();
				Log.e("GET_UNDATAINFO_ERROR", "GET_UNDATAINFO_ERROR: "+"获取服务器更新信息失败");
			}else if (what == DOWN_ERROR) {
				//下载apk失败
				Toast.makeText(getApplicationContext(), "下载新版本失败", Toast.LENGTH_LONG).show();
				pd.dismiss();
			}
		}
	}

	boolean flag = false;
	public void initCredit() throws IOException {
		// TODO Auto-generated method stub
		//1>创建HttpClient对象
		HttpClient client=new DefaultHttpClient();
		//2>创建HttpGet请求对象
		String urls= SysConfig.url+"credit/taskdone/"+getData(SysContants.TEL,"");
		HttpGet get=new HttpGet(urls);
		//3>execute
		HttpResponse resp=client.execute(get);
		//4>解析HttpResponse
		StatusLine line = resp.getStatusLine();
		int code=line.getStatusCode();
		if(code==200){ //正常返回
			//获取响应数据包的实体部分
			HttpEntity entity=resp.getEntity();
			String xml= EntityUtils.toString(entity);
			Log.e("xml", xml );
			flag = true;
		}else{ //不正常
			flag = false;
			Log.e("info", "请求发送失败...状态码不是200，是:"+code);
		}
	}


	public static String GPGGA = "";
	public static String GNGGA = "";
	public static String GPGSA = "";
	public static Location unRectifyLocation;
	public static int trackIndex = 0;
	public static final String[] INITALL_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};
	double location_x;
	double location_y;

	private class MoveListener implements IMoveListener {

		public void onMoveDetected() {
			if(mAutoFollow)
				setAutoFollow(false);
		}

		public void onZoomDetected() {
			setTitle();
		}

		@Override
		public void onCenterDetected() {
		}

	}


	private float updateBearing(float newBearing) {
		float dif = newBearing - mLastBearing;
		// find difference between new and current position
		if (Math.abs(dif) > 180)
			dif = 360 - dif;
		// if difference is bigger than 180 degrees,
		// it's faster to rotate in opposite direction
		if (Math.abs(dif) < 1)
			return mLastBearing;
		// if difference is less than 1 degree, leave things as is
		if (Math.abs(dif) >= 90)
			return mLastBearing = newBearing;
		// if difference is bigger than 90 degress, just update it
		mLastBearing += 90 * Math.signum(dif) * Math.pow(Math.abs(dif) / 90, 2);
		// bearing is updated proportionally to the square of the difference
		// value
		// sign of difference is paid into account
		// if difference is 90(max. possible) it is updated exactly by 90
		while (mLastBearing > 360)
			mLastBearing -= 360;
		while (mLastBearing < 0)
			mLastBearing += 360;
		// prevent bearing overrun/underrun
		return mLastBearing;
	}

	/**
	 * 显示点
	 *
	 * @param queryIntent
	 */
	private void ActionShowPoints(Intent queryIntent) {
		final ArrayList<String> locations = queryIntent.getStringArrayListExtra("locations");
		if(!locations.isEmpty()){
			GeoPoint point = null;
			int id = -1;
			Iterator<String> it = locations.iterator();
			while(it.hasNext()) {
				final String [] fields = it.next().split(";");
				String locns = "", title = "", descr = "";
				if(fields.length > 0) locns = fields[0];
				if(fields.length > 1) title = fields[1];
				if(fields.length > 2) descr = fields[2];

				point = GeoPoint.fromDoubleString(locns);
			}
			setAutoFollow(false);
			if(point != null)
				mMap.getController().setCenter(point);
		}
	}

	private int keyBackClickCount = 0;

	@Override
	public void onBackPressed() {
		if (SearchResultOverlay.mSearchGeoPoints != null
				&& SearchResultOverlay.mSearchGeoPoints.size() > 0) {
			SearchResultOverlay.mSearchDescrs.clear();
			SearchResultOverlay.mSearchGeoPoints.clear();
		}

		if(popupWindow!=null&&popupWindow.isShowing()){
			popupWindow.dismiss();
		}
		mDrillPointOverlay.setTapIndex(DrillPointOverlay.NO_TAP);
		mMap.Refresh();

		switch (keyBackClickCount++) {
			case 0:
				Toast.makeText(this,
						getResources().getString(R.string.press_again_exit),
						Toast.LENGTH_SHORT).show();
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						keyBackClickCount = 0;
					}
				}, 2000);
				break;
			case 1:
				setData(SysContants.WORK_TYPE, SysConfig.workType);
				setData(SysContants.ISFIRST, true);
				super.onBackPressed();
				break;
			default:
				break;
		}

	}

	private ProgressDialog dlgWait;
	private ExecutorService mThreadExecutor = null;
	private GeoPoint lastPoint;
	private ProgressDialog progressDialog = null;
	public class KuiTanTask extends AsyncTask<String, Integer, Boolean> {

		public KuiTanTask() {
		}

		@Override
		protected void onPreExecute() {
			if (progressDialog != null && progressDialog.isShowing()
					&& !MainActivity.this.isFinishing()) {
			} else {
				progressDialog = new ProgressDialog(MainActivity.this);
				progressDialog.setMessage("正在检查...");
				progressDialog.setCancelable(false);
				progressDialog.show();
			}
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
							.addReceivers(ByteString.copyFrom(SysConfig.DSCLOUD, "GB2312"))
							.setReceivers(0, ByteString.copyFrom(SysConfig.DSCLOUD, "GB2312"))
							.setPriority(1).setExpired(0).build();
					if (DataProcess.isLoginDscloud) {
						try {
							return DataProcess.GetInstance().sendData(
									SocketUtils.writeBytes(head.toByteArray(),
											proto_WellShotData.toByteArray()));
						} catch (Exception e) {
							e.printStackTrace();
							if (progressDialog != null
									&& progressDialog.isShowing()
									&& !MainActivity.this.isFinishing()) {
								progressDialog.dismiss();
							}
						}
					}
					mMainMsgHandler.sendEmptyMessageDelayed(MainMSG.MSG_KUITAN_FAIL,
							5000);
				} else {
					mMainMsgHandler.sendEmptyMessageDelayed(MainMSG.MSG_KUITAN_FAIL,
							5000);
					return DataProcess.GetInstance().sendData(
							new RF01(SysConfig.SC_ID));
				}
			} catch (IOException e) {
				e.printStackTrace();
				LogFileUtil.saveFileToSDCard("RF01: " + e.toString());
				if (progressDialog != null && progressDialog.isShowing()
						&& !MainActivity.this.isFinishing()) {
					progressDialog.dismiss();
				}
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (progressDialog != null && progressDialog.isShowing()
					&& !MainActivity.this.isFinishing()) {
				progressDialog.dismiss();
			}
			if (!result) {
			}
		}

	}
	@Override
	public void setTaskEntity(String station) {
		if (SysConfig.workType == WorkType.WORK_TYPE_SHOT) {
			ShotPoint shotPoint = mPointDBDao.selectShotPoint(station);
			mShotPointOverlay.setTapIndex(shotPoint.getId());
			mMap.getController().setCenter(shotPoint.geoPoint);
			mMap.Refresh();
		}else if (SysConfig.workType == WorkType.WORK_TYPE_DRILE) {
			DrillPoint drillPoint = mPointDBDao.selectDrillPoint(station);
			mDrillPointOverlay.setTapIndex(drillPoint.getId());
			mMap.getController().setCenter(drillPoint.geoPoint);
			mMap.Refresh();
		}else if (SysConfig.workType == WorkType.WORK_TYPE_ARRANGE){
			ArrangePoint arrangePoint = mPointDBDao.selectArrangePoint(station);
			mArrangePointOverlay.setTapIndex(arrangePoint.getId());
			mMap.getController().setCenter(arrangePoint.geoPoint);
			mMap.Refresh();
		}
	}

	@Override
	public void ShowMap(String mapId, String center, String zoom) {
		mMapId = mapId;
		if(center != null && !center.equalsIgnoreCase("")){
			try {
				final GeoPoint geo = GeoPoint.fromDoubleString(center);
				mMap.getController().setCenter(geo);
			} catch (Exception e) {
			}
		}
		if(zoom != null && !zoom.equalsIgnoreCase("")){
			try {
				final int mapzoom = Integer.valueOf(zoom);
				mMap.getController().setZoom(mapzoom);
				Editor editor = uiState.edit();
				editor.putInt("ZoomLevel", mMap.getZoomLevel());
				editor.commit();
			} catch (Exception e) {
			}
		}
		setTileSource(mMapId, "", true);
		if(mTileSource.MAP_TYPE == TileSource.PREDEF_ONLINE) {
//			mTracker.setCustomVar(1, "MAP", mMapId);
//			mTracker.trackPageView("/maps");
		}
		FillOverlays();
		setTitle();

		mMap.Refresh();
		isChangeMap = true;
	}

	/*private void gpsIsOpen() {
		mLocationListener.getBestProvider();
	}*/

	//检测版本是否一致
	public class CheckVersionTask implements Runnable {
		InputStream is;
		public void run() {
			try {
				String path = SysConfig.UPDATEURL+"/api/android/version";
				URL url = new URL(path);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setConnectTimeout(5000);
				conn.setRequestMethod("GET");
				int responseCode = conn.getResponseCode();
				if (responseCode == 200) {
					// 从服务器获得一个输入流
					is = conn.getInputStream();
				}
				info = UpdataInfoParser.getUpdataInfo(is);
				Log.e("version", "run: "+info.getVersion() +"||"+localVersion);
				if (info.getVersion().equals(localVersion)) {
					Log.e("version", "版本号相同");
					Message msg = new Message();
					msg.what = UPDATA_NONEED;
					mCallbackHandler.sendMessage(msg);
					// LoginMain();
				} else {
					Log.e("version", "版本号不相同 ");
					Message msg = new Message();
					msg.what = UPDATA_CLIENT;
					mCallbackHandler.sendMessage(msg);
				}
			} catch (Exception e) {
				Message msg = new Message();
				msg.what = GET_UNDATAINFO_ERROR;
				mCallbackHandler.sendMessage(msg);
				e.printStackTrace();
			}
		}
	}
	private final int UPDATA_NONEED = -1;
	private final int UPDATA_CLIENT = -2;
	private final int GET_UNDATAINFO_ERROR = -3;
	private final int DOWN_ERROR = -4;
	private UpdateInfo info;
	private String localVersion;
	//显示升级对话框
	protected void showUpdataDialog() {
		AlertDialog.Builder builer = new AlertDialog.Builder(this);
		builer.setTitle("版本升级");
		builer.setMessage(info.getDescription());

		builer.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Log.i("version", "下载apk,更新");
				downLoadApk();
			}
		});
		builer.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//do sth
			}
		});
		AlertDialog dialog = builer.create();
		dialog.show();
	}
	//显示下载对话框
	protected void downLoadApk() {
		pd = new  ProgressDialog(this);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setCancelable(false);
		pd.setMessage("正在下载更新");
		pd.show();
		new Thread(){
			@Override
			public void run() {
				try {
					File file = DownLoadManager.getFileFromServer(SysConfig.UPDATEURL+info.getUrl(), pd);
					installApk(file);
					pd.dismiss(); //结束掉进度条对话框
				} catch (Exception e) {
					Message msg = new Message();
					msg.what = DOWN_ERROR;
					mCallbackHandler.sendMessage(msg);
					e.printStackTrace();
				}
			}}.start();
	}

	//安装apk
	protected void installApk(File file) {
		Intent intent = new Intent();
		//执行动作
		intent.setAction(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		//执行的数据类型
		intent.setDataAndType(getUriForFile(MainActivity.this,file), "application/vnd.android.package-archive");
		startActivity(intent);
	}

	public Uri getUriForFile(Context context, File file) {
		if (context == null || file == null) {
			throw new NullPointerException();
		}
		Uri uri;
		if (Build.VERSION.SDK_INT >= 24) {
			uri = FileProvider.getUriForFile(context.getApplicationContext(), "com.robert.maps.fileprovider", file);
		} else {
			uri = Uri.fromFile(file);
		}
		return uri;
	}

	//开始检测版本
	public void checkVersion(){
		try {
			localVersion = getVersionName();
			CheckVersionTask cv = new CheckVersionTask();
			new Thread(cv).start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//获取版本信息
	private String getVersionName() throws Exception {
		//getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageManager packageManager = getPackageManager();
		PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),
				0);
		return packInfo.versionName;
	}

	String url = "http://106.38.74.187:4029/dzd/get_userlist/";
	String xml;
	ContactPersons contactPersons;
	Collection coll;
	public void initOkHttp() throws IOException {
		// TODO Auto-generated method stub
		//1>创建HttpClient对象
		HttpClient client=new DefaultHttpClient();
		//2>创建HttpGet请求对象
		String urls=url+getData(SysContants.TEL, "");;
		HttpGet get=new HttpGet(urls);
		//3>execute
		HttpResponse resp=client.execute(get);
		//4>解析HttpResponse
		StatusLine line = resp.getStatusLine();
		int code=line.getStatusCode();
		if(code==200){ //正常返回
			//获取响应数据包的实体部分
			HttpEntity entity=resp.getEntity();
			xml= EntityUtils.toString(entity);
			Log.e("xml", xml );
			contactPersons = JSON.parseObject(xml, ContactPersons.class);
		}else{ //不正常
			Log.e("info", "请求发送失败...状态码不是200，是:"+code);
			return;
		}
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
			Editor editor = mPreferences.edit();
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

	protected void showMessage(String msg) {
		Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private final SensorEventListener mListener = new SensorEventListener() {
		private int iOrientation = -1;

		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}

		public void onSensorChanged(SensorEvent event) {
			if (iOrientation < 0) {
				iOrientation = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
						.getDefaultDisplay().getOrientation();
			}

			mMyLocationOverlay.setmBearing(event.values[SensorManager.DATA_X] + 90 * iOrientation);

			if (mCompassEnabled) {
//				if (mNorthDirectionUp) {
				if (mDrivingDirectionUp == false || mLastSpeed == 0) {
					mMap.setBearing(updateBearing(event.values[0]) + 90 * iOrientation);
				}
//				}
			} else {
				mMap.setBearing(0);
			}

			mMap.Refresh();
		}
	};

	//周边查询
	public void startSearchAround(){
		new ZhuangHaoAroundQueryTask(
				GeoPoint.fromDouble(unRectifyLocation.getLatitude(),
						unRectifyLocation.getLongitude()), 0).execute("");
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
		List<ArrangePoint> lstArrangePoints = new ArrayList<>();
		List<DrillPoint> lstDrillPoints = new ArrayList<>();
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
			progressDialog = new ProgressDialog(MainActivity.this);
			progressDialog.setMessage("正在周边查找...");
			progressDialog.setCancelable(false);
			progressDialog.show();
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			if (SysConfig.workType == WorkType.WORK_TYPE_SHOT){
				SparseArray<ShotPoint> sparseArray = new SparseArray<ShotPoint>();
				sparseArray = mPointDBDao.selectShotListNotHidden(16, centerPoint2d, 0.001, 0.001);
				if (sparseArray == null || sparseArray.size() == 0) {
					for (double newDis : arrDis) {
						sparseArray = mPointDBDao.selectShotListNotHidden(16, centerPoint2d, newDis, newDis);
						if (sparseArray != null && sparseArray.size() > 0) {
							for (int i = 0; i < sparseArray.size(); i++) {
								if (!sparseArray.get(sparseArray.keyAt(i)).isDone){//只要未完成的任务点
									lstShotPoints.add(sparseArray.get(sparseArray.keyAt(i)));
								}
							}
							break;
						}
					}
				} else {
					for (int i = 0; i < sparseArray.size(); i++) {
						if (!sparseArray.get(sparseArray.keyAt(i)).isDone){//只要未完成的任务点
							lstShotPoints.add(sparseArray.get(sparseArray.keyAt(i)));
						}
					}
				}
				Log.e("doInBackground", "doInBackground: "+lstShotPoints.size() );
				return lstShotPoints != null && lstShotPoints.size() > 0;
			}else if (SysConfig.workType == WorkType.WORK_TYPE_ARRANGE){
				SparseArray<ArrangePoint> sparseArray = new SparseArray<ArrangePoint>();
				sparseArray = mPointDBDao.selectArrangeListNotHidden(16, centerPoint2d, 0.001, 0.001);
				if (sparseArray == null || sparseArray.size() == 0) {
					for (double newDis : arrDis) {
						sparseArray = mPointDBDao.selectArrangeListNotHidden(16, centerPoint2d, newDis, newDis);
						if (sparseArray != null && sparseArray.size() > 0) {
							for (int i = 0; i < sparseArray.size(); i++) {
								if (!sparseArray.get(sparseArray.keyAt(i)).isDone){//只要未完成的任务点
									lstArrangePoints.add(sparseArray.get(sparseArray.keyAt(i)));
								}
							}
							break;
						}
					}
				} else {
					for (int i = 0; i < sparseArray.size(); i++) {
						if (!sparseArray.get(sparseArray.keyAt(i)).isDone){//只要未完成的任务点
							lstArrangePoints.add(sparseArray.get(sparseArray.keyAt(i)));
						}
					}
				}
				Log.e("doInBackground", "doInBackground: "+lstShotPoints.size() );
				return lstArrangePoints != null && lstArrangePoints.size() > 0;
			}else if (SysConfig.workType == WorkType.WORK_TYPE_DRILE){
				SparseArray<DrillPoint> sparseArray = new SparseArray<DrillPoint>();
				sparseArray = mPointDBDao.selectDrillListNotHidden(16, centerPoint2d, 0.001, 0.001);
				if (sparseArray == null || sparseArray.size() == 0) {
					for (double newDis : arrDis) {
						sparseArray = mPointDBDao.selectDrillListNotHidden(16, centerPoint2d, newDis, newDis);
						if (sparseArray != null && sparseArray.size() > 0) {
							for (int i = 0; i < sparseArray.size(); i++) {
								if (!sparseArray.get(sparseArray.keyAt(i)).isDone){//只要未完成的任务点
									lstDrillPoints.add(sparseArray.get(sparseArray.keyAt(i)));
								}
							}
							break;
						}
					}
				} else {
					for (int i = 0; i < sparseArray.size(); i++) {
						if (!sparseArray.get(sparseArray.keyAt(i)).isDone){//只要未完成的任务点
							lstDrillPoints.add(sparseArray.get(sparseArray.keyAt(i)));
						}
					}
				}
				Log.e("doInBackground", "doInBackground: "+lstShotPoints.size() );
				return lstDrillPoints != null && lstDrillPoints.size() > 0;
			}else
				return false;
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
				if (SysConfig.workType == WorkType.WORK_TYPE_SHOT){
					showTaskAroundSelectDlg(lstShotPoints);
				}else if (SysConfig.workType == WorkType.WORK_TYPE_ARRANGE){
					showArrangeAroundSelectDlg(lstArrangePoints);
				}else if (SysConfig.workType == WorkType.WORK_TYPE_DRILE){
					showDrillAroundSelectDlg(lstDrillPoints);
				}

			}
		}
	}


	/***
	 * 常规任务点周边查询 shotpoints
	 */
	private void showTaskAroundSelectDlg(final List<ShotPoint> shotPoints) {
		final ZhuangHaoAroundSelectAdapter adapter = new ZhuangHaoAroundSelectAdapter(
				MainActivity.this, shotPoints, MainActivity.unRectifyLocation);

		// 设置默认选择
		int defIndex = 0;
		adapter.setSelectedItem(defIndex);
		new AlertDialog.Builder(this)
//                .setTitle("周边任务桩号选择")
//                 .setView(layout)
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
								sendBroadcast(intent);
							}
						}).setNegativeButton(R.string.cancel, null).create()
				.show();
	}

	/***
	 * 常规任务点周边查询 arrangePoints
	 */
	private void showArrangeAroundSelectDlg(final List<ArrangePoint> arrangePoints) {
		final ZhuangHaoAroundArrangeAdapter adapter = new ZhuangHaoAroundArrangeAdapter(
				MainActivity.this, arrangePoints, MainActivity.unRectifyLocation);

		// 设置默认选择
		int defIndex = 0;
		adapter.setSelectedItem(defIndex);
		new AlertDialog.Builder(this)
//                .setTitle("周边任务桩号选择")
//                 .setView(layout)
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
								ArrangePoint point = arrangePoints.get(adapter
										.getSelectedItem());
								String stationNo = point.stationNo;
								Intent intent = new Intent(ACTION_POINT_LOC);
								intent.putExtra("stationNo",stationNo);
								sendBroadcast(intent);
							}
						}).setNegativeButton(R.string.cancel, null).create()
				.show();
	}

	/***
	 * 常规任务点周边查询 drillPoints
	 */
	private void showDrillAroundSelectDlg(final List<DrillPoint> drillPoints) {
		final ZhuangHaoAroundDrillAdapter adapter = new ZhuangHaoAroundDrillAdapter(
				MainActivity.this, drillPoints, MainActivity.unRectifyLocation);

		// 设置默认选择
		int defIndex = 0;
		adapter.setSelectedItem(defIndex);
		new AlertDialog.Builder(this)
//                .setTitle("周边任务桩号选择")
//                 .setView(layout)
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
								DrillPoint point = drillPoints.get(adapter
										.getSelectedItem());
								String stationNo = point.stationNo;
								Intent intent = new Intent(ACTION_POINT_LOC);
								intent.putExtra("stationNo",stationNo);
								sendBroadcast(intent);
							}
						}).setNegativeButton(R.string.cancel, null).create()
				.show();
	}

	//------------------------------------------------------百度语音-------------------------------------------------------------------

	VoiceEntity voiceEntity;
	@Override
	public void onEvent(String name, String params, byte[] data, int offset, int length) {
		String logTxt = "name: " + name;
		if (params != null && !params.isEmpty()) {
			logTxt += " ;params :" + params;
		}
		if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
			if (params != null && params.contains("\"nlu_result\"")) {
				if (length > 0 && data.length > 0) {
					logTxt += ", 语义解析结果：" + new String(data, offset, length);
				}
			}
		} else if (data != null) {
			logTxt += " ;data length=" + data.length;
		}
		Log.e("logTxt", logTxt);
		voiceEntity = new VoiceEntity();
		voiceEntity = JSON.parseObject(params,VoiceEntity.class);
		if (voiceEntity!=null && voiceEntity.getResult_type()!=null){
			if (voiceEntity.getResult_type().equals("partial_result")){
				String content = voiceEntity.getBest_result().toString();
				Log.e("partial_result", "partial_result: "+content );
				tv_title.setVisibility(View.INVISIBLE);
				tv_content.setText(content);
			}else if (voiceEntity.getResult_type().equals("final_result")){
				String final_content = voiceEntity.getBest_result().toString();
				Log.e("final_result", "final_result: "+final_content );
				tv_content.setText(final_content);
				if (final_content.contains("定位") && final_content.length()>2){
					String stationNum = final_content.split("位")[1];
//					String regex = "[^0-9]";
//					Pattern p = Pattern.compile(regex);
//					Matcher m = p.matcher(final_content);
//					String stationNum = m.replaceAll("").trim();
//                    m.find();
//					stationNum = m.group();
					Log.e("voice", "stationNum: "+stationNum);
					TaskPoint shotPoint = null;
					if (stationNum!=null){
						if (SysConfig.workType == WorkType.WORK_TYPE_ARRANGE){
							shotPoint = mPointDBDao.selectArrangePoint(stationNum);
						}else if (SysConfig.workType == WorkType.WORK_TYPE_SHOT){
							shotPoint = mPointDBDao.selectShotPoint(stationNum);
						}else if (SysConfig.workType == WorkType.WORK_TYPE_DRILE){
							shotPoint = mPointDBDao.selectDrillPoint(stationNum);
						}

						if (shotPoint!=null){
							Intent intentx = new Intent(ACTION_POINT_LOC);
							intentx.putExtra("stationNo",stationNum);
							sendBroadcast(intentx);
						}else {
							tv_content.setText("桩号不存在");
						}
					}

				}else if (final_content.contains("导航") && final_content.length()>2){
					String stationNum = final_content.split("航")[1];
					if (stationNum!=null) {
						TaskPoint shotPoint = null;
						if (SysConfig.workType == WorkType.WORK_TYPE_ARRANGE){
							shotPoint = mPointDBDao.selectArrangePoint(stationNum);
						}else if (SysConfig.workType == WorkType.WORK_TYPE_SHOT){
							shotPoint = mPointDBDao.selectShotPoint(stationNum);
						}else if (SysConfig.workType == WorkType.WORK_TYPE_DRILE){
							shotPoint = mPointDBDao.selectDrillPoint(stationNum);
						}
						if (shotPoint!=null){
							Message message = new Message();
							message.what = RequestCode.TASK_GUIDE;
							message.obj = shotPoint;
							mCallbackHandler.sendMessage(message);
						}else {
							tv_content.setText("桩号不存在");
						}
					}
				}else {
					tv_content.setText("您输入的指令不正确");
				}
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						voice_popupWindow.dismiss();
						tv_title.setVisibility(View.VISIBLE);
						tv_title.setText("你可以说：");
						tv_content.setText("“定位+桩号”  \n“导航+桩号” ");
					}
				},1500);
			}

		}else if (voiceEntity!=null && voiceEntity.getError()!=0){
			voice_popupWindow.dismiss();
			tv_title.setVisibility(View.VISIBLE);
			tv_title.setText("你可以说：");
			tv_content.setText("“定位+桩号”  \n“导航+桩号” ");
		}

	}



	/**
	 * enableOffline设为true时，在onCreate中调用
	 * 基于SDK离线命令词1.4 加载离线资源(离线时使用)
	 */
	private void loadOfflineEngine() {
		Map<String, Object> params = new LinkedHashMap<String, Object>();
		params.put(SpeechConstant.DECODER, 2);
		params.put(SpeechConstant.ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH, "assets://baidu_speech_grammar.bsg");
		asr.send(SpeechConstant.ASR_KWS_LOAD_ENGINE, new JSONObject(params).toString(), null, 0, 0);
	}

	/**
	 * enableOffline为true时，在onDestory中调用，与loadOfflineEngine对应
	 * 基于SDK集成5.1 卸载离线资源步骤(离线时使用)
	 */
	private void unloadOfflineEngine() {
		asr.send(SpeechConstant.ASR_KWS_UNLOAD_ENGINE, null, null, 0, 0); //
	}
	/**
	 * 基于SDK集成2.2 发送开始事件
	 * 点击开始按钮
	 * 测试参数填在这里
	 */
	private void start() {
		Map<String, Object> params = new LinkedHashMap<String, Object>();
		String event = null;
		event = SpeechConstant.ASR_START; // 替换成测试的event

		if (enableOffline) {
			params.put(SpeechConstant.DECODER, 2);
		}
		// 基于SDK集成2.1 设置识别参数
		params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
		// params.put(SpeechConstant.NLU, "enable");
		// params.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT, 0); // 长语音
		// params.put(SpeechConstant.IN_FILE, "res:///com/baidu/android/voicedemo/16k_test.pcm");
		// params.put(SpeechConstant.VAD, SpeechConstant.VAD_DNN);
		// params.put(SpeechConstant.PID, 1537); // 中文输入法模型，有逗号

		/* 语音自训练平台特有参数 */
		// params.put(SpeechConstant.PID, 8002);
		// 语音自训练平台特殊pid，8002：搜索模型类似开放平台 1537  具体是8001还是8002，看自训练平台页面上的显示
		// params.put(SpeechConstant.LMID,1068); // 语音自训练平台已上线的模型ID，https://ai.baidu.com/smartasr/model
		// 注意模型ID必须在你的appId所在的百度账号下
		/* 语音自训练平台特有参数 */

		String json = null; // 可以替换成自己的json
		json = new JSONObject(params).toString(); // 这里可以替换成你需要测试的json
		asr.send(event, json, null, 0, 0);
		Log.e("json", json );
	}

	/**
	 * 点击停止按钮
	 *  基于SDK集成4.1 发送停止事件
	 */
	private void stop() {
		Log.e("ASR_STOP", "停止识别：ASR_STOP");
		asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0); //
	}

}
