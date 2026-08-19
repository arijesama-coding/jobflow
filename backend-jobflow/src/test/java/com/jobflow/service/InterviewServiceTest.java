package com.jobflow.service;

import com.jobflow.dto.request.InterviewRequest;
import com.jobflow.entity.*;
import com.jobflow.exception.ResourceNotFoundException;
import com.jobflow.mapper.InterviewMapper;
import com.jobflow.repository.ApplicationRepository;
import com.jobflow.repository.InterviewRepository;
import com.jobflow.security.CurrentUserProvider;
import com.jobflow.service.impl.InterviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Interview has no direct user_id column — ownership is only reachable via
 * interview.application.user. That indirection is exactly the kind of thing
 * that regresses quietly (e.g. someone "simplifies" getOwnedOrThrow later),
 * so it gets its own test rather than relying on the Company/Application
 * pattern being copied correctly by inspection.
 */
@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {

    @Mock private InterviewRepository interviewRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private InterviewMapper interviewMapper;
    @Mock private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private InterviewServiceImpl interviewService;

    private User owner;
    private User intruder;
    private Application application;
    private Interview interview;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(UUID.randomUUID()).email("owner@example.com").build();
        intruder = User.builder().id(UUID.randomUUID()).email("intruder@example.com").build();
        application = Application.builder().id(UUID.randomUUID()).user(owner).status(ApplicationStatus.INTERVIEW).build();
        interview = Interview.builder()
                .id(UUID.randomUUID())
                .application(application)
                .type(InterviewType.VIDEO)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .result(InterviewResult.PENDING)
                .build();
    }

    @Test
    void get_throwsNotFound_whenInterviewsApplicationBelongsToAnotherUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(intruder);
        when(interviewRepository.findById(interview.getId())).thenReturn(Optional.of(interview));

        assertThatThrownBy(() -> interviewService.get(interview.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_throwsNotFound_whenLinkedApplicationBelongsToAnotherUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(intruder);
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        InterviewRequest request = new InterviewRequest();
        request.setApplicationId(application.getId());
        request.setType(InterviewType.PHONE);
        request.setScheduledAt(LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> interviewService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(interviewRepository, never()).save(any());
    }

    @Test
    void delete_succeeds_forOwner() {
        when(currentUserProvider.getCurrentUser()).thenReturn(owner);
        when(interviewRepository.findById(interview.getId())).thenReturn(Optional.of(interview));

        interviewService.delete(interview.getId());

        verify(interviewRepository).delete(interview);
    }
}
