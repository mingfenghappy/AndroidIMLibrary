package com.renyu.mt.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.renyu.mt.utils.DownloadUtils;
import com.focustech.common.MD5Utils;
import com.renyu.mt.utils.RecordUtils;
import com.focustech.dbhelper.PlainTextDBHelper;
import com.focustech.tm.open.sdk.messages.protobuf.Enums;
import com.focustech.tm.open.sdk.params.FusionField;
import com.focustech.webtm.protocol.tm.message.MTService;
import com.focustech.webtm.protocol.tm.message.model.BroadcastBean;
import com.focustech.webtm.protocol.tm.message.model.FileInfoBean;
import com.focustech.webtm.protocol.tm.message.model.IMBaseResponseList;
import com.focustech.webtm.protocol.tm.message.model.MessageBean;
import com.focustech.webtm.protocol.tm.message.model.OfflineIMDetailResponse;
import com.focustech.webtm.protocol.tm.message.model.UserInfoRsp;
import com.focustech.webtm.protocol.tm.message.params.MessageMeta;
import com.renyu.commonlibrary.commonutils.ACache;
import com.renyu.commonlibrary.network.Retrofit2Utils;
import com.renyu.imagelibrary.commonutils.Utils;
import com.renyu.mt.MTApplication;
import com.renyu.mt.R;
import com.renyu.mt.adapter.ConversationAdapter;
import com.renyu.mt.base.BaseIMActivity;
import com.renyu.mt.impl.RetrofitImpl;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.dreamtobe.kpswitch.util.KPSwitchConflictUtil;
import cn.dreamtobe.kpswitch.util.KeyboardUtil;
import cn.dreamtobe.kpswitch.widget.KPSwitchPanelRelativeLayout;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by Administrator on 2017/7/24.
 */

public class ConversationActivity extends BaseIMActivity {

    @BindView(R.id.layout_nav_right)
    LinearLayout layout_nav_right;
    @BindView(R.id.rv_conversation)
    RecyclerView rv_conversation;
    ConversationAdapter adapter;
    @BindView(R.id.kp_panel_root)
    KPSwitchPanelRelativeLayout kp_panel_root;
    @BindView(R.id.iv_emoji)
    ImageView iv_emoji;
    @BindView(R.id.iv_sendvoice)
    ImageView iv_sendvoice;
    @BindView(R.id.edit_conversation)
    EditText edit_conversation;
    View layout_imagechoice;
    View layout_voicechoice;
    View layout_emojichoice;
    @BindView(R.id.layout_record)
    RelativeLayout layout_record;
    @BindView(R.id.iv_record)
    ImageView iv_record;
    @BindView(R.id.tv_record)
    TextView tv_record;

    ArrayList<MessageBean> messageBeens;

    BroadcastReceiver registerReceiver;

    // 当前用户信息
    UserInfoRsp currentUserInfo;
    // 聊天对方用户ID
    String chatUserId;

    // 页码
    int page=0;
    int pageSize=20;

    Vibrator mVibrator;
    RecordUtils recordTool;
    MediaPlayer mediaPlayer;

