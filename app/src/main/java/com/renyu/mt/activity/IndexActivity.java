package com.renyu.mt.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;

import com.renyu.mt.utils.DownloadUtils;
import com.focustech.dbhelper.PlainTextDBHelper;
import com.focustech.params.FusionField;
import com.focustech.webtm.protocol.tm.message.model.BroadcastBean;
import com.focustech.webtm.protocol.tm.message.model.FriendGroupRsp;
import com.focustech.webtm.protocol.tm.message.model.FriendInfoRsp;
import com.focustech.webtm.protocol.tm.message.model.MessageBean;
import com.focustech.webtm.protocol.tm.message.model.SystemMessageBean;
import com.focustech.webtm.protocol.tm.message.model.UpdateUserStatusNty;
import com.focustech.webtm.protocol.tm.message.model.UserInfoRsp;
import com.renyu.commonlibrary.commonutils.ACache;
import com.renyu.mt.R;
import com.renyu.mt.base.BaseIMActivity;
import com.renyu.mt.fragment.ChatListFragment;
import com.renyu.mt.fragment.FriendListFragment;
import com.focustech.webtm.protocol.tm.message.MTService;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/7/18.
 */

public class IndexActivity extends BaseIMActivity {

    @BindView(R.id.iv_index_conversationlist)
    ImageView iv_index_conversationlist;
    @BindView(R.id.iv_index_friendlist)
    ImageView iv_index_friendlist;

    Fragment currentFragment;
    // 离线消息列表
    ChatListFragment conversationFragment;
    // 好友列表
    FriendListFragment friendListFragment;

    // 是否初始化启动
    boolean isFirst=false;
    // 是否正在同步好友列表数据
    public boolean isLoadFriendData=false;
    // 是否正在同步离线消息数据
    public boolean isLoadOfflineData=false;

    // 获取所有好友信息
    HashMap<String, UserInfoRsp> userInfoRsps=null;

    // 当前登录用户
    UserInfoRsp userInfoRsp;

    BroadcastReceiver receiver;

