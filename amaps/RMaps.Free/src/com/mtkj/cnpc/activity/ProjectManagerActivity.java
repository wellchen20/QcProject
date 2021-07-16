package com.mtkj.cnpc.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openintents.filemanager.util.FileUtils;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mtkj.cnpc.activity.MainActivity.RequestCode;
import com.mtkj.cnpc.activity.adapter.InterestListAdapter;
import com.mtkj.cnpc.activity.adapter.InterestListAdapter.IInterest;
import com.mtkj.cnpc.activity.adapter.ProjectLayerListAdapter;
import com.mtkj.cnpc.activity.adapter.ProjectLayerListAdapter.IProjectLayer;
import com.mtkj.cnpc.activity.adapter.TracksListAdapter;
import com.mtkj.cnpc.activity.adapter.TracksListAdapter.ITrack;
import com.mtkj.cnpc.view.swipemenulistview.SwipeMenuListView;
import com.mtkj.cnpc.R;
import com.robert.maps.applib.kml.ImportPoiActivity;
import com.robert.maps.applib.kml.ImportTrackActivity;
import com.robert.maps.applib.kml.PoiActivity;
import com.robert.maps.applib.kml.PoiManager;
import com.robert.maps.applib.kml.PoiPoint;
import com.robert.maps.applib.kml.Track;
import com.robert.maps.applib.kml.TrackActivity;
import com.robert.maps.applib.utils.DialogUtils;
import com.robert.maps.applib.utils.ListViewUtils;

public class ProjectManagerActivity extends BaseActivity {
	
	private CheckBox ck_project_layer, ck_project_interest, ck_project_track;
	private SwipeMenuListView lv_project, lv_insterests, lv_tracks;
	private LinearLayout ll_project_layer, ll_project_interest, ll_project_track, ll_import_interest_file, ll_import_track_file;
	private TextView tv_project_layer, tv_project_interest, tv_project_track;
	private RelativeLayout rl_project_layer_null, rl_project_interest_null, rl_project_track_null,
							rl_project_layer, rl_project_interest, rl_project_track;
	
	private PoiManager mPoiManager;
	
	// 工区图层管理
	private List<File> projectFiles = new ArrayList<File>();
	private ProjectLayerListAdapter projectLayerListAdapter;
	
	// 兴趣点
	private List<PoiPoint> mPoiPoints = new ArrayList<PoiPoint>();
	private InterestListAdapter mInterestListAdapter;
	
