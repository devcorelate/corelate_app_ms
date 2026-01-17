package com.corelate.app.dto;

import lombok.Data;

import java.util.List;

@Data
public class FormUpdateRequestDto {
	private String templateId;
	private String formId;
	private List<UpdatedDataDto> updatedData;
}

