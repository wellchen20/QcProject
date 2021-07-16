package com.mtkj.utils.entity;

import java.io.Serializable;

/**
 * Created by Administrator on 2019/3/10.
 */

public class TalkEntity implements Serializable{

    /**
     * time :
     * content :
     * type_who : 0
     * type_talk : 0
     */
    private int id;
    private String time;
    private String content;
    private String device;
    private String name;
    private int type_who;
    private int type_talk;
    private int msgtype;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType_who() {
        return type_who;
    }

    public void setType_who(int type_who) {
        this.type_who = type_who;
    }

    public int getType_talk() {
        return type_talk;
    }

    public void setType_talk(int type_talk) {
        this.type_talk = type_talk;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public int getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(int msgtype) {
        this.msgtype = msgtype;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
