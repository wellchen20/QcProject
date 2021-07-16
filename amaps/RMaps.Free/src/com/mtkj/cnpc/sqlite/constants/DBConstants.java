package com.mtkj.cnpc.sqlite.constants;

public class DBConstants {
	
	public final static String DB_NAME = "work";
	public final static int VERSION = 1;//数据库变化的话，升级记得改这个参数
	public final static int ZERO = 0;
	public final static int ONE = 1;
	
	/**
	 * 表名
	 */
	public final static String PACKAGETNUM_TABLE = "packaget_numm";
	public final static String BULLDOZTASKT_ABLE = "bulldoz_task";
	public final static String BULLDOZPOINT_TABLE = "bulldoz_point";
	public final static String DRILLPOINT_TABLE = "drill_point";
	public final static String SHOTPOINT_TABLE = "shot_point";
	public final static String DRILLRECORD_TABLE = "drill_record";
	public final static String DAILY_TASK = "daily_task";
	public final static String DAILY_POINT = "daily_point";
	public final static String TRAVE = "trave";
	public final static String CARS = "cars";
	public final static String CARKEYS = "carkeys";
	public final static String SEARCH_HISTORY = "search_history";
	public final static String ARRANGEPOINT_TABLE = "arrange_point";
	public final static String ARRANGERECORD_TABLE = "arrange_record";
	public final static String TASK_TABLE = "task_table";//
	public final static String TALK_TABLE = "talk_table";//
	public final static String CHECKRECORD_TABLE = "check_record";
	/**
	 * 字段
	 */
	public final static String STATIONNO = "stationNo";
	public final static String LINENO = "lineNo";
	public final static String SPOINTNO = "spointNo";
	public final static String MX = "mx";
	public final static String MY = "my";
	public final static String MZ = "mz";
	public final static String BLOCK = "block";
	public final static String LON = "lon";
	public final static String LAT = "lat";
	public final static String HEIGHT = "height";
	public final static String ISUPLOAD = "isupload";
	public final static String WELLNUM = "wellnum";
	public final static String DESWELLDEPHT = "desWellDepth";
	public final static String BOMBWEIGHT = "bombWeight";
	public final static String DETONATOR = "detonator";
	public final static String AREAID = "areaid";
	public final static String XCROSS = "xcross";
	public final static String YCROSS = "ycross";
	public final static String REMARK = "remark";
	public final static String TYPE = "type";
	public final static String CURRENT = "current";
	public final static String COUNT = "count";
	public final static String HIDDEN = "hidden";
	public final static String ISDONE = "isdone";
	public final static String TIME = "time";
	public final static String POINTID = "pointid";
	public final static String RECEIVETIME = "receivetime";
	public final static String DRILLTIME = "drilltime";
	public final static String WELLLITHOLOGY = "welllithology";
	public final static String DRILLDEPTH = "drilldepth";
	public final static String BOMBDEPTH = "bombdepth";
	public final static String BOMBID = "bombid";
	public final static String DETONATORID = "detonatorid";
	public final static String IMAGE1 = "image1";
	public final static String IMAGE2 = "image2";
	public final static String IMAGE3 = "image3";
	public final static String PNAME = "pname";
	public final static String STATUS = "status";
	public final static String DESCRIPTION = "description";
	
	// 车辆四汇报
	public final static String CARNUM = "carnum";
	public final static String DRIVER = "driver";
	public final static String DESTINATION = "destination";
	public final static String TASK = "task";
	
	public final static String START_PLACE = "start_palce";
	public final static String START_TIME = "start_time";//
	public final static String START_PEOPLE = "start_people";
	public final static String START_LON = "start_lon";
	public final static String START_LAT = "start_lat";
	public final static String START_REMARK = "start_remark";
	public final static String START_ISUPLOAD = "start_isupload";
	
	public final static String ARRIVED_PLACE = "arrived_palce";
	public final static String ARRIVED_TIME = "arrived_time";
	public final static String ARRIVED_PEOPLE = "arrived_people";
	public final static String ARRIVED_LON = "arrived_lon";
	public final static String ARRIVED_LAT = "arrived_lat";
	public final static String ARRIVED_REMARK = "arrived_remark";
	public final static String ARRIVED_ISUPLOAD = "arrived_isupload";
	
