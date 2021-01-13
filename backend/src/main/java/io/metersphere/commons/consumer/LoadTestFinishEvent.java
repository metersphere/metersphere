package io.metersphere.commons.consumer;

import io.metersphere.base.domain.LoadTestReport;

public interface LoadTestFinishEvent {
    void execute(LoadTestReport loadTestReport);
}
