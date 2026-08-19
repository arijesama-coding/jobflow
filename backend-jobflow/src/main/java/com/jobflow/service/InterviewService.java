package com.jobflow.service;

import com.jobflow.dto.request.InterviewRequest;
import com.jobflow.dto.response.InterviewResponse;
import com.jobflow.entity.InterviewResult;
import com.jobflow.entity.InterviewType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface InterviewService {
    InterviewResponse create(InterviewRequest request);
    InterviewResponse update(UUID id, InterviewRequest request);
    void delete(UUID id);
    InterviewResponse get(UUID id);
    Page<InterviewResponse> list(UUID applicationId, InterviewType type, InterviewResult result,
                                  LocalDateTime from, LocalDateTime to, Pageable pageable);
    List<InterviewResponse> calendar(LocalDateTime from, LocalDateTime to);
}
