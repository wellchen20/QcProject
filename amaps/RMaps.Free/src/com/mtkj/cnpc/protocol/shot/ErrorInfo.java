package com.mtkj.cnpc.protocol.shot;

/***
 * 井炮错误提示
 * 
 * @author TNT
 * 
 */
public interface ErrorInfo {

	/***
	 * 等待起爆超时
	 */
	public final static String ERROR_01 = "等待起爆超时";
	/***
	 * 等待PFS超时离开队列
	 */
	public final static String ERROR_02 = "等待PFS超时离开队列";
	/**
	 * 放炮完毕
	 */
	public final static String ERROR_03 = "放炮完毕";
	/**
	 * 等待READY充电超时
	 */
	public final static String ERROR_04 = "仪器未收到充电信号，请重新排队";

}
