package com.focustech.common;

import org.apache.commons.lang.ArrayUtils;

import java.nio.ByteOrder;
import java.util.BitSet;

/**
 * 字节工具类
 */
public final class ByteUtils {
    private ByteUtils() {
    }

    /**
     * long -> byte[] <br/>
     *
     * @param longValue
     * @param order     字节序
     * @return
     */
    public static byte[] long2byte(long longValue, ByteOrder order) {
        byte[] bt = new byte[8];
        bt[7] = (byte) (0xffl & longValue);
        bt[6] = (byte) ((0xff00l & longValue) >> 8);
        bt[5] = (byte) ((0xff0000l & longValue) >> 16);
        bt[4] = (byte) ((0xff000000l & longValue) >> 24);
        bt[3] = (byte) ((0xff00000000l & longValue) >> 32);
        bt[2] = (byte) ((0xff0000000000l & longValue) >> 40);
        bt[1] = (byte) ((0xff000000000000l & longValue) >> 48);
        bt[0] = (byte) ((0xff00000000000000l & longValue) >> 56);

        // 高字节序 -> 低字节序
        if (ByteOrder.LITTLE_ENDIAN == order) {
            reverse(bt);
        }

        return bt;
    }

    /**
     * long -> byte[] <br/>
     *
     * @param longValue
     * @return
     */
    public static byte[] long2byte(long longValue) {
        return long2byte(longValue, ByteOrder.BIG_ENDIAN);
    }

    /**
     * int -> byte[] <br/>
     *
     * @param intValue
     * @param order    字节序
     * @return
     */
    public static byte[] int2byte(int intValue, ByteOrder order) {
        byte[] bt = new byte[4];
        bt[3] = (byte) (0xff & intValue);
        bt[2] = (byte) ((0xff00 & intValue) >> 8);
        bt[1] = (byte) ((0xff0000 & intValue) >> 16);
        bt[0] = (byte) ((0xff000000 & intValue) >> 24);

        // 高字节序 -> 低字节序
        if (ByteOrder.LITTLE_ENDIAN == order) {
            reverse(bt);
        }

        return bt;
    }

    /**
     * unsigned int -> byte[] <br/>
     *
     * @param intValue
     * @param order    字节序
     * @return
     */
    public static byte[] unsignedInt2byte(long intValue, ByteOrder order) {
        byte[] bt = new byte[4];
        bt[3] = (byte) (0xff & intValue);
        bt[2] = (byte) ((0xff00 & intValue) >> 8);
        bt[1] = (byte) ((0xff0000 & intValue) >> 16);
        bt[0] = (byte) ((0xff000000 & intValue) >> 24);

        // 高字节序 -> 低字节序
        if (ByteOrder.LITTLE_ENDIAN == order) {
            reverse(bt);
        }

        return bt;
    }

    /**
     * int -> byte[] <br/>
     *
     * @param intValue
     * @return
     */
    public static byte[] unsignedInt2byte(long intValue) {
        return unsignedInt2byte(intValue, ByteOrder.BIG_ENDIAN);
    }

    /**
     * unsigned int -> byte[] <br/>
     *
     * @param intValue
     * @return
     */
    public static byte[] int2byte(int intValue) {
        return int2byte(intValue, ByteOrder.BIG_ENDIAN);
    }

    /**
     * short -> byte[] <br/>
     *
     * @param n
     * @param order 字节序
     * @return
     */
    public static byte[] short2byte(short n, ByteOrder order) {
        byte[] bt = new byte[2];
        bt[1] = (byte) (0xff & n);
        bt[0] = (byte) ((0xff00 & n) >> 8);

        // 高字节序 -> 低字节序
        if (ByteOrder.LITTLE_ENDIAN == order) {
            reverse(bt);
        }

        return bt;
    }

    /**
     * short -> byte[] <br/>
     *
     * @param shortValue
     * @return
     */
    public static byte[] short2byte(short shortValue) {
        return short2byte(shortValue, ByteOrder.BIG_ENDIAN);
    }

    /**
     * byte[] -> int <br/>
     *
     * @param byteValue 字节码
     * @param order     字节序
     * @return
     */
    public static int byte2int(byte[] byteValue, ByteOrder order) {
        byte[] data = ArrayUtils.clone(byteValue);

        // 低字节序 -> 高字节序
        if (ByteOrder.LITTLE_ENDIAN == order) {
            reverse(data);
        }

        int x = 0;
        for (int i = 0; i < data.length; i++) {
            x <<= 8;
            x |= data[i] & 0xff;
        }
        return x;
    }

