package com.renyu.mt.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;
import com.alibaba.android.vlayout.layout.StickyLayoutHelper;
import com.focustech.dbhelper.PlainTextDBHelper;
import com.focustech.webtm.protocol.tm.message.model.FriendGroupRsp;
import com.focustech.webtm.protocol.tm.message.model.FriendInfoRsp;
import com.focustech.webtm.protocol.tm.message.model.FriendStatusRsp;
import com.focustech.webtm.protocol.tm.message.model.UpdateUserStatusNty;
import com.focustech.webtm.protocol.tm.message.model.UserInfoRsp;
import com.focustech.tm.open.sdk.messages.protobuf.Enums;
import com.renyu.mt.MTApplication;
import com.renyu.mt.R;
import com.renyu.mt.activity.IndexActivity;
import com.renyu.mt.adapter.FriendListHeaderAdapter;
import com.renyu.mt.adapter.FriendListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 好友列表
 * Created by Administrator on 2017/7/18.
 */

public class FriendListFragment extends Fragment {

    @BindView(R.id.swipe_friendlist)
    SwipeRefreshLayout swipe_friendlist;
    @BindView(R.id.rv_friendlist)
    RecyclerView rv_friendlist;
    List<DelegateAdapter.Adapter> adapters;
    DelegateAdapter delegateAdapter;

    // 以groupId作为键，存储加载的数据
    HashMap<String, List<FriendStatusRsp>> datas;
    // 以groupId作为键，存储加载的顺序
    HashMap<String, Integer> dataPositions;

