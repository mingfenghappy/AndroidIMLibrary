package com.focustech.webtm.protocol.tm.message;

import android.content.Context;
import android.text.TextUtils;

import com.focustech.webtm.protocol.tm.message.msg.TMMessage;
import com.focustech.webtm.protocol.tm.message.model.BroadcastBean;
import com.focustech.webtm.protocol.tm.message.model.FriendGroupRsp;
import com.focustech.webtm.protocol.tm.message.model.FriendInfoRsp;
import com.focustech.webtm.protocol.tm.message.model.GetFriendRuleRsp;
import com.focustech.webtm.protocol.tm.message.model.LoginRsp;
import com.focustech.webtm.protocol.tm.message.model.MessageBean;
import com.focustech.webtm.protocol.tm.message.model.ReceptNty;
import com.focustech.webtm.protocol.tm.message.model.SystemMessageBean;
import com.focustech.webtm.protocol.tm.message.model.UpdateUserStatusNty;
import com.focustech.webtm.protocol.tm.message.model.UserInfoRsp;
import com.focustech.webtm.protocol.tm.message.params.SystemMsgType;
import com.focustech.tm.open.sdk.messages.protobuf.Contacts;
import com.focustech.tm.open.sdk.messages.protobuf.Enums;
import com.focustech.tm.open.sdk.messages.protobuf.Group;
import com.focustech.tm.open.sdk.messages.protobuf.Messages;
import com.focustech.tm.open.sdk.messages.protobuf.Session;
import com.focustech.tm.open.sdk.messages.protobuf.User;
import com.focustech.webtm.protocol.tm.message.group.GroupMsgResponse;
import com.focustech.webtm.protocol.tm.message.msg.MessageResponse;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息返回处理类，先保存缓存数据，发送广播的同时按需要持久化数据
 */
public class MTMessageHandlerAdapter implements IMessageHandler {

	Context context;

	public MTMessageHandlerAdapter(Context context) {
		this.context=context;
	}

