package com.renyu.mt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.focustech.dbhelper.PlainTextDBHelper;
import com.focustech.message.model.BroadcastBean;
import com.focustech.message.model.MessageBean;
import com.focustech.message.model.SystemMessageBean;
import com.focustech.message.model.UserInfoRsp;
import com.focustech.params.FusionField;
import com.focustech.tm.open.sdk.messages.protobuf.Enums;
import com.renyu.commonlibrary.commonutils.ACache;
import com.renyu.commonlibrary.commonutils.ImagePipelineConfigUtils;
import com.renyu.commonlibrary.commonutils.NotificationUtils;
import com.renyu.commonlibrary.commonutils.Utils;
import com.renyu.commonlibrary.network.HttpsUtils;
import com.renyu.commonlibrary.network.Retrofit2Utils;
import com.renyu.commonlibrary.params.InitParams;
import com.renyu.mt.params.CommonParams;
import com.renyu.mt.service.HeartBeatService;
import com.renyu.mt.service.MTService;
import com.renyu.mt.utils.DownloadUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by Administrator on 2017/7/7.
 */

public class MTApplication extends MultiDexApplication {

    // 连接状态
    public BroadcastBean.MTCommand connState = BroadcastBean.MTCommand.Disconn;
    // 基础广播
    public BroadcastReceiver baseReceiver = null;

