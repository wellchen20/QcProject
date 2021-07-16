package com.mtkj.cnpc.protocol.socket;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mtkj.cnpc.protocol.constants.DSSProtoDataConstants;
import com.mtkj.cnpc.protocol.constants.SysConfig;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_BDDataNotice;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_DpData;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_DpRecordResponse;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_Head;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_Login_Response;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_SpData;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_TaskData;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_TravelImforResponce;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_UserLogin_Response;
import com.mtkj.cnpc.protocol.shot.DSSProtoDataJava.Proto_WellShotData;
import com.mtkj.cnpc.protocol.utils.SocketUtils;

/***
 * 数据读取
 *
 * @author TNT
 *
 */
public class ReadThread extends Thread {
	private int dataBuffer = 1024 * 2 * 1024;
	private int versionLength = 24;
	boolean state;
	byte[] sData;
	byte[] dsCloudData = new byte[dataBuffer];
	byte[] dsData = new byte[dataBuffer];
	Socket socket;
	OnReadListener listener = null;
	BufferedReader in = null;
	BufferedWriter out = null;
	PrintWriter writer = null;

	public ReadThread(Socket socket, OnReadListener listener)
			throws IOException {
		this.socket = socket;
		this.listener = listener;
		if (this.socket != null) {
			if (SysConfig.isDSCloud) {
				in = new BufferedReader(new InputStreamReader(
						this.socket.getInputStream(), "UTF-8"));
			} else {
				in = new BufferedReader(new InputStreamReader(
						this.socket.getInputStream(), "GBK"));
			}
		}
	}

	/*
	 * @see java.lang.Thread#run() 线程接收主循环
	 */
	public void run() {
		readMethod0();
	}

