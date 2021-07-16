package com.mtkj.cnpc.activity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.ByteString;
import com.mtkj.cnpc.activity.adapter.GridViewPhotoAdapter;
import com.mtkj.cnpc.activity.adapter.GridViewPhotoAdapter.IPhotoBack;
import com.mtkj.cnpc.activity.utils.BitmapUtils;
import com.mtkj.cnpc.protocol.bean.DrillPoint;
import com.mtkj.cnpc.protocol.bean.DrillRecord;
import com.mtkj.cnpc.protocol.bean.Jing;
import com.mtkj.cnpc.protocol.constants.DSSProtoDataConstants.ProtoMsgType;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_DpRecord;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_Head;
import com.mtkj.cnpc.protocol.socket.DataProcess;
import com.mtkj.cnpc.protocol.utils.DialogUtils;
import com.mtkj.cnpc.protocol.utils.SocketUtils;
import com.mtkj.cnpc.sqlite.PointDBDao;
import com.mtkj.cnpc.R;
import com.robert.maps.applib.utils.TimeUtil;

public class RecordActivity extends Activity implements OnClickListener {
	
	public static String[] lithology = new String[] { "沙层", "胶泥层", "砾石层", "黄土层", "泥岩", "砂岩", "其他" };

	// title
	private TextView tv_title;
	private LinearLayout back_linear;

	private TextView tv_Sline, tv_Spoint, tv_StationNo, tv_data, tv_WellNum;
	private EditText et_Mark;
	private ImageView iv_detail;
	private Button btn_record;
	private Spinner sp_Lithology;
	private ArrayAdapter<String> adapter;
	private int sp_position = 0;

	/**
	 * 图片
	 */
	private LinearLayout ll_add;
	private GridView gv_photo;
	private List<String> mPhotoStrings = new LinkedList<String>();
	private GridViewPhotoAdapter mPhotoAdapter;
	private String Filepath, imagePath;
	Bitmap myBitmap;

	/**
	 * 传替数据
	 */
	private DrillPoint drillPoint;
	private Dialog dialog;
	private PointDBDao mPointDBDao;
	
	/**
	 * 获取数据
	 */
	private String startTime, mark;

	/**
	 * 组合井处理
	 */
	private LayoutInflater mInflater;
	public static boolean isDelete = false;
	public static boolean isRefresh = false;

	/**
	 * 拍照Pop
	 */
	private View popView;
	private PopupWindow pop;
	private TextView tv_photo_graph, tv_photo_local, tv_cancle;

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if (!RecordActivity.this.isFinishing() && !dialog.isShowing()) {
					dialog.show();
				}
				break;

			case 1:
				if (!RecordActivity.this.isFinishing() && dialog.isShowing()) {
					dialog.dismiss();
				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);
		
		mPointDBDao = new PointDBDao(this);
		dialog = DialogUtils.alertProgress(RecordActivity.this);
		mInflater = LayoutInflater.from(RecordActivity.this);
		Filepath = Environment.getExternalStorageDirectory().getPath() + "/rmaps/MyImg";

		if (AddActivity.jings != null && AddActivity.jings.size() > 0) {
			AddActivity.jings.clear();
		}

