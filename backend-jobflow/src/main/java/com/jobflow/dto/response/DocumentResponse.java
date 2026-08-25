package com.jobflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class DocumentResponse {
    private UUID id;
    private UUID applicationId;
    private String type;
    private String fileName;
    private int version;
    private boolean primary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
