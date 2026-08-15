package com.jobflow.dto.request;

import com.jobflow.entity.ApplicationSource;
import com.jobflow.entity.ApplicationStatus;
import com.jobflow.entity.Priority;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class ApplicationRequest {

    private UUID companyId;
    private UUID jobOfferId;
    private ApplicationStatus status;
    private LocalDate applicationDate;

    @PositiveOrZero
    private BigDecimal salaryExpectation;

    private ApplicationSource source;
    private Priority priority;
    private String notes;
    private LocalDate nextFollowUpDate;
}
