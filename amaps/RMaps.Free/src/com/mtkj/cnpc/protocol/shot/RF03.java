package com.mtkj.cnpc.protocol.shot;

import com.mtkj.cnpc.protocol.SendBody;
import com.mtkj.cnpc.protocol.constants.ProtocolConstants;

/**
 * 取消配对 <br>
 * 编码规范:<br>
 * RF03;手持ID;爆炸机编号id;组织机构id;<br>
 * 例如:<br>
 * RF03;1;1;1<br>
 * 
 * @author drh
 *
 */
public class RF03 extends SendBody {

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
	public RF03(String SCNumber, String BZJNumber, String ZZJGNumber) {
		super(ProtocolConstants.NAME_JINGPAO.RF03,
				ProtocolConstants.NAME_JINGPAO.RF03
						+ ProtocolConstants.SPLITE_SYMBLE + SCNumber
						+ ProtocolConstants.SPLITE_SYMBLE + BZJNumber
						+ ProtocolConstants.SPLITE_SYMBLE + ZZJGNumber
						+ ProtocolConstants.SPLITE_SYMBLE);

	}

}
