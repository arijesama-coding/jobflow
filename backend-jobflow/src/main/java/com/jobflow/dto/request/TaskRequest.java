package com.jobflow.dto.request;

import com.jobflow.entity.Priority;
import com.jobflow.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class TaskRequest {

    @NotBlank
    private String title;

    private String description;
    private UUID applicationId;
    private LocalDate dueDate;
    private Priority priority;
    private TaskStatus status;
}
