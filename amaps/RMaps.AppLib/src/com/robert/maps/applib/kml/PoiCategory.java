package com.robert.maps.applib.kml;

import com.robert.maps.applib.kml.constants.PoiConstants;

/**
 * 兴趣点类别
 * 
 * @author DRH
 *
 */
public class PoiCategory implements PoiConstants {
	private final int Id;
	public String Title;
	public boolean Hidden;
	public int IconId;
	public int MinZoom;// 最小显示级数

	public PoiCategory(int id, String title, boolean hidden, int iconid, int minzoom) {
		super();
		Id = id;
		Title = title;
		Hidden = hidden;
		IconId = iconid;
		MinZoom = minzoom;
	}

	public PoiCategory() {
		this(PoiConstants.EMPTY_ID, "", false, 0, 14);
	}

	public PoiCategory(String title) {
		this(PoiConstants.EMPTY_ID, title, false, 0, 14);
	}

	public int getId() {
		return Id;
	}

}