	// 轨迹
	private List<Track> mTracks = new ArrayList<Track>();
	private TracksListAdapter mTracksListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_project_manager);
		
		mPoiManager = new PoiManager(ProjectManagerActivity.this);
		
		initViews();
		initDatas();
		getDatas();
	}
	
	@Override
	protected void onDestroy() {
 		super.onDestroy();
	}
	
	private ProgressDialog dlgWait;
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

	private void initViews() {
		lv_project = (SwipeMenuListView) findViewById(R.id.lv_project);
		lv_insterests = (SwipeMenuListView) findViewById(R.id.lv_insterests);
		lv_tracks = (SwipeMenuListView) findViewById(R.id.lv_tracks);
		
		ck_project_layer = (CheckBox) findViewById(R.id.ck_project_layer);
		ck_project_interest = (CheckBox) findViewById(R.id.ck_project_interest);
		ck_project_track = (CheckBox) findViewById(R.id.ck_project_track);
		
		ll_project_layer = (LinearLayout) findViewById(R.id.ll_project_layer);
		ll_project_interest = (LinearLayout) findViewById(R.id.ll_project_interest);
		ll_project_track = (LinearLayout) findViewById(R.id.ll_project_track);
		ll_import_interest_file = (LinearLayout) findViewById(R.id.ll_import_interest_file);
		ll_import_track_file = (LinearLayout) findViewById(R.id.ll_import_track_file);
		
		tv_project_layer = (TextView) findViewById(R.id.tv_project_layer);
		tv_project_interest = (TextView) findViewById(R.id.tv_project_interest);
		tv_project_track = (TextView) findViewById(R.id.tv_project_track);
		
		rl_project_layer = (RelativeLayout) findViewById(R.id.rl_project_layer);
		rl_project_layer_null = (RelativeLayout) findViewById(R.id.rl_project_layer_null);
		rl_project_interest = (RelativeLayout) findViewById(R.id.rl_project_interest);
		rl_project_interest_null = (RelativeLayout) findViewById(R.id.rl_project_interest_null);
		rl_project_track = (RelativeLayout) findViewById(R.id.rl_project_track);
		rl_project_track_null = (RelativeLayout) findViewById(R.id.rl_project_track_null);
		
		findViewById(R.id.ll_title_back).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				setResult(RESULT_OK, intent);
				ProjectManagerActivity.this.finish();
			}
		});;
		((TextView) findViewById(R.id.tv_title_title)).setText(getResources().getString(R.string.activity_project_manager));
	}

	private void initDatas() {
		ck_project_layer.setChecked(true);
		tv_project_layer.setTextColor(0xff00B9EC);
		ll_project_layer.setVisibility(View.VISIBLE);
		
		ck_project_interest.setChecked(false);
		tv_project_interest.setTextColor(0xff000000);
		ll_project_interest.setVisibility(View.GONE);
		
		ck_project_track.setChecked(false);
		tv_project_track.setTextColor(0xff000000);
		ll_project_track.setVisibility(View.GONE);
		
		ck_project_layer.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					ck_project_layer.setChecked(true);
					tv_project_layer.setTextColor(0xff00B9EC);
					ll_project_layer.setVisibility(View.VISIBLE);
					
					projectLayerListAdapter.notifyDataSetChanged();
					ListViewUtils.getListViewHeightBasedOnChildren(lv_project);
				} else {
					ck_project_layer.setChecked(false);
					tv_project_layer.setTextColor(0xff000000);
					ll_project_layer.setVisibility(View.GONE);
				}
			}
		});
		tv_project_layer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean isChecked = ck_project_layer.isChecked();
				if (!isChecked) {
					ck_project_layer.setChecked(true);
					tv_project_layer.setTextColor(0xff00B9EC);
					ll_project_layer.setVisibility(View.VISIBLE);
					
					projectLayerListAdapter.notifyDataSetChanged();
					ListViewUtils.getListViewHeightBasedOnChildren(lv_project);
				} else {
					ck_project_layer.setChecked(false);
					tv_project_layer.setTextColor(0xff000000);
					ll_project_layer.setVisibility(View.GONE);
				}
			}
		});
		ck_project_interest.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					ck_project_interest.setChecked(true);
					tv_project_interest.setTextColor(0xff00B9EC);
					ll_project_interest.setVisibility(View.VISIBLE);
					
					mInterestListAdapter.notifyDataSetChanged();
					ListViewUtils.getListViewHeightBasedOnChildren(lv_insterests);
				} else {
					ck_project_interest.setChecked(false);
					tv_project_interest.setTextColor(0xff000000);
					ll_project_interest.setVisibility(View.GONE);
				}
			}
		});
		tv_project_interest.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean isChecked = ck_project_interest.isChecked();
				if (!isChecked) {
					ck_project_interest.setChecked(true);
					tv_project_interest.setTextColor(0xff00B9EC);
					ll_project_interest.setVisibility(View.VISIBLE);
					
					mInterestListAdapter.notifyDataSetChanged();
					ListViewUtils.getListViewHeightBasedOnChildren(lv_insterests);
				} else {
					ck_project_interest.setChecked(false);
					tv_project_interest.setTextColor(0xff000000);
					ll_project_interest.setVisibility(View.GONE);
				}
			}
		});
		ll_import_interest_file.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivityForResult((new Intent(ProjectManagerActivity.this, ImportPoiActivity.class)), RequestCode.IMPORT_POI);
			}
		});
		ck_project_track.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					ck_project_track.setChecked(true);
					tv_project_track.setTextColor(0xff00B9EC);
					ll_project_track.setVisibility(View.VISIBLE);
					
					mTracksListAdapter.notifyDataSetChanged();
					ListViewUtils.getListViewHeightBasedOnChildren(lv_tracks);
				} else {
					ck_project_track.setChecked(false);
					tv_project_track.setTextColor(0xff000000);
					ll_project_track.setVisibility(View.GONE);
				}
			}
		});
		tv_project_track.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean isChecked = ck_project_track.isChecked();
				if (!isChecked) {
					ck_project_track.setChecked(true);
					tv_project_track.setTextColor(0xff00B9EC);
					ll_project_track.setVisibility(View.VISIBLE);
					
					mTracksListAdapter.notifyDataSetChanged();
					ListViewUtils.getListViewHeightBasedOnChildren(lv_tracks);
				} else {
					ck_project_track.setChecked(false);
					tv_project_track.setTextColor(0xff000000);
					ll_project_track.setVisibility(View.GONE);
				}
			}
		});
		ll_import_track_file.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivityForResult((new Intent(ProjectManagerActivity.this, ImportTrackActivity.class)), RequestCode.IMPORT_TRACK);
			}
		});
	}
	
	private void getDatas() {
		// 工区图层
		projectFiles = FileUtils.getProjectList(ProjectManagerActivity.this);
		projectLayerListAdapter= new ProjectLayerListAdapter(ProjectManagerActivity.this, projectFiles, projectLayer);
		lv_project.setAdapter(projectLayerListAdapter);
//		SwipeMenuCreator projectCreator = new SwipeMenuCreator() {
//			
//			@Override
//			public void create(SwipeMenu menu) {
//				SwipeMenuItem deleteItem = new SwipeMenuItem(ProjectManagerActivity.this);
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
//		lv_project.setMenuCreator(projectCreator);
//		lv_project.setOnMenuItemClickListener(projectMenuItemClickListener);
		lv_project.setOnItemLongClickListener(layerOnItemLongClickListener);
		ListViewUtils.getListViewHeightBasedOnChildren(lv_project);
		if (projectFiles != null && projectFiles.size() > 0) {
			lv_project.setVisibility(View.VISIBLE);
			rl_project_layer_null.setVisibility(View.GONE);
		} else {
			lv_project.setVisibility(View.GONE);
			rl_project_layer_null.setVisibility(View.VISIBLE);
		}
		// 兴趣点
		mPoiPoints = mPoiManager.getAllPoiPoint();
		mInterestListAdapter = new InterestListAdapter(ProjectManagerActivity.this, mPoiPoints, interest);
		lv_insterests.setAdapter(mInterestListAdapter);
//		SwipeMenuCreator interestCreator = new SwipeMenuCreator() {
//			
//			@Override
//			public void create(SwipeMenu menu) {
//				SwipeMenuItem editItem = new SwipeMenuItem(ProjectManagerActivity.this);
//				// set item background
//				editItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
//				// set item width
//				editItem.setWidth(dp2px(70));
//				
//				editItem.setTitle(R.string.menu_edit);
//				editItem.setTitleSize(20);
//				editItem.setTitleColor(0xffffffff);
//				// add to menu
//				menu.addMenuItem(editItem);
//				
//				SwipeMenuItem deleteItem = new SwipeMenuItem(ProjectManagerActivity.this);
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
//		lv_insterests.setMenuCreator(interestCreator);
//		lv_insterests.setOnMenuItemClickListener(interestMenuItemClickListener);
		lv_insterests.setOnItemClickListener(interestItemClickListener);
//		lv_insterests.setOnItemLongClickListener(interestOnItemLongClickListener);
		ListViewUtils.getListViewHeightBasedOnChildren(lv_insterests);
		if (mPoiPoints != null && mPoiPoints.size() > 0) {
			lv_insterests.setVisibility(View.VISIBLE);
			rl_project_interest_null.setVisibility(View.GONE);
		} else {
			lv_insterests.setVisibility(View.GONE);
			rl_project_interest_null.setVisibility(View.VISIBLE);
		}
		// 轨迹
		mTracks = mPoiManager.getAllTrack();
		Log.e("mTracks", mTracks.size()+"");
		mTracksListAdapter = new TracksListAdapter(ProjectManagerActivity.this, mTracks, iTrack);
		lv_tracks.setAdapter(mTracksListAdapter);
//		SwipeMenuCreator trackCreator = new SwipeMenuCreator() {
//			
//			@Override
//			public void create(SwipeMenu menu) {
//				SwipeMenuItem editItem = new SwipeMenuItem(ProjectManagerActivity.this);
//				// set item background
//				editItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,0xCE)));
//				// set item width
//				editItem.setWidth(dp2px(70));
//				
//				editItem.setTitle(R.string.menu_edit);
//				editItem.setTitleSize(20);
//				editItem.setTitleColor(0xffffffff);
//				// add to menu
//				menu.addMenuItem(editItem);
//				
//				SwipeMenuItem deleteItem = new SwipeMenuItem(ProjectManagerActivity.this);
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
//		lv_tracks.setMenuCreator(trackCreator);
//		lv_tracks.setOnMenuItemClickListener(trackMenuItemClickListener);
		lv_tracks.setOnItemClickListener(trackItemClickListener);
//		lv_tracks.setOnItemLongClickListener(trackOnItemLongClickListener);
		ListViewUtils.getListViewHeightBasedOnChildren(lv_tracks);
		if (mTracks != null && mTracks.size() > 0) {
			lv_tracks.setVisibility(View.VISIBLE);
			rl_project_track_null.setVisibility(View.GONE);
		} else {
			lv_tracks.setVisibility(View.GONE);
			rl_project_track_null.setVisibility(View.VISIBLE);
		}
	}

	private IProjectLayer projectLayer = new IProjectLayer() {
		
		@Override
		public void selectedProjectLayer(int position) {
			
		}
		
		@Override
		public void deleteProjectLayer(int position) {
			File file = projectFiles.get(position);
			file.delete();
			
			projectFiles = FileUtils.getProjectList(ProjectManagerActivity.this);
			projectLayerListAdapter.setmFiles(projectFiles);
			ListViewUtils.getListViewHeightBasedOnChildren(lv_project);
			if (projectFiles != null && projectFiles.size() > 0) {
				lv_project.setVisibility(View.VISIBLE);
				rl_project_layer_null.setVisibility(View.GONE);
			} else {
				lv_project.setVisibility(View.GONE);
				rl_project_layer_null.setVisibility(View.VISIBLE);
			}
		}
		
		@Override
		public void cancelProjectLayer(int position) {
			
		}
	};
	
	private OnItemClickListener layerItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private Dialog dialog;
	private OnItemLongClickListener layerOnItemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				final int position, long id) {
			dialog = DialogUtils.Alert(ProjectManagerActivity.this, getResources().getString(R.string.reminder), 
					"是否删除该地图工区图层文件？",
					new String[]{getResources().getString(R.string.ok), getResources().getString(R.string.cancel)},
					new OnClickListener[]{new OnClickListener() {
					
						@Override
						public void onClick(View v) {
							if (dialog != null && dialog.isShowing()) {
								dialog.dismiss();
							}
							
							File file = projectFiles.get(position);
							file.delete();
							projectFiles = FileUtils.getProjectList(ProjectManagerActivity.this);
							projectLayerListAdapter.setmFiles(projectFiles);
							ListViewUtils.getListViewHeightBasedOnChildren(lv_project);
							if (projectFiles != null && projectFiles.size() > 0) {
								lv_project.setVisibility(View.VISIBLE);
								rl_project_layer_null.setVisibility(View.GONE);
							} else {
								lv_project.setVisibility(View.GONE);
								rl_project_layer_null.setVisibility(View.VISIBLE);
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
	
//	private OnMenuItemClickListener projectMenuItemClickListener = new OnMenuItemClickListener() {
//		
//		@Override
//		public void onMenuItemClick(int position, SwipeMenu menu, int index) {
//			switch (index) {
//			case 0:
//				File file = projectFiles.get(position);
//				file.delete();
//				
//				projectFiles = FileUtils.getProjectList(ProjectManagerActivity.this);
//				projectLayerListAdapter.setmFiles(projectFiles);
//				projectLayerListAdapter.notifyDataSetChanged();
//				ListViewUtils.getListViewHeightBasedOnChildren(lv_project);
//				break;
//
//			default:
//				break;
//			}
//		}
//	};
	
	private IInterest interest = new IInterest() {
		
		@Override
		public void isShowInterest(int position, boolean isShow) {
			PoiPoint poiPoint = mPoiPoints.get(position);
			poiPoint.Hidden = !isShow;
			mPoiManager.updatePoi(poiPoint);
			mInterestListAdapter.notifyDataSetChanged();
		}
		
		@Override
		public void editInterest(int position) {
			PoiPoint poiPoint = mPoiPoints.get(position);
			final Intent PoiIntent = new Intent(ProjectManagerActivity.this, PoiActivity.class); 
	        PoiIntent.putExtra("pointid", poiPoint.getId());
			startActivityForResult(PoiIntent, RequestCode.COLLECT_POI);
		}
	};
	
	private OnItemClickListener interestItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			final PoiPoint poiPoint = mPoiPoints.get(position);
			startActivityForResult((new Intent(ProjectManagerActivity.this, PoiActivity.class)).putExtra("pointid", poiPoint.getId()), RequestCode.COLLECT_POI);
		}
	};
	
	private OnItemLongClickListener interestOnItemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				final int position, long id) {
//			dialog = DialogUtils.Alert(ProjectManagerActivity.this, getResources().getString(R.string.reminder), 
//					"是否删除该兴趣点？",
//					new String[]{getResources().getString(R.string.ok), getResources().getString(R.string.cancel)},
//					new OnClickListener[]{new OnClickListener() {
//					
//						@Override
//						public void onClick(View v) {
//							if (dialog != null && dialog.isShowing()) {
//								dialog.dismiss();
//							}
//							mPoiManager.deletePoi(mPoiPoints.get(position).getId());
//							mPoiPoints = mPoiManager.getAllPoiPoint();
//							mInterestListAdapter.setmPoints(mPoiPoints);
//							ListViewUtils.getListViewHeightBasedOnChildren(lv_insterests);
//							if (mPoiPoints != null && mPoiPoints.size() > 0) {
//								lv_insterests.setVisibility(View.VISIBLE);
//								rl_project_interest_null.setVisibility(View.GONE);
//							} else {
//								lv_insterests.setVisibility(View.GONE);
//								rl_project_interest_null.setVisibility(View.VISIBLE);
//							}
//						}
//					}, new OnClickListener() {
//					
//						@Override
//						public void onClick(View v) {
//							dialog.dismiss();
//						}
//					}
//			});
			return false;
		}
	};
	
//	private OnMenuItemClickListener interestMenuItemClickListener = new OnMenuItemClickListener() {
//		
//		@Override
//		public void onMenuItemClick(int position, SwipeMenu menu, int index) {
//			final PoiPoint poiPoint = mPoiPoints.get(position);
//			switch (index) {
//			case 0:
//				startActivity((new Intent(ProjectManagerActivity.this, PoiActivity.class)).putExtra("pointid", poiPoint.getId()));
//				break;
//
//			case 1:
//				new AlertDialog.Builder(ProjectManagerActivity.this) 
//				.setTitle(R.string.app_name)
//				.setMessage(getResources().getString(R.string.question_delete, getText(R.string.poi)) )
//				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int whichButton) {
//						mPoiManager.deletePoi(poiPoint.getId());
//						
//						mPoiPoints = mPoiManager.getAllPoiPoint();
//						mInterestListAdapter.setmPoints(mPoiPoints);
//						mInterestListAdapter.notifyDataSetChanged();
//						ListViewUtils.getListViewHeightBasedOnChildren(lv_insterests);
//					}
//				}).setNegativeButton(R.string.no, null).create().show();
//				break;
//			default:
//				break;
//			}
//		}
//	};
	
	private ITrack iTrack = new ITrack() {
		
		@Override
		public void isShowTrack(int position, boolean isShow) {
			Track track = mTracks.get(position);
			track.Show = isShow;
			mPoiManager.updateTrack(track);
			mTracksListAdapter.notifyDataSetChanged();
		}
		
		@Override
		public void editTrack(int position) {
			Track track = mTracks.get(position);
			startActivityForResult((new Intent(ProjectManagerActivity.this, TrackActivity.class)).putExtra("id", track.getId()), RequestCode.EDIT_TRACK);
		}
	};
	
	private OnItemClickListener trackItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			final Track track = mTracks.get(position);
			startActivityForResult((new Intent(ProjectManagerActivity.this, TrackActivity.class)).putExtra("id", track.getId()), RequestCode.EDIT_TRACK);
		}
	};
	
	private OnItemLongClickListener trackOnItemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				final int position, long id) {
//			dialog = DialogUtils.Alert(ProjectManagerActivity.this, getResources().getString(R.string.reminder), 
//					"是否删除该轨迹？",
//					new String[]{getResources().getString(R.string.ok), getResources().getString(R.string.cancel)},
//					new OnClickListener[]{new OnClickListener() {
//					
//						@Override
//						public void onClick(View v) {
//							if (dialog != null && dialog.isShowing()) {
//								dialog.dismiss();
//							}
//							mPoiManager.deleteTrack(mTracks.get(position).getId());
//							
//							mTracks = mPoiManager.getAllTrack();
//							mTracksListAdapter.setmTracks(mTracks);
//							ListViewUtils.getListViewHeightBasedOnChildren(lv_tracks);
//							if (mTracks != null && mTracks.size() > 0) {
//								lv_tracks.setVisibility(View.VISIBLE);
//								rl_project_track_null.setVisibility(View.GONE);
//							} else {
//								lv_tracks.setVisibility(View.GONE);
//								rl_project_track_null.setVisibility(View.VISIBLE);
//							}
//						}
//					}, new OnClickListener() {
//					
//						@Override
//						public void onClick(View v) {
//							dialog.dismiss();
//						}
//					}
//			});
			return false;
		}
	};
	
