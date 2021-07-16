package com.mtkj.cnpc.protocol.shot;

import com.mtkj.cnpc.protocol.SendBody;
import com.mtkj.cnpc.protocol.constants.ProtocolConstants;

/**
 * 窥探<br>
 * 编码规范:<br>
 * RF01;手持Id;<br>
 * 例如 RF01;1;<br>
 * 手持机每次发送数据前先窥探<br>
 * 
 * @author drh
 *
 */
public class RF01 extends SendBody {

	/***
	 * 窥探信息
	 * 
	 * @param SCNumber
	 *            :手持ID
	 * 
	 */
	public RF01(String SCNumber) {
		super(ProtocolConstants.NAME_JINGPAO.RF01,
				ProtocolConstants.NAME_JINGPAO.RF01
						+ ProtocolConstants.SPLITE_SYMBLE + SCNumber
						+ ProtocolConstants.SPLITE_SYMBLE);

	}

}
