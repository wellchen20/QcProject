package com.mtkj.cnpc.protocol.shot;

import com.mtkj.cnpc.protocol.ReceiveBody;
import com.mtkj.cnpc.protocol.constants.ProtocolConstants;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.robert.maps.applib.utils.LogFileUtil;

/**
 * 配对反馈<br>
 * GK02;手持Id;结果标志（1成功;0失败）;<br>
 * 例如:GK02;1;1;</br>
 * 
 * @author drh
 *
 */
public class GK02 extends ReceiveBody {

	public boolean isOk = false;
	public String errorInfo = "";
	public String error01 = "配对失败";

	public GK02(String content) {
		super(ProtocolConstants.NAME_JINGPAO.GK02, content);
	}

	@Override
	public Object parseMsg(String msg) {
		if (msg != null && msg.length() > 0) {
			String[] datas = msg.split(ProtocolConstants.SPLITE_SYMBLE);
			if (datas != null && datas.length >= 3) {
				if (String.valueOf(Integer.valueOf(SysConfig.SC_ID)).equals(datas[1])) {
					if (isSuccess(datas[2])) {
						isOk = true;
					} else {
						isOk = false;
					}
				}
			} else {
				LogFileUtil.saveFileToSDCard(msg);
			}
		}
		return isOk;
	}
}
