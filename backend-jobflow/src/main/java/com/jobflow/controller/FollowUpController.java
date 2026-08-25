package com.jobflow.controller;

import com.jobflow.dto.request.FollowUpRequest;
import com.jobflow.dto.response.FollowUpResponse;
import com.jobflow.dto.response.FollowUpStatsResponse;
import com.jobflow.entity.FollowUpStatus;
import com.jobflow.entity.FollowUpType;
import com.jobflow.service.FollowUpService;
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
@RequestMapping("/api/follow-ups")
@RequiredArgsConstructor
public class FollowUpController {

    private final FollowUpService followUpService;

    @GetMapping
    public Page<FollowUpResponse> list(
            @RequestParam(required = false) UUID applicationId,
            @RequestParam(required = false) FollowUpType type,
            @RequestParam(required = false) FollowUpStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @PageableDefault(size = 20, sort = "followUpDate") Pageable pageable) {
        return followUpService.list(applicationId, type, status, dateFrom, dateTo, pageable);
    }

    @GetMapping("/calendar")
    public List<FollowUpResponse> calendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return followUpService.calendar(from, to);
    }

    @GetMapping("/stats")
    public FollowUpStatsResponse getStats() {
        return followUpService.getStats();
    }

    @GetMapping("/{id}")
    public FollowUpResponse get(@PathVariable UUID id) {
        return followUpService.get(id);
    }

    @PostMapping
    public ResponseEntity<FollowUpResponse> create(@Valid @RequestBody FollowUpRequest request) {
        return ResponseEntity.status(201).body(followUpService.create(request));
    }

    @PutMapping("/{id}")
    public FollowUpResponse update(@PathVariable UUID id, @Valid @RequestBody FollowUpRequest request) {
        return followUpService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        followUpService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
