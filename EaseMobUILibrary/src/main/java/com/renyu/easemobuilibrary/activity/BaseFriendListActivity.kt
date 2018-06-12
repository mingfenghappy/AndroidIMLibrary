package com.renyu.easemobuilibrary.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.renyu.easemobuilibrary.base.BaseIMActivity
import com.renyu.easemobuilibrary.fragment.FriendListFragment
import com.renyu.easemobuilibrary.model.BroadcastBean
import com.renyu.easemobuilibrary.params.CommonParams
import java.util.*

abstract class BaseFriendListActivity : BaseIMActivity() {

    @JvmField var friendListFragment: FriendListFragment? = null

    override fun initParams() {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == actionName) {
                    // 获取好友关系组数据
                    if (intent?.getSerializableExtra(BroadcastBean.COMMAND) === BroadcastBean.EaseMobCommand.FriendGroupsRsp) {
                        // 通知好友列表刷新
                        friendListFragment?.refreshFriendList(intent.getSerializableExtra(BroadcastBean.SERIALIZABLE) as ArrayList<String>?)
                    }
                    // 删除了联系人
                    if (intent?.getSerializableExtra(BroadcastBean.COMMAND) == BroadcastBean.EaseMobCommand.onContactDeleted) {
                        // 刷新好友列表
                        friendListFragment?.deleteFriendsRelation(intent.getSerializableExtra(BroadcastBean.SERIALIZABLE) as String?)
                    }
                    // 增加了联系人
                    if (intent?.getSerializableExtra(BroadcastBean.COMMAND) == BroadcastBean.EaseMobCommand.onContactAdded) {
                        // 刷新好友列表
                        friendListFragment?.addFriendsRelation(intent.getSerializableExtra(BroadcastBean.SERIALIZABLE) as String?)
                    }
                    // 被踢下线
                    if (intent?.getSerializableExtra(BroadcastBean.COMMAND) == BroadcastBean.EaseMobCommand.Kickout) {
                        CommonParams.isKickout = true
                        if (!isPause) {
                            kickout()
                        }
                    }
                }
            }
        }
        openCurrentReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        closeCurrentReceiver()
    }
}