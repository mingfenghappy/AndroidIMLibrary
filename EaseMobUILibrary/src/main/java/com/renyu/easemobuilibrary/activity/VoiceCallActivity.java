package com.renyu.easemobuilibrary.activity;

import android.media.AudioManager;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.Utils;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.renyu.easemobuilibrary.R;

import java.util.UUID;

public class VoiceCallActivity extends CallActivity implements View.OnClickListener {

    private LinearLayout comingBtnContainer;
    private Button hangupBtn;
    private Button refuseBtn;
    private Button answerBtn;
    private ImageView muteImage;
    private ImageView handsFreeImage;

    private boolean isMuteState;
    private boolean isHandsfreeState;

    private TextView callStateTextView;
    private boolean endCallTriggerByMe = false;
    private Chronometer chronometer;
    String st1;
    private LinearLayout voiceContronlLayout;
    private TextView netwrokStatusVeiw;

    @Override
    public void initParams() {
        super.initParams();

        callType = 0;

        comingBtnContainer = findViewById(R.id.ll_coming_call);
        refuseBtn = findViewById(R.id.btn_refuse_call);
        answerBtn = findViewById(R.id.btn_answer_call);
        hangupBtn = findViewById(R.id.btn_hangup_call);
        muteImage = findViewById(R.id.iv_mute);
        handsFreeImage = findViewById(R.id.iv_handsfree);
        callStateTextView = findViewById(R.id.tv_call_state);
        TextView nickTextView = findViewById(R.id.tv_nick);
        chronometer = findViewById(R.id.chronometer);
        voiceContronlLayout = findViewById(R.id.ll_voice_control);
        netwrokStatusVeiw = findViewById(R.id.tv_network_status);

        refuseBtn.setOnClickListener(this);
        answerBtn.setOnClickListener(this);
        hangupBtn.setOnClickListener(this);
        muteImage.setOnClickListener(this);
        handsFreeImage.setOnClickListener(this);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        addCallStateListener();
        msgid = UUID.randomUUID().toString();

        username = getIntent().getStringExtra("username");
        isInComingCall = getIntent().getBooleanExtra("isComingCall", false);
        nickTextView.setText(username);
        if (!isInComingCall) {// outgoing call
            soundPool = new SoundPool(1, AudioManager.STREAM_RING, 0);
            outgoing = soundPool.load(this, R.raw.em_outgoing, 1);

            comingBtnContainer.setVisibility(View.INVISIBLE);
            hangupBtn.setVisibility(View.VISIBLE);
            st1 = "正在连接对方...";
            callStateTextView.setText(st1);
            handler.sendEmptyMessage(MSG_CALL_MAKE_VOICE);
            handler.postDelayed(() -> streamID = playMakeCallSounds(), 300);
        } else { // incoming call
            voiceContronlLayout.setVisibility(View.INVISIBLE);
            Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            audioManager.setMode(AudioManager.MODE_RINGTONE);
            audioManager.setSpeakerphoneOn(true);
            ringtone = RingtoneManager.getRingtone(this, ringUri);
            ringtone.play();
        }
        final int MAKE_CALL_TIMEOUT = 50 * 1000;
        handler.removeCallbacks(timeoutHangup);
        handler.postDelayed(timeoutHangup, MAKE_CALL_TIMEOUT);
    }

    @Override
    public int initViews() {
        return R.layout.em_activity_voice_call;
    }

    @Override
    public void loadData() {

    }

    @Override
    public int setStatusBarColor() {
        return 0;
    }

    @Override
    public int setStatusBarTranslucent() {
        return 1;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if(savedInstanceState != null){
            finish();
            return;
        }
        super.onCreate(savedInstanceState);
    }

