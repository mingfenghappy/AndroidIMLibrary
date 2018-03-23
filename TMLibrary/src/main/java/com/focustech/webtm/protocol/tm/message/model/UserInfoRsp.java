package com.focustech.webtm.protocol.tm.message.model;

import com.focustech.tm.open.sdk.messages.protobuf.Enums;
import com.focustech.tm.open.sdk.messages.protobuf.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/7.
 */

public class UserInfoRsp implements Serializable {
    String userId = "";                             // 用户ID
    String userName = "";                           // 用户名
    String token = "";                              // token
    String userNickName = "";                       // 用户昵称
    String userSignature = "";                      // 用户签名
    Enums.HeadType userHeadType;                    // 用户头像类型
    String userHeadId = "";                         // 用户头像ID
    String netIp = "";                              // 用户公网ID
    long timestamp = 0;                             // 用户资料最后更新时间戳
    ArrayList<Enums.EquipmentStatus> equipments;    // 好友状态
    long tmNum = 0;                                 // 麦通号码
    int role = 0;                                   // 用户状态

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserNickName() {
        return userNickName;
    }

    public void setUserNickName(String userNickName) {
        this.userNickName = userNickName;
    }

    public String getUserSignature() {
        return userSignature;
    }

    public void setUserSignature(String userSignature) {
        this.userSignature = userSignature;
    }

    public Enums.HeadType getUserHeadType() {
        return userHeadType;
    }

    public void setUserHeadType(Enums.HeadType userHeadType) {
        this.userHeadType = userHeadType;
    }

    public String getUserHeadId() {
        return userHeadId;
    }

    public void setUserHeadId(String userHeadId) {
        this.userHeadId = userHeadId;
    }

    public String getNetIp() {
        return netIp;
    }

    public void setNetIp(String netIp) {
        this.netIp = netIp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public ArrayList<Enums.EquipmentStatus> getEquipments() {
        return equipments;
    }

    public void setEquipments(ArrayList<Enums.EquipmentStatus> equipments) {
        this.equipments = equipments;
    }

    public long getTmNum() {
        return tmNum;
    }

    public void setTmNum(long tmNum) {
        this.tmNum = tmNum;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public static UserInfoRsp parse(User.UserInfoRsp rsp) {
        UserInfoRsp userInfoRsp=new UserInfoRsp();
        userInfoRsp.setNetIp(rsp.getNetIp());
        userInfoRsp.setRole(rsp.getRole());
        userInfoRsp.setTimestamp(rsp.getTimestamp());
        userInfoRsp.setTmNum(rsp.getTmNum());
        userInfoRsp.setToken(rsp.getToken());
        userInfoRsp.setUserHeadId(rsp.getUserHeadId());
        userInfoRsp.setUserHeadType(rsp.getUserHeadType());
        userInfoRsp.setUserName(rsp.getUserName());
        userInfoRsp.setUserNickName(rsp.getUserNickName());
        userInfoRsp.setUserId(rsp.getUserId());
        userInfoRsp.setUserSignature(rsp.getUserSignature());
        ArrayList<Enums.EquipmentStatus> statuses=new ArrayList<>();
        statuses.addAll(rsp.getEquipmentsList());
        userInfoRsp.setEquipments(statuses);
        return userInfoRsp;
    }

    /**
     * 获取用户状态
     * @param equipmentStatusList
     * @return
     */
    public static Enums.EquipmentStatus getShowStatus(List<Enums.EquipmentStatus> equipmentStatusList) {
        Enums.EquipmentStatus.Builder builder = Enums.EquipmentStatus.newBuilder();
        builder.setEquipment(Enums.Equipment.PC);
        builder.setStatus(Enums.Status.OFFLINE);
        Map<Enums.Equipment, Enums.Status> map = new HashMap<>();
        if (equipmentStatusList != null && equipmentStatusList.size() > 0) {
            for (Enums.EquipmentStatus equipmentStatus : equipmentStatusList) {
                Enums.Equipment equipment = equipmentStatus.getEquipment();
                Enums.Status statu = equipmentStatus.getStatus();
                map.put(equipment, statu);
            }
            if (map.containsKey(Enums.Equipment.PC)) {
                if (map.get(Enums.Equipment.PC).getNumber() < 4) {
                    builder.setEquipment(Enums.Equipment.PC);
                    builder.setStatus(map.get(Enums.Equipment.PC));
                }
            }
            else if (map.containsKey(Enums.Equipment.MOBILE_IOS)) {
                if (map.get(Enums.Equipment.MOBILE_IOS) == Enums.Status.ONLINE) {
                    builder.setEquipment(Enums.Equipment.MOBILE_IOS);
                    builder.setStatus(map.get(Enums.Equipment.MOBILE_IOS));
                }
            }
            else if (map.containsKey(Enums.Equipment.MOBILE_ANDROID)) {
                if (map.get(Enums.Equipment.MOBILE_ANDROID) == Enums.Status.ONLINE) {
                    builder.setEquipment(Enums.Equipment.MOBILE_ANDROID);
                    builder.setStatus(map.get(Enums.Equipment.MOBILE_ANDROID));
                }
            }
        }
        return builder.build();
    }

    public static boolean isOnline(int status) {
        boolean bool = false;
        switch (status) {
            case 1:
                bool = true;
                break;
            case 2:
                bool = true;
                break;
            case 3:
                bool = true;
                break;
            case 4:
                bool = false;
                break;
            case 5:
                bool = false;
                break;
        }
        return bool;
    }
}
