package com.zhuchao.android.fileutils;

public class ByteUtils {
    private static final String TAG = "ByteUtils";

    //-------------------------------------------------------
    // 判断奇数或偶数，位运算，最后一位是1则为奇数，为0是偶数
    public static int isOdd(int num) {
        return num & 1;
    }

    //从高位开始算起，b1,b2,b3,b4 ...
    public static int DoubleBytesToInt(byte b1, byte b2) {
        int a = b1;
        a = (a << 8) | b2;
        //MMLog.log(TAG, "DoubleBytesToInt " + b1 + "," + b2 + ",a = " + a);
        return a;
    }

    public static int ThreeBytesToInt(byte b1, byte b2, byte b3) {
        int a = DoubleBytesToInt(b1, b2);
        a = (a << 8) | b3;
        //MMLog.log(TAG, "ThreeBytesToInt " + b1 + "," + b2 + "," + b3 + ",a = " + a);
        return a;
    }

    public static int FourBytesToInt(byte b1, byte b2, byte b3, byte b4) {
        int a = ThreeBytesToInt(b1, b2, b3);
        a = (a << 8) | b4;
        //MMLog.log(TAG, "FourBytesToInt " + b1 + "," + b2 + "," + b3 + "," + b4 + ",a = " + a);
        return a;
    }

    public static byte[] intToBytes(int value) {
        byte[] b = new byte[4];
        b[3] = (byte) (value & 0x000000ff);
        b[2] = (byte) (value >> 8 & 0x000000ff);
        b[1] = (byte) (value >> 16 & 0x000000ff);
        b[0] = (byte) (value >> 24 & 0x000000ff);
        return b;
    }

    //-------------------------------------------------------
    //Hex字符串转int
    public static int HexToInt(String hex) {
        return Integer.parseInt(hex, 16);
    }

    //Hex字符串转byte
    public static byte HexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }

    //16转十进制
    public static long Hex2Long(String hex) {
        return Long.parseLong(hex, 16);
    }

    //-------------------------------------------------------
    //1字节转2个Hex字符
    public static String Byte2Hex(Byte inByte) {
        return String.format("%02x", inByte).toUpperCase();
    }

    public static byte BytesAdd(byte[] inBytArr, int count) {
        byte aa = 0;
        for (int i = 0; i < count; i++)
            aa = (byte) (aa + inBytArr[i]);
        // aa = (byte) (aa + Byte.valueOf(inBytArr[i]));
        return aa;
    }

    //-------------------------------------------------------
    //字节数组转转hex字符串
    public static String BuffToHexStr(byte[] bytes, String separatorChars) {
        StringBuilder strBuilder = new StringBuilder();
        for (byte valueOf : bytes) {
            strBuilder.append(Byte2Hex(valueOf));
            strBuilder.append(separatorChars);
        }
        return strBuilder.toString();
    }

    public static String BuffToHexStr(byte[] bytes) {
        StringBuilder strBuilder = new StringBuilder();
        for (byte valueOf : bytes) {
            strBuilder.append(Byte2Hex(valueOf));
            strBuilder.append(" ");
        }
        return strBuilder.toString();
    }

    //-------------------------------------------------------
    //字节数组转转hex字符串，可选长度
    public static String ByteArrToHexStr(byte[] inBytArr, int offset, int byteCount) {
        StringBuilder strBuilder = new StringBuilder();
        int j = byteCount;
        for (int i = offset; i < j; i++) {
            //strBuilder.append(Byte2Hex(Byte.valueOf(inBytArr[i])));
            strBuilder.append(Byte2Hex(inBytArr[i]));
        }
        return strBuilder.toString();
    }

    //-------------------------------------------------------
    //把hex字符串转字节数组
    public static byte[] HexToByteArr(String inHex) {
        byte[] result;
        int hexlen = inHex.length();
        if (isOdd(hexlen) == 1) {
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = HexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }
}
