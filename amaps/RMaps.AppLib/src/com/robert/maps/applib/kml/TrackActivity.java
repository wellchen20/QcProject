package com.robert.maps.applib.kml;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.robert.maps.applib.R;
import com.robert.maps.applib.kml.XMLparser.SimpleXML;
import com.robert.maps.applib.kml.utils.TrackStyleDrawable;
import com.robert.maps.applib.kml.utils.TrackStylePickerDialog;
import com.robert.maps.applib.kml.utils.TrackStylePickerDialog.OnTrackStyleChangedListener;
import com.robert.maps.applib.utils.DialogUtils;
import com.robert.maps.applib.utils.ListViewUtils;
import com.robert.maps.applib.utils.SimpleThreadFactory;
import com.robert.maps.applib.utils.TimeUtil;
import com.robert.maps.applib.utils.Ut;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 轨迹编辑
 * 
 * @author DRH
 *
 */
public class TrackActivity extends Activity implements OnTrackStyleChangedListener{
	private EditText ed_track_title, ed_track_descr;
	private Button saveButton, discardButton, btn_export;
	private TextView tv_track_point, tv_track_distance, tv_track_time, tv_track_date;
	
	private Track mTrack;
	private PoiManager mPoiManager;
	TrackStylePickerDialog mDialog;
	private int id;
	private ExecutorService mThreadExecutor = null;
	Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.track);

		if(mPoiManager == null)
			mPoiManager = new PoiManager(this);

		initViews();
		getDatas();
		initDatas();
		ed_track_title.requestFocus();

		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				if (msg.what==1){
					String filename = msg.obj.toString();
					Toast.makeText(TrackActivity.this,"轨迹保存在根目录下："+filename,Toast.LENGTH_LONG).show();
				}else if (msg.what==0){
					Toast.makeText(TrackActivity.this,"轨迹导出失败",Toast.LENGTH_LONG).show();
				}
			}
		};
	}

	private void initViews() {
		ed_track_title = (EditText) findViewById(R.id.ed_track_title);
		ed_track_descr = (EditText) findViewById(R.id.ed_track_descr);
		saveButton = ((Button) findViewById(R.id.saveButton));
		saveButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				doSaveAction();
			}
		});
		discardButton = ((Button) findViewById(R.id.discardButton));
		btn_export = (Button) findViewById(R.id.btn_export);
		discardButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (id < 0) {
					Intent intent = new Intent();
					setResult(RESULT_OK, intent);
					TrackActivity.this.finish();
				} else {
					deleteDialog();
				}
			}
		});
		btn_export.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (id < 0) {
					Intent intent = new Intent();
					setResult(RESULT_OK, intent);
					TrackActivity.this.finish();
				} else {
					DoExportTrackGPX(id);
//					DoExportTrackKML(id);
				}
			}
		});
		findViewById(R.id.ll_title_back).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				setResult(RESULT_OK, intent);
				TrackActivity.this.finish();
			}
		});;
		((TextView) findViewById(R.id.tv_title_title)).setText("轨迹编辑");
		findViewById(R.id.trackstyle).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mDialog = new TrackStylePickerDialog(TrackActivity.this, mTrack.Color, mTrack.Width, mTrack.ColorShadow, mTrack.ShadowRadius);
				mDialog.setOnTrackStyleChangedListener(TrackActivity.this);
				mDialog.show();
			}
		});
		tv_track_point = (TextView) findViewById(R.id.tv_track_point);
		tv_track_distance = (TextView) findViewById(R.id.tv_track_distance);
		tv_track_time = (TextView) findViewById(R.id.tv_track_time);
		tv_track_date = (TextView) findViewById(R.id.tv_track_date);
	}
	
	private void initDatas() {
		final Drawable dr = new TrackStyleDrawable(mTrack.Color, mTrack.Width, mTrack.ColorShadow, mTrack.ShadowRadius);
		final Drawable[] d = {getResources().getDrawable(R.drawable.r_home_other1), dr};
		LayerDrawable ld = new LayerDrawable(d);
		((Button) findViewById(R.id.trackstyle)).setCompoundDrawablesWithIntrinsicBounds(null, null, ld, null);
	}
	
	private void getDatas() {
		Bundle extras = getIntent().getExtras();
        if(extras == null) extras = new Bundle();
        id = extras.getInt("id", PoiPoint.EMPTY_ID());
        if(id < 0){
        	mTrack = new Track();
			ed_track_title.setText(extras.getString("name"));
			ed_track_descr.setText(extras.getString("descr"));
			discardButton.setText(getResources().getString(R.string.menu_doNotSave));
        }else{
        	mTrack = mPoiManager.getTrack(id);

        	if(mTrack == null)
        		finish();

        	ed_track_title.setText(mTrack.Name);
        	ed_track_descr.setText(mTrack.Descr);
        	discardButton.setText(getResources().getString(R.string.menu_delete));
        	tv_track_point.setText(mTrack.Cnt + "");
        	String distance = "";
        	if (mTrack.Distance > 1000) {
        		distance = String.format("%.3f", (mTrack.Distance / 1000)) + getResources().getString(R.string.km);
			} else {
				distance = String.format("%.3f", mTrack.Distance) + getResources().getString(R.string.m);
			}
        	tv_track_distance.setText(distance);
        	tv_track_time.setText(TimeUtil.getTimeLong(TrackActivity.this, mTrack.Duration));
        	tv_track_date.setText(TimeUtil.getTime(mTrack.Date));
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
			// 带考量,是否返回就保存
			doSaveAction();
			return true;
		}
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 保存并更新轨迹
	 */
	private void doSaveAction() {
		mTrack.Name = ed_track_title.getText().toString();
		mTrack.Descr = ed_track_descr.getText().toString();

		mPoiManager.updateTrack(mTrack);
		Intent intent = new Intent();
		setResult(RESULT_OK, intent);
		TrackActivity.this.finish();

		Toast.makeText(TrackActivity.this, R.string.message_saved, Toast.LENGTH_SHORT).show();
	}

	public void onTrackStyleChanged(int color, int width, int colorshadow, double shadowradius) {
		mTrack.Color = color;
		mTrack.Width = width;
		mTrack.ColorShadow = colorshadow;
		mTrack.ShadowRadius = shadowradius;
		mTrack.Style = mTrack.getStyle();
		
		initDatas();
	}
	
	private Dialog dialog;
	private void deleteDialog() {
		dialog = DialogUtils.Alert(TrackActivity.this, getResources().getString(R.string.reminder), 
			"是否删除该轨迹？",
			new String[]{getResources().getString(R.string.ok), getResources().getString(R.string.cancel)},
			new OnClickListener[]{new OnClickListener() {
			
				@Override
				public void onClick(View v) {
					if (dialog != null && dialog.isShowing()) {
						dialog.dismiss();
					}
					mPoiManager.deleteTrack(id);
					Intent intent = new Intent();
					setResult(RESULT_OK, intent);
					TrackActivity.this.finish();
				}
			}, new OnClickListener() {
			
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			}
		});
	}
	/**
	 * 导出轨迹为GPX格式
	 *
	 * @param id
	 */
	private void DoExportTrackGPX(int id) {
		final int trackid = id;
		if(mThreadExecutor == null)
			mThreadExecutor = Executors.newSingleThreadExecutor(new SimpleThreadFactory("DoExportTrackGPX"));

		this.mThreadExecutor.execute(new Runnable() {
			public void run() {
				final Track track = mPoiManager.getTrack(trackid);
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
				SimpleXML xml = new SimpleXML("gpx");
				xml.setAttr("xsi:schemaLocation", "http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd");
				xml.setAttr("xmlns", "http://www.topografix.com/GPX/1/0");
				xml.setAttr("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
				xml.setAttr("creator", "RMaps - http://code.google.com/p/robertprojects/");
				xml.setAttr("version", "1.0");

				xml.createChild("name").setText(track.Name);
				xml.createChild("desc").setText(track.Descr);

				SimpleXML trk = xml.createChild("trk");
				SimpleXML trkseg = trk.createChild("trkseg");
				SimpleXML trkpt = null;
				for (Track.TrackPoint tp : track.getPoints()){
					trkpt = trkseg.createChild("trkpt");
					trkpt.setAttr("lat", Double.toString(tp.lat));
					trkpt.setAttr("lon", Double.toString(tp.lon));
					trkpt.createChild("ele").setText(Double.toString(tp.alt));
					trkpt.createChild("time").setText(formatter.format(tp.date));
				}

				String filename = Environment.getExternalStorageDirectory().getPath() + "/" + track.Name+".gpx";//轨迹保存在根目录下
				FileOutputStream fos;
				try {
					String gpx = SimpleXML.saveXml(xml);
					byte[] buffer = gpx.getBytes();
					fos = new FileOutputStream(filename);
					if (buffer != null) {
						fos.write(buffer, 0, buffer.length);
						fos.flush();
						fos.close();
					}
					Log.e("gpx", "gpx: "+gpx );
					Message msg = new Message();
					msg.what=1;
					msg.obj = track.Name;
					handler.sendMessage(msg);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					Message msg = new Message();
					msg.what=0;
					msg.obj = track.Name;
					handler.sendMessage(msg);
					e.printStackTrace();
				}

			}
		});

	}

	/**
	 * 导出轨迹为KML格式
	 *
	 * @param id
	 */
	private void DoExportTrackKML(int id) {
		final int trackid = id;
		if(mThreadExecutor == null)
			mThreadExecutor = Executors.newSingleThreadExecutor(new SimpleThreadFactory("DoExportTrackKML"));

		this.mThreadExecutor.execute(new Runnable() {
			public void run() {
				final Track track = mPoiManager.getTrack(trackid);

				SimpleXML xml = new SimpleXML("kml");
				xml.setAttr("xmlns:gx", "http://www.google.com/kml/ext/2.2");
				xml.setAttr("xmlns", "http://www.opengis.net/kml/2.2");

				SimpleXML Placemark = xml.createChild("Placemark");
				Placemark.createChild("name").setText(track.Name);
				Placemark.createChild("description").setText(track.Descr);
				SimpleXML LineString = Placemark.createChild("LineString");
				SimpleXML coordinates = LineString.createChild("coordinates");
				StringBuilder builder = new StringBuilder();

				for (Track.TrackPoint tp : track.getPoints()){
					builder.append(tp.lon).append(",").append(tp.lat).append(",").append(tp.alt).append(" ");
				}
				coordinates.setText(builder.toString().trim());

				File folder = Ut.getRMapsProjectPrivateTracksOutputDir(TrackActivity.this);
				String filename = folder.getAbsolutePath() + "/track" + trackid + ".kml";
				File file = new File(filename);
				FileOutputStream out;
				try {
					file.createNewFile();
					out = new FileOutputStream(file);
					OutputStreamWriter wr = new OutputStreamWriter(out);
					wr.write(SimpleXML.saveXml(xml));
					wr.close();
					Message message = new Message();
					message.what=1;
					message.obj = filename;
					handler.sendMessage(message);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					Message message = new Message();
					message.what=0;
					message.obj = filename;
					handler.sendMessage(message);
					e.printStackTrace();
				}

			}
		});


	}
}
