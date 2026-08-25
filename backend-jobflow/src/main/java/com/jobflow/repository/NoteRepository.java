package com.jobflow.repository;

import com.jobflow.entity.Note;
import com.jobflow.entity.NoteEntityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NoteRepository extends JpaRepository<Note, UUID> {
    List<Note> findByUser_IdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(UUID userId, NoteEntityType entityType, UUID entityId);
}
