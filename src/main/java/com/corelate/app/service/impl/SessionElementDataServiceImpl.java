package com.corelate.app.service.impl;

import com.corelate.app.dto.SessionElementDataWithLabelDto;
import com.corelate.app.entity.SessionElementData;
import com.corelate.app.exeption.ResourceNotFoundException;
import com.corelate.app.repository.SessionElementDataRepository;
import com.corelate.app.service.ISessionElementDataService;
import com.corelate.app.service.client.FormFeignClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class SessionElementDataServiceImpl implements ISessionElementDataService {

    private static final Logger logger = LoggerFactory.getLogger(SessionElementDataServiceImpl.class);

    private final SessionElementDataRepository sessionElementDataRepository;
    private final FormFeignClient formFeignClient;

    public SessionElementDataServiceImpl(SessionElementDataRepository sessionElementDataRepository,
                                         FormFeignClient formFeignClient) {
        this.sessionElementDataRepository = sessionElementDataRepository;
        this.formFeignClient = formFeignClient;
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
    public List<SessionElementDataWithLabelDto> fetchAllDataWithLabel() {
        return sessionElementDataRepository.findAll()
                .stream()
                .map(this::mapToDataWithLabelDto)
                .toList();
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

    private SessionElementDataWithLabelDto mapToDataWithLabelDto(SessionElementData sessionElementData) {
        JsonNode data = sessionElementData.getData();
        String elementId = extractElementId(data);
        JsonNode value = extractValue(data);
        String label = resolveLabel(elementId);
        return new SessionElementDataWithLabelDto(elementId, label, value);
    }

    private String resolveLabel(String elementId) {
        if (elementId == null) {
            return null;
        }

        try {
            return formFeignClient.fetchLabelByElementId(elementId);
        } catch (Exception exception) {
            logger.warn("Unable to fetch label from forms service for elementId={}", elementId, exception);
            return null;
        }
    }

    private String extractElementId(JsonNode data) {
        if (data == null || !data.hasNonNull("elementId")) {
            return null;
        }
        return data.get("elementId").asText();
    }

    private JsonNode extractValue(JsonNode data) {
        if (data == null || !data.has("value")) {
            return null;
        }
        return data.get("value");
    }
}
