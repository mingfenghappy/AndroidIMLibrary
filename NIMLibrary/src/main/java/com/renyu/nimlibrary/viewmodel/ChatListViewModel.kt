package com.renyu.nimlibrary.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.view.View
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.netease.nimlib.sdk.uinfo.UserService
import com.renyu.nimapp.bean.Resource
import com.renyu.nimlibrary.bean.ObserveResponse
import com.renyu.nimlibrary.bean.ObserveResponseType
import com.renyu.nimlibrary.binding.EventImpl
import com.renyu.nimlibrary.manager.MessageManager
import com.renyu.nimlibrary.manager.UserManager
import com.renyu.nimlibrary.repository.Repos
import com.renyu.nimlibrary.ui.adapter.ChatListAdapter
import com.renyu.nimlibrary.util.RxBus
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.*
import kotlin.collections.ArrayList

class ChatListViewModel : BaseViewModel(), EventImpl {

    // 置顶功能可直接使用，也可作为思路，供开发者充分利用RecentContact的tag字段
    private val RECENT_TAG_STICKY: Long = 1 // 联系人置顶tag

    private val beans: ArrayList<RecentContact> by lazy {
        ArrayList<RecentContact>()
    }

    // 用来设置的adapter
    val adapter: ChatListAdapter by lazy {
        ChatListAdapter(beans, this)
    }

    // 接口请求数据
    private val recentContactListRequest = MutableLiveData<String>()
    var recentContactListResponse: LiveData<Resource<List<RecentContact>>>? = null

    init {
        recentContactListResponse = Transformations.switchMap(recentContactListRequest) {
            if (it == null) {
                MutableLiveData()
            }
            else {
                Repos.queryRecentContacts()
            }
        }

        compositeDisposable.add(RxBus.getDefault()
                .toObservable(ObserveResponse::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    // 添加最近会话列表监听
                    if (it.type == ObserveResponseType.ObserveRecentContact) {
                        (it.data as List<*>).forEach {
                            if (it is RecentContact) {
                                var indexTemp = -1
                                // 找到同一个Item并删除
                                beans.forEachIndexed { index, recentContact ->
                                    if (recentContact.contactId == it.contactId &&
                                            recentContact.sessionType == it.sessionType) {
                                        indexTemp = index
                                        return@forEachIndexed
                                    }
                                }
                                if (indexTemp != -1) {
                                    beans.removeAt(indexTemp)
                                }
                                // 添加当前Item
                                beans.add(it)
                            }
                        }
                        sortRecentContacts(beans)
                        adapter.notifyDataSetChanged()

                        // 刷新用户个人数据
                        refreshUserInfo()
                    }
                    // 用户资料变更
                    if (it.type == ObserveResponseType.UserInfoUpdate) {
                        adapter.notifyDataSetChanged()
                    }
                    // 从服务器获取用户资料
                    if (it.type == ObserveResponseType.FetchUserInfo) {
                        adapter.notifyDataSetChanged()
                    }
                }.subscribe())
    }

    /**
     * 获取会话列表
     */
    fun queryRecentContacts() {
        recentContactListRequest.value = "refresh"
    }

    /**
     * 刷新数据
     */
    fun notifyDataSetChanged(recentContacts: List<RecentContact>) {
        beans.clear()
        beans.addAll(sortRecentContacts(recentContacts))
        adapter.notifyDataSetChanged()

        // 刷新用户个人数据
        refreshUserInfo()
    }

    /**
     * 从最近联系人列表中删除一项。
     */
    override fun deleteRecentContact(view: View, contactId: String) {
        super.deleteRecentContact(view, contactId)

        beans.filter {
            it.contactId == contactId
        }.forEach {
            MessageManager.deleteRecentContact(it)
            beans.remove(it)
        }
        adapter.notifyDataSetChanged()
    }

    /**
     * 刷新用户个人数据
     */
    private fun refreshUserInfo() {
        val refreshLists = ArrayList<String>()
        beans.forEach {
            val userInfo = NIMClient.getService(UserService::class.java).getUserInfo(it.contactId)
            if (userInfo == null) {
                refreshLists.add(it.contactId)
            }
        }
        if (refreshLists.size>0) {
            UserManager.fetchUserInfo(refreshLists)
        }
    }

    private fun sortRecentContacts(list: List<RecentContact>):  List<RecentContact> {
        if (!list.isEmpty()) {
            Collections.sort(list, comp)
        }
        return list
    }

    private val comp = Comparator<RecentContact> { o1, o2 ->
        // 先比较置顶tag
        val sticky = (o1.tag and RECENT_TAG_STICKY) - (o2.tag and RECENT_TAG_STICKY)
        if (sticky != 0L) {
            if (sticky > 0) -1 else 1
        } else {
            val time = o1.time - o2.time
            if (time == 0L) 0 else if (time > 0) -1 else 1
        }
    }
}