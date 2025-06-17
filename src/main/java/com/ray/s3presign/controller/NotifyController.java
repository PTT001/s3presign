package com.ray.s3presign.controller;

import com.ray.s3presign.service.NotifyService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class NotifyController {

    private final NotifyService fileService;

    @PostMapping("/finalize/{fileId}")
    public ResponseEntity<String> finalizeUpload(@PathVariable UUID fileId) {
        System.out.println("收到上傳完成通知，準備驗證 File ID: " + fileId);

        fileService.finalizeFileUpload(fileId);

        return ResponseEntity.ok("檔案驗證成功並已記錄。");
    }
}

