package com.focustech.webtm.protocol.tm.message;

import android.content.Context;

import com.focustech.common.Utils;
import com.focustech.tm.open.sdk.messages.protobuf.Contacts;
import com.focustech.tm.open.sdk.messages.protobuf.Enums;
import com.focustech.tm.open.sdk.messages.protobuf.Enums.Enable;
import com.focustech.tm.open.sdk.messages.protobuf.Enums.Equipment;
import com.focustech.tm.open.sdk.messages.protobuf.Group;
import com.focustech.tm.open.sdk.messages.protobuf.Head;
import com.focustech.tm.open.sdk.messages.protobuf.Messages;
import com.focustech.tm.open.sdk.messages.protobuf.Messages.Message;
import com.focustech.tm.open.sdk.messages.protobuf.Session.LoginReq;
import com.focustech.tm.open.sdk.messages.protobuf.Session.MobileLogoutReq;
import com.focustech.tm.open.sdk.messages.protobuf.User;
import com.focustech.tm.open.sdk.net.base.NetConnector;
import com.focustech.tm.open.sdk.net.base.TMConnection;
import com.focustech.tm.open.sdk.net.base.TMMessageProcessorAdapter;
import com.focustech.params.FusionField;
import com.focustech.webtm.protocol.tm.message.group.GroupMsgRequest;
import com.focustech.webtm.protocol.tm.message.msg.MessageResponse;
import com.focustech.webtm.protocol.tm.message.msg.TMMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 消息发送工具类
 */
public class RequestClient {

	// TM连接器
	private NetConnector connector;

	Context context;

	private AtomicInteger mAtomicInteger = new AtomicInteger(1);

	public RequestClient(Context context) {
		this.context=context;
		initConnector();
	}

	/**
	 * 初始化参数
	 */
	public void initConnector() {
		List<String[]> servers = new ArrayList<>();
		servers.add(new String[] {FusionField.socketAddress, FusionField.socketPort});
		// 设置心跳包发送字节
		connector = new NetConnector(context);
	}

	/**
	 * 开始连接
	 * @return
	 */
	public boolean startConnector() {
		connector.setHandler(new TMMessageProcessorAdapter(context));
		// 配置链接服务器
		List<String[]> servers = new ArrayList<>();
		servers.add(new String[] {FusionField.socketAddress, FusionField.socketPort});
		for (String[] server : servers) {
			connector.addServer(server[0], Integer.parseInt(server[1]));
		}
		return connector.connect();
	}

	/**
	 * 关闭连接
	 */
	public void closeConnector() {
		if(null != connector)
			connector.close();
	}

	public int getCliSeqId()
	{
		return mAtomicInteger.get();
	}

	public int setCliSeqId() {
		return mAtomicInteger.addAndGet(1);
	}

	/**
	 * 判断连接状态
	 * @return
	 */
	public boolean isConnected() {
		return null != connector && connector.isConnected();
	}

