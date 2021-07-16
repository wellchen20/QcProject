package com.mtkj.utils.entity;

import java.io.Serializable;

/**
 * Created by Administrator on 2019/4/2.
 */

public class ArrangeEntity implements Serializable{

    /**
     * stationno :
     * user :
     * time :
     * description :
     * remark :
     * msgtype :
     */

    private String stationno;
    private String user;
    private String time;
    private String description;
    private String remark;
    private int msgtype;

    public String getStationno() {
        return stationno;
    }

    public void setStationno(String stationno) {
        this.stationno = stationno;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(int msgtype) {
        this.msgtype = msgtype;
    }
}
