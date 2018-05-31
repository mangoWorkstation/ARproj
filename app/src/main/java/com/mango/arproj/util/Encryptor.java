package com.mango.arproj.util;

/**
 * Created by mangguojun on 2018/4/22.
 */

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Encryptor {
    /**
     * 传入文本内容，返回 SHA-256 串
     *
     * @param strText
     * @return SHA512
     */

    public String SHA256(final String strText) {
        return SHA(strText, "SHA-256");
    }

    /**
     * 传入文本内容，返回 SHA-512 串
     *
     * @param strText
     * @return
     */
    public String SHA512(final String strText) {
        return SHA(strText, "SHA-512");
    }

    /**
     * 传入文本内容，返回 加盐加密后的SHA-512 串
     *
     * @param str 明文
     * @param salt 盐
     * @return
     */
    public String encodeSHA512Salt(final String str,String salt) {
        return SHA(SHA(str,"SHA-512")+salt,"SHA-512");
    }

    /**
     * 字符串 SHA 加密
     *
     * @param strText
     * @return
     */
    private String SHA(final String strText, final String strType) {
        // 返回值
        String strResult = null;

        // 是否是有效字符串
        if (strText != null && strText.length() > 0) {
            try {
                // SHA 加密开始
                // 创建加密对象 并传入加密类型
                MessageDigest messageDigest = MessageDigest
                        .getInstance(strType);
                // 传入要加密的字符串
                messageDigest.update(strText.getBytes());
                // 得到 byte 类型结果
                byte byteBuffer[] = messageDigest.digest();

                // 将 byte 转换为 string
                StringBuffer strHexString = new StringBuffer();
                // 遍历 byte buffer
                for (int i = 0; i < byteBuffer.length; i++) {
                    String hex = Integer.toHexString(0xff & byteBuffer[i]);
                    if (hex.length() == 1) {
                        strHexString.append('0');
                    }
                    strHexString.append(hex);
                }
                // 得到返回结果
                strResult = strHexString.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        return strResult;
    }
}
