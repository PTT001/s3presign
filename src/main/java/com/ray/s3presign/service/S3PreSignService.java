package com.ray.s3presign.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ray.s3presign.DTO.FileDto;
import com.ray.s3presign.DTO.MetadataDto;
import com.ray.s3presign.DTO.PreSignedResponse;
import com.ray.s3presign.entity.FileStorage;
import com.ray.s3presign.entity.Resources;
import com.ray.s3presign.entity.File;
import com.ray.s3presign.entity.FileMetadata;
import com.ray.s3presign.repository.ResourceRepository;
import com.ray.s3presign.repository.FileMetadataRepository;
import com.ray.s3presign.repository.FileRepository;
import com.ray.s3presign.repository.FileStorageRepository;
import com.ray.s3presign.utils.AwsSignatureUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.ray.s3presign.entity.File.FileStatus.PENDING;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3PreSignService {

    private final ResourceRepository assetRepository;
    private final FileRepository fileRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final FileStorageRepository fileStorageRepository;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.access.key}")
    private String awsAccessKey;

    @Value("${aws.secret.key}")
    private String awsSecretKey;

    private final ObjectMapper objectMapper;

    public List<PreSignedResponse> generatePreSignedPostsForAsset(String ownerId, String resourceType, List<FileDto> fileDtos) throws Exception {

        Resources resources = new Resources();
        resources.setOwnerId(Long.parseLong(ownerId));
        resources.setResourceType(Resources.resourceType.valueOf(resourceType));
        Resources savedResource = assetRepository.save(resources);

        List<PreSignedResponse> results = new ArrayList<>();

        for (FileDto fileDto : fileDtos) {
            File fileEntity = new File();
            fileEntity.setResources(savedResource);
            fileEntity.setRole(File.Role.valueOf(fileDto.getRole()));
            fileEntity.setFilename(fileDto.getFilename());
            fileEntity.setFileExtension(fileDto.getFileExtension());
            fileEntity.setMimeType(fileDto.getMimeType());
            fileEntity.setStatus(PENDING);

            // After saving, the entity will have its generated UUID.
            File savedFile = fileRepository.save(fileEntity);
            UUID fileId = savedFile.getFileId();

            List<MetadataDto> metadataDtos = fileDto.getMetadata();

            // 2. 檢查 DTO 中是否存在 metadata
            if (metadataDtos != null && !metadataDtos.isEmpty()) {

                List<FileMetadata> metadataEntities = metadataDtos.stream()
                        .map(metadataDto -> {
                            FileMetadata metadataEntity = new FileMetadata();
                            metadataEntity.setFile(savedFile);
                            metadataEntity.setMetadataKey(metadataDto.getMetadataKey());
                            metadataEntity.setMetadataValue(metadataDto.getMetadataValue());
                            return metadataEntity;
                        }).collect(Collectors.toList());

                fileMetadataRepository.saveAll(metadataEntities);
            }

            String fileName = fileDto.getFilename();
            String objectKey = "upload/" + fileId + "-" + fileName;

            FileStorage storage = new FileStorage();
            storage.setFile(fileEntity);
            storage.setStorageProvider("S3");
            storage.setStorageRegion(region);
            storage.setBucketName(bucketName);
            storage.setBucketKey(objectKey);
            fileStorageRepository.save(storage);

            PreSignedResponse preSignedPostData = createS3PreSignedPost(fileId, objectKey, fileDto);

            results.add(preSignedPostData);
        }

        return results;
    }

    public PreSignedResponse createS3PreSignedPost(UUID fileId, String objectKey, FileDto fileDto) throws Exception {
        // 1. 從傳入的 fileDto 獲取檔名和類型
        String fileName = fileDto.getFilename();
        String contentType = fileDto.getMimeType();

        // 3. 使用 java.time 處理日期 (執行緒安全)
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Taipei")); // AWS 標準使用 UTC 時間
        String amzDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
        String credentialDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 4. 設定過期時間
        ZonedDateTime expirationTime = now.plus(Duration.ofHours(1));
        String expiration = expirationTime.format(DateTimeFormatter.ISO_INSTANT); // 正確的 ISO 8601 格式

        // 5. 構建認證範圍
        String credentialScope = credentialDate + "/" + region + "/s3/aws4_request";
        String credential = awsAccessKey + "/" + credentialScope;

        final long maxFileSize = 10 * 1024 * 1024; // 10MB in bytes

        Map<String, Object> policyMap = new HashMap<>();
        policyMap.put("expiration", expiration);
        policyMap.put("conditions", List.of(
                Map.of("bucket", bucketName),
                List.of("starts-with", "$key", objectKey),
                Map.of("acl", "private"),
                List.of("starts-with", "$Content-Type", contentType),
                Map.of("x-amz-credential", credential),
                Map.of("x-amz-algorithm", "AWS4-HMAC-SHA256"),
                Map.of("x-amz-date", amzDate),
                List.of("content-length-range", 1, maxFileSize)
        ));
        String policy = objectMapper.writeValueAsString(policyMap);

        // 7. 將 policy 編碼為 Base64
        String policyBase64 = Base64.getEncoder().encodeToString(policy.getBytes(StandardCharsets.UTF_8));

        // 8. 生成簽名
        String signature = AwsSignatureUtils.generateSignature(awsSecretKey, credentialDate, region, policyBase64);

        // 9. 準備回傳的 fields
        Map<String, String> fields = new HashMap<>();
        fields.put("key", objectKey);
        fields.put("acl", "private");
        fields.put("Content-Type", contentType);
        fields.put("x-amz-credential", credential);
        fields.put("x-amz-algorithm", "AWS4-HMAC-SHA256");
        fields.put("x-amz-date", amzDate);
        fields.put("policy", policyBase64);
        fields.put("x-amz-signature", signature);

        // 10. 封裝成 DTO 回傳
        return PreSignedResponse.builder()
                .fileId(fileId.toString())
                .url("https://" + bucketName + ".s3." + region + ".amazonaws.com")
                .fields(fields)
                .originalFilename(fileName)
                .objectKey(objectKey)
                .build();
    }
}
