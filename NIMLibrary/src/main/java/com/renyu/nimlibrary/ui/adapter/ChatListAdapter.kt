package com.renyu.nimlibrary.ui.adapter

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.databinding.library.baseAdapters.BR
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.renyu.nimlibrary.R
import com.renyu.nimlibrary.databinding.AdapterChatlistBinding

class ChatListAdapter(private val beans: ArrayList<RecentContact>) : RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        val viewDataBinding = DataBindingUtil.inflate<AdapterChatlistBinding>(LayoutInflater.from(parent.context), R.layout.adapter_chatlist, parent, false)
        return ChatListViewHolder(viewDataBinding)
    }

    override fun getItemCount() = beans.size

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        holder.vd.setVariable(BR.recentContact, beans[holder.layoutPosition])
        holder.vd.executePendingBindings()
    }

    class ChatListViewHolder(viewDataBinding: ViewDataBinding) : RecyclerView.ViewHolder(viewDataBinding.root) {
        val vd = viewDataBinding
    }
}