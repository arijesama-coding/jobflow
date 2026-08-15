package com.jobflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class JobOfferResponse {
    private UUID id;
    private String title;
    private UUID companyId;
    private String companyName;
    private String description;
    private String location;
    private String remoteType;
    private String contractType;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String currency;
    private String jobUrl;
    private String source;
    private Set<String> skills;
    private LocalDate publicationDate;
    private LocalDate deadline;
    private boolean deadlinePassed;
    private String notes;
    private boolean favorite;
    private boolean archived;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
