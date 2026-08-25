package com.jobflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FollowUpStatsResponse {
    private long dueToday;
    private long overdue;
    private long dueThisWeek;
}
