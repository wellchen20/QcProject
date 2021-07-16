package com.mtkj.cnpc.activity;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.andnav.osm.util.GeoPoint;
import org.openintents.filemanager.util.FileUtils;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mtkj.cnpc.activity.MainActivity.RequestCode;
import com.mtkj.cnpc.activity.adapter.OnMapListAdapter;
import com.mtkj.cnpc.activity.adapter.OnMapListAdapter.IInmap;
import com.mtkj.cnpc.activity.adapter.OutMapListAdapter;
import com.mtkj.cnpc.activity.adapter.OutMapListAdapter.IOutmap;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.constants.SysContants;
import com.mtkj.cnpc.view.swipemenulistview.SwipeMenuListView;
import com.mtkj.cnpc.R;
import com.robert.maps.applib.downloader.AreaSelectorActivity;
import com.robert.maps.applib.kml.OutmapActivity;
import com.robert.maps.applib.kml.XMLparser.PredefMapsParser;
import com.robert.maps.applib.tileprovider.TileProviderFileBase;
import com.robert.maps.applib.tileprovider.TileSource;
import com.robert.maps.applib.tileprovider.TileSourceBase;
import com.robert.maps.applib.utils.DialogUtils;
import com.robert.maps.applib.utils.ListViewUtils;
import com.robert.maps.applib.utils.SQLiteMapDatabase;

@SuppressLint("ResourceAsColor")
public class MapManagerActivity extends BaseActivity {

	private TextView tv_online_map, tv_outline_map, tv_map_settings, tv_mapset_rotate;
	private LinearLayout ll_outmap_maker, ll_onlinemap, ll_outlinemap, ll_map_settings;
	private CheckBox ck_online_map, ck_outline_map, ck_mapset_rotate, ck_map_settings;
	private ListView lv_online_map;
	private SwipeMenuListView lv_outline_map;
	private RelativeLayout rl_outmap_null;
	
	private String selectedName;
	private int Latitude, Longitude, ZoomLevel;
	private boolean isOverlayer = false, isRotate;
	// 离线地图
	private List<File> outmapFiles = new ArrayList<File>();
	private OutMapListAdapter mOutMapListAdapter;
	
