package com.renyu.nimlibrary.manager

import android.util.Log
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.uinfo.UserService
import com.netease.nimlib.sdk.uinfo.UserServiceObserve
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo
import com.renyu.nimlibrary.bean.ObserveResponse
import com.renyu.nimlibrary.bean.ObserveResponseType
import com.renyu.nimlibrary.util.RxBus

object UserManager {
    /**
     * 监听用户资料变更
     */
    fun observeUserInfoUpdate() {
        NIMClient.getService(UserServiceObserve::class.java).observeUserInfoUpdate({
            it.forEach {
                Log.d("NIM_APP", "用户资料变更通知：${it.name}")
            }
        }, true)
    }

    /**
     * 从服务器获取用户资料
     */
    fun fetchUserInfo(accounts: List<String>) {
        NIMClient.getService(UserService::class.java).fetchUserInfo(accounts).setCallback(object : RequestCallback<List<NimUserInfo>> {
            override fun onSuccess(param: List<NimUserInfo>?) {
                RxBus.getDefault().post(ObserveResponse(param, ObserveResponseType.FetchUserInfo))
                param?.forEach {
                    Log.d("NIM_APP", "从服务器获取用户资料：${it.name}")
                }
            }

            override fun onFailed(code: Int) {

            }

            override fun onException(exception: Throwable?) {

            }
        })
    }
}