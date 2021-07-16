package com.mtkj.utils.entity;

import java.io.Serializable;
import java.util.List;

public class UploadEntity implements Serializable {

    /**
     * station : 223123
     * name : 陈威
     * time : 123213123
     * lat : 2.2123123
     * lon : 1.1123123
     * station_content : hhhaklfjalkf
     * tel : 138123443
     * status : 1
     * files : [{"fileId":"c1a7fa97ac9f4b4b95f93e966c39b9ab"}]
     */

    private String station;
    private String name;
    private long time;
    private double lat;
    private double lon;
    private String station_content;
    private String tel;
    private String status;
    private List<FilesBean> files;

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
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

    public String getStation_content() {
        return station_content;
    }

    public void setStation_content(String station_content) {
        this.station_content = station_content;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<FilesBean> getFiles() {
        return files;
    }

    public void setFiles(List<FilesBean> files) {
        this.files = files;
    }

    public static class FilesBean implements Serializable{
        /**
         * fileId : c1a7fa97ac9f4b4b95f93e966c39b9ab
         */

        private String fileId;

        public String getFileId() {
            return fileId;
        }

        public void setFileId(String fileId) {
            this.fileId = fileId;
        }
    }
}
