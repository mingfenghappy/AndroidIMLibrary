package com.renyu.mt.service;

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
import com.focustech.params.FusionField;
import com.focustech.message.MTService;
import com.focustech.message.model.BroadcastBean;
import com.renyu.commonlibrary.commonutils.NotificationUtils;
import com.renyu.mt.MTApplication;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/8/1.
 */

public class HeartBeatService extends Service {

    ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    // 上一次收到心跳包返回的时间
    long lastReceiveTime=-1;
    // 心跳包计数器
    long sendCount=0;
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
            Log.d("MTAPP", "初始化线程池");
            scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
            scheduledThreadPoolExecutor.scheduleAtFixedRate(() -> {
                // 首次开启不算做错误
                if (((MTApplication) getApplicationContext()).connState != BroadcastBean.MTCommand.Conn ||
                        sendCount == 0) {
                    Log.d("MTAPP", "算是首次开启连接");
                    // 重置相关数据
                    errorTime = 0;
                    sendCount = 1;
                    lastReceiveTime = System.currentTimeMillis();
                    MTService.conn(getApplicationContext());
                    return;
                }
                // 每30s发送心跳数据
                int interval = FusionField.heartBeatSendInterval/FusionField.heartBeatThreadInterval;
                if (sendCount%interval == 0) {
                    Log.d("MTAPP", "发送心跳包");
                    MTService.heartbeat(getApplicationContext());
                }
                // 超过30s之后判定一次失败
                // TODO: 2018/3/26 0026 偷个懒
                if (System.currentTimeMillis()-lastReceiveTime > FusionField.heartBeatSendInterval*1000) {
                    errorTime++;
                    Log.d("MTAPP", "当前出现错误，错误次数为" + errorTime);
                }
                sendCount++;
                // 错误超过3次重新发起连接登录
                if (errorTime >= FusionField.retryTime) {
                    errorTime = 0;
                    sendCount = 0;
                    lastReceiveTime = System.currentTimeMillis();
                    Log.d("MTAPP", "达到错误，准备下次重新连接");
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
                    lastReceiveTime=System.currentTimeMillis();
                    errorTime=0;
                    Log.d("MTAPP", "收到心跳包，重置");
                }
            }
        }
    };
}
