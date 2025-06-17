package com.ray.s3presign.repository;

import com.ray.s3presign.entity.Resources;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resources, Long> {
}
