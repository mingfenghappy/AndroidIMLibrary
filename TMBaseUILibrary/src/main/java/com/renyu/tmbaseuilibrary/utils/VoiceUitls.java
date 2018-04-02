package com.renyu.tmbaseuilibrary.utils;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.blankj.utilcode.util.Utils;
import com.renyu.commonlibrary.commonutils.NotificationUtils;
import com.renyu.tmbaseuilibrary.R;

public class VoiceUitls {
    public static void playNewMessage(String ticker, String title, String content, int music, Intent intent) {
        NotificationCompat.Builder builder = NotificationUtils.getNotificationCenter(Utils.getApp())
                .getSimpleBuilder(ticker, title, content, Color.RED,
                        R.drawable.ic_im_notification, R.drawable.ic_im_notification, NotificationUtils.channelDefaultId, intent);
        builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS);
        Uri sound=Uri.parse("android.resource://" + Utils.getApp().getPackageName() + "/" + music);
        builder.setSound(sound);
        NotificationUtils.getNotificationCenter(Utils.getApp()).getNotificationManager().notify(2001, builder.build());
    }

    public static void playNoVoiceMessage(String ticker, String title, String content) {
        NotificationCompat.Builder builder = NotificationUtils.getNotificationCenter(Utils.getApp())
                .getSimpleBuilder(ticker, title, content, Color.RED,
                        R.drawable.ic_im_notification, R.drawable.ic_im_notification, NotificationUtils.channelDefaultId, new Intent());
        builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS);
        NotificationUtils.getNotificationCenter(Utils.getApp()).getNotificationManager().notify(2000, builder.build());
    }
}
