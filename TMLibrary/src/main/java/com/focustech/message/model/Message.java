package com.focustech.message.model;

/**
 * 消息
 */
public class Message {
	public static final int TEXT_MSG = 0;
	public static final int PICTURE_MSG = 8;
	public static final int VOICE_MSG = 7;
	public static final int AUTO_MSG = 9;
	public static final int FILE_MSG = 2;
	// 发出的消息
	public static final int FALG_SEND = 0;
	// 接收的消息
	public static final int FALG_RECEIVE = 1;
	public static final int Sending = 0;
	public static final int SendSuccess = 1;
	public static final int SendError = 2;
	public enum MessageType {
		TEXT, FACE, RECORD, IMG, FILE, TEXT_AND_FACE
	}
	private int _id;
	// 所属会话类型
	private int conversationType;
	// 所属会话id(userId或groupId)
	private String conversationId;
	// 本地时间
	private long localTime;
	private int messageFalg = FALG_SEND;
	private String msg;
	private String messageId;
	private long serveTime;
	private String fromUserId = "0";
	private String toUserId = "0";
	// 判断会话中是否需要显示时间
	protected int isShowTime = 0;
	// 需要显示的图片名称在服务器上的地址
	private String localFileName = "";
	// 区别是否是离线文件0不是，1是
	protected String isOffLineFile = "0";
	protected String fileSize;
	// 记录文件已下载的数量
	protected String fileDownInfo;
	// 系统分配的id
	private String servermsgid = "0";
	private String picname = "";
	// 消息类型 0:文本 1:图片 2:文件 7:语音 9:自动回复 10:加入群消息
	private int MsgType;
	// 发送方名
	private String fromUserName;
	// 会话好友的备注
	private String remark;
	// 发送方头像
	private String fromUserface;
	// 发送方自定义头像
	private String fromUserFileid;
	// 发送方的状态
	private String userStatus;
	// 群名称
	private String groupName;
	private boolean isSyncMsg = false;
	// 群备注
	private String groupNote;
	private String groupNickName;
	// 录音下载 0:未下载等待 1:下载中等待2：下载完成
	private int isDownVoice = 0;
	// 记录多图片情况下，此message对应第几个
	private String picPosition = "-1|-1";
	
	private String fromSvrMsgId = null;

	private int voice_time = -1;
	
	private int sendState = 2;

	public boolean isSyncMsg()
	{
		return isSyncMsg;
	}

	public void setSyncMsg(boolean isSyncMsg)
	{
		this.isSyncMsg = isSyncMsg;
	}

	public int getVoice_time()
	{
		return voice_time;
	}

	public void setVoice_time(int voice_time)
	{
		this.voice_time = voice_time;
	}

	public String getUserStatus()
	{
		return userStatus;
	}

	public void setUserStatus(String userStatus)
	{
		this.userStatus = userStatus;
	}

	public int getIsDownVoice()
	{
		return isDownVoice;
	}

	public void setIsDownVoice(int isDownVoice)
	{
		this.isDownVoice = isDownVoice;
	}

	public int getMsgType()
	{
		return MsgType;
	}

	public void setMsgType(int msgType)
	{
		MsgType = msgType;
	}

	public int getConversationType()
	{
		return conversationType;
	}

	public void setConversationType(int conversationType)
	{
		this.conversationType = conversationType;
	}

	public String getGroupName()
	{
		return groupName;
	}

	public void setGroupName(String groupName)
	{
		this.groupName = groupName;
	}

	public int get_id()
	{
		return _id;
	}

	public void set_id(int _id)
	{
		this._id = _id;
	}

	public long getLocalTime()
	{
		return localTime;
	}

	public void setLocalTime(long localTime)
	{
		this.localTime = localTime;
	}

	public int getMessageFalg()
	{
		return messageFalg;
	}

	public void setMessageFalg(int messageFalg)
	{
		this.messageFalg = messageFalg;
	}

