package com.focustech.webtm.protocol.tm.message;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.focustech.webtm.protocol.tm.message.model.BroadcastBean;
import com.focustech.webtm.protocol.tm.message.model.UserInfoRsp;
import com.renyu.commonlibrary.commonutils.ACache;

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
        if(null==scheduledThreadPoolExecutor){
            scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
            scheduledThreadPoolExecutor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    // 超过10s秒则认为1次错误
                    if (System.currentTimeMillis()-lastTime>20*1000) {
                        errorTime++;
                        Log.d("HeartBeatService", "出现错误，错误次数为" + errorTime);
                    }
                    // 错误超过3次重新发起连接登录
                    if (errorTime>3) {
                        MTService.conn(HeartBeatService.this);
                        Log.d("HeartBeatService", "达到错误，开始重新连接");
                    }
                    else {
                        MTService.heartbeat(HeartBeatService.this);
                        Log.d("HeartBeatService", "发送心跳包");
                    }
                }
            }, 5, 10, TimeUnit.SECONDS);
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
    }

    BroadcastReceiver registerReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("MT")) {
                BroadcastBean bean = (BroadcastBean) intent.getSerializableExtra("broadcast");
                if (bean.getCommand()== BroadcastBean.MTCommand.Conn) {
                    if (ACache.get(HeartBeatService.this).getAsObject("UserInfoRsp")!=null) {
                        UserInfoRsp userInfoRsp= (UserInfoRsp) ACache.get(HeartBeatService.this).getAsObject("UserInfoRsp");
                        MTService.reqLogin(HeartBeatService.this, userInfoRsp.getLoginUserName(), userInfoRsp.getPwd());
                    }
                }
                if (bean.getCommand() == BroadcastBean.MTCommand.HeartBeat) {
                    lastTime=System.currentTimeMillis();
                    errorTime=0;
                    Log.d("HeartBeatService", "收到心跳包，重置");
                }
                if (bean.getCommand()== BroadcastBean.MTCommand.LoginRsp) {
                    lastTime=System.currentTimeMillis();
                    errorTime=0;
                }
            }
        }
    };
}
