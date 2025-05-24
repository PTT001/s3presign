package com.ray.s3presign.controller;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPost;
import software.amazon.awssdk.services.s3.model.PostPolicyConditions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
public class S3Controller {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @GetMapping("/generate-presigned-post")
    public Map<String, String> generatePresignedPost(@RequestParam String fileName) {
        // 配置 AWS 認證
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);

        // 配置 S3 Presigner
        try (S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build()) {

            // 設置 POST 策略條件
            PostPolicyConditions conditions = PostPolicyConditions.builder()
                    .addCondition("starts-with", "$Content-Type", "image/") // 限制檔案類型
                    .addCondition("eq", "$key", fileName) // 指定檔案名稱
                    .build();

            // 設置 POST 請求
            PresignedPost presignedPost = presigner.presignPost(b -> b
                    .bucket(bucketName)
                    .key(fileName)
                    .policyConditions(conditions)
                    .signatureDuration(Duration.ofMinutes(10)) // 有效期限 10 分鐘
            );

            // 返回 POST URL 和必要的欄位
            Map<String, String> response = new HashMap<>();
            response.put("url", presignedPost.url().toString());
            response.putAll(presignedPost.signedFormFields());
            return response;
        }
    }
}