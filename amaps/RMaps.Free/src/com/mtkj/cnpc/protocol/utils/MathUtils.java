package com.mtkj.cnpc.protocol.utils;

import java.text.DecimalFormat;

/***
 * 
 * @author TNT
 * 
 */
public class MathUtils {

	/** WGS84保留小数点后几位 */
	public final static int WGS_FORMAT_NUM = 6;
	/** Map坐标保留小数点后几位 */
	public final static int MAP_FORMAT_NUM = 2;

	// public final static double VALUE_OF_MU= 666.666666667d;

	private final static String TAG = "MathUtils";

	/***
	 * 每亩多少平方米
	 */
	public final static double MeterPerMu = 666.666666667d;

	/***
	 * 平方米转亩
	 * 
	 * @param meterValue
	 * @return
	 */
	public static double Meter2Mu(double meterValue) {
		return meterValue / MeterPerMu;
	}

	/***
	 * 获得WGS84对应的文本，避免科学计数法
	 * 
	 * @param value
	 * @return
	 */
	public static String GetWGS84ValueText(double value) {
		// return new DecimalFormat("#,##0.000000")
		// .format(GetWGS84FormatValue(value));
		return GetAccurateNumberText(value, WGS_FORMAT_NUM);
	}

	/***
	 * 获得Map对应的文本，避免科学计数法
	 * 
	 * @param value
	 * @return
	 */
	public static String GetMapValueText(double value) {
		// return new
		// DecimalFormat("#,##0.00").format(GetMapFormatValue(value));
		return GetAccurateNumberText(value, MAP_FORMAT_NUM);
	}

	public static double GetWGS84FormatValue(double value) {
		return GetAccurateNumber(value, WGS_FORMAT_NUM);
	}

	public static double GetMapFormatValue(double value) {
		return GetAccurateNumber(value, MAP_FORMAT_NUM);
	}

	/**
	 * 获得精确的浮点数
	 * 
	 * @param value
	 *            之前的值
	 * @param decimalPointIndex
	 *            精确到几位
	 * @return
	 */
	public static double GetAccurateNumber(double value, int decimalPointIndex) {
		double dValue = value;

		// 生成格式串
		String formatZero = "0.";
		for (int i = 0; i < decimalPointIndex; i++) {
			formatZero += "0";
		}

		// 格式转换
		try {
			DecimalFormat format = new DecimalFormat(formatZero);
			String strNewValue = format.format(value);
			dValue = Double.parseDouble(strNewValue);
		} catch (Exception e) {
			// TODO: handle exception
			dValue = value;
		}

		return dValue;
	}

	/**
	 * 获得精确的浮点数对应文本
	 * 
	 * @param value
	 *            之前的值
	 * @param decimalPointIndex
	 *            精确到几位
	 * @return
	 */
	public static String GetAccurateNumberText(double value,
			int decimalPointIndex) {
		String result = "";

		// 生成格式串
		String formatZero = "#0";
		// String formatZero = "#,##0.";
		for (int i = 0; i < decimalPointIndex; i++) {
			if (i == 0) {
				formatZero += ".0";
			} else {
				formatZero += "0";
			}
		}

		// 格式转换
		try {
			DecimalFormat format = new DecimalFormat(formatZero);
			result = format.format(value);
		} catch (Exception e) {
		}

		return result;
	}
}
