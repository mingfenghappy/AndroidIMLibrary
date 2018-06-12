package com.renyu.easemobuilibrary.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;

import com.blankj.utilcode.util.Utils;
import com.renyu.easemobuilibrary.R;
import com.renyu.easemobuilibrary.base.BaseIMActivity;
import com.renyu.easemobuilibrary.manager.ContactManager;
import com.renyu.easemobuilibrary.model.BroadcastBean;
import com.renyu.easemobuilibrary.params.CommonParams;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Administrator on 2017/8/2.
 */

public class UserDetailActivity extends BaseIMActivity {

    Button btn_userdetail_delete;
    Button btn_userdetail_add;

    @Override
    public void initParams() {
        btn_userdetail_delete = findViewById(R.id.btn_userdetail_delete);
        btn_userdetail_delete.setOnClickListener(v -> ContactManager.deleteContact(getIntent().getStringExtra("UserId")));
        btn_userdetail_add = findViewById(R.id.btn_userdetail_add);
        btn_userdetail_add.setOnClickListener(v -> ContactManager.addContact(getIntent().getStringExtra("UserId"), "hi"));

        receiver =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(actionName)) {
                    // 删除了联系人
                    if (intent.getSerializableExtra(BroadcastBean.COMMAND) == BroadcastBean.EaseMobCommand.onContactDeleted) {
                        UserDetailActivity.this.setResult(RESULT_OK, UserDetailActivity.this.getIntent());
                        finish();
                    }
                    // 增加了联系人
                    if (intent.getSerializableExtra(BroadcastBean.COMMAND) == BroadcastBean.EaseMobCommand.onContactAdded) {
                        // 刷新好友关系
                        refreshFriendState();
                    }
                    // 被踢下线
                    if (intent.getSerializableExtra(BroadcastBean.COMMAND) == BroadcastBean.EaseMobCommand.Kickout) {
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

    /**
     * 更新好友状态
     */
    private void refreshFriendState() {
        btn_userdetail_add.setVisibility(View.GONE);
        btn_userdetail_delete.setVisibility(View.GONE);
        Observable.create((ObservableOnSubscribe<List<String>>) emitter -> {
            List<String> friendGroupRsps= ContactManager.getAllContactsFromServer();
            emitter.onNext(friendGroupRsps);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<List<String>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(List<String> strings) {
                boolean isFriend=false;
                for (String string : strings) {
                    if (string.equals(getIntent().getStringExtra("UserId"))) {
                        isFriend=true;
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
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    public int initViews() {
        return R.layout.activity_userdetail;
    }

    @Override
    public void loadData() {
        refreshFriendState();
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