//	private OnMenuItemClickListener trackMenuItemClickListener = new OnMenuItemClickListener() {
//		
//		@Override
//		public void onMenuItemClick(int position, SwipeMenu menu, int index) {
//			final Track track = mTracks.get(position);
//			switch (index) {
//			case 0:
//				startActivity((new Intent(ProjectManagerActivity.this, TrackActivity.class)).putExtra("id", track.getId()));
//				break;
//
//			case 1:
//				new AlertDialog.Builder(ProjectManagerActivity.this) 
//				.setTitle(R.string.app_name)
//				.setMessage(getResources().getString(R.string.question_delete, getText(R.string.track)) )
//				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int whichButton) {
//						// 删除轨迹
//						mPoiManager.deleteTrack(track.getId());
//						
//						mTracks = mPoiManager.getAllTrack();
//						mTracksListAdapter.setmTracks(mTracks);
//						mTracksListAdapter.notifyDataSetChanged();
//						ListViewUtils.getListViewHeightBasedOnChildren(lv_tracks);
//					}
//				}).setNegativeButton(R.string.no, null).create().show();
//				break;
//			default:
//				break;
//			}
//		}
//	};
	
	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				ProjectManagerActivity.this.getResources().getDisplayMetrics());
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case RequestCode.COLLECT_POI:
		case RequestCode.IMPORT_POI:
			mPoiPoints.clear();
			mPoiPoints = mPoiManager.getAllPoiPoint();
			mInterestListAdapter = new InterestListAdapter(ProjectManagerActivity.this, mPoiPoints, interest);
			lv_insterests.setAdapter(mInterestListAdapter);
			lv_insterests.setOnItemClickListener(interestItemClickListener);
			ListViewUtils.getListViewHeightBasedOnChildren(lv_insterests);
			if (mPoiPoints != null && mPoiPoints.size() > 0) {
				lv_insterests.setVisibility(View.VISIBLE);
				rl_project_interest_null.setVisibility(View.GONE);
			} else {
				lv_insterests.setVisibility(View.GONE);
				rl_project_interest_null.setVisibility(View.VISIBLE);
			}
			break;

		case RequestCode.EDIT_TRACK:
		case RequestCode.IMPORT_TRACK:
			mTracks.clear();
			mTracks = mPoiManager.getAllTrack();
			mTracksListAdapter = new TracksListAdapter(ProjectManagerActivity.this, mTracks, iTrack);
			lv_tracks.setAdapter(mTracksListAdapter);
			lv_tracks.setOnItemClickListener(trackItemClickListener);
			ListViewUtils.getListViewHeightBasedOnChildren(lv_tracks);
			if (mTracks != null && mTracks.size() > 0) {
				lv_tracks.setVisibility(View.VISIBLE);
				rl_project_track_null.setVisibility(View.GONE);
			} else {
				lv_tracks.setVisibility(View.GONE);
				rl_project_track_null.setVisibility(View.VISIBLE);
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		setResult(RESULT_OK, intent);
		ProjectManagerActivity.this.finish();
	}
}
