package com.mtkj.cnpc.activity;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.ByteString;
import com.mtkj.cnpc.protocol.constants.DSSProtoDataConstants.ProtoMsgType;
import com.mtkj.cnpc.protocol.constants.ProtocolConstants;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.constants.SysConfig.RULE;
import com.mtkj.cnpc.protocol.constants.SysConfig.WorkType;
import com.mtkj.cnpc.protocol.constants.SysContants;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_Head;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_WellShotData;
import com.mtkj.cnpc.protocol.shot.GK08;
import com.mtkj.cnpc.protocol.shot.GK12;
import com.mtkj.cnpc.protocol.shot.RF08;
import com.mtkj.cnpc.protocol.socket.DataProcess;
import com.mtkj.cnpc.protocol.utils.SocketUtils;
import com.mtkj.cnpc.R;
import com.robert.maps.applib.utils.LogFileUtil;

/***
 * 任务设置--
 * 
 * @author TNT
 * 
 */
public class TaskSettingActivity extends Activity implements OnClickListener {

	private SharedPreferences mPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initViews();
	}

	protected void initViews() {
		setContentView(R.layout.activity_task_setting);
		mPreferences = getSharedPreferences(SysContants.SYSCONFIG, MODE_PRIVATE);
		String name = "";
		if (SysConfig.workType == WorkType.WORK_TYPE_BULLDOZE) {
			name = "推土";
		} else if (SysConfig.workType == WorkType.WORK_TYPE_DRILE) {
			name = "钻井";
		} else if (SysConfig.workType == WorkType.WORK_TYPE_SHOT) {
			name = "井炮";
		}

		// 标题和菜单
		findViewById(R.id.ll_title_back).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TaskSettingActivity.this.finish();
			}
		});;
		((TextView) findViewById(R.id.tv_title_title)).setText("任务设置(" + name + ")");

		// 菜单点击监听
		findViewById(R.id.setting_appkey).setOnClickListener(this);
		findViewById(R.id.setting_01).setOnClickListener(this);
		findViewById(R.id.setting_02).setOnClickListener(this);
//		findViewById(R.id.setting_03).setOnClickListener(this);
//		findViewById(R.id.setting_04).setOnClickListener(this);
		findViewById(R.id.setting_05).setOnClickListener(this);
		findViewById(R.id.setting_06).setOnClickListener(this);
		findViewById(R.id.setting_07).setOnClickListener(this);
		findViewById(R.id.setting_08).setOnClickListener(this);
		findViewById(R.id.setting_09).setOnClickListener(this);
