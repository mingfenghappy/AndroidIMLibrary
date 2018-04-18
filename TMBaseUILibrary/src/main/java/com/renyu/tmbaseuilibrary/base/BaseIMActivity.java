package com.renyu.tmbaseuilibrary.base;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import com.blankj.utilcode.util.ServiceUtils;
import com.renyu.commonlibrary.baseact.BaseActivity;
import com.renyu.commonlibrary.commonutils.ACache;
import com.renyu.commonlibrary.views.dialog.ChoiceDialog;
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

    public void kickout() {
        new Handler().post(() -> {
            ChoiceDialog dialog = ChoiceDialog.getInstanceByTextCommit("已经下线", "确定");
            dialog.setOnDialogPosListener(() -> {
                CommonParams.isKickout = false;
                try {
                    Class clazz = Class.forName("com.renyu.mt.params.InitParams");
                    String initActivityName = clazz.getField("InitActivityName").get(clazz).toString();

                    Class initClass = Class.forName(initActivityName);
                    Intent intent = new Intent(this, initClass);
                    intent.putExtra(CommonParams.TYPE, CommonParams.KICKOUT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
                    e.printStackTrace();
                }

            });
            dialog.show(BaseIMActivity.this);
        });
    }

    /**
     * 存在回收之后再次回收，造成下线标志位出错
     * @return
     */
    public boolean checkNullInfo() {
        // 存在回收之后再次回收，造成下线标志位出错
        if (ACache.get(this).getAsObject("UserInfoRsp") == null) {
            CommonParams.isKickout = true;
            finish();
            Log.d("MTAPP", "回退到上一页");
            return true;
        }
        return false;
    }
}