    @Override
    public void initParams() {
        TextView textView=new TextView(ConversationActivity.this);
        textView.setText("个人信息");
        textView.setOnClickListener(view -> {
            Intent intent=new Intent(ConversationActivity.this, UserDetailActivity.class);
            intent.putExtra("UserId", chatUserId);
            startActivity(intent);
        });
        layout_nav_right.addView(textView);

        currentUserInfo = (UserInfoRsp) ACache.get(this).getAsObject("UserInfoRsp");
        chatUserId = getIntent().getStringExtra("UserId");

        messageBeens=new ArrayList<>();
        ArrayList<MessageBean> temp=PlainTextDBHelper.getInstance().getConversationByUser(chatUserId, page, pageSize);
        page++;
        messageBeens.addAll(temp);

        rv_conversation.setHasFixedSize(true);
        LinearLayoutManager manager=new LinearLayoutManager(this);
        rv_conversation.setLayoutManager(manager);
        adapter=new ConversationAdapter(this, messageBeens, getIntent().getBooleanExtra("isGroup", false), chatUserId);
        rv_conversation.setAdapter(adapter);
        rv_conversation.post(new Runnable() {
            @Override
            public void run() {
                rv_conversation.scrollToPosition(messageBeens.size()-1);
                rv_conversation.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        // 上拉加载更多
                        boolean canScrollDown=rv_conversation.canScrollVertically(-1);
                        if (!canScrollDown) {
                            ArrayList<MessageBean> temp=PlainTextDBHelper.getInstance().getConversationByUser(chatUserId, page, pageSize);
                            page++;
                            messageBeens.addAll(0, temp);
                            adapter.notifyItemRangeInserted(0, temp.size());
                        }
                    }
                });
            }
        });
        layout_imagechoice=kp_panel_root.findViewById(R.id.layout_imagechoice);
        layout_voicechoice=kp_panel_root.findViewById(R.id.layout_voicechoice);
        final ImageView iv_record_click = layout_voicechoice.findViewById( R.id.iv_record_click );
        final ImageView iv_record_bg1 = layout_voicechoice.findViewById( R.id.iv_record_bg1 );
        final ImageView iv_record_bg2 = layout_voicechoice.findViewById( R.id.iv_record_bg2 );
        layout_voicechoice.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    iv_record_click.setBackgroundResource(R.mipmap.click_pressed);
                    iv_record_bg1.setBackgroundResource(R.mipmap.click_voice_pressed);
                    iv_record_bg2.setBackgroundResource(R.mipmap.click_voice_pressed);
                    mVibrator.vibrate(new long[]{ 100 , 50 }, -1);
                    layout_record.setVisibility(View.VISIBLE);
                    iv_record.setImageResource(R.mipmap.record_microphone);
                    tv_record.setText("取消");
                    recordTool.start();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (motionEvent.getY()<0) {
                        iv_record.setImageResource(R.mipmap.record_cancel);
                        tv_record.setText("松开手指，取消发送");
                    }
                    else {
                        iv_record.setImageResource(R.mipmap.record_microphone);
                        tv_record.setText("手指上滑，取消发送");
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    iv_record_click.setBackgroundResource(R.mipmap.click_normal);
                    iv_record_bg1.setBackgroundResource(R.mipmap.click_voice_normal);
                    iv_record_bg2.setBackgroundResource(R.mipmap.click_voice_normal);
                    layout_record.setVisibility(View.GONE);
                    if (motionEvent.getY()<0) {
                        recordTool.stopRecorder(true);
                    }
                    else {
                        recordTool.stopRecorder( false );
                        mVibrator.cancel();
                    }
                    break;
            }
            return true;
        });
        layout_emojichoice=kp_panel_root.findViewById(R.id.layout_emojichoice);
        KeyboardUtil.attach(this, kp_panel_root, isShowing -> {
            if (isShowing) {
                rv_conversation.scrollToPosition(messageBeens.size()-1);
            }
        });
        KPSwitchConflictUtil.attach(kp_panel_root, edit_conversation, switchToPanel -> {
            if (switchToPanel) {
                edit_conversation.clearFocus();
            } else {
                edit_conversation.requestFocus();
            }
            rv_conversation.scrollToPosition(messageBeens.size()-1);
        }, new KPSwitchConflictUtil.SubPanelAndTrigger(layout_emojichoice, iv_emoji)
                , new KPSwitchConflictUtil.SubPanelAndTrigger(layout_voicechoice, iv_sendvoice));
        edit_conversation.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                KPSwitchConflictUtil.showKeyboard(kp_panel_root, edit_conversation);
            }
            return false;
        });
        kp_panel_root.post(() -> {
            int height=KeyboardUtil.getKeyboardHeight(ConversationActivity.this);
            LinearLayout.LayoutParams params= (LinearLayout.LayoutParams) kp_panel_root.getLayoutParams();
            params.height=height;
            kp_panel_root.setLayoutParams(params);
        });

        // 震动功能初始化
        mVibrator = (Vibrator) getApplication().getSystemService(VIBRATOR_SERVICE);
        // 录音播放功能初始化，并设置自定义的监听事件
        recordTool = new RecordUtils();
        recordTool.setRecorderListener(path -> {
            File file=new File(path);
            if (file.exists()) {
                sendVoiceMessage(file);
            }
        });

        registerReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("MT")) {
                    BroadcastBean bean = (BroadcastBean) intent.getSerializableExtra("broadcast");
                    // 因为断线或者回收造成的登录成功
                    if (bean.getCommand() == BroadcastBean.MTCommand.LoginRsp) {
                        Log.d("MTAPP", "二次登录成功");
                        // 刷新远程数据
                        getOfflineIMFromRemote();
                    }
                    // 收到消息之后立即更新
                    if (bean.getCommand() == BroadcastBean.MTCommand.MessageReceive) {
                        MessageBean messageBean = (MessageBean) ((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable();
                        if (messageBean.getUserId().equals(chatUserId)) {
                            messageBeens.add(messageBean);
                            adapter.notifyItemInserted(messageBeens.size()-1);
                            if (!rv_conversation.canScrollVertically(1)) {
                                rv_conversation.scrollToPosition(messageBeens.size()-1);
                            }
                        }
                    }
                    // 当前用户发送消息之后刷新列表
                    if (bean.getCommand() == BroadcastBean.MTCommand.MessageSend) {
                        MessageBean messageBean = (MessageBean) ((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable();
                        messageBeens.add(messageBean);
                        adapter.notifyItemInserted(messageBeens.size()-1);
                        if (!rv_conversation.canScrollVertically(1)) {
                            rv_conversation.scrollToPosition(messageBeens.size()-1);
                        }
                    }
                    // 语音、图片上传完成之后刷新列表
                    if (bean.getCommand() == BroadcastBean.MTCommand.MessageUploadComp) {
                        // 语音、图片上传完成之后刷新列表
                        MessageBean temp = (MessageBean) ((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable();
                        for (MessageBean messageBeen : messageBeens) {
                            if (messageBeen.getSvrMsgId().equals(temp.getSvrMsgId())) {
                                messageBeen.setSync(Enums.Enable.ENABLE);
                                break;
                            }
                        }
                        ProgressBar pb=rv_conversation.findViewWithTag(temp.getSvrMsgId()+"_pb");
                        if (pb!=null) {
                            pb.setVisibility(View.GONE);
                        }
                    }
                    // 语音、图片上传失败之后刷新列表
                    if (bean.getCommand() == BroadcastBean.MTCommand.MessageUploadFail) {
                        MessageBean temp = (MessageBean) ((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable();
                        for (MessageBean messageBeen : messageBeens) {
                            if (messageBeen.getSvrMsgId().equals(temp.getSvrMsgId())) {
                                messageBeen.setSync(Enums.Enable.ENABLE);
                                messageBeen.setResend(Enums.Enable.ENABLE);
                                break;
                            }
                        }
                        ProgressBar pb=rv_conversation.findViewWithTag(temp.getSvrMsgId()+"_pb");
                        if (pb!=null) {
                            pb.setVisibility(View.GONE);
                        }
                        ImageView imageView=rv_conversation.findViewWithTag(temp.getSvrMsgId()+"_status");
                        if (imageView!=null) {
                            imageView.setVisibility(View.VISIBLE);
                        }
                    }
                    // 接口同步的文件如果下载完成，则刷新列表
                    if (bean.getCommand() == BroadcastBean.MTCommand.MessageDownloadComp) {
                        FileInfoBean fileInfoBean = (FileInfoBean) ((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable();
                        TextView textView = rv_conversation.findViewWithTag(fileInfoBean.getSvrMsgId()+"_length");
                        if (textView!=null) {
                            textView.setText(fileInfoBean.getFileSize()/1600+"\'\'");
                        }
                    }
                }
            }
        };

        openCurrentReceiver();
    }

    @Override
    public int initViews() {
        return R.layout.activity_conversation;
    }

    @Override
    public void loadData() {
        getOfflineIMFromRemote();
    }

    @Override
    public int setStatusBarColor() {
        return Color.BLACK;
    }

    @Override
    public int setStatusBarTranslucent() {
        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        closeCurrentReceiver();

        // 设置当前会话列表消息均已读
        PlainTextDBHelper.getInstance().updateRead(chatUserId);
        BroadcastBean.sendBroadcast(this, BroadcastBean.MTCommand.UpdateRead, chatUserId);

        // 关闭音频播放
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @OnClick({R.id.btn_send_conversation, R.id.iv_image})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send_conversation:
                sendTextMessage();
                break;
            case R.id.iv_image:
                Utils.choicePic(ConversationActivity.this, 1, 1000);
                break;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        //点击返回键的时候，如果面板开着，则隐藏面板
        if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (kp_panel_root.getVisibility() != View.GONE) {
                KPSwitchConflictUtil.hidePanelAndKeyboard(kp_panel_root);
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * 获取远程数据
     */
    private void getOfflineIMFromRemote() {
        Log.d("MTAPP", "获取会话详情数据");
        Observable<IMBaseResponseList<OfflineIMDetailResponse>> observable = null;
        if (messageBeens.size()>0) {
            // 对方发来的消息是最后一条消息
            if (messageBeens.get(messageBeens.size()-1).getIsSend().equals("0")) {
                observable = retrofit.create(RetrofitImpl.class).getOfflineIMDetailList(chatUserId, currentUserInfo.getUserId(), messageBeens.get(messageBeens.size()-1).getTimestamp());
            }
            // 自己发出的消息是最后一条消息
            else if (messageBeens.get(messageBeens.size()-1).getIsSend().equals("1")) {
                observable = retrofit.create(RetrofitImpl.class).getOfflineIMDetailList(currentUserInfo.getUserId(), chatUserId, messageBeens.get(messageBeens.size()-1).getTimestamp());
            }
        }
        else {
            observable = retrofit.create(RetrofitImpl.class).getOfflineIMDetailList(chatUserId, currentUserInfo.getUserId(), 0);
        }
        observable.compose(Retrofit2Utils.backgroundList())
                .subscribe(new Observer<List<OfflineIMDetailResponse>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<OfflineIMDetailResponse> offlineIMDetailResponses) {
                       if (offlineIMDetailResponses.size()>0) {
                           // 第一条消息需要过滤，他是当前最后一条消息
                           if (messageBeens.size() != 0) {
                               offlineIMDetailResponses.remove(0);
                           }
                           // 全部离线消息
                           ArrayList<MessageBean> offlineMessages=new ArrayList<>();
                           ArrayList<OfflineIMDetailResponse> temp = PlainTextDBHelper.getInstance().checkOfflineMessages(offlineIMDetailResponses);
                           for (OfflineIMDetailResponse offlineIMDetailRespons : temp) {
                               MessageBean bean=new MessageBean();
                               bean.setTimestamp(offlineIMDetailRespons.getAddTime());
                               bean.setFromSvrMsgId(offlineIMDetailRespons.getSvrMsgId());
                               bean.setMsg(offlineIMDetailRespons.getMsg());
                               bean.setMsgMeta(offlineIMDetailRespons.getMsgMeta());
                               if (offlineIMDetailRespons.getMessageType() == 8 ||
                                       offlineIMDetailRespons.getMessageType() == 7) {
                                   bean.setMsgType(Enums.MessageType.MULTI_MEDIA);
                                   try {
                                       JSONObject object = new JSONObject(offlineIMDetailRespons.getMsgMeta());
                                       if (offlineIMDetailRespons.getMessageType() == 8) {
                                           if (object.has(MessageMeta.picid) && !"".equals(object.getString(MessageMeta.picid).trim())
                                                   && object.getString(MessageMeta.picid) != null) {
                                               // 添加媒体消息的fileId
                                               JSONObject pidObject = new JSONObject(object.getString(MessageMeta.picid));
                                               String localfilename = "";
                                               for (int i = 0; i < object.getInt(MessageMeta.piccount); i++) {
                                                   if (pidObject.has(String.valueOf(i))) {
                                                       String fileId = pidObject.getString(String.valueOf(i));
                                                       localfilename = fileId;
                                                   }
                                               }
                                               bean.setLocalFileName(localfilename);
                                           }
                                       }
                                       else if (offlineIMDetailRespons.getMessageType() == 7) {
                                           if (object.has(MessageMeta.picid) && !"".equals(object.getString(MessageMeta.picid).trim())
                                                   && object.getString(MessageMeta.picid) != null) {
                                               // 添加媒体消息的fileId
                                               JSONObject pidObject = new JSONObject(object.getString(MessageMeta.picid));
                                               String localfilename = "";
                                               if (pidObject.has(String.valueOf(1))) {
                                                   String fileId = pidObject.getString(String.valueOf(1));
                                                   localfilename = fileId;
                                               }
                                               bean.setLocalFileName(localfilename);
                                           }
                                       }
                                   } catch (JSONException e) {
                                       e.printStackTrace();
                                   }
                               }
                               else {
                                   bean.setMsgType(Enums.MessageType.TEXT);
                               }
                               bean.setMessageType(""+offlineIMDetailRespons.getMessageType());
                               bean.setResend(Enums.Enable.DISABLE);
                               bean.setSvrMsgId(offlineIMDetailRespons.getSvrMsgId());
                               bean.setSync(Enums.Enable.ENABLE);
                               // 自己是发送方
                               if (offlineIMDetailRespons.getFromUserId().equals(currentUserInfo.getUserId())) {
                                   bean.setUserId(offlineIMDetailRespons.getToUserId());
                                   bean.setIsSend("1");
                               }
                               else {
                                   bean.setUserId(offlineIMDetailRespons.getFromUserId());
                                   bean.setIsSend("0");
                               }
                               bean.setIsRead("1");
                               bean.setIsVoicePlay("1");
                               offlineMessages.add(bean);

                               if (bean.getMessageType().equals("7")) {
                                   String token = currentUserInfo.getToken();
                                   String fileId = bean.getLocalFileName();
                                   StringBuilder sb = new StringBuilder(FusionField.downloadUrl);
                                   sb.append("fileid=").append(fileId).append("&type=").append("voice").append("&token=").append(token);
                                   // 单纯下载文件
                                   DownloadUtils.addFile(ConversationActivity.this.getApplicationContext(), sb.toString(), fileId, bean.getSvrMsgId());
                               }
                           }
                           // 更新数据库，回到第一页
                           if (offlineMessages.size() != 0) {
                               PlainTextDBHelper.getInstance().insertMessages(offlineMessages);
                               page = 1;
                               ArrayList<MessageBean> temp1=PlainTextDBHelper.getInstance().getConversationByUser(chatUserId, 0, pageSize);
                               messageBeens.clear();
                               messageBeens.addAll(temp1);
                               adapter.notifyDataSetChanged();
                               rv_conversation.scrollToPosition(messageBeens.size()-1);
                           }
                       }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void sendTextMessage() {
        if (((MTApplication) getApplication()).connState != BroadcastBean.MTCommand.Conn) {
            Toast.makeText(this, "服务器未连接成功", Toast.LENGTH_SHORT).show();
            return;
        }
        MessageBean messageBean=new MessageBean();
        messageBean.setMsg(edit_conversation.getText().toString());
        messageBean.setMsgMeta("");
        messageBean.setMsgType(Enums.MessageType.TEXT);
        messageBean.setUserId(chatUserId);
        messageBean.setIsSend("1");
        messageBean.setTimestamp(System.currentTimeMillis());
        messageBean.setSvrMsgId(MD5Utils.getMD5String(""+System.currentTimeMillis()));
        messageBean.setFromSvrMsgId("");
        messageBean.setSync(Enums.Enable.ENABLE);
        messageBean.setResend(Enums.Enable.DISABLE);
        messageBean.setMessageType("0");
        messageBean.setLocalFileName("");
        messageBean.setIsRead("1");
        PlainTextDBHelper.getInstance().insertMessage(messageBean);
        BroadcastBean.sendBroadcast(this, BroadcastBean.MTCommand.MessageSend, messageBean);

        // 发送消息
        MTService.sendTextMessage(this, chatUserId, edit_conversation.getText().toString(), currentUserInfo.getUserName());
        edit_conversation.setText("");
    }

    private void sendPicMessage(File file) {
        if (((MTApplication) getApplication()).connState != BroadcastBean.MTCommand.Conn) {
            Toast.makeText(this, "服务器未连接成功", Toast.LENGTH_SHORT).show();
            return;
        }
        MessageBean messageBean=new MessageBean();
        messageBean.setMsg("");
        messageBean.setMsgMeta("");
        messageBean.setMsgType(Enums.MessageType.MULTI_MEDIA);
        messageBean.setIsSend("1");
        messageBean.setUserId(chatUserId);
        messageBean.setTimestamp(System.currentTimeMillis());
        messageBean.setSvrMsgId(MD5Utils.getMD5String(""+System.currentTimeMillis()));
        messageBean.setFromSvrMsgId("");
        messageBean.setSync(Enums.Enable.DISABLE);
        messageBean.setResend(Enums.Enable.DISABLE);
        messageBean.setMessageType("8");
        messageBean.setLocalFileName(file.getPath());
        messageBean.setIsRead("1");
        PlainTextDBHelper.getInstance().insertMessage(messageBean);
        BroadcastBean.sendBroadcast(this, BroadcastBean.MTCommand.MessageSend, messageBean);

        // 发送消息
        MTService.sendPicMessage(this, chatUserId, file.getPath(), currentUserInfo.getUserName(), messageBean);
    }

    private void sendVoiceMessage(File file) {
        if (((MTApplication) getApplication()).connState != BroadcastBean.MTCommand.Conn) {
            Toast.makeText(this, "服务器未连接成功", Toast.LENGTH_SHORT).show();
            return;
        }
        MessageBean messageBean=new MessageBean();
        messageBean.setMsg("");
        messageBean.setMsgMeta("");
        messageBean.setMsgType(Enums.MessageType.MULTI_MEDIA);
        messageBean.setIsSend("1");
        messageBean.setUserId(chatUserId);
        messageBean.setTimestamp(System.currentTimeMillis());
        messageBean.setSvrMsgId(MD5Utils.getMD5String(""+System.currentTimeMillis()));
        messageBean.setFromSvrMsgId("");
        messageBean.setSync(Enums.Enable.DISABLE);
        messageBean.setResend(Enums.Enable.DISABLE);
        messageBean.setMessageType("7");
        messageBean.setLocalFileName(file.getPath());
        messageBean.setIsRead("1");
        messageBean.setIsVoicePlay("1");
        PlainTextDBHelper.getInstance().insertMessage(messageBean);
        BroadcastBean.sendBroadcast(this, BroadcastBean.MTCommand.MessageSend, messageBean);

        // 发送消息
        MTService.sendVoiceMessage(this, chatUserId, file.getPath(), currentUserInfo.getUserName(), messageBean);
    }

    /**
     * 重新发送语音消息
     * @param messageBean
     */
    public void resendVoiceMessageState(MessageBean messageBean) {
        if (((MTApplication) getApplication()).connState != BroadcastBean.MTCommand.Conn) {
            Toast.makeText(this, "服务器未连接成功", Toast.LENGTH_SHORT).show();
            return;
        }
        // 修改数据库状态
        PlainTextDBHelper.getInstance().updateSendState(messageBean.getSvrMsgId(), Enums.Enable.DISABLE, Enums.Enable.DISABLE);
        // 修改列表状态并刷新
        messageBean.setSync(Enums.Enable.DISABLE);
        messageBean.setResend(Enums.Enable.DISABLE);
        adapter.notifyDataSetChanged();

        // 发送消息
        MTService.sendVoiceMessage(this, chatUserId, new File(messageBean.getLocalFileName()).getPath(), currentUserInfo.getUserName(), messageBean);
    }

    /**
     * 重新发送图片消息
     * @param messageBean
     */
    public void resendPicMessage(MessageBean messageBean) {
        if (((MTApplication) getApplication()).connState != BroadcastBean.MTCommand.Conn) {
            Toast.makeText(this, "服务器未连接成功", Toast.LENGTH_SHORT).show();
            return;
        }
        // 修改数据库状态
        PlainTextDBHelper.getInstance().updateSendState(messageBean.getSvrMsgId(), Enums.Enable.DISABLE, Enums.Enable.DISABLE);
        // 修改列表状态并刷新
        messageBean.setSync(Enums.Enable.DISABLE);
        messageBean.setResend(Enums.Enable.DISABLE);
        adapter.notifyDataSetChanged();

        // 发送消息
        MTService.sendPicMessage(this, chatUserId, new File(messageBean.getLocalFileName()).getPath(), currentUserInfo.getUserName(), messageBean);
    }

    /**
     * 播放音频文件
     * @param fileName
     * @param tag
     */
    public void playMedia(String fileName, String tag) {
        if (!new File(fileName).exists()) {
            return;
        }
        recycleMedia();
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(fileName);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if (adapter.mediaPlayerTag!=null && rv_conversation.findViewWithTag(adapter.mediaPlayerTag)!=null) {
                        ((ImageView) rv_conversation.findViewWithTag(adapter.mediaPlayerTag)).setImageResource(R.mipmap.ease_chatto_voice_playing);
                        adapter.mediaPlayerTag=null;
                    }
                    else {
                        adapter.mediaPlayerTag=null;
                        adapter.notifyDataSetChanged();
                    }
                }
            });
            mediaPlayer.prepare();
            mediaPlayer.start();
            adapter.mediaPlayerTag=tag;
            if (rv_conversation.findViewWithTag(adapter.mediaPlayerTag)!=null) {
                ((ImageView) rv_conversation.findViewWithTag(adapter.mediaPlayerTag)).setImageResource(R.mipmap.ic_launcher);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止音频文件
     */
    public void recycleMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (adapter.mediaPlayerTag!=null && rv_conversation.findViewWithTag(adapter.mediaPlayerTag)!=null) {
            adapter.mediaPlayerTag=null;
            ((ImageView) rv_conversation.findViewWithTag(adapter.mediaPlayerTag)).setImageResource(R.mipmap.ease_chatto_voice_playing);
        }
        else {
            adapter.mediaPlayerTag=null;
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1000 && resultCode==RESULT_OK) {
            ArrayList<String> temp=data.getExtras().getStringArrayList("choiceImages");
            File file=null;
            if (temp.size()>0) {
                file=new File(temp.get(0));
            }
            if (file!=null) {
                sendPicMessage(file);
            }
        }
    }

    @Nullable
    @Override
    public BroadcastReceiver getReceiver() {
        return registerReceiver;
    }
}
