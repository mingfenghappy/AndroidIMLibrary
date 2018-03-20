package com.focustech.tm.open.sdk.net.base;

import android.content.Context;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

/**
 * 业务处理接口
 */
public class TMMessageProcessorAdapter extends IoHandlerAdapter {

    Context context;

    public TMMessageProcessorAdapter(Context context) {
        this.context=context;
    }

    private TMMessageProcessorAdapter() {

    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
    	TMConnection.getInstance(context).recvice(message);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {

	}
}
