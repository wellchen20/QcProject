package com.mtkj.cnpc.protocol.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2019/4/2.
 */

public class ArrangeRecord implements Serializable {
    public String stationNo;
    public String lineNo;
    public String spointNo;
    public String time;
    public double lon;
    public double lat;
    public String description;
    public String arrived_time;
    public String remark;
    public String image1;
    public String image2;
    public String image3;
    public int status = 0;
    public String isupload ;

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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getArrived_time() {
        return arrived_time;
    }

    public void setArrived_time(String arrived_time) {
        this.arrived_time = arrived_time;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getImage1() {
        return image1;
    }

    public void setImage1(String image1) {
        this.image1 = image1;
    }

    public String getImage2() {
        return image2;
    }

    public void setImage2(String image2) {
        this.image2 = image2;
    }

    public String getImage3() {
        return image3;
    }

    public void setImage3(String image3) {
        this.image3 = image3;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getIsupload() {
        return isupload;
    }

    public void setIsupload(String isupload) {
        this.isupload = isupload;
    }

    @Override
    public String toString() {
        return "ArrangeRecord{" +
                "stationNo='" + stationNo + '\'' +
                ", lineNo='" + lineNo + '\'' +
                ", spointNo='" + spointNo + '\'' +
                ", time='" + time + '\'' +
                ", lon='" + lon + '\'' +
                ", lat='" + lat + '\'' +
                ", description='" + description + '\'' +
                ", arrived_time='" + arrived_time + '\'' +
                ", remark='" + remark + '\'' +
                ", image1='" + image1 + '\'' +
                ", image2='" + image2 + '\'' +
                ", image3='" + image3 + '\'' +
                ", status=" + status +
                ", isupload='" + isupload + '\'' +
                '}';
    }
}
