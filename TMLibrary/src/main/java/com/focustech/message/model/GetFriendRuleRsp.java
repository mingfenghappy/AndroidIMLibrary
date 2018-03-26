package com.focustech.message.model;

import com.focustech.tm.open.sdk.messages.protobuf.Contacts;
import com.focustech.tm.open.sdk.messages.protobuf.Enums;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/8/2.
 */

public class GetFriendRuleRsp implements Serializable {
    String userId;
    Enums.ValidateRule friendRule;
    long code = 3;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Enums.ValidateRule getFriendRule() {
        return friendRule;
    }

    public void setFriendRule(Enums.ValidateRule friendRule) {
        this.friendRule = friendRule;
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public static GetFriendRuleRsp parse(Contacts.GetFriendRuleRsp rsp) {
        GetFriendRuleRsp getFriendRuleRsp=new GetFriendRuleRsp();
        getFriendRuleRsp.setUserId(rsp.getUserId());
        getFriendRuleRsp.setCode(rsp.getCode());
        getFriendRuleRsp.setFriendRule(rsp.getFriendRule());
        return getFriendRuleRsp;
    }
}
