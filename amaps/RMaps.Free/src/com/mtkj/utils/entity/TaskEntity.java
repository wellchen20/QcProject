package com.mtkj.utils.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2019/3/7.
 */

public class TaskEntity implements Serializable{

    /**
     * start_time : 2019/3/7 16:16:19
     * finish_time : 2019/3/7 16:16:19
     * task_type : 1
     * task : [{"line_no":"100","s_point_no":"1","e_point_no":"10"},{"line_no":"200","s_point_no":"1","e_point_no":"10"}]
     */
    private int id;
    private String msgtype;
    private String start_time;
    private String finish_time;
    private int task_type;
    private List<TaskBean> task = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getFinish_time() {
        return finish_time;
    }

    public void setFinish_time(String finish_time) {
        this.finish_time = finish_time;
    }

    public int getTask_type() {
        return task_type;
    }

    public void setTask_type(int task_type) {
        this.task_type = task_type;
    }

    public List<TaskBean> getTask() {
        return task;
    }

    public void setTask(List<TaskBean> task) {
        this.task = task;
    }

    public static class TaskBean implements Serializable{
        /**
         * line_no : 100
         * s_point_no : 1
         * e_point_no : 10
         */

        private String line_no;
        private String s_point_no;
        private String e_point_no;

        public String getLine_no() {
            return line_no;
        }

        public void setLine_no(String line_no) {
            this.line_no = line_no;
        }

        public String getS_point_no() {
            return s_point_no;
        }

        public void setS_point_no(String s_point_no) {
            this.s_point_no = s_point_no;
        }

        public String getE_point_no() {
            return e_point_no;
        }

        public void setE_point_no(String e_point_no) {
            this.e_point_no = e_point_no;
        }
    }
}
