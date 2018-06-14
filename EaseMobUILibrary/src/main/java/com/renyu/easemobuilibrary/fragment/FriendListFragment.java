package com.renyu.easemobuilibrary.fragment;

import android.support.v7.widget.RecyclerView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;
import com.alibaba.android.vlayout.layout.StickyLayoutHelper;
import com.blankj.utilcode.util.Utils;
import com.renyu.commonlibrary.basefrag.BaseFragment;
import com.renyu.easemobuilibrary.R;
import com.renyu.easemobuilibrary.adapter.FriendListAdapter;
import com.renyu.easemobuilibrary.adapter.FriendListHeaderAdapter;
import com.renyu.easemobuilibrary.manager.ContactManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 好友列表
 * Created by Administrator on 2017/7/18.
 */

public class FriendListFragment extends BaseFragment {

    RecyclerView rv_friendlist;
    List<DelegateAdapter.Adapter> adapters;
    DelegateAdapter delegateAdapter;

    // 所有联系人
    ArrayList<String> users;

    @Override
    public void initParams() {
        adapters=new ArrayList<>();
        users = new ArrayList<>();

        rv_friendlist = view.findViewById(R.id.rv_friendlist);
        VirtualLayoutManager manager=new VirtualLayoutManager(context);
        rv_friendlist.setLayoutManager(manager);
        RecyclerView.RecycledViewPool pool=new RecyclerView.RecycledViewPool();
        pool.setMaxRecycledViews(0, 20);
        rv_friendlist.setRecycledViewPool(pool);
        delegateAdapter=new DelegateAdapter(manager, false);
        rv_friendlist.setAdapter(delegateAdapter);
    }

    @Override
    public int initViews() {
        return R.layout.fragment_friendlist;
    }

    @Override
    public void loadData() {
        // 加载好友列表
        ContactManager.aysncGetAllContactsFromServer();
    }

    /**
     * 收到新的好友对应组信息之后进行刷新
     * @param temp
     */
    public void refreshFriendList(ArrayList<String> temp) {
        // 清除缓存
        adapters.clear();
        users.clear();
        users.addAll(temp);

        adapters.add(new FriendListHeaderAdapter(getActivity(), new StickyLayoutHelper(), "我的好友"));
        adapters.add(new FriendListAdapter(getActivity(), new LinearLayoutHelper(), users));
        delegateAdapter.setAdapters(adapters);
    }

    /**
     * 删除好友关系
     * @param userId
     */
    public void deleteFriendsRelation(String userId) {
        users.remove(userId);
        adapters.get(1).notifyDataSetChanged();
    }

    public void addFriendsRelation(String userId) {
        users.add(userId);
        adapters.get(1).notifyDataSetChanged();
    }
}
