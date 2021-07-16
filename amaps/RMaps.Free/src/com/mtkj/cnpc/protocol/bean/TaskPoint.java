package com.mtkj.cnpc.protocol.bean;

import java.io.Serializable;

import org.andnav.osm.util.GeoPoint;

public class TaskPoint implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2409420463304634381L;
	
	public static final int EMPTY_ID = -777;
	public int Id;
	public String stationNo;
	public String lineNo;
	public String spointNo;
	public GeoPoint geoPoint;
	public double Alt;
	public boolean isDone;
}
