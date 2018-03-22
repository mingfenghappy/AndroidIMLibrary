package com.renyu.mt.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import com.focustech.common.IDownloadFile
import com.focustech.dbhelper.PlainTextDBHelper
import com.focustech.tm.open.sdk.params.FusionField
import com.focustech.webtm.protocol.tm.message.MTService
import com.focustech.webtm.protocol.tm.message.model.BroadcastBean
import com.focustech.webtm.protocol.tm.message.model.FriendInfoRsp
import com.focustech.webtm.protocol.tm.message.model.MessageBean
import com.focustech.webtm.protocol.tm.message.model.UserInfoRsp
import com.renyu.commonlibrary.commonutils.ACache
import com.renyu.mt.R
import com.renyu.mt.base.BaseIMActivity
import com.renyu.mt.fragment.ChatListFragment
import java.util.*

/**
 * Created by Administrator on 2018/3/21 0021.
 */
class ChatListActivity : BaseIMActivity() {

    // 当前登录用户
    var currentUserInfo: UserInfoRsp? = null
    // 是否第一次获取离线消息
    private var isFirst = false

    var conversationFragment: ChatListFragment? = null

    private var mReceiver: BroadcastReceiver? = null

    override fun setStatusBarColor() = Color.WHITE

    override fun setStatusBarTranslucent() = 0

    override fun loadData() {
        isFirst = true
        // 获取离线消息
        MTService.reqGetOfflineMessage(this)
        // 获取系统消息
        MTService.getSysNtyReq(this, 0)
    }

    override fun initParams() {
        // 获取当前用户信息
        currentUserInfo = ACache.get(this).getAsObject("UserInfoRsp") as UserInfoRsp

        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "MT") {
                    val bean = intent.getSerializableExtra("broadcast") as BroadcastBean
                    // 获取到离线消息。离线数据不进行更新，全部依赖本地数据与接口数据的返回
                    if (bean.command == BroadcastBean.MTCommand.GetOfflineMessageRsp) {
                        val tempOffline = (intent.getSerializableExtra("broadcast") as BroadcastBean).serializable as ArrayList<MessageBean>
                        // 发送已读回执
                        for (messageBean in tempOffline) {
                            MTService.hasReadOfflineMessage(this@ChatListActivity, messageBean.fromSvrMsgId)
                        }
                        // 下载语音文件
                        for (i in tempOffline.indices) {
                            if (tempOffline[i].messageType == "7") {
                                val token = currentUserInfo!!.token
                                val fileId = tempOffline[i].localFileName
                                val sb = StringBuilder(FusionField.downloadUrl)
                                sb.append("fileid=").append(fileId).append("&type=").append("voice").append("&token=").append(token)
                                IDownloadFile.addFile(sb.toString(), fileId)
                            }
                        }
                        // 更新数据库
                        PlainTextDBHelper.getInstance().insertMessages((intent.getSerializableExtra("broadcast") as BroadcastBean).serializable as ArrayList<MessageBean>)
                    }
                    // 收到消息刷新列表
                    if (bean.command == BroadcastBean.MTCommand.MessageReceive) {
                        // 通知会话列表结束刷新
                        conversationFragment?.refreshOneMessage((intent.getSerializableExtra("broadcast") as BroadcastBean).serializable as MessageBean)
                    }
                    // 发送消息刷新列表
                    if (bean.command == BroadcastBean.MTCommand.MessageSend) {
                        conversationFragment?.refreshOneMessage((intent.getSerializableExtra("broadcast") as BroadcastBean).serializable as MessageBean)
                    }
                    // 消息已读
                    if (bean.command == BroadcastBean.MTCommand.UpdateRead) {
                        // 通知会话列表结束刷新
                        conversationFragment?.updateRead((intent.getSerializableExtra("broadcast") as BroadcastBean).serializable.toString())
                    }
                    // 好友信息刷新（好友信息数据会单条多次返回，所以更新比较频繁。没有采用结束标志位，怕中断）
                    if (bean.command == BroadcastBean.MTCommand.FriendInfoRsp) {
                        val temp = (intent.getSerializableExtra("broadcast") as BroadcastBean).serializable as FriendInfoRsp
                        // 插入或更新好友信息
                        PlainTextDBHelper.getInstance().insertFriendList(temp)
                        // 通知会话列表刷新
                        conversationFragment?.refreshOfflineUser(temp.friend)
                    }
                    if (bean.command == BroadcastBean.MTCommand.UserInfoRsp) {
                        val userInfoRsp = (intent.getSerializableExtra("broadcast") as BroadcastBean).serializable as UserInfoRsp
                        // 当前用户重新登录完成
                        if (currentUserInfo!!.userId == userInfoRsp.userId) {

                        }
                        else {
                            // 未知用户个人信息刷新，通知会话列表刷新
                            conversationFragment?.refreshOfflineUser(userInfoRsp)
                        }
                    }
                }
            }
        }
        receiver = mReceiver
        openCurrentReceiver()

        conversationFragment = ChatListFragment()
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.layout_chatlistframe, conversationFragment, "conversationFragment")
                .commitAllowingStateLoss()
    }

    override fun initViews() = R.layout.activity_chatlist

    override fun onDestroy() {
        super.onDestroy()
        closeCurrentReceiver()
    }
}