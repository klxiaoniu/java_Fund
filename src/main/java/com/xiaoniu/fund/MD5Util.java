package com.xiaoniu.fund;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 获取字符串MD5值
 * https://www.codenong.com/cs105508248/
 * @author tangbincheng
 */
public class MD5Util {

    /**
     * 获取字符串的16位md5值
     *
     * @param sourceStr
     * @return
     */
    public static String MD5_16(String sourceStr) {
        String md5 = MD5_32(sourceStr).substring(8, 24);
        return md5;
    }

    /**
     * 获取字符串的32位md5值
     *
     * @param sourceStr
     * @return
     */
    public static String MD5_32(String sourceStr) {
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(sourceStr.getBytes());
            result = getString(result, md);
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
        }
        return result;
    }

    public static String MD5_32_bytes(byte[] bytes) {
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(bytes);
            result = getString(result, md);
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
        }
        return result;
    }

    private static String getString(String result, MessageDigest md) {
        byte b[] = md.digest();
        int i;
        StringBuffer buf = new StringBuffer("");
        for (int offset = 0; offset < b.length; offset++) {
            i = b[offset];
            if (i < 0)
                i += 256;
            if (i < 16)
                buf.append("0");
            buf.append(Integer.toHexString(i));
        }
        result = buf.toString();
        return result;
    }
}