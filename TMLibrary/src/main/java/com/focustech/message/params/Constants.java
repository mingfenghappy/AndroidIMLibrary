package com.focustech.message.params;

public interface Constants {
	/**
	 * 无需验证
	 */
	String NO_NEED_VERIFICATION = "0";

	/**
	 * 需要验证
	 */
	String NEED_VERIFICATION = "1";

	/**
	 * 拒绝任何人
	 */
	String REFUSE_ANYONE = "2";

	/**
	 * 好友会话
	 */
	int FRIEND_CONVERSATION = 0;

	/**
	 * 群会话
	 */
	int GROUP_CONVERSATION = 1;
	
	/**
	 * 系统消息会话
	 */
	int SYSTEM_CONVERSATION = 2;
	
	/**
	 * 系统消息会话
	 */
	int WARNING_CONVERSATION = 3;
	
	/**
	 * 讨论组会话
	 */
	int DISCUSSION_CONVERSATION = 4;

	/**
	 * 系统会话ID(本地约定)
	 */
	String SYSTEM_CONVERSATION_ID = "-1";
	
	/**
	 * 系统会话ID(本地约定)
	 */
	String WARNING_CONVERSATION_ID = "-2";

	/**
	 * 接收消息
	 */
	int RECEIVE_MESSAGE = 1;

	/**
	 * 发送消息
	 */
	int SEND_MESSAGE = 0;

	/**
	 *群被解散
	 */
	int DELETE_GROUP = 0;

	/**
	 * 被踢出群
	 */
	int KICK_OUT_GROUP = 1;

	/**
	 * 退出出群
	 */
	int EXIT_GROUP = 2;

	/**
	 * 群被群主解散
	 */
	int DELETE_GROUP_BY_OWNER = 3;

	/**
	 * 登录类型:ios:2 android:3
	 * 待server端发布正式版改为3
	 */
	int LOGIN_TYPE_ANDROID = 3;
	
	/**
	 * 添加好友上限
	 */
	int FRIEND_COUNT_LIMIT = 300;
	
	/**
	 * 退出讨论组
	 */
	int EXIT_DISCUSSION = 4;
	
	int DISCUSSION_INVALID = 5;
}
