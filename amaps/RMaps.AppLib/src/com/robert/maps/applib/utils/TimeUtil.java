package com.robert.maps.applib.utils;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.robert.maps.applib.R;

import android.annotation.SuppressLint;
import android.content.Context;

/**
 * 
 * @author drh
 * 
 */
@SuppressLint("SimpleDateFormat")
public class TimeUtil implements Serializable {

	private static final long serialVersionUID = -1844761923580220290L;

	public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public static final SimpleDateFormat DATE_FORMAT_DATE = new SimpleDateFormat(
			"yyyy-MM-dd");

	/**
	 * 
	 * long time to string
	 * 
	 * @param timeInMillis
	 * @param dateFormat
	 * @return
	 */

	public static String getTime(long timeInMillis, SimpleDateFormat dateFormat) {
		return dateFormat.format(new Date(timeInMillis));
	}

	/**
	 * 
	 * String time to date
	 * 
	 * @param timeInMillis
	 * @param dateFormat
	 * @return
	 */

	public static Date getTime(String timeInMillis, SimpleDateFormat dateFormat) {
		Date date = null;
		try {
			date = dateFormat.parse(timeInMillis);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	/**
	 * 
	 * long time to string, format is {@link #DEFAULT_DATE_FORMAT}
	 * 
	 * @param timeInMillis
	 * @return
	 */

	public static String getTime(long timeInMillis) {
		return getTime(timeInMillis, DEFAULT_DATE_FORMAT);
	}

	/**
	 * 
	 * String time to date, format is {@link #DEFAULT_DATE_FORMAT}
	 * 
	 * @param timeInMillis
	 * @return
	 */

	public static Date getTime(String timeInMillis) {
		return getTime(timeInMillis, DEFAULT_DATE_FORMAT);
	}

	/**
	 * 
	 * get current time in milliseconds
	 * 
	 * @return
	 */

	public static long getCurrentTimeInLong() {
		return System.currentTimeMillis();
	}

	/**
	 * 
	 * get current time in milliseconds, format is {@link #DEFAULT_DATE_FORMAT}
	 * 
	 * @return
	 */

	public static String getCurrentTimeInString() {
		return getTime(getCurrentTimeInLong());
	}

	/**
	 * 
	 * get current time in milliseconds
	 * 
	 * @return
	 */

	public static String getCurrentTimeInString(SimpleDateFormat dateFormat) {
		return getTime(getCurrentTimeInLong(), dateFormat);
	}
	
	/***
	 * 获得当前时间
	 */
	public static String getCurrentUtcTime() {
		Date l_datetime = new Date();
		DateFormat formatter = new SimpleDateFormat("HHmmss");
		TimeZone l_timezone = TimeZone.getTimeZone("GMT-0");
		formatter.setTimeZone(l_timezone);
		return formatter.format(l_datetime);
	}
	
	public static String getTimeLong(Context context, double time){
		String timeLong = "";
		if (time < 60) {
			timeLong =  time + context.getResources().getString(R.string.s);
		} else if (time >= 60 && time < 3600) {
			timeLong =  ((int)(time / 60)) + context.getResources().getString(R.string.min) 
					+ " " +
					((int)(time % 60)) + context.getResources().getString(R.string.s);
		} else if (time >= 3600) {
			timeLong = ((int)(time / 3600)) + context.getResources().getString(R.string.h) 
					+ " " +
					((int)(time % 3600 / 60)) + context.getResources().getString(R.string.min)
					+ " " +
					((int)(time % 3600 % 60)) + context.getResources().getString(R.string.s);
		}
		return timeLong;
	}
}
