package com.focustech.message.model;

/**
 * Created by Administrator on 2018/3/23 0023.
 */

public class OfflineIMDetailResponse {
    /**
     * addTime : 1519438522000
     * addUserId : 6786
     * deleteFlag : 1
     * fromUserId : ttxPdzMAHf4
     * messageType : 0
     * msg : 。
     * msgId : 4881423
     * msgMeta : {"ct":0,"fp":"","fs":0,"fc":0,"f":10,"fst":0,"ht":0,"exe":false,"fi":0,"mt":0,"pc":0,"pp":"{}","rfs":0,"t":0,"s":"wuhan"}
     * svrMsgId : d7a44737659445e19172ab21bfa65a64
     * timestamp : 1519438522609
     * toUserId : InI60G--Oqs
     * type : 0
     * updateTime : 1519438522000
     * updateUserId : ttxPdzMAHf4
     */

    private long addTime;
    private int addUserId;
    private int deleteFlag;
    private String fromUserId;
    private int messageType;
    private String msg;
    private int msgId;
    private String msgMeta;
    private String svrMsgId;
    private long timestamp;
    private String toUserId;
    private int type;
    private long updateTime;
    private String updateUserId;

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    public int getAddUserId() {
        return addUserId;
    }

    public void setAddUserId(int addUserId) {
        this.addUserId = addUserId;
    }

    public int getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(int deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public String getMsgMeta() {
        return msgMeta;
    }

    public void setMsgMeta(String msgMeta) {
        this.msgMeta = msgMeta;
    }

    public String getSvrMsgId() {
        return svrMsgId;
    }

    public void setSvrMsgId(String svrMsgId) {
        this.svrMsgId = svrMsgId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(String updateUserId) {
        this.updateUserId = updateUserId;
    }
}
