package com.renyu.nimlibrary.bean

enum class ObserveResponseType {
    // 最近会话变更
    ObserveRecentContact,
    // 新消息接收
    ObserveReceiveMessage,
    // 用户资料刷新
    UserInfoUpdate,
    // 从服务器获取用户资料
    FetchUserInfo
}