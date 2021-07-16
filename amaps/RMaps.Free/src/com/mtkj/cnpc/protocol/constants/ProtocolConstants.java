package com.mtkj.cnpc.protocol.constants;

/***
 * 协议常量
 * 
 * @author TNT
 * 
 */
public interface ProtocolConstants {
//	public final static String GPGGA_FORMAT = "$GPGGA,080525,%s,N,%s,E,1,04,2,-0008.8,M,,,*";
	public final static String GPGGA_FORMAT = "$GPGGA,%s,%s,N,%s,E,1,05,0,+0000.0,M,,,*";
	public static String[] allProtocols = null;
	/** 内容分隔符--# */
	public final static String SPLITE_SYMBLE = ";";

	/***
	 * 标志位
	 * 
	 * @author TNT
	 * 
	 */
	public interface FLAG {
		String SUCCESS = "1";
		String FAILED = "0";
	}

	// /***
	// * 井炮作业协议类型
	// *
	// * @author TNT
	// *
	// */
	// public enum TYPE{
	// RF01, RF02, RF03, GK01, GK02, GK03, GK04, GK05
	// }

	/***
	 * 井炮作业协议名称
	 * 
	 * @author TNT
	 * 
	 */
	public interface NAME_JINGPAO {
		// /--------窥探相关协议--------------------
		/** 窥探  **/
		public static String RF01 = "RF01";
		/** 窥探反馈  **/
		public static String GK01 = "GK01";
		// /#######################################
		// /--------配对相关协议--------------------
		/** 请求配对  **/
		public static String RF02 = "RF02";
		/** 请求取消配对  **/
		public static String RF03 = "RF03";
		/** 请求配对反馈  **/
		public static String GK02 = "GK02";
		/** 请求取消配对反馈   **/
		public static String GK03 = "GK03";
		// /#######################################
		// /-------------排队相关协议----------------
		/** 请求排队 **/
		public static String RF04 = "RF04";
		/** 请求取消排队 **/
		public static String RF05 = "RF05";
		/** 请求取消排队反馈  **/
		public static String GK04 = "GK04";
		/** 请求取消排队反馈   **/
		public static String GK05 = "GK05";
		// /#######################################
		// /-------------位置坐标----------------
		/** 发送位置坐标 **/
		public static String RF06 = "RF06";
		// /#######################################
		// /-------------参数配置----------------
		/** 请求配置信息 **/
		public static String RF08 = "RF08";

		/** 请求配置信息 **/
		public static String RF12 = "RF12";

		/** 发送中继模式ready信号 **/
		public static String RF13 = "RF13";

		/** 参数反馈 **/
		public static String GK08 = "GK08";
		// /#######################################
		// /-------------信号----------------
		/** 充电提示  **/
		public static String GK09 = "GK09";
		/** 充电超时/起爆超时  **/
		public static String GK10 = "GK10";
		/** FPS完成  **/
		public static String GK11 = "GK11";
		/**接受放炮模式修改*/
		public static String GK12 = "GK12";
	}

}
