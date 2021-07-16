package com.mtkj.cnpc.protocol.shot;

import com.mtkj.cnpc.protocol.SendBody;
import com.mtkj.cnpc.protocol.constants.ProtocolConstants;

/**
 * 取消排队 <br>
 * 编码规范:<br>
 * RF05;手持ID;<br>
 * 例如:<br>
 * RF05;1;<br>
 * 
 * @author drh
 *
 */
public class RF05 extends SendBody {

	public RF05(String SCNumber) {
		super(ProtocolConstants.NAME_JINGPAO.RF05,
				ProtocolConstants.NAME_JINGPAO.RF05
						+ ProtocolConstants.SPLITE_SYMBLE + SCNumber
						+ ProtocolConstants.SPLITE_SYMBLE);

	}

}
