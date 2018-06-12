package com.renyu.easemobuilibrary.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.blankj.utilcode.util.Utils;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.exceptions.HyphenateException;
import com.renyu.commonlibrary.params.InitParams;
import com.renyu.easemoblibrary.manager.EMMessageManager;
import com.renyu.easemoblibrary.model.BroadcastBean;
import com.renyu.easemobuilibrary.R;
import com.renyu.easemobuilibrary.adapter.ConversationAdapter;
import com.renyu.easemobuilibrary.base.BaseIMActivity;
import com.renyu.easemobuilibrary.params.CommonParams;
import com.renyu.easemobuilibrary.view.DetectDelEventEditText;
import com.renyu.easemobuilibrary.view.VoiceRecorderView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.dreamtobe.kpswitch.util.KPSwitchConflictUtil;
import cn.dreamtobe.kpswitch.util.KeyboardUtil;
import cn.dreamtobe.kpswitch.widget.KPSwitchPanelRelativeLayout;
import id.zelory.compressor.Compressor;

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
    VoiceRecorderView layout_record;

    ArrayList<EMMessage> messageBeens;

    // 聊天对方用户ID
    public String chatUserId;

    MediaPlayer mediaPlayer;

    int pagesize = 20;

    // 语音动画相关
    AnimationDrawable voiceAnimation=null;
    boolean isSend = false;

    // 是否已经收起
    boolean isExecuteCollapse = false;

    @Override
    public void initParams() {
        chatUserId = getIntent().getStringExtra("UserId");

        messageBeens=new ArrayList<>();

        // 初始化第一页消息
        initMessages();

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
        adapter=new ConversationAdapter(this, messageBeens, getIntent().getBooleanExtra("isGroup", false));
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
                            loadMoreLocalMessage();
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

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(actionName)) {
                    // 收到消息刷新列表
                    if (intent.getSerializableExtra(BroadcastBean.COMMAND) == BroadcastBean.EaseMobCommand.MessageReceive) {
                        EMMessage emMessage = intent.getParcelableExtra(BroadcastBean.PARCELABLE);
                        if (emMessage.getFrom().equals(chatUserId)) {
                            // 发送已读回执
                            EMMessageManager.sendAckMessage(emMessage);

                            // 设置当前聊天人的消息已读
                            EMMessageManager.markAllMessagesAsRead(chatUserId);
                            BroadcastBean.sendBroadcast(context, BroadcastBean.EaseMobCommand.UpdateRead);

                            messageBeens.add(emMessage);
                            adapter.notifyItemInserted(messageBeens.size()-1);
                            if (!rv_conversation.canScrollVertically(1)) {
                                rv_conversation.scrollToPosition(messageBeens.size()-1);
                            }
                        }
                    }
                    // 消息发送成功或失败刷新列表
                    if (intent.getSerializableExtra(BroadcastBean.COMMAND) == BroadcastBean.EaseMobCommand.MessageSend) {
                        EMMessage emMessage = intent.getParcelableExtra(BroadcastBean.PARCELABLE);
                        if (emMessage.status() == EMMessage.Status.SUCCESS) {
                            try {
                                // 来自音视频通话
                                if (emMessage.getBooleanAttribute("is_video_call") || emMessage.getBooleanAttribute("is_voice_call")) {
                                    messageBeens.add(emMessage);
                                    adapter.notifyItemInserted(messageBeens.size()-1);
                                    // 移动列表位置
                                    if (!rv_conversation.canScrollVertically(1)) {
                                        rv_conversation.scrollToPosition(messageBeens.size()-1);
                                    }
                                    return;
                                }
                            } catch (HyphenateException e) {

                            }
                            new Handler().postDelayed(() -> {
                                // 找到已存在的那条数据，调整它的文本发送时间，以保证发送时间与最初时间相同
                                for (EMMessage messageBeen : messageBeens) {
                                    if (messageBeen.getMsgId().equals(emMessage.getMsgId())) {
                                        messageBeen.setMsgTime(emMessage.localTime());
                                        messageBeen.setStatus(emMessage.status());
                                        // 更新缓存
                                        EMMessageManager.saveMessage(messageBeen);
                                        // 更新数据库
                                        EMMessageManager.updateMessage(messageBeen);
                                    }
                                }

                                ProgressBar pb=rv_conversation.findViewWithTag(emMessage.localTime()+"_pb");
                                if (pb!=null) {
                                    pb.setVisibility(View.GONE);
                                }
                            }, 1000);
                        }
                        if (emMessage.status() == EMMessage.Status.FAIL) {
                            new Handler().postDelayed(() -> {
                                ProgressBar pb=rv_conversation.findViewWithTag(emMessage.localTime()+"_pb");
                                if (pb!=null) {
                                    pb.setVisibility(View.GONE);
                                }
                                ImageView imageView=rv_conversation.findViewWithTag(emMessage.localTime()+"_status");
                                if (imageView!=null) {
                                    imageView.setVisibility(View.VISIBLE);
                                }
                            }, 1000);
                        }
                    }
                    // 被踢下线
                    if (intent.getSerializableExtra(BroadcastBean.COMMAND) == BroadcastBean.EaseMobCommand.Kickout) {
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
        EMMessageManager.markAllMessagesAsRead(chatUserId);
        BroadcastBean.sendBroadcast(getApplicationContext(), BroadcastBean.EaseMobCommand.UpdateRead);

        // 关闭音频播放
        recycleMedia();
        // 关闭动画
        stopVoicePlayAnimation(isSend);
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
            // 选择的是当前项目Cache目录下的图片
            if (file.getParentFile().getPath().equals(InitParams.CACHE_PATH)) {
                Toast.makeText(this, "图片选择有误", Toast.LENGTH_SHORT).show();
                return;
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
     * 初始化pagesize个消息
     */
    private void initMessages() {
        this.messageBeens.clear();
        List<EMMessage> msgs = EMMessageManager.getAllMessages(chatUserId);
        checkCreateAndInProgress(msgs);
        this.messageBeens.addAll(msgs);
        int msgCount = msgs.size();
        if (msgCount < pagesize) {
            String msgId = null;
            if (msgs.size() > 0) {
                msgId = msgs.get(0).getMsgId();
            }
            List<EMMessage> msgs2 = EMMessageManager.getConversationByStartMsgId(chatUserId, msgId, pagesize - msgCount);
            checkCreateAndInProgress(msgs2);
            this.messageBeens.addAll(0, msgs2);
        }
    }

    private void loadMoreLocalMessage() {
        List<EMMessage> messages = EMMessageManager.getConversationByStartMsgId(
                chatUserId,
                EMMessageManager.getAllMessages(chatUserId).size() == 0 ? "" : EMMessageManager.getAllMessages(chatUserId).get(0).getMsgId(),
                pagesize);
        checkCreateAndInProgress(messages);
        this.messageBeens.addAll(0, messages);
        adapter.notifyItemRangeInserted(0, messages.size());
    }

    /**
     * 发送过程中出现异常导致发送失败，发送状态并未修改，所以在此处进行检查、修改
     * @param msgs2
     */
    private void checkCreateAndInProgress(List<EMMessage> msgs2) {
        for (EMMessage msg : msgs2) {
            if ((msg.status() == EMMessage.Status.CREATE || msg.status() == EMMessage.Status.INPROGRESS) &&
                    !EMMessageManager.isSendingMessage(msg.getMsgId())) {
                msg.setStatus(EMMessage.Status.FAIL);
            }
        }
    }

    private void sendTextMessage() {
        if (TextUtils.isEmpty(edit_conversation.getText().toString())) {
            return;
        }
        // 刷新列表
        EMMessage message = EMMessageManager.prepareTxtEMMessage(edit_conversation.getText().toString(), chatUserId);
        messageBeens.add(message);
        adapter.notifyItemInserted(messageBeens.size()-1);
        // 移动列表位置
        if (!rv_conversation.canScrollVertically(1)) {
            rv_conversation.scrollToPosition(messageBeens.size()-1);
        }
        // 重置文本框
        edit_conversation.setText("");
        // 发送
        EMMessageManager.sendSingleMessage(Utils.getApp(), message);
        EMMessageManager.addSendingMessage(message.getMsgId());
    }

    private void sendPicMessage(File file) {
        // 刷新列表
        EMMessage message = EMMessageManager.prepareImageMessage(file.getPath(), true, chatUserId);
        messageBeens.add(message);
        adapter.notifyItemInserted(messageBeens.size()-1);
        // 移动列表位置
        if (!rv_conversation.canScrollVertically(1)) {
            rv_conversation.scrollToPosition(messageBeens.size()-1);
        }
        // 发送
        EMMessageManager.sendSingleMessage(Utils.getApp(), message);
        EMMessageManager.addSendingMessage(message.getMsgId());
    }

    private void sendVoiceMessage(File file) {
        EMMessage message = EMMessageManager.prepareVoiceEMMessage(file.getPath(), (int) (file.length()/1000), chatUserId);
        messageBeens.add(message);
        adapter.notifyItemInserted(messageBeens.size()-1);
        // 移动列表位置
        if (!rv_conversation.canScrollVertically(1)) {
            rv_conversation.scrollToPosition(messageBeens.size()-1);
        }
        // 发送
        EMMessageManager.sendSingleMessage(Utils.getApp(), message);
        EMMessageManager.addSendingMessage(message.getMsgId());
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

    /**
     * 发起语音聊天
     */
    protected void startVoiceCall() {
        if (EMClient.getInstance().isConnected()) {
            startActivity(new Intent(this, VoiceCallActivity.class)
                    .putExtra("username", chatUserId)
                    .putExtra("isComingCall", false));
        }
    }

    /**
     * 发起视频聊天
     */
    protected void startVideoCall() {
        if (EMClient.getInstance().isConnected()) {
            startActivity(new Intent(this, VideoCallActivity.class)
                    .putExtra("username", chatUserId)
                    .putExtra("isComingCall", false));
        }
    }

    private File compress(File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), options);

        File cropFile = null;
        try {
            cropFile = new Compressor(getApplicationContext())
                    .setMaxWidth(options.outWidth/2)
                    .setMaxHeight(options.outHeight/2)
                    .setQuality(70)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .setDestinationDirectoryPath(InitParams.CACHE_PATH)
                    .compressToFile(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cropFile;
    }
}