    void addCallStateListener() {
        callStateListener = (callState, error) -> {
            switch (callState) {
                case CONNECTING:
                    runOnUiThread(() -> callStateTextView.setText(st1));
                    break;
                case CONNECTED:
                    runOnUiThread(() -> callStateTextView.setText("已经和对方建立连接"));
                    break;
                case ACCEPTED:
                    handler.removeCallbacks(timeoutHangup);
                    runOnUiThread(() -> {
                        try {
                            if (soundPool != null)
                                soundPool.stop(streamID);
                        } catch (Exception e) {

                        }
                        if(!isHandsfreeState)
                            closeSpeakerOn();
                        chronometer.setVisibility(View.VISIBLE);
                        chronometer.setBase(SystemClock.elapsedRealtime());
                        chronometer.start();
                        callStateTextView.setText("通话中...");
                        callingState = CallingState.NORMAL;
                    });
                    break;
                case NETWORK_UNSTABLE:
                    runOnUiThread(() -> {
                        netwrokStatusVeiw.setVisibility(View.VISIBLE);
                        if(error == EMCallStateChangeListener.CallError.ERROR_NO_DATA){
                            netwrokStatusVeiw.setText("没有通话数据");
                        }else{
                            netwrokStatusVeiw.setText("网络不稳定");
                        }
                    });
                    break;
                case NETWORK_NORMAL:
                    runOnUiThread(() -> netwrokStatusVeiw.setVisibility(View.INVISIBLE));
                    break;
                case VOICE_PAUSE:
                    runOnUiThread(() -> Toast.makeText(Utils.getApp(), "通话暂停", Toast.LENGTH_SHORT).show());
                    break;
                case VOICE_RESUME:
                    runOnUiThread(() -> Toast.makeText(Utils.getApp(), "通话恢复", Toast.LENGTH_SHORT).show());
                    break;
                case DISCONNECTED:
                    handler.removeCallbacks(timeoutHangup);
                    final EMCallStateChangeListener.CallError fError = error;
                    runOnUiThread(new Runnable() {
                        private void postDelayedCloseMsg() {
                            handler.postDelayed(() -> runOnUiThread(() -> {
                                removeCallStateListener();
                                saveCallRecord();
                                Animation animation = new AlphaAnimation(1.0f, 0.0f);
                                animation.setDuration(800);
                                findViewById(R.id.root_layout).startAnimation(animation);
                                finish();
                            }), 200);
                        }

                        @Override
                        public void run() {
                            chronometer.stop();
                            callDruationText = chronometer.getText().toString();
                            String st1 = "已拒绝";
                            String st2 = "对方拒绝接受！";
                            String st3 = "连接建立失败！";
                            String st4 = "对方不在线，请稍后再拨...";
                            String st5 = "对方正在通话中，请稍后再拨";
                            String st6 = "对方未接听";
                            String st7 = "通话协议版本不一致";
                            String st8 = "对方已经挂断";
                            String st9 = "对方未接听";
                            String st10 = "已取消";
                            String st11 = "挂断";
                            if (fError == EMCallStateChangeListener.CallError.REJECTED) {
                                callingState = CallingState.BEREFUSED;
                                callStateTextView.setText(st2);
                            } else if (fError == EMCallStateChangeListener.CallError.ERROR_TRANSPORT) {
                                callStateTextView.setText(st3);
                            } else if (fError == EMCallStateChangeListener.CallError.ERROR_UNAVAILABLE) {
                                callingState = CallingState.OFFLINE;
                                callStateTextView.setText(st4);
                            } else if (fError == EMCallStateChangeListener.CallError.ERROR_BUSY) {
                                callingState = CallingState.BUSY;
                                callStateTextView.setText(st5);
                            } else if (fError == EMCallStateChangeListener.CallError.ERROR_NORESPONSE) {
                                callingState = CallingState.NO_RESPONSE;
                                callStateTextView.setText(st6);
                            } else if (fError == EMCallStateChangeListener.CallError.ERROR_LOCAL_SDK_VERSION_OUTDATED || fError == EMCallStateChangeListener.CallError.ERROR_REMOTE_SDK_VERSION_OUTDATED){
                                callingState = CallingState.VERSION_NOT_SAME;
                                callStateTextView.setText(st7);
                            } else {
                                if (isRefused) {
                                    callingState = CallingState.REFUSED;
                                    callStateTextView.setText(st1);
                                }
                                else if (isAnswered) {
                                    callingState = CallingState.NORMAL;
                                    if (endCallTriggerByMe) {
                                    } else {
                                        callStateTextView.setText(st8);
                                    }
                                } else {
                                    if (isInComingCall) {
                                        callingState = CallingState.UNANSWERED;
                                        callStateTextView.setText(st9);
                                    } else {
                                        if (callingState != CallingState.NORMAL) {
                                            callingState = CallingState.CANCELLED;
                                            callStateTextView.setText(st10);
                                        }else {
                                            callStateTextView.setText(st11);
                                        }
                                    }
                                }
                            }
                            postDelayedCloseMsg();
                        }
                    });
                    break;
                default:
                    break;
            }

        };
        EMClient.getInstance().callManager().addCallStateChangeListener(callStateListener);
    }

    void removeCallStateListener() {
        EMClient.getInstance().callManager().removeCallStateChangeListener(callStateListener);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_refuse_call) {
            isRefused = true;
            refuseBtn.setEnabled(false);
            handler.sendEmptyMessage(MSG_CALL_REJECT);
        }
        else if (v.getId() == R.id.btn_answer_call) {
            answerBtn.setEnabled(false);
            closeSpeakerOn();
            callStateTextView.setText("正在接听...");
            comingBtnContainer.setVisibility(View.INVISIBLE);
            hangupBtn.setVisibility(View.VISIBLE);
            voiceContronlLayout.setVisibility(View.VISIBLE);
            handler.sendEmptyMessage(MSG_CALL_ANSWER);
        }
        else if (v.getId() == R.id.btn_hangup_call) {
            hangupBtn.setEnabled(false);
            chronometer.stop();
            endCallTriggerByMe = true;
            callStateTextView.setText("正在挂断...");
            handler.sendEmptyMessage(MSG_CALL_END);
        }
        else if (v.getId() == R.id.iv_mute) {
            if (isMuteState) {
                muteImage.setImageResource(R.mipmap.em_icon_mute_normal);
                try {
                    EMClient.getInstance().callManager().resumeVoiceTransfer();
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
                isMuteState = false;
            } else {
                muteImage.setImageResource(R.mipmap.em_icon_mute_on);
                try {
                    EMClient.getInstance().callManager().pauseVoiceTransfer();
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
                isMuteState = true;
            }
        }
        else if (v.getId() == R.id.iv_handsfree) {
            if (isHandsfreeState) {
                handsFreeImage.setImageResource(R.mipmap.em_icon_speaker_normal);
                closeSpeakerOn();
                isHandsfreeState = false;
            } else {
                handsFreeImage.setImageResource(R.mipmap.em_icon_speaker_on);
                openSpeakerOn();
                isHandsfreeState = true;
            }
        }
    }

    @Override
    public void onBackPressed() {
        callDruationText = chronometer.getText().toString();
    }
}
