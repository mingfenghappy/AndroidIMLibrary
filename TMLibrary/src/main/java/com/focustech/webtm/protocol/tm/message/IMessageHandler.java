package com.focustech.webtm.protocol.tm.message;

import com.focustech.tm.open.sdk.net.impl.Cmd;
import com.focustech.webtm.protocol.tm.message.msg.TMMessage;

public interface IMessageHandler {

	@Cmd("HeartBeatRsp")
	void onHeartBeatRsp();

	@Cmd("LoginRsp")
	void onLoginRsp(TMMessage message);

	@Cmd("UserInfoRsp")
	void onUserInfoRsp(TMMessage message);

	@Cmd("UserOptionsRsp")
	void onUserOptionsRsp(TMMessage message);

	@Cmd("FriendGroupsRsp")
	void onFriendGroupsRsp(TMMessage message);

	@Cmd("FriendInfoRsp")
	void onFriendInfoRsp(TMMessage message);

	@Cmd("FriendInfoEndRsp")
	void onFriendInfoEndRsp(TMMessage message);

	@Cmd(value = "UserSettingRsp")
	void onUserSettingRsp(TMMessage message);

	@Cmd(value = "KickoutNty")
	void onKickoutNty(TMMessage message);

	@Cmd(value = "NewSysNty")
	void onNewSysNty(TMMessage message);

	@Cmd(value = "Message")
	void onMessage(TMMessage message);

	@Cmd(value = "UpdateUserStatusNty")
	void onUpdateUserStatusNty(TMMessage message);

	@Cmd(value = "GroupMessage")
	void onGroupMessage(TMMessage message);

	@Cmd(value = "GetGroupOfflineMessageRsp")
	void onGetGroupOfflineMessageRsp(TMMessage message);

	@Cmd(value = "ReceptNty")
	void onReceptNty(TMMessage message);

	@Cmd(value = "UserSignatureNty")
	void onUserSignatureNty(TMMessage message);

	@Cmd(value = "GetOfflineMessageRsp")
	void onGetOfflineMessageRsp(TMMessage message);

	@Cmd(value = "GroupsRsp")
	void onGroupsRsp(TMMessage message);

	@Cmd(value = "GroupsInfoRsp")
	void onGroupsInfoRsp(TMMessage message);

	@Cmd(value = "GroupUserSettingRsp")
	void onGroupUserSettingRsp(TMMessage message);

	@Cmd(value = "GroupUserInfoRsp")
	void onGroupUserInfoRsp(TMMessage message);

	@Cmd(value = "UpdateGroupUserSettingRsp")
	void onUpdateGroupUserSettingRsp(TMMessage message);

	@Cmd(value = "GroupInfoRsp")
	void onGroupInfoRsp(TMMessage message);

    @Cmd(value = "GroupUserInfosRsp")
    void onGroupUserInfosRsp(TMMessage message);

    @Cmd(value = "UsersInfoRsp")
    void onUsersInfoRsp(TMMessage message);

	@Cmd(value = "AgreeInviteUserJoinGroupSucceededSysNty")
	void onAgreeInviteUserJoinGroupSucceededSysNty(TMMessage message);

	@Cmd(value = "MyGroupInfoRsp")
	void onMyGroupInfoRsp(TMMessage message);

	@Cmd(value = "DeleteGroupUserRsp")
	void onDeleteGroupUserRsp(TMMessage message);

	@Cmd(value = "UpdateGroupNickNameRsp")
	void onUpdateGroupNickNameRsp(TMMessage message);

	@Cmd(value = "UpdateGroupRemarkRsp")
	void onUpdateGroupRemarkRsp(TMMessage message);

	@Cmd(value = "AddedFriendSucceededSysNty")
	void onAddedFriendSucceededSysNty(TMMessage message);

	@Cmd(value = "AddFriendWithoutValidateSucceededSysNty")
	void onAddFriendWithoutValidateSucceededSysNty(TMMessage message);

	@Cmd(value = "FriendInfoNty")
	void onFriendInfoNty(TMMessage message);

	@Cmd(value = "GetFriendRuleRsp")
	void onGetFriendRuleRsp(TMMessage message);

	@Cmd(value = "DeleteFriendRsp")
	void onDeleteFriendRsp(TMMessage message);

}