	public final static String BACK_PLACE = "back_palce";
	public final static String BACK_TIME = "back_time";
	public final static String BACK_PEOPLE = "back_people";
	public final static String BACK_LON = "back_lon";
	public final static String BACK_LAT = "back_lat";
	public final static String BACK_REMARK = "back_remark";
	public final static String BACK_ISUPLOAD = "back_isupload";
	
	public final static String END_PLACE = "end_palce";
	public final static String END_TIME = "end_time";
	public final static String END_PEOPLE = "end_people";
	public final static String END_LON = "end_lon";
	public final static String END_LAT = "end_lat";
	public final static String END_REMARK = "end_remark";
	public final static String END_ISUPLOAD = "end_isupload";
	public final static String FINISH_TIME = "finish_time";//
	
	public final static String ESTIMATED_ARRIVED_TIME = "estimated_arrived_time";
	public final static String ESTMATED_RETURN_TIME = "estimated_return_time";
	
	public final static String ID = "id";
	public final static String HISTORY = "history";

	//任务
	public final static String  TASK_CONTENT = "task_content";
	public final static String  TASK_TYPE = "task_type";

	//聊天记录
	public final static String TALK_TIME = "talk_time";
	public final static String TALK_CONTENT = "talk_content";
	public final static String TALK_WHO ="talk_who";
	public final static String TALK_TYPE = "talk_type";
	public final static String TALK_DEVICE = "talk_device";
	public final static String TALK_NAME = "talk_name";

	//质检记录
	public final static String NAME = "name";
	public final static String TEL = "tel";
	public final static String VIDEO = "video";

	/**
	 * 创建表
	 */
	public final static String CREATE_PACKAGETNUM_TABLE = "create table if not exists " + PACKAGETNUM_TABLE
			+ " (" + TYPE + " varchar," + CURRENT + " varchar," + COUNT + " varchar," + TIME + " varchar);";
	public final static String CREATE_BULLDOZTASK_TABLE = "create table if not exists " + BULLDOZTASKT_ABLE
				+ " (" + BLOCK + " varchar," + LON + " varchar," + LAT + " varchar );";
	public final static String CREATE_BULLDOZPOINT_TABLE = "create table if not exists " + BULLDOZPOINT_TABLE
			+ " (" + BLOCK + " varchar," + LON + " varchar," + LAT + " varchar," + ISUPLOAD + " varchar );";
	public static final String CREATE_DRILLPOINT_TABLE = "create table if not exists '" + DRILLPOINT_TABLE
			+ "' (" + POINTID + " INTEGER NOT NULL PRIMARY KEY UNIQUE, " + STATIONNO +" VARCHAR NOT NULL UNIQUE, "
			+ LINENO + " VARCHAR, " + SPOINTNO + " VARCHAR, " + LON + " DOUBLE DEFAULT '0'," + LAT + " DOUBLE DEFAULT '0',"
			+ HEIGHT + " DOUBLE DEFAULT '0'," + WELLNUM + " DOUBLE, " + DESWELLDEPHT + " DOUBLE, " + BOMBWEIGHT + " FLOAT, "
			+ DETONATOR + " DOUBLE," + HIDDEN + " INTEGER DEFAULT '0', " + ISDONE + " INTEGER DEFAULT '0', " + TIME + " VARCHAR);";
	public static final String CREATE_SHOTPOINT_TABLE = "CREATE TABLE if not exists '" + SHOTPOINT_TABLE 
			+ "' (" + POINTID + " INTEGER NOT NULL PRIMARY KEY UNIQUE," + STATIONNO + " VARCHAR NOT NULL UNIQUE, " + LINENO + " VARCHAR, "
			+ SPOINTNO + " VARCHAR, " + LON + " DOUBLE DEFAULT '0'," + LAT + " DOUBLE DEFAULT '0'," + HEIGHT + " DOUBLE DEFAULT '0',"
			+ HIDDEN + " INTEGER DEFAULT '0', " + ISDONE + " INTEGER DEFAULT '0'," + TIME + " VARCHAR);";
	public static final String CREATE_DRILLRECORD_TABLE = "create table if not exists " + DRILLRECORD_TABLE
			+ " (" + STATIONNO + " varchar," + LINENO + " varchar," + SPOINTNO + " varchar," + RECEIVETIME + " varchar,"
			+ WELLLITHOLOGY + " varchar," + WELLNUM + " varchar," + LON + " varchar," + LAT + " varchar,"+ VIDEO + " varchar,"
			+ DRILLDEPTH + " varchar," + BOMBDEPTH + " varchar," + BOMBWEIGHT + " varchar," + DETONATOR + " varchar,"
			+ BOMBID + " varchar," + DETONATORID + " varchar," + REMARK + " varchar,"+ NAME + " varchar," + TEL  + " varchar,"
			+ IMAGE1 + " varchar," + IMAGE2 + " varchar," + IMAGE3 + " varchar," + DRILLTIME + " varchar,"+ STATUS +" int,"
			+ ISUPLOAD + " varchar );";
	public static final String CREATE_CHECKRECORD_TABLE = "create table if not exists " + CHECKRECORD_TABLE
			+ " (" + STATIONNO + " varchar," + LON + " varchar," + LAT + " varchar,"
			+ NAME + " varchar," + TEL  + " varchar,"+ REMARK + " varchar,"+ VIDEO + " varchar,"
			+ IMAGE1 + " varchar," + IMAGE2 + " varchar," + IMAGE3 + " varchar," + TIME + " varchar,"
			+ STATUS + " int );";
	public final static String CREATE_DAILY_TASK = "create table if not exists " + DAILY_TASK
			+ " (" + PNAME + " varchar," + TIME + " varchar," + TYPE + " varchar," + STATUS + " int);";
	public final static String CREATE_DAILY_POINT = "create table if not exists " + DAILY_POINT
			+ " (" + TIME + " varchar," + STATIONNO + " varchar," + TYPE + " varchar);";
	public final static String CREATE_TRAVE = "create table if not exists " + TRAVE
			+ " (id INTEGER NOT NULL PRIMARY KEY UNIQUE," + CARNUM + " varchar," + DRIVER + " varchar," + DESTINATION + " varchar," + TASK + " varchar,"
			+ START_PLACE + " varchar," + START_TIME + " varchar," + START_PEOPLE + " varchar," + ESTIMATED_ARRIVED_TIME + " varchar,"
			+ START_LAT + " varchar," + START_LON + " varchar," + START_REMARK + " varchar," + START_ISUPLOAD + " int,"
			
