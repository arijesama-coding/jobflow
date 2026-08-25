package com.jobflow.service;

import com.jobflow.exception.ResourceNotFoundException;
import com.jobflow.service.impl.FileStorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The one rule that must never break here: a stored file is only ever
 * reachable through its generated UUID name, and a path-traversal attempt
 * against loadAsResource must not escape the storage root.
 */
class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageServiceImpl storageService;

    @BeforeEach
    void setUp() {
        storageService = new FileStorageServiceImpl(tempDir.toString());
    }

    @Test
    void store_generatesRandomFilename_notTheOriginalName() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", "content".getBytes());

        String storagePath = storageService.store(file);

        assertThat(storagePath).endsWith(".pdf");
        assertThat(storagePath).doesNotContain("resume");
    }

    @Test
    void loadAsResource_returnsStoredContent_forAGenuineStoredFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", "hello world".getBytes());
        String storagePath = storageService.store(file);

        Resource resource = storageService.loadAsResource(storagePath);

        assertThat(resource.exists()).isTrue();
        assertThat(new String(resource.getInputStream().readAllBytes())).isEqualTo("hello world");
    }

    @Test
    void loadAsResource_rejectsPathTraversalAttempt() {
        assertThatThrownBy(() -> storageService.loadAsResource("../../etc/passwd"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void loadAsResource_rejectsNonExistentFile() {
        assertThatThrownBy(() -> storageService.loadAsResource("00000000-0000-0000-0000-000000000000.pdf"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
