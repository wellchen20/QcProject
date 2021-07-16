package com.mtkj.cnpc.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mtkj.cnpc.protocol.bean.Jing;
import com.mtkj.cnpc.R;

public class AddActivity extends Activity implements OnClickListener {

	private LayoutInflater mInflater;
	
	/**
	 * 控件
	 */
	private TextView tv_title;
	private EditText et_add_yaoliang, et_add_leiguan;
	private EditText et_add_koushu, et_add_zuanjing, et_add_xiayao;
	private ImageView iv_leiguan;
	private Button btn_add_jing, btn_add_back, btn_lock_location;
	private LinearLayout back_linear, ll_add_location;
	private LinearLayout ll_add_leiguan, ll_add_zhayao;
	
	/**
	 * 录入井口数据
	 */
	private String add_koushu, add_zuanjing, add_xiayao, add_yaoliang, add_leiguan;
	private List<String> zhayao = new ArrayList<String>();
	private List<String> leiguan = new ArrayList<String>();

	/**
	 * 保存数据及其他判断
	 */
	public static List<Jing> jings = new ArrayList<Jing>();
	private boolean isUpdate = false;
	private int index = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_jing);

		mInflater = LayoutInflater.from(AddActivity.this);
		isUpdate = false;

		initView();
		initData();
		bindEvent();
	}

	private void initView() {
		tv_title = (TextView) findViewById(R.id.tv_title_title);
		back_linear = (LinearLayout) findViewById(R.id.ll_title_back);
//		tv_help = (TextView) findViewById(R.id.txt_help);

		et_add_koushu = (EditText) findViewById(R.id.et_add_koushu);
		et_add_zuanjing = (EditText) findViewById(R.id.et_add_zuanjing);
		et_add_xiayao = (EditText) findViewById(R.id.et_add_xiayao);
		et_add_yaoliang = (EditText) findViewById(R.id.et_add_yaoliang);
		ll_add_zhayao = (LinearLayout) findViewById(R.id.ll_add_zhayao);
		et_add_leiguan = (EditText) findViewById(R.id.et_add_leiguan);
		ll_add_leiguan = (LinearLayout) findViewById(R.id.ll_add_leiguan);
		iv_leiguan = (ImageView) findViewById(R.id.iv_leiguan);
		btn_lock_location = (Button) findViewById(R.id.btn_lock_location);
		ll_add_location = (LinearLayout) findViewById(R.id.ll_add_location);

		btn_add_jing = (Button) findViewById(R.id.btn_add_jing);
		btn_add_back = (Button) findViewById(R.id.btn_add_back);

	}
	
	private void addZhayaoView(int position) {
		ll_add_zhayao.removeAllViews();
		for (int i = 0; i < zhayao.size(); i++) {
			final int num = i;
			final LinearLayout linearLayout = new LinearLayout(AddActivity.this);
			linearLayout.setOrientation(LinearLayout.HORIZONTAL);
			linearLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

			View view = mInflater.inflate(R.layout.add_bianhao, null);
			view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

			View view2 = view.findViewById(R.id.v_bianhao);
			if (i == 0) {
				view2.setVisibility(View.GONE);
			} else {
				view2.setVisibility(View.VISIBLE);
			}
			
			final EditText editText = (EditText) view.findViewById(R.id.et_add_bianhao);
			editText.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				}

				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				}

				@Override
				public void afterTextChanged(Editable arg0) {
					String str = editText.getText().toString();
					if (str != null && !"".equals(str)) {
						zhayao.set(num, str);
					}
				}
			});
			editText.setText(zhayao.get(i));
			if (i == zhayao.size() - 1) {
				editText.setFocusable(true);
				editText.setFocusableInTouchMode(true);
				editText.requestFocus();
			}

			ImageView imageView = (ImageView) view.findViewById(R.id.iv_add_bianhao);
			imageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					String str = editText.getText().toString();
					if (str != null && !"".equals(str)) {
						zhayao.add(str);
						zhayao.set(num, str);
						addZhayaoView(num);
						et_add_yaoliang.setText("" + zhayao.size());
					} else {
						Toast.makeText(AddActivity.this, "请输入炸药编号", Toast.LENGTH_SHORT).show();
					}
				}
			});
			ImageView button = (ImageView) view.findViewById(R.id.iv_delete_bianhao);
			button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (zhayao.size() > 1) {
						zhayao.remove(num);
						addZhayaoView(-1);
						et_add_yaoliang.setText("" + zhayao.size());
					}

				}
			});
			linearLayout.addView(view);
			ll_add_zhayao.addView(linearLayout);
		}
	}

	private void addLeiguanView(int position) {
		ll_add_leiguan.removeAllViews();
		for (int i = 0; i < leiguan.size(); i++) {
			final int num = i;
			final LinearLayout linearLayout = new LinearLayout(AddActivity.this);
			linearLayout.setOrientation(LinearLayout.HORIZONTAL);
			linearLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

			View view = mInflater.inflate(R.layout.add_bianhao, null);
			view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

			View view2 = view.findViewById(R.id.v_bianhao);
			if (i == 0) {
				view2.setVisibility(View.GONE);
			} else {
				view2.setVisibility(View.VISIBLE);
			}
			
			final EditText editText = (EditText) view.findViewById(R.id.et_add_bianhao);
			editText.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				}

				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				}

				@Override
				public void afterTextChanged(Editable arg0) {
					String str = editText.getText().toString();
					if (str != null && !"".equals(str)) {
						leiguan.set(num, str);
					}
				}
			});
			editText.setText(leiguan.get(i));
			if (i == leiguan.size() - 1) {
				editText.setFocusable(true);
				editText.setFocusableInTouchMode(true);
				editText.requestFocus();
			}

			ImageView imageView = (ImageView) view.findViewById(R.id.iv_add_bianhao);
			imageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					String str = editText.getText().toString();
					if (str != null && !"".equals(str)) {
						leiguan.add(str);
						leiguan.set(num, str);
						addLeiguanView(num);
						et_add_leiguan.setText("" + leiguan.size());
					} else {
						Toast.makeText(AddActivity.this, "请输入雷管编号", Toast.LENGTH_SHORT).show();
					}
				}
			});
			ImageView button = (ImageView) view.findViewById(R.id.iv_delete_bianhao);
			button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (leiguan.size() > 1) {
						leiguan.remove(num);
						addLeiguanView(-1);
						et_add_leiguan.setText("" + leiguan.size());
					}

				}
			});
			linearLayout.addView(view);

			ll_add_leiguan.addView(linearLayout);
		}
	}

	private void initData() {
		tv_title.setText("钻井信息");
//		tv_help.setVisibility(View.GONE);
//		tv_help.setText("帮助");

		Intent intent = getIntent();
		index = intent.getIntExtra("value", -1);
		if (index != -1 && jings != null && jings.size() > index) {
			isUpdate = true;
			Jing jing = jings.get(index);

			et_add_koushu.setText(jing.getJinghao());
			et_add_zuanjing.setText(jing.getZuanjing());
			et_add_xiayao.setText(jing.getXiayao());
			et_add_yaoliang.setText(jing.getYaoliang());
			et_add_leiguan.setText(jing.getLeiguan());
			
			for(String string : jing.getZhayao()) {
				zhayao.add(string);
			}
			for(String string : jing.getLeiguanList()) {
				leiguan.add(string);
			}
			addZhayaoView(-1);
			addLeiguanView(-1);
			et_add_yaoliang.setText("" + zhayao.size());
			et_add_leiguan.setText("" + leiguan.size());
			
//			btn_lock_location.setVisibility(View.GONE);
//			LocationView(jing);
		} else {
			et_add_koushu.setText("井号1");
			zhayao.add("");
			addZhayaoView(-1);
			leiguan.add("");
			addLeiguanView(-1);
		}
	}

	private void bindEvent() {
		iv_leiguan.setOnClickListener(this);
		back_linear.setOnClickListener(this);
//		tv_help.setOnClickListener(this);
		btn_add_jing.setOnClickListener(this);
		btn_add_back.setOnClickListener(this);
		btn_lock_location.setOnClickListener(this);
	}

	/**
	 * 保存数据，返回
	 * 
	 * @return
	 */
	public boolean getDataFinish() {
		add_koushu = et_add_koushu.getText().toString();
		add_zuanjing = et_add_zuanjing.getText().toString();
		add_xiayao = et_add_xiayao.getText().toString();
		add_yaoliang = et_add_yaoliang.getText().toString();
		add_leiguan = et_add_leiguan.getText().toString();

		Jing jing = new Jing();
		if (add_koushu != null && !"".equals(add_koushu)) {

			if (add_zuanjing != null && !"".equals(add_zuanjing)) {

				if (add_xiayao != null && !"".equals(add_xiayao)) {

					if (add_yaoliang != null && !"".equals(add_yaoliang)) {

						if (add_leiguan != null && !"".equals(add_leiguan)) {
							jing.setJinghao(add_koushu);
							jing.setZuanjing(add_zuanjing);
							jing.setXiayao(add_xiayao);
							jing.setYaoliang(zhayao.size() + "");
							jing.setLeiguan(leiguan.size() + "");
							jing.setLeiguanList(leiguan);
							jing.setZhayao(zhayao);

							jings.add(jing);
							AddActivity.this.finish();

							return true;
						} else {
							AddActivity.this.finish();
							return true;
						}
					} else {
						AddActivity.this.finish();
						return true;
					}
				} else {
					AddActivity.this.finish();
					return true;
				}
			} else {
				AddActivity.this.finish();
				return true;
			}
		} else {
			AddActivity.this.finish();
			return true;
		}
	}

	/**
	 * 保存并返回
	 * 
	 * @return
	 */
	public boolean getDataAndFinish() {
		add_koushu = et_add_koushu.getText().toString();
		add_zuanjing = et_add_zuanjing.getText().toString();
		add_xiayao = et_add_xiayao.getText().toString();
		add_yaoliang = et_add_yaoliang.getText().toString();
		add_leiguan = et_add_leiguan.getText().toString();

		Jing jing = new Jing();
		if (add_koushu != null && !"".equals(add_koushu)) {

			if (add_zuanjing != null && !"".equals(add_zuanjing)) {

				if (add_xiayao != null && !"".equals(add_xiayao)) {

					if (add_yaoliang != null && !"".equals(add_yaoliang)) {

						if (add_leiguan != null && !"".equals(add_leiguan)) {
							jing.setJinghao(add_koushu);
							jing.setZuanjing(add_zuanjing);
							jing.setXiayao(add_xiayao);
							jing.setYaoliang(zhayao.size() + "");
							jing.setLeiguan(leiguan.size() + "");
							jing.setLeiguanList(leiguan);
							jing.setZhayao(zhayao);

							jings.add(jing);
							
							AddActivity.this.finish();
							return true;
						} else {
							Toast.makeText(AddActivity.this, "请输入雷管", Toast.LENGTH_SHORT).show();
							return false;
						}
					} else {
						Toast.makeText(AddActivity.this, "请输入药量", Toast.LENGTH_SHORT).show();
						return false;
					}
				} else {
					Toast.makeText(AddActivity.this, "请输入下药深度", Toast.LENGTH_SHORT).show();
					return false;
				}
			} else {
				Toast.makeText(AddActivity.this, "请输入钻井深度", Toast.LENGTH_SHORT).show();
				return false;
			}
		} else {
			Toast.makeText(AddActivity.this, "请输入钻井深度", Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	/**
	 * 获取数据并添加
	 */
	public void getData() {
		add_koushu = et_add_koushu.getText().toString();
		add_zuanjing = et_add_zuanjing.getText().toString();
		add_xiayao = et_add_xiayao.getText().toString();
		add_yaoliang = et_add_yaoliang.getText().toString();
		add_leiguan = et_add_leiguan.getText().toString();

		Jing jing = new Jing();
		if (add_koushu != null && !"".equals(add_koushu)) {
			if (add_zuanjing != null && !"".equals(add_zuanjing)) {
				if (add_xiayao != null && !"".equals(add_xiayao)) {
					if (add_yaoliang != null && !"".equals(add_yaoliang)) {
						if (add_leiguan != null && !"".equals(add_leiguan)) {
							jing.setJinghao(add_koushu);
							jing.setZuanjing(add_zuanjing);
							jing.setXiayao(add_xiayao);
							jing.setYaoliang(zhayao.size() + "");
							jing.setLeiguan(leiguan.size() + "");
							jing.setLeiguanList(leiguan);
							jing.setZhayao(zhayao);

							jings.add(jing);

						} else {
							Toast.makeText(AddActivity.this, "请输入雷管", Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(AddActivity.this, "请输入药量", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(AddActivity.this, "请输入下药深度", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(AddActivity.this, "请输入钻井深度", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(AddActivity.this, "请输入钻井深度", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 更新数据并添加
	 */
	public void updataData() {
		add_koushu = et_add_koushu.getText().toString();
		add_zuanjing = et_add_zuanjing.getText().toString();
		add_xiayao = et_add_xiayao.getText().toString();
		add_yaoliang = et_add_yaoliang.getText().toString();
		add_leiguan = et_add_leiguan.getText().toString();

		Jing jing = jings.get(index);
		if (add_koushu != null && !"".equals(add_koushu)) {
			jing.setJinghao(add_koushu);
			if (add_zuanjing != null && !"".equals(add_zuanjing)) {
				jing.setZuanjing(add_zuanjing);
				if (add_xiayao != null && !"".equals(add_xiayao)) {
					jing.setXiayao(add_xiayao);
					if (add_yaoliang != null && !"".equals(add_yaoliang)) {
						if (add_leiguan != null && !"".equals(add_leiguan)) {
							jing.setYaoliang(zhayao.size() + "");
							jing.setLeiguan(leiguan.size() + "");
							
							jing.setLeiguanList(leiguan);
							jing.setZhayao(zhayao);

						} else {
							Toast.makeText(AddActivity.this, "请输入雷管", Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(AddActivity.this, "请输入药量", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(AddActivity.this, "请输入下药深度", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(AddActivity.this, "请输入钻井深度", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(AddActivity.this, "请输入井号", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 更新数据并返回
	 * 
	 * @return
	 */
	public boolean updataDataAndFinish() {
		add_koushu = et_add_koushu.getText().toString();
		add_zuanjing = et_add_zuanjing.getText().toString();
		add_xiayao = et_add_xiayao.getText().toString();
		add_yaoliang = et_add_yaoliang.getText().toString();
		add_leiguan = et_add_leiguan.getText().toString();

		Jing jing = jings.get(index);
		if (add_koushu != null && !"".equals(add_koushu)) {
			jing.setJinghao(add_koushu);
			if (add_zuanjing != null && !"".equals(add_zuanjing)) {
				jing.setZuanjing(add_zuanjing);
				if (add_xiayao != null && !"".equals(add_xiayao)) {
					jing.setXiayao(add_xiayao);
					if (add_yaoliang != null && !"".equals(add_yaoliang)) {
						if (add_leiguan != null && !"".equals(add_leiguan)) {
							jing.setYaoliang(zhayao.size() + "");
							jing.setLeiguan(leiguan.size() + "");
							
							jing.setLeiguanList(leiguan);
							jing.setZhayao(zhayao);

							AddActivity.this.finish();
							isUpdate = false;
							return true;
						} else {
							Toast.makeText(AddActivity.this, "请输入雷管", Toast.LENGTH_SHORT).show();
							return false;
						}
					} else {
						Toast.makeText(AddActivity.this, "请输入药量", Toast.LENGTH_SHORT).show();
						return false;
					}
				} else {
					Toast.makeText(AddActivity.this, "请输入下药深度", Toast.LENGTH_SHORT).show();
					return false;
				}
			} else {
				Toast.makeText(AddActivity.this, "请输入钻井深度", Toast.LENGTH_SHORT).show();
				return false;
			}
		} else {
			Toast.makeText(AddActivity.this, "请输入井号", Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	/**
	 * 返回
	 * 
	 * @return
	 */
	public boolean updataDataFinish() {
		add_koushu = et_add_koushu.getText().toString();
		add_zuanjing = et_add_zuanjing.getText().toString();
		add_xiayao = et_add_xiayao.getText().toString();
		add_yaoliang = et_add_yaoliang.getText().toString();
		add_leiguan = et_add_leiguan.getText().toString();

		Jing jing = jings.get(index);
		if (add_koushu != null && !"".equals(add_koushu)) {
			jing.setJinghao(add_koushu);
			if (add_zuanjing != null && !"".equals(add_zuanjing)) {
				jing.setZuanjing(add_zuanjing);
				if (add_xiayao != null && !"".equals(add_xiayao)) {
					jing.setXiayao(add_xiayao);
					if (add_yaoliang != null && !"".equals(add_yaoliang)) {
						if (add_leiguan != null && !"".equals(add_leiguan)) {
							jing.setYaoliang(zhayao.size() + "");
							jing.setLeiguan(leiguan.size() + "");
							
							jing.setLeiguanList(leiguan);
							jing.setZhayao(zhayao);

							AddActivity.this.finish();
							isUpdate = false;
							return true;
						} else {
							AddActivity.this.finish();
							return true;
						}
					} else {
						AddActivity.this.finish();
						return true;
					}
				} else {
					AddActivity.this.finish();
					return true;
				}
			} else {
				AddActivity.this.finish();
				return true;
			}
		} else {
			AddActivity.this.finish();
			return true;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_leiguan:
			Toast.makeText(AddActivity.this, "", Toast.LENGTH_SHORT).show();
			break;

		case R.id.btn_add_jing: // 保存并添加
			if (isUpdate) {
				updataData();
				isUpdate = false;
			} else {
				getData();
			}

			et_add_koushu.setText("井号" + (jings.size() + 1));
			et_add_zuanjing.setText("");
			et_add_xiayao.setText("");
			et_add_yaoliang.setText("");
			et_add_leiguan.setText("");
			ll_add_location.removeAllViews();
			zhayao.clear();
			leiguan.clear();
			zhayao.add("");
			addZhayaoView(-1);
			leiguan.add("");
			addLeiguanView(-1);

			et_add_koushu.setFocusable(true);
			et_add_koushu.setClickable(true);

			RecordActivity.isRefresh = true;
			break;

		case R.id.ll_title_back:
			ll_add_location.removeAllViews();
			AddActivity.this.finish();
			break;

		case R.id.btn_add_back: // 保存并返回
			if (isUpdate) {
				updataDataAndFinish();
				isUpdate = false;
			} else {
				getDataAndFinish();
			}
			ll_add_location.removeAllViews();
			break;

		case R.id.btn_lock_location:
			break;
		default:
			break;
		}
	}

}
