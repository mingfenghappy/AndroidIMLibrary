package com.renyu.easemobuilibrary.activity;

import android.media.AudioManager;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMVideoCallHelper;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.media.EMCallSurfaceView;
import com.renyu.easemobuilibrary.R;
import com.superrtc.sdk.VideoView;

import java.io.File;
import java.util.Date;
import java.util.UUID;

public class VideoCallActivity extends CallActivity implements View.OnClickListener {

    private boolean isMuteState;
    private boolean isHandsfreeState;
    private boolean isAnswered;
    private boolean endCallTriggerByMe = false;

    // 视频通话画面显示控件，这里在新版中使用同一类型的控件，方便本地和远端视图切换
    protected EMCallSurfaceView localSurface;
    protected EMCallSurfaceView oppositeSurface;
    private int surfaceState = -1;

    private TextView callStateTextView;

    private LinearLayout comingBtnContainer;
    private Button refuseBtn;
    private Button answerBtn;
    private Button hangupBtn;
    private ImageView muteImage;
    private ImageView handsFreeImage;
    private TextView nickTextView;
    private Chronometer chronometer = (Chronometer) findViewById(R.id.chronometer);
    private LinearLayout voiceContronlLayout;
    private RelativeLayout rootContainer;
    private LinearLayout topContainer;
    private LinearLayout bottomContainer;
    private TextView monitorTextView;
    private TextView netwrokStatusVeiw;

    private Handler uiHandler;

    private boolean isInCalling;
    boolean isRecording = false;
    private EMVideoCallHelper callHelper;

    @Override
    public int initViews() {
        return R.layout.em_activity_video_call;
    }

