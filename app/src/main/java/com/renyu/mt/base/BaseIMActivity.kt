package com.renyu.mt.base

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import com.blankj.utilcode.util.ServiceUtils
import com.renyu.commonlibrary.baseact.BaseActivity
import com.renyu.mt.R
import com.renyu.mt.activity.SignInActivity
import com.renyu.mt.params.CommonParams
import com.renyu.mt.service.HeartBeatService

/**
 * Created by Administrator on 2018/3/20 0020.
 */
abstract class BaseIMActivity: BaseActivity() {
    @JvmField var isPause = false

    @JvmField var receiver: BroadcastReceiver? = null

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

    override fun onResume() {
        super.onResume()

        // 开启心跳服务并进行连接
        if (!ServiceUtils.isServiceRunning("com.renyu.mt.service.HeartBeatService")) {
            if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
                val intent = Intent(this, HeartBeatService::class.java)
                intent.putExtra("smallIcon", R.mipmap.ic_launcher)
                intent.putExtra("largeIcon", R.mipmap.ic_launcher)
                startForegroundService(intent)
            } else {
                startService(Intent(this, HeartBeatService::class.java))
            }
        }

        isPause = false

        if (CommonParams.isKickout) {
            kickout()
        }
    }

    override fun onPause() {
        super.onPause()

        isPause = true
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putBoolean("isRestore", true)
    }

    fun kickout() {
        val intent = Intent(this, SignInActivity::class.java)
        intent.putExtra(CommonParams.TYPE, CommonParams.KICKOUT)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }
}