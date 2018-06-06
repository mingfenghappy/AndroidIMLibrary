package com.renyu.easemob.app;

import android.content.Intent;

import com.renyu.easemobuilibrary.app.EaseMobApplication;

public class DemoApplication extends EaseMobApplication {
    @Override
    public Intent getNotificationIntent(String userId) {
        return null;
    }
}
