package com.jobflow.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ContactRequest {

    @NotBlank
    private String name;

    @Email
    private String email;

    private String phone;
    private String position;
    private UUID companyId;
    private String linkedinUrl;
    private String notes;
}
