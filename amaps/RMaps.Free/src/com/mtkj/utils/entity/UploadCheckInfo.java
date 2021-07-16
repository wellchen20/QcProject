package com.mtkj.utils.entity;

import java.io.Serializable;

public class UploadCheckInfo implements Serializable {
    public String pileNo;						//桩号
    public int processType;					//工序类型
    public int status;						//质检状态
    public String sinceTheCardName;			//		质检人
    public String sinceTheCardPhone;			//	质检人手机号
    public long sinceTheCardTime;			//		质检时间
    public String  pileNoContent;				//	桩号内容识别
    //------------------ 检波器埋置
    public int isLine;						//是否压耳线
    public int isGrass;						//是否除杂草
    public String img;
    //------------------ 钻井
    public String  drillDepth;					//钻井井深
    public String measureDepth;				//	量井井深
    public int  isWearClothes;				//	穿戴工服 (1:未穿 0：穿戴)
    public int isWearHat;					//佩戴安全帽(1:未穿 0：穿戴)
    public int isViolation;					//是否有违规行为(1:未穿 0：穿戴)
    //----------------- 下药
    public String  wellDepth;					//井深
    public String explosiveDepth;					//下药深度
    public String explosiveWeight;				//	药量
    public String  shotNumber;					//雷管量
    // isWearClothes;				//	穿戴工服 (1:未穿 0：穿戴)
    //isWearHat;					//佩戴安全帽 (1:未穿 0：穿戴)
    //  isViolation;					//是否有违规行为(1:未穿 0：穿戴)
    public int  isFill;					//	是否回填 (1：否 0：是 )
    public int  isRecover;				//	是否恢复测量标志 (1：否 0：是 )
    public int distance;//距离目标点距离

    public String getPileNo() {
        return pileNo;
    }

    public void setPileNo(String pileNo) {
        this.pileNo = pileNo;
    }

    public int getProcessType() {
        return processType;
    }

    public void setProcessType(int processType) {
        this.processType = processType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getSinceTheCardName() {
        return sinceTheCardName;
    }

    public void setSinceTheCardName(String sinceTheCardName) {
        this.sinceTheCardName = sinceTheCardName;
    }

    public String getSinceTheCardPhone() {
        return sinceTheCardPhone;
    }

    public void setSinceTheCardPhone(String sinceTheCardPhone) {
        this.sinceTheCardPhone = sinceTheCardPhone;
    }

    public long getSinceTheCardTime() {
        return sinceTheCardTime;
    }

    public void setSinceTheCardTime(long sinceTheCardTime) {
        this.sinceTheCardTime = sinceTheCardTime;
    }

    public String getPileNoContent() {
        return pileNoContent;
    }

    public void setPileNoContent(String pileNoContent) {
        this.pileNoContent = pileNoContent;
    }

    public int getIsLine() {
        return isLine;
    }

    public void setIsLine(int isLine) {
        this.isLine = isLine;
    }

    public int getIsGrass() {
        return isGrass;
    }

    public void setIsGrass(int isGrass) {
        this.isGrass = isGrass;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getDrillDepth() {
        return drillDepth;
    }

    public void setDrillDepth(String drillDepth) {
        this.drillDepth = drillDepth;
    }

    public String getMeasureDepth() {
        return measureDepth;
    }

    public void setMeasureDepth(String measureDepth) {
        this.measureDepth = measureDepth;
    }

    public int getIsWearClothes() {
        return isWearClothes;
    }

    public void setIsWearClothes(int isWearClothes) {
        this.isWearClothes = isWearClothes;
    }

    public int getIsWearHat() {
        return isWearHat;
    }

    public void setIsWearHat(int isWearHat) {
        this.isWearHat = isWearHat;
    }

    public int getIsViolation() {
        return isViolation;
    }

    public void setIsViolation(int isViolation) {
        this.isViolation = isViolation;
    }

    public String getWellDepth() {
        return wellDepth;
    }

    public void setWellDepth(String wellDepth) {
        this.wellDepth = wellDepth;
    }

    public String getExplosiveDepth() {
        return explosiveDepth;
    }

    public void setExplosiveDepth(String explosiveDepth) {
        this.explosiveDepth = explosiveDepth;
    }

    public String getExplosiveWeight() {
        return explosiveWeight;
    }

    public void setExplosiveWeight(String explosiveWeight) {
        this.explosiveWeight = explosiveWeight;
    }

    public String getShotNumber() {
        return shotNumber;
    }

    public void setShotNumber(String shotNumber) {
        this.shotNumber = shotNumber;
    }

    public int getIsFill() {
        return isFill;
    }

    public void setIsFill(int isFill) {
        this.isFill = isFill;
    }

    public int getIsRecover() {
        return isRecover;
    }

    public void setIsRecover(int isRecover) {
        this.isRecover = isRecover;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
}
