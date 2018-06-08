package com.renyu.easemobuilibrary.fragment;

import android.app.Activity;
import android.content.Context;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.renyu.commonlibrary.basefrag.BaseFragment;
import com.renyu.easemobuilibrary.R;
import com.renyu.easemobuilibrary.adapter.ChatListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ChatListFragment extends BaseFragment {

    SwipeRefreshLayout swipe_conversationlist;
    LinearLayout layout_conversationlist;
    RecyclerView rv_conversationlist;
    ChatListAdapter adapter;

    // 离线消息
    ArrayList<EMMessage> offlineMessages;

    private OnHeaderViewSetListener headerViewSetListener;

    public interface OnHeaderViewSetListener {
        View getHeadView();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        headerViewSetListener = (OnHeaderViewSetListener) activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        headerViewSetListener = (OnHeaderViewSetListener) context;
    }

    @Override
    public void initParams() {
        offlineMessages=new ArrayList<>();

        rv_conversationlist = view.findViewById(R.id.rv_conversationlist);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setSmoothScrollbarEnabled(true);
        manager.setAutoMeasureEnabled(true);
        rv_conversationlist.setLayoutManager(manager);
        rv_conversationlist.setNestedScrollingEnabled(false);
        rv_conversationlist.setHasFixedSize(true);
        adapter=new ChatListAdapter(getActivity(), offlineMessages);
        rv_conversationlist.setAdapter(adapter);
        swipe_conversationlist = view.findViewById(R.id.swipe_conversationlist);
        swipe_conversationlist.setOnRefreshListener(() -> {
            loadConversationList();
        });
        layout_conversationlist = view.findViewById(R.id.layout_conversationlist);
        if (headerViewSetListener != null && headerViewSetListener.getHeadView() != null) {
            layout_conversationlist.addView(headerViewSetListener.getHeadView());
        }
    }

    @Override
    public int initViews() {
        return R.layout.fragment_chatlist;
    }

    @Override
    public void loadData() {
        loadConversationList();
    }

    private void loadConversationList() {
        Map<String, EMConversation> conversations = EMClient.getInstance().chatManager().getAllConversations();
        List<Pair<Long, EMConversation>> sortList = new ArrayList<>();
        synchronized (conversations) {
            for (EMConversation conversation : conversations.values()) {
                if (conversation.getAllMessages().size() != 0) {
                    sortList.add(new Pair<>(conversation.getLastMessage().getMsgTime(), conversation));
                }
            }
        }
        try {
            sortConversationByLastChatTime(sortList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        offlineMessages.clear();
        for (Pair<Long, EMConversation> sortItem : sortList) {
            offlineMessages.add(sortItem.second.getLastMessage());
        }
        adapter.notifyDataSetChanged();
        swipe_conversationlist.setRefreshing(false);
    }

    private void sortConversationByLastChatTime(List<Pair<Long, EMConversation>> conversationList) {
        Collections.sort(conversationList, (con1, con2) -> {
            if (con1.first.equals(con2.first)) {
                return 0;
            } else if (con2.first.longValue() > con1.first.longValue()) {
                return 1;
            } else {
                return -1;
            }
        });
    }

    public void refresh() {
        loadConversationList();
        adapter.notifyDataSetChanged();
    }
}
