package com.mtkj.cnpc.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mtkj.cnpc.protocol.bean.DrillPoint;
import com.mtkj.cnpc.protocol.bean.DrillRecord;
import com.mtkj.cnpc.sqlite.PointDBDao;
import com.mtkj.cnpc.R;

import java.util.ArrayList;
import java.util.List;

public class SeeDrillActivity extends Activity {
    TextView tv_stationNo;
    TextView tv_lineNo;
    TextView tv_pointNo;
    TextView tv_lon;
    TextView tv_lat;
    TextView tv_height;
    TextView tv_designHig;
    TextView tv_arrivedTime;
    TextView tv_activeHig;
    TextView tv_remark;
    TextView tv_completeTime;
    Button btn_submit;
    LinearLayout ll_title_back;
    TextView txt_title;

    private PointDBDao mPointDBDao;
    private DrillRecord mDrillRecord;
    private DrillPoint drillPoint;
    private String stationNo;
    int size;

    private ImageView iv_see_image1, iv_see_image2, iv_see_image3;
    private List<Bitmap> bitmaps = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_drill);
        setViews();
        initData();
        setListeners();
    }

    private void initData() {
        mPointDBDao = new PointDBDao(this);
        stationNo = getIntent().getStringExtra("drillPoint");
        size = mPointDBDao.selectDrillRecord(stationNo).size();
        drillPoint = mPointDBDao.selectDrillPoint(stationNo);
        if(size!=0){
            mDrillRecord = mPointDBDao.selectDrillRecord(stationNo).get(0);
            tv_stationNo.setText(mDrillRecord.stationNo);
            tv_lineNo.setText(mDrillRecord.lineNo);
            tv_pointNo.setText(mDrillRecord.spointNo);
            tv_lon.setText(mDrillRecord.lon);
            tv_lat.setText(mDrillRecord.lat);
            tv_arrivedTime.setText(mDrillRecord.receivetime);
            tv_activeHig.setText(mDrillRecord.drilldepth+"m");
            tv_remark.setText(mDrillRecord.remark);
            tv_completeTime.setText(mDrillRecord.drilltime);
        }
        if (drillPoint!=null){
            tv_height.setText(drillPoint.Alt+"m");
            tv_designHig.setText(drillPoint.desWellDepth+"m");
        }

        if (mDrillRecord.image1 != null && !"".equals(mDrillRecord.image1)) {
            Bitmap bitmap = BitmapFactory.decodeFile(mDrillRecord.image1, null);
            iv_see_image1.setImageBitmap(bitmap);
            bitmaps.add(bitmap);
        } else {
            iv_see_image1.setVisibility(View.GONE);
        }
        if (mDrillRecord.image2 != null && !"".equals(mDrillRecord.image2)) {
            Bitmap bitmap = BitmapFactory.decodeFile(mDrillRecord.image2, null);
            iv_see_image2.setImageBitmap(bitmap);
            bitmaps.add(bitmap);
        } else {
            iv_see_image2.setVisibility(View.GONE);
        }
        if (mDrillRecord.image3 != null && !"".equals(mDrillRecord.image3)) {
            Bitmap bitmap = BitmapFactory.decodeFile(mDrillRecord.image3, null);
            iv_see_image3.setImageBitmap(bitmap);
            bitmaps.add(bitmap);
        } else {
            iv_see_image3.setVisibility(View.GONE);
        }
        txt_title.setText("钻井数据");
    }

    private void setViews() {
        ll_title_back = (LinearLayout) findViewById(R.id.ll_title_back);
        txt_title = (TextView) findViewById(R.id.tv_title_title);
        tv_stationNo = (TextView) findViewById(R.id.tv_stationNo);
        tv_lineNo = (TextView) findViewById(R.id.tv_lineNo);
        tv_pointNo = (TextView) findViewById(R.id.tv_pointNo);
        tv_lon = (TextView) findViewById(R.id.tv_lon);
        tv_lat = (TextView) findViewById(R.id.tv_lat);
        tv_height = (TextView) findViewById(R.id.tv_height);
        tv_designHig = (TextView) findViewById(R.id.tv_designHig);
        tv_arrivedTime = (TextView) findViewById(R.id.tv_arrivedTime);
        tv_activeHig = (TextView) findViewById(R.id.tv_activeHig);
        tv_remark = (TextView) findViewById(R.id.tv_remark);
        btn_submit = (Button) findViewById(R.id.btn_submit);
        tv_completeTime = (TextView) findViewById(R.id.tv_completeTime);
        iv_see_image1 = (ImageView) findViewById(R.id.iv_see_image1);
        iv_see_image2 = (ImageView) findViewById(R.id.iv_see_image2);
        iv_see_image3 = (ImageView) findViewById(R.id.iv_see_image3);
    }

    private void setListeners() {
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ll_title_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        iv_see_image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrillRecord.image1 != null && !"".equals(mDrillRecord.image1)) {
                    Intent intent = new Intent(SeeDrillActivity.this,TurnLargeActivity.class);
//                    Uri uri = FileUtils.getUriForFile(SeeDrillActivity.this,new File(mDrillRecord.image1));
                    String pathName = mDrillRecord.image1;
                    intent.putExtra("pathName",pathName);
                    startActivity(intent);
                }
            }
        });

        iv_see_image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrillRecord.image2 != null && !"".equals(mDrillRecord.image2)) {
                    Intent intent = new Intent(SeeDrillActivity.this,TurnLargeActivity.class);
                    String pathName = mDrillRecord.image2;
                    intent.putExtra("pathName",pathName);
                    startActivity(intent);
                }
            }
        });

        iv_see_image3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrillRecord.image3 != null && !"".equals(mDrillRecord.image3)) {
                    Intent intent = new Intent(SeeDrillActivity.this,TurnLargeActivity.class);
                    String pathName = mDrillRecord.image3;
                    intent.putExtra("pathName",pathName);
                    startActivity(intent);
                }
            }
        });
    }

}
