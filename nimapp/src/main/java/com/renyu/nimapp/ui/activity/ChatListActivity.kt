package com.renyu.nimapp.ui.activity

import android.content.Intent
import android.os.Bundle
import com.renyu.nimapp.R
import com.renyu.nimlibrary.ui.activity.BaseChatListActivity
import com.renyu.nimlibrary.ui.fragment.ChatListFragment

class ChatListActivity : BaseChatListActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatlist)

        conversationFragment = ChatListFragment()
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.layout_chatlistframe, conversationFragment, "conversationFragment")
                .commitAllowingStateLoss()

//        Repos.sendTextMessage("r17171708", "Hello2")
//        Repos.queryMessageListEx(MessageBuilder.createEmptyMessage("r17171708", SessionTypeEnum.P2P, 0))
    }

    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addCategory(Intent.CATEGORY_HOME)
        startActivity(intent)
    }
}