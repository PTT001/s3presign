package com.ray.s3presign.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class PreSignedResponse {
    private String fileId;
    private String url;
    private Map<String, String> fields;
    private String originalFilename;
    private String objectKey;
}
