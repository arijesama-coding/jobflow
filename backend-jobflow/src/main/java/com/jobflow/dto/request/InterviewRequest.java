package com.jobflow.dto.request;

import com.jobflow.entity.InterviewResult;
import com.jobflow.entity.InterviewType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class InterviewRequest {

    @NotNull
    private UUID applicationId;

    @NotNull
    private InterviewType type;

    @NotNull
    private LocalDateTime scheduledAt;

    @Positive
    private Integer durationMinutes;

    private String location;
    private String meetingUrl;
    private String interviewer;
    private String notes;
    private String feedback;
    private InterviewResult result;
}
