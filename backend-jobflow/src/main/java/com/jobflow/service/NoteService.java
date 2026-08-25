package com.jobflow.service;

import com.jobflow.dto.request.NoteRequest;
import com.jobflow.dto.response.NoteResponse;
import com.jobflow.entity.NoteEntityType;

import java.util.List;
import java.util.UUID;

public interface NoteService {
    NoteResponse create(NoteRequest request);
    NoteResponse update(UUID id, String content);
    void delete(UUID id);
    List<NoteResponse> listForEntity(NoteEntityType entityType, UUID entityId);
}
