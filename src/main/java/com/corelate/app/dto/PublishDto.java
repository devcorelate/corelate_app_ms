package com.corelate.app.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PublishDto {

	private String applicationId;

	@JsonProperty("isPublish")
	private boolean isPublish;

	private LocalDateTime publishAt;

	private List<Long> users; // assuming numeric IDs

	private String visibility;

	private String publishBy;
}