//		findViewById(R.id.setting_10).setOnClickListener(this);
		findViewById(R.id.setting_11).setOnClickListener(this);
		findViewById(R.id.setting_12).setOnClickListener(this);
		findViewById(R.id.setting_13).setOnClickListener(this);
		
		findViewById(R.id.setting_is_dscloud).setOnClickListener(this);
		findViewById(R.id.setting_is_dscloud).setSelected(getData(SysContants.ISCLOUD, SysConfig.isDSCloud));

		defSetting();

		// 初始化菜单
		// initMenu();
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.setting_appkey:{
//			showAppkeySetting();
		}
			break;
		case R.id.setting_01: {// 爆炸机编号
			showBZJSelected();
		}
			break;
		case R.id.setting_02: {// 手持设备编号
//			showNAVSelected();
		}
			break;
		case R.id.setting_03: {// 井口坐标匹配最大距离
			PiPeiDisSetting();
		}
			break;
		case R.id.setting_04: {// 安全距离
			SafeDisSetting();
		}
			break;
		case R.id.setting_05: {// 电台IP
			WifiIPSettting();
		}
			break;
		case R.id.setting_06: {// 电台端口
			WifiPortSetting();
		}
			break;
		case R.id.setting_07: {// 大小号规则
			// showZhuangHaoRoule();
			showZhuangHaoRoule2();
			showZhuangHaoRoule1();
		}
			break;
		case R.id.setting_08: {// 线号差值
			ShowRuleLineValueSetting();
		}
			break;
		case R.id.setting_09: {// 点号差值
			ShowRulePointValueSetting();
		}
			break;
		case R.id.setting_10: {// Ready信号等待时间
			ShowReadyTimeoutSetting();
		}
			break;
		case R.id.setting_11: {// 组织机构编号
			showZZJGSelected();
		}
			break;
		case R.id.setting_12: {// 发送位置时间间隔
			showTimeSelected();
		}
			break;
		
		case R.id.setting_is_dscloud:{ // 是否使用dscloud
			v.setSelected(!v.isSelected());
			SysConfig.isDSCloud = v.isSelected();
			setData(SysContants.ISCLOUD, SysConfig.isDSCloud);
		}
			break;
		}
	}
	
	private void defSetting() {
		((TextView) findViewById(R.id.setting_01_value))
				.setText(SysConfig.BZJ_ID);
		((TextView) findViewById(R.id.setting_02_value))
				.setText(String.valueOf(Integer.valueOf(SysConfig.SC_ID)));
		((TextView)findViewById(R.id.setting_11_value))
				.setText(SysConfig.ZZJG_ID);
		((TextView) findViewById(R.id.setting_03_value))
				.setText(SysConfig.ShotproMax + "米");
		((TextView) findViewById(R.id.setting_04_value))
				.setText(SysConfig.safe_Distance + "米");
		((TextView) findViewById(R.id.setting_05_value)).setText(SysConfig.IP);
		((TextView) findViewById(R.id.setting_06_value)).setText(SysConfig.PORT
				+ "");

		String[] arrData = getResources().getStringArray(
				R.array.zhuanghao_roule_mode);
		((TextView) findViewById(R.id.setting_07_value))
				.setText(arrData[SysConfig.SelectRule]);

		((TextView) findViewById(R.id.setting_08_value)).setText("差值:"
				+ SysConfig.SelectRuleLine + "");

		((TextView) findViewById(R.id.setting_09_value)).setText("差值:"
				+ SysConfig.SelectRulePoint + "");

		((TextView) findViewById(R.id.setting_10_value))
				.setText(SysConfig.Readytimeout + "");
		((TextView) findViewById(R.id.setting_powertime_value))
				.setText(SysConfig.PowerTimeout + "");
		((TextView)findViewById(R.id.setting_appkey_value))
				.setText(SysConfig.APP_KEY);;
		((TextView)findViewById(R.id.setting_12_value))
				.setText(SysConfig.GPS_UP_TIME_TIP + "");
		if (getData(SysContants.SHOTSELECTTYPE,0)==0){
			((TextView)findViewById(R.id.setting_13_value))
					.setText("普通放炮模式");
		}else {
			((TextView)findViewById(R.id.setting_13_value))
					.setText("中继放炮模式");
		}
	}
	
	/**
	 * 设置APPKEY
	 * 
	 */
	private void showAppkeySetting() {
		View view = getLayoutInflater().inflate(
				R.layout.dialog_set_app_key_dis, null);
		final EditText editText = (EditText) view.findViewById(R.id.name);
		editText.setText(SysConfig.APP_KEY);
		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.set_app_key))
				.setView(view)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String info = editText.getText().toString()
										.trim();
								if (!info.equalsIgnoreCase("")) {
									SysConfig.APP_KEY = info;
									saveAppkeyConfig();
								} else {
									showMessage("不可为空");
								}
							}
						}).setNegativeButton(R.string.cancel, null).create()
				.show();
	}
	private void saveAppkeyConfig() {
		setData(SysContants.APPKEY, SysConfig.APP_KEY);
		((TextView) findViewById(R.id.setting_appkey_value))
				.setText(SysConfig.APP_KEY);
	}

	/**
	 * 设置IP地址
	 * 
	 */
	private void WifiIPSettting() {
		View view = getLayoutInflater().inflate(
				R.layout.dialog_set_service_add, null);
		((TextView) view.findViewById(R.id.title))
				.setText(getString(R.string.wifi_url_setting));
		final EditText editText_ip_1 = (EditText) view.findViewById(R.id.tv_ip_1);
		final EditText editText_ip_2 = (EditText) view.findViewById(R.id.tv_ip_2);
		final EditText editText_ip_3 = (EditText) view.findViewById(R.id.tv_ip_3);
		final EditText editText_ip_4 = (EditText) view.findViewById(R.id.tv_ip_4);
		String[] ip = SysConfig.IP.split("\\.");
		editText_ip_1.setText(ip[0]);
		editText_ip_2.setText(ip[1]);
		editText_ip_3.setText(ip[2]);
		editText_ip_4.setText(ip[3]);
		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.wifi_url_setting))
				.setView(view)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String ip_1 = editText_ip_1.getText().toString()
										.trim();
								String ip_2 = editText_ip_2.getText().toString()
										.trim();
								String ip_3 = editText_ip_3.getText().toString()
										.trim();
								String ip_4 = editText_ip_4.getText().toString()
										.trim();
								if (!ip_1.equalsIgnoreCase("") && !ip_2.equalsIgnoreCase("") && !ip_3.equalsIgnoreCase("") && !ip_4.equalsIgnoreCase("")
										&& Integer.parseInt(ip_1) > 0 && Integer.parseInt(ip_1) < 256 && Integer.parseInt(ip_2) >= 0 && Integer.parseInt(ip_2) < 256
										&& Integer.parseInt(ip_3) >= 0 && Integer.parseInt(ip_3) < 256 && Integer.parseInt(ip_4) >= 0 && Integer.parseInt(ip_4) < 255) {
									SysConfig.IP = new StringBuffer().append(ip_1).append(".")
											.append(ip_2).append(".").append(ip_3).append(".").append(ip_4).toString();
									saveWifiIPConfig();
								} else {
									Toast.makeText(TaskSettingActivity.this, "ip无效", Toast.LENGTH_SHORT).show();
								}
							}
						}).setNegativeButton(R.string.cancel, null).create()
				.show();
	}

	/**
	 * 端口号设置
	 * 
	 */
	private void WifiPortSetting() {
		View view = getLayoutInflater().inflate(R.layout.dialog_set_port_add,
				null);
		((TextView) view.findViewById(R.id.title))
				.setText(getString(R.string.wifi_port_setting));
		final EditText editText = (EditText) view.findViewById(R.id.name);
		editText.setText(SysConfig.PORT + "");
		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.wifi_port_setting))
				.setView(view)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String info = editText.getText().toString()
										.trim();
								if (!info.equalsIgnoreCase("")) {
									try {
										SysConfig.PORT = Integer.parseInt(info);
										saveWifiPortConfig();
									} catch (Exception e) {
									}
								}
							}
						}).setNegativeButton(R.string.cancel, null).create()
				.show();
	}

	/**
	 * 匹配距离设置
	 * 
	 */
	private void PiPeiDisSetting() {
		View view = getLayoutInflater().inflate(
				R.layout.dialog_set_min_pipei_dis, null);
		final EditText editText = (EditText) view.findViewById(R.id.name);
		editText.setText(SysConfig.ShotproMax + "");
		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.pipei_dis_setting))
				.setView(view)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String info = editText.getText().toString()
										.trim();
								if (!info.equalsIgnoreCase("")) {
									try {
										float dis = Float.parseFloat(info);
										if (dis <= 1000) {
											SysConfig.ShotproMax = dis;
											savePiPeiDisConfig();
										} else {
											showMessage("匹配距离太大");
										}
									} catch (Exception e) {
									}
								} else {
									showMessage("不可为空");
								}
							}
						}).setNegativeButton(R.string.cancel, null).create()
				.show();
	}

	/**
	 * 安全距离设置
	 * 
	 */
	private void SafeDisSetting() {
		View view = getLayoutInflater().inflate(
				R.layout.dialog_set_min_safe_dis, null);
		final EditText editText = (EditText) view.findViewById(R.id.name);
		editText.setText(SysConfig.safe_Distance + "");
		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.safe_dis_setting))
				.setView(view)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String info = editText.getText().toString()
										.trim();
								if (!info.equalsIgnoreCase("")) {
									try {
										float dis = Float.parseFloat(info);
										if (dis >= 15) {
											SysConfig.safe_Distance = dis;
											saveSafeDisConfig();
										} else {
											showMessage("最小安全距离不低于15米");
										}
									} catch (Exception e) {
									}
								} else {
									showMessage("不可为空");
								}
							}
						}).setNegativeButton(R.string.cancel, null).create()
				.show();
	}

	/**
	 * 爆炸机编号设置
	 * 
	 */
	private void showBZJSelected() {
		final String[] arrBZJ = getResources().getStringArray(R.array.bzj_list);
		new AlertDialog.Builder(TaskSettingActivity.this)
				.setTitle("请选择爆炸机编号")
				.setSingleChoiceItems(arrBZJ, -1,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								SysConfig.BZJ_ID = arrBZJ[which];
								savePeiDuiBZJConfig();
							}
						}).create().show();
	}

	/**
	 * 设置手机编号
	 * 
	 */
	private void showNAVSelected() {
		final String[] arrNAV = getResources().getStringArray(R.array.nav_list);
		new AlertDialog.Builder(TaskSettingActivity.this)
				.setTitle("请选择手持机编号")
				.setSingleChoiceItems(arrNAV, -1,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								SysConfig.SC_ID = arrNAV[which];
								
								if (!"".equals(SysConfig.SC_ID)) {
									if (Integer.valueOf(SysConfig.SC_ID) < 10) {
//										if (SysConfig.workType == WorkType.WORK_TYPE_SHOT) {
											SysConfig.SC = new StringBuffer().append(SysConfig.HANDSET).append("0").append(SysConfig.SC_ID).toString();
//										} else if (SysConfig.workType == WorkType.WORK_TYPE_DRILE) {
//											SysConfig.SC = new StringBuffer().append(SysConfig.DRILLSET).append("0").append(SysConfig.SC_ID).toString();
//										}
									} else {
//										if (SysConfig.workType == WorkType.WORK_TYPE_SHOT) {
											SysConfig.SC = new StringBuffer().append(SysConfig.HANDSET).append(SysConfig.SC_ID).toString();
//										} else if (SysConfig.workType == WorkType.WORK_TYPE_DRILE) {
//											SysConfig.SC = new StringBuffer().append(SysConfig.DRILLSET).append(SysConfig.SC_ID).toString();
//										}
									}
								}
								
								savePeiDuiNAVConfig();
							}
						}).create().show();
	}
	
	/**
	 * 设置组织机构编号
	 * 
	 */
	private void showZZJGSelected() {
		final String[] arrZZJG = getResources().getStringArray(R.array.zzjg_list);
		new AlertDialog.Builder(TaskSettingActivity.this)
				.setTitle("请选择组织机构编号")
				.setSingleChoiceItems(arrZZJG, -1,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								SysConfig.ZZJG_ID = arrZZJG[which];
								savePeiDuiZZJGConfig();
							}
						}).create().show();
	}

	private void showZhuangHaoRoule1() {
		String[] arrData = getResources().getStringArray(
				R.array.zhuanghao_roule_mode1);
		new AlertDialog.Builder(TaskSettingActivity.this)
				.setTitle("请选择模式")
				.setSingleChoiceItems(arrData, -1,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								SysConfig.SelectRuleMode = which;
								// saveSelectZhuangHaoConfig();
							}
						}).create().show();
	}

	private void showZhuangHaoRoule2() {
		String[] arrData = getResources().getStringArray(
				R.array.zhuanghao_roule_mode2);
		new AlertDialog.Builder(TaskSettingActivity.this)
				.setTitle("请选择变化规则")
				.setSingleChoiceItems(arrData, -1,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								SysConfig.SelectRuleMethod = which;
								if (SysConfig.SelectRuleMode == 0) {
									if (SysConfig.SelectRuleMethod == 0) {
										SysConfig.SelectRule = RULE.RULE_0;

									} else if (SysConfig.SelectRuleMethod == 1) {
										SysConfig.SelectRule = RULE.RULE_1;
									}
								} else if (SysConfig.SelectRuleMode == 1) {
									if (SysConfig.SelectRuleMethod == 0) {
										SysConfig.SelectRule = RULE.RULE_2;

									} else if (SysConfig.SelectRuleMethod == 1) {
										SysConfig.SelectRule = RULE.RULE_3;
									}
								}
								saveSelectZhuangHaoConfig();
							}
						}).create().show();
	}

	private void showZhuangHaoRouleOld() {
		String[] arrData = getResources().getStringArray(
				R.array.zhuanghao_roule);
		new AlertDialog.Builder(TaskSettingActivity.this)
				.setTitle("请选择桩号规则")
				.setSingleChoiceItems(arrData, -1,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								SysConfig.SelectRule = which;
								saveSelectZhuangHaoConfig();
							}
						}).create().show();
	}

	private void ShowRuleLineValueSetting() {
		View view = getLayoutInflater().inflate(R.layout.dialog_set_rule_line,
				null);
		final EditText editText = (EditText) view.findViewById(R.id.name);
		editText.setText(SysConfig.SelectRuleLine + "");
		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.rule_line_setting))
				.setView(view)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String info = editText.getText().toString()
										.trim();
								if (!info.equalsIgnoreCase("")) {
									try {
										int dis = Integer.parseInt(info);
										if (dis >= 0) {
											SysConfig.SelectRuleLine = dis;
											saveRuleLineValueConfig();
										} else {
											showMessage("不可为负数");
										}
									} catch (Exception e) {
									}
								} else {
									showMessage("不可为空");
								}
							}
						}).setNegativeButton(R.string.cancel, null).create()
				.show();
	}

	private void ShowRulePointValueSetting() {
		View view = getLayoutInflater().inflate(R.layout.dialog_set_rule_point,
				null);
		final EditText editText = (EditText) view.findViewById(R.id.name);
		editText.setText(SysConfig.SelectRulePoint + "");
		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.rule_point_setting))
				.setView(view)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String info = editText.getText().toString()
										.trim();
								if (!info.equalsIgnoreCase("")) {
									try {
										try {
											int dis = Integer.parseInt(info);
											if (dis >= 0) {
												SysConfig.SelectRulePoint = dis;
												saveRulePointValueConfig();
											} else {
												showMessage("不可为负数");
											}
										} catch (Exception e) {
										}
									} catch (Exception e) {
									}
								} else {
									showMessage("不可为空");
								}
							}
						}).setNegativeButton(R.string.cancel, null).create()
				.show();
	}

	
	private void ShowReadyTimeoutSetting() {
		View view = getLayoutInflater().inflate(
				R.layout.dialog_set_ready_timeout, null);
		final EditText editText = (EditText) view.findViewById(R.id.name);
		editText.setText(SysConfig.Readytimeout + "");
		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.ready_timeout_setting))
				.setView(view)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String info = editText.getText().toString()
										.trim();
								if (!info.equalsIgnoreCase("")) {
									try {
										try {
											int value = Integer.parseInt(info);
											if (value >= 0) {
												SysConfig.Readytimeout = value;
												saveReadyTimeOutConfig();
											} else {
												showMessage("不可为负数");
											}
										} catch (Exception e) {
										}
									} catch (Exception e) {
									}
								} else {
									showMessage("不可为空");
								}
							}
						}).setNegativeButton(R.string.cancel, null).create()
				.show();
	}

	private void savePeiDuiBZJConfig() {
		setData(SysContants.BZJ, SysConfig.BZJ_ID);
		((TextView) findViewById(R.id.setting_01_value))
				.setText(SysConfig.BZJ_ID);
	}

	private void savePeiDuiNAVConfig() {
		setData(SysContants.SC, SysConfig.SC_ID);
		((TextView) findViewById(R.id.setting_02_value))
				.setText(SysConfig.SC_ID);
	}
	
	private void savePeiDuiZZJGConfig() {
		setData(SysContants.ZZJG, SysConfig.ZZJG_ID);
		((TextView) findViewById(R.id.setting_11_value))
				.setText(SysConfig.ZZJG_ID);
	}

	private void saveSelectZhuangHaoConfig() {
		String[] arrData = getResources().getStringArray(
				R.array.zhuanghao_roule_mode);
		setData(SysContants.STATION_RULE, SysConfig.SelectRule);
		((TextView) findViewById(R.id.setting_07_value))
				.setText(arrData[SysConfig.SelectRule]);
	}

	private void saveRuleLineValueConfig() {
		setData(SysContants.RULE_LINE_VALUE,SysConfig.SelectRuleLine);
		((TextView) findViewById(R.id.setting_08_value)).setText("差值:"
				+ SysConfig.SelectRuleLine);
	}

	private void saveRulePointValueConfig() {
		setData(SysContants.RULE_SPOINT_VALUE, SysConfig.SelectRulePoint);
		((TextView) findViewById(R.id.setting_09_value)).setText("差值:"
				+ SysConfig.SelectRulePoint);
	}

	private void saveReadyTimeOutConfig() {
		setData(SysContants.READY_TIMEOUT, SysConfig.Readytimeout);
		((TextView) findViewById(R.id.setting_10_value)).setText(""
				+ SysConfig.Readytimeout);
	}
	
	private void savePowerTimeOutConfig() {
		setData(SysContants.POWER_TIMEOUT, SysConfig.PowerTimeout);
		((TextView) findViewById(R.id.setting_powertime_value)).setText("" + SysConfig.PowerTimeout);
	}

	private void savePiPeiDisConfig() {
		setData(SysContants.SHOTPRO_MAX, SysConfig.ShotproMax);
		((TextView) findViewById(R.id.setting_03_value))
				.setText(SysConfig.ShotproMax + "米");
	}

	private void saveSafeDisConfig() {
		setData(SysContants.SAFE_DISTANCE, SysConfig.safe_Distance);
		((TextView) findViewById(R.id.setting_04_value))
				.setText(SysConfig.safe_Distance + "米");
	}

	private void saveWifiIPConfig() {
		setData(SysContants.WIFI_IP, SysConfig.IP);
//		HttpContact.URL = "http://" + SysConfig.IP + ":" + SysConfig.PORT + "/huobao-service/";
		((TextView) findViewById(R.id.setting_05_value)).setText(SysConfig.IP);
	}

	private void saveWifiPortConfig() {
		setData(SysContants.WIFI_PORT, SysConfig.PORT);
//		HttpContact.URL = "http://" + SysConfig.IP + ":" + SysConfig.PORT + "/huobao-service/";
		((TextView) findViewById(R.id.setting_06_value)).setText(SysConfig.PORT
				+ "");
	}

	private void showTimeSelected() {
		final String[] arrTime = getResources().getStringArray(R.array.send_location_time);
		new AlertDialog.Builder(TaskSettingActivity.this)
				.setTitle("请选择定时发送位置间隔时间")
				.setSingleChoiceItems(arrTime, -1,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								SysConfig.GPS_UP_TIME_TIP = Integer.parseInt(arrTime[which]);
								saveTimeConfig();
							}
						}).create().show();
	}
	
	private void saveTimeConfig() {
		setData(SysContants.GPS_UPTIME, SysConfig.GPS_UP_TIME_TIP);
		((TextView) findViewById(R.id.setting_12_value)).setText(SysConfig.GPS_UP_TIME_TIP
				+ "");
	}
	
	
	// 发送参数配置请求
	private boolean isConn = false;
	public class Task extends AsyncTask<String, Integer, Boolean> {

		public Task() {
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				if (SysConfig.SC_ID != null && !"".equals(SysConfig.SC_ID)) {
					if (SysConfig.isDSCloud) {
						Proto_WellShotData proto_WellShotData = Proto_WellShotData.newBuilder()
//								.setMdata(ByteString.copyFrom(new RF08(SysConfig.SC_ID).content, "GB2312"))
								.setMdata(ByteString.copyFrom(SocketUtils.writeBodyBytes(new RF08(String.valueOf(Integer.valueOf(SysConfig.SC_ID))).content.getBytes())))
								.build();
						Proto_Head head = Proto_Head.newBuilder()
								.setProtoMsgType(ProtoMsgType.protoMsgType_HandsetToWSC)
								.setCmdSize(proto_WellShotData.toByteArray().length)
								.addReceivers(ByteString.copyFrom(SysConfig.WSC, "GB2312"))
								.setReceivers(0, ByteString.copyFrom(SysConfig.WSC, "GB2312"))
								.setSender(ByteString.copyFrom(SysConfig.SC, "GB2312"))
								.setMsgId(0).setPriority(1).setExpired(0)
								.build();
						try {
							return DataProcess.GetInstance().sendData(SocketUtils.writeBytes(head.toByteArray(), proto_WellShotData.toByteArray()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						return DataProcess.GetInstance().sendData(
								new RF08(SysConfig.SC_ID));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				LogFileUtil.saveFileToSDCard("RF08: " + e.getMessage());
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (!result) {
				isConn = DataProcess.GetInstance().isConnected();
				if (!isConn) {
					new ConnTask().execute("");
				}
			}
		}
	}
	
	// 连接WIFI模块
	public class ConnTask extends AsyncTask<String, Integer, Boolean> {
		public static final int TYPE_NEW_CONN = 0;
		public static final int TYPE_RECONN = 1;
		public int nType = TYPE_NEW_CONN;

		public ConnTask() {
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			return DataProcess.GetInstance().startConn(SysConfig.IP,
					SysConfig.PORT);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
		}
	}
	
	// 配置参数handler
	/*public class handler extends Handler {

		public handler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DataProcess.MSG.RECEIVE:
				handleMsg((String) msg.obj);
				break;
				
			}
		}
	}*/
	
	private void handleMsg(String msg) {
		if (SysConfig.isTestMode) {
			Toast.makeText(TaskSettingActivity.this, "测试模式--收到数据:" + msg,
					Toast.LENGTH_LONG).show();
		}
		if (msg != null && msg.length() > 0) {
			if (msg.startsWith(ProtocolConstants.NAME_JINGPAO.GK08)) {// 参数反馈
				handleMsgGK08(msg);
				Log.e("gk08",msg);
			} else if (msg.startsWith(ProtocolConstants.NAME_JINGPAO.GK12)){//更改放炮模式
				Log.e("gk12",msg);
				handleMsgGK12(msg);
			}
		}
	}

	private void saveShotSelectTypeConfig(){
		setData(SysContants.SHOTSELECTTYPE,SysConfig.shotSelectType);
		if (getData(SysContants.SHOTSELECTTYPE,0)==0){
			((TextView)findViewById(R.id.setting_13_value))
					.setText("普通放炮模式");
		}else {
			((TextView)findViewById(R.id.setting_13_value))
					.setText("中继放炮模式");
		}
	}

	public void handleMsgGK12(String msg){
		GK12 gk12 = new GK12(msg);
		String result = (String) gk12.parseMsg(msg);
		if (result.equals("0")){//更新为普通放炮模式
			SysConfig.shotSelectType = Integer.parseInt(result);
			saveShotSelectTypeConfig();
		}else if (result.equals("1")){//更新为中继放炮模式
			SysConfig.shotSelectType = Integer.parseInt(result);
			saveShotSelectTypeConfig();
		}
	}
	
	/***
	 * 配置更新
	 * 
	 * @param msg
	 */
	@SuppressWarnings("unchecked")
	private void handleMsgGK08(String msg) {
		GK08 gk08 = new GK08(msg);
		List<String> configResult = (List<String>) gk08.parseMsg(msg);
		if (configResult != null && configResult.size() > 0) {
			try {
				String shotproMax = configResult.get(0);
				if (shotproMax != null && shotproMax.length() > 0) {
					SysConfig.ShotproMax = Float.parseFloat(shotproMax);
					savePiPeiDisConfig();
//					if (SysConfig.isTestMode) {
//						Util.MsgBox(TaskSettingActivity.this, "收到配置：shotpromax=="
//								+ SysConfig.ShotproMax);
//					}
				}
			} catch (Exception e) {
			}

			try {
				String Distance = configResult.get(1);
				if (Distance != null && Distance.length() > 0) {
					SysConfig.safe_Distance = Float.parseFloat(Distance);
					saveSafeDisConfig();
//					if (SysConfig.isTestMode) {
//						Util.MsgBox(TaskSettingActivity.this, "收到配置：distance=="
//								+ SysConfig.ShotproMax);
//					}
				}
			} catch (Exception e) {
			}

			try {
				String Distance = configResult.get(2);
				if (Distance != null && Distance.length() > 0) {
					SysConfig.Readytimeout = Float.parseFloat(Distance);
					saveReadyTimeOutConfig();
//					if (SysConfig.isTestMode) {
//						Util.MsgBox(TaskSettingActivity.this, "收到配置：readytimeout=="
//								+ SysConfig.Readytimeout);
//					}
				}
			} catch (Exception e) {
			}
			
			try {
				String Distance = configResult.get(3);
				if (Distance != null && Distance.length() > 0) {
					SysConfig.PowerTimeout = Float.parseFloat(Distance);
					savePowerTimeOutConfig();
//					if (SysConfig.isTestMode) {
//						Util.MsgBox(TaskSettingActivity.this, "收到配置：readytimeout=="
//								+ SysConfig.Readytimeout);
//					}
				}
			} catch (Exception e) {
			}
		}
	}

	public String getData(String key, String defValue) {
		return mPreferences.getString(key, defValue);
	}

	public int getData(String key, int defValue) {
		return mPreferences.getInt(key, defValue);
	}

	public long getData(String key, long defValue) {
		return mPreferences.getLong(key, defValue);
	}

	public float getData(String key, float defValue) {
		return mPreferences.getFloat(key, defValue);
	}

	public boolean getData(String key, boolean defValue) {
		return mPreferences.getBoolean(key, defValue);
	}

	public void setData(String key, Object o) {
		if (o != null) {
			SharedPreferences.Editor editor = mPreferences.edit();
			if (o instanceof Boolean) {
				editor.putBoolean(key, (Boolean) o);
			} else if (o instanceof Integer) {
				editor.putInt(key, (Integer) o);
			} else if (o instanceof Long) {
				editor.putLong(key, (Long) o);
			} else if (o instanceof Float) {
				editor.putFloat(key, (Float) o);
			} else if (o instanceof String) {
				editor.putString(key, (String) o);
			}
			editor.commit();
		}
	}

	/**
	 * 显示提示信息
	 *
	 * @param msg
	 */
	protected void showMessage(String msg) {
		Toast.makeText(TaskSettingActivity.this, msg, Toast.LENGTH_SHORT).show();
	}
}
