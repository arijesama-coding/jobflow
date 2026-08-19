package com.jobflow.mapper;

import com.jobflow.dto.response.InterviewResponse;
import com.jobflow.entity.Interview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InterviewMapper {

    @Mapping(target = "applicationId", expression = "java(interview.getApplication().getId())")
    @Mapping(target = "companyName", expression = "java(interview.getApplication().getCompany() != null ? interview.getApplication().getCompany().getName() : null)")
    @Mapping(target = "jobOfferTitle", expression = "java(interview.getApplication().getJobOffer() != null ? interview.getApplication().getJobOffer().getTitle() : null)")
    InterviewResponse toResponse(Interview interview);
}
