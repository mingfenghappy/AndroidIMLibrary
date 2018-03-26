package com.focustech.message.msg;

/**********************************************************
 * @文件名称：AbstractTMMessage.java
 * @文件作者：yangyanlong
 * @创建时间：2013-11-11 下午3:07:53
 * @文件描述：
 * @修改历史：2013-11-11创建初始版本
 **********************************************************/
public abstract class AbstractTMMessage
{
	protected String cmd;

	protected String src;

	protected String fromUserId = "0";

	protected String toUserId = "0";

	public String getFromUserId()
	{
		return fromUserId;
	}

	public String getSrc()
	{
		return src;
	}

	public void setSrc(String src)
	{
		this.src = src;
	}

	public void setFromUserId(String fromUserId)
	{
		this.fromUserId = fromUserId;
	}

	public String getToUserId() {
		return toUserId;
	}

	public void setToUserId(String toUserId)
	{
		this.toUserId = toUserId;
	}

	public String getCmd()
	{
		return cmd;
	}

	public void setCmd(String cmd)
	{
		this.cmd = cmd;
	}
}
