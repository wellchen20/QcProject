package com.mtkj.cnpc.protocol.shot;

import com.mtkj.cnpc.protocol.ReceiveBody;
import com.mtkj.cnpc.protocol.constants.ProtocolConstants;
import com.mtkj.cnpc.protocol.constants.SysConfig;

/**
 * 发送充电提示信号 <br>
 * GK09;手持id;<br>
 * 例如:GK09;1;</br>
 * 
 * @author drh
 *
 */
public class GK11 extends ReceiveBody {

	public boolean isOk = false;
	
	public String result = "";

	public GK11(String content) {
		super(ProtocolConstants.NAME_JINGPAO.GK11, content);
	}

	@Override
	public Object parseMsg(String msg) {
		if (msg != null && msg.length() > 0) {
			String[] datas = msg.split(ProtocolConstants.SPLITE_SYMBLE);
			if (datas != null && datas.length >= 4) {
				if (String.valueOf(Integer.valueOf(SysConfig.SC_ID)).equals(datas[1])) {
					isOk = true;
					result = datas[3];
				} else  {
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
