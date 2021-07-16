package com.mtkj.cnpc.protocol.bean;

import java.io.Serializable;

import org.andnav.osm.util.GeoPoint;

/**
 * 兴趣点
 * 
 * @author DRH
 *
 */
public class DrillPoint extends TaskPoint implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5536679943640707144L;
	public double wellnum;
	public double desWellDepth;
	public float bombWeight;
	public double detonator;
	public boolean Hidden;
	public String time;

	public DrillPoint(int id, String stationNo, String lineNo, String spointNo,
			GeoPoint mGeoPoint, double alt, double wellnum, double desWellDepth,
			float bombWeight ,double detonator, int hidden, int isDone) {
		this.Id = id;
		this.stationNo = stationNo;
		this.lineNo = lineNo;
		this.spointNo = spointNo;
		this.geoPoint = mGeoPoint;
		this.Alt = alt;
		this.wellnum = wellnum;
		this.desWellDepth = desWellDepth;
		this.bombWeight = bombWeight;
		this.detonator = detonator;
		this.Hidden = hidden == 1 ? true : false;
		this.isDone = isDone == 1 ? true : false;
	}
	
	public DrillPoint() {
		this(EMPTY_ID, "", "", "", null, 0, 0, 0, 0, 0, 0, 0);
	}

	public int getId() {
		return Id;
	}

	public static int EMPTY_ID(){
		return EMPTY_ID;
	}

}
