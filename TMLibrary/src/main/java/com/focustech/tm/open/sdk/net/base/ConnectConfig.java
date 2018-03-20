package com.focustech.tm.open.sdk.net.base;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * TM 连接配置
 */
public class ConnectConfig {
    /**
     * 心跳消息
     */
    private Object heartbeatMsg;
    /**
     * 连接超时时间，单位毫秒
     */
    private long connectTimeout = 25000;
    /**
     * 连接失败错误尝试次数
     */
    private int failedRetryTimes = 3;
    /**
     * 是否启用心跳
     */
    private boolean activeHeartbeat = false;
    /**
     * 连接失败错误尝试间隔时间，单位毫秒
     */
    private long failedRetryInterval = 3000;
    /**
     * 心跳间隔，单位毫秒
     */
    private long heartbeatInterval = 90000;
    /**
     * 通信平台
     */
    private List<String[]> servers = new ArrayList<>();
    /**
     * 消息监听器
     */
    private IoHandler ioHandler;
    /**
     * 编解码factory
     */
    private ProtocolCodecFactory codecFactory;
    /**
     * 默认IO处理器个数
     */
    private int ioProcessCount = Runtime.getRuntime().availableProcessors();

    public int getIoProcessCount() {
        return ioProcessCount;
    }

    public void setIoProcessCount(int ioProcessCount) {
        this.ioProcessCount = ioProcessCount;
    }

    public ProtocolCodecFactory getCodecFactory() {
        return codecFactory;
    }

    public void setCodecFactory(ProtocolCodecFactory codecFactory) {
        this.codecFactory = codecFactory;
    }

    public IoHandler getIoHandler() {
        return ioHandler;
    }

    public void setIoHandler(IoHandler ioHandler) {
        this.ioHandler = ioHandler;
    }

    public Object getHeartbeatMsg() {
        return heartbeatMsg;
    }

    public void setHeartbeatMsg(Object heartbeatMsg) {
        this.heartbeatMsg = heartbeatMsg;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getFailedRetryTimes() {
        return failedRetryTimes;
    }

    public void setFailedRetryTimes(int failedRetryTimes) {
        this.failedRetryTimes = failedRetryTimes;
    }

    public long getFailedRetryInterval() {
        return failedRetryInterval;
    }

    public void setFailedRetryInterval(long failedRetryInterval) {
        this.failedRetryInterval = failedRetryInterval;
    }

    public long getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(long heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public List<String[]> getServers() {
        return servers;
    }

    public void addServers(List<String[]> servers) {
        this.servers.addAll(servers);
    }

    public void addServer(String ip, int port) {
        this.servers.add(new String[]{ip, String.valueOf(port)});
    }

    public boolean isActiveHeartbeat() {
        return activeHeartbeat;
    }

    public void setActiveHeartbeat(boolean activeHeartbeat) {
        this.activeHeartbeat = activeHeartbeat;
    }
}
