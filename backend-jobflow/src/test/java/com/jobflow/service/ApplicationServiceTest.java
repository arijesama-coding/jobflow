package com.jobflow.service;

import com.jobflow.dto.request.ApplicationRequest;
import com.jobflow.entity.*;
import com.jobflow.exception.ResourceNotFoundException;
import com.jobflow.mapper.ApplicationMapper;
import com.jobflow.repository.ApplicationRepository;
import com.jobflow.repository.ApplicationStatusHistoryRepository;
import com.jobflow.repository.CompanyRepository;
import com.jobflow.repository.JobOfferRepository;
import com.jobflow.security.CurrentUserProvider;
import com.jobflow.service.impl.ApplicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Covers the two things that must never silently break in this module:
 * every status transition is recorded in application_status_history, and
 * ownership is enforced (get/updateStatus 404 on another user's application).
 */
@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private ApplicationStatusHistoryRepository statusHistoryRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private JobOfferRepository jobOfferRepository;
    @Mock private ApplicationMapper applicationMapper;
    @Mock private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    private User owner;
    private User intruder;
    private Application application;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(UUID.randomUUID()).email("owner@example.com").build();
        intruder = User.builder().id(UUID.randomUUID()).email("intruder@example.com").build();
        application = Application.builder()
                .id(UUID.randomUUID())
                .user(owner)
                .status(ApplicationStatus.APPLIED)
                .priority(Priority.MEDIUM)
                .build();
    }

    @Test
    void create_recordsInitialStatusHistoryWithNullFromStatus() {
        when(currentUserProvider.getCurrentUser()).thenReturn(owner);
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        ApplicationRequest request = new ApplicationRequest();
        request.setStatus(ApplicationStatus.WISHLIST);

        applicationService.create(request);

        ArgumentCaptor<ApplicationStatusHistory> captor = ArgumentCaptor.forClass(ApplicationStatusHistory.class);
        verify(statusHistoryRepository).save(captor.capture());
        assertThat(captor.getValue().getFromStatus()).isNull();
        assertThat(captor.getValue().getToStatus()).isEqualTo(ApplicationStatus.WISHLIST);
    }

    @Test
    void updateStatus_recordsTransition_whenStatusActuallyChanges() {
        when(currentUserProvider.getCurrentUser()).thenReturn(owner);
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        applicationService.updateStatus(application.getId(), ApplicationStatus.INTERVIEW);

        ArgumentCaptor<ApplicationStatusHistory> captor = ArgumentCaptor.forClass(ApplicationStatusHistory.class);
        verify(statusHistoryRepository).save(captor.capture());
        assertThat(captor.getValue().getFromStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(captor.getValue().getToStatus()).isEqualTo(ApplicationStatus.INTERVIEW);
    }

    @Test
    void updateStatus_doesNothing_whenStatusIsUnchanged() {
        when(currentUserProvider.getCurrentUser()).thenReturn(owner);
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        applicationService.updateStatus(application.getId(), ApplicationStatus.APPLIED);

        verify(statusHistoryRepository, never()).save(any());
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void get_throwsNotFound_whenApplicationBelongsToAnotherUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(intruder);
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.get(application.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStatus_throwsNotFound_whenApplicationBelongsToAnotherUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(intruder);
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.updateStatus(application.getId(), ApplicationStatus.OFFER))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(applicationRepository, never()).save(any());
    }
}
