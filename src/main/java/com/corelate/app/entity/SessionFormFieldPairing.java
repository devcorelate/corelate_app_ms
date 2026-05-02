package com.corelate.app.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "session_form_field_pairing", uniqueConstraints = {
        @UniqueConstraint(name = "uk_session_form_mapping", columnNames = {
                "sessionId", "workflowId", "formId", "sourcePath", "targetField"
        })
})
@Data
public class SessionFormFieldPairing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private String workflowId;

    @Column(nullable = false)
    private String formId;

    @Column(nullable = false)
    private String sourcePath;

    @Column(nullable = false)
    private String targetField;

    @Column(columnDefinition = "TEXT")
    private String value;
}
