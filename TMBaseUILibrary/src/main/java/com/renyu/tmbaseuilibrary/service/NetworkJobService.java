package com.renyu.tmbaseuilibrary.service;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.Utils;
import com.focustech.message.model.BroadcastBean;
import com.renyu.tmbaseuilibrary.app.MTApplication;

/**
 * Created by Administrator on 2018/4/19.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class NetworkJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        if (NetworkUtils.isConnected() && ((MTApplication) getApplicationContext()).connState != BroadcastBean.MTCommand.Conn) {
            // 开启心跳服务并进行连接
            Intent intent = new Intent(this, HeartBeatService.class);
            intent.putExtra("from", 1);
            if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
                startForegroundService(intent);
            }
            else {
                startService(intent);
            }
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void scheduleJob(){
        JobScheduler jobScheduler = (JobScheduler) Utils.getApp().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(Utils.getApp().getPackageName(), NetworkJobService.class.getName()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPeriodic(JobInfo.getMinPeriodMillis(), JobInfo.getMinFlexMillis());
        }
        else {
            builder.setPeriodic(3 * 60 * 1000);
        }
        builder.setPersisted(true);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        jobScheduler.schedule(builder.build());
    }
}
