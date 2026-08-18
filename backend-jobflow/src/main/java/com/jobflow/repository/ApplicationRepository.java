package com.jobflow.repository;

import com.jobflow.entity.Application;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID>, JpaSpecificationExecutor<Application> {

    @EntityGraph(attributePaths = {"company", "jobOffer"})
    List<Application> findByUser_IdAndDeletedAtIsNull(UUID userId);
}
