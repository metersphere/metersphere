package io.metersphere.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MsgDTO {
    private String reportId;
    private String toReport;
    private boolean execEnd;
    private String content;
}
