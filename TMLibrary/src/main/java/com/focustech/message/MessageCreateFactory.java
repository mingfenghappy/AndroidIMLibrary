package com.focustech.message;

import com.focustech.message.msg.TMMessage;
import com.focustech.message.params.MessageMeta;
import com.focustech.tm.open.sdk.messages.protobuf.Enums.Enable;
import com.focustech.tm.open.sdk.messages.protobuf.Enums.MessageType;
import com.focustech.tm.open.sdk.messages.protobuf.Messages;
import com.focustech.tm.open.sdk.messages.protobuf.Messages.Message;
import com.focustech.message.group.GroupMsgResponse;
import com.focustech.message.msg.MessageResponse;
import com.google.protobuf.InvalidProtocolBufferException;

import org.json.JSONException;
import org.json.JSONObject;

/**********************************************************
 * @文件名称：MessageCreateFactory.java
 * @文件作者：huangshuo
 * @创建时间：2014-9-10 下午4:21:31
 * @文件描述：
 * @修改历史：2014-9-10创建初始版本
 **********************************************************/
public class MessageCreateFactory {
	/**
	 * 普通消息适配
	 * @param message
	 * @param isOfflineMsg 
	 * @param isSyncMsg 
	 * @return
	 */
	public static MessageResponse createMessage(TMMessage message, boolean isOfflineMsg, boolean isSyncMsg) {
		Message m = null;
		MessageResponse resp = null;
		try {
			m = Message.parseFrom(message.getBody());
		}
		catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

		if (m != null) {
			resp = new MessageResponse();
			resp.setCmd(message.getHead().getCmd());
			resp.setMsg(m.getMsg());
			resp.setFromUserId(m.getUserId());
			resp.setServeTime(m.getTimestamp() + "");
			resp.setOfflineMessage(isOfflineMsg);
			resp.setFromSvrMsgId(m.getFromSvrMsgId());
			if (!m.hasResend() || m.getResend() == Enable.DISABLE) {
				resp.setResendMessage(false);
			}
			else {
				resp.setResendMessage(true);
			}
			if (isOfflineMsg) {
				// 如果是离线消息，svrMsgId是body中的
				resp.setServermsgid(m.getSvrMsgId());
			}
			else {
				// 如果是普通消息，svrMsgId是head中的
				resp.setServermsgid(message.getHead().getSvrSeqId());
			}
			resp.setSyncMessage(isSyncMsg);
			resp.setMessageId(message.getHead().getCliSeqId() + "");
			getMessageMetaData(resp, m);
			resp.setMsgMeta(m.getMsgMeta());
		}
		return resp;
	}

	public static GroupMsgResponse createGroupMessage(TMMessage message, boolean isOfflineMsg, boolean isSyncMsg) {
		Messages.GroupMessage groupMessage = null;
		GroupMsgResponse response = null;
		try {
			groupMessage = Messages.GroupMessage.parseFrom(message.getBody());
		}
		catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		if (groupMessage != null) {
			response = new GroupMsgResponse();
			if (groupMessage.getMsgType() == MessageType.USER_QUIT) {
				response.setMessagetype("-1");
				return response;
			}
			if (groupMessage.getMsgType() == MessageType.INVITE_USER_JOIN) {
				response.setMessagetype("-1");
				return response;
			}
			response.setCmd(message.getHead().getCmd());
			response.setMsg(groupMessage.getMsg());
			response.setGroupid(groupMessage.getGroupId());
			response.setFromUserId(groupMessage.getUserId());
			response.setServeTime(String.valueOf(groupMessage.getTimestamp()));
			response.setOfflineMessage(isOfflineMsg);
			response.setMsgid(String.valueOf(message.getHead().getCliSeqId()));
			response.setServermsgid(groupMessage.getSvrMsgId());
			response.setSyncMessage(isSyncMsg);
			// 设置是否重发
			if (!groupMessage.hasResend() || groupMessage.getResend() == Enable.DISABLE) {
				response.setResendMessage(false);
			}
			else {
				response.setResendMessage(true);
			}
		}
		getGroupMessageMetaData(response, groupMessage);
		if (groupMessage.getMsgType() == MessageType.AUTO_REPLY) {
			response.setMessagetype("9");
		}
		if (groupMessage.getMsgType() == MessageType.USER_ADD) {
			response.setMessagetype("10");
			getJoinMeta(response, groupMessage);
		}
		return response;
	}

