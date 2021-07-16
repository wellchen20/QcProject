package com.mtkj.cnpc.activity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.andnav.osm.util.GeoPoint;
import org.openintents.filemanager.util.FileUtils;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mtkj.cnpc.protocol.bean.ArrangePoint;
import com.mtkj.cnpc.protocol.bean.DrillPoint;
import com.mtkj.cnpc.protocol.bean.ShotPoint;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.constants.SysConfig.WorkType;
import com.mtkj.cnpc.sqlite.PointDBDao;
import com.mtkj.utils.StatusBarUtil;
import com.robert.maps.applib.R;
import com.robert.maps.applib.kml.ImportFileListAdapter;
import com.robert.maps.applib.utils.DialogUtils;
import com.robert.maps.applib.utils.SimpleThreadFactory;

/**
 * 兴趣点导入
 * 
 * @author DRH
 *
 */
public class ImportTaskActivity extends Activity {
	private ListView lv_import_task;
	
	private List<File> taskFiles = new ArrayList<File>();
	private ImportFileListAdapter mTaskFileListAdapter;
	private Dialog dialog;
	private File selectedFile;
	protected PointDBDao mPointDBDao;
	private ProgressDialog dlgWait;
	protected ExecutorService mThreadPool = Executors.newSingleThreadExecutor(new SimpleThreadFactory("ImportTask"));

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.importpoi);
		StatusBarUtil.setTheme(this);
		initViews();
		initDatas();
	}

	private void initViews() {
		mPointDBDao = new PointDBDao(this);
		lv_import_task = (ListView) findViewById(R.id.lv_import_poi);
		
		((Button) findViewById(R.id.ImportBtn))
		.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (selectedFile == null) {
					DialogUtils.alertInfo(ImportTaskActivity.this, getResources().getString(R.string.reminder), "请先选择导入炮点文件");
				} else {
					doImportTask(selectedFile);// 导入任务
				}
			}
		});
		findViewById(R.id.ll_title_back).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ImportTaskActivity.this.finish();
			}
		});;
		((TextView) findViewById(R.id.tv_title_title)).setText("兴趣点导入");
	}

	private void initDatas() {
		taskFiles = FileUtils.getTasksList(ImportTaskActivity.this);
		mTaskFileListAdapter = new ImportFileListAdapter(ImportTaskActivity.this, taskFiles);
		lv_import_task.setAdapter(mTaskFileListAdapter);
		lv_import_task.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				File file = taskFiles.get(position);
				selectedFile = file;
				
				mTaskFileListAdapter.setSelectedIndex(position);
				return;
				
			}
		});
		lv_import_task.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					final int position, long id) {
				dialog = DialogUtils.Alert(ImportTaskActivity.this, getResources().getString(R.string.reminder), 
						"是否删除该炮点文件？",
						new String[]{getResources().getString(R.string.ok), getResources().getString(R.string.cancel)},
						new OnClickListener[]{new OnClickListener() {
						
							@Override
							public void onClick(View v) {
								if (dialog != null && dialog.isShowing()) {
									dialog.dismiss();
								}
								
								final File file = taskFiles.get(position);
								file.delete();
								taskFiles = FileUtils.getInsterestsList(ImportTaskActivity.this);
								mTaskFileListAdapter.setmFiles(taskFiles);
							}
						}, new OnClickListener() {
						
							@Override
							public void onClick(View v) {
								dialog.dismiss();
							}
						}
				});
				return false;
			}
		});
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if(id == R.id.dialog_wait) {
			dlgWait = new ProgressDialog(this);
			dlgWait.setMessage("Please wait while loading...");
			dlgWait.setIndeterminate(true);
			dlgWait.setCancelable(false);
			return dlgWait;
		}
		return null;
	}

	/**
	 * 导入兴趣点
	 */
	private void doImportTask(final File file) {
//		File file = new File(mFileName.getText().toString());

		if(!file.exists()){
			Toast.makeText(this, "No such file", Toast.LENGTH_LONG).show();
			return;
		}
		
		new ImportDataTask(file).execute("");
	}
	
	/****
	 * 数据导入操作
	 * 
	 * @author TNT
	 * 
	 */
	class ImportDataTask extends AsyncTask<String, Integer, Boolean> {
		
		private File importFile;
		
		ProgressDialog progressDialog = null;
		private long startTime = 0;
		int totalCount = 0;
		
		private boolean isBreak;
		
		private String getTimeText(long time) {
			String castTime = "";
			if (time > 1000 * 60 * 60) {
				castTime += (time / (1000 * 60 * 60)) + "小时"
						+ getTimeText(time % (1000 * 60 * 60));

			} else if (time > 1000 * 60) {
				castTime += (time / (1000 * 60)) + "分钟"
						+ getTimeText((time % (1000 * 60)));

			} else if (time > 1000) {
				castTime += (time / (1000)) + "秒";
			} else {
				castTime = "<1秒";
			}
			return castTime;
		}
		
		public ImportDataTask(File file) {
			importFile = file;
			startTime = System.currentTimeMillis();
		}
		
		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(ImportTaskActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setTitle("数据导入");
			progressDialog.setMessage("数据计算中...");
			progressDialog.setProgress(1);
			progressDialog.setCancelable(false);
			progressDialog.setButton(ProgressDialog.BUTTON2, "中断",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							isBreak = true;
						}
					});
			progressDialog.show();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			totalCount = getCsvFileRecordCount(importFile);
			progressDialog.setMax(totalCount);
			// 读取导入csv文件首行
			List<String> dataList = null;
			// 获取csv文件读取对象
			CsvListReader listReader = getCsvReader(importFile);
			if (listReader != null) {
				try {
					int i = 0;
					// 去首行
					listReader.read();
					
					while ((dataList = listReader.read()) != null) {
						if (isBreak) {
							break;
						}
						if (SysConfig.workType == WorkType.WORK_TYPE_SHOT) {
							ShotPoint shotPoint = new ShotPoint();
							
							shotPoint.stationNo = dataList.get(0);
							shotPoint.lineNo = dataList.get(1);
							shotPoint.spointNo = dataList.get(2);
							shotPoint.geoPoint = GeoPoint.fromDouble(Double.valueOf(dataList.get(4)), 
																Double.valueOf(dataList.get(3)));
							shotPoint.Alt = Double.valueOf(dataList.get(5));
							
							mPointDBDao.insertShotPoint(shotPoint);
						} else if (SysConfig.workType == WorkType.WORK_TYPE_DRILE) {
							DrillPoint drillPoint = new DrillPoint();
							
							drillPoint.stationNo = dataList.get(0);
							drillPoint.lineNo = dataList.get(1);
							drillPoint.spointNo = dataList.get(2);
							drillPoint.geoPoint = GeoPoint.fromDouble(Double.valueOf(dataList.get(4)), 
																Double.valueOf(dataList.get(3)));
							drillPoint.Alt = Double.valueOf(dataList.get(5));
							drillPoint.wellnum = Double.valueOf(dataList.get(6));
							drillPoint.desWellDepth = Double.valueOf(dataList.get(7));
							drillPoint.bombWeight = Float.valueOf(dataList.get(8));
							drillPoint.detonator = Double.valueOf(dataList.get(9));
							
							mPointDBDao.insertDrillPoint(drillPoint);
						} else if (SysConfig.workType == WorkType.WORK_TYPE_ARRANGE){
							ArrangePoint arrangePoint = new ArrangePoint();
							arrangePoint.stationNo = dataList.get(0);
							arrangePoint.lineNo = dataList.get(1);
							arrangePoint.spointNo = dataList.get(2);
							arrangePoint.geoPoint = GeoPoint.fromDouble(Double.valueOf(dataList.get(4)),
									Double.valueOf(dataList.get(3)));

							mPointDBDao.insertArrangePoint(arrangePoint);
						}
						publishProgress(++i);
					}
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			} else {
				return false;
			}
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			progressDialog.setProgress(values[0] + 1);
			progressDialog
					.setMessage("业务点导入中 ..."
							+ "\n"
							+ "已用时间:"
							+ getTimeText(System.currentTimeMillis()
									- startTime)
							+ "\n"
							+ "剩余大约:"
							+ getTimeText(((System.currentTimeMillis() - startTime) / values[0])
									* (totalCount - values[0])));
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
			if (result) {
				ImportTaskActivity.this.finish();
			} else {
				Toast.makeText(ImportTaskActivity.this,"导入任务失败",Toast.LENGTH_SHORT).show();
			}
		}
		
		private int getCsvFileRecordCount(File file) {
			int ncount = 0;
			try {
				CsvListReader reader = getCsvReader(file);
				if (reader != null) {
					while (reader.read() != null) {
					}
					ncount = reader.getLineNumber();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return ncount;
		}
		
		private CsvListReader getCsvReader(File file) {
			CsvListReader listReader = null;
			try {
				if (!IsUTF8(file)) {
					listReader = new CsvListReader(new BufferedReader(
							new InputStreamReader(
									new FileInputStream(file), "GB2312")),
							CsvPreference.STANDARD_PREFERENCE);

				} else {
					listReader = new CsvListReader(new BufferedReader(
							new InputStreamReader(
									new FileInputStream(file), "UTF-8")),
							CsvPreference.STANDARD_PREFERENCE);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return listReader;
		}
	}
	
	public static boolean IsUTF8(File file) {
		boolean bRt = false;
		if (file != null && file.exists()) {
			InputStream in;
			try {
				in = new java.io.FileInputStream(file);
				byte[] b = new byte[3];
				in.read(b);
				in.close();
				if (b[0] == -17 && b[1] == -69 && b[2] == -65) {
					bRt = true;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bRt;
	}

	@Override
	protected void onDestroy() {
		mThreadPool.shutdown();
		super.onDestroy();
	}
	
}
