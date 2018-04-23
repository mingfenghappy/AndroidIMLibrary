package com.renyu.mt.app;

import android.content.Intent;

import com.focustech.dbhelper.PlainTextDBHelper;
import com.focustech.message.model.UserInfoRsp;
import com.renyu.mt.activity.ConversationActivity;
import com.renyu.tmbaseuilibrary.app.MTApplication;

public class DemoApplication extends MTApplication {

    /**
     * 定制进入会话详情页
     * @param userId
     * @return
     */
    @Override
    public Intent getNotificationIntent(String userId) {
        // 查询会话人信息
        UserInfoRsp resp = PlainTextDBHelper.getInstance(this).getFriendsInfoById(userId);

        Intent intent=new Intent(this, ConversationActivity.class);
        intent.putExtra("UserId", userId);
        if (resp != null) {
            intent.putExtra("UserHeadId", resp.getUserHeadId());
            intent.putExtra("UserNickName", resp.getUserNickName());
            intent.putExtra("UserHeadType", resp.getUserHeadType());
        }
        else {
            intent.putExtra("UserHeadId", "");
            intent.putExtra("UserNickName", "");
            intent.putExtra("UserHeadType", 0);
        }

        return intent;
    }
}
