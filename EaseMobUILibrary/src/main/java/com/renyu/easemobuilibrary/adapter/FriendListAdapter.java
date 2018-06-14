package com.renyu.easemobuilibrary.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;
import com.blankj.utilcode.util.SizeUtils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.renyu.easemobuilibrary.R;
import com.renyu.easemobuilibrary.manager.EMMessageManager;
import com.renyu.easemobuilibrary.model.BroadcastBean;

import java.util.List;

/**
 * Created by Administrator on 2017/8/4.
 */

public class FriendListAdapter extends DelegateAdapter.Adapter<FriendListAdapter.FriendListHolder> {

    Context context;
    LayoutHelper layoutHelper;

    List<String> beans;

    public FriendListAdapter(Context context, LayoutHelper layoutHelper, List<String> beans) {
        this.context=context;
        this.layoutHelper=layoutHelper;
        this.beans=beans;
    }

    @Override
    public LayoutHelper onCreateLayoutHelper() {
        return layoutHelper;
    }

    @Override
    public FriendListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.adapter_friendlist, parent, false);
        return new FriendListHolder(view);
    }

    @Override
    public void onBindViewHolder(FriendListHolder holder, int position) {
        final int position_=position;
        holder.tv_adapter_friendlist.setText(beans.get(position));
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse("res:///"+R.drawable.default_grey_avatar))
                .setResizeOptions(new ResizeOptions(SizeUtils.dp2px(40), SizeUtils.dp2px(40))).build();
        DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request).setAutoPlayAnimations(true).build();
        holder.iv_adapter_friendlist.setController(draweeController);
        holder.layout_adapter_friendlist.setOnClickListener(view -> {
            // 设置当前会话列表消息均已读
            EMMessageManager.markAllMessagesAsRead(beans.get(position_));
            BroadcastBean.sendBroadcast(BroadcastBean.EaseMobCommand.UpdateRead);

            try {
                Class clazz = Class.forName("com.renyu.easemobapp.params.InitParams");
                String conversationActivityName = clazz.getField("ConversationActivityName").get(clazz).toString();

                Class conversationClass = Class.forName(conversationActivityName);

                Intent intent=new Intent(context, conversationClass);
                intent.putExtra("UserId", beans.get(position_));
                intent.putExtra("isGroup", false);
                context.startActivity(intent);
            } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return beans.size();
    }

    public class FriendListHolder extends RecyclerView.ViewHolder {
        LinearLayout layout_adapter_friendlist;
        SimpleDraweeView iv_adapter_friendlist;
        TextView tv_adapter_friendlist;

        public FriendListHolder(View itemView) {
            super(itemView);

            layout_adapter_friendlist = itemView.findViewById(R.id.layout_adapter_friendlist);
            iv_adapter_friendlist = itemView.findViewById(R.id.iv_adapter_friendlist);
            tv_adapter_friendlist = itemView.findViewById(R.id.tv_adapter_friendlist);
        }
    }
}
