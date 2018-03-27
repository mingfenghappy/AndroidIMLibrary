package com.focustech.tm.open.sdk.net.base;

import android.content.Context;
import android.util.Log;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
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
        Log.d("MTAPP", "messageReceived");
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
        Log.d("MTAPP", "messageSent");
	}

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        super.sessionClosed(session);
        Log.d("MTAPP", "sessionClosed");
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        super.sessionCreated(session);
        Log.d("MTAPP", "sessionCreated");
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        super.sessionIdle(session, status);
        Log.d("MTAPP", "sessionIdle");
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        super.sessionOpened(session);
        Log.d("MTAPP", "sessionOpened");
    }
}
