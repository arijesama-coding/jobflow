package com.jobflow.service.impl;

import com.jobflow.dto.request.DocumentUpdateRequest;
import com.jobflow.dto.response.DocumentResponse;
import com.jobflow.entity.*;
import com.jobflow.exception.ResourceNotFoundException;
import com.jobflow.mapper.DocumentMapper;
import com.jobflow.repository.ApplicationRepository;
import com.jobflow.repository.DocumentRepository;
import com.jobflow.security.CurrentUserProvider;
import com.jobflow.service.DocumentService;
import com.jobflow.service.FileStorageService;
import com.jobflow.specification.DocumentSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final ApplicationRepository applicationRepository;
    private final FileStorageService fileStorageService;
    private final DocumentMapper documentMapper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public DocumentResponse upload(MultipartFile file, DocumentType type, UUID applicationId, UUID replaceDocumentId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalStateException("No file provided");
        }

        User user = currentUserProvider.getCurrentUser();
        Application application = resolveOwnedApplication(applicationId, user);

        int version = 1;
        boolean primary = false;

        if (replaceDocumentId != null) {
            Document previous = getOwnedOrThrow(replaceDocumentId);
            version = previous.getVersion() + 1;
            primary = previous.isPrimary();
        }

        String storagePath = fileStorageService.store(file);

        Document document = Document.builder()
                .user(user)
                .application(application)
                .type(type)
                .fileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "document")
                .storagePath(storagePath)
                .version(version)
                .primary(primary)
                .build();

        documentRepository.save(document);

        if (primary) {
            // Re-run the same "only one primary per group" rule the explicit setPrimary() uses.
            clearOtherPrimaries(document);
        }

        return documentMapper.toResponse(document);
    }

    @Override
    public Page<DocumentResponse> list(UUID applicationId, DocumentType type, Pageable pageable) {
        User user = currentUserProvider.getCurrentUser();
        var spec = DocumentSpecification.build(user, applicationId, type);
        return documentRepository.findAll(spec, pageable).map(documentMapper::toResponse);
    }

    @Override
    @Transactional
    public DocumentResponse rename(UUID id, DocumentUpdateRequest request) {
        Document document = getOwnedOrThrow(id);
        document.setFileName(request.getFileName());
        return documentMapper.toResponse(documentRepository.save(document));
    }

    @Override
    @Transactional
    public DocumentResponse setPrimary(UUID id) {
        Document document = getOwnedOrThrow(id);
        document.setPrimary(true);
        documentRepository.save(document);
        clearOtherPrimaries(document);
        return documentMapper.toResponse(document);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Document document = getOwnedOrThrow(id);
        fileStorageService.delete(document.getStoragePath());
        documentRepository.delete(document);
    }

    @Override
    public DownloadPayload download(UUID id) {
        Document document = getOwnedOrThrow(id);
        Resource resource = fileStorageService.loadAsResource(document.getStoragePath());
        return new DownloadPayload(document, resource);
    }

    // ===================== HELPERS =====================

    /** Only one primary document per (user, type, application) group — mirrors the "document principal" rule in spec section 16. */
    private void clearOtherPrimaries(Document justSetPrimary) {
        List<Document> siblings = justSetPrimary.getApplication() != null
                ? documentRepository.findByUser_IdAndTypeAndApplication_Id(
                        justSetPrimary.getUser().getId(), justSetPrimary.getType(), justSetPrimary.getApplication().getId())
                : documentRepository.findByUser_IdAndTypeAndApplicationIsNull(
                        justSetPrimary.getUser().getId(), justSetPrimary.getType());

        for (Document sibling : siblings) {
            if (!sibling.getId().equals(justSetPrimary.getId()) && sibling.isPrimary()) {
                sibling.setPrimary(false);
                documentRepository.save(sibling);
            }
        }
    }

    private Application resolveOwnedApplication(UUID applicationId, User user) {
        if (applicationId == null) return null;
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (application.getDeletedAt() != null || !application.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Application not found");
        }
        return application;
    }

    private Document getOwnedOrThrow(UUID id) {
        User user = currentUserProvider.getCurrentUser();
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        if (!document.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Document not found");
        }
        return document;
    }
}
