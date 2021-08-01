package com.frontbackend.thymeleaf.bootstrap.repository;

import com.frontbackend.thymeleaf.bootstrap.model.Upload;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadRepository extends JpaRepository<Upload, Integer> {
}
