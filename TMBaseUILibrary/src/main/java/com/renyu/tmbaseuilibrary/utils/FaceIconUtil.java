package com.renyu.tmbaseuilibrary.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;

import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.Utils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.interfaces.DraweeHierarchy;
import com.facebook.drawee.span.DraweeSpan;
import com.facebook.drawee.span.DraweeSpanStringBuilder;
import com.facebook.drawee.span.SimpleDraweeSpanTextView;
import com.renyu.tmbaseuilibrary.R;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FaceIconUtil {

    private static FaceIconUtil faceIconUtil;

    // 表情表情的数量
    private static final int totalFaceCount = 58;
    // 本地表情数组集合
    private int[] iconfaceRsId;
    private String[] arr;
    private Map<String, Integer> faceMap;

    public static FaceIconUtil getInstance() {
        if (faceIconUtil == null) {
            synchronized (FaceIconUtil.class) {
                if (faceIconUtil == null) {
                    faceIconUtil = new FaceIconUtil();
                }
            }
        }
        return faceIconUtil;
    }

    private FaceIconUtil() {
        // 配置表情資源
        iconfaceRsId = new int[totalFaceCount];
        int resourceId = 0;
        String fileName;
        try {
            for (int i = 0; i < totalFaceCount; i++) {
                if (i < 9) {
                    fileName = "face_0" + (i + 1);
                } else {
                    fileName = "face_" + (i + 1);
                }
                Field field = R.drawable.class.getDeclaredField(fileName);
                resourceId = field.getInt(R.drawable.class);
                iconfaceRsId[i] = resourceId;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        arr = Utils.getApp().getResources().getStringArray(R.array.faceid);
        faceMap = new HashMap<>();
        for (int i = 0; i < arr.length; i++) {
            faceMap.put(arr[i], iconfaceRsId[i]);
        }
    }

    /**
     * 获取表情资源
     * @return
     */
    public int[] getFaceIconRsId() {
        return iconfaceRsId;
    }

    /**
     * 获取表情对应的文字
     * @param i
     * @return
     */
    public String getFaceKey(int i) {
        return arr[i];
    }

    /**
     * 通过emoji找到其对应的SpannableString
     * @param value
     * @param res
     * @return
     */
    public SpannableString getEmojiSpannableString(String value, int res) {
        Drawable drawable = Utils.getApp().getResources().getDrawable(res);
        drawable.setBounds(0, 0, (int) (drawable.getIntrinsicWidth()*0.5), (int) (drawable.getIntrinsicHeight()*0.5));
        ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
        String str = "[" + value + "]";
        SpannableString spannableString = new SpannableString(str);
        spannableString.setSpan(imageSpan, 0, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    /**
     * 储存数据的时候剔除分隔符
     * @param msg
     * @return
     */
    public String replaceAddBounce(String msg) {
        for (int i = 0; i < arr.length; i++) {
            if (msg.contains(arr[i])) {
                msg = msg.replace("[" + arr[i] + "]", arr[i]);
            }
        }
        return msg;
    }

    /**
     * TextView显示静态emoji表情
     * @param msg
     * @return
     */
    public SpannableStringBuilder replaceFaceMsg(String msg) {
        SpannableStringBuilder builder = new SpannableStringBuilder(msg);
        for (String anArr : arr) {
            if (msg.contains(anArr)) {
                Pattern mPattern = Pattern.compile(anArr);
                Matcher matcher = mPattern.matcher(msg);
                while (matcher.find()) {
                    int resId = faceMap.get(anArr);
                    Drawable mdrawable = Utils.getApp().getResources().getDrawable(resId);
                    mdrawable.setBounds(0, 0, SizeUtils.dp2px(18), SizeUtils.dp2px(18));
                    ImageSpan span = new ImageSpan(mdrawable, ImageSpan.ALIGN_BOTTOM);
                    builder.setSpan(span, matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        return builder;
    }

    /**
     * 基于fresco的TextView显示动态emoji表情解决方案
     * @param simpleDraweeSpanTextView
     * @param msg
     */
    public void replaceFaceMsgByFresco(SimpleDraweeSpanTextView simpleDraweeSpanTextView, String msg) {
        Context context = simpleDraweeSpanTextView.getContext();

        DraweeSpanStringBuilder draweeSpanStringBuilder = new DraweeSpanStringBuilder(msg);

        for (String anArr : arr) {
            if (msg.contains(anArr)) {
                Pattern mPattern = Pattern.compile(anArr);
                Matcher matcher = mPattern.matcher(msg);
                while (matcher.find()) {
                    int resId = faceMap.get(anArr);
                    DraweeHierarchy draweeAnimatedHierarchy =
                            GenericDraweeHierarchyBuilder.newInstance(context.getResources())
                                    .setPlaceholderImage(new ColorDrawable(Color.TRANSPARENT))
                                    .setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                                    .build();
                    DraweeController animatedController =
                            Fresco.newDraweeControllerBuilder()
                                    .setUri(Uri.parse("res:///"+resId))
                                    .setAutoPlayAnimations(true)
                                    .build();
                    draweeSpanStringBuilder.setImageSpan(
                            context,
                            draweeAnimatedHierarchy,
                            animatedController,
                            matcher.start(),
                            matcher.end()-1,
                            SizeUtils.dp2px(18),
                            SizeUtils.dp2px(18),
                            false,
                            DraweeSpan.ALIGN_CENTER);
                }
            }
        }
        simpleDraweeSpanTextView.setDraweeSpanStringBuilder(draweeSpanStringBuilder);
    }
}
