package com.mtkj.cnpc.protocol.bean;

import org.andnav.osm.util.GeoPoint;

import java.io.Serializable;

/**
 * 查排列
 * 
 * @author CW
 *
 */
public class ArrangePoint extends TaskPoint implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5536679943640707144L;
	public boolean Hidden;
	public String time;
	public String remark;
	public int dis = 0;

	public ArrangePoint(int id, String stationNo, String lineNo, String spointNo,String time,String remark,
                        GeoPoint mGeoPoint, int hidden, int isDone) {
		this.Id = id;
		this.stationNo = stationNo;
		this.lineNo = lineNo;
		this.spointNo = spointNo;
		this.time = time;
		this.remark = remark;
		this.geoPoint = mGeoPoint;
		this.Hidden = hidden == 1 ? true : false;
		this.isDone = isDone == 1 ? true : false;
	}

	public ArrangePoint() {
		this(EMPTY_ID, "", "", "","","" ,null, 0,  0);
	}

	public int getId() {
		return Id;
	}

	public static int EMPTY_ID(){
		return EMPTY_ID;
	}

}
