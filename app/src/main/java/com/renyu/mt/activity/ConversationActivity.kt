package com.renyu.mt.activity

import android.content.Intent
import android.widget.LinearLayout
import android.widget.TextView
import com.renyu.tmuilibrary.R
import com.renyu.tmuilibrary.activity.BaseConversationActivity
import com.renyu.tmuilibrary.activity.UserDetailActivity

class ConversationActivity : BaseConversationActivity() {

    var layout_nav_right: LinearLayout? = null

    override fun initParams() {
        super.initParams()

        val textView = TextView(this@ConversationActivity)
        textView.text = "个人信息"
        textView.setOnClickListener { _ ->
            val intent = Intent(this@ConversationActivity, UserDetailActivity::class.java)
            intent.putExtra("UserId", chatUserId)
            startActivity(intent)
        }
        layout_nav_right = findViewById(R.id.layout_nav_right)
        layout_nav_right?.addView(textView)

    }
}