package com.jobflow.service;

import com.jobflow.dto.response.DashboardResponse;
import com.jobflow.entity.*;
import com.jobflow.repository.ApplicationRepository;
import com.jobflow.repository.ApplicationStatusHistoryRepository;
import com.jobflow.security.CurrentUserProvider;
import com.jobflow.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * The one non-obvious rule this module implements: a later REJECTED status
 * must not erase the fact that an application had already reached, say,
 * INTERVIEW. Rejections are unranked on purpose — these tests pin that down,
 * since it silently breaks the funnel and rate numbers if it regresses.
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private ApplicationStatusHistoryRepository statusHistoryRepository;
    @Mock private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(UUID.randomUUID()).email("owner@example.com").build();
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
    }

    @Test
    void rejectionAfterInterview_stillCountsTowardInterviewFunnelStage() {
        Application app = Application.builder()
                .id(UUID.randomUUID())
                .user(user)
                .status(ApplicationStatus.REJECTED)
                .priority(Priority.MEDIUM)
                .createdAt(LocalDateTime.now())
                .build();

        ApplicationStatusHistory toApplied = history(app, null, ApplicationStatus.APPLIED);
        ApplicationStatusHistory toInterview = history(app, ApplicationStatus.APPLIED, ApplicationStatus.INTERVIEW);
        ApplicationStatusHistory toRejected = history(app, ApplicationStatus.INTERVIEW, ApplicationStatus.REJECTED);

        when(applicationRepository.findByUser_IdAndDeletedAtIsNull(user.getId())).thenReturn(List.of(app));
        when(statusHistoryRepository.findByApplication_User_IdOrderByChangedAtAsc(user.getId()))
                .thenReturn(List.of(toApplied, toInterview, toRejected));

        DashboardResponse response = dashboardService.getDashboard();

        DashboardResponse.FunnelStage interviewStage = response.getConversionFunnel().stream()
                .filter(s -> s.getStage().equals("Interview"))
                .findFirst().orElseThrow();

        assertThat(interviewStage.getCount()).isEqualTo(1);
        assertThat(response.getRejectedApplications()).isEqualTo(1);
        assertThat(response.getResponseRate()).isEqualTo(100.0); // it did get a response before being rejected
    }

    @Test
    void offersReceived_countsHistoricalOffers_evenIfLaterRejected() {
        Application app = Application.builder()
                .id(UUID.randomUUID())
                .user(user)
                .status(ApplicationStatus.REJECTED)
                .priority(Priority.MEDIUM)
                .createdAt(LocalDateTime.now())
                .build();

        when(applicationRepository.findByUser_IdAndDeletedAtIsNull(user.getId())).thenReturn(List.of(app));
        when(statusHistoryRepository.findByApplication_User_IdOrderByChangedAtAsc(user.getId()))
                .thenReturn(List.of(
                        history(app, null, ApplicationStatus.APPLIED),
                        history(app, ApplicationStatus.APPLIED, ApplicationStatus.OFFER),
                        history(app, ApplicationStatus.OFFER, ApplicationStatus.REJECTED)
                ));

        DashboardResponse response = dashboardService.getDashboard();

        assertThat(response.getOffersReceived()).isEqualTo(1);
    }

    private ApplicationStatusHistory history(Application app, ApplicationStatus from, ApplicationStatus to) {
        return ApplicationStatusHistory.builder()
                .application(app)
                .fromStatus(from)
                .toStatus(to)
                .changedAt(LocalDateTime.now())
                .build();
    }
}
