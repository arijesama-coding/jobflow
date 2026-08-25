package com.jobflow.repository;

import com.jobflow.entity.FollowUp;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FollowUpRepository extends JpaRepository<FollowUp, UUID>, JpaSpecificationExecutor<FollowUp> {

    @EntityGraph(attributePaths = {"application", "application.company", "application.jobOffer"})
    List<FollowUp> findByApplication_User_IdAndFollowUpDateBetween(UUID userId, LocalDate from, LocalDate to);

    List<FollowUp> findByApplication_User_IdAndStatus(UUID userId, com.jobflow.entity.FollowUpStatus status);
}