	// 在线地图
	private ArrayList<String> onmapStrings = new ArrayList<String>();
	private ArrayList<String> onmapIds = new ArrayList<String>();
	private OnMapListAdapter mOnMapListAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_manager);
		
		initViews();
		initDatas();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		getdatas();
	}

	private void initViews() {
		tv_online_map = (TextView) findViewById(R.id.tv_online_map);
		tv_outline_map = (TextView) findViewById(R.id.tv_outline_map);
		ll_onlinemap = (LinearLayout) findViewById(R.id.ll_onlinemap);
		ll_outlinemap = (LinearLayout) findViewById(R.id.ll_outlinemap);
		ck_online_map = (CheckBox) findViewById(R.id.ck_online_map);
		ck_outline_map = (CheckBox) findViewById(R.id.ck_outline_map);
		lv_online_map = (ListView) findViewById(R.id.lv_online_map);
		lv_outline_map = (SwipeMenuListView) findViewById(R.id.lv_outline_map);
		ll_outmap_maker = (LinearLayout) findViewById(R.id.ll_outmap_maker);
		ck_mapset_rotate = (CheckBox) findViewById(R.id.ck_mapset_rotate);
		tv_map_settings = (TextView) findViewById(R.id.tv_map_settings);
		ll_map_settings = (LinearLayout) findViewById(R.id.ll_map_setttings);
		ck_map_settings = (CheckBox) findViewById(R.id.ck_map_settings);
		rl_outmap_null = (RelativeLayout) findViewById(R.id.rl_outmap_null);
		tv_mapset_rotate = (TextView) findViewById(R.id.tv_mapset_rotate);
		findViewById(R.id.ll_title_back).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("rotate", isRotate);
//				if (!SysConfig.isOnlineMap) {
//					if (selectedName != null && !"".equals(selectedName)) {
//						intent.putExtra("mapId", selectedName);
//					} else {
//						DialogUtils.alertInfo(MapManagerActivity.this, getResources().getString(R.string.reminder), "请选择地图");
//						return;
//					}
//				}
				setResult(RESULT_CANCELED, intent);
				MapManagerActivity.this.finish();
			}
		});
		((TextView) findViewById(R.id.tv_title_title)).setText(getResources().getString(R.string.activity_map_manager));
	}
	
	private void initDatas() {
		ck_online_map.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					tv_online_map.setTextColor(0xff00B9EC);
					ck_online_map.setChecked(true);
					ll_onlinemap.setVisibility(View.VISIBLE);
					
					mOnMapListAdapter.notifyDataSetChanged();
					ListViewUtils.getListViewHeightBasedOnChildren(lv_online_map);
				} else {
					tv_online_map.setTextColor(0xff000000);
					ck_online_map.setChecked(false);
					ll_onlinemap.setVisibility(View.GONE);
				}
			}
		});
		tv_online_map.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean isChecked = !ck_online_map.isChecked();
				if (isChecked) {
					tv_online_map.setTextColor(0xff00B9EC);
					ck_online_map.setChecked(true);
					ll_onlinemap.setVisibility(View.VISIBLE);
					
					mOnMapListAdapter.notifyDataSetChanged();
					ListViewUtils.getListViewHeightBasedOnChildren(lv_online_map);
				} else {
					tv_online_map.setTextColor(0xff000000);
					ck_online_map.setChecked(false);
					ll_onlinemap.setVisibility(View.GONE);
				}
			}
		});
		ck_outline_map.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					tv_outline_map.setTextColor(0xff00B9EC);
					ck_outline_map.setChecked(true);
					ll_outlinemap.setVisibility(View.VISIBLE);
					
					mOutMapListAdapter.notifyDataSetChanged();
					ListViewUtils.getListViewHeightBasedOnChildren(lv_outline_map);
				} else {
					tv_outline_map.setTextColor(0xff000000);
					ck_outline_map.setChecked(false);
					ll_outlinemap.setVisibility(View.GONE);
				}
			}
		});
		tv_outline_map.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean isChecked = !ck_outline_map.isChecked();
				if (isChecked) {
					tv_outline_map.setTextColor(0xff00B9EC);
					ck_outline_map.setChecked(true);
					ll_outlinemap.setVisibility(View.VISIBLE);
					
					mOutMapListAdapter.notifyDataSetChanged();
					ListViewUtils.getListViewHeightBasedOnChildren(lv_outline_map);
				} else {
					tv_outline_map.setTextColor(0xff000000);
					ck_outline_map.setChecked(false);
					ll_outlinemap.setVisibility(View.GONE);
				}
			}
		});
		ck_map_settings.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					tv_map_settings.setTextColor(0xff00B9EC);
					ck_map_settings.setChecked(true);
					ll_map_settings.setVisibility(View.VISIBLE);
					
				} else {
					tv_map_settings.setTextColor(0xff000000);
					ck_map_settings.setChecked(false);
					ll_map_settings.setVisibility(View.GONE);
				}
			}
		});
		tv_map_settings.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean isChecked = !ck_map_settings.isChecked();
				if (isChecked) {
					tv_map_settings.setTextColor(0xff00B9EC);
					ck_map_settings.setChecked(true);
					ll_map_settings.setVisibility(View.VISIBLE);
					
				} else {
					tv_map_settings.setTextColor(0xff000000);
					ck_map_settings.setChecked(false);
					ll_map_settings.setVisibility(View.GONE);
				}
			}
		});
		ll_outmap_maker.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
					startActivity(new Intent(MapManagerActivity.this, AreaSelectorActivity.class)
					.putExtra("new", true)
					.putExtra("MapName", TileSource.MAPNIK)
					.putExtra("Latitude", Latitude)
					.putExtra("Longitude", Longitude)
					.putExtra("ZoomLevel", ZoomLevel));
					MapManagerActivity.this.finish();
			}
		});
		ck_mapset_rotate.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				isRotate = isChecked;
			}
		});
		tv_mapset_rotate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				isRotate = !ck_mapset_rotate.isChecked();
				ck_mapset_rotate.setChecked(isRotate);
			}
		});
	}
	
	private void getdatas() {
		// 离线地图
		outmapFiles = FileUtils.getOutlineMapList(MapManagerActivity.this);
		mOutMapListAdapter = new OutMapListAdapter(MapManagerActivity.this, outmapFiles, "", outmap);
		lv_outline_map.setAdapter(mOutMapListAdapter);
//		SwipeMenuCreator creator = new SwipeMenuCreator() {
//			
//			@Override
//			public void create(SwipeMenu menu) {
//				SwipeMenuItem deleteItem = new SwipeMenuItem(MapManagerActivity.this);
//				// set item background
//				deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
//				// set item width
//				deleteItem.setWidth(dp2px(70));
//				
//				deleteItem.setTitle(R.string.menu_delete);
//				deleteItem.setTitleSize(20);
//				deleteItem.setTitleColor(0xffffffff);
//				// add to menu
//				menu.addMenuItem(deleteItem);
//			}
//		};
//		lv_outline_map.setMenuCreator(creator);
//		lv_outline_map.setOnMenuItemClickListener(menuItemClickListener);
		lv_outline_map.setOnItemClickListener(outmapItemClickListener);
//		lv_outline_map.setOnItemLongClickListener(outmapOnItemLongClickListener);
		ListViewUtils.getListViewHeightBasedOnChildren(lv_outline_map);
		if (outmapFiles != null && outmapFiles.size() > 0) {
			lv_outline_map.setVisibility(View.VISIBLE);
			rl_outmap_null.setVisibility(View.GONE);
		} else {
			lv_outline_map.setVisibility(View.GONE);
			rl_outmap_null.setVisibility(View.VISIBLE);
		}
		
		// 在线地图
		final SAXParserFactory fac = SAXParserFactory.newInstance();
		SAXParser parser = null;
		if (onmapIds != null) {
			onmapIds.clear();
		}
		if (onmapStrings != null) {
			onmapStrings.clear();
		}
		PredefMapsParser mapsParser = new PredefMapsParser(onmapIds, onmapStrings, true, false, 0);
		try {
			parser = fac.newSAXParser();
			if(parser != null){
				final InputStream in = getResources().openRawResource(R.raw.predefmaps);
				parser.parse(in, mapsParser);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		mOnMapListAdapter = new OnMapListAdapter(MapManagerActivity.this, onmapStrings, onmapIds, inmap, "");
		lv_online_map.setAdapter(mOnMapListAdapter);
		ListViewUtils.getListViewHeightBasedOnChildren(lv_online_map);
		
		selectedName = getIntent().getStringExtra("mapname");
		if (selectedName != null) {
			if (SysConfig.isOnlineMap) {
				
				mOnMapListAdapter.setSelectedName(selectedName);
				mOutMapListAdapter.setSelectedMapName("");
				
				ck_online_map.setChecked(true);
				ck_outline_map.setChecked(false);
				tv_online_map.setTextColor(0xff00B9EC);
				tv_outline_map.setTextColor(0xff000000);
				
				ll_onlinemap.setVisibility(View.VISIBLE);
				ll_outlinemap.setVisibility(View.GONE);
			} else {
				mOnMapListAdapter.setSelectedName("");
				mOutMapListAdapter.setSelectedMapName(selectedName);
				
				ck_online_map.setChecked(false);
				ck_outline_map.setChecked(true);
				
				tv_online_map.setTextColor(0xff000000);
				tv_outline_map.setTextColor(0xff00B9EC);
				
				ll_onlinemap.setVisibility(View.GONE);
				ll_outlinemap.setVisibility(View.VISIBLE);
			}
		}
		tv_map_settings.setTextColor(0xff000000);
		ck_map_settings.setChecked(false);
		ll_map_settings.setVisibility(View.GONE);
		
		if (MainActivity.unRectifyLocation != null) {
			GeoPoint locPoint = GeoPoint.fromDouble(MainActivity.unRectifyLocation.getLatitude(), MainActivity.unRectifyLocation.getLongitude());
			Latitude = getIntent().getIntExtra("Latitude", locPoint.getLatitudeE6());
			Longitude = getIntent().getIntExtra("Longitude", locPoint.getLongitudeE6());
		}
		ZoomLevel = getIntent().getIntExtra("ZoomLevel", 16);
		isOverlayer = getIntent().getBooleanExtra("isoverlay", false);
		
		isRotate = getIntent().getBooleanExtra("rotate", false);
		ck_mapset_rotate.setChecked(isRotate);
		
	}
	
	private IOutmap outmap = new IOutmap() {
		
		@Override
		public void selectedOutmap(int position) {
			if (position == -1) {
				mOutMapListAdapter.setSelectedMapName("");
				mOutMapListAdapter.notifyDataSetChanged();
			} else {
				SysConfig.isOnlineMap = false;
				ck_outline_map.setChecked(true);
				setData(SysContants.ISONLINEMAP, SysConfig.isOnlineMap);
				
				mOnMapListAdapter.setSelectedName("");
				mOnMapListAdapter.notifyDataSetChanged();
				mOutMapListAdapter.setSelectedMapName(outmapFiles.get(position).getName());
				mOutMapListAdapter.notifyDataSetChanged();
				
				int[] zooms = initOutMap(position);
				
				Intent intent = new Intent();
				intent.putExtra("mapId", "usermap_" + outmapFiles.get(position).getName());
				intent.putExtra("rotate", isRotate);
				intent.putExtra("isOnline", false);
				if (zooms != null) {
					intent.putExtra("maxzoom", zooms[1]);
				}
				if (centerPoint != null) {
					intent.putExtra("lat", centerPoint.getLatitude());
					intent.putExtra("lon", centerPoint.getLongitude());
				}
				setResult(RESULT_OK, intent);
				MapManagerActivity.this.finish();
			}
		}

		private GeoPoint centerPoint;
		private int[] initOutMap(int position) {
			int[] zooms = null;
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MapManagerActivity.this);
			String name = outmapFiles.get(position).getName();
			if (name.toLowerCase().endsWith("sqlitedb")) {
				String string = pref.getString(TileSourceBase.PREF_USERMAP_ + name + "_name", "");
				if (string == null || "".equals(string)) {
					final Editor editor = pref.edit();
					editor.putBoolean(TileSourceBase.PREF_USERMAP_ + name + "_enabled", true);
					editor.putString(TileSourceBase.PREF_USERMAP_ + name + "_name", (String) name.subSequence(0, name.length() - 9));
					editor.putString(TileSourceBase.PREF_USERMAP_ + name + "_projection", "1");
					editor.putString(TileSourceBase.PREF_USERMAP_ + name + "_baseurl", outmapFiles.get(position).getAbsolutePath());
					editor.putBoolean(TileSourceBase.PREF_USERMAP_+name+"_isoverlay", isOverlayer);
					editor.commit();
				}
				
				try {
					SQLiteMapDatabase cacheDatabase = new SQLiteMapDatabase();
					cacheDatabase.setFile(outmapFiles.get(position).getAbsolutePath());
					zooms = cacheDatabase.getZoom();
					centerPoint = cacheDatabase.getMapCenter();
					if (zooms != null) {
						TileProviderFileBase provider = new TileProviderFileBase(MapManagerActivity.this);
						provider.CommitIndex(TileSourceBase.PREF_USERMAP_ + name, 0, 0, zooms[0], zooms[1]);
						provider.Free();
					}
				} catch (Exception e) {
				}
			}
			return zooms;
		}
		
		@Override
		public void deleteOutmap(int position) {
			File file = outmapFiles.get(position);
			file.delete();
			
			// 离线地图
			outmapFiles = FileUtils.getOutlineMapList(MapManagerActivity.this);
			mOutMapListAdapter.setmFiles(outmapFiles);
			ListViewUtils.getListViewHeightBasedOnChildren(lv_outline_map);
			
			if (outmapFiles != null && outmapFiles.size() > 0) {
				lv_outline_map.setVisibility(View.VISIBLE);
				rl_outmap_null.setVisibility(View.GONE);
			} else {
				lv_outline_map.setVisibility(View.GONE);
				rl_outmap_null.setVisibility(View.VISIBLE);
			}
		}
	};
	
	private OnItemClickListener outmapItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Intent intent = new Intent(MapManagerActivity.this, OutmapActivity.class);
			intent.putExtra("file", outmapFiles.get(position).getAbsolutePath());
			startActivityForResult(intent, RequestCode.OUTMAP);
		}
	};
	
	private Dialog dialog;
	private OnItemLongClickListener outmapOnItemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				final int position, long id) {
			dialog = DialogUtils.Alert(MapManagerActivity.this, getResources().getString(R.string.reminder), 
					"是否删除该离线地图文件？",
					new String[]{getResources().getString(R.string.ok), getResources().getString(R.string.cancel)},
					new OnClickListener[]{new OnClickListener() {
					
						@Override
						public void onClick(View v) {
							if (dialog != null && dialog.isShowing()) {
								dialog.dismiss();
							}
							
							File file = outmapFiles.get(position);
							file.delete();
							
							// 离线地图
							outmapFiles = FileUtils.getOutlineMapList(MapManagerActivity.this);
							mOutMapListAdapter.setmFiles(outmapFiles);
							ListViewUtils.getListViewHeightBasedOnChildren(lv_outline_map);
							
							if (outmapFiles != null && outmapFiles.size() > 0) {
								lv_outline_map.setVisibility(View.VISIBLE);
								rl_outmap_null.setVisibility(View.GONE);
							} else {
								lv_outline_map.setVisibility(View.GONE);
								rl_outmap_null.setVisibility(View.VISIBLE);
							}
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
	};
	
//	private OnMenuItemClickListener menuItemClickListener = new OnMenuItemClickListener() {
//		
//		@Override
//		public void onMenuItemClick(int position, SwipeMenu menu, int index) {
//			switch (index) {
//			case 0:
//				File file = outmapFiles.get(position);
//				file.delete();
//				
//				// 离线地图
//				outmapFiles = FileUtils.getOutlineMapList(MapManagerActivity.this);
//				mOutMapListAdapter.setmFiles(outmapFiles);
//				
//				ListViewUtils.getListViewHeightBasedOnChildren(lv_online_map);
//				ListViewUtils.getListViewHeightBasedOnChildren(lv_outline_map);
//				break;
//
//			default:
//				break;
//			}
//		}
//	};
	
	private IInmap inmap = new IInmap() {
		
		@Override
		public void selectedInmap(int position) {
			if (position == -1) {
				mOnMapListAdapter.setSelectedName("");
				mOnMapListAdapter.notifyDataSetChanged();
			} else {
				SysConfig.isOnlineMap = true;
				ck_online_map.setChecked(true);
				setData(SysContants.ISONLINEMAP, SysConfig.isOnlineMap);
				
				mOutMapListAdapter.setSelectedMapName("");
				mOutMapListAdapter.notifyDataSetChanged();
				mOnMapListAdapter.setSelectedName(onmapIds.get(position));
				mOnMapListAdapter.notifyDataSetChanged();
				
				Intent intent = new Intent();
				intent.putExtra("mapId", TileSource.MAPNIK);
				intent.putExtra("rotate", isRotate);
				intent.putExtra("isOnline", true);
				setResult(RESULT_OK, intent);
				MapManagerActivity.this.finish();
			}
		}
	};
	
	public void onBackPressed() {
		Intent intent = new Intent();
		intent.putExtra("rotate", isRotate);
//		if (!SysConfig.isOnlineMap) {
//			if (selectedName != null && !"".equals(selectedName)) {
//				intent.putExtra("mapId", selectedName);
//			} else {
//				DialogUtils.alertInfo(MapManagerActivity.this, getResources().getString(R.string.reminder), "请选择地图");
//				return;
//			}
//		}
		setResult(RESULT_CANCELED, intent);
		super.onBackPressed();
	};
	
	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				MapManagerActivity.this.getResources().getDisplayMetrics());
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case RequestCode.OUTMAP:
			outmapFiles.clear();
			outmapFiles = FileUtils.getOutlineMapList(MapManagerActivity.this);
			mOutMapListAdapter = new OutMapListAdapter(MapManagerActivity.this, outmapFiles, "", outmap);
			lv_outline_map.setAdapter(mOutMapListAdapter);
			lv_outline_map.setOnItemClickListener(outmapItemClickListener);
			ListViewUtils.getListViewHeightBasedOnChildren(lv_outline_map);
			if (outmapFiles != null && outmapFiles.size() > 0) {
				lv_outline_map.setVisibility(View.VISIBLE);
				rl_outmap_null.setVisibility(View.GONE);
			} else {
				lv_outline_map.setVisibility(View.GONE);
				rl_outmap_null.setVisibility(View.VISIBLE);
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
