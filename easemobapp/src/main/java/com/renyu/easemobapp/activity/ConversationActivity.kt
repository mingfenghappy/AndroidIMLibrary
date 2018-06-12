package com.renyu.easemobapp.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.widget.LinearLayout
import android.widget.TextView
import com.renyu.commonlibrary.impl.OnPermissionCheckedImpl
import com.renyu.commonlibrary.views.permission.PermissionActivity
import com.renyu.easemobapp.R
import com.renyu.easemobuilibrary.activity.BaseConversationActivity
import com.renyu.easemobuilibrary.activity.UserDetailActivity

/**
 * 涉及到会话详情的可以参考此类的写法
 */
class ConversationActivity : BaseConversationActivity() {

    var layout_nav_right: LinearLayout? = null

    var permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE)

    override fun initParams() {
        super.initParams()

        val textView = TextView(this@ConversationActivity)
        textView.text = "个人信息"
        textView.setOnClickListener { _ ->
            val intent = Intent(this@ConversationActivity, UserDetailActivity::class.java)
            intent.putExtra("UserId", chatUserId)
            startActivityForResult(intent, 100)
        }
        layout_nav_right = findViewById(R.id.layout_nav_right)
        layout_nav_right?.addView(textView)

        if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
            PermissionActivity.gotoActivity(this, permissions, "请授予SD卡读写权限与录音权限", object : OnPermissionCheckedImpl {
                override fun denied() {
                    finish()
                }

                override fun grant() {

                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            finish()
        }
    }
}