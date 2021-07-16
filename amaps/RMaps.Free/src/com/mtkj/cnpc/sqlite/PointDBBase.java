package com.mtkj.cnpc.sqlite;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.mtkj.cnpc.sqlite.constants.DBConstants;
import com.mtkj.utils.entity.CheckRecord;

public class PointDBBase extends DBConstants {
	
	protected Context mContext;
	protected PointDBHelper dbHelper;
	
	public PointDBBase(Context context) {
		dbHelper = PointDBHelper.getInstance(context);
		dbHelper.getReadableDatabase();
		mContext = context;
	}
	
	/**
	 * 释放数据库
	 * 
	 */
	public void FreeDatabases(){
		if(dbHelper != null){
			dbHelper.close();
			dbHelper = null;
		}
	}
	
	/***************************************数据包  开始********************************************************/
	/**
	 * 插入一条数据
	 * 
	 * @param type
	 * @param current
	 * @param count
	 */
	public void addPackagetNum(final String type, final String current, final String count, final String time) {
		final ContentValues cv = new ContentValues();
		cv.put(TYPE, type);
		cv.put(CURRENT, current);
		cv.put(COUNT, count);
		cv.put(TIME, time);
		dbHelper.getWritableDatabase().insert(PACKAGETNUM_TABLE, null, cv);
	}
	
	/**
	 * 更新数据
	 * 
	 * @param type
	 * @param current
	 * @param count
	 */
	public void updatePackagerNum(final String type, final String current, final String count, final String time) {
		final ContentValues cv = new ContentValues();
		cv.put(TYPE, type);
		cv.put(CURRENT, current);
		cv.put(COUNT, count);
		cv.put(TIME, time);
		String[] args = new String[]{type};
		dbHelper.getWritableDatabase().update(PACKAGETNUM_TABLE, cv, UPDATE_TYPE, args);
	}
	
