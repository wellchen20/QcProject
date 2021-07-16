package com.robert.maps.applib.kml;

import java.util.Locale;

import org.andnav.osm.util.GeoPoint;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.robert.maps.applib.R;
import com.robert.maps.applib.utils.CoordFormatter;
import com.robert.maps.applib.utils.DialogUtils;

/**
 * 添加、编辑兴趣点
 * 
 * @author DRH
 *
 */
public class PoiActivity extends Activity {
	private EditText ed_poi_title, ed_poi_lat, ed_poi_lon, ed_poi_descr, ed_poi_alt;
	private Button saveButton, discardButton;
	
	private PoiPoint mPoiPoint;
	private PoiManager mPoiManager;
	private CoordFormatter mCf;
	private int id;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.poi);

		if(mPoiManager == null)
			mPoiManager = new PoiManager(this);
		mCf = new CoordFormatter(this);
		
		initViews();
		initDatas();
        getDatas();
	}

	private void initViews() {
		ed_poi_title = (EditText) findViewById(R.id.ed_poi_title);
		ed_poi_lat = (EditText) findViewById(R.id.ed_poi_lat);
		ed_poi_lon = (EditText) findViewById(R.id.ed_poi_lon);
		ed_poi_alt = (EditText) findViewById(R.id.ed_poi_alt);
		ed_poi_descr = (EditText) findViewById(R.id.ed_poi_descr);
		saveButton = ((Button) findViewById(R.id.saveButton));
		saveButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				doSaveAction();
			}
		});
		discardButton = ((Button) findViewById(R.id.discardButton));
		discardButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (id < 0) {
					Intent intent = new Intent();
					setResult(RESULT_OK, intent);
					
					PoiActivity.this.finish();
				} else {
					deleteDialog();
				}
			}
		});
		findViewById(R.id.ll_title_back).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				setResult(RESULT_OK, intent);
				
				PoiActivity.this.finish();
			}
		});;
		((TextView) findViewById(R.id.tv_title_title)).setText("兴趣点编辑");
	}
	
	private void initDatas() {
		ed_poi_lat.setHint(mCf.getHint());
		ed_poi_lat.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus) {
					try {
						ed_poi_lat.setText(mCf.convertLat(CoordFormatter.convertTrowable(ed_poi_lat.getText().toString())));
					} catch (Exception e) {
						ed_poi_lat.setText("");
					}
				}
			}
		});

		ed_poi_lon.setHint(mCf.getHint());
		ed_poi_lon.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus) {
					try {
						ed_poi_lon.setText(mCf.convertLon(CoordFormatter.convertTrowable(ed_poi_lon.getText().toString())));
					} catch (Exception e) {
						ed_poi_lon.setText("");
					}
				}
			}
		});
	}
	
	private void getDatas() {
		Bundle extras = getIntent().getExtras();
        if(extras == null) extras = new Bundle();
        id = extras.getInt("pointid", PoiPoint.EMPTY_ID());
        
        if(id < 0){
        	mPoiPoint = new PoiPoint();
			ed_poi_title.setText(extras.getString("title"));
			ed_poi_lat.setText(mCf.convertLat(extras.getDouble("lat")));
			ed_poi_lon.setText(mCf.convertLon(extras.getDouble("lon")));
			ed_poi_alt.setText(String.format(Locale.UK, "%.1f", extras.getDouble("alt", 0.0)));
			ed_poi_descr.setText(extras.getString("descr"));
			discardButton.setText(getResources().getString(R.string.cancel));
        }else{
        	mPoiPoint = mPoiManager.getPoiPoint(id);
        	
        	if(mPoiPoint == null)
        		finish();
        	
        	ed_poi_title.setText(mPoiPoint.Title);
         	ed_poi_lat.setText(mCf.convertLat(mPoiPoint.GeoPoint.getLatitude()));
        	ed_poi_lon.setText(mCf.convertLon(mPoiPoint.GeoPoint.getLongitude()));
        	ed_poi_alt.setText(String.format(Locale.UK, "%.1f", mPoiPoint.Alt));
        	ed_poi_descr.setText(mPoiPoint.Descr);
        	discardButton.setText(getResources().getString(R.string.menu_delete));
        }
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mPoiManager.FreeDatabases();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK: {
			doSaveAction();
			return true;
		}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void doSaveAction() {
		mPoiPoint.Title = ed_poi_title.getText().toString();
		mPoiPoint.Descr = ed_poi_descr.getText().toString();
		mPoiPoint.GeoPoint = GeoPoint.fromDouble(CoordFormatter.convert(ed_poi_lat.getText().toString()), CoordFormatter.convert(ed_poi_lon.getText().toString()));
		try {
			mPoiPoint.Alt = Double.parseDouble(ed_poi_alt.getText().toString());
		} catch (NumberFormatException e) {
		}
		
		mPoiManager.updatePoi(mPoiPoint);
		
		Intent intent = new Intent();
		setResult(RESULT_OK, intent);
		PoiActivity.this.finish();
		
		Toast.makeText(PoiActivity.this, R.string.message_saved, Toast.LENGTH_SHORT).show();
	}
	
	private Dialog dialog;
	private void deleteDialog() {
		dialog = DialogUtils.Alert(PoiActivity.this, getResources().getString(R.string.reminder), 
			"是否删除该兴趣点？",
			new String[]{getResources().getString(R.string.ok), getResources().getString(R.string.cancel)},
			new OnClickListener[]{new OnClickListener() {
			
				@Override
				public void onClick(View v) {
					if (dialog != null && dialog.isShowing()) {
						dialog.dismiss();
					}
					mPoiManager.deletePoi(id);
					
					Intent intent = new Intent();
					setResult(RESULT_OK, intent);
					PoiActivity.this.finish();
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
