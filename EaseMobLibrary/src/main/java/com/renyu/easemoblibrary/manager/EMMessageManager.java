package com.renyu.easemoblibrary.manager;

import android.content.Context;
import android.util.Log;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.exceptions.HyphenateException;
import com.renyu.easemoblibrary.model.BroadcastBean;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EMMessageManager {

    static final String KEY_DING_ACK = "EMDingMessageAck";
    static final String KEY_CONVERSATION_ID = "EMConversationID";

    /**
     * 文本消息
     * @param content 消息文字内容
     * @param toChatUsername 对方用户或者群聊的id
     * @return 文本消息结构体
     */
    public static EMMessage prepareTxtEMMessage(String content, String toChatUsername) {
        return EMMessage.createTxtSendMessage(content, toChatUsername);
    }

    /**
     * 语音消息
     * @param filePath 语音文件路径
     * @param timeLength 录音时间(秒)
     * @param toChatUsername 对方用户或者群聊的id
     * @return 语音消息结构体
     */
    public static EMMessage prepareVoiceEMMessage(String filePath, int timeLength, String toChatUsername) {
        return EMMessage.createVoiceSendMessage(filePath, timeLength, toChatUsername);
    }


    /**
     * 发送视频消息
     * @param videofilePath 视频本地路径
     * @param imageThumbPath 视频预览图路径
     * @param timeLength 视频时间长度
     * @param toChatUsername 对方用户或者群聊的id
     */
    public static EMMessage prepareVideoMessage(String videofilePath, String imageThumbPath, int timeLength, String toChatUsername) {
        return EMMessage.createVideoSendMessage(videofilePath, imageThumbPath, timeLength, toChatUsername);
    }

    /**
     * 发送图片消息
     * @param filePath 图片本地路径
     * @param sendOriginalImage false为不发送原图（默认超过100k的图片会压缩后发给对方），需要发送原图传true
     * @param toChatUsername 对方用户或者群聊的id
     */
    public static EMMessage prepareImageMessage(String filePath, boolean sendOriginalImage, String toChatUsername) {
        return EMMessage.createImageSendMessage(filePath, sendOriginalImage, toChatUsername);
    }

    /**
     * 发送地理位置消息
     * @param latitude 纬度
     * @param longitude 经度
     * @param locationAddress 具体位置内容
     * @param toChatUsername 对方用户或者群聊的id
     */
    public static EMMessage prepareLocationMessage(double latitude, double longitude, String locationAddress, String toChatUsername) {
        return EMMessage.createLocationSendMessage(latitude, longitude, locationAddress, toChatUsername);
    }

    /**
     * 发送文件消息
     * @param filePath 文件路径
     * @param toChatUsername 对方用户或者群聊的id
     */
    public static EMMessage prepareFileMessage(String filePath, String toChatUsername) {
        return EMMessage.createFileSendMessage(filePath, toChatUsername);
    }

    /**
     * 发送透传消息
     * @param action 自定义动作
     * @param toUsername 对方用户或者群聊的id
     */
    public static EMMessage prepareCMDMessage(String action, String toUsername) {
        EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
        EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
        cmdMsg.setTo(toUsername);
        cmdMsg.addBody(cmdBody);
        return cmdMsg;
    }

    /**
     * 设置已读回执
     * @param message 消息体
     */
    public static void sendAckMessage(EMMessage message) {
        if (!message.isAcked() && message.getChatType() == EMMessage.ChatType.Chat) {
            try {
                EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
            return;
        }
        EMMessage msg = EMMessage.createSendMessage(EMMessage.Type.CMD);
        msg.setTo(message.getFrom());
        msg.setAttribute(KEY_CONVERSATION_ID, message.getTo());
        msg.setAttribute(KEY_DING_ACK, true);
        msg.addBody(new EMCmdMessageBody(message.getMsgId()));
        EMClient.getInstance().chatManager().sendMessage(msg);
        message.setAcked(true);
    }

    public static void sendSingleMessage(Context context, EMMessage emMessage) {
        sendMessage(context, emMessage, null, null);
    }

    public static void sendSingleMessage(Context context, EMMessage emMessage, HashMap<String, Object> attributes) {
        sendMessage(context, emMessage, null, attributes);
    }

    public static void sendGroupMessage(Context context, EMMessage emMessage) {
        sendMessage(context, emMessage, EMMessage.ChatType.GroupChat, null);
    }

    public static void sendGroupMessage(Context context, EMMessage emMessage, HashMap<String, Object> attributes) {
        sendMessage(context, emMessage, EMMessage.ChatType.GroupChat, attributes);
    }

    public static void sendChatRoomMessage(Context context, EMMessage emMessage) {
        sendMessage(context, emMessage, EMMessage.ChatType.ChatRoom, null);
    }

    public static void sendChatRoomMessage(Context context, EMMessage emMessage, HashMap<String, Object> attributes) {
        sendMessage(context, emMessage, EMMessage.ChatType.ChatRoom, attributes);
    }

    /**
     * 发送消息
     * @param message 消息体
     * @param chatType 如果是群聊，设置chattype，默认是单聊
     * @param attributes 扩展消息
     */
    private static void sendMessage(final Context context, EMMessage message, EMMessage.ChatType chatType, HashMap<String, Object> attributes) {
        if (attributes != null && attributes.size() > 0) {
            Iterator<Map.Entry<String, Object>> iterator = attributes.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                if (entry.getValue() instanceof String) {
                    message.setAttribute(entry.getKey(), (String) entry.getValue());
                }
                else if (entry.getValue() instanceof Integer) {
                    message.setAttribute(entry.getKey(), (Integer) entry.getValue());
                }
                else if (entry.getValue() instanceof Boolean) {
                    message.setAttribute(entry.getKey(), (Boolean) entry.getValue());
                }
                else if (entry.getValue() instanceof Long) {
                    message.setAttribute(entry.getKey(), (Long) entry.getValue());
                }
                else if (entry.getValue() instanceof JSONObject) {
                    message.setAttribute(entry.getKey(), (JSONObject) entry.getValue());
                }
                else if (entry.getValue() instanceof JSONArray) {
                    message.setAttribute(entry.getKey(), (JSONArray) entry.getValue());
                }
                else {
                    throw new IllegalArgumentException("attributes中的value只可以为String/Integer/Boolean/Long/JSONObject/JSONArray类型中的一种");
                }
            }
        }

        if (chatType != null) {
            message.setChatType(chatType);
        }

        registerMessageStatusCallback(context, message);

        EMClient.getInstance().chatManager().sendMessage(message);
    }

    /**
     * 监听消息发送状态
     * @param message 消息体
     */
    private static void registerMessageStatusCallback(final Context context, final EMMessage message) {
        message.setMessageStatusCallback(new EMCallBack() {
            @Override
            public void onSuccess() {
                message.setStatus(EMMessage.Status.SUCCESS);
                BroadcastBean.sendBroadcastParcelable(context, BroadcastBean.EaseMobCommand.MessageReceive, message);
            }

            @Override
            public void onError(int code, String error) {
                message.setStatus(EMMessage.Status.FAIL);
                BroadcastBean.sendBroadcastParcelable(context, BroadcastBean.EaseMobCommand.MessageReceive, message);
            }

            @Override
            public void onProgress(int progress, String status) {
            }
        });
    }

    /**
     * 设置消息监听
     */
    public static void registerMessageListener(Context context) {
        EMClient.getInstance().chatManager().addMessageListener(new EMMessageListener() {
            @Override
            public void onMessageRead(List<EMMessage> messages) {
                // 收到已读回执
                Log.d("EaseMobUtils", "onMessageRead");
            }
            @Override
            public void onMessageDelivered(List<EMMessage> messages) {
                // 收到已送达回执
                Log.d("EaseMobUtils", "onMessageDelivered");
            }

            @Override
            public void onMessageRecalled(List<EMMessage> messages) {
                // 消息被撤回
                Log.d("EaseMobUtils", "onMessageRecalled");
            }

            @Override
            public void onMessageReceived(List<EMMessage> messages) {
                // 收到消息
                Log.d("EaseMobUtils", "onMessageReceived");
                for (EMMessage message : messages) {
                    BroadcastBean.sendBroadcastParcelable(context, BroadcastBean.EaseMobCommand.MessageReceive, message);
                }
            }

            @Override
            public void onMessageChanged(EMMessage message, Object change) {
                // 消息状态变动
                Log.d("EaseMobUtils", "onMessageChanged");
            }
            @Override
            public void onCmdMessageReceived(List<EMMessage> messages) {
                // 收到透传消息
                Log.d("EaseMobUtils", "onCmdMessageReceived");
                for (EMMessage message : messages) {

                }
            }
        });
    }

    /**
     * 获取所有会话
     * @return
     */
    public static Map<String, EMConversation> getAllConversations() {
        return EMClient.getInstance().chatManager().getAllConversations();
    }

    /**
     * 获取此conversation当前内存所有的message。如果内存中为空，再从db中加载
     * SDK初始化加载的聊天记录为20条，到顶时需要去DB里获取更多
     * @param username user id or group id
     * @return
     */
    public static List<EMMessage> getAllMessages(String username, EMConversation.EMConversationType type) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username, type, true);
        return conversation.getAllMessages();
    }

    public static List<EMMessage> getAllMessages(String username) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username, EMConversation.EMConversationType.Chat, true);
        return conversation.getAllMessages();
    }

    /**
     * 根据传入的参数从db加载startMsgId之前(存储顺序)指定数量的message
     * @param username user id or group id
     * @param startMsgId
     * @param pagesize
     * @return
     */
    public static List<EMMessage> getConversationByStartMsgId(String username, EMConversation.EMConversationType type, String startMsgId, int pagesize) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username, type, true);
        return conversation.loadMoreMsgFromDB(startMsgId, pagesize);
    }

    public static List<EMMessage> getConversationByStartMsgId(String username, String startMsgId, int pagesize) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username, EMConversation.EMConversationType.Chat, true);
        return conversation.loadMoreMsgFromDB(startMsgId, pagesize);
    }

    /**
     * 获取此对话中未读取的消息数量
     * @param username user id or group id
     * @return
     */
    public static int getUnreadMsgCount(String username, EMConversation.EMConversationType type) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username, type, true);
        return conversation.getUnreadMsgCount();
    }

    public static int getUnreadMsgCount(String username) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username, EMConversation.EMConversationType.Chat, true);
        return conversation.getUnreadMsgCount();
    }

    /**
     * 指定会话消息未读数清零
     */
    public static void markAllMessagesAsRead(String username, EMConversation.EMConversationType type) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username, type, true);
        conversation.markAllMessagesAsRead();
    }

    public static void markAllMessagesAsRead(String username) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username, EMConversation.EMConversationType.Chat, true);
        conversation.markAllMessagesAsRead();
    }

    /**
     * 把一条消息置为已读
     * @param username
     * @param messageId
     */
    public static void markMessageAsRead(String username, EMConversation.EMConversationType type, String messageId) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username, type, true);
        conversation.markMessageAsRead(messageId);
    }

    public static void markMessageAsRead(String username, String messageId) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username, EMConversation.EMConversationType.Chat, true);
        conversation.markMessageAsRead(messageId);
    }

    /**
     * 所有未读消息数清零
     */
    public static void markAllConversationsAsRead() {
        EMClient.getInstance().chatManager().markAllConversationsAsRead();
    }

    /**
     * 删除会话及聊天记录
     * @param username 删除和某个user会话
     * @param deleteMessages 需要保留聊天记录，传false
     */
    public static void deleteConversation(String username, boolean deleteMessages) {
        //删除和某个user会话，如果需要保留聊天记录，传false
        EMClient.getInstance().chatManager().deleteConversation(username, deleteMessages);
    }

    /**
     * 删除当前会话的某条聊天记录
     * @param username
     * @param msgId
     */
    public static void removeMessage(String username, String msgId) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username);
        conversation.removeMessage(msgId);
    }

    /**
     * 导入消息到数据库
     * @param message
     */
    public static void importMessages(List<EMMessage> message) {
        EMClient.getInstance().chatManager().importMessages(message);
    }

    /**
     * 同步加载所有的会话，并且每条会话读入EMChatOptions.getNumberOfMessagesLoaded()条消息, 默认是20条以保持兼容
     */
    public static void loadAllConversations() {
        EMClient.getInstance().chatManager().loadAllConversations();
    }
}
