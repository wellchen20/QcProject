package com.mtkj.cnpc.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mtkj.cnpc.activity.adapter.GridViewPhotoAdapter;
import com.mtkj.cnpc.activity.adapter.GridViewPhotoAdapter.IPhotoBack;
import com.mtkj.cnpc.R;

public class FeedActivity extends Activity implements OnClickListener {

	// title
	private TextView tv_title;
	private LinearLayout back_linear;

	private EditText et_feed_miaoshu;
	private GridView gv_feed_photo;
	private Button btn_feed;
	
	private String miaoshu;

	/**
	 * 图片
	 */
	private List<String> mPhotoStrings = new LinkedList<String>();
	private GridViewPhotoAdapter mPhotoAdapter;
	private String Filepath, imagePath;

	/**
	 * 拍照Pop
	 */
	private LayoutInflater mInflater;
	private View popView;
	private PopupWindow pop;
	private TextView tv_photo_graph, tv_photo_local, tv_cancle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed);

		mInflater = LayoutInflater.from(FeedActivity.this);
		Filepath = Environment.getExternalStorageDirectory().getPath() + "/rmaps/MyImg";

		initPop();
		initView();
		initData();
		bindEvent();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (pop != null && pop.isShowing()) {
			pop.dismiss();
		}
	}

	private void initPop() {
		popView = mInflater.inflate(R.layout.pop_photo, null);
		tv_photo_graph = (TextView) popView.findViewById(R.id.tv_photo_graph);
		tv_photo_local = (TextView) popView.findViewById(R.id.tv_photo_local);
		tv_cancle = (TextView) popView.findViewById(R.id.tv_cancle);

		pop = new PopupWindow(popView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
	}

	private void initView() {
		tv_title = (TextView) findViewById(R.id.tv_title_title);
		back_linear = (LinearLayout) findViewById(R.id.ll_title_back);
		
		et_feed_miaoshu = (EditText) findViewById(R.id.et_feed_miaoshu);
		gv_feed_photo = (GridView) findViewById(R.id.gv_feed_photo);
		btn_feed = (Button) findViewById(R.id.btn_feed);
	}

	private void initData() {
		tv_title.setText("作业反馈");

		mPhotoAdapter = new GridViewPhotoAdapter(FeedActivity.this, mPhotoStrings, photoBack);
		gv_feed_photo.setAdapter(mPhotoAdapter);
	}

	private void bindEvent() {
		tv_cancle.setOnClickListener(this);
		tv_photo_graph.setOnClickListener(this);
		tv_photo_local.setOnClickListener(this);
		
		back_linear.setOnClickListener(this);
		btn_feed.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_cancle:
			if (pop != null && pop.isShowing()) {
				pop.dismiss();
			}
			break;

		case R.id.tv_photo_graph:
			Intent intentGraph = new Intent();
			// 指定开启系统相机的Action
			intentGraph.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			intentGraph.addCategory(Intent.CATEGORY_DEFAULT);
			String nameGraph = getTimeKey();
			// 把文件地址转换成Uri格式
			imagePath = Filepath + "/" + nameGraph + ".jpg";
			Uri uriGraph = null;
			if (Build.VERSION.SDK_INT >= 24) {
				uriGraph = FileProvider.getUriForFile(FeedActivity.this, "com.mtkj.cnpc.photo.fileprovider", new File(imagePath));
			} else {
				uriGraph = Uri.fromFile(new File(imagePath));
			}
			// 设置系统相机拍摄照片完成后图片文件的存放地址
			intentGraph.putExtra(MediaStore.EXTRA_OUTPUT, uriGraph);
			startActivityForResult(intentGraph, 100);
			break;

		case R.id.tv_photo_local:
			Intent intentLocal = new Intent(Intent.ACTION_GET_CONTENT);
			intentLocal.addCategory(Intent.CATEGORY_OPENABLE);
			intentLocal.setType("image/*");
			intentLocal.putExtra("return-data", true);

			startActivityForResult(intentLocal, 101);
			break;

		case R.id.ll_title_back:
			FeedActivity.this.finish();
			break;
			
		case R.id.btn_feed:
			getFeedData();
			break;
			
		default:
			break;
		}
	}
	
	private void getFeedData() {
		miaoshu = et_feed_miaoshu.getText().toString();
		mPhotoStrings = mPhotoAdapter.getPhotoList();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 8;
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case 100:
				Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
				imgYS(bitmap, imagePath);
				mPhotoStrings.add(imagePath);
				mPhotoAdapter.addPhoto(mPhotoStrings);
				break;
			case 101:
				Uri originalUri = intent.getData();
				String[] proj = { MediaStore.Images.Media.DATA };
				// 好像是android多媒体数据库的封装接口，具体的看Android文档
				Cursor cursor = managedQuery(originalUri, proj, null, null, null);
				// 按我个人理解 这个是获得用户选择的图片的索引值
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				// 将光标移至开头 ，这个很重要，不小心很容易引起越界
				cursor.moveToFirst();
				// 最后根据索引值获取图片路径
				String path = cursor.getString(column_index);
				mPhotoStrings.add(path);
				mPhotoAdapter.addPhoto(mPhotoStrings);
				break;

			default:
				break;
			}
		}
	}
	
	@SuppressLint("SimpleDateFormat")
	public static String getTimeKey() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String string = dateFormat.format(new Date(System.currentTimeMillis()));
		return string.trim();
	}

	/**
	 * 保存图片
	 * 
	 * @param bitmap
	 * @param filepath
	 */
	public void imgYS(Bitmap bitmap, String filepath) {
		FileOutputStream b = null;
		try {
			b = new FileOutputStream(filepath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (bitmap != null) {
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);
		}
	}
	
	private IPhotoBack photoBack = new IPhotoBack() {
		
		@Override
		public void addPhoto() {
			pop.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#b0000000")));
			pop.showAtLocation(tv_title, Gravity.BOTTOM, 0, 0);
			pop.setAnimationStyle(R.style.app_pop);
			pop.setOutsideTouchable(true);
			pop.setFocusable(true);
			pop.update();
		}
	};
}
