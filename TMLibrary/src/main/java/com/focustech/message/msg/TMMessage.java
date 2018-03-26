package com.focustech.message.msg;

import com.focustech.tm.open.sdk.messages.protobuf.Head;
import org.apache.commons.lang.ArrayUtils;

/**
 * 通信平台消息
 */
public class TMMessage {

    // 消息头
    private Head.TMHeadMessage head;
    // 消息体
    private byte[] body = ArrayUtils.EMPTY_BYTE_ARRAY;

    public Head.TMHeadMessage getHead() {
        return head;
    }

    public void setHead(Head.TMHeadMessage head) {
        this.head = head;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    @Override
    public String toString () {
        return "TMMessage:\nhead="+head+"\nbody="+new String(body);
    }
}
