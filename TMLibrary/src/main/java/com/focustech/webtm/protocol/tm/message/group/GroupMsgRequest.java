package com.focustech.webtm.protocol.tm.message.group;

import com.focustech.webtm.protocol.tm.message.msg.MessageRequest;

/**********************************************************
 * @文件名称：GroupMsgRequest.java
 * @文件作者：yangyanlong
 * @创建时间：2013-11-11 下午3:04:40
 * @文件描述：
 * @修改历史：2013-11-11创建初始版本
 **********************************************************/
public class GroupMsgRequest extends MessageRequest
{
	private String groupId;

	public String getGroupId()
	{
		return groupId;
	}

	public void setGroupId(String groupId)
	{
		this.groupId = groupId;
	}
}
