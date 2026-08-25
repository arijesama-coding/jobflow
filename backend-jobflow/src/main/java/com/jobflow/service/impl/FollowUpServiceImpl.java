package com.jobflow.service.impl;

import com.jobflow.dto.request.FollowUpRequest;
import com.jobflow.dto.response.FollowUpResponse;
import com.jobflow.dto.response.FollowUpStatsResponse;
import com.jobflow.entity.*;
import com.jobflow.exception.ResourceNotFoundException;
import com.jobflow.mapper.FollowUpMapper;
import com.jobflow.repository.ApplicationRepository;
import com.jobflow.repository.FollowUpRepository;
import com.jobflow.security.CurrentUserProvider;
import com.jobflow.service.FollowUpService;
import com.jobflow.specification.FollowUpSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FollowUpServiceImpl implements FollowUpService {

    private final FollowUpRepository followUpRepository;
    private final ApplicationRepository applicationRepository;
    private final FollowUpMapper followUpMapper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public FollowUpResponse create(FollowUpRequest request) {
        User user = currentUserProvider.getCurrentUser();
        Application application = resolveOwnedApplication(request.getApplicationId(), user);

        FollowUp followUp = FollowUp.builder()
                .application(application)
                .followUpDate(request.getFollowUpDate())
                .type(request.getType())
                .status(request.getStatus() != null ? request.getStatus() : FollowUpStatus.PLANNED)
                .notes(request.getNotes())
                .build();

        return followUpMapper.toResponse(followUpRepository.save(followUp));
    }

    @Override
    @Transactional
    public FollowUpResponse update(UUID id, FollowUpRequest request) {
        FollowUp followUp = getOwnedOrThrow(id);
        User user = currentUserProvider.getCurrentUser();

        followUp.setApplication(resolveOwnedApplication(request.getApplicationId(), user));
        followUp.setFollowUpDate(request.getFollowUpDate());
        followUp.setType(request.getType());
        if (request.getStatus() != null) followUp.setStatus(request.getStatus());
        followUp.setNotes(request.getNotes());

        return followUpMapper.toResponse(followUpRepository.save(followUp));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        followUpRepository.delete(getOwnedOrThrow(id));
    }

    @Override
    public FollowUpResponse get(UUID id) {
        return followUpMapper.toResponse(getOwnedOrThrow(id));
    }

    @Override
    public Page<FollowUpResponse> list(UUID applicationId, FollowUpType type, FollowUpStatus status,
                                        LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        User user = currentUserProvider.getCurrentUser();
        var spec = FollowUpSpecification.build(user, applicationId, type, status, dateFrom, dateTo);
        return followUpRepository.findAll(spec, pageable).map(followUpMapper::toResponse);
    }

    @Override
    public List<FollowUpResponse> calendar(LocalDate from, LocalDate to) {
        User user = currentUserProvider.getCurrentUser();
        return followUpRepository.findByApplication_User_IdAndFollowUpDateBetween(user.getId(), from, to).stream()
                .map(followUpMapper::toResponse)
                .toList();
    }

    @Override
    public FollowUpStatsResponse getStats() {
        User user = currentUserProvider.getCurrentUser();
        List<FollowUp> planned = followUpRepository.findByApplication_User_IdAndStatus(user.getId(), FollowUpStatus.PLANNED);

        LocalDate today = LocalDate.now();
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));

        long dueToday = planned.stream().filter(f -> f.getFollowUpDate().isEqual(today)).count();
        long overdue = planned.stream().filter(f -> f.getFollowUpDate().isBefore(today)).count();
        long dueThisWeek = planned.stream()
                .filter(f -> !f.getFollowUpDate().isBefore(today) && !f.getFollowUpDate().isAfter(endOfWeek))
                .count();

        return FollowUpStatsResponse.builder()
                .dueToday(dueToday)
                .overdue(overdue)
                .dueThisWeek(dueThisWeek)
                .build();
    }

    // ===================== HELPERS =====================

    private Application resolveOwnedApplication(UUID applicationId, User user) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (application.getDeletedAt() != null || !application.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Application not found");
        }
        return application;
    }

    private FollowUp getOwnedOrThrow(UUID id) {
        User user = currentUserProvider.getCurrentUser();
        FollowUp followUp = followUpRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Follow-up not found"));

        if (!followUp.getApplication().getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Follow-up not found");
        }
        return followUp;
    }
}
