package com.mtkj.cnpc.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mtkj.cnpc.protocol.bean.DrillRecord;
import com.mtkj.cnpc.sqlite.PointDBDao;
import com.mtkj.cnpc.R;

public class SeeRecordActivity extends Activity {

	private PointDBDao mPointDBDao;
	private DrillRecord mDrillRecord;
	private String stationNo;
	
	private TextView txt_title, tv_see_xianshu, tv_see_paopai, tv_see_zhuanghao, tv_see_jingdu, tv_see_weidu,
						tv_see_data, tv_see_suredata, tv_see_koushu, tv_see_zuanjing, tv_see_xiayao,
						tv_see_yaoliang, tv_see_leiguan, tv_see_sizuan, tv_see_jingjian, tv_see_beizhu;
	private ImageView iv_see_image1, iv_see_image2, iv_see_image3;
	private Button btn_see_record;
	private View v_leiguan, v_sizuan, v_beizu;
	private LinearLayout ll_title_back;
	int size;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_see_record);

		mPointDBDao = new PointDBDao(this);
		initView();
		initDatas();
		getData();
	}

	private void initDatas() {
		stationNo = getIntent().getStringExtra("drillPoint");
		size = mPointDBDao.selectDrillRecord(stationNo).size();
		if(size!=0){
			mDrillRecord = mPointDBDao.selectDrillRecord(stationNo).get(0);
		}else{
			Toast.makeText(SeeRecordActivity.this,"当前井号信号信息未填写，无法显示正确信息！",Toast.LENGTH_LONG).show();
			return;
		}
	}

	private void initView() {
		ll_title_back = (LinearLayout) findViewById(R.id.ll_title_back);
		ll_title_back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SeeRecordActivity.this.finish();
			}
		});
		txt_title = (TextView) findViewById(R.id.tv_title_title);
		tv_see_xianshu = (TextView) findViewById(R.id.tv_see_xianshu);
		tv_see_paopai = (TextView) findViewById(R.id.tv_see_paopai);
		tv_see_zhuanghao = (TextView) findViewById(R.id.tv_see_zhuanghao);
		tv_see_jingdu = (TextView) findViewById(R.id.tv_see_jingdu);
		tv_see_weidu = (TextView) findViewById(R.id.tv_see_weidu);
		tv_see_data = (TextView) findViewById(R.id.tv_see_data);
		tv_see_suredata = (TextView) findViewById(R.id.tv_see_suredata);
		tv_see_koushu = (TextView) findViewById(R.id.tv_see_koushu);
		tv_see_zuanjing = (TextView) findViewById(R.id.tv_see_zuanjing);
		tv_see_xiayao = (TextView) findViewById(R.id.tv_see_xiayao);
		tv_see_yaoliang = (TextView) findViewById(R.id.tv_see_yaoliang);
		tv_see_leiguan = (TextView) findViewById(R.id.tv_see_leiguan);
		tv_see_sizuan = (TextView) findViewById(R.id.tv_see_sizuan);
		tv_see_jingjian = (TextView) findViewById(R.id.tv_see_jingjian);
		tv_see_beizhu = (TextView) findViewById(R.id.tv_see_beizhu);
		
		iv_see_image1 = (ImageView) findViewById(R.id.iv_see_image1);
		iv_see_image2 = (ImageView) findViewById(R.id.iv_see_image2);
		iv_see_image3 = (ImageView) findViewById(R.id.iv_see_image3);
		
		btn_see_record = (Button) findViewById(R.id.btn_see_record);
		btn_see_record.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				SeeRecordActivity.this.finish();
			}
		});
		
		v_leiguan = findViewById(R.id.v_leiguan);
		v_sizuan = findViewById(R.id.v_sizuan);
		v_beizu = findViewById(R.id.v_beizhu);
	}
	

	private void getData() {
		if(size==0){
			return;
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 8;
		
		txt_title.setText("钻井数据");
		
		tv_see_xianshu.setText(mDrillRecord.lineNo);
		tv_see_paopai.setText(mDrillRecord.spointNo);
		tv_see_zhuanghao.setText(mDrillRecord.stationNo);
		tv_see_jingdu.setText(mDrillRecord.lon);
		tv_see_weidu.setText(mDrillRecord.lat);
		tv_see_data.setText(mDrillRecord.receivetime);
		tv_see_koushu.setText(mDrillRecord.wellnum);
		tv_see_zuanjing.setText(mDrillRecord.drilldepth);
		tv_see_xiayao.setText(mDrillRecord.bombdepth);
		tv_see_yaoliang.setText(mDrillRecord.bombWeight);
		if (mDrillRecord.detonator != null && !"".equals(mDrillRecord.detonator)) {
			tv_see_leiguan.setText(mDrillRecord.detonator);
		} else {
			tv_see_leiguan.setVisibility(View.GONE);
			v_leiguan.setVisibility(View.GONE);
		}
//		if (values.get(11) != null && !"".equals(values.get(11))) {
//			tv_see_sizuan.setText(values.get(11));
//		} else {
//			tv_see_sizuan.setVisibility(View.GONE);
//		}
//		if (values.get(12) != null && !"".equals(values.get(12))) {
//			tv_see_jingjian.setText(values.get(12));
//		} else {
//			tv_see_jingjian.setVisibility(View.GONE);
//		}
		if (tv_see_sizuan.getVisibility() == View.GONE && tv_see_jingjian.getVisibility() == View.GONE) {
			v_sizuan.setVisibility(View.GONE);
		}
		if (mDrillRecord.remark != null && !"".equals(mDrillRecord.remark)) {
			tv_see_beizhu.setText(mDrillRecord.remark);
		} else {
			tv_see_beizhu.setVisibility(View.GONE);
			v_beizu.setVisibility(View.GONE);
		}
		if (mDrillRecord.image1 != null && !"".equals(mDrillRecord.image1)) {
			Bitmap bitmap = BitmapFactory.decodeFile(mDrillRecord.image1, options);
			iv_see_image1.setImageBitmap(bitmap);
		} else {
			iv_see_image1.setVisibility(View.GONE);
		}
		if (mDrillRecord.image2 != null && !"".equals(mDrillRecord.image2)) {
			Bitmap bitmap = BitmapFactory.decodeFile(mDrillRecord.image2, options);
			iv_see_image2.setImageBitmap(bitmap);
		} else {
			iv_see_image2.setVisibility(View.GONE);
		}
		if (mDrillRecord.image3 != null && !"".equals(mDrillRecord.image3)) {
			Bitmap bitmap = BitmapFactory.decodeFile(mDrillRecord.image3, options);
			iv_see_image3.setImageBitmap(bitmap);
		} else {
			iv_see_image3.setVisibility(View.GONE);
		}
		tv_see_suredata.setText(mDrillRecord.drilltime);
	}
}
