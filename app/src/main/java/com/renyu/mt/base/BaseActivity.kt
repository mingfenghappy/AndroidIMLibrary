package com.renyu.mt.base

import android.content.BroadcastReceiver
import android.content.IntentFilter
import com.renyu.commonlibrary.baseact.BaseActivity

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
}