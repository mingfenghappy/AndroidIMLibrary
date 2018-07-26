package com.renyu.nimlibrary.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.renyu.nimlibrary.repository.Repos

open class BaseConversationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Repos.queryMessageListEx(MessageBuilder.createEmptyMessage(intent.getStringExtra("contactId"), SessionTypeEnum.P2P, 0))
    }
}