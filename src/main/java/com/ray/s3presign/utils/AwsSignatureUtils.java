package com.ray.s3presign.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AwsSignatureUtils {

    public static String generateSignature(String secretKey, String date, String region, String policyBase64)
            throws NoSuchAlgorithmException, InvalidKeyException {

        String key = "AWS4" + secretKey;
        byte[] kSecret = key.getBytes(StandardCharsets.UTF_8);
        byte[] kDate = hmacSHA256(date, kSecret);
        byte[] kRegion = hmacSHA256(region, kDate);
        byte[] kService = hmacSHA256("s3", kRegion);
        byte[] kSigning = hmacSHA256("aws4_request", kService);

        byte[] signature = hmacSHA256(policyBase64, kSigning);
        return bytesToHex(signature);
    }

    private static byte[] hmacSHA256(String data, byte[] key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