	/**
	 * 根据类型获取数据
	 * 
	 * @param type
	 * @return
	 */
	public Cursor getPackagetNumByType(final String type) {
		String[] args = new String[]{type};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_PACKAGETNUM_BY_TYPE, args);
	}
	
	/**
	 * 删除数据
	 */
	public void deletePackagetNum() {
		dbHelper.getWritableDatabase().execSQL(DELETE_PACKAGETNUM);
	}
	/***************************************数据包  结束********************************************************/
	
	
	/***************************************推土  开始********************************************************/
	/**
	 * 插入一条推土任务
	 * 
	 * @param block
	 * @param alat
	 * @param alon
	 * @param aAlt
	 */
	public void addBulldozTask(final String block, final double alat, final double alon) {
		final ContentValues cv = new ContentValues();
		cv.put(BLOCK, block);
		cv.put(LAT, alat);
		cv.put(LON, alon);
		dbHelper.getWritableDatabase().insert(BULLDOZTASKT_ABLE, null, cv);
	}
	
	/**
	 * 根据区块获取推土任务
	 * 
	 * @param block
	 * @return
	 */
	public Cursor getBulldozTaskByBlock(String block) {
		String[] args = new String[]{block};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_BULLDOZTASK_BY_BLOCK, args);
	}
	
	/**
	 * 获取所有推土任务
	 * 
	 * @return
	 */
	public Cursor getBulldozTask() {
		return dbHelper.getReadableDatabase().rawQuery(SELECT_BULLDOZTASK_ALL, null);
	}
	
	/**
	 * 删除推土任务
	 * 
	 */
	public void deleteBulledozTask() {
		dbHelper.getWritableDatabase().execSQL(DELETE_BULLDOZTASK);
	}
	
	/**
	 * 查找某一区域的点，并以经纬度排序
	 * 
	 * @param zoom
	 * @param left
	 * @param right
	 * @param top
	 * @param bottom
	 * @return
	 */
	public Cursor getBulldozTaskListNotHiddenCursor(final String block, final int zoom, final double left, final double right, final double top, final double bottom) {
		final String[] args = {block, Double.toString(left), Double.toString(right), Double.toString(bottom), Double.toString(top)};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_BULLDOZTASK, args);
	}
	
	/**
	 * 插入一个下地口
	 * 
	 * @param block
	 * @param alat
	 * @param alon
	 * @param isupload
	 */
	public void addBulldozPoint(final String block, final double alat, final double alon, final int isupload) {
		final ContentValues cv = new ContentValues();
		cv.put(BLOCK, block);
		cv.put(LAT, alat);
		cv.put(LON, alon);
		cv.put(ISUPLOAD, isupload);
		dbHelper.getWritableDatabase().insert(BULLDOZPOINT_TABLE, null, cv);
	}
	
	/**
	 * 根据区块获取下地口
	 * 
	 * @param block
	 * @return
	 */
	public Cursor getBulldozPointByBlock(String block) {
		String[] args = new String[]{block};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_BULLDOZPOINT_BY_BLOCK, args);
	}
	
	/**
	 * 获取所有下地口
	 * 
	 * @return
	 */
	public Cursor getBulldozPoint() {
		return dbHelper.getReadableDatabase().rawQuery(SELECT_BULLDOZPOINT_ALL, null);
	}
	
	/**
	 * 更新下地口
	 * 
	 * @param block
	 * @param alat
	 * @param alon
	 * @param isupload
	 */
	public void updateBlldozPoint(final String block, final double alat, final double alon, final int isupload) {
		String[] args = new String[]{String.valueOf(isupload), block, String.valueOf(alon), String.valueOf(alat)};
		dbHelper.getWritableDatabase().execSQL(UPDATE_BULLDOZPOINT, args);
	}
	
	public void deleteBulldozPoint() {
		dbHelper.getWritableDatabase().execSQL(DELETE_BULLDOZPOINT);
	}
	
	/**
	 * 查找某一区域的点，并以经纬度排序
	 * 
	 * @param zoom
	 * @param left
	 * @param right
	 * @param top
	 * @param bottom
	 * @return
	 */
	public Cursor getBulldozPointListNotHiddenCursor(final int zoom, final double left, final double right, final double top, final double bottom) {
		final String[] args = {Double.toString(left), Double.toString(right), Double.toString(bottom), Double.toString(top)};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_BULLDOZPOINT, args);
	}
	/************************************************推土  结束************************************************/
	
	/************************************************钻井下药炮点 开始******************************************/
	
	/**
	 * 插入一条钻井下药炮点
	 * 
	 * @param stationNo
	 * @param lineNo
	 * @param spointNo
	 * @param aLat
	 * @param aLon
	 * @param aAlt
	 * @param wellnum
	 * @param desWellDepth
	 * @param bombWeight
	 * @param detonator
	 * @param hidden
	 * @param isDone
	 */
	public void addDrillPoint(final String stationNo, final String lineNo, final String spointNo, 
			final double aLat, final double aLon, final double aAlt,
			final double wellnum, final double desWellDepth, final float bombWeight, final double detonator, final int hidden, final int isDone) {
		final ContentValues cv = new ContentValues();
		cv.put(STATIONNO, stationNo);
		cv.put(LINENO, lineNo);
		cv.put(SPOINTNO, spointNo);
		cv.put(LAT, aLat);
		cv.put(LON, aLon);
		cv.put(HEIGHT, aAlt);
		cv.put(WELLNUM, wellnum);
		cv.put(DESWELLDEPHT, desWellDepth);
		cv.put(BOMBWEIGHT, bombWeight);
		cv.put(DETONATOR, detonator);
		cv.put(HIDDEN, hidden);
		cv.put(ISDONE, isDone);
		cv.put(TIME, "");
		dbHelper.getWritableDatabase().insert(DRILLPOINT_TABLE, null, cv);
	}

	/**
	 * 更新钻井下要炮点
	 * 
	 * @param id
	 * @param stationNo
	 * @param lineNo
	 * @param spointNo
	 * @param aLat
	 * @param aLon
	 * @param aAlt
	 * @param wellnum
	 * @param desWellDepth
	 * @param bombWeight
	 * @param detonator
	 * @param hidden
	 * @param isDone
	 * @param time
	 */
	public void updateDrillPoint(final int id,final String stationNo, final String lineNo, final String spointNo, 
			final double aLat, final double aLon, final double aAlt,
			final double wellnum, final double desWellDepth, final float bombWeight, final double detonator, 
			final int hidden, final int isDone, final String time) {
		final ContentValues cv = new ContentValues();
		cv.put(STATIONNO, stationNo);
		cv.put(LINENO, lineNo);
		cv.put(SPOINTNO, spointNo);
		cv.put(LAT, aLat);
		cv.put(LON, aLon);
		cv.put(HEIGHT, aAlt);
		cv.put(WELLNUM, wellnum);
		cv.put(DESWELLDEPHT, desWellDepth);
		cv.put(BOMBWEIGHT, bombWeight);
		cv.put(DETONATOR, detonator);
		cv.put(HIDDEN, hidden);
		cv.put(ISDONE, isDone);
		cv.put(TIME, time);
		final String[] args = {Integer.toString(id)};
		dbHelper.getWritableDatabase().update(DRILLPOINT_TABLE, cv, UPDATE_POINTID, args);
	}
	
	/**
	 * 根据桩号搜索点
	 * 
	 * @param stationNo
	 * @return
	 */
	public Cursor getDrillCursorByStation(String stationNo) {
		final String[] args = {stationNo};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_DRILLPOINT_BY_STATION, args);
	}
	
	/**
	 * 根据id搜索点
	 * 
	 * @param pointid
	 * @return
	 */
	public Cursor getDrillCursorByid(int pointid) {
		final String[] args = {String.valueOf(pointid)};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_DRILLPOINT_BY_POINTID, args);
	}
	
	/**
	 * 获取所有钻井下药炮点
	 * 
	 * @return
	 */
	public Cursor getAllDrill() {
		return dbHelper.getReadableDatabase().rawQuery(SELECT_DRILLPOINT_ALL, null);
	}
	
	/**
	 * 获取所有已完成钻井下药炮点
	 */
	public Cursor getDrillisDone() {
		final String[] args = {"1"};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_DRILLPOINT_IS_ALLDONE, args);
	}
	
	/**
	 * 获取所有未完成钻井下药炮点
	 */
	public Cursor getDrillunDone() {
		final String[] args = {"0"};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_DRILLPOINT_IS_ALLDONE, args);
	}
	
	/**
	 * 根据关键词获取钻井下药炮点
	 * 
	 * @param keyword
	 * @return
	 */
	public Cursor getDrillbyKeyword(String keyword) {
		final String[] args = {"%" + keyword + "%"};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_DRILLPOINT_BY_KEYWORD, args);
	}
	
	/**
	 * 根据stationNo删除炮点
	 * 
	 * @param id
	 */
	public void deleteDrillPoint(final int id) {
		final String[] args = {String.valueOf(id)};
		dbHelper.getWritableDatabase().delete(DRILLPOINT_TABLE, DELETE_DRILLPOINT_BY_POINTID, args);
	}
	
	public void DeleteAllDrillPoint() {
		dbHelper.getWritableDatabase().execSQL(DELETE_DRILLPOINT);
	}
	
	/**
	 * 查找某一区域的点，并以经纬度排序
	 * 
	 * @param zoom
	 * @param left
	 * @param right
	 * @param top
	 * @param bottom
	 * @return
	 */
	public Cursor getDrillListNotHiddenCursor(final int zoom, final double left, final double right, final double top, final double bottom) {
		final String[] args = {Double.toString(left), Double.toString(right), Double.toString(bottom), Double.toString(top)};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_DRILLPOINT, args);
	}
	
	/************************************************钻井下药炮点 结束******************************************/
	
	/************************************************井炮炮点 开始********************************************/
	
	/**
	 *  插入一条井炮炮点
	 * 
	 * @param stationNo
	 * @param lineNo
	 * @param spointNo
	 * @param aLat
	 * @param aLon
	 * @param aAlt
	 * @param hidden
	 * @param isDone
	 */
	public void addShotPoint(final String stationNo, final String lineNo, final String spointNo, 
			final double aLat, final double aLon, final double aAlt, final int hidden, final int isDone) {
		final ContentValues cv = new ContentValues();
		cv.put(STATIONNO, stationNo);
		cv.put(LINENO, lineNo);
		cv.put(SPOINTNO, spointNo);
		cv.put(LAT, aLat);
		cv.put(LON, aLon);
		cv.put(HEIGHT, aAlt);
		cv.put(HIDDEN, hidden);
		cv.put(ISDONE, isDone);
		cv.put(TIME, "");
		dbHelper.getWritableDatabase().insert(SHOTPOINT_TABLE, null, cv);
	}

	/**
	 * 更新井炮炮点
	 * 
	 * @param id
	 * @param stationNo
	 * @param lineNo
	 * @param spointNo
	 * @param aLat
	 * @param aLon
	 * @param aAlt
	 * @param hidden
	 * @param isDone
	 * @param time
	 */
	public void updateShotPoint(final int id, final String stationNo, final String lineNo, final String spointNo, 
			final double aLat, final double aLon, final double aAlt, final int hidden, final int isDone, final String time) {
		final ContentValues cv = new ContentValues();
		cv.put(STATIONNO, stationNo);
		cv.put(LINENO, lineNo);
		cv.put(SPOINTNO, spointNo);
		cv.put(LAT, aLat);
		cv.put(LON, aLon);
		cv.put(HEIGHT, aAlt);
		cv.put(HIDDEN, hidden);
		cv.put(ISDONE, isDone);
		cv.put(TIME, time);
		final String[] args = {Integer.toString(id)};
		dbHelper.getWritableDatabase().update(SHOTPOINT_TABLE, cv, UPDATE_POINTID, args);
	}
	
	/**
	 * 根据stationNo删除炮点
	 * 
	 * @param id
	 */
	public void deleteShot(final int id) {
		final Double[] args = {Double.valueOf(id)};
		dbHelper.getWritableDatabase().execSQL(DELETE_SHOTPOINT_BY_POINTID, args);
	}
	
	public void DeleteAllShot() {
		dbHelper.getWritableDatabase().execSQL(DELETE_SHOTPOINT);
	}
	
	/**
	 * 根据桩号搜索点
	 * 
	 * @param stationNo
	 * @return
	 */
	public Cursor getShotCursorBYStation(String stationNo) {
		final String[] args = {stationNo};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_SHOTPOINT_BY_STATION, args);
	}
	
	/**
	 * 根据id搜索点
	 * 
	 * @param pointid
	 * @return
	 */
	public Cursor getShotCursorByid(int pointid) {
		final String[] args = {String.valueOf(pointid)};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_SHOTPOINT_BY_POINTID, args);
	}
	
	/**
	 * 获取所有井炮炮点
	 * 
	 * @return
	 */
	public Cursor getAllShot() {
		return dbHelper.getReadableDatabase().rawQuery(SELECT_SHOTPOINT_ALL, null);
	}
	
	/**
	 * 获取所有未完成井炮炮点
	 * 
	 * @return
	 */
	public Cursor getShotUnDone() {
		final String[] args = {"0"};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_SHOTPOINT_IS_ALLDONE, args);
	}
	
	/**
	 * 获取所有已完成井炮炮点
	 */
	public Cursor getShotisDone() {
		final String[] args = {String.valueOf("1")};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_SHOTPOINT_IS_ALLDONE, args);
	}
	
	/**
	 * 根据关键词获取井炮炮点
	 * 
	 * @param keyword
	 * @return
	 */
	public Cursor getShotbyKeyword(String keyword) {
		final String[] args = {"%" + keyword + "%"};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_SHOTPOINT_BY_KEYWORD, args);
	}

	/**
	 * 查找某一区域的点，并以经纬度排序
	 * 
	 * @param zoom
	 * @param left
	 * @param right
	 * @param top
	 * @param bottom
	 * @return
	 */
	public Cursor getShotListNotHiddenCursor(final int zoom, final double left, final double right, final double top, final double bottom) {
		final String[] args = {Double.toString(left), Double.toString(right), Double.toString(bottom), Double.toString(top)};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_SHOTPOIINT, args);

	}
	
	/************************************************井炮炮点 结束******************************************/

	/************************************************钻井下药记录 开始******************************************/
	
	/**
	 * 插入一条钻井下药记录
	 * 
	 * @param stationNo
	 * @param lineNo
	 * @param spointNo
	 * @param receivetime
	 * @param welllithology
	 * @param aLat
	 * @param aLon
	 * @param wellnum
	 * @param drilldepth
	 * @param bombdepth
	 * @param bombWeight
	 * @param detonator
	 * @param bombid
	 * @param detonatorid
	 * @param remark
	 * @param image1
	 * @param image2
	 * @param image3
	 * @param drilltime
	 * @param isupload
	 */
	public void addDrillRecord(final String stationNo, final String lineNo, final String spointNo, final String receivetime,final String name,
			final String welllithology, final String aLat, final String aLon, final String wellnum, final String drilldepth,final String tel,
			final String bombdepth, final String bombWeight, final String detonator, final String bombid, final String detonatorid,final int status,
			final String remark, final String image1, final String image2, final String image3, final String drilltime, final String isupload,final String video) {
		final ContentValues cv = new ContentValues();
		cv.put(STATIONNO, stationNo);
		cv.put(LINENO, lineNo);
		cv.put(SPOINTNO, spointNo);
		cv.put(RECEIVETIME, receivetime);
		cv.put(WELLLITHOLOGY, welllithology);
		cv.put(LAT, aLat);
		cv.put(LON, aLon);
		cv.put(WELLNUM, wellnum);
		cv.put(DRILLDEPTH, drilldepth);
		cv.put(BOMBDEPTH, bombdepth);
		cv.put(BOMBWEIGHT, bombWeight);
		cv.put(DETONATOR, detonator);
		cv.put(BOMBID, bombid);
		cv.put(DETONATORID, detonatorid);
		cv.put(REMARK, remark);
		cv.put(IMAGE1, image1);
		cv.put(IMAGE2, image2);
		cv.put(IMAGE3, image3);
		cv.put(DRILLTIME, drilltime);
		cv.put(ISUPLOAD, isupload);
		cv.put(NAME,name);
		cv.put(TEL,tel);
		cv.put(STATUS,status);
		cv.put(VIDEO,video);
		dbHelper.getWritableDatabase().insert(DRILLRECORD_TABLE, null, cv);
	}

	/**
	 * 更新钻井下药记录
	 * 
	 * @param stationNo
	 * @param lineNo
	 * @param spointNo
	 * @param receivetime
	 * @param welllithology
	 * @param aLat
	 * @param aLon
	 * @param wellnum
	 * @param drilldepth
	 * @param bombdepth
	 * @param bombWeight
	 * @param detonator
	 * @param bombid
	 * @param detonatorid
	 * @param remark
	 * @param image1
	 * @param image2
	 * @param image3
	 * @param drilltime
	 * @param isupload
	 */
	public void updateDrillRecord(final String stationNo, final String lineNo, final String spointNo, final String receivetime,final String name,
			final String welllithology, final String aLat, final String aLon, final String wellnum, final String drilldepth,final String tel,
			final String bombdepth, final String bombWeight, final String detonator, final String bombid, final String detonatorid,final int status,
			final String remark, final String image1, final String image2, final String image3, final String drilltime, final String isupload,final String video) {
		final ContentValues cv = new ContentValues();
		cv.put(STATIONNO, stationNo);
		cv.put(LINENO, lineNo);
		cv.put(SPOINTNO, spointNo);
		cv.put(RECEIVETIME, receivetime);
		cv.put(WELLLITHOLOGY, welllithology);
		cv.put(LAT, aLat);
		cv.put(LON, aLon);
		cv.put(WELLNUM, wellnum);
		cv.put(DRILLDEPTH, drilldepth);
		cv.put(BOMBDEPTH, bombdepth);
		cv.put(BOMBWEIGHT, bombWeight);
		cv.put(DETONATOR, detonator);
		cv.put(BOMBID, bombid);
		cv.put(DETONATORID, detonatorid);
		cv.put(REMARK, remark);
		cv.put(IMAGE1, image1);
		cv.put(IMAGE2, image2);
		cv.put(IMAGE3, image3);
		cv.put(DRILLTIME, drilltime);
		cv.put(ISUPLOAD, isupload);
		cv.put(NAME,name);
		cv.put(TEL,tel);
		cv.put(STATUS,status);
		cv.put(VIDEO,video);
		final String[] args = {stationNo};
		dbHelper.getWritableDatabase().update(DRILLRECORD_TABLE, cv, UPDATE_STATTION, args);
	}
	
	/**
	 * 根据桩号搜索点
	 * 
	 * @param stationNo
	 * @return
	 */
	public Cursor getDrillRecordCursorByStation(String stationNo) {
		final String[] args = {stationNo};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_DRILLRECORD_BY_STATION, args);
	}
	
	/**
	 * 获取未上传的钻井记录
	 * 
	 * @param stationNo
	 * @return
	 */
	public Cursor getDrillRecordCursorUnUpload() {
		final String[] args = {"0"};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_DRILLRECORD_BY_UNUPLOAD, args);
	}
	
	/**
	 * 根据stationNo删除炮点
	 * 
	 * @param station
	 */
	public void deleteDrillRecord(final String station) {
		final String[] args = {station};
		dbHelper.getWritableDatabase().delete(DRILLRECORD_TABLE, DELETE_DRILLRECORD_BY_STATION, args);
	}
	
	public void DeleteAllDrillRecord() {
		dbHelper.getWritableDatabase().execSQL(DELETE_DRILLRECORD);
	}
	
	/**
	 * 查找某一区域的点，并以经纬度排序
	 * 
	 * @param zoom
	 * @param left
	 * @param right
	 * @param top
	 * @param bottom
	 * @return
	 */
	public Cursor getDrillRecordListNotHiddenCursor(final int zoom, final double left, final double right, final double top, final double bottom) {
		final String[] args = {Double.toString(left), Double.toString(right), Double.toString(bottom), Double.toString(top)};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_DRILLRECORD, args);
	}
	
	/************************************************钻井下药记录 结束******************************************/
	
	/************************************************每日任务  开始 ********************************************/
	/**
	 * 插入每日任务
	 * 
	 * @param pname
	 * @param time
	 * @param status
	 */
	public void addDailyTask(final String pname, final String time, final int status, final String type) {
		final ContentValues cv = new ContentValues();
		cv.put(PNAME, pname);
		cv.put(TIME, time);
		cv.put(STATUS, status);
		cv.put(TYPE, type);
		dbHelper.getWritableDatabase().insert(DAILY_TASK, null, cv);
	}
	
	/**
	 * 更新每日任务
	 * 
	 * @param pname
	 * @param time
	 * @param status
	 */
	public void updateDailyTask(final String pname, final String time, final int status, final String type) {
		final ContentValues cv = new ContentValues();
		cv.put(PNAME, pname);
		cv.put(TIME, time);
		cv.put(STATUS, status);
		cv.put(TYPE, type);
		String[] args = new String[]{time};
		dbHelper.getWritableDatabase().update(DAILY_TASK, cv, TIME, args);
	}
	
	/**
	 * 获取所有任务
	 * 
	 * @return
	 */
	public Cursor getDailyTask(final String type) {
		final String[] args = {type};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_DAILTTASK, args);
	}
	
	/**
	 * 删除所有任务
	 */
	public void deleteDailyTask(final String type) {
		final String[] args = {type};
		dbHelper.getWritableDatabase().execSQL(DELETE_DAILYTASK, args);
	}
	
	/**
	 * 删除每日任务
	 */
	public void deleteDailyTaskByTime(final String time, final String type) {
		final String[] args = {time, type};
		dbHelper.getWritableDatabase().execSQL(DELETE_DAILYTASK_BY_DAILY, args);
	}
	
	/**
	 * 插入每日任务
	 * 
	 * @param pname
	 * @param time
	 * @param status
	 */
	public void addDailyPoints(final String time, final List<String> stations, final String type) {
		if (stations != null && stations.size() > 0) {
			for (String station : stations) {
				addDailyPoint(time, station, type);
			}
		}
	}
	
	/**
	 * 插入每日任务炮点
	 * 
	 * @param pname
	 * @param time
	 * @param status
	 */
	public void addDailyPoint(final String time, final String station, final String type) {
		final ContentValues cv = new ContentValues();
		cv.put(TIME, time);
		cv.put(STATIONNO, station);
		cv.put(TYPE, type);
		dbHelper.getWritableDatabase().insert(DAILY_POINT, null, cv);
	}
	
	/**
	 * 获取所有任务炮点
	 * 
	 * @return
	 */
	public Cursor getDailyPoint(final String type) {
		final String[] args = {type};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_DAILTTPOINT, args);
	}
	
	/**
	 * 获取每日已完成井炮炮点
	 * 
	 * @param time
	 * @return
	 */
	public Cursor getDailyPointShotDone(final String time) {
		final String[] args = {time};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_DAILYPOINT_SHOT_DONE, args);
	}
	
	/**
	 * 获取每日已完成钻井炮点
	 * 
	 * @param time
	 * @return
	 */
	public Cursor getDailyPointDrillDone(final String time) {
		final String[] args = {time};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_DAILYPOINT_DRILL_DONE, args);
	}
	
	/**
	 * 根据日期获取每日任务炮点
	 * 
	 * @param time
	 * @return
	 */
	public Cursor getDailyPointByTime(final String time, final String type) {
		final String[] args = {time, type};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_DAILYPOINT_BY_DAILY, args);
	}
	
	/**
	 * 删除每日任务炮点
	 */
	public void deleteDailyPoint(final String type) {
		final String[] args = {type};
		dbHelper.getWritableDatabase().execSQL(DELETE_DAILYPOINT, args);
	}
	
	/**
	 * 根据日期删除每日任务炮点
	 * 
	 * @param time
	 */
	public void deleteDailyPointByTime(final String time, final String type) {
		final String[] args = {time, type};
		dbHelper.getWritableDatabase().execSQL(DELETE_DAILYPOINT_BY_DAILY, args);
	}
	
	/************************************************每日任务  结束 ********************************************/
	
	/************************************************车辆 开始 ********************************************/
	
	/**
	 * 插入车辆数据
	 * 
	 * @param carnum
	 * @param driver
	 * @param destination
	 * @param task
	 * @param start_place
	 * @param start_time
	 * @param start_peoplenum
	 * @param start_lon
	 * @param start_lat
	 * @param start_remark
	 * @param start_isUpload
	 * @param estimated_arrived_time
	 * @param arrived_place
	 * @param arrived_time
	 * @param arrived_peoplenum
	 * @param arrived_lon
	 * @param arrived_lat
	 * @param arrived_remark
	 * @param arrived_isUpload
	 * @param back_place
	 * @param back_time
	 * @param back_peoplenum
	 * @param back_lon
	 * @param back_lat
	 * @param back_remark
	 * @param back_isUpload
	 * @param estimated_return_time
	 * @param end_place
	 * @param end_time
	 * @param end_peoplenum
	 * @param end_lon
	 * @param end_lat
	 * @param end_remark
	 * @param end_isUpload
	 */
	public void addTrave(final String carnum, final String driver, final String destination, final String task,
			final String start_place, final String start_time, final String start_peoplenum, final String start_lon, final String start_lat, final String start_remark, final String start_isUpload, final String estimated_arrived_time,
			final String arrived_place, final String arrived_time, final String arrived_peoplenum, final String arrived_lon, final String arrived_lat, final String arrived_remark, final String arrived_isUpload,
			final String back_place, final String back_time, final String back_peoplenum, final String back_lon, final String back_lat, final String back_remark, final String back_isUpload, final String estimated_return_time,
			final String end_place, final String end_time, final String end_peoplenum, final String end_lon, final String end_lat, final String end_remark, final String end_isUpload) {
		final ContentValues cv = new ContentValues();
		
		cv.put(CARNUM, carnum);
		cv.put(DRIVER, driver);
		cv.put(DESTINATION, destination);
		cv.put(TASK, task);
		
		cv.put(START_PLACE, start_place);
		cv.put(START_TIME, start_time);
		cv.put(START_PEOPLE, start_peoplenum);
		cv.put(START_LON, start_lon);
		cv.put(START_LAT, start_lat);
		cv.put(START_REMARK, start_remark);
		cv.put(START_ISUPLOAD, start_isUpload);
		cv.put(ESTIMATED_ARRIVED_TIME, estimated_arrived_time);
		
		cv.put(ARRIVED_PLACE, arrived_place);
		cv.put(ARRIVED_TIME, arrived_time);
		cv.put(ARRIVED_PEOPLE, arrived_peoplenum);
		cv.put(ARRIVED_LON, arrived_lon);
		cv.put(ARRIVED_LAT, arrived_lat);
		cv.put(ARRIVED_REMARK, arrived_remark);
		cv.put(ARRIVED_ISUPLOAD, arrived_isUpload);
		
		cv.put(BACK_PLACE, back_place);
		cv.put(BACK_TIME, back_time);
		cv.put(BACK_PEOPLE, back_peoplenum);
		cv.put(BACK_LON, back_lon);
		cv.put(BACK_LAT, back_lat);
		cv.put(BACK_REMARK, back_remark);
		cv.put(BACK_ISUPLOAD, back_isUpload);
		cv.put(ESTMATED_RETURN_TIME, estimated_return_time);
		
		cv.put(END_PLACE, end_place);
		cv.put(END_TIME, end_time);
		cv.put(END_PEOPLE, end_peoplenum);
		cv.put(END_LON, end_lon);
		cv.put(END_LAT, end_lat);
		cv.put(END_REMARK, end_remark);
		cv.put(END_ISUPLOAD, end_isUpload);
		
		dbHelper.getWritableDatabase().insert(TRAVE, null, cv);
	}
	
	/**
	 * 更新车辆
	 * 
	 * @param id
	 * @param carnum
	 * @param driver
	 * @param destination
	 * @param task
	 * @param start_place
	 * @param start_time
	 * @param start_peoplenum
	 * @param start_lon
	 * @param start_lat
	 * @param start_remark
	 * @param start_isUpload
	 * @param estimated_arrived_time
	 * @param arrived_place
	 * @param arrived_time
	 * @param arrived_peoplenum
	 * @param arrived_lon
	 * @param arrived_lat
	 * @param arrived_remark
	 * @param arrived_isUpload
	 * @param back_place
	 * @param back_time
	 * @param back_peoplenum
	 * @param back_lon
	 * @param back_lat
	 * @param back_remark
	 * @param back_isUpload
	 * @param estimated_return_time
	 * @param end_place
	 * @param end_time
	 * @param end_peoplenum
	 * @param end_lon
	 * @param end_lat
	 * @param end_remark
	 * @param end_isUpload
	 */
	public void updateTrave(final String id, final String carnum, final String driver, final String destination, final String task,
			final String start_place, final String start_time, final String start_peoplenum, final String start_lon, final String start_lat, final String start_remark, final String start_isUpload, final String estimated_arrived_time, 
			final String arrived_place, final String arrived_time, final String arrived_peoplenum, final String arrived_lon, final String arrived_lat, final String arrived_remark, final String arrived_isUpload,
			final String back_place, final String back_time, final String back_peoplenum, final String back_lon, final String back_lat, final String back_remark, final String back_isUpload, final String estimated_return_time,
			final String end_place, final String end_time, final String end_peoplenum, final String end_lon, final String end_lat, final String end_remark, final String end_isUpload) {
		
		final ContentValues cv = new ContentValues();
		
		cv.put(CARNUM, carnum);
		cv.put(DRIVER, driver);
		cv.put(DESTINATION, destination);
		cv.put(TASK, task);
		
		cv.put(START_PLACE, start_place);
		cv.put(START_TIME, start_time);
		cv.put(START_PEOPLE, start_peoplenum);
		cv.put(START_LON, start_lon);
		cv.put(START_LAT, start_lat);
		cv.put(START_REMARK, start_remark);
		cv.put(START_ISUPLOAD, start_isUpload);
		cv.put(ESTIMATED_ARRIVED_TIME, estimated_arrived_time);
		
		cv.put(ARRIVED_PLACE, arrived_place);
		cv.put(ARRIVED_TIME, arrived_time);
		cv.put(ARRIVED_PEOPLE, arrived_peoplenum);
		cv.put(ARRIVED_LON, arrived_lon);
		cv.put(ARRIVED_LAT, arrived_lat);
		cv.put(ARRIVED_REMARK, arrived_remark);
		cv.put(ARRIVED_ISUPLOAD, arrived_isUpload);
		
		cv.put(BACK_PLACE, back_place);
		cv.put(BACK_TIME, back_time);
		cv.put(BACK_PEOPLE, back_peoplenum);
		cv.put(BACK_LON, back_lon);
		cv.put(BACK_LAT, back_lat);
		cv.put(BACK_REMARK, back_remark);
		cv.put(BACK_ISUPLOAD, back_isUpload);
		cv.put(ESTMATED_RETURN_TIME, estimated_return_time);
		
		cv.put(END_PLACE, end_place);
		cv.put(END_TIME, end_time);
		cv.put(END_PEOPLE, end_peoplenum);
		cv.put(END_LON, end_lon);
		cv.put(END_LAT, end_lat);
		cv.put(END_REMARK, end_remark);
		cv.put(END_ISUPLOAD, end_isUpload);
		
		final String[] args = {id};
		dbHelper.getWritableDatabase().update(TRAVE, cv, UPDATE_ID, args);
	}
	
	/**
	 * 获取最后一条数据
	 * 
	 * @return
	 */
	public Cursor getLastTrave() {
		return dbHelper.getReadableDatabase().rawQuery(SELECT_TRAVE_LAST, null);
	}
	
	public Cursor getTraveById(final String id) {
		final String[] args = {id};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_TRAVE_BY_ID, args);
	}
	public Cursor getTraveByStarttime(final String time) {
		final String[] args = {time};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_TRAVE_BY_STARTTIME, args);
	}
	
	public void clearTrave() {
		dbHelper.getWritableDatabase().execSQL(DELETE_TRAVE);
	}
	
	/**
	 * 增加车牌号码
	 * 
	 * @param carnum
	 */
	public void AddCarNum(final String carnum) {
		final ContentValues cv = new ContentValues();
		cv.put(CARNUM, carnum);
		dbHelper.getWritableDatabase().insert(CARS, null, cv);
	}
	
	/**
	 * 获取所有车牌号
	 * 
	 * @return
	 */
	public Cursor getAllCarNum() {
		return dbHelper.getReadableDatabase().rawQuery(SELECT_CARNUM, null);
	}
	
	/**
	 * 删除所有车牌
	 */
	public void deleteAllCarNum() {
		dbHelper.getWritableDatabase().execSQL(DELETE_CARS);
	}
	
	public void deleteByCarNum(final String carnum) {
		final String[] args = {carnum};
		dbHelper.getWritableDatabase().execSQL(DELETE_CAR_BY_CAENUM, args);
	}
	/************************************************车辆 结束 ********************************************/
	
	/************************************************车辆钥匙管理 开始 ********************************************/
	
	/**
	 * 插入车辆钥匙管理
	 * 
	 * @param carnum
	 * @param driver
	 * @param start_time
	 * @param start_isUpload
	 * @param back_time
	 * @param back_isUpload
	 */
	public void addCarKey(final String carnum, final String driver, final String start_time,  final String start_isUpload,
			final String back_time, final String back_isUpload) {
		final ContentValues cv = new ContentValues();
		
		cv.put(CARNUM, carnum);
		cv.put(DRIVER, driver);
		cv.put(START_TIME, start_time);
		cv.put(START_ISUPLOAD, start_isUpload);
		cv.put(BACK_TIME, back_time);
		cv.put(BACK_ISUPLOAD, back_isUpload);
		
		dbHelper.getWritableDatabase().insert(CARKEYS, null, cv);
	}
	
	/**
	 * 更新车辆钥匙管理
	 * 
	 * @param id
	 * @param carnum
	 * @param driver
	 * @param start_time
	 * @param start_isUpload
	 * @param back_time
	 * @param back_isUpload
	 */
	public void updateCarKey(final String id, final String carnum, final String driver, 
			final String start_time, final String start_isUpload, final String back_time,final String back_isUpload) {
		
		final ContentValues cv = new ContentValues();
		
		cv.put(CARNUM, carnum);
		cv.put(DRIVER, driver);
		cv.put(START_TIME, start_time);
		cv.put(START_ISUPLOAD, start_isUpload);
		cv.put(BACK_TIME, back_time);
		cv.put(BACK_ISUPLOAD, back_isUpload);
		
		final String[] args = {id};
		dbHelper.getWritableDatabase().update(CARKEYS, cv, UPDATE_ID, args);
	}
	
	/**
	 * 获取最后一条车辆钥匙管理
	 * 
	 * @return
	 */
	public Cursor getLastCarkey() {
		return dbHelper.getReadableDatabase().rawQuery(SELECT_CARKEY_LAST, null);
	}
	
	public Cursor getCarkeyByStarttime(final String time) {
		final String[] args = {time};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_CARKEY_BY_STARTTIME, args);
	}
	
	public void clearCarkeys() {
		dbHelper.getWritableDatabase().execSQL(DELETE_CARKEY);
	}
	/************************************************车辆钥匙管理 结束 ********************************************/
	
	/************************************************搜索历史  开始 ********************************************/
	
	/**
	 * 插入搜索历史
	 * 
	 * @param type
	 * @param history
	 */
	public void addHistory(final String type, final String history, final String time) {
		final ContentValues cv = new ContentValues();
		cv.put(TYPE, type);
		cv.put(HISTORY, history);
		cv.put(TIME, time);
		dbHelper.getWritableDatabase().insert(SEARCH_HISTORY, null, cv);
	}
	
	/**
	 * 查询所有搜索历史
	 * 
	 * @return
	 */
	public Cursor getAllHistory() {
		return dbHelper.getReadableDatabase().rawQuery(SELECT_HISTORY, null);
	}
	
	/**
	 * 根据类型获取搜索历史
	 * 
	 * @param type
	 * @return
	 */
	public Cursor getHistoryByType(final String type) {
		final String[] args = {type};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_HISTORY_BY_TYPE, args);
	}
	
	/**
	 * 删除所有搜索历史
	 */
	public void cancelHistory() {
		dbHelper.getWritableDatabase().execSQL(DELETE_HISTORY);
	}
	
	/**
	 * 根据类型删除搜索历史
	 * 
	 * @param type
	 */
	public void cancelHistoryByType(final String type) {
		final String[] args = {type};
		dbHelper.getWritableDatabase().execSQL(DELETE_HISTORY_BY_TYPE, args);
	}
	
	/**
	 * 根据历史删除搜索历史
	 * 
	 * @param history
	 */
	public void cancelHistoryByHistory(final String history) {
		final String[] args = {history};
		dbHelper.getWritableDatabase().execSQL(DELETE_HISTORY_BY_HISTORY, args);
	}
	
	/************************************************搜索历史  结束 ********************************************/
	
	/************************************************排列炮点 开始********************************************/
	
	/**
	 * 插入一条排列炮点
	 * 
	 * @param stationNo
	 * @param lineNo
	 * @param spointNo
	 * @param aLat
	 * @param aLon
	 */
	public void addArrangePoint(final String stationNo, final String lineNo, final String spointNo, 
			final double aLat, final double aLon,final String time,final String remark,final int hidden,final int isDone) {
		final ContentValues cv = new ContentValues();
		cv.put(STATIONNO, stationNo);
		cv.put(LINENO, lineNo);
		cv.put(SPOINTNO, spointNo);
		cv.put(LAT, aLat);
		cv.put(LON, aLon);
		cv.put(TIME, time);
		cv.put(REMARK, remark);
		cv.put(HIDDEN, hidden);
		cv.put(ISDONE, isDone);
		dbHelper.getWritableDatabase().insert(ARRANGEPOINT_TABLE, null, cv);
	}

	/**
	 * 更新排列炮点
	 * 
	 * @param id
	 * @param stationNo
	 * @param lineNo
	 * @param spointNo
	 * @param aLat
	 * @param aLon
	 */
	public void updateArrangePoint(final int id, final String stationNo, final String lineNo, final String spointNo, 
			final double aLat, final double aLon,final String time,final String remark,final int hidden,final int isDone) {
		final ContentValues cv = new ContentValues();
		cv.put(STATIONNO, stationNo);
		cv.put(LINENO, lineNo);
		cv.put(SPOINTNO, spointNo);
		cv.put(LAT, aLat);
		cv.put(LON, aLon);
		cv.put(TIME, time);
		cv.put(REMARK, remark);
		cv.put(HIDDEN, hidden);
		cv.put(ISDONE, isDone);
		final String[] args = {Integer.toString(id)};
		dbHelper.getWritableDatabase().update(ARRANGEPOINT_TABLE, cv, UPDATE_POINTID, args);
	}
	
	/**
	 * 根据stationNo删除排列炮点
	 * 
	 * @param id
	 */
	public void deleteArrange(final int id) {
		final Double[] args = {Double.valueOf(id)};
		dbHelper.getWritableDatabase().execSQL(DELETE_ARRANGEPOINT_BY_POINTID, args);
	}
	
	/**
	 * 删除所有排列炮点
	 */
	public void DeleteAllArrange() {
		dbHelper.getWritableDatabase().execSQL(DELETE_ARRANGEPOINT);
	}
	
	/**
	 * 根据桩号搜索点
	 * 
	 * @param stationNo
	 * @return
	 */
	public Cursor getArrangeCursorBYStation(String stationNo) {
		final String[] args = {stationNo};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_ARRANGEPOINT_BY_STATION, args);
	}
	
	/**
	 * 根据id搜索点
	 * 
	 * @param pointid
	 * @return
	 */
	public Cursor getArrangeCursorByid(int pointid) {
		final String[] args = {String.valueOf(pointid)};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_ARRANGEPOINT_BY_POINTID, args);
	}
	
	/**
	 * 获取所有排列炮点
	 * 
	 * @return
	 */
	public Cursor getAllArrange() {
		return dbHelper.getReadableDatabase().rawQuery(SELECT_ARRANGEPOINT_ALL, null);
	}
	
	/**
	 * 根据关键词获取排列炮点
	 * 
	 * @param keyword
	 * @return
	 */
	public Cursor getArrangebyKeyword(String keyword) {
		final String[] args = {"%" + keyword + "%"};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_ARRANGEPOINT_BY_KEYWORD, args);
	}

	/**
	 * 获取所有排列线号
	 * 
	 * @return
	 */
	public Cursor getAllArrangeLineNo() {
		return dbHelper.getReadableDatabase().rawQuery(SELECT_ARRANGEPOINT_LINE, null);
	}
	
	/**
	 * 获取所有排列点号
	 * 
	 * @return
	 */
	public Cursor getAllArrageSpointNo(String lineNo) {
		final String[] args = {lineNo};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_ARRANGEPOINT_SPOINTNO, args);
	}

	/**
	 * 查找某一区域的点，并以经纬度排序
	 *
	 * @param zoom
	 * @param left
	 * @param right
	 * @param top
	 * @param bottom
	 * @return
	 */
	public Cursor getArrangeListNotHiddenCursor(final int zoom, final double left, final double right, final double top, final double bottom) {
		final String[] args = {Double.toString(left), Double.toString(right), Double.toString(bottom), Double.toString(top)};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_ARRANGEPOINT, args);
	}

	/**
	 * 获取所有已完成查排列炮点
	 */
	public Cursor getArrangeisDone() {
		final String[] args = {String.valueOf("1")};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_ARRANGEPOINT_IS_ALLDONE, args);
	}
	
	/************************************************排列炮点 结束******************************************/
	/************************************************任务 开始******************************************/

	/**
	 * 插入一条任务
	 *
	 */
	public void addTask(final String startTime, final String finishTime, final int taskType,final String task_content) {
		final ContentValues cv = new ContentValues();
		cv.put(START_TIME, startTime);
		cv.put(FINISH_TIME, finishTime);
		cv.put(TASK_TYPE, taskType);
		cv.put(TASK_CONTENT, task_content);
		dbHelper.getWritableDatabase().insert(TASK_TABLE, null, cv);
	}


	/**
	 * 获取cursor
	 *
	 * @param type
	 * @return
	 */
	public Cursor getTask() {
		return dbHelper.getReadableDatabase().rawQuery(SELECT_TASK_BY_TIME, null);
	}

	/**
	 * 根据ID删除任务
	 *
	 * @param id
	 */
	public void deleteTask(final int id) {
		final Double[] args = {Double.valueOf(id)};
		dbHelper.getWritableDatabase().execSQL(DELETE_TASK_BY_ID, args);
	}

	/**
	 * 删除所有任务
	 */
	public void DeleteAllTask() {
		dbHelper.getWritableDatabase().execSQL(DELETE_ALLTASK);
	}

	/************************************************任务结束******************************************/
	/************************************************聊天开始******************************************/
	/**
	 * 获取群聊cursor
	 *
	 * @param type
	 * @return
	 */
	public Cursor getAllTalk() {
		return dbHelper.getReadableDatabase().rawQuery(SELECT_TALK_BY_TIME, null);
	}

	/**
	 * 获取聊天cursor
	 *
	 * @param type
	 * @return
	 */
	public Cursor getTalk(String device) {
		final String[] args = {device};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_TALK_BY_DEVICE, args);
	}

	/**
	 * 插入一条聊天记录
	 *
	 */
	public void addTalk(final String time, final String content, final int type_who,
						final int type_talk,final String device,final String name) {
		final ContentValues cv = new ContentValues();
		cv.put(TALK_TIME, time);
		cv.put(TALK_CONTENT, content);
		cv.put(TALK_WHO, type_who);
		cv.put(TALK_TYPE, type_talk);
		cv.put(TALK_DEVICE, device);
		cv.put(TALK_NAME, name);
		dbHelper.getWritableDatabase().insert(TALK_TABLE, null, cv);
	}

	//删除所有记录
	public void DeleteAllTalk() {
		dbHelper.getWritableDatabase().execSQL(DELETE_ALLTALK);
	}

	/**
	 * 根据device删除聊天记录
	 *
	 * @param id
	 */
	public void deleteTalkByDevice(final String device) {
		final String[] args = {device};
		dbHelper.getWritableDatabase().execSQL(DELETE_TALK_BY_DEVICE, args);
	}


	/************************************************聊天结束******************************************/


	/************************************************查排列记录 开始******************************************/

	/**
	 * 插入一条查排列记录
	 *
	 */
	public void addArrangeRecord(final String stationNo, final String lineNo, final String spointNo, final String time,
							    final double aLat, final double aLon, final String description, final String arrived_time,
							   final String remark, final String image1, final String image2, final String image3, final int status, final String isupload) {
		final ContentValues cv = new ContentValues();
		cv.put(STATIONNO, stationNo);
		cv.put(LINENO, lineNo);
		cv.put(SPOINTNO, spointNo);
		cv.put(TIME, time);
		cv.put(LAT, aLat);
		cv.put(LON, aLon);
		cv.put(DESCRIPTION, description);
		cv.put(ARRIVED_TIME, arrived_time);
		cv.put(REMARK, remark);
		cv.put(IMAGE1, image1);
		cv.put(IMAGE2, image2);
		cv.put(IMAGE3, image3);
		cv.put(STATUS, status);
		cv.put(ISUPLOAD, isupload);
		dbHelper.getWritableDatabase().insert(ARRANGERECORD_TABLE, null, cv);
	}

	/**
	 * 更新查排列记录
	 *
	 */
	public void updateArrangeRecord(final String stationNo, final String lineNo, final String spointNo, final String time,
									final double aLat, final double aLon, final String description, final String arrived_time,
									final String remark, final String image1, final String image2, final String image3, final int status, final String isupload) {
		final ContentValues cv = new ContentValues();
		cv.put(STATIONNO, stationNo);
		cv.put(LINENO, lineNo);
		cv.put(SPOINTNO, spointNo);
		cv.put(TIME, time);
		cv.put(LAT, aLat);
		cv.put(LON, aLon);
		cv.put(DESCRIPTION, description);
		cv.put(ARRIVED_TIME, arrived_time);
		cv.put(REMARK, remark);
		cv.put(IMAGE1, image1);
		cv.put(IMAGE2, image2);
		cv.put(IMAGE3, image3);
		cv.put(STATUS, status);
		cv.put(ISUPLOAD, isupload);
		final String[] args = {stationNo};
		dbHelper.getWritableDatabase().update(ARRANGERECORD_TABLE, cv, UPDATE_STATTION, args);
	}

	/**
	 * 根据桩号搜索点
	 *
	 * @param stationNo
	 * @return
	 */
	public Cursor getArrangeRecordCursorByStation(String stationNo) {
		final String[] args = {stationNo};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_ARRANGERECORD_BY_STATION, args);
	}

	/**
	 * 获取未上传的查排列记录
	 *
	 * @param stationNo
	 * @return
	 */
	public Cursor getArrangeRecordCursorUnUpload() {
		final String[] args = {"0"};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_ARRANGERECORD_BY_UNUPLOAD, args);
	}

	/**
	 * 根据stationNo删除炮点
	 *
	 * @param station
	 */
	public void deleteArrangeRecord(final String station) {
		final String[] args = {station};
		dbHelper.getWritableDatabase().delete(ARRANGERECORD_TABLE, DELETE_ARRANGERECORD_BY_STATION, args);
	}

	public void DeleteAllArrangeRecord() {
		dbHelper.getWritableDatabase().execSQL(DELETE_ARRANGERECORD);
	}

	/************************************************查排列记录 结束******************************************/

	//质检
	/**
	 * 插入一条质检记录
	 *
	 */
	public void addCheckRecord(final String stationNo, final String aLat, final String aLon,final String name,final String tel,final String video,
							   final String remark, final String image1, final String image2, final String image3, final String time, final int status) {
		final ContentValues cv = new ContentValues();
		cv.put(STATIONNO, stationNo);
		cv.put(LAT, aLat);
		cv.put(LON, aLon);
		cv.put(NAME, name);
		cv.put(TEL, tel);
		cv.put(REMARK, remark);
		cv.put(IMAGE1, image1);
		cv.put(IMAGE2, image2);
		cv.put(IMAGE3, image3);
		cv.put(TIME, time);
		cv.put(STATUS, status);
		cv.put(VIDEO,video);
		dbHelper.getWritableDatabase().insert(CHECKRECORD_TABLE, null, cv);
	}

	/**
	 * 根据桩号搜索点
	 *
	 * @param stationNo
	 * @return
	 */
	public Cursor getCheckRecordCursorByStation(String stationNo) {
		final String[] args = {stationNo};
		return dbHelper.getReadableDatabase().rawQuery(SELECT_CHECKRECORD_BY_STATION, args);
	}

	public void DeleteAllCheckRecord() {
		dbHelper.getWritableDatabase().execSQL(DELETE_CHECKRECORD);
	}
}
