package com.jobflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class DashboardResponse {
    private long totalApplications;
    private long activeApplications;
    private long interviews;
    private long offersReceived;
    private long acceptedApplications;
    private long rejectedApplications;
    private double responseRate;
    private double conversionRate;

    private List<MonthlyCount> applicationsByMonth;
    private List<StatusCount> statusDistribution;
    private List<FunnelStage> conversionFunnel;
    private List<CompanyCount> topCompanies;
    private List<SourceStat> sourceEffectiveness;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class MonthlyCount {
        private String month;
        private long count;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class StatusCount {
        private String status;
        private long count;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class FunnelStage {
        private String stage;
        private long count;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CompanyCount {
        private String companyName;
        private long count;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class SourceStat {
        private String source;
        private long totalApplications;
        private double effectivenessRate;
    }
}
