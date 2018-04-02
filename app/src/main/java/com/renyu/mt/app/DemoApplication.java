package com.renyu.mt.app;

import android.content.Intent;

import com.renyu.tmbaseuilibrary.app.MTApplication;

public class DemoApplication extends MTApplication {
    @Override
    public Intent getNotificationIntent() {
        // TODO: 2018/4/2 0002 会话列表或者会话详情页面
        return new Intent();
    }
}
