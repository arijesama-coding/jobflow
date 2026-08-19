package com.jobflow.service.impl;

import com.jobflow.dto.request.InterviewRequest;
import com.jobflow.dto.response.InterviewResponse;
import com.jobflow.entity.*;
import com.jobflow.exception.ResourceNotFoundException;
import com.jobflow.mapper.InterviewMapper;
import com.jobflow.repository.ApplicationRepository;
import com.jobflow.repository.InterviewRepository;
import com.jobflow.security.CurrentUserProvider;
import com.jobflow.service.InterviewService;
import com.jobflow.specification.InterviewSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewMapper interviewMapper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public InterviewResponse create(InterviewRequest request) {
        User user = currentUserProvider.getCurrentUser();
        Application application = resolveOwnedApplication(request.getApplicationId(), user);

        Interview interview = Interview.builder()
                .application(application)
                .type(request.getType())
                .scheduledAt(request.getScheduledAt())
                .durationMinutes(request.getDurationMinutes())
                .location(request.getLocation())
                .meetingUrl(request.getMeetingUrl())
                .interviewer(request.getInterviewer())
                .notes(request.getNotes())
                .feedback(request.getFeedback())
                .result(request.getResult() != null ? request.getResult() : InterviewResult.PENDING)
                .build();

        return interviewMapper.toResponse(interviewRepository.save(interview));
    }

    @Override
    @Transactional
    public InterviewResponse update(UUID id, InterviewRequest request) {
        Interview interview = getOwnedOrThrow(id);
        User user = currentUserProvider.getCurrentUser();

        interview.setApplication(resolveOwnedApplication(request.getApplicationId(), user));
        interview.setType(request.getType());
        interview.setScheduledAt(request.getScheduledAt());
        interview.setDurationMinutes(request.getDurationMinutes());
        interview.setLocation(request.getLocation());
        interview.setMeetingUrl(request.getMeetingUrl());
        interview.setInterviewer(request.getInterviewer());
        interview.setNotes(request.getNotes());
        interview.setFeedback(request.getFeedback());
        if (request.getResult() != null) interview.setResult(request.getResult());

        return interviewMapper.toResponse(interviewRepository.save(interview));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Interview interview = getOwnedOrThrow(id);
        interviewRepository.delete(interview);
    }

    @Override
    public InterviewResponse get(UUID id) {
        return interviewMapper.toResponse(getOwnedOrThrow(id));
    }

    @Override
    public Page<InterviewResponse> list(UUID applicationId, InterviewType type, InterviewResult result,
                                         LocalDateTime from, LocalDateTime to, Pageable pageable) {
        User user = currentUserProvider.getCurrentUser();
        var spec = InterviewSpecification.build(user, applicationId, type, result, from, to);
        return interviewRepository.findAll(spec, pageable).map(interviewMapper::toResponse);
    }

    /** Backs the Calendar page — every interview scheduled within [from, to], unpaginated. */
    @Override
    public List<InterviewResponse> calendar(LocalDateTime from, LocalDateTime to) {
        User user = currentUserProvider.getCurrentUser();
        return interviewRepository.findByApplication_User_IdAndScheduledAtBetween(user.getId(), from, to).stream()
                .map(interviewMapper::toResponse)
                .toList();
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

    private Interview getOwnedOrThrow(UUID id) {
        User user = currentUserProvider.getCurrentUser();
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));

        if (!interview.getApplication().getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Interview not found");
        }
        return interview;
    }
}