			+ ARRIVED_PLACE + " varchar," + ARRIVED_TIME + " varchar,"  + ARRIVED_PEOPLE + " varchar," 
			+ ARRIVED_LAT + " varchar," + ARRIVED_LON + " varchar," + ARRIVED_REMARK + " varchar," + ARRIVED_ISUPLOAD + " int,"
			
			+ BACK_PLACE + " varchar," + BACK_TIME + " varchar," + BACK_PEOPLE + " varchar,"  + ESTMATED_RETURN_TIME + " varchar,"
			+ BACK_LAT + " varchar," + BACK_LON + " varchar," + BACK_REMARK + " varchar," + BACK_ISUPLOAD + " int,"
			
			+ END_PLACE + " varchar," + END_TIME + " varchar," + END_PEOPLE + " varchar," 
			+ END_LAT + " varchar," + END_LON + " varchar," + END_REMARK + " varchar," + END_ISUPLOAD + " int);";
	
	public final static String CREATE_CARNUM = "create table if not exists " + CARS
			+ " (" + CARNUM + " varchar);";
	public final static String CREATE_CARKEYS = "create table if not exists " + CARKEYS
			+ " (id INTEGER NOT NULL PRIMARY KEY UNIQUE," + CARNUM + " varchar," + DRIVER + " varchar," 
			+ START_TIME + " varchar," + START_ISUPLOAD +" int" + BACK_TIME + " varchar," + BACK_ISUPLOAD +" int);";
	
	public final static String CREATE_SEARCH_HISTORY= "create table if not exists " + SEARCH_HISTORY
			+ " (" + TYPE + " varchar," + HISTORY + " varchar," + TIME + " varchar);";
	public static final String CREATE_ARRANGEPOINT_TABLE = "CREATE TABLE if not exists '" + ARRANGEPOINT_TABLE
			+ "' (" + POINTID + " INTEGER NOT NULL PRIMARY KEY UNIQUE," + STATIONNO + " VARCHAR NOT NULL UNIQUE, " + LINENO + " VARCHAR, "
			+ SPOINTNO + " VARCHAR, " +TIME + " VARCHAR, " +REMARK + " VARCHAR, " + HIDDEN + " INTEGER DEFAULT '0', " + ISDONE + " INTEGER DEFAULT '0', "+ LON + " DOUBLE DEFAULT '0'," + LAT + " DOUBLE DEFAULT '0');";

