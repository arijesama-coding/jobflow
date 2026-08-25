package com.jobflow.mapper;

import com.jobflow.dto.response.NoteResponse;
import com.jobflow.entity.Note;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NoteMapper {
    NoteResponse toResponse(Note note);
}
