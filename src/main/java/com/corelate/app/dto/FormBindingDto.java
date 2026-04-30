package com.corelate.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FormBindingDto {

    private String appId;

    private String formId;
}
