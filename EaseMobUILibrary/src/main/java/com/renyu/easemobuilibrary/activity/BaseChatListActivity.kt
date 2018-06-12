package com.renyu.easemobuilibrary.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.renyu.easemobuilibrary.model.BroadcastBean
import com.renyu.easemobuilibrary.base.BaseIMActivity
import com.renyu.easemobuilibrary.fragment.ChatListFragment
import com.renyu.easemobuilibrary.params.CommonParams

abstract class BaseChatListActivity : BaseIMActivity() {
    @JvmField var conversationFragment: ChatListFragment? = null

    override fun initParams() {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == actionName) {
                    // 收到消息刷新列表
                    if (intent?.getSerializableExtra(BroadcastBean.COMMAND) == BroadcastBean.EaseMobCommand.MessageReceive ||
                            intent?.getSerializableExtra(BroadcastBean.COMMAND) == BroadcastBean.EaseMobCommand.MessageSend) {
                        conversationFragment?.refresh()
                    }
                    // 消息已读
                    if (intent?.getSerializableExtra(BroadcastBean.COMMAND) == BroadcastBean.EaseMobCommand.UpdateRead) {
                        conversationFragment?.refresh()
                    }
                    // 被踢下线
                    if (intent?.getSerializableExtra(BroadcastBean.COMMAND) == BroadcastBean.EaseMobCommand.Kickout) {
                        CommonParams.isKickout = true
                        if (!isPause) {
                            kickout()
                        }
                    }
                }
            }
        }

        openCurrentReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()

        closeCurrentReceiver()
    }
}