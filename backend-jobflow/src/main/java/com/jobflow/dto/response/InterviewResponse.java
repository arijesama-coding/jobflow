package com.jobflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class InterviewResponse {
    private UUID id;
    private UUID applicationId;
    private String companyName;
    private String jobOfferTitle;
    private String type;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private String location;
    private String meetingUrl;
    private String interviewer;
    private String notes;
    private String feedback;
    private String result;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
