package com.robert.maps.applib.kml.constants;

public interface PoiConstants {
	public static final int EMPTY_ID = -777;
	public static final int ZERO = 0;
	public static final int ONE = 1;
	public static final String EMPTY = "";
	public static final String ONE_SPACE = " ";

	// 表属性
	public static final String LON = "lon";
	public static final String LAT = "lat";
	public static final String LATLON = "lat,lon";
	public static final String ELE = "ele";
	public static final String NAME = "name";
	public static final String DESCR = "descr";
	public static final String DESC = "desc";
	public static final String DESCRIPTION = "description";
	public static final String ALT = "alt";
	public static final String TYPE = "type";
	public static final String EXTENSIONS = "extensions";
	public static final String CATEGORYID = "categoryid";
	public static final String POINTSOURCEID = "pointsourceid";
	public static final String HIDDEN = "hidden";
	public static final String ICONID = "iconid";
	public static final String MINZOOM = "minzoom";
	public static final String MAXZOOM = "maxzoom";
	public static final String SHOW = "show";
	public static final String TRACKID = "trackid";
	public static final String SPEED = "speed";
	public static final String DATE = "date";
	public static final String STYLE = "style";
	public static final String CNT = "cnt";
	public static final String DISTANCE = "distance";
	public static final String DURATION = "duration";
	public static final String ACTIVITY = "activity";
	public static final String MAPID = "mapid";
	public static final String STATIONNO = "stationNo";
	public static final String LINENO = "lineNo";
	public static final String SPOINTNO = "spointNo";
	public static final String WELLNUM = "wellnum";
	public static final String DESWELLDEPTH = "desWellDepth";
	public static final String BOMBWEIGHT = "bombWeight";
	public static final String DETONATOR = "detonator";
	public static final String AREAID = "areaid";
	public static final String XCROSS = "xcross";
	public static final String YCROSS = "ycross";
	public static final String POINTID = "pointid";
	public static final String ISDONE = "isDone";
	public static final String TIME = "time";
	
	// 数据库表名
	public static final String POINTS = "points";
	public static final String CATEGORY = "category";
	public static final String TRACKS = "tracks";
	public static final String TRACKPOINTS = "trackpoints";
	public static final String DATA = "data";
	public static final String GEODATA_FILENAME = "/geodata.db";
	public static final String TRACK = "Track";
	public static final String PARAMS = "params";
	public static final String MAPS = "maps";

	public static final String UPDATE_POINTS = "pointid = @1";
	public static final String UPDATE_CATEGORY = "categoryid = @1";
	public static final String UPDATE_TRACKS = "trackid = @1";
	public static final String UPDATE_MAPS = "mapid = @1";

	// 查询语句
	public static final String STAT_GET_POI_LIST = "SELECT lat, lon, points.name, descr, pointid, pointid _id, pointid ID, category.iconid, category.name , points.hidden as catname FROM points LEFT JOIN category ON category.categoryid = points.categoryid ORDER BY ";
	public static final String STAT_PoiListNotHidden = "SELECT poi.lat, poi.lon, poi.name, poi.descr, poi.pointid, poi.pointid _id, poi.pointid ID, poi.categoryid, cat.iconid FROM points poi LEFT JOIN category cat ON cat.categoryid = poi.categoryid WHERE poi.hidden = 0 AND cat.hidden = 0 "
		+"AND cat.minzoom <= @1"
		+ " AND poi.lon BETWEEN @2 AND @3"
		+ " AND poi.lat BETWEEN @4 AND @5"
		+ " ORDER BY lat, lon";
	public static final String STAT_PoiCategoryList = "SELECT name, iconid, categoryid _id, hidden FROM category ORDER BY name";
	public static final String STAT_ActivityList = "SELECT name, activityid _id FROM activity ORDER BY activityid";
	public static final String STAT_getpois = "SELECT * FROM points";
	public static final String STAT_getPoi = "SELECT lat, lon, name, descr, pointid, alt, hidden, categoryid, pointsourceid, iconid FROM points WHERE pointid = @1";
	public static final String STAT_getPoi_name = "SELECT lat, lon, name, descr, pointid, alt, hidden, categoryid, pointsourceid, iconid FROM points WHERE name = @1";
	public static final String STAT_getpoinames = "SELECT name FROM points WHERE name like @1";
	public static final String STAT_deletePoi = "DELETE FROM points WHERE pointid = @1";
	public static final String STAT_deletePoiCategory = "DELETE FROM category WHERE categoryid = @1";
	public static final String STAT_getPoiCategory = "SELECT name, categoryid, hidden, iconid, minzoom FROM category WHERE categoryid = @1";
	public static final String STAT_DeleteAllPoi = "DELETE FROM points";
	public static final String STAT_getTrackList = "SELECT tracks.name, activity.name || ', ' || strftime('%%d/%%m/%%Y %%H:%%M:%%S', date, 'unixepoch', 'localtime') As title2, descr, trackid _id, cnt, TIME('2011-01-01', duration || ' seconds') as duration, round(distance/1000, 2) AS distance0, show, IFNULL(duration, -1) As NeedStatUpdate, '%s' as units, round(distance/1000/1.609344, 2) AS distance1 FROM tracks LEFT JOIN activity ON activity.activityid = tracks.activity ORDER BY ";
	public static final String STAT_getTrackChecked = "SELECT name, descr, show, trackid, cnt, distance, duration, categoryid, activity, date, style FROM tracks WHERE show = 1";
	public static final String STAT_getTracks = "SELECT name, descr, show, trackid, cnt, distance, duration, categoryid, activity, date, style FROM tracks";
	public static final String STAT_getTrack = "SELECT name, descr, show, cnt, distance, duration, categoryid, activity, date, style FROM tracks WHERE trackid = @1";
	public static final String STAT_getTrackPoints = "SELECT lat, lon, alt, speed, date FROM trackpoints WHERE trackid = @1 ORDER BY id";
	public static final String STAT_getTrackPointByid = "SELECT * FROM trackpoints WHERE trackid = @1"
			+ " AND lon BETWEEN @2 AND @3"
			+ " AND lat BETWEEN @4 AND @5";
	public static final String STAT_setTrackChecked_1 = "UPDATE tracks SET show = 1 - show * 1 WHERE trackid = @1";
	public static final String STAT_setCategoryHidden = "UPDATE category SET hidden = 1 - hidden * 1 WHERE categoryid = @1";
	public static final String STAT_setTrackChecked_2 = "UPDATE tracks SET show = 0 WHERE trackid <> @1";
	public static final String STAT_deleteTrack_1 = "DELETE FROM trackpoints WHERE trackid = @1";
	public static final String STAT_deleteTrack_2 = "DELETE FROM tracks WHERE trackid = @1";
	public static final String STAT_saveTrackFromWriter = "SELECT lat, lon, alt, speed, date FROM trackpoints ORDER BY id;";
	public static final String STAT_CLEAR_TRACKPOINTS = "DELETE FROM 'trackpoints';";
	public static final String STAT_get_maps = "SELECT mapid, name, type, params FROM 'maps';";
	public static final String STAT_get_map = "SELECT mapid, name, type, params FROM 'maps' WHERE mapid = @1;";
	
