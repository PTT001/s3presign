package com.ray.s3presign.config;

import com.amazonaws.SdkClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AWS S3 配置類，負責初始化 S3 客戶端
 */
@Configuration
public class S3Config {

    // 從配置檔案讀取 AWS 存取金鑰
    @Value("${aws.access.key}")
    private String awsAccessKey;

    // 從配置檔案讀取 AWS 秘密金鑰
    @Value("${aws.secret.key}")
    private String awsSecretKey;

    // 從配置檔案讀取 S3 儲存桶名稱
    @Value("${aws.s3.bucket}")
    private String bucketName;

    // 從配置檔案讀取 AWS 區域
    @Value("${aws.region}")
    private String region;

    /**
     * 創建並配置 AmazonS3 客戶端
     *
     * @return 配置好的 AmazonS3 實例
     */
    @Bean
    public AmazonS3 amazonS3Client() {
//         使用存取金鑰和秘密金鑰創建認證物件
        BasicAWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);

//         構建 S3 客戶端，設置認證和區域
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
    }

    /**
     * 提供儲存桶名稱給其他組件
     *
     * @return 儲存桶名稱
     */
    @Bean
    public String bucketName() {
        return bucketName;
    }

    /**
     * 提供區域名稱給其他組件
     *
     * @return 區域名稱
     */
    @Bean
    public String region() {
        return region;
    }
}