	/**
	 * 发送心跳包
	 */
	public void heartbeat() {
		TMConnection.getInstance(context).send(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 });
	}

	public Head.TMHeadMessage getHead(String cmd) {
		Head.TMHeadMessage.Builder builder = Head.TMHeadMessage.newBuilder();
		builder.setCliSeqId(mAtomicInteger.get());
		builder.setCmd(cmd);
		builder.setDomainName(FusionField.messageHeaderDomain);
		builder.setVersion(FusionField.VERSION);
		mAtomicInteger.addAndGet(1);
		return builder.build();
	}

	private Head.TMHeadMessage getHead(String cmd, String serverMessageId) {
		Head.TMHeadMessage.Builder builder = Head.TMHeadMessage.newBuilder();
		builder.setCliSeqId(mAtomicInteger.get());
		mAtomicInteger.addAndGet(1);
		builder.setCmd(cmd);
		builder.setDomainName(FusionField.messageHeaderDomain);
		builder.setVersion(FusionField.VERSION);
		builder.setSvrSeqId(serverMessageId);
		return builder.build();
	}

	public Head.TMHeadMessage getHead(String cmd, int cliseqId) {
		Head.TMHeadMessage.Builder builder = Head.TMHeadMessage.newBuilder();
		builder.setDomainName(FusionField.messageHeaderDomain);
		builder.setCmd(cmd);
		builder.setVersion(FusionField.VERSION);
		builder.setCliSeqId(cliseqId);
		return builder.build();
	}

	/**
	 * 用户登录
	 * @param userName
	 * @param password
	 */
	public void reqLogin(String userName, String password) {
		TMMessage message = new TMMessage();
		message.setHead(getHead("LoginReq"));
		LoginReq.Builder bodyBuilder = LoginReq.newBuilder();
		bodyBuilder.setUserName(userName);
		bodyBuilder.setUserPassword(password);
		bodyBuilder.setEquipment(Equipment.MOBILE_ANDROID);
		message.setBody(bodyBuilder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	public void logout(String lastId) {
		TMMessage message = new TMMessage();
		message.setHead(getHead("MobileLogoutReq"));
		MobileLogoutReq.Builder builder = MobileLogoutReq.newBuilder();
		builder.setUserId(lastId);
		builder.setEquipment(Equipment.MOBILE_ANDROID);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 请求好友列表
	 */
	public void reqFriendGroups() {
		TMMessage message = new TMMessage();
		message.setHead(getHead("FriendGroupsReq"));
		Contacts.FriendGroupsReq.Builder builder = Contacts.FriendGroupsReq.newBuilder();
		builder.setTimestamp(0);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 获取全部好友信息
	 */
	public void reqFriends() {
		TMMessage message = new TMMessage();
		message.setHead(getHead("FriendsReq"));
		Contacts.FriendsReq.Builder builder = Contacts.FriendsReq.newBuilder();
		builder.setTimestamp(0);
		builder.setNeedEndRsp(Enable.ENABLE);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 获取全部离线消息
	 */
	public void reqGetOfflineMessage() {
		TMMessage message = new TMMessage();
		message.setHead(getHead("GetOfflineMessageReq", getCliSeqId()));
		setCliSeqId();
		Messages.GetOfflineMessageReq.Builder builder = Messages.GetOfflineMessageReq.newBuilder();
		builder.setTimestamp(System.currentTimeMillis());
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 发送离线消息回执
	 * 消息接收完毕之后要发送回执
	 */
	public void hasReadOfflineMessage(String svrSeqId) {
		MessageResponse resp = new MessageResponse();
		resp.setCmd("GetOfflineMessageRsp");
		resp.setServermsgid(svrSeqId);
		setMsgReply(resp);
	}

	/**
	 * 发送聊消息回执
	 * 消息接收完毕之后要发送回执
	 */
	public void hasReadMessage(String svrSeqId) {
		MessageResponse resp = new MessageResponse();
		resp.setCmd("Message");
		resp.setServermsgid(svrSeqId);
		setMsgReply(resp);
	}

	public void setMsgReply(MessageResponse resp) {
		TMMessage message = new TMMessage();
		message.setHead(getHead("ReceptNty", resp.getServermsgid()));
		Messages.ReceptNty.Builder builder = Messages.ReceptNty.newBuilder();
		builder.setCmd(resp.getCmd());
		builder.setEquipment(Equipment.MOBILE_ANDROID);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 获取用户设置信息
	 */
	public void getUserSettingsInfo() {
		TMMessage message = new TMMessage();
		message.setHead(getHead("UserSettingReq"));
		User.UserSettingReq.Builder builder = User.UserSettingReq.newBuilder();
		builder.setTimestamp(0);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 单聊发送文本
	 * @param toUserId 接收消息UserId
	 * @param msg 发送的文本
	 * @param userName 发送消息UserName
	 */
	public void sendTextMessage(String toUserId, String msg, String userName) {
		TMMessage message = new TMMessage();
		message.setHead(getHead("Message", getCliSeqId()));
		setCliSeqId();
		Message.Builder builder = Message.newBuilder();
		builder.setMsg(Utils.phraseSendText(msg));
		builder.setUserId(toUserId);
		builder.setTimestamp(System.currentTimeMillis());
		builder.setMsgMeta(Utils.createMetaData("0", Enums.MessageType.TEXT, userName, "", ""));
		builder.setMsgType(Enums.MessageType.TEXT);
		builder.setSync(Enable.ENABLE);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 单聊发送图片
	 * @param toUserId 接收消息UserId
	 * @param filePath 图片路径
	 * @param userName 发送消息UserName
	 * @param fileId 上传成功文件ID
	 */
	public void sendPicMessage(String toUserId, String filePath, String userName, String fileId) {
		TMMessage message = new TMMessage();
		message.setHead(getHead("Message", getCliSeqId()));
		setCliSeqId();
		Message.Builder builder = Message.newBuilder();
		builder.setMsg(Utils.phraseSendText("/:b0"));
		builder.setUserId(toUserId);
		builder.setTimestamp(System.currentTimeMillis());
		builder.setMsgMeta(Utils.createMetaData("8", Enums.MessageType.MULTI_MEDIA, userName, filePath, fileId));
		builder.setMsgType(Enums.MessageType.MULTI_MEDIA);
		builder.setSync(Enable.ENABLE);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 单聊发送语音
	 * @param toUserId 接收消息UserId
	 * @param filePath 语音路径
	 * @param userName 发送消息UserName
	 * @param fileId 上传成功文件ID
	 */
	public void sendVoiceMessage(String toUserId, String filePath, String userName, String fileId) {
		TMMessage message = new TMMessage();
		message.setHead(getHead("Message", getCliSeqId()));
		setCliSeqId();
		Message.Builder builder = Message.newBuilder();
		builder.setMsg(Utils.phraseSendText("[语音消息]"));
		builder.setUserId(toUserId);
		builder.setTimestamp(System.currentTimeMillis());
		builder.setMsgMeta(Utils.createMetaData("7", Enums.MessageType.MULTI_MEDIA, userName, filePath, fileId));
		builder.setMsgType(Enums.MessageType.MULTI_MEDIA);
		builder.setSync(Enable.ENABLE);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 获取群消息
	 */
	public void requestGroupList() {
		TMMessage message = new TMMessage();
		message.setHead(getHead("GroupsReq"));
		Group.GroupsReq.Builder builder = Group.GroupsReq.newBuilder();
		builder.setTimestamp(0);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 请求单个群成员资料
	 * @param groupId
	 * @param userId
	 */
	public void requestGroupSingleUserInfo(String groupId, String userId) {
		TMMessage message = new TMMessage();
		message.setHead(getHead("GroupSingleUserInfoReq"));
		Group.GroupSingleUserInfoReq.Builder builder = Group.GroupSingleUserInfoReq.newBuilder();
		builder.setGroupId(groupId);
		builder.setUserId(userId);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 修改群昵称
	 * @param newGroupNickName
	 * @param groupId
	 * @param userId
	 */
	public void updateGroupNickName(String newGroupNickName, String groupId, String userId) {
		TMMessage message = new TMMessage();
		message.setHead(getHead("UpdateGroupNickNameReq"));
		Group.UpdateGroupNickNameReq.Builder builder = Group.UpdateGroupNickNameReq.newBuilder();
		builder.setGroupId(groupId);
		builder.setUserId(userId);
		builder.setNewGroupNickName(newGroupNickName);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 修改群备注信息
	 * @param newGroupRemark
	 * @param groupId
	 */
	public void updateGroupRemark(String newGroupRemark, String groupId) {
		TMMessage message = new TMMessage();
		message.setHead(getHead("UpdateGroupRemarkReq"));
		Group.UpdateGroupRemarkReq.Builder builder = Group.UpdateGroupRemarkReq.newBuilder();
		builder.setGroupId(groupId);
		builder.setNewGroupRemark(newGroupRemark);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 修改群签名或群简介
	 * @param groupBulletin 群简介
	 * @param groupSignature 群签名
	 * @param groupId
	 * @param groupName
	 * @param groupKeyword
	 * @param groupType
	 * @param validateRule
	 */
	public void updateGroupInfo(String groupBulletin, String groupSignature, String groupId, String groupName, String groupKeyword, String groupType, String validateRule) {
		TMMessage message = new TMMessage();
		message.setHead(getHead("UpdateGroupInfoReq"));
		Group.UpdateGroupInfoReq.Builder builder = Group.UpdateGroupInfoReq.newBuilder();
		builder.setGroupId(groupId);
		builder.setGroupName(groupName);
		builder.setGroupKeyword(groupKeyword);
		builder.setGroupType(Enums.GroupType.valueOf(groupType));
		builder.setValidateRule(Enums.ValidateRule.valueOf(validateRule));
		builder.setGroupSignature(groupSignature);
		builder.setGroupDesc(groupBulletin);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 设置接收消息类型
	 * @param groupId
	 * @param messageSetting
	 */
	public void updateGroupMessageSetting(String groupId, Enums.MessageSetting messageSetting) {
		TMMessage message = new TMMessage();
		message.setHead(getHead("UpdateGroupUserSettingReq"));
		Group.UpdateGroupUserSettingReq.Builder builder = Group.UpdateGroupUserSettingReq.newBuilder();
		builder.setGroupId(groupId);
		builder.setMessageSetting(messageSetting);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 获取群组信息
	 * @param groupId
	 */
	public void requestGroupInfo(String groupId) {
		TMMessage message = new TMMessage();
		message.setHead(getHead("GroupInfoReq"));
		Group.GroupInfoReq.Builder builder = Group.GroupInfoReq.newBuilder();
		builder.setGroupId(groupId);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 获取群成员列表
	 * @param groupId
	 */
	public void requestGroupMember(String groupId) {
		TMMessage message = new TMMessage();
		message.setHead(getHead("GroupUserInfoReq"));
		Group.GroupUserInfoReq.Builder builder = Group.GroupUserInfoReq.newBuilder();
		builder.setGroupId(groupId);
		builder.setTimestamp(0);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 获取多个成员信息
	 * @param userIdList 成员ID集合
	 */
	public void requestGroupMemberInfo(List<String> userIdList) {
		TMMessage message = new TMMessage();
		message.setHead(getHead("UsersInfoReq"));
		User.UsersInfoReq.Builder builder = User.UsersInfoReq.newBuilder();
		builder.addAllTargetUserId(userIdList);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 删除群成员
	 */
	public void deleteGroupUser(String groupId, String mSelectedUserId) {
		TMMessage message = new TMMessage();
		message.setHead(getHead("DeleteGroupUserReq"));
		Group.DeleteGroupUserReq.Builder builder = Group.DeleteGroupUserReq.newBuilder();
		builder.setGroupId(groupId);
		builder.addUserIds(mSelectedUserId);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 向多个好友发起加群申请
	 * @param groupId
	 * @param invitedUserIds
	 */
	public void inviteUserJoinGroup(String groupId, List<String> invitedUserIds) {
		TMMessage message = new TMMessage();
		message.setHead(getHead("InviteUserJoinGroupReq"));
		Group.InviteUserJoinGroupReq.Builder builder = Group.InviteUserJoinGroupReq.newBuilder();
		builder.setGroupId(groupId);
		builder.addAllInvitedUserIds(invitedUserIds);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

    /**
     * 获取通知消息
     * @param timestamp
     */
    public void getSysNtyReq(long timestamp) {
        TMMessage message = new TMMessage();
        message.setHead(getHead("GetSysNtyReq"));
        Messages.GetSysNtyReq.Builder builder = Messages.GetSysNtyReq.newBuilder();
        builder.setTimestamp(timestamp);
        builder.setEquipment(Equipment.MOBILE_ANDROID);
        message.setBody(builder.build().toByteArray());
        TMConnection.getInstance(context).send(message);
    }

	/**
	 * 创建群组
	 * @param groupName
	 * @param signature
	 * @param keyword
	 * @param groupDesc
	 * @param groupType
	 * @param validateRule
	 */
    public void createGroup(String groupName, String signature, String keyword, String groupDesc, Enums.GroupType groupType, Enums.ValidateRule validateRule) {
		TMMessage message = new TMMessage();
		message.setHead(getHead("AddGroupReq"));
		Group.AddGroupReq.Builder builder=Group.AddGroupReq.newBuilder();
		builder.setGroupName(groupName);
		builder.setGroupSignature(signature);
		builder.setGroupKeyword(keyword);
		builder.setGroupDesc(groupDesc);
		builder.setGroupType(groupType);
		builder.setValidateRule(validateRule);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 获取群组加入规则
	 * @param groupId
	 */
	public void getGroupRule(String groupId) {
		TMMessage message = new TMMessage();
		message.setHead(getHead("GetGroupRuleReq"));
		Group.GetGroupRuleReq.Builder builder = Group.GetGroupRuleReq.newBuilder();
		builder.setGroupId(groupId);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 群聊发送语音
	 * @param fromUserId
	 * @param groupId
	 * @param filePath
	 * @param userName
	 * @param fileId
	 */
	public void sendGroupVoiceMessage(String fromUserId, String groupId, String filePath, String userName, String fileId) {
		GroupMsgRequest request = new GroupMsgRequest();
		request.setFromUserId(fromUserId);
		request.setGroupId(groupId);
		request.setToUserId(groupId);
		request.setMsg("[语音消息]");
		request.setMessagetype( "7" );
		request.setSrc( "GROUP" );
		request.setFilePath(filePath);
		request.setServeTime(System.currentTimeMillis() + "");
		request.setFileId(fileId);

		TMMessage message = new TMMessage();
		message.setHead(getHead("GroupMessage", getCliSeqId()));
		setCliSeqId();
		Messages.GroupMessage.Builder builder = Messages.GroupMessage.newBuilder();
		builder.setMsg(Utils.phraseSendText(request.getMsg()));
		builder.setUserId(request.getFromUserId());
		builder.setGroupId(request.getGroupId());
		builder.setTimestamp(Long.parseLong(request.getServeTime()));
		builder.setMsgMeta(Utils.createMetaData(request, Enums.MessageType.MULTI_MEDIA, userName));
		builder.setMsgType(Enums.MessageType.MULTI_MEDIA);
		builder.setSync(Enable.ENABLE);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 群聊发送图片
	 * @param fromUserId
	 * @param groupId
	 * @param filePath
	 * @param userName
	 * @param fileId
	 */
	public void sendGroupPicMessage(String fromUserId, String groupId, String filePath, String userName, String fileId) {
		GroupMsgRequest request = new GroupMsgRequest();
		request.setFromUserId(fromUserId);
		request.setGroupId(groupId);
		request.setToUserId(groupId);
		request.setMsg("/:b0");
		request.setSrc("GROUP");
		request.setMessagetype("8");
		request.setFilePath(filePath);
		request.setServeTime(""+System.currentTimeMillis());
		request.setFileId(fileId);

		TMMessage message = new TMMessage();
		message.setHead(getHead("GroupMessage", getCliSeqId()));
		setCliSeqId();
		Messages.GroupMessage.Builder builder = Messages.GroupMessage.newBuilder();
		builder.setMsg(Utils.phraseSendText(request.getMsg()));
		builder.setUserId(request.getFromUserId());
		builder.setGroupId(request.getGroupId());
		builder.setTimestamp(Long.parseLong(request.getServeTime()));
		builder.setMsgMeta(Utils.createMetaData(request, Enums.MessageType.MULTI_MEDIA, userName));
		builder.setMsgType(Enums.MessageType.MULTI_MEDIA);
		builder.setSync(Enable.ENABLE);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 获取添加好友权限
	 * @param context
	 * @param userId
	 */
	public void getFriendRuleReq(Context context, String userId) {
		TMMessage message = new TMMessage();
		message.setHead(getHead("GetFriendRuleReq"));
		Contacts.GetFriendRuleReq.Builder builder = Contacts.GetFriendRuleReq.newBuilder();
		builder.setUserId(userId);
		message.setBody( builder.build().toByteArray() );
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 添加好友
	 * @param context
	 * @param userId
	 * @param srcFriendGroupId
	 */
	public void addFriendReq(Context context, String userId, String ext, String srcFriendGroupId) {
		TMMessage message = new TMMessage();
		message.setHead(getHead("AddFriendReq"));
		Contacts.AddFriendReq.Builder builder = Contacts.AddFriendReq.newBuilder();
		builder.setTargetFriendUserId(userId);
		builder.setExt(ext);
		builder.setSrcFriendGroupId(srcFriendGroupId);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 删除好友
	 * @param context
	 * @param userId
	 */
	public void deleteFriendReq(Context context, String userId) {
		TMMessage message = new TMMessage();
		message.setHead(getHead("DeleteFriendReq"));
		Contacts.DeleteFriendReq.Builder builder = Contacts.DeleteFriendReq.newBuilder();
		builder.setFriendUserId(userId);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}

	/**
	 * 获取用户信息
	 * @param userId
	 */
	public void getUserInfo(Context context, String userId) {
		TMMessage message = new TMMessage();
		message.setHead(getHead("UserInfoReq"));
		User.UserInfoReq.Builder builder = User.UserInfoReq.newBuilder();
		builder.setTargetUserId(userId);
		message.setBody(builder.build().toByteArray());
		TMConnection.getInstance(context).send(message);
	}
}
