package com.renyu.nimlibrary.params;

import android.os.Environment;

import java.io.File;

/**
 * Created by Administrator on 2018/3/21 0021.
 */

public class CommonParams {
    // 用户登录、密码信息
    public static final String SP_UNAME = "sp_uname";
    public static final String SP_PWD = "sp_pwd";

    // SDK根目录路径
    public static final String SDKROOT = Environment.getExternalStorageDirectory().getPath() + File.separator + "example";

    // app是否发生了回收，用来给中控页使用
    public static boolean isRestore = false;

    // 是否被踢下线
    public static boolean isKickout = false;

    public static final String TYPE = "type";
    // 退出App
    public static final int FINISH = 1;
    // 被踢下线
    public static final int KICKOUT = 2;
    // 登录返回键返回
    public static final int SIGNINBACK = 3;
    // 去主页
    public static final int MAIN = 4;

    // 发出"正在输入提示"指令
    public static final String COMMAND_INPUT = "command_input";
}
