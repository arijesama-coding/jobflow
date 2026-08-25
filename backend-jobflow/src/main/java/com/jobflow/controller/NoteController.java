package com.jobflow.controller;

import com.jobflow.dto.request.NoteRequest;
import com.jobflow.dto.request.NoteUpdateRequest;
import com.jobflow.dto.response.NoteResponse;
import com.jobflow.entity.NoteEntityType;
import com.jobflow.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @GetMapping
    public List<NoteResponse> listForEntity(@RequestParam NoteEntityType entityType, @RequestParam UUID entityId) {
        return noteService.listForEntity(entityType, entityId);
    }

    @PostMapping
    public ResponseEntity<NoteResponse> create(@Valid @RequestBody NoteRequest request) {
        return ResponseEntity.status(201).body(noteService.create(request));
    }

    @PutMapping("/{id}")
    public NoteResponse update(@PathVariable UUID id, @Valid @RequestBody NoteUpdateRequest request) {
        return noteService.update(id, request.getContent());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        noteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
