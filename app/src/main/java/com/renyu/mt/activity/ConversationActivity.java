package com.renyu.mt.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.focustech.common.MD5Util;
import com.focustech.common.RecordTool;
import com.focustech.dbhelper.PlainTextDBHelper;
import com.focustech.tm.open.sdk.messages.protobuf.Enums;
import com.focustech.webtm.protocol.tm.message.model.BroadcastBean;
import com.focustech.webtm.protocol.tm.message.model.MessageBean;
import com.focustech.webtm.protocol.tm.message.model.UserInfoRsp;
import com.renyu.commonlibrary.commonutils.ACache;
import com.renyu.imagelibrary.commonutils.Utils;
import com.renyu.mt.R;
import com.renyu.mt.adapter.ConversationAdapter;
import com.focustech.webtm.protocol.tm.message.MTService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.dreamtobe.kpswitch.util.KPSwitchConflictUtil;
import cn.dreamtobe.kpswitch.util.KeyboardUtil;
import cn.dreamtobe.kpswitch.widget.KPSwitchPanelRelativeLayout;

/**
 * Created by Administrator on 2017/7/24.
 */

public class ConversationActivity extends AppCompatActivity {

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

    // 页码
    int page=0;
    int pageSize=20;

    Vibrator mVibrator;
    RecordTool recordTool;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        ButterKnife.bind(this);

