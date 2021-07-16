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
import com.mtkj.cnpc.activity.adapter.TalkAdapter;
import com.mtkj.cnpc.protocol.constants.DSSProtoDataConstants;
import com.mtkj.cnpc.protocol.constants.SysContants;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava;
import com.mtkj.cnpc.protocol.socket.DataProcess;
import com.mtkj.cnpc.protocol.utils.SocketUtils;
import com.mtkj.cnpc.sqlite.PointDBDao;
import com.mtkj.utils.entity.ContactPersons;
import com.mtkj.utils.entity.TalkEntity;
import com.mtkj.cnpc.R;
import com.robert.maps.applib.utils.DialogUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TalkActivity extends Activity{

    ImageView iv_back;
    TextView tv_name;
    EditText et_content;
    Button btn_send;
    ImageView iv_menu;
    ListView list;
    TalkAdapter adapter;
    ContactPersons.UserlistBean person;
    List<TalkEntity> talkEntities = new ArrayList<>();;
    String ACTION_GET_NEWS = "action_get_new";
    GetNewsReceiver getNewsReceiver;
    protected PointDBDao mPointDBDao;
    String device;
    private SharedPreferences mPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);
        mPointDBDao = new PointDBDao(this);
        getMessages();
        setViews();
        setAdapters();
        setListeners();
        registReceiver();
    }

    private void getMessages() {
        person = (ContactPersons.UserlistBean) getIntent().getSerializableExtra("person");
        Log.e("person", person.getName()+"||"+person.getDevice()+"||"+person.getPhone());
        mPreferences = getSharedPreferences(SysContants.SYSCONFIG, MODE_PRIVATE);
        device = mPreferences.getString(SysContants.DEVICE, "");
    }

    private void registReceiver(){
        getNewsReceiver = new GetNewsReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(ACTION_GET_NEWS);
        registerReceiver(getNewsReceiver, filter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_OK);
    }

    private void setViews() {
        iv_back = (ImageView) findViewById(R.id.iv_back);
        tv_name = (TextView) findViewById(R.id.tv_name);
        et_content = (EditText) findViewById(R.id.et_content);
        btn_send = (Button) findViewById(R.id.btn_send);
        tv_name = (TextView) findViewById(R.id.tv_name);
        iv_menu = (ImageView) findViewById(R.id.iv_menu);
        tv_name.setText(person.getName());
        list = (ListView) findViewById(R.id.list);
    }

    private void setAdapters() {
        talkEntities = mPointDBDao.getTalk(person.getDevice());
        adapter = new TalkAdapter(TalkActivity.this,talkEntities);
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

    class MySendAsync extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String content = params[0];
            TalkEntity talkEntity = new TalkEntity();
            talkEntity.setContent(content);
            talkEntity.setTime(new SimpleDateFormat("MM-dd HH:mm").format(new Date()));
            talkEntity.setType_who(0);
            talkEntity.setType_talk(0);
            talkEntity.setDevice(device);
            talkEntity.setName(person.getName());
            talkEntity.setMsgtype(3);
            talkEntities.add(talkEntity);//添加到listview
            String msg = JSON.toJSONString(talkEntity);//转化成json转发给用户
            try {
                DSSProtoDataJava.Proto_Notice proto_notice =
                        DSSProtoDataJava.Proto_Notice.newBuilder().setMsg(ByteString.copyFrom(msg, "UTF-8")).
                                build();
                DSSProtoDataJava.Proto_Head head = DSSProtoDataJava.Proto_Head.newBuilder()
                        .setProtoMsgType(DSSProtoDataConstants.ProtoMsgType.ProtoMsgType_Notice)
                        .setCmdSize(proto_notice.toByteArray().length)
                        .addReceivers(ByteString.copyFrom(person.getDevice(), "GB2312"))
//                        .setReceivers(0, ByteString.copyFrom(person.getDevice(), "GB2312"))
                        .setSender(ByteString.copyFrom("", "GB2312"))
                        .setPriority(1).setExpired(0).build();
                DataProcess.GetInstance().sendData(SocketUtils.writeBytes(head.toByteArray(), proto_notice.toByteArray()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            talkEntity.setDevice(person.getDevice());
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
            if(action.equals(ACTION_GET_NEWS)){
                //判断传过来的device跟当前device一样，一样就显示 不一样不显示
                TalkEntity talkEntity = (TalkEntity) intent.getSerializableExtra("talkEntity");
                if (talkEntity.getDevice().equals(person.getDevice())){
                    talkEntities.add(talkEntity);
                    adapter.notifyDataSetChanged();
                    list.setSelection(adapter.getCount());
                }
            }
        }
    }
    private Dialog dialog;
    public void showClearDataDialog() {
        dialog = DialogUtils.Alert(TalkActivity.this, "提示", "清除当前聊天记录？",
                new String[]{TalkActivity.this.getString(R.string.ok), TalkActivity.this.getString(R.string.cancel)},
                new View.OnClickListener[]{new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // 清空数据库
                        mPointDBDao.deleteTalkWithDevice(person.getDevice());
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(getNewsReceiver);
    }
}
