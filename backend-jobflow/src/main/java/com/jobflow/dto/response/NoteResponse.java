package com.jobflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class NoteResponse {
    private UUID id;
    private String entityType;
    private UUID entityId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
