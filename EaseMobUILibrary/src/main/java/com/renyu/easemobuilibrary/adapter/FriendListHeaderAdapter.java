package com.renyu.easemobuilibrary.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;
import com.renyu.easemobuilibrary.R;

/**
 * Created by Administrator on 2017/8/4.
 */

public class FriendListHeaderAdapter extends DelegateAdapter.Adapter<FriendListHeaderAdapter.FriendListHeaderHolder> {

    Context context;
    LayoutHelper layoutHelper;

    String name;

    public FriendListHeaderAdapter(Context context, LayoutHelper layoutHelper, String name) {
        this.context=context;
        this.layoutHelper=layoutHelper;
        this.name=name;
    }

    @Override
    public LayoutHelper onCreateLayoutHelper() {
        return layoutHelper;
    }

    @Override
    public FriendListHeaderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.header_friendlist, parent, false);
        return new FriendListHeaderHolder(view);
    }

    @Override
    public void onBindViewHolder(FriendListHeaderHolder holder, int position) {
        holder.tv_friendlist_head.setText(name);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public class FriendListHeaderHolder extends RecyclerView.ViewHolder {
        TextView tv_friendlist_head;

        public FriendListHeaderHolder(View itemView) {
            super(itemView);

            tv_friendlist_head = itemView.findViewById(R.id.tv_friendlist_head);
        }
    }
}
