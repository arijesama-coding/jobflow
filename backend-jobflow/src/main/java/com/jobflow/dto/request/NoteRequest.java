package com.jobflow.dto.request;

import com.jobflow.entity.NoteEntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class NoteRequest {

    @NotNull
    private NoteEntityType entityType;

    @NotNull
    private UUID entityId;

    @NotBlank
    private String content;
}
