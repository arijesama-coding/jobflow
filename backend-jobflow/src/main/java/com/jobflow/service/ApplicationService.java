package com.jobflow.service;

import com.jobflow.dto.request.ApplicationRequest;
import com.jobflow.dto.response.ApplicationResponse;
import com.jobflow.dto.response.ApplicationStatusHistoryResponse;
import com.jobflow.entity.ApplicationStatus;
import com.jobflow.entity.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ApplicationService {
    ApplicationResponse create(ApplicationRequest request);
    ApplicationResponse update(UUID id, ApplicationRequest request);
    void delete(UUID id);
    ApplicationResponse get(UUID id);
    Page<ApplicationResponse> list(String search, ApplicationStatus status, Priority priority, UUID companyId,
                                    LocalDate dateFrom, LocalDate dateTo, Pageable pageable);
    ApplicationResponse updateStatus(UUID id, ApplicationStatus newStatus);
    List<ApplicationStatusHistoryResponse> getStatusHistory(UUID id);
}
