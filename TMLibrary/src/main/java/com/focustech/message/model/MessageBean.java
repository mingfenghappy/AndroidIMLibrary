package com.focustech.message.model;

import android.text.TextUtils;

import com.focustech.message.params.MessageMeta;
import com.focustech.tm.open.sdk.messages.protobuf.Enums;
import com.focustech.tm.open.sdk.messages.protobuf.Head;
import com.focustech.tm.open.sdk.messages.protobuf.Messages;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/7/20.
 */

public class MessageBean implements Serializable {
    String msg;                      // 消息内容
    String msgMeta;                  // 消息元数据
    Enums.MessageType msgType;       // 消息类型
    String userId;                   // c -> s 代表接收方用户，s -> c 代表发送方用户
    long timestamp;                  // 客户端自己的NTP时间戳
    String svrMsgId;                 // 客户端需要--离线消息时会传递，用于客户端自己去重判断
    Enums.Enable sync;               // 消息是否发送完成 已发送1 未发送0
    Enums.Enable resend;             // 是否是需要重发消息
    // 临时使用的数据
    String messageType;              // 消息类型（文本、音频、图片）
    String localFileName;            // 文件路径
    String isSend;                   // 1:发送 0:接收
    String isRead;                   // 消息是否已读  1:已读 0:未读
    String isVoicePlay;              // 语音消息是否已读  1:已读 0:未读
    String cliSeqId;                 // 发送消息顺序，用来做发送成功失败判断，接收端无需使用

    int count;                       // 未读消息数（仅会话列表使用）

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsgMeta() {
        return msgMeta;
    }

    public void setMsgMeta(String msgMeta) {
        this.msgMeta = msgMeta;
    }

    public Enums.MessageType getMsgType() {
        return msgType;
    }