		initPop();
		initView();
		initData();
		bendEvent();
		if (!DataProcess.GetInstance().isConnected()) {
			new ConnTask(ConnTask.TYPE_NEW_CONN).execute("");
		}
	}

	private void initPop() {
		popView = mInflater.inflate(R.layout.pop_photo, null);
		tv_photo_graph = (TextView) popView.findViewById(R.id.tv_photo_graph);
		tv_photo_local = (TextView) popView.findViewById(R.id.tv_photo_local);
		tv_cancle = (TextView) popView.findViewById(R.id.tv_cancle);

		pop = new PopupWindow(popView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isRefresh) {
			addJingView();
		}
		if (AddActivity.jings != null && AddActivity.jings.size() > 0) {
			tv_WellNum.setText(AddActivity.jings.get(0).getJinghao());
		}
		if (pop != null && pop.isShowing()) {
			pop.dismiss();
		}
	}

	private void initData() {
		tv_title.setText("确认信息");

		Intent intent = getIntent();
		String station = intent.getStringExtra("drillPoint");
		drillPoint = mPointDBDao.selectDrillPoint(station);
		
		tv_Sline.setText(drillPoint.lineNo);
		tv_Spoint.setText(drillPoint.spointNo);
		tv_StationNo.setText(drillPoint.stationNo);

		tv_data.setText(TimeUtil.getCurrentTimeInString());
		startTime = TimeUtil.getCurrentTimeInString();
	}

	private void initView() {
		tv_title = (TextView) findViewById(R.id.tv_title_title);
		back_linear = (LinearLayout) findViewById(R.id.ll_title_back);

		tv_Sline = (TextView) findViewById(R.id.tv_xianshu);
		tv_Spoint = (TextView) findViewById(R.id.tv_paopai);
		tv_StationNo = (TextView) findViewById(R.id.tv_zhuanghao);
		tv_data = (TextView) findViewById(R.id.tv_data);
		tv_WellNum = (TextView) findViewById(R.id.tv_jing);

		et_Mark = (EditText) findViewById(R.id.et_beizhu);

		iv_detail = (ImageView) findViewById(R.id.iv_detail);
		btn_record = (Button) findViewById(R.id.btn_record);
		ll_add = (LinearLayout) findViewById(R.id.ll_add);

		gv_photo = (GridView) findViewById(R.id.gv_photo);
		mPhotoAdapter = new GridViewPhotoAdapter(RecordActivity.this, mPhotoStrings, photoBack);
		gv_photo.setAdapter(mPhotoAdapter);

		sp_Lithology = (Spinner) findViewById(R.id.sp_yanxing);
		adapter = new ArrayAdapter(RecordActivity.this, android.R.layout.simple_spinner_dropdown_item, lithology);
		sp_Lithology.setAdapter(adapter);
		sp_Lithology.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				sp_position = arg2;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}


	private void bendEvent() {
		tv_WellNum.setOnClickListener(this);
		iv_detail.setOnClickListener(this);
		btn_record.setOnClickListener(this);
		btn_record.setOnClickListener(this);

		tv_cancle.setOnClickListener(this);
		tv_photo_graph.setOnClickListener(this);
		tv_photo_local.setOnClickListener(this);
		
		back_linear.setOnClickListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 8;
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case 100:
				Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
				// 压缩图片处理
				bitmap = comp(bitmap);
				// 保存压缩图片
				imgYS(bitmap, imagePath);
				
				mPhotoStrings.add(imagePath);
				mPhotoAdapter.notifyDataSetChanged();
				break;
			case 101:
				ContentResolver resolver = getContentResolver();
				try{
					Uri selectedIcon = intent.getData();
					//final String fileName = FileUtils.getRealFilePath(RecordActivity.this,selectedIcon);
					// 将图片内容解析成字节数组
					byte[] mContent = BitmapUtils.readStream(resolver.openInputStream(Uri.parse(selectedIcon.toString())));
					// 将字节数组转换为ImageView可调用的Bitmap对象
					myBitmap = BitmapUtils.getPicFromBytes(mContent, null);
					//将字节换成KB
					double mid = mContent.length / 1024;
					if(mid>100){
						Dialog alertDialog = new AlertDialog.Builder(this).
								setTitle("确定压缩图片？").
								setMessage("您的图片过大，需要压缩处理吗？压缩可能导致您的图片清晰度下降，请谨慎！").
								setIcon(R.drawable.warning).setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								myBitmap = BitmapUtils.imageZoom(myBitmap, 300.00);
								String nameGraph = getTimeKey();
								// 把文件地址转换成Uri格式
								String path = Filepath + "/" + nameGraph + ".jpg";
								// 保存压缩图片
								imgYS(myBitmap, path);

								mPhotoStrings.add(path);
								mPhotoAdapter.notifyDataSetChanged();
									}
						}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {

							}
						}).create();
						alertDialog.show();
					}else{
						String nameGraph = getTimeKey();
						String path = Filepath + "/" + nameGraph + ".jpg";
						// 保存压缩图片
						imgYS(myBitmap, path);

						mPhotoStrings.add(path);
						mPhotoAdapter.notifyDataSetChanged();
					}
				}catch (Exception e){
					e.printStackTrace();
				}
				break;

			default:
				break;
			}
		}
	}

	@Override
	public void onClick(View v) {
		File file = new File(Filepath);
		if (!file.exists()) {
			file.mkdir();
		}
		switch (v.getId()) {
		case R.id.tv_jing:
			Intent intentDetail = new Intent(RecordActivity.this, AddActivity.class);
			intentDetail.putExtra("value", 0);
			startActivity(intentDetail);
			break;
		
		case R.id.iv_detail:
			Intent intent = new Intent(RecordActivity.this, AddActivity.class);
			intent.putExtra("value", 0);
			startActivity(intent);
			break;

		case R.id.btn_record:
			if (AddActivity.jings != null && AddActivity.jings.size() > 0) {
				handler.sendEmptyMessage(0);
				getData();
				btn_record.setClickable(false);
			}else {
				Toast.makeText(RecordActivity.this,"请填写井号信息",Toast.LENGTH_SHORT).show();
			}
			break;

		case R.id.tv_cancle:
			if (pop != null && pop.isShowing()) {
				pop.dismiss();
			}
			break;

		case R.id.tv_photo_graph:
			Intent intentGraph = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			// 指定开启系统相机的Action
			intentGraph.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			String nameGraph = getTimeKey();
			// 把文件地址转换成Uri格式
			imagePath = Filepath + "/" + nameGraph + ".jpg";
			Uri uriGraph = null;
			if (Build.VERSION.SDK_INT >= 24) {
				uriGraph = FileProvider.getUriForFile(RecordActivity.this, "com.robert.maps.fileprovider", new File(imagePath));
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
			/*if (AddActivity.jings != null && AddActivity.jings.size() > 0) {
				handler.sendEmptyMessage(0);
				getData();

			}*/
			
			Intent intentSure = new Intent();
//			intentSure.putExtra("station", drillPoint.stationNo);
			setResult(RESULT_OK, intentSure);
			finish();
			
			break;
		default:
			break;
		}
	}

	/**
	 * 添加井布局
	 */
	public void addJingView() {
		ll_add.removeAllViews();
		if (AddActivity.jings != null && AddActivity.jings.size() > 1) {
			for (int i = 1; i < AddActivity.jings.size(); i++) {
				final int num = i;
				final LinearLayout linearLayout = new LinearLayout(RecordActivity.this);
				linearLayout.setOrientation(LinearLayout.HORIZONTAL);
				linearLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

				View view = mInflater.inflate(R.layout.add_jing, null);
				view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				final TextView editText = (TextView) view.findViewById(R.id.tv_add_jing);
				editText.setText(AddActivity.jings.get(i).getJinghao());
				editText.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent(RecordActivity.this, AddActivity.class);
						intent.putExtra("value", num);
						startActivity(intent);
					}
				});
				ImageView imageView = (ImageView) view.findViewById(R.id.iv_add_detail);
				imageView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent(RecordActivity.this, AddActivity.class);
						intent.putExtra("value", num);
						startActivity(intent);
					}
				});
				ImageView button = (ImageView) view.findViewById(R.id.btn_cancel_jing);
				button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						AddActivity.jings.remove(num);
						addJingView();
					}
				});
				linearLayout.addView(view);

				ll_add.addView(linearLayout);
			}
		}
	}

	/**
	 * 获取页面数据，存入数据库
	 */
	private void getData() {
		mark = et_Mark.getText().toString();
		String time = TimeUtil.getCurrentTimeInString();
		mPhotoStrings = mPhotoAdapter.getPhotoList();

		List<DrillRecord> records = new ArrayList<DrillRecord>();
		if (AddActivity.jings != null && AddActivity.jings.size() > 0) {
			for (int i = 0; i < AddActivity.jings.size(); i++) {
				DrillRecord record = new DrillRecord();
				
				record.stationNo = drillPoint.stationNo;
				record.lineNo = drillPoint.lineNo;
				record.spointNo = drillPoint.spointNo;
				record.receivetime = startTime;
				record.welllithology = lithology[sp_position]; // DSCloud不支持中文
				record.welllithology = sp_position + "";

				Jing jing = AddActivity.jings.get(i);
				record.wellnum = jing.getJinghao();
				record.lon = String.valueOf(drillPoint.geoPoint.getLongitude());
				record.lat = String.valueOf(drillPoint.geoPoint.getLatitude());
				Log.e("getLongitude", drillPoint.geoPoint.getLongitude()+"" );
				Log.e("getLatitude", drillPoint.geoPoint.getLatitude()+"" );
				record.drilldepth = jing.getZuanjing();
				record.bombdepth = jing.getXiayao();
				record.bombWeight = jing.getYaoliang();
				record.detonator = jing.getLeiguan();
				
				record.bombid = jing.getZhayao();
				StringBuffer buffer = new StringBuffer();
				for (int j = 0; j < jing.getZhayao().size(); j++) {
					if (j == jing.getZhayao().size() - 1) {
						buffer.append(jing.getZhayao().get(j));
					} else {
						buffer.append(jing.getZhayao().get(j)).append(",");
					}
				}
				
				record.detonatorid = jing.getLeiguanList();
				buffer = new StringBuffer();
				for (int j = 0; j < jing.getLeiguanList().size(); j++) {
					if (j == jing.getLeiguanList().size() - 1) {
						buffer.append(jing.getLeiguanList().get(j));
					} else {
						buffer.append(jing.getLeiguanList().get(j)).append(",");
					}
				}
				
				if (mark != null && !"".equals(mark)) {
					record.remark = mark;
				} else {
					record.remark = "";
				}

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
				record.drilltime = time;

				records.add(record);
			}
		} else {
			Toast.makeText(RecordActivity.this, "请输入井号信息", Toast.LENGTH_SHORT).show();
		}

		for (final DrillRecord record : records) {
			mPointDBDao.insertDrillRecord(record);
			uploadRecord(record);
		}

		drillPoint.isDone = true;
		mPointDBDao.updateDrillPoint(drillPoint);
	}

	/**
	 * DSCLOUD上传
	 * 
	 * @param record
	 */
	protected void uploadRecord(final DrillRecord record) {
		new UploadAnytask(record).execute("");
	}
	
	public class UploadAnytask extends AsyncTask<String, Integer, Boolean> {

		private DrillRecord mRecord;
		
		public UploadAnytask(DrillRecord drillRecord) {
			mRecord = drillRecord;
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			StringBuffer BombIdbuffer = new StringBuffer();
			List<String> explosivenumids = mRecord.bombid;
			for (int i = 0; i < explosivenumids.size(); i++ ) {
				if (i == explosivenumids.size() - 1) {
					BombIdbuffer.append(explosivenumids.get(i));
				} else {
					BombIdbuffer.append(explosivenumids.get(i)).append(",");
				}
			}
			String BombId = BombIdbuffer.toString();
			StringBuffer DetonatorIdbuffer = new StringBuffer();
			List<String> capnumids = mRecord.detonatorid;
			for (int i = 0; i < capnumids.size(); i++) {
				if (i == capnumids.size() - 1) {
					DetonatorIdbuffer.append(capnumids.get(i));
				} else {
					DetonatorIdbuffer.append(capnumids.get(i)).append(",");
				}
			}
			if (SysConfig.SC_ID != null && !"".equals(SysConfig.SC_ID)) {
				try {
					String DetonatorId = DetonatorIdbuffer.toString();
					Proto_DpRecord proto_DpRecord = Proto_DpRecord.newBuilder()
							.setStationNo(ByteString.copyFrom(mRecord.stationNo, "GB2312"))
							.setSline(ByteString.copyFrom(mRecord.lineNo, "GB2312"))
							.setSpoint(ByteString.copyFrom(mRecord.spointNo, "GB2312"))
							.setDeviceID(Integer.valueOf(SysConfig.SC_ID))
							.setDeviceName(ByteString.copyFrom(SysConfig.SC, "GB2312"))
							.setReceiveTime(ByteString.copyFrom(mRecord.receivetime, "GB2312"))
							.setShotTime(ByteString.copyFrom(mRecord.drilltime, "GB2312"))
							.setWellLithology(ByteString.copyFrom(mRecord.welllithology, "GB2312"))
							.setWellNum(1)
							.setLon(drillPoint.geoPoint.getLongitude())
							.setLat(drillPoint.geoPoint.getLatitude())
							.setDrillDepth(Double.valueOf(mRecord.drilldepth))
							.setBombDepth(Double.valueOf(mRecord.bombdepth))
							.setBombWeight(Float.valueOf(mRecord.bombWeight))
							.setDetonator(Integer.valueOf(mRecord.detonator))
							.setBombId(ByteString.copyFrom(BombId, "GB2312"))
							.setDetonatorId(ByteString.copyFrom(DetonatorId, "GB2312"))
							.setStatusId(502)
							.build();
					Proto_Head head = Proto_Head.newBuilder()
							.setProtoMsgType(ProtoMsgType.protoMsgType_DpRecord)
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
			
			if (result) {
				mRecord.isupload = "1";
				mPointDBDao.updateDrilRecord(mRecord);
			}
			
			handler.sendEmptyMessage(1);
			Intent intentSure = new Intent();
			intentSure.putExtra("station", drillPoint.stationNo);
			setResult(RESULT_OK, intentSure);
			finish();
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

	@Override
	public void onBackPressed() {
		//此处不填写井号信息返回也会默认确认过井号信息。
		/*if (AddActivity.jings != null && AddActivity.jings.size() > 0) {
			handler.sendEmptyMessage(0);
			getData();
		}*/
		Intent intentSure = new Intent();
//		intentSure.putExtra("station", drillPoint.stationNo);
		setResult(RESULT_OK, intentSure);
		finish();
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
	
	
	/**
	 * 图片按比例大小压缩方法（根据Bitmap图片压缩）
	 * 
	 * @param image
	 * @return
	 */
	private Bitmap comp(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();        
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if(baos.toByteArray().length / 1024>1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出    
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        newOpts.inPreferredConfig = Config.RGB_565;//降低图片从ARGB888到RGB565
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
    }
	
	/**
	 * 质量压缩法
	 * 
	 * @param image
	 * @return
	 */
	private Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while ( baos.toByteArray().length / 1024>100) {    //循环判断如果压缩后图片是否大于100kb,大于继续压缩        
            baos.reset();//重置baos即清空baos
            options -= 10;//每次都减少10
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中

        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }
	
	/**
	 * 图片按比例大小压缩方法（根据路径获取图片并压缩）
	 * 
	 * @param srcPath
	 * @return
	 */
	private Bitmap getimage(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts);//此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
    }
	
	public class ConnTask extends AsyncTask<String, Integer, Boolean> {
		public static final int TYPE_NEW_CONN = 0;
		public static final int TYPE_RECONN = 1;
		public int nType = TYPE_NEW_CONN;

		public ConnTask(int type) {
			nType = type;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			boolean iscon = false;
			if (!DataProcess.isConning) {
				iscon = DataProcess.GetInstance().startConn(SysConfig.IP,
						SysConfig.PORT);
			}
			return iscon;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (!result) {
				new ConnTask(ConnTask.TYPE_NEW_CONN).execute("");
			}
		}

	}
}
