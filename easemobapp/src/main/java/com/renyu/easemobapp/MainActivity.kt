package com.renyu.easemobapp

import android.graphics.Color
import com.blankj.utilcode.util.Utils
import com.renyu.commonlibrary.baseact.BaseActivity
import com.renyu.easemoblibrary.manager.EMMessageManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    override fun setStatusBarColor() = Color.BLACK

    override fun setStatusBarTranslucent() = 0

    override fun loadData() {

    }

    override fun initParams() {
        btn_chattext.setOnClickListener {
            EMMessageManager.sendSingleMessage(Utils.getApp(), EMMessageManager.prepareTxtEMMessage("from local", "admin"))
        }
    }

    override fun initViews() = R.layout.activity_main
}