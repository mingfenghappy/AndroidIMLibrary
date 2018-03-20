package com.focustech.webtm.protocol.tm.message;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.focustech.R;
import com.focustech.webtm.protocol.tm.message.model.BroadcastBean;
import com.renyu.commonlibrary.commonutils.NotificationUtils;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/8/1.
 */

public class HeartBeatService extends Service {

    ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    // 上一次收到心跳返回的时间
    long lastTime=-1;
    // 总错误次数
    int errorTime=0;

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
        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
            NotificationUtils.getNotificationCenter(getApplicationContext()).showStartForeground(
                    this,
                    "提示",
                    "Push Service",
                    "Push Service",
                    R.color.colorPrimary,
                    intent.getExtras().getInt("smallIcon"),
                    intent.getExtras().getInt("largeIcon"),
                    1000);
        }
        if(null==scheduledThreadPoolExecutor){
            scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
            scheduledThreadPoolExecutor.scheduleAtFixedRate(() -> {
                // 首次开启不算做错误
                if (lastTime == -1) {
                    Log.d("HeartBeatService", "开启连接");
                    errorTime = 0;
                    lastTime = System.currentTimeMillis();
                    MTService.conn(HeartBeatService.this);
                }
                else {
                    // 超过10s秒则认为1次错误
                    if (System.currentTimeMillis()-lastTime>8*1000) {
                        errorTime++;
                        Log.d("HeartBeatService", "出现错误，错误次数为" + errorTime);
                    }
                    // 错误超过3次重新发起连接登录
                    if (errorTime >= 3) {
                        errorTime = 0;
                        lastTime = -1;
                        Log.d("HeartBeatService", "达到错误，准备下次重新连接");
                    }
                    else {
                        MTService.heartbeat(HeartBeatService.this);
                        Log.d("HeartBeatService", "发送心跳包");
                    }
                }

            }, 1, 5, TimeUnit.SECONDS);
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
                    lastTime=System.currentTimeMillis();
                    errorTime=0;
                    Log.d("HeartBeatService", "收到心跳包，重置");
                }
            }
        }
    };
}
