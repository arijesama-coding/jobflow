package com.jobflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyRequest {

    @NotBlank
    private String name;

    private String logoUrl;
    private String website;
    private String industry;
    private String location;
    private String description;
    private String size;
    private String linkedinUrl;
    private String notes;
}
