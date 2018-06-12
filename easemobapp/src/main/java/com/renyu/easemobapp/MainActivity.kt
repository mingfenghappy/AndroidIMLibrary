package com.renyu.easemobapp

import android.graphics.Color
import com.hyphenate.chat.EMClient
import com.renyu.commonlibrary.baseact.BaseActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    override fun setStatusBarColor() = Color.BLACK

    override fun setStatusBarTranslucent() = 0

    override fun loadData() {

    }

    override fun initParams() {
        btn_chattext.setOnClickListener {
//            EMMessageManager.sendSingleMessage(Utils.getApp(), EMMessageManager.prepareTxtEMMessage("from local", "admin"))
            EMClient.getInstance().contactManager().addContact("r17171708", "Hello")
        }
    }

    override fun initViews() = R.layout.activity_main
}