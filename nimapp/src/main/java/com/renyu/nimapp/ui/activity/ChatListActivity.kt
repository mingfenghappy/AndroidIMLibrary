package com.renyu.nimapp.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.renyu.nimapp.repository.Repos

class ChatListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        Repos.sendTextMessage("r17171708", "Hello2")
        Repos.queryMessageListEx(MessageBuilder.createEmptyMessage("r17171708", SessionTypeEnum.P2P, 0))
    }
}