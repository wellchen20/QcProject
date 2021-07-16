package com.mtkj.utils.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2019/3/19.
 */

public class DrillTaskEntity implements Serializable{

    private List<PointsBean> points;

    public List<PointsBean> getPoints() {
        return points;
    }

    public void setPoints(List<PointsBean> points) {
        this.points = points;
    }

    public static class PointsBean {
        /**
         * stationno : 52131700
         * linenno : 5213
         * pointno : 1700
         * lon : 103.6237917
         * lat : 30.59357421
         * height : 710239.9229
         * drilldepth : 12.0
         */

        private String stationno;
        private String linenno;
        private String pointno;
        private double lon;
        private double lat;
        private double height;
        private double drilldepth;

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

        public double getHeight() {
            return height;
        }

        public void setHeight(double height) {
            this.height = height;
        }

        public double getDrilldepth() {
            return drilldepth;
        }

        public void setDrilldepth(double drilldepth) {
            this.drilldepth = drilldepth;
        }
    }
}