    @Override
    public void onCreate() {
        super.onCreate();

        String processName= Utils.getProcessName(android.os.Process.myPid());
        if (processName.equals(getPackageName())) {
            // 初始化工具库
            com.blankj.utilcode.util.Utils.init(this);

            // 初始化fresco
            Fresco.initialize(this, ImagePipelineConfigUtils.getDefaultImagePipelineConfig(this));

            // 初始化相关配置参数
            // 项目根目录
            // 请注意修改xml文件夹下filepaths.xml中的external-path节点，此值需与ROOT_PATH值相同，作为fileprovider使用
            InitParams.ROOT_PATH= Environment.getExternalStorageDirectory().getPath()+ File.separator + "mt";
            // 项目图片目录
            InitParams.IMAGE_PATH= InitParams.ROOT_PATH + File.separator + "image";
            // 项目文件目录
            InitParams.FILE_PATH= InitParams.ROOT_PATH + File.separator + "file";
            // 项目热修复目录
            InitParams.HOTFIX_PATH= InitParams.ROOT_PATH + File.separator + "hotfix";
            // 项目日志目录
            InitParams.LOG_PATH= InitParams.ROOT_PATH + File.separator + "log";
            InitParams.LOG_NAME= "mt_log";
            // 缓存目录
            InitParams.CACHE_PATH= InitParams.ROOT_PATH + File.separator + "cache";
            // fresco缓存目录
            InitParams.FRESCO_CACHE_NAME= "fresco_cache";

            // 初始化网络请求
            Retrofit2Utils retrofit2Utils = Retrofit2Utils.getInstance(CommonParams.HTTPURL);
            OkHttpClient.Builder baseBuilder = new OkHttpClient.Builder()
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .connectTimeout(10, TimeUnit.SECONDS);
            //https默认信任全部证书
            HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(null, null, null);
            baseBuilder.hostnameVerifier((s, sslSession) -> true).sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
            retrofit2Utils.addBaseOKHttpClient(baseBuilder.build());
            retrofit2Utils.baseBuild();
            OkHttpClient.Builder imageUploadOkBuilder = new OkHttpClient.Builder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .connectTimeout(30, TimeUnit.SECONDS);
            retrofit2Utils.addImageOKHttpClient(imageUploadOkBuilder.build());
            retrofit2Utils.imageBuild();

            // 注册基础广播
            if (baseReceiver == null) {
                Log.d("MTAPP", "注册基础广播");
                openBaseReceiver();
            }
            // 开启心跳服务并进行连接
            if (!ServiceUtils.isServiceRunning("com.renyu.mt.service.HeartBeatService")) {
                if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
                    Intent intent = new Intent(this, HeartBeatService.class);
                    intent.putExtra("smallIcon", R.mipmap.ic_launcher);
                    intent.putExtra("largeIcon", R.mipmap.ic_launcher);
                    startForegroundService(intent);
                }
                else {
                    startService(new Intent(this, HeartBeatService.class));
                }
            }
        }
    }

    /**
     * 注册基础广播
     */
    private void openBaseReceiver() {
        // 注册连接监听广播
        baseReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("MT")) {
                    BroadcastBean bean = (BroadcastBean) intent.getSerializableExtra("broadcast");
                    if (bean.getCommand() == BroadcastBean.MTCommand.Conn) {
                        connState = BroadcastBean.MTCommand.Conn;
                        // 如果用户已经登录过，则执行登录操作
                        // 如果是因为App被回收导致页面新建，则执行自动登录
                        // 如果是因为App使用过程中掉线，则连接成功后执行自动登录
                        if (!TextUtils.isEmpty(SPUtils.getInstance().getString(CommonParams.SP_UNAME)) &&
                                !TextUtils.isEmpty(SPUtils.getInstance().getString(CommonParams.SP_PWD))) {
                            MTService.reqLogin(MTApplication.this,
                                    SPUtils.getInstance().getString(CommonParams.SP_UNAME),
                                    SPUtils.getInstance().getString(CommonParams.SP_PWD));
                        }
                        changeNotificationState();
                    }
                    if (bean.getCommand() == BroadcastBean.MTCommand.Disconn) {
                        connState = BroadcastBean.MTCommand.Disconn;
                        changeNotificationState();
                    }
                    if (bean.getCommand() == BroadcastBean.MTCommand.Conning) {
                        connState = BroadcastBean.MTCommand.Conning;
                        changeNotificationState();
                    }
                    // 登录成功
                    if (bean.getCommand()== BroadcastBean.MTCommand.LoginRsp) {
                        Toast.makeText(MTApplication.this, "登录成功", Toast.LENGTH_SHORT).show();
                    }
                    // 此处为当前用户登录后返回的信息
                    if (bean.getCommand()== BroadcastBean.MTCommand.UserInfoRsp) {
                        UserInfoRsp userInfoRsp = (UserInfoRsp) ((BroadcastBean) intent.getSerializableExtra("broadcast")).getSerializable();
                        if (ACache.get(MTApplication.this).getAsObject("UserInfoRsp") == null) {
                            ACache.get(MTApplication.this).put("UserInfoRsp", userInfoRsp);

                            // 获取离线消息
                            MTService.reqGetOfflineMessage(MTApplication.this);
                            // 获取系统消息
                            MTService.getSysNtyReq(MTApplication.this, 0);
                        }
                        else if (ACache.get(MTApplication.this).getAsObject("UserInfoRsp") != null &&
                                ((UserInfoRsp) ACache.get(MTApplication.this).getAsObject("UserInfoRsp")).getUserId().equals(userInfoRsp.getUserId())) {
                            ACache.get(MTApplication.this).put("UserInfoRsp", userInfoRsp);

                            // 获取离线消息
                            MTService.reqGetOfflineMessage(MTApplication.this);
                            // 获取系统消息
                            MTService.getSysNtyReq(MTApplication.this, 0);
                        }
                    }
                    // 获取到离线消息。离线数据不进行更新，全部依赖本地数据与接口数据的返回
                    if (bean.getCommand() == BroadcastBean.MTCommand.GetOfflineMessageRsp) {
                        ArrayList<MessageBean> tempOffline = (ArrayList<MessageBean>) ((BroadcastBean) (intent.getSerializableExtra("broadcast"))).getSerializable();
                        // 发送已读回执
                        for (MessageBean messageBean : tempOffline) {
                            MTService.hasReadOfflineMessage(MTApplication.this, messageBean.getFromSvrMsgId());
                        }
                        // 下载语音文件
                        for (MessageBean messageBean : tempOffline) {
                            if (messageBean.getMessageType().equals("7")) {
                                UserInfoRsp currentUserInfo = (UserInfoRsp) ACache.get(MTApplication.this).getAsObject("UserInfoRsp");
                                String token = currentUserInfo.getToken();
                                String fileId = messageBean.getLocalFileName();
                                StringBuilder sb = new StringBuilder(FusionField.downloadUrl);
                                sb.append("fileid=").append(fileId).append("&type=").append("voice").append("&token=").append(token);
                                // 单纯下载文件
                                DownloadUtils.addFile(MTApplication.this, sb.toString(), fileId, messageBean.getSvrMsgId());
                            }
                        }
                        // 更新数据库
                        PlainTextDBHelper.getInstance(MTApplication.this).insertMessages((ArrayList<MessageBean>) ((BroadcastBean) (intent.getSerializableExtra("broadcast"))).getSerializable());
                    }
                    // 收到系统消息
                    if (bean.getCommand() == BroadcastBean.MTCommand.NewSysNty) {
                        MTService.getSysNtyReq(MTApplication.this, Long.parseLong(bean.getSerializable().toString()));
                    }
                    // 获取到系统消息
                    if (bean.getCommand() == BroadcastBean.MTCommand.SystemMessageResp) {
                        // 插入系统消息
                        SystemMessageBean messageBean = (SystemMessageBean) ((BroadcastBean) (intent.getSerializableExtra("broadcast"))).getSerializable();
                        PlainTextDBHelper.getInstance(MTApplication.this).insertSystemMessage(messageBean);
                        // 通知会话列表刷新以及会话详情刷新
                        BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.MessageReceive, messageBean);
                    }
                    // 收到新消息
                    if (bean.getCommand() == BroadcastBean.MTCommand.Message) {
                        MessageBean messageBean = (MessageBean) ((BroadcastBean) (intent.getSerializableExtra("broadcast"))).getSerializable();
                        // 发送已读回执
                        MTService.hasReadMessage(MTApplication.this, messageBean.getFromSvrMsgId());
                        // 下载语音文件
                        if (messageBean.getMessageType().equals("7")) {
                            // 下载完成语音文件之后，方可同步数据库与刷新页面
                            DownloadUtils.addFileAndDb(MTApplication.this, messageBean);
                        } else {
                            PlainTextDBHelper.getInstance(MTApplication.this).insertMessage(messageBean);
                            // 通知会话列表刷新以及会话详情刷新
                            BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.MessageReceive, messageBean);
                        }
                    }
                    // 语音、图片上传完成之后更新表字段
                    if (bean.getCommand() == BroadcastBean.MTCommand.MessageUploadComp) {
                        // 语音、图片上传完成之后更新表字段
                        MessageBean messageBean = (MessageBean) ((BroadcastBean) (intent.getSerializableExtra("broadcast"))).getSerializable();
                        PlainTextDBHelper.getInstance(MTApplication.this).updateSendState(messageBean.getSvrMsgId(), Enums.Enable.DISABLE, Enums.Enable.ENABLE);
                    }
                    // 语音、图片上传失败之后更新表字段
                    if (bean.getCommand() == BroadcastBean.MTCommand.MessageUploadFail) {
                        MessageBean messageBean = (MessageBean) ((BroadcastBean) (intent.getSerializableExtra("broadcast"))).getSerializable();
                        PlainTextDBHelper.getInstance(MTApplication.this).updateSendState(messageBean.getSvrMsgId(), Enums.Enable.ENABLE, Enums.Enable.ENABLE);
                    }
                    // 删除好友
                    if (bean.getCommand()== BroadcastBean.MTCommand.DeleteFriendRsp) {
                        Toast.makeText(MTApplication.this, "删除好友成功", Toast.LENGTH_SHORT).show();

                        String userId=((UserInfoRsp) bean.getSerializable()).getUserId();
                        // 数据库中删除好友关联关系
                        PlainTextDBHelper.getInstance(MTApplication.this).deleteFriendsRelation(userId);
                    }
                    // 被添加好友
                    if (bean.getCommand()== BroadcastBean.MTCommand.AddedFriendSucceededSysNty) {
                        Toast.makeText(MTApplication.this, "被"+((UserInfoRsp) bean.getSerializable()).getUserName()+"添加好友成功", Toast.LENGTH_SHORT).show();

                        String userId=((UserInfoRsp) bean.getSerializable()).getUserId();
                        // 数据库添加好友关联
                        PlainTextDBHelper.getInstance(MTApplication.this).addFriendsRelation(userId);

                        // 刷新好友列表数据
                        BroadcastBean.sendBroadcast(MTApplication.this, BroadcastBean.MTCommand.RefreshFriendList, "");
                    }
                    // 添加好友
                    if (bean.getCommand()== BroadcastBean.MTCommand.AddFriendWithoutValidateSucceededSysNty) {
                        Toast.makeText(MTApplication.this, "添加好友成功", Toast.LENGTH_SHORT).show();

                        String userId=((UserInfoRsp) bean.getSerializable()).getUserId();
                        // 数据库添加好友关联
                        PlainTextDBHelper.getInstance(MTApplication.this).addFriendsRelation(userId);

                        // 刷新好友列表数据
                        BroadcastBean.sendBroadcast(MTApplication.this, BroadcastBean.MTCommand.RefreshFriendList, "");
                    }
                    // 新增好友时信息通知
                    if (bean.getCommand()== BroadcastBean.MTCommand.FriendInfoNty) {
                        UserInfoRsp userInfoRsp = (UserInfoRsp) bean.getSerializable();
                        // 数据库添加好友信息
                        PlainTextDBHelper.getInstance(MTApplication.this).insertFriendList(userInfoRsp);
                    }
                    // 被踢下线
                    if (bean.getCommand()== BroadcastBean.MTCommand.Kickout) {
                        CommonParams.isKickout = true;

                        // 清除缓存内容
                        SPUtils.getInstance().remove(CommonParams.SP_UNAME);
                        SPUtils.getInstance().remove(CommonParams.SP_PWD);
                        ACache.get(com.blankj.utilcode.util.Utils.getApp()).remove("UserInfoRsp");
                        Log.d("MTApp", "发生注销");
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("MT");
        registerReceiver(baseReceiver, filter);
    }

    /**
     * 反注册基础广播
     */
    private void closeBaseReceiver() {
        if (baseReceiver != null) {
            unregisterReceiver(baseReceiver);
            baseReceiver = null;
        }
    }

    /**
     * 通知App连接状态发生改变
     */
    public void changeNotificationState() {
        String state = "未知状态";
        switch (connState) {
            case Conn:
                state = "连接成功";
                break;
            case Conning:
                state = "正在连接";
                break;
            case Disconn:
                state = "连接失败";
        }
        NotificationUtils.getNotificationCenter(this).createNormalNotification("提示",
                "APP在线情况",
                state,
                R.color.colorPrimary,
                R.mipmap.ic_launcher,
                R.mipmap.ic_launcher,
                new Intent(),
                1001);
    }
}
