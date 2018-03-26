package com.focustech.tm.open.sdk.net.base;

import android.content.Context;
import android.util.Log;

import com.focustech.tm.open.sdk.net.impl.Cmd;
import com.focustech.params.FusionField;
import com.focustech.message.IMessageHandler;
import com.focustech.message.MTMessageHandlerAdapter;
import com.focustech.message.msg.TMMessage;

import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TMConnection {

	private static volatile TMConnection current;

	// 会话session
	private IoSession session;

	private ExecutorService outService;
	private ExecutorService inService;

	private IMessageHandler messageHandler;
	private HashMap<String, Method> methodMapper;

	public static TMConnection getInstance(Context context) {
		synchronized (TMConnection.class) {
			if (current==null) {
				synchronized (TMConnection.class) {
					current = new TMConnection(context);
				}
			}
		}
		return current;
	}

	private TMConnection(Context context) {
		methodMapper = new HashMap<>();

		this.outService = Executors.newSingleThreadExecutor();
		this.inService = Executors.newSingleThreadExecutor();

		if(null == messageHandler) {
			messageHandler = new MTMessageHandlerAdapter(context);
			for (Method m : IMessageHandler.class.getDeclaredMethods()) {
				Cmd cmd = m.getAnnotation(Cmd.class);
				if (cmd != null) {
					// 将所有事件的注解方法进行存储
					methodMapper.put(cmd.value(), m);
				}
			}
		}
	}

	/**
	 * 设置会话session
	 * @param session
	 */
	public void setIo(IoSession session) {
		this.session = session;
	}

	/**
	 * 发送消息
	 * @param message
	 */
	public void send(final Object message) {
		outService.execute(() -> {
            if(null != session) {
                if(null == message) {
                    return;
                }
                if (message instanceof TMMessage && ((TMMessage) message).getHead()!=null) {
					Log.i("MT", "out ===>>" + message.toString());
				}
                try {
                    // 发送
                    WriteFuture future = session.write(message);
                    future.awaitUninterruptibly(FusionField.connectTimeout, TimeUnit.SECONDS);
                    if(!future.isWritten()) {
                        // 数据发送失败
						Log.d("MTAPP", "发送失败");
					}
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
	}

	/**
	 * 接受消息
	 * @param message
	 */
	public void recvice(final Object message) {
		inService.execute(() -> {
            if(message instanceof TMMessage) {
                TMMessage msg = (TMMessage)message;
				// 心跳包
				if (msg.getHead()==null) {
					try {
						methodMapper.get("HeartBeatRsp").invoke(messageHandler);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
                else {
					Log.i("MT", "in ===>>" + message.toString());
					String cmd = msg.getHead().getCmd();
					if (methodMapper.containsKey(cmd)) {
						try {
							methodMapper.get(cmd).invoke(messageHandler, new Object[]{message});
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
            }
        });
	}
}
