package com.corelate.app.dto;

import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class UpdatedDataDto {

	private long id;

	private Instant timestamp;

	private Map<String, String> data;

}
