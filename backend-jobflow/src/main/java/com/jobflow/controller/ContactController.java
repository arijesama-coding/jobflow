package com.jobflow.controller;

import com.jobflow.dto.request.ContactRequest;
import com.jobflow.dto.response.ContactResponse;
import com.jobflow.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @GetMapping
    public Page<ContactResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID applicationId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return contactService.list(search, companyId, applicationId, pageable);
    }

    @GetMapping("/{id}")
    public ContactResponse get(@PathVariable UUID id) {
        return contactService.get(id);
    }

    @PostMapping
    public ResponseEntity<ContactResponse> create(@Valid @RequestBody ContactRequest request) {
        return ResponseEntity.status(201).body(contactService.create(request));
    }

    @PutMapping("/{id}")
    public ContactResponse update(@PathVariable UUID id, @Valid @RequestBody ContactRequest request) {
        return contactService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        contactService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/applications/{applicationId}")
    public ContactResponse link(@PathVariable UUID id, @PathVariable UUID applicationId) {
        return contactService.linkToApplication(id, applicationId);
    }

    @DeleteMapping("/{id}/applications/{applicationId}")
    public ContactResponse unlink(@PathVariable UUID id, @PathVariable UUID applicationId) {
        return contactService.unlinkFromApplication(id, applicationId);
    }
}
