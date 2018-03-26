package com.renyu.mt.fragment;

import android.app.Activity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.focustech.dbhelper.PlainTextDBHelper;
import com.focustech.webtm.protocol.tm.message.MTService;
import com.focustech.webtm.protocol.tm.message.model.MessageBean;
import com.focustech.webtm.protocol.tm.message.model.OfflineIMResponse;
import com.focustech.webtm.protocol.tm.message.model.UserInfoRsp;
import com.renyu.commonlibrary.basefrag.BaseFragment;
import com.renyu.commonlibrary.commonutils.ACache;
import com.renyu.commonlibrary.network.Retrofit2Utils;
import com.renyu.mt.R;
import com.renyu.mt.adapter.ChatListAdapter;
import com.renyu.mt.impl.RetrofitImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by Administrator on 2017/7/20.
 */

public class ChatListFragment extends BaseFragment {

    @BindView(R.id.swipe_conversationlist)
    SwipeRefreshLayout swipe_conversationlist;
    @BindView(R.id.rv_conversationlist)
    RecyclerView rv_conversationlist;
    ChatListAdapter adapter;

    // 当前用户信息
    UserInfoRsp currentUserInfo;
    // 离线消息
    ArrayList<OfflineIMResponse> offlineMessages;
    // 好友用户信息Map
    HashMap<String, UserInfoRsp> userInfoRsps;
    // 是否正在请求接口数据
    boolean isRequest = false;

    Activity activity;

    @Override
    public void initParams() {
        currentUserInfo = (UserInfoRsp) ACache.get(getActivity()).getAsObject("UserInfoRsp");

        offlineMessages=new ArrayList<>();

        userInfoRsps= PlainTextDBHelper.getInstance().getFriendsInfo();

        rv_conversationlist.setHasFixedSize(true);
        rv_conversationlist.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter=new ChatListAdapter(getActivity(), offlineMessages);
        rv_conversationlist.setAdapter(adapter);
        swipe_conversationlist.setOnRefreshListener(() -> {
            if (!isRequest) {
                getOfflineIMFromRemote();
            }
        });
    }

    @Override
    public int initViews() {
        return R.layout.fragment_chatlist;
    }

