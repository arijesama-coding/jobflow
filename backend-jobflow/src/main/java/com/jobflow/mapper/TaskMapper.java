package com.jobflow.mapper;

import com.jobflow.dto.response.TaskResponse;
import com.jobflow.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "applicationId", expression = "java(task.getApplication() != null ? task.getApplication().getId() : null)")
    @Mapping(target = "applicationLabel", expression = "java(buildApplicationLabel(task))")
    TaskResponse toResponse(Task task);

    default String buildApplicationLabel(Task task) {
        if (task.getApplication() == null) return null;
        var app = task.getApplication();
        String title = app.getJobOffer() != null ? app.getJobOffer().getTitle() : null;
        String company = app.getCompany() != null ? app.getCompany().getName() : null;
        if (title != null && company != null) return title + " @ " + company;
        return title != null ? title : company;
    }
}
