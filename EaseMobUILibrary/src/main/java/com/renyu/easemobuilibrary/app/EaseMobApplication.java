package com.renyu.easemobuilibrary.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.renyu.commonlibrary.commonutils.ImagePipelineConfigUtils;
import com.renyu.commonlibrary.network.HttpsUtils;
import com.renyu.commonlibrary.network.Retrofit2Utils;
import com.renyu.commonlibrary.params.InitParams;
import com.renyu.easemoblibrary.EaseMobUtils;
import com.renyu.easemoblibrary.manager.EMMessageManager;
import com.renyu.easemobuilibrary.params.CommonParams;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public abstract class EaseMobApplication extends MultiDexApplication {

    // 基础广播
    public BroadcastReceiver baseReceiver = null;

    String actionName = "";
    String storageName = "mt";

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            Class clazz = Class.forName("com.renyu.easemob.params.InitParams");
            actionName = clazz.getField("actionName").get(clazz).toString();
            storageName = clazz.getField("StorageName").get(clazz).toString();
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

        String processName= com.renyu.commonlibrary.commonutils.Utils.getProcessName(android.os.Process.myPid());
        if (processName.equals(getPackageName())) {
            // 初始化工具库
            com.blankj.utilcode.util.Utils.init(this);

            // 初始化fresco
            Fresco.initialize(this, ImagePipelineConfigUtils.getDefaultImagePipelineConfig(this));

            // 初始化相关配置参数
            // 项目根目录
            // 请注意修改xml文件夹下filepaths.xml中的external-path节点，此值需与ROOT_PATH值相同，作为fileprovider使用
            InitParams.ROOT_PATH= Environment.getExternalStorageDirectory().getPath()+ File.separator + storageName;
            // 项目图片目录
            InitParams.IMAGE_PATH= InitParams.ROOT_PATH + File.separator + "image";
            // 项目文件目录
            InitParams.FILE_PATH= InitParams.ROOT_PATH + File.separator + "file";
            // 项目热修复目录
            InitParams.HOTFIX_PATH= InitParams.ROOT_PATH + File.separator + "hotfix";
            // 项目日志目录
            InitParams.LOG_PATH= InitParams.ROOT_PATH + File.separator + "log";
            InitParams.LOG_NAME= storageName+"_log";
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

            // 注册基础广播
            if (baseReceiver == null) {
                Log.d("MTAPP", "注册基础广播");
                openBaseReceiver();
            }

            // 环信配置
            EaseMobUtils.initChatOptions(this);
            // 开启环信广播监听
            EaseMobUtils.registerMessageListener();
            // 设置消息监听
            EMMessageManager.registerMessageListener();

        }
    }

    private void openBaseReceiver() {
        baseReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(actionName)) {

                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(actionName);
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

    abstract public Intent getNotificationIntent(String userId);
}
