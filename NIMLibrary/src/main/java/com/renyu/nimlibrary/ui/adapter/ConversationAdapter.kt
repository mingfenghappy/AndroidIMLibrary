package com.renyu.nimlibrary.ui.adapter

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.databinding.library.baseAdapters.BR
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.renyu.nimlibrary.R
import com.renyu.nimlibrary.binding.EventImpl
import com.renyu.nimlibrary.databinding.AdapterReceiveTextBinding
import com.renyu.nimlibrary.databinding.AdapterSendTextBinding

class ConversationAdapter(private val messages: ArrayList<IMMessage>, private val eventImpl: EventImpl) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when(viewType) {
            0 -> return ReceiverTextViewHolder(
                        DataBindingUtil.inflate<AdapterReceiveTextBinding>(
                                LayoutInflater.from(parent.context),
                                R.layout.adapter_receive_text, parent,
                                false))
            1 -> return SendTextViewHolder(
                    DataBindingUtil.inflate<AdapterSendTextBinding>(
                            LayoutInflater.from(parent.context),
                            R.layout.adapter_send_text, parent,
                            false))
        }

        throw Throwable("对指定viewType类型缺少判断")
    }

    override fun getItemCount() = messages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(holder.layoutPosition)) {
            0 -> {
                (holder as ReceiverTextViewHolder).receiverTvDataBinding.setVariable(BR.iMMessage, messages[holder.layoutPosition])
                holder.receiverTvDataBinding.setVariable(BR.eventImpl, eventImpl)
                holder.receiverTvDataBinding.executePendingBindings()
                // 调整时间显示
                modifyShowTime(holder.layoutPosition, holder.receiverTvDataBinding)
            }
            1 -> {
                (holder as SendTextViewHolder).sendTvDataBinding.setVariable(BR.iMMessage, messages[holder.layoutPosition])
                holder.sendTvDataBinding.setVariable(BR.eventImpl, eventImpl)
                holder.sendTvDataBinding.executePendingBindings()
                // 调整时间显示
                modifyShowTime(holder.layoutPosition, holder.sendTvDataBinding)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        // 接收文字消息
        if (messages[position].msgType == MsgTypeEnum.text && messages[position].direct == MsgDirectionEnum.In) {
            return 0
        }
        // 发送文字消息
        else if (messages[position].msgType == MsgTypeEnum.text && messages[position].direct == MsgDirectionEnum.Out) {
            return 1
        }
        // 接收图片消息
        if (messages[position].msgType == MsgTypeEnum.image && messages[position].direct == MsgDirectionEnum.In) {

        }
        // 发送图片消息
        else if (messages[position].msgType == MsgTypeEnum.image && messages[position].direct == MsgDirectionEnum.Out) {

        }
        // 接收音频消息
        if (messages[position].msgType == MsgTypeEnum.audio && messages[position].direct == MsgDirectionEnum.In) {

        }
        // 发送音频消息
        else if (messages[position].msgType == MsgTypeEnum.audio && messages[position].direct == MsgDirectionEnum.Out) {

        }
        // 接收视频消息
        if (messages[position].msgType == MsgTypeEnum.video && messages[position].direct == MsgDirectionEnum.In) {

        }
        // 发送视频消息
        else if (messages[position].msgType == MsgTypeEnum.video && messages[position].direct == MsgDirectionEnum.Out) {

        }
        // 接收位置消息
        if (messages[position].msgType == MsgTypeEnum.location && messages[position].direct == MsgDirectionEnum.In) {

        }
        // 发送位置消息
        else if (messages[position].msgType == MsgTypeEnum.location && messages[position].direct == MsgDirectionEnum.Out) {

        }
        // 接收文件消息
        if (messages[position].msgType == MsgTypeEnum.file && messages[position].direct == MsgDirectionEnum.In) {

        }
        // 发送文件消息
        else if (messages[position].msgType == MsgTypeEnum.file && messages[position].direct == MsgDirectionEnum.Out) {

        }
        // 接收音视频消息
        if (messages[position].msgType == MsgTypeEnum.avchat && messages[position].direct == MsgDirectionEnum.In) {

        }
        // 发送音视频消息
        else if (messages[position].msgType == MsgTypeEnum.avchat && messages[position].direct == MsgDirectionEnum.Out) {

        }
        // 接收通知消息
        if (messages[position].msgType == MsgTypeEnum.notification && messages[position].direct == MsgDirectionEnum.In) {

        }
        // 发送通知消息
        else if (messages[position].msgType == MsgTypeEnum.notification && messages[position].direct == MsgDirectionEnum.Out) {

        }
        // 接收提醒类型消息
        if (messages[position].msgType == MsgTypeEnum.tip && messages[position].direct == MsgDirectionEnum.In) {

        }
        // 发送提醒类型消息
        else if (messages[position].msgType == MsgTypeEnum.tip && messages[position].direct == MsgDirectionEnum.Out) {

        }
        // 接收自定义消息
        if (messages[position].msgType == MsgTypeEnum.custom && messages[position].direct == MsgDirectionEnum.In) {

        }
        // 发送自定义消息
        else if (messages[position].msgType == MsgTypeEnum.custom && messages[position].direct == MsgDirectionEnum.Out) {

        }
        return super.getItemViewType(position)
    }

    class ReceiverTextViewHolder(viewDataBinding: ViewDataBinding): RecyclerView.ViewHolder(viewDataBinding.root) {
        val receiverTvDataBinding = viewDataBinding
    }

    class SendTextViewHolder(viewDataBinding: ViewDataBinding): RecyclerView.ViewHolder(viewDataBinding.root) {
        val sendTvDataBinding = viewDataBinding
    }

    private fun modifyShowTime(position: Int, viewDataBinding: ViewDataBinding) {
        if (timedItems.contains(messages[position].uuid)) {
            viewDataBinding.root.findViewById<TextView>(R.id.aurora_tv_msgitem_date).visibility = View.VISIBLE
        }
        else {
            viewDataBinding.root.findViewById<TextView>(R.id.aurora_tv_msgitem_date).visibility = View.GONE
        }
    }

    // 需要显示消息时间的消息ID
    private val timedItems: MutableSet<String> by lazy {
        HashSet<String>()
    }
    // 用于消息时间显示,判断和上条消息间的时间间隔
    private var lastShowTimeItem: IMMessage? = null

    /**
     * 列表加入新消息时，更新时间显示
     */
    fun updateShowTimeItem(items: List<IMMessage>, fromStart: Boolean, update: Boolean) {
        var anchor = if (fromStart) null else lastShowTimeItem
        for (message in items) {
            if (setShowTimeFlag(message, anchor)) {
                anchor = message
            }
        }
        // 添加旧数据的时候无需刷新lastShowTimeItem
        if (update) {
            lastShowTimeItem = anchor
        }
    }

    /**
     * 是否需要显示时间
     */
    fun needShowTime(message: IMMessage): Boolean {
        return timedItems.contains(message.uuid)
    }

    /**
     * 设置显示时间列表
     */
    private fun setShowTime(message: IMMessage, show: Boolean) {
        if (show) {
            timedItems.add(message.uuid)
        } else {
            timedItems.remove(message.uuid)
        }
    }

    private fun setShowTimeFlag(message: IMMessage, anchor: IMMessage?): Boolean {
        var update = false
        // 添加第一条消息时间
        if (anchor == null) {
            setShowTime(message, true)
            update = true
        }
        else {
            val messageTime = message.time
            val anchorTime = anchor.time
            if (messageTime - anchorTime == 0.toLong()) {
                // 消息撤回时使用
                setShowTime(message, true)
                lastShowTimeItem = message
                update = true
            }
            // 时间间隔小于5分钟，则添加
            else if (messageTime - anchorTime < 5 * 60 * 1000) {
                // 去除之前可能已经存在的
                setShowTime(message, false)
            }
            else {
                setShowTime(message, true)
                update = true
            }
        }
        return update
    }

    /**
     * 由于消息被删除而重新调整显示的时间
     */
    fun relocateShowTimeItemAfterDelete(messageItem: IMMessage, index: Int, messages: ArrayList<IMMessage>) {
        if (needShowTime(messageItem)) {
            setShowTime(messageItem, false)
            if (messages.size > 0) {
                // 找到下一项
                val nextItem = if (index == messages.size) {
                    // 如果被删除的是最后一项
                    messages[(index - 1)]
                } else {
                    // 如果被删除的不是最后一项
                    messages[(index)]
                }
                // 如果被删的项显示了时间，需要继承
                setShowTime(nextItem, true)
                // 把nextItem作为时间判断起始点
                if (lastShowTimeItem == null || (lastShowTimeItem != null && lastShowTimeItem!!.isTheSame(messageItem))) {
                    lastShowTimeItem = nextItem
                }
            } else {
                lastShowTimeItem = null
            }
        }
    }
}