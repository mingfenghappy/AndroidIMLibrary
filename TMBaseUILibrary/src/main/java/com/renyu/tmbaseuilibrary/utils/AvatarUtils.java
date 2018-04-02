package com.renyu.tmbaseuilibrary.utils;

import android.content.res.Resources;
import android.text.TextUtils;

import com.focustech.params.FusionField;
import com.focustech.tm.open.sdk.messages.protobuf.Enums;
import com.renyu.tmbaseuilibrary.R;

import java.lang.reflect.Field;

/**
 * Created by Administrator on 2017/7/19.
 */

public class AvatarUtils {

    public static Object displayImg(String faceCode, String fileId, String token) {
        Enums.HeadType headType;
        if (TextUtils.isEmpty(faceCode)) {
            headType = Enums.HeadType.SYSTEM;
        }
        else {
            headType = Enums.HeadType.valueOf(Integer.parseInt(faceCode));
        }
        if (headType == Enums.HeadType.SYSTEM) {
            // 系统头像
            if (TextUtils.isEmpty(fileId)) {
                return R.drawable.default_avatar0;
            }
            else {
                return getImgRes(Integer.valueOf(fileId));
            }
        }
        else if (headType == Enums.HeadType.CUSTOM) {
            // 自定义头像
            if (TextUtils.isEmpty(fileId)) {
                return R.drawable.default_avatar0;
            }
            else {
                StringBuilder sb = new StringBuilder(FusionField.downloadUrl);
                sb.append("fileid=").append(fileId).append("&type=").append("head").append("&token=").append(token);
                return sb.toString();
            }
        }
        return R.drawable.default_avatar0;
    }

    /**
     * 自动获取系统头像的方法
     * 但头像的命名规则得按照规则,例如default_avatar11.png
     * @param face
     * @return
     */
    private static int getImgRes(int face) {
        String key = "default_avatar" + face;
        Class c = R.drawable.class;
        Field[] fields = c.getFields();
        for (Field f : fields) {
            try {
                if (key.equals(f.getName())) {
                    return f.getInt(f);
                }
            }
            catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return R.drawable.default_avatar0;
    }
}
