package com.jobflow.dto.request;

import com.jobflow.entity.FollowUpStatus;
import com.jobflow.entity.FollowUpType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class FollowUpRequest {

    @NotNull
    private UUID applicationId;

    @NotNull
    private LocalDate followUpDate;

    @NotNull
    private FollowUpType type;

    private FollowUpStatus status;
    private String notes;
}
