package com.renyu.nimlibrary.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum

class ConversationViewModelFactory(private val account: String, private val sessionType: SessionTypeEnum) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (ViewModel::class.java.isAssignableFrom(modelClass)) {
            try {
                return ConversationViewModel(account, sessionType) as T
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return super.create<T>(modelClass)
    }
}