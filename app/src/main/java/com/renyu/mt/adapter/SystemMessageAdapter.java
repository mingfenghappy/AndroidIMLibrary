package com.renyu.mt.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.focustech.message.model.SystemMessageBean;
import com.renyu.mt.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/8/8.
 */

public class SystemMessageAdapter extends RecyclerView.Adapter<SystemMessageAdapter.SystemMessageHolder> {

    Context context;
    ArrayList<SystemMessageBean> beans;

    public SystemMessageAdapter(Context context, ArrayList<SystemMessageBean> beans) {
        this.context = context;
        this.beans = beans;
    }

    @Override
    public SystemMessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.adapter_systemmessage, parent, false);
        return new SystemMessageHolder(view);
    }

    @Override
    public void onBindViewHolder(SystemMessageHolder holder, int position) {
        holder.tv_systemmessage.setText(SystemMessageBean.getSystemMsgContent(beans.get(position)));
    }

    @Override
    public int getItemCount() {
        return beans.size();
    }

    public class SystemMessageHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_systemmessage)
        TextView tv_systemmessage;

        public SystemMessageHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
