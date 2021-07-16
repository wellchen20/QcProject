package com.robert.maps.applib.utils;

/**
 *    * 各地图API坐标系统比较与转换; 
 *  * WGS84坐标系：即地球坐标系，国际上通用的坐标系。设备一般包含GPS芯片或者北斗芯片获取的经纬度为WGS84地理坐标系, 
 *  * 谷歌地图采用的是WGS84地理坐标系（中国范围除外）; 
 *  * GCJ02坐标系：即火星坐标系，是由中国国家测绘局制订的地理信息系统的坐标系统。由WGS84坐标系经加密后的坐标系。 
 *  * 谷歌中国地图和搜搜中国地图采用的是GCJ02地理坐标系; BD09坐标系：即百度坐标系，GCJ02坐标系经加密后的坐标系; 
 *  * 搜狗坐标系、图吧坐标系等，估计也是在GCJ02基础上加密而成的。    
 */
public class GpsCorrect {
	private final static double pi = 3.1415926535897932384626;
	private final static double cross = 6378245.0;
	private final static double ratio = 0.00669342162296594323;

	/**
	 * 84 to 火星坐标系 (GCJ-02) World Geodetic System ==> Mars Geodetic System 
	 * 
	 * @param wgLat
	 * @param wgLon
	 * @param latlon
	 */
	public static Gps GPS84_TO_GCJ02(Gps wgGps) {
		double wgLat = wgGps.lat;
		double wgLon = wgGps.lon;
		if (outChina(wgLat, wgLon)) {
			return new Gps(wgLat, wgLon);
		}

		double dLat = transformLat(wgLon - 105.0, wgLat - 35.0);
		double dLon = transformLon(wgLon - 105.0, wgLat - 35.0);
		double radLat = wgLat / 180.0 * pi;
		double magic = Math.sin(radLat);
		magic = 1 - ratio * magic * magic;
		double sqrtMagic = Math.sqrt(magic);
		dLat = (dLat * 180.0)
				/ ((cross * (1 - ratio)) / (magic * sqrtMagic) * pi);
		dLon = (dLon * 180.0) / (cross / sqrtMagic * Math.cos(radLat) * pi);
		double lat = wgLat + dLat;
		double lon = wgLon + dLon;
		return new Gps(lat, lon);
	}

	/**
	 * 火星坐标系 (GCJ-02) to 84
	 * 
	 * @param gcjGps
	 * @return
	 */
	public static Gps GCJ02_TO_GPS84(Gps gcjGps) {
		Gps Gps = GPS84_TO_GCJ02(gcjGps);
		double lon = gcjGps.lon * 2 - Gps.lon;
		double lat = gcjGps.lat * 2 - Gps.lat;
		return new Gps(lat, lon);
	}

	/**
	 * 将 GCJ-02 坐标转换成 BD-09 坐标
	 * 
	 * @param gcjGps
	 * @return
	 */
	public static Gps GCJ02_TO_BD09(Gps gcjGps) {
		double x = gcjGps.lon;
		double y = gcjGps.lat;
		double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * pi);
		double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * pi);
		double lon = z * Math.cos(theta) + 0.0065;
		double lat = z * Math.sin(theta) + 0.006;
		return new Gps(lat, lon);
	}

	/**
	 *  将 BD-09 坐标转换成GCJ-02 坐标
	 * 
	 * @param bdGps
	 * @return
	 */
	public static Gps BD09_TO_GCJ02(Gps bdGps) {
		double x = bdGps.lon - 0.0065;
		double y = bdGps.lat - 0.006;
		double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * pi);
		double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * pi);
		double lon = z * Math.cos(theta);
		double lat = z * Math.sin(theta);
		return new Gps(lat, lon);
	}
	
	/**
	 * (BD-09)-->84 
	 * 
	 * @param bdGps
	 * @return
	 */
	public static Gps BD09_TO_GPS84(Gps bdGps) {
		Gps gcjGps = BD09_TO_GCJ02(bdGps);
		Gps wgGps = GCJ02_TO_GPS84(gcjGps);
		return wgGps;
	}

	private static boolean outChina(double lat, double lon) {
		if (lon < 72.004 || lon > 137.8347) {
			return true;
		}
		if (lat < 0.8293 || lat > 55.8271) {
			return true;
		}
		return false;
	}

	public static void transform(double lat, double lon , double[] latlon) {
		double dLat = transformLat(lon - 105.0, lat - 35.0);
		double dLon = transformLon(lon - 105.0, lat - 35.0);
		double radLat = lat / 180.0 * pi;
		double magic = Math.sin(radLat);
		magic = 1 - ratio * magic * magic;
		double sqrtMagic = Math.sqrt(magic);
		dLat = (dLat * 180.0) / ((cross * (1 - ratio)) / (magic * sqrtMagic) * pi);
		dLon = (dLon * 180.0) / (cross / sqrtMagic * Math.cos(radLat) * pi);
		double mgLat = lat + dLat;
		double mgLon = lon + dLon;
		latlon[0] = mgLat;
		latlon[1] = mgLon;
	}

	private static double transformLat(double x, double y) {
		double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
				+ 0.2 * Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
		ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
		return ret;
	}

	private static double transformLon(double x, double y) {
		double ret = 300 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1
				* Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
		ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0
				* pi)) * 2.0 / 3.0;
		return ret;
	}
	
	/**
	 * 保留6位小數
	 * 
	 * @param num
	 * @return
	 */
	private static double retain6(double num) {
		String result = String.format("%.6f", num);
		return Double.valueOf(result);
	}
	
	public static class Gps {
		public double lon;
		public double lat;

		public Gps(double lat, double lon) {
			this.lon = lon;
			this.lat = lat;
		}
	}
}