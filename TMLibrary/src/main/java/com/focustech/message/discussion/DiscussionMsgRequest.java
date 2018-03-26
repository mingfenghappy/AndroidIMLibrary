package com.focustech.message.discussion;

import com.focustech.message.msg.MessageRequest;

/**********************************************************
 * @文件名称：DiscussionMsgRequest.java
 * @文件作者：huangshuo
 * @创建时间：2015-2-25 下午2:19:29
 * @文件描述：
 * @修改历史：2015-2-25创建初始版本
 **********************************************************/
public class DiscussionMsgRequest extends MessageRequest
{
	private String discussionId;

	public String getDiscussionId()
	{
		return discussionId;
	}

	public void setDiscussionId(String discussionId)
	{
		this.discussionId = discussionId;
	}
	

}