	public final static String CREATE_TASK = "create table if not exists "+TASK_TABLE
			+" (id INTEGER PRIMARY KEY AUTOINCREMENT,"+ START_TIME+" varchar,"+FINISH_TIME+" varchar,"
			+TASK_CONTENT+" varchar,"+TASK_TYPE+" int);";

	public final static String CREATE_TALK = "create table if not exists "+TALK_TABLE
			+" (id INTEGER PRIMARY KEY AUTOINCREMENT,"+ TALK_TIME+" varchar,"+TALK_CONTENT+" varchar,"
			+TALK_WHO+" int,"+TALK_TYPE+" int,"+TALK_DEVICE+" varchar,"+TALK_NAME+" varchar);";

	public static final String CREATE_ARRANGERECORD_TABLE = "create table if not exists " + ARRANGERECORD_TABLE
			+ " (" + STATIONNO + " varchar," + LINENO + " varchar," + SPOINTNO + " varchar," + TIME + " varchar,"
			 + LON + " DOUBLE DEFAULT '0'," + LAT + " DOUBLE DEFAULT '0'," + DESCRIPTION + " varchar," + ARRIVED_TIME + " varchar,"
			+ REMARK + " varchar," + IMAGE1 + " varchar," + IMAGE2 + " varchar," + IMAGE3 + " varchar,"
			+ STATUS + " int," + ISUPLOAD + " varchar );";
	
	/**
	 * 插入
	 */
	public final static String INSERT_BULLDOZTASK = "insert into " + BULLDOZTASKT_ABLE + "(" 
			+ BLOCK + "," + LON + "," + LAT + ")" + "values(?,?,?)";
	public final static String INSERT_BULLDOZPOINT = "insert into " + BULLDOZPOINT_TABLE + "("
			+ BLOCK + "," + LON + "," + LAT + "," + ISUPLOAD + ")" + "values(?,?,?,?)";
	
