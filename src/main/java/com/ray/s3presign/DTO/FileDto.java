package com.ray.s3presign.DTO;

import lombok.Data;

import java.util.List;

@Data
public class FileDto {

    private String filename;

    private long fileSize;

    private String mimeType;

    private String fileExtension;

    private String role;

    private String fileType;

    private List<MetadataDto> metadata;
}
