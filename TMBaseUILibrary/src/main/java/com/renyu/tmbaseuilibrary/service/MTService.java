package com.renyu.tmbaseuilibrary.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.focustech.message.RequestClient;
import com.focustech.message.model.BroadcastBean;
import com.focustech.message.model.MessageBean;
import com.focustech.tm.open.sdk.messages.protobuf.Enums;
import com.renyu.commonlibrary.network.OKHttpHelper;
import com.renyu.commonlibrary.network.OKHttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
        Runnable runnable = () -> {
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
                    client.sendTextMessage(intent.getStringExtra("toUserId"), intent.getStringExtra("msg"), intent.getStringExtra("userName"), intent.getStringExtra("cliSeqId"));
                }
                if (intent.getStringExtra("type").equals("sendPicMessage")) {
                    uploadFile(intent, "picture", intent.getStringExtra("cliSeqId"));
                }
                if (intent.getStringExtra("type").equals("sendVoiceMessage")) {
                    uploadFile(intent, "voice", intent.getStringExtra("cliSeqId"));
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
                // TODO: 2018/3/22 0022 群聊没有测试
                if (intent.getStringExtra("type").equals("groupchatpic")) {
                    // client.sendGroupPicMessage("ttxPdzMAHf4", "KfqHpbiMBXg", picPath, "365房博士-武汉", str);
                }
                if (intent.getStringExtra("type").equals("groupchatvoice")) {
                    // client.sendGroupVoiceMessage("ttxPdzMAHf4", "KfqHpbiMBXg", voicePath, "365房博士-武汉", str);
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
        };
        service.execute(runnable);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 上传语音跟图片
     * @param intent
     * @param type
     */
    private void uploadFile(Intent intent, String type, String cliSeqId) {
        File file = new File(intent.getStringExtra("filePath"));
        HashMap<String, File> fileHashMap = new HashMap<>();
        fileHashMap.put("file", file);
        HashMap<String, String> paramsHashmap = new HashMap<>();
        paramsHashmap.put("type", type);
        paramsHashmap.put("token", "qEWY3kh6WZ6NTVdTX5s4rhTh-lF6JwaJzXyaeiF7qYOKa7vCCHMccqEfjjHFF6-gPaXxYPrgUjkmzNwUygwGl3ORAqWmqemBbnP_cGTY1ZQeLjkm6GS0KjGlY3hzbS0o");
        HashMap<String, String> heads = new HashMap<>();
        heads.put("Data-Range", "0-"+(file.length()-1));
        heads.put("Data-Length", ""+file.length());
        OKHttpHelper.getInstance().getOkHttpUtils().asyncUpload("http://webim.house365.com/tm/file/upload", paramsHashmap, fileHashMap, heads, new OKHttpUtils.RequestListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(String string) {
                try {
                    JSONObject jsonObject = new JSONObject(string);
                    if (!TextUtils.isEmpty(jsonObject.getString("fileId"))) {
                        if (type.equals("picture")) {
                            client.sendPicMessage(intent.getStringExtra("toUserId"), intent.getStringExtra("filePath"), intent.getStringExtra("userName"), jsonObject.getString("fileId"), cliSeqId);
                        }
                        else if (type.equals("voice")) {
                            client.sendVoiceMessage(intent.getStringExtra("toUserId"), intent.getStringExtra("filePath"), intent.getStringExtra("userName"), jsonObject.getString("fileId"), cliSeqId);
                        }
                    }
                    else {
                        BroadcastBean.sendBroadcast(MTService.this, BroadcastBean.MTCommand.MessageFail, cliSeqId);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    BroadcastBean.sendBroadcast(MTService.this, BroadcastBean.MTCommand.MessageFail, cliSeqId);
                }
            }

            @Override
            public void onError() {
                BroadcastBean.sendBroadcast(MTService.this, BroadcastBean.MTCommand.MessageFail, cliSeqId);
            }
        }, (currentBytesCount, totalBytesCount) -> {

        });
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
    public static void sendTextMessage(Context context, String toUserId, String msg, String userName, String cliSeqId) {
        Intent intent=new Intent(context, MTService.class);
        intent.putExtra("type", "sendTextMessage");
        intent.putExtra("toUserId", toUserId);
        intent.putExtra("msg", msg);
        intent.putExtra("userName", userName);
        intent.putExtra("cliSeqId", cliSeqId);
        context.startService(intent);
    }

    /**
     * 发送单聊图片消息
     * @param context
     * @param toUserId
     * @param filePath
     * @param userName
     */
    public static void sendPicMessage(Context context, String toUserId, String filePath, String userName, String cliSeqId, MessageBean messageBean) {
        Intent intent=new Intent(context, MTService.class);
        intent.putExtra("type", "sendPicMessage");
        intent.putExtra("toUserId", toUserId);
        intent.putExtra("filePath", filePath);
        intent.putExtra("userName", userName);
        intent.putExtra("cliSeqId", cliSeqId);
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
    public static void sendVoiceMessage(Context context, String toUserId, String filePath, String userName, String cliSeqId, MessageBean messageBean) {
        Intent intent=new Intent(context, MTService.class);
        intent.putExtra("type", "sendVoiceMessage");
        intent.putExtra("toUserId", toUserId);
        intent.putExtra("filePath", filePath);
        intent.putExtra("userName", userName);
        intent.putExtra("cliSeqId", cliSeqId);
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
