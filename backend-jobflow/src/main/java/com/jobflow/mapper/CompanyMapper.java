package com.jobflow.mapper;

import com.jobflow.dto.request.CompanyRequest;
import com.jobflow.dto.response.CompanyResponse;
import com.jobflow.entity.Company;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    CompanyResponse toResponse(Company company);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(CompanyRequest request, @MappingTarget Company company);
}
