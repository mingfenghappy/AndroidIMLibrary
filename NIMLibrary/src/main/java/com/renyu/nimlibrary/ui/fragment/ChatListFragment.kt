package com.renyu.nimlibrary.ui.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.renyu.nimapp.bean.Resource
import com.renyu.nimapp.bean.Status
import com.renyu.nimlibrary.R
import com.renyu.nimlibrary.databinding.FragmentChatlistBinding
import com.renyu.nimlibrary.repository.Repos
import com.renyu.nimlibrary.viewmodel.ChatListViewModel
import kotlinx.android.synthetic.main.fragment_chatlist.*

class ChatListFragment : Fragment() {

    var viewDataBinding: FragmentChatlistBinding? = null

    var vm: ChatListViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_chatlist, container, false)
        return viewDataBinding?.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewDataBinding.also {
            vm = ViewModelProviders.of(this).get(ChatListViewModel::class.java)
            vm?.recentContactListResponse?.observe(this, Observer<Resource<List<RecentContact>>> { t ->
                when(t?.status) {
                    Status.LOADING -> {

                    }
                    Status.SUCESS -> {
                        if (t.data != null) {
                            vm?.notifyDataSetChanged(t.data)
                        }
                        swipe_conversationlist.isRefreshing = false
                    }
                    Status.FAIL -> {
                        swipe_conversationlist.isRefreshing = false
                    }
                    Status.Exception -> {
                        swipe_conversationlist.isRefreshing = false
                    }
                }
            })

            viewDataBinding?.adapter = vm?.adapter

            Handler().postDelayed({
                vm?.queryRecentContacts()
            }, 250)

            swipe_conversationlist.setOnRefreshListener {
                vm?.queryRecentContacts()
            }

//            Repos.sendTextMessage("r17171708", "Hello4")
        }
    }

}