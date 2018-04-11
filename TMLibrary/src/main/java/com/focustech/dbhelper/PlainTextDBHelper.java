package com.focustech.dbhelper;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;

import com.focustech.tm.open.sdk.messages.protobuf.Enums;
import com.focustech.message.model.FriendGroupRsp;
import com.focustech.message.model.FriendInfoRsp;
import com.focustech.message.model.FriendStatusRsp;
import com.focustech.message.model.MessageBean;
import com.focustech.message.model.OfflineIMDetailResponse;
import com.focustech.message.model.OfflineIMResponse;
import com.focustech.message.model.SystemMessageBean;
import com.focustech.message.model.UserInfoRsp;
import com.tencent.wcdb.Cursor;
import com.tencent.wcdb.database.SQLiteDatabase;
import com.tencent.wcdb.database.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by renyu on 2017/6/16.
 */

public class PlainTextDBHelper extends SQLiteOpenHelper {

    private static PlainTextDBHelper dbHelper;
    private static SQLiteDatabase db;

    private final static String DATABASENAME="test.db";
    private final static int VERSION=1;
    private final static String ID="_id";

    public final static String UserInfoRspTable="UserInfoRspTable";
    public final static String userId="userId";
    public final static String userName="userName";
    public final static String token="token";
    public final static String userNickName="userNickName";
    public final static String userSignature="userSignature";
    public final static String HeadType="HeadType";
    public final static String userHeadId="userHeadId";
    public final static String netIp="netIp";
    public final static String timestamp="timestamp";
    public final static String tmNum="tmNum";
    public final static String role="role";

    public final static String FriendInfoRspTable="FriendInfoRspTable";
    public final static String friendGroupId="friendGroupId";
    public final static String friendGroupName="friendGroupName";
    public final static String friendGroupType="friendGroupType";

    public final static String MessageTable="MessageTable";
    public final static String msg="msg";
    public final static String msgMeta="msgMeta";
    public final static String msgType="msgType";
    public final static String svrMsgId="svrMsgId";
    public final static String fromSvrMsgId="fromSvrMsgId";
    // 消息发送成功/正在发送状态位
    public final static String sync="sync";
    // 消息是否需要重新发送
    public final static String resend="resend";
    // 消息是否已读
    public final static String isRead="isRead";
    // 语音消息是否已读
    public final static String isVoicePlay="isVoicePlay";
    public final static String messageType="messageType";
    public final static String localFileName="localFileName";
    public final static String isSend="isSend";

    public final static String SystemMessageTable="SystemMessageTable";
    public final static String ext="ext";
    public final static String src="src";
    public final static String type="type";
    public final static String groupId="groupId";
    public final static String groupName="groupName";

    public static PlainTextDBHelper getInstance(Context context) {
        if (dbHelper == null) {
            synchronized (PlainTextDBHelper.class) {
                if (dbHelper == null) {
                    dbHelper = new PlainTextDBHelper(context);
                    dbHelper.setWriteAheadLoggingEnabled(true);
                    db=dbHelper.getWritableDatabase();
                }
            }
        }
        return dbHelper;
    }

    public void closeDb() {
        if (dbHelper!=null && db!=null && db.isOpen()) {
            db.close();
            db=null;
            dbHelper=null;
        }
    }

