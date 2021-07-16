package com.mtkj.utils.entity;

import java.util.List;

public class WorkAreaEntity {
    private List<WorkAreaBean> work_area;

    public List<WorkAreaBean> getWork_area() {
        return work_area;
    }

    public void setWork_area(List<WorkAreaBean> work_area) {
        this.work_area = work_area;
    }

    public static class WorkAreaBean {
        /**
         * lat : 30.85368489
         * lon : 105.0418692
         */

        private double lat;
        private double lon;

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
    }
}
