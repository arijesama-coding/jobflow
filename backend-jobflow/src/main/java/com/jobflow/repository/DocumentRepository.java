package com.jobflow.repository;

import com.jobflow.entity.Document;
import com.jobflow.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID>, JpaSpecificationExecutor<Document> {

    List<Document> findByUser_IdAndTypeAndApplication_Id(UUID userId, DocumentType type, UUID applicationId);

    List<Document> findByUser_IdAndTypeAndApplicationIsNull(UUID userId, DocumentType type);
}
