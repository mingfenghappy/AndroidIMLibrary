package com.renyu.tmuilibrary.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.Utils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.focustech.dbhelper.PlainTextDBHelper;
import com.focustech.message.model.MessageBean;
import com.focustech.message.model.UserInfoRsp;
import com.focustech.params.FusionField;
import com.focustech.tm.open.sdk.messages.protobuf.Enums;
import com.renyu.commonlibrary.commonutils.ACache;
import com.renyu.commonlibrary.params.InitParams;
import com.renyu.tmbaseuilibrary.utils.AvatarUtils;
import com.renyu.tmbaseuilibrary.utils.FaceIconUtil;
import com.renyu.tmuilibrary.R;
import com.renyu.tmuilibrary.activity.ConversationActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import jp.wasabeef.fresco.processors.GrayscalePostprocessor;

/**
 * Created by Administrator on 2017/7/24.
 */

public class ConversationAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<MessageBean> messages;
    boolean isGroup;
    String userId;
    // 当前用户信息
    UserInfoRsp currentUserInfo;
    // 聊天对象信息
    HashMap<String, UserInfoRsp> userInfoRsps;
    // 当前正在播放的音频文件tag
    public String mediaPlayerTag=null;

    public ConversationAdapter(Context context, ArrayList<MessageBean> messages, boolean isGroup, String userId) {
        this.context = context;
        this.messages = messages;
        this.isGroup = isGroup;
        this.userId = userId;

        this.userInfoRsps = PlainTextDBHelper.getInstance(Utils.getApp()).getFriendsInfo();
        this.currentUserInfo= (UserInfoRsp) ACache.get(context).getAsObject("UserInfoRsp");
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType==0) {
            View view= LayoutInflater.from(context).inflate(R.layout.adapter_receive_text, parent, false);
            return new ReceiverTextViewHolder(view);
        }
        else if (viewType==1) {
            View view= LayoutInflater.from(context).inflate(R.layout.adapter_receive_photo, parent, false);
            return new ReceiverImageViewHolder(view);
        }
        else if (viewType==2) {
            View view= LayoutInflater.from(context).inflate(R.layout.adapter_receive_voice, parent, false);
            return new ReceiverVoiceViewHolder(view);
        }
        else if (viewType==3) {
            View view= LayoutInflater.from(context).inflate(R.layout.adapter_send_text, parent, false);
            return new SendTextViewHolder(view);
        }
        else if (viewType==4) {
            View view= LayoutInflater.from(context).inflate(R.layout.adapter_send_photo, parent, false);
            return new SendImageViewHolder(view);
        }
        else if (viewType==5) {
            View view= LayoutInflater.from(context).inflate(R.layout.adapter_send_voice, parent, false);
            return new SendVoiceViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        // 判断是否显示日期
        if (position==0) {
            if (getItemViewType(position)==0) {
                ((ReceiverTextViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.VISIBLE);
                ((ReceiverTextViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getTimestamp(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
            }
            else if (getItemViewType(position)==1) {
                ((ReceiverImageViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.VISIBLE);
                ((ReceiverImageViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getTimestamp(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
            }
            else if (getItemViewType(position)==2) {
                ((ReceiverVoiceViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.VISIBLE);
                ((ReceiverVoiceViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getTimestamp(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
            }
            else if (getItemViewType(position)==3) {
                ((SendTextViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.VISIBLE);
                ((SendTextViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getTimestamp(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
            }
            else if (getItemViewType(position)==4) {
                ((SendImageViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.VISIBLE);
                ((SendImageViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getTimestamp(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
            }
            else if (getItemViewType(position)==5) {
                ((SendVoiceViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.VISIBLE);
                ((SendVoiceViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getTimestamp(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
            }
        }
        else {
            if (messages.get(position).getTimestamp()-messages.get(position-1).getTimestamp()>1000*60) {
                if (getItemViewType(position)==0) {
                    ((ReceiverTextViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.VISIBLE);
                    ((ReceiverTextViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getTimestamp(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
                }
                else if (getItemViewType(position)==1) {
                    ((ReceiverImageViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.VISIBLE);
                    ((ReceiverImageViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getTimestamp(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
                }
                else if (getItemViewType(position)==2) {
                    ((ReceiverVoiceViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.VISIBLE);
                    ((ReceiverVoiceViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getTimestamp(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
                }
                else if (getItemViewType(position)==3) {
                    ((SendTextViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.VISIBLE);
                    ((SendTextViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getTimestamp(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
                }
                else if (getItemViewType(position)==4) {
                    ((SendImageViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.VISIBLE);
                    ((SendImageViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getTimestamp(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
                }
                else if (getItemViewType(position)==5) {
                    ((SendVoiceViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.VISIBLE);
                    ((SendVoiceViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getTimestamp(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
                }
            }
            else {
                if (getItemViewType(position)==0) {
                    ((ReceiverTextViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.GONE);
                }
                else if (getItemViewType(position)==1) {
                    ((ReceiverImageViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.GONE);
                }
                else if (getItemViewType(position)==2) {
                    ((ReceiverVoiceViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.GONE);
                }
                else if (getItemViewType(position)==3) {
                    ((SendTextViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.GONE);
                }
                else if (getItemViewType(position)==4) {
                    ((SendImageViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.GONE);
                }
                else if (getItemViewType(position)==5) {
                    ((SendVoiceViewHolder) holder).aurora_tv_msgitem_date.setVisibility(View.GONE);
                }
            }
        }
        String token=currentUserInfo.getToken();
        UserInfoRsp userInfo=userInfoRsps.get(messages.get(position).getUserId());
        ImageRequest request;
        if (userInfo!=null) {
            String faceCode = String.valueOf(userInfo.getUserHeadType().getNumber());
            String fileId = userInfo.getUserHeadId();
            // 判断是不是自己发送的
            if (messages.get(position).getIsSend().equals("1")) {
                faceCode = String.valueOf(currentUserInfo.getUserHeadType().getNumber());
                fileId = currentUserInfo.getUserHeadId();
            }
            Object avatar= AvatarUtils.displayImg(faceCode, fileId, token);
            request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(avatar instanceof String?avatar.toString():"res:///"+Integer.parseInt(avatar.toString())))
                    .setResizeOptions(new ResizeOptions(SizeUtils.dp2px(50), SizeUtils.dp2px(50))).build();
        }
        else {
            request = ImageRequestBuilder.newBuilderWithSource(Uri.parse("res:///"+R.drawable.default_avatar0))
                    .setPostprocessor(new GrayscalePostprocessor())
                    .setResizeOptions(new ResizeOptions(SizeUtils.dp2px(40), SizeUtils.dp2px(40))).build();
        }
        DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request).setAutoPlayAnimations(true).build();
        if (getItemViewType(position)==0) {
            ((ReceiverTextViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getTimestamp(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
            ((ReceiverTextViewHolder) holder).aurora_iv_msgitem_avatar.setController(draweeController);
            // 判断显示用户昵称还是userId
            if (userInfo!=null) {
                ((ReceiverTextViewHolder) holder).aurora_tv_msgitem_display_name.setText(userInfo.getUserName());
            }
            else {
                ((ReceiverTextViewHolder) holder).aurora_tv_msgitem_display_name.setText(userId);
            }
            // 如果是群组则显示用户昵称或者userId
            if (isGroup) {
                ((ReceiverTextViewHolder) holder).aurora_tv_msgitem_display_name.setVisibility(View.VISIBLE);
            }
            else {
                ((ReceiverTextViewHolder) holder).aurora_tv_msgitem_display_name.setVisibility(View.GONE);
            }
            // 加载文字
            ((ReceiverTextViewHolder) holder).aurora_tv_msgitem_message.setText(FaceIconUtil.getInstance().replaceFaceMsg(messages.get(position).getMsg()));
        }
        else if (getItemViewType(position)==1) {
            ((ReceiverImageViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getTimestamp(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
            ((ReceiverImageViewHolder) holder).aurora_iv_msgitem_avatar.setController(draweeController);
            // 判断显示用户昵称还是userId
            if (userInfo!=null) {
                ((ReceiverImageViewHolder) holder).aurora_tv_msgitem_display_name.setText(userInfo.getUserName());
            }
            else {
                ((ReceiverImageViewHolder) holder).aurora_tv_msgitem_display_name.setText(userId);
            }
            // 如果是群组则显示用户昵称或者userId
            if (isGroup) {
                ((ReceiverImageViewHolder) holder).aurora_tv_msgitem_display_name.setVisibility(View.VISIBLE);
            }
            else {
                ((ReceiverImageViewHolder) holder).aurora_tv_msgitem_display_name.setVisibility(View.GONE);
            }
            // 加载图片
            String fileId_ = messages.get(position).getLocalFileName();
            StringBuilder sb = new StringBuilder(FusionField.downloadUrl);
            sb.append("fileid=").append(fileId_).append("&type=").append("picture").append("&token=").append(token);
            ImageRequest request_ = ImageRequestBuilder.newBuilderWithSource(Uri.parse(sb.toString()))
                    .setResizeOptions(new ResizeOptions(SizeUtils.dp2px(100), SizeUtils.dp2px(100))).build();
            DraweeController draweeController_ = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request_).setAutoPlayAnimations(true).build();
            ((ReceiverImageViewHolder) holder).aurora_iv_msgitem_photo.setController(draweeController_);
        }
        else if (getItemViewType(position)==2) {
            ((ReceiverVoiceViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getTimestamp(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
            ((ReceiverVoiceViewHolder) holder).aurora_iv_msgitem_avatar.setController(draweeController);
            // 判断显示用户昵称还是userId
            if (userInfo!=null) {
                ((ReceiverVoiceViewHolder) holder).aurora_tv_msgitem_display_name.setText(userInfo.getUserName());
            }
            else {
                ((ReceiverVoiceViewHolder) holder).aurora_tv_msgitem_display_name.setText(userId);
            }
            // 如果是群组则显示用户昵称或者userId
            if (isGroup) {
                ((ReceiverVoiceViewHolder) holder).aurora_tv_msgitem_display_name.setVisibility(View.VISIBLE);
            }
            else {
                ((ReceiverVoiceViewHolder) holder).aurora_tv_msgitem_display_name.setVisibility(View.GONE);
            }
            ((ReceiverVoiceViewHolder) holder).bubble.setOnClickListener(view -> {
                // 播放语音
                ((ConversationActivity) context).playMedia(InitParams.FILE_PATH+"/"+messages.get(position).getLocalFileName()+".amr", messages.get(position).getSvrMsgId()+"_voice");
                // 更新数据库
                PlainTextDBHelper.getInstance(Utils.getApp()).updateVoiceRead(messages.get(position).getSvrMsgId());
                // 更新本地缓存
                messages.get(position).setIsVoicePlay("1");
                // 刷新视图
                ((ReceiverVoiceViewHolder) holder).aurora_iv_msgitem_read_status.setVisibility(View.GONE);
            });
            ((ReceiverVoiceViewHolder) holder).iv_voice.setTag(messages.get(position).getSvrMsgId()+"_voice");
            // 判断是否正在播放
            if (mediaPlayerTag!=null && mediaPlayerTag.equals(messages.get(position).getSvrMsgId()+"_voice")) {
                ((ReceiverVoiceViewHolder) holder).iv_voice.setImageResource(R.drawable.default_avatar0);
            }
            else {
                ((ReceiverVoiceViewHolder) holder).iv_voice.setImageResource(R.mipmap.ease_chatto_voice_playing);
            }
            // 判断语音是否已读
            if (messages.get(position).getIsVoicePlay().equals("0")) {
                ((ReceiverVoiceViewHolder) holder).aurora_iv_msgitem_read_status.setVisibility(View.VISIBLE);
            }
            else {
                ((ReceiverVoiceViewHolder) holder).aurora_iv_msgitem_read_status.setVisibility(View.GONE);
            }
            ((ReceiverVoiceViewHolder) holder).aurora_tv_voice_length.setTag(messages.get(position).getSvrMsgId()+"_length");
            // 音频播放时间秒数
            File file = new File(InitParams.FILE_PATH+"/"+messages.get(position).getLocalFileName()+".amr");
            if (file == null) {
                ((ReceiverVoiceViewHolder) holder).aurora_tv_voice_length.setText(0+"\'\'");
            }
            else {
                if (file.length()/1600 < 1) {
                    ((ReceiverVoiceViewHolder) holder).aurora_tv_voice_length.setText(1+"\'\'");
                }
                else {
                    ((ReceiverVoiceViewHolder) holder).aurora_tv_voice_length.setText(file.length()/1600+"\'\'");
                }
            }
        }
        else if (getItemViewType(position)==3) {
            ((SendTextViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getTimestamp(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
            ((SendTextViewHolder) holder).aurora_iv_msgitem_avatar.setController(draweeController);
            ((SendTextViewHolder) holder).aurora_tv_msgitem_message.setText(FaceIconUtil.getInstance().replaceFaceMsg(messages.get(position).getMsg()));
        }
        else if (getItemViewType(position)==4) {
            ((SendImageViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getTimestamp(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
            ((SendImageViewHolder) holder).aurora_iv_msgitem_avatar.setController(draweeController);
            ((SendImageViewHolder) holder).aurora_iv_msgitem_send_status.setTag(messages.get(position).getSvrMsgId()+"_status");
            // 不需要重发则不显示图标
            if (messages.get(position).getResend()== Enums.Enable.DISABLE) {
                ((SendImageViewHolder) holder).aurora_iv_msgitem_send_status.setVisibility(View.GONE);
            }
            else {
                ((SendImageViewHolder) holder).aurora_iv_msgitem_send_status.setVisibility(View.VISIBLE);
            }
            ((SendImageViewHolder) holder).aurora_iv_msgitem_send_status.setOnClickListener(view -> {
                // 发送失败则通过点击进行重新发送
                ((ConversationActivity) context).resendPicMessage(messages.get(position));
            });
            ((SendImageViewHolder) holder).aurora_iv_msgitem_send_progress_bar.setTag(messages.get(position).getSvrMsgId()+"_pb");
            // 未发送完成则需要显示进度圈
            if (messages.get(position).getSync()== Enums.Enable.DISABLE) {
                ((SendImageViewHolder) holder).aurora_iv_msgitem_send_progress_bar.setVisibility(View.VISIBLE);
            }
            else {
                ((SendImageViewHolder) holder).aurora_iv_msgitem_send_progress_bar.setVisibility(View.GONE);
            }
            // 加载本地图片
            String filePath = messages.get(position).getLocalFileName();
            ImageRequest request_ = ImageRequestBuilder.newBuilderWithSource(Uri.parse("file:///"+filePath))
                    .setResizeOptions(new ResizeOptions(SizeUtils.dp2px(100), SizeUtils.dp2px(100))).build();
            DraweeController draweeController_ = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request_).setAutoPlayAnimations(true).build();
            ((SendImageViewHolder) holder).aurora_iv_msgitem_photo.setController(draweeController_);
            ((SendImageViewHolder) holder).bubble.setOnClickListener(view -> {

            });
        }
        else if (getItemViewType(position)==5) {
            ((SendVoiceViewHolder) holder).aurora_tv_msgitem_date.setText(TimeUtils.millis2String(messages.get(position).getTimestamp(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
            ((SendVoiceViewHolder) holder).aurora_iv_msgitem_avatar.setController(draweeController);
            ((SendVoiceViewHolder) holder).aurora_iv_msgitem_read_status.setTag(messages.get(position).getSvrMsgId()+"_status");
            // 不需要重发则不显示图标
            if (messages.get(position).getResend()== Enums.Enable.DISABLE) {
                ((SendVoiceViewHolder) holder).aurora_iv_msgitem_read_status.setVisibility(View.GONE);
            }
            else {
                ((SendVoiceViewHolder) holder).aurora_iv_msgitem_read_status.setVisibility(View.VISIBLE);
            }
            ((SendVoiceViewHolder) holder).aurora_iv_msgitem_read_status.setOnClickListener(view -> {
                // 本地真实自己发送的文件
                if (messages.get(position).getLocalFileName().indexOf(".amr") != -1) {
                    ((ConversationActivity) context).resendVoiceMessageState(messages.get(position));
                }
                // 同步时远程拿下的文件
                else {
                    ((ConversationActivity) context).playMedia(InitParams.FILE_PATH+"/"+messages.get(position).getLocalFileName()+".amr", messages.get(position).getSvrMsgId()+"_voice");
                }
            });
            ((SendVoiceViewHolder) holder).aurora_iv_msgitem_send_progress_bar.setTag(messages.get(position).getSvrMsgId()+"_pb");
            // 未发送完成则需要显示进度圈
            if (messages.get(position).getSync()== Enums.Enable.DISABLE) {
                ((SendVoiceViewHolder) holder).aurora_iv_msgitem_send_progress_bar.setVisibility(View.VISIBLE);
            }
            else {
                ((SendVoiceViewHolder) holder).aurora_iv_msgitem_send_progress_bar.setVisibility(View.GONE);
            }
            ((SendVoiceViewHolder) holder).bubble.setOnClickListener(view -> {
                // 播放语音
                // 本地真实自己发送的文件
                if (messages.get(position).getLocalFileName().indexOf(".amr") != -1) {
                    ((ConversationActivity) context).playMedia(messages.get(position).getLocalFileName(), messages.get(position).getSvrMsgId()+"_voice");
                }
                // 同步时远程拿下的文件
                else {
                    ((ConversationActivity) context).playMedia(InitParams.FILE_PATH+"/"+messages.get(position).getLocalFileName()+".amr", messages.get(position).getSvrMsgId()+"_voice");
                }
            });
            ((SendVoiceViewHolder) holder).iv_voice.setTag(messages.get(position).getSvrMsgId()+"_voice");
            // 判断是否正在播放
            if (mediaPlayerTag!=null && mediaPlayerTag.equals(messages.get(position).getSvrMsgId()+"_voice")) {
                ((SendVoiceViewHolder) holder).iv_voice.setImageResource(R.drawable.default_avatar0);
            }
            else {
                ((SendVoiceViewHolder) holder).iv_voice.setImageResource(R.mipmap.ease_chatto_voice_playing);
            }
            ((SendVoiceViewHolder) holder).aurora_tv_voice_length.setTag(messages.get(position).getSvrMsgId()+"_length");
            // 本地真实自己发送的文件
            if (messages.get(position).getLocalFileName().indexOf(".amr") != -1) {
                File file = new File(messages.get(position).getLocalFileName());
                ((SendVoiceViewHolder) holder).aurora_tv_voice_length.setText(file.length()/1600+"\'\'");
            }
            // 同步时远程拿下的文件
            else {
                File file = new File(InitParams.FILE_PATH+"/"+messages.get(position).getLocalFileName()+".amr");
                if (file == null) {
                    ((SendVoiceViewHolder) holder).aurora_tv_voice_length.setText(0+"\'\'");
                }
                else {
                    if (file.length()/1600 < 1) {
                        ((SendVoiceViewHolder) holder).aurora_tv_voice_length.setText(1+"\'\'");
                    }
                    else {
                        ((SendVoiceViewHolder) holder).aurora_tv_voice_length.setText(file.length()/1600+"\'\'");
                    }
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
        if (messages.get(position).getMessageType().equals("0")
                || messages.get(position).getMessageType().equals("9")) {
            if (messages.get(position).getIsSend().equals("1")) {
                return 3;
            }
            else {
                return 0;
            }
        }
        else if (messages.get(position).getMessageType().equals("8")) {
            if (messages.get(position).getIsSend().equals("1")) {
                return 4;
            }
            else {
                return 1;
            }
        }
        else if (messages.get(position).getMessageType().equals("7")) {
            if (messages.get(position).getIsSend().equals("1")) {
                return 5;
            }
            else {
                return 2;
            }
        }
        return super.getItemViewType(position);
    }

    class ReceiverTextViewHolder extends RecyclerView.ViewHolder {
        TextView aurora_tv_msgitem_date;
        SimpleDraweeView aurora_iv_msgitem_avatar;
        TextView aurora_tv_msgitem_display_name;
        TextView aurora_tv_msgitem_message;

        public ReceiverTextViewHolder(View itemView) {
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

        public ReceiverImageViewHolder(View itemView) {
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

        public ReceiverVoiceViewHolder(View itemView) {
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

        public SendTextViewHolder(View itemView) {
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

        public SendImageViewHolder(View itemView) {
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

        public SendVoiceViewHolder(View itemView) {
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
}
