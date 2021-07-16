package com.robert.maps.applib.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.robert.maps.applib.utils.Ut;

public class LogFileUtil {

	private static String fileName = Ut.getExternalStorageDirectory() + File.separator + "rmaps" + File.separator + "log.txt";

	/**
	 * 获得文件大小
	 * 
	 * @param path
	 *            文件和文件夹均可
	 * @return 文件大小
	 */
	public static final void getSize() {
		long bit = 0;
		File file = new File(fileName);
		if (file.exists()) {
			if (file.isFile()) {
				bit += file.length();
			}
		} else {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (bit > 10 * 1024 * 1024) {
			deleteFile();
		}
	}

	/**
	 * 删除文件包括文件夹
	 * @param path
	 */
	private static final void  deleteFile(){
		File file = new File(fileName);
		if(file.exists()){
			if(file.isFile()){
				file.delete();
			}
		}
	}
	
	public static void saveFileToSDCard(String data) {
		String String = new StringBuffer().append(getCurrentTimeInString())
				.append("\r\n").append(data).toString();
		File toFile = new File(fileName);
		try {
			saveFile(String, toFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static FileWriter writer;
	private static void saveFile(String data, File toFile) throws IOException {
		if (writer != null) {// 关闭
			writer.flush();
			writer.close();
			writer = null;
		}
		writer = new FileWriter(toFile, true);
		writer.write(data + "\r\n");
		writer.flush();
	}

	private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	/**
	 * 
	 * get current time in milliseconds
	 * 
	 * @return
	 */

	private static long getCurrentTimeInLong() {
		return System.currentTimeMillis();
	}

	/**
	 * 
	 * get current time in milliseconds, format is {@link #DEFAULT_DATE_FORMAT}
	 * 
	 * @return
	 */

	private static String getCurrentTimeInString() {
		return getTime(getCurrentTimeInLong());
	}

	/**
	 * 
	 * long time to string, format is {@link #DEFAULT_DATE_FORMAT}
	 * 
	 * @param timeInMillis
	 * @return
	 */

	private static String getTime(long timeInMillis) {
		return getTime(timeInMillis, DEFAULT_DATE_FORMAT);
	}

	/**
	 * 
	 * long time to string
	 * 
	 * @param timeInMillis
	 * @param dateFormat
	 * @return
	 */

	private static String getTime(long timeInMillis, SimpleDateFormat dateFormat) {
		return dateFormat.format(new Date(timeInMillis));
	}

}
