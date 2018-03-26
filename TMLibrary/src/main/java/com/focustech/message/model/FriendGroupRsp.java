package com.focustech.message.model;

import com.focustech.tm.open.sdk.messages.protobuf.Contacts;
import com.focustech.tm.open.sdk.messages.protobuf.Enums;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/18.
 */

public class FriendGroupRsp implements Serializable {
    String friendGroupId;                     // 好友分组ID
    String friendGroupName;                   // 好友分组名称
    Enums.FriendGroupType friendGroupType;    // 好友分组类型
    List<FriendStatusRsp> friends;            // 所有好友

    public String getFriendGroupId() {
        return friendGroupId;
    }

    public void setFriendGroupId(String friendGroupId) {
        this.friendGroupId = friendGroupId;
    }

    public String getFriendGroupName() {
        return friendGroupName;
    }

    public void setFriendGroupName(String friendGroupName) {
        this.friendGroupName = friendGroupName;
    }

    public Enums.FriendGroupType getFriendGroupType() {
        return friendGroupType;
    }

    public void setFriendGroupType(Enums.FriendGroupType friendGroupType) {
        this.friendGroupType = friendGroupType;
    }

    public List<FriendStatusRsp> getFriends() {
        return friends;
    }

    public void setFriends(List<FriendStatusRsp> friends) {
        this.friends = friends;
    }

    public static FriendGroupRsp parse(Contacts.FriendGroupRsp friendGroup) {
        FriendGroupRsp rsp=new FriendGroupRsp();
        rsp.setFriendGroupId(friendGroup.getFriendGroupId());
        rsp.setFriendGroupName(friendGroup.getFriendGroupName());
        rsp.setFriendGroupType(friendGroup.getFriendGroupType());
        ArrayList<FriendStatusRsp> lists=new ArrayList<>();
        for (Enums.FriendStatusRsp friendStatusRsp : friendGroup.getFriendsList()) {
            FriendStatusRsp temp=FriendStatusRsp.parse(friendStatusRsp);
            lists.add(temp);
        }
        rsp.setFriends(lists);
        return rsp;
    }
}
