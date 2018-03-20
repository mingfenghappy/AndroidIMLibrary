package com.focustech.webtm.protocol.tm.message.model;

import com.focustech.tm.open.sdk.messages.protobuf.Contacts;
import com.focustech.tm.open.sdk.messages.protobuf.Enums;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/7/18.
 */

public class FriendInfoRsp implements Serializable {
    UserInfoRsp friend;           // 用户
    String friendGroupId;         // 好友分组ID
    String friendGroupName;       // 好友分组名称
    String remark;                // 用户备注
    long lastChatTimestamp;       // 最后聊天时间
    Enums.Enable onlineRemind ;   // 上线提醒

    public UserInfoRsp getFriend() {
        return friend;
    }

    public void setFriend(UserInfoRsp friend) {
        this.friend = friend;
    }

    public String getFriendGroupId() {
        return friendGroupId;
    }

    public void setFriendGroupId(String friendGroupId) {
        this.friendGroupId = friendGroupId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public long getLastChatTimestamp() {
        return lastChatTimestamp;
    }

    public void setLastChatTimestamp(long lastChatTimestamp) {
        this.lastChatTimestamp = lastChatTimestamp;
    }

    public Enums.Enable getOnlineRemind() {
        return onlineRemind;
    }

    public void setOnlineRemind(Enums.Enable onlineRemind) {
        this.onlineRemind = onlineRemind;
    }

    public String getFriendGroupName() {
        return friendGroupName;
    }

    public void setFriendGroupName(String friendGroupName) {
        this.friendGroupName = friendGroupName;
    }

    public static FriendInfoRsp parse(Contacts.FriendInfoRsp rsp) {
        FriendInfoRsp friendInfoRsp=new FriendInfoRsp();
        friendInfoRsp.setFriendGroupId(rsp.getFriendGroupId());
        friendInfoRsp.setLastChatTimestamp(rsp.getLastChatTimestamp());
        friendInfoRsp.setOnlineRemind(rsp.getOnlineRemind());
        friendInfoRsp.setRemark(rsp.getRemark());
        UserInfoRsp userInfoRsp=UserInfoRsp.parse(rsp.getFriend());
        friendInfoRsp.setFriend(userInfoRsp);
        return friendInfoRsp;
    }
}
