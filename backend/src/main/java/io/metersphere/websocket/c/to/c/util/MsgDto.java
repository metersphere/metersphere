package io.metersphere.websocket.c.to.c.util;

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
public class MsgDto {
    private String reportId;
    private String toReport;
    private boolean execEnd;
    private String content;
}
