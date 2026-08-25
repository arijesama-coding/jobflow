package com.jobflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class FollowUpResponse {
    private UUID id;
    private UUID applicationId;
    private String companyName;
    private String jobOfferTitle;
    private LocalDate followUpDate;
    private String type;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
