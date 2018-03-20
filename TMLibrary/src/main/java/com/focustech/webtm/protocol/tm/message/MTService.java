package com.focustech.webtm.protocol.tm.message;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.focustech.webtm.protocol.tm.message.model.BroadcastBean;
import com.focustech.webtm.protocol.tm.message.model.MessageBean;
import com.focustech.tm.open.sdk.messages.protobuf.Enums;
import com.focustech.common.IUpdateFile;
import com.focustech.common.UploadPictureTool;
import com.focustech.common.UploadTool;
import com.focustech.common.UploadVoiceTool;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2017/7/7.
 */

public class MTService extends Service {

    RequestClient client;

    ExecutorService service;

    @Override
    public void onCreate() {
        super.onCreate();
        client = new RequestClient(getApplicationContext());
        service = Executors.newSingleThreadExecutor();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (intent!=null && intent.getStringExtra("type")!=null) {
                    // 连接服务器
                    if (intent.getStringExtra("type").equals("conn")) {
                        client.startConnector();
                    }
                    // 登录
                    if (intent.getStringExtra("type").equals("reqLogin")) {
                        client.reqLogin(intent.getStringExtra("username"), intent.getStringExtra("pwd"));
                    }
                    // 发送心跳包
                    if (intent.getStringExtra("type").equals("heartbeat")) {
                        client.heartbeat();
                    }
                    // 获取好友分组信息
                    if (intent.getStringExtra("type").equals("reqFriendGroups")) {
                        client.reqFriendGroups();
                    }
                    // 获取全部好友信息
                    if (intent.getStringExtra("type").equals("reqFriends")) {
                        client.reqFriends();
                    }
                    // 获取全部离线消息
                    if (intent.getStringExtra("type").equals("reqGetOfflineMessage")) {
                        client.reqGetOfflineMessage();
                    }
                    // 已读离线消息回执
                    if (intent.getStringExtra("type").equals("hasReadOfflineMessage")) {
                        client.hasReadOfflineMessage(intent.getStringExtra("svrSeqId"));
                    }
                    // 已读单聊消息回执
                    if (intent.getStringExtra("type").equals("hasReadMessage")) {
                        client.hasReadMessage(intent.getStringExtra("svrSeqId"));
                    }
                    if (intent.getStringExtra("type").equals("getsetting")) {
                        client.getUserSettingsInfo();
                    }
                    if (intent.getStringExtra("type").equals("sendTextMessage")) {
                        client.sendTextMessage(intent.getStringExtra("toUserId"), intent.getStringExtra("msg"), intent.getStringExtra("userName"));
                    }
                    if (intent.getStringExtra("type").equals("sendPicMessage")) {
                        UploadTool tool = new UploadPictureTool();
                        tool.setOnUpdataListener(new IUpdateFile() {
                            @Override
                            public void updateFile() {

                            }

                            @Override
                            public void onUploadFinish(String str) {
                                client.sendPicMessage(intent.getStringExtra("toUserId"), intent.getStringExtra("filePath"), intent.getStringExtra("userName"), str);
                                BroadcastBean.sendBroadcast(MTService.this, BroadcastBean.MTCommand.MessageUploadComp, intent.getSerializableExtra("messageBean"));
                            }

                            @Override
                            public void onUploadError() {
                                BroadcastBean.sendBroadcast(MTService.this, BroadcastBean.MTCommand.MessageUploadFail, intent.getSerializableExtra("messageBean"));
                            }
                        });
                        tool.upload(intent.getStringExtra("filePath"));
                    }
                    if (intent.getStringExtra("type").equals("sendVoiceMessage")) {
                        UploadTool tool = new UploadVoiceTool();
                        tool.setOnUpdataListener(new IUpdateFile() {
                            @Override
                            public void updateFile() {

                            }

                            @Override
                            public void onUploadFinish(String str) {
                                client.sendVoiceMessage(intent.getStringExtra("toUserId"), intent.getStringExtra("filePath"), intent.getStringExtra("userName"), str);
                                BroadcastBean.sendBroadcast(MTService.this, BroadcastBean.MTCommand.MessageUploadComp, intent.getSerializableExtra("messageBean"));
                            }

                            @Override
                            public void onUploadError() {
                                BroadcastBean.sendBroadcast(MTService.this, BroadcastBean.MTCommand.MessageUploadFail, intent.getSerializableExtra("messageBean"));
                            }
                        });
                        tool.upload(intent.getStringExtra("filePath"));
                    }
                    // 获取系统消息
                    if (intent.getStringExtra("type").equals("getSysNtyReq")) {
                        client.getSysNtyReq(intent.getLongExtra("time", 0));
                    }
                    if (intent.getStringExtra("type").equals("getGroupList")) {
                        client.requestGroupList();
                    }
                    if (intent.getStringExtra("type").equals("getGroupSingleUserInfo")) {
                        client.requestGroupSingleUserInfo("KfqHpbiMBXg", "ttxPdzMAHf4");
                    }
                    if (intent.getStringExtra("type").equals("updateGroupNickName")) {
                        client.updateGroupNickName("测试群组任", "KfqHpbiMBXg", "ttxPdzMAHf4");
                    }
                    if (intent.getStringExtra("type").equals("updateGroupRemark")) {
                        client.updateGroupRemark("测试群组任备注", "KfqHpbiMBXg");
                    }
                    if (intent.getStringExtra("type").equals("updateGroupInfo")) {
                        client.updateGroupInfo("测试简介", "测试签名", "KfqHpbiMBXg", "测试群组陈天宇", "", "INNOVATION_WORKS", "ALLOW_WITHOUT_VALIDATE");
                    }
                    if (intent.getStringExtra("type").equals("updateGroupMessageSetting")) {
                        client.updateGroupMessageSetting("KfqHpbiMBXg", Enums.MessageSetting.ACCEPT_AND_PROMPT);
                    }
                    if (intent.getStringExtra("type").equals("requestGroupInfo")) {
                        client.requestGroupInfo("KfqHpbiMBXg");
                    }
                    if (intent.getStringExtra("type").equals("requestGroupMember")) {
                        client.requestGroupMember("KfqHpbiMBXg");
                    }
                    if (intent.getStringExtra("type").equals("requestGroupMemberInfo")) {
                        ArrayList<String> userIds = new ArrayList<>();
                        userIds.add("ttxPdzMAHf4");
                        userIds.add("InI60G--Oqs");
                        userIds.add("2Tj0laQY-KU");
                        client.requestGroupMemberInfo(userIds);
                    }
                    if (intent.getStringExtra("type").equals("deleteGroupUser")) {
                        client.deleteGroupUser("KfqHpbiMBXg", "2Tj0laQY-KU");
                    }
                    if (intent.getStringExtra("type").equals("inviteUserJoinGroup")) {
                        ArrayList<String> userIds = new ArrayList<>();
                        userIds.add("2Tj0laQY-KU");
                        client.inviteUserJoinGroup("KfqHpbiMBXg", userIds);
                    }
                    if (intent.getStringExtra("type").equals("createGroup")) {
                        ArrayList<String> userIds = new ArrayList<>();
                        userIds.add("2Tj0laQY-KU");
                        client.createGroup("任groupName", "任groupSignature", "任groupKeyword", "任groupDesc", Enums.GroupType.OTHER, Enums.ValidateRule.ALLOW_WITHOUT_VALIDATE);
                    }
                    if (intent.getStringExtra("type").equals("groupchatpic")) {
                        final String picPath = "/storage/emulated/0/Pictures/rBEBYFlRuDeADZnqAACxZbaFEtI819.jpeg";
                        UploadTool tool = new UploadPictureTool();
                        tool.setOnUpdataListener(new IUpdateFile() {
                            @Override
                            public void updateFile() {

                            }

                            @Override
                            public void onUploadFinish(String str) {
                                client.sendGroupPicMessage("ttxPdzMAHf4", "KfqHpbiMBXg", picPath, "365房博士-武汉", str);
                            }

                            @Override
                            public void onUploadError() {

                            }
                        });
                        tool.upload(picPath);
                    }
                    if (intent.getStringExtra("type").equals("groupchatvoice")) {
                        final String voicePath = "/storage/emulated/0/bubugao.amr";
                        UploadTool tool = new UploadVoiceTool();
                        tool.setOnUpdataListener(new IUpdateFile() {
                            @Override
                            public void updateFile() {

                            }

                            @Override
                            public void onUploadFinish(String str) {
                                client.sendGroupVoiceMessage("ttxPdzMAHf4", "KfqHpbiMBXg", voicePath, "365房博士-武汉", str);
                            }

                            @Override
                            public void onUploadError() {

                            }
                        });
                        tool.upload(voicePath);
                    }
                    // 添加好友
                    if (intent.getStringExtra("type").equals("addFriendReq")) {
                        client.addFriendReq(MTService.this, intent.getStringExtra("userId"), intent.getStringExtra("ext"), intent.getStringExtra("srcFriendGroupId"));
                    }
                    // 获取添加好友权限
                    if (intent.getStringExtra("type").equals("getFriendRuleReq")) {
                        client.getFriendRuleReq(MTService.this, intent.getStringExtra("userId"));
                    }
                    // 删除好友
                    if (intent.getStringExtra("type").equals("deleteFriendReq")) {
                        client.deleteFriendReq(MTService.this, intent.getStringExtra("userId"));
                    }
                    // 获取单个好友信息
                    if (intent.getStringExtra("type").equals("getUserInfo")) {
                        client.getUserInfo(MTService.this, intent.getStringExtra("userId"));
                    }
                }
            }
        };
        service.execute(runnable);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        client.closeConnector();
    }

    /**
     * 连接服务端
     * @param context
     */
    public static void conn(Context context) {
        Intent intent=new Intent(context, MTService.class);
        intent.putExtra("type", "conn");
        context.startService(intent);

        // 发送正在连接广播
        BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.Conning, "");
    }

    /**
     * 登录
     * @param context
     */
    public static void reqLogin(Context context, String username, String pwd) {
        Intent intent=new Intent(context, MTService.class);
        intent.putExtra("type", "reqLogin");
        intent.putExtra("username", username);
        intent.putExtra("pwd", pwd);
        context.startService(intent);
    }

    /**
     * 发送心跳包
     * @param context
     */
    public static void heartbeat(Context context) {
        Intent intent=new Intent(context, MTService.class);
        intent.putExtra("type", "heartbeat");
        context.startService(intent);
    }

    /**
     * 获取系统消息
     * @param context
     */
    public static void getSysNtyReq(Context context, long time) {
        Intent intent=new Intent(context, MTService.class);
        intent.putExtra("time", time);
        intent.putExtra("type", "getSysNtyReq");
        context.startService(intent);
    }

    /**
     * 获取好友分组信息
     * @param context
     */
    public static void reqFriendGroups(Context context) {
        Intent intent=new Intent(context, MTService.class);
        intent.putExtra("type", "reqFriendGroups");
        context.startService(intent);
    }

    /**
     * 获取全部好友信息
     * @param context
     */
    public static void reqFriendInfo(Context context) {
        Intent intent=new Intent(context, MTService.class);
        intent.putExtra("type", "reqFriends");
        context.startService(intent);
    }

    /**
     * 获取全部离线消息
     * @param context
     */
    public static void reqGetOfflineMessage(Context context) {
        Intent intent=new Intent(context, MTService.class);
        intent.putExtra("type", "reqGetOfflineMessage");
        context.startService(intent);
    }

    /**
     * 已读离线消息回执
     * @param context
     * @param svrSeqId
     */
    public static void hasReadOfflineMessage(Context context, String svrSeqId) {
        Intent intent=new Intent(context, MTService.class);
        intent.putExtra("type", "hasReadOfflineMessage");
        intent.putExtra("svrSeqId", svrSeqId);
        context.startService(intent);
    }

    /**
     * 已读单聊消息回执
     * @param context
     * @param svrSeqId
     */
    public static void hasReadMessage(Context context, String svrSeqId) {
        Intent intent=new Intent(context, MTService.class);
        intent.putExtra("type", "hasReadMessage");
        intent.putExtra("svrSeqId", svrSeqId);
        context.startService(intent);
    }

    /**
     * 发送单聊文本消息
     * @param context
     * @param toUserId
     * @param msg
     * @param userName
     */
    public static void sendTextMessage(Context context, String toUserId, String msg, String userName) {
        Intent intent=new Intent(context, MTService.class);
        intent.putExtra("type", "sendTextMessage");
        intent.putExtra("toUserId", toUserId);
        intent.putExtra("msg", msg);
        intent.putExtra("userName", userName);
        context.startService(intent);
    }

    /**
     * 发送单聊图片消息
     * @param context
     * @param toUserId
     * @param filePath
     * @param userName
     */
    public static void sendPicMessage(Context context, String toUserId, String filePath, String userName, MessageBean messageBean) {
        Intent intent=new Intent(context, MTService.class);
        intent.putExtra("type", "sendPicMessage");
        intent.putExtra("toUserId", toUserId);
        intent.putExtra("filePath", filePath);
        intent.putExtra("userName", userName);
        intent.putExtra("messageBean", messageBean);
        context.startService(intent);
    }

    /**
     * 发送单聊语音消息
     * @param context
     * @param toUserId
     * @param filePath
     * @param userName
     */
    public static void sendVoiceMessage(Context context, String toUserId, String filePath, String userName, MessageBean messageBean) {
        Intent intent=new Intent(context, MTService.class);
        intent.putExtra("type", "sendVoiceMessage");
        intent.putExtra("toUserId", toUserId);
        intent.putExtra("filePath", filePath);
        intent.putExtra("userName", userName);
        intent.putExtra("messageBean", messageBean);
        context.startService(intent);
    }

    /**
     * 添加好友
     * @param context
     * @param userId
     * @param srcFriendGroupId
     */
    public static void addFriendReq(Context context, String userId, String ext, String srcFriendGroupId) {
        Intent intent=new Intent(context, MTService.class);
        intent.putExtra("type", "addFriendReq");
        intent.putExtra("userId", userId);
        intent.putExtra("ext", ext);
        intent.putExtra("srcFriendGroupId", srcFriendGroupId);
        context.startService(intent);
    }

    /**
     * 删除好友
     * @param context
     * @param userId
     */
    public static void deleteFriendReq(Context context, String userId) {
        Intent intent=new Intent(context, MTService.class);
        intent.putExtra("type", "deleteFriendReq");
        intent.putExtra("userId", userId);
        context.startService(intent);
    }

    /**
     * 获取单个好友信息
     * @param context
     * @param userId
     */
    public static void getUserInfo(Context context, String userId) {
        Intent intent=new Intent(context, MTService.class);
        intent.putExtra("type", "getUserInfo");
        intent.putExtra("userId", userId);
        context.startService(intent);
    }

    /**
     * 获取添加好友权限
     * @param context
     * @param userId
     */
    public static void getFriendRuleReq(Context context, String userId) {
        Intent intent=new Intent(context, MTService.class);
        intent.putExtra("type", "getFriendRuleReq");
        intent.putExtra("userId", userId);
        context.startService(intent);
    }
}
