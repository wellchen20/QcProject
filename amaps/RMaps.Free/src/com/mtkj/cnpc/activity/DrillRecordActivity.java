package com.mtkj.cnpc.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.ByteString;
import com.mtkj.cnpc.activity.adapter.GridViewPhotoAdapter;
import com.mtkj.cnpc.activity.adapter.GridViewPhotoAdapter.IPhotoBack;
import com.mtkj.cnpc.activity.service.SendService;
import com.mtkj.cnpc.activity.utils.BitmapUtils;
import com.mtkj.cnpc.protocol.bean.DrillPoint;
import com.mtkj.cnpc.protocol.bean.DrillRecord;
import com.mtkj.cnpc.protocol.constants.DSSProtoDataConstants;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.constants.SysContants;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava;
import com.mtkj.cnpc.protocol.socket.DataProcess;
import com.mtkj.cnpc.protocol.utils.SocketUtils;
import com.mtkj.cnpc.sqlite.PointDBDao;
import com.mtkj.cnpc.R;
import com.robert.maps.applib.utils.TimeUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

//import okhttp3.Call;
//import okhttp3.MediaType;
//import okhttp3.MultipartBody;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;
/**
 * 新钻井界面*/
public class DrillRecordActivity extends Activity implements View.OnClickListener {

    TextView tv_stationNo;
    TextView tv_lineNo;
    TextView tv_pointNo;
//    TextView tv_lon;
//    TextView tv_lat;
//    TextView tv_height;
    TextView tv_designHig;
    TextView tv_arrivedTime;
    EditText et_activeHig;
    EditText et_remark;
    Button btn_submit;
    ImageView iv_back;
    Button btn_remove;
    GridView gv_photo;


    private DrillPoint drillPoint;
    private PointDBDao mPointDBDao;
    private List<String> mPhotoStrings = new LinkedList<String>();
    private SharedPreferences mPreferences;
    ProgressDialog progressDialog = null;
    /**
     * 拍照Pop
     */
    private View popView;
    private PopupWindow pop;
    private TextView tv_photo_graph, tv_photo_local, tv_cancle;
    private GridViewPhotoAdapter mPhotoAdapter;
    private String Filepath, imagePath;
    Bitmap myBitmap;
    private byte[] mContent;
    Uri uri_take;
    String userName;
    int count = 1;
    String stationNo_new;
    String stationNo_old;

