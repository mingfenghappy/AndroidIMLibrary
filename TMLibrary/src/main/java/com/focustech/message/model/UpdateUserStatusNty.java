package com.focustech.message.model;

import com.focustech.tm.open.sdk.messages.protobuf.Enums;
import com.focustech.tm.open.sdk.messages.protobuf.User;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/7/24.
 */

public class UpdateUserStatusNty implements Serializable {
    String userId;                         // 状态更新的用户userId
    Enums.EquipmentStatus status;          // 状态
    Enums.Enable isNotice;                 // 是否提示

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Enums.EquipmentStatus getStatus() {
        return status;
    }

    public void setStatus(Enums.EquipmentStatus status) {
        this.status = status;
    }

    public Enums.Enable getIsNotice() {
        return isNotice;
    }

    public void setIsNotice(Enums.Enable isNotice) {
        this.isNotice = isNotice;
    }

    public static UpdateUserStatusNty parse(User.UpdateUserStatusNty nty) {
        UpdateUserStatusNty updateUserStatusNty=new UpdateUserStatusNty();
        updateUserStatusNty.setIsNotice(nty.getIsNotice());
        updateUserStatusNty.setStatus(nty.getStatus());
        updateUserStatusNty.setUserId(nty.getUserId());
        return updateUserStatusNty;
    }
}
