package com.robert.maps.applib.kml;

import java.io.File;

import com.robert.maps.applib.R;
import com.robert.maps.applib.utils.DialogUtils;
import com.robert.maps.applib.utils.RException;
import com.robert.maps.applib.utils.SQLiteMapDatabase;
import com.robert.maps.applib.utils.Ut;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class OutmapActivity extends Activity {
	
	private EditText ed_outmap_title;
	private TextView tv_outmap_minzoom, tv_outmap_maxzoom, tv_outmap_descr;
	
	private String outmapFileName;
	private File outmapFile;
	private SQLiteMapDatabase cacheDatabase;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.outmap);
		
		initViews();
		initDatas();
	}

	private void initViews() {
		ed_outmap_title = (EditText) findViewById(R.id.ed_outmap_title);
		tv_outmap_descr = (TextView) findViewById(R.id.tv_outmap_descr);
		tv_outmap_minzoom = (TextView) findViewById(R.id.tv_outmap_minzoom);
		tv_outmap_maxzoom = (TextView) findViewById(R.id.tv_outmap_maxzoom);
		((Button) findViewById(R.id.saveButton))
		.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				saveOutmapFile();
			}
		});
		((Button) findViewById(R.id.discardButton))
		.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				deleteDialog();
			}
		});
		findViewById(R.id.ll_title_back).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				setResult(RESULT_OK, intent);
				
				OutmapActivity.this.finish();
			}
		});;
		((TextView) findViewById(R.id.tv_title_title)).setText("离线地图编辑");
	}
	
	private void initDatas() {
		outmapFileName = getIntent().getStringExtra("file");
		outmapFile = new File(outmapFileName);
		
		try {
			cacheDatabase = new SQLiteMapDatabase();
			cacheDatabase.setFile(outmapFileName);
		} catch (RException e) {
			e.printStackTrace();
		}
		int[] zooms = cacheDatabase.getZoom();
		
		ed_outmap_title.setText(outmapFile.getName().substring(0, outmapFile.getName().indexOf(".")));
		tv_outmap_minzoom.setText("" + zooms[0]);
		tv_outmap_maxzoom.setText("" + zooms[1]);
		tv_outmap_descr.setText(cacheDatabase.getParams().toString());
	}
	
	private void saveOutmapFile() {
		String fileName = ed_outmap_title.getText().toString().trim();
		if (fileName != null && !"".equals(fileName)) {
			outmapFile.renameTo(new File(Ut.getRMapsMapsDir(OutmapActivity.this) + "/" + fileName + ".sqlitedb"));
			
			Intent intent = new Intent();
			setResult(RESULT_OK, intent);
			OutmapActivity.this.finish();
		} else {
			Toast.makeText(OutmapActivity.this, "请输入离线地图名称", Toast.LENGTH_SHORT).show();
		}
		
	}

	private Dialog dialog;
	private void deleteDialog() {
		dialog = DialogUtils.Alert(OutmapActivity.this, getResources().getString(R.string.reminder), 
			"是否删除该离线地图？",
			new String[]{getResources().getString(R.string.ok), getResources().getString(R.string.cancel)},
			new OnClickListener[]{new OnClickListener() {
			
				@Override
				public void onClick(View v) {
					if (dialog != null && dialog.isShowing()) {
						dialog.dismiss();
					}
					outmapFile.delete();
					
					Intent intent = new Intent();
					setResult(RESULT_OK, intent);
					OutmapActivity.this.finish();
				}
			}, new OnClickListener() {
			
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			}
		});	
	}
}
