package com.corelate.app.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionElementDataWithLabelDto {

    private String id;
    private String label;
    private JsonNode value;
}
