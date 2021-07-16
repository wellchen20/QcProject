package com.mtkj.cnpc.protocol.shot;

import com.mtkj.cnpc.protocol.ReceiveBody;
import com.mtkj.cnpc.protocol.constants.ProtocolConstants;

/**
 * 发送充电提示信号<br>
 * GK09;手持id;<br>
 * 例如:GK09;1;</br>
 * 
 * @author drh
 *
 */
public class GK09 extends ReceiveBody {

	public boolean isOk = false;
	
	public String result_id = "";

	public GK09(String content) {
		super(ProtocolConstants.NAME_JINGPAO.GK09, content);
	}

	@Override
	public Object parseMsg(String msg) {
		if (msg != null && msg.length() > 0) {
			String[] datas = msg.split(ProtocolConstants.SPLITE_SYMBLE);
			if (datas != null && datas.length >= 2) {
				result_id = datas[1];
			}
		}
		return result_id;
	}
}
