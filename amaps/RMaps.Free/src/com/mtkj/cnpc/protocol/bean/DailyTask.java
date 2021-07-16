package com.mtkj.cnpc.protocol.bean;

import java.io.Serializable;

/**
 * 兴趣点
 * 
 * @author DRH
 *
 */
public class DailyTask implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6794537055622394866L;
	public String time;
	public String pname;
	public int status;
	public int total;
	public int done;
}
