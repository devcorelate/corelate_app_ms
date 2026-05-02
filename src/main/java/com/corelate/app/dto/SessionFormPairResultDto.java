package com.corelate.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SessionFormPairResultDto {
    private int createdCount;
    private int updatedCount;
    private int skippedCount;
}
