package com.jobflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ContactResponse {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String position;
    private UUID companyId;
    private String companyName;
    private String linkedinUrl;
    private String notes;
    private Set<UUID> applicationIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
