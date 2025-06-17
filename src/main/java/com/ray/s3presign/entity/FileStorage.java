package com.ray.s3presign.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "file_storage")
@Data
public class FileStorage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storageId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false, unique = true) // 指定外鍵欄位名稱
    private File file;

    @Column(name = "storage_provider")
    private String storageProvider;

    @Column(name = "storage_region")
    private String storageRegion;

    @Column(name = "bucket_name")
    private String bucketName;

    @Column(name = "bucket_key")
    private String bucketKey;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "etag")
    private String etag;
}
