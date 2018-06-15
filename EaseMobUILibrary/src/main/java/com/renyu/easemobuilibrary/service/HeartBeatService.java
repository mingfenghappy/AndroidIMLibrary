package com.renyu.easemobuilibrary.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.hyphenate.chat.EMClient;
import com.renyu.commonlibrary.commonutils.NotificationUtils;
import com.renyu.easemobuilibrary.manager.EMMessageManager;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/8/1.
 */

public class HeartBeatService extends Service {

    ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int notificationIcon = 0;
        try {
            Class clazz = Class.forName("com.renyu.mt.params.InitParams");
            notificationIcon = Integer.parseInt(clazz.getField("notificationIcon").get(clazz).toString());
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
            NotificationUtils.getNotificationCenter(getApplicationContext()).showStartForeground(
                    this,
                    "提示",
                    "Push Service",
                    "Push Service",
                    Color.WHITE,
                    notificationIcon,
                    notificationIcon,
                    1000);
        }

        if(null==scheduledThreadPoolExecutor){
            Log.d("EaseMobUtils", "初始化线程池");
            scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
            scheduledThreadPoolExecutor.scheduleAtFixedRate(() -> {
                boolean isConnected = EMClient.getInstance().isConnected();
                Log.d("EaseMobUtils", "isConnected:" + isConnected);
                if (!isConnected && EMClient.getInstance().isLoggedInBefore()) {
                    EMMessageManager.sendSingleMessage(EMMessageManager.prepareCMDMessage("conn", "admin"));
                }
            }, 1, 10, TimeUnit.SECONDS);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (scheduledThreadPoolExecutor!=null) {
            scheduledThreadPoolExecutor.shutdownNow();
        }
        // android o 关闭后台服务
        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
            NotificationUtils.getNotificationCenter(getApplicationContext()).hideStartForeground(this, 1000);
        }
    }
}
