package com.renyu.nimapp.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.renyu.nimapp.R
import com.renyu.nimlibrary.ui.fragment.ConversationFragment

class ConversationActivity : AppCompatActivity(), ConversationFragment.ChangeTipListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        val fragment = ConversationFragment.getInstance(intent.getStringExtra("contactId"),
                intent.getBooleanExtra("isGroup", false))

        supportFragmentManager.beginTransaction()
                .replace(R.id.layout_conversation, fragment)
                .commitAllowingStateLoss()
    }

    override fun titleChange(reset: Boolean) {
        if (reset) {
            Log.d("NIM_APP", "恢复")
        }
        else {
            Log.d("NIM_APP", "正在输入...")
        }
    }
}