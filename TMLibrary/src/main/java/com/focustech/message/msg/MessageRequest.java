package com.focustech.message.msg;

/**********************************************************
 * @文件名称：MessageRequest.java
 * @文件作者：yangyanlong
 * @创建时间：2013-11-11 下午3:02:34
 * @文件描述：
 * @修改历史：2013-11-11创建初始版本
 **********************************************************/
public class MessageRequest extends TMRequest
{
	private String msg;
	private String fontFace;
	private String fontSize = "10";
	private String fontColor;
	private String fontFlag;
	private String parameters;
	private int msgid = 0;
	private String messagetype = "0";
	private int notsync;
	private String to;
	private String serveTime;
	
	private String filePath = "";

	private String fileId = "";
	
	public String getFilePath()
	{
		return filePath;
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
	}

	/**
	 * 原来的parameters是piccount=0|picname=|filesize=0形式
	 * 现在将msgMeta中的以字符串显示的json数据写入其中
	 */
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

	public int getMsgid()
	{
		return msgid;
	}

	public void setMsgid(int msgid)
	{
		this.msgid = msgid;
	}

	public String getMessagetype()
	{
		return messagetype;
	}

	public void setMessagetype(String messagetype)
	{
		this.messagetype = messagetype;
	}

	public int getNotsync()
	{
		return notsync;
	}

	public void setNotsync(int notsync)
	{
		this.notsync = notsync;
	}

	public String getTo()
	{
		return to;
	}

	public void setTo(String to)
	{
		this.to = to;
	}

	public String getServeTime()
	{
		return serveTime;
	}

	public void setServeTime(String serveTime)
	{
		this.serveTime = serveTime;
	}

	public String getFileId()
	{
		return fileId;
	}

	public void setFileId(String fileId)
	{
		this.fileId = fileId;
	}
	
}
