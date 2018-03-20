package com.focustech.webtm.protocol.tm.message.params;

public interface SystemMsgType {
	/**
	 * 离线文件
	 */
	int OFFLINE_FILE_NOTIFY = -2;
	/**
	 * 添加好友请求：系统->被添加人
	 */
	int ADD_FRIEND_REQ = 1;
	/**
	 * 同意添加好友请求:系统->添加人
	 */
	int ADD_FRIEND_AGREE_REQ = 2;

	/**
	 * 拒绝添加好友请求:系统->添加人
	 */
	int ADD_FRIEND_REFUSE_REQ = 3;

	/**
	 * 添加好友成功通知:系统->被添加人
	 * 我同意别人添加我为好友，我不需要验证
	 */
	int ADD_FRIEND_SUCC = 5;

	/**
	 * 添加好友成功通知：系统->添加人
	 * 对方不需要验证，添加对方为好友
	 */
	int ADD_FRIEND_SUCC_RECEVER = 6;

	/**
	 * 同意并添加为好友
	 */
	int AGREE_AND_ACCEPT = 7;

	/**
	 * 拒绝离线文件
	 */
	int REJECT_OFFLINE_FILE = 8;

	/**
	 * 对方已经成功接收了离线文件
	 */
	int OFFLINE_FILE_RECEIVE_COMPLETE = 9;

	/**
	 * 申请加入群请求：系统->群管理员
	 */
	int ADD_GROUP_REQ = 10;

	/**
	 * 同意加入群通知：系统->发起人
	 */
	int ADD_GROUP_AGREE_REQ = 11;

	/**
	 * 拒绝加入群通知：系统->发起人
	 */
	int ADD_GROUP_REFUSE_REQ = 12;

	/**
	 * 邀请加入群通知:系统->被邀请人
	 */
	int INVITE_GROUP_REQ = 13;

	/**
	 * 同意加入群通知：系统->群主
	 */
	int INVITE_GROUP_AGREE_REQ = 14;

	/**
	 * 拒绝加入群通知：系统->群主
	 */
	int INVITE_GROUP_REFUSE_REQ = 15;

	/**
	 *  被踢出群
	 */
	int GROUP_KICK_OUT = 16;

	/**
	 * 群主解散群
	 */
	int GROUP_DELETE = 17;

	/**
	 * 获得某群管理员身份
	 */
	int GROUP_TO_BE_GROUP_MGR = 18;

	/**
	 * 失去某群管理员身份
	 */
	int GROUP_LOSE_GROUP_MGR = 19;

	/**
	 * 申请加群成功，群不需要验证，系统消息发给成员
	 */
	int APLLY_JOIN_GROUP_SUCCESS = 20;

	/**
	 * 邀请加群成功，成员不需要验证，系统消息发给成员
	 */
	int INVITE_ADD_GROUP_SUCCESS = 21;

	/**
	 * 申请入群，群不需要验证，系统消息发给群主及管理员
	 */
	int APLLY_JOIN_GROUP_TO_ADMIN_SUCCESS = 22;

	/**
	 * 退出群，系统消息发给群主或管理员
	 */
	int EXIT_GROUP_TO_ADMIN_SUCCESS = 23;

	/**
	 * 添加群成员，成员不需要验证，系统消息发给群主及管理员
	 */
	int INVITE_GROUP_TO_ADMIN_SUCCESS = 24;

	/**
	 * 群级别升级提示，普通群(300人上限)，高级群(500人上限)
	 */
	int GROUPMODE_PROMOTION = 25;

	/**
	 * 管理员A删除群成员后，管理员B收到通知
	 */
	int DELETE_GROUPMEMBER_TO_ADMIN = 30;
}
