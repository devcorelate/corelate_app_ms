package com.corelate.app.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class FormData {

	@Id
	private Long dataId;

	private String templateId;
	private String formId;
	private Instant timestamp;

	@OneToMany(mappedBy = "formData", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<FormDataEntry> dataEntries;
}
