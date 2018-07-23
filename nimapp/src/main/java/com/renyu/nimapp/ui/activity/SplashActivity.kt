package com.renyu.nimapp.ui.activity

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.SPUtils
import com.renyu.nimapp.R
import com.renyu.nimlibrary.params.CommonParams

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (CommonParams.isKickout) {
            CommonParams.isKickout = false
            // 重置回收标志位
            CommonParams.isRestore = false
        }

        // 发生回收，若执行返回操作则执行页面关闭
        if (CommonParams.isRestore) {
            finish()
            return
        }

        val name = SPUtils.getInstance().getString(CommonParams.SP_UNAME)
        val password = SPUtils.getInstance().getString(CommonParams.SP_PWD)
        // 登录成功跳转首页
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(password)) {
            startActivity(Intent(this@SplashActivity, ChatListActivity::class.java))
        }
        // 没有用户信息则执行登录操作
        else {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 重置回收标志位
        CommonParams.isRestore = false
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getIntExtra(CommonParams.TYPE, -1) == CommonParams.FINISH) {
            finish()
        }
        if (intent.getIntExtra(CommonParams.TYPE, -1) == CommonParams.KICKOUT) {
            CommonParams.isKickout = false
            startActivity(Intent(this@SplashActivity, SignInActivity::class.java))
        }
        if (intent.getIntExtra(CommonParams.TYPE, -1) == CommonParams.SIGNINBACK) {
            finish()
        }
        if (intent.getIntExtra(CommonParams.TYPE, -1) == CommonParams.MAIN) {
            startActivity(Intent(this@SplashActivity, ChatListActivity::class.java))
        }
    }
}