package com.ray.s3presign.repository;

import com.ray.s3presign.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends JpaRepository<File, UUID> {

//    Optional<File> findByFileIdAndStatus(UUID fileId, File.FileStatus status);
}
