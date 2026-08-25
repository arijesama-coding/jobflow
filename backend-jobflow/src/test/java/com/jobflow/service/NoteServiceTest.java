package com.jobflow.service;

import com.jobflow.dto.request.NoteRequest;
import com.jobflow.entity.*;
import com.jobflow.exception.ResourceNotFoundException;
import com.jobflow.mapper.NoteMapper;
import com.jobflow.repository.*;
import com.jobflow.security.CurrentUserProvider;
import com.jobflow.service.impl.NoteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Notes are polymorphic — entity_type + entity_id with no FK constraint —
 * so ownership of the *target* has to be checked per type in application
 * code. Each branch of that switch is a place this can quietly regress
 * (e.g. someone adds a new NoteEntityType and forgets the case), so every
 * branch gets a test rather than trusting the switch is exhaustive by eye.
 */
@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock private NoteRepository noteRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private InterviewRepository interviewRepository;
    @Mock private ContactRepository contactRepository;
    @Mock private NoteMapper noteMapper;
    @Mock private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private NoteServiceImpl noteService;

    private User owner;
    private User intruder;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(UUID.randomUUID()).email("owner@example.com").build();
        intruder = User.builder().id(UUID.randomUUID()).email("intruder@example.com").build();
    }

    @Test
    void create_rejectsNoteOnApplication_belongingToAnotherUser() {
        UUID applicationId = UUID.randomUUID();
        Application application = Application.builder().id(applicationId).user(owner).build();
        when(currentUserProvider.getCurrentUser()).thenReturn(intruder);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> noteService.create(request(NoteEntityType.APPLICATION, applicationId)))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(noteRepository, never()).save(any());
    }

    @Test
    void create_rejectsNoteOnInterview_whoseApplicationBelongsToAnotherUser() {
        UUID interviewId = UUID.randomUUID();
        Application application = Application.builder().id(UUID.randomUUID()).user(owner).build();
        Interview interview = Interview.builder().id(interviewId).application(application).build();
        when(currentUserProvider.getCurrentUser()).thenReturn(intruder);
        when(interviewRepository.findById(interviewId)).thenReturn(Optional.of(interview));

        assertThatThrownBy(() -> noteService.create(request(NoteEntityType.INTERVIEW, interviewId)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_rejectsNoteOnContact_belongingToAnotherUser() {
        UUID contactId = UUID.randomUUID();
        Contact contact = Contact.builder().id(contactId).user(owner).build();
        when(currentUserProvider.getCurrentUser()).thenReturn(intruder);
        when(contactRepository.findById(contactId)).thenReturn(Optional.of(contact));

        assertThatThrownBy(() -> noteService.create(request(NoteEntityType.CONTACT, contactId)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_rejectsNoteOnCompany_belongingToAnotherUser() {
        UUID companyId = UUID.randomUUID();
        Company company = Company.builder().id(companyId).user(owner).build();
        when(currentUserProvider.getCurrentUser()).thenReturn(intruder);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        assertThatThrownBy(() -> noteService.create(request(NoteEntityType.COMPANY, companyId)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_rejectsNoteOnNonExistentEntity() {
        UUID missingId = UUID.randomUUID();
        when(currentUserProvider.getCurrentUser()).thenReturn(owner);
        when(applicationRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.create(request(NoteEntityType.APPLICATION, missingId)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private NoteRequest request(NoteEntityType type, UUID entityId) {
        NoteRequest request = new NoteRequest();
        request.setEntityType(type);
        request.setEntityId(entityId);
        request.setContent("some note");
        return request;
    }
}
