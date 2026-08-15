package com.jobflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ApplicationResponse {
    private UUID id;
    private UUID companyId;
    private String companyName;
    private UUID jobOfferId;
    private String jobOfferTitle;
    private String status;
    private LocalDate applicationDate;
    private BigDecimal salaryExpectation;
    private String source;
    private String priority;
    private String notes;
    private LocalDate nextFollowUpDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
