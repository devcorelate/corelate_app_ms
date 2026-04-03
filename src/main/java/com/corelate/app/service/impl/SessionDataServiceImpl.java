package com.corelate.app.service.impl;

import com.corelate.app.dto.SessionDataDto;
import com.corelate.app.entity.SessionData;
import com.corelate.app.exeption.ResourceNotFoundException;
import com.corelate.app.repository.SessionDataRepository;
import com.corelate.app.service.ISessionDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

        sessionDataDto.setSessionId(sessionId);
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

    @Override
    @Transactional(readOnly = true)
    public List<SessionDataDto> fetchAllSessionData() {
        return sessionDataRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SessionDataDto fetchSessionDataById(String sessionId) {
        SessionData sessionData = sessionDataRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("SessionData", "sessionId", sessionId));
        return mapToDto(sessionData);
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

    private SessionDataDto mapToDto(SessionData sessionData) {
        SessionDataDto dto = new SessionDataDto();
        dto.setSessionId(sessionData.getSessionId());
        dto.setWorkflowId(sessionData.getWorkflowId());
        dto.setStartedAt(sessionData.getStartedAt());
        dto.setLastUpdatedAt(sessionData.getLastUpdatedAt());
        dto.setCurrentNodeId(sessionData.getCurrentNodeId());
        dto.setSteps(toStepMap(sessionData.getSteps()));
        dto.setGatewayDecisions(toGatewayDecisionMap(sessionData.getGatewayDecisions()));
        dto.setReturnTo(sessionData.getReturnTo());
        return dto;
    }

    private Map<String, SessionDataDto.SessionStepDto> toStepMap(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid steps JSON stored for SessionData", ex);
        }
    }

    private Map<String, JsonNode> toGatewayDecisionMap(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid gateway decisions JSON stored for SessionData", ex);
        }
    }
}
