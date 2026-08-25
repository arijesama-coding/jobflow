package com.jobflow.service.impl;

import com.jobflow.dto.request.ContactRequest;
import com.jobflow.dto.response.ContactResponse;
import com.jobflow.entity.Application;
import com.jobflow.entity.Company;
import com.jobflow.entity.Contact;
import com.jobflow.entity.User;
import com.jobflow.exception.ResourceNotFoundException;
import com.jobflow.mapper.ContactMapper;
import com.jobflow.repository.ApplicationRepository;
import com.jobflow.repository.CompanyRepository;
import com.jobflow.repository.ContactRepository;
import com.jobflow.security.CurrentUserProvider;
import com.jobflow.service.ContactService;
import com.jobflow.specification.ContactSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final CompanyRepository companyRepository;
    private final ApplicationRepository applicationRepository;
    private final ContactMapper contactMapper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public ContactResponse create(ContactRequest request) {
        User user = currentUserProvider.getCurrentUser();

        Contact contact = Contact.builder()
                .user(user)
                .company(resolveOwnedCompany(request.getCompanyId(), user))
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .position(request.getPosition())
                .linkedinUrl(request.getLinkedinUrl())
                .notes(request.getNotes())
                .build();

        return contactMapper.toResponse(contactRepository.save(contact));
    }

    @Override
    @Transactional
    public ContactResponse update(UUID id, ContactRequest request) {
        Contact contact = getOwnedOrThrow(id);
        User user = currentUserProvider.getCurrentUser();

        contact.setCompany(resolveOwnedCompany(request.getCompanyId(), user));
        contact.setName(request.getName());
        contact.setEmail(request.getEmail());
        contact.setPhone(request.getPhone());
        contact.setPosition(request.getPosition());
        contact.setLinkedinUrl(request.getLinkedinUrl());
        contact.setNotes(request.getNotes());

        return contactMapper.toResponse(contactRepository.save(contact));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Contact contact = getOwnedOrThrow(id);
        contact.setDeletedAt(LocalDateTime.now());
        contactRepository.save(contact);
    }

    @Override
    public ContactResponse get(UUID id) {
        return contactMapper.toResponse(getOwnedOrThrow(id));
    }

    @Override
    public Page<ContactResponse> list(String search, UUID companyId, UUID applicationId, Pageable pageable) {
        User user = currentUserProvider.getCurrentUser();
        var spec = ContactSpecification.build(user, search, companyId, applicationId);
        return contactRepository.findAll(spec, pageable).map(contactMapper::toResponse);
    }

    @Override
    @Transactional
    public ContactResponse linkToApplication(UUID contactId, UUID applicationId) {
        Contact contact = getOwnedOrThrow(contactId);
        User user = currentUserProvider.getCurrentUser();
        Application application = resolveOwnedApplication(applicationId, user);

        contact.getApplications().add(application);
        return contactMapper.toResponse(contactRepository.save(contact));
    }

    @Override
    @Transactional
    public ContactResponse unlinkFromApplication(UUID contactId, UUID applicationId) {
        Contact contact = getOwnedOrThrow(contactId);
        contact.getApplications().removeIf(a -> a.getId().equals(applicationId));
        return contactMapper.toResponse(contactRepository.save(contact));
    }

    // ===================== HELPERS =====================

    private Company resolveOwnedCompany(UUID companyId, User user) {
        if (companyId == null) return null;
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        if (company.getDeletedAt() != null || !company.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Company not found");
        }
        return company;
    }

    private Application resolveOwnedApplication(UUID applicationId, User user) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (application.getDeletedAt() != null || !application.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Application not found");
        }
        return application;
    }

    private Contact getOwnedOrThrow(UUID id) {
        User user = currentUserProvider.getCurrentUser();
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));

        if (contact.getDeletedAt() != null || !contact.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Contact not found");
        }
        return contact;
    }
}
