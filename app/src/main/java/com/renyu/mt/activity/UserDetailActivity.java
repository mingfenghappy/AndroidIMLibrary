package com.renyu.mt.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.focustech.dbhelper.PlainTextDBHelper;
import com.focustech.tm.open.sdk.messages.protobuf.Enums;
import com.focustech.webtm.protocol.tm.message.model.BroadcastBean;
import com.focustech.webtm.protocol.tm.message.model.FriendGroupRsp;
import com.focustech.webtm.protocol.tm.message.model.FriendStatusRsp;
import com.focustech.webtm.protocol.tm.message.model.GetFriendRuleRsp;
import com.renyu.mt.R;
import com.focustech.webtm.protocol.tm.message.MTService;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/8/2.
 */

public class UserDetailActivity extends AppCompatActivity {

    @BindView(R.id.btn_userdetail_delete)
    Button btn_userdetail_delete;
    @BindView(R.id.btn_userdetail_add)
    Button btn_userdetail_add;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userdetail);
        ButterKnife.bind(this);

        IntentFilter filter=new IntentFilter();
        filter.addAction("MT");
        registerReceiver(registerReceiver, filter);

        boolean isFriend=false;
        ArrayList<FriendGroupRsp> friendGroupRsps= PlainTextDBHelper.getInstance().getFriendList();
        for (FriendGroupRsp friendGroupRsp : friendGroupRsps) {
            for (FriendStatusRsp friendStatusRsp : friendGroupRsp.getFriends()) {
                // 如果已经是好友，则显示删除好友按钮，否则显示添加好友按钮
                if (friendStatusRsp.getFriendUserId().equals(getIntent().getStringExtra("UserId"))) {
                    isFriend=true;
                    break;
                }
            }
        }
        if (isFriend) {
            btn_userdetail_delete.setVisibility(View.VISIBLE);
        }
        else {
            btn_userdetail_add.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(registerReceiver);
    }

    BroadcastReceiver registerReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("MT")) {
                BroadcastBean bean= (BroadcastBean) intent.getSerializableExtra("broadcast");
                if (bean.getCommand()== BroadcastBean.MTCommand.GetFriendRuleRsp) {
                    String userId = ((GetFriendRuleRsp) bean.getSerializable()).getUserId();
                    Enums.ValidateRule validateRule = ((GetFriendRuleRsp) bean.getSerializable()).getFriendRule();
                    // 无需认证直接添加
                    if (validateRule == Enums.ValidateRule.ALLOW_WITHOUT_VALIDATE) {
                        ArrayList<FriendGroupRsp> friendGroupRsps = PlainTextDBHelper.getInstance().getGroupInfo();
                        for (FriendGroupRsp friendGroupRsp : friendGroupRsps) {
                            if (friendGroupRsp.getFriendGroupType()== Enums.FriendGroupType.DEFAULT) {
                                MTService.addFriendReq(UserDetailActivity.this, userId, "", friendGroupRsp.getFriendGroupId());
                            }
                        }
                    }
                    // 需要二次认证，前往添加好友页面
                    else if (validateRule == Enums.ValidateRule.ALLOW_AFTER_VALIDATE) {

                    }
                    // 用户拒绝添加
                    else if (validateRule == Enums.ValidateRule.DENY) {

                    }
                }
                if (bean.getCommand()== BroadcastBean.MTCommand.DeleteFriendRsp) {
                    Toast.makeText(UserDetailActivity.this, "删除好友成功", Toast.LENGTH_SHORT).show();
                    finish();
                }
                if (bean.getCommand()== BroadcastBean.MTCommand.FriendInfoNty) {
                    Toast.makeText(UserDetailActivity.this, "添加好友成功", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    };

    @OnClick({R.id.btn_userdetail_delete, R.id.btn_userdetail_add})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_userdetail_delete:
                // TODO: 2017/8/2 返回速度相对比较慢，并且删除对方之后，对方的好友列表里面依然存在当前使用者
                MTService.deleteFriendReq(this, getIntent().getStringExtra("UserId"));
                break;
            case R.id.btn_userdetail_add:
                // TODO: 2017/8/8 加了好友之后，只是当前使用人与对方建立好友关系，但是对方并未与使用人建立好友关系
                MTService.getFriendRuleReq(this, getIntent().getStringExtra("UserId"));
                break;
        }
    }
}
