package com.renyu.easemobuilibrary.activity;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.SoundPool;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMCallManager;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.Status;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.exceptions.EMServiceNotReadyException;
import com.renyu.easemobuilibrary.base.BaseIMActivity;
import com.renyu.easemobuilibrary.model.BroadcastBean;

public abstract class CallActivity extends BaseIMActivity {
    protected final int MSG_CALL_MAKE_VIDEO = 0;
    protected final int MSG_CALL_MAKE_VOICE = 1;
    protected final int MSG_CALL_ANSWER = 2;
    protected final int MSG_CALL_REJECT = 3;
    protected final int MSG_CALL_END = 4;
    protected final int MSG_CALL_RELEASE_HANDLER = 5;
    protected final int MSG_CALL_SWITCH_CAMERA = 6;

    protected boolean isInComingCall;
    protected boolean isRefused = false;
    protected String username;
    protected CallingState callingState = CallingState.CANCELLED;
    protected String callDruationText;
    protected String msgid;
    protected AudioManager audioManager;
    protected SoundPool soundPool;
    protected Ringtone ringtone;
    protected int outgoing;
    protected EMCallStateChangeListener callStateListener;
    protected boolean isAnswered = false;
    protected int streamID = -1;
    
    EMCallManager.EMCallPushProvider pushProvider;
    
    // 0音频  1视频
    protected int callType = 0;

