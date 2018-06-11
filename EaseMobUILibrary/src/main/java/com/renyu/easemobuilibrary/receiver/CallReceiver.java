package com.renyu.easemobuilibrary.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.blankj.utilcode.util.SPUtils;
import com.renyu.easemobuilibrary.activity.VideoCallActivity;
import com.renyu.easemobuilibrary.activity.VoiceCallActivity;
import com.renyu.easemobuilibrary.params.CommonParams;

public class CallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.isEmpty(SPUtils.getInstance().getString(CommonParams.SP_UNAME))) {
            return;
        }
        //username
        String from = intent.getStringExtra("from");
        //call type
        String type = intent.getStringExtra("type");
        if("video".equals(type)){
            //video call
            context.startActivity(new Intent(context, VideoCallActivity.class).
                    putExtra("username", from).putExtra("isComingCall", true).
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } else {
            //voice call
            context.startActivity(new Intent(context, VoiceCallActivity.class).
                    putExtra("username", from).putExtra("isComingCall", true).
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }
}
