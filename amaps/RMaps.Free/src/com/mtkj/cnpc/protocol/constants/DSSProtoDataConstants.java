package com.mtkj.cnpc.protocol.constants;

public class DSSProtoDataConstants {
	
	//操作类型
	public class OperType {
		public static final int Oper_Add = 1;
		public static final int Oper_Del = 2;
		public static final int Oper_Mod = 3;
	}
	
	public class ResultType {
		public static final int Result_Sucess = 1;
		public static final int Result_Fail = 2;
	};

	// 设备类型
	public class DeviceType {
		public static final int DeviceType_DSC = 0x01; //
		public static final int DeviceType_DSG = 0x02; //
		public static final int DeviceType_WSC = 0x03;
		public static final int DeviceType_Handset = 0x04; //
		public static final int DeviceType_Drillset = 0x05;
		public static final int DeviceType_SmartGuide = 0x06;
		public static final int DeviceType_Other = 0x07;
	};
	
	public class businessType {
		public static final int BusType_Leader = 0x02; //领导
		public static final int BusType_Construction = 0x03; //施工组
		public static final int BusType_Measure = 0x04; //测量组
		public static final int BusType_RoutePlan = 0x05; //推土
		public static final int BusType_Drill = 0x07; // 钻井下药
		public static final int BusType_WellShot = 0x0a; // 井炮
		public static final int BusType_VibShot = 0x0b; //震源
		public static final int BusType_Vehicle = 0x13; //车辆组
		public static final int BusType_Arrange = 0x09; // 放线
	}

	// 协议命令类型
	public class ProtoMsgType {
		public static final int ProtoMsgType_None = 0x00; //

		public static final int ProtoMsgType_Login = 0x01; // 登陆协议
		public static final int ProtoMsgType_LoginResponse = 0x02; // 登陆响应
		public static final int ProtoMsgType_GetOnlineStatus = 0x03; // 请求设备登陆信息
		public static final int ProtoMsgType_OnlineStatus = 0x04;// 通知dsc
																	// dsg登陆信息（上下线）
		public static final int ProtoMsgType_RedisBDData = 0x05;// 发送获取sps，geo等数据的请求
		public static final int ProtoMsgType_RedisBDDataResponse = 0x06;// dsg获取sps，geo等数据

		public static final int ProtoMsgType_Heartbeat = 0x07;// 心跳协议
		public static final int ProtoMsgType_HeartbeatResponse = 0x08;// 心跳协议响应

		public static final int ProtoMsgType_UploadDeviceTrace = 0x09;// 上报轨迹协议
		public static final int ProtoMsgType_GetTraceFileList = 0x0a;// 获取轨迹文件列表协议
		public static final int ProtoMsgType_RetTraceFileList = 0x0b;// 轨迹文件列表

		public static final int ProtoMsgType_ShotNotice = 0x0c;// 放炮通知
		public static final int ProtoMsgType_ShotRecord = 0x0d;// 放炮结果
		public static final int ProtoMsgType_ShotRecordResponse = 0x0e;// 放炮结果

		public static final int ProtoMsgType_GetConfigData = 0x0f;// 获取配置参数
		public static final int ProtoMsgType_ConfigData = 0x10;// 配置参数
		public static final int ProtoMsgType_ChangeConfigData = 0x11;// 修改配置参数
		public static final int ProtoMsgType_ChangeConfigDataResponse = 0x12;// 修改配置参数的响应

		public static final int ProtoMsgType_GetSpsPackageNum = 0x13;// 获取server端当前sps总包数
		public static final int ProtoMsgType_SpsPackageNum = 0x14;// sps总包数
		public static final int ProtoMsgType_SpsData = 0x15; // 炮点源数据
		public static final int ProtoMsgType_SpsDataResponse = 0x16; // 炮点源数据接收反馈
		public static final int ProtoMsgType_SpsDataCancel = 0x17;// DSC取消发送炮点源数据
		public static final int ProtoMsgType_GetSpsDataStatus = 0x18;// DSC向server发送获取sps接收状况信息的请求
		public static final int ProtoMsgType_SpsDataStatus = 0x19;// 返回给DSC设备的sps接收情况
		public static final int ProtoMsgType_SpsDataNotice = 0x1a;// 发送炮点源数据更新通知
		public static final int ProtoMsgType_SpsDataNoticeResponse = 0x1b; // 获得放炮源数据通知的反馈
		public static final int ProtoMsgType_GetSpsData = 0x1c;// 发送获取放炮源数据的请求
		public static final int ProtoMsgType_GetSpsDataAnswer = 0x1d;// 设备发送获取包成功的回执

