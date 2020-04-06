package io.metersphere.dto;

import io.metersphere.base.domain.TestCase;

public class TestPlanCaseDTO extends TestCase {

    private String executor;
    private String status;

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
