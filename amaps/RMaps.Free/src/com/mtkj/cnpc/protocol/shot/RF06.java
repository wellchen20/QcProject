package com.mtkj.cnpc.protocol.shot;

import java.text.DecimalFormat;

import org.andnav.osm.util.GeoPoint;

import android.location.Location;

import com.mtkj.cnpc.activity.MainActivity;
import com.mtkj.cnpc.protocol.SendBody;
import com.mtkj.cnpc.protocol.constants.ProtocolConstants;
import com.mtkj.cnpc.protocol.utils.SocketUtils;
import com.robert.maps.applib.utils.TimeUtil;

/***
 * 固定间隔发送位置坐标  <br>
 * 格式:<br>
 * RF06;手持id;当前坐标位置;<br>
 * 例如:<br>
 * RF06;1;
 * $GPGGA,042820,4500.9950,N,08838.9532,E,4,09,0,+0000.0,M,,,*;<br>
 * 
 * 
 * $GPGGA,<1>,<2>,<3>,<4>,<5>,<6>,<7>,<8>,<9>,M,<10>,M,<11>,<12>*hh<CR><LF>
 * 传送的信息说明如下： $GPGGA： 起始引导符及语句格式说明（本句为GPS定位数据） <1> UTC时间，时时分分秒秒格式 <2>
 * 纬度，度度分分.分分分分格式（第一位是零也将传送） <3> 纬度半球，N或S（北纬或南纬） <4> 经度，度度分分.分分分分格式（第一位零也将传送）
 * <5> 经度半球，E或W（东经或西经） <6> GPS质量指示，0=方位无法使用，1=非差分GPS获得方位，2=差分方式获得方位（DGPS），6=估计获得
 * <7> 使用卫星数量，从00到12（第一个零也将传送） <8> 水平精确度，0.5到99.9 <9> 天线离海平面的高度，-9999.9到9999.9米
 * M 指单位米 <10> 大地水准面高度，-999.9到9999.9米 M 指单位米 <11> 差分GPS数据期限（RTCM
 * SC-104），最后设立RTCM传送的秒数量（如无DGPS为0） <12> 差分参考基站标号，从0000到1023（首位0也将传送。如无DGPS为0）
 * 语句结束标志符 hh 从$开始的所有ASCII码的校验和
 * 
 * @author drh
 * 
 */
public class RF06 extends SendBody {
	// String strTest1 =
	// "$GPGGA,080525,3928.9236,N,11601.7376,E,1,04,2,-0008.8,M,,,*";
	// String strTest2 =
	// "$GPGGA,080525,3929.0394,N,11601.5654,E,1,04,2,+0000.8,M,,,*";

	// $GPGGA,<1>,<2>,<3>,<4>,<5>,<6>,<7>,<8>,<9>,M,<10>,M,<11>,<12>*hh<CR><LF>
	// 传送的信息说明如下：
	// $GPGGA： 起始引导符及语句格式说明（本句为GPS定位数据）
	// <1> UTC时间，时时分分秒秒格式
	// <2> 纬度，度度分分.分分分分格式（第一位是零也将传送）
	// <3> 纬度半球，N或S（北纬或南纬）
	// <4> 经度，度度分分.分分分分格式（第一位零也将传送）
	// <5> 经度半球，E或W（东经或西经）
	// <6> GPS质量指示，0=方位无法使用，1=非差分GPS获得方位，2=差分方式获得方位（DGPS），6=估计获得
	// <7> 使用卫星数量，从00到12（第一个零也将传送）
	// <8> 水平精确度，0.5到99.9
	// <9> 天线离海平面的高度，-9999.9到9999.9米
	// M 指单位米
	// <10> 大地水准面高度，-999.9到9999.9米
	// M 指单位米
	// <11> 差分GPS数据期限（RTCM SC-104），最后设立RTCM传送的秒数量（如无DGPS为0）
	// <12> 差分参考基站标号，从0000到1023（首位0也将传送。如无DGPS为0）
	// * 语句结束标志符
	// hh 从$开始的所有ASCII码的校验和

	/** 历史GPGGA */
	public static String HisGPGGA = "";
	/** 历史GNGGA */
	public static String HisGNGGA = "";

