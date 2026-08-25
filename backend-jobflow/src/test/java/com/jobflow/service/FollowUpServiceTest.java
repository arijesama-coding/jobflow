package com.jobflow.service;

import com.jobflow.dto.response.FollowUpStatsResponse;
import com.jobflow.entity.*;
import com.jobflow.mapper.FollowUpMapper;
import com.jobflow.repository.ApplicationRepository;
import com.jobflow.repository.FollowUpRepository;
import com.jobflow.security.CurrentUserProvider;
import com.jobflow.service.impl.FollowUpServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Pins down the "due today / overdue / due this week" bucketing (spec
 * section 14's follow-up dashboard) — an off-by-one here silently tells
 * someone their overdue follow-up is "due this week" instead, which is
 * exactly the kind of thing that erodes trust in the whole tracker.
 */
@ExtendWith(MockitoExtension.class)
class FollowUpServiceTest {

    @Mock private FollowUpRepository followUpRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private FollowUpMapper followUpMapper;
    @Mock private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private FollowUpServiceImpl followUpService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(UUID.randomUUID()).email("owner@example.com").build();
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
    }

    @Test
    void getStats_bucketsFollowUpsCorrectly() {
        LocalDate today = LocalDate.now();

        FollowUp overdue = plannedFollowUp(today.minusDays(3));
        FollowUp dueToday = plannedFollowUp(today);
        FollowUp dueLaterThisWeek = plannedFollowUp(today.with(java.time.DayOfWeek.SUNDAY));
        FollowUp farFuture = plannedFollowUp(today.plusMonths(2));

        when(followUpRepository.findByApplication_User_IdAndStatus(user.getId(), FollowUpStatus.PLANNED))
                .thenReturn(List.of(overdue, dueToday, dueLaterThisWeek, farFuture));

        FollowUpStatsResponse stats = followUpService.getStats();

        assertThat(stats.getOverdue()).isEqualTo(1);
        assertThat(stats.getDueToday()).isEqualTo(1);
        // "due this week" includes today through Sunday, so dueToday + dueLaterThisWeek = 2
        assertThat(stats.getDueThisWeek()).isEqualTo(2);
    }

    private FollowUp plannedFollowUp(LocalDate date) {
        return FollowUp.builder()
                .id(UUID.randomUUID())
                .followUpDate(date)
                .type(FollowUpType.EMAIL)
                .status(FollowUpStatus.PLANNED)
                .build();
    }
}
