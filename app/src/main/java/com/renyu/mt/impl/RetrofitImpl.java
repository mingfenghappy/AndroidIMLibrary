package com.renyu.mt.impl;

import com.focustech.webtm.protocol.tm.message.model.IMBaseResponseList;
import com.focustech.webtm.protocol.tm.message.model.OfflineIMResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by renyu on 2017/12/15.
 */

public interface RetrofitImpl {

    /**
     * 获取会话列表
     *
     * @param toUserId
     * @return
     */
    @GET("getRecentMessageList")
    Observable<IMBaseResponseList<OfflineIMResponse>> getOfflineIMList(
            @Query("toUserId") String toUserId);
}
