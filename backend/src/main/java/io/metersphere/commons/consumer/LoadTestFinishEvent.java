package io.metersphere.commons.consumer;

import io.metersphere.base.domain.LoadTestReport;
import org.springframework.scheduling.annotation.Async;

public interface LoadTestFinishEvent {
    @Async
    void execute(LoadTestReport loadTestReport);
}
