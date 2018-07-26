package com.renyu.nimlibrary.binding

import android.view.View

interface EventImpl {
    fun click(view: View) {}

    // 删除联系人
    fun deleteRecentContact(view: View, contactId: String) {}

    // 跳转会话详情
    fun gotoConversationActivity(view: View, contactId: String) {}

    // 重新发送消息
    fun resendIMMessage(view: View, uuid: String) {}
}