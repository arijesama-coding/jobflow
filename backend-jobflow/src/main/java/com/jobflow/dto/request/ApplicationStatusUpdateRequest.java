package com.jobflow.dto.request;

import com.jobflow.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationStatusUpdateRequest {

    @NotNull
    private ApplicationStatus status;
}
