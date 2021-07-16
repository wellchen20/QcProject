package com.mtkj.cnpc.protocol.shot;

import java.util.ArrayList;
import java.util.List;

import com.mtkj.cnpc.protocol.ReceiveBody;
import com.mtkj.cnpc.protocol.constants.ProtocolConstants;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.robert.maps.applib.utils.LogFileUtil;

/***
 * 请求排队反馈<br>
 * 
 * GK04;手持Id；手持Id；手持Id；……<br>
 * 例如:GK04;1;2;3...</br>
 * 
 * @author drh
 * 
 */
public class GK04 extends ReceiveBody {

	/***
	 * 是否排队成功
	 */
	public boolean isOk = false;

	/***
	 * 排队失败错误提醒
	 */
	public String errorInfo = "";
	
	public String error01 = "配对池中没有此设备";
	
	public List<String> lstQueueInfo = new ArrayList<String>();
	
	public GK04(String content) {
		super(ProtocolConstants.NAME_JINGPAO.GK04, content);
	}

	@Override
	public Object parseMsg(String msg) {
		if (msg != null && msg.length() > 0) {
			String[] datas = msg.split(ProtocolConstants.SPLITE_SYMBLE);
			if (datas != null && datas.length >= 3) {
				if(lstQueueInfo == null){
					lstQueueInfo = new ArrayList<String>();
				}else{
					lstQueueInfo.clear();
				}
				if ("0".equals(datas[1])) {
					for (int i = 2; i <= datas.length - 1; i++) {
						lstQueueInfo.add(datas[i]);
					}
				} else if (String.valueOf(Integer.valueOf(SysConfig.SC_ID)).equals(datas[1])) {
					for (int i = 2; i <= datas.length - 1; i++) {
						lstQueueInfo.add(datas[i]);
					}
				}
				
			} else {
				LogFileUtil.saveFileToSDCard(msg);
			}
		}
		return lstQueueInfo;
	}

}
