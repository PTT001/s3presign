package com.ray.s3presign.repository;

import com.ray.s3presign.entity.File;
import com.ray.s3presign.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    List<FileMetadata> findByFile(File file);

    List<FileMetadata> findByFile_FileId(UUID fileId);
}
