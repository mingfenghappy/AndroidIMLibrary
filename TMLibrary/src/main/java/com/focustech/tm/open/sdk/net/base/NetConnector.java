package com.focustech.tm.open.sdk.net.base;

import android.content.Context;
import android.util.Log;

import com.focustech.tm.open.sdk.params.ConnectConfig;
import com.focustech.webtm.protocol.tm.message.model.BroadcastBean;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * 建立mina连接
 */
public class NetConnector {

	private IoConnector connector = null;
	private ConnectFuture cuture = null;
	private IoSession session = null;
	private IoHandler handler;

	// 可用的server地址
	private final List<InetSocketAddress> servers = new ArrayList<>();
	private final ConnectConfig connectConfig;
	// 当前连接状态
	private boolean isConnected;
	// 当前使用的服务
	private int useIndex = 0;
	// 当前连接的socket地址
	private InetSocketAddress currentServer;

	Context context;

	public NetConnector(ConnectConfig connectConfig, Context context) {
		this.connectConfig = connectConfig;
		this.context = context;
	}

	// 断开socket
	public void close() {
		if(session!=null && session.isConnected()){
			session.close(false);
			session=null;
		}
		if(cuture!=null && cuture.isConnected()) {
			cuture.cancel();
			cuture=null;
		}
		if (isConnected && connector!=null && connector.isActive() && !connector.isDisposed()) {
			isConnected = false;
			connector.dispose();
			connector=null;
		}
	}

	private void initConnector() {
		if (null!=connector && !connector.isDisposed()) {
			connector.dispose(true);
			connector=null;
		}
		connector = new NioSocketConnector(connectConfig.getIoProcessCount());
		//设置过滤器
		connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(connectConfig.getCodecFactory()));
		// 为接收器设置管理服务
		connector.setHandler(this.handler);
		connector.addListener(new IoServiceListener() {

			@Override
			public void sessionDestroyed(IoSession arg0) throws Exception {
				TMConnection.getInstance(context).setIo(null);
				Log.d("MTAPP", "服务器与客户端断开连接...");
				// 发送连接断开广播
				BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.Disconn, "");
				close();
			}

			@Override
			public void sessionCreated(IoSession arg0) throws Exception {
				Log.d("MTAPP", "服务器与客户端连接创建成功...");
				// 发送连接创建成功广播
				BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.Conn, "");
			}

			@Override
			public void serviceIdle(IoService arg0, IdleStatus arg1) throws Exception {
				Log.d("MTAPP", "客户端进入空闲状态...");
			}

			@Override
			public void serviceDeactivated(IoService arg0) throws Exception {
				Log.d("MTAPP", "服务被停用");
			}

			@Override
			public void serviceActivated(IoService arg0) throws Exception {
				Log.d("MTAPP", "服务被激活");
			}
		});
	}

	// 连接socket
	public boolean connect() {
		currentServer = currentServer();
		initConnector();
		Log.d("MTAPP", "开始连接:"+currentServer.toString());
		// 开始连接
		cuture = connector.connect(currentServer);
		// 等待是否连接成功，相当于是转异步执行为同步执行。
		cuture.awaitUninterruptibly(this.connectConfig.getConnectTimeout());
		isConnected = cuture.isConnected();
		Log.d("MTAPP", "连接状态:"+ (isConnected ? "connected" : "disconnected"));
		if (isConnected) {
			// 连接成功后获取会话对象。如果没有上面的等待，由于connect()方法是异步的，session可能会无法获取。
			session = cuture.getSession();
			// 设置发送连接session
			TMConnection.getInstance(context).setIo(session);
			return true;
		}
		return false;
	}

	/**
	 * 拿到当前的连接信息
	 * @return
	 */
	private InetSocketAddress currentServer() {
		if (useIndex >= this.servers.size()) {
			useIndex = 0;
		}
		return this.servers.get(useIndex++);
	}

	/**
	 * 判断当前是否连接成功
	 * @return
	 */
	public boolean isConnected() {
		return isConnected && null != session && session.isConnected() && null != connector && connector.isActive();
	}

	public void setHandler(IoHandler handler) {
		this.handler = handler;
	}

	public void addServer(String ip, int port) {
		this.servers.add(new InetSocketAddress(ip, port));
	}
}
