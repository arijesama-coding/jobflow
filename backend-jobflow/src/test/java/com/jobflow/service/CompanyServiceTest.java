package com.jobflow.service;

import com.jobflow.dto.request.CompanyRequest;
import com.jobflow.entity.Company;
import com.jobflow.entity.User;
import com.jobflow.exception.ResourceNotFoundException;
import com.jobflow.mapper.CompanyMapper;
import com.jobflow.repository.CompanyRepository;
import com.jobflow.security.CurrentUserProvider;
import com.jobflow.service.impl.CompanyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * The one thing that must never regress in this module: a user can never
 * read, update, delete, or favorite another user's company. The spec calls
 * this out explicitly (section 3) — these tests pin that behaviour down.
 */
@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock private CompanyRepository companyRepository;
    @Mock private CompanyMapper companyMapper;
    @Mock private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private CompanyServiceImpl companyService;

    private User owner;
    private User intruder;
    private Company company;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(UUID.randomUUID()).email("owner@example.com").build();
        intruder = User.builder().id(UUID.randomUUID()).email("intruder@example.com").build();
        company = Company.builder().id(UUID.randomUUID()).user(owner).name("Acme Corp").build();
    }

    @Test
    void get_throwsNotFound_whenCompanyBelongsToAnotherUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(intruder);
        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));

        assertThatThrownBy(() -> companyService.get(company.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_throwsNotFound_whenCompanyBelongsToAnotherUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(intruder);
        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));

        assertThatThrownBy(() -> companyService.update(company.getId(), new CompanyRequest()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(companyRepository, never()).save(any());
    }

    @Test
    void delete_throwsNotFound_onSoftDeletedCompany_evenForOwner() {
        company.setDeletedAt(java.time.LocalDateTime.now());
        when(currentUserProvider.getCurrentUser()).thenReturn(owner);
        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));

        assertThatThrownBy(() -> companyService.delete(company.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void get_succeeds_forOwner() {
        when(currentUserProvider.getCurrentUser()).thenReturn(owner);
        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));
        when(companyMapper.toResponse(company)).thenReturn(
                com.jobflow.dto.response.CompanyResponse.builder().id(company.getId()).name("Acme Corp").build());

        var result = companyService.get(company.getId());

        org.assertj.core.api.Assertions.assertThat(result.getName()).isEqualTo("Acme Corp");
    }
}
