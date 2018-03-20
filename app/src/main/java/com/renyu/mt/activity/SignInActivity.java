package com.renyu.mt.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.focustech.webtm.protocol.tm.message.model.BroadcastBean;
import com.focustech.webtm.protocol.tm.message.model.UserInfoRsp;
import com.renyu.commonlibrary.commonutils.ACache;
import com.renyu.mt.R;
import com.focustech.webtm.protocol.tm.message.HeartBeatService;
import com.focustech.webtm.protocol.tm.message.MTService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/7/18.
 */

public class SignInActivity extends AppCompatActivity {

    @BindView(R.id.ed_username)
    EditText ed_username;
    @BindView(R.id.ed_pwd)
    EditText ed_pwd;

    // app是否已经连接服务端成功
    boolean isMTConn=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        ButterKnife.bind(this);

        if (ACache.get(SignInActivity.this).getAsObject("UserInfoRsp")!=null) {
            UserInfoRsp userInfoRsp= (UserInfoRsp) ACache.get(SignInActivity.this).getAsObject("UserInfoRsp");
            ed_username.setText(userInfoRsp.getLoginUserName());
            ed_pwd.setText(userInfoRsp.getPwd());
        }

        ed_username.post(new Runnable() {
            @Override
            public void run() {
                // 连接服务端
                MTService.conn(SignInActivity.this);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter=new IntentFilter();
        filter.addAction("MT");
        registerReceiver(registerReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(registerReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(SignInActivity.this, MTService.class));
        stopService(new Intent(SignInActivity.this, HeartBeatService.class));
    }

    @OnClick({R.id.btn_signin})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_signin:
                if (isMTConn && !TextUtils.isEmpty(ed_username.getText().toString()) && !TextUtils.isEmpty(ed_pwd.getText().toString())) {
                    MTService.reqLogin(SignInActivity.this, ed_username.getText().toString(), ed_pwd.getText().toString());
                }
                break;
        }
    }

    // 广播回调
    BroadcastReceiver registerReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("MT")) {
                BroadcastBean bean= (BroadcastBean) intent.getSerializableExtra("broadcast");
                if (bean.getCommand()== BroadcastBean.MTCommand.Conn) {
                    isMTConn=true;
                    if (ACache.get(SignInActivity.this).getAsObject("UserInfoRsp")!=null) {
                        UserInfoRsp userInfoRsp= (UserInfoRsp) ACache.get(SignInActivity.this).getAsObject("UserInfoRsp");
                        MTService.reqLogin(SignInActivity.this, userInfoRsp.getLoginUserName(), userInfoRsp.getPwd());
                    }
                }
                if (bean.getCommand()== BroadcastBean.MTCommand.Disconn) {
                    Toast.makeText(SignInActivity.this, "断开连接", Toast.LENGTH_SHORT).show();
                }
                if (bean.getCommand()== BroadcastBean.MTCommand.LoginRsp) {
                    Toast.makeText(SignInActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                }
                // 此处为当前用户登录后返回的信息
                if (bean.getCommand()== BroadcastBean.MTCommand.UserInfoRsp) {
                    UserInfoRsp userInfoRsp= (UserInfoRsp) ((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable();
                    userInfoRsp.setLoginUserName(ed_username.getText().toString());
                    userInfoRsp.setPwd(ed_pwd.getText().toString());
                    ACache.get(SignInActivity.this).put("UserInfoRsp", userInfoRsp);

                    startActivity(new Intent(SignInActivity.this, IndexActivity.class));

                    // 连接成功发送心跳包
                    Intent hbs=new Intent(SignInActivity.this, HeartBeatService.class);
                    startService(hbs);
                }
                if (bean.getCommand()== BroadcastBean.MTCommand.LoginRspERROR) {
                    Toast.makeText(SignInActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        finish();
    }
}
