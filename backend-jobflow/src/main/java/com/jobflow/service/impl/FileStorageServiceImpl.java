package com.jobflow.service.impl;

import com.jobflow.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Local-disk storage behind the FileStorageService interface, so swapping in
 * S3/GCS later only touches this class. Every stored filename is a generated
 * UUID — the user's original filename is kept only as display metadata
 * (Document.fileName) and is never used to build a filesystem path, which is
 * what actually prevents path traversal here, not input sanitization.
 */
@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private final Path storageRoot;

    public FileStorageServiceImpl(@Value("${jobflow.storage.location:./uploads}") String location) {
        this.storageRoot = Paths.get(location).toAbsolutePath().normalize();
        try {
            Files.createDirectories(storageRoot);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not initialize storage directory: " + storageRoot, e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        String extension = extractExtension(file.getOriginalFilename());
        String generatedName = UUID.randomUUID() + extension;
        Path target = storageRoot.resolve(generatedName).normalize();

        if (!target.getParent().equals(storageRoot)) {
            // Defense in depth: this should be unreachable since generatedName is our own UUID, not user input.
            throw new IllegalStateException("Resolved storage path escaped the storage root");
        }

        try (var in = file.getInputStream()) {
            Files.copy(in, target);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store uploaded file", e);
        }

        return generatedName;
    }

    @Override
    public Resource loadAsResource(String storagePath) {
        try {
            Path file = storageRoot.resolve(storagePath).normalize();
            if (!file.getParent().equals(storageRoot)) {
                throw new ResourceNotFoundException("File not found");
            }
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("File not found");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("File not found");
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            Path file = storageRoot.resolve(storagePath).normalize();
            if (file.getParent().equals(storageRoot)) {
                Files.deleteIfExists(file);
            }
        } catch (IOException e) {
            log.warn("Failed to delete stored file {}: {}", storagePath, e.getMessage());
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null) return "";
        int dot = originalFilename.lastIndexOf('.');
        // Cap extension length so a malicious "filename" can't be used to smuggle a huge string into the path.
        return (dot >= 0 && dot < originalFilename.length() - 1 && originalFilename.length() - dot <= 10)
                ? originalFilename.substring(dot)
                : "";
    }
}
