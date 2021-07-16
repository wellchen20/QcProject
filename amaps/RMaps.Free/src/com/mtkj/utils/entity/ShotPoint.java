package com.mtkj.utils.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2019/3/9.
 */

public class ShotPoint implements Serializable{

    /**
     * msgtype : 2
     * count : 18251
     * begin : 5000
     * end : 6000
     * type : 2
     * op : 1
     * points : [["59541596","5954","1596","30.4632","103.747","0"]]
     */
    public static final int EMPTY_ID = -777;
    private int msgtype;
    private int count;
    private int begin;
    private int end;
    private int type;
    private int op;
    private List<List<String>> points;

    public int getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(int msgtype) {
        this.msgtype = msgtype;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getOp() {
        return op;
    }

    public void setOp(int op) {
        this.op = op;
    }

    public List<List<String>> getPoints() {
        return points;
    }

    public void setPoints(List<List<String>> points) {
        this.points = points;
    }
}
