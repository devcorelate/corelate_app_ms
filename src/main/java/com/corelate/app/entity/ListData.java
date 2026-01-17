package com.corelate.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @ToString @AllArgsConstructor @NoArgsConstructor
public class ListData extends BaseEntity{

    @Id
    private String listId;

    private String listName;

    private String formId;

    private String schemaId;

    @Column(name = "communication_sw")
    private Boolean communicationSw;

}
