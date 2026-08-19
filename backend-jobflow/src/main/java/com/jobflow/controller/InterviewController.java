package com.jobflow.controller;

import com.jobflow.dto.request.InterviewRequest;
import com.jobflow.dto.response.InterviewResponse;
import com.jobflow.entity.InterviewResult;
import com.jobflow.entity.InterviewType;
import com.jobflow.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @GetMapping
    public Page<InterviewResponse> list(
            @RequestParam(required = false) UUID applicationId,
            @RequestParam(required = false) InterviewType type,
            @RequestParam(required = false) InterviewResult result,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20, sort = "scheduledAt") Pageable pageable) {
        return interviewService.list(applicationId, type, result, from, to, pageable);
    }

    @GetMapping("/calendar")
    public List<InterviewResponse> calendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return interviewService.calendar(from, to);
    }

    @GetMapping("/{id}")
    public InterviewResponse get(@PathVariable UUID id) {
        return interviewService.get(id);
    }

    @PostMapping
    public ResponseEntity<InterviewResponse> create(@Valid @RequestBody InterviewRequest request) {
        return ResponseEntity.status(201).body(interviewService.create(request));
    }

    @PutMapping("/{id}")
    public InterviewResponse update(@PathVariable UUID id, @Valid @RequestBody InterviewRequest request) {
        return interviewService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        interviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
