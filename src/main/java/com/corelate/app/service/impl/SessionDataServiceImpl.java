package com.corelate.app.service.impl;

import com.corelate.app.dto.SessionDataDto;
import com.corelate.app.entity.SessionData;
import com.corelate.app.exeption.ResourceNotFoundException;
import com.corelate.app.repository.SessionDataRepository;
import com.corelate.app.service.ISessionDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SessionDataServiceImpl implements ISessionDataService {

    private final SessionDataRepository sessionDataRepository;
    private final ObjectMapper objectMapper;

    public SessionDataServiceImpl(SessionDataRepository sessionDataRepository, ObjectMapper objectMapper) {
        this.sessionDataRepository = sessionDataRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void addSessionData(SessionDataDto sessionDataDto) {
        SessionData sessionData = mapToEntity(sessionDataDto, new SessionData());
        sessionData.setCreatedAt(LocalDateTime.now());
        sessionData.setCreatedBy(sessionDataDto.getCreatedBy());
        sessionDataRepository.save(sessionData);
    }

    @Override
    public void updateSessionData(String sessionId, SessionDataDto sessionDataDto) {
        SessionData existingData = sessionDataRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("SessionData", "sessionId", sessionId));

        mapToEntity(sessionDataDto, existingData);
        existingData.setUpdatedAt(LocalDateTime.now());
        existingData.setUpdatedBy(sessionDataDto.getUpdatedBy());
        sessionDataRepository.save(existingData);
    }

    @Override
    public void deleteSessionData(String sessionId) {
        SessionData existingData = sessionDataRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("SessionData", "sessionId", sessionId));
        sessionDataRepository.delete(existingData);
    }

    private SessionData mapToEntity(SessionDataDto sessionDataDto, SessionData sessionData) {
        sessionData.setSessionId(sessionDataDto.getSessionId());
        sessionData.setWorkflowId(sessionDataDto.getWorkflowId());
        sessionData.setStartedAt(sessionDataDto.getStartedAt());
        sessionData.setLastUpdatedAt(sessionDataDto.getLastUpdatedAt());
        sessionData.setCurrentNodeId(sessionDataDto.getCurrentNodeId());
        sessionData.setReturnTo(sessionDataDto.getReturnTo());
        sessionData.setSteps(toJson(sessionDataDto.getSteps()));
        sessionData.setGatewayDecisions(toJson(sessionDataDto.getGatewayDecisions()));
        return sessionData;
    }

    private String toJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid JSON payload for SessionData", ex);
        }
    }
}
