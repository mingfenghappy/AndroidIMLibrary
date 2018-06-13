package com.renyu.easemobuilibrary.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.Utils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMLocationMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.EMVideoMessageBody;
import com.hyphenate.chat.EMVoiceMessageBody;
import com.renyu.easemobuilibrary.manager.EMMessageManager;
import com.renyu.easemobuilibrary.R;
import com.renyu.easemobuilibrary.activity.BaseConversationActivity;
import com.renyu.easemobuilibrary.params.CommonParams;
import com.renyu.easemobuilibrary.utils.FaceIconUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ConversationAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<EMMessage> messages;
    boolean isGroup;

    // 当前正在播放的音频文件tag
    public String mediaPlayerTag=null;

    public ConversationAdapter(Context context, ArrayList<EMMessage> messages, boolean isGroup) {
        this.context = context;
        this.messages = messages;
        this.isGroup = isGroup;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==0) {
            View view= LayoutInflater.from(context).inflate(R.layout.adapter_receive_text, parent, false);
            return new ReceiverTextViewHolder(view);
        }
        else if (viewType==1) {
            View view= LayoutInflater.from(context).inflate(R.layout.adapter_send_text, parent, false);
            return new SendTextViewHolder(view);
        }
        else if (viewType==2) {
            View view= LayoutInflater.from(context).inflate(R.layout.adapter_receive_photo, parent, false);
            return new ReceiverImageViewHolder(view);
        }
        else if (viewType==3) {
            View view= LayoutInflater.from(context).inflate(R.layout.adapter_send_photo, parent, false);
            return new SendImageViewHolder(view);
        }
        else if (viewType==4) {
            View view= LayoutInflater.from(context).inflate(R.layout.adapter_receive_voice, parent, false);
            return new ReceiverVoiceViewHolder(view);
        }
        else if (viewType==5) {
            View view= LayoutInflater.from(context).inflate(R.layout.adapter_send_voice, parent, false);
            return new SendVoiceViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // 判断是否显示日期
        if (position==0) {
            if (getItemViewType(position)==0) {
                ((ReceiverTextViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.VISIBLE);
                ((ReceiverTextViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getMsgTime(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
            }
            else if (getItemViewType(position)==1) {
                ((SendTextViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.VISIBLE);
                ((SendTextViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getMsgTime(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
            }
            else if (getItemViewType(position)==2) {
                ((ReceiverImageViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.VISIBLE);
                ((ReceiverImageViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getMsgTime(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
            }
            else if (getItemViewType(position)==3) {
                ((SendImageViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.VISIBLE);
                ((SendImageViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getMsgTime(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
            }
            else if (getItemViewType(position)==4) {
                ((ReceiverVoiceViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.VISIBLE);
                ((ReceiverVoiceViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getMsgTime(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
            }
            else if (getItemViewType(position)==5) {
                ((SendVoiceViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.VISIBLE);
                ((SendVoiceViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getMsgTime(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
            }
        }
        else {
            if (messages.get(position).getMsgTime()-messages.get(position-1).getMsgTime()>1000*60) {
                if (getItemViewType(position)==0) {
                    ((ReceiverTextViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.VISIBLE);
                    ((ReceiverTextViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getMsgTime(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
                }
                else if (getItemViewType(position)==1) {
                    ((SendTextViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.VISIBLE);
                    ((SendTextViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getMsgTime(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
                }
                else if (getItemViewType(position)==2) {
                    ((ReceiverImageViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.VISIBLE);
                    ((ReceiverImageViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getMsgTime(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
                }
                else if (getItemViewType(position)==3) {
                    ((SendImageViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.VISIBLE);
                    ((SendImageViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getMsgTime(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
                }
                else if (getItemViewType(position)==4) {
                    ((ReceiverVoiceViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.VISIBLE);
                    ((ReceiverVoiceViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getMsgTime(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
                }
                else if (getItemViewType(position)==5) {
                    ((SendVoiceViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.VISIBLE);
                    ((SendVoiceViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getMsgTime(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
                }
            }
            else {
                if (getItemViewType(position)==0) {
                    ((ReceiverTextViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.GONE);
                }
                else if (getItemViewType(position)==1) {
                    ((SendTextViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.GONE);
                }
                else if (getItemViewType(position)==2) {
                    ((ReceiverImageViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.GONE);
                }
                else if (getItemViewType(position)==3) {
                    ((SendImageViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.GONE);
                }
                else if (getItemViewType(position)==4) {
                    ((ReceiverVoiceViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.GONE);
                }
                else if (getItemViewType(position)==5) {
                    ((SendVoiceViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.GONE);
                }
            }
        }
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse("res:///"+R.drawable.default_grey_avatar))
                .setResizeOptions(new ResizeOptions(SizeUtils.dp2px(40), SizeUtils.dp2px(40))).build();
        DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request).setAutoPlayAnimations(true).build();
        if (getItemViewType(position)==0) {
            ((ReceiverTextViewHolder) holder).aurora_tv_msgitem_date.setText(getFriendlyTimeSpanByNow(messages.get(position).getMsgTime()));
            ((ReceiverTextViewHolder) holder).aurora_iv_msgitem_avatar.setController(draweeController);
            ((ReceiverTextViewHolder) holder).aurora_tv_msgitem_message.setText(FaceIconUtil.getInstance().replaceFaceMsg(((EMTextMessageBody) messages.get(position).getBody()).getMessage()));
            ((ReceiverTextViewHolder) holder).aurora_tv_msgitem_display_name.setText(messages.get(position).getFrom());
            // 如果是群组则显示用户昵称或者userId
            if (isGroup) {
                ((ReceiverTextViewHolder) holder).aurora_tv_msgitem_display_name.setVisibility(View.VISIBLE);
            }
            else {
                ((ReceiverTextViewHolder) holder).aurora_tv_msgitem_display_name.setVisibility(View.GONE);
            }
        }
        else if (getItemViewType(position)==1) {
            ((SendTextViewHolder) holder).aurora_tv_msgitem_date.setText(getFriendlyTimeSpanByNow(messages.get(position).getMsgTime()));
            ((SendTextViewHolder) holder).aurora_iv_msgitem_avatar.setController(draweeController);
            ((SendTextViewHolder) holder).aurora_tv_msgitem_message.setText(FaceIconUtil.getInstance().replaceFaceMsg(((EMTextMessageBody) messages.get(position).getBody()).getMessage()));
            ((SendTextViewHolder) holder).aurora_iv_msgitem_send_status.setTag(messages.get(position).localTime()+"_status");
            ((SendTextViewHolder) holder).aurora_iv_msgitem_send_progress_bar.setTag(messages.get(position).localTime()+"_pb");

            // 发送失败显示图标
            if (messages.get(position).status() == EMMessage.Status.FAIL) {
                ((SendTextViewHolder) holder).aurora_iv_msgitem_send_status.setVisibility(View.VISIBLE);
            }
            else {
                ((SendTextViewHolder) holder).aurora_iv_msgitem_send_status.setVisibility(View.GONE);
            }

            // 未发送完成则需要显示进度圈
            if (messages.get(position).status() == EMMessage.Status.INPROGRESS ||
                    messages.get(position).status() == EMMessage.Status.CREATE) {
                ((SendTextViewHolder) holder).aurora_iv_msgitem_send_progress_bar.setVisibility(View.VISIBLE);
            }
            else {
                ((SendTextViewHolder) holder).aurora_iv_msgitem_send_progress_bar.setVisibility(View.GONE);
            }

            ((SendTextViewHolder) holder).aurora_iv_msgitem_send_status.setOnClickListener(view -> {
                // 发送失败则通过点击进行重新发送
                EMMessage temp = messages.get(position);
                temp.setStatus(EMMessage.Status.INPROGRESS);
                EMMessageManager.sendSingleMessage(Utils.getApp(), temp);
                notifyItemChanged(position);
            });
        }
        else if (getItemViewType(position)==2) {
            ((ReceiverImageViewHolder) holder).aurora_tv_msgitem_date.setText(getFriendlyTimeSpanByNow(messages.get(position).getMsgTime()));
            ((ReceiverImageViewHolder) holder).aurora_iv_msgitem_avatar.setController(draweeController);
            ((ReceiverImageViewHolder) holder).aurora_tv_msgitem_display_name.setText(messages.get(position).getFrom());
            // 如果是群组则显示用户昵称或者userId
            if (isGroup) {
                ((ReceiverImageViewHolder) holder).aurora_tv_msgitem_display_name.setVisibility(View.VISIBLE);
            }
            else {
                ((ReceiverImageViewHolder) holder).aurora_tv_msgitem_display_name.setVisibility(View.GONE);
            }
            // 加载图片
            ImageRequest request_ = ImageRequestBuilder.newBuilderWithSource(Uri.parse(((EMImageMessageBody) messages.get(position).getBody()).getRemoteUrl()))
                    .setResizeOptions(new ResizeOptions(SizeUtils.dp2px(100), SizeUtils.dp2px(100))).build();
            File file = new File(((EMImageMessageBody) messages.get(position).getBody()).getLocalUrl());
            if (file.exists()) {
                request_ = ImageRequestBuilder.newBuilderWithSource(Uri.parse("file:///"+file.getPath()))
                        .setResizeOptions(new ResizeOptions(SizeUtils.dp2px(100), SizeUtils.dp2px(100))).build();
            }
            DraweeController draweeController_ = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request_).setAutoPlayAnimations(true).build();
            ((ReceiverImageViewHolder) holder).aurora_iv_msgitem_photo.setController(draweeController_);
        }
        else if (getItemViewType(position)==3) {
            ((SendImageViewHolder) holder).aurora_tv_msgitem_date.setText(getFriendlyTimeSpanByNow(messages.get(position).getMsgTime()));
            ((SendImageViewHolder) holder).aurora_iv_msgitem_avatar.setController(draweeController);
            ((SendImageViewHolder) holder).aurora_iv_msgitem_send_status.setTag(messages.get(position).localTime()+"_status");

            // 发送失败显示图标
            if (messages.get(position).status() == EMMessage.Status.FAIL) {
                ((SendImageViewHolder) holder).aurora_iv_msgitem_send_status.setVisibility(View.VISIBLE);
            }
            else {
                ((SendImageViewHolder) holder).aurora_iv_msgitem_send_status.setVisibility(View.GONE);
            }
            ((SendImageViewHolder) holder).aurora_iv_msgitem_send_status.setOnClickListener(view -> {
                // 发送失败则通过点击进行重新发送
                EMMessage temp = messages.get(position);
                temp.setStatus(EMMessage.Status.INPROGRESS);
                EMMessageManager.sendSingleMessage(Utils.getApp(), temp);
                notifyItemChanged(position);
            });

            ((SendImageViewHolder) holder).aurora_iv_msgitem_send_progress_bar.setTag(messages.get(position).localTime()+"_pb");
            // 未发送完成则需要显示进度圈
            if (messages.get(position).status() == EMMessage.Status.INPROGRESS ||
                    messages.get(position).status() == EMMessage.Status.CREATE) {
                ((SendImageViewHolder) holder).aurora_iv_msgitem_send_progress_bar.setVisibility(View.VISIBLE);
            }
            else {
                ((SendImageViewHolder) holder).aurora_iv_msgitem_send_progress_bar.setVisibility(View.GONE);
            }

            // 加载图片
            ImageRequest request_ = ImageRequestBuilder.newBuilderWithSource(Uri.parse(((EMImageMessageBody) messages.get(position).getBody()).getRemoteUrl()))
                    .setResizeOptions(new ResizeOptions(SizeUtils.dp2px(100), SizeUtils.dp2px(100))).build();
            File file = new File(((EMImageMessageBody) messages.get(position).getBody()).getLocalUrl());
            if (file.exists()) {
                request_ = ImageRequestBuilder.newBuilderWithSource(Uri.parse("file:///"+file.getPath()))
                        .setResizeOptions(new ResizeOptions(SizeUtils.dp2px(100), SizeUtils.dp2px(100))).build();
            }
            DraweeController draweeController_ = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request_).setAutoPlayAnimations(true).build();
            ((SendImageViewHolder) holder).aurora_iv_msgitem_photo.setController(draweeController_);
            ((SendImageViewHolder) holder).bubble.setOnClickListener(view -> {

            });
        }
        else if (getItemViewType(position)==4) {
            ((ReceiverVoiceViewHolder) holder).aurora_tv_msgitem_date.setText(getFriendlyTimeSpanByNow(messages.get(position).getMsgTime()));
            ((ReceiverVoiceViewHolder) holder).aurora_iv_msgitem_avatar.setController(draweeController);
            ((ReceiverVoiceViewHolder) holder).aurora_tv_msgitem_display_name.setText(messages.get(position).getFrom());
            // 如果是群组则显示用户昵称或者userId
            if (isGroup) {
                ((ReceiverVoiceViewHolder) holder).aurora_tv_msgitem_display_name.setVisibility(View.VISIBLE);
            }
            else {
                ((ReceiverVoiceViewHolder) holder).aurora_tv_msgitem_display_name.setVisibility(View.GONE);
            }

            ((ReceiverVoiceViewHolder) holder).bubble.setOnClickListener(view -> {
                // 播放语音和动画
                ((BaseConversationActivity) context).playMedia(
                        ((EMVoiceMessageBody) messages.get(position).getBody()).getLocalUrl(),
                        messages.get(position).localTime()+"_voice",
                        false);
                // 更新数据库
                EMMessageManager.setVoiceMessageListened(messages.get(position));
                // 更新本地缓存
                messages.get(position).setListened(true);
                // 刷新视图
                ((ReceiverVoiceViewHolder) holder).aurora_iv_msgitem_read_status.setVisibility(View.GONE);
            });

            ((ReceiverVoiceViewHolder) holder).iv_voice.setTag(messages.get(position).localTime()+"_voice");
            // 判断是否正在播放
            if (mediaPlayerTag!=null && mediaPlayerTag.equals(messages.get(position).localTime()+"_voice")) {
                ((BaseConversationActivity) context).startVoicePlayAnimation(((ReceiverVoiceViewHolder) holder).iv_voice, false);
            }
            else {
                ((ReceiverVoiceViewHolder) holder).iv_voice.setImageResource(R.mipmap.ease_chatfrom_voice_playing);
            }

            // 判断语音是否已读
            if (messages.get(position).isListened()) {
                ((ReceiverVoiceViewHolder) holder).aurora_iv_msgitem_read_status.setVisibility(View.GONE);
            }
            else {
                ((ReceiverVoiceViewHolder) holder).aurora_iv_msgitem_read_status.setVisibility(View.VISIBLE);
            }

            // 音频播放时间秒数
            File file = new File(((EMVoiceMessageBody) messages.get(position).getBody()).getLocalUrl());
            if (file == null) {
                ((ReceiverVoiceViewHolder) holder).aurora_tv_voice_length.setText(0+"\'\'");
            }
            else {
                if (file.length()/1000 < 1) {
                    ((ReceiverVoiceViewHolder) holder).aurora_tv_voice_length.setText(1+"\'\'");
                }
                else {
                    ((ReceiverVoiceViewHolder) holder).aurora_tv_voice_length.setText(file.length()/1000+"\'\'");
                }
            }
        }
        else if (getItemViewType(position)==5) {
            ((SendVoiceViewHolder) holder).aurora_tv_msgitem_date.setText(getFriendlyTimeSpanByNow(messages.get(position).getMsgTime()));
            ((SendVoiceViewHolder) holder).aurora_iv_msgitem_avatar.setController(draweeController);
            ((SendVoiceViewHolder) holder).aurora_iv_msgitem_read_status.setTag(messages.get(position).localTime()+"_status");

            // 发送失败显示图标
            if (messages.get(position).status() == EMMessage.Status.FAIL) {
                ((SendVoiceViewHolder) holder).aurora_iv_msgitem_read_status.setVisibility(View.VISIBLE);
            }
            else {
                ((SendVoiceViewHolder) holder).aurora_iv_msgitem_read_status.setVisibility(View.GONE);
            }
            ((SendVoiceViewHolder) holder).aurora_iv_msgitem_read_status.setOnClickListener(view -> {
                // 发送失败则通过点击进行重新发送
                EMMessage temp = messages.get(position);
                temp.setStatus(EMMessage.Status.INPROGRESS);
                EMMessageManager.sendSingleMessage(Utils.getApp(), temp);
                notifyItemChanged(position);
            });

            ((SendVoiceViewHolder) holder).aurora_iv_msgitem_send_progress_bar.setTag(messages.get(position).localTime()+"_pb");
            // 未发送完成则需要显示进度圈
            if (messages.get(position).status() == EMMessage.Status.INPROGRESS ||
                    messages.get(position).status() == EMMessage.Status.CREATE) {
                ((SendVoiceViewHolder) holder).aurora_iv_msgitem_send_progress_bar.setVisibility(View.VISIBLE);
            }
            else {
                ((SendVoiceViewHolder) holder).aurora_iv_msgitem_send_progress_bar.setVisibility(View.GONE);
            }

            ((SendVoiceViewHolder) holder).bubble.setOnClickListener(view -> {
                // 播放语音
                ((BaseConversationActivity) context).playMedia(
                        ((EMVoiceMessageBody) messages.get(position).getBody()).getLocalUrl(),
                        messages.get(position).localTime()+"_voice",
                        true);
            });

            ((SendVoiceViewHolder) holder).iv_voice.setTag(messages.get(position).localTime()+"_voice");
            // 判断是否正在播放
            if (mediaPlayerTag!=null && mediaPlayerTag.equals(messages.get(position).localTime()+"_voice")) {
                ((BaseConversationActivity) context).startVoicePlayAnimation(((SendVoiceViewHolder) holder).iv_voice, true);
            }
            else {
                ((SendVoiceViewHolder) holder).iv_voice.setImageResource(R.mipmap.ease_chatto_voice_playing);
            }

            // 音频播放时间秒数
            File file = new File(((EMVoiceMessageBody) messages.get(position).getBody()).getLocalUrl());
            if (file == null) {
                ((SendVoiceViewHolder) holder).aurora_tv_voice_length.setText(0+"\'\'");
            }
            else {
                if (file.length()/1000 < 1) {
                    ((SendVoiceViewHolder) holder).aurora_tv_voice_length.setText(1+"\'\'");
                }
                else {
                    ((SendVoiceViewHolder) holder).aurora_tv_voice_length.setText(file.length()/1000+"\'\'");
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        boolean isSend = messages.get(position).getFrom().equals(SPUtils.getInstance().getString(CommonParams.SP_UNAME));
        if (messages.get(position).getBody() instanceof EMTextMessageBody) {
            if (isSend) {
                return 1;
            }
            else {
                return 0;
            }
        }
        else if (messages.get(position).getBody() instanceof EMImageMessageBody) {
            if (isSend) {
                return 3;
            }
            else {
                return 2;
            }
        }
        else if (messages.get(position).getBody() instanceof EMVoiceMessageBody) {
            if (isSend) {
                return 5;
            }
            else {
                return 4;
            }
        }
        else if (messages.get(position).getBody() instanceof EMVideoMessageBody) {
            if (isSend) {
                return 7;
            }
            else {
                return 6;
            }
        }
        else if (messages.get(position).getBody() instanceof EMLocationMessageBody) {
            if (isSend) {
                return 9;
            }
            else {
                return 8;
            }
        }
        else if (messages.get(position).getBody() instanceof EMFileMessageBody) {
            if (isSend) {
                return 11;
            }
            else {
                return 10;
            }
        }
        return super.getItemViewType(position);
    }

    class ReceiverTextViewHolder extends RecyclerView.ViewHolder {
        TextView aurora_tv_msgitem_date;
        SimpleDraweeView aurora_iv_msgitem_avatar;
        TextView aurora_tv_msgitem_display_name;
        TextView aurora_tv_msgitem_message;

        ReceiverTextViewHolder(View itemView) {
            super(itemView);

            aurora_tv_msgitem_date = itemView.findViewById(R.id.aurora_tv_msgitem_date);
            aurora_iv_msgitem_avatar = itemView.findViewById(R.id.aurora_iv_msgitem_avatar);
            aurora_tv_msgitem_display_name = itemView.findViewById(R.id.aurora_tv_msgitem_display_name);
            aurora_tv_msgitem_message = itemView.findViewById(R.id.aurora_tv_msgitem_message);
        }
    }

    class ReceiverImageViewHolder extends RecyclerView.ViewHolder {
        TextView aurora_tv_msgitem_date;
        SimpleDraweeView aurora_iv_msgitem_avatar;
        TextView aurora_tv_msgitem_display_name;
        SimpleDraweeView aurora_iv_msgitem_photo;

        ReceiverImageViewHolder(View itemView) {
            super(itemView);

            aurora_tv_msgitem_date = itemView.findViewById(R.id.aurora_tv_msgitem_date);
            aurora_iv_msgitem_avatar = itemView.findViewById(R.id.aurora_iv_msgitem_avatar);
            aurora_tv_msgitem_display_name = itemView.findViewById(R.id.aurora_tv_msgitem_display_name);
            aurora_iv_msgitem_photo = itemView.findViewById(R.id.aurora_iv_msgitem_photo);
        }
    }

    class ReceiverVoiceViewHolder extends RecyclerView.ViewHolder {
        TextView aurora_tv_msgitem_date;
        SimpleDraweeView aurora_iv_msgitem_avatar;
        TextView aurora_tv_msgitem_display_name;
        ImageView iv_voice;
        TextView aurora_tv_voice_length;
        ImageView aurora_iv_msgitem_read_status;
        RelativeLayout bubble;

        ReceiverVoiceViewHolder(View itemView) {
            super(itemView);

            aurora_tv_msgitem_date = itemView.findViewById(R.id.aurora_tv_msgitem_date);
            aurora_iv_msgitem_avatar = itemView.findViewById(R.id.aurora_iv_msgitem_avatar);
            aurora_tv_msgitem_display_name = itemView.findViewById(R.id.aurora_tv_msgitem_display_name);
            iv_voice = itemView.findViewById(R.id.iv_voice);
            aurora_tv_voice_length = itemView.findViewById(R.id.aurora_tv_voice_length);
            aurora_iv_msgitem_read_status = itemView.findViewById(R.id.aurora_iv_msgitem_read_status);
            bubble = itemView.findViewById(R.id.bubble);
        }
    }

    class SendTextViewHolder extends RecyclerView.ViewHolder {
        TextView aurora_tv_msgitem_date;
        SimpleDraweeView aurora_iv_msgitem_avatar;
        ImageView aurora_iv_msgitem_send_status;
        ProgressBar aurora_iv_msgitem_send_progress_bar;
        TextView aurora_tv_msgitem_message;

        SendTextViewHolder(View itemView) {
            super(itemView);

            aurora_tv_msgitem_date = itemView.findViewById(R.id.aurora_tv_msgitem_date);
            aurora_iv_msgitem_avatar = itemView.findViewById(R.id.aurora_iv_msgitem_avatar);
            aurora_iv_msgitem_send_status = itemView.findViewById(R.id.aurora_iv_msgitem_send_status);
            aurora_iv_msgitem_send_progress_bar = itemView.findViewById(R.id.aurora_iv_msgitem_send_progress_bar);
            aurora_tv_msgitem_message = itemView.findViewById(R.id.aurora_tv_msgitem_message);
        }
    }

    class SendImageViewHolder extends RecyclerView.ViewHolder {
        TextView aurora_tv_msgitem_date;
        SimpleDraweeView aurora_iv_msgitem_avatar;
        SimpleDraweeView aurora_iv_msgitem_photo;
        ImageView aurora_iv_msgitem_send_status;
        ProgressBar aurora_iv_msgitem_send_progress_bar;
        RelativeLayout bubble;

        SendImageViewHolder(View itemView) {
            super(itemView);

            aurora_tv_msgitem_date = itemView.findViewById(R.id.aurora_tv_msgitem_date);
            aurora_iv_msgitem_avatar = itemView.findViewById(R.id.aurora_iv_msgitem_avatar);
            aurora_iv_msgitem_photo = itemView.findViewById(R.id.aurora_iv_msgitem_photo);
            aurora_iv_msgitem_send_status = itemView.findViewById(R.id.aurora_iv_msgitem_send_status);
            aurora_iv_msgitem_send_progress_bar = itemView.findViewById(R.id.aurora_iv_msgitem_send_progress_bar);
            bubble = itemView.findViewById(R.id.bubble);
        }
    }

    class SendVoiceViewHolder extends RecyclerView.ViewHolder {
        TextView aurora_tv_msgitem_date;
        SimpleDraweeView aurora_iv_msgitem_avatar;
        TextView aurora_tv_voice_length;
        ImageView iv_voice;
        ProgressBar aurora_iv_msgitem_send_progress_bar;
        ImageView aurora_iv_msgitem_read_status;
        RelativeLayout bubble;

        SendVoiceViewHolder(View itemView) {
            super(itemView);

            aurora_tv_msgitem_date = itemView.findViewById(R.id.aurora_tv_msgitem_date);
            aurora_iv_msgitem_avatar = itemView.findViewById(R.id.aurora_iv_msgitem_avatar);
            aurora_tv_voice_length = itemView.findViewById(R.id.aurora_tv_voice_length);
            iv_voice = itemView.findViewById(R.id.iv_voice);
            aurora_iv_msgitem_send_progress_bar = itemView.findViewById(R.id.aurora_iv_msgitem_send_progress_bar);
            aurora_iv_msgitem_read_status = itemView.findViewById(R.id.aurora_iv_msgitem_read_status);
            bubble = itemView.findViewById(R.id.bubble);
        }
    }

    private static String getFriendlyTimeSpanByNow(long millis) {
        long now = System.currentTimeMillis();
        // 获取当天00:00
        long wee = (now / TimeConstants.DAY) * TimeConstants.DAY - 8 * TimeConstants.HOUR;
        if (millis >= wee+1000*3600*12) {
            return String.format("下午 %tR", millis);
        } else if (millis >= wee) {
            return String.format("上午 %tR", millis);
        } else if (millis >= wee - TimeConstants.DAY) {
            return String.format("昨天 %tR", millis);
        } else {
            if (isSameDate(now, millis)) {
                return String.format(TimeUtils.getChineseWeek(millis)+" %tR", millis);
            }
            else {
                if (isSameYear(now, millis)) {
                    return String.format(TimeUtils.millis2String(millis, new SimpleDateFormat("MM-dd", Locale.getDefault()))+" %tR", millis);
                }
                else {
                    return String.format(TimeUtils.millis2String(millis, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()))+" %tR", millis);
                }
            }
        }
    }

    private static boolean isSameDate(long t1, long t2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeInMillis(t1);
        cal2.setTimeInMillis(t2);
        int subYear = cal1.get(Calendar.YEAR)-cal2.get(Calendar.YEAR);
        if(subYear == 0) {
            if(cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR))
                return true;
        }
        else if(subYear==1 && cal2.get(Calendar.MONTH)==11) {
            if(cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR))
                return true;
        }
        else if(subYear==-1 && cal1.get(Calendar.MONTH)==11) {
            if(cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR))
                return true;
        }
        return false;
    }

    private static boolean isSameYear(long t1, long t2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeInMillis(t1);
        cal2.setTimeInMillis(t2);
        int subYear = cal1.get(Calendar.YEAR)-cal2.get(Calendar.YEAR);
        if(subYear == 0) {
            return true;
        }
        return false;
    }
}
