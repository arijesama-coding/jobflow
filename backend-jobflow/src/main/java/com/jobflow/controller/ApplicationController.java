package com.jobflow.controller;

import com.jobflow.dto.request.ApplicationRequest;
import com.jobflow.dto.request.ApplicationStatusUpdateRequest;
import com.jobflow.dto.response.ApplicationResponse;
import com.jobflow.dto.response.ApplicationStatusHistoryResponse;
import com.jobflow.entity.ApplicationStatus;
import com.jobflow.entity.Priority;
import com.jobflow.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping
    public Page<ApplicationResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return applicationService.list(search, status, priority, companyId, dateFrom, dateTo, pageable);
    }

    @GetMapping("/{id}")
    public ApplicationResponse get(@PathVariable UUID id) {
        return applicationService.get(id);
    }

    @GetMapping("/{id}/history")
    public List<ApplicationStatusHistoryResponse> getHistory(@PathVariable UUID id) {
        return applicationService.getStatusHistory(id);
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> create(@Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.status(201).body(applicationService.create(request));
    }

    @PutMapping("/{id}")
    public ApplicationResponse update(@PathVariable UUID id, @Valid @RequestBody ApplicationRequest request) {
        return applicationService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public ApplicationResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody ApplicationStatusUpdateRequest request) {
        return applicationService.updateStatus(id, request.getStatus());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        applicationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
