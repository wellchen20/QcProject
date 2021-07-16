package com.mtkj.cnpc.activity;

import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.google.protobuf.ByteString;
import com.mtkj.cnpc.activity.service.SendService;
import com.mtkj.cnpc.protocol.constants.DSSProtoDataConstants;
import com.mtkj.cnpc.protocol.constants.DSSProtoDataConstants.ProtoMsgType;
import com.mtkj.cnpc.protocol.constants.DSSProtoDataConstants.businessType;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.constants.SysConfig.WorkType;
import com.mtkj.cnpc.protocol.constants.SysContants;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_Head;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_UserLogin_Request;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_UserLogin_Response;
import com.mtkj.cnpc.protocol.socket.DataProcess;
import com.mtkj.cnpc.protocol.utils.SocketUtils;
import com.mtkj.cnpc.R;
import com.mtkj.utils.StatusBarUtil;

/**
 * 登录页面
 *
 */
public class LoginActivity extends Activity {
	public static boolean isLogin = false;
	private EditText usernameEditText;
	private SharedPreferences mPreferences;
	private String currentUsername;
	private ProgressDialog pd;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		StatusBarUtil.setTheme(this);
		mPreferences = getSharedPreferences(SysContants.SYSCONFIG, MODE_PRIVATE);
		usernameEditText = (EditText) findViewById(R.id.username);

		String name = getData(SysContants.TEL, "");
		if (name != null && !"".equals(name)) {
			usernameEditText.setText(name);
		}
		usernameEditText.requestFocus();
		findViewById(R.id.tv_login).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				login();
			}
		});
		initPb();
	}

	private void initPb() {
		pd = new ProgressDialog(LoginActivity.this);
		pd.setCanceledOnTouchOutside(false);
		pd.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
			}
		});
		pd.setMessage(getString(R.string.Is_landing));
	}

	@Override
	protected void onDestroy() {
		if (pd!=null){
			pd.dismiss();
		}
		super.onDestroy();
	}

	/**
	 * 登录
	 *
	 * @param
	 */
	public void login() {
		currentUsername = usernameEditText.getText().toString().trim();
		if (TextUtils.isEmpty(currentUsername)) {
			if (pd != null && pd.isShowing()
					&& !LoginActivity.this.isFinishing()) {
				pd.dismiss();
			}
			Toast.makeText(this, R.string.User_name_cannot_be_empty,
					Toast.LENGTH_SHORT).show();
			return;
		}

		if ("12345678".equals(currentUsername)){
			setData(SysContants.TEL,"12345678");
			setData(SysContants.USERNAME,"TYPE_ARRANGE");
			setData(SysContants.ISLOGIN,true);
			setData(SysContants.WORK_TYPE,WorkType.WORK_TYPE_ARRANGE);
		}else if ("87654321".equals(currentUsername)){
			setData(SysContants.TEL,"87654321");
			setData(SysContants.USERNAME,"TYPE_DRILE");
			setData(SysContants.ISLOGIN,true);
			setData(SysContants.WORK_TYPE,WorkType.WORK_TYPE_DRILE);
		}else {
			Toast.makeText(this, "用户不存在",
					Toast.LENGTH_SHORT).show();
			return;
		}
		startActivity(new Intent(LoginActivity.this,MainActivity.class));
		finish();
	}


	public void server_setting(View view) {
		startActivity(new Intent(LoginActivity.this,
				ServerSettingActivity.class));
	}

	public void forget(View view) {

	}

	@Override
	protected void onResume() {
		super.onResume();
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
	protected void showMessage(String msg) {
		Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
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
}
