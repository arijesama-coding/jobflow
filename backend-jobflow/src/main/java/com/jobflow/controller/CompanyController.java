package com.jobflow.controller;

import com.jobflow.dto.request.CompanyRequest;
import com.jobflow.dto.response.CompanyResponse;
import com.jobflow.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public Page<CompanyResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) Boolean favorite,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return companyService.list(search, industry, favorite, pageable);
    }

    @GetMapping("/{id}")
    public CompanyResponse get(@PathVariable UUID id) {
        return companyService.get(id);
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> create(@Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.status(201).body(companyService.create(request));
    }

    @PutMapping("/{id}")
    public CompanyResponse update(@PathVariable UUID id, @Valid @RequestBody CompanyRequest request) {
        return companyService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        companyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/favorite")
    public CompanyResponse toggleFavorite(@PathVariable UUID id) {
        return companyService.toggleFavorite(id);
    }
}
