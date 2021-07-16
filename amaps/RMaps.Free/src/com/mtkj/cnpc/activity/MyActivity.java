package com.mtkj.cnpc.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.mtkj.cnpc.activity.InterFace.IIntentTabHost;
import com.mtkj.cnpc.activity.utils.DownLoadManager;
import com.mtkj.cnpc.activity.utils.UpdataInfoParser;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.constants.SysConfig.WorkType;
import com.mtkj.cnpc.protocol.constants.SysContants;
import com.mtkj.cnpc.protocol.socket.DataProcess;
import com.mtkj.cnpc.sqlite.PointDBDao;
import com.mtkj.utils.entity.UpdateInfo;
import com.mtkj.utils.zxing.camera.MipcaActivityCapture;
import com.mtkj.cnpc.R;
import com.robert.maps.applib.utils.DialogUtils;

public class MyActivity extends BaseActivity implements OnClickListener {

	// 标题栏
	private TextView mybacktiletext, txttiletext, tvMymsg;
	private ImageView imgchangimg;
	private LinearLayout backlayout;
	private ImageView iv_news,iv_task_new;
	private String ACTION_GET_MESSAGE = "action_get_message";
	private String ACTION_MESSAGE_CANCEL = "action_message_cancel";
	private String ACTION_WEB_NOTICE = "action_web_notice";
	private String ACTION_WEB_CANCEL = "action_web_cancel";

	// 注销
	private Button btn_logout;

	// 个人信息
	private LinearLayout ll_person_plate, ll_shot_takssetting, ll_person_clear_data,ll_get_task,ll_user_credit,ll_person_task;
	private ImageView iv_user_head;
	private TextView tv_user_username, tv_user_geodept, tv_user_teamname,
			tv_user_tel, tv_user_oname, tv_user_job,
			tv_user_duty, tv_user_plate, tv_app_version, tv_clear_data;

	// 头像
	private LayoutInflater mInflater;
	private View popView;
	private PopupWindow pop;
	private TextView tv_photo_graph, tv_photo_local, tv_cancle;
	private String Filepath, imagePath;

