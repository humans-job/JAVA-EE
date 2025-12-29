package org.example.army.militaryauthenticate.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * MD5加密工具类（包含纯MD5和加盐MD5）
 */
public class MD5Util {

    // 字符集固定为UTF-8，避免编码不一致导致加密结果不同
    private static final String CHARSET = "UTF-8";

    /**
     * 纯MD5加密（无加盐，仅用于学习，实际开发禁止单独使用）
     * @param plainText 明文密码
     * @return 32位小写MD5密文
     */
    public static String md5(String plainText) {
        try {
            // 1. 获取MD5算法实例
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 2. 将明文转为UTF-8字节数组（必须指定编码，否则默认系统编码会乱码）
            byte[] inputBytes = plainText.getBytes(CHARSET);
            // 3. 计算MD5摘要（16字节数组）
            byte[] md5Bytes = md.digest(inputBytes);
            // 4. 将16字节数组转为32位16进制字符串（核心步骤）
            return bytesToHex(md5Bytes);
        } catch (NoSuchAlgorithmException e) {
            // MD5是JDK内置算法，理论上不会抛出该异常
            throw new RuntimeException("MD5算法不存在", e);
        } catch (Exception e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }

    /**
     * 加盐MD5加密（实际开发推荐）
     * @param plainText 明文密码
     * @return 盐值+MD5密文（格式：盐值$密文，便于登录时解析盐值）
     */
    public static String md5WithSalt(String plainText) {
        // 1. 生成随机盐值（8位随机字符串，可自定义长度）
        String salt = generateSalt(8);
        // 2. 明文+盐值拼接后加密
        String saltedText = plainText + salt;
        String md5 = md5(saltedText);
        // 3. 盐值和密文拼接（用$分隔，登录时拆分）
        return salt + "$" + md5;
    }

    /**
     * 登录验证：验证明文密码是否匹配加盐MD5密文
     * @param plainText 前端传入的明文密码
     * @param saltedMd5 数据库存储的“盐值$密文”
     * @return 是否匹配
     */
    public static boolean verifySaltedMd5(String plainText, String saltedMd5) {
        if (plainText == null || saltedMd5 == null || !saltedMd5.contains("$")) {
            return false;
        }
        // 拆分盐值和密文
        String[] parts = saltedMd5.split("\\$");
        if (parts.length != 2) {
            return false;
        }
        String salt = parts[0];
        String md5InDb = parts[1];
        // 明文+盐值加密后对比
        String md5 = md5(plainText + salt);
        return md5.equals(md5InDb);
    }

    /**
     * 生成随机盐值（字母+数字）
     * @param length 盐值长度
     * @return 随机盐值
     */
    private static String generateSalt(int length) {
        String chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 将字节数组转为16进制字符串（MD5核心转换）
     * @param bytes 16字节的MD5摘要数组
     * @return 32位小写16进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            // 将字节转为16进制（& 0xFF避免负数问题）
            String hex = Integer.toHexString(b & 0xFF);
            // 单个字节不足两位时补0（比如0x05 → "05"，而非"5"）
            if (hex.length() == 1) {
                sb.append("0");
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}