package com.mtkj.cnpc.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mtkj.cnpc.protocol.bean.ArrangeRecord;
import com.mtkj.cnpc.sqlite.PointDBDao;
import com.mtkj.cnpc.R;

public class SeeArrangeActivity extends Activity {

    TextView tv_stationNo;
    TextView tv_lineNo;
    TextView tv_pointNo;
    TextView tv_lon;
    TextView tv_lat;
    ImageView iv_back;
    TextView tv_completeTime;
    TextView tv_remark;
    TextView tv_description;

    Button btn_submit;
    private PointDBDao mPointDBDao;
    ArrangeRecord arrangeRecord;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_arrange);
        initData();
        setViews();
        setListeners();
    }

    private void initData() {
        mPointDBDao = new PointDBDao(this);
        String station = getIntent().getStringExtra("arrangePoint");
        arrangeRecord = mPointDBDao.selectArrangeRecord(station);
    }

    private void setViews() {
        tv_stationNo = (TextView) findViewById(R.id.tv_stationNo);
        tv_lineNo = (TextView) findViewById(R.id.tv_lineNo);
        tv_pointNo = (TextView) findViewById(R.id.tv_pointNo);
        tv_lon = (TextView) findViewById(R.id.tv_lon);
        tv_lat = (TextView) findViewById(R.id.tv_lat);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        btn_submit = (Button) findViewById(R.id.btn_submit);
        tv_completeTime = (TextView) findViewById(R.id.tv_completeTime);
        tv_remark = (TextView) findViewById(R.id.tv_remark);
        tv_description = (TextView) findViewById(R.id.tv_description);

        if (arrangeRecord!=null){
            tv_stationNo.setText(arrangeRecord.getStationNo());
            tv_lineNo.setText(arrangeRecord.getLineNo());
            tv_pointNo.setText(arrangeRecord.getSpointNo());
            tv_lon.setText(arrangeRecord.getLon()+"");
            tv_lat.setText(arrangeRecord.getLat()+"");
            tv_remark.setText(arrangeRecord.getRemark()+"");
            tv_completeTime.setText(arrangeRecord.getTime()+"");
            tv_description.setText(arrangeRecord.getDescription());
        }

    }

    private void setListeners() {
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
