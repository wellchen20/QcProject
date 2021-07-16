package com.mtkj.cnpc.activity.utils;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2019/4/11.
 */

public class ShotTaskEntity implements Serializable{

    private List<PointsBean> points;

    public List<PointsBean> getPoints() {
        return points;
    }

    public void setPoints(List<PointsBean> points) {
        this.points = points;
    }

    public static class PointsBean {
        /**
         * stationno : 51991734
         * linenno : 5199
         * pointno : 1734
         * lon : 103.6271093
         * lat : 30.60134601
         * height : 0
         */

        private String stationno;
        private String linenno;
        private String pointno;
        private double lon;
        private double lat;
        private int height;

        public String getStationno() {
            return stationno;
        }

        public void setStationno(String stationno) {
            this.stationno = stationno;
        }

        public String getLinenno() {
            return linenno;
        }

        public void setLinenno(String linenno) {
            this.linenno = linenno;
        }

        public String getPointno() {
            return pointno;
        }

        public void setPointno(String pointno) {
            this.pointno = pointno;
        }

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }
}
