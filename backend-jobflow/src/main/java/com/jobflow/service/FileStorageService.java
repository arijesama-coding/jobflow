package com.jobflow.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /** Saves the file under a generated name and returns the relative storage path (never the original filename). */
    String store(MultipartFile file);

    Resource loadAsResource(String storagePath);

    void delete(String storagePath);
}
