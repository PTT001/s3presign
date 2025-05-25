package com.ray.s3presign.service;

import com.ray.s3presign.utils.AwsSignatureUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * S3 預簽署服務類，負責生成 POST 預簽署 URL 的邏輯
 */
@Service
public class S3PresignService {

    // 注入儲存桶名稱
    @Value("${aws.s3.bucket}")
    private String bucketName;

    // 注入區域名稱
    @Value("${aws.region}")
    private String region;

    // 注入 AWS 存取金鑰
    @Value("${aws.access.key}")
    private String awsAccessKey;

    // 注入 AWS 秘密金鑰
    @Value("${aws.secret.key}")
    private String awsSecretKey;

    /**
     * 生成 S3 POST 預簽署表單資料
     *
     * @param fileName    檔案名稱
     * @param contentType 檔案內容類型
     * @return 包含表單欄位的 Map
     * @throws Exception 如果簽名生成失敗
     */

    public Map<String, Object> generatePresignedPostData(String fileName, String contentType) throws Exception {
        // 生成唯一的檔案鍵
        String key = "upload/" + UUID.randomUUID() + "-" + fileName;

        // 準備 AWS 日期格式
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        String amzDate = dateFormat.format(new Date());
        String credentialDate = amzDate.substring(0, 8);

        // 構建認證範圍
        String credentialScope = credentialDate + "/" + region + "/s3/aws4_request";

        // 構建策略 JSON，使用正確的 ISO 8601 格式
        SimpleDateFormat expirationFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        expirationFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        String expiration = expirationFormat.format(new Date(System.currentTimeMillis() + 3600 * 1000)); // 1 小時到期

        String policy = "{\"expiration\": \"" + expiration + "\"," +
                "\"conditions\": [" +
                "{\"bucket\": \"" + bucketName + "\"}," +
                "[\"starts-with\", \"$key\", \"" + key + "\"]," +
                "{\"acl\": \"private\"}," +
                "[\"starts-with\", \"$Content-Type\", \"" + contentType + "\"]," +
                "{\"x-amz-credential\": \"" + awsAccessKey + "/" + credentialScope + "\"}," +
                "{\"x-amz-algorithm\": \"AWS4-HMAC-SHA256\"}," +
                "{\"x-amz-date\": \"" + amzDate + "\"}," + // 添加逗號
                "[\"content-length-range\", 1024, 10485760]" +
                "]}";

        // 將策略編碼為 Base64
        String policyBase64 = Base64.getEncoder().encodeToString(policy.getBytes("UTF-8"));

        // 生成簽名
        String signature = AwsSignatureUtils.generateSignature(awsSecretKey, credentialDate, region, policyBase64);

        // 準備 fields 欄位
        Map<String, String> fields = new HashMap<>();
        fields.put("key", key);
        fields.put("acl", "private");
        fields.put("Content-Type", contentType);
        fields.put("x-amz-algorithm", "AWS4-HMAC-SHA256");
        fields.put("x-amz-credential", awsAccessKey + "/" + credentialScope);
        fields.put("x-amz-date", amzDate);
        fields.put("policy", policyBase64);
        fields.put("x-amz-signature", signature);

        // 構建回應
        Map<String, Object> response = new HashMap<>();
        response.put("url", "https://" + bucketName + ".s3." + region + ".amazonaws.com");
        response.put("fields", fields); // 將欄位封裝到 fields 物件

        return response;
    }
}
