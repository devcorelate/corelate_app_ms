package com.corelate.app.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PublishLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long publishId;

	private String applicationId;

	@JsonProperty("isPublish")
	private boolean isPublish;

	private LocalDateTime publishAt;

	private List<Long> users; // assuming numeric IDs

	private String visibility;

	private String publishBy;

}
