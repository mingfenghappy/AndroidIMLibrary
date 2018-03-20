package com.focustech.tm.open.sdk.net.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class TMMessageCodecFactory implements ProtocolCodecFactory {

    @Override
    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return new TMMessageEncoderAdapter();
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        return new TMMessageDecoderAdapter();
    }
}
