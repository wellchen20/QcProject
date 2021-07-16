package com.xylink.sdk.sample;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.log.L;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ainemo.sdk.otf.ConnectNemoCallback;
import com.ainemo.sdk.otf.LoginResponseData;
import com.ainemo.sdk.otf.NemoSDK;
import com.robert.maps.applib.R;

import io.reactivex.annotations.Nullable;

/**
 * 登录界面
 * @author zhangyazhou
 */

public class LoginActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "XYLink_LoginActivity";
    private NemoSDK nemoSDK = NemoSDK.getInstance();
    private EditText displayName;
    private EditText externalId;
    private ProgressDialog loginDialog;
    private String name = "";
    private String password = "";
    private boolean islogin = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_xy);
        findViewById(R.id.sign).setOnClickListener(this);
        findViewById(R.id.connect_nemo).setOnClickListener(this);
        findViewById(R.id.login).setOnClickListener(this);
        displayName = findViewById(R.id.display_name);
        externalId = findViewById(R.id.register_external_id);
        islogin = getIntent().getBooleanExtra("islogin",false);
        if (islogin){
            name = getIntent().getStringExtra("name");
            password = getIntent().getStringExtra("password");
            displayName.setText(name);
            externalId.setText(password);
            login();
        }else {
            displayName.setHint("请输入手机号/邮箱");
            externalId.setHint("请输入密码");
        }
        checkPermission();
    }

    private void checkPermission() {
        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) &&
                !(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 0);
        } else if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
        } else if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        } else if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW}, 0);
        }
    }

    @Override
    public void onClick(View v) {

        int i = v.getId();
        if (i == R.id.connect_nemo) {
            login();
        } else if (i == R.id.sign) {
        } else if (i == R.id.login) {
        }
    }

    public void login(){
        if (displayName.getText().toString().length() == 0 || externalId.getText().toString().length() == 0) {
            Toast.makeText(getApplicationContext(), "displayName或externalId为空!", Toast.LENGTH_SHORT).show();
            return;
        }
        showLoginDialog();
        nemoSDK.loginXYlinkAccount(displayName.getText().toString(), externalId.getText().toString(), new ConnectNemoCallback() {
            @Override
            public void onFailed(final int i) {
                dismissDialog();
                L.e(TAG, "使用小鱼账号登录失败，错误码：" + i);
                try {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show());
                } catch (Exception e) {
                    L.e(TAG, e.getMessage());
                }

            }

            @Override
            public void onSuccess(LoginResponseData data, boolean isDetectingNetworkTopology) {
                dismissDialog();
                L.i(TAG, "匿名登录成功，号码为：" + data.getCallNumber());
                try {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "小鱼账号登录成功", Toast.LENGTH_SHORT).show());
                } catch (Exception e) {
                    L.e(TAG, e.getMessage());
                }
                saveNumber(displayName.getText().toString(),externalId.getText().toString(),1);//保存小鱼账号
                Intent intent = new Intent(LoginActivity.this, CallActivity.class);
                intent.putExtra("MY_NUMBER", data.getCallNumber());
                intent.putExtra("displayName", displayName.getText().toString());
                L.i(TAG, "displayNameCallActivity11=" + displayName.getText().toString() + "MY_NUMBER" + data.getCallNumber());
                startActivity(intent);
            }

            @Override
            public void onNetworkTopologyDetectionFinished(LoginResponseData resp) {
                L.i(TAG, "net detect onNetworkTopologyDetectionFinished 2");
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "网络探测已完成", Toast.LENGTH_SHORT).show());
            }
        });
    }
    private void showLoginDialog() {
        loginDialog = new ProgressDialog(this);
        loginDialog.setTitle("登录");
        loginDialog.setMessage("正在登录,请稍后...");
        loginDialog.setCancelable(false);
        loginDialog.show();
    }

    private void dismissDialog() {
        if (loginDialog != null && loginDialog.isShowing()) {
            loginDialog.dismiss();
        }
    }

    public void saveNumber(String name,String password,int type){
        SharedPreferences preferences = getSharedPreferences("xytest", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("name",name);
        editor.putString("password",password);
        editor.putInt("type",type);
        editor.putBoolean("flag",true);
        editor.commit();
    }
}
