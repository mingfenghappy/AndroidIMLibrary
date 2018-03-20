package com.focustech.webtm.protocol.tm.message.model;

import com.focustech.webtm.protocol.tm.message.params.SystemMsgType;
import com.focustech.tm.open.sdk.messages.protobuf.Contacts;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/8/1.
 */

public class SystemMessageBean implements Serializable {
    String srcFriendUserId;
	String srcFriendUserName;
	String timestamp;
    String ext;               // 附加信息
    String src;               // 好友消息:IM or群消息:GROUP
    int type;                 // 系统消息类型
    String groupId="";
    String groupName="";

    public String getSrcFriendUserId() {
        return srcFriendUserId;
    }

    public void setSrcFriendUserId(String srcFriendUserId) {
        this.srcFriendUserId = srcFriendUserId;
    }

    public String getSrcFriendUserName() {
        return srcFriendUserName;
    }

    public void setSrcFriendUserName(String srcFriendUserName) {
        this.srcFriendUserName = srcFriendUserName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public static SystemMessageBean parse(Contacts.AddedFriendSucceededSysNty nty) {
        SystemMessageBean systemMessageBean=new SystemMessageBean();
        systemMessageBean.setExt("");
        systemMessageBean.setSrcFriendUserName(nty.getSrcFriendUserName());
        systemMessageBean.setSrcFriendUserId(nty.getSrcFriendUserId());
        systemMessageBean.setSrc("IM");
        systemMessageBean.setType(SystemMsgType.ADD_FRIEND_SUCC);
        systemMessageBean.setTimestamp(""+nty.getTimestamp());
        return systemMessageBean;
    }

    public static String getSystemMsgContent(SystemMessageBean systemMsg) {
        String msg = null;
        String showUserName = systemMsg.getSrcFriendUserName();
        String showGroupName = systemMsg.getGroupName();
        switch (systemMsg.getType()) {
            // ------------------好友-----------------------
            case SystemMsgType.ADD_FRIEND_REQ:
                msg = showUserName + "他(她)请求加我为好友";
                break;
            case SystemMsgType.ADD_FRIEND_AGREE_REQ:
                msg = showUserName + "通过了您的好友请求";
                break;
            case SystemMsgType.ADD_FRIEND_REFUSE_REQ:
                msg = showUserName + "拒绝了您的好友请求";
                break;
            case SystemMsgType.ADD_FRIEND_SUCC:
                msg = showUserName + "将您添加为好友";
                break;
            case SystemMsgType.ADD_FRIEND_SUCC_RECEVER:
                msg = showUserName + "成功被加为好友";
                break;
            case SystemMsgType.AGREE_AND_ACCEPT:
                msg = showUserName + "同意了您的好友请求并添加您为好友";
                break;
            // ------------------申请加群-------------------
            case SystemMsgType.ADD_GROUP_REQ:
                msg = showUserName + "申请加入群" + showGroupName;
                break;
            case SystemMsgType.APLLY_JOIN_GROUP_SUCCESS:
                msg = "您已成功加入群" + showGroupName;
                break;
            case SystemMsgType.ADD_GROUP_AGREE_REQ:
                msg = showGroupName + "管理员" + showUserName + "通过了你的加群请求";
                break;
            case SystemMsgType.ADD_GROUP_REFUSE_REQ:
                msg = showGroupName + "管理员" + showUserName + "拒绝你的加群请求";
                break;
            // --------------------邀请加群-------------------------
            case SystemMsgType.INVITE_GROUP_REQ:
                msg = showUserName + "邀请您加入群" + showGroupName;
                break;
            case SystemMsgType.INVITE_ADD_GROUP_SUCCESS:
                msg = showGroupName + "管理员" + showUserName + "已将您加入该群";
                break;
            case SystemMsgType.INVITE_GROUP_TO_ADMIN_SUCCESS:
                msg = showUserName + "成功加入群" + showGroupName;
                break;
            case SystemMsgType.INVITE_GROUP_AGREE_REQ:
                msg = showUserName + "同意加入您的群" + showGroupName;
                break;
            case SystemMsgType.INVITE_GROUP_REFUSE_REQ:
                msg = showUserName + "拒绝加入您的群" + showGroupName;
                break;
            // ---------------------删除群成员----------------------
            case SystemMsgType.GROUP_KICK_OUT:
                msg = showUserName + "已将您请出了群" + showGroupName;
                break;
            case SystemMsgType.DELETE_GROUPMEMBER_TO_ADMIN:
                msg = showUserName + "已被请出群" + showGroupName;
                break;
            case SystemMsgType.EXIT_GROUP_TO_ADMIN_SUCCESS:
                msg = showUserName + "退出群" + showGroupName;
                break;
            case SystemMsgType.GROUP_DELETE:
                msg = showGroupName + "的群主" + showUserName + "已经停用该群";
                break;
            // ---------------------设置管理员-----------------------
            case SystemMsgType.GROUP_TO_BE_GROUP_MGR:
                msg = "您已经成为" + showGroupName + "的管理员";
                break;
            case SystemMsgType.GROUP_LOSE_GROUP_MGR:
                msg = "您已经被取消" + showGroupName + "的管理员身份";
                break;

        }
        return msg;
    }
}
