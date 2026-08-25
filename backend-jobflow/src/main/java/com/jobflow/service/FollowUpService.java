package com.jobflow.service;

import com.jobflow.dto.request.FollowUpRequest;
import com.jobflow.dto.response.FollowUpResponse;
import com.jobflow.dto.response.FollowUpStatsResponse;
import com.jobflow.entity.FollowUpStatus;
import com.jobflow.entity.FollowUpType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FollowUpService {
    FollowUpResponse create(FollowUpRequest request);
    FollowUpResponse update(UUID id, FollowUpRequest request);
    void delete(UUID id);
    FollowUpResponse get(UUID id);
    Page<FollowUpResponse> list(UUID applicationId, FollowUpType type, FollowUpStatus status,
                                 LocalDate dateFrom, LocalDate dateTo, Pageable pageable);
    List<FollowUpResponse> calendar(LocalDate from, LocalDate to);
    FollowUpStatsResponse getStats();
}
