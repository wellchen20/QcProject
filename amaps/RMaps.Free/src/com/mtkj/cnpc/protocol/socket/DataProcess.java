package com.mtkj.cnpc.protocol.socket;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.mtkj.cnpc.activity.LoginActivity;
import com.mtkj.cnpc.protocol.SendBody;
import com.mtkj.cnpc.protocol.constants.DSSProtoDataConstants;
import com.mtkj.cnpc.protocol.constants.DSSProtoDataConstants.DeviceType;
import com.mtkj.cnpc.protocol.constants.DSSProtoDataConstants.DssErrorCode;
import com.mtkj.cnpc.protocol.constants.DSSProtoDataConstants.ProtoMsgType;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_DpData;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_Fransfer;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_GetBDData;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_Head;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_Login_Request;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_SpData;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_TaskData;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_TravelImforResponce;
import com.mtkj.cnpc.protocol.socket.ReadThread.OnReadListener;
import com.mtkj.cnpc.protocol.utils.SocketUtils;
import com.robert.maps.applib.utils.LogFileUtil;
import com.robert.maps.applib.utils.TimeUtil;

/***
 * TCP数据处理
 * 
 * @author TNT
 * 
 */
public class DataProcess implements OnReadListener {

	public static boolean isLoginDscloud = false;
	protected Socket socket;// Socket 数据
	// 目标端口
	public boolean State;
	ReadThread readThread = null;
	Handler msgHandler = null;
	Handler baseHandler = null;
	
	public static DataProcess process = null;
	int count = 0;

	public static DataProcess GetInstance() {
		if (process == null) {
			process = new DataProcess();
		}
		return process;
	}

	/**
	 * @param s
	 * @param ctrlHandle
	 */
	public DataProcess() {
		socket = new Socket();
	}

	/**
	 * @return the msgHandler
	 */
	public Handler getMsgHandler() {
		return msgHandler;
	}

	/**
	 * @param msgHandler
	 *            the msgHandler to set
	 */
	public void setMsgHandler(Handler msgHandler) {
		this.msgHandler = msgHandler;
	}

	/**
	 * @param baseHandler
	 */
	public void setBaseHandler(Handler baseHandler) {
		this.baseHandler = baseHandler;
	}

	/**
	 * 数据发送
	 * 
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public boolean sendData(String data) throws IOException {
		return sendData(data.getBytes());
	}

	/**
	 * 数据发送
	 * 
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public boolean sendData(SendBody body) throws IOException {
		return sendData(body.content);
	}

	/***
	 * 数据发送
	 * 
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public synchronized boolean sendData(byte[] data) throws IOException {
		if (SysConfig.isDSCloud) {
			OutputStream out = socket.getOutputStream();
			if (out == null)
				return false;
			out.write(data);
			LogFileUtil.saveFileToSDCard("SEND MSG: " + new String(data));
		} else {
			OutputStream out = socket.getOutputStream();
			if (out == null)
				return false;
			byte[] dataNew = new byte[data.length + 3];
			for (int i = 0; i < data.length; i++) {
				dataNew[i] = data[i];
			}
			//增加结尾信息
			dataNew[dataNew.length - 3] = (byte) data.length;
			dataNew[dataNew.length - 2] = 0x0D;
			dataNew[dataNew.length - 1] = 0x0A;
			out.write(dataNew);
			if (this.msgHandler != null) {
				Message msg = new Message();
				msg.what = MSG.WRITING;
				msg.obj = new String(data);
				this.msgHandler.sendMessage(msg);
				LogFileUtil.saveFileToSDCard("SEND MSG: " + new String(data));
			}
		}
		return true;
	}
	
	/***
	 * 结束连接
	 * 
	 * @return
	 */
	public boolean stopConn() {
		State = false;
		if (readThread == null)
			return false;
		isLoginDscloud = false;
		readThread.abortRead();
		isRun = false;
		if (heartbeatThread != null) {
			heartbeatThread = null;
		}
		
		/*Message msg = new Message();
		msg.what = MSG.LOGIN;
		msg.obj = isLoginDscloud;
		this.msgHandler.sendMessage(msg);*/
		
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		count=0;
		Log.e("Socket","stop socket");
		return true;
	}

	public boolean isConnected() {
		count++;
		boolean flag = socket.isConnected() && !socket.isClosed();
		Log.e("isConnected",count+"++++"+flag);
		return flag;
	}

	public static boolean isConning = false;
	
