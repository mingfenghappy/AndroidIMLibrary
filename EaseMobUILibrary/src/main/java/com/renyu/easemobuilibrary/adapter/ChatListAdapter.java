package com.renyu.easemobuilibrary.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.TimeUtils;
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
import com.renyu.easemoblibrary.manager.EMMessageManager;
import com.renyu.easemoblibrary.model.BroadcastBean;
import com.renyu.easemobuilibrary.R;
import com.renyu.easemobuilibrary.params.CommonParams;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ConversationListHolder> {

    Context context;
    ArrayList<EMMessage> messages;

    public ChatListAdapter(Context context, ArrayList<EMMessage> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public ConversationListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.adapter_chatlist, parent, false);
        return new ConversationListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationListHolder holder, int position) {
        final int position_=position;
        ImageRequest request;
        DraweeController draweeController;
        String uid = messages.get(position).getFrom().equals(SPUtils.getInstance().getString(CommonParams.SP_UNAME))?
                messages.get(position).getTo():
                messages.get(position).getFrom();
        holder.tv_adapter_conversationlist_name.setText(uid);
        if (messages.get(position).getBody() instanceof EMTextMessageBody) {
            holder.tv_adapter_conversationlist_msg.setText(((EMTextMessageBody) messages.get(position).getBody()).getMessage());
        }
        else if (messages.get(position).getBody() instanceof EMImageMessageBody) {
            holder.tv_adapter_conversationlist_msg.setText("[图片]");
        }
        else if (messages.get(position).getBody() instanceof EMVoiceMessageBody) {
            holder.tv_adapter_conversationlist_msg.setText("[语音]");
        }
        else if (messages.get(position).getBody() instanceof EMVideoMessageBody) {
            // TODO: 2018/6/8 0008 这里需要添加文件类型以及文件名称
            holder.tv_adapter_conversationlist_msg.setText("[视频]");
        }
        else if (messages.get(position).getBody() instanceof EMLocationMessageBody) {
            // TODO: 2018/6/8 0008 这里需要添加反地理编码
            holder.tv_adapter_conversationlist_msg.setText("[位置]");
        }
        else if (messages.get(position).getBody() instanceof EMFileMessageBody) {
            // TODO: 2018/6/8 0008 这里需要添加文件类型以及文件名称
            holder.tv_adapter_conversationlist_msg.setText("[文件]");
        }
        request = ImageRequestBuilder.newBuilderWithSource(Uri.parse("res:///"+R.drawable.default_grey_avatar))
                .setResizeOptions(new ResizeOptions(SizeUtils.dp2px(40), SizeUtils.dp2px(40))).build();
        draweeController = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request).setAutoPlayAnimations(true).build();
        holder.iv_adapter_conversationlist.setController(draweeController);
        holder.tv_adapter_conversationlist_time.setText(getFriendlyTimeSpanByNow(messages.get(position).getMsgTime()));
        if (EMMessageManager.getUnreadMsgCount(uid)>0) {
            holder.tv_adapter_conversationlist_message.setText(String.valueOf(EMMessageManager.getUnreadMsgCount(uid)));
            holder.tv_adapter_conversationlist_message.setVisibility(View.VISIBLE);
        }
        else {
            holder.tv_adapter_conversationlist_message.setVisibility(View.INVISIBLE);
        }
        holder.layout_adapter_conversationlist.setOnClickListener(view -> {
            EMMessageManager.markAllMessagesAsRead(uid);

            try {
                Class clazz = Class.forName("com.renyu.easemobapp.params.InitParams");
                String conversationActivityName = clazz.getField("ConversationActivityName").get(clazz).toString();

                Class conversationClass = Class.forName(conversationActivityName);

//                Intent intent=new Intent(context, conversationClass);
//                intent.putExtra("UserId", messages.get(position_).getFromUserId());
//                intent.putExtra("UserHeadId", messages.get(position_).getUserHeadId());
//                intent.putExtra("UserNickName", messages.get(position_).getUserNickName());
//                intent.putExtra("UserHeadType", messages.get(position_).getUserHeadType());
//                context.startActivity(intent);
            } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
            BroadcastBean.sendBroadcast(context, BroadcastBean.EaseMobCommand.UpdateRead);
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class ConversationListHolder extends RecyclerView.ViewHolder {

        LinearLayout layout_adapter_conversationlist;
        SimpleDraweeView iv_adapter_conversationlist;
        TextView tv_adapter_conversationlist_name;
        TextView tv_adapter_conversationlist_msg;
        TextView tv_adapter_conversationlist_time;
        TextView tv_adapter_conversationlist_message;

        public ConversationListHolder(View itemView) {
            super(itemView);

            layout_adapter_conversationlist = itemView.findViewById(R.id.layout_adapter_conversationlist);
            iv_adapter_conversationlist = itemView.findViewById(R.id.iv_adapter_conversationlist);
            tv_adapter_conversationlist_name = itemView.findViewById(R.id.tv_adapter_conversationlist_name);
            tv_adapter_conversationlist_msg = itemView.findViewById(R.id.tv_adapter_conversationlist_msg);
            tv_adapter_conversationlist_time = itemView.findViewById(R.id.tv_adapter_conversationlist_time);
            tv_adapter_conversationlist_message = itemView.findViewById(R.id.tv_adapter_conversationlist_message);
        }
    }

    private static String getFriendlyTimeSpanByNow(long millis) {
        long now = System.currentTimeMillis();
        // 获取当天00:00
        long wee = (now / TimeConstants.DAY) * TimeConstants.DAY - 8 * TimeConstants.HOUR;
        if (millis >= wee+1000*3600*12) {
            return String.format("下午%tR", millis);
        } else if (millis >= wee) {
            return String.format("上午%tR", millis);
        } else if (millis >= wee - TimeConstants.DAY) {
            return String.format("昨天", millis);
        } else {
            if (isSameDate(now, millis)) {
                return TimeUtils.getChineseWeek(millis);
            }
            else {
                return TimeUtils.millis2String(millis, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()));
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
}
