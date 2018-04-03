package com.renyu.mt.activity

import android.graphics.Color
import com.renyu.mt.R
import com.renyu.tmuilibrary.activity.BaseFriendListActivity
import com.renyu.tmuilibrary.fragment.FriendListFragment

/**
 * Created by Administrator on 2018/3/26 0026.
 * 涉及到还有列表的可以参考此类的写法
 */
class FriendListActivity : BaseFriendListActivity() {

    override fun setStatusBarColor() = Color.BLACK

    override fun setStatusBarTranslucent() = 0

    override fun loadData() {

    }

    override fun initParams() {
        super.initParams()

        friendListFragment = FriendListFragment()
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.layout_friendlistframe, friendListFragment, "friendListFragment")
                .commitAllowingStateLoss()
    }

    override fun initViews() = R.layout.activity_friendlist
}