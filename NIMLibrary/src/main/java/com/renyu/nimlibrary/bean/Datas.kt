package com.renyu.nimapp.bean

import android.databinding.ObservableField

data class LoginInfoBean(
        var account: ObservableField<String>,
        var token: ObservableField<String>
)