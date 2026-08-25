package com.jobflow.service.impl;

import com.jobflow.dto.request.NoteRequest;
import com.jobflow.dto.response.NoteResponse;
import com.jobflow.entity.*;
import com.jobflow.exception.ResourceNotFoundException;
import com.jobflow.mapper.NoteMapper;
import com.jobflow.repository.*;
import com.jobflow.security.CurrentUserProvider;
import com.jobflow.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Notes are polymorphic (entity_type + entity_id, no FK) so ownership of the
 * *target* entity has to be verified per type before a note can be attached
 * to it. Once a note exists, its own user_id is the source of truth for
 * update/delete — no need to re-walk the target entity each time.
 */
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final ApplicationRepository applicationRepository;
    private final CompanyRepository companyRepository;
    private final InterviewRepository interviewRepository;
    private final ContactRepository contactRepository;
    private final NoteMapper noteMapper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public NoteResponse create(NoteRequest request) {
        User user = currentUserProvider.getCurrentUser();
        verifyTargetOwnership(request.getEntityType(), request.getEntityId(), user);

        Note note = Note.builder()
                .user(user)
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .content(request.getContent())
                .build();

        return noteMapper.toResponse(noteRepository.save(note));
    }

    @Override
    @Transactional
    public NoteResponse update(UUID id, String content) {
        Note note = getOwnedOrThrow(id);
        note.setContent(content);
        return noteMapper.toResponse(noteRepository.save(note));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        noteRepository.delete(getOwnedOrThrow(id));
    }

    @Override
    public List<NoteResponse> listForEntity(NoteEntityType entityType, UUID entityId) {
        User user = currentUserProvider.getCurrentUser();
        return noteRepository.findByUser_IdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(user.getId(), entityType, entityId).stream()
                .map(noteMapper::toResponse)
                .toList();
    }

    // ===================== HELPERS =====================

    private void verifyTargetOwnership(NoteEntityType type, UUID entityId, User user) {
        boolean owned = switch (type) {
            case APPLICATION -> applicationRepository.findById(entityId)
                    .map(a -> a.getUser().getId().equals(user.getId())).orElse(false);
            case COMPANY -> companyRepository.findById(entityId)
                    .map(c -> c.getUser().getId().equals(user.getId())).orElse(false);
            case INTERVIEW -> interviewRepository.findById(entityId)
                    .map(i -> i.getApplication().getUser().getId().equals(user.getId())).orElse(false);
            case CONTACT -> contactRepository.findById(entityId)
                    .map(c -> c.getUser().getId().equals(user.getId())).orElse(false);
        };

        if (!owned) {
            throw new ResourceNotFoundException(type.name() + " not found");
        }
    }

    private Note getOwnedOrThrow(UUID id) {
        User user = currentUserProvider.getCurrentUser();
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

        if (!note.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Note not found");
        }
        return note;
    }
}