	@Override
	public void onHeartBeatRsp() {
		BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.HeartBeat, "");
	}

	/**
	 * 登录成功回调
	 * @param message
	 */
	@Override
	public void onLoginRsp(TMMessage message) {
		try {
			Session.LoginRsp rsp = Session.LoginRsp.parseFrom(message.getBody());
			LoginRsp loginRsp=LoginRsp.parse(rsp);
			if (rsp.getCode()==0 && !TextUtils.isEmpty(loginRsp.getUserId()) ) {
				BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.LoginRsp, "");
			}
			else {
				BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.LoginRspERROR, "");
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 登录获取用户信息
	 * @param message
	 */
	@Override
	public void onUserInfoRsp(TMMessage message) {
		try {
			User.UserInfoRsp rsp = User.UserInfoRsp.parseFrom(message.getBody());
			UserInfoRsp userInfoRsp = UserInfoRsp.parse(rsp);
			BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.UserInfoRsp, userInfoRsp);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 用户限制回调，如好友上限、建群上限、加群上限
	 * @param message
	 */
	@Override
	public void onUserOptionsRsp(TMMessage message) {

	}

	/**
	 * 执行动作反馈
	 * @param message
	 */
	@Override
	public void onReceptNty(TMMessage message) {
		try {
			Messages.ReceptNty nty=Messages.ReceptNty.parseFrom(message.getBody());
			ReceptNty receptNty=ReceptNty.parse(nty);
			BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.ReceptNty, receptNty);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取好友列表
	 * @param message
	 */
	@Override
	public void onFriendGroupsRsp(TMMessage message) {
		try {
			Contacts.FriendGroupsRsp rsp = Contacts.FriendGroupsRsp.parseFrom(message.getBody());
			List<Contacts.FriendGroupRsp> friendGroupList = rsp.getFriendGroupsList();
			if (friendGroupList == null || friendGroupList.size() == 0) {
				return;
			}
			ArrayList<FriendGroupRsp> rsps=new ArrayList<>();
			for (Contacts.FriendGroupRsp friendGroup : friendGroupList) {
				// 过滤黑名单和陌生人
				if (friendGroup.getFriendGroupType() == Enums.FriendGroupType.BLACKLIST
						|| friendGroup.getFriendGroupType() == Enums.FriendGroupType.STRANGER) {
					continue;
				}
				FriendGroupRsp friendGroupRsp=FriendGroupRsp.parse(friendGroup);
				rsps.add(friendGroupRsp);
			}
			BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.FriendGroupsRsp, rsps);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取好友详情
	 * @param message
	 */
	@Override
	public void onFriendInfoRsp(TMMessage message) {
		try {
			Contacts.FriendInfoRsp rsp = Contacts.FriendInfoRsp.parseFrom(message.getBody());
			FriendInfoRsp friendInfoRsp = FriendInfoRsp.parse(rsp);
			BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.FriendInfoRsp, friendInfoRsp);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 好友信息获取结束标志
	 * @param message
	 */
	@Override
	public void onFriendInfoEndRsp(TMMessage message) {
		try {
			Contacts.FriendInfoEndRsp rsp = Contacts.FriendInfoEndRsp.parseFrom(message.getBody());
			// 最后一次加载好友列表时间
			long timestamp = rsp.getTimestamp();
			BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.FriendInfoEndRsp, "");
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 离线消息
	 * @param message
	 */
	@Override
	public void onGetOfflineMessageRsp(TMMessage message) {
		try {
			Messages.GetOfflineMessageRsp rsp = Messages.GetOfflineMessageRsp.parseFrom(message.getBody());
			List<Messages.Message> orgnList = rsp.getMessageList();
			ArrayList<TMMessage> list = new ArrayList<>();
			for (Messages.Message m : orgnList) {
				TMMessage newTMMessage = new TMMessage();
				newTMMessage.setHead(message.getHead());
				newTMMessage.setBody(m.toByteArray());
				list.add(newTMMessage);
			}
			ArrayList<MessageBean> offlineMessages=new ArrayList<>();
			for (TMMessage tmMessage : list) {
				MessageBean messageBean=MessageBean.parse(Messages.Message.parseFrom(tmMessage.getBody()), tmMessage.getHead());
				offlineMessages.add(messageBean);
			}
			BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.GetOfflineMessageRsp, offlineMessages);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUserSettingRsp(TMMessage message) {
		try {
			User.UserSettingRsp rsp = User.UserSettingRsp.parseFrom(message.getBody());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onKickoutNty(TMMessage message) {

	}

	/**
	 * 系统消息
	 * @param message
	 */
	@Override
	public void onNewSysNty(TMMessage message) {
		try {
			Messages.NewSysNty nty = Messages.NewSysNty.parseFrom(message.getBody());
			// 循环获取系统消息
			BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.NewSysNty, nty.getTimestamp());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 单聊接收消息
	 * @param message
	 */
	@Override
	public void onMessage(TMMessage message) {
		try {
			MessageBean messageBean=MessageBean.parse(Messages.Message.parseFrom(message.getBody()), message.getHead());
			BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.Message, messageBean);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 推送过来好友更新后的状态
	 */
	@Override
	public void onUpdateUserStatusNty(TMMessage message) {
		try {
			User.UpdateUserStatusNty rsp = User.UpdateUserStatusNty.parseFrom(message.getBody());
            UpdateUserStatusNty nty=UpdateUserStatusNty.parse(rsp);
            BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.UpdateUserStatusNty, nty);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGroupMessage(TMMessage message) {
        onGetGroupMessage(message, false, false);
	}

	@Override
	public void onGetGroupOfflineMessageRsp(TMMessage message) {

	}

	@Override
	public void onUserSignatureNty(TMMessage message) {
		try {
			User.UserSignatureNty nty = User.UserSignatureNty.parseFrom(message.getBody());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	private void onGetMessage(TMMessage message, boolean isOfflineMsg, boolean isSyncMsg) {
		// 新报文 通过得到的TMMessage内容适配成MessageResponse的内容以便兼容
		MessageResponse resp = MessageCreateFactory.createMessage(message, isOfflineMsg, isSyncMsg);
	}

    private void onGetGroupMessage(TMMessage message, boolean isOfflineMsg, boolean isSyncMsg) {
        // 新报文 通过得到的TMMessage内容适配成MessageResponse的内容以便兼容
        GroupMsgResponse resp = MessageCreateFactory.createGroupMessage(message, isOfflineMsg, isSyncMsg);
    }

	/**
	 * 群列表，包含groupId(群id)和FriendStatusRsp列表，
	 * FriendStatusRsp中包含friendUserId(好友id)和EquipmentStatus列表(多端登录情况，设备和状态可能有多个)
	 */
	@Override
	public void onGroupsRsp(TMMessage message) {
		try {
			Group.GroupsRsp rsp = Group.GroupsRsp.parseFrom(message.getBody());
			List<Group.GroupRsp> groupList = rsp.getGroupsList();
			for (Group.GroupRsp groupRsp : groupList) {

			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 包含GroupInfoRsp(群详细信息)和groupRemark(群备注)
	 * @param message
	 */
	@Override
	public void onGroupsInfoRsp(TMMessage message) {
		try {
			Group.GroupsInfoRsp rsp = Group.GroupsInfoRsp.parseFrom(message.getBody());
			List<Group.MyGroupInfoRsp> myGroupInfoRspList = rsp.getMyGroupInfoRspList();
			for (Group.MyGroupInfoRsp myGroupInfoRsp : myGroupInfoRspList) {
				com.focustech.webtm.protocol.tm.message.model.Group.parse(myGroupInfoRsp);
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 包含MessageSetting(群消息接收设置)
	 * @param message
	 */
	@Override
	public void onGroupUserSettingRsp(TMMessage message) {
		try {
			Group.GroupUserSettingRsp rsp = Group.GroupUserSettingRsp.parseFrom(message.getBody());
			List<Group.UpdateGroupUserSettingRsp> updateGroupUserSettingRspList = rsp.getUpdateGroupUserSettingRspList();
			for (Group.UpdateGroupUserSettingRsp updateGroupUserSettingRsp : updateGroupUserSettingRspList) {

			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取单一群组信息
	 * @param message
	 */
	@Override
	public void onGroupUserInfoRsp(TMMessage message) {
		try {
			Group.GroupUserInfoRsp rsp = Group.GroupUserInfoRsp.parseFrom(message.getBody());
			Enums.UserType userType = rsp.getUserType();
			if (userType == Enums.UserType.OWNER) {
				// 群主
			}
			else if (userType == Enums.UserType.ADMIN) {
				// 管理员
			}
			else if (userType == Enums.UserType.NORMAL) {
				// 普通成员
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 邀请好友加群成功后收到的系统消息
	 * @param message
	 */
	@Override
	public void onAgreeInviteUserJoinGroupSucceededSysNty(TMMessage message) {
		try {
			Group.AgreeInviteUserJoinGroupSucceededSysNty nty = Group.AgreeInviteUserJoinGroupSucceededSysNty.parseFrom(message.getBody());
			String groupId = nty.getGroupId();
			String groupName = nty.getGroupName();
			String userId = nty.getInvitedUserIds(0);
			if (TextUtils.isEmpty(userId)) {
				return;
			}
			long timestamp = nty.getTimestamp();
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 群信息修改
	 * @param message
	 */
	@Override
	public void onMyGroupInfoRsp(TMMessage message) {
		try {
			Group.MyGroupInfoRsp rsp = Group.MyGroupInfoRsp.parseFrom(message.getBody());
			Enums.Enable groupEnable = rsp.getGroupInfoRsp().getGroupEnable();
			if (groupEnable == Enums.Enable.DISABLE) {
				return;
			}
			com.focustech.webtm.protocol.tm.message.model.Group group = com.focustech.webtm.protocol.tm.message.model.Group.parse(rsp);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 群成员列表
	 * @param message
	 */
	@Override
	public void onGroupUserInfosRsp(TMMessage message) {
		try {
			Group.GroupUserInfosRsp rsp = Group.GroupUserInfosRsp.parseFrom(message.getBody());
			List<Group.GroupUserInfoRsp> groupUserInfoList = rsp.getGroupUserInfosList();
			if (groupUserInfoList != null && groupUserInfoList.size() == 0) {
				for (Group.GroupUserInfoRsp groupUserInfo : groupUserInfoList) {

				}
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

    /**
     * 获取多个成员信息
     * @param message
     */
    @Override
    public void onUsersInfoRsp(TMMessage message) {
        try {
            User.UsersInfoRsp rsp = User.UsersInfoRsp.parseFrom(message.getBody());
            List<User.UserInfoRsp> UserInfoList = rsp.getUserInfoRspList();
            for (User.UserInfoRsp userInfo : UserInfoList) {

            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

	/**
	 * 删除群成员
	 * @param message
	 */
	@Override
	public void onDeleteGroupUserRsp(TMMessage message) {
		try {
			Group.DeleteGroupUserRsp rsp = Group.DeleteGroupUserRsp.parseFrom(message.getBody());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 修改群昵称
	 * @param message
	 */
	@Override
	public void onUpdateGroupNickNameRsp(TMMessage message) {
		try {
			Group.UpdateGroupNickNameRsp rsp = Group.UpdateGroupNickNameRsp.parseFrom(message.getBody());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 修改群备注
	 * @param message
	 */
	@Override
	public void onUpdateGroupRemarkRsp(TMMessage message) {
		try {
			Group.UpdateGroupRemarkRsp rsp = Group.UpdateGroupRemarkRsp.parseFrom(message.getBody());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 对方被添加为好友的成功通知
	 * @param message
	 */
	@Override
	public void onAddedFriendSucceededSysNty(TMMessage message) {
		try {
			Contacts.AddedFriendSucceededSysNty nty = Contacts.AddedFriendSucceededSysNty.parseFrom(message.getBody());
			UserInfoRsp userInfoRsp=new UserInfoRsp();
			userInfoRsp.setUserId(nty.getSrcFriendUserId());
			userInfoRsp.setUserName(nty.getSrcFriendUserName());
			BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.AddFriendWithoutValidateSucceededSysNty, userInfoRsp);

			// 手动建造添加好友成功通知消息
			SystemMessageBean bean=new SystemMessageBean();
			bean.setExt("");
			bean.setSrc("IM");
			bean.setSrcFriendUserId(nty.getSrcFriendUserId());
			bean.setSrcFriendUserName(nty.getSrcFriendUserName());
			bean.setTimestamp(""+nty.getTimestamp());
			bean.setType(SystemMsgType.ADD_FRIEND_SUCC);
			BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.SystemMessageBean, bean);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取添加好友权限
	 * @param message
	 */
	@Override
	public void onGetFriendRuleRsp(TMMessage message) {
		try {
			Contacts.GetFriendRuleRsp rsp = Contacts.GetFriendRuleRsp.parseFrom(message.getBody());
			GetFriendRuleRsp getFriendRuleRsp=GetFriendRuleRsp.parse(rsp);
			BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.GetFriendRuleRsp, getFriendRuleRsp);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除好友
	 * @param message
	 */
	@Override
	public void onDeleteFriendRsp(TMMessage message) {
		try {
			Contacts.DeleteFriendRsp rsp= Contacts.DeleteFriendRsp.parseFrom(message.getBody());
			UserInfoRsp userInfoRsp=new UserInfoRsp();
			userInfoRsp.setUserId(rsp.getFriendUserId());
			BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.DeleteFriendRsp, userInfoRsp);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 对方不需要验证，添加对方为好友
	 * @param message
	 */
	@Override
	public void onAddFriendWithoutValidateSucceededSysNty(TMMessage message) {
		try {
			Contacts.AddFriendWithoutValidateSucceededSysNty nty= Contacts.AddFriendWithoutValidateSucceededSysNty.parseFrom(message.getBody());
			UserInfoRsp userInfoRsp=new UserInfoRsp();
			userInfoRsp.setUserId(nty.getTargetFriendUserId());
			userInfoRsp.setUserName(nty.getTargetFriendUserName());
			BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.AddFriendWithoutValidateSucceededSysNty, userInfoRsp);

			// 手动建造添加好友成功通知消息
			SystemMessageBean bean=new SystemMessageBean();
			bean.setExt("");
			bean.setSrc("IM");
			bean.setSrcFriendUserId(nty.getTargetFriendUserId());
			bean.setSrcFriendUserName(nty.getTargetFriendUserName());
			bean.setTimestamp(""+nty.getTimestamp());
			bean.setType(SystemMsgType.ADD_FRIEND_SUCC_RECEVER);
			BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.SystemMessageBean, bean);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 好友信息通知
	 * @param message
	 */
	@Override
	public void onFriendInfoNty(TMMessage message) {
		BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.FriendInfoNty, "");
	}

	/**
	 * 接收群消息类型
	 * @param message
	 */
	@Override
	public void onUpdateGroupUserSettingRsp(TMMessage message) {
		try {
			Group.UpdateGroupUserSettingRsp rsp = Group.UpdateGroupUserSettingRsp.parseFrom(message.getBody());
			Enums.MessageSetting messageSetting = rsp.getMessageSetting();
			if (messageSetting == Enums.MessageSetting.ACCEPT_AND_PROMPT) {

			}
			else if (messageSetting == Enums.MessageSetting.ACCEPT_NO_PROMPT) {

			}
			else if (messageSetting == Enums.MessageSetting.REFUSE_MESSAGE) {

			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGroupInfoRsp(TMMessage message) {
		try {
			Group.GroupInfoRsp rsp = Group.GroupInfoRsp.parseFrom(message.getBody());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}
}
