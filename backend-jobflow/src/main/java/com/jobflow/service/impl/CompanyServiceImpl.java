package com.jobflow.service.impl;

import com.jobflow.dto.request.CompanyRequest;
import com.jobflow.dto.response.CompanyResponse;
import com.jobflow.entity.Company;
import com.jobflow.entity.User;
import com.jobflow.exception.ResourceNotFoundException;
import com.jobflow.mapper.CompanyMapper;
import com.jobflow.repository.CompanyRepository;
import com.jobflow.security.CurrentUserProvider;
import com.jobflow.service.CompanyService;
import com.jobflow.specification.CompanySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public CompanyResponse create(CompanyRequest request) {
        User user = currentUserProvider.getCurrentUser();

        Company company = Company.builder()
                .user(user)
                .name(request.getName())
                .logoUrl(request.getLogoUrl())
                .website(request.getWebsite())
                .industry(request.getIndustry())
                .location(request.getLocation())
                .description(request.getDescription())
                .size(request.getSize())
                .linkedinUrl(request.getLinkedinUrl())
                .notes(request.getNotes())
                .build();

        return companyMapper.toResponse(companyRepository.save(company));
    }

    @Override
    @Transactional
    public CompanyResponse update(UUID id, CompanyRequest request) {
        Company company = getOwnedOrThrow(id);
        companyMapper.updateEntity(request, company);
        return companyMapper.toResponse(companyRepository.save(company));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Company company = getOwnedOrThrow(id);
        company.setDeletedAt(LocalDateTime.now());
        companyRepository.save(company);
    }

    @Override
    public CompanyResponse get(UUID id) {
        return companyMapper.toResponse(getOwnedOrThrow(id));
    }

    @Override
    public Page<CompanyResponse> list(String search, String industry, Boolean favorite, Pageable pageable) {
        User user = currentUserProvider.getCurrentUser();
        var spec = CompanySpecification.build(user, search, industry, favorite);
        return companyRepository.findAll(spec, pageable).map(companyMapper::toResponse);
    }

    @Override
    @Transactional
    public CompanyResponse toggleFavorite(UUID id) {
        Company company = getOwnedOrThrow(id);
        company.setFavorite(!company.isFavorite());
        return companyMapper.toResponse(companyRepository.save(company));
    }

    /**
     * Fetches the company and verifies it belongs to the caller AND isn't
     * soft-deleted. Returns 404 (not 403) on ownership mismatch so callers
     * can't distinguish "not yours" from "doesn't exist" — that ambiguity is
     * intentional (IDOR protection).
     */
    private Company getOwnedOrThrow(UUID id) {
        User user = currentUserProvider.getCurrentUser();
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        if (company.getDeletedAt() != null || !company.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Company not found");
        }
        return company;
    }
}
