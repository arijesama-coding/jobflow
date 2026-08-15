package com.jobflow.mapper;

import com.jobflow.dto.response.ApplicationResponse;
import com.jobflow.dto.response.ApplicationStatusHistoryResponse;
import com.jobflow.entity.Application;
import com.jobflow.entity.ApplicationStatusHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    @Mapping(target = "companyId", expression = "java(application.getCompany() != null ? application.getCompany().getId() : null)")
    @Mapping(target = "companyName", expression = "java(application.getCompany() != null ? application.getCompany().getName() : null)")
    @Mapping(target = "jobOfferId", expression = "java(application.getJobOffer() != null ? application.getJobOffer().getId() : null)")
    @Mapping(target = "jobOfferTitle", expression = "java(application.getJobOffer() != null ? application.getJobOffer().getTitle() : null)")
    ApplicationResponse toResponse(Application application);

    @Mapping(target = "fromStatus", expression = "java(history.getFromStatus() != null ? history.getFromStatus().name() : null)")
    @Mapping(target = "toStatus", expression = "java(history.getToStatus().name())")
    ApplicationStatusHistoryResponse toHistoryResponse(ApplicationStatusHistory history);
}
