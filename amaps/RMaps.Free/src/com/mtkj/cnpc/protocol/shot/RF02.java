package com.mtkj.cnpc.protocol.shot;

import com.mtkj.cnpc.protocol.SendBody;
import com.mtkj.cnpc.protocol.constants.ProtocolConstants;

/**
 * 请求配对 <br>
 * 编码规范:<br>
 * RF02;手持ID;爆炸机编号id;组织机构id;<br>
 * 例如:<br>
 * RF02;1;1;1<br>
 * 
 * @author drh
 *
 */
public class RF02 extends SendBody {

	/**
	 * 请求配对
	 * 
	 * @param SCNumber
	 * 				:手持id
	 * @param BZJNumber
	 * 				:爆炸机id
	 * @param ZZJGNumber
	 * 				:组织机构id
	 */
	public RF02(String SCNumber, String BZJNumber, String ZZJGNumber) {
		super(ProtocolConstants.NAME_JINGPAO.RF02,
				ProtocolConstants.NAME_JINGPAO.RF02
						+ ProtocolConstants.SPLITE_SYMBLE + SCNumber
						+ ProtocolConstants.SPLITE_SYMBLE + BZJNumber
						+ ProtocolConstants.SPLITE_SYMBLE + 1
						+ ProtocolConstants.SPLITE_SYMBLE);

	}

}
