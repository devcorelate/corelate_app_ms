package com.corelate.app.dto;

import lombok.Data;

@Data
public class MockAppDto extends BaseDto {

    private String appId;

    private String name;

    private String description;

    private String role;

    private String audience;

    private String applicationType;

    private String formId;

    private String pageMessage;

    private String workflowId;
}
