package com.mtkj.cnpc.protocol.bean;

import java.io.Serializable;

public class CarTrave implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2084217335452347853L;
	
	public String id = "";
	public String carnum = "";
	public String driver = "";
	public String task = "";
	public String destination = ""; 
	// 驻地出发
	public String start_place = "";
	public String start_time = "";
	public String start_peoplenum = "";
	public String start_lon = "";
	public String start_lat = "";
	public String start_remark = ""; 
	public String start_isUpload = "0";
	// 抵达工地
	public String arrived_place = ""; 
	public String arrived_time = "";
	public String arrived_peoplenum = "";
	public String arrived_lon = "";
	public String arrived_lat = "";
	public String arrived_remark = "";
	public String arrived_isUpload = "0";
	// 工地返回
	public String back_place = "";
	public String back_time = "";
	public String back_peoplenum = "";
	public String back_lon = "";
	public String back_lat = "";
	public String back_remark = "";
	public String back_isUpload = "0";
	// 抵达驻地
	public String end_place = "";
	public String end_time = "";
	public String end_peoplenum = "";
	public String end_lon = "";
	public String end_lat = "";
	public String end_remark = "";
	public String end_isUpload = "0";
	// 预计时间
	public String estimated_arrived_time = "";
	public String estimated_return_time = "";
}
