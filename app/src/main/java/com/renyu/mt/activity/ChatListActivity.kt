package com.renyu.mt.activity

import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.blankj.utilcode.util.Utils
import com.focustech.dbhelper.PlainTextDBHelper
import com.renyu.mt.R
import com.renyu.tmbaseuilibrary.params.CommonParams
import com.renyu.tmbaseuilibrary.utils.IntentWrapper
import com.renyu.tmuilibrary.activity.BaseChatListActivity
import com.renyu.tmuilibrary.fragment.ChatListFragment

/**
 * Created by Administrator on 2018/3/21 0021.
 * 涉及到会话列表的可以参考此类的写法
 */
class ChatListActivity : BaseChatListActivity(), ChatListFragment.OnHeaderViewSetListener {

    override fun setStatusBarColor() = Color.BLACK

    override fun setStatusBarTranslucent() = 0

    override fun loadData() {

    }

    override fun initParams() {
        // 注意这里有调用父类的方法
        super.initParams()

        val textView = TextView(this)
        textView.text = "联系人"
        textView.setOnClickListener { view ->
            val intent = Intent(this, FriendListActivity::class.java)
            startActivity(intent)
        }
        findViewById<LinearLayout>(R.id.layout_nav_right).addView(textView)

        conversationFragment = ChatListFragment()
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.layout_chatlistframe, conversationFragment, "conversationFragment")
                .commitAllowingStateLoss()

        // 开启白名单判断
        IntentWrapper.whiteListMatters(this, null)
    }

    override fun initViews() = R.layout.activity_chatlist

    override fun getHeadView(): View? {
//        val view = LinearLayout(this)
//        view.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(100f))
//        view.backgroundColor = Color.BLUE
        return null
    }

    override fun onBackPressed() {
        val intent = Intent(this, SplashActivity::class.java)
        intent.putExtra(CommonParams.TYPE, CommonParams.FINISH)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        val unReadCount = PlainTextDBHelper.getInstance(Utils.getApp()).allUnreadMessageNum
    }
}