		public static final int ProtoMsgType_TaskData = 0x1e; // 任务数据
		public static final int ProtoMsgType_TaskDataResponse = 0x1f;// server接收任务数据反馈

		public static final int ProtoMsgType_Geo = 0x20;// Geo数据
		public static final int ProtoMsgType_GeoResponse = 0x21;// Geo数据的响应
		public static final int ProtoMsgType_GeoDataNotice = 0x22; // 发送Geo数据更新通知
		public static final int ProtoMsgType_GeoDataNoticeResponse = 0x23;// 获得Geo数据通知的反馈
		public static final int ProtoMsgType_GetGeoData = 0x24; // 发送Geo源数据的请求
		public static final int ProtoMsgType_GetGeoDataFail = 0x25;// 获取Geo数据失败
		public static final int ProtoMsgType_GetGeoDataAnswer = 0x26;// 设备发送Geo成功的回执
		public static final int ProtoMsgType_GeoDel = 0x27;// 删除Geo包
		public static final int ProtoMsgType_GeoDelResponse = 0x28;// 删除Geo包反馈

		public static final int ProtoMsgType_CheckPoint = 0x29;// 检查点数据
		public static final int ProtoMsgType_CheckPointResponse = 0x2a;// 检查点数据的响应

		public static final int ProtoMsgType_Fleet = 0x2b; // 分组数据
		public static final int ProtoMsgType_Notice = 0x2c; // 公告数据

		public static final int protoMsgType_Fransfer = 0x2d; // 转发数据的反馈

		public static final int protoMsgType_FPSData = 0x2e;
		public static final int protoMsgType_FPSDataResponse = 0x2f;// 井炮数据

		public static final int protoMsgType_ChangeDev = 0x30;// 添加删除设备
		public static final int protoMsgType_ChangeDevResponse = 0x31;// 添加删除设备的反馈
		public static final int protoMsgType_ChangeOrg = 0x32;// 添加组织机构
		public static final int protoMsgType_ChangeOrgResponse = 0x33;// 添加组织机构的反馈
		public static final int protoMsgType_ChangeEmployee = 0x34;// 添加人员
		public static final int protoMsgType_ChangeEmployeeResponse = 0x35;// 添加人员的反馈

		public static final int protoMsgType_ProjectInfoRequest = 0x36; // 获取项目信息
		public static final int protoMsgType_ProjectInfoResponse = 0x37;// 返回项目信息

		public static final int protoMsgType_Hankdata = 0x38;// 线束数据
		public static final int protoMsgType_HankdataResponse = 0x39;// 线束数据

		public static final int protoMsgType_Schedule = 0x3a;// 计划信息
		public static final int protoMsgType_ScheduleResponse = 0x3b;// 计划信息的反馈
		public static final int protoMsgType_DailySchedule = 0x3c;// 日计划信息
		public static final int protoMsgType_DailyScheduleResponse = 0x3d;// 日计划信息的反馈
		public static final int protoMsgType_Progress = 0x3e;// 进度信息
		public static final int protoMsgType_ProgressResponse = 0x3f;// 进度信息的反馈

		public static final int protoMsgType_HandsetToWSC = 0x40;// 手持机 TO WSC
																	// 数据
		public static final int protoMsgType_WSCToHandset = 0x41;// WSC TO 手持机
																	// 数据
		public static final int protoMsgType_HandsetTrace = 0x42;//轨迹数据 手持 TO  dscloud 
		public static final int protoMsgType_HandsetTraceResponse = 0x43;//轨迹数据 dscloud TO 手持
		
		public static final int protoMsgType_DpRecord = 0x44; // 钻井下药结果上传
		public static final int protoMsgType_DpRecordResponse = 0x45; // 钻井下药结果反馈
		
