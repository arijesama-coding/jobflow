package com.jobflow.service;

import com.jobflow.dto.request.JobOfferRequest;
import com.jobflow.dto.response.JobOfferResponse;
import com.jobflow.entity.ContractType;
import com.jobflow.entity.RemoteType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface JobOfferService {
    JobOfferResponse create(JobOfferRequest request);
    JobOfferResponse update(UUID id, JobOfferRequest request);
    void delete(UUID id);
    JobOfferResponse get(UUID id);
    Page<JobOfferResponse> list(String search, UUID companyId, RemoteType remoteType,
                                 ContractType contractType, Boolean favorite, Boolean archived, Pageable pageable);
    JobOfferResponse toggleFavorite(UUID id);
    JobOfferResponse toggleArchived(UUID id);
}
