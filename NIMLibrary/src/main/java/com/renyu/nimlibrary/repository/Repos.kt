package com.renyu.nimapp.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.netease.nimlib.sdk.AbortableFuture
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.auth.*
import com.netease.nimlib.sdk.auth.constant.LoginSyncStatus
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum
import com.netease.nimlib.sdk.msg.model.RecentContact
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

    /**
     * 监听消息接收观察者
     */
    fun observeReceiveMessage() {
        NIMClient.getService(MsgServiceObserve::class.java)
                .observeReceiveMessage({ t -> Log.d("NIMAPP", "收到新消息${t?.size}") }, true)
    }

    /**
     * 监听最近会话变更
     */
    fun observeRecentContact() {
        NIMClient.getService(MsgServiceObserve::class.java)
                .observeRecentContact({ t -> Log.d("NIMAPP", "最近会话变更${t?.size}") }, true)
    }

    /**
     * 监听消息状态
     */
    fun observeMsgStatus() {
        NIMClient.getService(MsgServiceObserve::class.java)
                .observeMsgStatus({ t -> Log.d("NIMAPP", "消息状态：${t.fromNick} ${t.content} ${t.status}") }, true)
    }

    /**
     * 获取最近会话列表
     */
    fun queryRecentContacts() {
        NIMClient.getService(MsgService::class.java).queryRecentContacts()
                .setCallback(object : RequestCallback<List<RecentContact>> {
            override fun onSuccess(param: List<RecentContact>?) {
                param?.forEach {
                    Log.d("NIMAPP", "最近会话列表：${it.fromNick} ${it.content}")
                }
            }

            override fun onFailed(code: Int) {

            }

            override fun onException(exception: Throwable?) {

            }
        })
    }

    /**
     * 获取会话详情
     */
    fun queryMessageListEx(message: IMMessage) {
        NIMClient.getService(MsgService::class.java)
                .queryMessageListEx(message, QueryDirectionEnum.QUERY_OLD, 20, true)
                .setCallback(object : RequestCallback<List<IMMessage>> {
                    override fun onSuccess(param: List<IMMessage>?) {
                        param?.forEach {
                            Log.d("NIMAPP", "会话列表：${it.fromNick} ${it.content}")
                        }
                    }

                    override fun onFailed(code: Int) {

                    }

                    override fun onException(exception: Throwable?) {

                    }
                })
    }

    private fun sendTextMessage(account: String, text: String, resend: Boolean) {
        val sessionTypeEnum = SessionTypeEnum.P2P
        val textMessage = MessageBuilder.createTextMessage(account, sessionTypeEnum, text)
        NIMClient.getService(MsgService::class.java).sendMessage(textMessage, resend)
                .setCallback(object : RequestCallback<Void> {
            override fun onSuccess(param: Void?) {
                Log.d("NIMAPP", "消息发送成功")
            }

            override fun onFailed(code: Int) {

            }

            override fun onException(exception: Throwable?) {

            }
        })
    }

    /**
     * 发送文字消息
     */
    fun sendTextMessage(account: String, text: String) {
        sendTextMessage(account, text, false)
    }

    /**
     * 重发文字消息
     */
    fun reSendTextMessage(account: String, text: String) {
        sendTextMessage(account, text, true)
    }
}