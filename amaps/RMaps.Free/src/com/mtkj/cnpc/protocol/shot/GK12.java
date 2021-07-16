package com.mtkj.cnpc.protocol.shot;

import com.mtkj.cnpc.protocol.ReceiveBody;
import com.mtkj.cnpc.protocol.constants.ProtocolConstants;

/**
 * 窥探发送充电超时信号<br>
 * GK10;手持id;<br>
 * 例如:GK01;1;</br>
 * 
 * @author drh
 *
 */
public class GK12 extends ReceiveBody {

	public boolean isOk = false;

	public String result = "";

	public GK12(String content) {
		super(ProtocolConstants.NAME_JINGPAO.GK10, content);
	}

	@Override
	public Object parseMsg(String msg) {
		if (msg != null && msg.length() > 0) {
			String[] datas = msg.split(ProtocolConstants.SPLITE_SYMBLE);
			if (datas != null && datas.length >= 3) {
				result = datas[2];
			}
		}
		return result;
	}
	
	public String getResult() {
		return result;
	}
}
