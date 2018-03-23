package com.focustech.webtm.protocol.tm.message.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/3/23 0023.
 */

public class FileInfoBean implements Serializable {
    String svrMsgId;
    long fileSize;

    public String getSvrMsgId() {
        return svrMsgId;
    }

    public void setSvrMsgId(String svrMsgId) {
        this.svrMsgId = svrMsgId;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
