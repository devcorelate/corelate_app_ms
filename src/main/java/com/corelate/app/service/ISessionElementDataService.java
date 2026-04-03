package com.corelate.app.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

public interface ISessionElementDataService {

    List<JsonNode> fetchAllData();

    List<JsonNode> fetchAllDataByWorkflowId(String workflowId);

    List<JsonNode> updateDataByWorkflowId(String workflowId, Map<String, JsonNode> updates);
}
