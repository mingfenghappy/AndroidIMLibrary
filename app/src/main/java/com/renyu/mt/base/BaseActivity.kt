package com.renyu.mt.base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ServiceUtils
import com.focustech.webtm.protocol.tm.message.HeartBeatService
import com.focustech.webtm.protocol.tm.message.MTService
import com.focustech.webtm.protocol.tm.message.model.BroadcastBean
import com.renyu.commonlibrary.baseact.BaseActivity
import com.renyu.mt.MTApplication

/**
 * Created by Administrator on 2018/3/20 0020.
 */
abstract class BaseIMActivity: BaseActivity() {

    open var receiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!ServiceUtils.isServiceRunning("com.focustech.webtm.protocol.tm.message.HeartBeatService")) {
            Log.d("BaseIMActivity", "注册基础广播")
            openBaseReceiver()
        }
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
        closeBaseReceiver()
        stopService(Intent(this, HeartBeatService::class.java))
        stopService(Intent(this, MTService::class.java))
    }

    /**
     * 注册基础广播
     */
    private fun openBaseReceiver() {
        // 注册连接监听广播
        (application as MTApplication).baseReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == "MT") {
                    val bean = intent.getSerializableExtra("broadcast") as BroadcastBean
                    if (bean.command == BroadcastBean.MTCommand.Conn) {
                        Log.d("MTApplication", "连接成功")
                        (application as MTApplication).connState = BroadcastBean.MTCommand.Conn
                    }
                    if (bean.command == BroadcastBean.MTCommand.Disconn) {
                        Log.d("MTApplication", "连接已断开")
                        (application as MTApplication).connState = BroadcastBean.MTCommand.Disconn
                    }
                    if (bean.command == BroadcastBean.MTCommand.Conning) {
                        Log.d("MTApplication", "正在连接")
                        (application as MTApplication).connState = BroadcastBean.MTCommand.Conning
                    }
                }
            }
        }
        val filter = IntentFilter()
        filter.addAction("MT")
        registerReceiver((application as MTApplication).baseReceiver, filter)
        // 开启心跳服务并进行连接
        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
            startForegroundService(Intent(this, HeartBeatService::class.java))
        }
        else {
            startService(Intent(this, HeartBeatService::class.java))
        }
    }

    /**
     * 反注册基础广播
     */
    private fun closeBaseReceiver() {
        if ((application as MTApplication).baseReceiver != null) {
            unregisterReceiver((application as MTApplication).baseReceiver)
            (application as MTApplication).baseReceiver = null
        }
    }
}