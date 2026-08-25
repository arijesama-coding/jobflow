package com.jobflow.mapper;

import com.jobflow.dto.response.DocumentResponse;
import com.jobflow.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(target = "applicationId", expression = "java(document.getApplication() != null ? document.getApplication().getId() : null)")
    DocumentResponse toResponse(Document document);
}
