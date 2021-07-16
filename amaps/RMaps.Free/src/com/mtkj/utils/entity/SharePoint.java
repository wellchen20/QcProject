package com.mtkj.utils.entity;

import org.andnav.osm.util.GeoPoint;

import java.io.Serializable;

/**
 * Created by Administrator on 2019/3/15.
 */

public class SharePoint implements Serializable{
    String name;
    GeoPoint point;
    public SharePoint(String name, GeoPoint point) {
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