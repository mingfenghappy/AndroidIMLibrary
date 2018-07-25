package com.renyu.nimlibrary.binding

import android.databinding.BindingAdapter
import android.net.Uri
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.blankj.utilcode.util.SizeUtils
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.uinfo.UserService

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
    @BindingAdapter(value = ["chatListAvatar"])
    fun loadChatListAvatar(simpleDraweeView: SimpleDraweeView, account: String) {
        val userInfo = NIMClient.getService(UserService::class.java).getUserInfo(account)
        if (userInfo != null) {
            val request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(userInfo.avatar))
                    .setResizeOptions(ResizeOptions(SizeUtils.dp2px(40f), SizeUtils.dp2px(40f))).build()
            val draweeController = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request).setAutoPlayAnimations(true).build()
            simpleDraweeView.controller = draweeController
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["tvVisible"])
    fun changeVisible(textView: TextView, count: Int) {
        textView.visibility = if (count>0) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["chatListName"])
    fun loadChatListName(textView: TextView, contactId: String) {
        val userInfo = NIMClient.getService(UserService::class.java).getUserInfo(contactId)
        if (userInfo != null) {
            textView.text = userInfo.name
        }
        else {
            textView.text = contactId
        }
    }
}