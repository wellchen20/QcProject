package com.mtkj.utils.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2019/1/2.
 */

public class PointDemo implements Serializable {

    private List<DataBean> data;

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * stationNo : 50011001
         * lineNo : 5001
         * spointNo : 1001
         * lat : 30.85368489
         * lon : 105.0418692
         * alt : 0
         * wellnum : 1
         * desWellDepth : 12
         * bombWeight : 6
         * detonator : 1
         */

        private String stationNo;
        private String lineNo;
        private String spointNo;
        private double lat;
        private double lon;
        private double alt;
        private double wellnum;
        private double desWellDepth;
        private float bombWeight;
        private double detonator;

        public String getStationNo() {
            return stationNo;
        }

        public void setStationNo(String stationNo) {
            this.stationNo = stationNo;
        }

        public String getLineNo() {
            return lineNo;
        }

        public void setLineNo(String lineNo) {
            this.lineNo = lineNo;
        }

        public String getSpointNo() {
            return spointNo;
        }

        public void setSpointNo(String spointNo) {
            this.spointNo = spointNo;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }

        public double getAlt() {
            return alt;
        }

        public void setAlt(int alt) {
            this.alt = alt;
        }

        public double getWellnum() {
            return wellnum;
        }

        public void setWellnum(int wellnum) {
            this.wellnum = wellnum;
        }

        public double getDesWellDepth() {
            return desWellDepth;
        }

        public void setDesWellDepth(double desWellDepth) {
            this.desWellDepth = desWellDepth;
        }

        public float getBombWeight() {
            return bombWeight;
        }

        public void setBombWeight(float bombWeight) {
            this.bombWeight = bombWeight;
        }

        public double getDetonator() {
            return detonator;
        }

        public void setDetonator(double detonator) {
            this.detonator = detonator;
        }
    }
}