	public Message()
	{
		super();
	}

	public String getMsg()
	{
		return msg;
	}

	public String getConversationId()
	{
		return conversationId;
	}

	public void setConversationId(String conversationId)
	{
		this.conversationId = conversationId;
	}

	public void setMsg(String msg)
	{
		this.msg = msg;
	}

	public String getMessageId()
	{
		return messageId;
	}

	public void setMessageId(String messageId)
	{
		this.messageId = messageId;
	}

	public String getFromUserId()
	{
		return fromUserId;
	}

	public void setFromUserId(String fromUserId)
	{
		this.fromUserId = fromUserId;
	}

	public String getToUserId()
	{
		return toUserId;
	}

	public void setToUserId(String toUserId)
	{
		this.toUserId = toUserId;
	}

	public long getServeTime()
	{
		return serveTime;
	}

	public void setServeTime(long serveTime)
	{
		this.serveTime = serveTime;
	}

	public int getIsShowTime()
	{
		return isShowTime;
	}

	// 0 is default ,1 is not show ,2 is showing
	public void setIsShowTime(int isShowTime)
	{
		this.isShowTime = isShowTime;
	}

	public String getFromUserName()
	{
		return fromUserName;
	}

	public void setFromUserName(String fromUserName)
	{
		if (fromUserName != null)
		{
			// 去掉两个字名字间的空格
			this.fromUserName = fromUserName.replace("　", "");
		}
	}

	public String getFromUserface()
	{
		return fromUserface;
	}

	public void setFromUserface(String fromUserface)
	{
		this.fromUserface = fromUserface;
	}

	public String getFromUserFileid()
	{
		return fromUserFileid;
	}

	public void setFromUserFileid(String fromUserFileid)
	{
		this.fromUserFileid = fromUserFileid;
	}

	public String getLocalFileName()
	{
		return localFileName;
	}

	public void setLocalFileName(String localFileName)
	{
		this.localFileName = localFileName;
	}

	public String getServermsgid()
	{
		return servermsgid;
	}

	public void setServermsgid(String servermsgid)
	{
		this.servermsgid = servermsgid;
	}

	public String getPicname()
	{
		return picname;
	}

	public void setPicname(String picname)
	{
		this.picname = picname;
	}

	public String getIsOffLineFile()
	{
		return isOffLineFile;
	}

	public void setIsOffLineFile(String isOffLineFile)
	{
		this.isOffLineFile = isOffLineFile;
	}

	public String getFileSize()
	{
		return fileSize;
	}

	public void setFileSize(String fileSize)
	{
		this.fileSize = fileSize;
	}

	public String getFileDownInfo()
	{
		return fileDownInfo;
	}

	public void setFileDownInfo(String fileDownInfo)
	{
		this.fileDownInfo = fileDownInfo;
	}

	public String getPicPosition()
	{
		return picPosition;
	}

	public void setPicPosition(String picPosition)
	{
		this.picPosition = picPosition;
	}

	public String getRemark()
	{
		return remark;
	}

	public void setRemark(String remark)
	{
		this.remark = remark;
	}

	public String getGroupNote()
	{
		return groupNote;
	}

	public void setGroupNote(String groupNote)
	{
		this.groupNote = groupNote;
	}
	
	public String getFromSvrMsgId()
	{
		return fromSvrMsgId;
	}

	public void setFromSvrMsgId(String fromSvrMsgId)
	{
		this.fromSvrMsgId = fromSvrMsgId;
	}
	
	public int getSendState()
	{
		return sendState;
	}

	public void setSendState(int sendState)
	{
		this.sendState = sendState;
	}


	@Override
	public String toString()
	{
		return "Message [msg=" + msg + ", messageId=" + messageId + ", serveTime=" + serveTime + ", fromUserId="
				+ fromUserId + ", toUserId=" + toUserId + ",fromUserName=" + fromUserName + ",conversationId="
				+ conversationId + "]";
	}

}
