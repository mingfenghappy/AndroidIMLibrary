package com.renyu.nimlibrary.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.netease.nimlib.sdk.AbortableFuture
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.auth.LoginInfo
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.renyu.nimapp.bean.Resource
import com.renyu.nimlibrary.manager.AuthManager
import com.renyu.nimlibrary.manager.MessageManager

object Repos {

    // 请求合集
    private val requests: HashMap<String, AbortableFuture<out Any>> = HashMap()

    /**
     * 登录
     */
    fun login(account: String, token: String): LiveData<Resource<LoginInfo>> {
        val temp = MutableLiveData<Resource<LoginInfo>>()
        temp.value = Resource.loading()
        // 添加请求
        requests["login"] = AuthManager.login(account, token, object : RequestCallback<LoginInfo> {
            override fun onSuccess(param: LoginInfo?) {
                temp.value = Resource.sucess(param)
            }

            override fun onFailed(code: Int) {
                temp.value = Resource.failed(code)
            }

            override fun onException(exception: Throwable?) {
                temp.value = Resource.exception(exception?.message)
            }
        })
        return temp
    }

    /**
     * 取消登录
     */
    fun cancelLogin() {
        requests["login"]?.abort()
    }

    /**
     * 获取最近会话列表
     */
    fun queryRecentContacts(): LiveData<Resource<List<RecentContact>>> {
        val temp = MutableLiveData<Resource<List<RecentContact>>>()
        temp.value = Resource.loading()
        MessageManager.queryRecentContacts(object : RequestCallback<List<RecentContact>> {
            override fun onSuccess(param: List<RecentContact>?) {
                temp.value = Resource.sucess(param)
            }

            override fun onFailed(code: Int) {
                temp.value = Resource.failed(code)
            }

            override fun onException(exception: Throwable?) {
                temp.value = Resource.exception(exception?.message)
            }
        })
        return temp
    }

    /**
     * 向前获取会话详情
     */
    fun queryMessageListExBefore(message: IMMessage): LiveData<Resource<List<IMMessage>>> {
        val temp = MutableLiveData<Resource<List<IMMessage>>>()
        temp.value = Resource.loading()
        MessageManager.queryMessageListExBefore(message, object : RequestCallback<List<IMMessage>> {
            override fun onSuccess(param: List<IMMessage>?) {
                temp.value = Resource.sucess(param)
            }

            override fun onFailed(code: Int) {
                temp.value = Resource.failed(code)
            }

            override fun onException(exception: Throwable?) {
                temp.value = Resource.exception(exception?.message)
            }
        })
        return temp
    }
}