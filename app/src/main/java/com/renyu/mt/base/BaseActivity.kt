package com.renyu.mt.base

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import com.focustech.webtm.protocol.tm.message.HeartBeatService
import com.focustech.webtm.protocol.tm.message.MTService
import com.renyu.commonlibrary.baseact.BaseActivity

/**
 * Created by Administrator on 2018/3/20 0020.
 */
abstract class BaseIMActivity: BaseActivity() {

    private var receiver: BroadcastReceiver? = null

    fun setCustomerReceiver(receiver: BroadcastReceiver) {
        this.receiver = receiver
    }

    override fun onResume() {
        super.onResume()
        if (receiver != null) {
            val filter = IntentFilter()
            filter.addAction("MT")
            registerReceiver(receiver, filter)
        }
    }

    override fun onPause() {
        super.onPause()
        if (receiver != null) {
            unregisterReceiver(receiver)
        }
    }

    /**
     * 关闭IM所有服务
     */
    fun closeAllIMService() {
        stopService(Intent(this, HeartBeatService::class.java))
        stopService(Intent(this, MTService::class.java))
    }
}