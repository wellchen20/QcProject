package com.mtkj.cnpc.protocol.shot;

import com.mtkj.cnpc.protocol.ReceiveBody;
import com.mtkj.cnpc.protocol.constants.ProtocolConstants;
import com.mtkj.cnpc.protocol.constants.SysConfig;

/**
 * 窥探发送充电超时信号<br>
 * GK10;手持id;<br>
 * 例如:GK01;1;</br>
 * 
 * @author drh
 *
 */
public class GK10 extends ReceiveBody {

	public boolean isOk = false;

	public String result = "";
	
	public GK10(String content) {
		super(ProtocolConstants.NAME_JINGPAO.GK10, content);
	}

	@Override
	public Object parseMsg(String msg) {
		if (msg != null && msg.length() > 0) {
			String[] datas = msg.split(ProtocolConstants.SPLITE_SYMBLE);
			if (datas != null && datas.length >= 3) {
				if (String.valueOf(Integer.valueOf(SysConfig.SC_ID)).equals(datas[1])) {
					isOk = true;
					result = datas[2];
				} else {
					isOk = false;
					result = "-1";
				}
			}
		}
		return result;
	}
	
	public String getResult() {
		return result;
	}
}
