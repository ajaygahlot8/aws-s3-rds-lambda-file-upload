package com.frontbackend.thymeleaf.bootstrap.upload;

import com.frontbackend.thymeleaf.bootstrap.upload.FileDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileDetailRepository extends JpaRepository<FileDetail, Integer> {
}
