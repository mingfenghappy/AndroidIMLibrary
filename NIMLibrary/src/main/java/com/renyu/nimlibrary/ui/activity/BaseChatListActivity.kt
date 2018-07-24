package com.renyu.nimlibrary.ui.activity

import android.support.v7.app.AppCompatActivity
import com.renyu.nimlibrary.ui.fragment.ChatListFragment

abstract class BaseChatListActivity : AppCompatActivity() {
    @JvmField var conversationFragment: ChatListFragment? = null
}