    @Override
    public void initParams() {
        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        pushProvider = new EMCallManager.EMCallPushProvider() {
            void updateMessageText(final EMMessage oldMsg) {
                EMConversation conv = EMClient.getInstance().chatManager().getConversation(oldMsg.getTo());
                conv.removeMessage(oldMsg.getMsgId());
            }

            @Override
            public void onRemoteOffline(final String to) {
                final EMMessage message = EMMessage.createTxtSendMessage("正在呼叫", to);
                message.setAttribute("em_apns_ext", true);
                message.setAttribute("is_voice_call", callType == 0);
                message.setMessageStatusCallback(new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        updateMessageText(message);
                    }

                    @Override
                    public void onError(int code, String error) {
                        updateMessageText(message);
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
                EMClient.getInstance().chatManager().sendMessage(message);
            }
        };
        EMClient.getInstance().callManager().setPushProvider(pushProvider);
    }
    
    @Override
    protected void onDestroy() {
        if (soundPool != null)
            soundPool.release();
        if (ringtone != null && ringtone.isPlaying())
            ringtone.stop();
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setMicrophoneMute(false);
        
        if(callStateListener != null)
            EMClient.getInstance().callManager().removeCallStateChangeListener(callStateListener);
        
        if (pushProvider != null) {
            EMClient.getInstance().callManager().setPushProvider(null);
            pushProvider = null;
        }
        releaseHandler();
        super.onDestroy();
    }
    
    @Override
    public void onBackPressed() {
        handler.sendEmptyMessage(MSG_CALL_END);
        saveCallRecord();
        finish();
    }

    Runnable timeoutHangup = new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessage(MSG_CALL_END);
        }
    };

    HandlerThread callHandlerThread = new HandlerThread("callHandlerThread");
    { callHandlerThread.start(); }

    protected Handler handler = new Handler(callHandlerThread.getLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CALL_MAKE_VIDEO:
                case MSG_CALL_MAKE_VOICE:
                    try {
                        if (msg.what == MSG_CALL_MAKE_VIDEO) {
                            EMClient.getInstance().callManager().makeVideoCall(username);
                        } else {
                            EMClient.getInstance().callManager().makeVoiceCall(username);
                        }
                    } catch (final EMServiceNotReadyException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            String st2 = e.getMessage();
                            if (e.getErrorCode() == EMError.CALL_REMOTE_OFFLINE) {
                                st2 = "对方不在线";
                            } else if (e.getErrorCode() == EMError.USER_NOT_LOGIN) {
                                st2 = "尚未连接至服务器";
                            } else if (e.getErrorCode() == EMError.INVALID_USER_NAME) {
                                st2 = "用户名不合法";
                            } else if (e.getErrorCode() == EMError.CALL_BUSY) {
                                st2 = "对方正在通话中";
                            } else if (e.getErrorCode() == EMError.NETWORK_ERROR) {
                                st2 = "连接不到聊天服务器";
                            }
                            Toast.makeText(CallActivity.this, st2, Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                    break;
                case MSG_CALL_ANSWER:
                    if (ringtone != null)
                        ringtone.stop();
                    if (isInComingCall) {
                        try {
                            EMClient.getInstance().callManager().answerCall();
                            isAnswered = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            saveCallRecord();
                            finish();
                            return;
                        }
                    }
                    break;
                case MSG_CALL_REJECT:
                    if (ringtone != null)
                        ringtone.stop();
                    try {
                        EMClient.getInstance().callManager().rejectCall();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        saveCallRecord();
                        finish();
                    }
                    callingState = CallingState.REFUSED;
                    break;
                case MSG_CALL_END:
                    if (soundPool != null)
                        soundPool.stop(streamID);
                    try {
                        EMClient.getInstance().callManager().endCall();
                    } catch (Exception e) {
                        saveCallRecord();
                        finish();
                    }
                    break;
                case MSG_CALL_RELEASE_HANDLER:
                    try {
                        EMClient.getInstance().callManager().endCall();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    handler.removeCallbacks(timeoutHangup);
                    handler.removeMessages(MSG_CALL_MAKE_VIDEO);
                    handler.removeMessages(MSG_CALL_MAKE_VOICE);
                    handler.removeMessages(MSG_CALL_ANSWER);
                    handler.removeMessages(MSG_CALL_REJECT);
                    handler.removeMessages(MSG_CALL_END);
                    callHandlerThread.quit();
                    break;
                case MSG_CALL_SWITCH_CAMERA:
                    EMClient.getInstance().callManager().switchCamera();
                    break;
                default:
                    break;
            }
        }
    };
    
    void releaseHandler() {
        handler.sendEmptyMessage(MSG_CALL_RELEASE_HANDLER);
    }

    protected int playMakeCallSounds() {
        try {
            audioManager.setMode(AudioManager.MODE_RINGTONE);
            audioManager.setSpeakerphoneOn(true);
            return soundPool.play(outgoing, // sound resource
                    0.3f, // left volume
                    0.3f, // right volume
                    1,    // priority
                    -1,   // loop，0 is no loop，-1 is loop forever
                    1);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    protected void openSpeakerOn() {
        try {
            if (!audioManager.isSpeakerphoneOn())
                audioManager.setSpeakerphoneOn(true);
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void closeSpeakerOn() {
        try {
            if (audioManager != null) {
                if (audioManager.isSpeakerphoneOn())
                    audioManager.setSpeakerphoneOn(false);
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存通话记录
     */
    protected void saveCallRecord() {
        EMMessage message;
        EMTextMessageBody txtBody;
        if (!isInComingCall) {
            // outgoing call
            message = EMMessage.createSendMessage(EMMessage.Type.TXT);
            message.setTo(username);
        } else {
            // incoming call
            message = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
            message.setFrom(username);
        }

        String st1 = "通话时长";
        String st2 = "已拒绝";
        String st3 = "对方已拒绝";
        String st4 = "对方不在线";
        String st5 = "对方正在通话中";
        String st6 = "对方未接听";
        String st7 = "未接听";
        String st8 = "已取消";
        String st9 = "通话协议版本不一致";
        switch (callingState) {
        case NORMAL:
            txtBody = new EMTextMessageBody(st1 + callDruationText);
            break;
        case REFUSED:
            txtBody = new EMTextMessageBody(st2);
            break;
        case BEREFUSED:
            txtBody = new EMTextMessageBody(st3);
            break;
        case OFFLINE:
            txtBody = new EMTextMessageBody(st4);
            break;
        case BUSY:
            txtBody = new EMTextMessageBody(st5);
            break;
        case NO_RESPONSE:
            txtBody = new EMTextMessageBody(st6);
            break;
        case UNANSWERED:
            txtBody = new EMTextMessageBody(st7);
            break;
        case VERSION_NOT_SAME:
            txtBody = new EMTextMessageBody(st9);
            break;
        default:
            txtBody = new EMTextMessageBody(st8);
            break;
        }
        if(callType == 0)
            message.setAttribute("is_voice_call", true);
        else
            message.setAttribute("is_video_call", true);
        message.addBody(txtBody);
        message.setMsgId(msgid);
        message.setUnread(false);
        message.setStatus(Status.SUCCESS);
        EMClient.getInstance().chatManager().saveMessage(message);

        // 发送刷新广播
        BroadcastBean.sendBroadcastParcelable(BroadcastBean.EaseMobCommand.MessageSend, message);
    }

    enum CallingState {
        CANCELLED, NORMAL, REFUSED, BEREFUSED, UNANSWERED, OFFLINE, NO_RESPONSE, BUSY, VERSION_NOT_SAME
    }
}
