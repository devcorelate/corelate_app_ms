package com.corelate.app.service.impl;

import com.corelate.app.dto.SessionFormFieldPairingDto;
import com.corelate.app.dto.SessionFormPairRequestDto;
import com.corelate.app.dto.SessionFormPairResultDto;
import com.corelate.app.entity.MockAppCertificateFieldMapping;
import com.corelate.app.entity.SessionData;
import com.corelate.app.entity.SessionFormFieldPairing;
import com.corelate.app.entity.SessionStep;
import com.corelate.app.exeption.ResourceNotFoundException;
import com.corelate.app.repository.MockAppCertificateFieldMappingRepository;
import com.corelate.app.repository.SessionDataRepository;
import com.corelate.app.repository.SessionFormFieldPairingRepository;
import com.corelate.app.service.ISessionFormPairingService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SessionFormPairingServiceImpl implements ISessionFormPairingService {

    private final SessionDataRepository sessionDataRepository;
    private final MockAppCertificateFieldMappingRepository mappingRepository;
    private final SessionFormFieldPairingRepository pairingRepository;

    public SessionFormPairingServiceImpl(SessionDataRepository sessionDataRepository,
                                         MockAppCertificateFieldMappingRepository mappingRepository,
                                         SessionFormFieldPairingRepository pairingRepository) {
        this.sessionDataRepository = sessionDataRepository;
        this.mappingRepository = mappingRepository;
        this.pairingRepository = pairingRepository;
    }

    @Override
    public SessionFormPairResultDto pairSessionFormData(SessionFormPairRequestDto requestDto) {
        SessionData sessionData = sessionDataRepository.findBySessionId(requestDto.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("SessionData", "sessionId", requestDto.getSessionId()));

        if (sessionData.getWorkflowId() == null || !sessionData.getWorkflowId().equals(requestDto.getWorkflowId())) {
            throw new IllegalArgumentException("workflowId mismatch with session data");
        }

        List<MockAppCertificateFieldMapping> mappings = mappingRepository
                .findByMockApp_AppIdAndMockApp_WorkflowIdAndMockApp_FormId(
                        requestDto.getMockAppId(),
                        requestDto.getWorkflowId(),
                        requestDto.getFormId()
                );

        int created = 0;
        int updated = 0;
        int skipped = 0;

        for (MockAppCertificateFieldMapping mapping : mappings) {
            String value = resolveSourceValue(sessionData, mapping.getSourcePath());
            if (value == null) {
                skipped++;
                continue;
            }

            Optional<SessionFormFieldPairing> existing = pairingRepository
                    .findBySessionIdAndWorkflowIdAndFormIdAndSourcePathAndTargetField(
                            requestDto.getSessionId(),
                            requestDto.getWorkflowId(),
                            requestDto.getFormId(),
                            mapping.getSourcePath(),
                            mapping.getTargetField()
                    );

            if (existing.isPresent()) {
                SessionFormFieldPairing pairing = existing.get();
                if (!value.equals(pairing.getValue())) {
                    pairing.setValue(value);
                    pairingRepository.save(pairing);
                    updated++;
                } else {
                    skipped++;
                }
            } else {
                SessionFormFieldPairing pairing = new SessionFormFieldPairing();
                pairing.setSessionId(requestDto.getSessionId());
                pairing.setWorkflowId(requestDto.getWorkflowId());
                pairing.setFormId(requestDto.getFormId());
                pairing.setSourcePath(mapping.getSourcePath());
                pairing.setTargetField(mapping.getTargetField());
                pairing.setValue(value);
                pairingRepository.save(pairing);
                created++;
            }
        }

        return new SessionFormPairResultDto(created, updated, skipped);
    }

    @Override
    public List<SessionFormFieldPairingDto> fetchBySessionId(String sessionId) {
        return pairingRepository.findBySessionId(sessionId)
                .stream()
                .map(pairing -> new SessionFormFieldPairingDto(
                        pairing.getSessionId(),
                        pairing.getWorkflowId(),
                        pairing.getFormId(),
                        pairing.getSourcePath(),
                        pairing.getTargetField(),
                        pairing.getValue()
                ))
                .toList();
    }

    private String resolveSourceValue(SessionData sessionData, String sourcePath) {
        String[] parts = sourcePath.split("\\.data\\.");
        if (parts.length == 2) {
            String stepId = parts[0];
            String fieldKey = parts[1];
            return resolveByStepAndField(sessionData, stepId, fieldKey);
        }

        for (SessionStep step : sessionData.getSteps()) {
            if (step.getSessionElementData() == null || step.getSessionElementData().getData() == null) {
                continue;
            }
            JsonNode fieldNode = step.getSessionElementData().getData().get(sourcePath);
            if (fieldNode != null && !fieldNode.isNull()) {
                return fieldNode.isTextual() ? fieldNode.asText() : fieldNode.toString();
            }
        }
        return null;
    }

    private String resolveByStepAndField(SessionData sessionData, String stepId, String fieldKey) {
        for (SessionStep step : sessionData.getSteps()) {
            if ((stepId.equals(step.getStepKey()) || stepId.equals(step.getElementId()))
                    && step.getSessionElementData() != null
                    && step.getSessionElementData().getData() != null) {
                JsonNode node = step.getSessionElementData().getData().get(fieldKey);
                if (node != null && !node.isNull()) {
                    return node.isTextual() ? node.asText() : node.toString();
                }
            }
        }
        return null;
    }
}
