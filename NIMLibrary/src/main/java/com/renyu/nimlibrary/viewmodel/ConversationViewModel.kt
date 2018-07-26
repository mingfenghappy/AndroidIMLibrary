package com.renyu.nimlibrary.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.view.View
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum
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

class ConversationViewModel : BaseViewModel(), EventImpl {

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
    fun queryMessageListsBefore(immessage: IMMessage) {
        messageListReqeuestBefore.value = immessage
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
        adapter.notifyDataSetChanged()
    }

    /**
     * 向后添加新消息数据
     */
    fun addNewIMMessages(imMessages: List<IMMessage>) {
        messages.addAll(imMessages)
        adapter.notifyDataSetChanged()
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
        sortMessages(messages)
        adapter.notifyDataSetChanged()
    }

    /**
     * 重新发送消息
     */
    override fun resendIMMessage(view: View, uuid: String) {
        super.resendIMMessage(view, uuid)
        messages.filter {
            it.uuid == uuid
        }.forEach {
            it.status = MsgStatusEnum.sending
            adapter.notifyDataSetChanged()

            MessageManager.reSendTextMessage(it)
        }
    }

    private fun sortMessages(list: List<IMMessage>) {
        if (list.isEmpty()) {
            return
        }
        Collections.sort(list, comp)
    }

    private val comp = Comparator<IMMessage> { o1, o2 ->
        val time = o1!!.time - o2!!.time
        if (time == 0L) 0 else if (time < 0) -1 else 1
    }
}