package com.renyu.easemobuilibrary.activity

import com.renyu.easemobuilibrary.base.BaseIMActivity
import com.renyu.easemobuilibrary.fragment.ChatListFragment

abstract class BaseChatListActivity : BaseIMActivity() {
    @JvmField var conversationFragment: ChatListFragment? = null

    override fun initParams() {
        openCurrentReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()

        closeCurrentReceiver()
    }
}