    /**
     * byte[] -> unsigned int <br/>
     *
     * @param byteValue 字节码
     * @param order     字节序
     * @return
     */
    public static long byte2UnSignedInt(byte[] byteValue, ByteOrder order) {
        byte[] data = ArrayUtils.clone(byteValue);

        // 低字节序 -> 高字节序
        if (ByteOrder.LITTLE_ENDIAN == order) {
            reverse(data);
        }

        long x = 0;
        for (int i = 0; i < data.length; i++) {
            x <<= 8;
            x |= data[i] & 0xff;
        }
        return x;
    }

    /**
     * byte[] -> int <br/>
     *
     * @param byteValue 字节码
     * @return
     */
    public static int byte2int(byte[] byteValue) {
        return byte2int(byteValue, ByteOrder.BIG_ENDIAN);
    }

    /**
     * byte[] -> unsigned int <br/>
     *
     * @param byteValue 字节码
     * @return
     */
    public static long byte2UnSignedInt(byte[] byteValue) {
        return byte2UnSignedInt(byteValue, ByteOrder.BIG_ENDIAN);
    }

    /**
     * byte[] -> int <br/>
     *
     * @param byteValue 字节码
     * @param offset    偏移量
     * @param order     当前字节序
     * @return
     */
    public static int byte2int(byte[] byteValue, int offset, ByteOrder order) {
        byte[] target = ArrayUtils.subarray(byteValue, offset, offset + 4);

        return byte2int(target, order);
    }

    /**
     * byte[] -> unsigned int <br/>
     *
     * @param byteValue 字节码
     * @param offset    偏移量
     * @param order     当前字节序
     * @return
     */
    public static long byte2UnSignedInt(byte[] byteValue, int offset, ByteOrder order) {
        byte[] target = ArrayUtils.subarray(byteValue, offset, offset + 4);

        return byte2UnSignedInt(target, order);
    }

    /**
     * byte[] -> int <br/>
     *
     * @param byteValue 字节码
     * @param offset    偏移量
     * @return
     */
    public static int byte2int(byte[] byteValue, int offset) {
        return byte2int(byteValue, offset, ByteOrder.BIG_ENDIAN);
    }

    /**
     * byte[] -> unsigned int <br/>
     *
     * @param byteValue 字节码
     * @param offset    偏移量
     * @return
     */
    public static long byte2UnSignedInt(byte[] byteValue, int offset) {
        return byte2UnSignedInt(byteValue, offset, ByteOrder.BIG_ENDIAN);
    }

    /**
     * byte[] -> short <br/>
     *
     * @param byteValue 字节码
     * @param order 当前字节序
     * @return
     */
    public static short byte2short(byte[] byteValue, ByteOrder order) {
        byte[] data = ArrayUtils.clone(byteValue);

        // 低字节序 -> 高字节序
        if (ByteOrder.LITTLE_ENDIAN == order) {
            reverse(data);
        }

        short x = 0;
        for (int i = 0; i < data.length; i++) {
            x <<= 8;
            x |= data[i] & 0xff;
        }
        return x;
    }

    /**
     * byte[] -> short <br/>
     *
     * @param byteValue 字节码
     * @param offset    偏移量
     * @param order     当前字节序
     * @return
     */
    public static short byte2short(byte[] byteValue, int offset, ByteOrder order) {
        byte[] target = ArrayUtils.subarray(byteValue, offset, offset + 2);

        return byte2short(target, order);
    }

    /**
     * byte[] -> long <br/>
     *
     * @param byteValue 字节码
     * @param order     当前字节序
     * @return
     */
    public static long byte2long(byte[] byteValue, ByteOrder order) {
        byte[] data = ArrayUtils.clone(byteValue);

        // 低字节序 -> 高字节序
        if (ByteOrder.LITTLE_ENDIAN == order) {
            reverse(data);
        }

        long x = 0;
        for (int i = 0; i < data.length; i++) {
            x <<= 8;
            x |= data[i] & 0xff;
        }
        return x;
    }

    /**
     * byte[] -> long <br/>
     *
     * @param byteValue 字节码
     * @param offset    偏移量
     * @param order     当前字节序
     * @return
     */
    public static long byte2long(byte[] byteValue, int offset, ByteOrder order) {
        byte[] target = ArrayUtils.subarray(byteValue, offset, offset + 8);

        // 低字节序 -> 高字节序
        if (ByteOrder.LITTLE_ENDIAN == order) {
            reverse(target);
        }

        long x = 0;
        for (int i = 0; i < target.length; i++) {
            x <<= 8;
            x |= target[i] & 0xff;
        }
        return x;
    }