    public void setMsgType(Enums.MessageType msgType) {
        this.msgType = msgType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIsSend() {
        return isSend;
    }

    public void setIsSend(String isSend) {
        this.isSend = isSend;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSvrMsgId() {
        return svrMsgId;
    }

    public void setSvrMsgId(String svrMsgId) {
        this.svrMsgId = svrMsgId;
    }

    public Enums.Enable getSync() {
        return sync;
    }

    public void setSync(Enums.Enable sync) {
        this.sync = sync;
    }

    public Enums.Enable getResend() {
        return resend;
    }

    public void setResend(Enums.Enable resend) {
        this.resend = resend;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getLocalFileName() {
        return localFileName;
    }

    public void setLocalFileName(String localFileName) {
        this.localFileName = localFileName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getIsRead() {
        return isRead;
    }

    public void setIsRead(String isRead) {
        this.isRead = isRead;
    }

    public String getIsVoicePlay() {
        return isVoicePlay;
    }

    public void setIsVoicePlay(String isVoicePlay) {
        this.isVoicePlay = isVoicePlay;
    }

    public String getCliSeqId() {
        return cliSeqId;
    }

    public void setCliSeqId(String cliSeqId) {
        this.cliSeqId = cliSeqId;
    }

    public static MessageBean parse(Messages.Message message, Head.TMHeadMessage headMessage) {
        MessageBean bean=new MessageBean();
        bean.setTimestamp(message.getTimestamp());
//        bean.setFromSvrMsgId(headMessage.getSvrSeqId());
        bean.setMsg(message.getMsg());
        bean.setMsgMeta(message.getMsgMeta());
        bean.setMsgType(message.getMsgType());
        bean.setResend(message.getResend());
        bean.setSvrMsgId(message.getSvrMsgId());
        bean.setSync(Enums.Enable.ENABLE);
        bean.setUserId(message.getUserId());
        bean.setIsSend("0");
        bean.setIsRead("0");
        bean.setIsVoicePlay("0");
        getMessageMetaData(bean, message);
        return bean;
    }

    public static void getMessageMetaData(MessageBean messageBean, Messages.Message m) {
        try {
            JSONObject object = new JSONObject(m.getMsgMeta());
            if (m.getMsgType() == Enums.MessageType.TEXT && (object.getInt(MessageMeta.msgtype) == 0 || object.getInt(MessageMeta.msgtype) == 1)) {
                // 接收普通文本
                messageBean.setMessageType("0");
            }
            else if (m.getMsgType() == Enums.MessageType.AUTO_REPLY && (object.getInt(MessageMeta.msgtype) == 0 || object.getInt(MessageMeta.msgtype) == 1)) {
                // 接收自动回复
                messageBean.setMessageType("9");
            }
            else if (m.getMsgType() == Enums.MessageType.MULTI_MEDIA && object.getInt(MessageMeta.msgtype) == 0) {
                // 接收图片
                messageBean.setMessageType("8");
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
                    messageBean.setLocalFileName(localfilename);
                }
            }
            else if (m.getMsgType() == Enums.MessageType.MULTI_MEDIA && object.getInt(MessageMeta.msgtype) == 7) {
                // 接收语音
                messageBean.setMessageType("7");
                if (object.has(MessageMeta.picid) && !"".equals(object.getString(MessageMeta.picid).trim())
                        && object.getString(MessageMeta.picid) != null) {
                    // 添加媒体消息的fileId
                    JSONObject pidObject = new JSONObject(object.getString(MessageMeta.picid));
                    String localfilename = "";
                    if (pidObject.has(String.valueOf(1))) {
                        String fileId = pidObject.getString(String.valueOf(1));
                        localfilename = fileId;
                    }
                    messageBean.setLocalFileName(localfilename);
                }
            }
        }
        catch (JSONException e) {
            messageBean.setMessageType("0");
            e.printStackTrace();
        }
    }

    /**
     * 聊天列表消息类型判断
     * @param response
     * @return
     */
    public static int getMessageTypeForCharList(OfflineIMResponse response) {
        // 在本地发送最后一条数据的情况下，选取type字段
        if (TextUtils.isEmpty(response.getMsgMeta())) {
            return response.getType();
        }
        try {
            JSONObject object = new JSONObject(response.getMsgMeta());
            if (response.getType() == Enums.MessageType.TEXT.getNumber() && (object.getInt(MessageMeta.msgtype) == 0 || object.getInt(MessageMeta.msgtype) == 1)) {
                // 接收普通文本
                return 0;
            }
            else if (response.getType() == Enums.MessageType.AUTO_REPLY.getNumber() && (object.getInt(MessageMeta.msgtype) == 0 || object.getInt(MessageMeta.msgtype) == 1)) {
                // 接收自动回复
                return 9;
            }
            else if (response.getType() == Enums.MessageType.MULTI_MEDIA.getNumber() && object.getInt(MessageMeta.msgtype) == 0) {
                // 接收图片
                return 8;
            }
            else if (response.getType() == Enums.MessageType.MULTI_MEDIA.getNumber() && object.getInt(MessageMeta.msgtype) == 7) {
                // 接收语音
                return 7;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 聊天列表消息类型判断
     * @param response
     * @return
     */
    public static int getMessageTypeForConversation(OfflineIMDetailResponse response) {
        try {
            JSONObject object = new JSONObject(response.getMsgMeta());
            if (response.getType() == Enums.MessageType.TEXT.getNumber() && (object.getInt(MessageMeta.msgtype) == 0 || object.getInt(MessageMeta.msgtype) == 1)) {
                // 接收普通文本
                return 0;
            }
            else if (response.getType() == Enums.MessageType.AUTO_REPLY.getNumber() && (object.getInt(MessageMeta.msgtype) == 0 || object.getInt(MessageMeta.msgtype) == 1)) {
                // 接收自动回复
                return 9;
            }
            else if (response.getType() == Enums.MessageType.MULTI_MEDIA.getNumber() && object.getInt(MessageMeta.msgtype) == 0) {
                // 接收图片
                return 8;
            }
            else if (response.getType() == Enums.MessageType.MULTI_MEDIA.getNumber() && object.getInt(MessageMeta.msgtype) == 7) {
                // 接收语音
                return 7;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }
}
