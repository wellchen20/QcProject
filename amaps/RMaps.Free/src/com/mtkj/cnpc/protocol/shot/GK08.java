package com.mtkj.cnpc.protocol.shot;

import java.util.ArrayList;
import java.util.List;

import com.mtkj.cnpc.protocol.ReceiveBody;
import com.mtkj.cnpc.protocol.constants.ProtocolConstants;
import com.robert.maps.applib.utils.LogFileUtil;

/***
 * 配置信息（定向/广播）<br>
 * 
 * GK08;手持id;参数列表<br>
 * GK08;0;参数列表<br>
 * 例如: <br>
 * GK08;1;shotproMax:11;shotproOptimumMin:22....... 针对某一个手持<br>
 * GK08;0;shotproMax:11;shotproOptimumMin:22....... 针对所有手持--广播</br>
 * 桩号匹配最大距离,最佳距离（MAX）,最佳距离（MIN）,可选距离（MAX）,可选距离（MIN）,安全距离<br>
 * shotproMax, OptimumMax, OptimunMin, OptionalMax, OptionalMin, Distance<br>
 * 
 * @author TNT
 * 
 */
public class GK08 extends ReceiveBody {

	/***
	 * 配置信息
	 */
	public List<String> configResult = new ArrayList<String>();

	public GK08(String content) {
		super(ProtocolConstants.NAME_JINGPAO.GK08, content);
	}

	@Override
	public Object parseMsg(String msg) {
		if (msg != null && msg.length() > 0) {
			String[] datas = msg.split(ProtocolConstants.SPLITE_SYMBLE);
			if (datas != null && datas.length >= 2) {
				if (configResult == null) {
					configResult = new ArrayList<String>();
				}
				configResult.clear();
				for (int i = 2; i < datas.length; i++) {
					if (datas[i].trim().length() > 0) {
						configResult.add(datas[i]);
					}
				}
			} else {
				LogFileUtil.saveFileToSDCard(msg);
			}
		}
		return configResult;
	}

	/**
	 * @return the result
	 */
	public List<String> getResult() {
		return configResult;
	}
}
