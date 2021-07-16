package com.robert.maps.applib.trackwriter;

import java.util.ArrayList;
import java.util.List;

import org.andnav.osm.util.GeoPoint;

import android.location.Location;

public class LocationConform {

	private static List<Location> mLocations = new ArrayList<Location>();
	public static int DISTANCE = 10;
	public static int MAXSIZE = 10;
	
	/**
	 * 设置距离
	 * 
	 * @param dISTANCE
	 */
	public static void setDISTANCE(int dISTANCE) {
		DISTANCE = dISTANCE;
	}

	/**
	 * 链表大小
	 * 
	 * @param mAXSIZE
	 */
	public static void setMAXSIZE(int mAXSIZE) {
		MAXSIZE = mAXSIZE;
	}

	public static boolean isConform(Location location) {
		boolean isconform = false;
		if (mLocations != null && mLocations.size() > 0) {
			GeoPoint locationGeoPoint = GeoPoint.fromDouble(location.getLatitude(), location.getLongitude());
			for (int i = 0; i < mLocations.size(); i++) {
				Location loc = mLocations.get(i);
				GeoPoint point = GeoPoint.fromDouble(loc.getLatitude(), loc.getLongitude());
				if (10 < locationGeoPoint.distanceTo(point)) {
					isconform = true;
					mLocations.remove(i);
					mLocations.add(loc);
					return isconform;
				}
			}
			addLocation(location);
		} else {
			isconform = true;
			addLocation(location);
		}
		return isconform;
	}
	
	public static void addLocation(Location location) {
		if (mLocations != null && mLocations.size() >= MAXSIZE) {
			mLocations.remove(0);
			mLocations.add(location);
		} else if (mLocations != null && mLocations.size() < MAXSIZE) {
			mLocations.add(location);
		}		
	}
	
}
