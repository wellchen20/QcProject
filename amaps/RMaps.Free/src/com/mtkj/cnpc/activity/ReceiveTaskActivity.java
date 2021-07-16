package com.mtkj.cnpc.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mtkj.cnpc.activity.adapter.TaskArrangesReceiveAdapter;
import com.mtkj.cnpc.activity.adapter.TaskDrillsReceiveAdapter;
import com.mtkj.cnpc.activity.adapter.TaskShotsReceiveAdapter;
import com.mtkj.cnpc.activity.interfaces.HttpUtil;
import com.mtkj.cnpc.activity.interfaces.RetrofitService;
import com.mtkj.cnpc.activity.utils.ShotTaskEntity;
import com.mtkj.cnpc.protocol.bean.ArrangePoint;
import com.mtkj.cnpc.protocol.bean.DrillPoint;
import com.mtkj.cnpc.protocol.bean.ShotPoint;
import com.mtkj.cnpc.protocol.bean.TaskPoint;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.constants.SysContants;
import com.mtkj.cnpc.sqlite.PointDBDao;
import com.mtkj.utils.StatusBarUtil;
import com.mtkj.utils.entity.AllPointsTask;
import com.mtkj.utils.entity.DrillTaskEntity;
import com.mtkj.cnpc.R;
import com.robert.maps.applib.utils.DialogUtils;

import org.andnav.osm.util.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import rx.Subscriber;

public class ReceiveTaskActivity extends Activity {

    TextView tv_task_yes;
    TextView tv_task_no;
    ListView lv_view;
    Button btn_submit;
    ImageView iv_back;
    private PointDBDao mPointDBDao;
    private List<DrillPoint> mAllDrills = new ArrayList<>();//所有
    private List<DrillPoint> mIsDoneDrills = new ArrayList<>();//已完成
    private List<ShotPoint> mAllShots = new ArrayList<>();//所有
    private List<ShotPoint> mIsDoneShots = new ArrayList<>();//已完成
    private List<ArrangePoint> mAllArranges = new ArrayList<>();//所有
    private List<ArrangePoint> mIsDoneArranges = new ArrayList<>();//已完成
    private TaskDrillsReceiveAdapter drillsAdapter;
    private TaskShotsReceiveAdapter shotsAdapter;
    private TaskArrangesReceiveAdapter arrangesAdapter;
    private int task_total = 0;
    private int task_isDone = 0;
    private int task_unDone = 0;
    private SharedPreferences mPreferences;

