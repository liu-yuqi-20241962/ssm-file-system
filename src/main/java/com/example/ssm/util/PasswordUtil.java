package com.example.ssm.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 密码工具类：SHA-256 加盐。
 * 算法：hash = SHA-256(明文密码 + 盐)，输出 64 位小写十六进制串。
 * 盐值每个用户随机生成，与哈希分开存储，登录校验时用同样的盐重新计算比对。
 */
public final class PasswordUtil {

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private PasswordUtil() {
    }

    /**
     * 生成随机盐值（16 字节 -> 32 位十六进制串）。
     */
    public static String generateSalt() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return toHex(bytes);
    }

    /**
     * 计算 SHA-256(明文密码 + 盐)。
     */
    public static String sha256(String rawPassword, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((rawPassword + salt).getBytes(StandardCharsets.UTF_8));
            return toHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 算法不可用", e);
        }
    }

    /**
     * 校验明文密码是否与库中哈希一致。
     */
    public static boolean verify(String rawPassword, String salt, String storedHash) {
        if (rawPassword == null || salt == null || storedHash == null) {
            return false;
        }
        String computed = sha256(rawPassword, salt);
        // 使用常量时间比较，避免时序侧信道
        return MessageDigest.isEqual(
                computed.getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8));
    }

    private static String toHex(byte[] bytes) {
        char[] out = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i] & 0xFF;
            out[i * 2] = HEX[b >>> 4];
            out[i * 2 + 1] = HEX[b & 0x0F];
        }
        return new String(out);
    }
}
