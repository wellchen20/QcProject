package com.mtkj.cnpc.protocol.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DrillRecord implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5782265984941808933L;
	public String stationNo = "";
	public String lineNo = "";
	public String spointNo = "";
	public String receivetime = "";
	public String welllithology = "";
	public String wellnum = "";
	public String lon = "";
	public String lat = "";
	public String drilldepth = "";
	public String bombdepth = "";
	public String bombWeight = "";
	public String detonator = "";
	public List<String> bombid = new ArrayList<String>();
	public List<String> detonatorid = new ArrayList<String>();
	public String remark = "";
	public String image1 = "";
	public String image2 = "";
	public String image3 = "";
	public String drilltime = "";
	public String isupload  = "0";
	public String name = "";
	public String tel = "";
	public int status = 0;
	public String video = "";
	
	public String getBombid() {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < bombid.size(); i++) {
			if (i == bombid.size() - 1) {
				buffer.append(bombid.get(i));
			} else {
				buffer.append(bombid.get(i)).append(",");
			}
		}
		return buffer.toString();
	}
	
	public String getdetonatorid() {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < detonatorid.size(); i++) {
			if (i == detonatorid.size() - 1) {
				buffer.append(detonatorid.get(i));
			} else {
				buffer.append(detonatorid.get(i)).append(",");
			}
		}
		return buffer.toString();
	}
	
}
