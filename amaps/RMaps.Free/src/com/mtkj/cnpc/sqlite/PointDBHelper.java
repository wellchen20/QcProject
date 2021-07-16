package com.mtkj.cnpc.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mtkj.cnpc.sqlite.constants.DBConstants;

public class PointDBHelper extends SQLiteOpenHelper {

	private static PointDBHelper instance = null;

	public static PointDBHelper getInstance(Context context) {
		if (instance == null) {
			instance = new PointDBHelper(context);
		}
		return instance;
	}
	
	private PointDBHelper(Context context) {
		super(context, DBConstants.DB_NAME, null, DBConstants.VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// 创建炮点更新表
		db.execSQL(DBConstants.CREATE_PACKAGETNUM_TABLE);
		// 创建推土任务表
		db.execSQL(DBConstants.CREATE_BULLDOZTASK_TABLE);
		// 创建推土下地口表
		db.execSQL(DBConstants.CREATE_BULLDOZPOINT_TABLE);
		// 创建钻井下药炮点数据表
		db.execSQL(DBConstants.CREATE_DRILLPOINT_TABLE);
		// 创建井炮炮点数据表
		db.execSQL(DBConstants.CREATE_SHOTPOINT_TABLE);
		// 创建钻井下药记录数据表
		db.execSQL(DBConstants.CREATE_DRILLRECORD_TABLE);
		//创建质检记录表
		db.execSQL(DBConstants.CREATE_CHECKRECORD_TABLE);
		// 创建每日任务表
		db.execSQL(DBConstants.CREATE_DAILY_TASK);
		// 创建每日任务炮点
		db.execSQL(DBConstants.CREATE_DAILY_POINT);
		// 创建车辆表
		db.execSQL(DBConstants.CREATE_TRAVE);
		// 创建车牌表
		db.execSQL(DBConstants.CREATE_CARNUM);
		// 创建车辆钥匙管理
		db.execSQL(DBConstants.CREATE_CARKEYS);
		// 创建搜索历史表
		db.execSQL(DBConstants.CREATE_SEARCH_HISTORY);
		// 创建排列表
		db.execSQL(DBConstants.CREATE_ARRANGEPOINT_TABLE);
		//创建任务表
		db.execSQL(DBConstants.CREATE_TASK);
		//创建聊天记录表
		db.execSQL(DBConstants.CREATE_TALK);
		db.execSQL(DBConstants.CREATE_ARRANGERECORD_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		/*db.execSQL("alter table arrange_point add column hidden integer DEFAULT '0'");
		db.execSQL("alter table arrange_point add column isdone integer DEFAULT '0'");
		db.execSQL("alter table arrange_point add column time text");
		db.execSQL("alter table arrange_point add column remark text");*/
		db.execSQL("drop table if exists arrange_point");
		onCreate(db);
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}
}
