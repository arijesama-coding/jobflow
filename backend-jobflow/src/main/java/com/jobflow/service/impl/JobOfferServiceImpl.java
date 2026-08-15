package com.jobflow.service.impl;

import com.jobflow.dto.request.JobOfferRequest;
import com.jobflow.dto.response.JobOfferResponse;
import com.jobflow.entity.*;
import com.jobflow.exception.ResourceNotFoundException;
import com.jobflow.mapper.JobOfferMapper;
import com.jobflow.repository.CompanyRepository;
import com.jobflow.repository.JobOfferRepository;
import com.jobflow.repository.SkillRepository;
import com.jobflow.security.CurrentUserProvider;
import com.jobflow.service.JobOfferService;
import com.jobflow.specification.JobOfferSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobOfferServiceImpl implements JobOfferService {

    private final JobOfferRepository jobOfferRepository;
    private final CompanyRepository companyRepository;
    private final SkillRepository skillRepository;
    private final JobOfferMapper jobOfferMapper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public JobOfferResponse create(JobOfferRequest request) {
        User user = currentUserProvider.getCurrentUser();

        JobOffer offer = JobOffer.builder()
                .user(user)
                .company(resolveOwnedCompany(request.getCompanyId(), user))
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .remoteType(request.getRemoteType())
                .contractType(request.getContractType())
                .salaryMin(request.getSalaryMin())
                .salaryMax(request.getSalaryMax())
                .currency(request.getCurrency() != null ? request.getCurrency() : "EUR")
                .jobUrl(request.getJobUrl())
                .source(request.getSource())
                .skills(resolveSkills(request.getSkills()))
                .publicationDate(request.getPublicationDate())
                .deadline(request.getDeadline())
                .notes(request.getNotes())
                .build();

        return jobOfferMapper.toResponse(jobOfferRepository.save(offer));
    }

    @Override
    @Transactional
    public JobOfferResponse update(UUID id, JobOfferRequest request) {
        JobOffer offer = getOwnedOrThrow(id);
        User user = currentUserProvider.getCurrentUser();

        offer.setTitle(request.getTitle());
        offer.setCompany(resolveOwnedCompany(request.getCompanyId(), user));
        offer.setDescription(request.getDescription());
        offer.setLocation(request.getLocation());
        offer.setRemoteType(request.getRemoteType());
        offer.setContractType(request.getContractType());
        offer.setSalaryMin(request.getSalaryMin());
        offer.setSalaryMax(request.getSalaryMax());
        if (request.getCurrency() != null) offer.setCurrency(request.getCurrency());
        offer.setJobUrl(request.getJobUrl());
        offer.setSource(request.getSource());
        offer.setSkills(resolveSkills(request.getSkills()));
        offer.setPublicationDate(request.getPublicationDate());
        offer.setDeadline(request.getDeadline());
        offer.setNotes(request.getNotes());

        return jobOfferMapper.toResponse(jobOfferRepository.save(offer));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        JobOffer offer = getOwnedOrThrow(id);
        offer.setDeletedAt(LocalDateTime.now());
        jobOfferRepository.save(offer);
    }

    @Override
    public JobOfferResponse get(UUID id) {
        return jobOfferMapper.toResponse(getOwnedOrThrow(id));
    }

    @Override
    public Page<JobOfferResponse> list(String search, UUID companyId, RemoteType remoteType,
                                        ContractType contractType, Boolean favorite, Boolean archived,
                                        Pageable pageable) {
        User user = currentUserProvider.getCurrentUser();
        var spec = JobOfferSpecification.build(user, search, companyId, remoteType, contractType, favorite, archived);
        return jobOfferRepository.findAll(spec, pageable).map(jobOfferMapper::toResponse);
    }

    @Override
    @Transactional
    public JobOfferResponse toggleFavorite(UUID id) {
        JobOffer offer = getOwnedOrThrow(id);
        offer.setFavorite(!offer.isFavorite());
        return jobOfferMapper.toResponse(jobOfferRepository.save(offer));
    }

    @Override
    @Transactional
    public JobOfferResponse toggleArchived(UUID id) {
        JobOffer offer = getOwnedOrThrow(id);
        offer.setArchived(!offer.isArchived());
        return jobOfferMapper.toResponse(jobOfferRepository.save(offer));
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

    /** Skills are shared, free-text tags — find-or-create by (case-insensitive) name. */
    private Set<Skill> resolveSkills(Set<String> names) {
        if (names == null) return new HashSet<>();
        Set<Skill> skills = new HashSet<>();
        for (String rawName : names) {
            String trimmed = rawName.trim();
            if (trimmed.isEmpty()) continue;
            Skill skill = skillRepository.findByNameIgnoreCase(trimmed)
                    .orElseGet(() -> skillRepository.save(Skill.builder().name(trimmed).build()));
            skills.add(skill);
        }
        return skills;
    }

    private JobOffer getOwnedOrThrow(UUID id) {
        User user = currentUserProvider.getCurrentUser();
        JobOffer offer = jobOfferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job offer not found"));

        if (offer.getDeletedAt() != null || !offer.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Job offer not found");
        }
        return offer;
    }
}
