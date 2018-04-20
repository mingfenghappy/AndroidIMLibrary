package com.renyu.tmbaseuilibrary.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.blankj.utilcode.util.Utils;
import com.renyu.commonlibrary.commonutils.NotificationUtils;
import com.renyu.tmbaseuilibrary.R;

import java.util.List;

public class CommonUtils {
    public static void playNewMessage(String ticker, String title, String content, int music, Intent intent) {
        NotificationCompat.Builder builder = NotificationUtils.getNotificationCenter(com.blankj.utilcode.util.Utils.getApp())
                .getSimpleBuilder(ticker, title, content, Color.RED,
                        R.drawable.ic_im_notification, R.drawable.ic_im_notification, NotificationUtils.channelDefaultId, intent);
        builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS);
        Uri sound=Uri.parse("android.resource://" + com.blankj.utilcode.util.Utils.getApp().getPackageName() + "/" + music);
        builder.setSound(sound);
        NotificationUtils.getNotificationCenter(com.blankj.utilcode.util.Utils.getApp()).getNotificationManager().notify(2001, builder.build());
    }

    public static void playNoVoiceMessage(String ticker, String title, String content) {
        NotificationCompat.Builder builder = NotificationUtils.getNotificationCenter(com.blankj.utilcode.util.Utils.getApp())
                .getSimpleBuilder(ticker, title, content, Color.RED,
                        R.drawable.ic_im_notification, R.drawable.ic_im_notification, NotificationUtils.channelDefaultId, new Intent());
        builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS);
        NotificationUtils.getNotificationCenter(com.blankj.utilcode.util.Utils.getApp()).getNotificationManager().notify(2000, builder.build());
    }

    public static boolean isServiceRunning(final String className) {
        ActivityManager am =
                (ActivityManager) Utils.getApp().getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return false;
        List<ActivityManager.RunningServiceInfo> info = am.getRunningServices(0x7FFFFFFF);
        if (info == null || info.size() == 0) return false;
        for (ActivityManager.RunningServiceInfo aInfo : info) {
            if (className.equals(aInfo.service.getClassName()) && aInfo.service.getPackageName().equals(Utils.getApp().getPackageName())) return true;
        }
        return false;
    }
}
