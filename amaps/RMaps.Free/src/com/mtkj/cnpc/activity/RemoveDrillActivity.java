package com.mtkj.cnpc.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mtkj.cnpc.protocol.bean.DrillPoint;
import com.mtkj.cnpc.sqlite.PointDBDao;
import com.mtkj.cnpc.R;

public class RemoveDrillActivity extends Activity {

    ImageView iv_back;
    TextView tv_stationNo_old;
    TextView tv_lineNo_old;
    TextView tv_pointNo_old;
    EditText et_stationNo_new;
    EditText et_lineNo_new;
    EditText et_pointNo_new;
    Button btn_submit;

    private DrillPoint drillPoint;
    private PointDBDao mPointDBDao;

    String stationNo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_drill);
        getData();
        setViews();
        setListeners();
    }

    private void getData() {
        stationNo = getIntent().getStringExtra("stationNo");
        mPointDBDao = new PointDBDao(this);
        drillPoint = mPointDBDao.selectDrillPoint(stationNo);
    }

    private void setViews() {
        iv_back = (ImageView) findViewById(R.id.iv_back);
        tv_stationNo_old = (TextView) findViewById(R.id.tv_stationNo_old);
        tv_lineNo_old = (TextView) findViewById(R.id.tv_lineNo_old);
        tv_pointNo_old = (TextView) findViewById(R.id.tv_pointNo_old);
        et_stationNo_new = (EditText) findViewById(R.id.et_stationNo_new);
        et_lineNo_new = (EditText) findViewById(R.id.et_lineNo_new);
        et_pointNo_new = (EditText) findViewById(R.id.et_pointNo_new);
        btn_submit = (Button) findViewById(R.id.btn_submit);

        tv_stationNo_old.setText(stationNo);
        tv_lineNo_old.setText(drillPoint.lineNo);
        tv_pointNo_old.setText(drillPoint.spointNo);
    }

    private void setListeners() {
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_stationNo_new.getText().toString().equals("") || et_lineNo_new.toString().equals("") || et_pointNo_new.toString().equals("")){
                    Toast.makeText(RemoveDrillActivity.this,"桩号、线号、点号不能为空！",Toast.LENGTH_SHORT).show();
                }else {
                    drillPoint.stationNo = et_stationNo_new.getText().toString();
                    drillPoint.lineNo = et_lineNo_new.getText().toString();
                    drillPoint.spointNo = et_pointNo_new.getText().toString();
                    mPointDBDao.updateDrillPoint(drillPoint);
                    Intent intent = new Intent();
                    intent.putExtra("stationNo",et_stationNo_new.getText().toString());
                    setResult(RESULT_OK,intent);
                    finish();
                }
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
