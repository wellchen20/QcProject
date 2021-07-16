package com.mtkj.cnpc.protocol.constants;

/***
 * 配置参数
 * 
 * @author TNT
 * 
 */
public class SysConfig {
	
	public static boolean isDSCloud = true;
	
	/**
	 * 工作类型
	 */
	public static int workType = WorkType.WORK_TYPE_NONE;
	/**
	 *井炮源驱动模式 普通模式 0；中继模式 1；
	 */
	public static int shotSelectType = 0;
	/**
	 * 地图加载模式
	 */
	public static boolean isOnlineMap = true;
	
	/**
	 * 工区图层
	 */
	public static boolean isProjectShow = false;
	
	/**
	 * APP key
	 */
//	public static String APP_KEY = "NEH3sIur";// 廊坊key
//	public static String APP_KEY = "CJgr74Gd";// 辽河key
//	public static String APP_KEY = "XN100001";// 西南key
	public static String APP_KEY = "MINGS001";// 测试key
	
	/***
	 * 图层小显示比例尺
	 */
	public static double LAYER_MIN_SCALE_SHOW = 10000.0d;

	/***
	 * 墨卡托转北京54距离变换参数
	 */
	public static final double M2B = 1.44;

	/** 上传时间间隔 */
	public static int GPS_UP_TIME_TIP = 60;

	/***
	 * 任务类型
	 */
	public static int TaskType = 0;

	/***
	 * 任务模式
	 */
	public static boolean isTaskMode = false;

	/***
	 * 当前人员信息
	 */
	public static String usersInfo = "";

	/**
	 * 是否为测试版本
	 */
	public static boolean isTestMode = false;

	public static String DEV_ID = "";

	/***
	 * 电台-IP
	 */
	//public static String IP = "10.10.100.254";
	/**
	 * neiwang
	 * */
	//public static String IP = "11.8.43.136";
	/**
	 * waiwang
	 * */
//	public static String IP = "219.148.142.230";
	public static String IP = "192.168.43.121";
	public static String UPDATEURL = "http://106.38.74.187:4026";
//	public static String BaseUrl = "http://106.38.74.187:4017";
	public static String BaseUrl = "http://219.148.142.230:8001";
//public static String BaseUrl = "http://192.168.43.51:8001";
	/**
	 * 电台端口
	 */
//	public static int PORT = 8899;
	public static int PORT = 8001;
	public static String url = "http://106.38.74.187:4029/dzd/";


	public static String WSC = "WSC_01";
	public static String DSCLOUD = "dscloud";
	/***
	 * 爆炸机编号
	 */
	public static String HANDSET = "handset";
	public static String DRILLSET = "drillset";
	public static String SC_ID = "1";
	public static String SC = "";
	
	/**
	 * 手持编号
	 */
	public static String BZJ_ID = "1";
	/**
	 * 组织机构
	 */
	public static String ZZJG_ID = "1";

	/***
	 * 桩号匹配最大距离,最佳距离（MAX）,最佳距离（MIN）,可选距离（MAX）,可选距离（MIN）,安全距离
	 */
	public static float ShotproMax = 15;
	/**安全距离**/
	public static float safe_Distance = 15;
	/**排队等待超时**/
	public static float Readytimeout = 40;
	/**能量衰减时间**/
	 public static float PowerTimeout = 25;
	
	/***
	 * 周边查询任务点默认距离
	 */
	public static float AroundQueryMaxDistance = 50;

	/**
	 * 桩号选择规则
	 */
	public static int SelectRule = RULE.RULE_0;
	/**
	/**
	 * 桩号选择规则-模式
	 */
	public static int SelectRuleMode = 0;
	/**
	/**
	 * 桩号选择规则-编号问题
	 */
	public static int SelectRuleMethod = 0;
	/**
	 * 桩号选择规则-线号差值
	 */
	public static int SelectRuleLine = 1;
	/**
	 * 桩号选择规则-点号差值
	 */
	public static int SelectRulePoint = 1;

	/***
	 * 大小号选择规则
	 * 
	 * @author TNT
	 * 
	 */
	public interface RULE {
		/** 线号不变-点号增长 */
		int RULE_0 = 0;
		/** 线号不变-点号减小 */
		int RULE_1 = 1;
		/** 线号增长-点号不变 */
		int RULE_2 = 2;
		/** 线号减小-点号不变 */
		int RULE_3 = 3;
		/** 线号增长-点号增长 */
		int RULE_4 = 4;
		/** 线号增长-点号减小 */
		int RULE_5 = 5;
		/** 线号减小-点号增长 */
		int RULE_6 = 6;
		/** 线号减小-点号减小 */
		int RULE_7 = 7;
	}

	/***
	 * 根据规则获得下一个任务线号
	 * @param lineNumber
	 * @return
	 */
	public static float getLineNumber(float lineNumber) {
		switch (SelectRule) {
		case RULE.RULE_0:
		case RULE.RULE_1:
			return lineNumber;
		case RULE.RULE_2:
		case RULE.RULE_4:
		case RULE.RULE_5:
			return lineNumber + SelectRuleLine;
		case RULE.RULE_3:
		case RULE.RULE_6:
		case RULE.RULE_7:
			return lineNumber - SelectRuleLine;
		default:
			return lineNumber;
		}
	}
	
	/***
	 * 根据规则获得下一个任务点号
	 * @param pointNumber
	 * @return
	 */
	public static float getPointNumber(float pointNumber) {
		switch (SelectRule) {
		case RULE.RULE_2:
		case RULE.RULE_3:
			return pointNumber;
		case RULE.RULE_0:
		case RULE.RULE_4:
		case RULE.RULE_6:
			return pointNumber + SelectRuleLine;
		case RULE.RULE_1:
		case RULE.RULE_5:
		case RULE.RULE_7:
			return pointNumber - SelectRuleLine;
		default:
			return pointNumber;
		}
	}
	
	public interface WorkType {
		public final int WORK_TYPE_NONE = 0x001;
		public final int WORK_TYPE_SHOT = 0x002;
		public final int WORK_TYPE_DRILE = 0x003;
		public final int WORK_TYPE_PERMUTATION = 0x004;
		public final int WORK_TYPE_BULLDOZE = 0x005;
		public final int WORK_TYPE_VEHICLE = 0X06;
		public final int WORK_TYPE_ARRANGE = 0x07;
	}
}
