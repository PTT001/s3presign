package com.ray.s3presign.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.ray.s3presign.service.S3PresignService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * S3 POST 預簽署控制器，處理客戶端請求並返回預簽署表單資料
 */
@RestController
@Data
@AllArgsConstructor
public class S3PostController {

    // 注入 S3 預簽署服務
    private S3PresignService s3PresignService;

    /**
     * 生成 S3 POST 預簽署 URL 的 API 端點
     *
     * @param metadata 檔案元資料（包含檔案名稱和內容類型）
     * @return 包含表單欄位的響應
     */
    @PostMapping("/generate-presigned-post")
    @CrossOrigin(origins = "http://127.0.0.1:5500")
    public ResponseEntity<Map<String, Object>> generatePresignedPost(@RequestBody FileMetadata metadata) {
        try {
            // 調用服務生成預簽署表單資料
            Map<String, Object> presignedPostData = s3PresignService.generatePresignedPostData(
                    metadata.getFileName(), metadata.getContentType());

            System.out.println(presignedPostData);
            return ResponseEntity.ok(presignedPostData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "無法生成預簽署 POST URL"));
        }
    }

    /**
     * 檔案元資料 DTO
     */
    @Data
    public static class FileMetadata {
        private String fileName;
        private String contentType;
    }
}