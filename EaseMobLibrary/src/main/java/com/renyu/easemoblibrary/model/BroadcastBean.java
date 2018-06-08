package com.renyu.easemoblibrary.model;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

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
        MessageSend
    }

    public static void sendBroadcast(Context context, EaseMobCommand command) {
        sendBroadcastParcelable(context, command, null);
    }

    public static void sendBroadcastParcelable(Context context, EaseMobCommand command, Parcelable parcelable) {
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
        context.sendBroadcast(intent);
    }

    public static void sendBroadcastSerializable(Context context, EaseMobCommand command, Serializable serializable) {
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
        context.sendBroadcast(intent);
    }
}
