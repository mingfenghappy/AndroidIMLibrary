package com.renyu.nimapp.ui.app

import android.support.multidex.MultiDexApplication
import android.text.TextUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.Utils
import com.facebook.drawee.backends.pipeline.Fresco
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.SDKOptions
import com.netease.nimlib.sdk.auth.LoginInfo
import com.netease.nimlib.sdk.util.NIMUtil
import com.renyu.nimlibrary.manager.MessageManager
import com.renyu.nimlibrary.manager.StatueManager
import com.renyu.nimlibrary.manager.UserManager
import com.renyu.nimlibrary.params.CommonParams

class ExampleApp : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()

        NIMClient.init(this, getLoginInfo(), options())

        if (NIMUtil.isMainProcess(this)) {
            // 初始化工具库
            Utils.init(this)
            // 初始化Fresco
            Fresco.initialize(this)

            // 监听用户在线状态
            StatueManager.observeOnlineStatus()
            // 监听数据同步状态
            StatueManager.observeLoginSyncDataStatus()
            // 监听多端登录状态
            StatueManager.observeOtherClients()
            // 消息接收观察者
            MessageManager.observeReceiveMessage()
            // 监听最近会话变更
            MessageManager.observeRecentContact()
            // 监听消息状态
            MessageManager.observeMsgStatus()
            // 监听最近联系人被删除
            MessageManager.observeRecentContactDeleted()
            // 监听消息撤回
            MessageManager.observeRevokeMessage()
            // 监听消息已读回执
            MessageManager.observeMessageReceipt()
            // 监听消息附件上传/下载进度
            MessageManager.observeAttachmentProgress()
            // 监听自定义通知
            MessageManager.observeCustomNotification()
            // 监听用户资料变更
            UserManager.observeUserInfoUpdate()
        }
    }

    private fun getLoginInfo() : LoginInfo? {
        val name = SPUtils.getInstance().getString(CommonParams.SP_UNAME)
        val password = SPUtils.getInstance().getString(CommonParams.SP_PWD)
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(password)) {
            return LoginInfo(name, password)
        }
        return null
    }

    private fun options() : SDKOptions {
        val options = SDKOptions()
        // 配置 APP 保存图片/语音/文件/log等数据的目录
        options.sdkStorageRootPath = CommonParams.SDKROOT
        // 配置数据库加密秘钥
        options.databaseEncryptKey = "house365"
        // 配置是否需要SDK自动预加载多媒体消息的附件
        options.preloadAttach = true
        // 配置附件缩略图的尺寸大小
        options.thumbnailSize =  ScreenUtils.getScreenWidth() / 2
        // 在线多端同步未读数
        options.sessionReadAck = true
        // 动图的缩略图直接下载原图
        options.animatedImageThumbnailEnabled = true
        // 采用异步加载SDK
        options.asyncInitSDK = true
        // 是否是弱IM场景
        options.reducedIM = false
        // 是否提高SDK进程优先级（默认提高，可以降低SDK核心进程被系统回收的概率）
        options.improveSDKProcessPriority = true
        // 预加载服务，默认true，不建议设置为false，预加载连接可以优化登陆流程
        options.preLoadServers = true
        // 是否在 SDK 初始化时检查清单文件配置是否完全，默认为 false，建议开发者在调试阶段打开，上线时关掉
        options.checkManifestConfig = true
        return options
    }
}