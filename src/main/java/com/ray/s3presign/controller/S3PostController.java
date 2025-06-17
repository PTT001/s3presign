package com.ray.s3presign.controller;

import com.ray.s3presign.DTO.FileRequestDto;
import com.ray.s3presign.DTO.PreSignedResponse;
import com.ray.s3presign.service.S3PreSignService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
public class S3PostController {

    private S3PreSignService s3PresignService;

    @PostMapping("/generate-preSigned-post")
    public ResponseEntity<List<PreSignedResponse>> generatePreSignedPost(@RequestBody FileRequestDto request) {

        try {
            List<PreSignedResponse> results = s3PresignService.generatePreSignedPostsForAsset(
                    request.getOwnerId(),
                    request.getResourceType(),
                    request.getFiles()
            );
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }
}