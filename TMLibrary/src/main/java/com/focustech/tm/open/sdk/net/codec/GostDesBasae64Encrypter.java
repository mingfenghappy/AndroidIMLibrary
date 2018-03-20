package com.focustech.tm.open.sdk.net.codec;

import android.util.Base64;

import com.focustech.common.ByteUtils;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.nio.ByteOrder;

/**
 * gost des 算法实现
 */
public class GostDesBasae64Encrypter {
    /* Gost的s-盒 */
    private static long[][] table = {{0x4, 0xa, 0x9, 0x2, 0xd, 0x8, 0x0, 0xe, 0x6, 0xb, 0x1, 0xc, 0x7, 0xf, 0x5, 0x3},
            {0xe, 0xb, 0x4, 0xc, 0x6, 0xd, 0xf, 0xa, 0x2, 0x3, 0x8, 0x1, 0x0, 0x7, 0x5, 0x9},
            {0x5, 0x8, 0x1, 0xd, 0xa, 0x3, 0x4, 0x2, 0xe, 0xf, 0xc, 0x7, 0x6, 0x0, 0x9, 0xb},
            {0x7, 0xd, 0xa, 0x1, 0x0, 0x8, 0x9, 0xf, 0xe, 0x4, 0x6, 0xc, 0xb, 0x2, 0x5, 0x3},
            {0x6, 0xc, 0x7, 0x1, 0x5, 0xf, 0xd, 0x8, 0x4, 0xa, 0x9, 0xe, 0x0, 0x3, 0xb, 0x2},
            {0x4, 0xb, 0xa, 0x0, 0x7, 0x2, 0x1, 0xd, 0x3, 0x6, 0x8, 0x5, 0x9, 0xc, 0xf, 0xe},
            {0xd, 0xb, 0x4, 0x1, 0x3, 0xf, 0x5, 0x9, 0x0, 0xa, 0xe, 0x7, 0x6, 0x8, 0x2, 0xc},
            {0x1, 0xf, 0xd, 0x0, 0x5, 0x7, 0xa, 0x4, 0x9, 0x2, 0x3, 0xe, 0x6, 0xb, 0x8, 0xc}};
    /* 加密密钥使用顺序表 */
    private static int[] key = {0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7, 7, 6, 5, 4, 3,
            2, 1, 0};

    /**
     * @param str
     * @param key32
     * @return
     */
    public static String encode(String str, String key32) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }

        byte[] value = str.getBytes();
        byte[] realValue = null;
        byte[] temp;

        int len = value.length;
        int pad = (8 - len % 8) % 8;

        realValue = new byte[pad + len];
        System.arraycopy(value, 0, realValue, pad, value.length);

        int times = realValue.length / 8;

        for (int j = 0; j < times; j++) {
            temp = gost_enc(ArrayUtils.subarray(realValue, 8 * j, 8 * j + 8), key32.getBytes());
            System.arraycopy(temp, 0, realValue, 8 * j, temp.length);
        }

        return new String(Base64.encode(realValue, Base64.URL_SAFE));
    }
    
    /**
     * @param str
     * @param key32
     * @return
     */
    public static String decode(String str, String key32) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }

        byte[] data = Base64.decode(str, Base64.URL_SAFE);

        int times = data.length / 8;

        byte[] value = data;
        byte[] temp;

        for (int j = 0; j < times; j++) {
            temp = gost_dec(ArrayUtils.subarray(value, 8 * j, 8 * j + 8), key32.getBytes());
            System.arraycopy(temp, 0, value, 8 * j, temp.length);
        }

        int offset = ArrayUtils.lastIndexOf(value, (byte) 0);

        return new String(value, offset + 1, value.length - offset - 1);
    }

    private static byte[] gost_enc(byte[] src64, byte[] key256) {
        long left = ByteUtils.byte2UnSignedInt(src64, 0, ByteOrder.LITTLE_ENDIAN);
        long right = ByteUtils.byte2UnSignedInt(src64, 4, ByteOrder.LITTLE_ENDIAN);

        long tmp;

        for (int i = 0; i < 32; i++) {
            right ^= f(left + ByteUtils.byte2UnSignedInt(key256, 4 * key[i], ByteOrder.LITTLE_ENDIAN));
            tmp = left;
            left = right;
            right = tmp;

        }

        tmp = left;
        left = right;
        right = tmp;

        byte[] enc = new byte[src64.length];
        System.arraycopy(ByteUtils.unsignedInt2byte(left, ByteOrder.LITTLE_ENDIAN), 0, enc, 0, 4);
        System.arraycopy(ByteUtils.unsignedInt2byte(right, ByteOrder.LITTLE_ENDIAN), 0, enc, 4, 4);
        return enc;
    }

    private static byte[] gost_dec(byte[] src64, byte[] key256) {
        long left = ByteUtils.byte2UnSignedInt(src64, 0, ByteOrder.LITTLE_ENDIAN);
        long right = ByteUtils.byte2UnSignedInt(src64, 4, ByteOrder.LITTLE_ENDIAN);

        long tmp;

        for (int i = 0; i < 32; i++) {
            right ^= f(left + ByteUtils.byte2UnSignedInt(key256, 4 * key[31 - i], ByteOrder.LITTLE_ENDIAN));
            tmp = left;
            left = right;
            right = tmp;

        }

        tmp = left;
        left = right;
        right = tmp;

        byte[] enc = new byte[src64.length];
        System.arraycopy(ByteUtils.unsignedInt2byte(left, ByteOrder.LITTLE_ENDIAN), 0, enc, 0, 4);
        System.arraycopy(ByteUtils.unsignedInt2byte(right, ByteOrder.LITTLE_ENDIAN), 0, enc, 4, 4);

        return enc;
    }

    private static long f(long x) {
        long v = 0;
        for (int i = 7; i >= 0; i--) {
            v <<= 4;
            v |= table[i][(int) ((x >> 4 * i) & 0xfL)];
        }

        long t = v;

        t &= 0x1fffffL;
        t <<= 11;

        return t | v >> 21;
    }
}
