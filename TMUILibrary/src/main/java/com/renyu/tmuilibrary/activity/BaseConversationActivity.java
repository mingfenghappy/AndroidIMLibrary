package com.renyu.tmuilibrary.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.Utils;
import com.focustech.common.MD5Utils;
import com.focustech.dbhelper.PlainTextDBHelper;
import com.focustech.message.model.BroadcastBean;
import com.focustech.message.model.FileInfoBean;
import com.focustech.message.model.MessageBean;
import com.focustech.message.model.OfflineIMDetailResponse;
import com.focustech.message.model.SystemMessageBean;
import com.focustech.message.model.UserInfoRsp;
import com.focustech.message.params.MessageMeta;
import com.focustech.params.FusionField;
import com.focustech.tm.open.sdk.messages.protobuf.Enums;
import com.renyu.commonlibrary.commonutils.ACache;
import com.renyu.commonlibrary.network.Retrofit2Utils;
import com.renyu.commonlibrary.params.InitParams;
import com.renyu.tmbaseuilibrary.app.MTApplication;
import com.renyu.tmbaseuilibrary.base.BaseIMActivity;
import com.renyu.tmbaseuilibrary.params.CommonParams;
import com.renyu.tmbaseuilibrary.service.MTService;
import com.renyu.tmbaseuilibrary.utils.DetectDelEventEditText;
import com.renyu.tmbaseuilibrary.utils.DownloadUtils;
import com.renyu.tmbaseuilibrary.utils.FaceIconUtil;
import com.renyu.tmuilibrary.R;
import com.renyu.tmuilibrary.adapter.ConversationAdapter;
import com.renyu.tmuilibrary.adapter.FaceAdapter;
import com.renyu.tmuilibrary.impl.IMBaseResponseList;
import com.renyu.tmuilibrary.impl.RetrofitImpl;
import com.renyu.tmuilibrary.view.VoiceRecorderView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.dreamtobe.kpswitch.util.KPSwitchConflictUtil;
import cn.dreamtobe.kpswitch.util.KeyboardUtil;
import cn.dreamtobe.kpswitch.widget.KPSwitchPanelRelativeLayout;
import id.zelory.compressor.Compressor;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by Administrator on 2017/7/24.
 */

public class BaseConversationActivity extends BaseIMActivity {

    RecyclerView rv_conversation;
    ConversationAdapter adapter;
    KPSwitchPanelRelativeLayout kp_panel_root;
    ImageView iv_emoji;
    ImageView iv_sendvoice;
    DetectDelEventEditText edit_conversation;
    View layout_voicechoice;
    View layout_emojichoice;
    RecyclerView rv_panel_content;
    FaceAdapter faceAdapter;
    VoiceRecorderView layout_record;

    ArrayList<MessageBean> messageBeens;

    // 当前用户信息
    public UserInfoRsp currentUserInfo;
    // 聊天对方用户ID
    public String chatUserId;
    public String chatUserHeadId;
    public String chatUserNickName;
    public int chatUserHeadType;

    // 页码
    int page=0;
    int pageSize=20;

    MediaPlayer mediaPlayer;

    // 语音动画相关
    AnimationDrawable voiceAnimation=null;
    boolean isSend = false;

    // 是否已经收起
    boolean isExecuteCollapse = false;

