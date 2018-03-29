package com.renyu.mt.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.blankj.utilcode.util.Utils;
import com.focustech.dbhelper.PlainTextDBHelper;
import com.focustech.message.model.BroadcastBean;
import com.focustech.message.model.SystemMessageBean;
import com.renyu.mt.R;
import com.renyu.mt.adapter.SystemMessageAdapter;
import com.renyu.mt.base.BaseIMActivity;
import com.renyu.mt.params.CommonParams;

import java.util.ArrayList;

import butterknife.BindView;

/**
 * Created by Administrator on 2017/8/8.
 */

public class SystemMessageActivity extends BaseIMActivity {

    @BindView(R.id.rv_messagelist)
    RecyclerView rv_messagelist;
    SystemMessageAdapter adapter;

    ArrayList<SystemMessageBean> beans;

    @Override
    public void initParams() {
        beans=new ArrayList<>();
        beans.addAll(PlainTextDBHelper.getInstance(Utils.getApp()).getSystemMessage());

        rv_messagelist.setHasFixedSize(true);
        rv_messagelist.setLayoutManager(new LinearLayoutManager(this));
        adapter=new SystemMessageAdapter(this, beans);
        rv_messagelist.setAdapter(adapter);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("MT")) {
                    BroadcastBean bean = (BroadcastBean) intent.getSerializableExtra("broadcast");
                    if (bean.getCommand() == BroadcastBean.MTCommand.MessageReceive) {
                        // 过滤掉非系统消息
                        if (((((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable()) instanceof SystemMessageBean)) {
                            SystemMessageBean systemMessageBean = (SystemMessageBean) ((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable();
                            beans.add(0, systemMessageBean);
                            adapter.notifyDataSetChanged();
                        }
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
    }

    @Override
    public int initViews() {
        return R.layout.activity_systemmessage;
    }

    @Override
    public void loadData() {

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