    @Override
    public void initParams() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("MT")) {
                    BroadcastBean bean= (BroadcastBean) intent.getSerializableExtra("broadcast");
                    if (bean.getCommand()== BroadcastBean.MTCommand.Conn) {

                    }
                    if (bean.getCommand()== BroadcastBean.MTCommand.Disconn) {
                        isLoadFriendData=false;
                        isLoadOfflineData=false;
                    }
                    if (bean.getCommand()== BroadcastBean.MTCommand.NewSysNty) {
                        MTService.getSysNtyReq(IndexActivity.this, Long.parseLong(bean.getSerializable().toString()));
                    }
                    if (bean.getCommand()== BroadcastBean.MTCommand.UpdateUserStatusNty) {
                        UpdateUserStatusNty updateUserStatusNty= (UpdateUserStatusNty) ((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable();
//                        if (conversationFragment!=null) {
//                            conversationFragment.refreshUserState(updateUserStatusNty);
//                        }
                        if (friendListFragment!=null) {
                            friendListFragment.refreshUserState(updateUserStatusNty);
                        }
                    }
                    // 获取好友关系组数据
                    if (bean.getCommand()== BroadcastBean.MTCommand.FriendGroupsRsp) {
                        // 清除所有好友组关系数据
                        PlainTextDBHelper.getInstance().clearAllFriendList();
                        // 增加所有好友组关系数据
                        PlainTextDBHelper.getInstance().insertFriendList((ArrayList<FriendGroupRsp>) ((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable());
                        // 通知好友列表刷新
                        if (friendListFragment!=null) {
                            friendListFragment.refreshFriendList();
                        }
                        // 获取完成组信息之后，就获取全部好友信息
                        MTService.reqFriendInfo(IndexActivity.this);
                    }
                    if (bean.getCommand()== BroadcastBean.MTCommand.FriendInfoRsp) {
                        FriendInfoRsp temp= (FriendInfoRsp) ((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable();
                        // 更新缓存好友信息数据
                        userInfoRsps.put(temp.getFriend().getUserId(), temp.getFriend());
                        // 插入或更新好友信息
                        PlainTextDBHelper.getInstance().insertFriendList(temp);
                        // 通知会话列表刷新
                        if (conversationFragment!=null) {
                            conversationFragment.refreshOfflineUser(temp.getFriend());
                        }
                        // 通知好友列表刷新
                        if (friendListFragment!=null) {
                            friendListFragment.refreshFriendInfo(temp.getFriend());
                        }
                    }
                    if (bean.getCommand()== BroadcastBean.MTCommand.FriendInfoEndRsp) {
                        isLoadFriendData=false;
                        // 通知好友列表结束刷新
                        if (friendListFragment!=null) {
                            friendListFragment.endRefresh();
                        }
                    }
                    // 收到新消息
                    if (bean.getCommand()== BroadcastBean.MTCommand.Message) {
                        MessageBean messageBean=(MessageBean) ((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable();
                        // 已读回执
                        MTService.hasReadMessage(IndexActivity.this, messageBean.getFromSvrMsgId());
                        // 下载语音文件
                        if (messageBean.getMessageType().equals("7")) {
                            // 下载完成语音文件之后，方可同步数据库与刷新页面
                            DownloadUtils.addFileAndDb(IndexActivity.this, messageBean);
                        }
                        else {
                            PlainTextDBHelper.getInstance().insertMessage(messageBean);
                            // 通知会话列表刷新
                            if (conversationFragment!=null) {
//                                conversationFragment.refreshOfflineMessageData();
                            }
                        }
                    }
                    // 语音消息下载完成或者发出的消息发送完成
                    if (bean.getCommand()== BroadcastBean.MTCommand.MessageSend) {
                        // 通知会话列表刷新
                        if (conversationFragment!=null) {
//                            conversationFragment.refreshOfflineMessageData();
                        }
                    }
                    // 获取到离线消息
                    if (bean.getCommand()== BroadcastBean.MTCommand.GetOfflineMessageRsp) {
                        isLoadOfflineData=false;
                        ArrayList<MessageBean> tempOffline=(ArrayList<MessageBean>) ((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable();
                        // 已读回执
                        for (MessageBean messageBean : tempOffline) {
                            MTService.hasReadOfflineMessage(IndexActivity.this, messageBean.getFromSvrMsgId());
                        }
                        // 下载语音文件
                        for (int i=0;i<tempOffline.size();i++) {
                            if (tempOffline.get(i).getMessageType().equals("7")){
                                String token=userInfoRsp.getToken();
                                final String fileId_ = tempOffline.get(i).getLocalFileName();
                                StringBuilder sb = new StringBuilder(FusionField.downloadUrl);
                                sb.append("fileid=").append(fileId_).append("&type=").append("voice").append("&token=").append(token);
//                                DownloadUtils.addFile(sb.toString(), fileId_);
                            }
                        }
                        // 更新数据库
                        PlainTextDBHelper.getInstance().insertMessages((ArrayList<MessageBean>) ((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable());
                        // 通知会话列表刷新
                        if (conversationFragment!=null) {
//                            conversationFragment.refreshOfflineMessageData();
                        }
                        // 首次加载，开始获取好友关系数据
//                        if (isFirst) {
//                            isFirst=false;
//                            isLoadFriendData=true;
//                            MTService.reqFriendGroups(IndexActivity.this);
//                        }
//                        else {
//                            // 不是首次加载的话，如果离线消息中的人不在好友列表中，则获取好友信息
//                            ArrayList<MessageBean> temp = PlainTextDBHelper.getInstance().getConversationList();
//                            for (MessageBean messageBean : temp) {
//                                if (!userInfoRsps.containsKey(messageBean.getUserId())) {
//                                    MTService.getUserInfo(getApplication(), messageBean.getUserId());
//                                    break;
//                                }
//                            }
//                            // 通知会话列表结束刷新
//                            if (conversationFragment!=null) {
//                                conversationFragment.endRefresh();
//                            }
//                        }
                    }
                    // 消息已读
                    if (bean.getCommand()== BroadcastBean.MTCommand.UpdateRead) {
                        // 通知会话列表结束刷新
                        if (conversationFragment!=null) {
                            conversationFragment.updateRead(((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable().toString());
                        }
                    }
                    // 删除好友
                    if (bean.getCommand()== BroadcastBean.MTCommand.DeleteFriendRsp) {
                        String userId=((UserInfoRsp) bean.getSerializable()).getUserId();
                        // 数据库中删除好友关联关系
                        PlainTextDBHelper.getInstance().deleteFriendsRelation(userId);
                        // 通知好友列表删除好友关系
                        if (friendListFragment!=null) {
                            friendListFragment.deleteFriendsRelation(userId);
                        }
                    }
                    // 添加好友
                    if (bean.getCommand()== BroadcastBean.MTCommand.AddFriendWithoutValidateSucceededSysNty) {
                        String userId=((UserInfoRsp) bean.getSerializable()).getUserId();
                        // 数据库添加好友关联
                        PlainTextDBHelper.getInstance().addFriendsRelation(userId);
                        // 通知会话列表刷新
                        if (conversationFragment!=null) {
                            conversationFragment.refreshOfflineUser(userInfoRsp);
                        }
                        // 通知好友列表刷新
                        if (friendListFragment!=null) {
                            friendListFragment.refreshFriendList();
                        }
                        // 获取好友信息
                        MTService.getUserInfo(getApplication(), userId);
                    }
                    // 获取到好友信息
                    if (bean.getCommand()== BroadcastBean.MTCommand.UserInfoRsp) {
                        UserInfoRsp userInfoRsp= (UserInfoRsp) ((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable();
                        // 更新缓存数据
                        userInfoRsps.put(userInfoRsp.getUserId(), userInfoRsp);
                        // 插入或更新好友信息
                        PlainTextDBHelper.getInstance().insertFriendList(userInfoRsp);
                        // 通知会话列表刷新
                        if (conversationFragment!=null) {
                            conversationFragment.refreshOfflineUser(userInfoRsp);
                        }
                        // 通知好友列表刷新
                        if (friendListFragment!=null) {
                            friendListFragment.refreshFriendInfo(userInfoRsp);
                        }
                    }
                    // 获取到系统消息
                    if (bean.getCommand()== BroadcastBean.MTCommand.SystemMessageBean) {
                        // 插入系统消息
                        PlainTextDBHelper.getInstance().insertSystemMessage((SystemMessageBean) bean.getSerializable());
                        // 通知会话列表刷新
                        if (conversationFragment!=null) {
//                            conversationFragment.refreshOfflineMessageData();
                        }
                    }
                }
            }
        };

        // 获取当前用户信息
        userInfoRsp= (UserInfoRsp) ACache.get(this).getAsObject("UserInfoRsp");
        // 获取所有好友信息
        userInfoRsps= PlainTextDBHelper.getInstance().getFriendsInfo();
        change(0);
    }

    @Override
    public int initViews() {
        return R.layout.activity_index;
    }

    @Override
    public void loadData() {
        // 获取离线消息
        isFirst=true;
        isLoadOfflineData=true;
        MTService.reqGetOfflineMessage(IndexActivity.this);
        // 获取系统消息
        MTService.getSysNtyReq(IndexActivity.this, 0);
    }

    @Override
    public int setStatusBarColor() {
        return Color.BLACK;
    }

    @Override
    public int setStatusBarTranslucent() {
        return 0;
    }

    @OnClick({R.id.iv_index_conversationlist, R.id.iv_index_friendlist})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_index_conversationlist:
                change(0);
                break;
            case R.id.iv_index_friendlist:
                change(1);
                break;
        }
    }

    /**
     * 下拉刷新更新好友列表数据
     */
    public void needReqFriendGroups() {
        isLoadFriendData=true;
        MTService.reqFriendGroups(IndexActivity.this);
    }

    /**
     * 下拉刷新获取离线消息数据
     */
    public void needReqGetOfflineMessage() {
        isLoadOfflineData=true;
        MTService.reqGetOfflineMessage(IndexActivity.this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent=new Intent(this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    @Override
    public BroadcastReceiver getReceiver() {
        return receiver;
    }

    private void change(int position) {
        iv_index_conversationlist.setImageResource(R.mipmap.em_conversation_normal);
        iv_index_friendlist.setImageResource(R.mipmap.em_contact_list_normal);

        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        if (currentFragment!=null) {
            transaction.hide(currentFragment);
        }
        if (position==0) {
            iv_index_conversationlist.setImageResource(R.mipmap.em_conversation_selected);
            if (conversationFragment==null) {
                conversationFragment=new ChatListFragment();
                transaction.add(R.id.layout_container, conversationFragment);
            }
            else {
                transaction.show(conversationFragment);
            }
            currentFragment=conversationFragment;
        }
        if (position==1) {
            iv_index_friendlist.setImageResource(R.mipmap.em_contact_list_selected);
            if (friendListFragment==null) {
                friendListFragment=FriendListFragment.getInstance(PlainTextDBHelper.getInstance().getFriendList());
                transaction.add(R.id.layout_container, friendListFragment);
            }
            else {
                transaction.show(friendListFragment);
            }
            currentFragment=friendListFragment;
        }
        transaction.commitAllowingStateLoss();
    }
}
