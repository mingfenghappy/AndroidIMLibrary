package com.renyu.easemobuilibrary.manager;

import android.content.Context;

import com.blankj.utilcode.util.Utils;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMContactListener;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.renyu.easemobuilibrary.model.BroadcastBean;

import java.util.ArrayList;
import java.util.List;

public class ContactManager {

    /**
     * 获取好友列表
     * @return
     */
    public static void aysncGetAllContactsFromServer(Context context) {
        EMClient.getInstance().contactManager().aysncGetAllContactsFromServer(new EMValueCallBack<List<String>>() {
            @Override
            public void onSuccess(List<String> value) {
                ArrayList<String> users = new ArrayList<>(value);
                BroadcastBean.sendBroadcastSerializable(context, BroadcastBean.EaseMobCommand.FriendGroupsRsp, users);
            }

            @Override
            public void onError(int error, String errorMsg) {
                BroadcastBean.sendBroadcast(context, BroadcastBean.EaseMobCommand.FriendGroupsFailRsp);
            }
        });
    }

    public static List<String> getAllContactsFromServer() {
        try {
            return EMClient.getInstance().contactManager().getAllContactsFromServer();
        } catch (HyphenateException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 添加好友
     * @param username
     * @param reason
     */
    public static void aysncAddContact(String username, String reason) {
        EMClient.getInstance().contactManager().aysncAddContact(username, reason, new EMCallBack() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onProgress(int progress, String status) {

            }
        });
    }

    public static void addContact(String username, String reason) {
        try {
            EMClient.getInstance().contactManager().addContact(username, reason);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除好友
     * @param username
     */
    public static void aysncDeleteContact(String username) {
        EMClient.getInstance().contactManager().aysncDeleteContact(username, new EMCallBack() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onProgress(int progress, String status) {

            }
        });
    }

    public static void deleteContact(String username) {
        try {
            EMClient.getInstance().contactManager().deleteContact(username);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 同意好友请求
     * @param username
     */
    public static void asyncAcceptInvitation(String username) {
        EMClient.getInstance().contactManager().asyncAcceptInvitation(username, new EMCallBack() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onProgress(int progress, String status) {

            }
        });
    }

    public static void acceptInvitation(String username) {
        try {
            EMClient.getInstance().contactManager().acceptInvitation(username);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拒绝好友请求
     * @param username
     */
    public static void asyncDeclineInvitation(String username) {
        EMClient.getInstance().contactManager().asyncDeclineInvitation(username, new EMCallBack() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onProgress(int progress, String status) {

            }
        });
    }

    public static void declineInvitation(String username) {
        try {
            EMClient.getInstance().contactManager().declineInvitation(username);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 监听好友状态事件
     */
    public static void setContactListener() {
        EMClient.getInstance().contactManager().setContactListener(new EMContactListener() {
            @Override
            public void onContactInvited(String username, String reason) {
                // 收到好友邀请
                ArrayList<String> strings = new ArrayList<>();
                strings.add(username);
                strings.add(reason);
                BroadcastBean.sendBroadcastSerializable(Utils.getApp(), BroadcastBean.EaseMobCommand.onContactInvited, strings);
            }

            @Override
            public void onFriendRequestAccepted(String username) {
                // 好友请求被同意
                BroadcastBean.sendBroadcastSerializable(Utils.getApp(), BroadcastBean.EaseMobCommand.onFriendRequestAccepted, username);
            }

            @Override
            public void onFriendRequestDeclined(String username) {
                // 好友请求被拒绝
                BroadcastBean.sendBroadcastSerializable(Utils.getApp(), BroadcastBean.EaseMobCommand.onFriendRequestDeclined, username);
            }

            @Override
            public void onContactDeleted(String username) {
                // 被删除时回调此方法
                BroadcastBean.sendBroadcastSerializable(Utils.getApp(), BroadcastBean.EaseMobCommand.onContactDeleted, username);
            }


            @Override
            public void onContactAdded(String username) {
                // 增加了联系人时回调此方法
                BroadcastBean.sendBroadcastSerializable(Utils.getApp(), BroadcastBean.EaseMobCommand.onContactAdded, username);
            }
        });
    }

    /**
     * 获取黑名单信息
     */
    public static void aysncGetBlackListFromServer() {
        EMClient.getInstance().contactManager().aysncGetBlackListFromServer(new EMValueCallBack<List<String>>() {
            @Override
            public void onSuccess(List<String> value) {

            }

            @Override
            public void onError(int error, String errorMsg) {

            }
        });
    }

    public static void getBlackListFromServer() {
        EMClient.getInstance().contactManager().getBlackListUsernames();
    }

    /**
     * 添加黑名单
     * @param username
     */
    public static void aysncAddUserToBlackList(String username) {
        EMClient.getInstance().contactManager().aysncAddUserToBlackList(username, true, new EMCallBack() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onProgress(int progress, String status) {

            }
        });
    }

    public static void addUserToBlackList(String username) {
        try {
            EMClient.getInstance().contactManager().addUserToBlackList(username,true);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    public static void aysncRemoveUserFromBlackList(String username) {
        EMClient.getInstance().contactManager().aysncRemoveUserFromBlackList(username, new EMCallBack() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onProgress(int progress, String status) {

            }
        });
    }

    public static void removeUserFromBlackList(String username) {
        try {
            EMClient.getInstance().contactManager().removeUserFromBlackList(username);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }
}
