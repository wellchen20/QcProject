package com.mtkj.cnpc.protocol.bean;

import java.io.Serializable;

public class CarKey implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8090172502564825547L;
	
	public String id = "";
	public String carnum = "";
	public String driver = "";
	public String start_time = "";
	public String start_isUpload = "0";
	public String back_time = "";
	public String back_isUpload = "0";
}
