package com.corelate.app.service.impl;

import com.corelate.app.dto.SessionDataDto;
import com.corelate.app.entity.SessionData;
import com.corelate.app.entity.SessionElementData;
import com.corelate.app.entity.SessionStep;
import com.corelate.app.exeption.SessionIdMismatchException;
import com.corelate.app.exeption.ResourceNotFoundException;
import com.corelate.app.exeption.SessionUpdateConflictException;
import com.corelate.app.repository.SessionDataRepository;
import com.corelate.app.service.ISessionDataService;
import com.corelate.app.service.SessionUpdateSynchronizationHook;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SessionDataServiceImpl implements ISessionDataService {
    private static final Logger logger = LoggerFactory.getLogger(SessionDataServiceImpl.class);

    private final SessionDataRepository sessionDataRepository;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<SessionUpdateSynchronizationHook> synchronizationHookProvider;

    public SessionDataServiceImpl(SessionDataRepository sessionDataRepository, ObjectMapper objectMapper,
                                  ObjectProvider<SessionUpdateSynchronizationHook> synchronizationHookProvider) {
        this.sessionDataRepository = sessionDataRepository;
        this.objectMapper = objectMapper;
        this.synchronizationHookProvider = synchronizationHookProvider;
    }

    @Override
    @Transactional
    public void addSessionData(SessionDataDto sessionDataDto) {
        sessionDataRepository.findBySessionId(sessionDataDto.getSessionId())
                .ifPresentOrElse(existingData -> replaceSessionData(existingData, sessionDataDto),
                        () -> createSessionData(sessionDataDto));
    }

    @Override
    @Transactional
    public void updateSessionData(String sessionId, SessionDataDto sessionDataDto) {
        validateSessionIdConsistency(sessionId, sessionDataDto);
        SessionData existingData = sessionDataRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("SessionData", "sessionId", sessionId));

        SessionUpdateSynchronizationHook syncHook = synchronizationHookProvider.getIfAvailable();
        if (syncHook != null) {
            syncHook.afterLoadBeforeUpdate(sessionId);
        }

        try {
            applyMutableFields(existingData, sessionDataDto);
            existingData.setUpdatedAt(LocalDateTime.now());
            existingData.setUpdatedBy(sessionDataDto.getUpdatedBy());
            sessionDataRepository.saveAndFlush(existingData);
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException ex) {
            logger.warn("Session update conflict for sessionId={}, traceId={}", sessionId, getRequestCorrelationId());
            throw new SessionUpdateConflictException(sessionId, ex);
        }
    }

    @Override
    @Transactional
    public void deleteSessionData(String sessionId) {
        SessionData existingData = sessionDataRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("SessionData", "sessionId", sessionId));
        sessionDataRepository.delete(existingData);
    }

    @Override
    @Transactional
    public void deleteAllSessionData() {
        sessionDataRepository.deleteAll();
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

    private void createSessionData(SessionDataDto sessionDataDto) {
        SessionData newSessionData = mapToEntity(sessionDataDto, new SessionData());
        newSessionData.setCreatedAt(LocalDateTime.now());
        newSessionData.setCreatedBy(sessionDataDto.getCreatedBy());
        sessionDataRepository.save(newSessionData);
    }

    private void replaceSessionData(SessionData existingData, SessionDataDto sessionDataDto) {
        applyMutableFields(existingData, sessionDataDto);
        existingData.setUpdatedAt(LocalDateTime.now());
        existingData.setUpdatedBy(sessionDataDto.getUpdatedBy());
        sessionDataRepository.save(existingData);
    }

    private void applyMutableFields(SessionData existingData, SessionDataDto sessionDataDto) {
        existingData.setSessionId(sessionDataDto.getSessionId());
        existingData.setWorkflowId(sessionDataDto.getWorkflowId());
        existingData.setStartedAt(sessionDataDto.getStartedAt());
        existingData.setLastUpdatedAt(sessionDataDto.getLastUpdatedAt());
        existingData.setCurrentNodeId(sessionDataDto.getCurrentNodeId());
        existingData.setReturnTo(sessionDataDto.getReturnTo());
        existingData.setGatewayDecisions(toJson(sessionDataDto.getGatewayDecisions()));
        existingData.getSteps().clear();
        mapSteps(sessionDataDto, existingData);
    }

    private SessionData mapToEntity(SessionDataDto sessionDataDto, SessionData sessionData) {
        sessionData.setSessionId(sessionDataDto.getSessionId());
        sessionData.setWorkflowId(sessionDataDto.getWorkflowId());
        sessionData.setStartedAt(sessionDataDto.getStartedAt());
        sessionData.setLastUpdatedAt(sessionDataDto.getLastUpdatedAt());
        sessionData.setCurrentNodeId(sessionDataDto.getCurrentNodeId());
        sessionData.setReturnTo(sessionDataDto.getReturnTo());
        sessionData.setGatewayDecisions(toJson(sessionDataDto.getGatewayDecisions()));
        mapSteps(sessionDataDto, sessionData);
        return sessionData;
    }

    private void mapSteps(SessionDataDto sessionDataDto, SessionData sessionData) {
        if (sessionDataDto.getSteps() != null) {
            sessionDataDto.getSteps().forEach((stepKey, stepDto) -> {
                SessionStep sessionStep = new SessionStep();
                sessionStep.setStepKey(stepKey);
                sessionStep.setElementId(stepDto.getElementId());
                sessionStep.setElementType(stepDto.getElementType());
                sessionStep.setStatus(stepDto.getStatus());
                sessionStep.setCompletedAt(stepDto.getCompletedAt());
                sessionStep.setSessionData(sessionData);

                if (shouldIncludeSessionElementData(stepDto.getData())) {
                    SessionElementData sessionElementData = new SessionElementData();
                    sessionElementData.setWorkflowId(sessionDataDto.getWorkflowId());
                    sessionElementData.setData(stepDto.getData());
                    sessionElementData.setSessionStep(sessionStep);
                    sessionStep.setSessionElementData(sessionElementData);
                }

                sessionData.getSteps().add(sessionStep);
            });
        }
    }

    private void validateSessionIdConsistency(String pathSessionId, SessionDataDto sessionDataDto) {
        if (sessionDataDto.getSessionId() != null && !pathSessionId.equals(sessionDataDto.getSessionId())) {
            throw new SessionIdMismatchException(pathSessionId, sessionDataDto.getSessionId());
        }
        sessionDataDto.setSessionId(pathSessionId);
    }

    private String getRequestCorrelationId() {
        String traceId = MDC.get("trace_id");
        return traceId != null ? traceId : "N/A";
    }

    private boolean shouldIncludeSessionElementData(JsonNode data) {
        return data != null && !data.has("reviewMarks");
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

    private Map<String, SessionDataDto.SessionStepDto> toStepMap(List<SessionStep> steps) {
        if (steps == null || steps.isEmpty()) {
            return Collections.emptyMap();
        }

        return steps.stream().collect(Collectors.toMap(SessionStep::getStepKey, step -> {
            SessionDataDto.SessionStepDto stepDto = new SessionDataDto.SessionStepDto();
            stepDto.setElementId(step.getElementId());
            stepDto.setElementType(step.getElementType());
            stepDto.setStatus(step.getStatus());
            stepDto.setCompletedAt(step.getCompletedAt());
            if (step.getSessionElementData() != null) {
                stepDto.setData(step.getSessionElementData().getData());
            }
            return stepDto;
        }));
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
