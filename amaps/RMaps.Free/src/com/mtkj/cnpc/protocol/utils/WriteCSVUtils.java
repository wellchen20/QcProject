package com.mtkj.cnpc.protocol.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import com.mtkj.cnpc.protocol.bean.DrillRecord;
import com.mtkj.cnpc.protocol.constants.SysConfig;

public class WriteCSVUtils {
	
	public static boolean WriteCsv(List<DrillRecord> drillRecords, String fileName) {
		boolean isSuccess = false;
		try {
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(fileName)));
			CsvListWriter listWriter = new CsvListWriter(writer, CsvPreference.EXCEL_PREFERENCE);
			if (drillRecords != null && drillRecords.size() > 0) {
				List<String> nameStrings = new ArrayList<String>();
				nameStrings.add("桩号");
				nameStrings.add("线号");
				nameStrings.add("点号");
				nameStrings.add("设备ID");
				nameStrings.add("设备名称");
				nameStrings.add("到达时间");
				nameStrings.add("完成时间");
				nameStrings.add("激发岩性");
				nameStrings.add("井口数");
				nameStrings.add("经度");
				nameStrings.add("维度");
				nameStrings.add("钻井深度");
				nameStrings.add("下药深度");
				nameStrings.add("药量");
				nameStrings.add("雷管");
				nameStrings.add("炸药编号");
				nameStrings.add("雷管编号");
				listWriter.write(nameStrings);
				for (DrillRecord drillRecord : drillRecords) {
					List<String> strings = new ArrayList<String>();
					strings.add(drillRecord.stationNo);
					strings.add(drillRecord.lineNo);
					strings.add(drillRecord.spointNo);
					strings.add(SysConfig.SC_ID);
					strings.add(SysConfig.SC);
					strings.add(drillRecord.receivetime);
					strings.add(drillRecord.drilltime);
					strings.add(drillRecord.welllithology);
					strings.add(drillRecord.wellnum);
					strings.add(drillRecord.lon);
					strings.add(drillRecord.lat);
					strings.add(drillRecord.drilldepth);
					strings.add(drillRecord.bombdepth);
					strings.add(drillRecord.bombWeight);
					strings.add(drillRecord.detonator);
					strings.add(drillRecord.getBombid());
					strings.add(drillRecord.getdetonatorid());
					listWriter.write(strings);
				}
			}
			listWriter.close();
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
			isSuccess = false;
		}
		return isSuccess;
	}

}
