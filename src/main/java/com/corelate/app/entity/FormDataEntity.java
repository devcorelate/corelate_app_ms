package com.corelate.app.entity;

import com.fasterxml.jackson.databind.ser.Serializers;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Map;

@Entity
@Table(name = "form_data_entity")
@Data
public class FormDataEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String templateId;

	private String formId;

	@ElementCollection
	@CollectionTable(name = "form_data_fields", joinColumns = @JoinColumn(name = "form_data_id"))
	@MapKeyColumn(name = "field_key")
	@Column(name = "field_value")
	private Map<String, String> formData;
}
