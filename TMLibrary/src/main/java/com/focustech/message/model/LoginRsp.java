package com.focustech.message.model;

import com.focustech.tm.open.sdk.messages.protobuf.Session;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/7/18.
 */

public class LoginRsp implements Serializable {
    String userId;    // 用户
    int code = 0;      // 登陆状态（非0为失败）
    String message;   // 登陆错误等描述信息，用户客户端显示。
    String token;     // token 字段
    long timestamp = 0; // 登录服务端时间戳
    long tmNum = 0;     // 麦通号码
    String plantData; // 平台返回字段组(JSON格式)

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTmNum() {
        return tmNum;
    }

    public void setTmNum(long tmNum) {
        this.tmNum = tmNum;
    }

    public String getPlantData() {
        return plantData;
    }

    public void setPlantData(String plantData) {
        this.plantData = plantData;
    }

    public static LoginRsp parse(Session.LoginRsp rsp) {
        LoginRsp loginRsp=new LoginRsp();
        loginRsp.setUserId(rsp.getUserId());
        loginRsp.setCode(rsp.getCode());
        loginRsp.setMessage(rsp.getMessage());
        loginRsp.setTimestamp(rsp.getTimestamp());
        loginRsp.setToken(rsp.getToken());
        return loginRsp;
    }
}
