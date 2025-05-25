package com.ray.s3presign.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * AWS 簽名工具類，負責生成 AWS4-HMAC-SHA256 簽名
 */
public class AwsSignatureUtils {

    /**
     * 生成 AWS4-HMAC-SHA256 簽名
     *
     * @param secretKey    AWS 秘密金鑰
     * @param date         日期（格式為 yyyyMMdd）
     * @param region       AWS 區域
     * @param policyBase64 Base64 編碼的策略
     * @return 簽名字串
     * @throws NoSuchAlgorithmException 如果 HMAC-SHA256 演算法不可用
     * @throws InvalidKeyException      如果金鑰無效
     */
    public static String generateSignature(String secretKey, String date, String region, String policyBase64)
            throws NoSuchAlgorithmException, InvalidKeyException {
        // 準備簽名金鑰
        String key = "AWS4" + secretKey;
        byte[] kSecret = key.getBytes(StandardCharsets.UTF_8);
        byte[] kDate = hmacSHA256(date, kSecret);
        byte[] kRegion = hmacSHA256(region, kDate);
        byte[] kService = hmacSHA256("s3", kRegion);
        byte[] kSigning = hmacSHA256("aws4_request", kService);

        // 對策略進行簽名
        byte[] signature = hmacSHA256(policyBase64, kSigning);
        return bytesToHex(signature);
    }

    /**
     * 執行 HMAC-SHA256 加密
     *
     * @param data 要加密的資料
     * @param key  加密金鑰
     * @return 加密後的位元組陣列
     * @throws NoSuchAlgorithmException 如果 HMAC-SHA256 演算法不可用
     * @throws InvalidKeyException      如果金鑰無效
     */
    private static byte[] hmacSHA256(String data, byte[] key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 將位元組陣列轉換為十六進位字串
     *
     * @param bytes 位元組陣列
     * @return 十六進位字串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