	/***
	 * 启动连接
	 * 
	 * @param ip
	 * @param port
	 * @return
	 */
	public boolean startConn(String ip, int port) {
		if (socket.isClosed())
			socket = new Socket();
		SocketAddress remoteAddr = new InetSocketAddress(ip, port);
		isConning = true;
		try {
			socket.connect(remoteAddr, 5000);
			socket.setKeepAlive(true);
		} catch (IOException e) {
			socket = new Socket();
			isConning = false;
			return false;
		} catch (Exception e) {
			socket = new Socket();
			isConning = false;
			return false;
		}
		try {
			this.readThread = new ReadThread(socket, this);
			readThread.start();
			State = true;
			if (this.msgHandler != null) {
				this.msgHandler.sendEmptyMessage(MSG.CONN_START);
			}
			if (LoginActivity.isLogin) {
				sendLogin();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		isConning = false;
		return true;
	}
	
	/**
	 * 发送登录
	 */
	public void sendLogin() {
		try {
			if (SysConfig.isDSCloud) {
				Proto_Login_Request login_Request = null;
				Proto_Head head = null;
					login_Request = Proto_Login_Request.newBuilder()
							.setAppKey(ByteString.copyFrom(SysConfig.APP_KEY, "GB2312"))
							.setUserName(ByteString.copyFrom(SysConfig.SC, "GB2312"))
							.setPasswd(ByteString.copyFrom("", "GB2312"))
							.setDeviceName(ByteString.copyFrom(SysConfig.SC, "GB2312"))
							.setDeviceType(DeviceType.DeviceType_Handset).build();
					head = Proto_Head.newBuilder()
							.setProtoMsgType(ProtoMsgType.ProtoMsgType_Login)
							.setCmdSize(login_Request.toByteArray().length)
							.addReceivers(ByteString.copyFrom("dscloud", "GB2312"))
							.setReceivers(0, ByteString.copyFrom("dscloud", "GB2312"))
							.setSender(ByteString.copyFrom(SysConfig.SC, "GB2312"))
							.setMsgId(0).setPriority(1).setExpired(0).build();
				
				try {
					DataProcess.GetInstance().sendData(SocketUtils.writeBytes(head.toByteArray(), login_Request.toByteArray()));
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
		}
	}
	
	/**
	 * 心跳
	 */
	public int countHeat = 0;
	public long lastHeartbeat = System.currentTimeMillis();
	private long heartbeatInterval = 30 * 1000;
	private boolean isRun = false;
	public class HeartbeatClient implements Runnable {
		public void run() {
			while (isRun) {
				if (SysConfig.isDSCloud) {
					if (countHeat < 5) {
						long currTime = System.currentTimeMillis();
						if (currTime - lastHeartbeat > heartbeatInterval) {
							Proto_Head head = null;
							try {
								head = Proto_Head.newBuilder()
										.setProtoMsgType(ProtoMsgType.ProtoMsgType_Heartbeat)
										.setCmdSize(0)
										.addReceivers(ByteString.copyFrom("dscloud", "GB2312"))
										.setReceivers(0, ByteString.copyFrom("dscloud", "GB2312"))
										.setSender(ByteString.copyFrom(SysConfig.SC, "GB2312"))
										.setMsgId(0).setPriority(1).setExpired(0).build();
								
							
								if (isLoginDscloud) {
									if (isConnected()) {
										sendData(SocketUtils.writeBytes(head.toByteArray(), null));
									}
								}
								countHeat = countHeat + 1;
								lastHeartbeat = currTime;
							} catch (Exception e) {
								reConn();
							}
						}
					} else {
						reConn();
					}
				}
			}
		}
	}

	/***
	 * 连接监听
	 * 
	 * @author TNT
	 * 
	 */
	public interface MSG {
		int CONN_START = 1;
		int CONN_STOP = 2;
		int CONN_BREAK = 3;
		int RECEIVE = 4;
		int WRITING = 5;
		int LOGIN = 6;
		int DRILL = 7;
		int DRILL_PACKAGETNUM = 8;
		int DRILL_WORKREPONSE = 11;
		int SHOT = 9;
		int SHOT_PACKAGETNUM = 10;
		int TASKDATA = 11;
		int TRAVE = 12;
		int CARKEY = 13;
		int NOTICE = 14;
		int TASK_NEWS_ALL = 15;
		int TALK_NEWS = 16;
		int TASK_RECEIVE_OFFLINE = 17;
		int WEB_TASK_NOTICE = 18;
		int SHOT_SETTING = 19;
	}

	@SuppressLint("DefaultLocale")
	@Override
	public void onReading(String info) {
		if (this.msgHandler != null && this.baseHandler != null) {
			Log.e("info",info);
			if (info.split(";").length > 1) {
				if (String.valueOf(Integer.valueOf(SysConfig.SC_ID)).equals(info.split(";")[1])
//						|| info.toUpperCase().contains("GK08")
//						|| info.toUpperCase().contains("GK12")
						|| info.toUpperCase().contains("GK04")) {
					Message msg = new Message();
					msg.what = MSG.RECEIVE;
					msg.obj = info;
					this.msgHandler.sendMessage(msg);
				}else if (info.toUpperCase().contains("GK08")
						|| info.toUpperCase().contains("GK12")){
					Message msg = new Message();
					msg.what = MSG.SHOT_SETTING;
					msg.obj = info;
					Log.e("SHOT_SETTING",info);
					this.baseHandler.sendMessage(msg);
				}
			}
		}
	}

	@Override
	public void onConnBreak() {
		reConn();
	}
	
	private void reConn(){
		isLoginDscloud = false;
		
		Message msg = new Message();
		msg.what = MSG.LOGIN;
		msg.obj = isLoginDscloud;
		this.msgHandler.sendMessage(msg);
		
		stopConn();
		if (!isConning) {
			if(!startConn(SysConfig.IP, SysConfig.PORT)){
				if (this.msgHandler != null) {
					this.msgHandler.sendEmptyMessage(MSG.CONN_BREAK);
				}
			}
		}
	}

	private Thread heartbeatThread;
	/**
	 * 登录
	 */
	@Override
	public void onLogin(int loginResult, byte[] result) {
		if (loginResult == DssErrorCode.DssErrorCode_Suc) {
			isLoginDscloud = true;
			
			Message msg = new Message();
			msg.what = MSG.LOGIN;
			msg.obj = isLoginDscloud;
			Bundle bundle = new Bundle();
			bundle.putByteArray("response", result);
			msg.setData(bundle);
			this.msgHandler.sendMessage(msg);
			
			isRun = true;
			heartbeatThread = new Thread(new HeartbeatClient());
			heartbeatThread.start();
			countHeat = 0;
			lastHeartbeat = System.currentTimeMillis();
		}
	}

	/**
	 * 钻井下药作业结果上传反馈
	 * 
	 * @param msg
	 */
	@Override
	public void onDrillResponse(String stationNo) {
		Message msg = new Message();
		msg.what = MSG.DRILL_WORKREPONSE;
		msg.obj = stationNo;
		this.msgHandler.sendMessage(msg);
	}

	/**
	 * 钻井下药炮点数据更新通知
	 */
	@Override
	public void onDpNotice(boolean isReplace, int packetCount, int msgId) {
		try {
			Proto_Fransfer fransfer = Proto_Fransfer.newBuilder()
					.setPrototype(ProtoMsgType.ProtoMsgType_DpData)
					.setPacketage(packetCount)
					.setPOper(DSSProtoDataConstants.OperType.Oper_Add)
					.setStatus(502)
					.setTime(ByteString.copyFrom(TimeUtil.getCurrentTimeInString(), "GB2312"))
					.setResult(1).build();
			Proto_Head head = Proto_Head.newBuilder()
					.setProtoMsgType(ProtoMsgType.protoMsgType_Fransfer)
					.setCmdSize(fransfer.toByteArray().length)
					.addReceivers(ByteString.copyFrom("dscloud", "GB2312"))
					.setReceivers(0, ByteString.copyFrom("dscloud", "GB2312"))
					.setSender(ByteString.copyFrom(SysConfig.SC, "GB2312"))
					.setMsgId(msgId).setPriority(1).setExpired(0).build();
			sendData(SocketUtils.writeBytes(head.toByteArray(), fransfer.toByteArray()));
		} catch (Exception e) {
			reConn();
		}
		
		Message msg = new Message();
		msg.what = MSG.DRILL_PACKAGETNUM;
		msg.obj = packetCount;
		Bundle bundle = new Bundle();
		bundle.putInt("packetCount",packetCount);
		bundle.putBoolean("isReplace",isReplace);
		msg.setData(bundle);
		this.baseHandler.sendMessage(msg);
	}

	/**
	 * 钻井下药炮点数据
	 */
	@Override
	public void onDpData(Proto_DpData dpData) {
		Message msg = new Message();
		msg.what = MSG.DRILL;
		msg.obj = dpData;
		this.baseHandler.sendMessage(msg);
	}
	
	/**
	 * 钻井下药炮点数据请求
	 * 
	 * @param packagetId
	 */
	public void sendDpDataQuest(int packagetId) {
		try {
			Proto_GetBDData getBDData = Proto_GetBDData.newBuilder()
					.setPacketId(packagetId).build();
			Proto_Head head = Proto_Head.newBuilder()
					.setProtoMsgType(ProtoMsgType.ProtoMsgType_GetDpData)
					.setCmdSize(getBDData.toByteArray().length)
					.addReceivers(ByteString.copyFrom("dscloud", "GB2312"))
					.setReceivers(0, ByteString.copyFrom("dscloud", "GB2312"))
					.setSender(ByteString.copyFrom(SysConfig.SC, "GB2312"))
					.setMsgId(0).setPriority(1).setExpired(0).build();
			sendData(SocketUtils.writeBytes(head.toByteArray(), getBDData.toByteArray()));
		} catch (Exception e) {
			reConn();
		}
	}
	
	/**
	 * 钻井下药获取包成功的回执
	 * 
	 * @param packagetId
	 */
	public void sendDPDataResponse(int packagetId){
		try {
			Proto_GetBDData getBDData = Proto_GetBDData.newBuilder()
					.setPacketId(packagetId).build();
			Proto_Head head = Proto_Head.newBuilder()
					.setProtoMsgType(ProtoMsgType.ProtoMsgType_GetDpDataAnswer)
					.setCmdSize(getBDData.toByteArray().length)
					.addReceivers(ByteString.copyFrom("dscloud", "GB2312"))
					.setReceivers(0, ByteString.copyFrom("dscloud", "GB2312"))
					.setSender(ByteString.copyFrom(SysConfig.SC, "GB2312"))
					.setMsgId(0).setPriority(1).setExpired(0).build();
			sendData(SocketUtils.writeBytes(head.toByteArray(), getBDData.toByteArray()));
		} catch (Exception e) {
			reConn();
		}
	}

	/**
	 * 井炮炮点更新通知
	 * 
	 */
	@Override
	public void onSPNotice(boolean isReplace, int packetCount, int msgId) {
		try {
			Proto_Fransfer fransfer = Proto_Fransfer.newBuilder()
					.setPrototype(ProtoMsgType.ProtoMsgType_TaskData)
					//.setPrototype(ProtoMsgType.ProtoMsgType_SpData)
					.setPacketage(packetCount)
					.setPOper(DSSProtoDataConstants.OperType.Oper_Add)
					.setStatus(502)
					.setTime(ByteString.copyFrom(TimeUtil.getCurrentTimeInString(), "GB2312"))
					.setResult(1).build();
			Proto_Head head = Proto_Head.newBuilder()
					.setProtoMsgType(ProtoMsgType.protoMsgType_Fransfer)
					.setCmdSize(fransfer.toByteArray().length)
					.addReceivers(ByteString.copyFrom("dscloud", "GB2312"))
					.setReceivers(0, ByteString.copyFrom("dscloud", "GB2312"))
					.setSender(ByteString.copyFrom(SysConfig.SC, "GB2312"))
					.setMsgId(msgId).setPriority(1).setExpired(0).build();
			sendData(SocketUtils.writeBytes(head.toByteArray(), fransfer.toByteArray()));
		} catch (Exception e) {
			reConn();
		}

		Message msg = new Message();
		msg.what = MSG.SHOT_PACKAGETNUM;
		msg.obj = packetCount;
		Bundle bundle = new Bundle();
		bundle.putInt("packetCount",packetCount);
		bundle.putBoolean("isReplace",isReplace);
		msg.setData(bundle);
		this.baseHandler.sendMessage(msg);
	}

	/**
	 * 井炮炮点数据
	 * 
	 */
	@Override
	public void onSPData(Proto_SpData spData) {
		Log.e("onSPData01","onSPData01");
		Message msg = new Message();
		msg.what = MSG.SHOT;
		msg.obj = spData;
		this.baseHandler.sendMessage(msg);
	}
	
	/**
	 * 井炮炮点数据请求
	 * 
	 * @param packagetId
	 */
	public void sendSPDataQuest(int packagetId) {
		try {
			Proto_GetBDData getBDData = Proto_GetBDData.newBuilder()
					.setPacketId(packagetId).build();
			Proto_Head head = Proto_Head.newBuilder()
					.setProtoMsgType(ProtoMsgType.ProtoMsgType_GetSpData)
					.setCmdSize(getBDData.toByteArray().length)
					.addReceivers(ByteString.copyFrom("dscloud", "GB2312"))
					.setReceivers(0, ByteString.copyFrom("dscloud", "GB2312"))
					.setSender(ByteString.copyFrom(SysConfig.SC, "GB2312"))
					.setMsgId(0).setPriority(1).setExpired(0).build();

			sendData(SocketUtils.writeBytes(head.toByteArray(), getBDData.toByteArray()));
		} catch (Exception e) {
			reConn();
		}
	}
	
	/**
	 * 井炮炮点数据请求反馈
	 * 
	 * @param packagetId
	 */
	public void sendSPDataResponse(int packagetId) {
		try {
			Proto_GetBDData getBDData = Proto_GetBDData.newBuilder()
					.setPacketId(packagetId).build();
			Proto_Head head = Proto_Head.newBuilder()
					.setProtoMsgType(ProtoMsgType.ProtoMsgType_GetSpDataAnswer)
					.setCmdSize(getBDData.toByteArray().length)
					.addReceivers(ByteString.copyFrom("dscloud", "GB2312"))
					.setReceivers(0, ByteString.copyFrom("dscloud", "GB2312"))
					.setSender(ByteString.copyFrom(SysConfig.SC, "GB2312"))
					.setMsgId(0).setPriority(1).setExpired(0).build();
			sendData(SocketUtils.writeBytes(head.toByteArray(), getBDData.toByteArray()));
		} catch (Exception e) {
			reConn();
		}
	}

	/**
	 * 任务通知反馈
	 */
	@Override
	public void onTaskData(int msgId, Proto_TaskData taskData) {
		try {
			Proto_Fransfer fransfer = Proto_Fransfer.newBuilder()
					.setPrototype(ProtoMsgType.ProtoMsgType_TaskData)
					.setPacketage(taskData.getPId())
					.setPOper(taskData.getPOper())
					.setStatus(502)
					.setTime(ByteString.copyFrom(TimeUtil.getCurrentTimeInString(), "GB2312"))
					.setResult(1).build();
			Proto_Head head = Proto_Head.newBuilder()
					.setProtoMsgType(ProtoMsgType.protoMsgType_Fransfer)
					.setCmdSize(fransfer.toByteArray().length)
					.addReceivers(ByteString.copyFrom("dscloud", "GB2312"))
					.setReceivers(0, ByteString.copyFrom("dscloud", "GB2312"))
					.setSender(ByteString.copyFrom(SysConfig.SC, "GB2312"))
					.setMsgId(msgId).setPriority(1).setExpired(0).build();
			sendData(SocketUtils.writeBytes(head.toByteArray(), fransfer.toByteArray()));
		} catch (Exception e) {
			reConn();
		}
		
		Message msg = new Message();
		msg.what = MSG.TASKDATA;
		msg.obj = taskData;
		this.baseHandler.sendMessage(msg);
	}

	/**
	 * 四汇报反馈
	 * 
	 */
	@Override
	public void onTrave(Proto_TravelImforResponce imforResponce) {
		Message msg = new Message();
		msg.what = MSG.TRAVE;
		msg.obj = imforResponce;
//		this.baseHandler.sendMessage(msg);
		this.msgHandler.sendMessage(msg);
	}

	@Override
	public void onNotice(DSSProtoDataJava.Proto_Notice notice) {
		try {
			DSSProtoDataJava.Proto_Notice proto_notice =
					DSSProtoDataJava.Proto_Notice.newBuilder().setMsg(ByteString.copyFrom("abc", "GB2312")).
							build();
			DSSProtoDataJava.Proto_Head head = DSSProtoDataJava.Proto_Head.newBuilder()
					.setProtoMsgType(ProtoMsgType.ProtoMsgType_Notice)
					.setCmdSize(proto_notice.toByteArray().length)
					.addReceivers(ByteString.copyFrom("DSCMAIN", "GB2312"))
					.setReceivers(0, ByteString.copyFrom("DSCMAIN", "GB2312"))
					.setSender(ByteString.copyFrom("", "GB2312"))
					.setPriority(1).setExpired(0).build();
			DataProcess.GetInstance().sendData(SocketUtils.writeBytes(head.toByteArray(), proto_notice.toByteArray()));
		} catch (Exception e) {
			reConn();
		}

		Message msg = new Message();
		msg.what = MSG.NOTICE;
		msg.obj = notice;
		this.baseHandler.sendMessage(msg);
	}

	@Override
	public void onTaskReceiveOffline(DSSProtoDataJava.Proto_DSG_TaskAssignment_ControledVibrator proto_dsg) {
		Message msg = new Message();
		msg.what = MSG.TASK_RECEIVE_OFFLINE;
		msg.obj = proto_dsg;
		this.baseHandler.sendMessage(msg);
	}

	@Override
	public void onWebTaskNotice(DSSProtoDataJava.Proto_WebTaskNotice notice) {
		Message msg = new Message();
		msg.what = MSG.WEB_TASK_NOTICE;
		msg.obj = notice;
		this.baseHandler.sendMessage(msg);
	}
}
