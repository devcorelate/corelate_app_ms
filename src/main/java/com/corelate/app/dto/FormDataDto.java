package com.corelate.app.dto;

import lombok.Data;

import java.util.Map;

@Data
public class FormDataDto extends BaseDto {

	private String templateId;

	private String formId;

	private Map<String, String> formData;

}
