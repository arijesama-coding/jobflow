package com.jobflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentUpdateRequest {
    @NotBlank
    private String fileName;
}
