package com.renyu.mt.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.focustech.dbhelper.PlainTextDBHelper;
import com.focustech.tm.open.sdk.messages.protobuf.Enums;
import com.focustech.webtm.protocol.tm.message.model.MessageBean;
import com.focustech.webtm.protocol.tm.message.model.UpdateUserStatusNty;
import com.focustech.webtm.protocol.tm.message.model.UserInfoRsp;
import com.renyu.mt.R;
import com.renyu.mt.activity.IndexActivity;
import com.renyu.mt.adapter.ConversationListAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/7/20.
 */

public class ConversationFragment extends Fragment {

    @BindView(R.id.swipe_conversationlist)
    SwipeRefreshLayout swipe_conversationlist;
    @BindView(R.id.rv_conversationlist)
    RecyclerView rv_conversationlist;
    ConversationListAdapter adapter;

    // 离线消息
    ArrayList<MessageBean> offlineMessages;
    // 用户信息
    HashMap<String, UserInfoRsp> userInfoRsps;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_conversation, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        offlineMessages=new ArrayList<>();
        userInfoRsps= PlainTextDBHelper.getInstance().getFriendsInfo();

        rv_conversationlist.setHasFixedSize(true);
        rv_conversationlist.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter=new ConversationListAdapter(getActivity(), offlineMessages);
        rv_conversationlist.setAdapter(adapter);
        swipe_conversationlist.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!((IndexActivity) getActivity()).isLoadOfflineData) {
                    ((IndexActivity) getActivity()).needReqGetOfflineMessage();
                }
                else {
                    swipe_conversationlist.setRefreshing(false);
                }
            }
        });
        refreshOfflineMessageData();
    }

    /**
     * 收到新的消息之后进行刷新
     */
    public ArrayList<MessageBean> refreshOfflineMessageData() {
        ArrayList<MessageBean> temp = PlainTextDBHelper.getInstance().getConversationList();
        for (MessageBean offlineMessage : temp) {
            if (userInfoRsps.containsKey(offlineMessage.getUserId())) {
                offlineMessage.setUserInfoRsp(userInfoRsps.get(offlineMessage.getUserId()));
            }
        }
        this.offlineMessages.clear();
        this.offlineMessages.addAll(temp);
        adapter.notifyDataSetChanged();
        return temp;
    }

    /**
     * 刷新用户信息
     * @param userInfoRsp
     */
    public void refreshOfflineUser(UserInfoRsp userInfoRsp) {
        for (MessageBean offlineMessage : offlineMessages) {
            if (offlineMessage.getUserId().equals(userInfoRsp.getUserId())) {
                offlineMessage.setUserInfoRsp(userInfoRsp);
            }
        }
        adapter.notifyDataSetChanged();
        // 更新缓存好友信息数据
        userInfoRsps.put(userInfoRsp.getUserId(), userInfoRsp);
    }

    public void endRefresh() {
        swipe_conversationlist.setRefreshing(false);
    }

    /**
     * 刷新用户状态
     * @param updateUserStatusNty
     */
    public void refreshUserState(UpdateUserStatusNty updateUserStatusNty) {
        for (MessageBean offlineMessage : offlineMessages) {
            if (offlineMessage.getUserId().equals(updateUserStatusNty.getUserId())) {
                ArrayList<Enums.EquipmentStatus> equipmentStatuses=new ArrayList<>();
                equipmentStatuses.add(updateUserStatusNty.getStatus());
                // TODO: 2017/7/28 这里仅仅做了好友上线下线判断，并未对陌生人进行判断
                if (offlineMessage.getUserInfoRsp()!=null) {
                    offlineMessage.getUserInfoRsp().setEquipments(equipmentStatuses);
                    adapter.notifyDataSetChanged();
                }
                break;
            }
        }
    }

    /**
     * 设置已读消息
     * @param userId
     */
    public void updateRead(String userId) {
        for (MessageBean offlineMessage : offlineMessages) {
            if (offlineMessage.getUserId().equals(userId)) {
                offlineMessage.setCount(0);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
