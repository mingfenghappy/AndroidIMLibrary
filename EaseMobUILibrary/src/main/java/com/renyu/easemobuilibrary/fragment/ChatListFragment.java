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
import com.renyu.commonlibrary.basefrag.BaseFragment;
import com.renyu.easemobuilibrary.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ChatListFragment extends BaseFragment {

    SwipeRefreshLayout swipe_conversationlist;
    LinearLayout layout_conversationlist;
    RecyclerView rv_conversationlist;

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
        rv_conversationlist = view.findViewById(R.id.rv_conversationlist);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setSmoothScrollbarEnabled(true);
        manager.setAutoMeasureEnabled(true);
        rv_conversationlist.setLayoutManager(manager);
        rv_conversationlist.setNestedScrollingEnabled(false);
        rv_conversationlist.setHasFixedSize(true);
    }

    @Override
    public int initViews() {
        return R.layout.fragment_chatlist;
    }

    @Override
    public void loadData() {
        loadConversationList();
    }

    protected List<EMConversation> loadConversationList(){
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
        List<EMConversation> list = new ArrayList<>();
        for (Pair<Long, EMConversation> sortItem : sortList) {
            list.add(sortItem.second);
        }
        return list;
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
}
