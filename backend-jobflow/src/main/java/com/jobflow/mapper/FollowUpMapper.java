package com.jobflow.mapper;

import com.jobflow.dto.response.FollowUpResponse;
import com.jobflow.entity.FollowUp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FollowUpMapper {

    @Mapping(target = "applicationId", expression = "java(followUp.getApplication().getId())")
    @Mapping(target = "companyName", expression = "java(followUp.getApplication().getCompany() != null ? followUp.getApplication().getCompany().getName() : null)")
    @Mapping(target = "jobOfferTitle", expression = "java(followUp.getApplication().getJobOffer() != null ? followUp.getApplication().getJobOffer().getTitle() : null)")
    FollowUpResponse toResponse(FollowUp followUp);
}
