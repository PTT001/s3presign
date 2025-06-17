package com.ray.s3presign.repository;

import com.ray.s3presign.entity.File;
import com.ray.s3presign.entity.FileStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileStorageRepository extends JpaRepository<FileStorage, Long> {

    Optional<FileStorage> findByFile(File file);

    Optional<FileStorage> findByFile_FileId(UUID fileId);
}
