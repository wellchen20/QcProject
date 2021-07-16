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
public class RF13 extends SendBody {

	/***
	 * 中继模式发送ready信号给wsc
	 *
	 * @param SCNumber
	 *  手持ID
	 *
	 */
	public RF13(String SCNumber) {
		super(ProtocolConstants.NAME_JINGPAO.RF13,
				ProtocolConstants.NAME_JINGPAO.RF13
						+ ProtocolConstants.SPLITE_SYMBLE + SCNumber
						+ ProtocolConstants.SPLITE_SYMBLE );

	}

}
