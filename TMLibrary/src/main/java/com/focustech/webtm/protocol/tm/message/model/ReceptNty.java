package com.focustech.webtm.protocol.tm.message.model;

import com.focustech.tm.open.sdk.messages.protobuf.Enums;
import com.focustech.tm.open.sdk.messages.protobuf.Messages;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/7/18.
 */

public class ReceptNty implements Serializable {
    String cmd = "";                        // 回执的报文类型
    Enums.Equipment equipment;              // 回执设备
    String recentId = "";                   // 联系人ID
    Enums.RecentContactType recentType;     // 联系人类型
    long timestamp = 0;                     // 时间戳
    Enums.Enable active;                    // 窗口激活状态

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public Enums.Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Enums.Equipment equipment) {
        this.equipment = equipment;
    }

    public String getRecentId() {
        return recentId;
    }

    public void setRecentId(String recentId) {
        this.recentId = recentId;
    }

    public Enums.RecentContactType getRecentType() {
        return recentType;
    }

    public void setRecentType(Enums.RecentContactType recentType) {
        this.recentType = recentType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Enums.Enable getActive() {
        return active;
    }

    public void setActive(Enums.Enable active) {
        this.active = active;
    }

    public static ReceptNty parse(Messages.ReceptNty nty) {
        ReceptNty receptNty=new ReceptNty();
        receptNty.setCmd(nty.getCmd());
        receptNty.setEquipment(nty.getEquipment());
        return receptNty;
    }
}
