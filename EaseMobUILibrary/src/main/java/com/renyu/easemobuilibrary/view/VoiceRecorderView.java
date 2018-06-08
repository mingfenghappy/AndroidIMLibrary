package com.renyu.easemobuilibrary.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.PowerManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.Utils;
import com.renyu.easemobuilibrary.R;
import com.renyu.easemobuilibrary.utils.VoiceRecordUtils;

/**
 * Voice recorder view
 *
 */
public class VoiceRecorderView extends RelativeLayout {

    protected Drawable[] micImages;
    protected VoiceRecordUtils voiceRecorder;

    protected PowerManager.WakeLock wakeLock;
    protected ImageView micImage;
    protected TextView recordingHint;

    protected Handler micImageHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            micImage.setImageDrawable(micImages[msg.what]);
        }
    };

    public VoiceRecorderView(Context context) {
        super(context);
        init(context);
    }

    public VoiceRecorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VoiceRecorderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.ease_widget_voice_recorder, this);

        micImage = findViewById(R.id.mic_image);
        recordingHint = findViewById(R.id.recording_hint);

        voiceRecorder = new VoiceRecordUtils(micImageHandler);

        micImages = new Drawable[] { getResources().getDrawable(R.mipmap.ease_record_animate_01),
                getResources().getDrawable(R.mipmap.ease_record_animate_02),
                getResources().getDrawable(R.mipmap.ease_record_animate_03),
                getResources().getDrawable(R.mipmap.ease_record_animate_04),
                getResources().getDrawable(R.mipmap.ease_record_animate_05),
                getResources().getDrawable(R.mipmap.ease_record_animate_06),
                getResources().getDrawable(R.mipmap.ease_record_animate_07),
                getResources().getDrawable(R.mipmap.ease_record_animate_08),
                getResources().getDrawable(R.mipmap.ease_record_animate_09),
                getResources().getDrawable(R.mipmap.ease_record_animate_10),
                getResources().getDrawable(R.mipmap.ease_record_animate_11),
                getResources().getDrawable(R.mipmap.ease_record_animate_12),
                getResources().getDrawable(R.mipmap.ease_record_animate_13),
                getResources().getDrawable(R.mipmap.ease_record_animate_14), };

        wakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK, "demo");
    }

    /**
     * 长按开始录音
     * @param v
     * @param event
     * @param recorderCallback
     * @return
     */
    public boolean onPressToSpeakBtnTouch(View v, MotionEvent event, EaseVoiceRecorderCallback recorderCallback) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            try {
                v.setPressed(true);
                setVisibility(VISIBLE);
                startRecording();
            } catch (Exception e) {
                v.setPressed(false);
            }
            return true;
        case MotionEvent.ACTION_MOVE:
            if (event.getY() < 0) {
                showReleaseToCancelHint();
            } else {
                showMoveUpToCancelHint();
            }
            return true;
        case MotionEvent.ACTION_UP:
            v.setPressed(false);
            setVisibility(GONE);
            if (event.getY() < 0) {
                discardRecording();
            } else {
                try {
                    int length = stopRecoding();
                    if (length > 0) {
                        if (recorderCallback != null) {
                            recorderCallback.onVoiceRecordComplete(getVoiceFilePath(), length);
                        }
                    } else if (length == -1) {
                        Toast.makeText(Utils.getApp(), "无录音权限", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Utils.getApp(), "录音时间太短", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(Utils.getApp(), "无录音权限", Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        default:
            discardRecording();
            return false;
        }
    }

    public interface EaseVoiceRecorderCallback {
        /**
         * 录音完成接口回调
         * @param voiceFilePath 录音完毕后的文件路径
         * @param voiceTimeLength 录音时长
         */
        void onVoiceRecordComplete(String voiceFilePath, int voiceTimeLength);
    }

    public void startRecording() {
        try {
            wakeLock.acquire();
            this.setVisibility(View.VISIBLE);
            recordingHint.setText("手指上滑，取消发送");
            recordingHint.setBackgroundColor(Color.TRANSPARENT);
            voiceRecorder.startRecording();
        } catch (Exception e) {
            e.printStackTrace();
            if (wakeLock.isHeld())
                wakeLock.release();
            if (voiceRecorder != null)
                voiceRecorder.discardRecording();
            this.setVisibility(View.INVISIBLE);
            Toast.makeText(Utils.getApp(), "录音失败，请重试！", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public void showReleaseToCancelHint() {
        recordingHint.setText("松开手指，取消发送");
        recordingHint.setBackgroundResource(R.drawable.ease_recording_text_hint_bg);
    }

    public void showMoveUpToCancelHint() {
        recordingHint.setText("手指上滑，取消发送");
        recordingHint.setBackgroundColor(Color.TRANSPARENT);
    }

    public void discardRecording() {
        if (wakeLock.isHeld())
            wakeLock.release();
        try {
            // stop recording
            if (voiceRecorder.isRecording()) {
                voiceRecorder.discardRecording();
                this.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
        }
    }

    public int stopRecoding() {
        this.setVisibility(View.INVISIBLE);
        if (wakeLock.isHeld())
            wakeLock.release();
        return voiceRecorder.stopRecoding();
    }

    public String getVoiceFilePath() {
        return voiceRecorder.getVoiceFilePath();
    }

    public boolean isRecording() {
        return voiceRecorder.isRecording();
    }
}
