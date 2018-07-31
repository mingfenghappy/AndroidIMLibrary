package com.renyu.nimapp.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.renyu.nimapp.R
import com.renyu.nimlibrary.ui.fragment.ConversationFragment
import java.io.File

class ConversationActivity : AppCompatActivity(), ConversationFragment.ConversationListener {
    private var conversationFragment: ConversationFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        conversationFragment = ConversationFragment.getInstance(intent.getStringExtra("contactId"),
                intent.getBooleanExtra("isGroup", false))

        supportFragmentManager.beginTransaction()
                .replace(R.id.layout_conversation, conversationFragment)
                .commitAllowingStateLoss()
    }

    /**
     * "正在输入"
     */
    override fun titleChange(reset: Boolean) {
        if (reset) {
            Log.d("NIM_APP", "恢复")
        }
        else {
            Log.d("NIM_APP", "正在输入...")
        }
    }

    /**
     * 拍照
     */
    override fun takePhoto() {
        val intent = Intent(this, TakePhotoActivity::class.java)
        intent.putExtra("key", "value")
        startActivityForResult(intent, 1000)
    }

    /**
     * 选择相册
     */
    override fun pickPhoto() {
        val intent = Intent(this, PickPhotoActivity::class.java)
        intent.putExtra("key", "value")
        startActivityForResult(intent, 1000)
    }

    /**
     * 浏览大图
     */
    override fun showBigImage(images: ArrayList<String>, index: Int) {
        val intent = Intent(this, PickPhotoActivity::class.java)
        intent.putExtra("images", images)
        intent.putExtra("index", index)
        startActivityForResult(intent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            // 回调fragment去发送图片
            conversationFragment?.sendImageFile(File(data?.getStringExtra("fileName")))
        }
    }
}