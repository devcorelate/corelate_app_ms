package com.corelate.app.service.impl;

import com.corelate.app.dto.SessionElementDataWithLabelDto;
import com.corelate.app.entity.SessionElementData;
import com.corelate.app.exeption.ResourceNotFoundException;
import com.corelate.app.repository.SessionElementDataRepository;
import com.corelate.app.service.ISessionElementDataService;
import com.corelate.app.service.client.FormFeignClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SessionElementDataServiceImpl implements ISessionElementDataService {

    private static final Logger logger = LoggerFactory.getLogger(SessionElementDataServiceImpl.class);

    private final SessionElementDataRepository sessionElementDataRepository;
    private final FormFeignClient formFeignClient;
    private final ObjectMapper objectMapper;

    public SessionElementDataServiceImpl(SessionElementDataRepository sessionElementDataRepository,
                                         FormFeignClient formFeignClient,
                                         ObjectMapper objectMapper) {
        this.sessionElementDataRepository = sessionElementDataRepository;
        this.formFeignClient = formFeignClient;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<JsonNode> fetchAllData() {
        return sessionElementDataRepository.findAll()
                .stream()
                .map(SessionElementData::getData)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<SessionElementDataWithLabelDto>> fetchAllDataWithLabel() {
        Map<String, String> labelCache = new HashMap<>();
        return sessionElementDataRepository.findAll()
                .stream()
                .filter(this::hasDataObject)
                .collect(Collectors.groupingBy(
                        SessionElementData::getWorkflowId,
                        LinkedHashMap::new,
                        Collectors.flatMapping(
                                sessionElementData -> mapToDataWithLabelDtos(sessionElementData, labelCache).stream(),
                                Collectors.toList()
                        )
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<JsonNode> fetchAllDataByWorkflowId(String workflowId) {
        List<SessionElementData> dataList = sessionElementDataRepository.findByWorkflowId(workflowId);
        if (dataList.isEmpty()) {
            throw new ResourceNotFoundException("SessionElementData", "workflowId", workflowId);
        }
        return dataList.stream().map(SessionElementData::getData).toList();
    }

    @Override
    @Transactional
    public List<JsonNode> updateDataByWorkflowId(String workflowId, Map<String, JsonNode> updates) {
        List<SessionElementData> dataList = sessionElementDataRepository.findByWorkflowId(workflowId);
        if (dataList.isEmpty()) {
            throw new ResourceNotFoundException("SessionElementData", "workflowId", workflowId);
        }

        dataList.forEach(sessionElementData -> applyUpdates(sessionElementData.getData(), updates));
        sessionElementDataRepository.saveAll(dataList);

        return dataList.stream().map(SessionElementData::getData).toList();
    }

    private void applyUpdates(JsonNode data, Map<String, JsonNode> updates) {
        if (!(data instanceof ObjectNode objectNode) || updates == null || updates.isEmpty()) {
            return;
        }

        updates.forEach((id, value) -> {
            if (objectNode.has(id)) {
                objectNode.set(id, value);
            }
        });
    }

    private List<SessionElementDataWithLabelDto> mapToDataWithLabelDtos(SessionElementData sessionElementData,
                                                                        Map<String, String> labelCache) {
        JsonNode data = sessionElementData.getData();
        List<SessionElementDataWithLabelDto> sessionElementDataWithLabelDtos = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> fields = data.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String elementId = field.getKey();
            JsonNode value = field.getValue();
            String label = labelCache.computeIfAbsent(elementId, this::resolveLabel);
            sessionElementDataWithLabelDtos.add(new SessionElementDataWithLabelDto(elementId, label, value));
        }

        return sessionElementDataWithLabelDtos;
    }

    private boolean hasDataObject(SessionElementData sessionElementData) {
        JsonNode data = sessionElementData.getData();
        return data != null && data.isObject();
    }

    private String resolveLabel(String elementId) {
        if (elementId == null) {
            return "none";
        }

        try {
            return normalizeLabel(formFeignClient.fetchLabelByElementId(elementId));
        } catch (Exception exception) {
            logger.warn("Unable to fetch label from forms service for elementId={}", elementId, exception);
            return "none";
        }
    }

    private String normalizeLabel(String rawLabelResponse) {
        if (rawLabelResponse == null || rawLabelResponse.isBlank()) {
            return "none";
        }

        String trimmedResponse = rawLabelResponse.trim();
        if (!trimmedResponse.startsWith("{")) {
            return rawLabelResponse;
        }

        try {
            JsonNode labelResponseNode = objectMapper.readTree(trimmedResponse);
            if (labelResponseNode.hasNonNull("label")) {
                String label = labelResponseNode.get("label").asText();
                return label.isBlank() ? "none" : label;
            }
            return "none";
        } catch (Exception exception) {
            logger.warn("Unable to parse label response from forms service: {}", rawLabelResponse, exception);
        }
        return "none";
    }

}
