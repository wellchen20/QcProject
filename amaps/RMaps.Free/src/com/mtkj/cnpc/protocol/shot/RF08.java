package com.mtkj.cnpc.protocol.shot;

import com.mtkj.cnpc.protocol.SendBody;
import com.mtkj.cnpc.protocol.constants.ProtocolConstants;

/**
 * 获取配置参数 <br>
 * 编码规范:<br>
 * RF08;手持Id;<br>
 * 例如 RF08;1;<br>
 * 手持机每次发送数据前先窥探<br>
 * 
 * @author drh
 *
 */
public class RF08 extends SendBody {

	/***
	 * 窥探信息
	 * 
	 * @param SCNumber
	 *            :手持ID
	 * 
	 */
	public RF08(String SCNumber) {
		super(ProtocolConstants.NAME_JINGPAO.RF08,
				ProtocolConstants.NAME_JINGPAO.RF08
						+ ProtocolConstants.SPLITE_SYMBLE + SCNumber
						+ ProtocolConstants.SPLITE_SYMBLE);

	}

}
