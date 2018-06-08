package com.renyu.easemobapp.activity

import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.renyu.easemobapp.R
import com.renyu.easemobuilibrary.activity.BaseChatListActivity
import com.renyu.easemobuilibrary.fragment.ChatListFragment

class ChatListActivity : BaseChatListActivity(), ChatListFragment.OnHeaderViewSetListener {
    override fun setStatusBarColor() = Color.BLACK

    override fun setStatusBarTranslucent() = 0

    override fun initParams() {
        // 注意这里有调用父类的方法
        super.initParams()

        val textView = TextView(this)
        textView.text = "联系人"
        textView.setOnClickListener { _ ->
            //            val intent = Intent(this, FriendListActivity::class.java)
//            startActivity(intent)
        }
        findViewById<LinearLayout>(R.id.layout_nav_right).addView(textView)

        conversationFragment = ChatListFragment()
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.layout_chatlistframe, conversationFragment, "conversationFragment")
                .commitAllowingStateLoss()
    }

    override fun loadData() {

    }

    override fun initViews() = R.layout.activity_chatlist

    override fun getHeadView(): View? {
        return null
    }

    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addCategory(Intent.CATEGORY_HOME)
        startActivity(intent)
    }
}
