package com.jobflow.service;

import com.jobflow.dto.request.TaskRequest;
import com.jobflow.dto.response.TaskResponse;
import com.jobflow.entity.Priority;
import com.jobflow.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TaskService {
    TaskResponse create(TaskRequest request);
    TaskResponse update(UUID id, TaskRequest request);
    void delete(UUID id);
    TaskResponse get(UUID id);
    Page<TaskResponse> list(UUID applicationId, TaskStatus status, Priority priority,
                             LocalDate dueFrom, LocalDate dueTo, Pageable pageable);
    List<TaskResponse> calendar(LocalDate from, LocalDate to);
}
