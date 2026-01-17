package com.corelate.app.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseDto {

	private String createdBy;

	private String createdByEmail;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private String updatedBy;

}
