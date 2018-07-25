package com.renyu.nimlibrary.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.renyu.nimapp.bean.Resource
import com.renyu.nimlibrary.bean.ObserveResponse
import com.renyu.nimlibrary.bean.ObserveResponseType
import com.renyu.nimlibrary.repository.Repos
import com.renyu.nimlibrary.ui.adapter.ChatListAdapter
import com.renyu.nimlibrary.util.RxBus
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class ChatListViewModel : BaseViewModel() {

    // 置顶功能可直接使用，也可作为思路，供开发者充分利用RecentContact的tag字段
    private val RECENT_TAG_STICKY: Long = 1 // 联系人置顶tag

    private val beans: ArrayList<RecentContact> by lazy {
        ArrayList<RecentContact>()
    }

    // 用来设置的adapter
    val adapter: ChatListAdapter by lazy {
        ChatListAdapter(beans)
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

        // 添加最近会话列表监听
        compositeDisposable.add(RxBus.getDefault()
                .toObservable(ObserveResponse::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
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
        // 刷新RV
        AtomicInteger().getAndIncrement()
        adapter.notifyDataSetChanged()
    }

    private fun sortRecentContacts(list: List<RecentContact>) :  List<RecentContact> {
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