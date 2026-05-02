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
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    @Transactional
    public SessionFormPairResultDto pairSessionFormData(SessionFormPairRequestDto requestDto) {
        SessionData sessionData = sessionDataRepository.findBySessionId(requestDto.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("SessionData", "sessionId", requestDto.getSessionId()));

        if (sessionData.getWorkflowId() == null || !sessionData.getWorkflowId().equals(requestDto.getWorkflowId())) {
            throw new IllegalArgumentException("workflowId mismatch with session data");
        }

        List<MockAppCertificateFieldMapping> mappings;
        if (StringUtils.hasText(requestDto.getMockAppId()) && StringUtils.hasText(requestDto.getFormId())) {
            mappings = mappingRepository.findByMockApp_AppIdAndMockApp_WorkflowIdAndMockApp_FormId(
                    requestDto.getMockAppId(),
                    requestDto.getWorkflowId(),
                    requestDto.getFormId()
            );
        } else {
            mappings = mappingRepository.findByMockApp_WorkflowId(requestDto.getWorkflowId());
        }

        int created = 0;
        int updated = 0;
        int skipped = 0;

        for (MockAppCertificateFieldMapping mapping : mappings) {
            String resolvedSourcePath = StringUtils.hasText(mapping.getSourcePath())
                    ? mapping.getSourcePath()
                    : mapping.getTargetField();

            String value = resolveSourceValue(sessionData, resolvedSourcePath);
            if (value == null) {
                skipped++;
                continue;
            }

            String effectiveFormId = StringUtils.hasText(requestDto.getFormId())
                    ? requestDto.getFormId()
                    : mapping.getMockApp() != null ? mapping.getMockApp().getFormId() : null;

            if (!StringUtils.hasText(effectiveFormId)) {
                skipped++;
                continue;
            }

            Optional<SessionFormFieldPairing> existing = pairingRepository
                    .findBySessionIdAndWorkflowIdAndFormIdAndSourcePathAndTargetField(
                            requestDto.getSessionId(),
                            requestDto.getWorkflowId(),
                            effectiveFormId,
                            resolvedSourcePath,
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
                pairing.setFormId(effectiveFormId);
                pairing.setSourcePath(resolvedSourcePath);
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
        String sourceLabel = extractSourceLabel(sourcePath);

        String sourceLabel = extractSourceLabel(sourcePath);

        for (SessionStep step : sessionData.getSteps()) {
            if (step.getSessionElementData() == null || step.getSessionElementData().getData() == null) {
                continue;
            }

            JsonNode stepData = step.getSessionElementData().getData();

            Map<String, String> normalizedLabelValueMap = extractLabelValueMap(stepData);
            if (sourceLabel != null) {
                String value = normalizedLabelValueMap.get(sourceLabel.toLowerCase());
                if (value != null) {
                    return value;
                }
            }

            String directValue = normalizedLabelValueMap.get(sourcePath.toLowerCase());
            if (directValue != null) {
                return directValue;
            }

            if (sourceLabel != null) {
                JsonNode mappedData = stepData.get("mappedData");
                if (mappedData != null && mappedData.isObject()) {
                    JsonNode mappedValue = mappedData.get(sourceLabel);
                    if (mappedValue != null && !mappedValue.isNull()) {
                        return mappedValue.isTextual() ? mappedValue.asText() : mappedValue.toString();
                    }
                }

                var fieldNames = stepData.fieldNames();
                while (fieldNames.hasNext()) {
                    String key = fieldNames.next();
                    String keyLabel = extractLabelFromKey(key);
                    if (key.endsWith("-" + sourceLabel)
                            || (keyLabel != null && keyLabel.equalsIgnoreCase(sourceLabel))) {
                        JsonNode matchedNode = stepData.get(key);
                        if (matchedNode != null && !matchedNode.isNull()) {
                            return matchedNode.isTextual() ? matchedNode.asText() : matchedNode.toString();
                        }
                    }
                }
            }
        }
        return null;
    }

    private Map<String, String> extractLabelValueMap(JsonNode stepData) {
        Map<String, String> labelValue = new HashMap<>();

        if (!(stepData instanceof ObjectNode objectNode)) {
            return labelValue;
        }

        Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();
            JsonNode valueNode = field.getValue();

            if ("mappedData".equals(key) && valueNode != null && valueNode.isObject()) {
                Iterator<Map.Entry<String, JsonNode>> mappedFields = valueNode.fields();
                while (mappedFields.hasNext()) {
                    Map.Entry<String, JsonNode> mappedField = mappedFields.next();
                    putScalar(labelValue, mappedField.getKey(), mappedField.getValue());
                }
                continue;
            }

            putScalar(labelValue, key, valueNode);
            String label = extractLabelFromKey(key);
            if (StringUtils.hasText(label)) {
                putScalar(labelValue, label, valueNode);
            }
        }

        return labelValue;
    }

    private void putScalar(Map<String, String> target, String key, JsonNode valueNode) {
        if (!StringUtils.hasText(key) || valueNode == null || valueNode.isNull() || valueNode.isContainerNode()) {
            return;
        }
        target.put(key.toLowerCase(), valueNode.asText());
    }

    private String extractSourceLabel(String sourcePath) {
        if (!StringUtils.hasText(sourcePath)) {
            return null;
        }

        int dashIndex = sourcePath.lastIndexOf('-');
        if (dashIndex < 0 || dashIndex == sourcePath.length() - 1) {
            return null;
        }

        return sourcePath.substring(dashIndex + 1);
    }

    private String extractLabelFromKey(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }
        int dashIndex = key.lastIndexOf('-');
        return (dashIndex >= 0 && dashIndex < key.length() - 1) ? key.substring(dashIndex + 1) : key;
    }

}
