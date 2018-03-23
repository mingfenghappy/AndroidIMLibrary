package com.focustech.webtm.protocol.tm.message.model;

/**
 * Created by renyu on 2017/6/6.
 */

public class OfflineIMResponse {
    /**
     * addTime : 1521608130000
     * fromUserId : 2Tj0laQY-KU
     * lastMsg : 一下
     * type : 0
     * unloadCount : 1
     * userHeadId : 570cb70be4b0d73781b29247
     * userHeadType : 1
     * userNickName : 365房博士-天津
     * userRole : 1
     */

    private long addTime;
    private String fromUserId;
    private String lastMsg;
    private int type;
    private int unloadCount;
    private String userHeadId;
    private int userHeadType;
    private String userNickName;
    private int userRole;

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getLastMsg() {
        return lastMsg;
    }

    public void setLastMsg(String lastMsg) {
        this.lastMsg = lastMsg;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getUnloadCount() {
        return unloadCount;
    }

    public void setUnloadCount(int unloadCount) {
        this.unloadCount = unloadCount;
    }

    public String getUserHeadId() {
        return userHeadId;
    }

    public void setUserHeadId(String userHeadId) {
        this.userHeadId = userHeadId;
    }

    public int getUserHeadType() {
        return userHeadType;
    }

    public void setUserHeadType(int userHeadType) {
        this.userHeadType = userHeadType;
    }

    public String getUserNickName() {
        return userNickName;
    }

    public void setUserNickName(String userNickName) {
        this.userNickName = userNickName;
    }

    public int getUserRole() {
        return userRole;
    }

    public void setUserRole(int userRole) {
        this.userRole = userRole;
    }
}
