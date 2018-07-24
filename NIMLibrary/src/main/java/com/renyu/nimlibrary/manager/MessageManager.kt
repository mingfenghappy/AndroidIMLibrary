package com.renyu.nimlibrary.manager

import android.util.Log
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.msg.MsgServiceObserve

object MessageManager {

    /**
     * 监听消息接收
     */
    fun observeReceiveMessage() {
        NIMClient.getService(MsgServiceObserve::class.java)
                .observeReceiveMessage({ t -> Log.d("NIMAPP", "收到新消息${t?.size}") }, true)
    }

    /**
     * 监听最近会话变更
     */
    fun observeRecentContact() {
        NIMClient.getService(MsgServiceObserve::class.java)
                .observeRecentContact({ t -> Log.d("NIMAPP", "最近会话列表变更${t?.size}") }, true)
    }

    /**
     * 监听最近联系人被删除
     */
    fun observeRecentContactDeleted() {
        NIMClient.getService(MsgServiceObserve::class.java)
                .observeRecentContactDeleted({ t -> Log.d("NIMAPP", "最近会话列表变更${t?.contactId}") }, true)
    }

    /**
     * 监听消息状态
     */
    fun observeMsgStatus() {
        NIMClient.getService(MsgServiceObserve::class.java)
                .observeMsgStatus({ t -> Log.d("NIMAPP", "消息状态：${t.fromNick} ${t.content} ${t.status}") }, true)
    }

    /**
     * 监听消息撤回
     */
    fun observeRevokeMessage() {
        NIMClient.getService(MsgServiceObserve::class.java)
                .observeRevokeMessage({ t -> Log.d("NIMAPP", "被撤回的消息消息ID：${t.message.uuid}") }, true)
    }
}