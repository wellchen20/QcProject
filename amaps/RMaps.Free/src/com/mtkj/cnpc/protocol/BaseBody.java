package com.mtkj.cnpc.protocol;

import java.io.Serializable;


/***
 * 协议基类
 * 
 * @author TNT
 * 
 */
public class BaseBody implements Serializable{
	public static String TAG = "protocol";
	
	/***消息体类型，默认无类型NULL**/
	public String type = null;
	/***消息体**/
	public String content = "";
	
	
	public BaseBody(){
		
	}
	
	public BaseBody(String type, String content) {
		super();
		this.type = type;
		this.content = content;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}
	
	
}