    @Override
    public void initParams() {
        // 存在回收之后再次回收，造成下线标志位出错
        if (checkNullInfo()) {
            return;
        }

        // 构建聊天用户数据
        currentUserInfo = (UserInfoRsp) ACache.get(this).getAsObject("UserInfoRsp");
        chatUserId = getIntent().getStringExtra("UserId");
        chatUserHeadId = getIntent().getStringExtra("UserHeadId");
        chatUserNickName = getIntent().getStringExtra("UserNickName");
        chatUserHeadType = getIntent().getIntExtra("UserHeadType", 0);
        if (chatUserHeadId.equals("") || chatUserNickName.equals("") || chatUserHeadType == 0) {
            UserInfoRsp tempUserInfoRsp = PlainTextDBHelper.getInstance(Utils.getApp()).getFriendsInfo().get(chatUserId);
            if (tempUserInfoRsp != null && chatUserHeadId.equals("")) {
                chatUserHeadId = tempUserInfoRsp.getUserHeadId();
            }
            if (tempUserInfoRsp != null && chatUserNickName.equals("")) {
                chatUserNickName = tempUserInfoRsp.getUserNickName();
            }
            if (tempUserInfoRsp != null && chatUserHeadType == 0) {
                chatUserHeadType = tempUserInfoRsp.getUserHeadType().getNumber();
            }
        }

        messageBeens=new ArrayList<>();
        ArrayList<MessageBean> temp=PlainTextDBHelper.getInstance(Utils.getApp()).getConversationByUser(chatUserId, page, pageSize);
        page++;
        messageBeens.addAll(temp);

        rv_conversation = findViewById(R.id.rv_conversation);
        rv_conversation.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                isExecuteCollapse = false;
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                if (!isExecuteCollapse) {
                    isExecuteCollapse = true;
                    KPSwitchConflictUtil.hidePanelAndKeyboard(kp_panel_root);
                }
            }
            return false;
        });
        rv_conversation.setHasFixedSize(true);
        LinearLayoutManager manager=new LinearLayoutManager(this);
        rv_conversation.setLayoutManager(manager);
        adapter=new ConversationAdapter(this, messageBeens, getIntent().getBooleanExtra("isGroup", false),
                chatUserId, chatUserHeadId, chatUserNickName, chatUserHeadType);
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
                            ArrayList<MessageBean> temp=PlainTextDBHelper.getInstance(Utils.getApp()).getConversationByUser(chatUserId, page, pageSize);
                            page++;
                            messageBeens.addAll(0, temp);
                            adapter.notifyItemRangeInserted(0, temp.size());
                        }
                    }
                });
            }
        });

        kp_panel_root = findViewById(R.id.kp_panel_root);
        iv_emoji = findViewById(R.id.iv_emoji);
        iv_sendvoice = findViewById(R.id.iv_sendvoice);
        edit_conversation = findViewById(R.id.edit_conversation);
        edit_conversation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString())) {
                    findViewById(R.id.btn_send_conversation).setBackgroundColor(Color.parseColor("#dddee2"));
                }
                else {
                    findViewById(R.id.btn_send_conversation).setBackgroundColor(Color.parseColor("#0099ff"));
                }
            }
        });
        edit_conversation.setDelListener(() -> {
            // 点击删除键
            String value = edit_conversation.getText().toString();
            if (value.equals("")) {
                return true;
            }
            int end = value.lastIndexOf("]");
            // 如果最后一个字符是]，则进入表情判断
            if (end == value.length()-1) {
                int start = value.lastIndexOf("[");
                String temp_ = value.substring(start+1, end);
                String[] faceids = getResources().getStringArray(R.array.faceid);
                boolean isEmoji = false;
                for (String faceid : faceids) {
                    if (faceid.equals(temp_)) {
                        isEmoji = true;
                        break;
                    }
                }
                if (isEmoji) {
                    edit_conversation.getText().delete(value.length()-(end+1-start), value.length());
                    edit_conversation.setSelection(value.length()-(end+1-start));
                    return true;
                }
            }
            edit_conversation.getText().delete(value.length()-1, value.length());
            edit_conversation.setSelection(value.length()-1);
            return true;
        });
        layout_voicechoice=kp_panel_root.findViewById(R.id.layout_voicechoice);
        layout_record = findViewById(R.id.layout_record);
        layout_voicechoice.setOnTouchListener((v, event) -> {
            // 关闭音频播放
            recycleMedia();
            // 关闭动画
            stopVoicePlayAnimation(isSend);
            return layout_record.onPressToSpeakBtnTouch(v, event,
                    (voiceFilePath, voiceTimeLength) -> {
                        File file=new File(voiceFilePath);
                        if (file.exists()) {
                            sendVoiceMessage(file);
                        }
                    });
        });
        layout_emojichoice=kp_panel_root.findViewById(R.id.layout_emojichoice);
        faceAdapter = new FaceAdapter(this, (value, res) -> {
            int currentPosition = edit_conversation.getSelectionStart();
            edit_conversation.getText().insert(currentPosition, FaceIconUtil.getInstance().getEmojiSpannableString(value, res));
        });
        rv_panel_content = layout_emojichoice.findViewById(R.id.rv_panel_content);
        rv_panel_content.setHasFixedSize(true);
        rv_panel_content.setLayoutManager(new GridLayoutManager(this, 7));
        rv_panel_content.setAdapter(faceAdapter);
        KeyboardUtil.attach(this, kp_panel_root, isShowing -> {
            if (isShowing) {
                rv_conversation.scrollToPosition(messageBeens.size()-1);
            }
        });
        KPSwitchConflictUtil.attach(kp_panel_root, edit_conversation, switchToPanel -> {
            if (switchToPanel) {
                edit_conversation.clearFocus();
                rv_conversation.scrollToPosition(messageBeens.size()-1);
            } else {
                edit_conversation.requestFocus();
            }
        }, new KPSwitchConflictUtil.SubPanelAndTrigger(layout_emojichoice, iv_emoji)
                , new KPSwitchConflictUtil.SubPanelAndTrigger(layout_voicechoice, iv_sendvoice));
        edit_conversation.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                KPSwitchConflictUtil.showKeyboard(kp_panel_root, edit_conversation);
            }
            return false;
        });
        kp_panel_root.post(() -> {
            int height=KeyboardUtil.getKeyboardHeight(BaseConversationActivity.this);
            LinearLayout.LayoutParams params= (LinearLayout.LayoutParams) kp_panel_root.getLayoutParams();
            params.height=height;
            kp_panel_root.setLayoutParams(params);
        });
        findViewById(R.id.btn_send_conversation).setOnClickListener(v -> sendTextMessage());
        findViewById(R.id.iv_image).setOnClickListener(v -> com.renyu.imagelibrary.commonutils.Utils.choicePic(BaseConversationActivity.this, 1, 1000));
        findViewById(R.id.iv_camera).setOnClickListener( v -> com.renyu.imagelibrary.commonutils.Utils.takePicture(this, 1001));

        receiver=new BroadcastReceiver() {
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
                        // 过滤掉系统消息
                        if (!((((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable()) instanceof SystemMessageBean)) {
                            MessageBean messageBean = (MessageBean) ((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable();
                            if (messageBean.getUserId().equals(chatUserId)) {
                                messageBeens.add(messageBean);
                                adapter.notifyItemInserted(messageBeens.size()-1);
                                if (!rv_conversation.canScrollVertically(1)) {
                                    rv_conversation.scrollToPosition(messageBeens.size()-1);
                                }
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
                    // 被踢下线
                    if (bean.getCommand() == BroadcastBean.MTCommand.Kickout) {
                        CommonParams.isKickout = true;
                        if (!isPause) {
                            kickout();
                        }
                    }
                }
            }
        };

        openCurrentReceiver();
    }

    @Override
    public int initViews() {
        return R.layout.activity_base_conversation;
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
        PlainTextDBHelper.getInstance(Utils.getApp()).updateRead(chatUserId);
        BroadcastBean.sendBroadcast(this, BroadcastBean.MTCommand.UpdateRead, chatUserId);

        // 关闭音频播放
        recycleMedia();
        // 关闭动画
        stopVoicePlayAnimation(isSend);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // 点击返回键的时候，如果面板开着，则隐藏面板
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
                           ArrayList<OfflineIMDetailResponse> temp = PlainTextDBHelper.getInstance(Utils.getApp()).checkOfflineMessages(offlineIMDetailResponses);
                           for (OfflineIMDetailResponse offlineIMDetailRespons : temp) {
                               MessageBean bean=new MessageBean();
                               bean.setTimestamp(offlineIMDetailRespons.getAddTime());
                               bean.setMsg(offlineIMDetailRespons.getMsg());
                               bean.setMsgMeta(offlineIMDetailRespons.getMsgMeta());
                               if (MessageBean.getMessageTypeForConversation(offlineIMDetailRespons) == 7 ||
                                       MessageBean.getMessageTypeForConversation(offlineIMDetailRespons) == 8) {
                                   bean.setMsgType(Enums.MessageType.MULTI_MEDIA);
                                   try {
                                       JSONObject object = new JSONObject(offlineIMDetailRespons.getMsgMeta());
                                       if (MessageBean.getMessageTypeForConversation(offlineIMDetailRespons) == 8) {
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
                                       else if (MessageBean.getMessageTypeForConversation(offlineIMDetailRespons) == 7) {
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
                               bean.setMessageType(""+MessageBean.getMessageTypeForConversation(offlineIMDetailRespons));
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
                                   DownloadUtils.addFile(BaseConversationActivity.this.getApplicationContext(), sb.toString(), fileId, bean.getSvrMsgId());
                               }
                           }
                           // 更新数据库，回到第一页
                           if (offlineMessages.size() != 0) {
                               PlainTextDBHelper.getInstance(Utils.getApp()).insertMessages(offlineMessages);
                               page = 1;
                               ArrayList<MessageBean> temp1=PlainTextDBHelper.getInstance(Utils.getApp()).getConversationByUser(chatUserId, 0, pageSize);
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
        if (TextUtils.isEmpty(edit_conversation.getText().toString())) {
            return;
        }
        if (((MTApplication) getApplication()).connState != BroadcastBean.MTCommand.Conn) {
            Toast.makeText(this, "服务器未连接成功", Toast.LENGTH_SHORT).show();
            return;
        }
        MessageBean messageBean=new MessageBean();
        messageBean.setMsg(FaceIconUtil.getInstance().replaceAddBounce(edit_conversation.getText().toString()));
        messageBean.setMsgMeta("");
        messageBean.setMsgType(Enums.MessageType.TEXT);
        messageBean.setUserId(chatUserId);
        messageBean.setIsSend("1");
        messageBean.setTimestamp(System.currentTimeMillis());
        messageBean.setSvrMsgId(MD5Utils.getMD5String(""+System.currentTimeMillis()));
        messageBean.setSync(Enums.Enable.ENABLE);
        messageBean.setResend(Enums.Enable.DISABLE);
        messageBean.setMessageType("0");
        messageBean.setLocalFileName("");
        messageBean.setIsRead("1");
        PlainTextDBHelper.getInstance(Utils.getApp()).insertMessage(messageBean);
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
        messageBean.setSync(Enums.Enable.DISABLE);
        messageBean.setResend(Enums.Enable.DISABLE);
        messageBean.setMessageType("8");
        messageBean.setLocalFileName(file.getPath());
        messageBean.setIsRead("1");
        PlainTextDBHelper.getInstance(Utils.getApp()).insertMessage(messageBean);
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
        messageBean.setSync(Enums.Enable.DISABLE);
        messageBean.setResend(Enums.Enable.DISABLE);
        messageBean.setMessageType("7");
        messageBean.setLocalFileName(file.getPath());
        messageBean.setIsRead("1");
        messageBean.setIsVoicePlay("1");
        PlainTextDBHelper.getInstance(Utils.getApp()).insertMessage(messageBean);
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
        PlainTextDBHelper.getInstance(Utils.getApp()).updateSendState(messageBean.getSvrMsgId(), Enums.Enable.DISABLE, Enums.Enable.DISABLE);
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
        PlainTextDBHelper.getInstance(Utils.getApp()).updateSendState(messageBean.getSvrMsgId(), Enums.Enable.DISABLE, Enums.Enable.DISABLE);
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
    public void playMedia(String fileName, String tag, boolean isSend) {
        if (!new File(fileName).exists()) {
            return;
        }
        // 停止上一个语音播放
        stopVoicePlayAnimation(BaseConversationActivity.this.isSend);
        BaseConversationActivity.this.isSend = isSend;
        recycleMedia();
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(fileName);
            mediaPlayer.setOnCompletionListener(mediaPlayer -> {
                if (adapter.mediaPlayerTag!=null && rv_conversation.findViewWithTag(adapter.mediaPlayerTag)!=null) {
                    stopVoicePlayAnimation(isSend);
                    adapter.mediaPlayerTag=null;
                }
                else {
                    adapter.mediaPlayerTag=null;
                    adapter.notifyDataSetChanged();
                }
            });
            mediaPlayer.prepare();
            mediaPlayer.start();
            adapter.mediaPlayerTag=tag;
            // 设置语音播放动画
            if (rv_conversation.findViewWithTag(adapter.mediaPlayerTag)!=null) {
                startVoicePlayAnimation(rv_conversation.findViewWithTag(adapter.mediaPlayerTag), isSend);
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
            File cropFile = compress(file);
            if (cropFile!=null) {
                sendPicMessage(cropFile);
            }
        }
        else if (requestCode == 1001 && resultCode == RESULT_OK) {
            File cropFile = compress(new File(data.getExtras().getString("path")));
            if (cropFile!=null) {
                sendPicMessage(cropFile);
            }
        }
    }

    /**
     * 播放语音
     * @param isSend
     */
    public void startVoicePlayAnimation(ImageView voiceImageView, boolean isSend) {
        if (!isSend) {
            voiceImageView.setImageResource(R.drawable.voice_from_icon);
        } else {
            voiceImageView.setImageResource(R.drawable.voice_to_icon);
        }
        voiceAnimation = (AnimationDrawable) voiceImageView.getDrawable();
        voiceAnimation.start();
    }

    /**
     * 关闭语音
     * @param isSend
     */
    public void stopVoicePlayAnimation(boolean isSend) {
        if (voiceAnimation != null) {
            voiceAnimation.stop();
        }
        if (!TextUtils.isEmpty(adapter.mediaPlayerTag) && rv_conversation.findViewWithTag(adapter.mediaPlayerTag) != null) {
            if (!isSend) {
                ((ImageView) rv_conversation.findViewWithTag(adapter.mediaPlayerTag)).setImageResource(R.mipmap.ease_chatfrom_voice_playing);
            } else {
                ((ImageView) rv_conversation.findViewWithTag(adapter.mediaPlayerTag)).setImageResource(R.mipmap.ease_chatto_voice_playing);
            }
        }
    }

    private File compress(File file) {
        File cropFile = null;
        try {
            cropFile = new Compressor(getApplicationContext())
                    .setMaxWidth(480)
                    .setMaxHeight(800)
                    .setQuality(80)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .setDestinationDirectoryPath(InitParams.CACHE_PATH)
                    .compressToFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cropFile;
    }
}
