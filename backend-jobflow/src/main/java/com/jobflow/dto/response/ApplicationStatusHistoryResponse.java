package com.jobflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ApplicationStatusHistoryResponse {
    private String fromStatus;
    private String toStatus;
    private LocalDateTime changedAt;
}
