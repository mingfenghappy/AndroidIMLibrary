package com.renyu.easemob.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.Utils;
import com.renyu.easemob.params.InitParams;
import com.renyu.easemoblibrary.EaseMobUtils;
import com.renyu.easemoblibrary.manager.EMMessageManager;
import com.renyu.easemoblibrary.manager.GroupManager;
import com.renyu.easemoblibrary.model.BroadcastBean;
import com.renyu.easemobuilibrary.base.BaseIMActivity;
import com.renyu.easemobuilibrary.params.CommonParams;
import com.renyu.mt.R;

import butterknife.BindView;
import butterknife.OnClick;

public class SignInActivity extends BaseIMActivity {

    @BindView(R.id.ed_username)
    EditText ed_username;
    @BindView(R.id.ed_pwd)
    EditText ed_pwd;

    @Override
    public void initParams() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(InitParams.actionName)) {
                    if (intent.getSerializableExtra(BroadcastBean.COMMAND) == BroadcastBean.EaseMobCommand.LoginRsp) {
                        // 用户登录信息
                        SPUtils.getInstance().put(CommonParams.SP_UNAME, ed_username.getText().toString());
                        SPUtils.getInstance().put(CommonParams.SP_PWD, ed_pwd.getText().toString());
                        // 保证进入主页面后本地会话和群组都 load 完毕。
                        GroupManager.loadAllGroups();
                        EMMessageManager.loadAllConversations();
                        // 登录成功跳转首页
                        Intent intent2 = new Intent(SignInActivity.this, SplashActivity.class);
                        intent2.putExtra(CommonParams.TYPE, CommonParams.MAIN);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent2);
                        finish();
                    }
                    else if (intent.getSerializableExtra(BroadcastBean.COMMAND) == BroadcastBean.EaseMobCommand.LoginRspERROR) {
                        Toast.makeText(SignInActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
    }

    @Override
    public int initViews() {
        return R.layout.activity_signin;
    }

    @Override
    public void loadData() {
        ed_username.setText(SPUtils.getInstance().getString(CommonParams.SP_UNAME));
        ed_pwd.setText(SPUtils.getInstance().getString(CommonParams.SP_PWD));
    }

    @Override
    public int setStatusBarColor() {
        return Color.BLACK;
    }

    @Override
    public int setStatusBarTranslucent() {
        return 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        openCurrentReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCurrentReceiver();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.putExtra(CommonParams.TYPE, CommonParams.SIGNINBACK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @OnClick({R.id.btn_signin})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_signin:
                EaseMobUtils.login(Utils.getApp(), ed_username.getText().toString(), ed_pwd.getText().toString());
                break;
        }
    }
}
