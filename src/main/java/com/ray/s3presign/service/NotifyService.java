package com.ray.s3presign.service;

import com.ray.s3presign.entity.File;
import com.ray.s3presign.entity.FileStorage;
import com.ray.s3presign.repository.FileRepository;
import com.ray.s3presign.repository.FileStorageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotifyService {

    private final S3Client s3Client;
    private final FileRepository fileRepository;
    private final FileStorageRepository fileStorageRepository;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Transactional
    public void finalizeFileUpload(UUID fileId) {

        File fileEntity = fileRepository.findById(fileId).
                orElseThrow(() -> new IllegalStateException("找不到對應的待處理檔案，或檔案狀態不正確。File ID: " + fileId));

        FileStorage fileStorageEntity = fileStorageRepository.findByFile_FileId(fileId)
                .orElseThrow(() -> new IllegalStateException("找不到對應的待處理檔案，或檔案狀態不正確。File ID: " + fileId));

        //取得當初為這個檔案指定的 S3 object key
        String objectKey = fileStorageEntity.getBucketKey();
        if (objectKey == null || objectKey.isBlank()) {
            throw new IllegalStateException("檔案紀錄中缺少 S3 object key。File ID: " + fileId);
        }

        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            // 4. [核心] 執行 HeadObject。不下載檔案，只取回中繼資料。
            // 如果 S3 上沒這個檔案，這裡就會拋出例外。
            HeadObjectResponse s3ObjectMetadata = s3Client.headObject(headRequest);

            long fileSize = s3ObjectMetadata.contentLength();
            String etag = s3ObjectMetadata.eTag().replace("\"", "");

            fileStorageEntity.setFileSize(fileSize);
            fileStorageEntity.setEtag(etag);
            fileStorageRepository.save(fileStorageEntity);

            // 將 File 的狀態從 PENDING 更新為 COMPLETED
            fileEntity.setStatus(File.FileStatus.COMPLETED);
            fileRepository.save(fileEntity);
        } catch (NoSuchKeyException e) {
            fileEntity.setStatus(File.FileStatus.UPLOAD_FAILED);
            fileRepository.save(fileEntity);
            throw new RuntimeException("S3 上找不到檔案，驗證失敗。Key: " + objectKey, e);
        } catch (S3Exception e) {
            throw new RuntimeException("驗證 S3 檔案時發生錯誤。Key: " + objectKey, e);
        }
    }
}