	private void readMethod0() {
		state = true;
		int rlRead;
		int byteLength = 0;
		int headlength = 0;
		int bodylength = 0;
		boolean isBody = false;
		boolean isFrist = true;
		if (SysConfig.isDSCloud) {
			try {
				while (state) {
					InputStream inputStream = socket.getInputStream();
					if (inputStream.available() != 0 && this.listener != null) {
						rlRead = inputStream.read(dsCloudData);
						isFrist = true;
						for (int i = 0; i < rlRead; i++) {
							dsData[byteLength + i] = dsCloudData[i];
						}
						while (rlRead > 0) {
							if (!isBody) {
								if (isFrist) {
									// 解析version
									if (byteLength + rlRead >= versionLength) {
										// 获取head 长度
										byte[] headlengths = new byte[4];
										headlengths[0] = dsData[16];
										headlengths[1] = dsData[17];
										headlengths[2] = dsData[18];
										headlengths[3] = dsData[19];
										headlength = SocketUtils.bytesToInt(headlengths, 0);

										// 获取body 长度
										byte[] bodylengths = new byte[4];
										bodylengths[0] = dsData[20];
										bodylengths[1] = dsData[21];
										bodylengths[2] = dsData[22];
										bodylengths[3] = dsData[23];
										bodylength = SocketUtils.bytesToInt(bodylengths, 0);
										isBody = true;

										// 解析head和body
										if ((byteLength + rlRead) >= (versionLength + headlength + bodylength)) {
											// 获取head
											byte[] head = new byte[headlength];
											for (int i = 0; i < head.length; i++) {
												head[i] = dsData[versionLength + i];
											}
											Proto_Head proto_Head = Proto_Head.parseFrom(head);
											// 获取body
											byte[] body = new byte[bodylength];
											for (int i = 0; i < bodylength; i++) {
												body[i] = dsData[versionLength + headlength
														+ i];
											}
											parseProtohead(proto_Head, body);

											// 将解析的删除，没有解析的往前提
											rlRead = byteLength + rlRead - versionLength - headlength - bodylength;
											int length = versionLength + headlength + bodylength;
											byte[] data = new byte[rlRead];
											for (int i = 0; i < data.length; i++) {
												data[i] = dsData[length + i];
											}
											dsData = new byte[dataBuffer];
											dsCloudData = new byte[dataBuffer];
											for (int i = 0; i < data.length; i++) {
												dsData[i] = data[i];
											}

											headlength = 0;
											bodylength = 0;
											byteLength = rlRead;
											isBody =false;
											isFrist = false;
										} else {
											byte[] data = new byte[rlRead + byteLength - versionLength];
											for (int i = 0; i < data.length; i++) {
												data[i] = dsData[versionLength + i];
											}
											dsData = new byte[dataBuffer];
											dsCloudData = new byte[dataBuffer];
											for (int i = 0; i < data.length; i++) {
												dsData[i] = data[i];
											}
											byteLength = rlRead + byteLength - versionLength;

											break;
										}
									} else {
										byte[] data = new byte[rlRead + byteLength];
										for (int i = 0; i < data.length; i++) {
											data[i] = dsData[i];
										}
										dsData = new byte[dataBuffer];
										dsCloudData = new byte[dataBuffer];
										for (int i = 0; i < data.length; i++) {
											dsData[i] = data[i];
										}
										byteLength = rlRead + byteLength;

										break;
									}
								} else {
									if (rlRead >= versionLength) {
										// 获取head 长度
										byte[] headlengths = new byte[4];
										headlengths[0] = dsData[16];
										headlengths[1] = dsData[17];
										headlengths[2] = dsData[18];
										headlengths[3] = dsData[19];
										headlength = SocketUtils.bytesToInt(headlengths, 0);

										// 获取body 长度
										byte[] bodylengths = new byte[4];
										bodylengths[0] = dsData[20];
										bodylengths[1] = dsData[21];
										bodylengths[2] = dsData[22];
										bodylengths[3] = dsData[23];
										bodylength = SocketUtils.bytesToInt(bodylengths, 0);
										isBody = true;

										// 解析head和body
										if (rlRead >= (versionLength + headlength + bodylength)) {
											// 获取head
											byte[] head = new byte[headlength];
											for (int i = 0; i < head.length; i++) {
												head[i] = dsData[versionLength + i];
											}
											Proto_Head proto_Head = Proto_Head.parseFrom(head);
											// 获取body
											byte[] body = new byte[bodylength];
											for (int i = 0; i < bodylength; i++) {
												body[i] = dsData[versionLength + headlength
														+ i];
											}
											parseProtohead(proto_Head, body);

											// 将解析的删除，没有解析的往前提
											rlRead = rlRead - versionLength - headlength - bodylength;
											int length = versionLength + headlength + bodylength;
											byte[] data = new byte[rlRead];
											for (int i = 0; i < data.length; i++) {
												data[i] = dsData[length + i];
											}
											dsData = new byte[dataBuffer];
											dsCloudData = new byte[dataBuffer];
											for (int i = 0; i < data.length; i++) {
												dsData[i] = data[i];
											}

											headlength = 0;
											bodylength = 0;
											byteLength = rlRead;
											isBody =false;
											isFrist = false;
										} else {
											byte[] data = new byte[rlRead - versionLength];
											for (int i = 0; i < data.length; i++) {
												data[i] = dsData[versionLength + i];
											}
											dsData = new byte[dataBuffer];
											dsCloudData = new byte[dataBuffer];
											for (int i = 0; i < data.length; i++) {
												dsData[i] = data[i];
											}
											byteLength = rlRead - versionLength;

											break;
										}
									} else {
										byte[] data = new byte[rlRead];
										for (int i = 0; i < data.length; i++) {
											data[i] = dsData[i];
										}
										dsData = new byte[dataBuffer];
										dsCloudData = new byte[dataBuffer];
										for (int i = 0; i < data.length; i++) {
											dsData[i] = data[i];
										}
										byteLength = rlRead;

										break ;
									}
								}
							} else {
								// 解析body
								if (isFrist) {
									if ((byteLength + rlRead) >= (headlength + bodylength)) {
										// 获取head
										byte[] head = new byte[headlength];
										for (int i = 0; i < head.length; i++) {
											head[i] = dsData[i];
										}
										Proto_Head proto_Head = Proto_Head.parseFrom(head);
										// 获取body
										byte[] body = new byte[bodylength];
										for (int i = 0; i < bodylength; i++) {
											body[i] = dsData[headlength + i];
										}
										parseProtohead(proto_Head, body);

										// 将解析的删除，没有解析的往前提
										rlRead = byteLength + rlRead - headlength - bodylength;
										int length = headlength + bodylength;
										byte[] data = new byte[rlRead];
										for (int i = 0; i < data.length; i++) {
											data[i] = dsData[length + i];
										}
										dsData = new byte[dataBuffer];
										dsCloudData = new byte[dataBuffer];
										for (int i = 0; i < data.length; i++) {
											dsData[i] = data[i];
										}

										isBody =false;
										headlength = 0;
										bodylength = 0;
										byteLength = rlRead;
										isFrist = false;
									} else {
										byte[] data = new byte[rlRead + byteLength];
										for (int i = 0; i < data.length; i++) {
											data[i] = dsData[i];
										}
										dsData = new byte[dataBuffer];
										dsCloudData = new byte[dataBuffer];
										for (int i = 0; i < data.length; i++) {
											dsData[i] = data[i];
										}
										byteLength = rlRead + byteLength;

										break;
									}
								} else {
									if (rlRead >= (headlength + bodylength)) {
										// 获取head
										byte[] head = new byte[headlength];
										for (int i = 0; i < head.length; i++) {
											head[i] = dsData[i];
										}
										Proto_Head proto_Head = Proto_Head.parseFrom(head);
										// 获取body
										byte[] body = new byte[bodylength];
										for (int i = 0; i < bodylength; i++) {
											body[i] = dsData[headlength + i];
										}
										parseProtohead(proto_Head, body);

										// 将解析的删除，没有解析的往前提
										rlRead = rlRead - headlength - bodylength;
										int length = headlength + bodylength;
										byte[] data = new byte[rlRead];
										for (int i = 0; i < data.length; i++) {
											data[i] = dsData[length + i];
										}
										dsData = new byte[dataBuffer];
										dsCloudData = new byte[dataBuffer];
										for (int i = 0; i < data.length; i++) {
											dsData[i] = data[i];
										}

										isBody =false;
										headlength = 0;
										bodylength = 0;
										byteLength = rlRead;
									} else {
										byte[] data = new byte[rlRead];
										for (int i = 0; i < data.length; i++) {
											data[i] = dsData[i];
										}
										dsData = new byte[dataBuffer];
										dsCloudData = new byte[dataBuffer];
										for (int i = 0; i < data.length; i++) {
											dsData[i] = data[i];
										}
										byteLength = rlRead;

										break;
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
				state = false;
				byteLength = 0;
				headlength = 0;
				bodylength = 0;
				isBody = false;
				if (this.listener != null) {
					this.listener.onConnBreak();
					dsCloudData = null;
				}
			}
		} else {
			if (this.sData == null || this.sData.length == 0) {
				this.sData = new byte[1000];
			}
			try {
				while (state) {
					rlRead = socket.getInputStream().read(sData);// sData
					// 对方断开返回-1
					if (rlRead > 0) {
						if (this.listener != null) {
							try {
								msgReading(new String(sData, "GBK").toString());
							} catch (Exception e) {
							}
						}
						Arrays.fill(sData, (byte) 0);
						// 将解析的删除，没有解析的往前提
//						String result = new String(sData, "GBK").toString(); 
//						int index = result.indexOf("\n");
//						byte[] data = new byte[sData.length - index];
//						for (int i = 0; i < data.length; i++) {
//							data[i] = sData[index + i];
//						}
//						sData = new byte[1000];
//						for (int i = 0; i < data.length; i++) {
//							sData[i] = data[i];
//						}
					} else {
						if (this.listener != null) {
							this.listener.onConnBreak();
						}
						state = false;
						break;
					}
				}
			} catch (Exception e) {
				state = false;
				if (this.listener != null) {
					this.listener.onConnBreak();
				}
			}
		}
	}

	private void parseProtohead(Proto_Head proto_Head, byte[] body) {
		// 处理head和body
		switch (proto_Head.getProtoMsgType()) {
			case DSSProtoDataConstants.ProtoMsgType.ProtoMsgType_LoginResponse: // 登陆反馈
				DataProcess.GetInstance().countHeat = 0;
				DataProcess.GetInstance().lastHeartbeat = System.currentTimeMillis();
				msgReadingLogin(body);
				break;

			case DSSProtoDataConstants.ProtoMsgType.ProtoMsgType_UserLoginResponse: // 手机号登录
				DataProcess.GetInstance().countHeat = 0;
				DataProcess.GetInstance().lastHeartbeat = System.currentTimeMillis();
				msgReadingUserLogin(body);
				break;

			case DSSProtoDataConstants.ProtoMsgType.protoMsgType_WSCToHandset: // WSC消息
				DataProcess.GetInstance().countHeat = 0;
				DataProcess.GetInstance().lastHeartbeat = System.currentTimeMillis();
				msgReadingShotData(body);
				break;

			case DSSProtoDataConstants.ProtoMsgType.protoMsgType_DpRecordResponse: // 钻井任务结果上传反馈
				DataProcess.GetInstance().countHeat = 0;
				DataProcess.GetInstance().lastHeartbeat = System.currentTimeMillis();
				msgDrillRespon(body);
				break;

			case DSSProtoDataConstants.ProtoMsgType.ProtoMsgType_HeartbeatResponse: // 心跳包
				DataProcess.GetInstance().countHeat = 0;
				DataProcess.GetInstance().lastHeartbeat = System.currentTimeMillis();
				break;

			case DSSProtoDataConstants.ProtoMsgType.ProtoMsgType_DpDataNotice: // 钻井下药炮点更新通知
				DataProcess.GetInstance().countHeat = 0;
				DataProcess.GetInstance().lastHeartbeat = System.currentTimeMillis();
				int msgDpid = proto_Head.getMsgId();
				msgDpNotice(msgDpid, body);
				break;

			case DSSProtoDataConstants.ProtoMsgType.ProtoMsgType_DpData: // 钻井下药炮点数据
				DataProcess.GetInstance().countHeat = 0;
				DataProcess.GetInstance().lastHeartbeat = System.currentTimeMillis();
				msgDpData(body);
				break;

			case DSSProtoDataConstants.ProtoMsgType.ProtoMsgType_SpDataNotice: // 井炮炮点更新通知
				DataProcess.GetInstance().countHeat = 0;
				DataProcess.GetInstance().lastHeartbeat = System.currentTimeMillis();
				int msgSPSid = proto_Head.getMsgId();
				msgSPNotice(msgSPSid, body);
				break;

			case DSSProtoDataConstants.ProtoMsgType.ProtoMsgType_SpData: // 井炮炮点数据
				Log.e("SpData", "SpDataxxx");
				DataProcess.GetInstance().countHeat = 0;
				DataProcess.GetInstance().lastHeartbeat = System.currentTimeMillis();
				msgSPData(body);
				break;

			case DSSProtoDataConstants.ProtoMsgType.ProtoMsgType_TaskData: // 每日任务
				DataProcess.GetInstance().countHeat = 0;
				DataProcess.GetInstance().lastHeartbeat = System.currentTimeMillis();
				int msgTaskid = proto_Head.getMsgId();
				msgTaskData(msgTaskid, body);
				break;

			case DSSProtoDataConstants.ProtoMsgType.ProtoMsgType_CarTravelResponse: // 车辆四汇报反馈

				DataProcess.GetInstance().countHeat = 0;
				DataProcess.GetInstance().lastHeartbeat = System.currentTimeMillis();
				msgTrave(body);
				break;

			case DSSProtoDataConstants.ProtoMsgType.ProtoMsgType_Notice://任务下发
				DataProcess.GetInstance().countHeat = 0;
				DataProcess.GetInstance().lastHeartbeat = System.currentTimeMillis();
				msgNotice(body);
				break;

			case DSSProtoDataConstants.ProtoMsgType.ProtoMsgType_DSG_TaskAssignment://离线任务接受
				DataProcess.GetInstance().countHeat = 0;
				DataProcess.GetInstance().lastHeartbeat = System.currentTimeMillis();
				msgAssignment(body);
				break;
			case DSSProtoDataConstants.ProtoMsgType.ProtoMsgType_WebTaskNotice:
				DataProcess.GetInstance().countHeat = 0;
				DataProcess.GetInstance().lastHeartbeat = System.currentTimeMillis();
				msgWebTaskNotice(body);
		}
	}


	public void abortRead() {
		if (socket == null)
			return;
		try {
			socket.shutdownInput();
			socket.shutdownOutput();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		state = false;
	}

	/**
	 * 电台消息解析
	 *
	 * @param msg
	 */
	private void msgReading(String msg) {
		if (msg != null && msg.length() > 0) {
			String[] results = msg.split("\r\n");
			if (results != null && results.length > 0) {
				for (String oneMsg : results) {
					if (oneMsg.startsWith("GK")) {
						this.listener.onReading(oneMsg);
					}
				}

			}
		}
	}

	private void msgReadingShotSetting(){

	}

	/**
	 * 解析WSC TO 手持数据
	 *
	 * @param msg
	 */
	private void msgReadingShotData(byte[] msg) {
		try {
			Proto_WellShotData proto_WellShotData = Proto_WellShotData.parseFrom(msg);
			msgReading(proto_WellShotData.getMdata().toStringUtf8());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 解析登录数据
	 *
	 * @param msg
	 */
	private void msgReadingLogin(byte[] msg) {
		try {
			Proto_Login_Response login_Response = Proto_Login_Response.parseFrom(msg);
			this.listener.onLogin(login_Response.getResult(), msg);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解析用户登录数据
	 *
	 * @param msg
	 */
	private void msgReadingUserLogin(byte[] msg) {
		try {
			Proto_UserLogin_Response login_Response = Proto_UserLogin_Response.parseFrom(msg);
			this.listener.onLogin(login_Response.getResult(), msg);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 钻井下药作业结果上传反馈
	 *
	 * @param msg
	 */
	private void msgDrillRespon(byte[] msg) {
		try {
			Proto_DpRecordResponse proto_DpRecordResponse = Proto_DpRecordResponse.parseFrom(msg);
			this.listener.onDrillResponse(proto_DpRecordResponse.getStationNo().toStringUtf8());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Dscloud向drillset发送钻井下药   任务数据更新通知
	 *
	 * @param body
	 */
	private void msgDpNotice(int msgId, byte[] msg) {
		try {
			Proto_BDDataNotice bdDataNotice = Proto_BDDataNotice.parseFrom(msg);
			this.listener.onDpNotice(bdDataNotice.getReplace(), bdDataNotice.getPacketCount(), msgId);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 钻井下药数据
	 *
	 * @param body
	 */
	private void msgDpData(byte[] msg) {
		try {
			Proto_DpData dpData = Proto_DpData.parseFrom(msg);
			this.listener.onDpData(dpData);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Dscloud向handset发送井炮   任务数据更新通知
	 *
	 * @param body
	 */
	private void msgSPNotice(int msgId, byte[] msg) {
		try {
			Proto_BDDataNotice bdDataNotice = Proto_BDDataNotice.parseFrom(msg);
			this.listener.onSPNotice(bdDataNotice.getReplace(), bdDataNotice.getPacketCount(), msgId);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 井炮数据
	 *
	 * @param body
	 */
	private void msgSPData(byte[] msg) {
		try {
			Log.e("onSPData01","onSPData01");
			Proto_SpData dpData = Proto_SpData.parseFrom(msg);
			this.listener.onSPData(dpData);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 任务数据
	 *
	 * @param msg
	 */
	private void msgTaskData(int msgId, byte[] msg) {
		try {
			Proto_TaskData taskData = Proto_TaskData.parseFrom(msg);
			this.listener.onTaskData(msgId, taskData);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 通知任务发送
	 *
	 * @param msg
	 */
	private void msgWebTaskNotice(byte[] msg) {
		try {
			DSSProtoDataJava.Proto_WebTaskNotice notice = DSSProtoDataJava.Proto_WebTaskNotice.parseFrom(msg);
			this.listener.onWebTaskNotice(notice);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 车辆四汇报
	 *
	 * @param msg
	 */
	private void msgTrave(byte[] msg) {
		try {
			Proto_TravelImforResponce imforResponce = Proto_TravelImforResponce.parseFrom(msg);
			this.listener.onTrave(imforResponce);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void msgNotice(byte[] msg) {
		try {
			DSSProtoDataJava.Proto_Notice proto_notice = DSSProtoDataJava.Proto_Notice.parseFrom(msg);
			this.listener.onNotice(proto_notice);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void msgAssignment(byte[] msg){
		try {
			DSSProtoDataJava.Proto_DSG_TaskAssignment_ControledVibrator proto_dsg = DSSProtoDataJava.Proto_DSG_TaskAssignment_ControledVibrator.parseFrom(msg);
			this.listener.onTaskReceiveOffline(proto_dsg);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}


	/***
	 * 连接监听
	 *
	 * @author TNT
	 *
	 */
	public interface OnReadListener {
		void onReading(String info);

		void onConnBreak();

		void onLogin(int loginResult, byte[] result);

		void onDrillResponse(String stationNo);

		void onDpNotice(boolean isReplace, int packetCount, int msgId);

		void onDpData(Proto_DpData dpData);

		void onSPNotice(boolean isReplace, int packetCount, int msgId);

		void onSPData(Proto_SpData spData);

		void onTaskData(int msgId, Proto_TaskData taskData);

		void onTrave(Proto_TravelImforResponce imforResponce);

		void onNotice(DSSProtoDataJava.Proto_Notice notice);

		void onTaskReceiveOffline(DSSProtoDataJava.Proto_DSG_TaskAssignment_ControledVibrator proto_dsg);

		void onWebTaskNotice(DSSProtoDataJava.Proto_WebTaskNotice notice);
	}
}
