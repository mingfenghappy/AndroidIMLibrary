package com.focustech.tm.open.sdk.params;

/**
 * 基本配置参数
 */
public class FusionField {

	// 用于通信服务器请求Head中
	public static String VERSION = "1.00.01";
	// 通信服务器地址
	public static String socketAddress = "imtcp.house365.com";
	// 消息头部域名，必须和服务器地址一致
	public static String messageHeaderDomain = "FBS365"; //房博士domain
	// 通信服务器端口
	public static String socketPort = "443";

	public static String FILEKEY = "file";
	// 读取超时
	public static int readTimeOut = 30 * 1000;
	// 连接超时
	public static int connectTimeout = 30 * 1000;
	// 设置编码
	public static String CHARSET = "utf-8";
	// 下载文件地址
	public static String downloadUrl = "http://webim.house365.com/tm/file/download?";
	// 上传图片URL
	public static String uploadVoiceURL = "http://webim.house365.com/tm/file/upload?type=voice&token=qEWY3kh6WZ6NTVdTX5s4rhTh-lF6JwaJzXyaeiF7qYOKa7vCCHMccqEfjjHFF6-gPaXxYPrgUjkmzNwUygwGl3ORAqWmqemBbnP_cGTY1ZQeLjkm6GS0KjGlY3hzbS0o";
	// 上传图片URL
	public static String uploadPicURL = "http://webim.house365.com/tm/file/upload?type=picture&token=qEWY3kh6WZ6NTVdTX5s4rhTh-lF6JwaJzXyaeiF7qYOKa7vCCHMccqEfjjHFF6-gPaXxYPrgUjkmzNwUygwGl3ORAqWmqemBbnP_cGTY1ZQeLjkm6GS0KjGlY3hzbS0o";
	// 下载语音URL
	public static String downloadVoiceURL = "http://webim.house365.com/tm/file/download?fileid=596074cde4b05ab1984db0c9&type=voice&token=qEWY3kh6WZ6NTVdTX5s4rhTh-lF6JwaJzXyaeiF7qYOKa7vCCHMccqEfjjHFF6-gPaXxYPrgUjkmzNwUygwGl3ORAqWmqemBbnP_cGTY1ZQeLjkm6GS0KjGlY3hzbS0o";
	// 下载图片URL
	public static String downloadPicUrl = "http://webim.house365.com/tm/file/download?fileid=597567bee4b095d682c58b80&type=picture&token=qEWY3kh6WZ6NTVdTX5s4rhTh-lF6JwaJzXyaeiF7qYOKa7vCCHMccqEfjjHFF6-gPaXxYPrgUjkmzNwUygwGl3ORAqWmqemBbnP_cGTY1ZQeLjkm6GS0KjGlY3hzbS0o";
}
