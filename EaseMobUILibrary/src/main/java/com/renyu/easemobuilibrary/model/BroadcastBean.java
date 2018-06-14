package com.renyu.easemobuilibrary.model;

import android.content.Intent;
import android.os.Parcelable;

import com.blankj.utilcode.util.Utils;

import java.io.Serializable;

public class BroadcastBean {

    public static String COMMAND = "command";
    public static String PARCELABLE = "commandParcelable";
    public static String SERIALIZABLE = "commandSerializable";

    public enum EaseMobCommand implements Serializable {
        LoginRsp,
        LoginRspERROR,
        UpdateRead,
        Kickout,
        MessageReceive,
        MessageSend,
        FriendGroupsRsp,
        FriendGroupsFailRsp,
        onContactInvited,
        onFriendRequestAccepted,
        onFriendRequestDeclined,
        onContactDeleted,
        onContactAdded
    }

    public static void sendBroadcast(EaseMobCommand command) {
        sendBroadcastParcelable(command, null);
    }

    public static void sendBroadcastParcelable(EaseMobCommand command, Parcelable parcelable) {
        String actionName = "";
        try {
            Class clazz = Class.forName("com.renyu.easemobapp.params.InitParams");
            actionName = clazz.getField("actionName").get(clazz).toString();
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

        Intent intent=new Intent();
        intent.putExtra(COMMAND, command);
        if (parcelable != null) {
            intent.putExtra(PARCELABLE, parcelable);
        }
        intent.setAction(actionName);
        Utils.getApp().sendBroadcast(intent);
    }

    public static void sendBroadcastSerializable(EaseMobCommand command, Serializable serializable) {
        String actionName = "";
        try {
            Class clazz = Class.forName("com.renyu.easemobapp.params.InitParams");
            actionName = clazz.getField("actionName").get(clazz).toString();
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

        Intent intent=new Intent();
        intent.putExtra(COMMAND, command);
        if (serializable != null) {
            intent.putExtra(SERIALIZABLE, serializable);
        }
        intent.setAction(actionName);
        Utils.getApp().sendBroadcast(intent);
    }
}
