package com.renyu.mt.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import com.focustech.dbhelper.PlainTextDBHelper
import com.focustech.webtm.protocol.tm.message.model.BroadcastBean
import com.focustech.webtm.protocol.tm.message.model.FriendInfoRsp
import com.focustech.webtm.protocol.tm.message.model.MessageBean
import com.focustech.webtm.protocol.tm.message.model.UserInfoRsp
import com.renyu.commonlibrary.commonutils.ACache
import com.renyu.mt.R
import com.renyu.mt.base.BaseIMActivity
import com.renyu.mt.fragment.ChatListFragment

/**
 * Created by Administrator on 2018/3/21 0021.
 */
class ChatListActivity : BaseIMActivity() {

    // 当前登录用户
    var currentUserInfo: UserInfoRsp? = null

    var conversationFragment: ChatListFragment? = null

    private var mReceiver: BroadcastReceiver? = null

    override fun setStatusBarColor() = Color.WHITE

    override fun setStatusBarTranslucent() = 0

    override fun loadData() {

    }

    override fun initParams() {
        // 获取当前用户信息
        currentUserInfo = ACache.get(this).getAsObject("UserInfoRsp") as UserInfoRsp

        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "MT") {
                    val bean = intent.getSerializableExtra("broadcast") as BroadcastBean
                    // 因为断线或者回收造成的登录成功
                    if (bean.command == BroadcastBean.MTCommand.LoginRsp) {
                        Log.d("MT", "二次登录成功")
                        // 刷新远程数据
                        conversationFragment?.getOfflineIMFromRemote()
                    }
                    // 收到消息刷新列表
                    if (bean.command == BroadcastBean.MTCommand.MessageReceive ||
                            bean.command == BroadcastBean.MTCommand.MessageSend) {
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
                        // 未知用户个人信息刷新，通知会话列表刷新
                        if (currentUserInfo!!.userId != userInfoRsp.userId) {
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

    override fun onBackPressed() {
        val intent = Intent(this, SignInActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }
}