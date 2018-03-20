package com.focustech.tm.open.sdk.net.codec;

import com.focustech.common.ByteUtils;
import com.focustech.webtm.protocol.tm.message.msg.TMMessage;

import org.apache.commons.lang.ArrayUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * TMMessage编码适配器，适配MINA的编码接口
 */
public class TMMessageEncoderAdapter extends ProtocolEncoderAdapter {

    private static final int TAG_HEAD_BODY_LENGTH = 9;

    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        byte[] dataToSend;

        // 字节数组
        if (message.getClass() == byte[].class) {
            // 直接发送
            dataToSend = (byte[]) message;
        }
        else {
            dataToSend = encode((TMMessage) message);
        }

        IoBuffer buffer = IoBuffer.allocate(1024);
        buffer.setAutoExpand(true);
        buffer.put(dataToSend);
        buffer.flip();
        out.write(buffer);
        out.flush();
    }

    /**
     * 编码 message
     *
     * @param message
     * @return
     */
    public static byte[] encode(TMMessage message) {
        byte[] head = null == message.getHead() ? ArrayUtils.EMPTY_BYTE_ARRAY : message.getHead().toByteArray();
        byte[] body = message.getBody();
        byte[] headLength = ByteUtils.int2byte(head.length);
        byte[] bodyLength = ByteUtils.int2byte(body.length);

        byte[] data = new byte[TAG_HEAD_BODY_LENGTH + head.length + body.length];

        int offset = 1;
        System.arraycopy(headLength, 0, data, offset, headLength.length);

        offset += headLength.length;
        System.arraycopy(bodyLength, 0, data, offset, bodyLength.length);

        if (0 != head.length) {
            offset += bodyLength.length;
            System.arraycopy(head, 0, data, offset, head.length);
        }

        if (0 != body.length) {
            offset += head.length;
            System.arraycopy(body, 0, data, offset, body.length);
        }

        return data;
    }
}