        public static final int ProtoMsgType_GetDpPackageNum = 0x46;//获取server端当前dpdata总包数
        public static final int ProtoMsgType_DpPackageNum = 0x47;//Dp总包数
        public static final int ProtoMsgType_DpData = 0x48; //钻井下药源数据
        public static final int ProtoMsgType_DpDataResponse = 0x49; //钻井下药数据接收反馈
        public static final int ProtoMsgType_DpDataCancel = 0x4a; //DSC取消发送钻井下药数据
        public static final int ProtoMsgType_GetDpDataStatus = 0x4b; //DSC向server发送获取Dp接收状况信息的请求
        public static final int ProtoMsgType_DpDataStatus = 0x4c; //返回给DSC设备的Dp接收情况
        public static final int ProtoMsgType_DpDataNotice = 0x4d; //发送钻井下药任务数据更新通知
        public static final int ProtoMsgType_DpDataNoticeResponse = 0x4e; //获得钻井下药通知的反馈
        public static final int ProtoMsgType_GetDpData = 0x4f; //发送获取钻井下药数据的请求
        public static final int ProtoMsgType_GetDpDataAnswer = 0x50; //设备发送获取包成功的回执

        public static final int ProtoMsgType_GetSpPackageNum = 0x51;//获取server端当前Spdata总包数
        public static final int ProtoMsgType_SpPackageNum = 0x52; //Sp总包数
        public static final int ProtoMsgType_SpData	= 0x53; //井炮源数据
        public static final int ProtoMsgType_SpDataResponse = 0x54; //井炮数据接收反馈
        public static final int ProtoMsgType_SpDataCancel = 0x55; //DSC取消发送井炮数据
        public static final int ProtoMsgType_GetSpDataStatus = 0x56;//DSC向server发送获取Sp接收状况信息的请求
        public static final int ProtoMsgType_SpDataStatus = 0x57; //返回给DSC设备的Sp接收情况
        public static final int ProtoMsgType_SpDataNotice = 0x58; //发送井炮数据更新通知
        public static final int ProtoMsgType_SpDataNoticeResponse= 0x59; //获得井炮通知的反馈
        public static final int ProtoMsgType_GetSpData = 0x5a; //发送获取井炮数据的请求
        public static final int ProtoMsgType_GetSpDataAnswer = 0x5b;//设备发送获取包成功的回执
        
        public static final int ProtoMsgType_UserLogin = 0x62;  //用户登录
        public static final int ProtoMsgType_UserLoginResponse = 0x63;  //用户登录反馈
        public static final int ProtoMsgType_CarTravel = 0x64;  //车辆四汇报协议
        public static final int ProtoMsgType_CarTravelResponse = 0x65; // 车辆四汇报反馈

		public static final int ProtoMsgType_DSG_TaskAssignment = 0x69;//离线任务点接收
		public static final int ProtoMsgType_WebTaskNotice = 0x6a;//任务发送通知
	};

	// 错误码
	public class DssErrorCode {
		public static final int DssErrorCode_Suc = 1; // 成功
		public static final int DssErrorCode_AdminDSCSuc = 2; // dscadmin登陆成功

		// 用户相关
		public static final int DssErrorCode_UserNameError = 3; // 用户名错误
		public static final int DssErrorCode_PswError = 4; // 密码错误
		public static final int DssErrorCode_UserAlreadyLogin = 5; // 用户已登录
		public static final int DssErrorCode_DeviceAlreadyLogin = 6; // 设备已登陆
		public static final int DssErrorCode_UserLoginTimeout = 7; // 登录超时
		public static final int DssErrorCode_AppKeyError = 8; // 未找到该key
		public static final int DssErrorCode_DeviceNoExist = 9; // 设备不存在

		// 协议相关
		public static final int DssErrorCode_ProtoDataError = 10; // 协议数据错误

		// 任务相关
		public static final int DssErrorCode_SameTaskId = 11;// 添加任务时发现已有该任务
		public static final int DssErrorCode_NoTaskId = 12;// 删除任务时发现没有该任务

		public static final int DssErrorCode_NoNeedTransfer = 13;// 不需要转发
		public static final int DssErrorCode_ReSend = 14;// 登陆时候发送消息队列的数据
	};
}
