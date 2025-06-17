package com.ray.s3presign.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "file")
@Data
public class File {

    public enum FileStatus {
        PENDING,
        COMPLETED,
        UPLOAD_FAILED
    }

    public enum Role {
        MAIN,
        THUMBNAIL,
        ATTACHMENT
    }

    @Id
    @Column(name = "file_id", columnDefinition = "CHAR(36)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID fileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resources_id", nullable = false)
    private Resources resources;

    @Column(name = "filename")
    private String filename;

    @Column(name = "mime_type")
    private String mimeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private FileStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @Column(name = "file_extension")
    private String fileExtension;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    public void onPrePersist() {
        if (this.fileId == null) {
            this.fileId = UUID.randomUUID();
        }
    }
}

