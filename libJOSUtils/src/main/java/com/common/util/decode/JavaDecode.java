package com.common.util.decode;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;


public class JavaDecode {

    // 转byte
    public static String decodingUTF(String hex) {
        return decodingUTF(hex, "utf-8");
    }

    /**
     * 解码函数
     *
     * @param hex 编码后的数据
     * @param enc 编码
     * @return
     */
    public static String decodingUTF(String hex, String enc) {
        try {
            return new String(decodingUTFToByte(hex), enc);
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
    }

    /**
     * 把hex解码成字节数组
     *
     * @param hex
     * @return
     */
    public static byte[] decodingUTFToByte(String hex) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len = hex.length();
        int i = 0, c;
        while (i < len) {
            c = hex.charAt(i++);
            if (c == '%') {
                c = Character.digit(hex.charAt(i++), 16) << 4;
                c = c + Character.digit(hex.charAt(i++), 16);
            }
            baos.write(c);
        }
        return baos.toByteArray();
    }


    /**
     * 把String转成byte[]
     *
     * @param str - String : 要转换的String
     * @return byte[] : 转换成的byte[]
     */
    public static byte[] StringtoBytes(String Str) { // 写成字节
        byte[] STB = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeUTF(Str);
            STB = baos.toByteArray();
            baos.close();
            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return STB;
    }

    /**
     * 把byte[]转成String
     *
     * @param rec - byte[] : 要转换的byte[]
     * @return String : 转换成的String
     */
    public static String BytesToString(byte[] rec) { // 从字节读取内容
        ByteArrayInputStream bais = new ByteArrayInputStream(rec);
        DataInputStream dis = new DataInputStream(bais);
        String BTS = null;
        try {
            BTS = dis.readUTF();
            bais.close();
            dis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BTS;

    }

    /**
     * 从数组里，指定位置转换出一个int(包括1~4字节)
     *
     * @param abyte    byte[]
     * @param beginPos int
     * @param len      int
     * @param LHorder  boolean 低位在前，高位在后 －－true | 高位在前，低位在后为false
     * @return int
     */
    public static int byteArrToInt(byte[] abyte, int beginPos, int len, boolean LHorder) {
        if (beginPos >= abyte.length - 1) {
            return 0;
        }
        int aint = 0;
        int tmpint;
        for (int i = 0; i < len; i++) {
            tmpint = abyte[i + beginPos];
            if (tmpint < 0) {
                tmpint += 256;
            }
            if (LHorder) tmpint = tmpint << (i * 8);
            else tmpint = tmpint << ((len - i - 1) * 8);
            aint |= tmpint;
        }
        return aint;
    }

    /**
     * 用给定的int类的数值va，转换成byte并且存入到指定数值abyte
     *
     * @param abyte    byte[]
     * @param beginPos int 存入数组开始位置
     * @param va       int
     * @return boolean 如果成功返回1，失败返回0
     */
    public static boolean intToByteArr(byte[] abyte, int beginPos, int len, int va, boolean LHorder) {
        if (beginPos >= abyte.length - 1) {
            return false;
        }

        for (int i = 0; i < len; i++) {
            // byte b = (byte) (va >> (i * 8) & 0xff);
            if (LHorder) {
                // (低位在前面)
                abyte[beginPos + i] = (byte) (va >> (i * 8) & 0xff);
            } else {
                abyte[beginPos + len - 1 - i] = (byte) (va >> (i * 8) & 0xff);
            }
        }
        return true;
    }

    /**
     * 把UTF-8字符串转成16进制编码
     *
     * @param msg - String : 要编码的String
     * @return String : 编码后的String
     */
    public static String encodingUTF(String msg) {
        return encodingUTF(msg, "utf-8");
    }


    /**
     * 把字符串按参数的编码来进行编码
     *
     * @param msg 要编码的内容
     * @param enc 编码
     * @return
     */
    public static String encodingUTF(String msg, String enc) {
        try {
            return encodingUTF(msg.getBytes(enc));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return msg;
        }
    }

    public static String encodingUTF(byte[] aData) {
        try {
            StringBuffer sb = new StringBuffer();
            byte[] b = aData;
            String c;
            for (int i = 0; i < b.length; i++) {
                if (((b[i] & 0xff) > 0x7f || (b[i] & 0xff) <= 0x20)) {
                    sb.append("%");
                    c = Integer.toHexString(b[i] & 0xff);
                    if (c.length() < 2) sb.append("0");
                    sb.append(c);
                } else sb.append((char) b[i]);
            }
            return sb.toString();
        } catch (Exception uee) {
            uee.printStackTrace();
            return new String(aData);
        }
    }

    /**
     * WriteUtf时作为转换参数使用.保证参数不为空
     *
     * @param aValue
     * @return
     */
    final static public String toUTF(String aValue) {
        return aValue == null ? "" : aValue;
    }

    /**
     * readUtf时转换参数使用.
     *
     * @param aValue
     * @return
     */
    final static public String fromUTF(String aValue) {
        return aValue.equals("") ? null : aValue;
    }

    /**
     * int转BCD码.value不能大于100;
     *
     * @param value
     * @return
     */
    public static final byte int2bcd(int value) {
        byte result = 0;
        result = (byte) ((value / 10) << 4);
        result |= (value % 10);
        return result;
    }

    /**
     * bcd转int
     *
     * @param value
     * @return
     */
    public static final int bcd2int(byte value) {
        int result = 0;
        result = ((value >> 4) & 0xf) * 10;
        result += (value & 0xf);
        return result;
    }

}
