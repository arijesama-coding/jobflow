package com.jobflow.service.impl;

import com.jobflow.dto.request.ApplicationRequest;
import com.jobflow.dto.response.ApplicationResponse;
import com.jobflow.dto.response.ApplicationStatusHistoryResponse;
import com.jobflow.entity.*;
import com.jobflow.exception.ResourceNotFoundException;
import com.jobflow.mapper.ApplicationMapper;
import com.jobflow.repository.ApplicationRepository;
import com.jobflow.repository.ApplicationStatusHistoryRepository;
import com.jobflow.repository.CompanyRepository;
import com.jobflow.repository.JobOfferRepository;
import com.jobflow.security.CurrentUserProvider;
import com.jobflow.service.ApplicationService;
import com.jobflow.specification.ApplicationSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;
    private final CompanyRepository companyRepository;
    private final JobOfferRepository jobOfferRepository;
    private final ApplicationMapper applicationMapper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public ApplicationResponse create(ApplicationRequest request) {
        User user = currentUserProvider.getCurrentUser();
        ApplicationStatus initialStatus = request.getStatus() != null ? request.getStatus() : ApplicationStatus.WISHLIST;

        Application application = Application.builder()
                .user(user)
                .company(resolveOwnedCompany(request.getCompanyId(), user))
                .jobOffer(resolveOwnedJobOffer(request.getJobOfferId(), user))
                .status(initialStatus)
                .applicationDate(request.getApplicationDate())
                .salaryExpectation(request.getSalaryExpectation())
                .source(request.getSource())
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .notes(request.getNotes())
                .nextFollowUpDate(request.getNextFollowUpDate())
                .build();

        applicationRepository.save(application);
        recordStatusChange(application, null, initialStatus);

        return applicationMapper.toResponse(application);
    }

    @Override
    @Transactional
    public ApplicationResponse update(UUID id, ApplicationRequest request) {
        Application application = getOwnedOrThrow(id);
        User user = currentUserProvider.getCurrentUser();

        ApplicationStatus previousStatus = application.getStatus();
        ApplicationStatus newStatus = request.getStatus() != null ? request.getStatus() : previousStatus;

        application.setCompany(resolveOwnedCompany(request.getCompanyId(), user));
        application.setJobOffer(resolveOwnedJobOffer(request.getJobOfferId(), user));
        application.setApplicationDate(request.getApplicationDate());
        application.setSalaryExpectation(request.getSalaryExpectation());
        application.setSource(request.getSource());
        if (request.getPriority() != null) application.setPriority(request.getPriority());
        application.setNotes(request.getNotes());
        application.setNextFollowUpDate(request.getNextFollowUpDate());
        application.setStatus(newStatus);

        applicationRepository.save(application);

        if (newStatus != previousStatus) {
            recordStatusChange(application, previousStatus, newStatus);
        }

        return applicationMapper.toResponse(application);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Application application = getOwnedOrThrow(id);
        application.setDeletedAt(LocalDateTime.now());
        applicationRepository.save(application);
    }

    @Override
    public ApplicationResponse get(UUID id) {
        return applicationMapper.toResponse(getOwnedOrThrow(id));
    }

    @Override
    public Page<ApplicationResponse> list(String search, ApplicationStatus status, Priority priority,
                                           UUID companyId, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        User user = currentUserProvider.getCurrentUser();
        var spec = ApplicationSpecification.build(user, search, status, priority, companyId, dateFrom, dateTo);
        return applicationRepository.findAll(spec, pageable).map(applicationMapper::toResponse);
    }

    /**
     * Dedicated status-only mutation — this is what the Kanban board (Phase 5)
     * calls on drag & drop: PATCH /api/applications/{id}/status.
     */
    @Override
    @Transactional
    public ApplicationResponse updateStatus(UUID id, ApplicationStatus newStatus) {
        Application application = getOwnedOrThrow(id);
        ApplicationStatus previousStatus = application.getStatus();

        if (newStatus == previousStatus) {
            return applicationMapper.toResponse(application);
        }

        application.setStatus(newStatus);
        applicationRepository.save(application);
        recordStatusChange(application, previousStatus, newStatus);

        return applicationMapper.toResponse(application);
    }

    @Override
    public List<ApplicationStatusHistoryResponse> getStatusHistory(UUID id) {
        getOwnedOrThrow(id); // ownership check before exposing history
        return statusHistoryRepository.findByApplicationIdOrderByChangedAtAsc(id).stream()
                .map(applicationMapper::toHistoryResponse)
                .toList();
    }

    // ===================== HELPERS =====================

    private void recordStatusChange(Application application, ApplicationStatus from, ApplicationStatus to) {
        ApplicationStatusHistory history = ApplicationStatusHistory.builder()
                .application(application)
                .fromStatus(from)
                .toStatus(to)
                .build();
        statusHistoryRepository.save(history);
    }

    private Company resolveOwnedCompany(UUID companyId, User user) {
        if (companyId == null) return null;
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        if (company.getDeletedAt() != null || !company.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Company not found");
        }
        return company;
    }

    private JobOffer resolveOwnedJobOffer(UUID jobOfferId, User user) {
        if (jobOfferId == null) return null;
        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new ResourceNotFoundException("Job offer not found"));
        if (jobOffer.getDeletedAt() != null || !jobOffer.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Job offer not found");
        }
        return jobOffer;
    }

    private Application getOwnedOrThrow(UUID id) {
        User user = currentUserProvider.getCurrentUser();
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (application.getDeletedAt() != null || !application.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Application not found");
        }
        return application;
    }
}
