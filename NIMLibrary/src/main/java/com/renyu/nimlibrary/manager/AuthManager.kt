package com.renyu.nimlibrary.manager

import com.netease.nimlib.sdk.AbortableFuture
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.auth.AuthService
import com.netease.nimlib.sdk.auth.LoginInfo

object AuthManager {

    /**
     * 登录
     */
    fun login(account: String, token: String, requestCallback: RequestCallback<LoginInfo>): AbortableFuture<out Any> {
        val loginRequest = NIMClient.getService(AuthService::class.java)
                .login(LoginInfo(account, token))
        loginRequest.setCallback(requestCallback)
        return loginRequest
    }
}