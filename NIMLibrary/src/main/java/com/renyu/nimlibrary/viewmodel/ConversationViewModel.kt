package com.renyu.nimlibrary.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.os.Handler
import android.view.View
import android.widget.Toast
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.Utils
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.ResponseCode
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomNotification
import com.netease.nimlib.sdk.msg.model.CustomNotificationConfig
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.RevokeMsgNotification
import com.renyu.nimapp.bean.Resource
import com.renyu.nimlibrary.bean.ObserveResponse
import com.renyu.nimlibrary.bean.ObserveResponseType
import com.renyu.nimlibrary.binding.EventImpl
import com.renyu.nimlibrary.manager.MessageManager
import com.renyu.nimlibrary.params.CommonParams
import com.renyu.nimlibrary.repository.Repos
import com.renyu.nimlibrary.ui.adapter.ConversationAdapter
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class ConversationViewModel(private val contactId: String, private val sessionType: SessionTypeEnum) : ViewModel(), EventImpl {

    private val messages: ArrayList<IMMessage> by lazy {
        ArrayList<IMMessage>()
    }

    // 接口请求数据
    private val messageListReqeuestBefore: MutableLiveData<IMMessage> = MutableLiveData()
    var messageListResponseBefore: LiveData<Resource<List<IMMessage>>>? = null

    val adapter: ConversationAdapter by lazy {
        ConversationAdapter(messages, this)
    }

    // 首次添加
    private var firstLoad = false

    // "正在输入提示"指令发送时间间隔
    private var typingTime: Long = 0

    init {
        messageListResponseBefore = Transformations.switchMap(messageListReqeuestBefore) {
            if (it == null) {
                MutableLiveData<Resource<List<IMMessage>>>()
            }
            else {
                Repos.queryMessageListExBefore(it)
            }
        }
    }

    /**
     * 获取会话详情列表
     */
    fun queryMessageLists(imMessage: IMMessage?) {
        var temp = imMessage
        if (imMessage == null) {
            temp = MessageBuilder.createEmptyMessage(contactId, sessionType, 0)
        }
        messageListReqeuestBefore.value = temp
    }

    /**
     * 加载更多消息
     */
    fun loadMoreLocalMessage() {
        if (messages.size != 0) {
            queryMessageLists(messages[0])
        }
    }

    /**
     * 添加旧的消息数据
     */
    fun addOldIMMessages(imMessages: List<IMMessage>) {
        if (imMessages.isEmpty()) {
            return
        }
        messages.addAll(0, imMessages)
        adapter.updateShowTimeItem(messages, true, firstLoad)
        // 添加新数据
        adapter.notifyItemRangeInserted(0, imMessages.size)
        // 根据时间变化刷新老数据
        adapter.notifyItemRangeChanged(imMessages.size, messages.size)
        firstLoad = false
    }

    /**
     * 收到新消息
     */
    fun receiveIMMessages(observeResponse: ObserveResponse) {
        if (observeResponse.type == ObserveResponseType.ReceiveMessage) {
            val temp = ArrayList<IMMessage>()
            for (message in observeResponse.data as List<*>) {
                if (message is IMMessage && isMyMessage(message)) {
                    temp.add(message)
                }
            }
            // 添加消息并排序
            messages.addAll(temp)
            sortMessages(messages)
            adapter.updateShowTimeItem(messages, false, true)
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * 发送消息
     */
    fun sendIMMessage(message: IMMessage) {
        messages.add(message)
        adapter.updateShowTimeItem(messages, false, true)
        adapter.notifyItemInserted(messages.size - 1)
    }

    /**
     * 重新发送消息
     */
    override fun resendIMMessage(view: View, uuid: String) {
        super.resendIMMessage(view, uuid)
        // 找到重发的那条消息
        val imMessages = messages.filter {
            it.uuid == uuid
        }.take(1)
        if (imMessages.isNotEmpty()) {
            val imMessage = imMessages[0]
            imMessage.status = MsgStatusEnum.sending
            // 删除之前的数据
            deleteItem(imMessage, true)
            // 添加为新的数据
            messages.add(imMessage)
            // 重新调整时间
            adapter.updateShowTimeItem(messages, false, true)
            adapter.notifyDataSetChanged()

            MessageManager.reSendMessage(imMessage)
        }
    }

    /**
     * 删除消息
     */
    private fun deleteIMMessage(imMessage: IMMessage) {
        val index = deleteItem(imMessage, true)
        adapter.notifyItemRemoved(index)
        // 重新调整时间
        adapter.updateShowTimeItem(messages, false, true)
        adapter.notifyDataSetChanged()
    }

    /**
     * 更新消息状态
     */
    fun updateIMMessage(observeResponse: ObserveResponse) {
        val imMessage = observeResponse.data as IMMessage
        // 遍历找到并刷新
        for ((index, message) in messages.withIndex()) {
            if (message.uuid == imMessage.uuid) {
                Handler().postDelayed({
                    adapter.notifyItemChanged(index)
                }, 250)
            }
        }
    }

    /**
     * 长按消息列表
     */
    override fun onLongClick(v: View, imMessage: IMMessage): Boolean {
        // 删除消息
//        deleteIMMessage(imMessage)
        // 消息撤回
        sendRevokeMessage(imMessage)
        return super.onLongClick(v, imMessage)
    }

    /**
     * 判断是不是当前聊天用户的消息
     */
    private fun isMyMessage(message: IMMessage): Boolean {
        return message.sessionType == sessionType && message.sessionId != null && message.sessionId == contactId
    }

    /**
     * 删除消息
     */
    private fun deleteItem(messageItem: IMMessage, isRelocateTime: Boolean): Int {
        MessageManager.deleteChattingHistory(messageItem)
        var index = 0
        for (item in messages) {
            if (item.isTheSame(messageItem)) {
                break
            }
            ++index
        }
        if (index < messages.size) {
            messages.removeAt(index)
            if (isRelocateTime) {
                adapter.relocateShowTimeItemAfterDelete(messageItem, index, messages)
            }
        }
        return index
    }

    /**
     * 被对方拉入黑名单后，发消息失败的交互处理
     */
    fun sendFailWithBlackList(content: String) {
        MessageManager.sendTipMessage(contactId, content)
    }

    /**
     * 消息主动撤回
     */
    private fun sendRevokeMessage(imMessage: IMMessage) {
        MessageManager.revokeMessage(imMessage, object : RequestCallback<Void> {
            override fun onSuccess(param: Void?) {
                deleteItem(imMessage, false)
                val revokeNick = if (imMessage.fromAccount == SPUtils.getInstance().getString(CommonParams.SP_UNAME)) "你" else "对方"
                MessageManager.sendRevokeMessage(imMessage, revokeNick + "撤回了一条消息")
            }

            override fun onFailed(code: Int) {
                if (code == ResponseCode.RES_OVERDUE.toInt()) {
                    Toast.makeText(Utils.getApp(), "发送时间超过2分钟的消息，不能被撤回", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(Utils.getApp(), "消息撤回出错", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onException(exception: Throwable?) {

            }
        })
    }

    /**
     * 对方撤回消息
     */
    fun receiverRevokeMessage(notification: RevokeMsgNotification) {
        if (notification.message.sessionId == contactId) {
            deleteItem(notification.message, false)
            MessageManager.sendRevokeMessage(notification.message, "对方撤回了一条消息")
        }
    }

    /**
     * 收到已读回执
     */
    fun receiverMsgReceipt() {
        adapter.notifyDataSetChanged()
    }

    /**
     * 发送已读回执
     */
    fun sendMsgReceipt() {
        if (sessionType !== SessionTypeEnum.P2P) {
            return
        }
        val message = getLastReceivedMessage()
        if (message != null) {
            MessageManager.sendReceipt(contactId, message)
        }
    }

    /**
     * 得到最后一条要发送回执的信息
     */
    private fun getLastReceivedMessage(): IMMessage? {
        var lastMessage: IMMessage? = null
        for (i in messages.indices.reversed()) {
            if (sendReceiptCheck(messages[i])) {
                lastMessage = messages[i]
                break
            }
        }
        return lastMessage
    }

    /**
     * 非收到的消息，Tip消息和通知类消息，不要发已读回执
     */
    private fun sendReceiptCheck(msg: IMMessage): Boolean {
        return !(msg.direct != MsgDirectionEnum.In ||
                msg.msgType == MsgTypeEnum.tip ||
                msg.msgType == MsgTypeEnum.notification)
    }

    private fun sortMessages(list: List<IMMessage>) {
        if (!list.isEmpty()) {
            Collections.sort(list, comp)
        }
    }

    private val comp = Comparator<IMMessage> { o1, o2 ->
        val time = o1!!.time - o2!!.time
        if (time == 0L) 0 else if (time < 0) -1 else 1
    }

    /**
     * 发送“正在输入”通知
     */
    fun sendTypingCommand() {
        if (sessionType === SessionTypeEnum.Team || sessionType === SessionTypeEnum.ChatRoom) {
            return
        }
        // 每5s发出一次
        if (System.currentTimeMillis() - typingTime > 5000L) {
            typingTime = System.currentTimeMillis()
            val command = CustomNotification()
            command.sessionId = contactId
            command.sessionType = sessionType
            val config = CustomNotificationConfig()
            config.enablePush = false
            config.enableUnreadCount = false
            command.config = config
            val json = JSONObject()
            json.put(CommonParams.TYPE, CommonParams.COMMAND_INPUT)
            command.content = json.toString()

            NIMClient.getService(MsgService::class.java).sendCustomNotification(command)
        }
    }
}