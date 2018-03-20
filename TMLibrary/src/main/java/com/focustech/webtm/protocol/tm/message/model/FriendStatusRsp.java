package com.focustech.webtm.protocol.tm.message.model;

import com.focustech.tm.open.sdk.messages.protobuf.Enums;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2017/7/18.
 */

public class FriendStatusRsp implements Serializable {
    String friendUserId;                // 好友ID
    List<Enums.EquipmentStatus> status; // 好友状态
    FriendInfoRsp friendInfoRsp;

    public String getFriendUserId() {
        return friendUserId;
    }

    public void setFriendUserId(String friendUserId) {
        this.friendUserId = friendUserId;
    }

    public List<Enums.EquipmentStatus> getStatus() {
        return status;
    }

    public void setStatus(List<Enums.EquipmentStatus> status) {
        this.status = status;
    }

    public FriendInfoRsp getFriendInfoRsp() {
        return friendInfoRsp;
    }

    public void setFriendInfoRsp(FriendInfoRsp friendInfoRsp) {
        this.friendInfoRsp = friendInfoRsp;
    }

    public static FriendStatusRsp parse(Enums.FriendStatusRsp rsp) {
        FriendStatusRsp friendStatusRsp=new FriendStatusRsp();
        friendStatusRsp.setFriendUserId(rsp.getFriendUserId());
        friendStatusRsp.setStatus(rsp.getStatusList());
        return friendStatusRsp;
    }
}