    public PlainTextDBHelper(Context context) {
        super(context, DATABASENAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "+UserInfoRspTable+"("+ID+" integer primary key AUTOINCREMENT, "+
                userId+" text, "+userName+" text, "+token+" text, "+userNickName+" text, "+
                userSignature+" text, "+HeadType+" text, "+userHeadId+" text, "+netIp+" text, "+
                timestamp+" text, "+tmNum+" text, "+role+" text);");
        db.execSQL("CREATE TABLE IF NOT EXISTS "+FriendInfoRspTable+"("+ID+" integer primary key AUTOINCREMENT, "+
                userId+" text, "+friendGroupId+" text, "+friendGroupName+" text, "+friendGroupType+" text);");
        db.execSQL("CREATE TABLE IF NOT EXISTS "+MessageTable+"("+ID+" integer primary key AUTOINCREMENT, "+
                msg+" text, "+msgMeta+" text, "+msgType+" text, "+userId+" text, "+isSend+" text, "+isRead+" text, "+
                isVoicePlay+" text, "+ timestamp+" text, "+svrMsgId+" text, "+fromSvrMsgId+" text, "+sync+" text, "+
                resend+" text, "+messageType+" text, "+localFileName+" text);");
        db.execSQL("CREATE TABLE IF NOT EXISTS "+SystemMessageTable+"("+ID+" integer primary key AUTOINCREMENT, "+
                userId+" text, "+userName+" text, "+isRead+" text, "+ext+" text, "+src+" text, "+timestamp+" text, "+
                type+" text, "+groupId+" text, "+groupName+" text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * 删除全部数据
     */
    public void clearAllInfo() {
        db.execSQL("delete from "+UserInfoRspTable);
        db.execSQL("delete from "+FriendInfoRspTable);
        db.execSQL("delete from "+MessageTable);
        db.execSQL("delete from "+SystemMessageTable);
    }

    /**
     * 删除好友对应组关系
     */
    public void clearAllFriendList() {
        db.execSQL("delete from "+FriendInfoRspTable);
    }

    /**
     * 插入好友对应组关系
     * @param rsps
     */
    public void insertFriendList(ArrayList<FriendGroupRsp> rsps) {
        for (FriendGroupRsp rsp : rsps) {
            for (FriendStatusRsp friendStatusRsp : rsp.getFriends()) {
                ContentValues cv1=new ContentValues();
                cv1.put(userId, friendStatusRsp.getFriendUserId());
                cv1.put(friendGroupId, rsp.getFriendGroupId());
                cv1.put(friendGroupName, rsp.getFriendGroupName());
                cv1.put(friendGroupType, ""+rsp.getFriendGroupType().getNumber());
                db.insert(FriendInfoRspTable, null, cv1);
            }
        }
    }

    /**
     * 插入好友信息
     * @param rsp
     */
    public void insertFriendList(FriendInfoRsp rsp) {
        Cursor cs=db.query(UserInfoRspTable, null, "userId=?", new String[]{rsp.getFriend().getUserId()}, null, null, null);
        cs.moveToFirst();
        if (cs.getCount()==0) {
            ContentValues cv2=new ContentValues();
            cv2.put(userId, rsp.getFriend().getUserId());
            cv2.put(userName, rsp.getFriend().getUserName());
            cv2.put(token, rsp.getFriend().getToken());
            cv2.put(userNickName, rsp.getFriend().getUserNickName());
            cv2.put(userSignature, rsp.getFriend().getUserSignature());
            cv2.put(HeadType, rsp.getFriend().getUserHeadType().getNumber());
            cv2.put(userHeadId, rsp.getFriend().getUserHeadId());
            cv2.put(netIp, rsp.getFriend().getNetIp());
            cv2.put(timestamp, rsp.getFriend().getTimestamp());
            cv2.put(tmNum, rsp.getFriend().getTmNum());
            cv2.put(role, rsp.getFriend().getRole());
            db.insert(UserInfoRspTable, null, cv2);
        }
        cs.close();
    }

    /**
     * 插入好友信息
     * @param rsp
     */
    public void insertFriendList(UserInfoRsp rsp) {
        ContentValues cv2=new ContentValues();
        cv2.put(userId, rsp.getUserId());
        cv2.put(userName, rsp.getUserName());
        cv2.put(token, rsp.getToken());
        cv2.put(userNickName, rsp.getUserNickName());
        cv2.put(userSignature, rsp.getUserSignature());
        cv2.put(HeadType, rsp.getUserHeadType().getNumber());
        cv2.put(userHeadId, rsp.getUserHeadId());
        cv2.put(netIp, rsp.getNetIp());
        cv2.put(timestamp, rsp.getTimestamp());
        cv2.put(tmNum, rsp.getTmNum());
        cv2.put(role, rsp.getRole());

        Cursor cs=db.query(UserInfoRspTable, null, "userId=?", new String[]{rsp.getUserId()}, null, null, null);
        cs.moveToFirst();
        if (cs.getCount()==0) {
            db.insert(UserInfoRspTable, null, cv2);
        }
        else {
            db.update(UserInfoRspTable, cv2, "userId=?", new String[]{rsp.getUserId()});
        }
        cs.close();
    }

    /**
     * 获取好友对应组关系
     */
    public ArrayList<FriendGroupRsp> getFriendList() {
        ArrayList<FriendGroupRsp> friendGroupRsps=new ArrayList<>();
        String lastGroupId="";
        FriendGroupRsp friendGroupRsp=null;
        List<FriendStatusRsp> friendStatusRsps=null;

        Cursor cs= db.query(FriendInfoRspTable, null, null, null, null, null, friendGroupId+" desc");
        cs.moveToFirst();
        for (int i=0;i<cs.getCount();i++) {
            cs.moveToPosition(i);
            String userId_=cs.getString(cs.getColumnIndex(userId));
            String friendGroupId_=cs.getString(cs.getColumnIndex(friendGroupId));
            String friendGroupName_=cs.getString(cs.getColumnIndex(friendGroupName));
            String friendGroupType_=cs.getString(cs.getColumnIndex(friendGroupType));

            // 组合数据
            // 若之前没有对应的lastGroupId则新建，反之复用
            if (!lastGroupId.equals(friendGroupId_)) {
                // 之前有数据则将之前的信息全部提交
                if (friendStatusRsps!=null && friendStatusRsps.size()>0) {
                    friendGroupRsps.add(friendGroupRsp);
                }
                lastGroupId=friendGroupId_;
                friendGroupRsp=new FriendGroupRsp();
                friendGroupRsp.setFriendGroupId(friendGroupId_);
                friendGroupRsp.setFriendGroupName(friendGroupName_);
                friendGroupRsp.setFriendGroupType(Enums.FriendGroupType.valueOf(Integer.parseInt(friendGroupType_)));
                friendStatusRsps=new ArrayList<>();
                friendGroupRsp.setFriends(friendStatusRsps);
            }
            FriendInfoRsp friendInfoRsp=new FriendInfoRsp();
            friendInfoRsp.setFriendGroupId(friendGroupId_);
            friendInfoRsp.setFriendGroupName(friendGroupName_);
            FriendStatusRsp friendStatusRsp=new FriendStatusRsp();
            friendStatusRsp.setFriendInfoRsp(friendInfoRsp);
            friendStatusRsp.setFriendUserId(userId_);
            friendStatusRsps.add(friendStatusRsp);
        }
        // 之前有数据则将之前的信息全部提交
        if (friendStatusRsps!=null && friendStatusRsps.size()>0) {
            friendGroupRsps.add(friendGroupRsp);
        }
        cs.close();
        return friendGroupRsps;
    }

    /**
     * 获取好友信息
     */
    public HashMap<String, UserInfoRsp> getFriendsInfo() {
        HashMap<String, UserInfoRsp> userInfoRsps=new HashMap<>();

        Cursor cs= db.query(UserInfoRspTable, null, null, null, null, null, null);
        cs.moveToFirst();
        for (int i=0;i<cs.getCount();i++) {
            cs.moveToPosition(i);
            String userId_=cs.getString(cs.getColumnIndex(userId));
            String userName_=cs.getString(cs.getColumnIndex(userName));
            String token_=cs.getString(cs.getColumnIndex(token));
            String userNickName_=cs.getString(cs.getColumnIndex(userNickName));
            String userSignature_=cs.getString(cs.getColumnIndex(userSignature));
            String HeadType_=cs.getString(cs.getColumnIndex(HeadType));
            String userHeadId_=cs.getString(cs.getColumnIndex(userHeadId));
            String netIp_=cs.getString(cs.getColumnIndex(netIp));
            String timestamp_=cs.getString(cs.getColumnIndex(timestamp));
            String tmNum_=cs.getString(cs.getColumnIndex(tmNum));
            String role_=cs.getString(cs.getColumnIndex(role));

            // 组合数据
            UserInfoRsp userInfoRsp=new UserInfoRsp();
            userInfoRsp.setUserId(userId_);
            userInfoRsp.setUserName(userName_);
            userInfoRsp.setToken(token_);
            userInfoRsp.setUserNickName(userNickName_);
            userInfoRsp.setUserSignature(userSignature_);
            userInfoRsp.setUserHeadType(Enums.HeadType.valueOf(Integer.parseInt(HeadType_)));
            userInfoRsp.setUserHeadId(userHeadId_);
            userInfoRsp.setNetIp(netIp_);
            userInfoRsp.setTimestamp(Long.parseLong(timestamp_));
            userInfoRsp.setTmNum(Long.parseLong(tmNum_));
            userInfoRsp.setRole(Integer.parseInt(role_));
            userInfoRsps.put(userId_, userInfoRsp);
        }
        cs.close();
        return userInfoRsps;
    }

    /**
     * 获取用户组信息
     * @return
     */
    public ArrayList<FriendGroupRsp> getGroupInfo() {
        ArrayList<FriendGroupRsp> temp=new ArrayList<>();
        Cursor cs = db.query(FriendInfoRspTable, null, null, null, null, null, friendGroupId + " asc");
        cs.moveToFirst();
        for (int i = 0; i < cs.getCount(); i++) {
            cs.moveToPosition(i);
            String friendGroupId_ = cs.getString(cs.getColumnIndex(friendGroupId));
            String friendGroupName_ = cs.getString(cs.getColumnIndex(friendGroupName));
            String friendGroupType_ = cs.getString(cs.getColumnIndex(friendGroupType));

            FriendGroupRsp rsp=new FriendGroupRsp();
            rsp.setFriendGroupName(friendGroupName_);
            rsp.setFriendGroupType(Enums.FriendGroupType.valueOf(Integer.parseInt(friendGroupType_)));
            rsp.setFriendGroupId(friendGroupId_);
            temp.add(rsp);
        }
        return temp;
    }

    /**
     * 插入系统消息
     * @param bean
     */
    public void insertSystemMessage(SystemMessageBean bean) {
        ContentValues cv1=new ContentValues();
        cv1.put(userId, bean.getSrcFriendUserId());
        cv1.put(userName, bean.getSrcFriendUserName());
        cv1.put(ext, bean.getExt());
        cv1.put(src, bean.getSrc());
        cv1.put(type, ""+bean.getType());
        cv1.put(groupId, bean.getGroupId());
        cv1.put(groupName, bean.getGroupName());
        cv1.put(timestamp, bean.getTimestamp());
        cv1.put(isRead, "0");
        db.insert(SystemMessageTable, null, cv1);
    }

    /**
     * 获取最新系统消息
     * @return
     */
    public SystemMessageBean getLatestSystemMessage() {
        SystemMessageBean systemMessageBean=null;
        Cursor cs=db.rawQuery("select *, max("+timestamp+") from "+SystemMessageTable, new String[]{});
        cs.moveToFirst();
        // 未读消息数
        int unReadCount = 0;
        for (int i=0;i<cs.getCount();i++) {
            cs.moveToPosition(i);
            if (TextUtils.isEmpty(cs.getString(cs.getColumnIndex(userId)))) {
                break;
            }
            systemMessageBean=new SystemMessageBean();
            systemMessageBean.setSrcFriendUserId(cs.getString(cs.getColumnIndex(userId)));
            systemMessageBean.setSrcFriendUserName(cs.getString(cs.getColumnIndex(userName)));
            systemMessageBean.setSrc(cs.getString(cs.getColumnIndex(src)));
            systemMessageBean.setTimestamp(cs.getString(cs.getColumnIndex(timestamp)));
            systemMessageBean.setGroupId(cs.getString(cs.getColumnIndex(groupId)));
            systemMessageBean.setGroupName(cs.getString(cs.getColumnIndex(groupName)));
            systemMessageBean.setType(Integer.parseInt(cs.getString(cs.getColumnIndex(type))));
            if (Integer.parseInt(cs.getString(cs.getColumnIndex(isRead))) == 0) {
                unReadCount++;
            }
        }
        cs.close();
        if (systemMessageBean != null) {
            systemMessageBean.setCount(unReadCount);
        }
        return systemMessageBean;
    }

    /**
     * 获取全部系统消息
     * @return
     */
    public ArrayList<SystemMessageBean> getSystemMessage() {
        ArrayList<SystemMessageBean> temps=new ArrayList<>();
        Cursor cs=db.query(SystemMessageTable, null, null, null, null, null, timestamp+" desc");
        cs.moveToFirst();
        for (int i=0;i<cs.getCount();i++) {
            cs.moveToPosition(i);
            SystemMessageBean systemMessageBean=new SystemMessageBean();
            systemMessageBean.setSrcFriendUserId(cs.getString(cs.getColumnIndex(userId)));
            systemMessageBean.setSrcFriendUserName(cs.getString(cs.getColumnIndex(userName)));
            systemMessageBean.setSrc(cs.getString(cs.getColumnIndex(src)));
            systemMessageBean.setTimestamp(cs.getString(cs.getColumnIndex(timestamp)));
            systemMessageBean.setGroupId(cs.getString(cs.getColumnIndex(groupId)));
            systemMessageBean.setGroupName(cs.getString(cs.getColumnIndex(groupName)));
            systemMessageBean.setType(Integer.parseInt(cs.getString(cs.getColumnIndex(type))));
            temps.add(systemMessageBean);
        }
        cs.close();
        return temps;
    }

    /**
     * 插入消息
     * @param offlineMessage
     */
    public boolean insertMessage(MessageBean offlineMessage) {
        synchronized (PlainTextDBHelper.class) {
            ArrayList<MessageBean> messageBeen=new ArrayList<>();
            messageBeen.add(offlineMessage);
            return insertMessages(messageBeen);
        }
    }

    /**
     * 插入消息
     * @param offlineMessages
     */
    public boolean insertMessages(ArrayList<MessageBean> offlineMessages) {
        boolean isExist = false;
        synchronized (PlainTextDBHelper.class) {
            for (int i = 0; i < offlineMessages.size(); i++) {
                MessageBean offlineMessage=offlineMessages.get(offlineMessages.size()-1-i);
                Cursor cs=db.query(MessageTable, null, "svrMsgId=?", new String[]{offlineMessage.getSvrMsgId()}, null, null, null);
                cs.moveToFirst();
                if (cs.getCount()==0) {
                    ContentValues cv1=new ContentValues();
                    cv1.put(msg, offlineMessage.getMsg());
                    cv1.put(msgMeta, offlineMessage.getMsgMeta());
                    cv1.put(msgType, ""+offlineMessage.getMsgType().getNumber());
                    cv1.put(userId, offlineMessage.getUserId());
                    cv1.put(isSend, offlineMessage.getIsSend());
                    cv1.put(timestamp, ""+offlineMessage.getTimestamp());
                    cv1.put(svrMsgId, offlineMessage.getSvrMsgId());
                    cv1.put(fromSvrMsgId, offlineMessage.getFromSvrMsgId());
                    cv1.put(sync, ""+offlineMessage.getSync().getNumber());
                    cv1.put(resend, ""+offlineMessage.getResend().getNumber());
                    cv1.put(messageType, offlineMessage.getMessageType());
                    cv1.put(localFileName, offlineMessage.getLocalFileName());
                    cv1.put(isRead, offlineMessage.getIsRead());
                    cv1.put(isVoicePlay, offlineMessage.getIsVoicePlay());
                    db.insert(MessageTable, null, cv1);
                    isExist = false;
                }
                else {
                    isExist = true;
                }
                cs.close();
            }
        }
        return isExist;
    }

    /**
     * 检查离线消息是不是已经存在于数据库中
     * @param offlineIMDetailResponses
     * @return
     */
    public ArrayList<OfflineIMDetailResponse> checkOfflineMessages(List<OfflineIMDetailResponse> offlineIMDetailResponses) {
        ArrayList<OfflineIMDetailResponse> temp = new ArrayList<>();
        for (OfflineIMDetailResponse offlineIMDetailRespons : offlineIMDetailResponses) {
            Cursor cs=db.query(MessageTable, null, "svrMsgId=?", new String[]{offlineIMDetailRespons.getSvrMsgId()}, null, null, null);
            cs.moveToFirst();
            if (cs.getCount()==0) {
                temp.add(offlineIMDetailRespons);
            }
            cs.close();
        }
        return temp;
    }

    /**
     * 获取会话列表消息
     * @return
     */
    public ArrayList<OfflineIMResponse> getConversationList() {
        ArrayList<OfflineIMResponse> messageBeens=new ArrayList<>();

        String sql="select * from ((select *, max("+timestamp+") from "+MessageTable+" group by "+userId+" order by "+timestamp+" desc) as k1 " +
                "left join " +
                "(select "+userId+" as "+userId+"1, count("+isRead+") from "+MessageTable+" where "+isRead+"='0' group by "+userId+"1) as k2 " +
                "on k1."+userId+" = k2."+userId+"1)";
        Cursor cs=db.rawQuery(sql, new String[]{});
        cs.moveToFirst();
        for (int i=0;i<cs.getCount();i++) {
            cs.moveToPosition(i);
            String msg=cs.getString(cs.getColumnIndex("msg"));
            String userId=cs.getString(cs.getColumnIndex("userId"));
            String timestamp=cs.getString(cs.getColumnIndex("timestamp"));
            String messageType=cs.getString(cs.getColumnIndex("messageType"));
            String count=cs.getString(cs.getColumnIndex("count("+PlainTextDBHelper.this.isRead+")"));

            // 组合数据
            OfflineIMResponse messageBean=new OfflineIMResponse();
            messageBean.setAddTime(Long.parseLong(timestamp));
            messageBean.setFromUserId(userId);
            messageBean.setLastMsg(msg);
            messageBean.setType(Integer.parseInt(messageType));
            messageBean.setUnloadCount(TextUtils.isEmpty(count)?0:Integer.parseInt(count));
            messageBeens.add(messageBean);
        }
        cs.close();

        SystemMessageBean systemMessageBean=getLatestSystemMessage();
        // 如果没有系统消息则直接返回
        if (systemMessageBean==null) {
            return messageBeens;
        }
        OfflineIMResponse messageBean=null;
        for (int i = 0; i < messageBeens.size(); i++) {
            if (Long.parseLong(systemMessageBean.getTimestamp())>messageBeens.get(i).getAddTime()) {
                messageBean=new OfflineIMResponse();
                messageBean.setFromUserId("-1");
                messageBean.setLastMsg(SystemMessageBean.getSystemMsgContent(systemMessageBean));
                messageBean.setAddTime(Long.parseLong(systemMessageBean.getTimestamp()));
                messageBean.setUnloadCount(systemMessageBean.getCount());
                // 插入系统消息到相应的位置
                messageBeens.add(i, messageBean);
                break;
            }
        }
        // 如果系统消息发生时间最早，就添加到最后
        if (messageBean==null) {
            messageBean=new OfflineIMResponse();
            messageBean.setFromUserId("-1");
            messageBean.setLastMsg(SystemMessageBean.getSystemMsgContent(systemMessageBean));
            messageBean.setAddTime(Long.parseLong(systemMessageBean.getTimestamp()));
            messageBean.setUnloadCount(systemMessageBean.getCount());
            messageBeens.add(messageBean);
        }
        return messageBeens;
    }

    /**
     * 获取具体聊天列表
     * @param userId_
     * @return
     */
    public ArrayList<MessageBean> getConversationByUser(String userId_, int page, int pageSize) {
        ArrayList<MessageBean> temp=new ArrayList<>();

        Cursor cs=db.query(MessageTable, null, userId+" =?", new String[]{userId_}, null, null, timestamp+" desc", page*pageSize+", "+pageSize);
        cs.moveToFirst();
        for (int i=0;i<cs.getCount();i++) {
            cs.moveToPosition(i);
            String msg=cs.getString(cs.getColumnIndex("msg"));
            String msgMeta=cs.getString(cs.getColumnIndex("msgMeta"));
            String msgType=cs.getString(cs.getColumnIndex("msgType"));
            String userId=cs.getString(cs.getColumnIndex("userId"));
            String isSend=cs.getString(cs.getColumnIndex("isSend"));
            String timestamp=cs.getString(cs.getColumnIndex("timestamp"));
            String svrMsgId=cs.getString(cs.getColumnIndex("svrMsgId"));
            String fromSvrMsgId=cs.getString(cs.getColumnIndex("fromSvrMsgId"));
            String sync=cs.getString(cs.getColumnIndex("sync"));
            String resend=cs.getString(cs.getColumnIndex("resend"));
            String messageType=cs.getString(cs.getColumnIndex("messageType"));
            String localFileName=cs.getString(cs.getColumnIndex("localFileName"));
            String isRead=cs.getString(cs.getColumnIndex("isRead"));
            String isVoicePlay=cs.getString(cs.getColumnIndex("isVoicePlay"));

            // 组合数据
            MessageBean messageBean=new MessageBean();
            messageBean.setMsg(msg);
            messageBean.setMsgMeta(msgMeta);
            messageBean.setMsgType(Enums.MessageType.valueOf(Integer.parseInt(msgType)));
            messageBean.setIsSend(isSend);
            messageBean.setUserId(userId);
            messageBean.setTimestamp(Long.parseLong(timestamp));
            messageBean.setSvrMsgId(svrMsgId);
            messageBean.setFromSvrMsgId(fromSvrMsgId);
            messageBean.setSync(Enums.Enable.valueOf(Integer.parseInt(sync)));
            messageBean.setResend(Enums.Enable.valueOf(Integer.parseInt(resend)));
            messageBean.setMessageType(messageType);
            messageBean.setLocalFileName(localFileName);
            messageBean.setIsRead(isRead);
            messageBean.setIsVoicePlay(isVoicePlay);
            temp.add(messageBean);
        }
        cs.close();
        ArrayList<MessageBean> messageBeens=new ArrayList<>();
        for (int i = 0; i < temp.size(); i++) {
            messageBeens.add(temp.get(temp.size()-1-i));
        }
        return messageBeens;
    }

    /**
     * 修改发送状态
     * @param svrMsgId_
     * @param resendEnable 重发状态
     * @param syncEnable 同步状态
     */
    public void updateSendState(String svrMsgId_, Enums.Enable resendEnable, Enums.Enable syncEnable) {
        ContentValues cv=new ContentValues();
        cv.put(resend, ""+resendEnable.getNumber());
        cv.put(sync, ""+syncEnable.getNumber());
        db.update(MessageTable, cv, svrMsgId+"=?", new String[]{svrMsgId_});
    }

    /**
     * 更新总消息已读状态
     * @param userId_
     */
    public void updateRead(String userId_) {
        ContentValues cv=new ContentValues();
        cv.put(isRead, "1");
        db.update(MessageTable, cv, userId+"=?", new String[]{userId_});
    }

    /**
     * 更新系统消息已读状态
     */
    public void updateSystemMessageRead() {
        ContentValues cv=new ContentValues();
        cv.put(isRead, "1");
        db.update(SystemMessageTable, cv, null, null);
    }

    /**
     * 更新语音已读状态
     * @param svrMsgId_
     */
    public void updateVoiceRead(String svrMsgId_) {
        ContentValues cv=new ContentValues();
        cv.put(isVoicePlay, "1");
        db.update(MessageTable, cv, svrMsgId+"=?", new String[]{svrMsgId_});
    }

    /**
     * 获取所有未读消息数量
     */
    public int getAllUnreadMessageNum() {
        int count = 0;

        Cursor cs2=db.query(MessageTable, null, isRead+" =?", new String[]{"0"}, null, null, null);
        cs2.moveToFirst();
        if (cs2.getCount()>0) {
            count += cs2.getCount();
        }
        cs2.close();

        Cursor cs1=db.query(SystemMessageTable, null, isRead+" =?", new String[]{"0"}, null, null, null);
        cs1.moveToFirst();
        if (cs1.getCount()>0) {
            count += cs1.getCount();
        }
        cs1.close();

        return count;
    }

    /**
     * 删除好友关系
     * @param userId_
     */
    public void deleteFriendsRelation(String userId_) {
        db.delete(FriendInfoRspTable, userId+"=?", new String[]{userId_});
    }

    /**
     * 添加好友关系
     * @param userId_
     */
    public void addFriendsRelation(String userId_) {
        Cursor cs= db.query(FriendInfoRspTable, null, friendGroupName+"=? and "+userId+"=?", new String[] {"default", userId_}, null, null, null);
        cs.moveToFirst();
        int count=cs.getCount();
        cs.close();
        // 判断是不是不存在该用户
        if (count==0) {
            Cursor cs2= db.query(FriendInfoRspTable, null, friendGroupName+"=?", new String[] {"default"}, null, null, null);
            if (cs2.getCount()>0) {
                cs2.moveToPosition(0);
                String friendGroupId_ = cs2.getString(cs2.getColumnIndex(friendGroupId));
                String friendGroupName_ = cs2.getString(cs2.getColumnIndex(friendGroupName));
                String friendGroupType_ = cs2.getString(cs2.getColumnIndex(friendGroupType));
                cs2.close();

                ContentValues cv1=new ContentValues();
                cv1.put(userId, userId_);
                cv1.put(friendGroupId, friendGroupId_);
                cv1.put(friendGroupName, friendGroupName_);
                cv1.put(friendGroupType, friendGroupType_);
                db.insert(FriendInfoRspTable, null, cv1);
            }
            else {
                cs2.close();
            }
        }
    }
}