        TextView textView=new TextView(ConversationActivity.this);
        textView.setText("个人信息");
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ConversationActivity.this, UserDetailActivity.class);
                intent.putExtra("UserId", getIntent().getStringExtra("UserId"));
                startActivity(intent);
            }
        });
        layout_nav_right.addView(textView);

        IntentFilter filter=new IntentFilter();
        filter.addAction("MT");
        registerReceiver(registerReceiver, filter);

        initParams();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(registerReceiver);

        // 设置当前会话列表消息均已读
        PlainTextDBHelper.getInstance().updateRead(getIntent().getStringExtra("UserId"));
        BroadcastBean.sendBroadcast(this, BroadcastBean.MTCommand.UpdateRead, getIntent().getStringExtra("UserId"));

        // 关闭音频播放
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    BroadcastReceiver registerReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("MT")) {
                BroadcastBean bean = (BroadcastBean) intent.getSerializableExtra("broadcast");
                if (bean.getCommand()== BroadcastBean.MTCommand.Conn) {

                }
                if (bean.getCommand()== BroadcastBean.MTCommand.Disconn) {

                }
                if (bean.getCommand()== BroadcastBean.MTCommand.Message) {
                    MessageBean messageBean = (MessageBean) ((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable();
                    if (messageBean.getUserId().equals(getIntent().getStringExtra("UserId"))) {
                        if (!messageBean.getMessageType().equals("7")) {
                            // 收到图片文字消息之后立即更新，语音消息等下载完成之后再更新
                            messageBeens.add(messageBean);
                            adapter.notifyItemInserted(messageBeens.size()-1);
                            if (!rv_conversation.canScrollVertically(1)) {
                                rv_conversation.scrollToPosition(messageBeens.size()-1);
                            }
                        }
                    }
                }
                if (bean.getCommand()== BroadcastBean.MTCommand.MessageVoiceDownload ||
                        bean.getCommand()== BroadcastBean.MTCommand.MessageSend) {
                    // 收到语音消息下载完成消息之后立即更新
                    MessageBean messageBean = (MessageBean) ((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable();
                    if (messageBean.getUserId().equals(getIntent().getStringExtra("UserId")) && bean.getCommand()== BroadcastBean.MTCommand.MessageVoiceDownload) {
                        messageBeens.add(messageBean);
                        adapter.notifyItemInserted(messageBeens.size()-1);
                        if (!rv_conversation.canScrollVertically(1)) {
                            rv_conversation.scrollToPosition(messageBeens.size()-1);
                        }
                    }
                }
                if (bean.getCommand()== BroadcastBean.MTCommand.MessageSend) {
                    // 当前用户发送的消息之后立即更新
                    MessageBean messageBean = (MessageBean) ((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable();
                    messageBeens.add(messageBean);
                    adapter.notifyItemInserted(messageBeens.size()-1);
                    if (!rv_conversation.canScrollVertically(1)) {
                        rv_conversation.scrollToPosition(messageBeens.size()-1);
                    }
                }
                if (bean.getCommand()== BroadcastBean.MTCommand.MessageUploadComp) {
                    // 语音、图片上传完成之后更新表字段，同时刷新列表
                    MessageBean temp = (MessageBean) ((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable();
                    PlainTextDBHelper.getInstance().updateSendState(temp.getSvrMsgId(), Enums.Enable.DISABLE, Enums.Enable.ENABLE);
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
                if (bean.getCommand()== BroadcastBean.MTCommand.MessageUploadFail) {
                    // 语音、图片上传完成之后更新表字段，同时刷新列表
                    MessageBean temp = (MessageBean) ((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable();
                    PlainTextDBHelper.getInstance().updateSendState(temp.getSvrMsgId(), Enums.Enable.ENABLE, Enums.Enable.ENABLE);
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
            }
        }
    };

    private void initParams() {
        messageBeens=new ArrayList<>();
        ArrayList<MessageBean> temp=PlainTextDBHelper.getInstance().getConversationByUser(getIntent().getStringExtra("UserId"), page, pageSize);
        page++;
        messageBeens.addAll(temp);

        rv_conversation.setHasFixedSize(true);
        LinearLayoutManager manager=new LinearLayoutManager(this);
        rv_conversation.setLayoutManager(manager);
        adapter=new ConversationAdapter(this, messageBeens, getIntent().getBooleanExtra("isGroup", false), getIntent().getStringExtra("UserId"));
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
                            ArrayList<MessageBean> temp=PlainTextDBHelper.getInstance().getConversationByUser(getIntent().getStringExtra("UserId"), page, pageSize);
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
        layout_voicechoice.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
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
            }
        });
        layout_emojichoice=kp_panel_root.findViewById(R.id.layout_emojichoice);
        KeyboardUtil.attach(this, kp_panel_root, new KeyboardUtil.OnKeyboardShowingListener() {
            @Override
            public void onKeyboardShowing(boolean isShowing) {
                if (isShowing) {
                    rv_conversation.scrollToPosition(messageBeens.size()-1);
                }
            }
        });
        KPSwitchConflictUtil.attach(kp_panel_root, edit_conversation, new KPSwitchConflictUtil.SwitchClickListener() {
            @Override
            public void onClickSwitch(boolean switchToPanel) {
                if (switchToPanel) {
                    edit_conversation.clearFocus();
                } else {
                    edit_conversation.requestFocus();
                }
                rv_conversation.scrollToPosition(messageBeens.size()-1);
            }
        }, new KPSwitchConflictUtil.SubPanelAndTrigger(layout_emojichoice, iv_emoji)
                , new KPSwitchConflictUtil.SubPanelAndTrigger(layout_voicechoice, iv_sendvoice));
        edit_conversation.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    KPSwitchConflictUtil.showKeyboard(kp_panel_root, edit_conversation);
                }
                return false;
            }
        });
        kp_panel_root.post(new Runnable() {
            @Override
            public void run() {
                int height=KeyboardUtil.getKeyboardHeight(ConversationActivity.this);
                LinearLayout.LayoutParams params= (LinearLayout.LayoutParams) kp_panel_root.getLayoutParams();
                params.height=height;
                kp_panel_root.setLayoutParams(params);
            }
        });

        // 震动功能初始化
        mVibrator = (Vibrator) getApplication().getSystemService(VIBRATOR_SERVICE);
        // 录音播放功能初始化，并设置自定义的监听事件
        recordTool = new RecordTool();
        recordTool.setRecorderListener(new RecordTool.RecorderListener() {
            @Override
            public void finishRecorder(String path) {
                File file=new File(path);
                if (file.exists()) {
                    sendVoiceMessage(file);
                }
            }
        });
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

    private void sendTextMessage() {
        MessageBean messageBean=new MessageBean();
        messageBean.setMsg(edit_conversation.getText().toString());
        messageBean.setMsgMeta("");
        messageBean.setMsgType(Enums.MessageType.TEXT);
        messageBean.setUserId(getIntent().getStringExtra("UserId"));
        messageBean.setIsSend("1");
        messageBean.setTimestamp(System.currentTimeMillis());
        messageBean.setSvrMsgId(MD5Util.getMD5String(""+System.currentTimeMillis()));
        messageBean.setFromSvrMsgId("");
        messageBean.setSync(Enums.Enable.ENABLE);
        messageBean.setResend(Enums.Enable.DISABLE);
        messageBean.setMessageType("0");
        messageBean.setLocalFileName("");
        messageBean.setIsRead("1");
        PlainTextDBHelper.getInstance().insertMessage(messageBean);
        BroadcastBean.sendBroadcast(this, BroadcastBean.MTCommand.MessageSend, messageBean);

        // 发送消息
        UserInfoRsp userInfoRsp= (UserInfoRsp) ACache.get(this).getAsObject("UserInfoRsp");
        MTService.sendTextMessage(this, getIntent().getStringExtra("UserId"), edit_conversation.getText().toString(), userInfoRsp.getUserName());
        edit_conversation.setText("");
    }

    private void sendPicMessage(File file) {
        MessageBean messageBean=new MessageBean();
        messageBean.setMsg("");
        messageBean.setMsgMeta("");
        messageBean.setMsgType(Enums.MessageType.MULTI_MEDIA);
        messageBean.setIsSend("1");
        messageBean.setUserId(getIntent().getStringExtra("UserId"));
        messageBean.setTimestamp(System.currentTimeMillis());
        messageBean.setSvrMsgId(MD5Util.getMD5String(""+System.currentTimeMillis()));
        messageBean.setFromSvrMsgId("");
        messageBean.setSync(Enums.Enable.DISABLE);
        messageBean.setResend(Enums.Enable.DISABLE);
        messageBean.setMessageType("8");
        messageBean.setLocalFileName(file.getPath());
        messageBean.setIsRead("1");
        PlainTextDBHelper.getInstance().insertMessage(messageBean);
        BroadcastBean.sendBroadcast(this, BroadcastBean.MTCommand.MessageSend, messageBean);

        // 发送消息
        UserInfoRsp userInfoRsp= (UserInfoRsp) ACache.get(this).getAsObject("UserInfoRsp");
        MTService.sendPicMessage(this, getIntent().getStringExtra("UserId"), file.getPath(), userInfoRsp.getUserName(), messageBean);
    }

    private void sendVoiceMessage(File file) {
        MessageBean messageBean=new MessageBean();
        messageBean.setMsg("");
        messageBean.setMsgMeta("");
        messageBean.setMsgType(Enums.MessageType.MULTI_MEDIA);
        messageBean.setIsSend("1");
        messageBean.setUserId(getIntent().getStringExtra("UserId"));
        messageBean.setTimestamp(System.currentTimeMillis());
        messageBean.setSvrMsgId(MD5Util.getMD5String(""+System.currentTimeMillis()));
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
        UserInfoRsp userInfoRsp= (UserInfoRsp) ACache.get(this).getAsObject("UserInfoRsp");
        MTService.sendVoiceMessage(this, getIntent().getStringExtra("UserId"), file.getPath(), userInfoRsp.getUserName(), messageBean);
    }

    /**
     * 重新发送语音消息
     * @param messageBean
     */
    public void resendVoiceMessageState(MessageBean messageBean) {
        // 修改数据库状态
        PlainTextDBHelper.getInstance().updateSendState(messageBean.getSvrMsgId(), Enums.Enable.DISABLE, Enums.Enable.DISABLE);
        // 修改列表状态并刷新
        messageBean.setSync(Enums.Enable.DISABLE);
        messageBean.setResend(Enums.Enable.DISABLE);
        adapter.notifyDataSetChanged();

        // 发送消息
        UserInfoRsp userInfoRsp= (UserInfoRsp) ACache.get(this).getAsObject("UserInfoRsp");
        MTService.sendVoiceMessage(this, getIntent().getStringExtra("UserId"), new File(messageBean.getLocalFileName()).getPath(), userInfoRsp.getUserName(), messageBean);
    }

    /**
     * 重新发送图片消息
     * @param messageBean
     */
    public void resendPicMessage(MessageBean messageBean) {
        // 修改数据库状态
        PlainTextDBHelper.getInstance().updateSendState(messageBean.getSvrMsgId(), Enums.Enable.DISABLE, Enums.Enable.DISABLE);
        // 修改列表状态并刷新
        messageBean.setSync(Enums.Enable.DISABLE);
        messageBean.setResend(Enums.Enable.DISABLE);
        adapter.notifyDataSetChanged();

        // 发送消息
        UserInfoRsp userInfoRsp= (UserInfoRsp) ACache.get(this).getAsObject("UserInfoRsp");
        MTService.sendPicMessage(this, getIntent().getStringExtra("UserId"), new File(messageBean.getLocalFileName()).getPath(), userInfoRsp.getUserName(), messageBean);
    }

    /**
     * 播放音频文件
     * @param fileName
     * @param tag
     */
    public void playMedia(String fileName, String tag) {
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
}
