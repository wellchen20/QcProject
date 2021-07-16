package com.mtkj.utils.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2019/3/10.
 */

public class ContactPersons implements Serializable{


    private List<UserlistBean> userlist = new ArrayList<>();

    public List<UserlistBean> getUserlist() {
        return userlist;
    }

    public void setUserlist(List<UserlistBean> userlist) {
        this.userlist = userlist;
    }

    public static class UserlistBean implements Serializable{
        /**
         * name : 13012345678
         * device : handset003
         * status : 102
         * oid : 9
         */
        private String phone;
        private String name;
        private String device;
        private int status;
        private int oid;
        private  int isNews = 0;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDevice() {
            return device;
        }

        public void setDevice(String device) {
            this.device = device;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getOid() {
            return oid;
        }

        public void setOid(int oid) {
            this.oid = oid;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public int getIsNews() {
            return isNews;
        }

        public void setIsNews(int isNews) {
            this.isNews = isNews;
        }
    }
}
