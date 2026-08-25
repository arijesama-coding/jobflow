package com.jobflow.controller;

import com.jobflow.dto.request.DocumentUpdateRequest;
import com.jobflow.dto.response.DocumentResponse;
import com.jobflow.entity.DocumentType;
import com.jobflow.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    public Page<DocumentResponse> list(
            @RequestParam(required = false) UUID applicationId,
            @RequestParam(required = false) DocumentType type,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return documentService.list(applicationId, type, pageable);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam DocumentType type,
            @RequestParam(required = false) UUID applicationId,
            @RequestParam(required = false) UUID replaceDocumentId) {
        return ResponseEntity.status(201).body(documentService.upload(file, type, applicationId, replaceDocumentId));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable UUID id) {
        var payload = documentService.download(id);
        String encodedName = URLEncoder.encode(payload.document().getFileName(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(payload.resource());
    }

    @PatchMapping("/{id}")
    public DocumentResponse rename(@PathVariable UUID id, @Valid @RequestBody DocumentUpdateRequest request) {
        return documentService.rename(id, request);
    }

    @PatchMapping("/{id}/primary")
    public DocumentResponse setPrimary(@PathVariable UUID id) {
        return documentService.setPrimary(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
