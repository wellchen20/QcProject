package com.mtkj.cnpc.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mtkj.cnpc.R;
import com.mtkj.cnpc.activity.adapter.VideoListAdapter;
import com.mtkj.cnpc.protocol.bean.DrillRecord;
import com.mtkj.cnpc.sqlite.PointDBDao;
import com.mtkj.utils.StatusBarUtil;
import com.robert.maps.applib.utils.DialogUtils;
import com.robert.maps.applib.utils.Ut;

import java.io.File;
import java.util.ArrayList;

public class VideoListActivity extends Activity {

    ListView listview;
    ImageView iv_back;
    ArrayList<String> localName;
    ArrayList<String> localPath;
    ArrayList<String> isuploadList;
    ArrayList<String> localStation;
    VideoListAdapter adapter;
    public String mSaveFolder;
    TextView tv_localpath;
    private PointDBDao mPointDBDao;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down_list);
        StatusBarUtil.setTheme(this);
        initData();
        getNames();
        setAdapters();
        setListeners();
    }

    private void initData() {
        mPointDBDao = new PointDBDao(this);
        File dir = Ut.getRMapsProjectPrivateTasksOutputDir(VideoListActivity.this);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        localName = new ArrayList<>();
        localPath = new ArrayList<>();
        isuploadList = new ArrayList<>();
        localStation = new ArrayList<>();
        mSaveFolder = dir.getPath();
        listview = findViewById(R.id.listview);
        iv_back = findViewById(R.id.iv_back);
        tv_localpath = findViewById(R.id.tv_localpath);
        tv_localpath.setText("视频路径:"+mSaveFolder);
    }

    public void refush(int position){
        Log.e("refush", "refush" );
        DrillRecord record = mPointDBDao.selectDrillRecord(localStation.get(position)).get(0);
        record.isupload = "0";
        mPointDBDao.updateDrilRecord(record);
        getNames();
        adapter.notifyDataSetChanged();
    }

    private void setAdapters() {
        adapter = new VideoListAdapter(VideoListActivity.this,localName,localPath,isuploadList,localStation);
        listview.setAdapter(adapter);
    }

    private void setListeners() {
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showClearDataDialog(position);
                return false;
            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void getNames() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File path = new File(mSaveFolder);// 获得SD卡路径
            //File path = new File("/mnt/sdcard/");
            File[] files = path.listFiles();// 读取
            getFileName(files);
        }
    }

    private void getFileName(File[] files) {
        localName.clear();
        localStation.clear();
        localPath.clear();
        isuploadList.clear();
        if (files != null) {// 先判断目录是否为空，否则会报空指针
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.endsWith(".mp4")) {
                    Log.e("fileName", "fileName: "+fileName );
                    String station = fileName.split(".mp4")[0];
                    if (station!=null){
                        localName.add(fileName);
                        localPath.add(mSaveFolder+"/"+fileName);
                        localStation.add(station);
                        if (mPointDBDao.selectDrillRecord(station).size()>0){
                            String isupload = mPointDBDao.selectDrillRecord(station).get(0).isupload;
                            if (null!=isupload){
                                isuploadList.add(isupload);
                            }
                            else {
                                isuploadList.add("2");//无效视频
                            }
                        }else {
                            isuploadList.add("2");//无效视频
                        }
                    }

                }
            }
            for (int i=0;i<localName.size();i++){
                Log.e("localName", "localName: "+localName);
            }
        }
    }

    private Dialog dialog;
    public void showClearDataDialog(final int position) {
        dialog = DialogUtils.Alert(VideoListActivity.this, "删除视频", "确定删除此视频吗？",
                new String[]{getString(R.string.ok), getString(R.string.cancel)},
                new View.OnClickListener[]{new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        File file = new File(localPath.get(position));
                        if (file.exists()){
                            if (file.delete()){
                                Toast.makeText(VideoListActivity.this,"删除成功",Toast.LENGTH_SHORT).show();
                                getNames();
                                setAdapters();
                            }else {
                                Toast.makeText(VideoListActivity.this,"删除失败",Toast.LENGTH_SHORT).show();
                            }
                        }

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
