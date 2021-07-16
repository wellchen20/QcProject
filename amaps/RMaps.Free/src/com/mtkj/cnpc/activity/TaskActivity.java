package com.mtkj.cnpc.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mtkj.cnpc.activity.adapter.TaskDailyListAdapter;
import com.mtkj.cnpc.protocol.bean.DailyTask;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.constants.SysConfig.WorkType;
import com.mtkj.cnpc.protocol.constants.SysContants;
import com.mtkj.cnpc.protocol.socket.DataProcess;
import com.mtkj.cnpc.view.numberprogressbar.NumberProgressBar;
import com.mtkj.cnpc.view.numberprogressbar.OnProgressBarListener;
import com.mtkj.cnpc.view.swipemenulistview.SwipeMenu;
import com.mtkj.cnpc.view.swipemenulistview.SwipeMenuCreator;
import com.mtkj.cnpc.view.swipemenulistview.SwipeMenuItem;
import com.mtkj.cnpc.view.swipemenulistview.SwipeMenuListView;
import com.mtkj.cnpc.view.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.mtkj.cnpc.R;
import com.robert.maps.applib.utils.ListViewUtils;

public class TaskActivity extends BaseActivity implements OnClickListener {

	private LinearLayout ll_back, ll_task_info;
	private RelativeLayout rl_task_pregress;
	private CheckBox ck_task_update;
	private TextView tv_task_updata, tv_task_total;
	private SwipeMenuListView lv_task_daliy;
	private NumberProgressBar npb_task_update;
	
	private String time, total;
	
	private List<DailyTask> mDailyTasks = new ArrayList<DailyTask>();
	private TaskDailyListAdapter mDailyListAdapter;
	
	private Timer timer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task);
		
		initViews();
		initDatas();
	}

	private void initViews() {
		ll_back = (LinearLayout) findViewById(R.id.ll_back);
		ll_back.setVisibility(View.GONE);
//		ll_back.setOnClickListener(this);
		ll_task_info = (LinearLayout) findViewById(R.id.ll_task_info);
		rl_task_pregress = (RelativeLayout) findViewById(R.id.rl_task_progress);
		ck_task_update = (CheckBox) findViewById(R.id.ck_task_update);
		tv_task_updata = (TextView) findViewById(R.id.tv_task_updata);
		tv_task_total = (TextView) findViewById(R.id.tv_task_total);
		lv_task_daliy = (SwipeMenuListView) findViewById(R.id.lv_task_daliy);
		npb_task_update = (NumberProgressBar) findViewById(R.id.npb_task_update);
		npb_task_update.setOnProgressBarListener(new OnProgressBarListener() {
			
			@Override
			public void onProgressChange(long current, int max) {
				if (current == max) {
					ck_task_update.setChecked(false);
					ll_task_info.setVisibility(View.VISIBLE);
					rl_task_pregress.setVisibility(View.GONE);
					npb_task_update.setProgress(0);
				}
			}
		});
		findViewById(R.id.ImportBtn).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(TaskActivity.this, ImportTaskActivity.class);
				startActivity(intent);
			}
		});
	}
	
	private void initDatas() {
		time = mPointDBDao.selectPackagetNumTime(String.valueOf(SysConfig.workType));
		if (time != null) {
			tv_task_updata.setText(time);
		}
		if (SysConfig.workType == WorkType.WORK_TYPE_DRILE) {
			total = String.valueOf(mPointDBDao.selectDrilltaotal());
		} else if (SysConfig.workType == WorkType.WORK_TYPE_SHOT) {
			total = String.valueOf(mPointDBDao.selectShottotal());
		}else if (SysConfig.workType == WorkType.WORK_TYPE_ARRANGE){
			total = String.valueOf(mPointDBDao.selectArraytotal());
		}
		if (total != null) {
			tv_task_total.setText(total);
		}
		
		ll_task_info.setVisibility(View.VISIBLE);
		rl_task_pregress.setVisibility(View.GONE);
		ck_task_update.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					ll_task_info.setVisibility(View.GONE);
					rl_task_pregress.setVisibility(View.VISIBLE);
					npb_task_update.setProgress(0);
					timer = new Timer();
					timer.schedule(new TimerTask() {
						
						@Override
						public void run() {
							runOnUiThread(new Runnable() {
								public void run() {
									npb_task_update.incrementProgressBy(1);
								}
							});
						}
					}, 5 * 100, 100);
				} else {
					ll_task_info.setVisibility(View.VISIBLE);
					rl_task_pregress.setVisibility(View.GONE);
				}
			}
		});
		
		
		mDailyTasks = mPointDBDao.selectDailyTask(String.valueOf(SysConfig.workType));
		mDailyListAdapter = new TaskDailyListAdapter(TaskActivity.this, mDailyTasks);
		lv_task_daliy.setAdapter(mDailyListAdapter);
		SwipeMenuCreator dailyCreator = new SwipeMenuCreator() {
			
			@Override
			public void create(SwipeMenu menu) {
				SwipeMenuItem deleteItem = new SwipeMenuItem(TaskActivity.this);
				// set item background
				deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
				// set item width
				deleteItem.setWidth(dp2px(70));
				
				deleteItem.setTitle(R.string.menu_delete);
				deleteItem.setTitleSize(20);
				deleteItem.setTitleColor(0xffffffff);
				// add to menu
				menu.addMenuItem(deleteItem);
			}
		};
		lv_task_daliy.setMenuCreator(dailyCreator);
		lv_task_daliy.setOnMenuItemClickListener(dailyMenuItemClickListener);
		ListViewUtils.getListViewHeightBasedOnChildren(lv_task_daliy);
	}
		
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_back:
			TaskActivity.this.finish();
			break;
			
		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		keyBackClickCount = 0;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		DataProcess.GetInstance().stopConn();
	}
	
	private int keyBackClickCount = 0;
	
	@Override
	public void onBackPressed() {
		switch (keyBackClickCount++) {
		case 0:
			Toast.makeText(this,
					getResources().getString(R.string.press_again_exit),
					Toast.LENGTH_SHORT).show();
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					keyBackClickCount = 0;
				}
			}, 2000);
			break;
		case 1:
			setData(SysContants.WORK_TYPE, SysConfig.workType);
			setData(SysContants.ISFIRST, true);
			super.onBackPressed();
			break;
		default:
			break;
		}
	}
	
	private OnMenuItemClickListener dailyMenuItemClickListener = new OnMenuItemClickListener() {
		
		@Override
		public void onMenuItemClick(int position, SwipeMenu menu, int index) {
			switch (index) {
			case 1:
				DailyTask dailyTask = mDailyTasks.get(position);
				mPointDBDao.deleteDailyTaskByDaily(dailyTask.time, String.valueOf(SysConfig.workType));
				mPointDBDao.deleteDailyPointByTime(dailyTask.time, String.valueOf(SysConfig.workType));
				
				mDailyTasks = mPointDBDao.selectDailyTask(String.valueOf(SysConfig.workType));
				mDailyListAdapter.setmDailyTasks(mDailyTasks);
				mDailyListAdapter.notifyDataSetChanged();
				
				break;

			default:
				break;
			}
		}
	};
	
	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				TaskActivity.this.getResources().getDisplayMetrics());
	}
}
