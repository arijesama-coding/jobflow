package com.jobflow.service.impl;

import com.jobflow.dto.request.TaskRequest;
import com.jobflow.dto.response.TaskResponse;
import com.jobflow.entity.*;
import com.jobflow.exception.ResourceNotFoundException;
import com.jobflow.mapper.TaskMapper;
import com.jobflow.repository.ApplicationRepository;
import com.jobflow.repository.TaskRepository;
import com.jobflow.security.CurrentUserProvider;
import com.jobflow.service.TaskService;
import com.jobflow.specification.TaskSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ApplicationRepository applicationRepository;
    private final TaskMapper taskMapper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public TaskResponse create(TaskRequest request) {
        User user = currentUserProvider.getCurrentUser();

        Task task = Task.builder()
                .user(user)
                .application(resolveOwnedApplication(request.getApplicationId(), user))
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO)
                .build();

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponse update(UUID id, TaskRequest request) {
        Task task = getOwnedOrThrow(id);
        User user = currentUserProvider.getCurrentUser();

        task.setApplication(resolveOwnedApplication(request.getApplicationId(), user));
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getStatus() != null) task.setStatus(request.getStatus());

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        taskRepository.delete(getOwnedOrThrow(id));
    }

    @Override
    public TaskResponse get(UUID id) {
        return taskMapper.toResponse(getOwnedOrThrow(id));
    }

    @Override
    public Page<TaskResponse> list(UUID applicationId, TaskStatus status, Priority priority,
                                    LocalDate dueFrom, LocalDate dueTo, Pageable pageable) {
        User user = currentUserProvider.getCurrentUser();
        var spec = TaskSpecification.build(user, applicationId, status, priority, dueFrom, dueTo);
        return taskRepository.findAll(spec, pageable).map(taskMapper::toResponse);
    }

    @Override
    public List<TaskResponse> calendar(LocalDate from, LocalDate to) {
        User user = currentUserProvider.getCurrentUser();
        return taskRepository.findByUser_IdAndDueDateBetween(user.getId(), from, to).stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    // ===================== HELPERS =====================

    private Application resolveOwnedApplication(UUID applicationId, User user) {
        if (applicationId == null) return null;
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (application.getDeletedAt() != null || !application.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Application not found");
        }
        return application;
    }

    private Task getOwnedOrThrow(UUID id) {
        User user = currentUserProvider.getCurrentUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Task not found");
        }
        return task;
    }
}