    private LayoutInflater mInflater;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drill_record);
        getData();
        setViews();
        initPop();
        initProgressDialog();
        initData();
        setAdapters();
        setListeners();
    }

    private void getData() {
        mPointDBDao = new PointDBDao(this);
        String station = getIntent().getStringExtra("drillPoint");
        drillPoint = mPointDBDao.selectDrillPoint(station);
        stationNo_old = drillPoint.stationNo;
        mPreferences = getSharedPreferences(SysContants.SYSCONFIG, MODE_PRIVATE);
        userName = mPreferences.getString(SysContants.USERNAME,"");
    }

    private void setViews() {
        iv_back = (ImageView) findViewById(R.id.iv_back);
        tv_stationNo = (TextView) findViewById(R.id.tv_stationNo);
        tv_lineNo = (TextView) findViewById(R.id.tv_lineNo);
        tv_pointNo = (TextView) findViewById(R.id.tv_pointNo);
//        tv_lon = (TextView) findViewById(R.id.tv_lon);
//        tv_lat = (TextView) findViewById(R.id.tv_lat);
//        tv_height = (TextView) findViewById(R.id.tv_height);
        tv_designHig = (TextView) findViewById(R.id.tv_designHig);
        tv_arrivedTime = (TextView) findViewById(R.id.tv_arrivedTime);
        et_activeHig = (EditText) findViewById(R.id.et_activeHig);
        et_remark = (EditText) findViewById(R.id.et_remark);
        btn_submit = (Button) findViewById(R.id.btn_submit);
        btn_remove = (Button) findViewById(R.id.btn_remove);
        gv_photo = (GridView) findViewById(R.id.gv_photo);

        mInflater = LayoutInflater.from(DrillRecordActivity.this);
        Filepath = Environment.getExternalStorageDirectory().getPath() + "/rmaps/MyImg";
    }

    private void initPop() {
        popView = mInflater.inflate(R.layout.pop_photo, null);
        tv_photo_graph = (TextView) popView.findViewById(R.id.tv_photo_graph);
        tv_photo_local = (TextView) popView.findViewById(R.id.tv_photo_local);
        tv_cancle = (TextView) popView.findViewById(R.id.tv_cancle);

        pop = new PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
    }

    public void initProgressDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在上传任务...");
    }

    private void initData() {
        tv_stationNo.setText(drillPoint.stationNo);
        tv_lineNo.setText(drillPoint.lineNo);
        tv_pointNo.setText(drillPoint.spointNo);
//        tv_lon.setText(String.valueOf(drillPoint.geoPoint.getLongitude()));
//        tv_lat.setText(String.valueOf(drillPoint.geoPoint.getLatitude()));
//        tv_height.setText(drillPoint.Alt+"m");
        tv_designHig.setText(drillPoint.desWellDepth+"m");
        tv_arrivedTime.setText(TimeUtil.getCurrentTimeInString());
    }

    private void setAdapters() {
        mPhotoAdapter = new GridViewPhotoAdapter(DrillRecordActivity.this, mPhotoStrings, photoBack);
        gv_photo.setAdapter(mPhotoAdapter);
    }

    private void setListeners() {
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataProcess.GetInstance().isConnected()){
                    submitInfo();
                }else {
                    Toast.makeText(DrillRecordActivity.this,"未连接服务器，请重试。",Toast.LENGTH_SHORT).show();
//                    startService(new Intent(DrillRecordActivity.this, SendService.class));
                }
            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DrillRecordActivity.this,RemoveDrillActivity.class);
                intent.putExtra("stationNo",drillPoint.stationNo);
                startActivityForResult(intent,102);
            }
        });

        tv_cancle.setOnClickListener(this);
        tv_photo_graph.setOnClickListener(this);
        tv_photo_local.setOnClickListener(this);
    }

    private void submitInfo() {
        if (et_activeHig.getText().toString().equals("")||et_activeHig.getText().toString()==null){
          Toast.makeText(DrillRecordActivity.this,"实际井深不能为空！",Toast.LENGTH_SHORT).show();
        }else {
            final DrillRecord record = new DrillRecord();
            record.stationNo = drillPoint.stationNo;//桩号
            record.lineNo = drillPoint.lineNo;//线号
            record.spointNo = drillPoint.spointNo;//点号
            record.receivetime = tv_arrivedTime.getText().toString();//到达时间
            record.lon = String.valueOf(drillPoint.geoPoint.getLongitude());//经度
            record.lat = String.valueOf(drillPoint.geoPoint.getLatitude());//纬度
            record.drilldepth = et_activeHig.getText().toString();//实际井深
            record.remark = et_remark.getText().toString();//备注
            record.drilltime = TimeUtil.getCurrentTimeInString();//完成时间

            if (mPhotoStrings.size() == 3) {
                record.image1 = mPhotoStrings.get(0);
                record.image2 = mPhotoStrings.get(1);
                record.image3 = mPhotoStrings.get(2);
            } else if (mPhotoStrings.size() == 2) {
                record.image1 = mPhotoStrings.get(0);
                record.image2 = mPhotoStrings.get(1);
            } else if (mPhotoStrings.size() == 1) {
                record.image1 = mPhotoStrings.get(0);
            }

            mPointDBDao.insertDrillRecord(record);//插入钻井记录
           /* drillPoint.isDone = true;//标记钻井点已经完成信息录入
            mPointDBDao.updateDrillPoint(drillPoint);//更新钻井点状态*/
            if (progressDialog!=null && !progressDialog.isShowing()){
                progressDialog.show();
            }
            //上传图片
            new Thread(new Runnable() {
                @Override
                public void run() {
//                    uploadImg(record);
                }
            }).start();
        }

    }

    /**
     * DSCLOUD上传
     *
     * @param record
     */
    protected void uploadRecord(final DrillRecord record) {
        new UploadAnytask(record).execute("");
    }

    @Override
    public void onClick(View v) {
        File file = new File(Filepath);
        if (!file.exists()) {
            file.mkdir();
        }
        switch (v.getId()) {
            case R.id.tv_cancle:
                if (pop != null && pop.isShowing()) {
                    pop.dismiss();
                }
                break;

            case R.id.tv_photo_graph:
                Intent intentGraph = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // 指定开启系统相机的Action
                intentGraph.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                String nameGraph = userName+"_"+drillPoint.stationNo+"_"+count;
                // 把文件地址转换成Uri格式
                imagePath = Filepath + "/" + nameGraph + ".jpg";
                if (Build.VERSION.SDK_INT >= 24) {
                    uri_take = FileProvider.getUriForFile(DrillRecordActivity.this, "com.robert.maps.fileprovider", new File(imagePath));
                } else {
                    uri_take = Uri.fromFile(new File(imagePath));
                }
                intentGraph.putExtra(MediaStore.EXTRA_OUTPUT,uri_take);
                startActivityForResult(intentGraph, 100);
                if (pop != null && pop.isShowing()) {
                    pop.dismiss();
                }
                count++;
                break;

            case R.id.tv_photo_local:
                Intent intentLocal = new Intent(Intent.ACTION_GET_CONTENT);
                intentLocal.addCategory(Intent.CATEGORY_OPENABLE);
                intentLocal.setType("image/*");
                intentLocal.putExtra("return-data", true);
                startActivityForResult(intentLocal, 101);
                if (pop != null && pop.isShowing()) {
                    pop.dismiss();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        ContentResolver resolver = getContentResolver();
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case 100:
                    try {
                        mContent = BitmapUtils.readStream(resolver.openInputStream(Uri.parse(uri_take.toString())));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // 将字节数组转换为ImageView可调用的Bitmap对象
                    myBitmap = BitmapUtils.getPicFromBytes(mContent, null);
                    myBitmap = BitmapUtils.imageZoom(myBitmap, 300.00);
                    BitmapUtils.saveImage(myBitmap, imagePath);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                    mPhotoStrings.add(imagePath);
                    mPhotoAdapter.notifyDataSetChanged();
                    break;
                case 101:
                    try{
                        Uri selectedIcon = intent.getData();
                        // 将图片内容解析成字节数组
                        byte[] mContent = BitmapUtils.readStream(resolver.openInputStream(Uri.parse(selectedIcon.toString())));
                        // 将字节数组转换为ImageView可调用的Bitmap对象
                        myBitmap = BitmapUtils.getPicFromBytes(mContent, null);
                        //将字节换成KB
                        double mid1 = mContent.length / 1024;
                        if(mid1>300){
                            Dialog alertDialog = new AlertDialog.Builder(this).
                                    setTitle("确定压缩图片？").
                                    setMessage("您的图片过大，需要压缩处理吗？压缩可能导致您的图片清晰度下降，请谨慎！").
                                    setIcon(R.drawable.warning).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    myBitmap = BitmapUtils.imageZoom(myBitmap, 300.00);
                                    String nameGraph = userName+"_"+drillPoint.stationNo+"_"+count;
                                    // 把文件地址转换成Uri格式
                                    String path = Filepath + "/" + nameGraph + ".jpg";
                                    // 保存压缩图片
                                    BitmapUtils.saveImage(myBitmap, path);

                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                                    mPhotoStrings.add(path);
                                    mPhotoAdapter.notifyDataSetChanged();
                                    count++;
                                }
                            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).create();
                            alertDialog.show();
                        }else{
                            String nameGraph = userName+"_"+drillPoint.stationNo+"_"+count;
                            String path = Filepath + "/" + nameGraph + ".jpg";
                            // 保存压缩图片
                            BitmapUtils.saveImage(myBitmap, path);

                            mPhotoStrings.add(path);
                            mPhotoAdapter.notifyDataSetChanged();
                            count++;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;

                case 102:
                    stationNo_new = intent.getStringExtra("stationNo");
                    Log.e("chenwei", "station: "+stationNo_new);
                    drillPoint = mPointDBDao.selectDrillPoint(stationNo_new);
                    initData();
                    et_remark.setText("备注：原井桩号： "+stationNo_old+" 移井到 新井桩号： "+stationNo_new);
                    break;
                default:
                    break;
            }
        }
    }

//    public void uploadImg(final DrillRecord record){
//        if (mPhotoStrings!=null&&mPhotoStrings.size()!=0){
//            MultipartBody.Builder builder= new MultipartBody.Builder().setType(MultipartBody.FORM);
//            for (int i=0;i<mPhotoStrings.size();i++){
//                File file = new File(mPhotoStrings.get(i));
//                if (file!=null){
//                    builder.addFormDataPart("file",file.getName(), RequestBody.create(MediaType.parse("image/jpg"),file));
//                }
//            }
//            builder.addFormDataPart("station_no",drillPoint.stationNo);
//            MultipartBody requestBody = builder.build();
//            Request request = new Request.Builder()
//                    .url(SysConfig.url+"uploadfile")
//                    .post(requestBody)
//                    .build();
//            OkHttpClient client = new OkHttpClient();
//            client.newCall(request).enqueue(new okhttp3.Callback(){
//
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    e.printStackTrace();
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    String uploadResult = response.body().string();
//                    Log.e("uploadResult", uploadResult);
//                    uploadRecord(record);
//                }
//            });
//        }else {
//            uploadRecord(record);
//        }
//
//    }

    public class UploadAnytask extends AsyncTask<String, Integer, Boolean> {

        private DrillRecord mRecord;

        public UploadAnytask(DrillRecord drillRecord) {
            mRecord = drillRecord;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            if (SysConfig.SC_ID != null && !"".equals(SysConfig.SC_ID)) {
                try {
                    DSSProtoDataJava.Proto_DpRecord proto_DpRecord = DSSProtoDataJava.Proto_DpRecord.newBuilder()
                            .setStationNo(ByteString.copyFrom(mRecord.stationNo, "GB2312"))
                            .setSline(ByteString.copyFrom(mRecord.lineNo, "GB2312"))
                            .setSpoint(ByteString.copyFrom(mRecord.spointNo, "GB2312"))
                            .setDeviceID(Integer.valueOf(SysConfig.SC_ID))
                            .setDeviceName(ByteString.copyFrom(SysConfig.SC, "GB2312"))
                            .setReceiveTime(ByteString.copyFrom(mRecord.receivetime, "GB2312"))
                            .setShotTime(ByteString.copyFrom(mRecord.drilltime, "GB2312"))
                            .setWellLithology(ByteString.copyFrom("", "GB2312"))
                            .setWellNum(1)
                            .setLon(drillPoint.geoPoint.getLongitude())
                            .setLat(drillPoint.geoPoint.getLatitude())
                            .setDrillDepth(Double.valueOf(mRecord.drilldepth))
                            .setBombDepth(Double.valueOf("0"))
                            .setBombWeight(Float.valueOf("0"))
                            .setDetonator(Integer.valueOf("0"))
                            .setBombId(ByteString.copyFrom("", "GB2312"))
                            .setDetonatorId(ByteString.copyFrom("", "GB2312"))
                            .setStatusId(502)
                            .build();
                    DSSProtoDataJava.Proto_Head head = DSSProtoDataJava.Proto_Head.newBuilder()
                            .setProtoMsgType(DSSProtoDataConstants.ProtoMsgType.protoMsgType_DpRecord)
                            .setCmdSize(proto_DpRecord.toByteArray().length)
                            .addReceivers(ByteString.copyFrom("dscloud", "GB2312"))
                            .setReceivers(0, ByteString.copyFrom("dscloud", "GB2312"))
                            .setSender(ByteString.copyFrom(SysConfig.SC, "GB2312"))
                            .setMsgId(0).setPriority(1).setExpired(0).build();
                    if (DataProcess.isLoginDscloud) {

                        return DataProcess.GetInstance().sendData(
                                SocketUtils.writeBytes(head.toByteArray(),
                                        proto_DpRecord.toByteArray()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (progressDialog!=null){
                progressDialog.dismiss();
            }
            if (result) {
                mRecord.isupload = "1";
                mPointDBDao.updateDrilRecord(mRecord);
            }
            Intent intentSure = new Intent();
            intentSure.putExtra("station", drillPoint.stationNo);
            setResult(RESULT_OK, intentSure);
            finish();
        }
    }

    private IPhotoBack photoBack = new IPhotoBack() {
        @Override
        public void addPhoto() {
            pop.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#b0000000")));
            pop.showAtLocation(btn_remove, Gravity.BOTTOM, 0, 0);
            pop.setAnimationStyle(R.style.app_pop);
            pop.setOutsideTouchable(true);
            pop.setFocusable(true);
            pop.update();
        }
    };
}
