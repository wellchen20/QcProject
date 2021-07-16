package com.robert.maps.applib.kml;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.robert.maps.applib.R;
import com.robert.maps.applib.kml.constants.PoiConstants;
import com.robert.maps.applib.utils.RSQLiteOpenHelper;
import com.robert.maps.applib.utils.TimeUtil;
import com.robert.maps.applib.utils.Ut;

public class GeoDatabase implements PoiConstants {
	protected final Context mCtx;
	private SQLiteDatabase mDatabase;
	@SuppressLint("SimpleDateFormat")
	protected final SimpleDateFormat DATE_FORMAT_ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

	public GeoDatabase(Context ctx) {
		super();
		mCtx = ctx;
		mDatabase = getDatabase();
	}

	/**
	 * 插入兴趣点
	 * 
	 * @param aName
	 * @param aDescr
	 * @param aLat
	 * @param aLon
	 * @param aAlt
	 * @param aCategoryId
	 * @param aPointSourceId
	 * @param hidden
	 * @param iconid
	 */
	public void addPoi(final String aName, final String aDescr, final double aLat, final double aLon, final double aAlt, final int aCategoryId,
			final int aPointSourceId, final int hidden, final int iconid) {
		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(NAME, aName);
			cv.put(DESCR, aDescr);
			cv.put(LAT, aLat);
			cv.put(LON, aLon);
			cv.put(ALT, aAlt);
			cv.put(CATEGORYID, aCategoryId);
			cv.put(POINTSOURCEID, aPointSourceId);
			cv.put(HIDDEN, hidden);
			cv.put(ICONID, iconid);
			this.mDatabase.insert(POINTS, null, cv);
		}
	}

	/**
	 * 更新兴趣点
	 * 
	 * @param id
	 * @param aName
	 * @param aDescr
	 * @param aLat
	 * @param aLon
	 * @param aAlt
	 * @param aCategoryId
	 * @param aPointSourceId
	 * @param hidden
	 * @param iconid
	 */
	public void updatePoi(final int id, final String aName, final String aDescr, final double aLat, final double aLon, final double aAlt, final int aCategoryId,
			final int aPointSourceId, final int hidden, final int iconid) {
		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(NAME, aName);
			cv.put(DESCR, aDescr);
			cv.put(LAT, aLat);
			cv.put(LON, aLon);
			cv.put(ALT, aAlt);
			cv.put(CATEGORYID, aCategoryId);
			cv.put(POINTSOURCEID, aPointSourceId);
			cv.put(HIDDEN, hidden);
			cv.put(ICONID, iconid);
			final String[] args = {Integer.toString(id)};
			this.mDatabase.update(POINTS, cv, UPDATE_POINTS, args);
		}
	}

	/**
	 * 关闭数据库
	 * 
	 */
	@Override
	protected void finalize() throws Throwable {
		if(mDatabase != null){
			if(mDatabase.isOpen()){
				mDatabase.close();
				mDatabase = null;
			}
		}
		super.finalize();
	}

	/**
	 * 根据经纬度搜索点
	 * 
	 * @return
	 */
	public Cursor getPoiListCursor() {
		return getPoiListCursor("lat, lon");
	}

	/**
	 * 查找所有点，根据规则排序
	 * 
	 * @param sortColNames
	 * @return
	 */
	public Cursor getPoiListCursor(String sortColNames) {
		if (isDatabaseReady()) {
			return mDatabase.rawQuery(STAT_GET_POI_LIST+sortColNames, null);
		}

		return null;
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
	public Cursor getPoiListNotHiddenCursor(final int zoom, final double left, final double right, final double top, final double bottom) {
		if (isDatabaseReady()) {
			final String[] args = {Integer.toString(zoom + 1),Double.toString(left),Double.toString(right),Double.toString(bottom),Double.toString(top)};
			return mDatabase.rawQuery(STAT_PoiListNotHidden, args);
		}

		return null;
	}

	/**
	 * 查找所有点，并以名字排序
	 * 
	 * @return
	 */
	public Cursor getPoiCategoryListCursor() {
		if (isDatabaseReady()) {
			return mDatabase.rawQuery(STAT_PoiCategoryList, null);
		}

		return null;
	}

	public Cursor getActivityListCursor() {
		if (isDatabaseReady()) {
			return mDatabase.rawQuery(STAT_ActivityList, null);
		}

		return null;
	}

	/**
	 * 根据ID查找点
	 * 
	 * @param id
	 * @return
	 */
	public Cursor getPoi(final int id) {
		if (isDatabaseReady()) {
			final String[] args = {Integer.toString(id)};
			return mDatabase.rawQuery(STAT_getPoi, args);
		}
		return null;
	}
	
	/**
	 * 根据名称查找点
	 * 
	 * @param id
	 * @return
	 */
	public Cursor getPoi(final String name) {
		if (isDatabaseReady()) {
			final String[] args = {name};
			return mDatabase.rawQuery(STAT_getPoi_name, args);
		}
		return null;
	}
	
	/**
	 * 根据名称查找兴趣点名称
	 * 
	 * @param id
	 * @return
	 */
	public Cursor getPoinames(final String name) {
		if (isDatabaseReady()) {
			final String[] args = {"%" + name + "%"};
			return mDatabase.rawQuery(STAT_getpoinames, args);
		}
		return null;
	}

	/**
	 * 根据ID删除点
	 * 
	 * @param id
	 */
	public void deletePoi(final int id) {
		if (isDatabaseReady()) {
			final Double[] args = {Double.valueOf(id)};
			mDatabase.execSQL(STAT_deletePoi, args);
		}
	}

	public void deletePoiCategory(final int id) {
		if (isDatabaseReady() && id != ZERO) { // predef category My POI never delete
			final Double[] args = {Double.valueOf(id)};
			mDatabase.execSQL(STAT_deletePoiCategory, args);
		}
	}

	/**
	 * 数据库是否已经准备好
	 * 
	 * @return
	 */
	private boolean isDatabaseReady() {
		boolean ret = true;

		if(mDatabase == null)
			mDatabase = getDatabase();

		if(mDatabase == null)
			ret = false;
		else if(!mDatabase.isOpen())
			mDatabase = getDatabase();

		if(ret == false)
			try {
				Toast.makeText(mCtx, mCtx.getText(R.string.message_geodata_notavailable), Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				e.printStackTrace();
			}

		return ret;
	}

	/**
	 * 释放数据库
	 * 
	 */
	public void FreeDatabases(){
		if(mDatabase != null){
			if(mDatabase.isOpen()){
				mDatabase.close();
			}
			mDatabase = null;
		}
	}

	protected SQLiteDatabase getDatabase() {
		File folder = Ut.getRMapsMainDir(mCtx, DATA);
		if(!folder.exists()) // no sdcard
			return null;

		SQLiteDatabase db;
		try {
			db = new GeoDatabaseHelper(mCtx, folder.getAbsolutePath() + GEODATA_FILENAME).getWritableDatabase();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return db;
	}

	protected class GeoDatabaseHelper extends RSQLiteOpenHelper {
		private final static int mCurrentVersion = 22;
		
		public GeoDatabaseHelper(final Context context, final String name) {
			super(context, name, null, mCurrentVersion);
		}

		@Override
		public void onCreate(final SQLiteDatabase db) {
			db.execSQL(PoiConstants.SQL_CREATE_points);
			db.execSQL(PoiConstants.SQL_CREATE_pointsource);
			db.execSQL(PoiConstants.SQL_CREATE_category);
			db.execSQL(PoiConstants.SQL_ADD_categoryblue);
//			db.execSQL(PoiConstants.SQL_ADD_categorygreen);
//			db.execSQL(PoiConstants.SQL_ADD_categorywhite);
//			db.execSQL(PoiConstants.SQL_ADD_categoryyellow);
			db.execSQL(PoiConstants.SQL_CREATE_tracks);
			db.execSQL(PoiConstants.SQL_CREATE_trackpoints);
			db.execSQL(PoiConstants.SQL_CREATE_maps);
			db.execSQL(PoiConstants.SQL_CREATE_routes);
			LoadActivityListFromResource(db);
		}

		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {

//			if (oldVersion < 2) {
//				db.execSQL(PoiConstants.SQL_UPDATE_1_1);
//				db.execSQL(PoiConstants.SQL_UPDATE_1_2);
//				db.execSQL(PoiConstants.SQL_UPDATE_1_3);
//				db.execSQL(PoiConstants.SQL_CREATE_points);
//				db.execSQL(PoiConstants.SQL_UPDATE_1_5);
//				db.execSQL(PoiConstants.SQL_UPDATE_1_6);
//				db.execSQL(PoiConstants.SQL_UPDATE_1_7);
//				db.execSQL(PoiConstants.SQL_UPDATE_1_8);
//				db.execSQL(PoiConstants.SQL_UPDATE_1_9);
//				db.execSQL(PoiConstants.SQL_CREATE_category);
//				db.execSQL(PoiConstants.SQL_ADD_category);
//				db.execSQL(PoiConstants.SQL_ADD_categoryblue);
//				db.execSQL(PoiConstants.SQL_ADD_categorygreen);
//				db.execSQL(PoiConstants.SQL_ADD_categorywhite);
//				db.execSQL(PoiConstants.SQL_ADD_categoryyellow);
//				//db.execSQL(PoiConstants.SQL_UPDATE_1_11);
//				//db.execSQL(PoiConstants.SQL_UPDATE_1_12);
//			}
//			if (oldVersion < 3) {
//				db.execSQL(PoiConstants.SQL_UPDATE_2_7);
//				db.execSQL(PoiConstants.SQL_UPDATE_2_8);
//				db.execSQL(PoiConstants.SQL_UPDATE_2_9);
//				db.execSQL(PoiConstants.SQL_CREATE_category);
//				db.execSQL(PoiConstants.SQL_UPDATE_2_11);
//				db.execSQL(PoiConstants.SQL_UPDATE_2_12);
//			}
//			if (oldVersion < 5) {
//				db.execSQL(PoiConstants.SQL_CREATE_tracks);
//				db.execSQL(PoiConstants.SQL_CREATE_trackpoints);
//			}
//			if (oldVersion < 18) {
//				db.execSQL(PoiConstants.SQL_UPDATE_6_1);
//				db.execSQL(PoiConstants.SQL_UPDATE_6_2);
//				db.execSQL(PoiConstants.SQL_UPDATE_6_3);
//				db.execSQL(PoiConstants.SQL_CREATE_tracks);
//				db.execSQL(PoiConstants.SQL_UPDATE_6_4);
//				db.execSQL(PoiConstants.SQL_UPDATE_6_5);
//				LoadActivityListFromResource(db);
//			}
//			if (oldVersion < 20) {
//				db.execSQL(PoiConstants.SQL_UPDATE_6_1);
//				db.execSQL(PoiConstants.SQL_UPDATE_6_2);
//				db.execSQL(PoiConstants.SQL_UPDATE_6_3);
//				db.execSQL(PoiConstants.SQL_CREATE_tracks);
//				db.execSQL(PoiConstants.SQL_UPDATE_20_1);
//				db.execSQL(PoiConstants.SQL_UPDATE_6_5);
//			}
//			if (oldVersion < 21) {
//				db.execSQL(PoiConstants.SQL_CREATE_maps);
//			}
//			if (oldVersion < 22) {
//				db.execSQL(PoiConstants.SQL_CREATE_routes);
//			}
		}

	}

	public Cursor getPoiCategory(final int id) {
		if (isDatabaseReady()) {
			final String[] args = {Integer.toString(id)};
			return mDatabase.rawQuery(STAT_getPoiCategory, args);
		}

		return null;
	}

	public void LoadActivityListFromResource(final SQLiteDatabase db) {
		db.execSQL(PoiConstants.SQL_CREATE_drop_activity);
		db.execSQL(PoiConstants.SQL_CREATE_activity);
    	String[] act = mCtx.getResources().getStringArray(R.array.track_activity);
    	for(int i = 0; i < act.length; i++){
    		db.execSQL(String.format(PoiConstants.SQL_CREATE_insert_activity, i, act[i]));
    	}
	}

	public long addPoiCategory(final String title, final int hidden, final int iconid) {
		long newId = -1;

		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(NAME, title);
			cv.put(HIDDEN, hidden);
			cv.put(ICONID, iconid);
			newId = this.mDatabase.insert(CATEGORY, null, cv);
		}
		
		return newId;
	}

	public void updatePoiCategory(final int id, final String title, final int hidden, final int iconid, final int minzoom) {
		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(NAME, title);
			cv.put(HIDDEN, hidden);
			cv.put(ICONID, iconid);
			cv.put(MINZOOM, minzoom);
			final String[] args = {Integer.toString(id)};
			this.mDatabase.update(CATEGORY, cv, UPDATE_CATEGORY, args);
		}
	}

	public void DeleteAllPoi() {
		if (isDatabaseReady()) {
			mDatabase.execSQL(STAT_DeleteAllPoi);
		}
	}

	public void beginTransaction(){
		mDatabase.beginTransaction();
	}

	public void rollbackTransaction(){
		mDatabase.endTransaction();
	}

	public void commitTransaction(){
		mDatabase.setTransactionSuccessful();
		mDatabase.endTransaction();
	}
	
	public Cursor getTracksAll() {
		return mDatabase.rawQuery(STAT_getTracks, null);
	}
	
	public Cursor getTrackListCursor(final String units) {
		return getTrackListCursor(units, "trackid DESC");
	}
	
	public Cursor getTrackListCursor(final String units, final String sortColNames) {
		if (isDatabaseReady()) {
			return mDatabase.rawQuery(String.format(STAT_getTrackList+sortColNames, units), null);
		}

		return null;
	}

	public long addTrack(final String name, final String descr, final int show, final int cnt, final double distance,
			final double duration, final int category, final int activity, final long date, final String style) {
		long newId = -1;

		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(NAME, name);
			cv.put(DESCR, descr);
			cv.put(SHOW, show);
			cv.put(CNT, cnt);
			cv.put(DISTANCE, distance);
			cv.put(DURATION, duration);
			cv.put(CATEGORYID, category);
			cv.put(ACTIVITY, activity);
			cv.put(DATE, date / 1000);
			cv.put(STYLE, style);
			newId = this.mDatabase.insert(TRACKS, null, cv);
		}

		return newId;
	}

	public void updateTrack(final int id, final String name, final String descr, final int show, final int cnt, final double distance, final double duration, final int category, final int activity, final long date, final String style) {
		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(NAME, name);
			cv.put(DESCR, descr);
			cv.put(SHOW, show);
			cv.put(CNT, cnt);
			cv.put(DISTANCE, distance);
			cv.put(DURATION, duration);
			cv.put(CATEGORYID, category);
			cv.put(ACTIVITY, activity);
			cv.put(DATE, date / 1000);
			cv.put(STYLE, style);
			final String[] args = {Integer.toString(id)};
			this.mDatabase.update(TRACKS, cv, UPDATE_TRACKS, args);
		}
	}

	public void addTrackPoint(final long trackid, final double lat,
			final double lon, final double alt, final double speed,
			final long date) {
		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(TRACKID, trackid);
			cv.put(LAT, lat);
			cv.put(LON, lon);
			cv.put(ALT, alt);
			cv.put(SPEED, speed);
			cv.put(DATE, date / 1000);
			this.mDatabase.insert(TRACKPOINTS, null, cv);
		}
	}

	public Cursor getTrackChecked() {
		if (isDatabaseReady()) {
			return mDatabase.rawQuery(STAT_getTrackChecked, null);
		}

		return null;
	}

	public Cursor getTrack(final long id) {
		if (isDatabaseReady()) {
			final String[] args = {Long.toString(id)};
			return mDatabase.rawQuery(STAT_getTrack, args);
		}

		return null;
	}

	public Cursor getTrackPoints(final long id) {
		if (isDatabaseReady()) {
			final String[] args = {Long.toString(id)};
			return mDatabase.rawQuery(STAT_getTrackPoints, args);
		}

		return null;
	}

	public void setTrackChecked(final int id){
		if (isDatabaseReady()) {
			final String[] args = {Long.toString(id)};
			mDatabase.execSQL(STAT_setTrackChecked_1, args);
			//mDatabase.execSQL(STAT_setTrackChecked_2, args);
		}
	}

	public void setCategoryHidden(final int id){
		if (isDatabaseReady()) {
			final String[] args = {Long.toString(id)};
			mDatabase.execSQL(STAT_setCategoryHidden, args);
		}
	}

	public void deleteTrack(final int id) {
		if (isDatabaseReady()) {
			beginTransaction();
			final String[] args = {Long.toString(id)};
			mDatabase.execSQL(STAT_deleteTrack_1, args);
			mDatabase.execSQL(STAT_deleteTrack_2, args);
			commitTransaction();
		}
	}

	/**
	 * 合并轨迹
	 * 
	 * @return
	 */
	public long JoinTracks() {
		final Cursor ctc = getTrackChecked();
		if(ctc == null) return -1;
		if(ctc.getCount() < 2) {
			ctc.close();
			return -1;
		}
		ctc.close();
		
		final ContentValues cv = new ContentValues();
		cv.put(NAME, TRACK);
		cv.put(SHOW, 0);
		cv.put(ACTIVITY, 0);
		cv.put(CATEGORYID, 0);
		final long newId = mDatabase.insert(TRACKS, null, cv);
		cv.put(NAME, TRACK+ONE_SPACE+newId);
		
		mDatabase.execSQL(String.format("INSERT INTO 'trackpoints' (trackid, lat, lon, alt, speed, date) SELECT %d, lat, lon, alt, speed, date FROM 'trackpoints' WHERE trackid IN (SELECT trackid FROM 'tracks' WHERE show = 1) ORDER BY date", newId));
		final String[] args = {Long.toString(newId)};
		final Cursor c = mDatabase.rawQuery("SELECT MIN(date) FROM 'trackpoints' WHERE trackid = @1", args);
		if(c != null) {
			if (c.moveToFirst()) {
				cv.put(DATE, c.getDouble(0));
			}
			c.close();
		}
		final String[] args2 = {Long.toString(newId)};
		mDatabase.update(TRACKS, cv, UPDATE_TRACKS, args2);
		
		return newId;
	}
	
	public int saveTrackFromWriter(final SQLiteDatabase db){
		int res = 0;
		if (isDatabaseReady()) {
			final Cursor c = db.rawQuery(STAT_saveTrackFromWriter, null);
			Log.e("Cursor", c.getCount()+"");
			if(c != null){
				if(c.getCount() > 1){
					beginTransaction();

					res = c.getCount();
					long newId = -1;

					final ContentValues cv = new ContentValues();
					cv.put(NAME, TRACK);
					cv.put(SHOW, 0);
					cv.put(ACTIVITY, 0);
					cv.put(CATEGORYID, 0);
					newId = mDatabase.insert(TRACKS, null, cv);
					res = (int) newId;

					cv.put(NAME, TRACK+ONE_SPACE+newId);
					if (c.moveToFirst()) {
						cv.put(DATE, c.getInt(4));
					}
					final String[] args = {Long.toString(newId)};
					mDatabase.update(TRACKS, cv, UPDATE_TRACKS, args);

					if (c.moveToFirst()) {
						do {
							cv.clear();
							cv.put(TRACKID, newId);
							cv.put(LAT, c.getDouble(0));
							cv.put(LON, c.getDouble(1));
							cv.put(ALT, c.getDouble(2));
							cv.put(SPEED, c.getDouble(3));
							cv.put(DATE, c.getInt(4));
							mDatabase.insert(TRACKPOINTS, null, cv);
						} while (c.moveToNext());
					}

					commitTransaction();
				}
				c.close();

				db.execSQL(STAT_CLEAR_TRACKPOINTS);
			}

		}

		return res;
	}

	/**
	 * 获取导航轨迹点
	 * 
	 * @param trackId
	 * @param left
	 * @param right
	 * @param top
	 * @param bottom
	 * @return
	 */
	public Cursor getTrackPointCursor(int trackId, final double left, final double right, final double top, final double bottom) {
		if (isDatabaseReady()) {
			final String[] args = {Integer.toString(trackId), Double.toString(left),Double.toString(right),Double.toString(bottom),Double.toString(top)};
			return mDatabase.rawQuery(STAT_getTrackPointByid, args);
		}
		return null;
	}
	
	
	public Cursor getMixedMaps() {
		if (isDatabaseReady())
			return mDatabase.rawQuery(STAT_get_maps, null);
		return null;
	}

	public long addMap(int type, String params) {
		long newId = -1;

		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(NAME, "New map");
			cv.put(TYPE, type);
			cv.put(PARAMS, params);
			newId = this.mDatabase.insert(MAPS, null, cv);
		}

		return newId;
	}
	
	public Cursor getMap(long id) {
		if (isDatabaseReady()) {
			final String[] args = {Long.toString(id)};
			return mDatabase.rawQuery(STAT_get_map, args);
		};
		return null;
	}
	
	public void updateMap(long id, String name, int type, String params) {
		if (isDatabaseReady()) {
			final ContentValues cv = new ContentValues();
			cv.put(NAME, name);
			cv.put(TYPE, type);
			cv.put(PARAMS, params);
			final String[] args = {Long.toString(id)};
			this.mDatabase.update(MAPS, cv, UPDATE_MAPS, args);
		}
	}

	public void deleteMap(long id) {
		if (isDatabaseReady()) {
			final String[] args = {Long.toString(id)};
			mDatabase.delete(MAPS, UPDATE_MAPS, args);
		}
	}

}
