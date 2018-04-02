package com.renyu.tmbaseuilibrary.base;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.blankj.utilcode.util.ServiceUtils;
import com.renyu.commonlibrary.baseact.BaseActivity;
import com.renyu.tmbaseuilibrary.R;
import com.renyu.tmbaseuilibrary.params.CommonParams;
import com.renyu.tmbaseuilibrary.service.HeartBeatService;

public abstract class BaseIMActivity extends BaseActivity {
    public boolean isPause = false;

    public BroadcastReceiver receiver = null;

    public void openCurrentReceiver() {
        if (receiver != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("MT");
            registerReceiver(receiver, filter);
        }
    }

    public void closeCurrentReceiver() {
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            CommonParams.isRestore = true;
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 开启心跳服务并进行连接
        if (!ServiceUtils.isServiceRunning("com.renyu.tmbaseuilibrary.service.HeartBeatService")) {
            if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
                Intent intent = new Intent(this, HeartBeatService.class);
                intent.putExtra("smallIcon", R.drawable.ic_im_notification);
                intent.putExtra("largeIcon", R.drawable.ic_im_notification);
                startForegroundService(intent);
            } else {
                startService(new Intent(this, HeartBeatService.class));
            }
        }

        isPause = false;

        if (CommonParams.isKickout) {
            kickout();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        isPause = true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isRestore", true);
    }

    abstract public void kickout();
}
