package com.renyu.nimlibrary.ui.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.renyu.nimapp.bean.Status
import com.renyu.nimlibrary.R
import com.renyu.nimlibrary.databinding.ActivityBaseConversationBinding
import com.renyu.nimlibrary.manager.MessageManager
import com.renyu.nimlibrary.viewmodel.ConversationViewModel
import kotlinx.android.synthetic.main.activity_base_conversation.*

open class BaseConversationActivity : AppCompatActivity() {

    var viewDataBinding: ActivityBaseConversationBinding? = null
    var vm: ConversationViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置当前正在聊天的对象
        MessageManager.setChattingAccount(intent.getStringExtra("contactId"),
                if (intent.getBooleanExtra("isGroup", false)) SessionTypeEnum.Team else SessionTypeEnum.P2P)

        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_base_conversation)
        viewDataBinding.also {
            vm = ViewModelProviders.of(this).get(ConversationViewModel::class.java)
            vm?.messageListResponseBefore?.observe(this, Observer {
                when(it?.status) {
                    Status.SUCESS -> {
                        vm?.addOldIMMessages(it.data!!)
                        // 首次加载完成滚动到最底部
                        rv_conversation.scrollToPosition(rv_conversation.adapter.itemCount - 1)
                    }
                    Status.FAIL -> {

                    }
                    Status.LOADING -> {

                    }
                    Status.Exception -> {

                    }
                }
            })
            vm?.messageListResponseAfter?.observe(this, Observer {
                when(it?.status) {
                    Status.SUCESS -> {
                        it.data?.forEach {
                            Log.d("NIM_APP", "发送数据：${it.content} 状态： ${it.status}")
                        }
                        vm?.addNewIMMessages(it.data!!)
                        // 发送完成滚动到最底部
                        rv_conversation.smoothScrollToPosition(rv_conversation.adapter.itemCount - 1)
                    }
                    Status.FAIL -> {

                    }
                    Status.LOADING -> {

                    }
                    Status.Exception -> {

                    }
                }
            })

            viewDataBinding?.adapter = vm?.adapter

            rv_conversation.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    // 上拉加载更多
                    val canScrollDown = rv_conversation.canScrollVertically(-1)
                    if (!canScrollDown) {
                        //                                        loadMoreLocalMessage()
                    }
                }
            })

            Handler().postDelayed({
                vm?.queryMessageListsBefore(MessageBuilder.createEmptyMessage(
                        intent.getStringExtra("contactId"),
                        if (intent.getBooleanExtra("isGroup", false)) SessionTypeEnum.Team else SessionTypeEnum.P2P,
                        0))
            }, 250)

            Handler().postDelayed({
                MessageManager.sendTextMessage(intent.getStringExtra("contactId"), "Hello12")
                vm?.queryMessageListsAfter()
            }, 5000)
        }
    }
}