package com.jobflow.mapper;

import com.jobflow.dto.response.ContactResponse;
import com.jobflow.entity.Application;
import com.jobflow.entity.Contact;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ContactMapper {

    @Mapping(target = "companyId", expression = "java(contact.getCompany() != null ? contact.getCompany().getId() : null)")
    @Mapping(target = "companyName", expression = "java(contact.getCompany() != null ? contact.getCompany().getName() : null)")
    @Mapping(target = "applicationIds", expression = "java(mapApplicationIds(contact.getApplications()))")
    ContactResponse toResponse(Contact contact);

    default Set<UUID> mapApplicationIds(Set<Application> applications) {
        return applications.stream().map(Application::getId).collect(Collectors.toSet());
    }
}
