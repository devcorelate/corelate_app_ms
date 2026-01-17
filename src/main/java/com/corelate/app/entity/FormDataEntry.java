package com.corelate.app.entity;

import com.corelate.app.entity.FormData;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class FormDataEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long entryId;

	private String fieldKey;
	private String fieldValue;

	@ManyToOne
	@JoinColumn(name = "form_data_id", nullable = false)
	private FormData formData;
}