	private ProgressDialog pd;
	GetNewsReceiver getNewsReceiver;
	private final int UPDATA_NONEED = -1;
	private final int UPDATA_CLIENT = -2;
	private final int GET_UNDATAINFO_ERROR = -3;
	private final int DOWN_ERROR = -4;
	private UpdateInfo info;
	private String localVersion;
	private Handler mCallbackHandler;
	PointDBDao mPointDBDao;
	String ACTION_POINT_LOC = "action_point_loc";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_personal);
		super.onCreate(savedInstanceState);
		mPointDBDao = new PointDBDao(this);
		mInflater = LayoutInflater.from(MyActivity.this);
		Filepath = Environment.getExternalStorageDirectory().getPath()
				+ "/rmaps/MyImg";

		File file = new File(Filepath);
		if (!file.exists()) {
			file.mkdirs();
		}

		initPop();
		initViews();
		getDatas();
		registReceiver();
	}

	public void registReceiver(){
		getNewsReceiver = new GetNewsReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_GET_MESSAGE);
		intentFilter.addAction(ACTION_MESSAGE_CANCEL);
		intentFilter.addAction(ACTION_WEB_NOTICE);
		intentFilter.addAction(ACTION_WEB_CANCEL);
		registerReceiver(getNewsReceiver, intentFilter);

		mCallbackHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				int what = msg.what;
				if (what == UPDATA_NONEED) {
					Toast.makeText(getApplicationContext(), "不需要更新",
							Toast.LENGTH_SHORT).show();
				}else if (what == UPDATA_CLIENT) {
					showUpdataDialog();
				}else if (what == GET_UNDATAINFO_ERROR) {
					//服务器超时
					Toast.makeText(getApplicationContext(), "获取服务器更新信息失败", Toast.LENGTH_LONG).show();
				}else if (what == DOWN_ERROR) {
					//下载apk失败
					Toast.makeText(getApplicationContext(), "下载新版本失败", Toast.LENGTH_LONG).show();
					pd.dismiss();
				}
			}
		};
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(getNewsReceiver);
		DataProcess.GetInstance().stopConn();
	}

	class GetNewsReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ACTION_GET_MESSAGE)){
				iv_news.setVisibility(View.VISIBLE);
			}/*else if (intent.getAction().equals(ACTION_MESSAGE_CANCEL)){
				iv_news.setVisibility(View.INVISIBLE);
			}*/else if (intent.getAction().equals(ACTION_WEB_NOTICE)){
				iv_task_new.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	protected void onResume() {
		if (pop != null && pop.isShowing()) {
			pop.dismiss();
		}
		keyBackClickCount = 0;
		refreshViews();
		super.onResume();
	}

	private int keyBackClickCount = 0;

	@Override
	public void onBackPressed() {
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


	public void initViews() {
		// 个人信息
		iv_user_head = (ImageView) findViewById(R.id.iv_user_head);
		iv_user_head.setOnClickListener(this);
		tv_user_username = (TextView) findViewById(R.id.tv_user_name);
		tv_user_username.setOnClickListener(this);
		tv_user_geodept = (TextView) findViewById(R.id.tv_user_geodept);
		tv_user_teamname = (TextView) findViewById(R.id.tv_user_teamname);
		tv_user_tel = (TextView) findViewById(R.id.tv_user_tel);
		tv_user_oname = (TextView) findViewById(R.id.tv_user_oname);
		tv_user_job = (TextView) findViewById(R.id.tv_user_job);
		tv_user_duty = (TextView) findViewById(R.id.tv_user_duty);
		ll_person_plate = (LinearLayout) findViewById(R.id.ll_person_plate);
		tv_user_plate = (TextView) findViewById(R.id.tv_user_plate);
		tv_app_version = (TextView) findViewById(R.id.tv_app_version);
		tv_clear_data = (TextView) findViewById(R.id.tv_clear_data);
		ll_person_clear_data = (LinearLayout) findViewById(R.id.ll_person_clear_data);
		tv_clear_data.setOnClickListener(this);
		ll_person_clear_data.setOnClickListener(this);
		ll_get_task = (LinearLayout) findViewById(R.id.ll_get_task);
		ll_get_task.setOnClickListener(this);
		ll_user_credit = (LinearLayout) findViewById(R.id.ll_user_credit);
		ll_user_credit.setOnClickListener(this);
		tvMymsg = (TextView) findViewById(R.id.tv_mymsg);
		tvMymsg.setOnClickListener(this);

		// 注销
		btn_logout = (Button) findViewById(R.id.btn_logout);
		btn_logout.setOnClickListener(this);

		ll_shot_takssetting = (LinearLayout) findViewById(R.id.ll_shot_takssetting);
		ll_shot_takssetting.setOnClickListener(this);

		iv_news = (ImageView) findViewById(R.id.iv_news);
		ll_person_task = (LinearLayout) findViewById(R.id.ll_person_task);
		iv_task_new = (ImageView) findViewById(R.id.iv_task_new);
	}

	private void initPop() {
		popView = mInflater.inflate(R.layout.pop_photo, null);
		tv_photo_graph = (TextView) popView.findViewById(R.id.tv_photo_graph);
		tv_photo_graph.setOnClickListener(this);
		tv_photo_local = (TextView) popView.findViewById(R.id.tv_photo_local);
		tv_photo_local.setOnClickListener(this);
		tv_cancle = (TextView) popView.findViewById(R.id.tv_cancle);
		tv_cancle.setOnClickListener(this);

		pop = new PopupWindow(popView, LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT, true);
	}

	public void getDatas() {
		tv_user_username.setText(getData(SysContants.USERNAME, ""));
		tv_user_job.setText(getData(SysContants.USERJOB, ""));
		tv_user_duty.setText(getData(SysContants.DUTY, ""));
		// 设置电话,地址,邮箱
		tv_user_geodept.setText(getData(SysContants.GEODEPT, ""));
		tv_user_tel.setText(getData(SysContants.TEL, ""));
		tv_user_teamname.setText(getData(SysContants.TEAMNAME, ""));
		tv_user_geodept.setText(getData(SysContants.GEODEPT, ""));
		tv_user_oname.setText(getData(SysContants.ONAME, ""));
		tv_app_version.setText(getAppVersionName());
	}

	private void refreshViews() {
		if (SysConfig.workType == WorkType.WORK_TYPE_SHOT) {
			ll_shot_takssetting.setVisibility(View.VISIBLE);
		} else {
			ll_shot_takssetting.setVisibility(View.GONE);
		}
		if (SysConfig.workType == WorkType.WORK_TYPE_VEHICLE) {
			tv_user_plate.setText(getData(SysContants.CARNUM, ""));
			ll_person_plate.setVisibility(View.VISIBLE);
		} else {
			ll_person_plate.setVisibility(View.GONE);
		}
		if (SysConfig.workType == WorkType.WORK_TYPE_ARRANGE){
			ll_person_task.setVisibility(View.VISIBLE);
		}else {
			ll_person_task.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.iv_user_head:
//			showPop();
				Intent intent = new Intent(MyActivity.this, MipcaActivityCapture.class);
				startActivityForResult(intent, 1);
				break;

			case R.id.tv_user_name:
				Intent intent1 = new Intent(MyActivity.this, MipcaActivityCapture.class);
				startActivityForResult(intent1, 1);
				break;

			case R.id.ll_person_tel:
				break;

			case R.id.ll_person_task:
				Intent intent2 = new Intent("action_message_cancel");
				sendBroadcast(intent2);
				iv_news.setVisibility(View.INVISIBLE);
				startActivity(new Intent(MyActivity.this,TaskListActivity.class));
				break;
			case R.id.ll_get_task:
				Intent intent3 = new Intent(MyActivity.this,ReceiveTaskActivity.class);
				Intent intentw = new Intent(ACTION_WEB_CANCEL);
				sendBroadcast(intentw);
				iv_task_new.setVisibility(View.INVISIBLE);
				startActivityForResult(intent3,105);
				break;
			case R.id.ll_user_credit:
				startActivity(new Intent(MyActivity.this,MyCreditActivity.class));
				break;
			case R.id.btn_logout:
				logout();
				break;

			case R.id.backlayout:
				break;
			case R.id.tv_mymsg:
				break;
			case R.id.ll_person_about:
				break;

			case R.id.ll_shot_takssetting:
				startActivity(new Intent(this, TaskSettingActivity.class));
				break;

			case R.id.tv_cancle:
				if (pop != null && pop.isShowing()) {
					pop.dismiss();
				}
				break;

			case R.id.tv_clear_data:
			case R.id.ll_person_clear_data:
				showClearDataDialog();
				break;

			case R.id.tv_photo_graph:
				Intent intentGraph = new Intent();
				// 指定开启系统相机的Action
				intentGraph.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
				intentGraph.addCategory(Intent.CATEGORY_DEFAULT);

				intentGraph.putExtra("return-data", true);
				imagePath = getFileNmae();
				Uri uriGraph = null;
				if (Build.VERSION.SDK_INT >= 24) {
					uriGraph = FileProvider.getUriForFile(MyActivity.this, "com.mtkj.cnpc.photo.fileprovider", new File(imagePath));
				} else {
					uriGraph = Uri.fromFile(new File(imagePath));
				}
				// 设置系统相机拍摄照片完成后图片文件的存放地址
				intentGraph.putExtra(MediaStore.EXTRA_OUTPUT, uriGraph);
				startActivityForResult(intentGraph, 100);
				break;

			case R.id.tv_photo_local:
				// Intent intentLocal = new Intent(Intent.ACTION_GET_CONTENT);
				Intent intentLocal = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				intentLocal.setType("image/*");
				startActivityForResult(intentLocal, 101);
				break;

			case R.id.tv_app_version:
				checkVersion();
				break;
			default:
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
									Intent intent) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case 100: // 照相机
					clipPhoto(Uri.fromFile(new File(imagePath)));

					// Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
					// imgYS(bitmap, imagePath);
					// user.image = imagePath;
					// iv_user_head.setImageBitmap(bitmap);
					break;

				case 101: // 相册

					clipPhoto(intent.getData());

					break;

				case 0:
					String name = intent.getStringExtra("name");
					tv_user_username.setText(name);

					Intent intent2 = new Intent();
					// 修改我的信息姓名
					intent2.putExtra("username", name);
					setResult(104, intent2);
					break;

				case 102:
					pd.show();
					break;

				case 1:
					String result = intent.getStringExtra("usercode");
					showMessage(result);
					break;

				case 105:
					mIntentTabHost.intentTab(1);
					String stationNo = intent.getStringExtra("stationNo");
					Intent intentx = new Intent(ACTION_POINT_LOC);
					intentx.putExtra("stationNo",stationNo);
					sendBroadcast(intentx);
					break;

				default:
					break;
			}
		}
		if (requestCode == 103) {
			if (resultCode == 1) {
				String tel = intent.getStringExtra("tel");
				tv_user_tel.setText(tel);
			}
		}
	}

	private Dialog dialog;
	public void showClearDataDialog() {
		dialog = DialogUtils.Alert(MyActivity.this, "提示", "是否清空当前数据？",
				new String[]{MyActivity.this.getString(R.string.ok), MyActivity.this.getString(R.string.cancel)},
				new OnClickListener[]{new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 清空数据库
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
						mPointDBDao.deleteAllArrangeRecord();
//						mPointDBDao.deleteAllTask();

						dialog.dismiss();
					}
				},
						new OnClickListener() {

							@Override
							public void onClick(View v) {
								dialog.dismiss();
							}
						}
				});
		dialog.show();
	}


	/**
	 * 注销用户
	 */
	public void logout() {
		LoginActivity.isLogin = false;
		SysConfig.workType = WorkType.WORK_TYPE_NONE;
		setData(SysContants.WORK_TYPE, SysConfig.workType);
		setData(SysContants.ISLOGIN, LoginActivity.isLogin);
		setData(SysContants.SHOTSELECTTYPE,SysConfig.shotSelectType);
		DataProcess.GetInstance().stopConn();
		mIntentTabHost.intentTab(4);
	}

	@SuppressLint("SimpleDateFormat")
	public static String getTimeKey() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String string = dateFormat.format(new Date(System.currentTimeMillis()));
		return string.trim();
	}

	private String getFileNmae() {
		// 拍完照片之后保存的路径（文件名）
		String fileName = getTimeKey();
		return Filepath + File.separator + fileName + ".jpg";
	}

	/**
	 * 保存图片
	 *
	 * @param bitmap
	 * @param filepath
	 */
	public void imgYS(Bitmap bitmap, String filepath) {
		FileOutputStream b = null;
		try {
			b = new FileOutputStream(filepath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (bitmap != null) {
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);
		}
	}

	private void clipPhoto(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 200);
		intent.putExtra("outputY", 200);
		imagePath = getFileNmae();
		intent.putExtra(MediaStore.EXTRA_OUTPUT,
				Uri.fromFile(new File(imagePath)));
		startActivityForResult(intent, 102);
	}

	/**
	 * 拍照pop
	 */
	public void showPop() {
		pop.setBackgroundDrawable(new ColorDrawable(Color
				.parseColor("#b0000000")));
		pop.showAtLocation(tv_user_username, Gravity.BOTTOM, 0, 0);
		pop.setAnimationStyle(R.style.app_pop);
		pop.setOutsideTouchable(true);
		pop.setFocusable(true);
		pop.update();
	}

	public boolean deleteFile(String path) {
		File file = new File(path);
		if (file.exists()) {
			if (file.isFile()) {
				file.delete();
			} else if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (File f : files) {
					deleteFile(f.getPath());
				}
				file.delete();
			}

		}
		return true;
	}

	/**
	 * 获取应用程序版本号
	 *
	 * @param
	 * @return
	 */
	private String getAppVersionName() {
		String versionName = "";
		try {
			PackageManager pm = this.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(this.getPackageName(), 0);
			versionName = pi.versionName;
			if (versionName == null || versionName.length() <= 0) {
				return "";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return versionName;
	}

	public static IIntentTabHost mIntentTabHost;

	public static void setIntentTabHost(IIntentTabHost intentTabHost) {
		mIntentTabHost = intentTabHost;
	}

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
		intent.setDataAndType(getUriForFile(MyActivity.this,file), "application/vnd.android.package-archive");
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
			localVersion = getAppVersionName();
			CheckVersionTask cv = new CheckVersionTask();
			new Thread(cv).start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
