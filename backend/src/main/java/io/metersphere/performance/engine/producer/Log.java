package io.metersphere.performance.engine.producer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Log {
    private String reportId;
    private String resourceId;
    private int resourceIndex;
    private String content;
}