	/**
	 * 查询
	 */
	public final static String SELECT_PACKAGETNUM_BY_TYPE = "select * from " + PACKAGETNUM_TABLE + " where "
			+ TYPE + "=?";
	public final static String SELECT_BULLDOZTASK_ALL = "select * from " + BULLDOZTASKT_ABLE;
	public final static String SELECT_BULLDOZTASK_BY_BLOCK = "select * from " + BULLDOZTASKT_ABLE + " where " 
			+ BLOCK + "=?";
	public static final String SELECT_BULLDOZTASK = "SELECT * FROM " + BULLDOZTASKT_ABLE + " WHERE " + BLOCK + " =@1"
			+ " AND " + LON + " BETWEEN @2 AND @3"
			+ " AND " + LAT + " BETWEEN @4 AND @5";
	public final static String SELECT_BULLDOZPOINT_ALL = "select * from " + BULLDOZPOINT_TABLE;
	public final static String SELECT_BULLDOZPOINT_BY_BLOCK = "select * from " + BULLDOZPOINT_TABLE + " where "
			+ BLOCK + "=?";
	public static final String SELECT_BULLDOZPOINT = "SELECT * FROM " + BULLDOZPOINT_TABLE + " WHERE "
			+ LON + " BETWEEN @1 AND @2"
			+ " AND " + LAT + " BETWEEN @3 AND @4";
	// 钻井下药炮点
	public static final String SELECT_DRILLPOINT_ORDERBY = "SELECT * FROM " + DRILLPOINT_TABLE + " ORDER BY ";
	public static final String SELECT_DRILLPOINT = "SELECT * FROM " + DRILLPOINT_TABLE + " WHERE " + HIDDEN + " = 0"
			+ " AND " + LON + " BETWEEN @1 AND @2"
			+ " AND " + LAT + " BETWEEN @3 AND @4";
	public static final String SELECT_DRILLPOINT_BY_POINTID = "SELECT * FROM " + DRILLPOINT_TABLE + " WHERE " + POINTID + " = ?";
	public static final String SELECT_DRILLPOINT_BY_STATION = "SELECT * FROM " + DRILLPOINT_TABLE + " WHERE " + STATIONNO + " = ?";
	public static final String SELECT_DRILLPOINT_IS_ALLDONE = "SELECT * FROM " + DRILLPOINT_TABLE + " WHERE " + ISDONE + " = ?";
	public static final String SELECT_DRILLPOINT_ALL = "SELECT * FROM " + DRILLPOINT_TABLE;
	public static final String SELECT_DRILLPOINT_BY_KEYWORD = "SELECT * FROM " + DRILLPOINT_TABLE + " WHERE " + STATIONNO + " like @1";
	// 井炮炮点
	public static final String SELECT_SHOTPOINT_ORDERBY = "SELECT * FROM " + SHOTPOINT_TABLE + " ORDER BY ";
	public static final String SELECT_SHOTPOIINT = "SELECT * FROM " + SHOTPOINT_TABLE + " WHERE " + HIDDEN + " = 0"
			+ " AND " + LON + " BETWEEN @1 AND @2"
			+ " AND " + LAT + " BETWEEN @3 AND @4";
	public static final String SELECT_SHOTPOINT_BY_POINTID = "SELECT * FROM " + SHOTPOINT_TABLE + " WHERE " + POINTID + " = ?";
	public static final String SELECT_SHOTPOINT_BY_STATION = "SELECT * FROM " + SHOTPOINT_TABLE + " WHERE " + STATIONNO + " = ?";
	public static final String SELECT_SHOTPOINT_IS_ALLDONE = "SELECT * FROM " + SHOTPOINT_TABLE + " WHERE " + ISDONE + " = ?";
	public static final String SELECT_SHOTPOINT_ALL = "SELECT * FROM " + SHOTPOINT_TABLE;
	public static final String SELECT_SHOTPOINT_BY_KEYWORD = "SELECT * FROM " + SHOTPOINT_TABLE + " WHERE " + STATIONNO + " like @1";
	public static final String SELECT_SHOTEPOINT_LINE = "SELECT DISTINCT " + LINENO + " FROM " + SHOTPOINT_TABLE;
	public static final String SELECT_SHOTPOINT_SPOINTNO = "SELECT DISTINCT " + SPOINTNO + " FROM " + SHOTPOINT_TABLE;
	// 钻井下药记录
	public static final String SELECT_DRILLRECORD_BY_STATION = "SELECT * FROM " + DRILLRECORD_TABLE + " WHERE " + STATIONNO + " = ?";
	public static final String SELECT_DRILLRECORD_BY_UNUPLOAD = "SELECT * FROM " + DRILLRECORD_TABLE + " WHERE " + ISUPLOAD + " = ?";
	public static final String SELECT_DRILLRECORD = "SELECT * FROM " + DRILLRECORD_TABLE + " WHERE "
			+ LON + " BETWEEN @1 AND @2"
			+ " AND " + LAT + " BETWEEN @3 AND @4";
	//质检记录
	public static final String SELECT_CHECKRECORD_BY_STATION = "SELECT * FROM " + CHECKRECORD_TABLE + " WHERE " + STATIONNO + " = ?";
	// 每日任务
	public static final String SELECT_DAILTTASK = "SELECT * FROM " + DAILY_TASK + " WHERE " + TYPE + "= @1";
	public static final String SELECT_DAILTTPOINT = "SELECT * FROM " + DAILY_POINT + " WHERE " + TYPE + "= @1";
	public static final String SELECT_DAILYPOINT_BY_DAILY = "SELECT * FROM " + DAILY_POINT + " WHERE " + TIME + " = @1 AND " + TYPE + " = @2";
	public static final String SELECT_DAILYPOINT_SHOT_DONE = "SELECT " + STATIONNO + " FROM " + DAILY_POINT + "," + SHOTPOINT_TABLE 
			+ " WHERE daily_point.time = @1 AND daily_point.stationNo = shot_point.stationNo AND shot_point.isdone = 1" ;
	public static final String SELECT_DAILYPOINT_DRILL_DONE = "SELECT " + STATIONNO + " FROM " + DAILY_POINT + "," + DRILLPOINT_TABLE 
			+ " WHERE daily_point.time = @1 AND daily_point.stationNo = drill_point.stationNo AND drill_point.isdone = 1" ;
	// 车辆四汇报
	public static final String SELECT_TRAVE_LAST = "SELECT * FROM " + TRAVE + " ORDER BY " + ID + " DESC LIMIT 1";
	public static final String SELECT_TRAVE_BY_STARTTIME = "SELECT * FROM " + TRAVE + " WHERE " + START_TIME + " = @1";
	public static final String SELECT_TRAVE_BY_ID = "SELECT * FROM " + TRAVE + " WHERE " + ID + " = @1";
	public static final String SELECT_CARNUM = "SELECT * FROM " + CARS;
	// 车辆钥匙管理
	public static final String SELECT_CARKEY_LAST = "SELECT * FROM " + CARKEYS + " ORDER BY " + ID + " DESC LIMIT 1";
	public static final String SELECT_CARKEY_BY_STARTTIME = "SELECT * FROM " + CARKEYS + " WHERE " + START_TIME + " = @1";
	public static final String SELECT_CARKEYS = "SELECT * FROM " + CARKEYS;
	// 搜索历史纪录
	public static final String SELECT_HISTORY = "SELECT * FROM " + SEARCH_HISTORY + " ORDER BY " + TIME + " DESC";
	public static final String SELECT_HISTORY_BY_TYPE = "SELECT * FROM " + SEARCH_HISTORY + " WHERE " + TYPE + " = @1 ORDER BY " + TIME + " DESC";
	// 排列
	public static final String SELECT_ARRANGEPOINT_ORDERBY = "SELECT * FROM " + ARRANGEPOINT_TABLE + " ORDER BY ";
	public static final String SELECT_ARRANGEPOINT_BY_POINTID = "SELECT * FROM " + ARRANGEPOINT_TABLE + " WHERE " + POINTID + " = ?";
	public static final String SELECT_ARRANGEPOINT_BY_STATION = "SELECT * FROM " + ARRANGEPOINT_TABLE + " WHERE " + STATIONNO + " = ?";
	public static final String SELECT_ARRANGEPOINT_ALL = "SELECT * FROM " + ARRANGEPOINT_TABLE;
	public static final String SELECT_ARRANGEPOINT_LINE = "SELECT DISTINCT " + LINENO + " FROM " + ARRANGEPOINT_TABLE;
	public static final String SELECT_ARRANGEPOINT_SPOINTNO = "SELECT DISTINCT " + SPOINTNO + " FROM " + ARRANGEPOINT_TABLE + " WHERE " + LINENO + "=@1";
	public static final String SELECT_ARRANGEPOINT_BY_KEYWORD = "SELECT * FROM " + ARRANGEPOINT_TABLE + " WHERE " + STATIONNO + " like @1";
	public static final String SELECT_ARRANGEPOINT = "SELECT * FROM " + ARRANGEPOINT_TABLE + " WHERE " + HIDDEN + " = 0"
			+ " AND " + LON + " BETWEEN @1 AND @2"
			+ " AND " + LAT + " BETWEEN @3 AND @4";
	public static final String SELECT_ARRANGEPOINT_IS_ALLDONE = "SELECT * FROM " + ARRANGEPOINT_TABLE + " WHERE " + ISDONE + " = ?";
	//任务
	public static final String SELECT_TASK_BY_TIME = "SELECT * FROM " +TASK_TABLE+" ORDER BY "+START_TIME+" DESC";
	public static final String DELETE_TASK_BY_ID = "DELETE FROM " + TASK_TABLE + " WHERE " + ID + " = @1";
	public final static String DELETE_ALLTASK = "delete from " + TASK_TABLE;
	//聊天记录
	public static final String SELECT_TALK_BY_TIME = "SELECT * FROM " +TALK_TABLE+" ORDER BY "+TALK_TIME+" ASC";
	public static final String DELETE_TALK_BY_ID = "DELETE FROM " + TALK_TABLE + " WHERE " + ID + " = @1";
	public static final String SELECT_TALK_BY_DEVICE = "SELECT * FROM " +TALK_TABLE+" WHERE "+TALK_DEVICE+" = @1 ORDER BY "+TALK_TIME+" ASC";
	public static final String DELETE_TALK_BY_DEVICE = "DELETE FROM " +TALK_TABLE+" WHERE "+TALK_DEVICE+" = @1";
	public final static String DELETE_ALLTALK = "delete from " + TALK_TABLE;

