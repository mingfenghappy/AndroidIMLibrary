package com.renyu.nimlibrary.util.emoji;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.util.LruCache;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Xml;

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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiUtils {
    private static final String EMOT_DIR = "emoji/";

    private static final int CACHE_MAX_SIZE = 1024;

    // Emoji数据集
    private static final List<Entry> defaultEntries = new ArrayList<>();
    private static final Map<String, Entry> text2entry = new HashMap<>();
    private static LruCache<String, Bitmap> drawableCache;

    static {
        // 构建Emoji表情数据源
        new EntryLoader().load(Utils.getApp(), EMOT_DIR + "emoji.xml");

        // 设置LruCache
        drawableCache = new LruCache<String, Bitmap>(CACHE_MAX_SIZE) {
            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                if (oldValue != newValue)
                    oldValue.recycle();
            }
        };
    }

    public static int getDisplayCount() {
        return defaultEntries.size();
    }

    /**
     * 获取Emoji对应的Drawable
     * @param context
     * @param index
     * @return
     */
    public static Drawable getDisplayDrawable(Context context, int index) {
        String text = (index >= 0 && index < defaultEntries.size() ?
                defaultEntries.get(index).text : null);
        return text == null ? null : getDrawable(context, text);
    }

    private static Drawable getDrawable(Context context, String text) {
        Entry entry = text2entry.get(text);
        if (entry == null) {
            return null;
        }

        Bitmap cache = drawableCache.get(entry.assetPath);
        if (cache == null) {
            cache = loadAssetBitmap(context, entry.assetPath);
        }
        return new BitmapDrawable(context.getResources(), cache);
    }

    /**
     * 获取Emoji对应的文字
     * @param index
     * @return
     */
    public static String getDisplayText(int index) {
        return index >= 0 && index < defaultEntries.size() ? defaultEntries
                .get(index).text : null;
    }

    /**
     * 获取Emoji对应的SpannableString
     * @param value
     * @param drawable
     * @return
     */
    public static SpannableString getEmojiSpannableString(String value, Drawable drawable) {
        drawable.setBounds(0, 0, (int) (drawable.getIntrinsicWidth()*0.5), (int) (drawable.getIntrinsicHeight()*0.5));
        ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
        SpannableString spannableString = new SpannableString(value);
        spannableString.setSpan(imageSpan, 0, value.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    /**
     * 基于fresco的TextView显示动态emoji表情解决方案
     * @param simpleDraweeSpanTextView
     * @param msg
     */
    public static void replaceFaceMsgByFresco(SimpleDraweeSpanTextView simpleDraweeSpanTextView, String msg) {
        Context context = simpleDraweeSpanTextView.getContext();

        DraweeSpanStringBuilder draweeSpanStringBuilder = new DraweeSpanStringBuilder(msg);

        Pattern mPattern = makePattern();
        Matcher matcher = mPattern.matcher(msg);
        while (matcher.find()) {
            String find = matcher.group();
            if (text2entry.containsKey(find)) {
                Entry defaultEntry = text2entry.get(find);
                DraweeHierarchy draweeAnimatedHierarchy =
                        GenericDraweeHierarchyBuilder.newInstance(context.getResources())
                                .setPlaceholderImage(new ColorDrawable(Color.TRANSPARENT))
                                .setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                                .build();
                DraweeController animatedController =
                        Fresco.newDraweeControllerBuilder()
                                .setUri(Uri.parse("asset:///"+defaultEntry.assetPath))
                                .setAutoPlayAnimations(true)
                                .build();
                draweeSpanStringBuilder.setImageSpan(
                        context,
                        draweeAnimatedHierarchy,
                        animatedController,
                        matcher.start(),
                        matcher.end()-1,
                        SizeUtils.dp2px(20),
                        SizeUtils.dp2px(20),
                        false,
                        DraweeSpan.ALIGN_BOTTOM);
            }
        }
        simpleDraweeSpanTextView.setDraweeSpanStringBuilder(draweeSpanStringBuilder);
    }

    private static Pattern makePattern() {
        return Pattern.compile(patternOfDefault());
    }

    private static String patternOfDefault() {
        return "\\[[^\\[]{1,10}\\]";
    }

    private static Bitmap loadAssetBitmap(Context context, String assetPath) {
        InputStream is = null;
        try {
            Resources resources = context.getResources();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDensity = DisplayMetrics.DENSITY_HIGH;
            options.inScreenDensity = resources.getDisplayMetrics().densityDpi;
            options.inTargetDensity = resources.getDisplayMetrics().densityDpi;
            is = context.getAssets().open(assetPath);
            Bitmap bitmap = BitmapFactory.decodeStream(is, new Rect(), options);
            if (bitmap != null) {
                drawableCache.put(assetPath, bitmap);
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static class EntryLoader extends DefaultHandler {
        private String catalog = "";

        void load(Context context, String assetPath) {
            InputStream is = null;
            try {
                is = context.getAssets().open(assetPath);
                Xml.parse(is, Xml.Encoding.UTF_8, this);
            } catch (IOException | SAXException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (localName.equals("Catalog")) {
                catalog = attributes.getValue(uri, "Title");
            } else if (localName.equals("Emoticon")) {
                String tag = attributes.getValue(uri, "Tag");
                String fileName = attributes.getValue(uri, "File");
                Entry entry = new Entry(tag, EMOT_DIR + catalog + "/" + fileName);

                text2entry.put(entry.text, entry);
                if (catalog.equals("default")) {
                    defaultEntries.add(entry);
                }
            }
        }
    }

    private static class Entry {
        String text;
        String assetPath;

        Entry(String text, String assetPath) {
            this.text = text;
            this.assetPath = assetPath;
        }
    }
}
