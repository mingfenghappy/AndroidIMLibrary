package com.renyu.mt.utils;

import android.media.MediaRecorder;

import com.renyu.commonlibrary.params.InitParams;

import java.io.File;

/**
 * Created by Administrator on 2017/7/26.
 */

public class RecordUtils {

    // 录音容许最长时间，超过该时间则结束录音 ,默认时间是60秒
    private int defalutMaxTime = 1000 * 60;
    public RecorderState state = RecorderState.stop;
    // 临时音频文件
    File tempAudioFile;
    // 开始录音时间
    long start_time = 0;
    // 结束录音时间
    long end_time = 0;
    RecorderListener recorderListener;
    MediaRecorder mediaRecorder;
    // 录音时间是否过短
    boolean isShortly = false;
    // 预防用户快速点击，造成record未start就stop，从而抛出异常。
    boolean isQuickClick = false;

    // 开始录音
    public void start() {
        new Thread() {
            public void run() {
                startRecorder();
            }
        }.start();
    }

    // 定义录音状态
    public enum RecorderState {
        before_recording, after_recording, stop
    }

    // 录音完成回调接口
    public interface RecorderListener {
        void finishRecorder(String path);
    }

    public void setRecorderListener(RecorderListener recorderListener) {
        this.recorderListener = recorderListener;
    }

    /**
     * 开始录音
     */
    private void startRecorder() {
        try {
            if (state != RecorderState.before_recording && state != RecorderState.after_recording) {
                state = RecorderState.before_recording;
                tempAudioFile = new File(InitParams.FILE_PATH+"/"+System.currentTimeMillis()+".amr");
                tempAudioFile.createNewFile();
                mediaRecorder = new MediaRecorder();
                // 设置音源从麦克风接入
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                // 设置输出格式
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mediaRecorder.setOutputFile(tempAudioFile.getAbsolutePath());
                mediaRecorder.setMaxDuration(defalutMaxTime);
                mediaRecorder.setOnErrorListener((mr, what, extra) -> {
                    // 发生错误，停止录制
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    mediaRecorder = null;
                });
                mediaRecorder.setOnInfoListener((mr, what, extra) -> {
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                        recorderListener.finishRecorder(tempAudioFile.getAbsolutePath());
                        state = RecorderState.stop;
                    }
                });
                mediaRecorder.prepare();
                mediaRecorder.start();
                start_time = System.currentTimeMillis();
                state = RecorderState.after_recording;
                // 快速点击的话直接在此进行关闭
                if (isQuickClick) {
                    stopRecorder(false);
                }
            }
        }
        catch (Exception e) {
            state = RecorderState.stop;
            e.printStackTrace();
        }
    }

    /**
     * 停止录音
     * @param b true：直接stop，不发送message。
     */
    public void stopRecorder(boolean b) {
        if (b) {
            try {
                state = RecorderState.stop;
                isQuickClick = false;
                isShortly = false;
                mediaRecorder.stop();
                mediaRecorder.release();
                return;
            }
            catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        if (state == RecorderState.after_recording && mediaRecorder != null) {
            end_time = System.currentTimeMillis();
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            state = RecorderState.stop;
            mediaRecorder = null;
            if (end_time - start_time < 1000) {
                deleteFile();
                isQuickClick = false;
                isShortly = true;
                return;
            }
            else {
                isShortly = false;
            }
            if (recorderListener != null) {
                recorderListener.finishRecorder(tempAudioFile.getAbsolutePath());
            }
            isQuickClick = false;
        }
        else if (state == RecorderState.before_recording) {
            isQuickClick = true;
        }
    }

    /**
     * 当时间过短时删除文件
     */
    private void deleteFile() {
        if (tempAudioFile.exists()) {
            tempAudioFile.delete();
        }
    }
}
