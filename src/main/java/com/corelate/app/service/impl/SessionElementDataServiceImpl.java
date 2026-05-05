package com.corelate.app.service.impl;

import com.corelate.app.dto.FormElementLabelRequestDto;
import com.corelate.app.dto.FormElementLabelResponseDto;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.AbstractMap;

@Service
public class SessionElementDataServiceImpl implements ISessionElementDataService {

    private static final Logger logger = LoggerFactory.getLogger(SessionElementDataServiceImpl.class);
    private static final int LABEL_FETCH_BATCH_SIZE = 200;

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
        return buildDataWithLabels(sessionElementDataRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<SessionElementDataWithLabelDto>> fetchAllDataWithLabelByWorkflowId(String workflowId) {
        List<SessionElementData> dataList = sessionElementDataRepository.findByWorkflowId(workflowId);
        if (dataList.isEmpty()) {
            throw new ResourceNotFoundException("SessionElementData", "workflowId", workflowId);
        }

        return buildDataWithLabels(dataList);
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

        updates.forEach((key, value) -> objectNode.set(key, normalizeEmailValueIfNeeded(key, value)));
    }

    private JsonNode normalizeEmailValueIfNeeded(String key, JsonNode value) {
        if (key == null || value == null || !value.isTextual() || !key.toLowerCase().contains("email")) {
            return value;
        }

        return objectMapper.getNodeFactory().textNode(value.asText().trim());
    }

    private List<SessionElementDataWithLabelDto> mapToDataWithLabelDtos(SessionElementData sessionElementData,
                                                                        Map<String, FormElementLabelResponseDto> labelCache) {
        JsonNode data = sessionElementData.getData();
        List<Map.Entry<String, JsonNode>> fieldEntries = new ArrayList<>();
        Set<String> missingElementIds = new HashSet<>();
        Iterator<Map.Entry<String, JsonNode>> fields = data.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            fieldEntries.add(new AbstractMap.SimpleEntry<>(field.getKey(), field.getValue()));
            if (!labelCache.containsKey(field.getKey())) {
                missingElementIds.add(field.getKey());
            }
        }

        if (!missingElementIds.isEmpty()) {
            labelCache.putAll(fetchLabelCacheByElementIds(missingElementIds));
        }

        List<SessionElementDataWithLabelDto> sessionElementDataWithLabelDtos = new ArrayList<>();
        for (Map.Entry<String, JsonNode> field : fieldEntries) {
            String elementId = field.getKey();
            JsonNode value = field.getValue();
            FormElementLabelResponseDto labelResponse = labelCache.get(elementId);
            String label = normalizeLabel(labelResponse == null ? null : labelResponse.getLabel());
            String formId = labelResponse == null ? null : labelResponse.getFormId();
            sessionElementDataWithLabelDtos.add(new SessionElementDataWithLabelDto(elementId, label, value, formId));
        }

        return sessionElementDataWithLabelDtos;
    }

    private boolean hasDataObject(SessionElementData sessionElementData) {
        JsonNode data = sessionElementData.getData();
        return data != null && data.isObject();
    }


    private Map<String, List<SessionElementDataWithLabelDto>> buildDataWithLabels(List<SessionElementData> dataList) {
        List<SessionElementData> filteredData = dataList.stream()
                .filter(this::hasDataObject)
                .toList();

        Map<String, FormElementLabelResponseDto> labelCache = new LinkedHashMap<>();
        return filteredData.stream()
                .collect(Collectors.groupingBy(
                        SessionElementData::getWorkflowId,
                        LinkedHashMap::new,
                        Collectors.flatMapping(
                                sessionElementData -> mapToDataWithLabelDtos(sessionElementData, labelCache).stream(),
                                Collectors.toList()
                        )
                ));
    }

    private Map<String, FormElementLabelResponseDto> fetchLabelCacheByElementIds(Set<String> elementIds) {
        if (elementIds == null || elementIds.isEmpty()) {
            return Map.of();
        }

        List<String> uniqueElementIds = new ArrayList<>(elementIds);
        Map<String, FormElementLabelResponseDto> labelCache = new LinkedHashMap<>();

        try {
            for (int startIndex = 0; startIndex < uniqueElementIds.size(); startIndex += LABEL_FETCH_BATCH_SIZE) {
                int endIndex = Math.min(startIndex + LABEL_FETCH_BATCH_SIZE, uniqueElementIds.size());
                List<String> elementIdBatch = uniqueElementIds.subList(startIndex, endIndex);

                List<FormElementLabelResponseDto> labels = formFeignClient.fetchLabelsByElementIds(
                        new FormElementLabelRequestDto(elementIdBatch)
                );

                if (labels == null || labels.isEmpty()) {
                    continue;
                }

                labels.stream()
                        .filter(label -> label.getElementId() != null)
                        .forEach(label -> labelCache.putIfAbsent(label.getElementId(), label));
            }

            return labelCache;
        } catch (Exception exception) {
            logger.warn("Unable to fetch labels from forms service for elementIds={}", elementIds, exception);
            return Map.of();
        }
    }

    private String normalizeLabel(String rawLabelResponse) {
        if (rawLabelResponse == null || rawLabelResponse.isBlank()) {
            return "none";
        }

        return rawLabelResponse;
    }

}
