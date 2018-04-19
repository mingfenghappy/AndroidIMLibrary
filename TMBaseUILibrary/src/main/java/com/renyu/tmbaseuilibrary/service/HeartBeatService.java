package com.renyu.tmbaseuilibrary.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.focustech.message.model.BroadcastBean;
import com.focustech.params.FusionField;
import com.renyu.commonlibrary.commonutils.NotificationUtils;
import com.renyu.tmbaseuilibrary.app.MTApplication;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/8/1.
 */

public class HeartBeatService extends Service {

    ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    // 心跳包计数器
    long sendCount=0;
    // 下一次认定连接断开的截止时间
    long failureTime=0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter filter=new IntentFilter();
        filter.addAction("MT");
        registerReceiver(registerReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 修正sendCount指令
        if (intent.getIntExtra("from", -1) == 1) {
            sendCount = 0;
            return super.onStartCommand(intent, flags, startId);
        }

        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
            NotificationUtils.getNotificationCenter(getApplicationContext()).showStartForeground(
                    this,
                    "提示",
                    "Push Service",
                    "Push Service",
                    Color.WHITE,
                    intent.getExtras().getInt("smallIcon"),
                    intent.getExtras().getInt("largeIcon"),
                    1000);
        }
        if(null==scheduledThreadPoolExecutor){
            Log.d("MTAPP", "初始化线程池");
            scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
            scheduledThreadPoolExecutor.scheduleAtFixedRate(() -> {
                if (((MTApplication) getApplicationContext()).connState != BroadcastBean.MTCommand.Conn || sendCount == 0) {
                    // 重置相关数据
                    sendCount = 1;
                    failureTime = System.currentTimeMillis() + FusionField.heartBeatSendInterval*1000*FusionField.retryTime;
                    // 开始连接
                    Log.d("MTAPP", "正在连接");
                    MTService.conn(getApplicationContext());
                    return;
                }
                // 判断是否发生连接断开
                if (System.currentTimeMillis()-failureTime > 0) {
                    sendCount = 0;
                    Log.d("MTAPP", "达到错误，准备下次重新连接");
                }
                else {
                    // 发送心跳数据
                    int interval = FusionField.heartBeatSendInterval/FusionField.heartBeatThreadInterval;
                    if (sendCount%interval == 0) {
                        Log.d("MTAPP", "发送心跳包");
                        MTService.heartbeat(getApplicationContext());
                    }
                    sendCount++;
                }
            }, 1, FusionField.heartBeatThreadInterval, TimeUnit.SECONDS);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(registerReceiver);
        if (scheduledThreadPoolExecutor!=null) {
            scheduledThreadPoolExecutor.shutdownNow();
        }
        // android o 关闭后台服务
        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
            NotificationUtils.getNotificationCenter(getApplicationContext()).hideStartForeground(this, 1000);
        }
    }

    BroadcastReceiver registerReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("MT")) {
                BroadcastBean bean = (BroadcastBean) intent.getSerializableExtra("broadcast");
                if (bean.getCommand() == BroadcastBean.MTCommand.HeartBeat ||
                        bean.getCommand() == BroadcastBean.MTCommand.Conn) {
                    // 重置相关数据
                    failureTime = System.currentTimeMillis() + FusionField.heartBeatSendInterval*1000*FusionField.retryTime;
                    Log.d("MTAPP", "收到心跳包，重置");
                }
            }
        }
    };
}
