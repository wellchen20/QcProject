package com.mtkj.cnpc.protocol.utils;

public class SocketUtils {

	public static int versionLength = 24;
	
	/**
	 * 发送数据
	 * 
	 * @param request
	 *            请求协议码
	 * @param bytes
	 *            要发送的数据
	 * @throws Exception 
	 * */
	public static byte[] writeBytes(byte[] headbytes, byte[] bodybytes) throws Exception {
		byte[] send;
		if (bodybytes != null) {
			// 发送byte数组
			send = new byte[versionLength + headbytes.length + bodybytes.length];
			
			// version部分
			char headmark0 = byteAsciiToChar(30);
			char headmark1 = byteAsciiToChar(30);
			byte[] chars = new byte[14];
			int headlength = headbytes.length;
	        byte[] headlengths = new byte[4];
	        headlengths = intToBytes(headlength);
	        
			int bodylength = bodybytes.length;
			byte[] bodylengths = new byte[4];
			bodylengths = intToBytes(bodylength);
			
			send[0] = (byte) headmark0;
			send[1] = (byte) headmark1;
			for (int i = 0; i < chars.length; i++) {
				send[2 + i] = chars[i];
			}
			for (int i = 0; i < headlengths.length; i++) {
				send[16 + i] = headlengths[i];
			}
			for (int i = 0; i < bodylengths.length; i++) {
				send[20 + i] = bodylengths[i];
			}
			for (int i = 0; i < headbytes.length; i++) {
				send[versionLength + i] = headbytes[i];
			}
			for (int i = 0; i < bodybytes.length; i++) {
				send[versionLength + headbytes.length + i] = bodybytes[i];
			}
		} else {
			// 发送byte数组
			send = new byte[versionLength + headbytes.length];
						
			// version部分
			char headmark0 = byteAsciiToChar(30);
			char headmark1 = byteAsciiToChar(30);
			byte[] chars = new byte[14];
			int headlength = headbytes.length;
			byte[] headlengths = new byte[4];
			headlengths = intToBytes(headlength);
				        
			int bodylength = 0;
			byte[] bodylengths = new byte[4];
			bodylengths = intToBytes(bodylength);
			
			send[0] = (byte) headmark0;
			send[1] = (byte) headmark1;
			for (int i = 0; i < chars.length; i++) {
				send[2 + i] = chars[i];
			}
			for (int i = 0; i < headlengths.length; i++) {
				send[16 + i] = headlengths[i];
			}
			for (int i = 0; i < bodylengths.length; i++) {
				send[20 + i] = bodylengths[i];
			}
			for (int i = 0; i < headbytes.length; i++) {
				send[versionLength + i] = headbytes[i];
			}
		}
        
		
		return send;
	}
	
	/**
	 * to WSC 命令体结构化
	 * 
	 * @param bodybytes
	 * @return
	 */
	public static byte[] writeBodyBytes(byte[] bodybytes) {
		byte[] dataNew = new byte[bodybytes.length + 3];
		for (int i = 0; i < bodybytes.length; i++) {
			dataNew[i] = bodybytes[i];
		}
		//增加结尾信息
		dataNew[dataNew.length - 3] = (byte) bodybytes.length;
		dataNew[dataNew.length - 2] = 0x0D;
		dataNew[dataNew.length - 1] = 0x0A;
		return dataNew;
	}
	
	/**  
	    * 将int数值转换为占四个字节的byte数组，本方法适用于(低位在前，高位在后)的顺序。 和bytesToInt（）配套使用 
	    * @param value  
	    *            要转换的int值 
	    * @return byte数组 
	    */    
	public static byte[] intToBytes( int value )   
	{   
	    byte[] src = new byte[4];  
	    src[3] =  (byte) ((value>>24) & 0xFF);  
	    src[2] =  (byte) ((value>>16) & 0xFF);  
	    src[1] =  (byte) ((value>>8) & 0xFF);    
	    src[0] =  (byte) (value & 0xFF);                  
	    return src;   
	}
	
	 /**  
	    * 将int数值转换为占四个字节的byte数组，本方法适用于(高位在前，低位在后)的顺序。  和bytesToInt2（）配套使用 
	    */    
	public static byte[] intToBytes2(int value)   
	{   
	    byte[] src = new byte[4];  
	    src[0] = (byte) ((value>>24) & 0xFF);  
	    src[1] = (byte) ((value>>16)& 0xFF);  
	    src[2] = (byte) ((value>>8)&0xFF);    
	    src[3] = (byte) (value & 0xFF);       
	    return src;  
	}
	
	/**  
	    * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和和intToBytes（）配套使用 
	    *   
	    * @param src  
	    *            byte数组  
	    * @param offset  
	    *            从数组的第offset位开始  
	    * @return int数值  
	    */    
	public static int bytesToInt(byte[] src, int offset) {  
	    int value;    
	    value = (int) ((src[offset] & 0xFF)   
	            | ((src[offset+1] & 0xFF)<<8)   
	            | ((src[offset+2] & 0xFF)<<16)   
	            | ((src[offset+3] & 0xFF)<<24));  
	    return value;  
	}  
	  
	 /**  
	    * byte数组中取int数值，本方法适用于(低位在后，高位在前)的顺序。和intToBytes2（）配套使用 
	    */  
	public static int bytesToInt2(byte[] src, int offset) {  
	    int value;    
	    value = (int) ( ((src[offset] & 0xFF)<<24)  
	            |((src[offset+1] & 0xFF)<<16)  
	            |((src[offset+2] & 0xFF)<<8)  
	            |(src[offset+3] & 0xFF));  
	    return value;  
	}
	
	public static String StringToInt(String string) {
		String str = "0";
		if (string != null && !"".equals(string)) {
			str = string.substring(0, string.indexOf("."));
			int index = Integer.parseInt(str);
			if (index > 9) {
				index = 9;
			}
			str = "" + index;
		}
		if (str == null || "".equals(str)) {
			str = "0";
		}
		return str;
	}
	
	/**
	 * ascii转换为char 直接int强制转换为char 
	 * 
	 * @param ascii
	 * @return
	 */
	public static char byteAsciiToChar(int ascii){
		char c = (char) ascii;
		return c;
	}
	
}
