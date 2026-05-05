package com.corelate.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormElementLabelResponseDto {

    private String elementId;
    private String label;
    private String formId;
}