	/**
	 * 删除
	 */
	public final static String DELETE_PACKAGETNUM = "delete from " + PACKAGETNUM_TABLE;
	public final static String DELETE_BULLDOZTASK = "delete from " + BULLDOZTASKT_ABLE;
	public final static String DELETE_BULLDOZPOINT = "delete from " + BULLDOZPOINT_TABLE;
	// 钻井下药炮点
	public static final String DELETE_DRILLPOINT_BY_POINTID = "DELETE FROM " + DRILLPOINT_TABLE + " WHERE " + POINTID + " = @1";
	public static final String DELETE_DRILLPOINT = "DELETE FROM " + DRILLPOINT_TABLE;
	// 井炮炮点
	public static final String DELETE_SHOTPOINT_BY_POINTID = "DELETE FROM " + SHOTPOINT_TABLE + " WHERE " + POINTID + " = @1";
	public static final String DELETE_SHOTPOINT = "DELETE FROM " + SHOTPOINT_TABLE;
	// 钻井下药记录
	public static final String DELETE_DRILLRECORD_BY_STATION = "DELETE FROM " + DRILLRECORD_TABLE + " WHERE " + STATIONNO + " = @1";
	public static final String DELETE_DRILLRECORD = "DELETE FROM " + DRILLRECORD_TABLE;
	//质检记录
	public static final String DELETE_CHECKRECORD = "DELETE FROM " + CHECKRECORD_TABLE;
	// 每日任务
	public static final String DELETE_DAILYTASK = "DELETE FROM " + DAILY_TASK + " WHERE " + TYPE + " = @1";
	public static final String DELETE_DAILYTASK_BY_DAILY = "DELETE FROM " + DAILY_TASK + "WHERE " + TIME + " = @1 AND " + TYPE + " = @2";
	public static final String DELETE_DAILYPOINT = "DELETE FROM "+ DAILY_POINT + " WHERE " + TYPE + " = @1";
	public static final String DELETE_DAILYPOINT_BY_DAILY = "DELETE FROM " + DAILY_POINT + "WHERE " + TIME + " = @1 AND " + TYPE + " = @2";
	// 车辆四汇报
	public static final String DELETE_TRAVE = "DELETE FROM " + TRAVE;
	public static final String DELETE_CARS = "DELETE FROM " + CARS;
	public static final String DELETE_CAR_BY_CAENUM = "DELETE FROM " + CARS + " WHERE " + CARNUM + " = @1 ";
	// 车辆钥匙管理
	public static final String DELETE_CARKEY = "DELETE FROM " + CARKEYS;
	// 搜索历史记录
	public static final String DELETE_HISTORY = "DELETE FROM " + SEARCH_HISTORY;
	public static final String DELETE_HISTORY_BY_TYPE = "DELETE FROM " + SEARCH_HISTORY + " WHERE " + TYPE + " = @1";
	public static final String DELETE_HISTORY_BY_HISTORY = "DELETE FROM " + SEARCH_HISTORY + " WHERE " + HISTORY + " =?";
	// 排列
	public static final String DELETE_ARRANGEPOINT_BY_POINTID = "DELETE FROM " + ARRANGEPOINT_TABLE + " WHERE " + POINTID + " = @1";
	public static final String DELETE_ARRANGEPOINT = "DELETE FROM " + ARRANGEPOINT_TABLE;

	// 查排列记录
	public static final String SELECT_ARRANGERECORD_BY_STATION = "SELECT * FROM " + ARRANGERECORD_TABLE + " WHERE " + STATIONNO + " = ?";
	public static final String SELECT_ARRANGERECORD_BY_UNUPLOAD = "SELECT * FROM " + ARRANGERECORD_TABLE + " WHERE " + ISUPLOAD + " = ?";
	public static final String DELETE_ARRANGERECORD_BY_STATION = "DELETE FROM " + ARRANGERECORD_TABLE + " WHERE " + STATIONNO + " = @1";
	public static final String DELETE_ARRANGERECORD = "DELETE FROM " + ARRANGERECORD_TABLE;
	
	/**
	 * 更新
	 */
	public final static String UPDATE_BULLDOZPOINT = "update " + BULLDOZPOINT_TABLE + " set " 
			+ ISUPLOAD + "=?" + " where " + BLOCK + "=?"
			+ " and " + LON + "=? and " + LAT + "=?";
	public static final String UPDATE_POINTID = POINTID + " = @1";
	public static final String UPDATE_STATTION = STATIONNO + " = @1";
	public static final String UPDATE_TYPE = TYPE + " = @1";
	public final static String UPDATE_ID = "id = @1";
}
