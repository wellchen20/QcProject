package com.mtkj.cnpc.protocol;

import com.mtkj.cnpc.protocol.constants.ProtocolConstants;

public abstract class ReceiveBody extends BaseBody {

	public ReceiveBody() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ReceiveBody(String type, String content) {
		super(type, content);
		// TODO Auto-generated constructor stub
		parseMsg(content);
	}

	public abstract Object parseMsg(String msg);

	/***
	 * 是否请求成功
	 * 
	 * @param flag
	 * @return
	 */
	public boolean isSuccess(String flag) {
		return flag.equalsIgnoreCase(ProtocolConstants.FLAG.SUCCESS);
	}
}