    @Override
    public void loadData() {
        getOfflineIMFromLocal();
        getOfflineIMFromRemote();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    /**
     * 加载本地会话列表
     */
    public void getOfflineIMFromLocal() {
        ArrayList<OfflineIMResponse> temp = PlainTextDBHelper.getInstance().getConversationList();
        for (OfflineIMResponse offlineMessage : temp) {
            if (userInfoRsps.containsKey(offlineMessage.getFromUserId())) {
                offlineMessage.setUserNickName(userInfoRsps.get(offlineMessage.getFromUserId()).getUserNickName());
                offlineMessage.setUserHeadType(userInfoRsps.get(offlineMessage.getFromUserId()).getUserHeadType().getNumber());
                offlineMessage.setUserHeadId(userInfoRsps.get(offlineMessage.getFromUserId()).getUserHeadId());
            }
        }
        this.offlineMessages.clear();
        this.offlineMessages.addAll(temp);
        adapter.notifyDataSetChanged();
    }

    /**
     * 加载远程会话列表
     */
    public void getOfflineIMFromRemote() {
        Log.d("MTAPP", "获取会话列表数据");
        retrofit.create(RetrofitImpl.class).getOfflineIMList(currentUserInfo.getUserId())
                .compose(Retrofit2Utils.backgroundList())
                .subscribe(new Observer<List<OfflineIMResponse>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        isRequest = true;
                    }

                    @Override
                    public void onNext(List<OfflineIMResponse> temp) {
                        Log.d("MTAPP", "找到" + temp.size() + "接口数据");
                        // 没有找到的数据列表
                        ArrayList<OfflineIMResponse> newAdd = new ArrayList<>();
                        // 对新消息进行遍历
                        for (OfflineIMResponse temp_ : temp) {
                            Log.d("MTAPP", "获取到的数据为："+temp_.getUserNickName() + " " + temp_.getLastMsg());
                            // 对老消息进行遍历
                            boolean find = false;
                            for (OfflineIMResponse offlineMessage : offlineMessages) {
                                if (temp_.getFromUserId().equals(offlineMessage.getFromUserId())) {
                                    // 找到该条消息，公共参数进行替换
                                    offlineMessage.setUserHeadId(temp_.getUserHeadId());
                                    offlineMessage.setUserHeadType(temp_.getUserHeadType());
                                    offlineMessage.setUserNickName(temp_.getUserNickName());
                                    // 如果接口消息时间比列表消息时间更新,则进行更新
                                    if (temp_.getAddTime()>offlineMessage.getAddTime()) {
                                        offlineMessage.setAddTime(temp_.getAddTime());
                                        offlineMessage.setLastMsg(temp_.getLastMsg());
                                        offlineMessage.setType(temp_.getType());
                                        offlineMessage.setUnloadCount(temp_.getUnloadCount());
                                    }
                                    find = true;
                                    break;
                                }
                            }
                            // 没有找到则为新增补充入列表
                            if (!find) {
                                newAdd.add(temp_);
                            }
                        }
                        // 进行数据整合
                        newAdd.addAll(offlineMessages);
                        // 进行排序
                        long[] arr = new long[newAdd.size()];
                        for (int i = 0; i < newAdd.size(); i++) {
                            arr[i] = newAdd.get(i).getAddTime();
                        }
                        for(int i=0;i<arr.length-1;i++){
                            for(int j=0;j<arr.length-1-i;j++){
                                if(arr[j]<arr[j+1]){
                                    long a=arr[j];
                                    arr[j]=arr[j+1];
                                    arr[j+1]=a;
                                }
                            }
                        }
                        // 填充数据
                        offlineMessages.clear();
                        for (long l : arr) {
                            for (OfflineIMResponse offlineIMResponse : newAdd) {
                                if (offlineIMResponse.getAddTime() == l) {
                                    offlineMessages.add(offlineIMResponse);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                        swipe_conversationlist.setRefreshing(false);

                        // 网络请求可能会需要获取好友关系数据以弥补信息不足部分
                        MTService.reqFriendInfo(activity.getApplicationContext());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();

                        Log.d("MTAPP", "没有接口数据");

                        swipe_conversationlist.setRefreshing(false);

                        // 网络请求可能会需要获取好友关系数据以弥补信息不足部分
                        MTService.reqFriendInfo(activity.getApplicationContext());

                        isRequest = false;
                    }

                    @Override
                    public void onComplete() {
                        isRequest = false;
                    }
                });
    }

    /**
     * 刷新单条消息
     * @param messageBean
     */
    public void refreshOneMessage(MessageBean messageBean) {
        OfflineIMResponse find = null;
        int findPosition = -1;
        for (int i = 0; i < offlineMessages.size(); i++) {
            // 找到IM列表中该联系人的消息
            OfflineIMResponse offlineMessage = offlineMessages.get(i);
            if (offlineMessage.getFromUserId().equals(messageBean.getUserId())) {
                offlineMessage.setAddTime(messageBean.getTimestamp());
                offlineMessage.setLastMsg(messageBean.getMsg());
                offlineMessage.setType(Integer.parseInt(messageBean.getMessageType()));
                if (messageBean.getIsSend().equals("0")) {
                    offlineMessage.setUnloadCount(offlineMessage.getUnloadCount()+1);
                }

                find = offlineMessage;
                findPosition = i;

                break;
            }
        }
        // 没有找到的话直接新增一条数据
        if (find == null || findPosition == -1) {
            OfflineIMResponse offlineMessage = new OfflineIMResponse();
            offlineMessage.setAddTime(messageBean.getTimestamp());
            offlineMessage.setLastMsg(messageBean.getMsg());
            offlineMessage.setType(Integer.parseInt(messageBean.getMessageType()));
            offlineMessage.setUnloadCount(offlineMessage.getUnloadCount()+1);
            offlineMessage.setFromUserId(messageBean.getUserId());
            offlineMessages.add(0, offlineMessage);
        }
        // 找到就前置
        else {
            offlineMessages.add(0, offlineMessages.remove(findPosition));
        }
        adapter.notifyDataSetChanged();

        // 不是首次加载的话，如果离线消息中的人不在好友列表中，则获取好友信息
        if (!userInfoRsps.containsKey(messageBean.getUserId())) {
            MTService.getUserInfo(activity.getApplicationContext(), messageBean.getUserId());
        }
        // 存在好友关系，直接使用存储的好友关系
        else {
            refreshOfflineUser(userInfoRsps.get(messageBean.getUserId()));
        }
    }

    /**
     * 刷新用户信息
     * @param userInfoRsp
     */
    public void refreshOfflineUser(UserInfoRsp userInfoRsp) {
        boolean find = false;
        for (OfflineIMResponse offlineMessage : offlineMessages) {
            if (offlineMessage.getFromUserId().equals(userInfoRsp.getUserId())) {
                offlineMessage.setUserHeadId(userInfoRsp.getUserHeadId());
                offlineMessage.setUserHeadType(userInfoRsp.getUserHeadType().getNumber());
                offlineMessage.setUserNickName(userInfoRsp.getUserNickName());

                find = true;
                break;
            }
        }
        if (find) {
            adapter.notifyDataSetChanged();
        }
        // 更新缓存好友信息数据
        userInfoRsps.put(userInfoRsp.getUserId(), userInfoRsp);
    }

    /**
     * 设置已读消息
     * @param userId
     */
    public void updateRead(String userId) {
        for (OfflineIMResponse offlineMessage : offlineMessages) {
            if (offlineMessage.getFromUserId().equals(userId)) {
                offlineMessage.setUnloadCount(0);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
