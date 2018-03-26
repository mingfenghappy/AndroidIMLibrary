package com.focustech.message.model;

import android.content.Context;
import android.content.Intent;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/7/18.
 */

public class BroadcastBean implements Serializable {
    public enum MTCommand implements Serializable {
        HeartBeat,               // 自增，用于标志心跳包
        Conn,                    // 自增，用于已连接
        Conning,                 // 自增，用于标志发起连接
        Disconn,                 // 自增，用于连接失败
        LoginRsp,
        LoginRspERROR,
        UserInfoRsp,
        FriendGroupsRsp,
        FriendInfoRsp,
        FriendInfoEndRsp,
        GetOfflineMessageRsp,
        UpdateUserStatusNty,
        Message,
        MessageSend,             // 自增，用于标志发送的消息完成
        MessageReceive,          // 自增，用于标志消息接收完成，用于数据库操作完成后刷新列表使用
        MessageUploadComp,       // 自增，用于标志发送的语音消息上传完成
        MessageUploadFail,       // 自增，用于标志发送的语音消息上传失败
        MessageDownloadComp,     // 自增，用于标志下载语音文件完成
        ReceptNty,
        UpdateRead,               // 自增，用于标志未读消息清除
        NewSysNty,
        GetFriendRuleRsp,
        DeleteFriendRsp,
        SystemMessageBean,
        AddFriendWithoutValidateSucceededSysNty,
        FriendInfoNty
    }

    MTCommand command;
    Serializable serializable;

    public MTCommand getCommand() {
        return command;
    }

    public void setCommand(MTCommand command) {
        this.command = command;
    }

    public Serializable getSerializable() {
        return serializable;
    }

    public void setSerializable(Serializable serializable) {
        this.serializable = serializable;
    }

    public static void sendBroadcast(Context context, MTCommand command, Serializable serializable) {
        BroadcastBean bean=new BroadcastBean();
        bean.setCommand(command);
        bean.setSerializable(serializable);
        Intent intent=new Intent();
        intent.putExtra("broadcast", bean);
        intent.setAction("MT");
        context.sendBroadcast(intent);
    }
}
