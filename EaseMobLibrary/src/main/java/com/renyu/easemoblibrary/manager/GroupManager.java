package com.renyu.easemoblibrary.manager;

import com.hyphenate.chat.EMClient;

public class GroupManager {

    /**
     * 同步加载所有群组
     */
    public static void loadAllGroups() {
        EMClient.getInstance().groupManager().loadAllGroups();
    }
}
