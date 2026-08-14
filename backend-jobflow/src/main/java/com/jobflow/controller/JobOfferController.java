package com.jobflow.controller;

import com.jobflow.dto.request.JobOfferRequest;
import com.jobflow.dto.response.JobOfferResponse;
import com.jobflow.entity.ContractType;
import com.jobflow.entity.RemoteType;
import com.jobflow.service.JobOfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobOfferController {

    private final JobOfferService jobOfferService;

    @GetMapping
    public Page<JobOfferResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) RemoteType remoteType,
            @RequestParam(required = false) ContractType contractType,
            @RequestParam(required = false) Boolean favorite,
            @RequestParam(required = false) Boolean archived,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return jobOfferService.list(search, companyId, remoteType, contractType, favorite, archived, pageable);
    }

    @GetMapping("/{id}")
    public JobOfferResponse get(@PathVariable UUID id) {
        return jobOfferService.get(id);
    }

    @PostMapping
    public ResponseEntity<JobOfferResponse> create(@Valid @RequestBody JobOfferRequest request) {
        return ResponseEntity.status(201).body(jobOfferService.create(request));
    }

    @PutMapping("/{id}")
    public JobOfferResponse update(@PathVariable UUID id, @Valid @RequestBody JobOfferRequest request) {
        return jobOfferService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        jobOfferService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/favorite")
    public JobOfferResponse toggleFavorite(@PathVariable UUID id) {
        return jobOfferService.toggleFavorite(id);
    }

    @PatchMapping("/{id}/archive")
    public JobOfferResponse toggleArchived(@PathVariable UUID id) {
        return jobOfferService.toggleArchived(id);
    }
}