    @Override
    public void initParams() {
        super.initParams();

        callType = 1;

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        uiHandler = new Handler();

        callStateTextView = findViewById(R.id.tv_call_state);
        comingBtnContainer = findViewById(R.id.ll_coming_call);
        rootContainer = findViewById(R.id.root_layout);
        refuseBtn = findViewById(R.id.btn_refuse_call);
        answerBtn = findViewById(R.id.btn_answer_call);
        hangupBtn = findViewById(R.id.btn_hangup_call);
        muteImage = findViewById(R.id.iv_mute);
        handsFreeImage = findViewById(R.id.iv_handsfree);
        callStateTextView = findViewById(R.id.tv_call_state);
        nickTextView = findViewById(R.id.tv_nick);
        voiceContronlLayout = findViewById(R.id.ll_voice_control);
        topContainer = findViewById(R.id.ll_top_container);
        bottomContainer = findViewById(R.id.ll_bottom_container);
        monitorTextView = findViewById(R.id.tv_call_monitor);
        netwrokStatusVeiw = findViewById(R.id.tv_network_status);
        Button switchCameraBtn = findViewById(R.id.btn_switch_camera);
        Button captureImageBtn = findViewById(R.id.btn_capture_image);

        refuseBtn.setOnClickListener(this);
        answerBtn.setOnClickListener(this);
        hangupBtn.setOnClickListener(this);
        muteImage.setOnClickListener(this);
        handsFreeImage.setOnClickListener(this);
        rootContainer.setOnClickListener(this);
        switchCameraBtn.setOnClickListener(this);
        captureImageBtn.setOnClickListener(this);

        msgid = UUID.randomUUID().toString();
        isInComingCall = getIntent().getBooleanExtra("isComingCall", false);
        username = getIntent().getStringExtra("username");

        nickTextView.setText(username);

        // local surfaceview
        localSurface = findViewById(R.id.local_surface);
        localSurface.setOnClickListener(this);
        localSurface.setZOrderMediaOverlay(true);
        localSurface.setZOrderOnTop(true);

        // remote surfaceview
        oppositeSurface = findViewById(R.id.opposite_surface);

        // set call state listener
        addCallStateListener();
        if (!isInComingCall) {// outgoing call
            soundPool = new SoundPool(1, AudioManager.STREAM_RING, 0);
            outgoing = soundPool.load(this, R.raw.em_outgoing, 1);

            comingBtnContainer.setVisibility(View.INVISIBLE);
            hangupBtn.setVisibility(View.VISIBLE);
            callStateTextView.setText("正在连接对方...");
            EMClient.getInstance().callManager().setSurfaceView(localSurface, oppositeSurface);
            handler.sendEmptyMessage(MSG_CALL_MAKE_VIDEO);
            handler.postDelayed(() -> streamID = playMakeCallSounds(), 300);
        } else { // incoming call
            callStateTextView.setText("Ringing");
            if(EMClient.getInstance().callManager().getCallState() == EMCallStateChangeListener.CallState.IDLE
                    || EMClient.getInstance().callManager().getCallState() == EMCallStateChangeListener.CallState.DISCONNECTED) {
                // the call has ended
                finish();
                return;
            }
            voiceContronlLayout.setVisibility(View.INVISIBLE);
            localSurface.setVisibility(View.INVISIBLE);
            Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            audioManager.setMode(AudioManager.MODE_RINGTONE);
            audioManager.setSpeakerphoneOn(true);
            ringtone = RingtoneManager.getRingtone(this, ringUri);
            ringtone.play();
            EMClient.getInstance().callManager().setSurfaceView(localSurface, oppositeSurface);
        }

        final int MAKE_CALL_TIMEOUT = 50 * 1000;
        handler.removeCallbacks(timeoutHangup);
        handler.postDelayed(timeoutHangup, MAKE_CALL_TIMEOUT);

        callHelper = EMClient.getInstance().callManager().getVideoCallHelper();
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
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.local_surface) {
            changeCallView();
        } else if (i == R.id.btn_refuse_call) {
            isRefused = true;
            refuseBtn.setEnabled(false);
            handler.sendEmptyMessage(MSG_CALL_REJECT);
        } else if (i == R.id.btn_answer_call) {
            answerBtn.setEnabled(false);
            openSpeakerOn();
            if (ringtone != null)
                ringtone.stop();
            callStateTextView.setText("answering...");
            handler.sendEmptyMessage(MSG_CALL_ANSWER);
            handsFreeImage.setImageResource(R.mipmap.em_icon_speaker_on);
            isAnswered = true;
            isHandsfreeState = true;
            comingBtnContainer.setVisibility(View.INVISIBLE);
            hangupBtn.setVisibility(View.VISIBLE);
            voiceContronlLayout.setVisibility(View.VISIBLE);
            localSurface.setVisibility(View.VISIBLE);
        } else if (i == R.id.btn_hangup_call) {
            hangupBtn.setEnabled(false);
            chronometer.stop();
            endCallTriggerByMe = true;
            callStateTextView.setText("正在挂断...");
            if (isRecording) {
                callHelper.stopVideoRecord();
            }
            handler.sendEmptyMessage(MSG_CALL_END);
        } else if (i == R.id.iv_mute) {
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
        } else if (i == R.id.iv_handsfree) {
            if (isHandsfreeState) {
                // turn off speaker
                handsFreeImage.setImageResource(R.mipmap.em_icon_speaker_normal);
                closeSpeakerOn();
                isHandsfreeState = false;
            } else {
                handsFreeImage.setImageResource(R.mipmap.em_icon_speaker_on);
                openSpeakerOn();
                isHandsfreeState = true;
            }
        } else if (i == R.id.root_layout) {
            if (callingState == CallingState.NORMAL) {
                if (bottomContainer.getVisibility() == View.VISIBLE) {
                    bottomContainer.setVisibility(View.GONE);
                    topContainer.setVisibility(View.GONE);
                    oppositeSurface.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
                } else {
                    bottomContainer.setVisibility(View.VISIBLE);
                    topContainer.setVisibility(View.VISIBLE);
                    oppositeSurface.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFit);
                }
            }
        } else if (i == R.id.btn_switch_camera) {
            handler.sendEmptyMessage(MSG_CALL_SWITCH_CAMERA);
        } else if (i == R.id.btn_capture_image) {
            DateFormat df = new DateFormat();
            Date d = new Date();
            File storage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            final String filename = storage.getAbsolutePath() + "/" + df.format("MM-dd-yy--h-mm-ss", d) + ".jpg";
            EMClient.getInstance().callManager().getVideoCallHelper().takePicture(filename);
            runOnUiThread(() -> Toast.makeText(VideoCallActivity.this, "saved image to:" + filename, Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * 切换通话界面，这里就是交换本地和远端画面控件设置，以达到通话大小画面的切换
     */
    private void changeCallView() {
        if (surfaceState == 0) {
            surfaceState = 1;
            EMClient.getInstance().callManager().setSurfaceView(oppositeSurface, localSurface);
        } else {
            surfaceState = 0;
            EMClient.getInstance().callManager().setSurfaceView(localSurface, oppositeSurface);
        }
    }

    /**
     * set call state listener
     */
    void addCallStateListener() {
        callStateListener = (callState, error) -> {
            switch (callState) {
                case CONNECTING: // is connecting
                    runOnUiThread(() -> callStateTextView.setText("正在连接对方..."));
                    break;
                case CONNECTED: // connected
                    runOnUiThread(() -> callStateTextView.setText("已经和对方建立连接"));
                    break;
                case ACCEPTED: // call is accepted
                    surfaceState = 0;
                    handler.removeCallbacks(timeoutHangup);
                    runOnUiThread(() -> {
                        try {
                            if (soundPool != null)
                                soundPool.stop(streamID);
                        } catch (Exception e) {

                        }
                        openSpeakerOn();
                        handsFreeImage.setImageResource(R.mipmap.em_icon_speaker_on);
                        isHandsfreeState = true;
                        isInCalling = true;
                        chronometer.setVisibility(View.VISIBLE);
                        chronometer.setBase(SystemClock.elapsedRealtime());
                        chronometer.start();
                        nickTextView.setVisibility(View.INVISIBLE);
                        callStateTextView.setText("通话中...");
                        callingState = CallingState.NORMAL;
                    });
                    break;
                case NETWORK_DISCONNECTED:
                    runOnUiThread(() -> {
                        netwrokStatusVeiw.setVisibility(View.VISIBLE);
                        netwrokStatusVeiw.setText("网络连接不可用，请检查网络");
                    });
                    break;
                case NETWORK_UNSTABLE:
                    runOnUiThread(() -> {
                        netwrokStatusVeiw.setVisibility(View.VISIBLE);
                        if(error == EMCallStateChangeListener.CallError.ERROR_NO_DATA) {
                            netwrokStatusVeiw.setText("没有通话数据");
                        } else {
                            netwrokStatusVeiw.setText("网络不稳定");
                        }
                    });
                    break;
                case NETWORK_NORMAL:
                    runOnUiThread(() -> netwrokStatusVeiw.setVisibility(View.INVISIBLE));
                    break;
                case VIDEO_PAUSE:
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "通话暂停", Toast.LENGTH_SHORT).show());
                    break;
                case VIDEO_RESUME:
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "通话恢复", Toast.LENGTH_SHORT).show());
                    break;
                case VOICE_PAUSE:
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "通话暂停", Toast.LENGTH_SHORT).show());
                    break;
                case VOICE_RESUME:
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "通话恢复", Toast.LENGTH_SHORT).show());
                    break;
                case DISCONNECTED: // call is disconnected
                    handler.removeCallbacks(timeoutHangup);
                    final EMCallStateChangeListener.CallError fError = error;
                    runOnUiThread(new Runnable() {
                        private void postDelayedCloseMsg() {
                            uiHandler.postDelayed(() -> {
                                removeCallStateListener();
                                saveCallRecord();
                                Animation animation = new AlphaAnimation(1.0f, 0.0f);
                                animation.setDuration(1200);
                                rootContainer.startAnimation(animation);
                                finish();
                            }, 200);
                        }

                        @Override
                        public void run() {
                            chronometer.stop();
                            callDruationText = chronometer.getText().toString();
                            String s1 = "对方拒绝接受！";
                            String s2 = "连接建立失败！";
                            String s3 = "对方不在线，请稍后再拨...";
                            String s4 = "对方正在通话中，请稍后再拨";
                            String s5 = "对方未接听";

                            String s6 = "挂断";
                            String s7 = "对方已经挂断";
                            String s8 = "对方未接听";
                            String s9 = "已取消";
                            String s10 = "已拒绝";
                            String s11 = "通话协议版本不一致";

                            if (fError == EMCallStateChangeListener.CallError.REJECTED) {
                                callingState = CallingState.BEREFUSED;
                                callStateTextView.setText(s1);
                            } else if (fError == EMCallStateChangeListener.CallError.ERROR_TRANSPORT) {
                                callStateTextView.setText(s2);
                            } else if (fError == EMCallStateChangeListener.CallError.ERROR_UNAVAILABLE) {
                                callingState = CallingState.OFFLINE;
                                callStateTextView.setText(s3);
                            } else if (fError == EMCallStateChangeListener.CallError.ERROR_BUSY) {
                                callingState = CallingState.BUSY;
                                callStateTextView.setText(s4);
                            } else if (fError == EMCallStateChangeListener.CallError.ERROR_NORESPONSE) {
                                callingState = CallingState.NO_RESPONSE;
                                callStateTextView.setText(s5);
                            }else if (fError == EMCallStateChangeListener.CallError.ERROR_LOCAL_SDK_VERSION_OUTDATED || fError == EMCallStateChangeListener.CallError.ERROR_REMOTE_SDK_VERSION_OUTDATED){
                                callingState = CallingState.VERSION_NOT_SAME;
                                callStateTextView.setText(s11);
                            } else {
                                if (isRefused) {
                                    callingState = CallingState.REFUSED;
                                    callStateTextView.setText(s10);
                                }
                                else if (isAnswered) {
                                    callingState = CallingState.NORMAL;
                                    if (endCallTriggerByMe) {

                                    } else {
                                        callStateTextView.setText(s7);
                                    }
                                } else {
                                    if (isInComingCall) {
                                        callingState = CallingState.UNANSWERED;
                                        callStateTextView.setText(s8);
                                    } else {
                                        if (callingState != CallingState.NORMAL) {
                                            callingState = CallingState.CANCELLED;
                                            callStateTextView.setText(s9);
                                        } else {
                                            callStateTextView.setText(s6);
                                        }
                                    }
                                }
                            }
                            Toast.makeText(VideoCallActivity.this, callStateTextView.getText(), Toast.LENGTH_SHORT).show();
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
    protected void onDestroy() {
        if(isRecording){
            callHelper.stopVideoRecord();
            isRecording = false;
        }
        localSurface.getRenderer().dispose();
        localSurface = null;
        oppositeSurface.getRenderer().dispose();
        oppositeSurface = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        callDruationText = chronometer.getText().toString();
        super.onBackPressed();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if(isInCalling){
            try {
                EMClient.getInstance().callManager().pauseVideoTransfer();
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isInCalling){
            try {
                EMClient.getInstance().callManager().resumeVideoTransfer();
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
        }
    }
}
