package io.metersphere.performance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ZipDTO {
    private String testId;
    private byte[] content;
}
