package com.mtkj.cnpc.sqlite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.andnav.osm.util.GeoPoint;

import android.content.Context;
import android.database.Cursor;
import android.util.SparseArray;

import com.alibaba.fastjson.JSON;
import com.mtkj.cnpc.protocol.bean.ArrangePoint;
import com.mtkj.cnpc.protocol.bean.ArrangeRecord;
import com.mtkj.cnpc.protocol.bean.CarKey;
import com.mtkj.cnpc.protocol.bean.CarTrave;
import com.mtkj.cnpc.protocol.bean.DailyTask;
import com.mtkj.cnpc.protocol.bean.DrillPoint;
import com.mtkj.cnpc.protocol.bean.DrillRecord;
import com.mtkj.cnpc.protocol.bean.ShotPoint;
import com.mtkj.cnpc.protocol.bean.TaskPoint;
import com.mtkj.cnpc.protocol.constants.SysConfig.WorkType;
import com.mtkj.cnpc.sqlite.constants.DBConstants;
import com.mtkj.utils.entity.CheckRecord;
import com.mtkj.utils.entity.TalkEntity;
import com.mtkj.utils.entity.TaskEntity;
import com.mtkj.utils.entity.UploadEntity;

public class PointDBDao extends DBConstants {
	
	private PointDBBase dbBase;
	private Context mContext;
	
	public PointDBDao(Context context) {
		mContext = context;
		dbBase = new PointDBBase(context);
	}
	
	/***************************************数据包  开始********************************************************/
	/**
	 * 插入一条数据
	 * 
	 * @param type
	 * @param current
	 * @param count
	 */
	public void insertPackagetNum(final String type, final String current, final String count, final String time) {
		dbBase.addPackagetNum(type, current, count, time);
	}
	
	/**
	 * 更新数据
	 * 
	 * @param type
	 * @param current
	 * @param count
	 */
	public void updatePackagerNum(final String type, final String current, final String count, final String time) {
		dbBase.updatePackagerNum(type, current, count, time);
	}
	
	/**
	 * 根据类型获取数据
	 * 
	 * @param type
	 * @return
	 */
	public HashMap<String, String> selectPackagetNum(final String type) {
		HashMap<String, String> dataLists = new HashMap<String, String>();
		Cursor cursor = dbBase.getPackagetNumByType(type);
		if (cursor != null) {
			while(cursor.moveToNext())
			{
				dataLists.put(CURRENT, cursor.getString(cursor.getColumnIndex(CURRENT)));
				dataLists.put(COUNT, cursor.getString(cursor.getColumnIndex(COUNT)));
			}
			cursor.close();
		}
		return dataLists;
	}
	
	/**
	 * 获取更新时间
	 * 
	 * @param type
	 * @return
	 */
	public String selectPackagetNumTime(final String type) {
		String time = "";
		Cursor cursor = dbBase.getPackagetNumByType(type);
		if (cursor != null) {
			while(cursor.moveToNext())
			{
				time =  cursor.getString(cursor.getColumnIndex(TIME));
			}
			cursor.close();
		}
		return time;
	}
	
	/**
	 * 删除数据
	 */
	public void deletePackagetNum() {
		dbBase.deletePackagetNum();
	}
	/***************************************数据包  结束********************************************************/
	
	/***************************************推土  开始*****************************************************************/
	/**
	 * 边界点批处理，插入多条数据
	 * 
	 * @param block
	 * @param geoPoints
	 */
	public synchronized void insertBulldozTask(String block, List<GeoPoint> geoPoints) {
		if (geoPoints != null && geoPoints.size() > 0) {
			for (GeoPoint geoPoint : geoPoints) {
				insertBulldozTask(block, geoPoint);
			}
		}
	}
	 
	/**
	 * 边界点，插入一条数据
	 * 
	 * @param block
	 * @param geoPoint
	 */
	public synchronized void insertBulldozTask(String block, GeoPoint geoPoint) {
		dbBase.addBulldozTask(block, geoPoint.getLatitude(), geoPoint.getLongitude());
	}
	
	/**
	 * 获取区块边界点
	 * 
	 * @param block
	 * @return
	 */
	public HashMap<String, List<GeoPoint>> selectBulldozTask(String block) {
		HashMap<String, List<GeoPoint>> hashMap = new HashMap<String, List<GeoPoint>>();
		List<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
		final Cursor cursor = dbBase.getBulldozTaskByBlock(block);
		if (cursor != null) {
			while(cursor.moveToNext())
			{
				GeoPoint geoPoint = GeoPoint.fromDouble(Double.valueOf(cursor.getString(cursor.getColumnIndex(LAT))), 
						Double.valueOf(cursor.getString(cursor.getColumnIndex(LON))));
				geoPoints.add(geoPoint);
			}
			hashMap.put(block, geoPoints);
			cursor.close();
		}
		
		return hashMap;
	}
	
	/**
	 * 获取所有区块
	 */
	public List<String> selectBulldozBlock() {
		List<String> blocks = new ArrayList<String>();
		Cursor cursor = dbBase.getBulldozTask();
		if (cursor != null) {
			while(cursor.moveToNext()){
				blocks.add(cursor.getString(cursor.getColumnIndex(BLOCK)));
			}
			cursor.close();
		}
		
		return blocks;
	}
	
	/**
	 * 批处理获取区块边界点
	 * 
	 * @param blocks
	 * @return
	 */
	public HashMap<String, List<GeoPoint>> selectBulldozTasks(List<String> blocks) {
		HashMap<String, List<GeoPoint>> hashMap = new HashMap<String, List<GeoPoint>>();
		for (int i = 0; i < blocks.size(); i++) {
			List<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
			Cursor cursor = dbBase.getBulldozTaskByBlock(blocks.get(i));
			if (cursor != null) {
				while(cursor.moveToNext())
				{
					GeoPoint geoPoint = GeoPoint.fromDouble(Double.valueOf(cursor.getString(cursor.getColumnIndex(LAT))), 
							Double.valueOf(cursor.getString(cursor.getColumnIndex(LON))));
					geoPoints.add(geoPoint);
				}
				cursor.close();
			}
			hashMap.put(blocks.get(i), geoPoints);
		}
		return hashMap;
	}
	
	/**
	 * 获取区域内的区块
	 * 
	 * @param zoom
	 * @param center
	 * @param deltaX
	 * @param deltaY
	 * @return
	 */
	public HashMap<String, List<GeoPoint>> selectBulldozTaskListNotHidden(List<String> blocks, int zoom, GeoPoint center, double deltaX, double deltaY){
		HashMap<String, List<GeoPoint>> hashMap = new HashMap<String, List<GeoPoint>>();
		for (String block : blocks) {
			hashMap.put(block, doCreateBulldozTaskListFromCursor(dbBase.getBulldozTaskListNotHiddenCursor(block, zoom, center.getLongitude() - deltaX, center.getLongitude() + deltaX
					, center.getLatitude() + deltaY, center.getLatitude() - deltaY)));
		}
		return hashMap;
	}
	
