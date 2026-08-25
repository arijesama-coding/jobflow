package com.jobflow.service;

import com.jobflow.dto.request.DocumentUpdateRequest;
import com.jobflow.dto.response.DocumentResponse;
import com.jobflow.entity.Document;
import com.jobflow.entity.DocumentType;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface DocumentService {
    DocumentResponse upload(MultipartFile file, DocumentType type, UUID applicationId, UUID replaceDocumentId);
    Page<DocumentResponse> list(UUID applicationId, DocumentType type, Pageable pageable);
    DocumentResponse rename(UUID id, DocumentUpdateRequest request);
    DocumentResponse setPrimary(UUID id);
    void delete(UUID id);

    /** Returns the owned Document + a loaded Resource together, since downloading needs both the file bytes and the display filename. */
    record DownloadPayload(Document document, Resource resource) {}
    DownloadPayload download(UUID id);
}
