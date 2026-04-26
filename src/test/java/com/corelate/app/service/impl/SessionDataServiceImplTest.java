package com.corelate.app.service.impl;

import com.corelate.app.dto.SessionDataDto;
import com.corelate.app.entity.SessionData;
import com.corelate.app.repository.SessionDataRepository;
import com.corelate.app.service.SessionUpdateSynchronizationHook;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionDataServiceImplTest {

    @Mock
    private SessionDataRepository sessionDataRepository;

    @Mock
    private ObjectProvider<SessionUpdateSynchronizationHook> synchronizationHookProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private SessionDataServiceImpl sessionDataService;

    @Test
    void addSessionData_shouldPersistCreatedByAndCreatedByEmail_forNewSession() {
        SessionDataDto dto = new SessionDataDto();
        dto.setSessionId("session-1");
        dto.setWorkflowId("wf-1");
        dto.setCreatedBy("Seth Edward");
        dto.setCreatedByEmail("seth@gmail.com");

        when(sessionDataRepository.findBySessionId("session-1")).thenReturn(Optional.empty());
        when(sessionDataRepository.save(any(SessionData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        sessionDataService.addSessionData(dto);

        ArgumentCaptor<SessionData> captor = ArgumentCaptor.forClass(SessionData.class);
        verify(sessionDataRepository).save(captor.capture());
        SessionData saved = captor.getValue();

        assertEquals("Seth Edward", saved.getCreatedBy());
        assertEquals("seth@gmail.com", saved.getCreatedByEmail());
    }
}
