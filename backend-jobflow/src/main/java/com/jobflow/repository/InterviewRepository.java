package com.jobflow.repository;

import com.jobflow.entity.Interview;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface InterviewRepository extends JpaRepository<Interview, UUID>, JpaSpecificationExecutor<Interview> {

    @EntityGraph(attributePaths = {"application", "application.company", "application.jobOffer"})
    List<Interview> findByApplication_User_IdAndScheduledAtBetween(UUID userId, LocalDateTime from, LocalDateTime to);
}
