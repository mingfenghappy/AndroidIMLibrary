package com.focustech.common;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.focustech.message.model.BroadcastBean;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2018/4/18.
 */
public class MessageQueueUtils {
    private static Context context;
    private ConcurrentHashMap<String, Runnable> messages = new ConcurrentHashMap<>();

    // 循环队列
    private Thread looperThread;
    private Handler looperHandler;

    private volatile static MessageQueueUtils utils = null;

    public MessageQueueUtils() {
        runLoop();
    }

    public static MessageQueueUtils getInstance(Context context_) {
        if (utils == null) {
            synchronized (MessageQueueUtils.class) {
                if (utils == null) {
                    context = context_;
                    utils = new MessageQueueUtils();
                }
            }
        }
        return utils;
    }

    private void runLoop() {
        looperThread=new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                looperHandler=new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                    }
                };
                Looper.loop();
            }
        });
        looperThread.start();
    }

    public void add(String cliSeqId) {
        Runnable runnable = () -> {
            // 60s时间到了就判定发送失败
            remove(cliSeqId, false);
        };
        messages.put(cliSeqId, runnable);
        looperHandler.postDelayed(runnable, 60*1000);
    }

    /**
     * 发送消息成功或失败处理
     * @param cliSeqId
     */
    public void remove(String cliSeqId, boolean isOK) {
        // 剔除runnable
        Runnable runnable = messages.remove(cliSeqId);
        if (runnable != null) {
            Log.d("MTAPP", "发送成功？" + isOK);
            looperHandler.removeCallbacks(runnable);
            // 发送广播
            BroadcastBean.sendBroadcast(context, isOK?BroadcastBean.MTCommand.MessageComp:BroadcastBean.MTCommand.MessageFail, cliSeqId);
        }
    }
}
