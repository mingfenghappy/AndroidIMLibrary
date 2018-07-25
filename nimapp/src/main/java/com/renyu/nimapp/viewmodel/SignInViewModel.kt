package com.renyu.nimapp.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.netease.nimlib.sdk.auth.LoginInfo
import com.renyu.nimapp.bean.Resource
import com.renyu.nimlibrary.repository.Repos

class SignInViewModel : ViewModel() {

    private val loginInfoRequest: MutableLiveData<LoginInfo> = MutableLiveData()
    var loginInfoResonse: LiveData<Resource<LoginInfo>>? = null

    init {
        loginInfoResonse = Transformations.switchMap(loginInfoRequest) {
            if (it == null) {
                MutableLiveData()
            }
            else {
                Repos.login(it.account, it.token)
            }
        }
    }

    /**
     * 登录
     */
    fun login(account: String, token: String) {
        loginInfoRequest.value = LoginInfo(account, token)
    }
}