    /**
     * 一个int值的一段连续的bit位的int值<br/>
     * <p/>
     * <pre>
     * 00000000000000001000011100111111
     * 1.index = 30 nbits = 2 return : 3
     * 2.index = 29 nbits = 3 return : 7
     * 2.index = 28 nbits = 4 return : 15
     * </pre>
     *
     * @param intValue
     * @param offset   开始位置
     * @param length   连续多少个bit位
     * @return
     */
    public static int intValueOfBits(int intValue, int offset, int length) {
        int result = 0;

        for (int i = 0; i < 32; i++) {
            result <<= 1;
            if (i >= offset && i < offset + length) {
                result++;
            }
        }

        result &= intValue;

        return result >> (32 - offset - length);
    }

    /**
     * 一个unsigned int值的一段连续的bit位的int值<br/>
     * <p/>
     * <pre>
     * 00000000000000001000011100111111
     * 1.index = 30 nbits = 2 return : 3
     * 2.index = 29 nbits = 3 return : 7
     * 2.index = 28 nbits = 4 return : 15
     * </pre>
     *
     * @param unsignedIntValue
     * @param offset           开始位置
     * @param length           连续多少个bit位
     * @return
     */
    public static long intValueOfBits(long unsignedIntValue, int offset, int length) {
        long result = 0;

        for (int i = 0; i < 32; i++) {
            result <<= 1;
            if (i >= offset && i < offset + length) {
                result++;
            }
        }

        result &= unsignedIntValue;

        return result >> (32 - offset - length);
    }

    /**
     * long -> BinaryString，8个字节的bit码，不足64位，高位补零
     *
     * @param longValue
     * @return
     */
    public static String toBinaryString(long longValue) {
        StringBuilder sb = new StringBuilder("0");

        long index = 0x4000000000000000L;

        for (int i = 1; i < 64; i++) {
            sb.append(0 != (index & longValue) ? 1 : 0);
            index >>= 1;
        }

        return sb.toString();
    }

    /**
     * short -> BinaryString，4个字节的bit码，不足32位，高位补零
     *
     * @param intValue
     * @return
     */
    public static String toBinaryString(int intValue) {
        StringBuilder sb = new StringBuilder("0");

        int index = 0x40000000;

        for (int i = 1; i < 32; i++) {
            sb.append(0 != (index & intValue) ? 1 : 0);
            index >>= 1;
        }

        return sb.toString();
    }

    /**
     * short -> BinaryString，2个字节的bit码，不足16位，高位补零
     *
     * @param shortValue
     * @return
     */
    public static String toBinaryString(short shortValue) {
        StringBuilder sb = new StringBuilder("0");

        short index = 0x4000;

        for (int i = 1; i < 16; i++) {
            sb.append(0 != (index & shortValue) ? 1 : 0);
            index >>= 1;
        }

        return sb.toString();
    }

    /**
     * byte -> BinaryString，1个字节的bit码，不足8位，高位补零
     *
     * @param byteValue
     * @return
     */
    public static String toBinaryString(byte byteValue) {
        StringBuilder sb = new StringBuilder("0");

        byte index = 0x40;

        for (int i = 1; i < 8; i++) {
            sb.append(0 != (index & byteValue) ? 1 : 0);
            index >>= 1;
        }

        return sb.toString();
    }

    /**
     * byte -> bitset<br/>
     *
     * @param byteValue 字节码
     * @return
     */
    public static BitSet byte2bit(byte[] byteValue) {
        int nbits = byteValue.length * 8;

        BitSet set = new BitSet(nbits);
        int offset = 0;
        for (int i = 0; i < byteValue.length; i++) {
            set.or(byte2bit(byteValue[i], nbits, offset));
            offset += 8;
        }

        return set;
    }

    /**
     * byte -> bitset<br/>
     *
     * @param byteValue
     * @return
     */
    public static BitSet byte2bit(byte byteValue, int nbits, int offset) {
        BitSet set = new BitSet(nbits);
        int index = 128; // 10000000

        for (int i = offset; i < offset + 8; i++) {
            set.set(i, 0 != (index & byteValue));
            index >>= 1;
        }

        return set;
    }

    /**
     * 反转数组，用于字节序的改变
     *
     * @param src
     * @return
     */
    private static void reverse(byte[] src) {
        ArrayUtils.reverse(src);
    }
}
