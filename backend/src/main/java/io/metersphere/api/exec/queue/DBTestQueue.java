package io.metersphere.api.exec.queue;

import io.metersphere.base.domain.ApiExecutionQueue;
import io.metersphere.base.domain.ApiExecutionQueueDetail;
import lombok.Data;

@Data
public class DBTestQueue extends ApiExecutionQueue {
    private ApiExecutionQueueDetail queue;
}
