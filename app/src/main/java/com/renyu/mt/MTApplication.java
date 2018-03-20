package com.renyu.mt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.focustech.webtm.protocol.tm.message.HeartBeatService;
import com.focustech.webtm.protocol.tm.message.model.BroadcastBean;
import com.renyu.commonlibrary.commonutils.ImagePipelineConfigUtils;
import com.renyu.commonlibrary.commonutils.Utils;
import com.renyu.commonlibrary.params.InitParams;

import java.io.File;

/**
 * Created by Administrator on 2017/7/7.
 */

public class MTApplication extends MultiDexApplication {

    // 是否已经连接完成
    public BroadcastBean.MTCommand connState = BroadcastBean.MTCommand.Disconn;

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

            // 注册连接监听广播
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals("MT")) {
                        BroadcastBean bean = (BroadcastBean) intent.getSerializableExtra("broadcast");
                        if (bean.getCommand() == BroadcastBean.MTCommand.Conn) {
                            Log.d("MTApplication", "连接成功");
                            connState = BroadcastBean.MTCommand.Conn;
                        }
                        if (bean.getCommand() == BroadcastBean.MTCommand.Disconn) {
                            Log.d("MTApplication", "连接已断开");
                            connState = BroadcastBean.MTCommand.Disconn;
                        }
                        if (bean.getCommand() == BroadcastBean.MTCommand.Conning) {
                            Log.d("MTApplication", "正在连接");
                            connState = BroadcastBean.MTCommand.Conning;
                        }
                    }
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction("MT");
            registerReceiver(receiver, filter);
            // 开启心跳服务并进行连接
            startService(new Intent(this, HeartBeatService.class));
        }
    }
}
