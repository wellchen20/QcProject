package com.mtkj.cnpc.protocol.bean;

import java.io.Serializable;

import org.andnav.osm.util.GeoPoint;

public class Arrange implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3852322844237594527L;
	public static final int EMPTY_ID = -777;
	public int Id;
	public String stationNo;
	public String lineNo;
	public String spointNo;
	public GeoPoint geoPoint;
	
	public Arrange(int id, String stationNo, String lineNo, String spointNo, GeoPoint mGeoPoint) {
		this.Id = id;
		this.stationNo = stationNo;
		this.lineNo = lineNo;
		this.spointNo = spointNo;
		this.geoPoint = mGeoPoint;
	}
	
	public Arrange() {
		this(EMPTY_ID, "", "", "", null);
	}

	public int getId() {
		return Id;
	}

	public static int EMPTY_ID(){
		return EMPTY_ID;
	}
}
