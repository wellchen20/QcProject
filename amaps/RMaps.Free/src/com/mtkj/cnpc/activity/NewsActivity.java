package com.mtkj.cnpc.activity;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.mtkj.cnpc.activity.adapter.NewsAdapter;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.constants.SysContants;
import com.mtkj.cnpc.protocol.socket.DataProcess;
import com.mtkj.utils.entity.ContactPersons;
import com.mtkj.utils.entity.TalkEntity;
import com.mtkj.cnpc.R;
import com.robert.maps.applib.utils.DialogUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


/**
 * 显示所有会话记录，比较简单的实现，更好的可能是把陌生人存入本地，这样取到的聊天记录是可控的
 *
 */
public class NewsActivity extends BaseActivity {
	private ListView listView;
	TextView tv_clear;
	NewsAdapter adapter;
	String url = "http://106.38.74.187:4029/dzd/get_userlist/";
	ContactPersons contactPersons = new ContactPersons();
	String xml;
	private Timer timer;
	private MyTimerTask timerTask;
	String ACTION_GET_NEWS = "action_get_new";
	String ACTION_NEWS_ALL = "action_news_all";
	GetNewsReceiver getNewsReceiver;
	public static int posArr;
	public static boolean flag = false;
	int pos;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversation_history);
		setViews();
		new MyGetPersonsAsync().execute("0");
		registReceiver();
	}
	private void registReceiver(){
		getNewsReceiver = new GetNewsReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction(ACTION_GET_NEWS);
		filter.addAction(ACTION_NEWS_ALL);
		registerReceiver(getNewsReceiver, filter);
	}


	//
	class MyGetPersonsAsync extends AsyncTask<String, Integer, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			String flag = params[0];
			try {
				initOkHttp();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (flag.equals("0")){
				return true;
			}else
				return false;
		}

		@Override
		protected void onPostExecute(Boolean aBoolean) {
			super.onPostExecute(aBoolean);
			if (aBoolean){
				setAdapters();
				setListeners();
			}else {
				//移除自己
				for (int i=0;i<contactPersons.getUserlist().size();i++){
					if (getData(SysContants.TEL,"").equals(contactPersons.getUserlist().get(i).getPhone())){
						setData(SysContants.DEVICE,contactPersons.getUserlist().get(i).getDevice());
						contactPersons.getUserlist().remove(i);
					}
				}
				ContactPersons.UserlistBean user = new ContactPersons.UserlistBean();
				user.setDevice("szdzd");
				user.setName(getData(SysContants.USERNAME,""));
				user.setOid(0);
				user.setStatus(101);
				user.setPhone("");
				contactPersons.getUserlist().add(0,user);
				adapter.refush(contactPersons.getUserlist());
			}
		}
	}

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
		}
	}


	private void setViews() {
		listView = (ListView)findViewById(R.id.list);
		tv_clear = (TextView) findViewById(R.id.tv_clear);
	}

	private void setAdapters() {
		for (int i=0;i<contactPersons.getUserlist().size();i++){
			if (getData(SysContants.TEL,"").equals(contactPersons.getUserlist().get(i).getPhone())){
				setData(SysContants.DEVICE,contactPersons.getUserlist().get(i).getDevice());
				contactPersons.getUserlist().remove(i);
			}
		}
		ContactPersons.UserlistBean user = new ContactPersons.UserlistBean();
		user.setDevice("szdzd");
		user.setName(getData(SysContants.USERNAME,""));
		user.setOid(0);
		user.setStatus(101);
		user.setPhone("");
		contactPersons.getUserlist().add(0,user);
		adapter = new NewsAdapter(NewsActivity.this,contactPersons.getUserlist());
		listView.setAdapter(adapter);
	}

	private void setListeners() {
		// 设置adapter
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position==0){
					Intent intent = new Intent(NewsActivity.this,TalkAllActivity.class);
					intent.putExtra("contactPersons",contactPersons);
					intent.putExtra("person",contactPersons.getUserlist().get(position));
					startActivity(intent);
				}else {
					Intent intent = new Intent(NewsActivity.this,TalkActivity.class);
					intent.putExtra("person",contactPersons.getUserlist().get(position));
					pos = position;
					startActivityForResult(intent,0);
				}
				Intent intent = new Intent("action_new_cancel");
				sendBroadcast(intent);

			}
		});

		tv_clear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showClearDataDialog();
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
			Log.e("onActivityResult", "onActivityResult: 1" );
			if (posArr == pos){
				flag = false;
				adapter.notifyDataSetChanged();
			}

			/*for (int i=0;i<posArr.size();i++){
				if (posArr.get(i)==pos){
					posArr.remove(i);
					listView.setAdapter(new NewsAdapter(NewsActivity.this,contactPersons.getUserlist()));
					adapter.notifyDataSetChanged();
				}
			}*/

	}

	@Override
	public void onResume() {
		super.onResume();
		timer = new Timer();
		timerTask = new MyTimerTask();
		timer.schedule(timerTask, 5000, 5000);
		keyBackClickCount = 0;
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

	class MyTimerTask extends TimerTask {

		@Override
		public void run() {
			// 更新在线
			new MyGetPersonsAsync().execute("1");
		}
	};


	private Dialog dialog;
	public void showClearDataDialog() {
		dialog = DialogUtils.Alert(NewsActivity.this, "提示", "清除所有聊天记录？",
				new String[]{NewsActivity.this.getString(R.string.ok), NewsActivity.this.getString(R.string.cancel)},
				new View.OnClickListener[]{new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// 清空数据库
						mPointDBDao.deleteAllTalk();
						dialog.dismiss();
					}
				},
						new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								dialog.dismiss();
							}
						}
				});
		dialog.show();
	}

	class GetNewsReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			TalkEntity talkEntity = (TalkEntity) intent.getSerializableExtra("talkEntity");
			if(action.equals(ACTION_GET_NEWS)){
				String device = talkEntity.getDevice();
				for (int i=0;i<contactPersons.getUserlist().size();i++){
					if (device.equals(contactPersons.getUserlist().get(i).getDevice())){
//						contactPersons.getUserlist().get(i).setIsNews(1);
//						adapter.refush(contactPersons.getUserlist());
						posArr = i;
						flag = true;
						Log.e("pos", i+"" );
						adapter.notifyDataSetChanged();
					}
				}
			}else if (action.equals(ACTION_NEWS_ALL)){

			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (timer!=null){
			timer.cancel();
			timer=null;
		}if (timerTask!=null){
			timerTask.cancel();
			timerTask = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (timer!=null){
			timer.cancel();
			timer=null;
		}if (timerTask!=null){
			timerTask.cancel();
			timerTask=null;
		}
		DataProcess.GetInstance().stopConn();
	}
}
