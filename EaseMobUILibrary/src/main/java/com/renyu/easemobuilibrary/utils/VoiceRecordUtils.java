package com.renyu.easemobuilibrary.utils;

import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import com.renyu.commonlibrary.params.InitParams;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class VoiceRecordUtils {

    MediaRecorder recorder;

    private boolean isRecording = false;
    private long startTime;
    private String voiceFilePath = null;
    private File file;
    private Handler handler;

    public VoiceRecordUtils(Handler handler) {
        this.handler = handler;
    }

    public String startRecording() {
        file = null;
        try {
            if (recorder != null) {
                recorder.release();
                recorder = null;
            }
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setAudioChannels(1);
            recorder.setAudioSamplingRate(8000);
            recorder.setAudioEncodingBitRate(64);
            voiceFilePath = InitParams.FILE_PATH+"/"+System.currentTimeMillis()+".amr";
            file = new File(voiceFilePath);
            recorder.setOutputFile(file.getAbsolutePath());
            recorder.prepare();
            isRecording = true;
            recorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(() -> {
            try {
                while (isRecording) {
                    Message msg = new Message();
                    msg.what = recorder.getMaxAmplitude() * 13 / 0x7FFF;
                    handler.sendMessage(msg);
                    SystemClock.sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        startTime = new Date().getTime();
        return file == null ? null : file.getAbsolutePath();
    }

    public void discardRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
                recorder.release();
                recorder = null;
                if (file != null && file.exists() && !file.isDirectory()) {
                    file.delete();
                }
            } catch (IllegalStateException e) {

            } catch (RuntimeException e) {

            }
            isRecording = false;
        }
    }

    public int stopRecoding() {
        if(recorder != null){
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            
            if(file == null || !file.exists() || !file.isFile()){
                return -1;
            }
            if (file.length() == 0) {
                file.delete();
                return -1;
            }
            int seconds = (int) (new Date().getTime() - startTime) / 1000;
            return seconds;
        }
        return 0;
    }

    protected void finalize() throws Throwable {
        super.finalize();
        if (recorder != null) {
            recorder.release();
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    public String getVoiceFilePath() {
        return voiceFilePath;
    }
}