	/***
	 * 初始化请求排队信息
	 * 
	 * @param SCNumber
	 *            :手持编号
	 * @param gpsInfo
	 *            :设备编号（导航编号）
	 */
	public RF06(String SCNumber, Location gpsInfo) {
		super();
		setType(ProtocolConstants.NAME_JINGPAO.RF06);

		// 转换为GGSA格式
		String loc1 = null;
		String loc2 = null;
		if (MainActivity.GNGGA != null && MainActivity.GNGGA.length() > 0) {
			if (HisGNGGA != null && HisGNGGA.length() > 0) {
//				gpsInfo.GNGGA = HisGPGGA;
				HisGNGGA = MainActivity.GNGGA;
			}
		}
		if (MainActivity.GPGGA != null && MainActivity.GPGGA.length() > 0) {
			if (HisGPGGA != null && HisGPGGA.length() > 0) {
//				gpsInfo.GNGGA = HisGPGGA;
				HisGPGGA = MainActivity.GPGGA;
			}
		}
		GeoPoint gpsPoint = GeoPoint.fromDouble(gpsInfo.getLatitude(), gpsInfo.getLongitude());
		if (MainActivity.GNGGA != null && MainActivity.GNGGA.length() > 0) {
			// MLog.e("GPGGA-origen", gpsInfo.GPGGA);
			loc1 = point2GPGGA(MainActivity.GNGGA, gpsPoint);
		} else if (MainActivity.GPGGA != null && MainActivity.GPGGA.length() > 0) {
			loc1 = point2GPGGA(MainActivity.GPGGA, gpsPoint);
		} else {
			// MLog.e("GPGGA-null", "GPGGA-NULL");
			loc1 = wgs84ToGPGGA(gpsPoint);
		}
		// MLog.e("GPGGA-loc1", loc1);
		// MLog.e("GPGGA-loc2", loc2);
		// 生成协议字符串
		String content = ProtocolConstants.NAME_JINGPAO.RF06
				+ ProtocolConstants.SPLITE_SYMBLE + SCNumber
				+ ProtocolConstants.SPLITE_SYMBLE + loc1
				+ ProtocolConstants.SPLITE_SYMBLE;
		//MLog.e("GPGGA", content);
		setContent(content);
	}

	/****
	 * 根据当前GPGGA生成固定的GPGGA串
	 * 
	 * @param GPGGA
	 * @param point2d
	 * @return
	 */
	public String point2GPGGA(String GPGGA, GeoPoint geoPoint) {
		if (GPGGA != null && GPGGA.length() > 0) {
			String[] arrDatas = GPGGA.split(",");
			if (arrDatas.length == 15) {
				String[] newLocStrings = toGPGGA84(geoPoint);
				if(arrDatas[1].contains(".")){
					arrDatas[1] = arrDatas[1].substring(0, arrDatas[1].indexOf("."));
				}
				if(arrDatas[7].length() == 1){
					arrDatas[7] = "0" + arrDatas[7];
				}
				try {
					float alt = Float.parseFloat(arrDatas[9]);
					if(alt > 0){
						alt = 10000 + alt;
						arrDatas[9] = "+" + (alt+"").substring(1, (alt+"").length());
					}else if(alt < 0){
						alt = 10000 - alt;
						arrDatas[9] = "-" + (alt+"").substring(1, (alt+"").length());
					}else{
						arrDatas[9] = "+0000.0";
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
				//int utcTime = (int)(Double.parseDouble(arrDatas[1]));
				return "$GPGGA," 
				+ arrDatas[1] + ","
				+ newLocStrings[0] + ","
				+ arrDatas[3] + ","
				+ newLocStrings[1] + ","
				+ arrDatas[5] + ","
				+ arrDatas[6] + ","
				+ arrDatas[7] + ","
				+ SocketUtils.StringToInt(arrDatas[8]) +","
				+ arrDatas[9]+ ","
				+ "M,,,*";
			}
		}
		return null;
	}

	public String wgs84ToGPGGA(GeoPoint geoPoint) {
		String[] gpsStrings = toGPGGA84(geoPoint);
		return String.format(ProtocolConstants.GPGGA_FORMAT,
				TimeUtil.getCurrentUtcTime(), gpsStrings[0], gpsStrings[1]);
	}

	public String[] toGPGGA84(GeoPoint geoPoint) {
		String[] results = new String[2];
		int duY = (int) geoPoint.getLatitude();
		int duX = (int) geoPoint.getLongitude();

		double fenY = ((geoPoint.getLatitude() - duY) * 60);
		double fenX = ((geoPoint.getLongitude() - duX) * 60);

		double newLat = duY * 100 + fenY;
		double newLon = duX * 100 + fenX;
		
		// 转换为GGSA格式
		DecimalFormat formatLatitude = new DecimalFormat("0000.0000");
		DecimalFormat formatLontitude = new DecimalFormat("00000.0000");

		results[0] = formatLatitude.format(newLat);
		results[1] = formatLontitude.format(newLon);
		return results;
	}
}
