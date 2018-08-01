package com.renyu.nimlibrary.util.audio;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.text.TextUtils;

import com.blankj.utilcode.util.SDCardUtils;
import com.blankj.utilcode.util.Utils;
import com.netease.nimlib.sdk.media.player.AudioPlayer;
import com.netease.nimlib.sdk.media.player.OnPlayListener;
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.renyu.nimlibrary.R;
import com.renyu.nimlibrary.manager.MessageManager;
import com.renyu.nimlibrary.ui.adapter.ConversationAdapter;

import java.io.File;
import java.util.List;

public class MessageAudioControl {
    private volatile static MessageAudioControl mMessageAudioControl = null;

    interface AudioControllerState {
        int stop = 0;
        int ready = 1;
        int playing = 2;
    }

    private AudioControlListener audioControlListener;

    private AudioPlayer currentAudioPlayer;
    private Playable currentPlayable;

    private int state;

    // 结尾声音
    private MediaPlayer mSuffixPlayer = null;

    // 是否需要连播
    private boolean mIsNeedPlayNext = false;

    // adapter参数
    private ConversationAdapter mAdapter;
    // 下一个需要播放的消息
    private IMMessage mItem = null;

    private Handler mHandler = new Handler();
    private Runnable playRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentAudioPlayer == null) {
                return;
            }
            currentAudioPlayer.start(AudioManager.STREAM_MUSIC);
        }
    };

    private MessageAudioControl() {}

    public static MessageAudioControl getInstance() {
        if (mMessageAudioControl == null) {
            synchronized (MessageAudioControl.class) {
                if (mMessageAudioControl == null) {
                    mMessageAudioControl = new MessageAudioControl();
                }
            }
        }
        return mMessageAudioControl;
    }

    public interface AudioControlListener {
        void onAudioControllerReady(Playable playable);

        /**
         * 结束播放
         * @param playable
         */
        void onEndPlay(Playable playable);

        /**
         * 显示播放过程中的进度条
         * @param playable
         * @param curPosition 当前进度，如果传-1则自动获取进度
         */
        void updatePlayingProgress(Playable playable, long curPosition);
    }

    /**
     * 是否正在播放音频
     * @return
     */
    private boolean isPlayingAudio() {
        return currentAudioPlayer != null && (state == AudioControllerState.playing || state == AudioControllerState.ready);
    }

    /**
     * 获取正在播放的音频消息
     * @return
     */
    private IMMessage getPlayingAudio() {
        if (isPlayingAudio() && AudioMessagePlayable.class.isInstance(currentPlayable)) {
            return ((AudioMessagePlayable) currentPlayable).getMessage();
        } else {
            return null;
        }
    }

    /**
     * 判断某一语音消息是否正在播放
     * @param message
     * @return
     */
    public boolean isMessagePlaying(IMMessage message) {
        return getPlayingAudio() != null && getPlayingAudio().isTheSame(message);
    }

    /**
     * 开始播放
     * @param message
     * @param audioControlListener
     */
    public void startPlayAudio(IMMessage message, AudioControlListener audioControlListener) {
        if (SDCardUtils.isSDCardEnableByEnvironment()) {
            if (startAudio(new AudioMessagePlayable(message), audioControlListener)) {
                // 将未读标识去掉,更新数据库
                if (isUnreadAudioMessage(message)) {
                    message.setStatus(MsgStatusEnum.read);
                    MessageManager.INSTANCE.updateIMMessageStatus(message);
                }
            }
        }
    }

    private boolean startAudio(Playable playable, AudioControlListener audioControlListener) {
        String filePath = playable.getPath();
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }

        // 正在播放则停止播放
        if (isPlayingAudio()) {
            stopAudio();
        }
        state = AudioControllerState.stop;

        // 配置相关参数
        currentPlayable = playable;
        currentAudioPlayer = new AudioPlayer(Utils.getApp());
        currentAudioPlayer.setDataSource(filePath);
        setOnPlayListener(currentPlayable, audioControlListener);

        // 延迟播放
        mHandler.postDelayed(playRunnable, 500);
        state = AudioControllerState.ready;
        // 回调到adapter
        if (audioControlListener != null) {
            audioControlListener.onAudioControllerReady(currentPlayable);
        }

        return true;
    }

    /**
     * 判断是否为未读音频
     * @param message
     * @return
     */
    private boolean isUnreadAudioMessage(IMMessage message) {
        return (message.getMsgType() == MsgTypeEnum.audio)
                && message.getDirect() == MsgDirectionEnum.In
                && message.getAttachStatus() == AttachStatusEnum.transferred
                && message.getStatus() != MsgStatusEnum.read;
    }

    public void stopAudio() {
        // 如果正在播放，则停止播放
        if (state == AudioControllerState.playing) {
            currentAudioPlayer.stop();
        } else if (state == AudioControllerState.ready) {
            // 停止播放准备
            mHandler.removeCallbacks(playRunnable);
            resetAudioController();

            if (audioControlListener != null) {
                audioControlListener.onEndPlay(currentPlayable);
            }
        }
    }

    private void setOnPlayListener(Playable playingPlayable, AudioControlListener audioControlListener) {
        this.audioControlListener = audioControlListener;

        BasePlayerListener basePlayerListener = new BasePlayerListener(currentAudioPlayer, playingPlayable) {

            @Override
            public void onInterrupt() {
                super.onInterrupt();
                cancelPlayNext();
            }

            @Override
            public void onError(String error) {
                super.onError(error);
                cancelPlayNext();
            }

            @Override
            public void onCompletion() {
                if (checkAudioPlayerValid()) {
                    return;
                }

                // 重置AudioPlayer
                resetAudioController();

                boolean isLoop = false;
                // 如果开启了自动播放下一个语音的功能，则向下查找
                if (mIsNeedPlayNext) {
                    if (mAdapter != null && mItem != null) {
                        isLoop = playNextAudio(mAdapter, mItem);
                    }
                }
                // 如果已经播放到最后一条，则播放结束
                if (!isLoop) {
                    if (audioControlListener != null) {
                        audioControlListener.onEndPlay(currentPlayable);
                    }
                    playSuffix();
                }
            }
        };

        basePlayerListener.setAudioControlListener(audioControlListener);
        currentAudioPlayer.setOnPlayListener(basePlayerListener);
    }

    public class BasePlayerListener implements OnPlayListener {
        AudioPlayer listenerPlayingAudioPlayer;
        Playable listenerPlayingPlayable;
        AudioControlListener audioControlListener;

        BasePlayerListener(AudioPlayer playingAudioPlayer, Playable playingPlayable) {
            listenerPlayingAudioPlayer = playingAudioPlayer;
            listenerPlayingPlayable = playingPlayable;
        }

        void setAudioControlListener(AudioControlListener audioControlListener) {
            this.audioControlListener = audioControlListener;
        }

        boolean checkAudioPlayerValid() {
            return currentAudioPlayer != listenerPlayingAudioPlayer;
        }

        @Override
        public void onPrepared() {
            if (checkAudioPlayerValid()) {
                return;
            }
            state = AudioControllerState.playing;
        }

        @Override
        public void onPlaying(long curPosition) {
            if (checkAudioPlayerValid()) {
                return;
            }
            if (audioControlListener != null) {
                audioControlListener.updatePlayingProgress(listenerPlayingPlayable, curPosition);
            }
        }

        @Override
        public void onInterrupt() {
            if (checkAudioPlayerValid()) {
                return;
            }
            resetAudioController();
            if (audioControlListener != null) {
                audioControlListener.onEndPlay(currentPlayable);
            }
        }

        @Override
        public void onError(String error) {
            if (checkAudioPlayerValid()) {
                return;
            }
            resetAudioController();
            if (audioControlListener != null) {
                audioControlListener.onEndPlay(currentPlayable);
            }
        }

        @Override
        public void onCompletion() {

        }
    }

    /**
     * 重置AudioPlayer
     */
    private void resetAudioController() {
        currentAudioPlayer.setOnPlayListener(null);
        currentAudioPlayer = null;

        state = AudioControllerState.stop;
    }

    /**
     * 播放结尾声音
     */
    private void playSuffix() {
        mSuffixPlayer = MediaPlayer.create(Utils.getApp(), R.raw.audio_end_tip);
        mSuffixPlayer.setLooping(false);
        mSuffixPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mSuffixPlayer.setOnCompletionListener(mp -> {
            mSuffixPlayer.release();
            mSuffixPlayer = null;
        });
        mSuffixPlayer.start();
    }

    /**
     * 播放取消，数据重置
     */
    private void cancelPlayNext() {
        setPlayNext(false, null, null);
    }

    public void setPlayNext(boolean isPlayNext, ConversationAdapter adapter, IMMessage item) {
        mIsNeedPlayNext = isPlayNext;
        mAdapter = adapter;
        mItem = item;
    }

    private boolean playNextAudio(ConversationAdapter tAdapter, IMMessage messageItem) {
        // 获取全部聊天数据
        List<IMMessage> list = tAdapter.getMessages();
        int index = 0;
        int nextIndex = -1;
        // 找到当前已经播放的
        for (int i = 0; i < list.size(); ++i) {
            IMMessage item = list.get(i);
            if (item.equals(messageItem)) {
                index = i;
                break;
            }
        }
        // 找到下一个将要播放的
        for (int i = index; i < list.size(); ++i) {
            IMMessage message = list.get(i);
            if (isUnreadAudioMessage(message)) {
                nextIndex = i;
                break;
            }
        }
        // 找不到则直接播放结束
        if (nextIndex == -1) {
            cancelPlayNext();
            return false;
        }

        // 播放并刷新界面
        IMMessage message = list.get(nextIndex);
        AudioAttachment attach = (AudioAttachment) message.getAttachment();
        if (mMessageAudioControl != null && attach != null) {
            if (message.getAttachStatus() != AttachStatusEnum.transferred) {
                cancelPlayNext();
                return false;
            }
            File file = new File(attach.getPathForSave());
            if (!file.exists()) {
                return false;
            }
            // 将未读标识去掉,更新数据库
            if (message.getStatus() != MsgStatusEnum.read) {
                message.setStatus(MsgStatusEnum.read);
                MessageManager.INSTANCE.updateIMMessageStatus(message);
            }
            mMessageAudioControl.startPlayAudio(message, null);
            mItem = list.get(nextIndex);
            tAdapter.notifyDataSetChanged();
            return true;
        }
        return false;
    }

    /**
     * 切换AudioControlListener
     * @param audioControlListener
     */
    public void changeAudioControlListener(AudioControlListener audioControlListener) {
        this.audioControlListener = audioControlListener;

        if (isPlayingAudio()) {
            OnPlayListener onPlayListener = currentAudioPlayer.getOnPlayListener();
            if (onPlayListener != null) {
                ((BasePlayerListener) onPlayListener).setAudioControlListener(audioControlListener);
            }
        }
    }

    public AudioControlListener getAudioControlListener() {
        return audioControlListener;
    }
}
