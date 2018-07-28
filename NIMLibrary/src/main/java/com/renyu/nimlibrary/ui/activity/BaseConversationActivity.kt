package com.renyu.nimlibrary.ui.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.renyu.nimapp.bean.Status
import com.renyu.nimlibrary.R
import com.renyu.nimlibrary.bean.ObserveResponse
import com.renyu.nimlibrary.bean.ObserveResponseType
import com.renyu.nimlibrary.databinding.ActivityBaseConversationBinding
import com.renyu.nimlibrary.manager.MessageManager
import com.renyu.nimlibrary.util.RxBus
import com.renyu.nimlibrary.viewmodel.ConversationViewModel
import com.renyu.nimlibrary.viewmodel.ConversationViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_base_conversation.*


open class BaseConversationActivity : AppCompatActivity() {

    var viewDataBinding: ActivityBaseConversationBinding? = null
    var vm: ConversationViewModel? = null

    var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置当前正在聊天的对象
        MessageManager.setChattingAccount(intent.getStringExtra("contactId"),
                if (intent.getBooleanExtra("isGroup", false)) SessionTypeEnum.Team else SessionTypeEnum.P2P)

        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_base_conversation)
        viewDataBinding.also {
            vm = ViewModelProviders.of(this,
                    ConversationViewModelFactory(intent.getStringExtra("contactId"),
                            if (intent.getBooleanExtra("isGroup", false)) SessionTypeEnum.Team else SessionTypeEnum.P2P))
                    .get(ConversationViewModel::class.java)
            vm?.messageListResponseBefore?.observe(this, Observer {
                when(it?.status) {
                    Status.SUCESS -> {
                        val needScrollToEnd = rv_conversation.adapter.itemCount == 0
                        vm?.addOldIMMessages(it.data!!)
                        // 首次加载完成滚动到最底部
                        if (needScrollToEnd) {
                            rv_conversation.scrollToPosition(rv_conversation.adapter.itemCount - 1)
                        }

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
                        vm?.loadMoreLocalMessage()
                    }
                }
            })

            // 获取会话列表数据
            Handler().postDelayed({
                vm?.queryMessageLists(null)
            }, 250)

            disposable = RxBus.getDefault()
                    .toObservable(ObserveResponse::class.java)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext {
                        // 处理接收到的新消息
                        if (it.type == ObserveResponseType.ReceiveMessage) {
                            // 如果当前消息是最后一条的话就自动滚动到最底部
                            val isLast = isLastMessageVisible()
                            vm?.receiveIMMessages(it)
                            if (isLast) {
                                rv_conversation.smoothScrollToPosition(rv_conversation.adapter.itemCount - 1)
                            }
                        }
                    }
                    .subscribe()

            Handler().postDelayed({
                vm?.addItem(MessageManager.sendTextMessage(intent.getStringExtra("contactId"), "Hello26"))
                rv_conversation.smoothScrollToPosition(rv_conversation.adapter.itemCount - 1)
            }, 5000)
        }
    }

    /**
     * 判断当前显示的是不是最后一条
     */
    private fun isLastMessageVisible(): Boolean {
        val layoutManager = rv_conversation.layoutManager as LinearLayoutManager
        val lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()
        return lastVisiblePosition >= vm?.adapter?.itemCount!! - 1
    }

    override fun onDestroy() {
        super.onDestroy()

        disposable?.dispose()
    }
}