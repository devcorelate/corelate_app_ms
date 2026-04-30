package com.corelate.app.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class FormBindingBatchRequestDto {

    @NotEmpty(message = "appIds must not be empty")
    private List<@NotEmpty(message = "appId must not be empty") String> appIds;
}
