package com.renyu.tmuilibrary.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.blankj.utilcode.util.Utils;
import com.focustech.dbhelper.PlainTextDBHelper;
import com.focustech.message.model.BroadcastBean;
import com.focustech.message.model.FriendGroupRsp;
import com.focustech.message.model.FriendStatusRsp;
import com.focustech.message.model.GetFriendRuleRsp;
import com.focustech.tm.open.sdk.messages.protobuf.Enums;
import com.renyu.tmbaseuilibrary.app.MTApplication;
import com.renyu.tmbaseuilibrary.base.BaseIMActivity;
import com.renyu.tmbaseuilibrary.params.CommonParams;
import com.renyu.tmbaseuilibrary.service.MTService;
import com.renyu.tmuilibrary.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/8/2.
 */

public class UserDetailActivity extends BaseIMActivity {

    Button btn_userdetail_delete;
    Button btn_userdetail_add;

    @Override
    public void initParams() {
        // 存在回收之后再次回收，造成下线标志位出错
        if (checkNullInfo()) {
            return;
        }

        btn_userdetail_delete = findViewById(R.id.btn_userdetail_delete);
        btn_userdetail_delete.setOnClickListener(v -> {
            if (((MTApplication) getApplication()).connState != BroadcastBean.MTCommand.Conn) {
                Toast.makeText(UserDetailActivity.this, "服务器未连接成功", Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO: 2017/8/2 删除对方之后，对方的好友列表里面依然存在当前使用者
            MTService.deleteFriendReq(UserDetailActivity.this, getIntent().getStringExtra("UserId"));
        });
        btn_userdetail_add = findViewById(R.id.btn_userdetail_add);
        btn_userdetail_add.setOnClickListener(v -> {
            if (((MTApplication) getApplication()).connState != BroadcastBean.MTCommand.Conn) {
                Toast.makeText(UserDetailActivity.this, "服务器未连接成功", Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO: 2017/8/8 加了好友之后，只是当前使用人与对方建立好友关系，但是对方并未与使用人建立好友关系
            MTService.getFriendRuleReq(UserDetailActivity.this, getIntent().getStringExtra("UserId"));
        });

        receiver =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("MT")) {
                    BroadcastBean bean= (BroadcastBean) intent.getSerializableExtra("broadcast");
                    // 收到好友列表信息，刷新好友关系
                    if (bean.getCommand() == BroadcastBean.MTCommand.FriendGroupsRsp) {
                        // 清除所有好友组关系数据
                        PlainTextDBHelper.getInstance(Utils.getApp()).clearAllFriendList();
                        // 增加所有好友组关系数据
                        PlainTextDBHelper.getInstance(Utils.getApp()).insertFriendList((ArrayList<FriendGroupRsp>) (((BroadcastBean) (intent.getSerializableExtra("broadcast"))).getSerializable()));
                        // 刷新好友关系
                        refreshFriendState();
                    }
                    if (bean.getCommand()== BroadcastBean.MTCommand.GetFriendRuleRsp) {
                        String userId = ((GetFriendRuleRsp) bean.getSerializable()).getUserId();
                        Enums.ValidateRule validateRule = ((GetFriendRuleRsp) bean.getSerializable()).getFriendRule();
                        // 无需认证直接添加
                        if (validateRule == Enums.ValidateRule.ALLOW_WITHOUT_VALIDATE) {
                            ArrayList<FriendGroupRsp> friendGroupRsps = PlainTextDBHelper.getInstance(Utils.getApp()).getGroupInfo();
                            for (FriendGroupRsp friendGroupRsp : friendGroupRsps) {
                                if (friendGroupRsp.getFriendGroupType()== Enums.FriendGroupType.DEFAULT) {
                                    MTService.addFriendReq(UserDetailActivity.this, userId, "", friendGroupRsp.getFriendGroupId());
                                    return;
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
                    // 删除好友
                    if (bean.getCommand()== BroadcastBean.MTCommand.DeleteFriendRsp) {
                        finish();
                    }
                    // 添加与被添加好友
                    if (bean.getCommand()== BroadcastBean.MTCommand.RefreshFriendList) {
                        // 刷新好友关系
                        refreshFriendState();
                    }
                    // 被踢下线
                    if (bean.getCommand() == BroadcastBean.MTCommand.Kickout) {
                        CommonParams.isKickout = true;
                        if (!isPause) {
                            kickout();
                        }
                    }
                }
            }
        };
        openCurrentReceiver();

        // 刷新好友关系
        refreshFriendState();
    }

    /**
     * 更新好友状态
     */
    private void refreshFriendState() {
        btn_userdetail_add.setVisibility(View.GONE);
        btn_userdetail_delete.setVisibility(View.GONE);
        boolean isFriend=false;
        ArrayList<FriendGroupRsp> friendGroupRsps= PlainTextDBHelper.getInstance(Utils.getApp()).getFriendList();
        outer:
        for (FriendGroupRsp friendGroupRsp : friendGroupRsps) {
            for (FriendStatusRsp friendStatusRsp : friendGroupRsp.getFriends()) {
                // 如果已经是好友，则显示删除好友按钮，否则显示添加好友按钮
                if (friendStatusRsp.getFriendUserId().equals(getIntent().getStringExtra("UserId"))) {
                    isFriend=true;
                    break outer;
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
    public int initViews() {
        return R.layout.activity_userdetail;
    }

    @Override
    public void loadData() {
        // 加载远程好友列表
        MTService.reqFriendGroups(this);
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
    protected void onDestroy() {
        super.onDestroy();
        closeCurrentReceiver();
    }
}
