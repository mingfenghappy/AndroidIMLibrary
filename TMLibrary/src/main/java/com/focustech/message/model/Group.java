package com.focustech.message.model;

import com.focustech.tm.open.sdk.messages.protobuf.Group.GroupInfoRsp;
import com.focustech.tm.open.sdk.messages.protobuf.Group.MyGroupInfoRsp;

/**
 * 群组信息
 */
public class Group {

	private String groupId = "0";
	private String groupName;
	private String groupNum;
	private String nickName;
	private String groupNote;
	private String groupTheme;
	private String groupBulletin;
	private String groupKeyword;
	// 群类型
	private int groupType;
	// 群验证设置
	private int validateRule;
	// 群消息设置
	private int messageSetting;
	// 创建人名称
	private String groupCreator;

	public String getGroupKeyword()
	{
		return groupKeyword;
	}

	public void setGroupKeyword(String groupKeyword)
	{
		this.groupKeyword = groupKeyword;
	}

	public int getValidateRule()
	{
		return validateRule;
	}

	public void setValidateRule(int validateRule)
	{
		this.validateRule = validateRule;
	}

	public int getGroupType()
	{
		return groupType;
	}

	public void setGroupType(int groupType)
	{
		this.groupType = groupType;
	}

	public String getGroupCreator()
	{
		return groupCreator;
	}

	public void setGroupCreator(String groupCreator)
	{
		this.groupCreator = groupCreator;
	}

	public String getGroupId()
	{
		return groupId;
	}

	public void setGroupId(String groupId)
	{
		this.groupId = groupId;
	}

	public String getGroupName()
	{
		return groupName;
	}

	public void setGroupName(String groupName)
	{
		this.groupName = groupName;
	}

	public String getGroupNum()
	{
		return groupNum;
	}

	public void setGroupNum(String groupNum)
	{
		this.groupNum = groupNum;
	}

	public String getNickName()
	{
		return nickName;
	}

	public void setNickName(String nickName)
	{
		this.nickName = nickName;
	}

	public String getGroupNote()
	{
		return groupNote;
	}

	public void setGroupNote(String groupNote)
	{
		this.groupNote = groupNote;
	}

	public String getGroupTheme()
	{
		return groupTheme;
	}

	public void setGroupTheme(String groupTheme)
	{
		this.groupTheme = groupTheme;
	}

	public String getGroupBulletin()
	{
		return groupBulletin;
	}

	public void setGroupBulletin(String groupBulletIn)
	{
		this.groupBulletin = groupBulletIn;
	}

	public int getMessageSetting()
	{
		return messageSetting;
	}

	public void setMessageSetting(int messageSetting)
	{
		this.messageSetting = messageSetting;
	}

	@Override
	public String toString()
	{
		return "Group [groupId=" + groupId + ", groupName=" + groupName + "]";
	}

	public static Group parse(MyGroupInfoRsp rsp)
	{
		GroupInfoRsp groupInfo = rsp.getGroupInfoRsp();
		Group group = new Group();
		group.setGroupId(groupInfo.getGroupId());
		group.setGroupName(groupInfo.getGroupName());
		group.setGroupNum(groupInfo.getGroupNo());
		group.setGroupNote(rsp.getGroupRemark());
		group.setGroupTheme(groupInfo.getGroupSignature());
		group.setGroupBulletin(groupInfo.getGroupDesc());
		group.setGroupKeyword(groupInfo.getGroupKeyword());
		group.setGroupType(groupInfo.getGroupType().getNumber());
		group.setValidateRule(groupInfo.getValidateRule().getNumber());
		return group;
	}
}
