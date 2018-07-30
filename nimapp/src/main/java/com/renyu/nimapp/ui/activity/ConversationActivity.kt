package com.renyu.nimapp.ui.activity

import android.util.Log
import com.renyu.nimlibrary.ui.activity.BaseConversationActivity

class ConversationActivity : BaseConversationActivity() {
    override fun titleChange(reset: Boolean) {
        if (reset) {
            Log.d("NIM_APP", "恢复")
        }
        else {
            Log.d("NIM_APP", "正在输入...")
        }
    }
}