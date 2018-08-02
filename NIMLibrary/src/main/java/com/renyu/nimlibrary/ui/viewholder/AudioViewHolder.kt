package com.renyu.nimlibrary.ui.viewholder

import android.databinding.ViewDataBinding
import android.graphics.drawable.AnimationDrawable
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import com.netease.nimlib.sdk.RequestCallbackWrapper
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.renyu.nimlibrary.R
import com.renyu.nimlibrary.manager.MessageManager
import com.renyu.nimlibrary.ui.adapter.ConversationAdapter
import com.renyu.nimlibrary.util.audio.MessageAudioControl
import com.renyu.nimlibrary.util.audio.Playable
import java.io.File


class AudioViewHolder(val viewDataBinding: ViewDataBinding, val adapter: ConversationAdapter): RecyclerView.ViewHolder(viewDataBinding.root) {
    val audioDataBinding = viewDataBinding

    // 语音动画
    private var voiceAnimation: AnimationDrawable? = null

    var imMessage: IMMessage? = null
    var audioControl: MessageAudioControl? = null

    val onPlayListener: MessageAudioControl.AudioControlListener = object : MessageAudioControl.AudioControlListener {
        override fun onAudioControllerReady(playable: Playable?) {
            if (!isTheSame(imMessage!!)) {
                return
            }
            startVoicePlayAnimation()
            val unReadDot = viewDataBinding.root.findViewById<ImageView>(R.id.aurora_iv_msgitem_read_status)
            if (unReadDot != null) {
                unReadDot.visibility = View.GONE
            }
        }

        override fun onEndPlay(playable: Playable?) {
            if (!isTheSame(imMessage!!)) {
                return
            }
            stopVoicePlayAnimation()
        }

        override fun updatePlayingProgress(playable: Playable?, curPosition: Long) {
            if (!isTheSame(imMessage!!)) {
                return
            }
        }
    }

    fun changeUI() {
        // 判断是否正在播放
        if (audioControl!!.isMessagePlaying(imMessage)) {
            // 切换AudioControlListener
            audioControl!!.changeAudioControlListener(onPlayListener)
            // 开启动画
            startVoicePlayAnimation()
        } else {
            if (audioControl!!.audioControlListener != null && audioControl!!.audioControlListener == onPlayListener) {
                audioControl!!.changeAudioControlListener(null)
            }
            // 复原图片
            if (imMessage!!.direct == MsgDirectionEnum.In) {
                audioDataBinding.root.findViewById<ImageView>(R.id.iv_voice).setImageResource(R.mipmap.ease_chatfrom_voice_playing)
            }
            else if (imMessage!!.direct == MsgDirectionEnum.Out) {
                audioDataBinding.root.findViewById<ImageView>(R.id.iv_voice).setImageResource(R.mipmap.ease_chatto_voice_playing)
            }
        }
        audioDataBinding.root.findViewById<RelativeLayout>(R.id.bubble).setOnClickListener {
            onItemClick()
        }
    }

    /**
     * 点击播放
     */
    private fun onItemClick() {
        if (audioControl != null) {
            // 如果收到的消息没有下载完成
            if (imMessage!!.direct == MsgDirectionEnum.In && imMessage!!.attachStatus != AttachStatusEnum.transferred) {
                // 如果状态是下载失败或者是未下载就去下载
                if (imMessage!!.attachStatus == AttachStatusEnum.fail || imMessage!!.attachStatus == AttachStatusEnum.def) {
                    MessageManager.downloadAttachment(imMessage!!, null)
                }
                return
            }
            // 检查附件是否存在
            val audioAttachment = imMessage!!.attachment as AudioAttachment
            val file = File(audioAttachment.pathForSave)
            if (!file.exists()) {
                MessageManager.downloadAttachment(imMessage!!, object : RequestCallbackWrapper<Void>() {
                    override fun onResult(code: Int, result: Void?, exception: Throwable?) {
                        if (code == 200) {
                            // 开始播放
                            audioControl!!.setPlayNext(true, adapter, imMessage)
                            audioControl!!.startPlayAudio(imMessage!!, onPlayListener)
                        }
                    }
                })
            }
            // 开始播放
            audioControl!!.setPlayNext(true, adapter, imMessage)
            audioControl!!.startPlayAudio(imMessage!!, onPlayListener)
        }
    }

    /**
     * 播放语音UI
     */
    private fun startVoicePlayAnimation() {
        val imageView = audioDataBinding.root.findViewById<ImageView>(R.id.iv_voice)
        if (imMessage!!.direct == MsgDirectionEnum.In) {
            imageView.setImageResource(R.drawable.voice_from_icon)
        } else {
            imageView.setImageResource(R.drawable.voice_to_icon)
        }
        voiceAnimation = imageView.drawable as AnimationDrawable
        voiceAnimation!!.start()
    }

    /**
     * 停止播放语音UI
     */
    private fun stopVoicePlayAnimation() {
        val imageView = audioDataBinding.root.findViewById<ImageView>(R.id.iv_voice)
        voiceAnimation?.stop()
        if (imMessage!!.direct == MsgDirectionEnum.In) {
            imageView.setImageResource(R.mipmap.ease_chatfrom_voice_playing)
        } else {
            imageView.setImageResource(R.mipmap.ease_chatto_voice_playing)
        }
    }

    /**
     * 判断当前消息与回调的消息是不是相同的
     */
    fun isTheSame(imMessage: IMMessage): Boolean {
        val tag = audioDataBinding.root.findViewById<ImageView>(R.id.iv_voice).tag
        return tag != null && tag.toString() == "iv_anim_"+imMessage.uuid
    }
}