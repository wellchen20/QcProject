package com.mtkj.utils.entity;

import java.io.Serializable;

public class PictureEntity implements Serializable {

    /**
     * msg : group1/M00/00/04/eFyRpV9wR5eADtRcAAIQp1-JPtc566.jpg@group1/M00/00/04/eFyRpV9wR5iAJCiWAAH_Pg3xrwM637.jpg@group1/M00/00/04/eFyRpV9wR5mAa1VAAAJzCnTF73M601.jpg@
     * code : 0
     */

    private String msg;
    private int code;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
