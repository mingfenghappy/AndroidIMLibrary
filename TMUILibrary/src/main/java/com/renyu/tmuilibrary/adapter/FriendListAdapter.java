package com.renyu.tmuilibrary.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.Utils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.focustech.dbhelper.PlainTextDBHelper;
import com.focustech.message.model.BroadcastBean;
import com.focustech.message.model.FriendStatusRsp;
import com.focustech.message.model.UserInfoRsp;
import com.renyu.commonlibrary.commonutils.ACache;
import com.renyu.tmbaseuilibrary.utils.AvatarUtils;
import com.renyu.tmuilibrary.R;
import com.renyu.tmuilibrary.activity.ConversationActivity;

import java.util.List;

import jp.wasabeef.fresco.processors.GrayscalePostprocessor;

/**
 * Created by Administrator on 2017/8/4.
 */

public class FriendListAdapter extends DelegateAdapter.Adapter<FriendListAdapter.FriendListHolder> {

    Context context;
    LayoutHelper layoutHelper;

    List<FriendStatusRsp> beans;

    public FriendListAdapter(Context context, LayoutHelper layoutHelper, List<FriendStatusRsp> beans) {
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
        ImageRequest request;
        if (beans.get(position).getFriendInfoRsp().getFriend()!=null) {
            holder.tv_adapter_friendlist.setText(TextUtils.isEmpty(beans.get(position).getFriendInfoRsp().getFriend().getUserName())?
                    beans.get(position).getFriendInfoRsp().getFriend().getUserNickName():
                    beans.get(position).getFriendInfoRsp().getFriend().getUserName());
            String faceCode = String.valueOf(beans.get(position).getFriendInfoRsp().getFriend().getUserHeadType().getNumber());
            String fileId = beans.get(position).getFriendInfoRsp().getFriend().getUserHeadId();
            UserInfoRsp userInfoRsp= (UserInfoRsp) ACache.get(context).getAsObject("UserInfoRsp");
            String token=userInfoRsp.getToken();
            Object avatar= AvatarUtils.displayImg(faceCode, fileId, token);
            request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(avatar instanceof String?avatar.toString():"res:///"+Integer.parseInt(avatar.toString())))
                    .setResizeOptions(new ResizeOptions(SizeUtils.dp2px(40), SizeUtils.dp2px(40))).build();
        }
        else {
            holder.tv_adapter_friendlist.setText(beans.get(position).getFriendUserId());
            request = ImageRequestBuilder.newBuilderWithSource(Uri.parse("res:///"+R.drawable.default_avatar0))
                    .setPostprocessor(new GrayscalePostprocessor())
                    .setResizeOptions(new ResizeOptions(SizeUtils.dp2px(40), SizeUtils.dp2px(40))).build();
        }
        DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request).setAutoPlayAnimations(true).build();
        holder.iv_adapter_friendlist.setController(draweeController);
        holder.layout_adapter_friendlist.setOnClickListener(view -> {
            PlainTextDBHelper.getInstance(Utils.getApp()).updateRead(beans.get(position_).getFriendUserId());
            BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.UpdateRead, beans.get(position_).getFriendUserId());

            Intent intent=new Intent(context, ConversationActivity.class);
            intent.putExtra("UserId", beans.get(position_).getFriendUserId());
            context.startActivity(intent);
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
