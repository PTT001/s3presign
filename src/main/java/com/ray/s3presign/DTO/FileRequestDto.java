package com.ray.s3presign.DTO;

import lombok.Data;

import java.util.List;

@Data
public class FileRequestDto {
    private String ownerId;
    private String resourceType;
    private List<FileDto> files;
}
