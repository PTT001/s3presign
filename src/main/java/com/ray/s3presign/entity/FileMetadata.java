package com.ray.s3presign.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "file_metadata")
@Data
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long metadataId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    @Column(name = "metadata_key")
    private String metadataKey;

    @Column(name = "metadata_value", columnDefinition = "TEXT")
    private String metadataValue;
}
