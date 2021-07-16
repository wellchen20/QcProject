package com.mtkj.cnpc.activity;

import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.Toast;

import com.mtkj.cnpc.activity.InterFace.IIntentTabHost;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.constants.SysConfig.WorkType;
import com.mtkj.cnpc.protocol.constants.SysContants;
import com.mtkj.cnpc.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import static com.mtkj.cnpc.protocol.constants.SysConfig.url;

@SuppressWarnings("deprecation")
public class MenuActivity extends TabActivity implements OnClickListener, IIntentTabHost {

	private RadioGroup tabRadioGroup;
	private TabHost tabHost;

	private RadioButton radio_button_map, radio_button_work, radio_button_news, radio_button_my;
	private SharedPreferences mPreferences;
	private ImageView iv_news;
	private ImageView iv_task;
	private String ACTION_GET_MESSAGE = "action_get_message";
	private String ACTION_MESSAGE_CANCEL = "action_message_cancel";
	private String ACTION_GET_NEW = "action_get_new";
	private String ACTION_NEWS_CANCEL = "action_new_cancel";
	private String ACTION_NEWS_ALL = "action_news_all";
	private String ACTION_WEB_NOTICE = "action_web_notice";
	private String ACTION_WEB_CANCEL = "action_web_cancel";
	GetNewsReceiver getNewsReceiver;
	String xml;
	private String tel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		mPreferences = getSharedPreferences(SysContants.SYSCONFIG, MODE_PRIVATE);
//		TaskActivity.setIntentTabHost(this);
		MyActivity.setIntentTabHost(this);

		tel = mPreferences.getString(SysContants.TEL, "");
		LoginActivity.isLogin = mPreferences.getBoolean(SysContants.ISLOGIN, false);
		if (LoginActivity.isLogin){
			new MyCreditAsync().execute();
		}
		initViews();

