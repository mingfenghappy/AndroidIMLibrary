package com.renyu.mt.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.blankj.utilcode.util.Utils;
import com.focustech.dbhelper.PlainTextDBHelper;
import com.renyu.mt.service.MTService;
import com.focustech.message.model.BroadcastBean;
import com.focustech.message.model.FriendGroupRsp;
import com.focustech.message.model.FriendStatusRsp;
import com.focustech.message.model.GetFriendRuleRsp;
import com.focustech.tm.open.sdk.messages.protobuf.Enums;
import com.renyu.mt.MTApplication;
import com.renyu.mt.R;
import com.renyu.mt.base.BaseIMActivity;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/8/2.
 */

public class UserDetailActivity extends BaseIMActivity {

    @BindView(R.id.btn_userdetail_delete)
    Button btn_userdetail_delete;
    @BindView(R.id.btn_userdetail_add)
    Button btn_userdetail_add;

    BroadcastReceiver registerReceiver;

    @Override
    public void initParams() {
        registerReceiver =new BroadcastReceiver() {
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

    @OnClick({R.id.btn_userdetail_delete, R.id.btn_userdetail_add})
    public void onClick(View view) {
        if (((MTApplication) getApplication()).connState != BroadcastBean.MTCommand.Conn) {
            Toast.makeText(this, "服务器未连接成功", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (view.getId()) {
            case R.id.btn_userdetail_delete:
                // TODO: 2017/8/2 删除对方之后，对方的好友列表里面依然存在当前使用者
                MTService.deleteFriendReq(this, getIntent().getStringExtra("UserId"));
                break;
            case R.id.btn_userdetail_add:
                // TODO: 2017/8/8 加了好友之后，只是当前使用人与对方建立好友关系，但是对方并未与使用人建立好友关系
                MTService.getFriendRuleReq(this, getIntent().getStringExtra("UserId"));
                break;
        }
    }

    @Nullable
    @Override
    public BroadcastReceiver getReceiver() {
        return registerReceiver;
    }
}
