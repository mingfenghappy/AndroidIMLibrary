package com.renyu.nimapp.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.netease.nimlib.sdk.AbortableFuture
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.auth.*
import com.netease.nimlib.sdk.auth.constant.LoginSyncStatus
import com.renyu.nimapp.bean.Resource

object Repos {

    // 请求合集
    private val requests: HashMap<String, AbortableFuture<out Any>> = HashMap()

    /**
     * 登录
     */
    fun login(account: String, token: String) : LiveData<Resource<LoginInfo>> {
        val temp = MutableLiveData<Resource<LoginInfo>>()
        temp.value = Resource.loading()
        val loginRequest = NIMClient.getService(AuthService::class.java)
                .login(LoginInfo(account, token))
        loginRequest.setCallback(object : RequestCallback<LoginInfo> {
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
        // 添加请求
        requests["login"] = loginRequest
        return temp
    }

    /**
     * 取消登录
     */
    fun cancelLogin() {
        requests["login"]?.abort()
    }

    /**
     * 监听多端登录状态
     */
    fun observeOtherClients() {
        NIMClient.getService(AuthServiceObserver::class.java)
                .observeOtherClients(com.netease.nimlib.sdk.Observer<List<OnlineClient>> { t ->
                    if (t == null || t.isEmpty()) {
                        return@Observer
                    } else {
                        when(t[0].clientType) {
                            ClientType.Windows -> {

                            }
                            ClientType.MAC -> {

                            }
                            ClientType.Web -> {

                            }
                            ClientType.iOS -> {

                            }
                            ClientType.Android -> {

                            }
                            ClientType.UNKNOW -> {

                            }
                        }
                    }
                }, true)
    }

    /**
     * 监听用户在线状态
     */
    fun observeOnlineStatus() {
        NIMClient.getService(AuthServiceObserver::class.java)
                .observeOnlineStatus({ t -> Log.d("NIMAPP", "在线状态${t?.value}") }, true)
    }

    /**
     * 监听数据同步状态
     */
    fun observeLoginSyncDataStatus() {
        NIMClient.getService(AuthServiceObserver::class.java)
                .observeLoginSyncDataStatus({ t ->
                    Log.d("NIMAPP", "数据同步状态${t?.name}")
                    when(t) {
                        LoginSyncStatus.NO_BEGIN -> {

                        }
                        LoginSyncStatus.BEGIN_SYNC -> {

                        }
                        LoginSyncStatus.SYNC_COMPLETED -> {

                        }
                    }
                }, true)
    }
}