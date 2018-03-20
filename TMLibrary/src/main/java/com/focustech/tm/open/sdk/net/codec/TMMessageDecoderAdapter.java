package com.focustech.tm.open.sdk.net.codec;

import com.focustech.webtm.protocol.tm.message.msg.TMMessage;
import com.focustech.tm.open.sdk.messages.protobuf.Head;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/**
 * TMMessage解码适配器，适配MINA的解码接口
 */
public class TMMessageDecoderAdapter extends CumulativeProtocolDecoder {
    // 内部缓冲，这里把未读完的数据缓存起来
    private final AttributeKey key = new AttributeKey(getClass(), "context");

    @Override
    protected boolean doDecode(IoSession session, IoBuffer in,
                               ProtocolDecoderOutput out) throws Exception {
        TMMessage message;

        Context ctx = getContext(session, in);

        byte curt;

        // 还有数据没有读就继续
        while (in.hasRemaining()) {
            // 读报文头
            if (!ctx.started) {
                curt = in.get();

                if (curt == 0x00) {
                    ctx.started = true;
                }
            }

            //读head length  4byte
            if (!ctx.isHeadLengthReaded()) {
                if (in.hasRemaining() && in.remaining() >= 4) {
                    ctx.headLength = in.getInt();
                } else {
                    // 数据不够等
                    return false;
                }
            }

            // 读body length 4byte
            if (!ctx.isBodyLengthReaded()) {
                if (in.hasRemaining() && in.remaining() >= 4) {
                    ctx.bodyLength = in.getInt();
                } else {
                    // 数据不够等
                    return false;
                }
            }

            // 读头消息
            if (!ctx.headReadEnd) {
                if (in.remaining() >= ctx.headLength) {
                    byte[] data = new byte[ctx.headLength];
                    in.get(data);
                    ctx.headData = data;
                    ctx.headReadEnd = true;
                } else{
                    // 数据不够等
                    return false;
                }
            }

            // 读body消息
            if (ctx.headReadEnd && !ctx.headBodyReadEnd) {
                if (in.remaining() >= ctx.bodyLength) {
                    byte[] data = new byte[ctx.bodyLength];
                    in.get(data);
                    ctx.bodyData = data;
                    ctx.headBodyReadEnd = true;
                }  else{
                    // 数据不够等
                    return false;
                }
            }

            // 读完消息了
            if (ctx.headBodyReadEnd) {
                // 清理
                session.removeAttribute(key);

                message = new TMMessage();
                message.setBody(ctx.bodyData);

                if (!(0 == ctx.headLength && 0 == ctx.bodyLength)) {
                    message.setHead(Head.TMHeadMessage.parseFrom(ctx.headData));
                }
                out.write(message);

                // 还有数据继续解包
                if (in.hasRemaining()) {
                    // 重新初始化
                    ctx = getContext(session, in);
                    continue;
                }

                // 返回读完了，不需要继续等报文内容
                return true;
            }
        }

        // 数据没有读完，返回false，继续等待数据
        return false;
    }

    /**
     * 获得上下文的缓冲
     *
     * @param session
     * @return
     */

    private Context getContext(IoSession session, IoBuffer in) {
        Context ctx = (Context) session.getAttribute(key);

        if (null == ctx) {
            ctx = new Context();
            session.setAttribute(key, ctx);
        }

        return ctx;
    }

    private class Context {
        // head数据缓冲
        public byte[] headData = null;
        // body数据
        public byte[] bodyData = null;
        public boolean started = false;
        public boolean headReadEnd = false;
        public boolean headBodyReadEnd = false;
        public int headLength = -1;
        public int bodyLength = -1;

        public boolean isHeadLengthReaded() {
            return -1 != headLength;
        }

        public boolean isBodyLengthReaded() {
            return -1 != bodyLength;
        }
    }
}
