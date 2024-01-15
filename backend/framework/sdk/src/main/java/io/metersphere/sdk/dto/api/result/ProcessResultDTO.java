package io.metersphere.sdk.dto.api.result;

import lombok.Data;

@Data
public class ProcessResultDTO {
    // 报告状态
    private String status;
    // 总数
    private long total;
    // 成功总数
    private long successCount;
    // 误报总数
    private long fakeErrorCount;
    // 失败总数
    private long errorCount;
    // 未执行总数
    private long pendingCount;
    // 断言总数
    private long assertionCount;
    // 成功断言数总数
    private long successAssertionCount;
    // 请求执行率
    private String requestExecutionRate;
    // 请求通过率
    private String requestPassRate;
    // 断言通过率
    private String assertionPassRate;

    public void computerTotal() {
        this.total = (this.successCount + this.errorCount + this.pendingCount);
    }

    public void computerFakeError(long fakeErrorCount) {
        this.fakeErrorCount += fakeErrorCount;
    }

    public void computerSuccess(long success) {
        this.successCount += success;
    }

    public void computerError(long error) {
        this.errorCount += error;
    }

    public void computerPending(long pending) {
        this.pendingCount += pending;
    }

    public void computerAssertion(long assertion) {
        this.assertionCount += assertion;
    }

    public void computerSuccessAssertion(long successAssertion) {
        this.successAssertionCount += successAssertion;
    }

    public void computerRequestExecutionRate() {
        this.computerTotal();
        double executionRate = (double) this.total / (this.successCount + this.errorCount);
        this.requestExecutionRate = String.format("%.2f", executionRate);
    }

    public void computerRequestPassRate() {
        this.computerTotal();
        this.requestPassRate = String.format("%.2f", (double) this.total / (this.successCount));
    }

    public void computerAssertionPassRate() {
        this.assertionPassRate = String.format("%.2f", (double) this.assertionCount / successAssertionCount);
    }

}



