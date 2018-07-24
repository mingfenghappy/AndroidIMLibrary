package com.renyu.nimapp.bean

import android.databinding.ObservableBoolean
import android.databinding.ObservableField

data class LoginInfoBean(
        var account: ObservableField<String>,
        var token: ObservableField<String>
)

data class RefreshBean(var refresh: ObservableBoolean)