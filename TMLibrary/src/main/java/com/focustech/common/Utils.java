package com.focustech.common;

import com.focustech.message.params.MessageMeta;
import com.focustech.tm.open.sdk.messages.protobuf.Enums;
import com.focustech.message.msg.MessageRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/7/7.
 */

public class Utils {

    /**
     * 由于原来的表情没有可标示的开头结尾字符，在表情加上去之后，我自己加上了[],所以在发送的时候需要去除
     */
    public static String phraseSendText(String string) {
        Pattern p1 = Pattern.compile("\\[/:.+?\\]");
        Matcher mat = p1.matcher(string);
        while (mat.find())
        {
            String oldString = mat.group();
            String newString = oldString.substring(1, oldString.length() - 1);
            string = string.replace(oldString, newString);
        }
        return string;
    }

    public static String createMetaData(MessageRequest request, Enums.MessageType type, String username) {
        String data = null;
        try {
            JSONObject object = new JSONObject();
            object.put(MessageMeta.chattime, 0);
            object.put(MessageMeta.filepath, "");
            object.put(MessageMeta.fontsize, 0);
            object.put(MessageMeta.fontcolor, 0);
            object.put(MessageMeta.font, 10);
            object.put(MessageMeta.fontstyle, 0);
            object.put(MessageMeta.handletype, 0);
            object.put(MessageMeta.isexe, false);
            object.put(MessageMeta.fileChannelId, 0);
            if("8".equals(request.getMessagetype())) {
                object.put(MessageMeta.msgtype, 0);
            }
            else {
                object.put(MessageMeta.msgtype, Integer.parseInt(request.getMessagetype()));
            }
            if(Enums.MessageType.TEXT == type) {
                object.put(MessageMeta.piccount, 0);
                object.put(MessageMeta.picpath, "{}");
            }
            else {
                object.put(MessageMeta.piccount, 1);
                object.put(MessageMeta.picpath, createPicpath(request.getFilePath()));
                object.put(MessageMeta.picid, createPicId(request.getFileId()));
                if("7".equals(request.getMessagetype())) {
                    File file = new File(request.getFilePath());
                    object.put(MessageMeta.filesize, file.length());
                    object.put(MessageMeta.picpath, createVoicePath(request.getFilePath()));
                    object.put(MessageMeta.picid, createVoiceId(request.getFileId()));
                }

            }
            object.put(MessageMeta.recvfilesize, 0);
            object.put(MessageMeta.eType, 0);
            object.put(MessageMeta.sender, username);
            data = object.toString();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static String createMetaData(String messageType, Enums.MessageType type, String username, String filePath, String fileId) {
        String data = null;
        try {
            JSONObject object = new JSONObject();
            object.put(MessageMeta.chattime, 0);
            object.put(MessageMeta.filepath, "");
            object.put(MessageMeta.fontsize, 0);
            object.put(MessageMeta.fontcolor, 0);
            object.put(MessageMeta.font, 10);
            object.put(MessageMeta.fontstyle, 0);
            object.put(MessageMeta.handletype, 0);
            object.put(MessageMeta.isexe, false);
            object.put(MessageMeta.fileChannelId, 0);
            if("8".equals(messageType)) {
                object.put(MessageMeta.msgtype, 0);
            }
            else {
                object.put(MessageMeta.msgtype, Integer.parseInt(messageType));
            }
            if(Enums.MessageType.TEXT == type) {
                object.put(MessageMeta.piccount, 0);
                object.put(MessageMeta.picpath, "{}");
            }
            else {
                object.put(MessageMeta.piccount, 1);
                object.put(MessageMeta.picpath, createPicpath(filePath));
                object.put(MessageMeta.picid, createPicId(fileId));
                if("7".equals(messageType)) {
                    File file = new File(filePath);
                    object.put(MessageMeta.filesize, file.length());
                    object.put(MessageMeta.picpath, createVoicePath(filePath));
                    object.put(MessageMeta.picid, createVoiceId(fileId));
                }
            }
            object.put(MessageMeta.recvfilesize, 0);
            object.put(MessageMeta.eType, 0);
            object.put(MessageMeta.sender, username);
            data = object.toString();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    private static Object createVoicePath(String filePath) {
        File file = new File(filePath);
        JSONObject object;
        try {
            object = new JSONObject();
            String aa = MD5Utils.getFileMD5String(file);
            object.put("1", aa);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return object.toString();
    }

    private static String createPicpath(String filePath) {
        File file = new File(filePath);
        JSONObject object;
        try {
            object = new JSONObject();
            String aa = MD5Utils.getFileMD5String(file);
            object.put("0", aa);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return object.toString();
    }

    private static String createPicId(String fileId) {
        JSONObject object;
        try {
            object = new JSONObject();
            object.put("0", fileId);
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return object.toString();
    }

    private static String createVoiceId(String fileId) {
        JSONObject object;
        try {
            object = new JSONObject();
            object.put("1", fileId);
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return object.toString();
    }
}
