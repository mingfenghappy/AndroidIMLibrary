package com.renyu.easemobuilibrary.base;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.SPUtils;
import com.renyu.commonlibrary.baseact.BaseActivity;
import com.renyu.commonlibrary.views.dialog.ChoiceDialog;
import com.renyu.easemobuilibrary.params.CommonParams;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class BaseIMActivity extends BaseActivity {
    public boolean isPause = false;

    public BroadcastReceiver receiver = null;

    public String actionName = "";

    public void openCurrentReceiver() {
        if (receiver != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(actionName);
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

        // 广播action标志名称设置
        try {
            Class clazz = Class.forName("com.renyu.easemobapp.params.InitParams");
            actionName = clazz.getField("actionName").get(clazz).toString();
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

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
                    Class clazz = Class.forName("com.renyu.easemobapp.params.InitParams");

                    // 加载自定义的踢下线方法
                    Method kickoutFuncMethod = clazz.getDeclaredMethod("kickoutFunc");
                    kickoutFuncMethod.invoke(null);

                    // 固定的踢下线方法
                    String initActivityName = clazz.getField("InitActivityName").get(clazz).toString();
                    Class initClass = Class.forName(initActivityName);
                    Intent intent = new Intent(this, initClass);
                    intent.putExtra(CommonParams.TYPE, CommonParams.KICKOUT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
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
        if (TextUtils.isEmpty(SPUtils.getInstance().getString(CommonParams.SP_UNAME, "")) ||
                TextUtils.isEmpty(SPUtils.getInstance().getString(CommonParams.SP_PWD, ""))) {
            CommonParams.isKickout = true;
            finish();
            Log.d("EaseMobUtils", "回退到上一页");
            return true;
        }
        return false;
    }
}
