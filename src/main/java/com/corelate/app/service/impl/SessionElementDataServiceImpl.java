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
import org.springframework.beans.factory.annotation.Value;
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
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SessionElementDataServiceImpl implements ISessionElementDataService {

    private static final Logger logger = LoggerFactory.getLogger(SessionElementDataServiceImpl.class);
    private static final String GENERATED_PDF_BASE64_FIELD = "generatedPdfBase64";

    private final SessionElementDataRepository sessionElementDataRepository;

    @Value("${app.form-label-fetch.batch-size:100}")
    private int labelFetchBatchSize;

    @Value("${app.form-label-fetch.max-retries:3}")
    private int labelFetchMaxRetries;

    @Value("${app.form-label-fetch.initial-backoff-ms:100}")
    private long labelFetchInitialBackoffMs;

    @Value("${app.form-label-fetch.jitter-ms:50}")
    private long labelFetchJitterMs;
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
                                                                        Map<String, FormElementLabelResponseDto> labelCache,
                                                                        Set<String> failedElementIds) {
        JsonNode data = sessionElementData.getData();
        if (data == null || !data.isObject()) {
            return List.of();
        }

        Map<String, JsonNode> fieldValueByElementId = new LinkedHashMap<>();
        Set<String> missingElementIds = new HashSet<>();
        Iterator<Map.Entry<String, JsonNode>> fields = data.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String elementId = field.getKey();
            if (GENERATED_PDF_BASE64_FIELD.equals(elementId)) {
                continue;
            }

            fieldValueByElementId.putIfAbsent(elementId, field.getValue());
            if (!labelCache.containsKey(elementId) && !failedElementIds.contains(elementId)) {
                missingElementIds.add(elementId);
            }
        }

        if (!missingElementIds.isEmpty()) {
            Map<String, FormElementLabelResponseDto> fetchedLabels = fetchLabelCacheByElementIds(missingElementIds);
            labelCache.putAll(fetchedLabels);
            missingElementIds.stream()
                    .filter(elementId -> !fetchedLabels.containsKey(elementId))
                    .forEach(failedElementIds::add);
            if (!failedElementIds.isEmpty()) {
                logger.debug("Skipping refetch for {} elementIds with previously failed label fetch", failedElementIds.size());
            }
        }

        return fieldValueByElementId.entrySet().stream()
                .map(field -> {
                    String elementId = field.getKey();
                    JsonNode value = field.getValue();
                    FormElementLabelResponseDto labelResponse = labelCache.get(elementId);
                    String label = normalizeLabel(labelResponse == null ? null : labelResponse.getLabel());
                    String formId = labelResponse == null ? null : labelResponse.getFormId();
                    return new SessionElementDataWithLabelDto(elementId, label, value, formId);
                })
                .toList();
    }

    private boolean hasDataObject(SessionElementData sessionElementData) {
        JsonNode data = sessionElementData.getData();
        return data != null && data.isObject();
    }


    private Map<String, List<SessionElementDataWithLabelDto>> buildDataWithLabels(List<SessionElementData> dataList) {
        logger.debug("Building data with labels for {} session records", dataList == null ? 0 : dataList.size());
        List<SessionElementData> filteredData = dataList.stream()
                .filter(this::hasDataObject)
                .toList();
        logger.debug("Filtered session records with object data: {}", filteredData.size());

        Map<String, FormElementLabelResponseDto> labelCache = new LinkedHashMap<>();
        Set<String> failedElementIds = new HashSet<>();
        Map<String, List<SessionElementDataWithLabelDto>> response = filteredData.stream()
                .collect(Collectors.groupingBy(
                        SessionElementData::getWorkflowId,
                        LinkedHashMap::new,
                        Collectors.flatMapping(
                                sessionElementData -> mapToDataWithLabelDtos(sessionElementData, labelCache, failedElementIds).stream(),
                                Collectors.toList()
                        )
                ));
        int dtoCount = response.values().stream().mapToInt(List::size).sum();
        logger.debug("Built API response for {} workflows with {} total labeled data entries", response.size(), dtoCount);
        return response;
    }

    private Map<String, FormElementLabelResponseDto> fetchLabelCacheByElementIds(Set<String> elementIds) {
        if (elementIds == null || elementIds.isEmpty()) {
            return Map.of();
        }

        List<String> uniqueElementIds = new ArrayList<>(elementIds);
        Map<String, FormElementLabelResponseDto> labelCache = new LinkedHashMap<>();

        int effectiveBatchSize = Math.max(1, labelFetchBatchSize);
        logger.debug("Fetching labels for {} unique elementIds using batchSize={}", uniqueElementIds.size(), effectiveBatchSize);
        for (int startIndex = 0; startIndex < uniqueElementIds.size(); startIndex += effectiveBatchSize) {
            int endIndex = Math.min(startIndex + effectiveBatchSize, uniqueElementIds.size());
            List<String> elementIdBatch = uniqueElementIds.subList(startIndex, endIndex);

            List<FormElementLabelResponseDto> labels = fetchBatchWithRetry(elementIdBatch);
            logger.debug("Fetched {} labels from forms service for batch range [{}-{})", labels == null ? 0 : labels.size(), startIndex, endIndex);
            if (labels == null || labels.isEmpty()) {
                continue;
            }

            labels.stream()
                    .filter(label -> label.getElementId() != null)
                    .forEach(label -> labelCache.putIfAbsent(label.getElementId(), label));
        }
        logger.debug("Populated label cache with {} labels after feign fetch", labelCache.size());
        return labelCache;
    }

    private List<FormElementLabelResponseDto> fetchBatchWithRetry(List<String> elementIdBatch) {
        int attempts = Math.max(1, labelFetchMaxRetries);
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                logger.debug("Fetching labels by feign for batchSize={} attempt={}", elementIdBatch.size(), attempt);
                List<FormElementLabelResponseDto> response = formFeignClient.fetchLabelsByElementIds(new FormElementLabelRequestDto(elementIdBatch));
                logger.debug("Feign returned {} labels for batchSize={} attempt={}", response == null ? 0 : response.size(), elementIdBatch.size(), attempt);
                return response;
            } catch (Exception exception) {
                if (attempt == attempts) {
                    logger.warn("Failed to fetch labels for batch after {} attempts. batchSize={}", attempts, elementIdBatch.size(), exception);
                    throw new IllegalStateException("Unable to fetch labels from forms service", exception);
                }

                long sleepMs = computeBackoffWithJitter(attempt);
                logger.debug("Retrying forms label fetch attempt={} sleepMs={} batchSize={}", attempt, sleepMs, elementIdBatch.size());
                sleepQuietly(sleepMs);
            }
        }
        return List.of();
    }

    private long computeBackoffWithJitter(int attempt) {
        long baseDelay = labelFetchInitialBackoffMs * (1L << Math.max(0, attempt - 1));
        long jitter = labelFetchJitterMs > 0 ? ThreadLocalRandom.current().nextLong(labelFetchJitterMs + 1) : 0L;
        return baseDelay + jitter;
    }

    private void sleepQuietly(long sleepMs) {
        if (sleepMs <= 0) {
            return;
        }

        try {
            Thread.sleep(sleepMs);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    private String normalizeLabel(String rawLabelResponse) {
        if (rawLabelResponse == null || rawLabelResponse.isBlank()) {
            return "none";
        }

        return rawLabelResponse;
    }

}
