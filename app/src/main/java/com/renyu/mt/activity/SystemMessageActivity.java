package com.renyu.mt.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.focustech.dbhelper.PlainTextDBHelper;
import com.focustech.webtm.protocol.tm.message.model.SystemMessageBean;
import com.renyu.mt.MTApplication;
import com.renyu.mt.R;
import com.renyu.mt.adapter.SystemMessageAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/8/8.
 */

public class SystemMessageActivity extends AppCompatActivity {

    @BindView(R.id.rv_messagelist)
    RecyclerView rv_messagelist;
    SystemMessageAdapter adapter;

    ArrayList<SystemMessageBean> beans;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_systemmessage);
        ButterKnife.bind(this);

        beans=new ArrayList<>();
        beans.addAll(PlainTextDBHelper.getInstance().getSystemMessage());

        rv_messagelist.setHasFixedSize(true);
        LinearLayoutManager manager=new LinearLayoutManager(this);
        rv_messagelist.setLayoutManager(manager);
        adapter=new SystemMessageAdapter(this, beans);
        rv_messagelist.setAdapter(adapter);
    }
}
