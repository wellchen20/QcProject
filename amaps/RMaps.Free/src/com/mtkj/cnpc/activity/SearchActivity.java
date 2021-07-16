package com.mtkj.cnpc.activity;

import java.util.ArrayList;
import java.util.List;

import org.andnav.osm.util.GeoPoint;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.cnpc.amap.search.util.AMapUtil;
import com.cnpc.amap.search.util.ToastUtil;
import com.mtkj.cnpc.activity.adapter.SearchHistoryListAdapter;
import com.mtkj.cnpc.activity.adapter.SearchHistoryListAdapter.ISearch;
import com.mtkj.cnpc.protocol.bean.TaskPoint;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.constants.SysConfig.WorkType;
import com.mtkj.cnpc.R;
import com.mtkj.utils.StatusBarUtil;
import com.robert.maps.applib.kml.PoiManager;
import com.robert.maps.applib.kml.PoiPoint;
import com.robert.maps.applib.overlays.SearchResultOverlay;
import com.robert.maps.applib.utils.TimeUtil;

public class SearchActivity extends BaseActivity implements OnClickListener {

	private int searchType = 0;
	public class SearchType {
		public final static int SHOT = 0x03;
	}

	private AutoCompleteTextView main_search_edit;
	private TextView tv_search_history,main_search;
	private ListView lv_search_history;
	private LinearLayout ll_tv_search_clear_history, ll_search_history;

	private PoiManager mPoiManager;
	private String keyWord;
	private ProgressDialog progDialog = null;// 搜索时进度条

	private List<String> mHistories = new ArrayList<String>();
	private SearchHistoryListAdapter mHistoryListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		StatusBarUtil.setTheme(this);
		mPoiManager = new PoiManager(SearchActivity.this);

		initViews();
		initDatas();
	}

	private void initViews() {
		main_search_edit = (AutoCompleteTextView) findViewById(R.id.main_search_edit);
		lv_search_history = (ListView) findViewById(R.id.lv_search_history);
		main_search = (TextView) findViewById(R.id.main_search);
		main_search.setOnClickListener(this);
		ll_tv_search_clear_history = (LinearLayout) findViewById(R.id.ll_tv_search_clear_history);
		ll_tv_search_clear_history.setOnClickListener(this);
		findViewById(R.id.ll_title_back).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SearchActivity.this.finish();
			}
		});;
		((TextView) findViewById(R.id.tv_title_title)).setText(getResources().getString(R.string.menu_search));
		tv_search_history = (TextView) findViewById(R.id.tv_search_history);
		ll_search_history = (LinearLayout) findViewById(R.id.ll_search_history);

		searchType = SearchType.SHOT;
	}

	private void initDatas() {
		mHistories = mPointDBDao.selectAllHistory();
		mHistoryListAdapter = new SearchHistoryListAdapter(SearchActivity.this, mHistories);
		lv_search_history.setAdapter(mHistoryListAdapter);
		lv_search_history.setOnItemClickListener(itemClickListener);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.main_search:
				keyWord = AMapUtil.checkEditText(main_search_edit);
				searchButton();
				hideSoftInput(main_search_edit);
				break;

			case R.id.ll_tv_search_clear_history:
				if (searchType == 0) {
					mPointDBDao.deleteHistory();
					mHistories = mPointDBDao.selectAllHistory();
					mHistoryListAdapter.setmHistorires(mHistories);

				} else {
					mPointDBDao.deleteHistoryByType(String.valueOf(searchType));
					mHistories = mPointDBDao.selectHistoryByType(String.valueOf(searchType));
					mHistoryListAdapter.setmHistorires(mHistories);
				}
				break;
		}
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
								long id) {
			keyWord = mHistories.get(position);
			searchButton();
			hideSoftInput(main_search_edit);
		}
	};


	/**
	 * 点击搜索按钮
	 */
	public void searchButton() {
		if ("".equals(keyWord)) {
			ToastUtil.show(SearchActivity.this, "请输入搜索关键字");
			return;
		} else {
			showProgressDialog();// 显示进度框
			switch (searchType) {
				case SearchType.SHOT:
					if (SysConfig.workType == WorkType.WORK_TYPE_NONE) {

					} else {
						TaskPoint taskPoint = null;
						if (SysConfig.workType == WorkType.WORK_TYPE_SHOT) {
							taskPoint = mPointDBDao.selectShotPointTotaskPoint(keyWord);
						} else if (SysConfig.workType == WorkType.WORK_TYPE_DRILE) {
							taskPoint = mPointDBDao.selectDrillPointToTaskPoint(keyWord);
						}else if (SysConfig.workType == WorkType.WORK_TYPE_ARRANGE){
							taskPoint = mPointDBDao.selectArrangePoint(keyWord);
						}
						if (taskPoint != null && !"".equals(taskPoint.stationNo)) {
							mPointDBDao.deleteHistoryByHistory(keyWord);
							mPointDBDao.insertHistory(String.valueOf(searchType), keyWord, TimeUtil.getCurrentTimeInString());

							dissmissProgressDialog();

							Intent intent = new Intent();
							intent.putExtra("type", searchType);
							intent.putExtra("isguide", false);
							intent.putExtra("shot", taskPoint.stationNo);
							setResult(RESULT_OK, intent);

							SearchActivity.this.finish();

						} else {
							showMessage("未查找到炮点");
						}
					}
					break;
			}
			dissmissProgressDialog();
		}
	}



	/**
	 * 显示进度框
	 */
	private void showProgressDialog() {
		if (progDialog == null)
			progDialog = new ProgressDialog(this);
		progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDialog.setIndeterminate(false);
		progDialog.setCancelable(false);
		progDialog.setMessage("正在搜索:\n" + keyWord);
		progDialog.show();
	}

	/**
	 * 隐藏进度框
	 */
	private void dissmissProgressDialog() {
		if (progDialog != null) {
			progDialog.dismiss();
		}
	}
}
