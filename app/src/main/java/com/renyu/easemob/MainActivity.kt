package com.renyu.easemob

import android.graphics.Color
import com.renyu.commonlibrary.baseact.BaseActivity
import com.renyu.mt.R

class MainActivity : BaseActivity() {
    override fun setStatusBarColor() = Color.BLACK

    override fun setStatusBarTranslucent() = 0

    override fun loadData() {

    }

    override fun initParams() {

    }

    override fun initViews() = R.layout.activity_main
}