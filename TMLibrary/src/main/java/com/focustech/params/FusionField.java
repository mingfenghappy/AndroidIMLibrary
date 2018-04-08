package com.focustech.params;

/**
 * 基本配置参数
 */
public class FusionField {

	// 用于通信服务器请求Head中
	// 房博士 1.00.01
	// 租售宝 2.00.03
	public static String VERSION = "1.00.01";
	// 通信服务器地址
	public static String socketAddress = "imtcp.house365.com";
	// 消息头部域名，必须和服务器地址一致
	// 房博士 FBS365
	// 租售宝 JJ365
	public static String messageHeaderDomain = "FBS365";
	// 通信服务器端口
	public static String socketPort = "443";
	// 连接超时时间，单位毫秒
	public static long connectTimeout = 10;
	// 超时重连次数
	public static int retryTime = 3;
	// 心跳包发送间隔
	public static int heartBeatSendInterval = 30;
	// 心跳包线程执行间隔
	public static int heartBeatThreadInterval = 10;

	// 下载文件地址
	public static String downloadUrl = "http://webim.house365.com/tm/file/download?";

	// gost加密 key
	public static String key32 = "=daAyeUKAWkun&umenkMbJYFzCeb=88d";

	static {
		try {
			Class clazz = Class.forName("com.renyu.mt.params.InitParams");
			VERSION = clazz.getField("VERSION").get(clazz).toString();
			messageHeaderDomain = clazz.getField("messageHeaderDomain").get(clazz).toString();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