    public static FriendListFragment getInstance(ArrayList<FriendGroupRsp> friendGroupRsps) {
        FriendListFragment friendListFragment=new FriendListFragment();
        Bundle bundle=new Bundle();
        bundle.putSerializable("friendGroupRsps", friendGroupRsps);
        friendListFragment.setArguments(bundle);
        return friendListFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_friendlist, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        datas=new HashMap<>();
        dataPositions=new HashMap<>();
        adapters=new ArrayList<>();

        VirtualLayoutManager manager=new VirtualLayoutManager(getActivity());
        rv_friendlist.setLayoutManager(manager);
        RecyclerView.RecycledViewPool pool=new RecyclerView.RecycledViewPool();
        pool.setMaxRecycledViews(0, 20);
        rv_friendlist.setRecycledViewPool(pool);
        delegateAdapter=new DelegateAdapter(manager, false);
        rv_friendlist.setAdapter(delegateAdapter);
        // 加载本地缓存好友列表
        getLocalData((ArrayList<FriendGroupRsp>) getArguments().getSerializable("friendGroupRsps"));

        swipe_friendlist.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!((IndexActivity) getActivity()).isLoadFriendData) {
                    ((IndexActivity) getActivity()).needReqFriendGroups();
                }
                else {
                    swipe_friendlist.setRefreshing(false);
                }
            }
        });
    }

    /**
     * 加载本地缓存好友列表
     */
    private void getLocalData(ArrayList<FriendGroupRsp> temp) {
        // 清除缓存
        adapters.clear();
        dataPositions.clear();
        datas.clear();

        HashMap<String, UserInfoRsp> userInfoRsps= PlainTextDBHelper.getInstance().getFriendsInfo();
        for (int j = 0; j < temp.size(); j++) {
            FriendGroupRsp friendGroupRsp=temp.get(j);
            adapters.add(new FriendListHeaderAdapter(getActivity(), new StickyLayoutHelper(), friendGroupRsp.getFriendGroupName().equals("default")?"我的好友":friendGroupRsp.getFriendGroupName()));
            for (int i=0;i<friendGroupRsp.getFriends().size();i++) {
                if (userInfoRsps.containsKey(friendGroupRsp.getFriends().get(i).getFriendUserId())) {
                    if (friendGroupRsp.getFriends().get(i).getFriendInfoRsp()!=null) {
                        friendGroupRsp.getFriends().get(i).getFriendInfoRsp().setFriend(userInfoRsps.get(friendGroupRsp.getFriends().get(i).getFriendUserId()));
                    }
                    else {
                        FriendInfoRsp friendInfoRsp=new FriendInfoRsp();
                        friendInfoRsp.setFriendGroupId(friendGroupRsp.getFriendGroupId());
                        friendInfoRsp.setFriendGroupName(friendGroupRsp.getFriendGroupName());
                        friendGroupRsp.getFriends().get(i).setFriendInfoRsp(friendInfoRsp);
                    }
                }
            }
            List<FriendStatusRsp> r=friendGroupRsp.getFriends();
            adapters.add(new FriendListAdapter(getActivity(), new LinearLayoutHelper(), r));
            // 保存到缓存中
            dataPositions.put(friendGroupRsp.getFriendGroupId(), j*2+1);
            datas.put(friendGroupRsp.getFriendGroupId(), r);
        }
        delegateAdapter.setAdapters(adapters);
    }

    /**
     * 收到新的好友对应组信息之后进行刷新
     */
    public void refreshFriendList() {
        getLocalData(PlainTextDBHelper.getInstance().getFriendList());
    }

    /**
     * 刷新用户信息
     * @param userInfoRsp
     */
    public synchronized void refreshFriendInfo(UserInfoRsp userInfoRsp) {
        Iterator<Map.Entry<String, List<FriendStatusRsp>>> it = datas.entrySet().iterator();
        String groupId=null;
        outer:
        while (it.hasNext()) {
            Map.Entry<String, List<FriendStatusRsp>> entry=it.next();
            for (FriendStatusRsp friendStatusRsp : entry.getValue()) {
                if (friendStatusRsp.getFriendInfoRsp().getFriend().getUserId().equals(userInfoRsp.getUserId())) {
                    friendStatusRsp.getFriendInfoRsp().getFriend().setUserHeadId(userInfoRsp.getUserHeadId());
                    friendStatusRsp.getFriendInfoRsp().getFriend().setEquipments(userInfoRsp.getEquipments());
                    friendStatusRsp.getFriendInfoRsp().getFriend().setRole(userInfoRsp.getRole());
                    friendStatusRsp.getFriendInfoRsp().getFriend().setUserSignature(userInfoRsp.getUserSignature());
                    friendStatusRsp.getFriendInfoRsp().getFriend().setTmNum(userInfoRsp.getTmNum());
                    friendStatusRsp.getFriendInfoRsp().getFriend().setNetIp(userInfoRsp.getNetIp());
                    friendStatusRsp.getFriendInfoRsp().getFriend().setTimestamp(userInfoRsp.getTimestamp());
                    friendStatusRsp.getFriendInfoRsp().getFriend().setUserHeadType(userInfoRsp.getUserHeadType());
                    friendStatusRsp.getFriendInfoRsp().getFriend().setUserName(userInfoRsp.getUserName());
                    friendStatusRsp.getFriendInfoRsp().getFriend().setUserNickName(userInfoRsp.getUserNickName());
                    groupId=entry.getKey();
                    break outer;
                }
            }
        }
        if (!TextUtils.isEmpty(groupId)) {
            int index=dataPositions.get(groupId);
            adapters.get(index).notifyDataSetChanged();
        }
    }

    public void endRefresh() {
        swipe_friendlist.setRefreshing(false);
    }

    /**
     * 刷新用户状态
     * @param updateUserStatusNty
     */
    public void refreshUserState(UpdateUserStatusNty updateUserStatusNty) {
        Iterator<Map.Entry<String, List<FriendStatusRsp>>> it = datas.entrySet().iterator();
        String groupId=null;
        outer:
        while (it.hasNext()) {
            Map.Entry<String, List<FriendStatusRsp>> entry = it.next();
            for (FriendStatusRsp friendStatusRsp : entry.getValue()) {
                if (friendStatusRsp.getFriendUserId().equals(updateUserStatusNty.getUserId())) {
                    ArrayList<Enums.EquipmentStatus> equipmentStatuses = new ArrayList<>();
                    equipmentStatuses.add(updateUserStatusNty.getStatus());
                    friendStatusRsp.getFriendInfoRsp().getFriend().setEquipments(equipmentStatuses);
                    groupId=entry.getKey();
                    break outer;
                }
            }
        }
        if (!TextUtils.isEmpty(groupId)) {
            int index=dataPositions.get(groupId);
            adapters.get(index).notifyDataSetChanged();
        }
    }

    /**
     * 删除好友关系
     * @param userId
     */
    public void deleteFriendsRelation(String userId) {
        Iterator<Map.Entry<String, List<FriendStatusRsp>>> it = datas.entrySet().iterator();
        String groupId=null;
        outer:
        while (it.hasNext()) {
            Map.Entry<String, List<FriendStatusRsp>> entry=it.next();
            List<FriendStatusRsp> temps=entry.getValue();
            for (int i = 0; i < temps.size(); i++) {
                FriendStatusRsp friendStatusRsp=temps.get(i);
                if (friendStatusRsp.getFriendInfoRsp().getFriend().getUserId().equals(userId)) {
                    temps.remove(i);
                    groupId=entry.getKey();
                    break outer;
                }
            }
        }
        if (!TextUtils.isEmpty(groupId)) {
            int index=dataPositions.get(groupId);
            adapters.get(index).notifyDataSetChanged();
        }
    }
}