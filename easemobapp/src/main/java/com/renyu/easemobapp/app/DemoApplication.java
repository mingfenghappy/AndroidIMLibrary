package com.renyu.easemobapp.app;

import android.content.Intent;

import com.renyu.easemobapp.activity.ConversationActivity;
import com.renyu.easemobuilibrary.app.EaseMobApplication;

public class DemoApplication extends EaseMobApplication {
    @Override
    public Intent getNotificationIntent(String userId) {
        Intent intent=new Intent(this, ConversationActivity.class);
        intent.putExtra("UserId", userId);
        intent.putExtra("isGroup", false);
        return intent;
    }
}
