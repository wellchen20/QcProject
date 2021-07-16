package com.mtkj.cnpc.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.mtkj.cnpc.activity.adapter.CreditRecordAdapter;
import com.mtkj.cnpc.activity.utils.CycleView;
import com.mtkj.cnpc.protocol.constants.SysContants;
import com.mtkj.utils.entity.CreditsEntity;
import com.mtkj.utils.entity.RecordEntity;
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

public class MyCreditActivity extends Activity {

    CycleView cyl_credit;
    Button btn_getMoney;
    ImageView iv_back;
    ListView lv_credit;
    public static String credit = "0";
    private SharedPreferences mPreferences;
    private String tel;
    Handler handler;
    CreditsEntity creditsEntity;
    RecordEntity recordEntity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_credit);
        setViews();
        getCredit();
        setAdapters();
        setListeners();
    }

    private void getCredit() {
        new MyCreditListAsync().execute();
        new MyCreditAsync().execute();
    }

    private void setViews() {
        mPreferences = getSharedPreferences(SysContants.SYSCONFIG, MODE_PRIVATE);
        tel = mPreferences.getString(SysContants.TEL, "");
        cyl_credit = (CycleView) findViewById(R.id.cyl_credit);
        btn_getMoney = (Button) findViewById(R.id.btn_getMoney);
        lv_credit = (ListView) findViewById(R.id.lv_credit);
        iv_back = (ImageView) findViewById(R.id.iv_back);
    }

    private void setAdapters() {

    }

    private void setListeners() {
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what==0){
                    credit = creditsEntity.getCredit()+"";

                }
            }
        };

        btn_getMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MyCreditActivity.this,"功能开发中...",Toast.LENGTH_SHORT).show();
            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void initOkHttp(int flag) throws IOException {
        // TODO Auto-generated method stub
        //1>创建HttpClient对象
        HttpClient client=new DefaultHttpClient();
        //2>创建HttpGet请求对象
        String urls = "";
        if (flag==0){
            urls=url+"credit/getcredit/"+tel;
        }else if (flag==1){
            urls=url+"credit/getcreditrecords/"+tel;
        }
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
            Message msg = new Message();
            if (flag==0){
                creditsEntity = JSON.parseObject(xml, CreditsEntity.class);
                msg.what = 0;
                handler.sendMessage(msg);
            }else if (flag==1){
                recordEntity = JSON.parseObject(xml,RecordEntity.class);
            }

        }else{ //不正常
            Log.e("info", "请求发送失败...状态码不是200，是:"+code);
        }
    }

    class MyCreditAsync extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                initOkHttp(0);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            for(int i=0;i<=100;i++){
                cyl_credit.setProgress(i);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    class MyCreditListAsync extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                initOkHttp(1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            //recordEntity
            CreditRecordAdapter adapter = new CreditRecordAdapter(MyCreditActivity.this,recordEntity);
            lv_credit.setAdapter(adapter);
        }
    }
}
