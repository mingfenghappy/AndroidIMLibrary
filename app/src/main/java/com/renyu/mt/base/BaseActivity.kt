package com.renyu.mt.base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ServiceUtils
import com.focustech.common.DownloadTool
import com.focustech.dbhelper.PlainTextDBHelper
import com.focustech.tm.open.sdk.messages.protobuf.Enums
import com.focustech.webtm.protocol.tm.message.MTService
import com.focustech.webtm.protocol.tm.message.model.BroadcastBean
import com.focustech.webtm.protocol.tm.message.model.MessageBean
import com.focustech.webtm.protocol.tm.message.model.SystemMessageBean
import com.renyu.commonlibrary.baseact.BaseActivity
import com.renyu.commonlibrary.commonutils.ACache
import com.renyu.mt.MTApplication
import com.renyu.mt.R
import com.renyu.mt.service.HeartBeatService

/**
 * Created by Administrator on 2018/3/20 0020.
 */
abstract class BaseIMActivity: BaseActivity() {

    open var receiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if ((application as MTApplication).baseReceiver == null) {
            Log.d("BaseIMActivity", "注册基础广播")
            openBaseReceiver()
        }
    }

    override fun onResume() {
        super.onResume()
        // 不在登录页，说明已经完成登录
        if (localClassName != "activity.SignInActivity") {
            ACache.get(application).put("isSignIn", true)
        }
        else {
            ACache.get(application).put("isSignIn", false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 登录页被关闭
        if (localClassName == "activity.SignInActivity") {
            ACache.get(application).put("isSignIn", false)
        }
    }

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
                    }
                    if (bean.command == BroadcastBean.MTCommand.Disconn) {
                        Log.d("MTApplication", "连接已断开")
                    }
                    if (bean.command == BroadcastBean.MTCommand.Conning) {
                        Log.d("MTApplication", "正在连接")
                    }
                    // 收到系统消息
                    if (bean.command == BroadcastBean.MTCommand.NewSysNty) {
                        MTService.getSysNtyReq(this@BaseIMActivity, bean.serializable.toString().toLong())
                    }
                    // 获取到系统消息
                    if (bean.command == BroadcastBean.MTCommand.SystemMessageBean) {
                        // 插入系统消息
                        PlainTextDBHelper.getInstance().insertSystemMessage(bean.serializable as SystemMessageBean)
                        // 通知会话列表刷新以及会话详情刷新
                        // TODO: 2018/3/21 0021  需要重新测试系统消息功能
//                        BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.MessageReceive, messageBean)
                    }
                    // 收到新消息
                    if (bean.command == BroadcastBean.MTCommand.Message) {
                        val messageBean = (intent.getSerializableExtra("broadcast") as BroadcastBean).serializable as MessageBean
                        // 发送已读回执
                        MTService.hasReadMessage(this@BaseIMActivity, messageBean.fromSvrMsgId)
                        // 下载语音文件
                        if (messageBean.messageType == "7") {
                            // 下载完成语音文件之后，方可同步数据库与刷新页面
                            DownloadTool.addFileAndDb(this@BaseIMActivity, messageBean)
                        } else {
                            PlainTextDBHelper.getInstance().insertMessage(messageBean)
                            // 通知会话列表刷新以及会话详情刷新
                            BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.MessageReceive, messageBean)
                        }
                    }
                    // 语音、图片上传完成之后更新表字段
                    if (bean.command == BroadcastBean.MTCommand.MessageUploadComp) {
                        // 语音、图片上传完成之后更新表字段
                        val temp = (intent.getSerializableExtra("broadcast") as BroadcastBean).serializable as MessageBean
                        PlainTextDBHelper.getInstance().updateSendState(temp.svrMsgId, Enums.Enable.DISABLE, Enums.Enable.ENABLE)
                    }
                    // 语音、图片上传失败之后更新表字段
                    if (bean.command == BroadcastBean.MTCommand.MessageUploadFail) {
                        val temp = (intent.getSerializableExtra("broadcast") as BroadcastBean).serializable as MessageBean
                        PlainTextDBHelper.getInstance().updateSendState(temp.svrMsgId, Enums.Enable.ENABLE, Enums.Enable.ENABLE)
                    }
                }
            }
        }
        val filter = IntentFilter()
        filter.addAction("MT")
        registerReceiver((application as MTApplication).baseReceiver, filter)
        // 开启心跳服务并进行连接
        if (!ServiceUtils.isServiceRunning("com.renyu.mt.service.HeartBeatService")) {
            if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
                val intent = Intent(this, HeartBeatService::class.java)
                intent.putExtra("smallIcon", R.mipmap.ic_launcher)
                intent.putExtra("largeIcon", R.mipmap.ic_launcher)
                startForegroundService(intent)
            }
            else {
                startService(Intent(this, HeartBeatService::class.java))
            }
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