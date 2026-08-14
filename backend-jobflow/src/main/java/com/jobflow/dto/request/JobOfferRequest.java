package com.jobflow.dto.request;

import com.jobflow.entity.ContractType;
import com.jobflow.entity.JobSource;
import com.jobflow.entity.RemoteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class JobOfferRequest {

    @NotBlank
    private String title;

    private java.util.UUID companyId;
    private String description;
    private String location;
    private RemoteType remoteType;
    private ContractType contractType;

    @Positive
    private BigDecimal salaryMin;

    @Positive
    private BigDecimal salaryMax;

    private String currency;
    private String jobUrl;
    private JobSource source;
    private Set<String> skills;
    private LocalDate publicationDate;
    private LocalDate deadline;
    private String notes;
}
