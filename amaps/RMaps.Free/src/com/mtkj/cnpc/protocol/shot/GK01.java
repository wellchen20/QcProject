package com.mtkj.cnpc.protocol.shot;

import com.mtkj.cnpc.protocol.ReceiveBody;
import com.mtkj.cnpc.protocol.constants.ProtocolConstants;
import com.mtkj.cnpc.protocol.constants.SysConfig;

/**
 * 窥探反馈<br>
 * GK01;手持id;<br>
 * 例如:GK01;1;</br>
 * 
 * @author drh
 *
 */
public class GK01 extends ReceiveBody {

	public boolean isOk = false;

	public GK01(String content) {
		super(ProtocolConstants.NAME_JINGPAO.GK01, content);
	}

	@Override
	public Object parseMsg(String msg) {
		if (msg != null && msg.length() > 0) {
			String[] datas = msg.split(ProtocolConstants.SPLITE_SYMBLE);
			if (datas != null && datas.length >= 2) {
				if (String.valueOf(Integer.valueOf(SysConfig.SC_ID)).equals(datas[1])) {
					isOk = true;
				} else {
					isOk = false;
				}
			}
		}
		return isOk;
	}
}
