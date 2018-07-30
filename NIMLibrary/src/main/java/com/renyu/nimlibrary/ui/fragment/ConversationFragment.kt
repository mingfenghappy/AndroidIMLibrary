package com.renyu.nimlibrary.ui.fragment

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import cn.dreamtobe.kpswitch.util.KPSwitchConflictUtil
import cn.dreamtobe.kpswitch.util.KeyboardUtil
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomNotification
import com.netease.nimlib.sdk.msg.model.RevokeMsgNotification
import com.renyu.nimapp.bean.Status
import com.renyu.nimlibrary.R
import com.renyu.nimlibrary.bean.ObserveResponse
import com.renyu.nimlibrary.bean.ObserveResponseType
import com.renyu.nimlibrary.databinding.FragmentConversationBinding
import com.renyu.nimlibrary.manager.MessageManager
import com.renyu.nimlibrary.params.CommonParams
import com.renyu.nimlibrary.util.RxBus
import com.renyu.nimlibrary.viewmodel.ConversationViewModel
import com.renyu.nimlibrary.viewmodel.ConversationViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_conversation.*
import org.json.JSONObject

/**
 * Created by Administrator on 2018/7/30.
 */
class ConversationFragment : Fragment() {

    companion object {
        fun getInstance(contactId: String, isGroup: Boolean): ConversationFragment {
            val fragment = ConversationFragment()
            val bundle = Bundle()
            bundle.putString("contactId", contactId)
            bundle.putBoolean("isGroup", isGroup)
            fragment.arguments = bundle
            return fragment
        }
    }

    private var viewDataBinding: FragmentConversationBinding? = null

    var vm: ConversationViewModel? = null

    private var disposable: Disposable? = null

    // 面板是否已经收起
    private var isExecuteCollapse = false

