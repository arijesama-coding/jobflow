package com.jobflow.service;

import com.jobflow.dto.request.CompanyRequest;
import com.jobflow.dto.response.CompanyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CompanyService {
    CompanyResponse create(CompanyRequest request);
    CompanyResponse update(UUID id, CompanyRequest request);
    void delete(UUID id);
    CompanyResponse get(UUID id);
    Page<CompanyResponse> list(String search, String industry, Boolean favorite, Pageable pageable);
    CompanyResponse toggleFavorite(UUID id);
}
