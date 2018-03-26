package com.renyu.mt.base

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Bundle
import com.renyu.commonlibrary.baseact.BaseActivity
import com.renyu.mt.params.CommonParams

/**
 * Created by Administrator on 2018/3/20 0020.
 */
abstract class BaseIMActivity: BaseActivity() {
    open var receiver: BroadcastReceiver? = null

    fun openCurrentReceiver() {
        if (receiver != null) {
            val filter = IntentFilter()
            filter.addAction("MT")
            registerReceiver(receiver, filter)
        }
    }

    fun closeCurrentReceiver() {
        if (receiver != null) {
            unregisterReceiver(receiver)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            CommonParams.isRestore = true
        }
        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putBoolean("isRestore", true)
    }
}