	private List<GeoPoint> doCreateBulldozTaskListFromCursor(Cursor c){
		final List<GeoPoint> items = new ArrayList<GeoPoint>();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					GeoPoint geoPoint = GeoPoint.fromDouble(Double.valueOf(c.getString(c.getColumnIndex(LAT))), 
							Double.valueOf(c.getString(c.getColumnIndex(LON))));
					items.add(0, geoPoint);
				} while (c.moveToNext());
			}
			c.close();
		}

		return items;
	}
	
	/**
	 * 删除所有区块边界
	 */
	public void deleteBulldozTask() {
		dbBase.deleteBulledozTask();
	}
	
	/**
	 * 下地口批处理，插入多条数据
	 * 
	 * @param block
	 * @param geoPoints
	 */
	public synchronized void insertBulldozPoint(String block, List<GeoPoint> geoPoints) {
		if (geoPoints != null && geoPoints.size() > 0) {
			for (GeoPoint geoPoint : geoPoints) {
				insertBulldozPoint(block, geoPoint);
			}
		}
	}
	 
	/**
	 * 插入一条数据
	 * 
	 * @param block
	 * @param geoPoint
	 */
	public synchronized void insertBulldozPoint(String block, GeoPoint geoPoint) {
		dbBase.addBulldozPoint(block, geoPoint.getLatitude(), geoPoint.getLongitude(), 0);
	}
	
	/**
	 * 获取区块下地口
	 * 
	 * @param block
	 * @return
	 */
	public List<GeoPoint> selectBulldozPoint(String block) {
		List<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
		Cursor cursor= dbBase.getBulldozPointByBlock(block);
		if (cursor != null) {
			while(cursor.moveToNext())
			{
				GeoPoint geoPoint = GeoPoint.fromDouble(Double.valueOf(cursor.getString(cursor.getColumnIndex(LAT))), 
						Double.valueOf(cursor.getString(cursor.getColumnIndex(LON))));
				geoPoints.add(geoPoint);
			}
			cursor.close();
		}
		
		return geoPoints;
	}
	
	/**
	 * 获取所有下地口
	 * 
	 * @param block
	 * @return
	 */
	public List<GeoPoint> selectAllBulldozPoint() {
		List<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
		Cursor cursor= dbBase.getBulldozPoint();
		if (cursor != null) {
			while(cursor.moveToNext())
			{
				GeoPoint geoPoint = GeoPoint.fromDouble(Double.valueOf(cursor.getString(cursor.getColumnIndex(LAT))), 
						Double.valueOf(cursor.getString(cursor.getColumnIndex(LON))));
				geoPoints.add(geoPoint);
			}
			cursor.close();
		}
		return geoPoints;
	}
	
	/**
	 * 获取区域内的下地口
	 * 
	 * @param zoom
	 * @param center
	 * @param deltaX
	 * @param deltaY
	 * @return
	 */
	public List<GeoPoint> selectBulldozPointListNotHidden(int zoom, GeoPoint center, double deltaX, double deltaY){
		return doCreateBulldozPointListFromCursor(dbBase.getBulldozPointListNotHiddenCursor(zoom, center.getLongitude() - deltaX, center.getLongitude() + deltaX
				, center.getLatitude() + deltaY, center.getLatitude() - deltaY));
	}
	
	private List<GeoPoint> doCreateBulldozPointListFromCursor(Cursor c){
		List<GeoPoint> items = new ArrayList<GeoPoint>();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					GeoPoint geoPoint = GeoPoint.fromDouble(Double.valueOf(c.getString(c.getColumnIndex(LAT))), 
							Double.valueOf(c.getString(c.getColumnIndex(LON))));
					items.add(geoPoint);
				} while (c.moveToNext());
			}
			c.close();
		}

		return items;
	}
	
	/**
	 * 删除所有下地口
	 */
	public void deleteBulldozPoint() {
		dbBase.deleteBulldozPoint();
	}
	
	/**
	 * 更新下地口是否上传
	 * 
	 * @param block
	 * @param geoPoint
	 */
	public void updataBulldozPoint(String block, GeoPoint geoPoint){
		dbBase.updateBlldozPoint(block, geoPoint.getLatitude(), geoPoint.getLongitude(), 1);
	}
	
	/************************************************推土  结束*************************************************************************************/
	
	/********************************************钻井下药炮点********************************************************************* */
	/**
	 * 批处理，插入多条炮点
	 * @param drillPoints
	 */
	public synchronized void insertDrillPoints(List<DrillPoint> drillPoints){
		if (drillPoints != null && drillPoints.size() > 0) {
			for (DrillPoint drillPoint : drillPoints) {
				insertDrillPoint(drillPoint);
			}
		}
	}
	
	
	/**
	 * 添加钻井下药炮点
	 * 
	 * @param point
	 */
	public synchronized void insertDrillPoint(DrillPoint point){
		dbBase.addDrillPoint(point.stationNo, point.lineNo, point.spointNo, point.geoPoint.getLatitude(), point.geoPoint.getLongitude(),
				point.Alt, point.wellnum, point.desWellDepth, point.bombWeight, point.detonator, ZERO, ZERO);
	}

	/**
	 * 更新钻井下药炮点
	 * 
	 * @param point
	 */
	public void updateDrillPoint(final DrillPoint point){
		if(point.getId() < 0)
			dbBase.addDrillPoint(point.stationNo, point.lineNo, point.spointNo, point.geoPoint.getLatitude(), point.geoPoint.getLongitude(),
					point.Alt, point.wellnum, point.desWellDepth, point.bombWeight, point.detonator, point.Hidden == true ? ONE : ZERO, point.isDone == true ? ONE : ZERO);
		else
			dbBase.updateDrillPoint(point.getId(), point.stationNo, point.lineNo, point.spointNo, point.geoPoint.getLatitude(), point.geoPoint.getLongitude(),
					point.Alt, point.wellnum, point.desWellDepth, point.bombWeight, point.detonator, point.Hidden == true ? ONE : ZERO, point.isDone == true ? ONE : ZERO, point.time);
	}

	/**
	 * 获取区域内的炮点
	 * 
	 * @param zoom
	 * @param center
	 * @param deltaX
	 * @param deltaY
	 * @return
	 */
	public SparseArray<DrillPoint> selectDrillListNotHidden(int zoom, GeoPoint center, double deltaX, double deltaY){
		return doCreateDrillListFromCursor(dbBase.getDrillListNotHiddenCursor(zoom, center.getLongitude() - deltaX, center.getLongitude() + deltaX
				, center.getLatitude() + deltaY, center.getLatitude() - deltaY));
	}
	
	private SparseArray<DrillPoint> doCreateDrillListFromCursor(Cursor c){
		final SparseArray<DrillPoint> items = new SparseArray<DrillPoint>();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					GeoPoint geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),c.getDouble(c.getColumnIndex(LON)));
					DrillPoint drillPoint = new DrillPoint(c.getInt(c.getColumnIndex(POINTID)), c.getString(c.getColumnIndex(STATIONNO)), c.getString(c.getColumnIndex(LINENO)),
							c.getString(c.getColumnIndex(SPOINTNO)), geoPoint, c.getDouble(c.getColumnIndex(HEIGHT)), c.getDouble(c.getColumnIndex(WELLNUM)), 
							c.getDouble(c.getColumnIndex(DESWELLDEPHT)),c.getFloat(c.getColumnIndex(BOMBWEIGHT)) ,c.getDouble(c.getColumnIndex(DETONATOR)),
							c.getInt(c.getColumnIndex(HIDDEN)), c.getInt(c.getColumnIndex(ISDONE)));
					items.put(drillPoint.getId(), drillPoint);
				} while (c.moveToNext());
			}
			c.close();
		}

		return items;
	}

	/**
	 * 根据id获取钻井下药炮点
	 * 
	 * @param id
	 * @return
	 */
	public DrillPoint selectDrillPoint(int id) {
		DrillPoint point = null;
		GeoPoint geoPoint = null;
		final Cursor c = dbBase.getDrillCursorByid(id);
		if (c != null) {
			if (c.moveToFirst()) {
				geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),c.getDouble(c.getColumnIndex(LON)));
				point = new DrillPoint(c.getInt(c.getColumnIndex(POINTID)), c.getString(c.getColumnIndex(STATIONNO)), c.getString(c.getColumnIndex(LINENO)),
					c.getString(c.getColumnIndex(SPOINTNO)), geoPoint, c.getDouble(c.getColumnIndex(HEIGHT)), c.getDouble(c.getColumnIndex(WELLNUM)), 
					c.getDouble(c.getColumnIndex(DESWELLDEPHT)),c.getFloat(c.getColumnIndex(BOMBWEIGHT)) ,c.getDouble(c.getColumnIndex(DETONATOR)),
					c.getInt(c.getColumnIndex(HIDDEN)), c.getInt(c.getColumnIndex(ISDONE)));
			}
			c.close();
		}

		return point;
	}
	
	/**
	 * 根据stationNo获取钻井下药炮点
	 * 
	 * @param stationNo
	 * @return
	 */
	public DrillPoint selectDrillPoint(String stationNo) {
		DrillPoint point = null;
		GeoPoint geoPoint = null;
		final Cursor c = dbBase.getDrillCursorByStation(stationNo);
		if (c != null) {
			if (c.moveToFirst()) {
				/*geoPoint = GeoPoint.fromDouble(Double.parseDouble(c.getString(c.getColumnIndex(LAT))),
						Double.parseDouble(c.getString(c.getColumnIndex(LON))));*/
				geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),c.getDouble(c.getColumnIndex(LON)));
				point = new DrillPoint(c.getInt(c.getColumnIndex(POINTID)), c.getString(c.getColumnIndex(STATIONNO)), c.getString(c.getColumnIndex(LINENO)),
					c.getString(c.getColumnIndex(SPOINTNO)), geoPoint, c.getDouble(c.getColumnIndex(HEIGHT)), c.getDouble(c.getColumnIndex(WELLNUM)), 
					c.getDouble(c.getColumnIndex(DESWELLDEPHT)),c.getFloat(c.getColumnIndex(BOMBWEIGHT)) ,c.getDouble(c.getColumnIndex(DETONATOR)),
					c.getInt(c.getColumnIndex(HIDDEN)), c.getInt(c.getColumnIndex(ISDONE)));
			}
			c.close();
		}

		return point;
	}
	/**
	 * 根据stationNo获取钻井下药炮点
	 * 
	 * @param stationNo
	 * @return
	 */
	public TaskPoint selectDrillPointToTaskPoint(String stationNo) {
		TaskPoint taskPoint = null;
		GeoPoint geoPoint = null;
		final Cursor c = dbBase.getDrillCursorByStation(stationNo);
		if (c != null) {
			if (c.moveToFirst()) {
				geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),c.getDouble(c.getColumnIndex(LON)));
				taskPoint = new TaskPoint();
				taskPoint.geoPoint = geoPoint;
				taskPoint.Id = c.getInt(c.getColumnIndex(POINTID));
				taskPoint.stationNo = c.getString(c.getColumnIndex(STATIONNO));
				taskPoint.lineNo = c.getString(c.getColumnIndex(LINENO));
				taskPoint.spointNo = c.getString(c.getColumnIndex(SPOINTNO));
				taskPoint.Alt = c.getDouble(c.getColumnIndex(HEIGHT));
				taskPoint.isDone = c.getInt(c.getColumnIndex(ISDONE)) == 1 ? true : false;
			}
			c.close();
		}

		return taskPoint;
	}

	/**
	 * 获取所有钻井下药炮点总数
	 * 
	 * @return
	 */
	public int selectDrilltaotal() {
		int total = 0;
		Cursor c = dbBase.getAllDrill();
		if (c != null) {
			total = c.getCount();
			c.close();
		}
		return total;
	}
	
	/**
	 * 获取所有钻井下药炮点
	 * 
	 * @return
	 */
	public List<DrillPoint> selectAllDrillPoint() {
		List<DrillPoint> drillPoints = new ArrayList<DrillPoint>();
		Cursor c = dbBase.getAllDrill();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					GeoPoint geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),c.getDouble(c.getColumnIndex(LON)));
					DrillPoint drillPoint = new DrillPoint(c.getInt(c.getColumnIndex(POINTID)), c.getString(c.getColumnIndex(STATIONNO)), c.getString(c.getColumnIndex(LINENO)),
							c.getString(c.getColumnIndex(SPOINTNO)), geoPoint, c.getDouble(c.getColumnIndex(HEIGHT)), c.getDouble(c.getColumnIndex(WELLNUM)), 
							c.getDouble(c.getColumnIndex(DESWELLDEPHT)),c.getFloat(c.getColumnIndex(BOMBWEIGHT)) ,c.getDouble(c.getColumnIndex(DETONATOR)),
							c.getInt(c.getColumnIndex(HIDDEN)), c.getInt(c.getColumnIndex(ISDONE)));
					drillPoints.add(drillPoint);
				} while (c.moveToNext());
			}
			c.close();
		}
		return drillPoints;
	}
	public List<TaskPoint> selectAllDrillToTaskPoint() {
		List<TaskPoint> taskPoints = new ArrayList<TaskPoint>();
		Cursor c = dbBase.getAllDrill();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					GeoPoint geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),c.getDouble(c.getColumnIndex(LON)));
					TaskPoint taskPoint = new TaskPoint();
					taskPoint.geoPoint = geoPoint;
					taskPoint.Id = c.getInt(c.getColumnIndex(POINTID));
					taskPoint.stationNo = c.getString(c.getColumnIndex(STATIONNO));
					taskPoint.lineNo = c.getString(c.getColumnIndex(LINENO));
					taskPoint.spointNo = c.getString(c.getColumnIndex(SPOINTNO));
					taskPoint.Alt = c.getDouble(c.getColumnIndex(HEIGHT));
					taskPoint.isDone = c.getInt(c.getColumnIndex(ISDONE)) == 1 ? true : false;
					taskPoints.add(taskPoint);
				} while (c.moveToNext());
			}
			c.close();
		}
		return taskPoints;
	}
	
	/**
	 * 获取已完成钻井下药炮点
	 * 
	 * @return
	 */
	public List<DrillPoint> selectDrillisDone() {
		List<DrillPoint> drillPoints = new ArrayList<DrillPoint>();
		Cursor c = dbBase.getDrillisDone();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					GeoPoint geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),c.getDouble(c.getColumnIndex(LON)));
					DrillPoint drillPoint = new DrillPoint(c.getInt(c.getColumnIndex(POINTID)), c.getString(c.getColumnIndex(STATIONNO)), c.getString(c.getColumnIndex(LINENO)),
							c.getString(c.getColumnIndex(SPOINTNO)), geoPoint, c.getDouble(c.getColumnIndex(HEIGHT)), c.getDouble(c.getColumnIndex(WELLNUM)), 
							c.getDouble(c.getColumnIndex(DESWELLDEPHT)),c.getFloat(c.getColumnIndex(BOMBWEIGHT)) ,c.getDouble(c.getColumnIndex(DETONATOR)),
							c.getInt(c.getColumnIndex(HIDDEN)), c.getInt(c.getColumnIndex(ISDONE)));
					drillPoints.add(drillPoint);
				} while (c.moveToNext());
			}
			c.close();
		}
		return drillPoints;
	}
	public List<TaskPoint> selectDrillisDoneToTaskPoint() {
		List<TaskPoint> taskPoints = new ArrayList<TaskPoint>();
		Cursor c = dbBase.getDrillisDone();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					GeoPoint geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),c.getDouble(c.getColumnIndex(LON)));
					TaskPoint taskPoint = new TaskPoint();
					taskPoint.geoPoint = geoPoint;
					taskPoint.Id = c.getInt(c.getColumnIndex(POINTID));
					taskPoint.stationNo = c.getString(c.getColumnIndex(STATIONNO));
					taskPoint.lineNo = c.getString(c.getColumnIndex(LINENO));
					taskPoint.spointNo = c.getString(c.getColumnIndex(SPOINTNO));
					taskPoint.Alt = c.getDouble(c.getColumnIndex(HEIGHT));
					taskPoint.isDone = c.getInt(c.getColumnIndex(ISDONE)) == 1 ? true : false;
					taskPoints.add(taskPoint);
				} while (c.moveToNext());
			}
			c.close();
		}
		return taskPoints;
	}
	
	/**
	 * 获取未完成钻井下药炮点
	 * 
	 * @return
	 */
	public List<DrillPoint> selectDrillunDone() {
		List<DrillPoint> drillPoints = new ArrayList<DrillPoint>();
		Cursor c = dbBase.getDrillunDone();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					GeoPoint geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),c.getDouble(c.getColumnIndex(LON)));
					DrillPoint drillPoint = new DrillPoint(c.getInt(c.getColumnIndex(POINTID)), c.getString(c.getColumnIndex(STATIONNO)), c.getString(c.getColumnIndex(LINENO)),
							c.getString(c.getColumnIndex(SPOINTNO)), geoPoint, c.getDouble(c.getColumnIndex(HEIGHT)), c.getDouble(c.getColumnIndex(WELLNUM)), 
							c.getDouble(c.getColumnIndex(DESWELLDEPHT)),c.getFloat(c.getColumnIndex(BOMBWEIGHT)) ,c.getDouble(c.getColumnIndex(DETONATOR)),
							c.getInt(c.getColumnIndex(HIDDEN)), c.getInt(c.getColumnIndex(ISDONE)));
					drillPoints.add(drillPoint);
				} while (c.moveToNext());
			}
			c.close();
		}
		return drillPoints;
	}
	public List<TaskPoint> selectDrillunDoneToTaskPoint() {
		List<TaskPoint> taskPoints = new ArrayList<TaskPoint>();
		Cursor c = dbBase.getDrillunDone();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					GeoPoint geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),c.getDouble(c.getColumnIndex(LON)));
					TaskPoint taskPoint = new TaskPoint();
					taskPoint.geoPoint = geoPoint;
					taskPoint.Id = c.getInt(c.getColumnIndex(POINTID));
					taskPoint.stationNo = c.getString(c.getColumnIndex(STATIONNO));
					taskPoint.lineNo = c.getString(c.getColumnIndex(LINENO));
					taskPoint.spointNo = c.getString(c.getColumnIndex(SPOINTNO));
					taskPoint.Alt = c.getDouble(c.getColumnIndex(HEIGHT));
					taskPoint.isDone = c.getInt(c.getColumnIndex(ISDONE)) == 1 ? true : false;
					taskPoints.add(taskPoint);
				} while (c.moveToNext());
			}
			c.close();
		}
		return taskPoints;
	}
	
	/**
	 * 根据关键词获取钻井下药炮点
	 * 
	 * @return
	 */
	public List<DrillPoint> selectDrillbyKeyWord(String key) {
		List<DrillPoint> drillPoints = new ArrayList<DrillPoint>();
		Cursor c = dbBase.getDrillbyKeyword(key);
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					GeoPoint geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),c.getDouble(c.getColumnIndex(LON)));
					DrillPoint drillPoint = new DrillPoint(c.getInt(c.getColumnIndex(POINTID)), c.getString(c.getColumnIndex(STATIONNO)), c.getString(c.getColumnIndex(LINENO)),
							c.getString(c.getColumnIndex(SPOINTNO)), geoPoint, c.getDouble(c.getColumnIndex(HEIGHT)), c.getDouble(c.getColumnIndex(WELLNUM)), 
							c.getDouble(c.getColumnIndex(DESWELLDEPHT)),c.getFloat(c.getColumnIndex(BOMBWEIGHT)) ,c.getDouble(c.getColumnIndex(DETONATOR)),
							c.getInt(c.getColumnIndex(HIDDEN)), c.getInt(c.getColumnIndex(ISDONE)));
					drillPoints.add(drillPoint);
				} while (c.moveToNext());
			}
			c.close();
		}
		return drillPoints;
	}
	public List<TaskPoint> selectDrillbyKeyWordToTaskPoint(String key) {
		List<TaskPoint> taskPoints = new ArrayList<TaskPoint>();
		Cursor c = dbBase.getDrillbyKeyword(key);
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					GeoPoint geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),c.getDouble(c.getColumnIndex(LON)));
					TaskPoint taskPoint = new TaskPoint();
					taskPoint.geoPoint = geoPoint;
					taskPoint.Id = c.getInt(c.getColumnIndex(POINTID));
					taskPoint.stationNo = c.getString(c.getColumnIndex(STATIONNO));
					taskPoint.lineNo = c.getString(c.getColumnIndex(LINENO));
					taskPoint.spointNo = c.getString(c.getColumnIndex(SPOINTNO));
					taskPoint.Alt = c.getDouble(c.getColumnIndex(HEIGHT));
					taskPoint.isDone = c.getInt(c.getColumnIndex(ISDONE)) == 1 ? true : false;
					taskPoints.add(taskPoint);
				} while (c.moveToNext());
			}
			c.close();
		}
		return taskPoints;
	}
	
	/**
	 * 根据pointid删除炮点
	 * 
	 * @param id
	 */
	public void deleteDrillPoint(final int id){
		dbBase.deleteDrillPoint(id);
	}
	
	/**
	 * 删除所有炮点
	 * 
	 */
	public void deleteAllDrillPoint() {
		dbBase.DeleteAllDrillPoint();
	}
	/********************************************钻井下药炮点********************************************************************* */
	
	/********************************************井炮炮点********************************************************************* */
	/**
	 * 批处理，插入多条炮点
	 * 
	 * @param shotPoints
	 */
	public synchronized void insertShotPoints(List<ShotPoint> shotPoints) {
		if (shotPoints != null && shotPoints.size() > 0) {
			for (ShotPoint shotPoint : shotPoints) {
				insertShotPoint(shotPoint);
			}
		}
	}
	
	/**
	 * 添加井炮炮点
	 * 
	 * @param point
	 */
	public void insertShotPoint(ShotPoint point){
		dbBase.addShotPoint(point.stationNo, point.lineNo, point.spointNo, point.geoPoint.getLatitude(), point.geoPoint.getLongitude(),
				point.Alt, ZERO, ZERO);
	}


	/**
	 * 更新井炮炮点
	 * 
	 * @param point
	 */
	public void updateShotPoint(final ShotPoint point){
		if(point.getId() < 0)
			dbBase.addShotPoint(point.stationNo, point.lineNo, point.spointNo, point.geoPoint.getLatitude(), point.geoPoint.getLongitude(),
					point.Alt, point.Hidden == true ? ONE : ZERO, point.isDone == true ? ONE : ZERO);
		else
			dbBase.updateShotPoint(point.getId(), point.stationNo, point.lineNo, point.spointNo, point.geoPoint.getLatitude(), point.geoPoint.getLongitude(),
					point.Alt, point.Hidden == true ? ONE : ZERO, point.isDone == true ? ONE : ZERO, point.time);
	}

	/**
	 * 获取区域内的炮点
	 * 
	 * @param zoom
	 * @param center
	 * @param deltaX
	 * @param deltaY
	 * @return
	 */
	public SparseArray<ShotPoint> selectShotListNotHidden(int zoom, GeoPoint center, double deltaX, double deltaY){
		return doCreateShotListFromCursor(zoom, dbBase.getShotListNotHiddenCursor(zoom, center.getLongitude() - deltaX, center.getLongitude() + deltaX
				, center.getLatitude() + deltaY, center.getLatitude() - deltaY));
	}
	
	private SparseArray<ShotPoint> doCreateShotListFromCursor(int zoom, Cursor c){
		final SparseArray<ShotPoint> items = new SparseArray<ShotPoint>();
		int i = 0;
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					if (zoom > 11) {
						GeoPoint geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),c.getDouble(c.getColumnIndex(LON)));
						ShotPoint shotPoint = new ShotPoint(c.getInt(c.getColumnIndex(POINTID)), c.getString(c.getColumnIndex(STATIONNO)), c.getString(c.getColumnIndex(LINENO)),
								c.getString(c.getColumnIndex(SPOINTNO)), geoPoint, c.getDouble(c.getColumnIndex(HEIGHT)), c.getInt(c.getColumnIndex(HIDDEN)), c.getInt(c.getColumnIndex(ISDONE)));
						items.put(shotPoint.getId(), shotPoint);
						i++;
					} else {
						if (i == 0 || i % (18 - zoom) == 0) {
							GeoPoint geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),c.getDouble(c.getColumnIndex(LON)));
							ShotPoint shotPoint = new ShotPoint(c.getInt(c.getColumnIndex(POINTID)), c.getString(c.getColumnIndex(STATIONNO)), c.getString(c.getColumnIndex(LINENO)),
									c.getString(c.getColumnIndex(SPOINTNO)), geoPoint, c.getDouble(c.getColumnIndex(HEIGHT)), c.getInt(c.getColumnIndex(HIDDEN)), c.getInt(c.getColumnIndex(ISDONE)));
							items.put(shotPoint.getId(), shotPoint);
						}
						i++;
					}
					
				} while (c.moveToNext());
			}
			c.close();
		}

		return items;
	}

	/**
	 * 根据id获取钻井下药炮点
	 * 
	 * @param id
	 * @return
	 */
	public ShotPoint selectShotPoint(int id) {
		GeoPoint geoPoint = null;
		ShotPoint shotPoint = null;
		final Cursor c = dbBase.getShotCursorByid(id);
		if (c != null) {
			if (c.moveToFirst()) {
				geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),c.getDouble(c.getColumnIndex(LON)));
				shotPoint = new ShotPoint(c.getInt(c.getColumnIndex(POINTID)), c.getString(c.getColumnIndex(STATIONNO)), c.getString(c.getColumnIndex(LINENO)),
					c.getString(c.getColumnIndex(SPOINTNO)), geoPoint, c.getDouble(c.getColumnIndex(HEIGHT)), c.getInt(c.getColumnIndex(HIDDEN)), c.getInt(c.getColumnIndex(ISDONE)));
			}
			c.close();
		}

		return shotPoint;
	}
	
	/**
	 * 根据stationNo获取钻井下药炮点
	 * 
	 * @param stationNo
	 * @return
	 */
	public ShotPoint selectShotPoint(String stationNo) {
		GeoPoint geoPoint = null;
		ShotPoint shotPoint = null;
		final Cursor c = dbBase.getShotCursorBYStation(stationNo);
		if (c != null) {
			if (c.moveToFirst()) {
				geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),c.getDouble(c.getColumnIndex(LON)));
				shotPoint = new ShotPoint(c.getInt(c.getColumnIndex(POINTID)), c.getString(c.getColumnIndex(STATIONNO)), c.getString(c.getColumnIndex(LINENO)),
					c.getString(c.getColumnIndex(SPOINTNO)), geoPoint, c.getDouble(c.getColumnIndex(HEIGHT)), c.getInt(c.getColumnIndex(HIDDEN)), c.getInt(c.getColumnIndex(ISDONE)));
			}
			c.close();
		}
		return shotPoint;
	}
	/**
	 * 根据stationNo获取钻井下药炮点
	 * 
	 * @param stationNo
	 * @return
	 */
	public TaskPoint selectShotPointTotaskPoint(String stationNo) {
		GeoPoint geoPoint = null;
		TaskPoint taskPoint = null;
		final Cursor c = dbBase.getShotCursorBYStation(stationNo);
		if (c != null) {
			if (c.moveToFirst()) {
				geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),c.getDouble(c.getColumnIndex(LON)));
				taskPoint = new TaskPoint();	
				taskPoint.geoPoint = geoPoint;
				taskPoint.Id = c.getInt(c.getColumnIndex(POINTID));
				taskPoint.stationNo = c.getString(c.getColumnIndex(STATIONNO));
				taskPoint.lineNo = c.getString(c.getColumnIndex(LINENO));
				taskPoint.spointNo = c.getString(c.getColumnIndex(SPOINTNO));
				taskPoint.Alt = c.getDouble(c.getColumnIndex(HEIGHT));
				taskPoint.isDone = c.getInt(c.getColumnIndex(ISDONE)) == 1 ? true : false;
			}
			c.close();
		}
		return taskPoint;
	}
	
	/**
	 * 获取所有井炮炮点总数
	 * 
	 * @return
	 */
	public int selectShottotal() {
		int total = 0;
		Cursor c = dbBase.getAllShot();
		if (c != null) {
			total = c.getCount();
			c.close();
		}
		return total;
	}
	
	/**
	 * 获取所有井炮炮点
	 * 
	 * @return
	 */
	public List<ShotPoint> selectAllShotPoint() {
		List<ShotPoint> shotPoints = new ArrayList<ShotPoint>();
		Cursor c = dbBase.getAllShot();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					GeoPoint geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),c.getDouble(c.getColumnIndex(LON)));
					ShotPoint shotPoint = new ShotPoint(c.getInt(c.getColumnIndex(POINTID)), c.getString(c.getColumnIndex(STATIONNO)), c.getString(c.getColumnIndex(LINENO)),
							c.getString(c.getColumnIndex(SPOINTNO)), geoPoint, c.getDouble(c.getColumnIndex(HEIGHT)), c.getInt(c.getColumnIndex(HIDDEN)), c.getInt(c.getColumnIndex(ISDONE)));
					shotPoints.add(shotPoint);
				} while (c.moveToNext());
			}
			c.close();
		}
		return shotPoints;
	}
	public List<TaskPoint> selectAllShotToTaskPoint() {
		List<TaskPoint> taskPoints = new ArrayList<TaskPoint>();
		Cursor c = dbBase.getAllShot();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					GeoPoint geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),c.getDouble(c.getColumnIndex(LON)));
					TaskPoint taskPoint = new TaskPoint();
					taskPoint.geoPoint = geoPoint;
					taskPoint.Id = c.getInt(c.getColumnIndex(POINTID));
					taskPoint.stationNo = c.getString(c.getColumnIndex(STATIONNO));
					taskPoint.lineNo = c.getString(c.getColumnIndex(LINENO));
					taskPoint.spointNo = c.getString(c.getColumnIndex(SPOINTNO));
					taskPoint.Alt = c.getDouble(c.getColumnIndex(HEIGHT));
					taskPoint.isDone = c.getInt(c.getColumnIndex(ISDONE)) == 1 ? true : false;
					taskPoints.add(taskPoint);
				} while (c.moveToNext());
			}
			c.close();
		}
		return taskPoints;
	}
	
	/**
	 * 获取已完成井炮炮点
	 * 
	 * @return
	 */
	public List<ShotPoint> selectShotPointisDone() {
		List<ShotPoint> shotPoints = new ArrayList<ShotPoint>();
		Cursor c = dbBase.getShotisDone();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					GeoPoint geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),c.getDouble(c.getColumnIndex(LON)));
					ShotPoint shotPoint = new ShotPoint(c.getInt(c.getColumnIndex(POINTID)), c.getString(c.getColumnIndex(STATIONNO)), c.getString(c.getColumnIndex(LINENO)),
							c.getString(c.getColumnIndex(SPOINTNO)), geoPoint, c.getDouble(c.getColumnIndex(HEIGHT)), c.getInt(c.getColumnIndex(HIDDEN)), c.getInt(c.getColumnIndex(ISDONE)));
					shotPoints.add(shotPoint);
				} while (c.moveToNext());
			}
			c.close();
		}
		return shotPoints;
	}
	public List<TaskPoint> selectShotisDoneToTaskPoint() {
		List<TaskPoint> taskPoints = new ArrayList<TaskPoint>();
		Cursor c = dbBase.getShotisDone();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					GeoPoint geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),c.getDouble(c.getColumnIndex(LON)));
					TaskPoint taskPoint = new TaskPoint();
					taskPoint.geoPoint = geoPoint;
					taskPoint.Id = c.getInt(c.getColumnIndex(POINTID));
					taskPoint.stationNo = c.getString(c.getColumnIndex(STATIONNO));
					taskPoint.lineNo = c.getString(c.getColumnIndex(LINENO));
					taskPoint.spointNo = c.getString(c.getColumnIndex(SPOINTNO));
					taskPoint.Alt = c.getDouble(c.getColumnIndex(HEIGHT));
					taskPoint.isDone = c.getInt(c.getColumnIndex(ISDONE)) == 1 ? true : false;
					taskPoints.add(taskPoint);
				} while (c.moveToNext());
			}
			c.close();
		}
		return taskPoints;
	}
	
	/**
	 * 获取未完成井炮炮点
	 * 
	 * @return
	 */
	public List<ShotPoint> selectShotPointunDone() {
		List<ShotPoint> shotPoints = new ArrayList<ShotPoint>();
		Cursor c = dbBase.getShotUnDone();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					GeoPoint geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),c.getDouble(c.getColumnIndex(LON)));
					ShotPoint shotPoint = new ShotPoint(c.getInt(c.getColumnIndex(POINTID)), c.getString(c.getColumnIndex(STATIONNO)), c.getString(c.getColumnIndex(LINENO)),
							c.getString(c.getColumnIndex(SPOINTNO)), geoPoint, c.getDouble(c.getColumnIndex(HEIGHT)), c.getInt(c.getColumnIndex(HIDDEN)), c.getInt(c.getColumnIndex(ISDONE)));
					shotPoints.add(shotPoint);
				} while (c.moveToNext());
			}
			c.close();
		}
		return shotPoints;
	}
	public List<TaskPoint> selectShotunDoneToTaskPoint() {
		List<TaskPoint> taskPoints = new ArrayList<TaskPoint>();
		Cursor c = dbBase.getShotUnDone();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					GeoPoint geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),c.getDouble(c.getColumnIndex(LON)));
					TaskPoint taskPoint = new TaskPoint();
					taskPoint.geoPoint = geoPoint;
					taskPoint.Id = c.getInt(c.getColumnIndex(POINTID));
					taskPoint.stationNo = c.getString(c.getColumnIndex(STATIONNO));
					taskPoint.lineNo = c.getString(c.getColumnIndex(LINENO));
					taskPoint.spointNo = c.getString(c.getColumnIndex(SPOINTNO));
					taskPoint.Alt = c.getDouble(c.getColumnIndex(HEIGHT));
					taskPoint.isDone = c.getInt(c.getColumnIndex(ISDONE)) == 1 ? true : false;
					taskPoints.add(taskPoint);
				} while (c.moveToNext());
			}
			c.close();
		}
		return taskPoints;
	}
	
	/**
	 * 获取所有井炮炮点
	 * 
	 * @return
	 */
	public List<ShotPoint> selectShotPointbyKeyword(String key) {
		List<ShotPoint> shotPoints = new ArrayList<ShotPoint>();
		Cursor c = dbBase.getShotbyKeyword(key);
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					GeoPoint geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),c.getDouble(c.getColumnIndex(LON)));
					ShotPoint shotPoint = new ShotPoint(c.getInt(c.getColumnIndex(POINTID)), c.getString(c.getColumnIndex(STATIONNO)), c.getString(c.getColumnIndex(LINENO)),
							c.getString(c.getColumnIndex(SPOINTNO)), geoPoint, c.getDouble(c.getColumnIndex(HEIGHT)), c.getInt(c.getColumnIndex(HIDDEN)), c.getInt(c.getColumnIndex(ISDONE)));
					shotPoints.add(shotPoint);
				} while (c.moveToNext());
			}
			c.close();
		}
		return shotPoints;
	}
	public List<TaskPoint> selectShotbyKeywordToTaskPoint(String key) {
		List<TaskPoint> taskPoints = new ArrayList<TaskPoint>();
		Cursor c = dbBase.getShotbyKeyword(key);
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					GeoPoint geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),c.getDouble(c.getColumnIndex(LON)));
					TaskPoint taskPoint = new TaskPoint();
					taskPoint.geoPoint = geoPoint;
					taskPoint.Id = c.getInt(c.getColumnIndex(POINTID));
					taskPoint.stationNo = c.getString(c.getColumnIndex(STATIONNO));
					taskPoint.lineNo = c.getString(c.getColumnIndex(LINENO));
					taskPoint.spointNo = c.getString(c.getColumnIndex(SPOINTNO));
					taskPoint.Alt = c.getDouble(c.getColumnIndex(HEIGHT));
					taskPoint.isDone = c.getInt(c.getColumnIndex(ISDONE)) == 1 ? true : false;
					taskPoints.add(taskPoint);
				} while (c.moveToNext());
			}
			c.close();
		}
		return taskPoints;
	}
	
	/**
	 * 根据pointid删除炮点
	 * 
	 * @param id
	 */
	public void deleteShot(final int id){
		dbBase.deleteShot(id);
	}
	
	/**
	 * 删除所有炮点
	 * 
	 */
	public void deleteAllShot() {
		dbBase.DeleteAllShot();
	}
	
	/**
	 * 是否全部完成炮点
	 * 
	 * @return
	 */
	public boolean shotIsAllDone() {
		boolean isDone = true;
		final Cursor cursor = dbBase.getShotUnDone();
		if (cursor != null) {
			if (cursor.getColumnCount() > 0) {
				isDone = false;
			}
		}
		return isDone;
	}
	
	/********************************************井炮炮点********************************************************************* */
	
	/********************************************钻井下药记录********************************************************************* */
	/**
	 * 批处理，插入多条记录
	 * @param drillPoints
	 */
	public synchronized void insertDrillRecords(final List<DrillRecord> drillRecords){
		if (drillRecords != null && drillRecords.size() > 0) {
			for (DrillRecord drillRecord : drillRecords) {
				insertDrillRecord(drillRecord);
			}
		}
	}
	
	/**
	 * 添加钻井下药记录
	 * 
	 * @param point
	 */
	public synchronized void insertDrillRecord(final DrillRecord drillRecord){
		dbBase.addDrillRecord(drillRecord.stationNo, drillRecord.lineNo, drillRecord.spointNo, drillRecord.receivetime,drillRecord.name, drillRecord.welllithology,
				drillRecord.lat, drillRecord.lon, drillRecord.wellnum, drillRecord.drilldepth, drillRecord.tel,drillRecord.bombdepth, drillRecord.bombWeight,
				drillRecord.detonator, drillRecord.getBombid(), drillRecord.getdetonatorid(),drillRecord.status, drillRecord.remark, drillRecord.image1, drillRecord.image2,
				drillRecord.image3, drillRecord.drilltime, drillRecord.isupload,drillRecord.video);
	}

	/**
	 * 更新钻井下药记录
	 * 
	 * @param record
	 */
	public void updateDrilRecord(final DrillRecord drillRecord){
		dbBase.updateDrillRecord(drillRecord.stationNo, drillRecord.lineNo, drillRecord.spointNo, drillRecord.receivetime,drillRecord.name, drillRecord.welllithology,
				drillRecord.lat, drillRecord.lon, drillRecord.wellnum, drillRecord.drilldepth,drillRecord.tel, drillRecord.bombdepth, drillRecord.bombWeight,
				drillRecord.detonator, drillRecord.getBombid(), drillRecord.getdetonatorid(),drillRecord.status, drillRecord.remark, drillRecord.image1, drillRecord.image2,
				drillRecord.image3, drillRecord.drilltime, drillRecord.isupload,drillRecord.video);
	}

	/**
	 * 根据stationNo获取钻井下药记录
	 * 
	 * @param stationNo
	 * @return
	 */
	public List<DrillRecord> selectDrillRecord(String stationNo) {
		List<DrillRecord> drillRecords = new ArrayList<>();
		final Cursor cursor = dbBase.getDrillRecordCursorByStation(stationNo);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				DrillRecord drillRecord = new DrillRecord();
				drillRecord.stationNo = cursor.getString(cursor.getColumnIndex(STATIONNO));
				drillRecord.lineNo = cursor.getString(cursor.getColumnIndex(LINENO));
				drillRecord.spointNo = cursor.getString(cursor.getColumnIndex(SPOINTNO));
				drillRecord.receivetime = cursor.getString(cursor.getColumnIndex(RECEIVETIME));
				drillRecord.welllithology = cursor.getString(cursor.getColumnIndex(WELLLITHOLOGY));
				drillRecord.wellnum = cursor.getString(cursor.getColumnIndex(WELLNUM));
				drillRecord.lon = cursor.getDouble(cursor.getColumnIndex(LON))+"";
				drillRecord.lat = cursor.getDouble(cursor.getColumnIndex(LAT))+"";
				drillRecord.drilldepth = cursor.getString(cursor.getColumnIndex(DRILLDEPTH));
				drillRecord.bombdepth = cursor.getString(cursor.getColumnIndex(BOMBDEPTH));
				drillRecord.bombWeight = cursor.getString(cursor.getColumnIndex(BOMBWEIGHT));
				drillRecord.detonator = cursor.getString(cursor.getColumnIndex(DETONATOR));
				drillRecord.bombid = new ArrayList<String>();
				drillRecord.detonatorid = new ArrayList<String>();
				drillRecord.remark = cursor.getString(cursor.getColumnIndex(REMARK));
				drillRecord.image1 = cursor.getString(cursor.getColumnIndex(IMAGE1));
				drillRecord.image2 = cursor.getString(cursor.getColumnIndex(IMAGE2));
				drillRecord.image3 = cursor.getString(cursor.getColumnIndex(IMAGE3));
				drillRecord.drilltime = cursor.getString(cursor.getColumnIndex(DRILLTIME));
				drillRecord.isupload = cursor.getString(cursor.getColumnIndex(ISUPLOAD));
				drillRecord.tel = cursor.getString(cursor.getColumnIndex(TEL));
				drillRecord.name = cursor.getString(cursor.getColumnIndex(NAME));
				drillRecord.status = cursor.getInt(cursor.getColumnIndex(STATUS));
				drillRecord.video = cursor.getString(cursor.getColumnIndex(VIDEO));
				String[] bombids = cursor.getString(cursor.getColumnIndex(BOMBID)).split(",");
				for (String string : bombids) {
					drillRecord.bombid.add(string);
				}
				String[] detonatorids = cursor.getString(cursor.getColumnIndex(DETONATORID)).split(",");
				for(String string : detonatorids) {
					drillRecord.detonatorid.add(string);
				}
				drillRecords.add(drillRecord);
			}
			cursor.close();
		}

		return drillRecords;
	}
	
	/**
	 * 获取未上传钻井下药记录
	 * 
	 * @param stationNo
	 * @return
	 */
	public List<DrillRecord> selectDrillRecordUnUpload() {
		List<DrillRecord> drillRecords = new ArrayList<>();
		final Cursor cursor = dbBase.getDrillRecordCursorUnUpload();
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					DrillRecord drillRecord = new DrillRecord();
					drillRecord.stationNo = cursor.getString(cursor.getColumnIndex(STATIONNO));
					drillRecord.lineNo = cursor.getString(cursor.getColumnIndex(LINENO));
					drillRecord.spointNo = cursor.getString(cursor.getColumnIndex(SPOINTNO));
					drillRecord.receivetime = cursor.getString(cursor.getColumnIndex(RECEIVETIME));
					drillRecord.welllithology = cursor.getString(cursor.getColumnIndex(WELLLITHOLOGY));
					drillRecord.wellnum = cursor.getString(cursor.getColumnIndex(WELLNUM));
					drillRecord.lon = cursor.getDouble(cursor.getColumnIndex(LON))+"";
					drillRecord.lat = cursor.getDouble(cursor.getColumnIndex(LAT))+"";
					drillRecord.drilldepth = cursor.getString(cursor.getColumnIndex(DRILLDEPTH));
					drillRecord.bombdepth = cursor.getString(cursor.getColumnIndex(BOMBDEPTH));
					drillRecord.bombWeight = cursor.getString(cursor.getColumnIndex(BOMBWEIGHT));
					drillRecord.detonator = cursor.getString(cursor.getColumnIndex(DETONATOR));
					drillRecord.bombid = new ArrayList<String>();
					drillRecord.detonatorid = new ArrayList<String>();
					drillRecord.remark = cursor.getString(cursor.getColumnIndex(REMARK));
					drillRecord.image1 = cursor.getString(cursor.getColumnIndex(IMAGE1));
					drillRecord.image2 = cursor.getString(cursor.getColumnIndex(IMAGE2));
					drillRecord.image3 = cursor.getString(cursor.getColumnIndex(IMAGE3));
					drillRecord.drilltime = cursor.getString(cursor.getColumnIndex(DRILLTIME));
					drillRecord.isupload = cursor.getString(cursor.getColumnIndex(ISUPLOAD));
					String[] bombids = cursor.getString(cursor.getColumnIndex(BOMBID)).split(",");
					for (String string : bombids) {
						drillRecord.bombid.add(string);
					}
					String[] detonatorids = cursor.getString(cursor.getColumnIndex(DETONATORID)).split(",");
					for(String string : detonatorids) {
						drillRecord.detonatorid.add(string);
					}
					drillRecords.add(drillRecord);
				} while (cursor.moveToNext());
			}
			cursor.close();
		}

		return drillRecords;
	}

	/**
	 * 根据station删除记录
	 * 
	 * @param id
	 */
	public void deleteDrillRecord(final String station){
		dbBase.deleteDrillRecord(station);
	}
	
	/**
	 * 删除所有记录
	 * 
	 */
	public void deleteAllDrillRecord() {
		dbBase.DeleteAllDrillRecord();
	}
	/********************************************钻井下药记录********************************************************************* */
	
	/************************************************每日任务  开始 ********************************************/
	/**
	 * 插入每日任务
	 * 
	 * @param pname
	 * @param time
	 * @param status
	 */
	public void insertDailyTask(final String pname, final String time, final int status, final String type) {
		dbBase.addDailyTask(pname, time, status, type);
	}
	
	/**
	 * 更新每日任务
	 * 
	 * @param pname
	 * @param time
	 * @param status
	 */
	public void updateDailyTask(final String pname, final String time, final int status, final String type) {
		dbBase.updateDailyTask(pname, time, status, type);
	}
	
	/**
	 * 获取所有任务
	 * 
	 * @return
	 */
	public List<DailyTask> selectDailyTask(final String type) {
		List<DailyTask> dailyTasks = new ArrayList<DailyTask>();
		Cursor c= dbBase.getDailyTask(type);
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					DailyTask dailyTask = new DailyTask();
					dailyTask.pname = c.getString(c.getColumnIndex(PNAME));
					dailyTask.time = c.getString(c.getColumnIndex(TIME));
					dailyTask.status = c.getInt(c.getColumnIndex(STATUS));
					dailyTask.total = selectDailyPointByTime(dailyTask.time, type);
					dailyTask.done = selectDailyPointDoneByTime(dailyTask.time, type);
					dailyTasks.add(dailyTask);
				} while (c.moveToNext());
			}
			c.close();
		}
		return dailyTasks;
	}
	
	/**
	 * 删除每日任务
	 */
	public void deleteDailyTask(final String type) {
		dbBase.deleteDailyTask(type);
	}
	
	/**
	 * 删除每日任务
	 */
	public void deleteDailyTaskByDaily(final String time, final String type) {
		dbBase.deleteDailyTaskByTime(time, type);
	}
	
	/**
	 * 插入每日任务炮点
	 * 
	 * @param pname
	 * @param time
	 * @param status
	 */
	public void insertDailyPoints(final String time, final List<String> stations, final String type) {
		dbBase.addDailyPoints(time, stations, type);
	}
	
	/**
	 * 插入每日任务炮点
	 * 
	 * @param pname
	 * @param time
	 * @param status
	 */
	public void insertDailyPoint(final String time, final String station, final String type) {
		dbBase.addDailyPoint(time, station, type);
	}
	
	/**
	 * 获取所有任务炮点数
	 * 
	 * @return
	 */
	public int selectDailyPoint(final String type) {
		int total = 0;
		final Cursor c = dbBase.getDailyPoint(type);
		if (c != null) {
			total = c.getCount();
			c.close();
		}
		return total;
	}
	
	/**
	 * 根据日期获取每日任务炮点数
	 * 
	 * @param time
	 * @return
	 */
	public int selectDailyPointByTime(final String time, final String type) {
		int total = 0;
		Cursor c = dbBase.getDailyPointByTime(time, type);
		if (c != null) {
			total = c.getCount();
			c.close();
		}
		return total;
	}
	
	/**
	 * 根据日期获取每日完成任务炮点数
	 * 
	 * @param time
	 * @param type
	 * @return
	 */
	public int selectDailyPointDoneByTime(final String time, final String type) {
		int total = 0;
		Cursor c = null;
		if (Integer.valueOf(type) == WorkType.WORK_TYPE_SHOT) {
			c = dbBase.getDailyPointShotDone(time);
		} else if (Integer.valueOf(type) == WorkType.WORK_TYPE_DRILE) {
			c = dbBase.getDailyPointDrillDone(time);
		}
		if (c != null) {
			total = c.getCount();
			c.close();
		}
		return total;
	}
	
	/**
	 * 删除每日任务炮点
	 */
	public void deleteDailyPoint(final String type) {
		dbBase.deleteDailyPoint(type);
	}
	
	/**
	 * 根据日期删除每日任务炮点
	 * 
	 * @param time
	 */
	public void deleteDailyPointByTime(final String time, final String type) {
		dbBase.deleteDailyPointByTime(time, type);
	}
	
	/************************************************每日任务  结束 ********************************************/
	
	/************************************************车辆   开始 ********************************************/
	
	/**
	 * 插入、更新车辆信息
	 * 
	 * @param carTrave
	 */
	public void insertCarTrave(final CarTrave carTrave) {
		dbBase.addTrave(carTrave.carnum, carTrave.driver, carTrave.destination, carTrave.task,
					carTrave.start_place, carTrave.start_time, carTrave.start_peoplenum, carTrave.start_lon, carTrave.start_lat, carTrave.start_remark, carTrave.start_isUpload, carTrave.estimated_arrived_time, 
					carTrave.arrived_place, carTrave.arrived_time, carTrave.arrived_peoplenum, carTrave.arrived_lon, carTrave.arrived_lat, carTrave.arrived_remark, carTrave.arrived_isUpload,
					carTrave.back_place, carTrave.back_time, carTrave.back_peoplenum, carTrave.back_lon, carTrave.back_lat, carTrave.back_remark, carTrave.back_isUpload, carTrave.estimated_return_time,
					carTrave.end_place, carTrave.end_time, carTrave.end_peoplenum, carTrave.end_lon, carTrave.end_lat, carTrave.end_remark, carTrave.end_isUpload);
		
	}
	
	public void updateCarTrave(final CarTrave carTrave) {
		dbBase.updateTrave(carTrave.id, carTrave.carnum, carTrave.driver, carTrave.destination, carTrave.task,
				carTrave.start_place, carTrave.start_time, carTrave.start_peoplenum, carTrave.start_lon, carTrave.start_lat, carTrave.start_remark, carTrave.start_isUpload, carTrave.estimated_arrived_time, 
				carTrave.arrived_place, carTrave.arrived_time, carTrave.arrived_peoplenum, carTrave.arrived_lon, carTrave.arrived_lat, carTrave.arrived_remark, carTrave.arrived_isUpload,
				carTrave.back_place, carTrave.back_time, carTrave.back_peoplenum, carTrave.back_lon, carTrave.back_lat, carTrave.back_remark, carTrave.back_isUpload, carTrave.estimated_return_time,
				carTrave.end_place, carTrave.end_time, carTrave.end_peoplenum, carTrave.end_lon, carTrave.end_lat, carTrave.end_remark, carTrave.end_isUpload);
	}
	
	/**
	 * 查询最后一条记录
	 */
	public CarTrave selectLastCarTrave() {
		CarTrave trave = new CarTrave();
		Cursor c = dbBase.getLastTrave();
		if (c != null) {
			if (c.moveToFirst()) {
				trave.id = c.getString(c.getColumnIndex(ID));
				trave.carnum = c.getString(c.getColumnIndex(CARNUM));
				trave.driver = c.getString(c.getColumnIndex(DRIVER));
				trave.task = c.getString(c.getColumnIndex(TASK));
				trave.destination = c.getString(c.getColumnIndex(DESTINATION)); 
				
				trave.start_place = c.getString(c.getColumnIndex(START_PLACE));
				trave.start_time = c.getString(c.getColumnIndex(START_TIME));
				trave.start_peoplenum = c.getString(c.getColumnIndex(START_PEOPLE));
				trave.start_lon = c.getString(c.getColumnIndex(START_LON));
				trave.start_lat = c.getString(c.getColumnIndex(START_LAT));
				trave.start_remark = c.getString(c.getColumnIndex(START_REMARK)); 
				trave.start_isUpload = c.getString(c.getColumnIndex(START_ISUPLOAD));
				trave.estimated_arrived_time = c.getString(c.getColumnIndex(ESTIMATED_ARRIVED_TIME));
				
				trave.arrived_place = c.getString(c.getColumnIndex(ARRIVED_PLACE));
				trave.arrived_peoplenum = c.getString(c.getColumnIndex(ARRIVED_PEOPLE));
				trave.arrived_time = c.getString(c.getColumnIndex(ARRIVED_TIME));
				trave.arrived_lon = c.getString(c.getColumnIndex(ARRIVED_LON));
				trave.arrived_lat = c.getString(c.getColumnIndex(ARRIVED_LAT));
				trave.arrived_remark = c.getString(c.getColumnIndex(ARRIVED_REMARK));
				trave.arrived_isUpload = c.getString(c.getColumnIndex(ARRIVED_ISUPLOAD));
				
				trave.back_place = c.getString(c.getColumnIndex(BACK_PLACE));
				trave.back_time = c.getString(c.getColumnIndex(BACK_TIME));
				trave.back_peoplenum = c.getString(c.getColumnIndex(BACK_PEOPLE));
				trave.back_lon = c.getString(c.getColumnIndex(BACK_LON));
				trave.back_lat = c.getString(c.getColumnIndex(BACK_LAT));
				trave.back_remark = c.getString(c.getColumnIndex(BACK_REMARK));
				trave.back_isUpload = c.getString(c.getColumnIndex(BACK_ISUPLOAD));
				trave.estimated_return_time = c.getString(c.getColumnIndex(ESTMATED_RETURN_TIME));
				
				trave.end_place = c.getString(c.getColumnIndex(END_PLACE));
				trave.end_peoplenum = c.getString(c.getColumnIndex(END_PEOPLE));
				trave.end_time = c.getString(c.getColumnIndex(END_TIME));
				trave.end_lon = c.getString(c.getColumnIndex(END_LON));
				trave.end_lat = c.getString(c.getColumnIndex(END_LAT));
				trave.end_remark = c.getString(c.getColumnIndex(END_REMARK));
				trave.end_isUpload = c.getString(c.getColumnIndex(END_ISUPLOAD));
			}
			c.close();
		}
		return trave;
	}
	
	/**
	 * 根据id查询一条记录
	 */
	public CarTrave selectCarTraveByID(final String id) {
		CarTrave trave = new CarTrave();
		Cursor c = dbBase.getTraveById(id);
		if (c != null) {
			if (c.moveToFirst()) {
				trave.id = c.getString(c.getColumnIndex(ID));
				trave.carnum = c.getString(c.getColumnIndex(CARNUM));
				trave.driver = c.getString(c.getColumnIndex(DRIVER));
				trave.task = c.getString(c.getColumnIndex(TASK));
				trave.destination = c.getString(c.getColumnIndex(DESTINATION));
				
				trave.start_place = c.getString(c.getColumnIndex(START_PLACE));
				trave.start_time = c.getString(c.getColumnIndex(START_TIME));
				trave.start_peoplenum = c.getString(c.getColumnIndex(START_PEOPLE));
				trave.start_lon = c.getString(c.getColumnIndex(START_LON));
				trave.start_lat = c.getString(c.getColumnIndex(START_LAT));
				trave.start_remark = c.getString(c.getColumnIndex(START_REMARK)); 
				trave.start_isUpload = c.getString(c.getColumnIndex(START_ISUPLOAD));
				trave.estimated_arrived_time = c.getString(c.getColumnIndex(ESTIMATED_ARRIVED_TIME));
				
				trave.arrived_place = c.getString(c.getColumnIndex(ARRIVED_PLACE));
				trave.arrived_peoplenum = c.getString(c.getColumnIndex(ARRIVED_PEOPLE));
				trave.arrived_time = c.getString(c.getColumnIndex(ARRIVED_TIME));
				trave.arrived_lon = c.getString(c.getColumnIndex(ARRIVED_LON));
				trave.arrived_lat = c.getString(c.getColumnIndex(ARRIVED_LAT));
				trave.arrived_remark = c.getString(c.getColumnIndex(ARRIVED_REMARK));
				trave.arrived_isUpload = c.getString(c.getColumnIndex(ARRIVED_ISUPLOAD));
				
				trave.back_place = c.getString(c.getColumnIndex(BACK_PLACE));
				trave.back_time = c.getString(c.getColumnIndex(BACK_TIME));
				trave.back_peoplenum = c.getString(c.getColumnIndex(BACK_PEOPLE));
				trave.back_lon = c.getString(c.getColumnIndex(BACK_LON));
				trave.back_lat = c.getString(c.getColumnIndex(BACK_LAT));
				trave.back_remark = c.getString(c.getColumnIndex(BACK_REMARK));
				trave.back_isUpload = c.getString(c.getColumnIndex(BACK_ISUPLOAD));
				trave.estimated_return_time = c.getString(c.getColumnIndex(ESTMATED_RETURN_TIME));
				
				trave.end_place = c.getString(c.getColumnIndex(END_PLACE));
				trave.end_peoplenum = c.getString(c.getColumnIndex(END_PEOPLE));
				trave.end_time = c.getString(c.getColumnIndex(END_TIME));
				trave.end_lon = c.getString(c.getColumnIndex(END_LON));
				trave.end_lat = c.getString(c.getColumnIndex(END_LAT));
				trave.end_remark = c.getString(c.getColumnIndex(END_REMARK));
				trave.end_isUpload = c.getString(c.getColumnIndex(END_ISUPLOAD));
			}
			c.close();
		}
		return trave;
	}
	
	/**
	 * 根据开始时间查询一条记录
	 */
	public CarTrave selectCarTraveByStarttime(final String starttime) {
		CarTrave trave = new CarTrave();
		Cursor c = dbBase.getTraveByStarttime(starttime);
		if (c != null) {
			if (c.moveToFirst()) {
				trave.id = c.getString(c.getColumnIndex(ID));
				trave.carnum = c.getString(c.getColumnIndex(CARNUM));
				trave.driver = c.getString(c.getColumnIndex(DRIVER));
				trave.task = c.getString(c.getColumnIndex(TASK));
				trave.destination = c.getString(c.getColumnIndex(DESTINATION));
				
				trave.start_place = c.getString(c.getColumnIndex(START_PLACE));
				trave.start_time = c.getString(c.getColumnIndex(START_TIME));
				trave.start_peoplenum = c.getString(c.getColumnIndex(START_PEOPLE));
				trave.start_lon = c.getString(c.getColumnIndex(START_LON));
				trave.start_lat = c.getString(c.getColumnIndex(START_LAT));
				trave.start_remark = c.getString(c.getColumnIndex(START_REMARK)); 
				trave.start_isUpload = c.getString(c.getColumnIndex(START_ISUPLOAD));
				trave.estimated_arrived_time = c.getString(c.getColumnIndex(ESTIMATED_ARRIVED_TIME));
				
				trave.arrived_place = c.getString(c.getColumnIndex(ARRIVED_PLACE));
				trave.arrived_peoplenum = c.getString(c.getColumnIndex(ARRIVED_PEOPLE));
				trave.arrived_time = c.getString(c.getColumnIndex(ARRIVED_TIME));
				trave.arrived_lon = c.getString(c.getColumnIndex(ARRIVED_LON));
				trave.arrived_lat = c.getString(c.getColumnIndex(ARRIVED_LAT));
				trave.arrived_remark = c.getString(c.getColumnIndex(ARRIVED_REMARK));
				trave.arrived_isUpload = c.getString(c.getColumnIndex(ARRIVED_ISUPLOAD));
				
				trave.back_place = c.getString(c.getColumnIndex(BACK_PLACE));
				trave.back_time = c.getString(c.getColumnIndex(BACK_TIME));
				trave.back_peoplenum = c.getString(c.getColumnIndex(BACK_PEOPLE));
				trave.back_lon = c.getString(c.getColumnIndex(BACK_LON));
				trave.back_lat = c.getString(c.getColumnIndex(BACK_LAT));
				trave.back_remark = c.getString(c.getColumnIndex(BACK_REMARK));
				trave.back_isUpload = c.getString(c.getColumnIndex(BACK_ISUPLOAD));
				trave.estimated_return_time = c.getString(c.getColumnIndex(ESTMATED_RETURN_TIME));
				
				trave.end_place = c.getString(c.getColumnIndex(END_PLACE));
				trave.end_peoplenum = c.getString(c.getColumnIndex(END_PEOPLE));
				trave.end_time = c.getString(c.getColumnIndex(END_TIME));
				trave.end_lon = c.getString(c.getColumnIndex(END_LON));
				trave.end_lat = c.getString(c.getColumnIndex(END_LAT));
				trave.end_remark = c.getString(c.getColumnIndex(END_REMARK));
				trave.end_isUpload = c.getString(c.getColumnIndex(END_ISUPLOAD));
			}
			c.close();
		}
		return trave;
	}
	
	/**
	 * 删除所有车辆数据
	 */
	public void deleteAllTrave() {
		dbBase.clearTrave();
	}
	
	/**
	 * 插入车牌号码
	 * 
	 * @param carnum
	 */
	public void insertCarNum(final String carnum) {
		dbBase.AddCarNum(carnum);
	}
	
	/**
	 * 查询所有车牌号码
	 * 
	 * @return
	 */
	public List<String> selectAddCarNum() {
		List<String> carnums = new ArrayList<String>();
		Cursor c = dbBase.getAllCarNum();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					carnums.add(c.getString(c.getColumnIndex(CARNUM)));
				} while (c.moveToNext());
			}
			c.close();
		}
		return carnums;
	}
	
	/**
	 * 删除所有车牌 
	 */
	public void deleteCarNums() {
		dbBase.deleteAllCarNum();
	}
	
	/**
	 * 根据车牌删除
	 * 
	 * @param carnum
	 */
	public void deleteByCarnum(final String carnum){
		dbBase.deleteByCarNum(carnum);
	}
	/************************************************车辆  结束 ********************************************/
	
	/************************************************车辆钥匙管理 开始 ********************************************/
	
	/**
	 * 插入车辆钥匙管理
	 * 
	 * @param carKey
	 */
	public void insertCarKey(final CarKey carKey) {
		dbBase.addCarKey(carKey.carnum, carKey.driver, 
				carKey.start_time, carKey.start_isUpload, carKey.back_time, carKey.back_isUpload);
	}
	
	/**
	 * 更新车辆钥匙管理
	 * 
	 * @param carKey
	 */
	public void updateCarKey(final CarKey carKey) {
		dbBase.updateCarKey(carKey.id, carKey.carnum, carKey.driver, 
				carKey.start_time, carKey.start_isUpload, carKey.back_time, carKey.back_isUpload);
	}
	
	/**
	 * 获取最后一条车辆钥匙管理
	 * 
	 * @return
	 */
	public CarKey selectLastCarkey() {
		CarKey carKey = new CarKey();
		Cursor c = dbBase.getLastCarkey();
		if (c != null) {
			if (c.moveToFirst()) {
				carKey.id = c.getString(c.getColumnIndex(ID));
				carKey.carnum = c.getString(c.getColumnIndex(CARNUM));
				carKey.driver = c.getString(c.getColumnIndex(DRIVER));
				carKey.start_time = c.getString(c.getColumnIndex(START_TIME));
				carKey.start_isUpload = c.getString(c.getColumnIndex(START_ISUPLOAD));
				carKey.back_time = c.getString(c.getColumnIndex(BACK_TIME));
				carKey.back_isUpload = c.getString(c.getColumnIndex(BACK_ISUPLOAD));
			}
			c.close();
		}
		return carKey;
	}
	
	public CarKey selectCarkeyByStarttime(final String time) {
		CarKey carKey = new CarKey();
		Cursor c = dbBase.getCarkeyByStarttime(time);
		if (c != null) {
			if (c.moveToFirst()) {
				carKey.id = c.getString(c.getColumnIndex(ID));
				carKey.carnum = c.getString(c.getColumnIndex(CARNUM));
				carKey.driver = c.getString(c.getColumnIndex(DRIVER));
				carKey.start_time = c.getString(c.getColumnIndex(START_TIME));
				carKey.start_isUpload = c.getString(c.getColumnIndex(START_ISUPLOAD));
				carKey.back_time = c.getString(c.getColumnIndex(BACK_TIME));
				carKey.back_isUpload = c.getString(c.getColumnIndex(BACK_ISUPLOAD));
			}
			c.close();
		}
		return carKey;
	}
	
	public void deleteCarkeys() {
		dbBase.clearCarkeys();
	}
	/************************************************车辆钥匙管理 结束 ********************************************/
	
	/************************************************搜索历史  开始 ********************************************/
	
	/**
	 * 插入搜索历史
	 * 
	 * @param type
	 * @param history
	 */
	public void insertHistory(final String type, final String history, final String time) {
		dbBase.addHistory(type, history, time);
	}
	
	/**
	 * 查询所有搜索历史
	 * 
	 * @return
	 */
	public List<String> selectAllHistory() {
		List<String> histories = new ArrayList<String>();
		Cursor c = dbBase.getAllHistory();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					histories.add(c.getString(c.getColumnIndex(HISTORY)));
				} while (c.moveToNext());
			}
			c.close();
		}
		return histories;
	}
	
	/**
	 * 根据类型获取搜索历史
	 * 
	 * @param type
	 * @return
	 */
	public List<String> selectHistoryByType(final String type) {
		List<String> histories = new ArrayList<String>();
		Cursor c = dbBase.getHistoryByType(type);
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					histories.add(c.getString(c.getColumnIndex(HISTORY)));
				} while (c.moveToNext());
			}
			c.close();
		}
		return histories;
	}
	
	/**
	 * 删除所有搜索历史
	 */
	public void deleteHistory() {
		dbBase.cancelHistory();
	}
	
	/**
	 * 根据类型删除搜索历史
	 * 
	 * @param type
	 */
	public void deleteHistoryByType(final String type) {
		dbBase.cancelHistoryByType(type);
	}
	
	/**
	 * 根据历史删除搜索历史
	 * 
	 * @param history
	 */
	public void deleteHistoryByHistory(final String history) {
		dbBase.cancelHistoryByHistory(history);
	}
	
	/************************************************搜索历史  结束 ********************************************/
	
	
	/********************************************排列炮点********************************************************************* */
	/**
	 * 批处理，插入多条炮点
	 * 
	 * @param shotPoints
	 */
	public synchronized void insertArrangePoints(List<ArrangePoint> arranges) {
		if (arranges != null && arranges.size() > 0) {
			for (ArrangePoint arrange : arranges) {
				insertArrangePoint(arrange);
			}
		}
	}
	
	/**
	 * 添加排列炮点
	 * 
	 * @param point
	 */
	public void insertArrangePoint(ArrangePoint arrange){
		dbBase.addArrangePoint(arrange.stationNo, arrange.lineNo, arrange.spointNo, arrange.geoPoint.getLatitude(), arrange.geoPoint.getLongitude(),arrange.time,arrange.remark, arrange.Hidden == true ? ONE : ZERO, arrange.isDone == true ? ONE : ZERO);
	}

	/**
	 * 更新排列炮点
	 * 
	 * @param point
	 */
	public void updateArrangePoint(final ArrangePoint arrange){
		if(arrange.getId() < 0)
			dbBase.addArrangePoint(arrange.stationNo, arrange.lineNo, arrange.spointNo, arrange.geoPoint.getLatitude(), arrange.geoPoint.getLongitude(), arrange.time,arrange.remark, arrange.Hidden == true ? ONE : ZERO, arrange.isDone == true ? ONE : ZERO);
		else
			dbBase.updateArrangePoint(arrange.getId(), arrange.stationNo, arrange.lineNo, arrange.spointNo, arrange.geoPoint.getLatitude(), arrange.geoPoint.getLongitude(), arrange.time,arrange.remark, arrange.Hidden == true ? ONE : ZERO, arrange.isDone == true ? ONE : ZERO);
	}

	/**
	 * 根据id获取排列炮点
	 * 
	 * @param id
	 * @return
	 */
	public ArrangePoint selectArrangePoint(int id) {
		GeoPoint geoPoint = null;
		ArrangePoint arrange = null;
		final Cursor c = dbBase.getArrangeCursorByid(id);
		if (c != null) {
			if (c.moveToFirst()) {
				geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),
						c.getDouble(c.getColumnIndex(LON)));
				arrange = new ArrangePoint(c.getInt(c.getColumnIndex(POINTID)), c.getString(c.getColumnIndex(STATIONNO)), c.getString(c.getColumnIndex(LINENO)),c.getString(c.getColumnIndex(TIME)),
					c.getString(c.getColumnIndex(REMARK)),c.getString(c.getColumnIndex(SPOINTNO)), geoPoint,c.getInt(c.getColumnIndex(HIDDEN)),c.getInt(c.getColumnIndex(ISDONE)));
			}
			c.close();
		}

		return arrange;
	}
	
	/**
	 * 根据stationNo获取排列炮点
	 * 
	 * @param stationNo
	 * @return
	 */
	public ArrangePoint selectArrangePoint(String stationNo) {
		GeoPoint geoPoint = null;
		ArrangePoint arrange = null;
		final Cursor c = dbBase.getArrangeCursorBYStation(stationNo);
		if (c != null) {
			if (c.moveToFirst()) {
				geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),
						c.getDouble(c.getColumnIndex(LON)));
				arrange = new ArrangePoint(c.getInt(c.getColumnIndex(POINTID)), c.getString(c.getColumnIndex(STATIONNO)), c.getString(c.getColumnIndex(LINENO)), c.getString(c.getColumnIndex(SPOINTNO)),c.getString(c.getColumnIndex(TIME)),
						c.getString(c.getColumnIndex(REMARK)), geoPoint,c.getInt(c.getColumnIndex(HIDDEN)),c.getInt(c.getColumnIndex(ISDONE)));
			}
			c.close();
		}
		return arrange;
	}
	
	/**
	 * 获取所有排列炮点
	 * 
	 * @return
	 */
	public List<ArrangePoint> selectAllArrangePoint() {
		List<ArrangePoint> arranges = new ArrayList<ArrangePoint>();
		Cursor c = dbBase.getAllArrange();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					GeoPoint geoPoint =GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),
							c.getDouble(c.getColumnIndex(LON)));
					ArrangePoint arrange = new ArrangePoint(c.getInt(c.getColumnIndex(POINTID)), c.getString(c.getColumnIndex(STATIONNO)), c.getString(c.getColumnIndex(LINENO)),c.getString(c.getColumnIndex(SPOINTNO)),
							c.getString(c.getColumnIndex(TIME)), c.getString(c.getColumnIndex(REMARK)),  geoPoint,c.getInt(c.getColumnIndex(HIDDEN)),c.getInt(c.getColumnIndex(ISDONE)));
					arranges.add(arrange);
				} while (c.moveToNext());
			}
			c.close();
		}
		return arranges;
	}

	/**
	 * 获取已完成排列炮点
	 *
	 * @return
	 */
	public List<ArrangePoint> selectAllArrangePointIsDone() {
		List<ArrangePoint> arranges = new ArrayList<ArrangePoint>();
		Cursor c = dbBase.getArrangeisDone();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					GeoPoint geoPoint =GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),
							c.getDouble(c.getColumnIndex(LON)));
					ArrangePoint arrange = new ArrangePoint(c.getInt(c.getColumnIndex(POINTID)), c.getString(c.getColumnIndex(STATIONNO)), c.getString(c.getColumnIndex(LINENO)),c.getString(c.getColumnIndex(SPOINTNO)),
							c.getString(c.getColumnIndex(TIME)), c.getString(c.getColumnIndex(REMARK)),  geoPoint,c.getInt(c.getColumnIndex(HIDDEN)),c.getInt(c.getColumnIndex(ISDONE)));
					arranges.add(arrange);
				} while (c.moveToNext());
			}
			c.close();
		}
		return arranges;
	}
	
	/**
	 * 根据关键字获取排列炮点
	 * 
	 * @return
	 */
	public List<ArrangePoint> selectArragePointbyKeyword(String key) {
		List<ArrangePoint> arranges = new ArrayList<ArrangePoint>();
		Cursor c = dbBase.getArrangebyKeyword(key);
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					GeoPoint geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),
							c.getDouble(c.getColumnIndex(LON)));
					ArrangePoint arrange = new ArrangePoint(c.getInt(c.getColumnIndex(POINTID)), c.getString(c.getColumnIndex(STATIONNO)), c.getString(c.getColumnIndex(LINENO)),c.getString(c.getColumnIndex(SPOINTNO)),
							c.getString(c.getColumnIndex(TIME)), c.getString(c.getColumnIndex(REMARK)), geoPoint,c.getInt(c.getColumnIndex(HIDDEN)),c.getInt(c.getColumnIndex(ISDONE)));
					arranges.add(arrange);
				} while (c.moveToNext());
			}
			c.close();
		}
		return arranges;
	}
	
	/**
	 * 根据pointid删除炮点
	 * 
	 * @param id
	 */
	public void deleteArrange(final int id){
		dbBase.deleteArrange(id);
	}
	
	/**
	 * 删除所有炮点
	 * 
	 */
	public void deleteAllArrange() {
		dbBase.DeleteAllArrange();
	}
	
	/**
	 * 获取排列炮点所有线号
	 * 
	 * @return
	 */
	public List<String> selectAllArrangeLineNo() {
		List<String> lineNoList = new ArrayList<String>();
		Cursor c = dbBase.getAllArrangeLineNo();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					lineNoList.add(c.getString(c.getColumnIndex(LINENO)));
				} while (c.moveToNext());
			}
			c.close();
		}
		return lineNoList;
	}
	
	/**
	 * 获取所有排列炮点点号
	 * 
	 * @return
	 */
	public List<String> selectAllArrangeSpointNo(String lineNo) {
		List<String> spointNoList = new ArrayList<String>();
		Cursor c = dbBase.getAllArrageSpointNo(lineNo);
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					spointNoList.add(c.getString(c.getColumnIndex(SPOINTNO)));
				} while (c.moveToNext());
			}
			c.close();
		}
		return spointNoList;
	}


	/**
	 * 获取区域内的炮点
	 *
	 * @param zoom
	 * @param center
	 * @param deltaX
	 * @param deltaY
	 * @return
	 */
	public SparseArray<ArrangePoint> selectArrangeListNotHidden(int zoom, GeoPoint center, double deltaX, double deltaY){
		return doCreateArrangeListFromCursor(dbBase.getArrangeListNotHiddenCursor(zoom, center.getLongitude() - deltaX, center.getLongitude() + deltaX
				, center.getLatitude() + deltaY, center.getLatitude() - deltaY));
	}

	private SparseArray<ArrangePoint> doCreateArrangeListFromCursor(Cursor c){
		final SparseArray<ArrangePoint> items = new SparseArray<ArrangePoint>();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					GeoPoint geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),
							c.getDouble(c.getColumnIndex(LON)));
					ArrangePoint arrangePoint = new ArrangePoint(c.getInt(c.getColumnIndex(POINTID)), c.getString(c.getColumnIndex(STATIONNO)), c.getString(c.getColumnIndex(LINENO)),c.getString(c.getColumnIndex(SPOINTNO)),
							c.getString(c.getColumnIndex(TIME)), c.getString(c.getColumnIndex(REMARK)), geoPoint, c.getInt(c.getColumnIndex(HIDDEN)), c.getInt(c.getColumnIndex(ISDONE)));
					items.put(arrangePoint.getId(), arrangePoint);
				} while (c.moveToNext());
			}
			c.close();
		}

		return items;
	}

	/**
	 * 获取所有井炮炮点总数
	 *
	 * @return
	 */
	public int selectArraytotal() {
		int total = 0;
		Cursor c = dbBase.getAllArrange();
		if (c != null) {
			total = c.getCount();
			c.close();
		}
		return total;
	}

	public List<ArrangePoint> selectArrangePointisDone() {
		List<ArrangePoint> arrangePoints = new ArrayList<ArrangePoint>();
		Cursor c = dbBase.getArrangeisDone();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					GeoPoint geoPoint = GeoPoint.fromDouble(c.getDouble(c.getColumnIndex(LAT)),
							c.getDouble(c.getColumnIndex(LON)));
					ArrangePoint arrange = new ArrangePoint(c.getInt(c.getColumnIndex(POINTID)), c.getString(c.getColumnIndex(STATIONNO)), c.getString(c.getColumnIndex(LINENO)),c.getString(c.getColumnIndex(SPOINTNO)),
							c.getString(c.getColumnIndex(TIME)), c.getString(c.getColumnIndex(REMARK)), geoPoint,c.getInt(c.getColumnIndex(HIDDEN)),c.getInt(c.getColumnIndex(ISDONE)));
					arrangePoints.add(arrange);
				} while (c.moveToNext());
			}
			c.close();
		}
		return arrangePoints;
	}
	
	/********************************************排列炮点结束********************************************************************* */
	/********************************************下发任务********************************************************************* */
	/**
	 * 添加任务
	 *
	 * @param task
	 */
	public void insertTask(TaskEntity entity){
		dbBase.addTask(entity.getStart_time(),entity.getFinish_time(),entity.getTask_type(),JSON.toJSONString(entity.getTask()));
	}

	/**
	 * 删除任务
	 *
	 * @param task
	 */
	public void deleteTask(int id){
		dbBase.deleteTask(id);
	}

	/**
	 * 删除所有任务
	 *
	 * @param task
	 */
	public void deleteAllTask(){
		dbBase.DeleteAllTask();
	}

	/**
	 * 获取所有task
	 *
	 * @param
	 * @return asd
	 */
	public List<TaskEntity> getAllTask() {
		List<TaskEntity> taskEntities = new ArrayList<TaskEntity>();
		Cursor cursor= dbBase.getTask();
		if (cursor != null) {
			while(cursor.moveToNext())
			{
				TaskEntity taskEntity = new TaskEntity();
				taskEntity.setId(cursor.getInt(cursor.getColumnIndex(ID)));
				taskEntity.setStart_time(cursor.getString(cursor.getColumnIndex(START_TIME)));
				taskEntity.setFinish_time(cursor.getString(cursor.getColumnIndex(FINISH_TIME)));
				taskEntity.setTask_type(cursor.getInt(cursor.getColumnIndex(TASK_TYPE)));

				String json = cursor.getString(cursor.getColumnIndex(TASK_CONTENT));
				List<TaskEntity.TaskBean> taskBeans = new ArrayList<>();
				taskBeans = JSON.parseArray(json,TaskEntity.TaskBean.class);

				taskEntity.setTask(taskBeans);
				taskEntities.add(taskEntity);
			}
			cursor.close();
		}
		return taskEntities;
	}
	/********************************************下发任务结束********************************************************************* */
	/********************************************聊天记录********************************************************************* */

	/**
	 * 添加聊天
	 *
	 * @param task
	 */
	public void insertTalk(TalkEntity entity){
		dbBase.addTalk(entity.getTime(),entity.getContent(),entity.getType_who(),entity.getType_talk(),entity.getDevice(),entity.getName());
	}

	/**
	 * 获取群聊talk
	 *
	 * @param
	 * @return asd
	 */
	public List<TalkEntity> getAllTalk() {
		List<TalkEntity> talkEntities = new ArrayList<TalkEntity>();
		Cursor cursor= dbBase.getAllTalk();
		if (cursor != null) {
			while(cursor.moveToNext())
			{
				TalkEntity taskEntity = new TalkEntity();
				taskEntity.setId(cursor.getInt(cursor.getColumnIndex(ID)));
				taskEntity.setTime(cursor.getString(cursor.getColumnIndex(TALK_TIME)));
				taskEntity.setContent(cursor.getString(cursor.getColumnIndex(TALK_CONTENT)));
				taskEntity.setType_who(cursor.getInt(cursor.getColumnIndex(TALK_WHO)));
				taskEntity.setType_talk(cursor.getInt(cursor.getColumnIndex(TALK_TYPE)));
				taskEntity.setDevice(cursor.getString(cursor.getColumnIndex(TALK_DEVICE)));
				taskEntity.setName(cursor.getString(cursor.getColumnIndex(TALK_NAME)));
				talkEntities.add(taskEntity);
			}
			cursor.close();
		}
		return talkEntities;
	}

	/**
	 * 获取聊天talk
	 *
	 * @param
	 * @return asd
	 */
	public List<TalkEntity> getTalk(String device) {
		List<TalkEntity> talkEntities = new ArrayList<TalkEntity>();
		Cursor cursor= dbBase.getTalk(device);
		if (cursor != null) {
			while(cursor.moveToNext())
			{
				TalkEntity taskEntity = new TalkEntity();
				taskEntity.setId(cursor.getInt(cursor.getColumnIndex(ID)));
				taskEntity.setTime(cursor.getString(cursor.getColumnIndex(TALK_TIME)));
				taskEntity.setContent(cursor.getString(cursor.getColumnIndex(TALK_CONTENT)));
				taskEntity.setType_who(cursor.getInt(cursor.getColumnIndex(TALK_WHO)));
				taskEntity.setType_talk(cursor.getInt(cursor.getColumnIndex(TALK_TYPE)));
				taskEntity.setDevice(cursor.getString(cursor.getColumnIndex(TALK_DEVICE)));
				taskEntity.setName(cursor.getString(cursor.getColumnIndex(TALK_NAME)));
				talkEntities.add(taskEntity);
			}
			cursor.close();
		}
		return talkEntities;
	}

	/**
	 * 删除所有聊天
	 *
	 * @param task
	 */
	public void deleteAllTalk(){
		dbBase.DeleteAllTalk();
	}


	/**
	 * 根据device删除当前对象聊天
	 *
	 * @param task
	 */
	public void deleteTalkWithDevice(String device){
		dbBase.deleteTalkByDevice(device);
	}


	/********************************************聊天记录结束********************************************************************* */
	/********************************************查排列记录开始********************************************************************* */
	/**
	 * 添加查排列记录
	 *
	 * @param
	 */
	public synchronized void insertArrangeRecord(final ArrangeRecord arrangeRecord){
		dbBase.addArrangeRecord(arrangeRecord.stationNo,arrangeRecord.lineNo,arrangeRecord.spointNo,arrangeRecord.time,
				arrangeRecord.lat,arrangeRecord.lon,arrangeRecord.description,arrangeRecord.arrived_time,arrangeRecord.remark,
				arrangeRecord.image1,arrangeRecord.image2, arrangeRecord.image3,arrangeRecord.status,arrangeRecord.isupload);
	}

	/**
	 * 更新查排列记录
	 *
	 * @param
	 */
	public void updateArrangeRecord(final ArrangeRecord arrangeRecord){
		dbBase.updateArrangeRecord(arrangeRecord.stationNo,arrangeRecord.lineNo,arrangeRecord.spointNo,arrangeRecord.time,
				arrangeRecord.lat,arrangeRecord.lon,arrangeRecord.description,arrangeRecord.arrived_time,arrangeRecord.remark,
				arrangeRecord.image1,arrangeRecord.image2, arrangeRecord.image3,arrangeRecord.status,arrangeRecord.isupload);
	}

	/**
	 * 根据桩号查排列记录
	 *
	 * @param
	 */
	public ArrangeRecord selectArrangeRecord(String stationNo) {
		ArrangeRecord record = null;
		final Cursor c = dbBase.getArrangeRecordCursorByStation(stationNo);
		if (c != null) {
			if (c.moveToFirst()) {
				record = new ArrangeRecord();
				record.setStationNo(c.getString(c.getColumnIndex(STATIONNO)));
				record.setLineNo(c.getString(c.getColumnIndex(LINENO)));
				record.setSpointNo(c.getString(c.getColumnIndex(SPOINTNO)));
				record.setTime(c.getString(c.getColumnIndex(TIME)));
				record.setLat(c.getDouble(c.getColumnIndex(LAT)));
				record.setLon(c.getDouble(c.getColumnIndex(LON)));
				record.setDescription(c.getString(c.getColumnIndex(DESCRIPTION)));
				record.setArrived_time(c.getString(c.getColumnIndex(ARRIVED_TIME)));
				record.setRemark(c.getString(c.getColumnIndex(REMARK)));
				record.setImage1(c.getString(c.getColumnIndex(IMAGE1)));
				record.setImage2(c.getString(c.getColumnIndex(IMAGE2)));
				record.setImage3(c.getString(c.getColumnIndex(IMAGE3)));
				record.setStatus(c.getInt(c.getColumnIndex(STATUS)));
				record.setIsupload(c.getString(c.getColumnIndex(ISUPLOAD)));
			}
			c.close();
		}

		return record;
	}

	/**
	 * 删除所有记录
	 *
	 */
	public void deleteAllArrangeRecord() {
		dbBase.DeleteAllArrangeRecord();
	}

