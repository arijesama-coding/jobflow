package com.jobflow.service.impl;

import com.jobflow.dto.response.DashboardResponse;
import com.jobflow.entity.Application;
import com.jobflow.entity.ApplicationStatus;
import com.jobflow.entity.ApplicationStatusHistory;
import com.jobflow.entity.User;
import com.jobflow.repository.ApplicationRepository;
import com.jobflow.repository.ApplicationStatusHistoryRepository;
import com.jobflow.security.CurrentUserProvider;
import com.jobflow.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * All aggregation happens in-memory over one user's applications: this is a
 * personal job-search tracker, not a multi-tenant analytics platform, so a
 * few hundred rows per user is the realistic ceiling and a single fetch +
 * Java-side grouping is simpler and just as fast as hand-written JPQL
 * aggregation queries here. Revisit with real DB-side aggregation only if
 * usage patterns prove otherwise.
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final Set<ApplicationStatus> ACTIVE_STATUSES = EnumSet.complementOf(
            EnumSet.of(ApplicationStatus.ACCEPTED, ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN));

    private static final Set<ApplicationStatus> CURRENT_INTERVIEW_STATUSES = EnumSet.of(
            ApplicationStatus.SCREENING, ApplicationStatus.INTERVIEW,
            ApplicationStatus.TECHNICAL_INTERVIEW, ApplicationStatus.FINAL_INTERVIEW);

    /** Pipeline order used to compute "did this application ever reach stage X" from its history. */
    private static final Map<ApplicationStatus, Integer> PIPELINE_RANK = Map.of(
            ApplicationStatus.WISHLIST, 0,
            ApplicationStatus.TO_APPLY, 1,
            ApplicationStatus.APPLIED, 2,
            ApplicationStatus.SCREENING, 3,
            ApplicationStatus.INTERVIEW, 4,
            ApplicationStatus.TECHNICAL_INTERVIEW, 5,
            ApplicationStatus.FINAL_INTERVIEW, 6,
            ApplicationStatus.OFFER, 7,
            ApplicationStatus.ACCEPTED, 8
            // REJECTED / WITHDRAWN deliberately unranked (-1 via getOrDefault) so a
            // later rejection doesn't erase progress the application had already made.
    );

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public DashboardResponse getDashboard() {
        User user = currentUserProvider.getCurrentUser();
        List<Application> applications = applicationRepository.findByUser_IdAndDeletedAtIsNull(user.getId());
        List<ApplicationStatusHistory> history = statusHistoryRepository.findByApplication_User_IdOrderByChangedAtAsc(user.getId());

        Map<UUID, Integer> maxRankByApplication = computeMaxRankReached(applications, history);

        long total = applications.size();
        long active = applications.stream().filter(a -> ACTIVE_STATUSES.contains(a.getStatus())).count();
        long interviews = applications.stream().filter(a -> CURRENT_INTERVIEW_STATUSES.contains(a.getStatus())).count();
        long accepted = applications.stream().filter(a -> a.getStatus() == ApplicationStatus.ACCEPTED).count();
        long rejected = applications.stream().filter(a -> a.getStatus() == ApplicationStatus.REJECTED).count();

        long everReachedOffer = countAtLeastRank(maxRankByApplication, PIPELINE_RANK.get(ApplicationStatus.OFFER));
        long everResponded = countAtLeastRank(maxRankByApplication, PIPELINE_RANK.get(ApplicationStatus.SCREENING));

        double responseRate = percentage(everResponded, total);
        double conversionRate = percentage(everReachedOffer, total);

        return DashboardResponse.builder()
                .totalApplications(total)
                .activeApplications(active)
                .interviews(interviews)
                .offersReceived(everReachedOffer)
                .acceptedApplications(accepted)
                .rejectedApplications(rejected)
                .responseRate(responseRate)
                .conversionRate(conversionRate)
                .applicationsByMonth(buildMonthlyCounts(applications))
                .statusDistribution(buildStatusDistribution(applications))
                .conversionFunnel(buildFunnel(maxRankByApplication, total, accepted))
                .topCompanies(buildTopCompanies(applications))
                .sourceEffectiveness(buildSourceEffectiveness(applications, maxRankByApplication))
                .build();
    }

    // ===================== HELPERS =====================

    private Map<UUID, Integer> computeMaxRankReached(List<Application> applications, List<ApplicationStatusHistory> history) {
        Map<UUID, Integer> maxRank = new HashMap<>();
        for (Application app : applications) {
            maxRank.put(app.getId(), PIPELINE_RANK.getOrDefault(app.getStatus(), -1));
        }
        for (ApplicationStatusHistory h : history) {
            UUID appId = h.getApplication().getId();
            int rank = PIPELINE_RANK.getOrDefault(h.getToStatus(), -1);
            maxRank.merge(appId, rank, Math::max);
        }
        return maxRank;
    }

    private long countAtLeastRank(Map<UUID, Integer> maxRankByApplication, int minRank) {
        return maxRankByApplication.values().stream().filter(rank -> rank >= minRank).count();
    }

    private double percentage(long part, long total) {
        if (total == 0) return 0.0;
        return Math.round((part * 10000.0) / total) / 100.0;
    }

    private List<DashboardResponse.MonthlyCount> buildMonthlyCounts(List<Application> applications) {
        YearMonth current = YearMonth.now();
        List<YearMonth> trailingMonths = new ArrayList<>();
        for (int i = 7; i >= 0; i--) {
            trailingMonths.add(current.minusMonths(i));
        }

        Map<YearMonth, Long> counts = applications.stream()
                .map(a -> {
                    LocalDate reference = a.getApplicationDate() != null
                            ? a.getApplicationDate()
                            : a.getCreatedAt().toLocalDate();
                    return YearMonth.from(reference);
                })
                .collect(Collectors.groupingBy(ym -> ym, Collectors.counting()));

        return trailingMonths.stream()
                .map(ym -> DashboardResponse.MonthlyCount.builder()
                        .month(ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                        .count(counts.getOrDefault(ym, 0L))
                        .build())
                .toList();
    }

    private List<DashboardResponse.StatusCount> buildStatusDistribution(List<Application> applications) {
        Map<ApplicationStatus, Long> counts = applications.stream()
                .collect(Collectors.groupingBy(Application::getStatus, Collectors.counting()));

        return counts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> DashboardResponse.StatusCount.builder()
                        .status(e.getKey().name())
                        .count(e.getValue())
                        .build())
                .toList();
    }

    private List<DashboardResponse.FunnelStage> buildFunnel(Map<UUID, Integer> maxRankByApplication, long total, long accepted) {
        return List.of(
                DashboardResponse.FunnelStage.builder().stage("Application").count(total).build(),
                DashboardResponse.FunnelStage.builder().stage("Screening")
                        .count(countAtLeastRank(maxRankByApplication, PIPELINE_RANK.get(ApplicationStatus.SCREENING))).build(),
                DashboardResponse.FunnelStage.builder().stage("Interview")
                        .count(countAtLeastRank(maxRankByApplication, PIPELINE_RANK.get(ApplicationStatus.INTERVIEW))).build(),
                DashboardResponse.FunnelStage.builder().stage("Offer")
                        .count(countAtLeastRank(maxRankByApplication, PIPELINE_RANK.get(ApplicationStatus.OFFER))).build(),
                DashboardResponse.FunnelStage.builder().stage("Accepted").count(accepted).build()
        );
    }

    private List<DashboardResponse.CompanyCount> buildTopCompanies(List<Application> applications) {
        Map<String, Long> counts = applications.stream()
                .filter(a -> a.getCompany() != null)
                .collect(Collectors.groupingBy(a -> a.getCompany().getName(), Collectors.counting()));

        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> DashboardResponse.CompanyCount.builder().companyName(e.getKey()).count(e.getValue()).build())
                .toList();
    }

    private List<DashboardResponse.SourceStat> buildSourceEffectiveness(List<Application> applications,
                                                                          Map<UUID, Integer> maxRankByApplication) {
        Map<String, List<Application>> bySource = applications.stream()
                .filter(a -> a.getSource() != null)
                .collect(Collectors.groupingBy(a -> a.getSource().name()));

        int interviewRank = PIPELINE_RANK.get(ApplicationStatus.INTERVIEW);

        return bySource.entrySet().stream()
                .map(e -> {
                    long sourceTotal = e.getValue().size();
                    long reachedInterview = e.getValue().stream()
                            .filter(a -> maxRankByApplication.getOrDefault(a.getId(), -1) >= interviewRank)
                            .count();
                    return DashboardResponse.SourceStat.builder()
                            .source(e.getKey())
                            .totalApplications(sourceTotal)
                            .effectivenessRate(percentage(reachedInterview, sourceTotal))
                            .build();
                })
                .sorted(Comparator.comparingDouble(DashboardResponse.SourceStat::getEffectivenessRate).reversed())
                .toList();
    }
}