	/**
	 * @param resp
	 * @param m 
	 */
	public static void getMessageMetaData(MessageResponse resp, Message m) {
		try {
			JSONObject object = new JSONObject(m.getMsgMeta());
			if (object.getInt(MessageMeta.msgtype) == 1) {
				resp.setMessagetype("0");
				resp.setParameters("piccount=0|picname=|filesize=");
				return;
			}
			if (object.has(MessageMeta.picpath) && !"".equals(object.getString(MessageMeta.picpath).trim())
					&& object.getString(MessageMeta.picpath) != null) {
				JSONObject picObject = new JSONObject(object.getString(MessageMeta.picpath));
				if (object.has(MessageMeta.piccount) && !"".equals(object.getString(MessageMeta.piccount))
						|| object.getString(MessageMeta.piccount) != null) {
					resp.setMessagetype("0");
					resp.setParameters("piccount=0|picname=|filesize=");
				}
				resp.setPicNum(object.getInt(MessageMeta.piccount) + "");
				if (m.getMsgType() == MessageType.TEXT
						&& (object.getInt(MessageMeta.msgtype) == 0 || object.getInt(MessageMeta.msgtype) == 1)) {
					// 接收普通文本
					resp.setMessagetype("0");
					resp.setParameters("piccount=0|picname=|filesize=");
				}
				else if (m.getMsgType() == MessageType.MULTI_MEDIA && object.getInt(MessageMeta.msgtype) == 0) {
					// 接收图片
					resp.setMessagetype("8");
					String picPath = picObject.getString("0");
					for (int i = 1; i < picObject.length(); i++) {
						picPath = picPath + "*" + picObject.getString(i + "");
					}
					resp.setParameters("piccount=" + object.getInt(MessageMeta.piccount) + "|picname=" + picPath
							+ "|filesize=");

					if (object.has(MessageMeta.picid) && !"".equals(object.getString(MessageMeta.picid).trim())
							&& object.getString(MessageMeta.picid) != null) {
						// 添加媒体消息的fileId
						JSONObject pidObject = new JSONObject(object.getString(MessageMeta.picid));
						String localfilename = "";
						for (int i = 0; i < object.getInt(MessageMeta.piccount); i++) {
							if (pidObject.has(String.valueOf(i))) {
								String fileId = pidObject.getString(String.valueOf(i));
								localfilename = fileId;
							}
						}
						resp.setLocalfilename(localfilename);
					}
				}
				else if (m.getMsgType() == MessageType.MULTI_MEDIA && object.getInt(MessageMeta.msgtype) == 7) {
					// 接收语音
					resp.setMessagetype("7");
					String voiceName = picObject.getString("1");
					resp.setParameters("piccount=" + object.getInt(MessageMeta.piccount) + "|picname=" + voiceName
							+ "|filesize=" + object.getInt(MessageMeta.filesize));
					if (object.has(MessageMeta.picid) && !"".equals(object.getString(MessageMeta.picid).trim())
							&& object.getString(MessageMeta.picid) != null) {
						// 添加媒体消息的fileId
						JSONObject pidObject = new JSONObject(object.getString(MessageMeta.picid));
						String localfilename = "";
						if (pidObject.has(String.valueOf(1))) {
							String fileId = pidObject.getString(String.valueOf(1));
							localfilename = fileId;
						}
						resp.setLocalfilename(localfilename);
					}
				}
			}
			else {
				resp.setMessagetype("0");
				resp.setParameters("piccount=0|picname=|filesize=");
			}
		}
		catch (JSONException e) {
			resp.setMessagetype("0");
			resp.setParameters("piccount=0|picname=|filesize=");
			e.printStackTrace();
		}
	}

	private static void getGroupMessageMetaData(GroupMsgResponse resp, Messages.GroupMessage m) {
		try {
			JSONObject object = new JSONObject(m.getMsgMeta());
			if (object.has(MessageMeta.msgtype) && object.getInt(MessageMeta.msgtype) == 1) {
				resp.setMessagetype("0");
				resp.setParameters("piccount=0|picname=|filesize=");
				return;
			}
			if (object.has(MessageMeta.picpath) && !"".equals(object.getString(MessageMeta.picpath).trim())
					&& object.getString(MessageMeta.picpath) != null) {
				JSONObject picObject = new JSONObject(object.getString(MessageMeta.picpath));
				if (object.has(MessageMeta.piccount) && !"".equals(object.getString(MessageMeta.piccount))
						|| object.getString(MessageMeta.piccount) != null) {
					resp.setMessagetype("0");
					resp.setParameters("piccount=0|picname=|filesize=");
				}
				resp.setPicNum(object.getInt(MessageMeta.piccount) + "");
				if (m.getMsgType() == MessageType.TEXT
						&& (object.getInt(MessageMeta.msgtype) == 0 || object.getInt(MessageMeta.msgtype) == 1)) {
					// 接收普通文本
					resp.setMessagetype("0");
					resp.setParameters("piccount=0|picname=|filesize=");
				}
				else if (m.getMsgType() == MessageType.MULTI_MEDIA && object.getInt(MessageMeta.msgtype) == 0) {
					// 接收图片
					resp.setMessagetype("8");
					String picPath = picObject.getString("0");
					for (int i = 1; i < picObject.length(); i++) {
						picPath = picPath + "*" + picObject.getString(i + "");
					}
					resp.setParameters("piccount=" + object.getInt(MessageMeta.piccount) + "|picname=" + picPath
							+ "|filesize=");
				}
				else if (m.getMsgType() == MessageType.MULTI_MEDIA && object.getInt(MessageMeta.msgtype) == 7) {
					// 接收语音
					resp.setMessagetype("7");
					String voiceName = picObject.getString("1");
					resp.setParameters("piccount=" + object.getInt(MessageMeta.piccount) + "|picname=" + voiceName
							+ "|filesize=" + object.getInt(MessageMeta.filesize));
				}
				if (object.has(MessageMeta.picid) && !"".equals(object.getString(MessageMeta.picid).trim())
						&& object.getString(MessageMeta.picid) != null) {
					JSONObject pidObject = new JSONObject(object.getString(MessageMeta.picid));
					String localfilename = "";
					if (pidObject.has(String.valueOf(1))) {
						String fileId = pidObject.getString(String.valueOf(1));
						localfilename = fileId;
					}
					resp.setLocalfilename(localfilename);
				}
			}
			else {
				resp.setMessagetype("0");
				resp.setParameters("piccount=0|picname=|filesize=");
			}
			if (object.has(MessageMeta.sender)) {
				resp.setUsername(object.getString(MessageMeta.sender));
			}
		}
		catch (JSONException e) {
			resp.setMessagetype("0");
			resp.setParameters("piccount=0|picname=|filesize=");
			e.printStackTrace();
		}
	}

	private static void getJoinMeta(GroupMsgResponse response, Messages.GroupMessage m) {
		try {
			JSONObject object = new JSONObject(m.getMsgMeta());
			response.setMsg(object.getString(MessageMeta.name));
			response.setFromUserId(object.getString(MessageMeta.userid));
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
