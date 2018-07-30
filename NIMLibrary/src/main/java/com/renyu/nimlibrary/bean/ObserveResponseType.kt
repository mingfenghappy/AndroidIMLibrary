package com.renyu.nimlibrary.bean

enum class ObserveResponseType {
    // 最近会话变更
    ObserveRecentContact,
    // 新消息接收
    ObserveReceiveMessage,
    // 用户资料刷新
    UserInfoUpdate,
    // 从服务器获取用户资料
    FetchUserInfo,
    // 用户发送的消息状态
    MsgStatus,
    // 用户收到的消息
    ReceiveMessage,
    // 撤销当前消息
    RevokeMessage
}