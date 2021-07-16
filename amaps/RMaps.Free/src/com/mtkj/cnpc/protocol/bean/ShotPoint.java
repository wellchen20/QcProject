package com.mtkj.cnpc.protocol.bean;

import java.io.Serializable;

import org.andnav.osm.util.GeoPoint;

/**
 * 兴趣点
 * 
 * @author DRH
 *
 */
public class ShotPoint extends TaskPoint implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6794537055622394866L;
	public boolean Hidden = false;
	public String time;
	public int dis = 0;
	public String remark = "";
	public String result = "";

	public ShotPoint(int id, String stationNo, String lineNo, String spointNo,
			GeoPoint mGeoPoint, double alt, int hidden, int isDone) {
		this.Id = id;
		this.stationNo = stationNo;
		this.lineNo = lineNo;
		this.spointNo = spointNo;
		this.geoPoint = mGeoPoint;
		this.Alt = alt;
		this.Hidden = hidden == 1 ? true : false;
		this.isDone = isDone == 1 ? true : false;
	}
	
	public ShotPoint() {
		this(EMPTY_ID, "", "", "", null, 0, 0, 0);
	}

	public int getId() {
		return Id;
	}

	public static int EMPTY_ID(){
		return EMPTY_ID;
	}

}
