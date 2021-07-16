package com.mtkj.cnpc.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ByteString;
import com.mtkj.cnpc.activity.service.SendService;
import com.mtkj.cnpc.protocol.bean.ArrangePoint;
import com.mtkj.cnpc.protocol.bean.ArrangeRecord;
import com.mtkj.cnpc.protocol.constants.DSSProtoDataConstants;
import com.mtkj.cnpc.protocol.constants.SysContants;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava;
import com.mtkj.cnpc.protocol.socket.DataProcess;
import com.mtkj.cnpc.protocol.utils.SocketUtils;
import com.mtkj.cnpc.sqlite.PointDBDao;
import com.mtkj.utils.entity.ArrangeEntity;
import com.mtkj.cnpc.R;
import com.robert.maps.applib.utils.TimeUtil;

public class ArrangeRecordActivity extends Activity {

    TextView tv_stationNo;
    TextView tv_lineNo;
    TextView tv_pointNo;
    TextView tv_lon;
    TextView tv_lat;
    ImageView iv_back;
    EditText et_remark;
    EditText et_description;
    Button btn_submit;
    RelativeLayout rl_all;
    TextView tv_in;
    ImageView iv_checkbox;
    private PointDBDao mPointDBDao;
    private SharedPreferences mPreferences;
    ArrangePoint arrangePoint;
    ArrangeRecord arrangeRecord;
    String userName;
    ProgressDialog progressDialog = null;
    ArrangeEntity arrangeEntity;
    String station;
    String[] data = {"破损","损坏","丢失","其他"};
    ListView lv;
    MyAdapter adapter;
    PopupWindow popupWindow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arrange_record);
        initData();
        initProgressDialog();
        setViews();
        setListeners();
    }

    private void initData() {
        mPointDBDao = new PointDBDao(this);
        station = getIntent().getStringExtra("arrangePoint");
        arrangePoint = mPointDBDao.selectArrangePoint(station);
        mPreferences = getSharedPreferences(SysContants.SYSCONFIG, MODE_PRIVATE);
        userName = mPreferences.getString(SysContants.USERNAME,"");
    }

    private void setViews() {
        tv_stationNo = (TextView) findViewById(R.id.tv_stationNo);
        tv_lineNo = (TextView) findViewById(R.id.tv_lineNo);
        tv_pointNo = (TextView) findViewById(R.id.tv_pointNo);
        tv_lon = (TextView) findViewById(R.id.tv_lon);
        tv_lat = (TextView) findViewById(R.id.tv_lat);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        et_remark = (EditText) findViewById(R.id.et_remark);
        btn_submit = (Button) findViewById(R.id.btn_submit);
        et_description = (EditText) findViewById(R.id.et_description);

        tv_in = (TextView) findViewById(R.id.tv_in);
        iv_checkbox = (ImageView) findViewById(R.id.iv_checkbox);
        rl_all = (RelativeLayout) findViewById(R.id.rl_all);


        tv_stationNo.setText(arrangePoint.stationNo);
        tv_lineNo.setText(arrangePoint.lineNo);
        tv_pointNo.setText(arrangePoint.spointNo);
        tv_lon.setText(arrangePoint.geoPoint.getLongitude()+"");
        tv_lat.setText(arrangePoint.geoPoint.getLatitude()+"");

        lv = new ListView(ArrangeRecordActivity.this);
        adapter = new MyAdapter();
        lv.setAdapter(adapter);
    }

    public void initProgressDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在上传任务...");
    }

    private void setListeners() {
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_description.getText().toString().equals("")||et_description.getText().toString()==null){
                    Toast.makeText(ArrangeRecordActivity.this,"描述不能为空！",Toast.LENGTH_SHORT).show();
                }else {
                    if (progressDialog!=null && !progressDialog.isShowing()){
                        progressDialog.show();
                    }
                    if (DataProcess.GetInstance().isConnected()){
                        new UploadAnytask().execute();
                    }else {
                        Toast.makeText(ArrangeRecordActivity.this,"未连接服务器，请重试。",Toast.LENGTH_SHORT).show();
//                        startService(new Intent(ArrangeRecordActivity.this, SendService.class));
                    }
                }
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tv_in.setText(data[position]);
                if (popupWindow!=null && popupWindow.isShowing()){
                    popupWindow.dismiss();
                }
            }
        });

        rl_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(popupWindow==null){
                    popupWindow = new PopupWindow(ArrangeRecordActivity.this);
                    popupWindow.setContentView(lv);
                    popupWindow.setWidth(rl_all.getWidth());
                    popupWindow.setHeight(500);
                    popupWindow.setFocusable(true);
                }
                popupWindow.showAsDropDown(rl_all,0,0);
            }
        });
    }


    public void submitInfo(){
        arrangeEntity = new ArrangeEntity();
        arrangeEntity.setUser(userName);
        arrangeEntity.setDescription(et_description.getText().toString());
        arrangeEntity.setRemark(et_remark.getText().toString());
        arrangeEntity.setStationno(station);
        arrangeEntity.setTime(TimeUtil.getCurrentTimeInString());
        arrangeEntity.setMsgtype(7);
        String json = JSON.toJSONString(arrangeEntity);
        try {
            DSSProtoDataJava.Proto_Notice proto_notice =
                    DSSProtoDataJava.Proto_Notice.newBuilder().setMsg(ByteString.copyFrom(json, "GB2312")).
                            build();
            DSSProtoDataJava.Proto_Head head = DSSProtoDataJava.Proto_Head.newBuilder()
                    .setProtoMsgType(DSSProtoDataConstants.ProtoMsgType.ProtoMsgType_Notice)
                    .setCmdSize(proto_notice.toByteArray().length)
                    .addReceivers(ByteString.copyFrom("DSCMAIN", "GB2312"))
                    .setSender(ByteString.copyFrom("", "GB2312"))
                    .setPriority(1).setExpired(0).build();
            DataProcess.GetInstance().sendData(SocketUtils.writeBytes(head.toByteArray(), proto_notice.toByteArray()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class UploadAnytask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            submitInfo();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (progressDialog!=null && progressDialog.isShowing()){
                progressDialog.dismiss();
            }
            //更新排列炮点信息
//            arrangePoint.time = TimeUtil.getCurrentTimeInString();//完成时间
//            arrangePoint.remark =et_remark.getText().toString();
            arrangePoint.isDone = true;
            mPointDBDao.updateArrangePoint(arrangePoint);

            arrangeRecord = new ArrangeRecord();
            arrangeRecord.setStationNo(arrangePoint.stationNo);
            arrangeRecord.setLineNo(arrangePoint.lineNo);
            arrangeRecord.setSpointNo(arrangePoint.spointNo);
            arrangeRecord.setIsupload("1");
            arrangeRecord.setRemark(et_remark.getText().toString());
            arrangeRecord.setDescription(et_description.getText().toString());
            arrangeRecord.setTime(arrangeEntity.getTime());
            arrangeRecord.setLat(arrangePoint.geoPoint.getLatitude());
            arrangeRecord.setLon(arrangePoint.geoPoint.getLongitude());
            mPointDBDao.insertArrangeRecord(arrangeRecord);
            Intent intentSure = new Intent();
            intentSure.putExtra("station", arrangePoint.stationNo);
            setResult(RESULT_OK, intentSure);
            finish();
        }
    }

    class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return data.length;
        }

        @Override
        public Object getItem(int position) {
            return data[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = View.inflate(ArrangeRecordActivity.this,R.layout.item_arrange_list,null);
            TextView tv_content= (TextView) convertView.findViewById(R.id.tv_content);
            tv_content.setText(data[position]);
            return convertView;
        }
    }
}
