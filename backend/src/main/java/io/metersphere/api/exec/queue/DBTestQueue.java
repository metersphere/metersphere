package io.metersphere.api.exec.queue;

import io.metersphere.base.domain.ApiExecutionQueue;
import io.metersphere.base.domain.ApiExecutionQueueDetail;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class DBTestQueue extends ApiExecutionQueue {
    private String completedReportId;
    private ApiExecutionQueueDetail queue;
    private Map<String, String> detailMap = new HashMap<>();
}