		Intent intent = getIntent();
		String action = intent.getAction();
		if (TextUtils.equals(action, Intent.ACTION_VIEW)) {
			Uri uri = intent.getData();
			if (TextUtils.equals(uri.getScheme(), "file")) {
				String path = uri.getPath();
				Toast.makeText(MenuActivity.this, path, Toast.LENGTH_LONG).show();
			}
		}
		registReceiver();
	}

	public void registReceiver(){
		getNewsReceiver = new GetNewsReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_GET_MESSAGE);
		intentFilter.addAction(ACTION_GET_NEW);
		intentFilter.addAction(ACTION_NEWS_CANCEL);
		intentFilter.addAction(ACTION_MESSAGE_CANCEL);
		intentFilter.addAction(ACTION_NEWS_ALL);
		intentFilter.addAction(ACTION_WEB_NOTICE);
		intentFilter.addAction(ACTION_WEB_CANCEL);
		registerReceiver(getNewsReceiver, intentFilter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(getNewsReceiver);
	}

	class GetNewsReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ACTION_GET_MESSAGE) || intent.getAction().equals(ACTION_WEB_NOTICE)){
				iv_task.setVisibility(View.VISIBLE);
			}else if (intent.getAction().equals(ACTION_MESSAGE_CANCEL) || intent.getAction().equals(ACTION_WEB_CANCEL)){
				iv_task.setVisibility(View.INVISIBLE);
			}else if (intent.getAction().equals(ACTION_GET_NEW)){
				iv_news.setVisibility(View.VISIBLE);
			}else if (intent.getAction().equals(ACTION_NEWS_CANCEL)){
				iv_news.setVisibility(View.INVISIBLE);
			}else if (intent.getAction().equals(ACTION_NEWS_ALL)){
				iv_news.setVisibility(View.VISIBLE);
			}
		}
	}

	private void initViews() {
		iv_news = (ImageView) findViewById(R.id.iv_news);
		iv_task = (ImageView) findViewById(R.id.iv_task);
		tabHost = getTabHost();
		tabHost.addTab(tabHost.newTabSpec("ONE").setIndicator("ONE").setContent(new Intent(this, MainActivity.class)));
		tabHost.addTab(tabHost.newTabSpec("THREE").setIndicator("THREE").setContent(new Intent(this, NewsActivity.class)));
		if (LoginActivity.isLogin) {
			if (SysConfig.workType == WorkType.WORK_TYPE_VEHICLE) {
				tabHost.addTab(tabHost.newTabSpec("TWO").setIndicator("TWO").setContent(new Intent(this, WorkTypeChoose.class)));// 四汇报
			} else {
				tabHost.addTab(tabHost.newTabSpec("TWO").setIndicator("TWO").setContent(new Intent(this, TaskActivity.class)));
			}
			tabHost.addTab(tabHost.newTabSpec("FOUR").setIndicator("FOUR").setContent(new Intent(this, MyActivity.class)));
		} else {
			tabHost.addTab(tabHost.newTabSpec("TWO").setIndicator("TWO").setContent(new Intent(this, TaskActivity.class)));
			tabHost.addTab(tabHost.newTabSpec("FOUR").setIndicator("FOUR").setContent(new Intent(this, LoginActivity.class)));
		}

		radio_button_map = (RadioButton) findViewById(R.id.radio_button_map);
		radio_button_work = (RadioButton) findViewById(R.id.radio_button_work);
		radio_button_news = (RadioButton) findViewById(R.id.radio_button_news);
		radio_button_my = (RadioButton) findViewById(R.id.radio_button_my);


		tabRadioGroup = (RadioGroup) findViewById(R.id.main_radio);
		tabRadioGroup.setOnCheckedChangeListener(changeListener);
		checkedMy();
		checkedMap();
		if (LoginActivity.isLogin) {
			tabRadioGroup.getChildAt(1).setClickable(true);
			tabRadioGroup.getChildAt(2).setClickable(true);
		} else {
			tabRadioGroup.getChildAt(1).setClickable(false);
			tabRadioGroup.getChildAt(2).setClickable(false);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.radio_button_map:
				checkedMap();
				break;

			case R.id.radio_button_work:
				checkedTask();
				break;

			case R.id.radio_button_news:
				checkedNews();
				break;

			case R.id.radio_button_my:
				checkedMy();
				break;
		}
	}

	private RadioGroup.OnCheckedChangeListener changeListener = new RadioGroup.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (checkedId) {
				case R.id.radio_button_map:
					checkedMap();
					break;

				case R.id.radio_button_work:
					checkedTask();
					break;

				case R.id.radio_button_news:
					checkedNews();
					break;

				case R.id.radio_button_my:
					checkedMy();
					break;
			}
		}
	};

	@Override
	public void intentTab(int position) {
		if (LoginActivity.isLogin) {
			tabHost.clearAllTabs();
			tabHost.addTab(tabHost.newTabSpec("ONE").setIndicator("ONE").setContent(new Intent(this, MainActivity.class)));
			if (SysConfig.workType == WorkType.WORK_TYPE_VEHICLE) {
				tabHost.addTab(tabHost.newTabSpec("TWO").setIndicator("TWO").setContent(new Intent(this, WorkTypeChoose.class)));//四汇报
			}  else {
				tabHost.addTab(tabHost.newTabSpec("TWO").setIndicator("TWO").setContent(new Intent(this, TaskActivity.class)));
			}
			tabHost.addTab(tabHost.newTabSpec("THREE").setIndicator("THREE").setContent(new Intent(this, NewsActivity.class)));
			tabHost.addTab(tabHost.newTabSpec("FOUR").setIndicator("FOUR").setContent(new Intent(this, MyActivity.class)));

			tabRadioGroup.getChildAt(1).setClickable(true);
			tabRadioGroup.getChildAt(2).setClickable(true);
		} else {
			tabHost.clearAllTabs();
			tabHost.addTab(tabHost.newTabSpec("ONE").setIndicator("ONE").setContent(new Intent(this, MainActivity.class)));
			tabHost.addTab(tabHost.newTabSpec("TWO").setIndicator("TWO").setContent(new Intent(this, TaskActivity.class)));
			tabHost.addTab(tabHost.newTabSpec("THREE").setIndicator("THREE").setContent(new Intent(this, NewsActivity.class)));
			tabHost.addTab(tabHost.newTabSpec("FOUR").setIndicator("FOUR").setContent(new Intent(this, LoginActivity.class)));

			tabRadioGroup.getChildAt(1).setClickable(false);
			tabRadioGroup.getChildAt(2).setClickable(false);
		}
		switch (position) {
			case 1:
				checkedMap();
				break;

			case 2:
				checkedTask();
				break;

			case 3:
				checkedNews();
				break;

			case 4:
				checkedMy();
				break;
		}
	}

	private void checkedMy() {
		tabHost.setCurrentTabByTag("FOUR");
		tabRadioGroup.check(R.id.radio_button_my);
		radio_button_map.setSelected(false);
		radio_button_map.setTextColor(getResources().getColor(R.color.host_bottom_text));
		radio_button_work.setSelected(false);
		radio_button_work.setTextColor(getResources().getColor(R.color.host_bottom_text));
		radio_button_news.setSelected(false);
		radio_button_news.setTextColor(getResources().getColor(R.color.host_bottom_text));
		radio_button_my.setSelected(true);
		radio_button_my.setTextColor(getResources().getColor(R.color.tabhost_bt_text));
	}

	private void checkedNews() {
		tabHost.setCurrentTabByTag("THREE");
		tabRadioGroup.check(R.id.radio_button_news);
		radio_button_map.setSelected(false);
		radio_button_map.setTextColor(getResources().getColor(R.color.host_bottom_text));
		radio_button_work.setSelected(false);
		radio_button_work.setTextColor(getResources().getColor(R.color.host_bottom_text));
		radio_button_news.setSelected(true);
		radio_button_news.setTextColor(getResources().getColor(R.color.tabhost_bt_text));
		radio_button_my.setSelected(false);
		radio_button_my.setTextColor(getResources().getColor(R.color.host_bottom_text));
	}

	private void checkedTask() {
		tabHost.setCurrentTabByTag("TWO");
		tabRadioGroup.check(R.id.radio_button_work);
		radio_button_map.setSelected(false);
		radio_button_map.setTextColor(getResources().getColor(R.color.host_bottom_text));
		radio_button_work.setSelected(true);
		radio_button_work.setTextColor(getResources().getColor(R.color.tabhost_bt_text));
		radio_button_news.setSelected(false);
		radio_button_news.setTextColor(getResources().getColor(R.color.host_bottom_text));
		radio_button_my.setSelected(false);
		radio_button_my.setTextColor(getResources().getColor(R.color.host_bottom_text));
	}

	private void checkedMap() {
		tabHost.setCurrentTabByTag("ONE");
		tabRadioGroup.check(R.id.radio_button_map);
		radio_button_map.setSelected(true);
		radio_button_map.setTextColor(getResources().getColor(R.color.tabhost_bt_text));
		radio_button_work.setSelected(false);
		radio_button_work.setTextColor(getResources().getColor(R.color.host_bottom_text));
		radio_button_news.setSelected(false);
		radio_button_news.setTextColor(getResources().getColor(R.color.host_bottom_text));
		radio_button_my.setSelected(false);
		radio_button_my.setTextColor(getResources().getColor(R.color.host_bottom_text));
	}

	public void initOkHttp() throws IOException {
		// TODO Auto-generated method stub
		//1>创建HttpClient对象
		HttpClient client=new DefaultHttpClient();
		//2>创建HttpGet请求对象
		String urls=url+"credit/login/"+tel;
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

		}else{ //不正常
			Log.e("info", "请求发送失败...状态码不是200，是:"+code);
		}
	}

	class MyCreditAsync extends AsyncTask<String, Integer, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			try {
				initOkHttp();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
	}

}
