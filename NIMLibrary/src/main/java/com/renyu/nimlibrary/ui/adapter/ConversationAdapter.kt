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

    fun modifyShowTime(position: Int, viewDataBinding: ViewDataBinding) {
        if (position == 0) {
            viewDataBinding.root.findViewById<TextView>(R.id.aurora_tv_msgitem_date).visibility = View.VISIBLE
        }
        else {
            if (messages[position].time - messages[position-1].time > 1000 * 60 * 5) {
                viewDataBinding.root.findViewById<TextView>(R.id.aurora_tv_msgitem_date).visibility = View.VISIBLE
            }
            else {
                viewDataBinding.root.findViewById<TextView>(R.id.aurora_tv_msgitem_date).visibility = View.GONE
            }
        }
    }
}