	// 创建数据库表
	public static final String SQL_CREATE_points = "CREATE TABLE 'points' (pointid INTEGER NOT NULL PRIMARY KEY UNIQUE,name VARCHAR,descr VARCHAR,lat FLOAT DEFAULT '0',lon FLOAT DEFAULT '0',alt FLOAT DEFAULT '0',hidden INTEGER DEFAULT '0',categoryid INTEGER,pointsourceid INTEGER,iconid INTEGER DEFAULT NULL);";
	public static final String SQL_CREATE_category = "CREATE TABLE 'category' (categoryid INTEGER NOT NULL PRIMARY KEY UNIQUE, name VARCHAR, hidden INTEGER DEFAULT '0', iconid INTEGER DEFAULT NULL, minzoom INTEGER DEFAULT '10');";
	public static final String SQL_CREATE_pointsource = "CREATE TABLE IF NOT EXISTS 'pointsource' (pointsourceid INTEGER NOT NULL PRIMARY KEY UNIQUE, name VARCHAR);";
	public static final String SQL_CREATE_tracks = "CREATE TABLE IF NOT EXISTS 'tracks' (trackid INTEGER NOT NULL PRIMARY KEY UNIQUE, name VARCHAR, descr VARCHAR, date LONG, show INTEGER, cnt INTEGER, duration INTEGER, distance INTEGER, categoryid INTEGER, activity INTEGER, style VARCHAR);";
	public static final String SQL_CREATE_routes = "CREATE TABLE IF NOT EXISTS 'routes' (routeid INTEGER NOT NULL PRIMARY KEY UNIQUE, name VARCHAR, descr VARCHAR, date LONG, show INTEGER, duration INTEGER, distance INTEGER, categoryid INTEGER, style VARCHAR);";
	public static final String SQL_CREATE_trackpoints = "CREATE TABLE IF NOT EXISTS 'trackpoints' (trackid INTEGER NOT NULL, id INTEGER NOT NULL PRIMARY KEY UNIQUE, lat FLOAT, lon FLOAT, alt FLOAT, speed FLOAT, date LONG, desc VARCHAR);";
	public static final String SQL_CREATE_activity = "CREATE TABLE 'activity' (activityid INTEGER NOT NULL PRIMARY KEY UNIQUE, name VARCHAR);";
	public static final String SQL_CREATE_drop_activity = "DROP TABLE IF EXISTS 'activity';";
	public static final String SQL_CREATE_insert_activity = "INSERT INTO 'activity' (activityid, name) VALUES (%d, '%s');";
	public static final String SQL_CREATE_maps = "CREATE TABLE IF NOT EXISTS 'maps' (mapid INTEGER NOT NULL PRIMARY KEY UNIQUE, name VARCHAR, type INTEGER, params VARCHAR)";
	
	// 增加兴趣点类型
	public static final String SQL_ADD_categoryblue = "INSERT INTO 'category' (categoryid, name, hidden, iconid) VALUES (0, 'MyInstert', 0, 'R.drawable.poiblue');";//blue
//	public static final String SQL_ADD_categorygreen = "INSERT INTO 'category' (categoryid, name, hidden, iconid) VALUES (1, 'MyHome', 0, 'R.drawable.poigreen');";//green
//	public static final String SQL_ADD_categorywhite = "INSERT INTO 'category' (categoryid, name, hidden, iconid) VALUES (2, 'MyCommom', 0, 'R.drawable.poiwhite');";//white
//	public static final String SQL_ADD_categoryyellow = "INSERT INTO 'category' (categoryid, name, hidden, iconid) VALUES (3, 'MyGuiding', 0, 'R.drawable.poiyellow');";//yellow

}
