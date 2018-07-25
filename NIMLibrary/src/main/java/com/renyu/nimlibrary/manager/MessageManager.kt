package com.renyu.nimlibrary.manager

import android.util.Log
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.renyu.nimlibrary.bean.ObserveResponse
import com.renyu.nimlibrary.bean.ObserveResponseType
import com.renyu.nimlibrary.util.RxBus

object MessageManager {

    /**
     * 监听新消息接收
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
                .observeRecentContact({
                    if (it != null) {
                        it.forEach {
                            Log.d("NIMAPP", "最近会话列表变更:${it.fromNick}")
                        }
                        RxBus.getDefault().post(ObserveResponse(it, ObserveResponseType.ObserveRecentContact))
                    }
                }, true)
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

    /**
     * 将所有联系人的未读数清零
     */
    fun clearAllUnreadCount() {
        NIMClient.getService(MsgService::class.java).clearAllUnreadCount()
    }

    /**
     * 清除与指定用户的所有消息记录
     */
    fun clearChattingHistory(account: String, sessionType: SessionTypeEnum) {
        NIMClient.getService(MsgService::class.java).clearChattingHistory(account, sessionType)
    }

    /**
     * 清空消息数据库的所有消息记录
     */
    fun clearMsgDatabase() {
        NIMClient.getService(MsgService::class.java).clearMsgDatabase(true)
    }

    /**
     * 将指定最近联系人的未读数清零
     */
    fun clearUnreadCount(account: String, sessionType: SessionTypeEnum) {
        NIMClient.getService(MsgService::class.java).clearUnreadCount(account, sessionType)
    }

    /**
     * 删除一条消息记录
     */
    fun deleteChattingHistory(message: IMMessage) {
        NIMClient.getService(MsgService::class.java).deleteChattingHistory(message)
    }

    /**
     * 从最近联系人列表中删除一项
     */
    fun deleteRecentContact(recent: RecentContact) {
        NIMClient.getService(MsgService::class.java).deleteRecentContact(recent)
    }

    /**
     * 删除最近联系人记录
     */
    fun deleteRecentContact2(account: String, sessionType: SessionTypeEnum) {
        NIMClient.getService(MsgService::class.java).deleteRecentContact2(account, sessionType)
    }

    /**
     * 获取未读数总数
     */
    fun getTotalUnreadCount(): Int {
        return NIMClient.getService(MsgService::class.java).totalUnreadCount
    }

    /**
     * 获取最近会话列表
     */
    fun queryRecentContacts(requestCallback: RequestCallback<List<RecentContact>>) {
        NIMClient.getService(MsgService::class.java)
                .queryRecentContacts()
                .setCallback(requestCallback)
    }

    /**
     * 获取会话详情
     */
    fun queryMessageListEx(message: IMMessage, requestCallback: RequestCallback<List<IMMessage>>) {
        NIMClient.getService(MsgService::class.java)
                .queryMessageListEx(message, QueryDirectionEnum.QUERY_OLD, 20, true)
                .setCallback(requestCallback)
    }

    /**
     * 消息撤回
     */
    fun revokeMessage(message: IMMessage, requestCallback: RequestCallback<Void>) {
        NIMClient.getService(MsgService::class.java).revokeMessage(message).setCallback(requestCallback)
    }

    /**
     * 保存消息到本地数据库，但不发送到服务器端
     */
    fun saveMessageToLocal(message: IMMessage, notify: Boolean, requestCallback: RequestCallback<Void>) {
        NIMClient.getService(MsgService::class.java).saveMessageToLocal(message, notify).setCallback(requestCallback)
    }

    /**
     * 保存消息到本地数据库，但不发送到服务器端
     */
    fun saveMessageToLocalEx(message: IMMessage, notify: Boolean, time: Long, requestCallback: RequestCallback<Void>) {
        NIMClient.getService(MsgService::class.java).saveMessageToLocalEx(message, notify, time).setCallback(requestCallback)
    }

    /**
     * 设置当前正在聊天的对象。设置后会影响内建的消息提醒。如果有新消息到达，且消息来源是正在聊天的对象，将不会有消息提醒。
     * 调用该接口还会附带调用clearUnreadCount(String, SessionTypeEnum),将正在聊天对象的未读数清零。
     */
    fun setChattingAccount(account: String, sessionType: SessionTypeEnum) {
        NIMClient.getService(MsgService::class.java).setChattingAccount(account, sessionType)
    }


}