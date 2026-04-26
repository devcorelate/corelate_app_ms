package com.corelate.app.service.impl;

import com.corelate.app.entity.SessionElementData;
import com.corelate.app.exeption.ResourceNotFoundException;
import com.corelate.app.repository.SessionElementDataRepository;
import com.corelate.app.service.client.FormFeignClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionElementDataServiceImplTest {

    @Mock
    private SessionElementDataRepository sessionElementDataRepository;

    @Mock
    private FormFeignClient formFeignClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private SessionElementDataServiceImpl sessionElementDataService;

    @Test
    void updateDataByWorkflowId_shouldAddCreatedByEmail_whenFieldDoesNotExist() {
        SessionElementData entity = new SessionElementData();
        entity.setWorkflowId("wf-1");
        entity.setData(objectMapper.createObjectNode().put("name", "Seth"));

        when(sessionElementDataRepository.findByWorkflowId("wf-1")).thenReturn(List.of(entity));
        when(sessionElementDataRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, JsonNode> updates = Map.of("createdByEmail", objectMapper.getNodeFactory().textNode("seth@gmail.com"));

        List<JsonNode> result = sessionElementDataService.updateDataByWorkflowId("wf-1", updates);

        assertEquals("seth@gmail.com", result.get(0).get("createdByEmail").asText());

        ArgumentCaptor<List<SessionElementData>> captor = ArgumentCaptor.forClass(List.class);
        verify(sessionElementDataRepository).saveAll(captor.capture());
        ObjectNode savedData = (ObjectNode) captor.getValue().get(0).getData();
        assertEquals("seth@gmail.com", savedData.get("createdByEmail").asText());
    }

    @Test
    void updateDataByWorkflowId_shouldHandleAnyEmailValue() {
        SessionElementData entity = new SessionElementData();
        entity.setWorkflowId("wf-2");
        entity.setData(objectMapper.createObjectNode());

        when(sessionElementDataRepository.findByWorkflowId("wf-2")).thenReturn(List.of(entity));
        when(sessionElementDataRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, JsonNode> updates = Map.of(
                "createdByEmail", objectMapper.getNodeFactory().textNode("  USER+tag@Example.CoM  "),
                "updatedByEmail", objectMapper.getNodeFactory().textNode("\nsecond.user@foo.org\t")
        );

        List<JsonNode> result = sessionElementDataService.updateDataByWorkflowId("wf-2", updates);

        assertEquals("USER+tag@Example.CoM", result.get(0).get("createdByEmail").asText());
        assertEquals("second.user@foo.org", result.get(0).get("updatedByEmail").asText());
    }

    @Test
    void updateDataByWorkflowId_shouldThrow_whenWorkflowDoesNotExist() {
        when(sessionElementDataRepository.findByWorkflowId("missing")).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class,
                () -> sessionElementDataService.updateDataByWorkflowId("missing", Map.of("createdByEmail", objectMapper.getNodeFactory().textNode("seth@gmail.com"))));
    }
}
