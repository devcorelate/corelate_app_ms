package com.corelate.app.service;

import com.corelate.app.dto.SessionElementDataWithLabelDto;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

public interface ISessionElementDataService {

    List<JsonNode> fetchAllData();

    Map<String, List<SessionElementDataWithLabelDto>> fetchAllDataWithLabel();

    Map<String, List<SessionElementDataWithLabelDto>> fetchAllDataWithLabelByWorkflowId(String workflowId);

    List<JsonNode> fetchAllDataByWorkflowId(String workflowId);

    List<JsonNode> updateDataByWorkflowId(String workflowId, Map<String, JsonNode> updates);
}
