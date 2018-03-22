package com.focustech.tm.open.sdk.params;

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

	// 下载文件地址
	public static String downloadUrl = "http://webim.house365.com/tm/file/download?";
}
