package com.renyu.nimlibrary.binding

import android.databinding.BindingAdapter
import android.net.Uri
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.blankj.utilcode.util.SizeUtils
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.span.SimpleDraweeSpanTextView
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.uinfo.UserService
import com.renyu.nimlibrary.util.emoji.EmojiUtils
import java.io.File

object BindingAdapters {
    @JvmStatic
    @BindingAdapter(value = ["adapter"])
    fun <T: RecyclerView.ViewHolder> setAdapter(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<T>) {
        recyclerView.setHasFixedSize(true)
        val manager = LinearLayoutManager(recyclerView.context)
        manager.isSmoothScrollbarEnabled = true
        manager.isAutoMeasureEnabled = true
        recyclerView.layoutManager = manager
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.adapter = adapter
    }

    @JvmStatic
    @BindingAdapter(value = ["avatarImage"])
    fun loadAvatarImage(simpleDraweeView: SimpleDraweeView, account: String) {
        val userInfo = NIMClient.getService(UserService::class.java).getUserInfo(account)
        if (userInfo != null) {
            if (simpleDraweeView.tag !=null &&
                    !TextUtils.isEmpty(simpleDraweeView.tag.toString()) &&
                    simpleDraweeView.tag.toString() == userInfo.avatar) {
                // 什么都不做，防止Fresco闪烁
            }
            else {
                val request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(userInfo.avatar))
                        .setResizeOptions(ResizeOptions(SizeUtils.dp2px(40f), SizeUtils.dp2px(40f))).build()
                val draweeController = Fresco.newDraweeControllerBuilder()
                        .setImageRequest(request).setAutoPlayAnimations(true).build()
                simpleDraweeView.controller = draweeController
                simpleDraweeView.tag = userInfo.avatar
            }
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["showUnreadNum"])
    fun loadUnreadNum(textView: TextView, count: Int) {
        textView.visibility = if (count>0) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["chatName"])
    fun loadChatName(textView: TextView, contactId: String) {
        val userInfo = NIMClient.getService(UserService::class.java).getUserInfo(contactId)
        if (userInfo != null) {
            textView.text = userInfo.name
        }
        else {
            textView.text = contactId
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["emojiText"])
    fun loadEmojiText(textView: SimpleDraweeSpanTextView, text: String) {
        EmojiUtils.replaceFaceMsgByFresco(textView, text)
    }

    @JvmStatic
    @BindingAdapter(value = ["cvListPbStatue"])
    fun changeConversationListProgressStatue(progressBar: ProgressBar, statue: Int) {
        if (statue == MsgStatusEnum.sending.value) {
            // 发送中
            progressBar.visibility = View.VISIBLE
        }
        else {
            progressBar.visibility = View.GONE
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["cvListIvStatue"])
    fun changeConversationListImageStatue(imageView: ImageView, statue: Int) {
        if (statue == MsgStatusEnum.fail.value) {
            // 发送失败
            imageView.visibility = View.VISIBLE
        }
        else {
            imageView.visibility = View.GONE
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["cvListAudiolength"])
    fun changeConversationListAudiolength(textView: TextView, imMessage: IMMessage) {
        val duration = (imMessage.attachment as AudioAttachment).duration / 1000
        textView.text = "$duration\'\'"
    }

    @JvmStatic
    @BindingAdapter(value = ["read"])
    fun changeReadedVisibility(textView: TextView, imMessage: IMMessage) {
        if (imMessage.sessionType == SessionTypeEnum.P2P
                && imMessage.direct == MsgDirectionEnum.Out
                && imMessage.msgType != MsgTypeEnum.tip
                && imMessage.msgType != MsgTypeEnum.notification
                && imMessage.isRemoteRead) {
            textView.visibility = View.VISIBLE
        }
        else {
            textView.visibility = View.GONE
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["audioRead"])
    fun changeConversationListAudioRead(imageView: ImageView, msgStatusEnum: MsgStatusEnum) {
        if (msgStatusEnum == MsgStatusEnum.success) {
            imageView.visibility = View.VISIBLE
        }
        else {
            imageView.visibility = View.GONE
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["cvListImageUrl"])
    fun loadChatListImageUrl(simpleDraweeView: SimpleDraweeView, imMessage: IMMessage) {
        val imageUrl = if ((imMessage.attachment as ImageAttachment).path == null) {
            if ((imMessage.attachment as ImageAttachment).thumbUrl == null) {
                ""
            }
            else {
                (imMessage.attachment as ImageAttachment).thumbUrl
            }
        }
        else {
            val file = File((imMessage.attachment as ImageAttachment).path)
            if (file.exists()) {
                "file://"+(imMessage.attachment as ImageAttachment).path
            }
            else {
                ""
            }
        }
        if (simpleDraweeView.tag !=null &&
                !TextUtils.isEmpty(simpleDraweeView.tag.toString()) &&
                simpleDraweeView.tag.toString() == imageUrl) {
            // 什么都不做，防止Fresco闪烁
        }
        else {
            val request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(imageUrl))
                    .setResizeOptions(ResizeOptions(SizeUtils.dp2px(123f), SizeUtils.dp2px(115f))).build()
            val draweeController = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request).setAutoPlayAnimations(true).build()
            simpleDraweeView.controller = draweeController
            simpleDraweeView.tag = imageUrl
        }
    }
}