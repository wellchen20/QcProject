package com.mtkj.utils.entity;

import java.io.Serializable;
import java.util.List;

public class AllPointsTask implements Serializable {

    /**
     * msg : success
     * code : 0
     * data : [{"theDot":"1742","lineNo":"5214","createTime":1600931949000,
     * "dimensionality":"30.60133252","explosiveWeight":"6","designDepth":"12",
     * "pileNo":"777","processType":"16","wellNum":"1","shotNumber":"1",
     * "taskId":"CS20200924151908","longitude":"103.6302098"}]
     */

    private String msg;
    private int code;
    private List<DataBean> data;

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

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * theDot : 1742
         * lineNo : 5214
         * createTime : 1600931949000
         * dimensionality : 30.60133252
         * explosiveWeight : 6
         * designDepth : 12
         * pileNo : 777
         * processType : 16
         * wellNum : 1
         * shotNumber : 1
         * taskId : CS20200924151908
         * longitude : 103.6302098
         */

        private String theDot;
        private String lineNo;
        private long createTime;
        private String dimensionality;
        private String explosiveWeight;
        private String designDepth;
        private String pileNo;
        private String processType;
        private String wellNum;
        private String shotNumber;
        private String taskId;
        private String longitude;

        public String getTheDot() {
            return theDot;
        }

        public void setTheDot(String theDot) {
            this.theDot = theDot;
        }

        public String getLineNo() {
            return lineNo;
        }

        public void setLineNo(String lineNo) {
            this.lineNo = lineNo;
        }

        public long getCreateTime() {
            return createTime;
        }

        public void setCreateTime(long createTime) {
            this.createTime = createTime;
        }

        public String getDimensionality() {
            return dimensionality;
        }

        public void setDimensionality(String dimensionality) {
            this.dimensionality = dimensionality;
        }

        public String getExplosiveWeight() {
            return explosiveWeight;
        }

        public void setExplosiveWeight(String explosiveWeight) {
            this.explosiveWeight = explosiveWeight;
        }

        public String getDesignDepth() {
            return designDepth;
        }

        public void setDesignDepth(String designDepth) {
            this.designDepth = designDepth;
        }

        public String getPileNo() {
            return pileNo;
        }

        public void setPileNo(String pileNo) {
            this.pileNo = pileNo;
        }

        public String getProcessType() {
            return processType;
        }

        public void setProcessType(String processType) {
            this.processType = processType;
        }

        public String getWellNum() {
            return wellNum;
        }

        public void setWellNum(String wellNum) {
            this.wellNum = wellNum;
        }

        public String getShotNumber() {
            return shotNumber;
        }

        public void setShotNumber(String shotNumber) {
            this.shotNumber = shotNumber;
        }

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }
    }
}
