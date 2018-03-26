package com.focustech.message.msg;

/**********************************************************
 * @文件名称：TMRequest.java
 * @文件作者：yangyanlong
 * @创建时间：2013-11-11 下午3:06:41
 * @文件描述：
 * @修改历史：2013-11-11创建初始版本
 **********************************************************/
public abstract class TMRequest extends AbstractTMMessage
{

	private long timestamp = System.currentTimeMillis();

	public long getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}


}
