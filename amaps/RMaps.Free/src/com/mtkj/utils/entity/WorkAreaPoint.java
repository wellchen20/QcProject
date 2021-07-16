package com.mtkj.utils.entity;

import org.andnav.osm.util.GeoPoint;

import java.io.Serializable;

/**
 * Created by CW
 */

public class WorkAreaPoint implements Serializable{
    String name;
    GeoPoint point;
    public WorkAreaPoint(String name, GeoPoint point) {
        this.name = name;
        this.point = point;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GeoPoint getPoint() {
        return point;
    }

    public void setPoint(GeoPoint point) {
        this.point = point;
    }
}