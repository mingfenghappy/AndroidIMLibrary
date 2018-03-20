package com.renyu.mt.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.focustech.dbhelper.PlainTextDBHelper;
import com.focustech.tm.open.sdk.messages.protobuf.Enums;
import com.focustech.webtm.protocol.tm.message.model.BroadcastBean;
import com.focustech.webtm.protocol.tm.message.model.MessageBean;
import com.focustech.webtm.protocol.tm.message.model.UserInfoRsp;
import com.renyu.commonlibrary.commonutils.ACache;
import com.renyu.mt.R;
import com.renyu.mt.activity.ConversationActivity;
import com.renyu.mt.activity.SystemMessageActivity;
import com.renyu.mt.utils.AvatarUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.fresco.processors.GrayscalePostprocessor;

/**
 * Created by Administrator on 2017/7/21.
 */

public class ConversationListAdapter extends RecyclerView.Adapter<ConversationListAdapter.ConversationListHolder> {

    Context context;
    ArrayList<MessageBean> offlineMessages;

    public ConversationListAdapter(Context context, ArrayList<MessageBean> offlineMessages) {
        this.context = context;
        this.offlineMessages = offlineMessages;
    }

    @Override
    public ConversationListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.adapter_conversationlist, parent, false);
        return new ConversationListHolder(view);
    }

    @Override
    public void onBindViewHolder(ConversationListHolder holder, int position) {
        final int position_=position;
        ImageRequest request;
        DraweeController draweeController;
        // 系统消息
        if (offlineMessages.get(position).getUserId().equals("-1")) {
            holder.tv_adapter_conversationlist_name.setText("系统消息");
            request = ImageRequestBuilder.newBuilderWithSource(Uri.parse("res:///"+R.drawable.default_avatar0))
                    .setPostprocessor(new GrayscalePostprocessor())
                    .setResizeOptions(new ResizeOptions(SizeUtils.dp2px(40), SizeUtils.dp2px(40))).build();
            holder.tv_adapter_conversationlist_msg.setText(offlineMessages.get(position).getMsg());
            holder.layout_adapter_conversationlist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context, SystemMessageActivity.class));
                }
            });
        }
        // 普通好友消息
        else {
            holder.tv_adapter_conversationlist_name.setText(offlineMessages.get(position).getUserInfoRsp()==null?
                    offlineMessages.get(position).getUserId():
                    offlineMessages.get(position).getUserInfoRsp().getUserName());
            if (offlineMessages.get(position).getMessageType().equals("0")) {
                holder.tv_adapter_conversationlist_msg.setText(offlineMessages.get(position).getMsg());
            }
            else if (offlineMessages.get(position).getMessageType().equals("9")) {
                holder.tv_adapter_conversationlist_msg.setText(offlineMessages.get(position).getMsg());
            }
            else if (offlineMessages.get(position).getMessageType().equals("8")) {
                holder.tv_adapter_conversationlist_msg.setText("[图片]");
            }
            else if (offlineMessages.get(position).getMessageType().equals("7")) {
                holder.tv_adapter_conversationlist_msg.setText("[语音]");
            }
            if (offlineMessages.get(position).getUserInfoRsp()!=null) {
                String faceCode = String.valueOf(offlineMessages.get(position).getUserInfoRsp().getUserHeadType().getNumber());
                String fileId = offlineMessages.get(position).getUserInfoRsp().getUserHeadId();
                UserInfoRsp userInfoRsp= (UserInfoRsp) ACache.get(context).getAsObject("UserInfoRsp");
                String token=userInfoRsp.getToken();
                Object avatar= AvatarUtils.displayImg(faceCode, fileId, token);
                Enums.EquipmentStatus showStatus = UserInfoRsp.getShowStatus(offlineMessages.get(position).getUserInfoRsp().getEquipments());
                // TODO: 2017/7/19 所有用户都是离线的？
                if (!UserInfoRsp.isOnline(showStatus.getStatus().getNumber())) {
                    request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(avatar instanceof String?avatar.toString():"res:///"+Integer.parseInt(avatar.toString())))
                            .setPostprocessor(new GrayscalePostprocessor())
                            .setResizeOptions(new ResizeOptions(SizeUtils.dp2px(40), SizeUtils.dp2px(40))).build();
                }
                else {
                    request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(avatar instanceof String?avatar.toString():"res:///"+Integer.parseInt(avatar.toString())))
                            .setResizeOptions(new ResizeOptions(SizeUtils.dp2px(40), SizeUtils.dp2px(40))).build();
                }
            }
            else {
                request = ImageRequestBuilder.newBuilderWithSource(Uri.parse("res:///"+R.drawable.default_avatar0))
                        .setPostprocessor(new GrayscalePostprocessor())
                        .setResizeOptions(new ResizeOptions(SizeUtils.dp2px(40), SizeUtils.dp2px(40))).build();
            }
            if (offlineMessages.get(position).getCount()>0) {
                holder.tv_adapter_conversationlist_message.setText(""+offlineMessages.get(position).getCount());
                holder.tv_adapter_conversationlist_message.setVisibility(View.VISIBLE);
            }
            else {
                holder.tv_adapter_conversationlist_message.setVisibility(View.INVISIBLE);
            }
            holder.layout_adapter_conversationlist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PlainTextDBHelper.getInstance().updateRead(offlineMessages.get(position_).getUserId());
                    BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.UpdateRead, offlineMessages.get(position_).getUserId());

                    Intent intent=new Intent(context, ConversationActivity.class);
                    intent.putExtra("UserInfoRsp", offlineMessages.get(position_).getUserInfoRsp());
                    intent.putExtra("UserId", offlineMessages.get(position_).getUserId());
                    context.startActivity(intent);
                }
            });
        }
        draweeController = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request).setAutoPlayAnimations(true).build();
        holder.iv_adapter_conversationlist.setController(draweeController);
        holder.tv_adapter_conversationlist_time.setText(getFriendlyTimeSpanByNow(offlineMessages.get(position).getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return offlineMessages.size();
    }

    public class ConversationListHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.layout_adapter_conversationlist)
        LinearLayout layout_adapter_conversationlist;
        @BindView(R.id.iv_adapter_conversationlist)
        SimpleDraweeView iv_adapter_conversationlist;
        @BindView(R.id.tv_adapter_conversationlist_name)
        TextView tv_adapter_conversationlist_name;
        @BindView(R.id.tv_adapter_conversationlist_msg)
        TextView tv_adapter_conversationlist_msg;
        @BindView(R.id.tv_adapter_conversationlist_time)
        TextView tv_adapter_conversationlist_time;
        @BindView(R.id.tv_adapter_conversationlist_message)
        TextView tv_adapter_conversationlist_message;

        public ConversationListHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public static String getFriendlyTimeSpanByNow(long millis) {
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
