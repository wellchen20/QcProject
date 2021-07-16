package com.robert.maps.applib.data;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.commonsware.cwac.loaderex.acl.SQLiteCursorLoader;
import com.robert.maps.applib.R;
import com.robert.maps.applib.kml.constants.PoiConstants;
import com.robert.maps.applib.utils.Ut;

public class GeoData {
	private static GeoData mInstance = null;
	
	private Context mContext;
	private GeoDataDatabaseOpenHelper mSQLiteOpenHelper;
	
	
	public static GeoData getInstance(Context context) {
		if(mInstance == null) {
			mInstance = new GeoData(context);
		}
		
		return mInstance;
	}

	public GeoData(Context context) {
		mContext = context;

		File folder = Ut.getRMapsMainDir(context, PoiConstants.DATA);
		mSQLiteOpenHelper = new GeoDataDatabaseOpenHelper(context, folder.getAbsolutePath() + PoiConstants.GEODATA_FILENAME);
	}
	
	protected class GeoDataDatabaseOpenHelper extends SQLiteSDOpenHelper {
		private final static int mCurrentVersion = 23;
		
		public GeoDataDatabaseOpenHelper(final Context context, final String name) {
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
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			onCreate(db);
		}

		@Override
		public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			super.onDowngrade(db, oldVersion, newVersion);
			onCreate(db);
		}

		public void LoadActivityListFromResource(final SQLiteDatabase db) {
			db.execSQL(PoiConstants.SQL_CREATE_drop_activity);
			db.execSQL(PoiConstants.SQL_CREATE_activity);
	    	String[] act = mContext.getResources().getStringArray(R.array.track_activity);
	    	for(int i = 0; i < act.length; i++){
	    		db.execSQL(String.format(PoiConstants.SQL_CREATE_insert_activity, i, act[i]));
	    	}
		}

	}

	public SQLiteCursorLoader getPoiListCursorLoader() {
		return getPoiListCursorLoader(PoiConstants.LATLON);
	}

	public SQLiteCursorLoader getPoiListCursorLoader(String sortColNames) {
		return new SQLiteCursorLoader(mContext, mSQLiteOpenHelper, PoiConstants.STAT_GET_POI_LIST + sortColNames, null);
	}


}
