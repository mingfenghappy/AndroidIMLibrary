package com.renyu.mt.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.Utils;
import com.focustech.dbhelper.PlainTextDBHelper;
import com.focustech.message.model.BroadcastBean;
import com.focustech.message.model.UserInfoRsp;
import com.renyu.commonlibrary.commonutils.ACache;
import com.renyu.mt.R;
import com.renyu.mt.params.InitParams;
import com.renyu.tmbaseuilibrary.app.MTApplication;
import com.renyu.tmbaseuilibrary.base.BaseIMActivity;
import com.renyu.tmbaseuilibrary.params.CommonParams;
import com.renyu.tmbaseuilibrary.service.MTService;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/7/18.
 * 中控页，此处需要根据不同流程进行不同的定制。例如腾讯QQ跟链家两个App的IM的流程是不同的，一个需强制登录成功，一个不需要登录成功
 */

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
                    BroadcastBean bean= (BroadcastBean) intent.getSerializableExtra("broadcast");
                    // 此处为当前用户登录后返回的信息
                    if (bean.getCommand()== BroadcastBean.MTCommand.UserInfoRsp) {
                        UserInfoRsp userInfoRsp= (UserInfoRsp) ((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable();
                        ACache.get(Utils.getApp()).put("UserInfoRsp", userInfoRsp);
                        // 用户登录信息
                        SPUtils.getInstance().put(CommonParams.SP_UNAME, ed_username.getText().toString());
                        SPUtils.getInstance().put(CommonParams.SP_PWD, ed_pwd.getText().toString());
//                        SPUtils.getInstance().put(CommonParams.SP_PWD, GostDesBasae64Encrypter.encode(ed_username.getText().toString(), FusionField.key32));
                        // 登录成功跳转首页
                        Intent intent2 = new Intent(SignInActivity.this, SplashActivity.class);
                        intent2.putExtra(CommonParams.TYPE, CommonParams.MAIN);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent2);
                        finish();
                    }
                    if (bean.getCommand()== BroadcastBean.MTCommand.LoginRspERROR) {
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

    @OnClick({R.id.btn_signin})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_signin:
                if (((MTApplication) getApplication()).connState == BroadcastBean.MTCommand.Conn &&
                        !TextUtils.isEmpty(ed_username.getText().toString())) {
                    // 删除旧数据
                    PlainTextDBHelper.getInstance(Utils.getApp()).clearAllInfo();

//                    MTService.reqLogin(getApplicationContext(), ed_username.getText().toString(),
//                            GostDesBasae64Encrypter.encode(ed_username.getText().toString(), FusionField.key32));

                    MTService.reqLogin(getApplicationContext(), ed_username.getText().toString(),
                            ed_pwd.getText().toString());
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.putExtra(CommonParams.TYPE, CommonParams.SIGNINBACK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