    String xml;
    DrillTaskEntity drillTaskEntity;
    ShotTaskEntity shotTaskEntity;
    AllPointsTask allPointsTask;
    ProgressDialog progressDialog = null;
    private String tel;
    int arrange_type = 14;
    int drill_type = 15;
    int explosive_type = 16;
    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_task);
        StatusBarUtil.setTheme(this);
        setViews();
        initProgressDialog();
        initData();
        setAdapters();
        setListeners();
    }


    public void initProgressDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在导入任务...");
    }
    private void setListeners() {
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SysConfig.workType == SysConfig.WorkType.WORK_TYPE_DRILE){
//                    showClearDataDialog(0);
                    showClearDataDialog(drill_type);
                }else if (SysConfig.workType == SysConfig.WorkType.WORK_TYPE_ARRANGE){
                    showClearDataDialog(arrange_type);
                }else {
                    if (progressDialog!=null && progressDialog.isShowing()){
                        progressDialog.dismiss();
                        Toast.makeText(ReceiveTaskActivity.this,"暂无可领取任务！",Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        lv_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TaskPoint taskPoint = null;
                if (SysConfig.workType == SysConfig.WorkType.WORK_TYPE_SHOT) {
                    taskPoint = mPointDBDao.selectShotPointTotaskPoint(mAllShots.get(position).stationNo);
                } else if (SysConfig.workType == SysConfig.WorkType.WORK_TYPE_DRILE) {
                    taskPoint = mPointDBDao.selectDrillPointToTaskPoint(mAllDrills.get(position).stationNo);
                }else if (SysConfig.workType == SysConfig.WorkType.WORK_TYPE_ARRANGE){
                    taskPoint = mPointDBDao.selectArrangePoint(mAllArranges.get(position).stationNo);
                }
                if (taskPoint != null && !"".equals(taskPoint.stationNo)) {
                    Log.e("lv_view", "lv_view" );
                    Intent intent = new Intent();
                    intent.putExtra("stationNo", taskPoint.stationNo);
                    setResult(200, intent);
                    finish();
                }
            }
        });
    }

    private void initData() {
        if (SysConfig.workType == SysConfig.WorkType.WORK_TYPE_DRILE){
            task_total = mAllDrills.size();
            task_isDone = mIsDoneDrills.size();
            task_unDone = task_total-task_isDone;
            tv_task_yes.setText(task_isDone+"");
            tv_task_no.setText(task_unDone+"");
        }else if (SysConfig.workType == SysConfig.WorkType.WORK_TYPE_SHOT){
            task_total = mAllShots.size();
            task_isDone = mIsDoneShots.size();
            task_unDone = task_total-task_isDone;
            tv_task_yes.setText(task_isDone+"");
            tv_task_no.setText(task_unDone+"");
        }else if (SysConfig.workType == SysConfig.WorkType.WORK_TYPE_ARRANGE){
            task_total = mAllArranges.size();
            task_isDone = mIsDoneArranges.size();
            task_unDone = task_total-task_isDone;
            tv_task_yes.setText(task_isDone+"");
            tv_task_no.setText(task_unDone+"");
        }
    }

    private void setViews() {
        tv_task_yes = (TextView) findViewById(R.id.tv_task_yes);
        tv_task_no = (TextView) findViewById(R.id.tv_task_no);
        lv_view = (ListView) findViewById(R.id.lv_view);
        btn_submit = (Button) findViewById(R.id.btn_submit);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        mPointDBDao = new PointDBDao(this);
        mAllDrills = mPointDBDao.selectAllDrillPoint();
        mIsDoneDrills = mPointDBDao.selectDrillisDone();
        mAllShots = mPointDBDao.selectAllShotPoint();
        mIsDoneShots = mPointDBDao.selectShotPointisDone();
        mAllArranges = mPointDBDao.selectAllArrangePoint();
        mIsDoneArranges = mPointDBDao.selectArrangePointisDone();
        mPreferences = getSharedPreferences(SysContants.SYSCONFIG, MODE_PRIVATE);
        tel = mPreferences.getString(SysContants.TEL, "");
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what==0){
                    if (allPointsTask.getData()==null || allPointsTask.getData().size()<1){
                        Toast.makeText(ReceiveTaskActivity.this,"暂无可领取任务！",Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(ReceiveTaskActivity.this,"任务领取成功！",Toast.LENGTH_SHORT).show();
                    }
                    refushAdapters();
                }
            }
        };
    }

    private void setAdapters() {
        if (SysConfig.workType == SysConfig.WorkType.WORK_TYPE_DRILE){
            drillsAdapter = new TaskDrillsReceiveAdapter(this,mAllDrills);
            lv_view.setAdapter(drillsAdapter);
        }else if (SysConfig.workType == SysConfig.WorkType.WORK_TYPE_SHOT){
            shotsAdapter = new TaskShotsReceiveAdapter(this,mAllShots);
            lv_view.setAdapter(shotsAdapter);
        }else if (SysConfig.workType == SysConfig.WorkType.WORK_TYPE_ARRANGE){
            arrangesAdapter = new TaskArrangesReceiveAdapter(this,mAllArranges);
            lv_view.setAdapter(arrangesAdapter);
        }

    }

    public void getTask(int type){//工序类型 检波器：14 钻井：15 下药：16
        HttpUtil.init(HttpUtil.getService(RetrofitService.class).toObtain(type), new Subscriber<ResponseBody>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                if (progressDialog!=null){
                    progressDialog.dismiss();
                }
                Toast.makeText(ReceiveTaskActivity.this, "任务获取失败", Toast.LENGTH_SHORT).show();
                Log.e("onError", "onError: "+e.getMessage());
            }

            @Override
            public void onNext(ResponseBody responseBody) {
                try {
                    if (progressDialog!=null){
                        progressDialog.dismiss();
                    }
                    String result = responseBody.string();
                    Log.e("onNext", "onNext: "+result );
                    allPointsTask = new Gson().fromJson(result, AllPointsTask.class);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (type==arrange_type){
                                getallPointsTask();
                            }else if (type==drill_type){
                                getDrillTask();
                            }
                        }
                    }).start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            private void getDrillTask() {
                if (allPointsTask.getData()!=null && allPointsTask.getData().size()>0){
                    mPointDBDao.deleteAllDrillPoint();
                    mPointDBDao.deleteAllDrillRecord();
                }
                if (null==allPointsTask.getData()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ReceiveTaskActivity.this, "任务获取失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                for (int i=0;i<allPointsTask.getData().size();i++){
                    DrillPoint drillPoint = new DrillPoint();
                    drillPoint.Alt = 0;
                    drillPoint.lineNo = allPointsTask.getData().get(i).getLineNo();//线号
                    drillPoint.spointNo = allPointsTask.getData().get(i).getTheDot();//点号
                    drillPoint.stationNo = allPointsTask.getData().get(i).getPileNo();//桩号
                    drillPoint.wellnum = Double.parseDouble(allPointsTask.getData().get(i).getWellNum());//井口数
                    if (allPointsTask.getData().get(i).getExplosiveWeight()!=null){
                        drillPoint.bombWeight = Float.parseFloat(allPointsTask.getData().get(i).getExplosiveWeight());//炸药重量
                    }
                    if (allPointsTask.getData().get(i).getShotNumber()!=null){
                        drillPoint.detonator = Double.parseDouble(allPointsTask.getData().get(i).getShotNumber());//雷管数量
                    }
                    drillPoint.desWellDepth = Double.parseDouble(allPointsTask.getData().get(i).getDesignDepth());//设计井深

                    drillPoint.geoPoint = GeoPoint.from2DoubleString(allPointsTask.getData().get(i).getDimensionality(),allPointsTask.getData().get(i).getLongitude());
                    mPointDBDao.insertDrillPoint(drillPoint);
                }
                Message msg = new Message();
                msg.what = 0;
                handler.sendMessage(msg);
            }
        });
    }

    public void getallPointsTask(){
        if (allPointsTask.getData()!=null && allPointsTask.getData().size()>0){
            mPointDBDao.deleteAllArrange();
            mPointDBDao.deleteAllCheckRecord();
        }
        if (null==allPointsTask.getData()){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ReceiveTaskActivity.this, "任务获取失败", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        for (int i=0;i<allPointsTask.getData().size();i++){
            ArrangePoint arrangePoint = new ArrangePoint();
            arrangePoint.Alt = 0;
            arrangePoint.lineNo = allPointsTask.getData().get(i).getLineNo();
            arrangePoint.spointNo = allPointsTask.getData().get(i).getTheDot();
            arrangePoint.stationNo = allPointsTask.getData().get(i).getPileNo();
            arrangePoint.geoPoint = GeoPoint.from2DoubleString(allPointsTask.getData().get(i).getDimensionality(),allPointsTask.getData().get(i).getLongitude());
            mPointDBDao.insertArrangePoint(arrangePoint);
        }
        Message msg = new Message();
        msg.what = 0;
        handler.sendMessage(msg);
    }

    public void refushAdapters(){
        if (SysConfig.workType == SysConfig.WorkType.WORK_TYPE_DRILE){
            mAllDrills.clear();
            mIsDoneDrills.clear();
            mAllDrills.addAll(mPointDBDao.selectAllDrillPoint());
            mIsDoneDrills.addAll(mPointDBDao.selectDrillisDone());
            drillsAdapter.notifyDataSetChanged();
        }else if (SysConfig.workType == SysConfig.WorkType.WORK_TYPE_SHOT){
            mAllShots.clear();
            mIsDoneShots.clear();
            mAllShots.addAll(mPointDBDao.selectAllShotPoint());
            mIsDoneShots.addAll(mPointDBDao.selectShotPointisDone());
            shotsAdapter.notifyDataSetChanged();
        }else if (SysConfig.workType == SysConfig.WorkType.WORK_TYPE_ARRANGE){
            mAllArranges.clear();
            mIsDoneArranges.clear();
            mAllArranges.addAll(mPointDBDao.selectAllArrangePoint());
            mIsDoneArranges.addAll(mPointDBDao.selectAllArrangePointIsDone());
            arrangesAdapter.notifyDataSetChanged();
        }
        initData();
    }

    private Dialog dialog;
    public void showClearDataDialog(final int type) {
        dialog = DialogUtils.Alert(ReceiveTaskActivity.this, "提示", "接收新任务会清空旧任务！确认继续吗？",
                new String[]{ReceiveTaskActivity.this.getString(R.string.ok), ReceiveTaskActivity.this.getString(R.string.cancel)},
                new View.OnClickListener[]{new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        progressDialog.show();
                        getTask(type);
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
}
