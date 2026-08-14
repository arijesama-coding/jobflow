package com.jobflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class CompanyResponse {
    private UUID id;
    private String name;
    private String logoUrl;
    private String website;
    private String industry;
    private String location;
    private String description;
    private String size;
    private String linkedinUrl;
    private String notes;
    private boolean favorite;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
