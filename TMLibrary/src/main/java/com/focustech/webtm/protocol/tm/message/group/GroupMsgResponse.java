package com.focustech.webtm.protocol.tm.message.group;

import com.focustech.webtm.protocol.tm.message.msg.TMResponse;

public class GroupMsgResponse extends TMResponse
{
	public static final int FLAG_ALREADY_READ = 1;
	public static final int FLAG_NOT_READ = 0;

	private String groupId;
	private String msg;
	private String fontFace;
	private String fontSize;
	private String fontColor;
	private String fontFlag;
	private String msgid;
	private String servermsgid;
	private String serveTime;
	private String parameters;
	private String position = "-1";

	private String username = "";
	
	private boolean isOfflineMessage;
	
	private boolean isSyncMessage;
	
	private boolean isResendMessage;
	
	/**
	 * 0:普通消息   1:自动回复消息   3:语音消息   5:文件信息   6:离线文件信息   7：语音文件消息  8:图片消息 9:同步消息 10:加群消息
	 */
	private String messagetype;
	
	private String picNum = "0";

	private int readFlag = -1;
	
	private boolean isShield = false;

	private String localfilename;
	
	public int getReadFlag()
	{
		return readFlag;
	}

	public void setReadFlag(int readFlag)
	{
		this.readFlag = readFlag;
	}

	public String getGroupId()
	{
		return groupId;
	}

	public void setGroupid(String groupId)
	{
		this.groupId = groupId;
	}

	public String getMsgid()
	{
		return msgid;
	}

	public void setMsgid(String msgid)
	{
		this.msgid = msgid;
	}

	public String getParameters()
	{
		return parameters;
	}

	public void setParameters(String parameters)
	{
		this.parameters = parameters;
	}

	public String getMsg()
	{
		return msg;
	}

	public void setMsg(String msg)
	{
		this.msg = msg;
	}

	public String getFontFace()
	{
		return fontFace;
	}

	public void setFontFace(String fontFace)
	{
		this.fontFace = fontFace;
	}

	public String getFontSize()
	{
		return fontSize;
	}

	public void setFontSize(String fontSize)
	{
		this.fontSize = fontSize;
	}

	public String getFontColor()
	{
		return fontColor;
	}

	public void setFontColor(String fontColor)
	{
		this.fontColor = fontColor;
	}

	public String getFontFlag()
	{
		return fontFlag;
	}

	public void setFontFlag(String fontFlag)
	{
		this.fontFlag = fontFlag;
	}

	public String getServeTime()
	{
		return serveTime;
	}

	public void setServeTime(String serveTime)
	{
		this.serveTime = serveTime;
	}

	public String getServermsgid()
	{
		return servermsgid;
	}

	public void setServermsgid(String servermsgid)
	{
		this.servermsgid = servermsgid;
	}

	public String getPosition()
	{
		return position;
	}

	public void setPosition(String position)
	{
		this.position = position;
	}

	public String getPicNum()
	{
		return picNum;
	}

	public void setPicNum(String picNum)
	{
		this.picNum = picNum;
	}

	public void setGroupId(String groupId)
	{
		this.groupId = groupId;
	}

	public boolean isShield()
	{
		return isShield;
	}

	public void setShield(boolean isShield)
	{
		this.isShield = isShield;
	}

	public boolean isOfflineMessage()
	{
		return isOfflineMessage;
	}

	public void setOfflineMessage(boolean isOfflineMessage)
	{
		this.isOfflineMessage = isOfflineMessage;
	}

	public boolean isSyncMessage()
	{
		return isSyncMessage;
	}

	public void setSyncMessage(boolean isSyncMessage)
	{
		this.isSyncMessage = isSyncMessage;
	}

	public boolean isResendMessage()
	{
		return isResendMessage;
	}

	public void setResendMessage(boolean isResendMessage)
	{
		this.isResendMessage = isResendMessage;
	}

	public String getMessagetype()
	{
		return messagetype;
	}

	public void setMessagetype(String messagetype)
	{
		this.messagetype = messagetype;
	}

	public String getLocalfilename()
	{
		return localfilename;
	}

	public void setLocalfilename(String localfilename)
	{
		this.localfilename = localfilename;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}
	
}
