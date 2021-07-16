package com.robert.maps.applib.kml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openintents.filemanager.util.FileUtils;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.robert.maps.applib.R;
import com.robert.maps.applib.kml.XMLparser.GpxPoiParser;
import com.robert.maps.applib.kml.XMLparser.KmlPoiParser;
import com.robert.maps.applib.utils.DialogUtils;
import com.robert.maps.applib.utils.SimpleThreadFactory;
import com.robert.maps.applib.utils.Ut;

/**
 * 兴趣点导入
 * 
 * @author DRH
 *
 */
public class ImportPoiActivity extends Activity {
	private ListView lv_import_poi;
	
	private PoiManager mPoiManager;
	private List<File> interestFiles = new ArrayList<File>();
	private ImportFileListAdapter mInterestFileListAdapter;
	private Dialog dialog;
	private File selectedFile;

	private ProgressDialog dlgWait;
	protected ExecutorService mThreadPool = Executors.newSingleThreadExecutor(new SimpleThreadFactory("ImportPoi"));

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
		this.setContentView(R.layout.importpoi);

		if (mPoiManager == null)
			mPoiManager = new PoiManager(this);

		initViews();
		initDatas();
	}

	private void initViews() {
		lv_import_poi = (ListView) findViewById(R.id.lv_import_poi);
		
		((Button) findViewById(R.id.ImportBtn))
		.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (selectedFile == null) {
					DialogUtils.alertInfo(ImportPoiActivity.this, getResources().getString(R.string.reminder), "请先选择导入兴趣点文件");
				} else {
					doImportPOI(selectedFile);// 导入兴趣点
				}
			}
		});
		((Button) findViewById(R.id.discardButton))
		.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ImportPoiActivity.this.finish();
			}
		});
		findViewById(R.id.ll_title_back).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ImportPoiActivity.this.finish();
			}
		});;
		((TextView) findViewById(R.id.tv_title_title)).setText("兴趣点导入");
	}

	private void initDatas() {
		interestFiles = FileUtils.getInsterestsList(ImportPoiActivity.this);
		mInterestFileListAdapter = new ImportFileListAdapter(ImportPoiActivity.this, interestFiles);
		lv_import_poi.setAdapter(mInterestFileListAdapter);
		lv_import_poi.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				File file = interestFiles.get(position);
				selectedFile = file;
				
				mInterestFileListAdapter.setSelectedIndex(position);
				return;
				
			}
		});
		lv_import_poi.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					final int position, long id) {
				dialog = DialogUtils.Alert(ImportPoiActivity.this, getResources().getString(R.string.reminder), 
						"是否删除该兴趣点文件？",
						new String[]{getResources().getString(R.string.ok), getResources().getString(R.string.cancel)},
						new OnClickListener[]{new OnClickListener() {
						
							@Override
							public void onClick(View v) {
								if (dialog != null && dialog.isShowing()) {
									dialog.dismiss();
								}
								
								final File file = interestFiles.get(position);
								file.delete();
								interestFiles = FileUtils.getInsterestsList(ImportPoiActivity.this);
								mInterestFileListAdapter.setmFiles(interestFiles);
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
	private void doImportPOI(final File file) {
//		File file = new File(mFileName.getText().toString());

		if(!file.exists()){
			Toast.makeText(this, "No such file", Toast.LENGTH_LONG).show();
			return;
		}

		showDialog(R.id.dialog_wait);

		this.mThreadPool.execute(new Runnable() {
			public void run() {
				int CategoryId = 0;
//				File file = new File(mFileName.getText().toString());

				SAXParserFactory fac = SAXParserFactory.newInstance();
				SAXParser parser = null;
				try {
					parser = fac.newSAXParser();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if(parser != null){
					mPoiManager.beginTransaction();
					Ut.dd("Start parsing file " + file.getName());
					try {
						if(FileUtils.getExtension(file.getName()).equalsIgnoreCase(".kml"))
							parser.parse(file, new KmlPoiParser(mPoiManager, CategoryId));
						else if(FileUtils.getExtension(file.getName()).equalsIgnoreCase(".gpx"))
							parser.parse(file, new GpxPoiParser(mPoiManager, CategoryId));
						// 保存兴趣点
						mPoiManager.commitTransaction();
					} catch (SAXException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						mPoiManager.rollbackTransaction();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						mPoiManager.rollbackTransaction();
					} catch (IllegalStateException e) {
					} catch (OutOfMemoryError e) {
						mPoiManager.rollbackTransaction();
					}
					Ut.dd("Pois commited");
				}

				dlgWait.dismiss();
				ImportPoiActivity.this.finish();
			};
		});

	}

	@Override
	protected void onDestroy() {
		mThreadPool.shutdown();
		super.onDestroy();
		mPoiManager.FreeDatabases();
	}
	
}
