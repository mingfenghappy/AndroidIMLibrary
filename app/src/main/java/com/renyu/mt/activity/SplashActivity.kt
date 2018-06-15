package com.renyu.mt.activity

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.text.TextUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.Utils
import com.renyu.commonlibrary.baseact.BaseActivity
import com.renyu.commonlibrary.commonutils.ACache
import com.renyu.commonlibrary.impl.OnPermissionCheckedImpl
import com.renyu.commonlibrary.params.InitParams
import com.renyu.commonlibrary.views.permission.PermissionActivity
import com.renyu.mt.R
import com.renyu.tmbaseuilibrary.params.CommonParams

/**
 * 中控页使用
 */
class SplashActivity : BaseActivity() {
    private var permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE)

    override fun setStatusBarColor() = Color.BLACK

    override fun setStatusBarTranslucent() = 0

    override fun initParams() {
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

        if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
            PermissionActivity.gotoActivity(this, permissions, "请授予SD卡读写权限与定位权限", object : OnPermissionCheckedImpl {
                override fun denied() {
                    finish()
                }

                override fun grant() {
                    this@SplashActivity.grant()
                }
            })
        }
        else {
            grant()
        }
    }

    private fun grant() {
        // 初始化文件夹
        FileUtils.createOrExistsDir(InitParams.IMAGE_PATH)
        FileUtils.createOrExistsDir(InitParams.HOTFIX_PATH)
        FileUtils.createOrExistsDir(InitParams.FILE_PATH)
        FileUtils.createOrExistsDir(InitParams.LOG_PATH)
        FileUtils.createOrExistsDir(InitParams.CACHE_PATH)

        // 登录成功跳转首页
        if (ACache.get(Utils.getApp()).getAsObject("UserInfoRsp") != null
                && !TextUtils.isEmpty(SPUtils.getInstance().getString(CommonParams.SP_UNAME))
                && !TextUtils.isEmpty(SPUtils.getInstance().getString(CommonParams.SP_PWD))) {
            startActivity(Intent(this@SplashActivity, ChatListActivity::class.java))
        }
        // 没有用户信息则执行登录操作
        else if (ACache.get(Utils.getApp()).getAsObject("UserInfoRsp") == null
                || TextUtils.isEmpty(SPUtils.getInstance().getString(CommonParams.SP_UNAME))
                || TextUtils.isEmpty(SPUtils.getInstance().getString(CommonParams.SP_PWD))) {
            startActivity(Intent(this@SplashActivity, SignInActivity::class.java))
        }
    }

    override fun loadData() {

    }

    override fun initViews() = R.layout.activity_splash


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