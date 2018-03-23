package com.renyu.mt.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SPUtils;
import com.focustech.webtm.protocol.tm.message.MTService;
import com.focustech.webtm.protocol.tm.message.model.BroadcastBean;
import com.focustech.webtm.protocol.tm.message.model.UserInfoRsp;
import com.renyu.commonlibrary.commonutils.ACache;
import com.renyu.commonlibrary.params.InitParams;
import com.renyu.mt.MTApplication;
import com.renyu.mt.R;
import com.renyu.mt.base.BaseIMActivity;
import com.renyu.mt.params.CommonParams;

import org.jetbrains.annotations.Nullable;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/7/18.
 */

public class SignInActivity extends BaseIMActivity {

    @BindView(R.id.ed_username)
    EditText ed_username;
    @BindView(R.id.ed_pwd)
    EditText ed_pwd;

    BroadcastReceiver mReceiver;

    @Override
    public void initParams() {
        // 初始化文件夹
        FileUtils.createOrExistsDir(InitParams.IMAGE_PATH);
        FileUtils.createOrExistsDir(InitParams.HOTFIX_PATH);
        FileUtils.createOrExistsDir(InitParams.FILE_PATH);
        FileUtils.createOrExistsDir(InitParams.LOG_PATH);
        FileUtils.createOrExistsDir(InitParams.CACHE_PATH);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("MT")) {
                    BroadcastBean bean= (BroadcastBean) intent.getSerializableExtra("broadcast");
                    // 此处为当前用户登录后返回的信息
                    if (bean.getCommand()== BroadcastBean.MTCommand.UserInfoRsp) {
                        UserInfoRsp userInfoRsp= (UserInfoRsp) ((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable();
                        ACache.get(SignInActivity.this).put("UserInfoRsp", userInfoRsp);
                        // 用户登录信息
                        SPUtils.getInstance().put(CommonParams.SP_UNAME, ed_username.getText().toString());
                        SPUtils.getInstance().put(CommonParams.SP_PWD, ed_pwd.getText().toString());
                        // 登录成功跳转首页
                        startActivity(new Intent(SignInActivity.this, ChatListActivity.class));
                    }
                    if (bean.getCommand()== BroadcastBean.MTCommand.LoginRspERROR) {
                        Toast.makeText(SignInActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        if (ACache.get(SignInActivity.this).getAsObject("UserInfoRsp") != null) {
            // 登录成功跳转首页
            startActivity(new Intent(SignInActivity.this, ChatListActivity.class));
        }
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

    @OnClick({R.id.btn_signin})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_signin:
                if (((MTApplication) getApplication()).connState == BroadcastBean.MTCommand.Conn &&
                        !TextUtils.isEmpty(ed_username.getText().toString()) &&
                        !TextUtils.isEmpty(ed_pwd.getText().toString())) {
                    MTService.reqLogin(getApplicationContext(), ed_username.getText().toString(), ed_pwd.getText().toString());
                }
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        finish();
    }

    @Nullable
    @Override
    public BroadcastReceiver getReceiver() {
        return mReceiver;
    }
}