    // "正在输入"提示刷新使用
    val titleChangeHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
        }
    }
    private val titleChangeRunnable: Runnable = Runnable { changeTipListener?.titleChange(true) }

    private var changeTipListener: ChangeTipListener? = null
    // "正在输入"提示接口
    interface ChangeTipListener {
        fun titleChange(reset: Boolean)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        changeTipListener = context as ChangeTipListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_conversation, container, false)
        return viewDataBinding?.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewDataBinding.also {
            vm = ViewModelProviders.of(this,
                    ConversationViewModelFactory(arguments!!.getString("contactId"),
                            if (arguments!!.getBoolean("isGroup")) SessionTypeEnum.Team else SessionTypeEnum.P2P))
                    .get(ConversationViewModel::class.java)
            vm?.messageListResponseBefore?.observe(this, Observer {
                when(it?.status) {
                    Status.SUCESS -> {
                        val firstLoad = rv_conversation.adapter.itemCount == 0
                        vm?.addOldIMMessages(it.data!!)
                        if (firstLoad) {
                            // 首次加载完成发送消息已读回执
                            vm?.sendMsgReceipt()
                            // 首次加载完成滚动到最底部
                            rv_conversation.scrollToPosition(rv_conversation.adapter.itemCount - 1)
                        }
                        // 不是首次加载更新则显示最后加载的那一条
                        else {
                            val linearManager = rv_conversation.layoutManager as LinearLayoutManager
                            val firstItemPosition = linearManager.findFirstVisibleItemPosition()
                            if (firstItemPosition == 0) {
                                rv_conversation.scrollToPosition(it.data!!.size - 1)
                            }
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

            initUI()

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
                            // 发送消息已读回执
                            vm?.sendMsgReceipt()
                            // "正在输入"提示重置
                            titleChangeHandler.removeCallbacks(titleChangeRunnable)
                            changeTipListener?.titleChange(true)
                        }
                        // 添加发出的消息状态监听
                        if (it.type == ObserveResponseType.MsgStatus) {
                            vm?.updateIMMessage(it)
                        }
                        // 对方消息撤回
                        if (it.type == ObserveResponseType.RevokeMessage) {
                            vm?.receiverRevokeMessage(it.data as RevokeMsgNotification)
                        }
                        // 收到已读回执
                        if (it.type == ObserveResponseType.MessageReceipt) {
                            vm?.receiverMsgReceipt()
                        }
                        if (it.type == ObserveResponseType.CustomNotification) {
                            // 收到的自定义通知属于当前会话中的用户
                            if ((it.data as CustomNotification).sessionId == arguments!!.getString("contactId")) {
                                val content = (it.data as CustomNotification).content
                                val type = JSONObject(content).getString(CommonParams.TYPE)
                                if (type == CommonParams.COMMAND_INPUT) {
                                    changeTipListener?.titleChange(false)
                                    titleChangeHandler.postDelayed(titleChangeRunnable, 4000)
                                }
                            }
                        }
                    }
                    .subscribe()

            // 获取会话列表数据
            Handler().postDelayed({
                vm?.queryMessageLists(null)
            }, 250)

//            Handler().postDelayed({
//                vm?.sendIMMessage(MessageManager.sendTextMessage(intent.getStringExtra("contactId"), "Hello37"))
//                rv_conversation.smoothScrollToPosition(rv_conversation.adapter.itemCount - 1)
//            }, 5000)
        }
    }

    override fun onResume() {
        super.onResume()
        // 设置当前正在聊天的对象
        MessageManager.setChattingAccount(arguments!!.getString("contactId"),
                if (arguments!!.getBoolean("isGroup")) SessionTypeEnum.Team else SessionTypeEnum.P2P)
    }

    override fun onPause() {
        super.onPause()
        // 去除正在聊天的对象
        NIMClient.getService(MsgService::class.java).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_NONE,
                SessionTypeEnum.None)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        disposable?.dispose()
    }

    /**
     * 判断当前显示的是不是最后一条
     */
    private fun isLastMessageVisible(): Boolean {
        val layoutManager = rv_conversation.layoutManager as LinearLayoutManager
        val lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()
        return lastVisiblePosition >= vm?.adapter?.itemCount!! - 1
    }

    private fun initUI() {
        edit_conversation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (TextUtils.isEmpty(s.toString())) {
                    btn_send_conversation.setBackgroundColor(Color.parseColor("#dddee2"))
                } else {
                    btn_send_conversation.setBackgroundColor(Color.parseColor("#0099ff"))
                }
                // 发送"正在输入"提示
                vm?.sendTypingCommand()
            }
        })
        // 触摸到RecyclerView之后自动收起面板
        rv_conversation.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                isExecuteCollapse = false
            }
            if (motionEvent.action == MotionEvent.ACTION_MOVE) {
                if (!isExecuteCollapse) {
                    isExecuteCollapse = true
                    KPSwitchConflictUtil.hidePanelAndKeyboard(kp_panel_root)
                }
            }
            false
        }
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
        // ***********************************  JKeyboardPanelSwitch配置  ***********************************
        KeyboardUtil.attach(context as Activity, kp_panel_root) { isShowing ->
            if (isShowing) {
                rv_conversation.scrollToPosition(rv_conversation.adapter.itemCount - 1)
            }
        }
        KPSwitchConflictUtil.attach(kp_panel_root, edit_conversation, KPSwitchConflictUtil.SwitchClickListener { switchToPanel ->
            if (switchToPanel) {
                edit_conversation.clearFocus()
                rv_conversation.scrollToPosition(rv_conversation.adapter.itemCount - 1)
            } else {
                edit_conversation.requestFocus()
            }
        }, KPSwitchConflictUtil.SubPanelAndTrigger(layout_emojichoice, iv_emoji), KPSwitchConflictUtil.SubPanelAndTrigger(layout_voicechoice, iv_sendvoice))
        edit_conversation.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                KPSwitchConflictUtil.showKeyboard(kp_panel_root, edit_conversation)
            }
            false
        }
        kp_panel_root.post {
            val height = KeyboardUtil.getKeyboardHeight(context)
            val params = kp_panel_root.layoutParams as LinearLayout.LayoutParams
            params.height = height
            kp_panel_root.layoutParams = params
        }
    }
}