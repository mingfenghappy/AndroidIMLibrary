package com.renyu.nimlibrary.manager

import android.util.Log
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.StatusCode
import com.netease.nimlib.sdk.auth.AuthServiceObserver
import com.netease.nimlib.sdk.auth.ClientType
import com.netease.nimlib.sdk.auth.OnlineClient
import com.netease.nimlib.sdk.auth.constant.LoginSyncStatus

object StatueManager {

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
                .observeOnlineStatus({
                    // 踢下线
                    if (it.wontAutoLogin()) {

                    }
                    else {
                        when (it) {
                            StatusCode.NET_BROKEN -> Log.d("NIM_APP", "在线状态：当前网络不可用")
                            StatusCode.UNLOGIN -> Log.d("NIM_APP", "在线状态：未登录")
                            StatusCode.CONNECTING -> Log.d("NIM_APP", "连接中")
                            StatusCode.LOGINING -> Log.d("NIM_APP", "登录中")
                            StatusCode.LOGINED -> Log.d("NIM_APP", "登录成功")
                            else -> {
                            }
                        }
                    }
                }, true)
    }

    /**
     * 监听数据同步状态
     */
    fun observeLoginSyncDataStatus() {
        NIMClient.getService(AuthServiceObserver::class.java)
                .observeLoginSyncDataStatus({ t ->
                    Log.d("NIM_APP", "数据同步状态${t?.name}")
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