/********************************************查排列记录结束********************************************************************* */

//质检记录
	/**
	 * 添加质检记录
	 *
	 * @param
	 */
	public synchronized void insertCheckRecord(final CheckRecord checkRecord){
		dbBase.addCheckRecord(checkRecord.getStation(), checkRecord.getLat(),checkRecord.getLon(),checkRecord.getName(),checkRecord.getTel(),checkRecord.getVideo(),
				checkRecord.getRemark(),checkRecord.getImage1(),checkRecord.getImage2(),checkRecord.getImage3(),checkRecord.getTime(),checkRecord.getStatus()
				);
	}

	/**
	 * 根据stationNo获取质检记录
	 *
	 * @param stationNo
	 * @return
	 */
	public CheckRecord selectCheckRecord(String stationNo) {
		CheckRecord checkRecord = null;
		final Cursor cursor = dbBase.getCheckRecordCursorByStation(stationNo);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
                checkRecord = new CheckRecord();
				checkRecord.setStation(cursor.getString(cursor.getColumnIndex(STATIONNO)));
				checkRecord.setLat(cursor.getDouble(cursor.getColumnIndex(LAT))+"");
				checkRecord.setLon(cursor.getDouble(cursor.getColumnIndex(LON))+"");
				checkRecord.setName(cursor.getString(cursor.getColumnIndex(NAME)));
				checkRecord.setTel(cursor.getString(cursor.getColumnIndex(TEL)));
				checkRecord.setRemark(cursor.getString(cursor.getColumnIndex(REMARK)));
				checkRecord.setImage1(cursor.getString(cursor.getColumnIndex(IMAGE1)));
				checkRecord.setImage2(cursor.getString(cursor.getColumnIndex(IMAGE2)));
				checkRecord.setImage3(cursor.getString(cursor.getColumnIndex(IMAGE3)));
				checkRecord.setTime(cursor.getString(cursor.getColumnIndex(TIME)));
				checkRecord.setStatus(cursor.getInt(cursor.getColumnIndex(STATUS)));
				checkRecord.setVideo(cursor.getString(cursor.getColumnIndex(VIDEO)));

			}
			cursor.close();
		}

		return checkRecord;
	}

	/**
	 * 删除所有记录
	 *
	 */
	public void deleteAllCheckRecord() {
		dbBase.DeleteAllCheckRecord();
	}
}
