package com.jobflow.service;

import com.jobflow.dto.request.ContactRequest;
import com.jobflow.dto.response.ContactResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ContactService {
    ContactResponse create(ContactRequest request);
    ContactResponse update(UUID id, ContactRequest request);
    void delete(UUID id);
    ContactResponse get(UUID id);
    Page<ContactResponse> list(String search, UUID companyId, UUID applicationId, Pageable pageable);
    ContactResponse linkToApplication(UUID contactId, UUID applicationId);
    ContactResponse unlinkFromApplication(UUID contactId, UUID applicationId);
}
