package com.renyu.mt.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import com.focustech.dbhelper.PlainTextDBHelper
import com.focustech.message.MTService
import com.focustech.message.model.BroadcastBean
import com.focustech.message.model.FriendGroupRsp
import com.focustech.message.model.FriendInfoRsp
import com.renyu.mt.R
import com.renyu.mt.base.BaseIMActivity
import com.renyu.mt.fragment.FriendListFragment
import java.util.*

/**
 * Created by Administrator on 2018/3/26 0026.
 */
class FriendListActivity : BaseIMActivity() {

    private var mReceiver: BroadcastReceiver? = null

    var friendListFragment: FriendListFragment? = null

    override fun setStatusBarColor() = Color.BLACK

    override fun setStatusBarTranslucent() = 0

    override fun loadData() {

    }

    override fun initParams() {
        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "MT") {
                    val bean = intent.getSerializableExtra("broadcast") as BroadcastBean
                    if (bean.command == BroadcastBean.MTCommand.FriendGroupsRsp) {
                        // 清除所有好友组关系数据
                        PlainTextDBHelper.getInstance().clearAllFriendList()
                        // 增加所有好友组关系数据
                        PlainTextDBHelper.getInstance().insertFriendList((intent.getSerializableExtra("broadcast") as BroadcastBean).serializable as ArrayList<FriendGroupRsp>)
                        // 通知好友列表刷新
                        friendListFragment?.refreshFriendList()
                        // 获取完成组信息之后，就获取全部好友信息
                        MTService.reqFriendInfo(this@FriendListActivity)

                        friendListFragment?.endRefresh()
                    }
                    if (bean.command == BroadcastBean.MTCommand.FriendInfoRsp) {
                        val temp = (intent.getSerializableExtra("broadcast") as BroadcastBean).serializable as FriendInfoRsp
                        // 刷新好友列表
                        friendListFragment?.refreshFriendInfo(temp.friend)
                    }
                }
            }
        }
        receiver = mReceiver
        openCurrentReceiver()

        friendListFragment = FriendListFragment()
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.layout_chatlistframe, friendListFragment, "friendListFragment")
                .commitAllowingStateLoss()
    }

    override fun initViews() = R.layout.activity_chatlist

    override fun onDestroy() {
        super.onDestroy()
        closeCurrentReceiver()
    }
}