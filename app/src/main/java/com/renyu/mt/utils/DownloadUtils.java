package com.renyu.mt.utils;

import android.content.Context;
import android.util.Log;

import com.blankj.utilcode.util.Utils;
import com.focustech.dbhelper.PlainTextDBHelper;
import com.focustech.params.FusionField;
import com.focustech.message.model.BroadcastBean;
import com.focustech.message.model.FileInfoBean;
import com.focustech.message.model.MessageBean;
import com.focustech.message.model.UserInfoRsp;
import com.renyu.commonlibrary.commonutils.ACache;
import com.renyu.commonlibrary.network.OKHttpHelper;
import com.renyu.commonlibrary.params.InitParams;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2017/7/25.
 */

public class DownloadUtils {

    static OKHttpHelper httpHelper;
    static ExecutorService executorService;

    static {
        httpHelper=OKHttpHelper.getInstance();
        executorService= Executors.newFixedThreadPool(1);
    }

    public static void addFile(final Context context, final String url, final String fileId, final String svrMsgId) {
        Runnable runnable= () -> {
            File file=httpHelper.getOkHttpUtils().syncDownload(url, InitParams.FILE_PATH);
            if (file != null) {
                File newNameFile = new File(file.getParentFile()+"/"+fileId+".amr");
                boolean isRenameOK=file.renameTo(newNameFile);
                Log.d("MTAPP", "isRenameOK:" + isRenameOK);
                // 发送文件下载完成广播
                FileInfoBean bean = new FileInfoBean();
                bean.setFileSize(newNameFile.length());
                bean.setSvrMsgId(svrMsgId);
                BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.MessageDownloadComp, bean);
            }
            else {
                // 下载失败直接忽略
                // TODO: 2018/3/23 0023 这里没有处理下载失败的情况
                Log.d("MTAPP", fileId + "下载失败");
            }
        };
        executorService.execute(runnable);
    }

    public static void addFileAndDb(final Context context, final MessageBean messageBean) {
        Runnable runnable= () -> {
            UserInfoRsp userInfoRsp= (UserInfoRsp) ACache.get(context).getAsObject("UserInfoRsp");
            String token=userInfoRsp.getToken();
            String fileId_ = messageBean.getLocalFileName();
            StringBuilder sb = new StringBuilder(FusionField.downloadUrl);
            sb.append("fileid=").append(fileId_).append("&type=").append("voice").append("&token=").append(token);
            // 下载文件
            File file=httpHelper.getOkHttpUtils().syncDownload(sb.toString(), InitParams.FILE_PATH);
            // 下载失败直接忽略
            // TODO: 2018/3/23 0023 这里没有处理下载失败的情况
            if (file != null) {
                boolean isRenameOK=file.renameTo(new File(file.getParentFile()+"/"+fileId_+".amr"));
                Log.d("MTAPP", "isRenameOK:" + isRenameOK);
                if (isRenameOK) {
                    // 更新数据库
                    PlainTextDBHelper.getInstance(Utils.getApp()).insertMessage(messageBean);
                    // 通知前台页面下载完成，可以更新
                    BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.MessageReceive, messageBean);
                }
            }
            else {
                Log.d("MTAPP", messageBean.getLocalFileName() + "下载失败");
            }
        };
        executorService.execute(runnable);
    }
}
