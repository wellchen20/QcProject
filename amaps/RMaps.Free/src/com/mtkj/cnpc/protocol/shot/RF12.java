package com.mtkj.cnpc.protocol.shot;

import com.mtkj.cnpc.protocol.SendBody;
import com.mtkj.cnpc.protocol.constants.ProtocolConstants;

/**
 * 获取配置参数 <br>
 * 编码规范:<br>
 * RF12;手持Id;<br>
 * 例如 RF12;1;<br>
 * 手持机每次发送数据前先窥探<br>
 * 
 * @author cw
 *
 */
public class RF12 extends SendBody {

	/***
	 * 引爆超时给wsc发送起爆状态
	 *
	 * @param SCNumber
	 *            :手持ID
	 *
	 */
	public RF12(String SCNumber,String stationNo,String status) {
		super(ProtocolConstants.NAME_JINGPAO.RF12,
				ProtocolConstants.NAME_JINGPAO.RF12
						+ ProtocolConstants.SPLITE_SYMBLE + SCNumber
						+ ProtocolConstants.SPLITE_SYMBLE + stationNo
						+ ProtocolConstants.SPLITE_SYMBLE + status
						+ ProtocolConstants.SPLITE_SYMBLE);

	}

}
