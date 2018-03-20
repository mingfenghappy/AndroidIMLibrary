package com.focustech.common;

import android.content.Context;
import android.util.Log;

import com.focustech.dbhelper.PlainTextDBHelper;
import com.focustech.tm.open.sdk.params.FusionField;
import com.focustech.webtm.protocol.tm.message.model.BroadcastBean;
import com.focustech.webtm.protocol.tm.message.model.MessageBean;
import com.focustech.webtm.protocol.tm.message.model.UserInfoRsp;
import com.renyu.commonlibrary.commonutils.ACache;
import com.renyu.commonlibrary.network.OKHttpHelper;
import com.renyu.commonlibrary.params.InitParams;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2017/7/25.
 */

public class IDownloadFile {

    static OKHttpHelper httpHelper;
    static ExecutorService executorService;

    static {
        httpHelper=OKHttpHelper.getInstance();
        executorService= Executors.newFixedThreadPool(1);
    }

    public static void addFile(final String url, final String fileId) {
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                File file=httpHelper.getOkHttpUtils().syncDownload(url, InitParams.FILE_PATH);
                boolean isRenameOK=file.renameTo(new File(file.getParentFile()+"/"+fileId+".amr"));
                Log.d("IDownloadFile", "isRenameOK:" + isRenameOK);
            }
        };
        executorService.execute(runnable);
    }

    public static void addFileAndDb(final Context context, final MessageBean messageBean) {
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                UserInfoRsp userInfoRsp= (UserInfoRsp) ACache.get(context).getAsObject("UserInfoRsp");
                String token=userInfoRsp.getToken();
                String fileId_ = messageBean.getLocalFileName();
                StringBuilder sb = new StringBuilder(FusionField.downloadUrl);
                sb.append("fileid=").append(fileId_).append("&type=").append("voice").append("&token=").append(token);
                // 下载文件
                File file=httpHelper.getOkHttpUtils().syncDownload(sb.toString(), InitParams.FILE_PATH);
                boolean isRenameOK=file.renameTo(new File(file.getParentFile()+"/"+fileId_+".amr"));
                Log.d("IDownloadFile", "isRenameOK:" + isRenameOK);
                if (isRenameOK) {
                    // 更新数据库
                    PlainTextDBHelper.getInstance().insertMessage(messageBean);
                    // 通知前台页面下载完成，可以更新
                    BroadcastBean.sendBroadcast(context, BroadcastBean.MTCommand.MessageVoiceDownload, messageBean);
                }
            }
        };
        executorService.execute(runnable);
    }
}
