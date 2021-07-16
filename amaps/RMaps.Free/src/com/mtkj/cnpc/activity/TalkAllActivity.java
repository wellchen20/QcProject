package com.mtkj.cnpc.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ByteString;
import com.mtkj.cnpc.activity.adapter.TalkAllAdapter;
import com.mtkj.cnpc.protocol.constants.DSSProtoDataConstants;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.constants.SysContants;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava;
import com.mtkj.cnpc.protocol.socket.DataProcess;
import com.mtkj.cnpc.protocol.utils.SocketUtils;
import com.mtkj.cnpc.sqlite.PointDBDao;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class TalkAllActivity extends Activity{

    ImageView iv_back;
    ImageView iv_menu;
    TextView tv_name;
    EditText et_content;
    Button btn_send;
    ListView list;
    TalkAllAdapter adapter;
    ContactPersons contactPersons;
    List<TalkEntity> talkEntities = new ArrayList<>();;
    String ACTION_NEWS_ALL = "action_news_all";
    GetNewsReceiver getNewsReceiver;
    protected PointDBDao mPointDBDao;
    private SharedPreferences mPreferences;
    String tel;
    String username;
    String xml;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk_all);
        mPointDBDao = new PointDBDao(this);
        getMessages();
        setViews();
        setAdapters();
        setListeners();
        registReceiver();
    }

    private void getMessages() {
        mPreferences = getSharedPreferences(SysContants.SYSCONFIG, MODE_PRIVATE);
        tel = mPreferences.getString(SysContants.TEL, "");
        username = mPreferences.getString(SysContants.USERNAME,"");
        contactPersons = (ContactPersons) getIntent().getSerializableExtra("contactPersons");
        if (contactPersons==null){
            new MyGetPersonsAsync().execute();
        }
    }

    private void registReceiver(){
        getNewsReceiver = new GetNewsReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(ACTION_NEWS_ALL);
        registerReceiver(getNewsReceiver, filter);
    }


    private void setViews() {
        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_menu = (ImageView) findViewById(R.id.iv_menu);
        tv_name = (TextView) findViewById(R.id.tv_name);
        et_content = (EditText) findViewById(R.id.et_content);
        btn_send = (Button) findViewById(R.id.btn_send);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_name.setText("聊天室");
        list = (ListView) findViewById(R.id.list);
    }

    private void setAdapters() {
        talkEntities = mPointDBDao.getTalk("szdzd");
        Log.e("talkEntities", talkEntities.size()+"" );
        adapter = new TalkAllAdapter(TalkAllActivity.this,talkEntities);
        list.setAdapter(adapter);
        list.setSelection(adapter.getCount());
    }

    private void setListeners() {
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MySendAsync().execute(et_content.getText().toString());
            }
        });

        iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClearDataDialog();
            }
        });
    }

    private Dialog dialog;
    public void showClearDataDialog() {
        dialog = DialogUtils.Alert(TalkAllActivity.this, "提示", "清除当前聊天记录？",
                new String[]{TalkAllActivity.this.getString(R.string.ok), TalkAllActivity.this.getString(R.string.cancel)},
                new View.OnClickListener[]{new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // 清空数据库
                        mPointDBDao.deleteTalkWithDevice("szdzd");
                        talkEntities.clear();
                        adapter.notifyDataSetChanged();
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

    class MySendAsync extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String content = params[0];
            TalkEntity talkEntity = new TalkEntity();
            talkEntity.setContent(content);
            talkEntity.setTime(new SimpleDateFormat("MM-dd HH:mm").format(new Date()));
            talkEntity.setType_who(0);
            talkEntity.setType_talk(0);
            talkEntity.setDevice("szdzd");
            talkEntity.setName(username);
            talkEntity.setMsgtype(3);
            talkEntities.add(talkEntity);//添加到listview
            String msg = JSON.toJSONString(talkEntity);//转化成json转发给用户

            Collection coll = new Vector();
            for (int i=0;i<contactPersons.getUserlist().size();i++){
                ByteString people = null;
                try {
                    people = ByteString.copyFrom(contactPersons.getUserlist().get(i).getDevice(), "GB2312");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                coll.add(people);
            }

            try {
                DSSProtoDataJava.Proto_Notice proto_notice =
                        DSSProtoDataJava.Proto_Notice.newBuilder().setMsg(ByteString.copyFrom(msg, "UTF-8")).
                                build();
                DSSProtoDataJava.Proto_Head head = DSSProtoDataJava.Proto_Head.newBuilder()
                        .setProtoMsgType(DSSProtoDataConstants.ProtoMsgType.ProtoMsgType_Notice)
                        .setCmdSize(proto_notice.toByteArray().length)
                        .addAllReceivers(coll)
                        .setSender(ByteString.copyFrom("", "GB2312"))
                        .setPriority(1).setExpired(0).build();
                DataProcess.GetInstance().sendData(SocketUtils.writeBytes(head.toByteArray(), proto_notice.toByteArray()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            mPointDBDao.insertTalk(talkEntity);//存储数据库

            return true;
        }


        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            et_content.setText("");
            adapter.notifyDataSetChanged();
            list.setSelection(adapter.getCount());
        }
    }

    class GetNewsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(ACTION_NEWS_ALL)){
                //判断传过来的device跟当前device一样，一样就显示 不一样不显示
                TalkEntity talkEntity = (TalkEntity) intent.getSerializableExtra("talkEntity");
                if (talkEntity.getDevice().equals("szdzd")){
                    talkEntities.add(talkEntity);
                    adapter.notifyDataSetChanged();
                    list.setSelection(adapter.getCount());
                }
            }
        }
    }

    public void initOkHttp() throws IOException {
        // TODO Auto-generated method stub
        //1>创建HttpClient对象
        HttpClient client=new DefaultHttpClient();
        //2>创建HttpGet请求对象
        String urls= SysConfig.url+"get_userlist/"+tel;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(getNewsReceiver);
    }

    class MyGetPersonsAsync extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                initOkHttp();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
                //移除自己
                for (int i=0;i<contactPersons.getUserlist().size();i++){
                    if (tel.equals(contactPersons.getUserlist().get(i).getPhone())){
                        contactPersons.getUserlist().remove(i);
                    }
                }
                ContactPersons.UserlistBean user = new ContactPersons.UserlistBean();
                user.setDevice("szdzd");
                user.setName(username);
                user.setOid(0);
                user.setStatus(101);
                user.setPhone("");
                contactPersons.getUserlist().add(0,user);
        }
    }
}
