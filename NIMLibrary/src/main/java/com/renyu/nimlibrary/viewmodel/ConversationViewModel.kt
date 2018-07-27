package com.renyu.nimlibrary.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.view.View
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.renyu.nimapp.bean.Resource
import com.renyu.nimlibrary.bean.ObserveResponse
import com.renyu.nimlibrary.bean.ObserveResponseType
import com.renyu.nimlibrary.binding.EventImpl
import com.renyu.nimlibrary.manager.MessageManager
import com.renyu.nimlibrary.repository.Repos
import com.renyu.nimlibrary.ui.adapter.ConversationAdapter
import com.renyu.nimlibrary.util.RxBus
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.*
import kotlin.collections.ArrayList

class ConversationViewModel(private val contactId: String, private val sessionType: SessionTypeEnum) : BaseViewModel(), EventImpl {

    private val messages: ArrayList<IMMessage> by lazy {
        ArrayList<IMMessage>()
    }

    // 接口请求数据
    private val messageListReqeuestBefore: MutableLiveData<IMMessage> = MutableLiveData()
    var messageListResponseBefore: LiveData<Resource<List<IMMessage>>>? = null

    private val messageListReqeuestAfter: MutableLiveData<IMMessage> = MutableLiveData()
    var messageListResponseAfter: LiveData<Resource<List<IMMessage>>>? = null

    val adapter: ConversationAdapter by lazy {
        ConversationAdapter(messages, this)
    }

    // 重写发送的数据集合
    val resendList: MutableList<String> by lazy {
        mutableListOf<String>()
    }

    init {
        messageListResponseBefore = Transformations.switchMap(messageListReqeuestBefore) {
            if (it == null) {
                MutableLiveData<Resource<List<IMMessage>>>()
            }
            else {
                Repos.queryMessageListExBefore(it)
            }
        }

        messageListResponseAfter = Transformations.switchMap(messageListReqeuestAfter) {
            if (it == null) {
                MutableLiveData<Resource<List<IMMessage>>>()
            }
            else {
                Repos.queryMessageListExAfter(it)
            }
        }

        compositeDisposable.add(RxBus.getDefault()
                .toObservable(ObserveResponse::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    // 添加发出的消息状态监听
                    if (it.type == ObserveResponseType.MsgStatus) {
                        updateIMMessage(it.data as IMMessage)
                    }
                }
                .subscribe())
    }

    /**
     * 向前获取会话详情列表
     */
    fun queryMessageListsBefore(imMessage: IMMessage?) {
        var temp = imMessage
        if (imMessage == null) {
            temp = MessageBuilder.createEmptyMessage(contactId, sessionType, 0)
        }
        messageListReqeuestBefore.value = temp
    }

    /**
     * 向后获取会话详情列表
     */
    fun queryMessageListsAfter() {
        messageListReqeuestAfter.value = messages[messages.size-1]
    }

    /**
     * 向前添加旧的消息数据
     */
    fun addOldIMMessages(imMessages: List<IMMessage>) {
        messages.addAll(0, imMessages)
        adapter.notifyItemRangeInserted(0, imMessages.size)
    }

    /**
     * 向后添加新消息数据
     */
    fun addNewIMMessages(imMessages: List<IMMessage>) {
        messages.addAll(imMessages)
        if (imMessages.size == 1) {
            adapter.notifyItemInserted(messages.size - 1)
        }
        else {
            sortMessages(messages)
            adapter.notifyDataSetChanged()
        }
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
            addNewIMMessages(temp)
        }
    }

    /**
     * 加载更多消息
     */
    fun loadMoreLocalMessage() {
        if (messages.size != 0) {
            queryMessageListsBefore(messages[0])
        }
    }

    /**
     * 修改消息状态
     */
    private fun updateIMMessage(imMessage: IMMessage) {
        messages.filter {
            it.uuid == imMessage.uuid
        }.forEach {
            it.status = imMessage.status
        }
        // 重发需要调整数据集
        if (resendList.contains(imMessage.uuid)) {
            sortMessages(messages)
            adapter.notifyDataSetChanged()
        }
        else {
            messages.forEachIndexed { index, temp ->
                if (temp.uuid == imMessage.uuid) {
                    adapter.notifyItemChanged(index)
                }
            }
        }
    }

    /**
     * 重新发送消息
     */
    override fun resendIMMessage(view: View, uuid: String) {
        super.resendIMMessage(view, uuid)
        messages.forEachIndexed { index, imMessage ->
            if (imMessage.uuid == uuid) {
                imMessage.status = MsgStatusEnum.sending
                adapter.notifyItemChanged(index)

                resendList.add(uuid)

                MessageManager.reSendTextMessage(imMessage)
            }
        }
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
     * 判断是不是当前聊天用户的消息
     */
    private fun isMyMessage(message: IMMessage): Boolean {
        return message.sessionType == sessionType && message.sessionId != null && message.sessionId == contactId
    }
}