package com.jobflow.repository;

import com.jobflow.entity.Task;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    @EntityGraph(attributePaths = {"application"})
    List<Task> findByUser_IdAndDueDateBetween(UUID userId, LocalDate from, LocalDate to);
}
